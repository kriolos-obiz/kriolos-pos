/*
 * Copyright (C) 2026 KriolOS
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

import com.openbravo.pos.printer.escpos.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Characterization test suite locking down printer ESC/POS command sequences,
 * hardware codes, and Unicode character translator mappings.
 * Ensures the internal byte protocol of receipt printers remains unchanged.
 */
public class PrinterCodesRegressionTest {

    @Test
    @DisplayName("CodesEpson must produce exact byte sequences for ESC/POS primitives")
    void testCodesEpsonByteSequences() {
        CodesEpson epson = new CodesEpson();

        assertArrayEquals(new byte[]{}, epson.getInitSequence(), "Epson init sequence must be empty");
        assertArrayEquals(new byte[]{0x1D, 0x21, 0x00}, epson.getSize0(), "Epson Size 0 must be 1D 21 00");
        assertArrayEquals(new byte[]{0x1D, 0x21, 0x01}, epson.getSize1(), "Epson Size 1 must be 1D 21 01");
        assertArrayEquals(new byte[]{0x1D, 0x21, 0x30}, epson.getSize2(), "Epson Size 2 must be 1D 21 30");
        assertArrayEquals(new byte[]{0x1D, 0x21, 0x31}, epson.getSize3(), "Epson Size 3 must be 1D 21 31");

        assertArrayEquals(new byte[]{0x1B, 0x45, 0x01}, epson.getBoldSet(), "Epson Bold Set must be 1B 45 01");
        assertArrayEquals(new byte[]{0x1B, 0x45, 0x00}, epson.getBoldReset(), "Epson Bold Reset must be 1B 45 00");

        assertArrayEquals(new byte[]{0x1B, 0x2D, 0x01}, epson.getUnderlineSet(), "Epson Underline Set must be 1B 2D 01");
        assertArrayEquals(new byte[]{0x1B, 0x2D, 0x00}, epson.getUnderlineReset(), "Epson Underline Reset must be 1B 2D 00");

        assertArrayEquals(new byte[]{0x1B, 0x70, 0x00, 0x32, -0x06}, epson.getOpenDrawer(), "Epson Open Drawer must match standard pulse");
        assertArrayEquals(new byte[]{0x1B, 0x69}, epson.getCutReceipt(), "Epson Cut Receipt must be 1B 69");
        assertArrayEquals(new byte[]{0x0D, 0x0A}, epson.getNewLine(), "Epson New Line must be CRLF");
    }

    @Test
    @DisplayName("CodesStar must produce exact Star Micronics command sequences")
    void testCodesStarByteSequences() {
        CodesStar star = new CodesStar();

        assertArrayEquals(new byte[]{0x1B, 0x69, 0x00, 0x00}, star.getSize0(), "Star Size 0");
        assertArrayEquals(new byte[]{0x1B, 0x69, 0x01, 0x00}, star.getSize1(), "Star Size 1");
        assertArrayEquals(new byte[]{0x1B, 0x45}, star.getBoldSet(), "Star Bold Set must be 1B 45");
        assertArrayEquals(new byte[]{0x1B, 0x46}, star.getBoldReset(), "Star Bold Reset must be 1B 46");
        assertArrayEquals(new byte[]{0x1B, 0x64, 0x30}, star.getCutReceipt(), "Star Cut Receipt must be 1B 64 30");
        assertArrayEquals(new byte[]{0x1C}, star.getOpenDrawer(), "Star Open Drawer must be 0x1C");
    }

    @Test
    @DisplayName("CodesTMU220 must produce exact Epson TM-U220 command sequences")
    void testCodesTMU220ByteSequences() {
        CodesTMU220 tmu = new CodesTMU220();

        assertArrayEquals(new byte[]{0x1B, 0x21, 0x01}, tmu.getSize0(), "TM-U220 Size 0");
        assertArrayEquals(new byte[]{0x1B, 0x21, 0x11}, tmu.getSize1(), "TM-U220 Size 1");
        assertArrayEquals(new byte[]{0x1B, 0x21, 0x21}, tmu.getSize2(), "TM-U220 Size 2");
        assertArrayEquals(new byte[]{0x1B, 0x21, 0x31}, tmu.getSize3(), "TM-U220 Size 3");
        assertArrayEquals(new byte[]{0x1B, 0x69}, tmu.getCutReceipt(), "TM-U220 Cut Receipt must be 1B 69");
    }

    @Test
    @DisplayName("UnicodeTranslatorInt must map standard and accented characters faithfully")
    void testUnicodeTranslatorIntMapping() {
        UnicodeTranslatorInt trans = new UnicodeTranslatorInt();

        // Standard ASCII range (0x00 - 0x7F) remains unchanged
        assertEquals((byte) 'A', trans.transChar('A'));
        assertEquals((byte) 'z', trans.transChar('z'));
        assertEquals((byte) '5', trans.transChar('5'));
        assertEquals((byte) '$', trans.transChar('$'));

        // Portuguese / International accented characters mapped to Code Table 13 / 850
        assertEquals((byte) -0x80, trans.transChar('\u00c7'), "Ç must map to 0x80 (-128)");
        assertEquals((byte) -0x79, trans.transChar('\u00e7'), "ç must map to 0x87 (-121)");
        assertEquals((byte) -0x60, trans.transChar('\u00e1'), "á must map to -0x60 (-96)");
        assertEquals((byte) -0x5E, trans.transChar('\u00f3'), "ó must map to -0x5E (-94)");
        assertEquals((byte) -0x5D, trans.transChar('\u00fa'), "ú must map to -0x5D (-93)");
        assertEquals((byte) -0x5C, trans.transChar('\u00f1'), "ñ must map to -0x5C (-92)");
    }

    @Test
    @DisplayName("UnicodeTranslatorEur must translate Euro symbol accurately")
    void testUnicodeTranslatorEurMapping() {
        UnicodeTranslatorEur trans = new UnicodeTranslatorEur();

        assertEquals((byte) -0x12, trans.transChar('\u20ac'), "Euro symbol must map to -0x12 / -18");
        assertEquals((byte) 'X', trans.transChar('X'));
    }
}
