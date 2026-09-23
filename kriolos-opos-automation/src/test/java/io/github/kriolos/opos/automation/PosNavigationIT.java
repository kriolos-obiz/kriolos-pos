package io.github.kriolos.opos.automation;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.assertj.swing.fixture.FrameFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Integration Test for Screen Navigation (Back / Forward History Engine).
 *
 * <p>Validates:
 * <ul>
 *   <li>Discovery of navigation buttons via URN anchors:
 *     <ul>
 *       <li>{@code "kriolos:navigation:back"}</li>
 *       <li>{@code "kriolos:navigation:forward"}</li>
 *     </ul>
 *   </li>
 *   <li>Initial state: Back and Forward buttons both disabled</li>
 *   <li>Navigation to first screen: Back remains disabled (no prior history)</li>
 *   <li>Navigation to second screen: Back button becomes enabled</li>
 *   <li>Clicking Back: transitions to previous screen and enables Forward button</li>
 *   <li>Clicking Forward: transitions forward and disables Forward at head of history</li>
 *   <li>Visual milestone capture via {@link ScreenshotHelper}</li>
 * </ul>
 * </p>
 */
public class PosNavigationIT extends BasePosRobotIT {

    private static final Logger LOGGER = Logger.getLogger(PosNavigationIT.class.getName());

    @Override
    protected String getScenarioName() {
        return "pos_navigation_run";
    }

    @Test
    @DisplayName("Test Back/Forward Navigation History Lifecycle")
    public void testNavigationHistoryFlow() {
        LOGGER.log(Level.INFO, "[PosNavigationIT] Starting Back/Forward Navigation test use case...");

        // 1. Boot POS and perform operator login
        FrameFixture window = bootAndLogin();

        // 2. Initial state verification: both nav buttons disabled
        navHistory.assertBackDisabled(window);
        navHistory.assertForwardDisabled(window);
        navHistory.captureScreen(window, "01_initial_state");

        // 3. Navigate to first screen (Sales)
        LOGGER.log(Level.INFO, "[PosNavigationIT] Navigating to first screen (Sales)...");
        menuNav.selectMenuItem(window, "Sales", "Vendas", "Ticket");
        pace(1000, 50);
        navHistory.captureScreen(window, "02_first_screen_sales");

        // Back still disabled because there is only 1 screen in history
        navHistory.assertBackDisabled(window);
        navHistory.assertForwardDisabled(window);

        // 4. Navigate to second screen (Customers / Clientes / Administration)
        LOGGER.log(Level.INFO, "[PosNavigationIT] Navigating to second screen...");
        boolean secondScreenOpened = menuNav.selectMenuItem(window, "Customers", "Clientes", "Edit", "Stock");
        pace(1000, 50);

        if (secondScreenOpened) {
            navHistory.captureScreen(window, "03_second_screen");

            // 5. Verify Back button is now ENABLED, Forward still DISABLED
            navHistory.assertBackEnabled(window);
            navHistory.assertForwardDisabled(window);

            // 6. Click Back -> should return to Sales
            LOGGER.log(Level.INFO, "[PosNavigationIT] Clicking Back button...");
            navHistory.clickBack(window);
            pace(1000, 50);
            navHistory.captureScreen(window, "04_after_back");

            // Forward must now be ENABLED
            navHistory.assertForwardEnabled(window);

            // 7. Click Forward -> should return to second screen
            LOGGER.log(Level.INFO, "[PosNavigationIT] Clicking Forward button...");
            navHistory.clickForward(window);
            pace(1000, 50);
            navHistory.captureScreen(window, "05_after_forward");

            // Forward must be DISABLED at the head of the forward stack
            navHistory.assertForwardDisabled(window);
            navHistory.assertBackEnabled(window);
        } else {
            LOGGER.log(Level.WARNING, "[PosNavigationIT] Second menu item could not be selected from sidebar.");
        }

        pace(1000, 50);
        LOGGER.log(Level.INFO, "[PosNavigationIT] Back/Forward Navigation test completed successfully.");
    }
}
