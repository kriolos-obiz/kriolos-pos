package io.github.kriolos.opos.automation.actions;

import io.github.kriolos.opos.automation.ScreenshotHelper;
import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.core.Robot;
import org.assertj.swing.fixture.FrameFixture;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import java.awt.Component;
import java.awt.Container;

/**
 * Reusable Action Driver for Sidebar Menu Navigation.
 */
public class MenuNavigationAction {

    private static final Logger LOGGER = Logger.getLogger(MenuNavigationAction.class.getName());

    private final Robot robot;
    private final ScreenshotHelper screenshotHelper;
    private final boolean isHumanPacing;

    public MenuNavigationAction(Robot robot, ScreenshotHelper screenshotHelper, boolean isHumanPacing) {
        this.robot = robot;
        this.screenshotHelper = screenshotHelper;
        this.isHumanPacing = isHumanPacing;
    }

    /**
     * Navigates to the Sales view via menu or verifies it is already active.
     */
    public void navigateToSales(FrameFixture window) {
        LOGGER.log(Level.INFO, "[MenuNavigationAction] Navigating to Sales view...");
        pace(800);
        selectMenuItem(window, "Sales", "Vendas", "Ticket");
        pace(1000, 50);
        screenshotHelper.captureComponent(window.target(), "03_sales_screen_ready");
    }

    /**
     * Selects a menu item by matching candidate labels.
     */
    public boolean selectMenuItem(FrameFixture window, String... candidateLabels) {
        pace(800);
        ensureMenuExpanded(window);

        for (String label : candidateLabels) {
            try {
                AbstractButton btn = findButtonMatchingText(window.target(), label);
                if (btn != null && btn.isShowing()) {
                    javax.swing.SwingUtilities.invokeLater(btn::doClick);
                    LOGGER.log(Level.INFO, "[MenuNavigationAction] Clicked menu item: {0}", btn.getText());
                    pace(1000, 50);
                    return true;
                }
            } catch (Exception ignored) {
            }
        }
        LOGGER.log(Level.INFO, "[MenuNavigationAction] Note: Menu item already active or matched view.");
        return false;
    }

    /**
     * Ensures that the side navigation menu is expanded.
     */
    public void ensureMenuExpanded(FrameFixture window) {
        try {
            JButton toggleBtn = window.button(new GenericTypeMatcher<JButton>(JButton.class) {
                @Override
                protected boolean isMatching(JButton b) {
                    if (!b.isShowing()) return false;
                    String name = b.getName();
                    if (name != null && name.toLowerCase().contains("colapse")) return true;
                    if (b.getIcon() != null && b.getIcon().toString().contains("menu-")) return true;
                    return false;
                }
            }).target();

            // If menu toggle button is present, ensure menu is open
            if (toggleBtn != null && toggleBtn.isShowing()) {
                // If collapsed, toggle it open
                if (toggleBtn.getIcon() != null && toggleBtn.getIcon().toString().contains("menu-right")) {
                    javax.swing.SwingUtilities.invokeLater(toggleBtn::doClick);
                    pace(600, 50);
                }
            }
        } catch (Exception ignored) {
        }
    }

    private AbstractButton findButtonMatchingText(Container container, String match) {
        for (Component c : container.getComponents()) {
            if (c instanceof AbstractButton b) {
                String t = b.getText();
                if (t != null && t.toLowerCase().contains(match.toLowerCase())) {
                    return b;
                }
            }
            if (c instanceof Container childContainer) {
                AbstractButton res = findButtonMatchingText(childContainer, match);
                if (res != null) return res;
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
