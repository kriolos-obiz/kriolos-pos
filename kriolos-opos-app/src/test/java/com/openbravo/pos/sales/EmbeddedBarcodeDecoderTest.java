//    KriolOS POS
//    Copyright (c) 2019-2026 KriolOS
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

package com.openbravo.pos.sales;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EmbeddedBarcodeDecoder Unit Tests")
public class EmbeddedBarcodeDecoderTest {

    @Test
    @DisplayName("Should detect variable EAN-13 barcodes")
    void testIsEanVariableBarcode() {
        assertTrue(EmbeddedBarcodeDecoder.isEanVariableBarcode("2012345678901"));
        assertTrue(EmbeddedBarcodeDecoder.isEanVariableBarcode("0212345678901"));
        assertTrue(EmbeddedBarcodeDecoder.isEanVariableBarcode("251234567890"));
        assertFalse(EmbeddedBarcodeDecoder.isEanVariableBarcode("5012345678901"));
        assertFalse(EmbeddedBarcodeDecoder.isEanVariableBarcode("123"));
        assertFalse(EmbeddedBarcodeDecoder.isEanVariableBarcode(null));
    }

    @Test
    @DisplayName("Should detect variable UPC-A barcodes")
    void testIsUpcVariableBarcode() {
        assertTrue(EmbeddedBarcodeDecoder.isUpcVariableBarcode("212345678901"));
        assertFalse(EmbeddedBarcodeDecoder.isUpcVariableBarcode("012345678901"));
        assertFalse(EmbeddedBarcodeDecoder.isUpcVariableBarcode("2123456789012")); // 13 chars is not UPC-A
        assertFalse(EmbeddedBarcodeDecoder.isUpcVariableBarcode(null));
    }
}
