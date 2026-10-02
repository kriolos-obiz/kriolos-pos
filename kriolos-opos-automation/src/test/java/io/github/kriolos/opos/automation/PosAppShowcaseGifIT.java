package io.github.kriolos.opos.automation;

import com.openbravo.pos.forms.WordspacePanel;
import java.awt.Component;
import java.awt.Container;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.assertj.swing.fixture.FrameFixture;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Automated UI Robot Integration Test to generate the official application showcase animated GIF.
 *
 * <p>Captures maximized milestone views across multiple subsystems:
 * <ul>
 *   <li>1. Authentication View (with CopyrightPanel)</li>
 *   <li>2. Sales View (Modern / Standard ticket sales)</li>
 *   <li>3. Customers Management View</li>
 *   <li>4. Stock / Products View</li>
 *   <li>5. Categories View</li>
 *   <li>6. Users Management (People) View</li>
 *   <li>7. Close Cash (Fecho de Caixa) View</li>
 *   <li>8. Tax Categories & Rates View</li>
 *   <li>9. System Configuration View</li>
 * </ul>
 *
 * <p>Compiles all captured high-resolution frames into {@code kriolos-opos-screenshot-2026.gif}
 * and distributes it to the docs image directories.
 */
public class PosAppShowcaseGifIT extends BasePosRobotIT {

    private static final Logger LOGGER = Logger.getLogger(PosAppShowcaseGifIT.class.getName());

    @Override
    protected String getScenarioName() {
        return "kriolos_app_showcase";
    }

    @Test
    @DisplayName("Generate KriolOS POS Application Showcase GIF (Maximized Views)")
    public void generateShowcaseGif() throws Exception {
        LOGGER.log(Level.INFO, "[Showcase] Starting KriolOS POS 2026 Application Showcase generation...");

        List<File> frames = new ArrayList<>();

        // 1. Boot POS and Attach Main Window (Maximized)
        FrameFixture window = bootAndAttachMainWindow();
        pace(1500, 300);

        // Frame 1: Authentication View (Maximized, displaying CopyrightPanel)
        LOGGER.log(Level.INFO, "[Showcase] Capturing Frame 1: Authentication View...");
        File f1 = screenshotHelper.captureComponent(window.target(), "01_authentication_view");
        if (f1 != null && f1.exists()) frames.add(f1);

        // 2. Operator Login
        LOGGER.log(Level.INFO, "[Showcase] Performing operator login ({0})...", TARGET_OPERATOR);
        login.login(window, TARGET_OPERATOR, TARGET_PASSWORD);
        pace(1500, 300);

        // Frame 2: Sales View (with catalog item added)
        LOGGER.log(Level.INFO, "[Showcase] Interacting with Sales View...");
        try {
            sales.addCatalogItem(window);
        } catch (Exception e) {
            LOGGER.log(Level.INFO, "[Showcase] Note on catalog addition: {0}", e.getMessage());
        }
        pace(1500, 300);
        LOGGER.log(Level.INFO, "[Showcase] Capturing Frame 2: Sales View...");
        File f2 = screenshotHelper.captureComponent(window.target(), "02_sales_view");
        if (f2 != null && f2.exists()) frames.add(f2);

        // Frame 3: Customers Management View
        File f3 = navigateAndCapture(window, "com.openbravo.pos.customers.CustomersPanel", "03_customers_view");
        if (f3 != null && f3.exists()) frames.add(f3);

        // Frame 4: Stock / Products Management View
        File f4 = navigateAndCapture(window, "com.openbravo.pos.inventory.ProductsPanel", "04_products_view");
        if (f4 != null && f4.exists()) frames.add(f4);

        // Frame 5: Categories Management View
        File f5 = navigateAndCapture(window, "com.openbravo.pos.inventory.CategoriesPanel", "05_categories_view");
        if (f5 != null && f5.exists()) frames.add(f5);

        // Frame 6: Users / People Management View
        File f6 = navigateAndCapture(window, "com.openbravo.pos.admin.PeoplePanel", "06_users_view");
        if (f6 != null && f6.exists()) frames.add(f6);

        // Frame 7: Close Cash View
        File f7 = navigateAndCapture(window, "com.openbravo.pos.panels.JPanelCloseMoney", "07_close_cash_view");
        if (f7 != null && f7.exists()) frames.add(f7);

        // Frame 8: Taxes View
        File f8 = navigateAndCapture(window, "com.openbravo.pos.inventory.TaxPanel", "08_taxes_view");
        if (f8 != null && f8.exists()) frames.add(f8);

        // Frame 9: Configuration View
        File f9 = navigateAndCapture(window, "com.openbravo.pos.config.JPanelConfiguration", "09_configuration_view");
        if (f9 != null && f9.exists()) frames.add(f9);

        LOGGER.log(Level.INFO, "[Showcase] Successfully captured {0} view frames!", frames.size());
        Assertions.assertTrue(frames.size() >= 7, "Showcase must capture at least 7 views");

        // 3. Compile animated GIF from captured frames
        File scenarioDir = new File("target/recordings/" + getScenarioName());
        scenarioDir.mkdirs();
        File gifOutput = new File(scenarioDir, "kriolos-opos-screenshot-2026.gif");

        LOGGER.log(Level.INFO, "[Showcase] Compiling animated GIF to {0}...", gifOutput.getAbsolutePath());
        boolean gifOk = FfmpegScreenGifGenerator.generateGifFromImages(frames, gifOutput, 2.0, 1200);
        Assertions.assertTrue(gifOk && gifOutput.exists() && gifOutput.length() > 0,
                "GIF generation should produce non-empty file: " + gifOutput);

        LOGGER.log(Level.INFO, "[Showcase] Animated GIF successfully generated! Size: {0} bytes", gifOutput.length());

        // 4. Distribute GIF to docs image directories
        File docsMainImages = new File("../docs/src/main/images/kriolos-opos-screenshot-2026.gif");
        File docsRootImages = new File("../docs/modules/ROOT/images/kriolos-opos-screenshot-2026.gif");

        copyFile(gifOutput, docsMainImages);
        copyFile(gifOutput, docsRootImages);

        LOGGER.log(Level.INFO, "[Showcase] Showcase GIF deployment completed successfully.");
    }

