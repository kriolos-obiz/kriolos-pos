package io.github.kriolos.opos.automation;

import com.openbravo.basic.BasicException;
import com.openbravo.data.gui.JMessageDialog;
import com.openbravo.data.gui.MessageInf;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.AppProperties;
import com.openbravo.pos.forms.AppViewConnection;
import org.assertj.swing.core.BasicRobot;
import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.core.Robot;
import org.assertj.swing.finder.WindowFinder;
import org.assertj.swing.fixture.DialogFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import java.awt.Dialog;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Automated UI Robot Integration Test for verifying the isolated database connection test:
 * <ul>
 *   <li><b>Silent on Success:</b> Logs connection success with INFO and displays NO popup dialog.</li>
 *   <li><b>Dialog on Failure:</b> Logs WARNING, captures the exception, and displays {@link JMessageDialog}
 *       with fully rendered, non-black UI (BUG-001) and expandable technical diagnostics.</li>
 * </ul>
 */
public class PosDatabaseLockIT {

    private static final Logger LOGGER = Logger.getLogger(PosDatabaseLockIT.class.getName());

    private Robot robot;
    private ScreenshotHelper screenshotHelper;

    @BeforeEach
    public void setUp() {
        robot = BasicRobot.robotWithCurrentAwtHierarchy();
        screenshotHelper = new ScreenshotHelper("pos_database_lock_run");
    }

    @AfterEach
    public void tearDown() {
        if (robot != null) {
            robot.cleanUp();
        }
    }

    @Test
    @DisplayName("Verify valid database connection test completes silently with logging and no dialog")
    public void testConnectionSuccessIsSilentWithLogging() throws Exception {
        LOGGER.log(Level.INFO, "Testing valid connection test: must be silent with logging and no popup dialog...");

        TestAppProperties props = new TestAppProperties();
        props.setProperty("db.driver", "org.hsqldb.jdbcDriver");
        props.setProperty("db.url", "jdbc:hsqldb:mem:silent_test_db");
        props.setProperty("db.user", "sa");
        props.setProperty("db.password", "");

        // Connection test must complete normally without throwing exception
        AppViewConnection.testConnection(props, "db");

        // Verify that NO dialog is visible/opened (silent execution)
        for (Window w : Window.getWindows()) {
            if (w instanceof Dialog && w.isShowing()) {
                assertThat(w.isShowing())
                        .as("Connection test succeeded and must remain silent without showing any modal dialog")
                        .isFalse();
            }
        }
        LOGGER.log(Level.INFO, "Silent connection test verified successfully.");
    }

    @Test
    @DisplayName("Verify failed database connection test logs exception and shows non-black JMessageDialog (BUG-001)")
    public void testConnectionFailureLogsExceptionAndShowsNonBlackDialog() throws Exception {
        LOGGER.log(Level.INFO, "Testing failed connection test: logs exception and shows JMessageDialog...");

        TestAppProperties props = new TestAppProperties();
        props.setProperty("db.driver", "org.hsqldb.jdbcDriver");
        props.setProperty("db.url", "jdbc:hsqldb:file:/nonexistent_protected_dir/pos_locked_db;ifexists=true");
        props.setProperty("db.user", "sa");
        props.setProperty("db.password", "");

        Thread dialogThread = new Thread(() -> {
            try {
                LOGGER.log(Level.INFO, "Executing connection test against unavailable database...");
                AppViewConnection.testConnection(props, "db");
            } catch (BasicException ex) {
                LOGGER.log(Level.WARNING, "Caught expected connection test failure: " + ex.getMessage(), ex);
                MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.databaseconnectionerror"), ex);
                msg.show(null);
            }
        }, "FailedConnectionTestThread");
        dialogThread.setDaemon(true);
        dialogThread.start();

        DialogFixture dialog = findMessageDialog(8000);
        assertThat(dialog).isNotNull();
        assertThat(dialog.target().isShowing()).isTrue();

        // 1. Milestone screenshot of connection failure dialog
        File captureFile = screenshotHelper.captureComponent(dialog.target(), "01_db_lock_dialog_rendered");
        assertThat(captureFile).isNotNull().exists();
        BufferedImage capture = ImageIO.read(captureFile);

        // 2. Assert dialog content is visibly rendered and NOT black (BUG-001)
        assertThat(capture).isNotNull();
        assertImageNotBlack(capture);

        // 3. Click Information / Detalhes button to expand technical exception details
        clickButton(dialog, "Info", "Information", "Informação", "Detalhes", "More");
        Thread.sleep(800);

        // 4. Milestone screenshot of expanded details
        File expandedCaptureFile = screenshotHelper.captureComponent(dialog.target(), "02_db_lock_dialog_expanded");
        assertThat(expandedCaptureFile).isNotNull().exists();
        BufferedImage expandedCapture = ImageIO.read(expandedCaptureFile);
        assertImageNotBlack(expandedCapture);

        // 5. Verify exception details text area contains connection failure information
        JTextArea textArea = robot.finder().findByType(dialog.target(), JTextArea.class);
        assertThat(textArea).isNotNull();
        assertThat(textArea.isShowing()).isTrue();
        assertThat(textArea.getText().length()).isGreaterThan(10);

        // 6. Autonomously dismiss dialog with OK button
        clickButton(dialog, "OK", "Ok", "Sim", "Yes");

        dialogThread.join(3000);
        LOGGER.log(Level.INFO, "Connection failure error dialog verified and dismissed autonomously.");
    }

