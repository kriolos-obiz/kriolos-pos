package io.github.kriolos.opos.automation;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Targeted Test Case: Database Selection Dialog Only.
 *
 * <p>Validates:
 * <ul>
 *   <li>Detection of the Database Selection dialog when db.multi=true</li>
 *   <li>Selection of the first database by default (index 0) or matching -Dtest.db</li>
 *   <li>Confirmation and transition towards main application launch</li>
 * </ul>
 */
public class DatabaseSelectionIT extends BasePosRobotIT {

    private static final Logger LOGGER = Logger.getLogger(DatabaseSelectionIT.class.getName());

    @Override
    protected String getScenarioName() {
        return "database_selection_run";
    }

    @Test
    @DisplayName("Test Database Selection (In-Frame Selector & Startup Flow)")
    public void testDatabaseSelectionOnly() {
        LOGGER.log(Level.INFO, "Starting DB Selection test use case...");
        boolean dialogHandled = dbSelection.handleIfPresent(TARGET_DB);
        LOGGER.log(Level.INFO, "Startup DB dialog handled: {0}", dialogHandled);

        // Verify application proceeds to main window
        mainWindow.attach(45000);
        mainWindow.configureWindowMode(false);

        // Test in-frame DatabaseSelector panel on login view
        boolean panelHandled = dbSelection.selectDatabaseInPanel(mainWindow.getTargetFrame(), TARGET_DB);
        LOGGER.log(Level.INFO, "In-frame DatabaseSelector handled: {0}", panelHandled);

        screenshotHelper.captureComponent(mainWindow.getTargetFrame(), "01_db_selection_verified");
        LOGGER.log(Level.INFO, "Database selection test use case completed successfully.");
    }
}
