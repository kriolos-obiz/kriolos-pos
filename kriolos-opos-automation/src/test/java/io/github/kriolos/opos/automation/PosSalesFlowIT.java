package io.github.kriolos.opos.automation;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.assertj.swing.fixture.DialogFixture;
import org.assertj.swing.fixture.FrameFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * End-to-End Test Use Case: Full Sales & Payment Lifecycle.
 *
 * <p>Validates the complete workflow:
 * <ol>
 *   <li>Database selection (if multi-database is enabled)</li>
 *   <li>Operator login with credentials</li>
 *   <li>Navigation to Sales view via side menu</li>
 *   <li>Adding catalog products to the shopping cart</li>
 *   <li>Pressing '=' to trigger the Payment dialog</li>
 *   <li>Selecting payment method (Cash) and verifying received amount</li>
 *   <li>Confirming payment with OK and closing ticket</li>
 *   <li>Returning to clean state ready for next sale</li>
 * </ol>
 */
public class PosSalesFlowIT extends BasePosRobotIT {

    private static final Logger LOGGER = Logger.getLogger(PosSalesFlowIT.class.getName());

    @Override
    protected String getScenarioName() {
        return "pos_sales_flow_run";
    }

    @Test
    @DisplayName("Complete Sales Flow: DB -> Login -> Sales View -> Add Cart -> Payment '=' -> Cash -> Confirm")
    public void testFullSalesAndPaymentFlow() {
        LOGGER.log(Level.INFO, "[PosSalesFlowIT] Starting Full Sales Flow test use case...");

        // 1. Boot POS and Login
        FrameFixture window = bootAndLogin();

        // 2. Open / Verify Sales view
        menuNav.navigateToSales(window);

        // 3. Add item to cart
        sales.addCatalogItem(window);

        // 4. Press '=' payment button on the keypad
        sales.pressPaymentButton(window);

        // 5. Interact with Payment dialog: select Cash and confirm
        DialogFixture paymentDialog = sales.getPaymentDialogAction().attach(6000);
        sales.getPaymentDialogAction().selectPaymentMethod(paymentDialog, "Cash");
        sales.getPaymentDialogAction().confirmPayment(paymentDialog);

        // 6. Capture final post-sale state
        pace(1500, 100);
        screenshotHelper.captureComponent(window.target(), "05_final_state");
        LOGGER.log(Level.INFO, "[PosSalesFlowIT] Full sales and payment flow test completed successfully!");
    }
}
