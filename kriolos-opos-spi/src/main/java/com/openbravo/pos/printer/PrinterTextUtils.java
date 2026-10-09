/*
 * Copyright (C) 2022 KriolOS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.openbravo.pos.printer;

import com.openbravo.pos.printer.DevicePrinter;

/**
 * Utility class for text and barcode formatting and alignment for printer
 * output. Fully optimized for Java 11+.
 *
 * @author JG uniCenta
 * @author KriolOS
 */
public final class PrinterTextUtils {

    /**
     * Default line character length for standard receipt printers.
     */
    public static final int DEFAULT_LINE_LENGTH = 42;

    // Private constructor to prevent instantiation of a utility class
    private PrinterTextUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Directs the text to the appropriate alignment method based on the
     * alignment type.
     *
     * @param textAlignment The alignment type constant from DevicePrinter
     * @param text The text string to align
     * @param textLength Maximum length of the output string
     * @return Aligned and padded string
     */
    public static String alignText(int textAlignment, String text, int textLength) {
        return switch (textAlignment) {
            case DevicePrinter.ALIGN_RIGHT ->
                alignRight(text, textLength);
            case DevicePrinter.ALIGN_CENTER ->
                alignCenter(text, textLength);
            default ->
                alignLeft(text, textLength); // DevicePrinter.ALIGN_LEFT
        };
    }
    
    /**
     * Aligns text to the left. If the text exceeds the maximum line size, it is
     * truncated. If it is shorter, spaces are appended to the right.
     *
     * @param line The text string to align
     * @param lineSize Maximum length of the output string
     * @return Left-aligned string padded with spaces
     */
    public static String alignLeft(String line, int lineSize) {
        if (line == null) {
            line = "";
        }
        if (line.length() > lineSize) {
            return line.substring(0, lineSize);
        }
        return line + " ".repeat(lineSize - line.length());
    }

    /**
     * Aligns text to the right. If the text exceeds the maximum line size, it
     * is truncated. If it is shorter, spaces are prepended to the left.
     *
     * @param line The text string to align
     * @param lineSize Maximum length of the output string
     * @return Right-aligned string padded with spaces
     */
    public static String alignRight(String line, int lineSize) {
        if (line == null) {
            line = "";
        }
        if (line.length() > lineSize) {
            return line.substring(0, lineSize);
        }
        return " ".repeat(lineSize - line.length()) + line;
    }

    /**
     * Centers the text within the specified line size. Correctly distributes
     * odd spaces to guarantee the exact total length.
     *
     * @param line The text string to align
     * @param lineSize Maximum length of the output string
     * @return Center-aligned string padded evenly with spaces
     */
    public static String alignCenter(String line, int lineSize) {
        if (line == null) {
            line = "";
        }
        if (line.length() > lineSize) {
            return line.substring(0, lineSize);
        }

        int totalSpaces = lineSize - line.length();
        int leftSpaces = totalSpaces / 2;
        int rightSpaces = totalSpaces - leftSpaces; // Safely handles odd numbers

        return " ".repeat(leftSpaces) + line + " ".repeat(rightSpaces);
    }

    /**
     * Centers the text using the default line length (42 characters).
     *
     * @param line The text string to align
     * @return Center-aligned string with the default line length
     */
    public static String alignCenter(String line) {
        return alignCenter(line, DEFAULT_LINE_LENGTH);
    }

    /**
     * Generates a string consisting of a specific character repeated a given
     * number of times.
     *
     * @param size The desired length of the string
     * @param paddingChar The character used to fill the string
     * @return A string composed of the repeated character
     */
    public static String getPaddingString(int size, char paddingChar) {
        if (size <= 0) {
            return "";
        }
        return String.valueOf(paddingChar).repeat(size);
    }

    /**
     * Generates a string consisting of spaces repeated a given number of times.
     *
     * @param size The desired length of the string
     * @return A string composed of spaces
     */
    public static String getPaddingString(int size) {
        return getPaddingString(size, ' ');
    }

    /**
     * Aligns a barcode value by padding it with leading zeros to meet the
     * target length. If the barcode length exceeds the target size, it
     * truncates from the left to keep the end.
     *
     * @param barcode The barcode string to align
     * @param targetSize The exact final size needed for the barcode
     * @return Zero-padded barcode string
     */
    public static String alignBarcode(String barcode, int targetSize) {
        if (barcode == null) {
            barcode = "";
        }

        if (barcode.length() > targetSize) {
            return barcode.substring(barcode.length() - targetSize);
        }

        return getPaddingString(targetSize - barcode.length(), '0') + barcode;
    }

    /**
     * Converts a string of digits to corresponding ASCII byte codes.
     *
     * @param sCad input string
     * @return byte array
     */
    public static byte[] transNumber(String sCad) {
        if (sCad == null) {
            return null;
        }
        byte[] bAux = new byte[sCad.length()];
        for (int i = 0; i < sCad.length(); i++) {
            bAux[i] = transNumberChar(sCad.charAt(i));
        }
        return bAux;
    }

    /**
     * Converts single char digit to ASCII byte.
     *
     * @param sChar input character
     * @return byte value
     */
    static byte transNumberChar(char sChar) {
        return switch (sChar) {
            case '0' ->
                0x30;
            case '1' ->
                0x31;
            case '2' ->
                0x32;
            case '3' ->
                0x33;
            case '4' ->
                0x34;
            case '5' ->
                0x35;
            case '6' ->
                0x36;
            case '7' ->
                0x37;
            case '8' ->
                0x38;
            case '9' ->
                0x39;
            default ->
                0x30;
        };
    }
}
