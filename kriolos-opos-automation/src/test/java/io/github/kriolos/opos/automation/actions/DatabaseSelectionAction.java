package io.github.kriolos.opos.automation.actions;

import io.github.kriolos.opos.automation.ScreenshotHelper;
import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.core.Robot;
import org.assertj.swing.finder.WindowFinder;
import org.assertj.swing.fixture.DialogFixture;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import java.awt.Frame;

/**
 * Reusable Action Driver for handling the Database Selection dialog (when db.multi=true).
 */
public class DatabaseSelectionAction {

    private static final Logger LOGGER = Logger.getLogger(DatabaseSelectionAction.class.getName());

    private final Robot robot;
    private final ScreenshotHelper screenshotHelper;
    private final boolean isHumanPacing;

    public DatabaseSelectionAction(Robot robot, ScreenshotHelper screenshotHelper, boolean isHumanPacing) {
        this.robot = robot;
        this.screenshotHelper = screenshotHelper;
        this.isHumanPacing = isHumanPacing;
    }

    /**
     * Checks if a database selection dialog appears within the specified timeout.
     */
    public boolean isDialogPresent(long timeoutMs) {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            for (Frame f : Frame.getFrames()) {
                if (f.isShowing() && f.getTitle() != null && !f.getTitle().isBlank()) {
                    return false; // Main frame already visible
                }
            }
            try {
                findDialog(500);
                return true;
            } catch (Exception ignored) {
                sleep(200);
            }
        }
        return false;
    }

    /**
     * Handles database selection if the dialog is present, defaulting to the first database (index 0)
     * or matching the given target database name.
     */
    public boolean handleIfPresent(String targetDbName) {
        LOGGER.log(Level.INFO, "Checking for Database Selection dialog...");
        try {
            DialogFixture dbDialog = findDialog(6000);
            LOGGER.log(Level.INFO, "Detected Database Selection dialog: {0}", dbDialog.target().getTitle());
            screenshotHelper.captureComponent(dbDialog.target(), "00_database_selection");
            pace(800);

            // Select database in dropdown (index 0 by default, or match targetDbName)
            JComboBox<?> combo = robot.finder().findByType(dbDialog.target(), JComboBox.class);
            if (combo != null && combo.getItemCount() > 0) {
                int selectedIdx = 0;
                if (targetDbName != null && !targetDbName.isBlank()) {
                    for (int i = 0; i < combo.getItemCount(); i++) {
                        Object item = combo.getItemAt(i);
                        if (item != null && item.toString().toLowerCase().contains(targetDbName.toLowerCase())) {
                            selectedIdx = i;
                            break;
                        }
                    }
                }
                final int idxToSet = selectedIdx;
                org.assertj.swing.edt.GuiActionRunner.execute(() -> combo.setSelectedIndex(idxToSet));
                LOGGER.log(Level.INFO, "Selected database option [{0}]: {1}", new Object[]{idxToSet, combo.getItemAt(idxToSet)});
            }

            pace(1000);

            // Confirm selection via OK button
            JButton okButton = dbDialog.button(new GenericTypeMatcher<JButton>(JButton.class) {
                @Override
                protected boolean isMatching(JButton btn) {
                    String txt = btn.getText();
                    return txt != null && (
                            txt.equalsIgnoreCase("OK") ||
                            txt.equalsIgnoreCase("Sim") ||
                            txt.equalsIgnoreCase("Yes")
                    );
                }
            }).target();

            javax.swing.SwingUtilities.invokeLater(okButton::doClick);
            LOGGER.log(Level.INFO, "Confirmed database selection.");
            pace(1000, 50);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.INFO, "No database selection dialog appeared (dialog bypassed or single DB).");
            return false;
        }
    }

    /**
     * Selects and activates a database using the integrated AuthenticationPanel database selector.
     */
    public boolean selectDatabaseInPanel(Frame mainFrame, String targetDbName) {
        LOGGER.log(Level.INFO, "Checking for integrated DatabaseSelector in AuthenticationPanel...");
        try {
            JComboBox<?> combo = findComponentByName(mainFrame, "kriolos:auth:combo-databases", "comboDatabases", JComboBox.class);
            if (combo != null && combo.getItemCount() > 0) {
                int selectedIdx = 0;
                if (targetDbName != null && !targetDbName.isBlank()) {
                    for (int i = 0; i < combo.getItemCount(); i++) {
                        Object item = combo.getItemAt(i);
                        if (item != null && item.toString().toLowerCase().contains(targetDbName.toLowerCase())) {
                            selectedIdx = i;
                            break;
                        }
                    }
                }
                final int idxToSet = selectedIdx;
                org.assertj.swing.edt.GuiActionRunner.execute(() -> combo.setSelectedIndex(idxToSet));
                LOGGER.log(Level.INFO, "Database selector set to [{0}]: {1}", new Object[]{idxToSet, combo.getItemAt(idxToSet)});

                pace(500);

                screenshotHelper.captureComponent(mainFrame, "00_database_empty_state");

                JButton btnSelect = findComponentByName(mainFrame, "kriolos:auth:btn-select-database", "btnSelectDatabase", JButton.class);
                if (btnSelect != null) {
                    javax.swing.SwingUtilities.invokeLater(btnSelect::doClick);
                    LOGGER.log(Level.INFO, "Clicked 'Ativar' button in DatabaseSelector.");

                    // Wait for non-blocking asynchronous connection / migration to complete
                    long deadline = System.currentTimeMillis() + 10000;
                    while (System.currentTimeMillis() < deadline) {
                        try {
                            javax.swing.JProgressBar pb = findComponentByName(mainFrame, "kriolos:auth:progress-bar", "progressBar", javax.swing.JProgressBar.class);
                            if (pb == null || !pb.isVisible()) {
                                break;
                            }
                        } catch (Exception ignored) {
                        }
                        sleep(150);
                    }

                    screenshotHelper.captureComponent(mainFrame, "00_database_selector_switched");
                    pace(1000);
                    return true;
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "DatabaseSelector controls not found or error: {0}", e.getMessage());
        }
        return false;
    }

    /**
     * Retrieves the text of the active database status badge.
     */
    public String getActiveDatabaseBadgeText(Frame mainFrame) {
        try {
            javax.swing.JLabel badge = findComponentByName(mainFrame, "kriolos:auth:active-database-badge", "lblActiveDatabaseBadge", javax.swing.JLabel.class);
            return badge != null ? badge.getText() : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Retrieves the current status / progress message text.
     */
    public String getStatusMessage(Frame mainFrame) {
        try {
            javax.swing.JLabel status = findComponentByName(mainFrame, "kriolos:auth:status-label", "statusLabel", javax.swing.JLabel.class);
            return status != null ? status.getText() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private <T extends java.awt.Component> T findComponentByName(Frame root, String urnName, String legacyName, Class<T> type) {
        try {
            return robot.finder().findByName(root, urnName, type);
        } catch (Exception e1) {
            if (legacyName != null) {
                try {
                    return robot.finder().findByName(root, legacyName, type);
                } catch (Exception ignored) {
                }
            }
            return null;
        }
    }

    private DialogFixture findDialog(long timeoutMs) {
        return WindowFinder.findDialog(new GenericTypeMatcher<JDialog>(JDialog.class) {
            @Override
            protected boolean isMatching(JDialog dialog) {
                if (!dialog.isShowing()) return false;
                String title = dialog.getTitle();
                return title != null && (
                        title.equalsIgnoreCase("Database Selection") ||
                        title.toLowerCase().contains("database") ||
                        title.toLowerCase().contains("base de dados") ||
                        title.toLowerCase().contains("select")
                );
            }
        }).withTimeout(timeoutMs).using(robot);
    }

    private void pace(long humanMs) {
        pace(humanMs, 0);
    }

    private void pace(long humanMs, long robotMs) {
        long delay = isHumanPacing ? humanMs : robotMs;
        if (delay > 0) {
            sleep(delay);
        }
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
