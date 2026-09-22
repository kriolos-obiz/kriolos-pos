package io.github.kriolos.opos.automation.actions;

import io.github.kriolos.opos.automation.ScreenshotHelper;
import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.core.Robot;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JButtonFixture;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import java.awt.Component;
import java.awt.Container;

/**
 * Reusable Action Driver for Sales Screen interactions (cart items, keypad, payment trigger).
 */
public class SalesAction {

    private static final Logger LOGGER = Logger.getLogger(SalesAction.class.getName());

    private final Robot robot;
    private final ScreenshotHelper screenshotHelper;
    private final boolean isHumanPacing;
    private final PaymentDialogAction paymentDialogAction;

    public SalesAction(Robot robot, ScreenshotHelper screenshotHelper, boolean isHumanPacing) {
        this.robot = robot;
        this.screenshotHelper = screenshotHelper;
        this.isHumanPacing = isHumanPacing;
        this.paymentDialogAction = new PaymentDialogAction(robot, screenshotHelper, isHumanPacing);
    }

    /**
     * Adds an item to the shopping cart by clicking a catalog product button.
     */
    public void addCatalogItem(FrameFixture window) {
        LOGGER.log(Level.INFO, "[SalesAction] Interacting with catalog item...");
        pace(1200, 200);

        try {
            // Priority 1: Match ProductCardV1 component directly
            JButton productBtn = findProductButton(window.target());
            if (productBtn != null) {
                javax.swing.SwingUtilities.invokeLater(productBtn::doClick);
                LOGGER.log(Level.INFO, "[SalesAction] Clicked product card: {0}", productBtn.getText());
            } else {
                JButtonFixture productButton = window.button(new GenericTypeMatcher<JButton>(JButton.class) {
                    @Override
                    protected boolean isMatching(JButton btn) {
                        if (!btn.isShowing() || btn.getText() == null) return false;
                        String text = btn.getText().trim().toLowerCase();
                        return !text.isBlank()
                                && !text.equals("admin")
                                && !text.equals("manager")
                                && !text.equals("empl")
                                && !text.equals("sair")
                                && !text.equals("exit")
                                && !text.contains("pedido")
                                && !text.contains("estoque")
                                && !text.contains("entrada")
                                && !text.contains("saida")
                                && btn.getWidth() > 30;
                    }
                });
                JButton btn = productButton.target();
                javax.swing.SwingUtilities.invokeLater(btn::doClick);
                LOGGER.log(Level.INFO, "[SalesAction] Clicked catalog item: {0}", btn.getText());
            }
        } catch (Exception e) {
            LOGGER.log(Level.INFO, "[SalesAction] Catalog button matching note: {0}", e.getMessage());
        }

        pace(1500, 300);
        screenshotHelper.captureComponent(window.target(), "04_item_added_to_cart");
    }

    private JButton findProductButton(Container container) {
        if (container == null) return null;
        for (Component c : container.getComponents()) {
            if (c instanceof com.openbravo.pos.catalog.ProductCardV1 pc && pc.isShowing()) {
                return pc;
            }
            if (c instanceof Container child) {
                JButton res = findProductButton(child);
                if (res != null) return res;
            }
        }
        return null;
    }

    /**
     * Triggers the Payment dialog by clicking the '=' button on the numeric keypad.
     */
    public void pressPaymentButton(FrameFixture window) {
        LOGGER.log(Level.INFO, "[SalesAction] Pressing '=' payment button...");
        pace(1000);

        // 1. Look for '=' button by icon or text in the keypad
        JButton equalsButton = findEqualsButton(window.target());
        if (equalsButton != null) {
            javax.swing.SwingUtilities.invokeLater(equalsButton::doClick);
            LOGGER.log(Level.INFO, "[SalesAction] Clicked '=' keypad button directly.");
        } else {
            // Fallback: search for any button containing '='
            try {
                JButtonFixture btnFixture = window.button(new GenericTypeMatcher<JButton>(JButton.class) {
                    @Override
                    protected boolean isMatching(JButton b) {
                        if (!b.isShowing()) return false;
                        String t = b.getText();
                        if (t != null && t.equals("=")) return true;
                        if (b.getIcon() != null && b.getIcon().toString().contains("btnequals")) return true;
                        return false;
                    }
                });
                btnFixture.click();
                LOGGER.log(Level.INFO, "[SalesAction] Clicked '=' button via AssertJ fixture.");
            } catch (Exception ex) {
                LOGGER.log(Level.INFO, "[SalesAction] Note triggering '=': {0}", ex.getMessage());
            }
        }
        pace(1000, 50);
    }

    /**
     * Performs a complete sales payment cycle: presses '=' and confirms payment.
     */
    public void checkoutWithCash(FrameFixture window) {
        pressPaymentButton(window);
        paymentDialogAction.payCashAndConfirm(5000);
        pace(1200, 50);
        screenshotHelper.captureComponent(window.target(), "05_sale_completed");
        LOGGER.log(Level.INFO, "[SalesAction] Sale checkout completed.");
    }

    public PaymentDialogAction getPaymentDialogAction() {
        return paymentDialogAction;
    }

    private JButton findEqualsButton(Container container) {
        for (Component c : container.getComponents()) {
            if (c instanceof JButton b) {
                if (b.getIcon() != null && b.getIcon().toString().contains("btnequals")) {
                    return b;
                }
                if ("=".equals(b.getText())) {
                    return b;
                }
            }
            if (c instanceof Container child) {
                JButton found = findEqualsButton(child);
                if (found != null) return found;
            }
        }
        return null;
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
