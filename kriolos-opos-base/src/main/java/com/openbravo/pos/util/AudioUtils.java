package com.openbravo.pos.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * Utility class for handling asynchronous audio playback compatible from Java 8 to Java 26.
 * Compliant with static code analysis (try-with-resources / rule enforcement).
 *
 * @author poolborges
 */
public class AudioUtils {

    private static final Logger LOGGER = Logger.getLogger(AudioUtils.class.getName());
    private static final int BUFFER_SIZE = 4096;

    private AudioUtils() {
    }

    public static void play(String resource) {
        try {
            URL audioURL = AudioUtils.class.getClassLoader().getResource(resource);

            if (audioURL != null) {
                // Call the extracted method to get the fully buffered stream
                AudioInputStream ais = createAudioStream(audioURL);

                Clip oAudio = AudioSystem.getClip();
                oAudio.open(ais);

                // Automatically release OS mixer lines when playback stops
                oAudio.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        try {
                            oAudio.close();
                            ais.close();
                        } catch (Exception ex) {
                            LOGGER.log(Level.FINE, "Error releasing background audio lines", ex);
                        }
                    }
                });

                oAudio.start();

            } else {
                LOGGER.warning("Audio resource not found: " + resource);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception playing audio from classpath resource: " + resource, e);
        }
    }

    /**
     * Extracted method that reads the resource into memory and returns a safe AudioInputStream.
     * All physical OS file descriptors are closed within the try-with-resources block.
     */
    private static AudioInputStream createAudioStream(URL audioURL) throws IOException, UnsupportedAudioFileException {
        byte[] audioBuffer;

        // Try-with-resources guarantees physical streams are closed immediately
        try (InputStream audioStream = audioURL.openStream();
             BufferedInputStream bis = new BufferedInputStream(audioStream);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = bis.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            audioBuffer = baos.toByteArray();
        }

        // Return the stream backed by the in-memory byte array
        ByteArrayInputStream bais = new ByteArrayInputStream(audioBuffer);
        return AudioSystem.getAudioInputStream(bais);
    }
}
