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

package com.openbravo.pos.util;

import com.openbravo.format.Formats;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Formats-Backed Converters Test Suite")
class FormatsConverterTest {

    @Test
    @DisplayName("Should convert Double values correctly using FormatsDoubleConverter")
    void shouldConvertDoubleValues() {
        FormatsDoubleConverter converter = new FormatsDoubleConverter();

        assertEquals(12.5, converter.toModel(Formats.DOUBLE.formatValue(12.5)), 0.0001);
        assertEquals(0.0, converter.toModel(Formats.DOUBLE.formatValue(0.0)), 0.0001);
        assertEquals(-4.75, converter.toModel(Formats.DOUBLE.formatValue(-4.75)), 0.0001);

        assertEquals(Formats.DOUBLE.formatValue(12.5), converter.toPresentation(12.5));
        assertEquals("", converter.toPresentation(null));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "invalid_number", "abc"})
    @DisplayName("Should return null for invalid or blank Double input")
    void shouldHandleInvalidDoubleInput(String input) {
        FormatsDoubleConverter converter = new FormatsDoubleConverter();
        assertNull(converter.toModel(input));
    }

    @Test
    @DisplayName("Should convert Currency values correctly using FormatsCurrencyConverter")
    void shouldConvertCurrencyValues() {
        FormatsCurrencyConverter converter = new FormatsCurrencyConverter();

        Double val = 1500.50;
        String presentation = Formats.CURRENCY.formatValue(val);
        assertEquals(val, converter.toModel(presentation), 0.01);
        assertEquals(presentation, converter.toPresentation(val));
        assertEquals("", converter.toPresentation(null));
        assertNull(converter.toModel(""));
        assertNull(converter.toModel(null));
    }

    @Test
    @DisplayName("Should convert Percent values correctly using FormatsPercentConverter")
    void shouldConvertPercentValues() {
        FormatsPercentConverter converter = new FormatsPercentConverter();

        Double val = 0.15; // 15%
        String presentation = Formats.PERCENT.formatValue(val);
        assertEquals(val, converter.toModel(presentation), 0.001);
        assertEquals(presentation, converter.toPresentation(val));
        assertEquals("", converter.toPresentation(null));
        assertNull(converter.toModel(""));
        assertNull(converter.toModel(null));
    }

    @Test
    @DisplayName("Should convert Integer values correctly using FormatsIntegerConverter")
    void shouldConvertIntegerValues() {
        FormatsIntegerConverter converter = new FormatsIntegerConverter();

        assertEquals(42, converter.toModel(Formats.INT.formatValue(42)));
        assertEquals(0, converter.toModel(Formats.INT.formatValue(0)));
        assertEquals(-10, converter.toModel(Formats.INT.formatValue(-10)));

        assertEquals(Formats.INT.formatValue(42), converter.toPresentation(42));
        assertEquals("", converter.toPresentation(null));
        assertNull(converter.toModel(""));
        assertNull(converter.toModel(null));
        assertNull(converter.toModel("not_an_int"));
    }
}
