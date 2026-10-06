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

@DisplayName("SalesKeypadStateMachine Unit Tests")
public class SalesKeypadStateMachineTest {

    @Test
    @DisplayName("Initial state should be zero/empty")
    void testInitialState() {
        SalesKeypadStateMachine sm = new SalesKeypadStateMachine();
        assertTrue(sm.isInputZero());
        assertTrue(sm.isPorZero());
        assertEquals("", sm.getPriceText());
        assertEquals("", sm.getPorText());
        assertEquals(0.0, sm.getInputValue());
        assertEquals(1.0, sm.getPorValue());
        assertEquals("", sm.getBarcode());
    }

    @Test
    @DisplayName("Typing digits should accumulate price text")
    void testDigitInput() {
        SalesKeypadStateMachine sm = new SalesKeypadStateMachine();
        sm.processKeypadChar('1');
        sm.processKeypadChar('2');
        sm.processKeypadChar('5');

        assertEquals("125", sm.getPriceText());
        assertEquals(125.0, sm.getInputValue());
        assertTrue(sm.isInputValid());
        assertTrue(sm.isPorZero());
        assertEquals("125", sm.getBarcode());
    }

    @Test
    @DisplayName("Decimal point entry in standard mode")
    void testDecimalPointStandard() {
        SalesKeypadStateMachine sm = new SalesKeypadStateMachine();
        sm.processKeypadChar('.');
        assertEquals("0.", sm.getPriceText());

        sm.processKeypadChar('5');
        sm.processKeypadChar('0');
        assertEquals("0.50", sm.getPriceText());
        assertEquals(0.50, sm.getInputValue(), 0.001);
    }

    @Test
    @DisplayName("Quantity multiplier with asterisk (*)")
    void testQuantityMultiplier() {
        SalesKeypadStateMachine sm = new SalesKeypadStateMachine();
        sm.processKeypadChar('1');
        sm.processKeypadChar('0');
        sm.processKeypadChar('*');
        sm.processKeypadChar('3');

        assertEquals("10", sm.getPriceText());
        assertEquals("x3", sm.getPorText());
        assertEquals(10.0, sm.getInputValue());
        assertEquals(3.0, sm.getPorValue());
        assertTrue(sm.isPorValid());
    }

    @Test
    @DisplayName("Clear / DEL key resets state")
    void testResetKey() {
        SalesKeypadStateMachine sm = new SalesKeypadStateMachine();
        sm.processKeypadChar('9');
        sm.processKeypadChar('*');
        sm.processKeypadChar('2');

        assertEquals("9", sm.getPriceText());
        assertEquals("x2", sm.getPorText());

        sm.processKeypadChar('\u007f');
        assertEquals("", sm.getPriceText());
        assertEquals("", sm.getPorText());
        assertTrue(sm.isInputZero());
        assertTrue(sm.isPorZero());
        assertEquals("", sm.getBarcode());
    }

    @Test
    @DisplayName("Price with 00 mode formatting")
    void testPriceWith00Mode() {
        SalesKeypadStateMachine sm = new SalesKeypadStateMachine();
        sm.setPriceWith00(true);

        sm.processKeypadChar('5');
        assertEquals("0.05", sm.getPriceText());

        sm.processKeypadChar('0');
        assertEquals("0.50", sm.getPriceText());

        sm.processKeypadChar('0');
        assertEquals("5.00", sm.getPriceText());
    }
}
