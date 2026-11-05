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
import com.openbravo.pos.panels.AppView;
import com.openbravo.pos.container.BeanContainer;
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

    @Test
    @DisplayName("StartBreakCommand should enforce invariants")
    void shouldEnforceStartBreakCommandInvariants() {
        assertThrows(NullPointerException.class, () -> new StartBreakCommand(null, "break-1"),
                "Null userId should throw NullPointerException");
        assertThrows(IllegalArgumentException.class, () -> new StartBreakCommand("", "break-1"),
                "Empty userId should throw IllegalArgumentException");
        assertThrows(IllegalArgumentException.class, () -> new StartBreakCommand("   ", "break-1"),
                "Blank userId should throw IllegalArgumentException");

        assertThrows(NullPointerException.class, () -> new StartBreakCommand("user-1", null),
                "Null breakId should throw NullPointerException");
        assertThrows(IllegalArgumentException.class, () -> new StartBreakCommand("user-1", ""),
                "Empty breakId should throw IllegalArgumentException");
        assertThrows(IllegalArgumentException.class, () -> new StartBreakCommand("user-1", "   "),
                "Blank breakId should throw IllegalArgumentException");

        StartBreakCommand cmd = StartBreakCommand.of("emp-42", "lunch-break");
        assertEquals("emp-42", cmd.userId());
        assertEquals("lunch-break", cmd.breakId());
    }

    @Test
    @DisplayName("ShiftService should dispatch StartBreakCommand and forward legacy overload")
    void shouldDispatchStartBreakCommand() throws BasicException {
        final StartBreakCommand[] captured = new StartBreakCommand[1];
        ShiftService service = new ShiftService() {
            @Override public void checkIn(String userId) {}
            @Override public void checkOut(String userId) {}
            @Override public boolean isCheckedIn(String userId) { return false; }
            @Override public void startBreak(StartBreakCommand command) { captured[0] = command; }
            @Override public void endBreak(String userId) {}
            @Override public boolean isOnBreak(String userId) { return false; }
            @Override public String getShiftId(String userId) { return null; }
            @Override public Date getLastCheckIn(String userId) { return null; }
            @Override public Date getLastCheckOut(String userId) { return null; }
            @Override public Date getStartBreakTime(String shiftId) { return null; }
            @Override public String getLastBreakId(String shiftId) { return null; }
            @Override public String getLastBreakName(String shiftId) { return null; }
            @Override public ShiftBreakActivity getLastBreakActivity(String userId) { return null; }
            @Override public Object[] getLastBreak(String userId) { return null; }
            @Override public boolean isOnLeave(String userId) { return false; }
            @Override public void blockEmployee(String userId) {}
            @Override public java.util.List<Break> listBreaksVisible() { return java.util.Collections.emptyList(); }
            @Override public java.util.List<BreaksInfo> getBreaksListAll() { return java.util.Collections.emptyList(); }
            @Override public java.util.List<LeavesInfo> getLeavesListAll() { return java.util.Collections.emptyList(); }
            @Override public EmployeeInfoExt loadEmployeeExt(String id) { return null; }
            @Override public com.openbravo.data.loader.TableDefinition<BreaksInfo> getTableBreaks() { return null; }
            @Override public com.openbravo.data.loader.TableDefinition<LeavesInfo> getTableLeaves() { return null; }
            @Override public com.openbravo.data.user.ListProvider<EmployeeInfoExt> getEmployeeListProvider(com.openbravo.data.user.EditorCreator<?> filter) { return null; }
        };

        StartBreakCommand cmd = StartBreakCommand.of("user-1", "break-lunch");
        service.startBreak(cmd);
        assertNotNull(captured[0]);
        assertEquals("user-1", captured[0].userId());
        assertEquals("break-lunch", captured[0].breakId());

        captured[0] = null;
        service.startBreak("user-2", "break-coffee");
        assertNotNull(captured[0]);
        assertEquals("user-2", captured[0].userId());
        assertEquals("break-coffee", captured[0].breakId());
    }
}
