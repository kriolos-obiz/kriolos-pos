package io.github.kriolos.opos.automation.actions;

import io.github.kriolos.opos.automation.ScreenshotHelper;
import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.fixture.FrameFixture;

import javax.swing.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Reusable Action Driver for Back/Forward navigation button workflows.
 *
 * <p>Locates buttons by their {@code .setName()} anchors:
 * <ul>
 *   <li>{@code "navigationBack"}    — the Back button in JPrincipalApp</li>
 *   <li>{@code "navigationForward"} — the Forward button in JPrincipalApp</li>
 *   <li>{@code "screenTitle"}       — the content title label</li>
 * </ul>
 * </p>
 */
public class NavigationHistoryAction {

    private static final Logger LOGGER = Logger.getLogger(NavigationHistoryAction.class.getName());

    private final ScreenshotHelper screenshotHelper;
    private final boolean isHumanPacing;

    public NavigationHistoryAction(ScreenshotHelper screenshotHelper, boolean isHumanPacing) {
        this.screenshotHelper = screenshotHelper;
        this.isHumanPacing = isHumanPacing;
    }

    // -------------------------------------------------------------------------

    /**
     * Asserts the Back button ({@code name="navigationBack"}) is enabled.
     */
    public void assertBackEnabled(FrameFixture window) {
        JButton btn = findNavButton(window, "navigationBack");
        assert btn != null && btn.isEnabled()
                : "[NavigationHistoryAction] Expected 'navigationBack' to be enabled";
        LOGGER.log(Level.INFO, "[NavigationHistoryAction] Back button is enabled — OK");
    }

    /**
     * Asserts the Back button is disabled.
     */
    public void assertBackDisabled(FrameFixture window) {
        JButton btn = findNavButton(window, "navigationBack");
        assert btn != null && !btn.isEnabled()
                : "[NavigationHistoryAction] Expected 'navigationBack' to be disabled";
    }

    /**
     * Asserts the Forward button ({@code name="navigationForward"}) is enabled.
     */
    public void assertForwardEnabled(FrameFixture window) {
        JButton btn = findNavButton(window, "navigationForward");
        assert btn != null && btn.isEnabled()
                : "[NavigationHistoryAction] Expected 'navigationForward' to be enabled";
    }

    /**
     * Asserts the Forward button is disabled.
     */
    public void assertForwardDisabled(FrameFixture window) {
        JButton btn = findNavButton(window, "navigationForward");
        assert btn != null && !btn.isEnabled()
                : "[NavigationHistoryAction] Expected 'navigationForward' to be disabled";
    }

    /** Clicks the Back button and waits for UI to settle. */
    public void clickBack(FrameFixture window) {
        pace(500);
        JButton btn = findNavButton(window, "navigationBack");
        if (btn != null && btn.isEnabled()) {
            SwingUtilities.invokeLater(btn::doClick);
            LOGGER.log(Level.INFO, "[NavigationHistoryAction] Clicked Back");
            pace(800, 50);
        } else {
            LOGGER.log(Level.WARNING, "[NavigationHistoryAction] Back button not found or disabled");
        }
    }

    /** Clicks the Forward button and waits for UI to settle. */
    public void clickForward(FrameFixture window) {
        pace(500);
        JButton btn = findNavButton(window, "navigationForward");
        if (btn != null && btn.isEnabled()) {
            SwingUtilities.invokeLater(btn::doClick);
            LOGGER.log(Level.INFO, "[NavigationHistoryAction] Clicked Forward");
            pace(800, 50);
        } else {
            LOGGER.log(Level.WARNING, "[NavigationHistoryAction] Forward button not found or disabled");
        }
    }

    /** Captures a screenshot of the current screen state. */
    public void captureScreen(FrameFixture window, String tag) {
        screenshotHelper.captureComponent(window.target(), "nav_" + tag);
        LOGGER.log(Level.INFO, "[NavigationHistoryAction] Screenshot: nav_{0}", tag);
    }

    // -------------------------------------------------------------------------

    private JButton findNavButton(FrameFixture window, String name) {
        try {
            return window.button(new GenericTypeMatcher<JButton>(JButton.class) {
                @Override
                protected boolean isMatching(JButton b) {
                    return name.equals(b.getName());
                }
            }).target();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "[NavigationHistoryAction] Button not found: " + name);
            return null;
        }
    }

    private void pace(long humanMs) { pace(humanMs, 0); }

    private void pace(long humanMs, long robotMs) {
        long delay = isHumanPacing ? humanMs : robotMs;
        if (delay > 0) {
            try { Thread.sleep(delay); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }
    }
}
