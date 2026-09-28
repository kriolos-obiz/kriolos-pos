package io.github.kriolos.opos.automation;

import java.io.File;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility to convert recorded automation MP4 videos or frame sequences into high-quality,
 * lightweight animated GIFs using FFmpeg's two-pass palettegen and paletteuse filters.
 */
public class FfmpegScreenGifGenerator {

    private static final Logger LOGGER = Logger.getLogger(FfmpegScreenGifGenerator.class.getName());

    public static final int DEFAULT_FPS = 10;
    public static final int DEFAULT_WIDTH = 800;

    private FfmpegScreenGifGenerator() {
    }

    /**
     * Generates an animated GIF from a source MP4 video with default settings (10 fps, 800px width).
     *
     * @param videoInput Source MP4 video file
     * @param gifOutput  Destination GIF file
     * @return true if generation succeeded, false otherwise
     */
    public static boolean generateGif(File videoInput, File gifOutput) {
        return generateGif(videoInput, gifOutput, DEFAULT_FPS, DEFAULT_WIDTH);
    }

    /**
     * Generates an animated GIF from a source MP4 video.
     *
     * @param videoInput Source MP4 video file
     * @param gifOutput  Destination GIF file
     * @param fps        Frames per second for output GIF
     * @param width      Target width in pixels (height is computed proportionally)
     * @return true if generation succeeded, false otherwise
     */
    public static boolean generateGif(File videoInput, File gifOutput, int fps, int width) {
        if (videoInput == null || !videoInput.exists()) {
            LOGGER.log(Level.WARNING, "[GifGenerator] Source video does not exist: {0}", videoInput);
            return false;
        }

        if (gifOutput == null) {
            LOGGER.log(Level.WARNING, "[GifGenerator] Output file is null");
            return false;
        }

        File parentDir = gifOutput.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        long start = System.currentTimeMillis();
        String filter = String.format("fps=%d,scale=%d:-1:flags=lanczos,split[s0][s1];[s0]palettegen=max_colors=128[p];[s1][p]paletteuse=dither=bayer",
                fps, width);

        ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg",
                "-y",
                "-i", videoInput.getAbsolutePath(),
                "-vf", filter,
                gifOutput.getAbsolutePath()
        );
        pb.redirectErrorStream(true);

        try {
            Process process = pb.start();
            boolean finished = process.waitFor(60, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                LOGGER.log(Level.WARNING, "[GifGenerator] FFmpeg process timed out after 60s");
                return false;
            }

            int exitCode = process.exitValue();
            if (exitCode == 0 && gifOutput.exists()) {
                long duration = System.currentTimeMillis() - start;
                LOGGER.log(Level.INFO, "[GifGenerator] Generated animated GIF in {0}ms: {1} ({2} bytes)",
                        new Object[]{duration, gifOutput.getAbsolutePath(), gifOutput.length()});
                return true;
            } else {
                LOGGER.log(Level.WARNING, "[GifGenerator] FFmpeg failed with exit code: {0}", exitCode);
                return false;
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "[GifGenerator] Error generating GIF: {0}", e.getMessage());
            return false;
        }
    }

    /**
     * Generates an animated GIF from an ordered list of screenshot image files using FFmpeg's concat demuxer
     * and two-pass palettegen/paletteuse filters.
     *
     * @param imageFiles Ordered list of PNG/JPEG image files
     * @param gifOutput Target GIF file
     * @param durationPerSlideSec Duration each slide is displayed in seconds (e.g. 2.0s)
     * @param targetWidth Output width in pixels (e.g. 1200), keeping aspect ratio
     * @return true if successful, false otherwise
     */
    public static boolean generateGifFromImages(java.util.List<File> imageFiles, File gifOutput, double durationPerSlideSec, int targetWidth) {
        if (imageFiles == null || imageFiles.isEmpty()) {
            LOGGER.log(Level.WARNING, "[GifGenerator] No images provided for GIF generation");
            return false;
        }
        if (gifOutput == null) {
            LOGGER.log(Level.WARNING, "[GifGenerator] Output GIF file is null");
            return false;
        }

        File parentDir = gifOutput.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        try {
            File concatFile = File.createTempFile("ffmpeg_concat_", ".txt");
            concatFile.deleteOnExit();

            try (java.io.PrintWriter pw = new java.io.PrintWriter(concatFile, java.nio.charset.StandardCharsets.UTF_8)) {
                for (File img : imageFiles) {
                    pw.println("file '" + img.getAbsolutePath() + "'");
                    pw.println("duration " + durationPerSlideSec);
                }
                // FFmpeg concat demuxer requires repeating the last file so its duration is honored
                File last = imageFiles.get(imageFiles.size() - 1);
                pw.println("file '" + last.getAbsolutePath() + "'");
            }

            long start = System.currentTimeMillis();
            String filter = String.format("fps=10,scale=%d:-1:flags=lanczos,split[s0][s1];[s0]palettegen=max_colors=256:stats_mode=diff[p];[s1][p]paletteuse=dither=bayer",
                    targetWidth);

            ProcessBuilder pb = new ProcessBuilder(
                    "ffmpeg",
                    "-y",
                    "-f", "concat",
                    "-safe", "0",
                    "-i", concatFile.getAbsolutePath(),
                    "-vf", filter,
                    "-loop", "0",
                    gifOutput.getAbsolutePath()
            );
            pb.redirectErrorStream(true);

            Process process = pb.start();
            boolean finished = process.waitFor(90, TimeUnit.SECONDS);
            concatFile.delete();

            if (!finished) {
                process.destroyForcibly();
                LOGGER.log(Level.WARNING, "[GifGenerator] FFmpeg process timed out after 90s");
                return false;
            }

            int exitCode = process.exitValue();
            if (exitCode == 0 && gifOutput.exists()) {
                long duration = System.currentTimeMillis() - start;
                LOGGER.log(Level.INFO, "[GifGenerator] Generated animated GIF from {0} images in {1}ms: {2} ({3} bytes)",
                        new Object[]{imageFiles.size(), duration, gifOutput.getAbsolutePath(), gifOutput.length()});
                return true;
            } else {
                LOGGER.log(Level.WARNING, "[GifGenerator] FFmpeg failed with exit code: {0}", exitCode);
                return false;
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "[GifGenerator] Error generating GIF from images: {0}", e.getMessage());
            return false;
        }
    }

    /**
     * Helper to generate a GIF for a scenario name from target/recordings/{scenarioName}/session.mp4.
     *
     * @param scenarioName Name of the scenario
     * @return File referencing the generated session.gif or null if unsuccessful
     */
    public static File generateForScenario(String scenarioName) {
        File sessionMp4 = new File("target/recordings/" + scenarioName + "/session.mp4");
        if (!sessionMp4.exists()) {
            return null;
        }
        File sessionGif = new File("target/recordings/" + scenarioName + "/session.gif");
        boolean ok = generateGif(sessionMp4, sessionGif);
        return ok ? sessionGif : null;
    }
}

