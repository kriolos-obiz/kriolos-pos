package io.github.kriolos.opos.automation;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.assertj.swing.fixture.FrameFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Targeted Test Case: Operator Login Flow Only.
 *
 * <p>Validates:
 * <ul>
 *   <li>Database selection (if multi-db is enabled)</li>
 *   <li>Main window appearance and operator button discovery</li>
 *   <li>Operator selection, password entry (if configured), and authentication submission</li>
 *   <li>Successful transition to the authenticated application view</li>
 * </ul>
 */
public class PosLoginIT extends BasePosRobotIT {

    private static final Logger LOGGER = Logger.getLogger(PosLoginIT.class.getName());

    @Override
    protected String getScenarioName() {
        return "pos_login_run";
    }

    @Test
    @DisplayName("Test Operator Login Flow Only")
    public void testLoginOnly() {
        LOGGER.log(Level.INFO, "[PosLoginIT] Starting Operator Login test use case...");

        // 1. Boot POS and attach main window
        FrameFixture window = bootAndAttachMainWindow();

        // 2. Perform operator login
        login.login(window, TARGET_OPERATOR, TARGET_PASSWORD);

        // 3. Verify authenticated state
        pace(1000, 50);
        screenshotHelper.captureComponent(window.target(), "03_logged_in_state");
        LOGGER.log(Level.INFO, "[PosLoginIT] Operator login test use case completed successfully.");
    }
}
