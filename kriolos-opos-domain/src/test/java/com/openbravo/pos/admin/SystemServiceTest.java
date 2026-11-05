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
package com.openbravo.pos.admin;

import com.openbravo.pos.resources.ResourceService;
import com.openbravo.pos.admin.SystemService;
import com.openbravo.pos.admin.SecurityService;
import com.openbravo.pos.admin.DataLogicSystem;
import com.openbravo.pos.container.BeanContainer;
import com.openbravo.pos.panels.AppView;
import com.openbravo.pos.admin.AppUser;
import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.TableDefinition;
import com.openbravo.pos.admin.ResourceInfo;
import java.awt.image.BufferedImage;
import java.lang.reflect.Proxy;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SystemService and ResourceService Port Test Suite")
class SystemServiceTest {

    @Test
    @DisplayName("Should resolve ResourceService and SystemService in BeanContainer to DataLogicSystem")
    void shouldResolveServicesInBeanContainer() {
        AppView appViewProxy = (AppView) Proxy.newProxyInstance(
                AppView.class.getClassLoader(),
                new Class<?>[]{AppView.class},
                (proxy, method, args) -> null
        );

        Object resourceBean = BeanContainer.getBean("com.openbravo.pos.resources.ResourceService", appViewProxy);
        assertNotNull(resourceBean, "BeanContainer should resolve ResourceService");
        assertInstanceOf(DataLogicSystem.class, resourceBean, "ResourceService should resolve to DataLogicSystem");
        assertInstanceOf(ResourceService.class, resourceBean, "DataLogicSystem should implement ResourceService");

        Object systemBean = BeanContainer.getBean("com.openbravo.pos.admin.SystemService", appViewProxy);
        assertNotNull(systemBean, "BeanContainer should resolve SystemService");
        assertInstanceOf(DataLogicSystem.class, systemBean, "SystemService should resolve to DataLogicSystem");
        assertInstanceOf(SystemService.class, systemBean, "DataLogicSystem should implement SystemService");
        assertInstanceOf(SecurityService.class, systemBean, "SystemService should extend SecurityService");
    }

    @Nested
    @DisplayName("Security Command Invariant Tests")
    class SecurityCommandInvariantTests {

        @Test
        @DisplayName("ChangePasswordCommand should validate required invariants")
        void shouldValidateChangePasswordCommandInvariants() {
            ChangePasswordCommand cmd = ChangePasswordCommand.of("user-01", "hash123");
            assertEquals("user-01", cmd.userId());
            assertEquals("hash123", cmd.newPassword());

            // Null password is permitted (clears password)
            ChangePasswordCommand emptyCmd = new ChangePasswordCommand("user-02", null);
            assertNull(emptyCmd.newPassword());

            assertThrows(NullPointerException.class, () -> new ChangePasswordCommand(null, "hash"));
            assertThrows(IllegalArgumentException.class, () -> new ChangePasswordCommand("", "hash"));
            assertThrows(IllegalArgumentException.class, () -> new ChangePasswordCommand("   ", "hash"));
        }

        @Test
        @DisplayName("UpdateRolePermissionsCommand should validate required invariants")
        void shouldValidateUpdateRolePermissionsCommandInvariants() {
            UpdateRolePermissionsCommand cmd = UpdateRolePermissionsCommand.of("role-admin", "<permissions/>");
            assertEquals("role-admin", cmd.roleId());
            assertEquals("<permissions/>", cmd.permissionsXml());

            assertThrows(NullPointerException.class, () -> new UpdateRolePermissionsCommand(null, "<xml/>"));
            assertThrows(IllegalArgumentException.class, () -> new UpdateRolePermissionsCommand("", "<xml/>"));
            assertThrows(IllegalArgumentException.class, () -> new UpdateRolePermissionsCommand("   ", "<xml/>"));
            assertThrows(NullPointerException.class, () -> new UpdateRolePermissionsCommand("role-admin", null));
        }
    }

