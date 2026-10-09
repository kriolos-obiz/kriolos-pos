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

import java.awt.Component;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JLabel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TicketHeaderPane Unit Tests")
class TicketHeaderPaneTest {

    @Test
    @DisplayName("Components are initialized and configured with correct defaults")
    void testInitialState() {
        TicketHeaderPane header = new TicketHeaderPane();

        assertNotNull(header.getBagContainer());
        assertNotNull(header.getBtnToggleScripts());
        assertNotNull(header.getBtnScale());
        assertNotNull(header.getBtnCustomer());
        assertNotNull(header.getBtnSplit());
        assertNotNull(header.getBtnRemoteOrder());
        assertNotNull(header.getBtnReprint());
        assertNotNull(header.getScriptsPanel());
        assertNotNull(header.getBagExtPanel());

        assertFalse(header.isToggleScriptsSelected());
        assertFalse(header.isScriptsVisible());
        assertFalse(header.getBtnSplit().isEnabled());
    }

    @Test
    @DisplayName("Bag and Script components can be added dynamically")
    void testDynamicComponents() {
        TicketHeaderPane header = new TicketHeaderPane();
        JLabel bagLabel = new JLabel("Bags");
        JLabel scriptLabel = new JLabel("Script1");

        header.setBagComponent(bagLabel);
        assertEquals(1, header.getBagContainer().getComponentCount());
        assertSame(bagLabel, header.getBagContainer().getComponent(0));

        header.addScriptComponent(scriptLabel);
        assertEquals(1, header.getBagExtPanel().getComponentCount());
        assertSame(scriptLabel, header.getBagExtPanel().getComponent(0));

        header.setBagComponent(null);
        assertEquals(0, header.getBagContainer().getComponentCount());
    }

    @Test
    @DisplayName("Visibility and enablement controls update child components")
    void testVisibilityAndEnablement() {
        TicketHeaderPane header = new TicketHeaderPane();

        header.setScriptsVisible(true);
        assertTrue(header.isScriptsVisible());
        assertTrue(header.getBagExtPanel().isVisible());

        header.setScaleVisible(false);
        assertFalse(header.getBtnScale().isVisible());

        header.setCustomerVisible(false);
        assertFalse(header.getBtnCustomer().isVisible());

        header.setCustomerEnabled(false);
        assertFalse(header.getBtnCustomer().isEnabled());

        header.setSplitEnabled(true);
        assertTrue(header.getBtnSplit().isEnabled());

        header.setRemoteOrderVisible(false);
        assertFalse(header.getBtnRemoteOrder().isVisible());

        header.setRemoteOrderEnabled(false);
        assertFalse(header.getBtnRemoteOrder().isEnabled());

        header.setReprintVisible(false);
        assertFalse(header.getBtnReprint().isVisible());

        header.setReprintEnabled(false);
        assertFalse(header.getBtnReprint().isEnabled());
    }

    @Test
    @DisplayName("Action listeners fire appropriately on button clicks")
    void testActionListeners() {
        TicketHeaderPane header = new TicketHeaderPane();
        AtomicReference<Boolean> toggleTriggered = new AtomicReference<>();
        AtomicBoolean scaleTriggered = new AtomicBoolean(false);
        AtomicBoolean customerTriggered = new AtomicBoolean(false);
        AtomicBoolean splitTriggered = new AtomicBoolean(false);
        AtomicBoolean remoteTriggered = new AtomicBoolean(false);
        AtomicBoolean reprintTriggered = new AtomicBoolean(false);

        header.setOnToggleScripts(toggleTriggered::set);
        header.setOnScaleAction(() -> scaleTriggered.set(true));
        header.setOnCustomerAction(() -> customerTriggered.set(true));
        header.setOnSplitAction(() -> splitTriggered.set(true));
        header.setOnRemoteOrderAction(() -> remoteTriggered.set(true));
        header.setOnReprintAction(() -> reprintTriggered.set(true));

        header.getBtnToggleScripts().doClick();
        header.getBtnScale().doClick();
        header.getBtnCustomer().doClick();
        header.getBtnSplit().setEnabled(true);
        header.getBtnSplit().doClick();
        header.getBtnRemoteOrder().doClick();
        header.getBtnReprint().doClick();

        assertEquals(Boolean.TRUE, toggleTriggered.get());
        assertTrue(scaleTriggered.get());
        assertTrue(customerTriggered.get());
        assertTrue(splitTriggered.get());
        assertTrue(remoteTriggered.get());
        assertTrue(reprintTriggered.get());
    }
}