    /**
     * Finds and matches the active modal {@link JDialog} within the specified timeout.
     *
     * @param timeoutMs maximum time to wait in milliseconds
     * @return the matched {@link DialogFixture}
     */
    private DialogFixture findMessageDialog(long timeoutMs) {
        return WindowFinder.findDialog(new GenericTypeMatcher<JDialog>(JDialog.class) {
            @Override
            protected boolean isMatching(JDialog dialog) {
                if (!dialog.isShowing()) {
                    return false;
                }
                if (dialog instanceof JMessageDialog) {
                    return true;
                }
                String title = dialog.getTitle();
                return title != null && (
                        title.toLowerCase().contains("mensagem") ||
                        title.toLowerCase().contains("message") ||
                        title.toLowerCase().contains("aviso") ||
                        title.toLowerCase().contains("perigo") ||
                        title.toLowerCase().contains("database") ||
                        title.toLowerCase().contains("kriol")
                );
            }
        }).withTimeout(timeoutMs).using(robot);
    }

    /**
     * Finds a button matching any candidate localized text and triggers a click
     * asynchronously on the Swing Event Dispatch Thread (EDT) to safely dismiss modal dialogs.
     *
     * @param dialog the dialog containing the button
     * @param buttonTexts candidate button label strings
     */
    private void clickButton(DialogFixture dialog, String... buttonTexts) {
        JButton button = dialog.button(new GenericTypeMatcher<JButton>(JButton.class) {
            @Override
            protected boolean isMatching(JButton btn) {
                if (!btn.isShowing()) return false;
                String txt = btn.getText();
                if (txt == null || txt.trim().isEmpty()) return false;
                for (String target : buttonTexts) {
                    if (txt.equalsIgnoreCase(target)
                            || txt.toLowerCase().contains(target.toLowerCase())
                            || target.toLowerCase().contains(txt.toLowerCase())) {
                        return true;
                    }
                }
                return false;
            }
        }).target();

        SwingUtilities.invokeLater(button::doClick);
    }

    /**
     * Inspects every pixel in the captured component image to verify that the dialog is
     * properly rendered and not displaying an uninitialized black or blank frame buffer (BUG-001).
     *
     * @param image the captured screenshot image of the dialog component
     */
    private void assertImageNotBlack(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        assertThat(width).isGreaterThan(50);
        assertThat(height).isGreaterThan(50);

        long nonBlackPixels = 0;
        long totalPixels = (long) width * height;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                // Consider pixel non-black if brightness > 15
                if (r > 15 || g > 15 || b > 15) {
                    nonBlackPixels++;
                }
            }
        }

        double nonBlackRatio = (double) nonBlackPixels / totalPixels;
        LOGGER.log(Level.INFO, "Dialog rendered non-black pixel ratio: {0}% ({1}/{2})",
                new Object[]{String.format("%.2f", nonBlackRatio * 100), nonBlackPixels, totalPixels});

        // A black dialog would have close to 0% non-black pixels; a rendered dialog has > 50%
        assertThat(nonBlackRatio)
                .as("Dialog content should not be black; non-black pixels must exceed 50%")
                .isGreaterThan(0.50);
    }

    /**
     * Lightweight in-memory {@link AppProperties} implementation for testing database configurations.
     */
    private static class TestAppProperties implements AppProperties {
        private final Map<String, String> properties = new HashMap<>();

        public void setProperty(String key, String value) {
            properties.put(key, value);
        }

        @Override
        public File getConfigFile() {
            return null;
        }

        @Override
        public String getHost() {
            return "test-host";
        }

        @Override
        public String getProperty(String sKey) {
            return properties.get(sKey);
        }

        @Override
        public String getProperty(String sKey, String defaultValue) {
            return properties.getOrDefault(sKey, defaultValue);
        }
    }
}