    @Test
    @DisplayName("SystemService mock implementation should satisfy domain contract")
    void testSystemServiceContract() throws BasicException {
        AtomicReference<ChangePasswordCommand> lastPasswordCmd = new AtomicReference<>();
        AtomicReference<UpdateRolePermissionsCommand> lastPermissionsCmd = new AtomicReference<>();

        SystemService service = new SystemService() {
            @Override
            public String getDBVersion() {
                return "MockDB 1.0";
            }

            @Override
            public String findVersion() {
                return "2.4.0";
            }

            @Override
            public String getUser() {
                return "admin";
            }

            @Override
            public void execDrawerOpened(RecordDrawerOpenedCommand command) {
            }

            @Override
            public TableDefinition<ResourceInfo> getTableResources() {
                return null;
            }

            @Override
            public String getResourceAsXML(String sName) {
                return "<xml/>";
            }

            @Override
            public String getResourceAsText(String sName) {
                return "text";
            }

            @Override
            public byte[] getResourceAsBinary(String sName) {
                return new byte[]{1, 2, 3};
            }

            @Override
            public BufferedImage getResourceAsImage(String sName) {
                return null;
            }

            @Override
            public Properties getResourceAsProperties(String sName) {
                return new Properties();
            }

            @Override
            public void setResource(String sName, int iType, byte[] data) {
            }

            @Override
            public void setResourceAsBinary(String sName, byte[] data) {
            }

            @Override
            public void setResourceAsProperties(String sName, Properties p) {
            }

            @Override
            public List<AppUser> listPeopleVisible() {
                return List.of();
            }

            @Override
            public AppUser findPeopleByCard(String card) {
                return null;
            }

            @Override
            public String findRolePermissions(String sRole) {
                return "";
            }

            @Override
            public List<String> getPermissions(String role) {
                return List.of();
            }

            @Override
            public void execChangePassword(ChangePasswordCommand command) throws BasicException {
                lastPasswordCmd.set(command);
            }

            @Override
            public void execUpdatePermissions(UpdateRolePermissionsCommand command) throws BasicException {
                lastPermissionsCmd.set(command);
            }
        };

        assertEquals("MockDB 1.0", service.getDBVersion());
        assertEquals("2.4.0", service.findVersion());
        assertEquals("admin", service.getUser());
        assertEquals("<xml/>", service.getResourceAsXML("test"));
        assertEquals("text", service.getResourceAsText("test"));
        assertNotNull(service.getResourceAsBinary("test"));
        assertNotNull(service.getResourceAsProperties("test"));
        assertNotNull(service.listPeopleVisible());

        // Test typed commands
        service.execChangePassword(ChangePasswordCommand.of("u1", "pass1"));
        assertNotNull(lastPasswordCmd.get());
        assertEquals("u1", lastPasswordCmd.get().userId());
        assertEquals("pass1", lastPasswordCmd.get().newPassword());

        service.execUpdatePermissions(UpdateRolePermissionsCommand.of("r1", "<perm/>"));
        assertNotNull(lastPermissionsCmd.get());
        assertEquals("r1", lastPermissionsCmd.get().roleId());
        assertEquals("<perm/>", lastPermissionsCmd.get().permissionsXml());

        // Test legacy default delegation
        service.execChangePassword(new Object[]{"pass2", "u2"});
        assertEquals("u2", lastPasswordCmd.get().userId());
        assertEquals("pass2", lastPasswordCmd.get().newPassword());

        service.execUpdatePermissions(new Object[]{"r2", "<perm2/>"});
        assertEquals("r2", lastPermissionsCmd.get().roleId());
        assertEquals("<perm2/>", lastPermissionsCmd.get().permissionsXml());
    }

    @Test
    @DisplayName("RecordDrawerOpenedCommand should enforce invariants and immutability")
    void shouldEnforceRecordDrawerOpenedCommandInvariants() {
        assertThrows(NullPointerException.class,
                () -> new RecordDrawerOpenedCommand(null, "No Sale", new Date()),
                "Null name should throw NullPointerException");
        assertThrows(IllegalArgumentException.class,
                () -> new RecordDrawerOpenedCommand("", "No Sale", new Date()),
                "Empty name should throw IllegalArgumentException");
        assertThrows(IllegalArgumentException.class,
                () -> new RecordDrawerOpenedCommand("   ", "No Sale", new Date()),
                "Blank name should throw IllegalArgumentException");

        Date now = new Date();
        RecordDrawerOpenedCommand cmd = RecordDrawerOpenedCommand.of("Manager", "Refund", now);
        assertEquals("Manager", cmd.name());
        assertEquals("Refund", cmd.action());
        assertEquals(now, cmd.openDate());

        // Defensive copy test
        long time = cmd.openDate().getTime();
        cmd.openDate().setTime(time + 10000);
        assertEquals(time, cmd.openDate().getTime(), "Mutation of returned Date should not affect record state");

        // Null openDate defaults to non-null current date
        RecordDrawerOpenedCommand cmd2 = RecordDrawerOpenedCommand.of("Cashier", "Cash Sale");
        assertNotNull(cmd2.openDate());
    }

    @Test
    @DisplayName("SystemService should dispatch RecordDrawerOpenedCommand and delegate legacy overload")
    void shouldDispatchDrawerOpenedCommand() throws BasicException {
        final RecordDrawerOpenedCommand[] captured = new RecordDrawerOpenedCommand[1];
        SystemService service = new SystemService() {
            @Override public String getDBVersion() { return "1.0"; }
            @Override public String findVersion() { return "1.0"; }
            @Override public String getUser() { return "admin"; }
            @Override public void execDrawerOpened(RecordDrawerOpenedCommand command) { captured[0] = command; }
            @Override public TableDefinition<ResourceInfo> getTableResources() { return null; }
            @Override public String getResourceAsXML(String sName) { return null; }
            @Override public String getResourceAsText(String sName) { return null; }
            @Override public byte[] getResourceAsBinary(String sName) { return null; }
            @Override public java.awt.image.BufferedImage getResourceAsImage(String sName) { return null; }
            @Override public java.util.Properties getResourceAsProperties(String sName) { return null; }
            @Override public void setResource(String name, int type, byte[] data) {}
            @Override public void setResourceAsBinary(String sName, byte[] data) {}
            @Override public void setResourceAsProperties(String sName, java.util.Properties p) {}
            @Override public java.util.List<AppUser> listPeopleVisible() { return java.util.Collections.emptyList(); }
            @Override public AppUser findPeopleByCard(String card) { return null; }
            @Override public String findRolePermissions(String sRole) { return null; }
            @Override public java.util.List<String> getPermissions(String role) { return java.util.Collections.emptyList(); }
            @Override public void execChangePassword(ChangePasswordCommand command) {}
            @Override public void execUpdatePermissions(UpdateRolePermissionsCommand command) {}
        };

        Date now = new Date();
        RecordDrawerOpenedCommand command = RecordDrawerOpenedCommand.of("admin", "Open Drawer", now);
        service.execDrawerOpened(command);
        assertNotNull(captured[0]);
        assertEquals("admin", captured[0].name());
        assertEquals("Open Drawer", captured[0].action());

        captured[0] = null;
        service.execDrawerOpened("cashier1", "Close Cash", now);
        assertNotNull(captured[0]);
        assertEquals("cashier1", captured[0].name());
        assertEquals("Close Cash", captured[0].action());
    }
}
