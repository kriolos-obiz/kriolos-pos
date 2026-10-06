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

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.DefaultComboBoxModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("InputPane Unit Tests")
class InputPaneTest {

    @Test
    @DisplayName("Input components are initialized properly")
    void testInitialState() {
        InputPane pane = new InputPane();

        assertNotNull(pane.getNumberKeys());
        assertNotNull(pane.getPriceLabel());
        assertNotNull(pane.getPorLabel());
        assertNotNull(pane.getKeyFactory());
        assertNotNull(pane.getBtnEnter());
        assertNotNull(pane.getAddTaxCheckBox());
        assertNotNull(pane.getTaxComboBox());

        assertEquals("..", pane.getPorLabel().getText());
        String priceText = pane.getPriceText();
        assertTrue(priceText == null || priceText.isEmpty());
        assertFalse(pane.isTaxIncluded());
    }

    @Test
    @DisplayName("Price and Por label mutators update text correctly")
    void testTextMutators() {
        InputPane pane = new InputPane();

        pane.setPriceText("15.50");
        pane.setPorText("2x");
        assertEquals("15.50", pane.getPriceText());
        assertEquals("2x", pane.getPorText());

        pane.clearInput();
        assertEquals("", pane.getPriceText());
        assertEquals("", pane.getPorText());
    }

    @Test
    @DisplayName("Tax controls visibility and model update correctly")
    void testTaxControls() {
        InputPane pane = new InputPane();

        pane.setTaxIncluded(true);
        assertTrue(pane.isTaxIncluded());

        pane.setTaxIncluded(false);
        assertFalse(pane.isTaxIncluded());

        pane.setTaxControlsVisible(false);
        assertFalse(pane.getAddTaxCheckBox().isVisible());
        assertFalse(pane.getTaxComboBox().isVisible());

        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>(new String[]{"Standard", "Reduced", "Zero"});
        pane.setTaxModel(model);
        assertEquals(3, pane.getTaxComboBox().getItemCount());
        assertEquals("Standard", pane.getTaxComboBox().getSelectedItem());
    }

    @Test
    @DisplayName("Action callbacks trigger when buttons/checkboxes are clicked")
    void testActionCallbacks() {
        InputPane pane = new InputPane();
        AtomicBoolean enterTriggered = new AtomicBoolean(false);
        AtomicBoolean addTaxTriggered = new AtomicBoolean(false);
        AtomicBoolean keyFactoryActionTriggered = new AtomicBoolean(false);

        pane.setOnEnterAction(() -> enterTriggered.set(true));
        pane.setOnAddTaxAction(() -> addTaxTriggered.set(true));
        pane.setOnKeyFactoryAction(() -> keyFactoryActionTriggered.set(true));

        pane.getBtnEnter().doClick();
        pane.getAddTaxCheckBox().doClick();
        pane.getKeyFactory().postActionEvent();

        assertTrue(enterTriggered.get());
        assertTrue(addTaxTriggered.get());
        assertTrue(keyFactoryActionTriggered.get());
    }
}
