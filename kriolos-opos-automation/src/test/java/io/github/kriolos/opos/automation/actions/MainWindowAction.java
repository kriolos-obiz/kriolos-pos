package io.github.kriolos.opos.automation.actions;

import io.github.kriolos.opos.automation.ScreenshotHelper;
import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.core.Robot;
import org.assertj.swing.finder.WindowFinder;
import org.assertj.swing.fixture.FrameFixture;

import javax.swing.JFrame;
import javax.swing.WindowConstants;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.awt.Frame;
import java.awt.event.WindowListener;

/**
 * Reusable Action Driver for controlling the Main Application Window (JRootFrame).
 */
public class MainWindowAction {

    private static final Logger LOGGER = Logger.getLogger(MainWindowAction.class.getName());

    private final Robot robot;
    private final ScreenshotHelper screenshotHelper;
    private final boolean isHumanPacing;
    private FrameFixture windowFixture;

    public MainWindowAction(Robot robot, ScreenshotHelper screenshotHelper, boolean isHumanPacing) {
        this.robot = robot;
        this.screenshotHelper = screenshotHelper;
        this.isHumanPacing = isHumanPacing;
    }

    /**
     * Waits for and attaches to the main JFrame.
     */
    public FrameFixture attach(long timeoutMs) {
        LOGGER.log(Level.INFO, "Waiting for main JRootFrame to appear...");
        windowFixture = WindowFinder.findFrame(new GenericTypeMatcher<JFrame>(JFrame.class) {
            @Override
            protected boolean isMatching(JFrame frame) {
                return frame.isShowing() && frame.getTitle() != null && !frame.getTitle().isBlank();
            }
        }).withTimeout(timeoutMs).using(robot);

        removeSystemExitListeners(windowFixture.target());
        LOGGER.log(Level.INFO, "Successfully attached to main POS window: {0}", windowFixture.target().getTitle());
        return windowFixture;
    }

    /**
     * Configures the window display mode (maximize or keep windowed).
     */
    public void configureWindowMode(boolean maximize) {
        if (windowFixture == null) return;
        if (maximize) {
            LOGGER.log(Level.INFO, "Maximizing main application window...");
            org.assertj.swing.edt.GuiActionRunner.execute(() -> {
                Frame frame = windowFixture.target();
                if (frame != null) {
                    frame.setExtendedState(Frame.MAXIMIZED_BOTH);
                    frame.revalidate();
                    frame.repaint();
                }
            });
            pace(1200, 100);
        } else {
            LOGGER.log(Level.INFO, "Keeping windowed mode...");
            pace(800, 50);
        }
    }

    /**
     * Takes a screenshot of the main application window.
     */
    public void capture(String stepName) {
        if (windowFixture != null && windowFixture.target() != null) {
            try {
                screenshotHelper.captureComponent(windowFixture.target(), stepName);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "[MainWindowAction] Could not capture screenshot: {0}", e.getMessage());
            }
        }
    }

    public FrameFixture getWindowFixture() {
        return windowFixture;
    }

    public Frame getTargetFrame() {
        return windowFixture != null ? windowFixture.target() : null;
    }

    public void removeSystemExitListeners(java.awt.Window frame) {
        if (frame == null) return;
        org.assertj.swing.edt.GuiActionRunner.execute(() -> {
            for (WindowListener wl : frame.getWindowListeners()) {
                frame.removeWindowListener(wl);
            }
            if (frame instanceof JFrame jFrame) {
                jFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            }
        });
    }

    public void cleanUp() {
        if (windowFixture != null) {
            try {
                removeSystemExitListeners(windowFixture.target());
                windowFixture.cleanUp();
            } catch (Exception ignored) {
            }
        }
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
