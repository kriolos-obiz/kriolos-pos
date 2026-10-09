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

import com.openbravo.pos.forms.AppProperties;
import com.openbravo.pos.forms.AppView;
import java.lang.reflect.Proxy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JPanelTicketSales Lazy Initialization Tests")
public class JPanelTicketSalesTest {

    @Test
    @DisplayName("Constructor should be lightweight and lazy (no child view instantiated)")
    void testConstructorIsLazy() {
        AppProperties props = (AppProperties) Proxy.newProxyInstance(
                AppProperties.class.getClassLoader(),
                new Class<?>[]{AppProperties.class},
                (proxy, method, args) -> {
                    if ("getProperty".equals(method.getName())) {
                        return "standard";
                    }
                    return null;
                }
        );

        AppView appMock = (AppView) Proxy.newProxyInstance(
                AppView.class.getClassLoader(),
                new Class<?>[]{AppView.class},
                (proxy, method, args) -> {
                    if ("getProperties".equals(method.getName())) {
                        return props;
                    }
                    return null;
                }
        );

        JPanelTicketSales salesPanel = new JPanelTicketSales(appMock);

        // Verification of lazy loading:
        // No child view has been created yet upon construction
        assertNull(salesPanel.getActiveLayoutMode(), "Active layout mode should be null before first activation/resolution");
        assertEquals(0, salesPanel.getComponentCount(), "No child components should be added to panel prior to activation");
        assertNotNull(salesPanel.getComponent(), "Panel component itself is available");
        assertNotNull(salesPanel.getTitle(), "Default title is available without eager view instantiation");
    }

    @Test
    @DisplayName("Should throw NullPointerException when app context is null")
    void testNullAppThrowsException() {
        assertThrows(NullPointerException.class, () -> new JPanelTicketSales(null));
    }
}
