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
package com.openbravo.pos.inventory;

import com.openbravo.basic.BasicException;
import com.openbravo.pos.panels.AppView;
import com.openbravo.pos.container.BeanContainer;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AttributeService Port Test Suite")
class AttributeServiceTest {

    @Test
    @DisplayName("AttributeInstEntry record should hold values accurately")
    void testAttributeInstEntryRecord() {
        AttributeInstEntry entry = new AttributeInstEntry("attr-1", "Red");
        assertEquals("attr-1", entry.attributeId());
        assertEquals("Red", entry.value());
    }

    private static class MockAttributeService implements AttributeService {
        final List<AttributeInstEntry> createdEntries = new ArrayList<>();

        @Override
        public List<AttributeInfo> getAttributeList() throws BasicException {
            return List.of(new AttributeInfo("a1", "Color"), new AttributeInfo("a2", "Size"));
        }

        @Override
        public List<AttributeSetInfo> getAttributeSetList() throws BasicException {
            return List.of(new AttributeSetInfo("s1", "Clothing Set"));
        }

        @Override
        public AttributeSetInfo findAttributeSet(String attributeSetId) throws BasicException {
            return "s1".equals(attributeSetId) ? new AttributeSetInfo("s1", "Clothing Set") : null;
        }

        @Override
        public List<AttributeInstInfo> getAttributeInstList(String attributeSetId, String attributeSetInstanceId) throws BasicException {
            return List.of(new AttributeInstInfo("a1", "Color", null, null));
        }

        @Override
        public List<String> getAttributeValues(String attributeId) throws BasicException {
            return "a1".equals(attributeId) ? List.of("Red", "Blue", "Green") : Collections.emptyList();
        }

        @Override
        public String findAttributeSetInstanceId(String attributeSetId, String description) throws BasicException {
            return "existing-desc".equals(description) ? "inst-existing" : null;
        }

        @Override
        public String findOrCreateAttributeSetInstance(CreateAttributeSetInstanceCommand command) throws BasicException {
            if (command.description() == null || command.description().trim().isEmpty()) {
                return null;
            }
            if ("existing-desc".equals(command.description())) {
                return "inst-existing";
            }
            createdEntries.addAll(command.entries());
            return "inst-new-123";
        }
    }

    @Test
    @DisplayName("Should resolve existing instance ID when found")
    void shouldResolveExistingInstance() throws BasicException {
        MockAttributeService service = new MockAttributeService();
        String id = service.findOrCreateAttributeSetInstance("s1", "existing-desc", List.of());
        assertEquals("inst-existing", id);
        assertTrue(service.createdEntries.isEmpty());
    }

    @Test
    @DisplayName("Should create new instance and record entries when not found")
    void shouldCreateNewInstance() throws BasicException {
        MockAttributeService service = new MockAttributeService();
        List<AttributeInstEntry> entries = List.of(new AttributeInstEntry("a1", "Blue"));
        String id = service.findOrCreateAttributeSetInstance("s1", "new-desc", entries);
        assertEquals("inst-new-123", id);
        assertEquals(1, service.createdEntries.size());
        assertEquals("Blue", service.createdEntries.get(0).value());
    }

    @Test
    @DisplayName("Should return null for empty description")
    void shouldReturnNullForEmptyDescription() throws BasicException {
        MockAttributeService service = new MockAttributeService();
        assertNull(service.findOrCreateAttributeSetInstance("s1", "", List.of()));
        assertNull(service.findOrCreateAttributeSetInstance("s1", null, List.of()));
    }

    @Test
    @DisplayName("Should resolve AttributeService in BeanContainer to DataLogicAttribute")
    void shouldResolveAttributeServiceInBeanContainer() {
        AppView appViewProxy = (AppView) Proxy.newProxyInstance(
                AppView.class.getClassLoader(),
                new Class<?>[]{AppView.class},
                (proxy, method, args) -> {
                    if ("getSession".equals(method.getName())) {
                        return null;
                    }
                    return null;
                }
        );

        Object bean = BeanContainer.getBean("com.openbravo.pos.inventory.AttributeService", appViewProxy);
        assertNotNull(bean, "BeanContainer should resolve AttributeService");
        assertInstanceOf(DataLogicAttribute.class, bean, "AttributeService should resolve to DataLogicAttribute");
        assertInstanceOf(AttributeService.class, bean, "DataLogicAttribute should implement AttributeService");
    }

    @Test
    @DisplayName("CreateAttributeSetInstanceCommand should enforce invariants and immutability")
    void shouldEnforceCreateAttributeSetInstanceCommandInvariants() {
        assertThrows(NullPointerException.class,
                () -> new CreateAttributeSetInstanceCommand(null, "desc", List.of()),
                "Null attributeSetId should throw NullPointerException");
        assertThrows(IllegalArgumentException.class,
                () -> new CreateAttributeSetInstanceCommand("", "desc", List.of()),
                "Empty attributeSetId should throw IllegalArgumentException");
        assertThrows(IllegalArgumentException.class,
                () -> new CreateAttributeSetInstanceCommand("   ", "desc", List.of()),
                "Blank attributeSetId should throw IllegalArgumentException");

        // Null description defaults to empty string
        CreateAttributeSetInstanceCommand nullDescCmd = new CreateAttributeSetInstanceCommand("set-1", null, null);
        assertEquals("", nullDescCmd.description());
        assertNotNull(nullDescCmd.entries());
        assertTrue(nullDescCmd.entries().isEmpty());

        // Defensive copy test
        java.util.ArrayList<AttributeInstEntry> mutableList = new java.util.ArrayList<>();
        mutableList.add(new AttributeInstEntry("attr-1", "Red"));
        CreateAttributeSetInstanceCommand cmd = CreateAttributeSetInstanceCommand.of("set-1", "Color: Red", mutableList);
        assertEquals(1, cmd.entries().size());

        mutableList.add(new AttributeInstEntry("attr-2", "Large"));
        assertEquals(1, cmd.entries().size(), "Modifying original list should not affect command record entries");
        assertThrows(UnsupportedOperationException.class,
                () -> cmd.entries().add(new AttributeInstEntry("attr-3", "Silk")),
                "Command entries list must be unmodifiable");
    }

    @Test
    @DisplayName("Should dispatch CreateAttributeSetInstanceCommand via typed port method")
    void shouldDispatchCreateAttributeSetInstanceCommand() throws BasicException {
        MockAttributeService service = new MockAttributeService();
        List<AttributeInstEntry> entries = List.of(new AttributeInstEntry("a1", "Green"));

        CreateAttributeSetInstanceCommand cmd = CreateAttributeSetInstanceCommand.of("s1", "new-desc", entries);
        String id = service.findOrCreateAttributeSetInstance(cmd);
        assertEquals("inst-new-123", id);
        assertEquals(1, service.createdEntries.size());
        assertEquals("Green", service.createdEntries.get(0).value());

        // Existing
        CreateAttributeSetInstanceCommand existingCmd = CreateAttributeSetInstanceCommand.of("s1", "existing-desc", List.of());
        assertEquals("inst-existing", service.findOrCreateAttributeSetInstance(existingCmd));

        // Blank/null description returns null
        CreateAttributeSetInstanceCommand blankCmd = CreateAttributeSetInstanceCommand.of("s1", "   ", List.of());
        assertNull(service.findOrCreateAttributeSetInstance(blankCmd));
    }
}
