package io.github.kriolos.opos.automation.actions;

import io.github.kriolos.opos.automation.ScreenshotHelper;
import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.core.Robot;
import org.assertj.swing.finder.WindowFinder;
import org.assertj.swing.fixture.DialogFixture;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JTabbedPane;
import java.awt.Component;
import java.awt.Container;

/**
 * Reusable Action Driver for the POS Payment Dialog (JPaymentSelect).
 */
public class PaymentDialogAction {

    private static final Logger LOGGER = Logger.getLogger(PaymentDialogAction.class.getName());

    private final Robot robot;
    private final ScreenshotHelper screenshotHelper;
    private final boolean isHumanPacing;

    public PaymentDialogAction(Robot robot, ScreenshotHelper screenshotHelper, boolean isHumanPacing) {
        this.robot = robot;
        this.screenshotHelper = screenshotHelper;
        this.isHumanPacing = isHumanPacing;
    }

    /**
     * Waits for the Payment dialog to appear within timeout.
     */
    public DialogFixture attach(long timeoutMs) {
        LOGGER.log(Level.INFO, "[PaymentDialogAction] Waiting for Payment dialog to appear...");
        DialogFixture paymentDialog = WindowFinder.findDialog(new GenericTypeMatcher<JDialog>(JDialog.class) {
            @Override
            protected boolean isMatching(JDialog d) {
                if (!d.isShowing()) return false;
                String title = d.getTitle();
                return title != null && (
                        title.equalsIgnoreCase("Payment") ||
                        title.equalsIgnoreCase("Pagamento") ||
                        title.toLowerCase().contains("payment") ||
                        title.toLowerCase().contains("pagamento")
                );
            }
        }).withTimeout(timeoutMs).using(robot);

        LOGGER.log(Level.INFO, "[PaymentDialogAction] Attached to Payment dialog: {0}", paymentDialog.target().getTitle());
        screenshotHelper.captureComponent(paymentDialog.target(), "04b_payment_dialog");
        pace(800);
        return paymentDialog;
    }

    /**
     * Selects payment method tab (e.g. Cash / Dinheiro).
     */
    public void selectPaymentMethod(DialogFixture dialog, String methodName) {
        try {
            JTabbedPane tabs = robot.finder().findByType(dialog.target(), JTabbedPane.class);
            if (tabs != null) {
                for (int i = 0; i < tabs.getTabCount(); i++) {
                    String title = tabs.getTitleAt(i);
                    if (title != null && title.toLowerCase().contains(methodName.toLowerCase())) {
                        final int tabIdx = i;
                        org.assertj.swing.edt.GuiActionRunner.execute(() -> tabs.setSelectedIndex(tabIdx));
                        LOGGER.log(Level.INFO, "[PaymentDialogAction] Selected payment tab: {0}", title.trim());
                        pace(600);
                        return;
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * Confirms the payment dialog by clicking OK.
     */
    public void confirmPayment(DialogFixture dialog) {
        pace(800, 100);
        JButton okButton = dialog.button(new GenericTypeMatcher<JButton>(JButton.class) {
            @Override
            protected boolean isMatching(JButton b) {
                if (!b.isShowing()) return false;
                String t = b.getText();
                return t != null && (t.equalsIgnoreCase("OK") || t.equalsIgnoreCase("Sim"));
            }
        }).target();

        javax.swing.SwingUtilities.invokeLater(okButton::doClick);
        LOGGER.log(Level.INFO, "[PaymentDialogAction] Confirmed payment via OK button.");
        pace(1000, 100);
    }

    /**
     * High-level flow: attaches to payment dialog, selects method, and confirms.
     */
    public void payCashAndConfirm(long timeoutMs) {
        DialogFixture dialog = attach(timeoutMs);
        selectPaymentMethod(dialog, "Cash");
        pace(800);
        confirmPayment(dialog);
    }

    private void pace(long humanMs) {
        pace(humanMs, 0);
    }

    private void pace(long humanMs, long robotMs) {
        long delay = isHumanPacing ? humanMs : robotMs;
        if (delay > 0) {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
