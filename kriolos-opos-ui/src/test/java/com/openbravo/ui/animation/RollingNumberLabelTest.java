package com.openbravo.ui.animation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import javax.swing.SwingUtilities;
import java.math.BigDecimal;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link RollingNumberLabel}.
 *
 * @author KriolOS
 */
@DisplayName("RollingNumberLabel Unit Tests")
class RollingNumberLabelTest {

    @ParameterizedTest(name = "Locale: {0}, Prefix: {1} -> Expected Decimals/Format match")
    @CsvSource({
        "en-US, 'USD ', '12,500.50'", // North America (US)
        "fr-FR, 'EUR ', '12500,50'", // Europe (France)
        "pt-CV, 'CVE ', '12500,50'", // West Africa (Cape Verde - 2 decimals)
        "pt-PT, 'EUR ', '12500,50'", // Europe (Portugal)
        "ja-JP, 'JPY ', '12,500'", // Asia (Japan - 0 decimals)
        "ar-AE, 'AED ', '12,500.50'", // Middle East (UAE)
        "en-ZA, 'ZAR ', '12500,50'", // Southern Africa (South Africa)
        "en-NG, 'NGN ', '12,500.50'", // West Africa (Nigeria)
        "fr-CI, 'XOF ', '12500'" // West Africa (Ivory Coast/CFA - 0 decimals naturally via ISO 4217)
    })
    @DisplayName("Verify cross-platform localization and currency fraction deduction including Africa")
    void testParameterizedLocales(String localeTag, String prefix, String expectedContains) {
        Locale locale = Locale.forLanguageTag(localeTag);
        RollingNumberLabel label = new RollingNumberLabel(12500.50, prefix, "", locale);

        assertTrue(label.getText().startsWith(prefix));

        // Normalizes all variants of spaces (\u00A0, \u202F) to preserve WORA cross-platform compliance
        String normalizedText = label.getText().replaceAll("[\\s\\u00A0\\u202F]", "");
        String normalizedExpected = expectedContains.replaceAll(" ", "");

        boolean matches = normalizedText.contains(normalizedExpected);
        if (!matches && "ar-AE".equals(localeTag)) {
            // Under CLDR (Linux/macOS/Temurin), ar-AE formats with Eastern Arabic-Indic numerals (١٢٬٥٠٠٫٥٠)
            // whereas under Windows/COMPAT environments, it may format with ASCII Latin numerals (12,500.50).
            matches = normalizedText.contains("١٢٬٥٠٠٫٥٠");
        }

        assertTrue(matches,
                String.format("Failed for Locale %s. Got: [%s], Expected to contain: [%s]",
                        localeTag, normalizedText, normalizedExpected));
    }

    @Test
    @DisplayName("Verify timing framework animation interpolation safely via EDT")
    void testAnimationInterpolation() throws Exception {
        // Initialize the Swing component safely on the Event Dispatch Thread
        final RollingNumberLabel[] labelHolder = new RollingNumberLabel[1];
        SwingUtilities.invokeAndWait(() -> {
            labelHolder[0] = new RollingNumberLabel(0.0, "$", "", Locale.US);
            labelHolder[0].setAnimationDurationMs(150); // Short duration to keep tests fast
        });

        RollingNumberLabel label = labelHolder[0];

        // Trigger the rolling animation which communicates with the Timing Framework in background
        SwingUtilities.invokeAndWait(() -> label.rollTo(100.0));

        // The target changes instantly, but the displayed current value increments frame-by-frame
        assertEquals(100.0, label.getTargetValue(), 0.001);

        // Loop to capture middle/interpolated execution frames over the EDT event queue
        long startTime = System.currentTimeMillis();
        boolean capturedMiddleState = false;

        while (System.currentTimeMillis() - startTime < 800) {
            double current = label.getValue();
            if (current > 5.0 && current < 95.0) {
                capturedMiddleState = true;
                // Validate that the label text is changing dynamically during the animation loop
                String text = label.getText();
                assertTrue(text.startsWith("$"));
                assertNotEquals("$0.00", text);
                assertNotEquals("$100.00", text);
                break;
            }
            // Small backoff interval to yield and allow the Timing Framework thread to push frames to the EDT
            Thread.sleep(15);
        }

        assertTrue(capturedMiddleState, "The animation jumped directly to the final state without emitting middle frames.");

        // Wait until the final easing curve phase stabilizes completely
        long endWait = System.currentTimeMillis();
        while (label.getValue() < 100.0 && (System.currentTimeMillis() - endWait < 800)) {
            Thread.sleep(20);
        }

        // Final deterministic state evaluation
        assertEquals(100.0, label.getValue(), 0.001);
        assertEquals("$100.00", label.getText());
    }

    @Test
    @DisplayName("Verify default initialization values and formatting")
    void testInitialization() {
        RollingNumberLabel label = new RollingNumberLabel(1500.50, "CVE ", "", Locale.US);

        assertEquals(1500.50, label.getValue(), 0.001);
        assertEquals(1500.50, label.getTargetValue(), 0.001);
        assertTrue(label.getText().startsWith("CVE "));
        assertTrue(label.getText().contains("1,500.50") || label.getText().contains("1.500,50"));
    }

    @Test
    @DisplayName("Verify setValueImmediate sets target and updates text synchronously")
    void testSetValueImmediate() {
        RollingNumberLabel label = new RollingNumberLabel(0.0, "", "", Locale.US);
        label.setPrefix("$");
        label.setValueImmediate(250.75);

        assertEquals(250.75, label.getValue(), 0.001);
        assertEquals(250.75, label.getTargetValue(), 0.001);
        assertTrue(label.getText().contains("250.75") || label.getText().contains("250,75"));
    }

    @Test
    @DisplayName("Verify rollTo updates target value correctly")
    void testRollToTarget() {
        RollingNumberLabel label = new RollingNumberLabel(100.0);
        label.rollTo(500.0);

        assertEquals(500.0, label.getTargetValue(), 0.001);
    }

    @Test
    @DisplayName("Verify BigDecimal overload support")
    void testBigDecimalOverload() {
        RollingNumberLabel label = new RollingNumberLabel(0.0);
        label.rollTo(new BigDecimal("1234.56"));

        assertEquals(1234.56, label.getTargetValue(), 0.001);
    }

    @Test
    @DisplayName("Verify decimal precision changes")
    void testDecimalPrecision() {
        RollingNumberLabel label = new RollingNumberLabel(100.0, "", "", Locale.US);
        label.setDecimalPlaces(0);
        label.setValueImmediate(100.0);

        assertFalse(label.getText().contains(".00"));
        assertFalse(label.getText().contains(",00"));
    }
}
