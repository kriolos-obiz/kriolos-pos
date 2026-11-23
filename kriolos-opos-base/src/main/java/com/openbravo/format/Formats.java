//    KriolOS POS
//    Copyright (c) 2019-2023 KriolOS
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program.  If not, see <http://www.gnu.org/licenses/>.
package com.openbravo.format;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import com.openbravo.basic.BasicException;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

/**
 *
 * @author JG uniCenta
 * @param <T>
 */
public abstract class Formats<T> {

    public final static Formats<Object> NULL = new FormatsNULL();
    public final static Formats<Integer> INT = new FormatsINT();
    public final static Formats<String> STRING = new FormatsSTRING();
    public final static Formats<Double> DOUBLE = new FormatsDOUBLE();
    public final static Formats<Double> CURRENCY = new FormatsCURRENCY();
    public final static Formats<Double> PERCENT = new FormatsPERCENT();
    public final static Formats<Boolean> BOOLEAN = new FormatsBOOLEAN();
    public final static Formats<Date> TIMESTAMP = new FormatsTIMESTAMP();
    public final static Formats<Date> DATE = new FormatsDATE();
    public final static Formats<Date> TIME = new FormatsTIME();
    public final static Formats<byte[]> BYTEA = new FormatsBYTEA();
    public final static Formats<Date> HOURMIN = new FormatsHOURMIN();
    public final static Formats<Date> DATETIME = new FormatsDATETIME();

    //Support those format up
    private static NumberFormat m_integerformat = NumberFormat.getIntegerInstance();
    private static NumberFormat m_doubleformat = NumberFormat.getNumberInstance();
    private static NumberFormat m_currencyformat = NumberFormat.getCurrencyInstance();
    private static NumberFormat m_percentformat = NumberFormat.getPercentInstance();
    private static DateTimeFormatter m_dateformat = getDateFormatterDefault();
    private static DateTimeFormatter m_timeformat = getTimeFormatter();
    private static DateTimeFormatter m_datetimeformat = getDateTimeFormatter();
    private static DateTimeFormatter m_hourminformat = getHourMinFormatter();

    private static Locale m_locale = Locale.getDefault();

    private static Locale getLocale() {
        return m_locale != null ? m_locale : Locale.getDefault();
    }

    /**
     * @return Format.MEDIUM: YYYY/MM/DD
     */
    private static DateTimeFormatter getDateFormatterDefault() {
        return DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(getLocale());
    }

