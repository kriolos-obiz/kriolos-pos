package io.github.kriolos.opos.automation;

import com.openbravo.pos.forms.StartPOS;
import io.github.kriolos.opos.automation.actions.DatabaseSelectionAction;
import io.github.kriolos.opos.automation.actions.LoginAction;
import io.github.kriolos.opos.automation.actions.MainWindowAction;
import io.github.kriolos.opos.automation.actions.MenuNavigationAction;
import io.github.kriolos.opos.automation.actions.NavigationHistoryAction;
import io.github.kriolos.opos.automation.actions.PaymentDialogAction;
import io.github.kriolos.opos.automation.actions.SalesAction;
import org.assertj.swing.core.BasicRobot;
import org.assertj.swing.core.Robot;
import org.assertj.swing.fixture.FrameFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Abstract Base Integration Test Harness for KriolOS POS Automation.
 *
 * <p>Provides:
 * <ul>
 *   <li>Application lifecycle management (launching StartPOS on a background thread)</li>
 *   <li>AssertJ-Swing Robot initialization and cleanup</li>
 *   <li>Wayland-safe in-memory screenshot and video recording management</li>
 *   <li>Configurable execution pacing (Human observational vs Robot fast mode)</li>
 *   <li>Pre-instantiated, reusable Action Drivers:
 *     <ul>
 *       <li>{@link DatabaseSelectionAction}</li>
 *       <li>{@link MainWindowAction}</li>
 *       <li>{@link LoginAction}</li>
 *       <li>{@link MenuNavigationAction}</li>
 *       <li>{@link SalesAction}</li>
 *       <li>{@link PaymentDialogAction}</li>
 *     </ul>
 *   </li>
 * </ul>
 */
public abstract class BasePosRobotIT {

    private static final Logger LOGGER = Logger.getLogger(BasePosRobotIT.class.getName());

    protected Robot robot;
    protected ScreenshotHelper screenshotHelper;
    protected FfmpegScreenRecorder videoRecorder;

    // Reusable Action Drivers
    protected DatabaseSelectionAction dbSelection;
    protected MainWindowAction mainWindow;
    protected LoginAction login;
    protected MenuNavigationAction menuNav;
    protected SalesAction sales;
    protected NavigationHistoryAction navHistory;

    // Common Configuration
    protected static final boolean RECORD_VIDEO = Boolean.parseBoolean(System.getProperty("test.record.video", "true"));
    protected static final boolean RECORD_GIF = Boolean.parseBoolean(System.getProperty("test.record.gif", "true"));
    protected static final String TARGET_OPERATOR = System.getProperty("test.user", "admin");
    protected static final String TARGET_PASSWORD = System.getProperty("test.password", TARGET_OPERATOR);
    protected static final String TARGET_DB = System.getProperty("test.db", "");
    protected static final boolean MAXIMIZE_WINDOW = Boolean.parseBoolean(System.getProperty("test.window.maximize", "true"));

    protected static final String BEHAVIOR = System.getProperty("test.behavior",
            System.getProperty("behavior", "human")).trim().toLowerCase();
    protected static final boolean IS_HUMAN = "human".equals(BEHAVIOR);

    /**
     * Subclasses can override this to customize recording session directory names.
     */
    protected String getScenarioName() {
        return "pos_sales_run";
    }

    @BeforeEach
    public void setUpBase() throws Exception {
        screenshotHelper = new ScreenshotHelper(getScenarioName());
        robot = BasicRobot.robotWithCurrentAwtHierarchy();

        // 1. Initialize Reusable Action Drivers
        dbSelection = new DatabaseSelectionAction(robot, screenshotHelper, IS_HUMAN);
        mainWindow = new MainWindowAction(robot, screenshotHelper, IS_HUMAN);
        login = new LoginAction(robot, screenshotHelper, IS_HUMAN);
        menuNav = new MenuNavigationAction(robot, screenshotHelper, IS_HUMAN);
        sales = new SalesAction(robot, screenshotHelper, IS_HUMAN);
        navHistory = new NavigationHistoryAction(screenshotHelper, IS_HUMAN);

        // 2. Start screen recording if enabled
        if (RECORD_VIDEO) {
            File videoFile = new File("target/recordings/" + getScenarioName() + "/session.mp4");
            videoRecorder = new FfmpegScreenRecorder(videoFile);
            videoRecorder.start(System.getenv("DISPLAY"), 1920, 1080, 25);
        }

        LOGGER.log(Level.INFO, "[BasePosRobotIT] Pacing mode: {0}",
                (IS_HUMAN ? "HUMAN (observational delays enabled)" : "ROBOT (fast/no artificial delays)"));

        // 3. Launch application
        launchApplication();
    }

    /**
     * Launches StartPOS in a background thread.
     */
    protected void launchApplication() {
        LOGGER.log(Level.INFO, "[BasePosRobotIT] Launching KriolOS POS Application...");
        Thread posThread = new Thread(() -> {
            try {
                StartPOS.main(new String[]{});
            } catch (Throwable t) {
                LOGGER.log(Level.SEVERE, "[BasePosRobotIT] Exception in StartPOS: " + t.getMessage(), t);
            }
        }, "PosAppThread");
        posThread.setDaemon(true);
        posThread.start();
    }

    /**
     * Helper to boot the app, handle DB selection if present, attach to main window, and configure sizing.
     */
    protected FrameFixture bootAndAttachMainWindow() {
        dbSelection.handleIfPresent(TARGET_DB);
        FrameFixture window = mainWindow.attach(45000);
        mainWindow.configureWindowMode(MAXIMIZE_WINDOW);
        dbSelection.selectDatabaseInPanel(window.target(), TARGET_DB);
        try {
            screenshotHelper.captureComponent(window.target(), "01_app_started");
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "[BasePosRobotIT] Could not capture screenshot: {0}", e.getMessage());
        }
        return window;
    }

    /**
     * Helper to boot the app, handle DB selection, attach window, and log in operator.
     */
    protected FrameFixture bootAndLogin() {
        FrameFixture window = bootAndAttachMainWindow();
        login.login(window, TARGET_OPERATOR, TARGET_PASSWORD);
        return window;
    }

    protected void pace(long humanMs) {
        pace(humanMs, 0);
    }

    protected void pace(long humanMs, long robotMs) {
        long delay = IS_HUMAN ? humanMs : robotMs;
        if (delay > 0) {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @AfterEach
    public void tearDownBase() {
        LOGGER.log(Level.INFO, "[BasePosRobotIT] Cleaning up test resources...");
        if (mainWindow != null) {
            mainWindow.cleanUp();
        }
        if (videoRecorder != null) {
            videoRecorder.stop();
            if (RECORD_GIF) {
                File videoFile = videoRecorder.getOutputFile();
                if (videoFile != null && videoFile.exists()) {
                    File gifFile = new File(videoFile.getParentFile(), "session.gif");
                    FfmpegScreenGifGenerator.generateGif(videoFile, gifFile);
                }
            }
        }
    }
}
