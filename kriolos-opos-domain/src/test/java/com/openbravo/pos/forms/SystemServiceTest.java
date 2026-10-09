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
package com.openbravo.pos.forms;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.TableDefinition;
import com.openbravo.pos.admin.ResourceInfo;
import java.awt.image.BufferedImage;
import java.lang.reflect.Proxy;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import org.junit.jupiter.api.DisplayName;
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

        Object resourceBean = BeanContainer.getBean("com.openbravo.pos.forms.ResourceService", appViewProxy);
        assertNotNull(resourceBean, "BeanContainer should resolve ResourceService");
        assertInstanceOf(DataLogicSystem.class, resourceBean, "ResourceService should resolve to DataLogicSystem");
        assertInstanceOf(ResourceService.class, resourceBean, "DataLogicSystem should implement ResourceService");

        Object systemBean = BeanContainer.getBean("com.openbravo.pos.forms.SystemService", appViewProxy);
        assertNotNull(systemBean, "BeanContainer should resolve SystemService");
        assertInstanceOf(DataLogicSystem.class, systemBean, "SystemService should resolve to DataLogicSystem");
        assertInstanceOf(SystemService.class, systemBean, "DataLogicSystem should implement SystemService");
        assertInstanceOf(SecurityService.class, systemBean, "SystemService should also implement SecurityService");
    }

    @Test
    @DisplayName("Mock SystemService should satisfy domain port contracts")
    void testSystemServiceContract() throws BasicException {
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
            public void execDrawerOpened(String name, String action, Date openDate) {
            }

            @Override
            public TableDefinition<ResourceInfo> getTableResources() {
                return null;
            }

            @Override
            public byte[] getResourceAsBinary(String sName) {
                return "binary".getBytes();
            }

            @Override
            public String getResourceAsText(String sName) {
                return "text";
            }

            @Override
            public String getResourceAsXML(String sName) {
                return "<xml/>";
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
            public void setResource(String name, int type, byte[] data) {
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
            public void execChangePassword(Object[] userdata) {
            }

            @Override
            public void execUpdatePermissions(Object[] permissions) {
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
    }
}
