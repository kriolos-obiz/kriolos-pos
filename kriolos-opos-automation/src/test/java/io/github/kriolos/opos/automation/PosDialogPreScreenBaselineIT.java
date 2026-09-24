package io.github.kriolos.opos.automation;

import com.openbravo.basic.BasicException;
import com.openbravo.beans.DinerNumber;
import com.openbravo.beans.JCalendarDialog;
import com.openbravo.beans.JDoubleDialog;
import com.openbravo.beans.JEditorTextDialog;
import com.openbravo.beans.JIntegerDialog;
import com.openbravo.beans.JPasswordDialog;
import com.openbravo.data.gui.FindInfo;
import com.openbravo.data.gui.JFind;
import com.openbravo.data.gui.JMessageDialog;
import com.openbravo.data.gui.JSort;
import com.openbravo.data.gui.MessageInf;
import com.openbravo.data.loader.ComparatorCreator;
import com.openbravo.data.loader.Vectorer;
import org.assertj.swing.core.BasicRobot;
import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.core.Robot;
import org.assertj.swing.finder.WindowFinder;
import org.assertj.swing.fixture.DialogFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.Component;
import java.awt.Container;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pre-Verification & Acceptance Test Suite: Pre-Screen Baseline.
 *
 * <p>Systematically opens every representative modal dialog across the application,
 * verifies proper non-black rendering (>50% non-black pixels), captures a baseline screenshot
 * ("pre-screen"), and dismisses each dialog 100% autonomously.
 *
 * <p>Serves as the golden baseline to visually and functionally compare before and after
 * the {@code PosUIModal} panel-first architectural migration.
 */
public class PosDialogPreScreenBaselineIT {

    private static final Logger LOGGER = Logger.getLogger(PosDialogPreScreenBaselineIT.class.getName());
    private static final String BASELINE_DIR = "target/recordings/dialog_baseline_prescreen/screenshots";
    private static final String ARTIFACTS_BASELINE_DIR = "/home/dev/.gemini/antigravity-ide/brain/0bd13745-dad9-4c13-8632-c74bdebc5ecb/screenshots/baseline";

    private Robot robot;
    private ScreenshotHelper screenshotHelper;
    private javax.swing.JFrame dummyParent;

