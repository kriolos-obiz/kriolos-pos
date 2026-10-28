package com.openbravo.ui.animation;

import javax.swing.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

/**
 * Animated rolling number label for real-time totals, prices, and quantities.
 * <p>
 * Instead of jumping abruptly between values, the display smoothly interpolates
 * between numeric states using timing framework easing curves.
 * Ideal for POS ticket subtotals, cash drawer change calculations, and loyalty points.
 * Fully configurable, free of magic values, and Look-and-Feel agnostic.
 * </p>
 *
 * @author KriolOS
 * @since 1.0.0
 */
public class RollingNumberLabel extends JLabel {

    /** Default duration of the rolling interpolation animation in milliseconds. */
    public static final int DEFAULT_ANIMATION_DURATION_MS = 500;

    /** Minimum supported animation duration in milliseconds. */
    public static final int MIN_ANIMATION_DURATION_MS = 50;

    private static final String EMPTY_STRING = "";

    private double currentValue = 0.0;
    private double targetValue = 0.0;
    private int decimalPlaces;
    private String prefix = EMPTY_STRING;
    private String suffix = EMPTY_STRING;
    private int animationDurationMs = DEFAULT_ANIMATION_DURATION_MS;
    private NativeAnimator animator;
    
    private NumberFormat formatter;
    private final Locale locale;

    /**
     * Constructs a rolling number label initialized at zero using the default system Locale.
     */
    public RollingNumberLabel() {
        this(0.0);
    }

    /**
     * Constructs a rolling number label with an initial numeric value using the default system Locale.
     *
     * @param initialValue the starting number
     */
    public RollingNumberLabel(double initialValue) {
        this(initialValue, EMPTY_STRING, EMPTY_STRING, Locale.getDefault());
    }

    /**
     * Constructs a rolling number label with initial value, currency prefix, and suffix using the default system Locale.
     *
     * @param initialValue the starting number
     * @param prefix text placed before the number (e.g. "$", "CVE ")
     * @param suffix text placed after the number (e.g. " CVE", " pts")
     */
    public RollingNumberLabel(double initialValue, String prefix, String suffix) {
        this(initialValue, prefix, suffix, Locale.getDefault());
    }

    /**
     * Constructs a rolling number label with full localization control.
     *
     * @param initialValue the starting number
     * @param prefix text placed before the number
     * @param suffix text placed after the number
     * @param locale the specific locale to drive number formatting and fraction precision
     */
    public RollingNumberLabel(double initialValue, String prefix, String suffix, Locale locale) {
        this.currentValue = initialValue;
        this.targetValue = initialValue;
        this.prefix = (prefix != null) ? prefix : EMPTY_STRING;
        this.suffix = (suffix != null) ? suffix : EMPTY_STRING;
        this.locale = (locale != null) ? locale : Locale.getDefault();
        
        // Define as casas decimais de forma inteligente antes de inicializar o formatador
        this.decimalPlaces = inferDefaultDecimalPlaces();
        
        updateFormatter();
        updateTextDisplay();
    }

    private int inferDefaultDecimalPlaces() {
        try {
            // Tenta isolar um código ISO de 3 letras do prefixo ou sufixo (ex: "CVE", "USD")
            String cleanCurrency = prefix.trim().length() == 3 ? prefix.trim() : suffix.trim();
            return Currency.getInstance(cleanCurrency).getDefaultFractionDigits();
        } catch (Exception e) {
            try {
                // Caso falhe, tenta herdar a precisão da moeda padrão do Locale atribuído
                return Currency.getInstance(this.locale).getDefaultFractionDigits();
            } catch (Exception ex) {
                // Fallback seguro caso o locale não possua moeda padrão (ex: Locale.ROOT)
                return 2;
            }
        }
    }

    private void updateFormatter() {
        this.formatter = NumberFormat.getNumberInstance(this.locale);
        this.formatter.setMinimumFractionDigits(decimalPlaces);
        this.formatter.setMaximumFractionDigits(decimalPlaces);
        this.formatter.setGroupingUsed(true); // Ativa separadores de milhares regionais automaticamente
    }

    private void updateTextDisplay() {
        setText(prefix + formatter.format(currentValue) + suffix);
    }

    /**
     * Overrides and sets the number of decimal places to format and display.
     *
     * @param decimalPlaces decimal precision (e.g. 2 for currency, 0 for integers)
     * @return this label instance for fluent configuration
     */
    public RollingNumberLabel setDecimalPlaces(int decimalPlaces) {
        this.decimalPlaces = Math.max(0, decimalPlaces);
        updateFormatter();
        updateTextDisplay();
        return this;
    }

    /**
     * Sets the text prefix prepended before the number.
     *
     * @param prefix the prefix string
     * @return this label instance
     */
    public RollingNumberLabel setPrefix(String prefix) {
        this.prefix = (prefix != null) ? prefix : EMPTY_STRING;
        updateTextDisplay();
        return this;
    }

    /**
     * Sets the text suffix appended after the number.
     *
     * @param suffix the suffix string
     * @return this label instance
     */
    public RollingNumberLabel setSuffix(String suffix) {
        this.suffix = (suffix != null) ? suffix : EMPTY_STRING;
        updateTextDisplay();
        return this;
    }

    /**
     * Sets the animation roll duration in milliseconds.
     *
     * @param durationMs duration in milliseconds (default: 500ms)
     * @return this label instance
     */
    public RollingNumberLabel setAnimationDurationMs(int durationMs) {
        this.animationDurationMs = Math.max(MIN_ANIMATION_DURATION_MS, durationMs);
        return this;
    }

    /**
     * Smoothly rolls the display from the current value to the new target value.
     *
     * @param target the target numeric value
     */
    public void rollTo(double target) {
        if (Double.compare(this.currentValue, target) == 0) {
            return;
        }

        if (animator != null && animator.isRunning()) {
            animator.stop();
        }

        final double startVal = this.currentValue;
        this.targetValue = target;

        animator = NativeAnimator.createWithInterpolator(
                animationDurationMs,
                fraction -> {
                    this.currentValue = startVal + (targetValue - startVal) * fraction;
                    updateTextDisplay();
                },
                NativeAnimator.EASE_IN_OUT
        );
        animator.start();
    }

    /**
     * Convenience method to animate to a {@link BigDecimal} value.
     *
     * @param target the target amount
     */
    public void rollTo(BigDecimal target) {
        if (target != null) {
            rollTo(target.setScale(decimalPlaces, RoundingMode.HALF_UP).doubleValue());
        }
    }

    /**
     * Instantly sets the numeric value without animation.
     *
     * @param value the direct value
     */
    public void setValueImmediate(double value) {
        if (animator != null && animator.isRunning()) {
            animator.stop();
        }
        this.currentValue = value;
        this.targetValue = value;
        updateTextDisplay();
    }

    /**
     * Returns the current value being displayed.
     *
     * @return numeric value
     */
    public double getValue() {
        return currentValue;
    }

    /**
     * Returns the final target value the label is rolling towards.
     *
     * @return target value
     */
    public double getTargetValue() {
        return targetValue;
    }
}