    /**
     * @return Format.MEDIUM: YYYY/MM/DD HH:MM:SS
     */
    private static DateTimeFormatter getDateTimeFormatter() {
        return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.MEDIUM).withLocale(getLocale());
    }

    /**
     * @return Format.MEDIUM: HH:MM:SS
     */
    private static DateTimeFormatter getTimeFormatter() {
        return DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM).withLocale(getLocale());
    }

    /**
     * @return Format.SHORT: HH:MM
     */
    private static DateTimeFormatter getHourMinFormatter() {
        return DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(getLocale());
    }

    private static boolean isNullOrBlank(String text) {
        return text == null || text.isBlank();
    }

    protected Formats() {
    }

    public static void setLocale(Locale locale) {
        if (locale != null) {
            m_locale = locale;
        } else {
            m_locale = Locale.getDefault();
        }
    }

    public static void setIntegerFormatter(NumberFormat format) {
        if (format != null) {
            m_integerformat = format;
        }
    }

    public static void setDoubleFormat(NumberFormat format) {
        if (format != null) {
            m_doubleformat = format;
        }
    }

    public static void setCurrencyFormat(NumberFormat format) {
        if (format != null) {
            m_currencyformat = format;
        }
    }

    public static void setPercentFormatter(NumberFormat format) {
        if (format != null) {
            m_percentformat = format;
        }
    }

    public static void setDateFormat(DateTimeFormatter format) {
        if (format != null) {
            m_dateformat = format;
        }
    }

    public static void setTimeFormatter(DateTimeFormatter format) {
        if (format != null) {
            m_timeformat = format;
        }
    }

    public static void setDateTimeFormatter(DateTimeFormatter format) {
        if (format != null) {
            m_datetimeformat = format;
        }
    }

    public static void getHourMinFormatter(DateTimeFormatter format) {
        if (format != null) {
            m_hourminformat = format;
        }
    }

    /**
     * Resolves the authoritative operational time zone boundary linked to the
     * provider regional context. Functions as a defensive boundary preventing
     * clock desynchronization across cloud infrastructures.
     *
     * @return The active geographic {@link java.time.ZoneId} anchor. Defaults
     * to the JVM system default.
     */
    private static java.time.ZoneId getZoneId() {
        // Enforces a deterministic, immutable global clock fallback boundary instead of OS-dependent states
        //return java.time.ZoneOffset.UTC;

        // Safe infrastructure fallback boundary
        return java.time.ZoneId.systemDefault();
    }

    /**
     * Formats a legacy java.util.Date object utilizing a modern
     * DateTimeFormatter layer. Extracts the millisecond instant timeline and
     * binds it to a structural ZoneId boundary.
     *
     * @param legacyDate The old java.util.Date instance to format.
     * @param formatter The modern thread-safe DateTimeFormatter matrix
     * template.
     * @param zoneId The specific geographic ZoneId boundary (e.g.,
     * ZoneId.systemDefault()).
     * @return A localized formatted String representation of the temporal mark.
     */
    public String formatLegacyDate(java.util.Date legacyDate, DateTimeFormatter formatter, java.time.ZoneId zoneId) {
        if (legacyDate == null || formatter == null || zoneId == null) {
            return "";
        }

        // 1. Converts legacy Date millisecond matrix into a modern instant timeline anchor
        java.time.Instant instant = java.time.Instant.ofEpochMilli(legacyDate.getTime());

        // 2. Binds the instant timestamp to a specific geographic ZoneId timezone layout
        java.time.ZonedDateTime zonedDateTime = java.time.ZonedDateTime.ofInstant(instant, zoneId);

        // 3. Executes the formatting pipeline returning a thread-safe immutable String representation
        return formatter.format(zonedDateTime);
    }

    public String formatLegacyDate(java.util.Date legacyDate, DateTimeFormatter formatter) {
        return formatLegacyDate(legacyDate, formatter, getZoneId());
    }

    /**
     * Parses a localized date string token into a legacy java.util.Date object
     * context. Converts formatted text records back into millisecond epoch
     * structures via java.time abstractions.
     *
     * @param dateText The formatted text string representation of the date to
     * parse.
     * @param formatter The active modern DateTimeFormatter layout used during
     * parsing evaluation.
     * @param zoneId The evaluation ZoneId target utilized to compute regional
     * timeline displacement offsets.
     * @return A re-instantiated legacy java.util.Date object tracking the
     * mapped timestamp.
     */
    protected java.util.Date parseToLegacyDate(String dateText, DateTimeFormatter formatter, java.time.ZoneId zoneId) throws ParseException {
        if (dateText == null || formatter == null || zoneId == null) {
            return null;
        }

        try {
            // 1. Parses text directly into a modern regional timezone ZonedDateTime vector layer
            // Note: If the pattern lacks time offsets (e.g. only dd/MM/yyyy), use LocalDateTime.parse and bind the zone later.
            java.time.LocalDateTime localDateTime = java.time.LocalDateTime.parse(dateText, formatter);

            // 2. Bundles the local clock coordinates together with the active regional timezone offset
            java.time.ZonedDateTime zonedDateTime = localDateTime.atZone(zoneId);

            // 3. Extracts the modern epoch instant timeline marker
            java.time.Instant instant = zonedDateTime.toInstant();

            // 4. Feeds the instant factory into the legacy constructor to rebuild the historical Date object
            return java.util.Date.from(instant);
        } catch (java.time.format.DateTimeParseException ex) {
            throw new ParseException(ex.getParsedString(), ex.getErrorIndex());
        }
    }

    protected java.util.Date parseToLegacyDate(String dateText, DateTimeFormatter formatter) throws ParseException {
        return parseToLegacyDate(dateText, formatter, getZoneId());
    }

    public static double fixDecimals(Number value) {
        return Math.rint((value).doubleValue() * 1000000.0) / 1000000.0;
    }

    public static int getCurrencyDecimals() {
        return m_currencyformat.getMaximumFractionDigits();
    }

    public String formatValue(T value) {
        if (value == null) {
            return "";
        } else {
            return formatValueInt(value);
        }
    }

    public T parseValue(String value, T defvalue) throws BasicException {
        if (value == null || "".equals(value)) {
            return defvalue;
        } else {
            try {
                return parseValueInt(value);
            } catch (ParseException | NumberFormatException e) {
                throw new BasicException("Exception parsing value: " + value, e);
            }
        }
    }

    public T parseValue(String value) throws BasicException {
        return parseValue(value, null);
    }

    public static void setIntegerPattern(String pattern) {
        if (pattern == null || pattern.equals("")) {
            m_integerformat = NumberFormat.getIntegerInstance();
        } else {
            m_integerformat = new DecimalFormat(pattern);
        }
    }

    public static void setDoublePattern(String pattern) {
        if (pattern == null || pattern.equals("")) {
            m_doubleformat = NumberFormat.getNumberInstance();
        } else {
            m_doubleformat = new DecimalFormat(pattern);
        }
    }

    public static void setCurrencyPattern(String pattern) {
        if (pattern == null || pattern.equals("")) {
            m_currencyformat = NumberFormat.getCurrencyInstance();
        } else {
            m_currencyformat = new DecimalFormat(pattern);
        }
    }

    public static void setPercentPattern(String pattern) {
        if (pattern == null || pattern.equals("")) {
            m_percentformat = NumberFormat.getPercentInstance();
        } else {
            m_percentformat = new DecimalFormat(pattern);
        }
    }

    public static void setDatePattern(String pattern) {
        if (!isNullOrBlank(pattern)) {
            m_dateformat = DateTimeFormatter.ofPattern(pattern, getLocale());
        }
    }

    public static void setTimePattern(String pattern) {
        if (!isNullOrBlank(pattern)) {
            m_timeformat = DateTimeFormatter.ofPattern(pattern, getLocale());
        }
    }

    public static void setDateTimePattern(String pattern) {
        if (!isNullOrBlank(pattern)) {
            m_datetimeformat = DateTimeFormatter.ofPattern(pattern, getLocale());
        }
    }

    protected abstract String formatValueInt(T value);

    protected abstract T parseValueInt(String value) throws ParseException;

    public abstract int getAlignment();

    private static final class FormatsNULL extends Formats {

        @Override
        protected String formatValueInt(Object value) {
            return null;
        }

        @Override
        protected Object parseValueInt(String value) throws ParseException {
            return null;
        }

        @Override
        public int getAlignment() {
            return javax.swing.SwingConstants.LEFT;
        }
    }

    private static final class FormatsINT extends Formats<Integer> {

        @Override
        protected String formatValueInt(Integer value) {
            return m_integerformat.format(((Number) value).longValue());
        }

        @Override
        protected Integer parseValueInt(String value) throws ParseException {
            return m_integerformat.parse(value).intValue();
        }

        @Override
        public int getAlignment() {
            return javax.swing.SwingConstants.RIGHT;
        }
    }

    private static final class FormatsSTRING extends Formats<String> {

        @Override
        protected String formatValueInt(String value) {
            return value;
        }

        @Override
        protected String parseValueInt(String value) throws ParseException {
            return value;
        }

        @Override
        public int getAlignment() {
            return javax.swing.SwingConstants.LEFT;
        }
    }

    private static final class FormatsDOUBLE extends Formats<Double> {

        @Override
        protected String formatValueInt(Double value) {
            return m_doubleformat.format(Formats.fixDecimals((Number) value)); // quickfix for 3838
        }

        @Override
        protected Double parseValueInt(String value) throws ParseException {
            return m_doubleformat.parse(value).doubleValue();
        }

        @Override
        public int getAlignment() {
            return javax.swing.SwingConstants.RIGHT;
        }
    }

    private static final class FormatsPERCENT extends Formats<Double> {

        @Override
        protected String formatValueInt(Double value) {
            return m_percentformat.format(Formats.fixDecimals((Number) value)); // quickfix for 3838
        }

        @Override
        protected Double parseValueInt(String value) throws ParseException {
            try {
                return m_percentformat.parse(value).doubleValue();
            } catch (ParseException e) {
                // Segunda oportunidad como numero normalito
                return m_doubleformat.parse(value).doubleValue() / 100;
            }
        }

        @Override
        public int getAlignment() {
            return javax.swing.SwingConstants.RIGHT;
        }
    }

    private static final class FormatsCURRENCY extends Formats<Double> {

        @Override
        protected String formatValueInt(Double value) {
            return m_currencyformat.format(Formats.fixDecimals((Number) value));
        }

        @Override
        protected Double parseValueInt(String value) throws ParseException {
            try {
                return m_currencyformat.parse(value).doubleValue();
            } catch (ParseException e) {
                return m_doubleformat.parse(value).doubleValue();
            }
        }

        @Override
        public int getAlignment() {
            return javax.swing.SwingConstants.RIGHT;
        }
    }

    private static final class FormatsBOOLEAN extends Formats<Boolean> {

        @Override
        protected String formatValueInt(Boolean value) {
            return value.toString();
        }

        @Override
        protected Boolean parseValueInt(String value) throws ParseException {
            return Boolean.valueOf(value);
        }

        @Override
        public int getAlignment() {
            return javax.swing.SwingConstants.CENTER;
        }
    }

    private static final class FormatsTIMESTAMP extends Formats<Date> {

        @Override
        protected String formatValueInt(Date value) {
            return formatLegacyDate(value, m_datetimeformat);
        }

        @Override
        protected Date parseValueInt(String value) throws ParseException {
            return parseToLegacyDate(value, m_datetimeformat);
        }

        @Override
        public int getAlignment() {
            return javax.swing.SwingConstants.CENTER;
        }
    }

    private static final class FormatsDATETIME extends Formats<Date> {

        @Override
        protected String formatValueInt(Date value) {
            return formatLegacyDate(value, m_datetimeformat);
        }

        @Override
        protected Date parseValueInt(String value) throws ParseException {
            return parseToLegacyDate(value, m_datetimeformat);
        }

        @Override
        public int getAlignment() {
            return javax.swing.SwingConstants.CENTER;
        }
    }

    private static final class FormatsDATE extends Formats<Date> {

        @Override
        protected String formatValueInt(Date value) {
            return formatLegacyDate(value, m_dateformat);
        }

        @Override
        protected Date parseValueInt(String value) throws ParseException {
            return parseToLegacyDate(value, m_dateformat);
        }

        @Override
        public int getAlignment() {
            return javax.swing.SwingConstants.CENTER;
        }
    }

    private static final class FormatsTIME extends Formats<Date> {

        @Override
        protected String formatValueInt(Date value) {
            return formatLegacyDate(value, m_timeformat);
        }

        @Override
        protected Date parseValueInt(String value) throws ParseException {
            return parseToLegacyDate(value, m_timeformat);
        }

        @Override
        public int getAlignment() {
            return javax.swing.SwingConstants.CENTER;
        }
    }

    private static final class FormatsBYTEA extends Formats<byte[]> {

        @Override
        protected String formatValueInt(byte[] value) {
            try {
                return new String(value, "UTF-8");
            } catch (java.io.UnsupportedEncodingException eu) {
                return "";
            }
        }

        @Override
        protected byte[] parseValueInt(String value) throws ParseException {
            try {
                return value.getBytes("UTF-8");
            } catch (java.io.UnsupportedEncodingException eu) {
                return new byte[0];
            }
        }

        @Override
        public int getAlignment() {
            return javax.swing.SwingConstants.LEADING;
        }
    }

    private static final class FormatsHOURMIN extends Formats<Date> {

        @Override
        protected String formatValueInt(Date value) {
            return formatLegacyDate(value, m_hourminformat);
        }

        @Override
        protected Date parseValueInt(String value) throws ParseException {
            return parseToLegacyDate(value, m_hourminformat);
        }

        @Override
        public int getAlignment() {

            return javax.swing.SwingConstants.CENTER;

        }

    }

    private static final class FormatsRESOURCE extends Formats {

        private ResourceBundle m_rb;
        private String m_sPrefix;

        public FormatsRESOURCE(ResourceBundle rb, String sPrefix) {
            m_rb = rb;
            m_sPrefix = sPrefix;
        }

        @Override
        protected String formatValueInt(Object value) {
            try {
                return m_rb.getString(m_sPrefix + (String) value);
            } catch (MissingResourceException e) {
                return (String) value;
            }
        }

        @Override
        protected Object parseValueInt(String value) throws ParseException {
            return value;
        }

        @Override
        public int getAlignment() {
            return javax.swing.SwingConstants.LEFT;
        }
    }
}
