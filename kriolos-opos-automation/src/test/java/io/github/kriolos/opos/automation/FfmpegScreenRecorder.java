package io.github.kriolos.opos.automation;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller to record the Swing UI test session into a crisp MP4 video.
 *
 * <p>Uses an in-memory Java frame-capture loop piped to an FFmpeg process via standard input.
 * To ensure modal dialogs (such as the Database Selection dialog) and main windows are never
 * clipped or displayed "too big", this recorder dynamically centers and scales all Swing components
 * onto the standard 720p HD video canvas.
 */
public class FfmpegScreenRecorder {

    private static final Logger LOGGER = Logger.getLogger(FfmpegScreenRecorder.class.getName());

    private final File outputFile;
    private final int width;
    private final int height;
    private final int fps;

    private Process ffmpegProcess;
    private OutputStream ffmpegStdin;
    private Thread captureThread;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    public FfmpegScreenRecorder(File outputFile) {
        this(outputFile, 1280, 720, 12);
    }

    public FfmpegScreenRecorder(File outputFile, int width, int height, int fps) {
        this.outputFile = outputFile;
        this.width = (width % 2 == 0) ? width : width + 1; // H.264 requires even dimensions
        this.height = (height % 2 == 0) ? height : height + 1;
        this.fps = fps > 0 ? fps : 12;
    }

    /**
     * Starts the FFmpeg video recording process and the background frame capture loop.
     */
    public void start() throws IOException {
        if (outputFile.getParentFile() != null) {
            outputFile.getParentFile().mkdirs();
        }

        ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg", "-y",
                "-f", "rawvideo",
                "-pix_fmt", "bgr24",
                "-s", width + "x" + height,
                "-r", String.valueOf(fps),
                "-i", "-",
                "-c:v", "libx264",
                "-preset", "ultrafast",
                "-pix_fmt", "yuv420p",
                outputFile.getAbsolutePath()
        );
        pb.redirectErrorStream(true);

