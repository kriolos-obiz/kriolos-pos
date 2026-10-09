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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TicketToolbarPane Unit Tests")
class TicketToolbarPaneTest {

    @Test
    @DisplayName("Buttons are initialized and visible by default")
    void testInitialState() {
        TicketToolbarPane toolbar = new TicketToolbarPane();

        assertNotNull(toolbar.getBtnDelete());
        assertNotNull(toolbar.getBtnList());
        assertNotNull(toolbar.getBtnEditLine());
        assertNotNull(toolbar.getBtnEditAttributes());
        assertNotNull(toolbar.getBtnCheckStock());

        assertTrue(toolbar.getBtnEditLine().isVisible());
        assertTrue(toolbar.getBtnList().isVisible());
    }

    @Test
    @DisplayName("Visibility controls update component visibility")
    void testVisibilityControls() {
        TicketToolbarPane toolbar = new TicketToolbarPane();

        toolbar.setEditLineVisible(false);
        toolbar.setFindProductVisible(false);

        assertFalse(toolbar.getBtnEditLine().isVisible());
        assertFalse(toolbar.getBtnList().isVisible());

        toolbar.setEditLineVisible(true);
        toolbar.setFindProductVisible(true);

        assertTrue(toolbar.getBtnEditLine().isVisible());
        assertTrue(toolbar.getBtnList().isVisible());
    }

    @Test
    @DisplayName("Action callbacks trigger when buttons are clicked")
    void testActionCallbacks() {
        TicketToolbarPane toolbar = new TicketToolbarPane();
        AtomicBoolean deleteTriggered = new AtomicBoolean(false);
        AtomicBoolean findTriggered = new AtomicBoolean(false);
        AtomicBoolean editTriggered = new AtomicBoolean(false);
        AtomicBoolean attrTriggered = new AtomicBoolean(false);
        AtomicBoolean stockTriggered = new AtomicBoolean(false);

        toolbar.setOnDeleteLine(() -> deleteTriggered.set(true));
        toolbar.setOnFindProduct(() -> findTriggered.set(true));
        toolbar.setOnEditLine(() -> editTriggered.set(true));
        toolbar.setOnEditAttributes(() -> attrTriggered.set(true));
        toolbar.setOnCheckStock(() -> stockTriggered.set(true));

        toolbar.getBtnDelete().doClick();
        toolbar.getBtnList().doClick();
        toolbar.triggerEditLine();
        toolbar.getBtnEditAttributes().doClick();
        toolbar.getBtnCheckStock().doClick();

        assertTrue(deleteTriggered.get());
        assertTrue(findTriggered.get());
        assertTrue(editTriggered.get());
        assertTrue(attrTriggered.get());
        assertTrue(stockTriggered.get());
    }
}
