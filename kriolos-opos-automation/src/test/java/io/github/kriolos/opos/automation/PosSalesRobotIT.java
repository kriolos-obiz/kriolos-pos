package io.github.kriolos.opos.automation;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.assertj.swing.fixture.FrameFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Automated UI Robot Integration Test for KriolOS POS Swing Application.
 *
 * <p>Leverages reusable Action Drivers:
 * <ul>
 *   <li>{@code dbSelection}: Multi-database selection dialog when {@code db.multi=true}</li>
 *   <li>{@code mainWindow}: Attach, sizing, and lifecycle</li>
 *   <li>{@code login}: Operator authentication with optional password prompt</li>
 *   <li>{@code menuNav}: Navigation to Sales interface</li>
 *   <li>{@code sales}: Product interaction and cart additions</li>
 * </ul>
 */
public class PosSalesRobotIT extends BasePosRobotIT {

    private static final Logger LOGGER = Logger.getLogger(PosSalesRobotIT.class.getName());

    @Override
    protected String getScenarioName() {
        return "pos_sales_run";
    }

    @Test
    @DisplayName("Automate POS Login, Navigation, and Sales Flow with Milestone Snapshots")
    public void testLoginAndSalesFlow() throws Exception {
        // 1. Boot POS and Attach Window
        FrameFixture window = bootAndAttachMainWindow();

        // 2. Operator Login
        login.login(window, TARGET_OPERATOR, TARGET_PASSWORD);

        // 3. Navigation to Sales View
        menuNav.navigateToSales(window);

        // 4. Product Selection / Cart Addition
        sales.addCatalogItem(window);

        // 5. Final State Capture
        pace(2000, 100);
        screenshotHelper.captureComponent(window.target(), "05_final_state");
        LOGGER.log(Level.INFO, "[PosSalesRobotIT] UI automation scenario completed successfully!");
    }
}
