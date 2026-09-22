package io.github.kriolos.opos.automation;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

/**
 * Utility helper to capture screenshots during Swing UI automation.
 *
 * <p>Supports both OS-level display capture and direct Swing component rendering.
 * Direct component rendering ({@code component.printAll(g2)}) avoids the solid-black image
 * issue caused by Linux Wayland security restrictions blocking {@code Robot.createScreenCapture()}.
 */
public class ScreenshotHelper {

    private static final Logger LOGGER = Logger.getLogger(ScreenshotHelper.class.getName());

    private final Path outputDir;
    private final Robot robot;

    public ScreenshotHelper(String scenarioName) {
        this.outputDir = Path.of("target", "recordings", scenarioName, "screenshots");
        try {
            Files.createDirectories(outputDir);
        } catch (IOException ignored) {}

        Robot r = null;
        try {
            r = new Robot();
        } catch (AWTException ignored) {}
        this.robot = r;
    }

    /**
     * Captures a Component or Window directly using Swing 2D graphics rendering.
     * This is 100% immune to Wayland security restrictions, headless mode, or obscured windows.
     *
     * @param component the Swing component or Window to capture
     * @param stepName milestone descriptor
     * @return the saved File
     */
    public File captureComponent(Component component, String stepName) {
        if (component == null) {
            return captureScreen(stepName);
        }

        try {
            int width = Math.max(component.getWidth(), 800);
            int height = Math.max(component.getHeight(), 600);

            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = image.createGraphics();

            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            Color bg = component.getBackground() != null ? component.getBackground() : Color.LIGHT_GRAY;
            g2.setColor(bg);
            g2.fillRect(0, 0, width, height);

            // Render on EDT to ensure thread safety
            if (SwingUtilities.isEventDispatchThread()) {
                component.printAll(g2);
            } else {
                try {
                    SwingUtilities.invokeAndWait(() -> component.printAll(g2));
                } catch (Exception e) {
                    component.paint(g2);
                }
            }
            g2.dispose();

            return saveImage(image, stepName);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "[Screenshot] Error capturing component snapshot: {0}", e.getMessage());
            return null;
        }
    }

    /**
     * Captures the active top-level Window (Frame or Dialog).
     *
     * @param stepName milestone descriptor
     * @return the saved File
     */
    public File captureActiveWindow(String stepName) {
        Window active = findActiveWindow();
        if (active != null) {
            return captureComponent(active, stepName);
        }
        return captureScreen(stepName);
    }

    /**
     * Captures the display screen. If the resulting image is completely black
     * (common under Linux Wayland), automatically falls back to rendering the visible Swing window.
     *
     * @param stepName milestone descriptor
     * @return the saved File
     */
    public File captureScreen(String stepName) {
        BufferedImage screenImage = null;

        if (robot != null) {
            try {
                Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
                screenImage = robot.createScreenCapture(screenRect);
            } catch (Exception ignored) {}
        }

        // On Wayland, Robot.createScreenCapture typically returns solid black
        if (screenImage == null || isAllBlack(screenImage)) {
            Window active = findActiveWindow();
            if (active != null) {
                return captureComponent(active, stepName);
            }
        }

        if (screenImage != null) {
            try {
                return saveImage(screenImage, stepName);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "[Screenshot] Error saving screen snapshot: {0}", e.getMessage());
            }
        }
        return null;
    }

    private Window findActiveWindow() {
        for (Window w : Window.getWindows()) {
            if (w.isShowing() && w.isFocused()) {
                return w;
            }
        }
        for (Window w : Window.getWindows()) {
            if (w.isShowing() && (w instanceof Frame || w instanceof Dialog)) {
                return w;
            }
        }
        return null;
    }

    private boolean isAllBlack(BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();
        int[] xs = {w / 4, w / 2, (3 * w) / 4};
        int[] ys = {h / 4, h / 2, (3 * h) / 4};
        for (int x : xs) {
            for (int y : ys) {
                int rgb = img.getRGB(x, y) & 0x00FFFFFF;
                if (rgb != 0) {
                    return false;
                }
            }
        }
        return true;
    }

    private File saveImage(BufferedImage img, String stepName) throws IOException {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS").format(new Date());
        String sanitizedStep = stepName.replaceAll("[^a-zA-Z0-9_-]", "_");
        File file = outputDir.resolve(String.format("%s_%s.png", timestamp, sanitizedStep)).toFile();
        ImageIO.write(img, "PNG", file);
        LOGGER.log(Level.INFO, "[Screenshot] Saved snapshot ({0}x{1}) to: {2}",
                new Object[]{img.getWidth(), img.getHeight(), file.getAbsolutePath()});
        return file;
    }

    public Path getOutputDir() {
        return outputDir;
    }
}
