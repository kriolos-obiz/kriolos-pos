package io.github.kriolos.opos.automation;

import com.openbravo.pos.forms.AppConfig;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.JRootFrame;
import com.openbravo.pos.forms.StartPOS;
import com.openbravo.pos.instance.InstanceManager;
import io.github.kriolos.opos.automation.actions.InstanceManagerAction;
import org.assertj.swing.core.BasicRobot;
import org.assertj.swing.core.Robot;
import org.assertj.swing.fixture.DialogFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.swing.SwingUtilities;
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Automated Integration Test for InstanceManager and Singleton Duplicate Instance handling.
 */
public class PosInstanceManagerIT {

    private static final Logger LOGGER = Logger.getLogger(PosInstanceManagerIT.class.getName());

    private Robot robot;
    private ScreenshotHelper screenshotHelper;
    private InstanceManagerAction instanceManagerAction;

    @BeforeEach
    public void setUp() {
        // Prevent System.exit(0) during tests
        StartPOS.setExitAction(() -> {});
        robot = BasicRobot.robotWithCurrentAwtHierarchy();
        screenshotHelper = new ScreenshotHelper("pos_instance_manager_run");
        instanceManagerAction = new InstanceManagerAction(robot, screenshotHelper, true);
    }

    @AfterEach
    public void tearDown() {
        if (robot != null) {
            robot.cleanUp();
        }
        // Clean up test system properties
        System.clearProperty("kriolos.config");
        System.clearProperty("machine.uniqueinstance");
    }

    private static void sanitizeFrame(JRootFrame frame) {
        if (frame != null) {
            for (java.awt.event.WindowListener wl : frame.getWindowListeners()) {
                frame.removeWindowListener(wl);
            }
            frame.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        }
    }

    @Test
    @DisplayName("Verify AppConfig resolves configuration from System variable before default file")
    public void testAppConfigSystemVariableResolution() throws Exception {
        LOGGER.log(Level.INFO, "Testing AppConfig resolution via system properties...");

        // 1. Create a temporary properties file
        File tempConfig = File.createTempFile("kriolos-test-config", ".properties");
        tempConfig.deleteOnExit();
        try (FileWriter writer = new FileWriter(tempConfig)) {
            writer.write("machine.hostname=test-machine\n");
            writer.write("machine.uniqueinstance=false\n");
        }

        // 2. Set system property pointing to the custom config file
        System.setProperty("kriolos.config", tempConfig.getAbsolutePath());

        // Reset singleton AppConfig for test verification
        resetAppConfigSingleton();

        AppConfig config = AppConfig.getInstance();
        assertThat(config.getConfigFile().getAbsolutePath())
                .isEqualTo(tempConfig.getAbsolutePath());

        // 3. Test individual system property override
        System.setProperty("machine.uniqueinstance", "false");
        assertThat(config.getProperty("machine.uniqueinstance")).isEqualTo("false");

        System.setProperty("machine.uniqueinstance", "true");
        assertThat(config.getProperty("machine.uniqueinstance")).isEqualTo("true");

        LOGGER.log(Level.INFO, "AppConfig system property override verified successfully.");
    }

    @Test
    @DisplayName("Verify Singleton Bypass when machine.uniqueinstance is set to false")
    public void testSingletonBypassWhenUniqueInstanceFalse() throws Exception {
        LOGGER.log(Level.INFO, "Testing Singleton check bypass with machine.uniqueinstance=false...");

        System.setProperty("machine.uniqueinstance", "false");
        resetAppConfigSingleton();

        AppConfig config = AppConfig.getInstance();
        config.load();

        AtomicBoolean exitInvoked = new AtomicBoolean(false);
        setStartPosExitAction(() -> exitInvoked.set(true));

        JRootFrame dummyFrame = new JRootFrame(config);
        sanitizeFrame(dummyFrame);

        // When machine.uniqueinstance is false, checkSingletonInstance must return true immediately
        boolean allowed = invokeCheckSingletonInstance(dummyFrame, config);

        assertThat(allowed).isTrue();
        assertThat(exitInvoked.get()).isFalse();
        assertThat(instanceManagerAction.isDuplicateInstanceDialogPresent(500)).isFalse();

        LOGGER.log(Level.INFO, "Multi-instance execution permitted cleanly without singleton blocking.");
    }

    @Test
    @DisplayName("Verify Duplicate Instance Action: Warning Dialog detection, screenshot, and dismissal")
    public void testDuplicateInstanceDetectionAndAction() throws Exception {
        LOGGER.log(Level.INFO, "Testing duplicate instance detection and automation action...");

        // Check if an RMI instance is already running (e.g. port 3005)
        System.setProperty("machine.uniqueinstance", "true");
        resetAppConfigSingleton();

        AppConfig config = AppConfig.getInstance();
        config.load();

        boolean rmiActive = instanceManagerAction.isRmiInstanceResponsive(config);
        LOGGER.log(Level.INFO, "Is background RMI instance active on port: {0}", rmiActive);

        if (!rmiActive) {
            LOGGER.log(Level.INFO, "No existing instance on port 3005. Registering test instance first...");
            JRootFrame rootFrame = new JRootFrame(config);
            sanitizeFrame(rootFrame);
            try {
                InstanceManager manager = new InstanceManager(rootFrame, config);
                manager.registerInstance();
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Could not register test instance: " + e.getMessage());
            }
        }

        // Now attempt to run duplicate instance check
        AtomicBoolean exitInvoked = new AtomicBoolean(false);
        setStartPosExitAction(() -> exitInvoked.set(true));

        JRootFrame secondFrame = new JRootFrame(config);
        sanitizeFrame(secondFrame);

        // Run check on background thread so modal dialog does not block test thread
        Thread checkThread = new Thread(() -> {
            invokeCheckSingletonInstance(secondFrame, config);
        }, "SingletonCheckThread");
        checkThread.setDaemon(true);
        checkThread.start();

        // Assert that the duplicate instance dialog appears
        DialogFixture dialog = instanceManagerAction.assertDuplicateInstanceDialog(8000);
        assertThat(dialog).isNotNull();
        assertThat(dialog.target().isShowing()).isTrue();

        // Dismiss the dialog autonomously using the action driver
        instanceManagerAction.dismissDuplicateInstanceDialog();

        // Wait for thread to finish and verify exit action was triggered
        checkThread.join(4000);
        assertThat(exitInvoked.get()).isTrue();

        LOGGER.log(Level.INFO, "Duplicate instance action test completed successfully.");
    }

    private boolean invokeCheckSingletonInstance(JRootFrame frame, AppConfig config) {
        try {
            java.lang.reflect.Method m = StartPOS.class.getDeclaredMethod("checkSingletonInstance", JRootFrame.class, AppConfig.class);
            m.setAccessible(true);
            return (boolean) m.invoke(null, frame, config);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setStartPosExitAction(Runnable action) {
        StartPOS.setExitAction(action);
    }

    private void resetAppConfigSingleton() {
        try {
            Field field = AppConfig.class.getDeclaredField("INSTANCE");
            field.setAccessible(true);
            field.set(null, null);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Could not reset AppConfig INSTANCE: " + e.getMessage());
        }
    }
}