        try {
            ffmpegProcess = pb.start();
            ffmpegStdin = ffmpegProcess.getOutputStream();
            isRunning.set(true);

            captureThread = new Thread(this::runCaptureLoop, "FfmpegScreenRecorder-Loop");
            captureThread.setDaemon(true);
            captureThread.start();

            LOGGER.log(Level.INFO, "[ScreenRecorder] In-memory Swing frame recording started (Canvas: {0}x{1} @ {2} fps) -> {3}",
                    new Object[]{width, height, fps, outputFile.getAbsolutePath()});
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "[ScreenRecorder] Failed to start FFmpeg: " + e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Backward-compatible start method.
     */
    public void start(String display, int width, int height, int fps) throws IOException {
        start();
    }

    private void runCaptureLoop() {
        BufferedImage frameBuffer = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        byte[] frameData = ((DataBufferByte) frameBuffer.getRaster().getDataBuffer()).getData();
        long frameDelayMs = 1000 / fps;

        while (isRunning.get()) {
            long loopStart = System.currentTimeMillis();

            Graphics2D g2 = frameBuffer.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            // Neutral dark studio background
            g2.setColor(new Color(32, 33, 36));
            g2.fillRect(0, 0, width, height);

            // 1. Check for visible main Frame
            Frame mainFrame = null;
            for (Window w : Window.getWindows()) {
                if (w.isShowing() && w instanceof Frame && w.getWidth() > 0 && w.getHeight() > 0) {
                    mainFrame = (Frame) w;
                    break;
                }
            }

            if (mainFrame != null) {
                // Scale and center main frame to fit within video canvas
                int fw = mainFrame.getWidth();
                int fh = mainFrame.getHeight();
                double scale = Math.min((double) width / fw, (double) height / fh);

                int drawW = (int) Math.round(fw * scale);
                int drawH = (int) Math.round(fh * scale);
                int targetX = (width - drawW) / 2;
                int targetY = (height - drawH) / 2;

                Graphics2D frameG2 = (Graphics2D) g2.create();
                frameG2.translate(targetX, targetY);
                frameG2.scale(scale, scale);
                try {
                    mainFrame.printAll(frameG2);
                } catch (Exception ignored) {}
                frameG2.dispose();

                // 2. Paint any visible Dialogs in their relative position on top of the main frame
                for (Window w : Window.getWindows()) {
                    if (w.isShowing() && w instanceof Dialog && w.getWidth() > 0 && w.getHeight() > 0) {
                        try {
                            Point relPos;
                            try {
                                Point dlgScreen = w.getLocationOnScreen();
                                Point frameScreen = mainFrame.getLocationOnScreen();
                                relPos = new Point(dlgScreen.x - frameScreen.x, dlgScreen.y - frameScreen.y);
                            } catch (Exception e) {
                                relPos = new Point((fw - w.getWidth()) / 2, (fh - w.getHeight()) / 2);
                            }

                            int dlgCanvasX = targetX + (int) Math.round(relPos.x * scale);
                            int dlgCanvasY = targetY + (int) Math.round(relPos.y * scale);

                            Graphics2D dlgG2 = (Graphics2D) g2.create();
                            dlgG2.translate(dlgCanvasX, dlgCanvasY);
                            dlgG2.scale(scale, scale);
                            w.printAll(dlgG2);
                            dlgG2.dispose();
                        } catch (Exception ignored) {}
                    }
                }
            } else {
                // 3. Main frame not yet visible (e.g. initial Database Selection dialog)
                // Center the active dialog cleanly in the middle of the video canvas
                for (Window w : Window.getWindows()) {
                    if (w.isShowing() && w instanceof Dialog && w.getWidth() > 0 && w.getHeight() > 0) {
                        int dw = w.getWidth();
                        int dh = w.getHeight();

                        // Scale down if dialog exceeds canvas, otherwise maintain 1:1 scale
                        double scale = Math.min(1.0, Math.min((double) (width - 60) / dw, (double) (height - 60) / dh));
                        int drawW = (int) Math.round(dw * scale);
                        int drawH = (int) Math.round(dh * scale);
                        int targetX = (width - drawW) / 2;
                        int targetY = (height - drawH) / 2;

                        Graphics2D dlgG2 = (Graphics2D) g2.create();
                        dlgG2.translate(targetX, targetY);
                        dlgG2.scale(scale, scale);
                        try {
                            w.printAll(dlgG2);
                        } catch (Exception ignored) {}
                        dlgG2.dispose();
                        break;
                    }
                }
            }

            g2.dispose();

            // Write raw video frame to FFmpeg stdin
            try {
                ffmpegStdin.write(frameData);
                ffmpegStdin.flush();
            } catch (IOException e) {
                break;
            }

            long elapsed = System.currentTimeMillis() - loopStart;
            long sleepTime = frameDelayMs - elapsed;
            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    /**
     * Stops the screen recording gracefully, flushing video frames to disk.
     */
    public void stop() {
        if (!isRunning.compareAndSet(true, false)) {
            return;
        }

        if (captureThread != null) {
            captureThread.interrupt();
        }

        if (ffmpegStdin != null) {
            try {
                ffmpegStdin.flush();
                ffmpegStdin.close(); // Signals EOF to FFmpeg
            } catch (IOException ignored) {}
        }

        if (ffmpegProcess != null && ffmpegProcess.isAlive()) {
            try {
                boolean exited = ffmpegProcess.waitFor(5, TimeUnit.SECONDS);
                if (!exited) {
                    ffmpegProcess.destroyForcibly();
                }
            } catch (InterruptedException e) {
                ffmpegProcess.destroyForcibly();
                Thread.currentThread().interrupt();
            }
            LOGGER.log(Level.INFO, "[ScreenRecorder] Video recording finalized and saved to: {0} ({1} bytes)",
                    new Object[]{outputFile.getAbsolutePath(), outputFile.length()});
        }
    }

    public File getOutputFile() {
        return outputFile;
    }
}
