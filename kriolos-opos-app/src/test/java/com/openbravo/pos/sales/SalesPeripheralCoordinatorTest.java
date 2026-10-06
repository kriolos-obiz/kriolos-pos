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

import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.ticket.TicketInfo;
import java.lang.reflect.Proxy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SalesPeripheralCoordinator Test Suite")
class SalesPeripheralCoordinatorTest {

    private AppView createMockAppView(boolean hasScale, Double weight) {
        return (AppView) Proxy.newProxyInstance(
                AppView.class.getClassLoader(),
                new Class<?>[]{AppView.class},
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "hasScale":
                            return hasScale;
                        case "readWeight":
                            return weight;
                        case "createTicketParser":
                            return null;
                        case "getAppUserView":
                            return null;
                        default:
                            return null;
                    }
                }
        );
    }

    @Test
    @DisplayName("readWeight returns null when scale is not available")
    void testReadWeight_NoScale() {
        AppView app = createMockAppView(false, null);
        SalesPeripheralCoordinator coordinator = new SalesPeripheralCoordinator(app, null, null, key -> null, key -> null);
        assertNull(coordinator.readWeight(null));
    }

    @Test
    @DisplayName("readWeight returns measured weight when scale is present")
    void testReadWeight_Success() {
        AppView app = createMockAppView(true, 1.450);
        SalesPeripheralCoordinator coordinator = new SalesPeripheralCoordinator(app, null, null, key -> null, key -> null);
        Double weight = coordinator.readWeight(null);

        assertNotNull(weight);
        assertEquals(1.450, weight);
    }

    @Test
    @DisplayName("sendRemoteOrder returns false when script resource is missing")
    void testSendRemoteOrder_MissingScript() {
        AppView app = createMockAppView(false, null);
        SalesPeripheralCoordinator coordinator = new SalesPeripheralCoordinator(app, null, null, key -> null, key -> null);
        TicketInfo ticket = new TicketInfo();

        boolean result = coordinator.sendRemoteOrder(ticket, "Table 1", null, true, false, "01", this);
        assertFalse(result);
    }

    @Test
    @DisplayName("sendRemoteOrder executes script successfully when resource is provided")
    void testSendRemoteOrder_Success() {
        AppView app = createMockAppView(false, null);
        // Valid simple BeanShell script
        String script = "var sent = true;";
        SalesPeripheralCoordinator coordinator = new SalesPeripheralCoordinator(app, null, null, key -> script, key -> null);
        TicketInfo ticket = new TicketInfo();

        boolean result = coordinator.sendRemoteOrder(ticket, "Table 1", null, true, false, "01", this);
        assertTrue(result);
    }
}
