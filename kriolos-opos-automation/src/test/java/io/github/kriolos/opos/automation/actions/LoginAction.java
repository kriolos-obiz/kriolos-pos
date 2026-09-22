package io.github.kriolos.opos.automation.actions;

import io.github.kriolos.opos.automation.ScreenshotHelper;
import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.core.Robot;
import org.assertj.swing.finder.WindowFinder;
import org.assertj.swing.fixture.DialogFixture;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JButtonFixture;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JDialog;

/**
 * Reusable Action Driver for POS Operator Authentication and Login.
 */
public class LoginAction {

    private static final Logger LOGGER = Logger.getLogger(LoginAction.class.getName());

    private final Robot robot;
    private final ScreenshotHelper screenshotHelper;
    private final boolean isHumanPacing;

    public LoginAction(Robot robot, ScreenshotHelper screenshotHelper, boolean isHumanPacing) {
        this.robot = robot;
        this.screenshotHelper = screenshotHelper;
        this.isHumanPacing = isHumanPacing;
    }

    /**
     * Executes the login sequence: selects the operator button, fills in password if prompted,
     * and submits.
     */
    public void login(FrameFixture window, String targetOperator, String password) {
        LOGGER.log(Level.INFO, "[LoginAction] Attempting login with operator: {0}", targetOperator);
        pace(1200, 200);

        clickUserButton(window, targetOperator);
        handlePasswordDialogIfPresent(password);

        // Wait for authenticated panel to activate JPrincipalApp
        long deadline = System.currentTimeMillis() + 6000;
        while (System.currentTimeMillis() < deadline) {
            try {
                com.openbravo.pos.forms.JPrincipalApp principal = robot.finder().findByType(window.target(), com.openbravo.pos.forms.JPrincipalApp.class);
                if (principal != null && principal.isShowing()) {
                    break;
                }
            } catch (Exception ignored) {}
            try {
                Thread.sleep(150);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        pace(1500, 50);
        screenshotHelper.captureComponent(window.target(), "02_user_logged_in");
        LOGGER.log(Level.INFO, "[LoginAction] Operator login completed.");
    }

    /**
     * Clicks the button matching the target operator username.
     */
    public void clickUserButton(FrameFixture window, String targetOperator) {
        try {
            JButtonFixture userButton = window.button(new GenericTypeMatcher<JButton>(JButton.class) {
                @Override
                protected boolean isMatching(JButton btn) {
                    if (!btn.isShowing() || btn.getText() == null) return false;
                    String text = btn.getText().trim().toLowerCase();
                    String target = targetOperator.trim().toLowerCase();
                    return text.equals(target) ||
                            (target.startsWith("admin") && text.equals("admin")) ||
                            (target.startsWith("manage") && text.equals("manager")) ||
                            (target.startsWith("emp") && text.equals("empl"));
                }
            });
            JButton btn = userButton.target();
            javax.swing.SwingUtilities.invokeLater(btn::doClick);
            LOGGER.log(Level.INFO, "[LoginAction] Clicked operator button: {0}", targetOperator);
        } catch (Exception e) {
            LOGGER.log(Level.INFO, "[LoginAction] Specific operator button not found, falling back to first operator button: {0}", e.getMessage());
            try {
                JButtonFixture anyUserBtn = window.button(new GenericTypeMatcher<JButton>(JButton.class) {
                    @Override
                    protected boolean isMatching(JButton btn) {
                        if (!btn.isShowing() || btn.getText() == null) return false;
                        String t = btn.getText().trim();
                        return !t.isBlank() && !t.equalsIgnoreCase("Sair") && !t.equalsIgnoreCase("Exit");
                    }
                });
                JButton btn = anyUserBtn.target();
                javax.swing.SwingUtilities.invokeLater(btn::doClick);
            } catch (Exception ex2) {
                LOGGER.log(Level.WARNING, "[LoginAction] Could not click user button: {0}", ex2.getMessage());
            }
        }
    }

    /**
     * Checks for a password prompt dialog, inputs password, and submits.
     */
    public boolean handlePasswordDialogIfPresent(String password) {
        try {
            DialogFixture pwdDialog = WindowFinder.findDialog(new GenericTypeMatcher<JDialog>(JDialog.class) {
                @Override
                protected boolean isMatching(JDialog d) {
                    if (!d.isShowing()) return false;
                    String title = d.getTitle();
                    return title != null && (
                            title.toLowerCase().contains("password") ||
                            title.toLowerCase().contains("palavra") ||
                            title.toLowerCase().contains("senha")
                    );
                }
            }).withTimeout(2500).using(robot);

            LOGGER.log(Level.INFO, "[LoginAction] Password dialog detected: {0}", pwdDialog.target().getTitle());
            screenshotHelper.captureComponent(pwdDialog.target(), "02b_password_dialog");
            pace(800);

            if (password != null && !password.isBlank()) {
                try {
                    com.openbravo.editor.JEditorText editor = robot.finder().findByType(pwdDialog.target(), com.openbravo.editor.JEditorText.class);
                    if (editor != null) {
                        org.assertj.swing.edt.GuiActionRunner.execute(() -> editor.setText(password));
                        LOGGER.log(Level.INFO, "[LoginAction] Injected password into editor.");
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.INFO, "[LoginAction] Note injecting password: {0}", e.getMessage());
                }
                pace(600);
            }
            pace(500);

            JButton okBtn = pwdDialog.button(new GenericTypeMatcher<JButton>(JButton.class) {
                @Override
                protected boolean isMatching(JButton b) {
                    if (!b.isShowing()) return false;
                    String t = b.getText();
                    if (t != null && !t.isBlank() && (t.equalsIgnoreCase("OK") || t.equalsIgnoreCase("Entrar"))) {
                        return true;
                    }
                    if (b.getIcon() != null && b.getIcon().toString().contains("ok")) {
                        return true;
                    }
                    return "jcmdOK".equals(b.getName());
                }
            }).target();
            javax.swing.SwingUtilities.invokeLater(okBtn::doClick);
            LOGGER.log(Level.INFO, "[LoginAction] Submitted password dialog.");
            pace(800, 50);
            return true;
        } catch (Exception ignored) {
            // No password was required
            return false;
        }
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
