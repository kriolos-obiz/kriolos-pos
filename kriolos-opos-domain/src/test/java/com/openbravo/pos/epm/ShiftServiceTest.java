/*
 * Copyright (C) 2026 KriolOS POS
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
package com.openbravo.pos.epm;

import com.openbravo.basic.BasicException;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.BeanContainer;
import java.lang.reflect.Proxy;
import java.util.Date;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ShiftService Port Test Suite")
class ShiftServiceTest {

    @Test
    @DisplayName("ShiftBreakActivity record should store breakName and startTime correctly")
    void testShiftBreakActivityRecord() {
        Date now = new Date();
        ShiftBreakActivity activity = new ShiftBreakActivity("Lunch", now);

        assertEquals("Lunch", activity.breakName());
        assertEquals(now, activity.startTime());
    }

    @Test
    @DisplayName("Should resolve ShiftService in BeanContainer to DataLogicPresenceManagement")
    void shouldResolveShiftServiceInBeanContainer() {
        AppView appViewProxy = (AppView) Proxy.newProxyInstance(
                AppView.class.getClassLoader(),
                new Class<?>[]{AppView.class},
                (proxy, method, args) -> null
        );

        Object bean = BeanContainer.getBean("com.openbravo.pos.epm.ShiftService", appViewProxy);
        assertNotNull(bean, "BeanContainer should resolve ShiftService");
        assertInstanceOf(DataLogicPresenceManagement.class, bean, "ShiftService should resolve to DataLogicPresenceManagement");
        assertInstanceOf(ShiftService.class, bean, "DataLogicPresenceManagement should implement ShiftService");
    }
}