    @BeforeAll
    public static void initEnvironment() {
        // Attempt to initialize FlatDarkLaf for authentic POS styling
        try {
            UIManager.setLookAndFeel("com.formdev.flatlaf.FlatDarkLaf");
            LOGGER.log(Level.INFO, "FlatDarkLaf initialized successfully for pre-screen tests.");
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "FlatDarkLaf not applied, using default Swing Look and Feel: " + e.getMessage());
        }
    }

    @BeforeEach
    public void setUp() {
        robot = BasicRobot.robotWithCurrentAwtHierarchy();
        screenshotHelper = new ScreenshotHelper("dialog_baseline_prescreen");
        dummyParent = new javax.swing.JFrame("KriolOS POS Base Frame");
        dummyParent.setSize(400, 300);
    }

    @AfterEach
    public void tearDown() {
        if (dummyParent != null) {
            dummyParent.dispose();
        }
        if (robot != null) {
            robot.cleanUp();
        }
    }

    @Test
    @DisplayName("Pre-Screen MODAL-001a: JMessageDialog Warning Message")
    public void testPreScreen_MODAL001a_MessageDialogWarning() throws Exception {
        captureAndDismiss("01a_message_dialog_warning_DIALOG", () -> {
            JMessageDialog.showMessage(dummyParent, new MessageInf(MessageInf.SGN_WARNING, "Database lock held by another instance."));
        });
    }

    @Test
    @DisplayName("Pre-Screen MODAL-001b: JMessageDialog Exception with Stack Trace")
    public void testPreScreen_MODAL001b_MessageDialogException() throws Exception {
        captureAndDismissWithExpand("01b_message_dialog_exception_DIALOG", () -> {
            Exception rootCause = new java.sql.SQLException("Connection refused: port 9001 blocked by external process");
            BasicException basicEx = new BasicException("Database Connection Failed", rootCause);
            JMessageDialog.showMessage(dummyParent, new MessageInf(MessageInf.SGN_DANGER, "Fatal Database Initialization Error", basicEx));
        });
    }

    @Test
    @DisplayName("Modal Migration 01a: JMessagePanel Warning Message via PosUIModal")
    public void testModal_001a_MessagePanelWarning() throws Exception {
        captureAndDismiss("01a_message_dialog_warning_MODAL_FIXED", () -> {
            com.openbravo.data.gui.JMessagePanel.showMessage(dummyParent, new MessageInf(MessageInf.SGN_WARNING, "Database lock held by another instance."));
        });
    }

    @Test
    @DisplayName("Modal Migration 01b: JMessagePanel Exception with Stack Trace via PosUIModal")
    public void testModal_001b_MessagePanelException() throws Exception {
        captureAndDismissWithExpand("01b_message_dialog_exception_MODAL_FIXED", () -> {
            Exception rootCause = new java.sql.SQLException("Connection refused: port 9001 blocked by external process");
            BasicException basicEx = new BasicException("Database Connection Failed", rootCause);
            com.openbravo.data.gui.JMessagePanel.showMessage(dummyParent, new MessageInf(MessageInf.SGN_DANGER, "Fatal Database Initialization Error", basicEx));
        });
    }

    @Test
    @DisplayName("Pre-Screen MODAL-002: JEditorTextDialog On-Screen Keypad")
    public void testPreScreen_MODAL002_EditorTextDialog() throws Exception {
        captureAndDismiss("02_editor_text_dialog_DIALOG", () -> {
            JEditorTextDialog.showEditor(dummyParent, "Input Customer Name", "Please enter the customer name:");
        });
    }

    @Test
    @DisplayName("Modal Migration 02: JEditorTextPanel via PosUIModal")
    public void testModal_002_EditorTextPanel() throws Exception {
        captureAndDismiss("02_editor_text_dialog_MODAL_FIXED", () -> {
            com.openbravo.beans.JEditorTextPanel.show(dummyParent, "Input Customer Name", "Please enter the customer name:");
        });
    }

    @Test
    @DisplayName("Pre-Screen MODAL-003: JCalendarDialog Date Picker")
    public void testPreScreen_MODAL003_CalendarDialogDate() throws Exception {
        captureAndDismiss("03_calendar_dialog_date_DIALOG", () -> {
            JCalendarDialog.showCalendar(dummyParent, new Date());
        });
    }

    @Test
    @DisplayName("Pre-Screen MODAL-004: JCalendarDialog Date and Time Picker")
    public void testPreScreen_MODAL004_CalendarDialogTime() throws Exception {
        captureAndDismiss("04_calendar_dialog_time_DIALOG", () -> {
            JCalendarDialog.showCalendarTime(dummyParent, new Date());
        });
    }

    @Test
    @DisplayName("Modal Migration 03: JCalendarDlgPanel Date Picker via PosUIModal")
    public void testModal_003_CalendarPanelDate() throws Exception {
        captureAndDismiss("03_calendar_dialog_date_MODAL_FIXED", () -> {
            com.openbravo.beans.JCalendarDlgPanel.showCalendar(dummyParent, new Date());
        });
    }

    @Test
    @DisplayName("Modal Migration 04: JCalendarDlgPanel Date and Time Picker via PosUIModal")
    public void testModal_004_CalendarPanelTime() throws Exception {
        captureAndDismiss("04_calendar_dialog_time_MODAL_FIXED", () -> {
            com.openbravo.beans.JCalendarDlgPanel.showCalendarTime(dummyParent, new Date());
        });
    }

    @Test
    @DisplayName("Pre-Screen MODAL-005: JIntegerDialog Number Keypad")
    public void testPreScreen_MODAL005_IntegerDialog() throws Exception {
        captureAndDismiss("05_integer_dialog_DIALOG", () -> {
            JIntegerDialog.showComponent(dummyParent, "Input Quantity", "Enter number of units:");
        });
    }

    @Test
    @DisplayName("Modal Migration 05: JIntegerPanel via PosUIModal")
    public void testModal_005_IntegerPanel() throws Exception {
        captureAndDismiss("05_integer_dialog_MODAL_FIXED", () -> {
            com.openbravo.beans.JIntegerPanel.show(dummyParent, "Input Quantity", "Enter number of units:");
        });
    }

    @Test
    @DisplayName("Pre-Screen MODAL-006: JDoubleDialog Price/Currency Keypad")
    public void testPreScreen_MODAL006_DoubleDialog() throws Exception {
        captureAndDismiss("06_double_dialog_DIALOG", () -> {
            JDoubleDialog.showComponent(dummyParent, "Input Custom Price", "Enter unit price:");
        });
    }

    @Test
    @DisplayName("Modal Migration 06: JDoublePanel via PosUIModal")
    public void testModal_006_DoublePanel() throws Exception {
        captureAndDismiss("06_double_dialog_MODAL_FIXED", () -> {
            com.openbravo.beans.JDoublePanel.show(dummyParent, "Input Custom Price", "Enter unit price:");
        });
    }

    @Test
    @DisplayName("Pre-Screen MODAL-007: DinerNumber Guests Keypad")
    public void testPreScreen_MODAL007_DinerNumber() throws Exception {
        captureAndDismiss("07_diner_number_DIALOG", () -> {
            DinerNumber.showEditNumber(dummyParent, "Select Guest Count", "Number of guests:", null);
        });
    }

    @Test
    @DisplayName("Modal Migration 07: DinerNumberPanel via PosUIModal")
    public void testModal_007_DinerNumberPanel() throws Exception {
        captureAndDismiss("07_diner_number_MODAL_FIXED", () -> {
            com.openbravo.beans.DinerNumberPanel.show(dummyParent, "Select Guest Count", "Number of guests:", null);
        });
    }

    @Test
    @DisplayName("Pre-Screen MODAL-008: JPasswordDialog Secret Keypad")
    public void testPreScreen_MODAL008_PasswordDialog() throws Exception {
        captureAndDismiss("08_password_dialog_DIALOG", () -> {
            JPasswordDialog.showEditor(dummyParent, "Enter Manager PIN", "PIN Required for Override:");
        });
    }

    @Test
    @DisplayName("Modal Migration 08: JPasswordPanel via PosUIModal")
    public void testModal_008_PasswordPanel() throws Exception {
        captureAndDismiss("08_password_dialog_MODAL_FIXED", () -> {
            com.openbravo.beans.JPasswordPanel.show(dummyParent, "Enter Manager PIN", "PIN Required for Override:");
        });
    }

    @Test
    @DisplayName("Pre-Screen MODAL-009: JFind Search and Query Dialog")
    public void testPreScreen_MODAL009_FindDialog() throws Exception {
        Vectorer<Object> vectorer = new Vectorer<>() {
            @Override
            public String[] getHeaders() {
                return new String[]{"Product Code", "Product Name", "Category", "Price"};
            }

            @Override
            public String[] getValues(Object obj) {
                return new String[]{"P001", "Coffee", "Beverages", "2.50"};
            }
        };
        FindInfo<Object> findInfo = new FindInfo<>(vectorer, "Coffee", 0, false, FindInfo.MATCH_STARTFIELD);

        captureAndDismiss("09_find_dialog_DIALOG", () -> {
            try {
                JFind.showMessage(null, findInfo);
            } catch (BasicException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    @DisplayName("Pre-Screen MODAL-010: JSort Column Ordering Dialog")
    public void testPreScreen_MODAL010_SortDialog() throws Exception {
        ComparatorCreator<Object> cc = new ComparatorCreator<>() {
            @Override
            public String[] getHeaders() {
                return new String[]{"Customer Name", "Balance Debt", "Created Date"};
            }

            @Override
            public Comparator<Object> createComparator(int[] index) {
                return (o1, o2) -> 0;
            }
        };

        captureAndDismiss("10_sort_dialog_DIALOG", () -> {
            try {
                JSort.showMessage(null, cc);
            } catch (BasicException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    @DisplayName("Modal Migration 09: JFindPanel via PosUIModal")
    public void testModal_009_FindPanel() throws Exception {
        Vectorer<Object> vectorer = new Vectorer<>() {
            @Override
            public String[] getHeaders() {
                return new String[]{"Product Code", "Product Name", "Category", "Price"};
            }

            @Override
            public String[] getValues(Object obj) {
                return new String[]{"P001", "Coffee", "Beverages", "2.50"};
            }
        };
        FindInfo<Object> findInfo = new FindInfo<>(vectorer, "Coffee", 0, false, FindInfo.MATCH_STARTFIELD);

        captureAndDismiss("09_find_dialog_MODAL_FIXED", () -> {
            try {
                com.openbravo.data.gui.JFindPanel.showMessage(null, findInfo);
            } catch (BasicException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    @DisplayName("Modal Migration 10: JSortPanel via PosUIModal")
    public void testModal_010_SortPanel() throws Exception {
        ComparatorCreator<Object> cc = new ComparatorCreator<>() {
            @Override
            public String[] getHeaders() {
                return new String[]{"Customer Name", "Balance Debt", "Created Date"};
            }

            @Override
            public Comparator<Object> createComparator(int[] index) {
                return (o1, o2) -> 0;
            }
        };

        captureAndDismiss("10_sort_dialog_MODAL_FIXED", () -> {
            try {
                com.openbravo.data.gui.JSortPanel.showMessage(null, cc);
            } catch (BasicException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Executes the dialog launcher on a dedicated thread, discovers the modal dialog,
     * asserts valid non-black rendering, takes a baseline screenshot, and dismisses it.
     *
     * @param milestoneName name identifying the screenshot and test milestone
     * @param dialogLauncher runnable containing the synchronous modal invocation
     * @throws Exception on execution failure or timeout
     */
    private void captureAndDismiss(String milestoneName, Runnable dialogLauncher) throws Exception {
        LOGGER.log(Level.INFO, "Executing pre-screen capture for: {0}", milestoneName);

        Thread dialogThread = new Thread(dialogLauncher, "DialogThread-" + milestoneName);
        dialogThread.setDaemon(true);
        dialogThread.start();

        // 1. Locate the modal dialog with timeout
        DialogFixture dialogFixture = WindowFinder.findDialog(JDialog.class)
                .withTimeout(6000)
                .using(robot);

        JDialog dialog = (JDialog) dialogFixture.target();
        assertThat(dialog.isShowing()).as("Dialog must be showing and visible").isTrue();

        // Brief stabilization pause for compositor layout
        Thread.sleep(300);

        // 2. Capture and persist baseline screenshot
        File screenshotFile = screenshotHelper.captureComponent(dialog, milestoneName);
        copyToArtifactsBaseline(screenshotFile, milestoneName);

        // 3. Verify framebuffer is not solid black (>50% non-black pixels)
        verifyNonBlackPixels(screenshotFile);

        // 4. Autonomously dismiss dialog
        dismissDialog(dialogFixture, dialog);

        // 5. Ensure launcher thread terminates cleanly
        dialogThread.join(4000);
        assertThat(dialog.isShowing()).as("Dialog must be closed after dismiss").isFalse();
        LOGGER.log(Level.INFO, "Completed pre-screen capture for: {0}", milestoneName);
    }

    /**
     * Executes an exception dialog launcher, locates and clicks the More/Details button,
     * verifies expanded layout, captures a baseline screenshot, and dismisses it.
     *
     * @param milestoneName name identifying the screenshot
     * @param dialogLauncher runnable containing the synchronous modal invocation
     * @throws Exception on execution failure or timeout
     */
    private void captureAndDismissWithExpand(String milestoneName, Runnable dialogLauncher) throws Exception {
        LOGGER.log(Level.INFO, "Executing pre-screen capture with expansion for: {0}", milestoneName);

        Thread dialogThread = new Thread(dialogLauncher, "DialogThread-" + milestoneName);
        dialogThread.setDaemon(true);
        dialogThread.start();

        DialogFixture dialogFixture = WindowFinder.findDialog(JDialog.class)
                .withTimeout(6000)
                .using(robot);

        JDialog dialog = (JDialog) dialogFixture.target();
        assertThat(dialog.isShowing()).isTrue();

        // Find and click the Details/More button to expand exception stack trace
        JButton moreButton = robot.finder().find(dialog, new GenericTypeMatcher<JButton>(JButton.class) {
            @Override
            protected boolean isMatching(JButton b) {
                if (!b.isShowing()) return false;
                String txt = b.getText();
                if (txt == null || txt.trim().isEmpty()) return false;
                return txt.equalsIgnoreCase("More") || txt.equalsIgnoreCase("Mais") || txt.equalsIgnoreCase("Info") || txt.contains("...");
            }
        });

        SwingUtilities.invokeLater(moreButton::doClick);
        Thread.sleep(400);

        // Capture expanded view
        File screenshotFile = screenshotHelper.captureComponent(dialog, milestoneName);
        copyToArtifactsBaseline(screenshotFile, milestoneName);

        verifyNonBlackPixels(screenshotFile);

        dismissDialog(dialogFixture, dialog);
        dialogThread.join(4000);
        assertThat(dialog.isShowing()).isFalse();
    }

    /**
     * Dismisses the dialog autonomously by finding an OK/Cancel button or disposing the window.
     *
     * @param dialogFixture AssertJ dialog fixture
     * @param dialog native JDialog instance
     */
    private void dismissDialog(DialogFixture dialogFixture, JDialog dialog) {
        try {
            JButton dismissButton = robot.finder().find(dialog, new GenericTypeMatcher<JButton>(JButton.class) {
                @Override
                protected boolean isMatching(JButton b) {
                    if (!b.isShowing()) return false;
                    String txt = b.getText();
                    if (txt == null || txt.trim().isEmpty()) return false;
                    String lower = txt.toLowerCase();
                    return lower.contains("cancel") || lower.contains("ok") || lower.contains("fechar") || lower.contains("close");
                }
            });
            SwingUtilities.invokeLater(dismissButton::doClick);
        } catch (Exception e) {
            LOGGER.log(Level.INFO, "Default dismiss button not matched, disposing window directly.");
            SwingUtilities.invokeLater(dialog::dispose);
        }
    }

    /**
     * Inspects captured image pixel data to verify that at least 50% of the pixels
     * are non-black, preventing BUG-001 blank window regressions.
     *
     * @param file screenshot image file
     * @throws Exception if image reading fails
     */
    private void verifyNonBlackPixels(File file) throws Exception {
        BufferedImage image = ImageIO.read(file);
        assertThat(image).as("Screenshot image must be readable").isNotNull();

        int width = image.getWidth();
        int height = image.getHeight();
        int totalPixels = width * height;
        int nonBlackPixels = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                if (r > 15 || g > 15 || b > 15) {
                    nonBlackPixels++;
                }
            }
        }

        double ratio = (double) nonBlackPixels / totalPixels;
        LOGGER.log(Level.INFO, "Image {0} pixel check: {1}% non-black ({2}/{3})",
                new Object[]{file.getName(), String.format("%.2f", ratio * 100), nonBlackPixels, totalPixels});
        assertThat(ratio).as("Dialog image must contain rendered content and not be a black box").isGreaterThan(0.50);
    }

    /**
     * Copies captured screenshot to the persistent artifacts directory for user review.
     *
     * @param sourceFile generated screenshot
     * @param milestoneName base descriptor name
     */
    private void copyToArtifactsBaseline(File sourceFile, String milestoneName) {
        if (sourceFile == null || !sourceFile.exists()) return;
        try {
            Path targetDir = Path.of(ARTIFACTS_BASELINE_DIR);
            Files.createDirectories(targetDir);
            Path dest = targetDir.resolve(milestoneName + ".png");
            Files.copy(sourceFile.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);
            LOGGER.log(Level.INFO, "Persisted baseline screenshot to: {0}", dest);

            Path wsDir = Path.of("/home/dev/PDEV/kriolos-obiz/kriolos-pos/modal-screenshots-baseline");
            if (Files.exists(wsDir)) {
                Files.copy(sourceFile.toPath(), wsDir.resolve(milestoneName + ".png"), StandardCopyOption.REPLACE_EXISTING);
                LOGGER.log(Level.INFO, "Copied screenshot to workspace folder: {0}", wsDir.resolve(milestoneName + ".png"));
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to copy baseline screenshot: " + e.getMessage());
        }
    }
}