    private File navigateAndCapture(FrameFixture window, String taskClass, String milestoneName) {
        LOGGER.log(Level.INFO, "[Showcase] Navigating to view task: {0} ({1})...",
                new Object[]{taskClass, milestoneName});
        WordspacePanel wsp = findWordspacePanel(window.target());
        if (wsp != null) {
            try {
                org.assertj.swing.edt.GuiActionRunner.execute(() -> wsp.showTask(taskClass));
                pace(1500, 300);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "[Showcase] Error navigating to " + taskClass + ": " + e.getMessage(), e);
            }
        } else {
            LOGGER.log(Level.WARNING, "[Showcase] WordspacePanel not found in window tree!");
        }
        return screenshotHelper.captureComponent(window.target(), milestoneName);
    }

    private WordspacePanel findWordspacePanel(Container container) {
        if (container instanceof WordspacePanel wsp) {
            return wsp;
        }
        for (Component c : container.getComponents()) {
            if (c instanceof WordspacePanel wsp) {
                return wsp;
            }
            if (c instanceof Container child) {
                WordspacePanel found = findWordspacePanel(child);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private void copyFile(File source, File destination) {
        try {
            if (destination.getParentFile() != null) {
                destination.getParentFile().mkdirs();
            }
            Files.copy(source.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
            LOGGER.log(Level.INFO, "[Showcase] Copied GIF to: {0} ({1} bytes)",
                    new Object[]{destination.getAbsolutePath(), destination.length()});
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "[Showcase] Error copying GIF to " + destination + ": " + e.getMessage(), e);
        }
    }
}
