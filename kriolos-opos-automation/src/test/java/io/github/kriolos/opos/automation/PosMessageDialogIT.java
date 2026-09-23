package io.github.kriolos.opos.automation;

import com.openbravo.data.gui.JMessageDialog;
import com.openbravo.data.gui.MessageInf;
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
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Container;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Automated UI Robot Integration Test for verifying JMessageDialog rendering
 * and preventing black/blank frame buffer anomalies (BUG-001).
 */
public class PosMessageDialogIT {

    private static final Logger LOGGER = Logger.getLogger(PosMessageDialogIT.class.getName());

    private Robot robot;
    private ScreenshotHelper screenshotHelper;

    @BeforeEach
    public void setUp() {
        robot = BasicRobot.robotWithCurrentAwtHierarchy();
        screenshotHelper = new ScreenshotHelper("pos_message_dialog_run");
    }

    @AfterEach
    public void tearDown() {
        if (robot != null) {
            robot.cleanUp();
        }
    }

    @Test
    @DisplayName("Verify MessageDialog renders fully with content visible and not a black box (BUG-001)")
    public void testWarningMessageDialogRendersContentAndNotBlack() throws Exception {
        LOGGER.log(Level.INFO, "Testing JMessageDialog rendering and buffer validation...");

        String testMsg = "Database connection test message";
        Thread dialogThread = new Thread(() -> {
            JMessageDialog.showMessage(null, new MessageInf(MessageInf.SGN_WARNING, testMsg));
        }, "MessageDialogThread");
        dialogThread.setDaemon(true);
        dialogThread.start();

        DialogFixture dialog = findMessageDialog(8000);
        assertThat(dialog).isNotNull();
        assertThat(dialog.target().isShowing()).isTrue();

        // Capture milestone screenshot
        File captureFile = screenshotHelper.captureComponent(dialog.target(), "01_warning_dialog_rendered");
        assertThat(captureFile).isNotNull().exists();
        BufferedImage capture = ImageIO.read(captureFile);

        // Verify that the dialog is NOT black (contains non-black painted pixels)
        assertThat(capture).isNotNull();
        assertImageNotBlack(capture);

        // Verify message label contains expected text
        JLabel messageLabel = robot.finder().find(dialog.target(), new GenericTypeMatcher<JLabel>(JLabel.class) {
            @Override
            protected boolean isMatching(JLabel label) {
                return label.getText() != null && label.getText().contains(testMsg);
            }
        });
        assertThat(messageLabel).isNotNull();
        assertThat(messageLabel.isShowing()).isTrue();
        assertThat(messageLabel.getWidth()).isGreaterThan(0);
        assertThat(messageLabel.getHeight()).isGreaterThan(0);

        // Autonomous dismissal: click OK button
        clickButton(dialog, "OK", "Sim", "Yes");

        dialogThread.join(3000);
        LOGGER.log(Level.INFO, "Warning MessageDialog verified and dismissed autonomously.");
    }

    @Test
    @DisplayName("Verify Exception details expansion and dynamic resizing in MessageDialog")
    public void testExceptionDialogInformationExpansion() throws Exception {
        LOGGER.log(Level.INFO, "Testing JMessageDialog exception details toggle...");

        String errorMsg = "System operational anomaly";
        String rootCauseMsg = "Root cause internal failure detail";

        Thread dialogThread = new Thread(() -> {
            MessageInf inf = new MessageInf(MessageInf.SGN_DANGER, errorMsg, new RuntimeException(rootCauseMsg));
            JMessageDialog.showMessage(null, inf);
        }, "ExceptionDialogThread");
        dialogThread.setDaemon(true);
        dialogThread.start();

        DialogFixture dialog = findMessageDialog(8000);
        assertThat(dialog).isNotNull();

        // 1. Initial screenshot
        screenshotHelper.captureComponent(dialog.target(), "02_exception_dialog_initial");

        // 2. Click Information button to expand stack trace details
        clickButton(dialog, "Info", "Information", "Informação", "Detalhes", "More");
        Thread.sleep(800);

        // 3. Capture expanded screenshot
        File expandedCaptureFile = screenshotHelper.captureComponent(dialog.target(), "03_exception_dialog_expanded");
        assertThat(expandedCaptureFile).isNotNull().exists();
        BufferedImage expandedCapture = ImageIO.read(expandedCaptureFile);
        assertImageNotBlack(expandedCapture);

        // Verify stack trace text area is showing and contains root cause message
        JTextArea textArea = robot.finder().findByType(dialog.target(), JTextArea.class);
        assertThat(textArea).isNotNull();
        assertThat(textArea.isShowing()).isTrue();
        assertThat(textArea.getText()).contains(rootCauseMsg);

        // Autonomous dismissal: click OK button
        clickButton(dialog, "OK", "Sim", "Yes");

        dialogThread.join(3000);
        LOGGER.log(Level.INFO, "Exception MessageDialog expanded and dismissed autonomously.");
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
     * Finds a button matching any of the candidate localized texts and triggers a click
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
                if (txt == null) return false;
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
}
