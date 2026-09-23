package io.github.kriolos.opos.automation.actions;

import com.openbravo.pos.forms.AppConfig;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.AppProperties;
import com.openbravo.pos.instance.AppMessage;
import com.openbravo.pos.instance.InstanceManager;
import io.github.kriolos.opos.automation.ScreenshotHelper;
import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.core.Robot;
import org.assertj.swing.finder.WindowFinder;
import org.assertj.swing.fixture.DialogFixture;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import java.awt.Component;
import java.awt.Container;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Reusable Action Driver for testing and interacting with InstanceManager
 * and singleton duplicate instance notifications.
 */
public class InstanceManagerAction {

    private static final Logger LOGGER = Logger.getLogger(InstanceManagerAction.class.getName());
    public static final String EXPECTED_WARNING_MESSAGE = "Another instance of the application is already running.";

    private final Robot robot;
    private final ScreenshotHelper screenshotHelper;
    private final boolean isHumanPacing;

    public InstanceManagerAction(Robot robot, ScreenshotHelper screenshotHelper, boolean isHumanPacing) {
        this.robot = robot;
        this.screenshotHelper = screenshotHelper;
        this.isHumanPacing = isHumanPacing;
    }

    /**
     * Checks whether the duplicate instance dialog is currently visible.
     */
    public boolean isDuplicateInstanceDialogPresent(long timeoutMs) {
        try {
            findDuplicateInstanceDialog(timeoutMs);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Finds and attaches to the "Another instance of the application is already running" dialog.
     */
    public DialogFixture findDuplicateInstanceDialog(long timeoutMs) {
        LOGGER.log(Level.INFO, "Searching for Duplicate Instance dialog (timeout: {0}ms)...", timeoutMs);
        return WindowFinder.findDialog(new GenericTypeMatcher<JDialog>(JDialog.class) {
            @Override
            protected boolean isMatching(JDialog dialog) {
                if (!dialog.isShowing()) {
                    return false;
                }
                String title = dialog.getTitle();
                boolean titleMatches = (title != null && (
                        title.equalsIgnoreCase(AppLocal.APP_NAME) ||
                        title.toLowerCase().contains("kriol") ||
                        title.toLowerCase().contains("pos") ||
                        title.toLowerCase().contains("message") ||
                        title.toLowerCase().contains("aviso")
                ));
                return titleMatches || containsText(dialog, EXPECTED_WARNING_MESSAGE);
            }
        }).withTimeout(timeoutMs).using(robot);
    }

    /**
     * Asserts that the duplicate instance warning dialog is present and captures a milestone screenshot.
     */
    public DialogFixture assertDuplicateInstanceDialog(long timeoutMs) {
        DialogFixture dialog = findDuplicateInstanceDialog(timeoutMs);
        LOGGER.log(Level.INFO, "Found Duplicate Instance dialog: {0}", dialog.target().getTitle());
        if (screenshotHelper != null) {
            screenshotHelper.captureComponent(dialog.target(), "00_instance_already_running");
        }
        pace(500);
        return dialog;
    }

    /**
     * Dismisses the duplicate instance dialog autonomously by clicking the "OK" button.
     */
    public void dismissDuplicateInstanceDialog() {
        DialogFixture dialog = findDuplicateInstanceDialog(5000);
        LOGGER.log(Level.INFO, "Autonomously clicking OK button on Duplicate Instance dialog...");

        try {
            JButton okButton = dialog.button(new GenericTypeMatcher<JButton>(JButton.class) {
                @Override
                protected boolean isMatching(JButton btn) {
                    if (!btn.isShowing()) {
                        return false;
                    }
                    String txt = btn.getText();
                    return txt != null && (
                            txt.equalsIgnoreCase("OK") ||
                            txt.equalsIgnoreCase("Sim") ||
                            txt.equalsIgnoreCase("Yes")
                    );
                }
            }).target();

            LOGGER.log(Level.INFO, "Found dialog confirmation button: [{0}]. Invoking click...", okButton.getText());
            javax.swing.SwingUtilities.invokeLater(okButton::doClick);
            pace(800);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to find button by text, closing dialog directly: {0}", e.getMessage());
            javax.swing.SwingUtilities.invokeLater(() -> dialog.target().dispose());
            pace(500);
        }
        LOGGER.log(Level.INFO, "Duplicate Instance dialog dismissed autonomously.");
    }

    /**
     * If the duplicate instance dialog is present, captures a screenshot and dismisses it.
     *
     * @return true if dialog was detected and handled, false otherwise
     */
    public boolean handleIfPresent() {
        if (isDuplicateInstanceDialogPresent(2000)) {
            LOGGER.log(Level.WARNING, "Detected active duplicate instance warning dialog. Handling and dismissing...");
            if (screenshotHelper != null) {
                screenshotHelper.captureScreen("00_duplicate_instance_detected");
            }
            dismissDuplicateInstanceDialog();
            return true;
        }
        return false;
    }

    /**
     * Direct query to verify if an active instance is registered and responsive on RMI.
     */
    public boolean isRmiInstanceResponsive(AppProperties config) {
        try {
            AppMessage appMessage = InstanceManager.queryInstance(config);
            if (appMessage != null) {
                appMessage.restoreWindow();
                LOGGER.log(Level.INFO, "Successfully contacted existing instance via RMI and restored window.");
                return true;
            }
        } catch (Exception e) {
            LOGGER.log(Level.INFO, "No responsive instance found on RMI: {0}", e.getMessage());
        }
        return false;
    }

    private static boolean containsText(Container container, String targetText) {
        for (Component c : container.getComponents()) {
            if (c instanceof JLabel) {
                String text = ((JLabel) c).getText();
                if (text != null && text.contains(targetText)) {
                    return true;
                }
            } else if (c instanceof JOptionPane) {
                Object msg = ((JOptionPane) c).getMessage();
                if (msg != null && msg.toString().contains(targetText)) {
                    return true;
                }
            } else if (c instanceof Container) {
                if (containsText((Container) c, targetText)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void pace(long humanDelayMs) {
        if (isHumanPacing && humanDelayMs > 0) {
            try {
                Thread.sleep(humanDelayMs);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
