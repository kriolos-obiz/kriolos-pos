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
package com.openbravo.pos.voucher;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.TableDefinition;
import com.openbravo.pos.panels.AppView;
import com.openbravo.pos.container.BeanContainer;
import java.lang.reflect.Proxy;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("VoucherService Port Test Suite")
class VoucherServiceTest {

    private static class MockVoucherService implements VoucherService {
        String lastSuffix = null;
        CreateVoucherCommand capturedCreate = null;
        DeactivateVoucherCommand capturedDeactivate = null;

        @Override
        public List<VoucherInfo> getVoucherList() throws BasicException {
            return List.of(new VoucherInfo("v1", "VO-10-26-00001", "c1", "Alice", 100.0, "A"));
        }

        @Override
        public VoucherInfo getVoucher(String id) throws BasicException {
            return "v1".equals(id) ? new VoucherInfo("v1", "VO-10-26-00001", "c1", "Alice", 100.0, "A") : null;
        }

        @Override
        public VoucherInfo getVoucherAll(String id) throws BasicException {
            return "v1".equals(id) ? new VoucherInfo("v1", "VO-10-26-00001", "c1", "Alice", 100.0, "A") : null;
        }

        @Override
        public String findLastVoucherNumber(String prefix) throws BasicException {
            return lastSuffix;
        }

        @Override
        public void createVoucher(CreateVoucherCommand command) throws BasicException {
            this.capturedCreate = command;
        }

        @Override
        public int deactivateVoucher(DeactivateVoucherCommand command) throws BasicException {
            this.capturedDeactivate = command;
            return 1;
        }

        @Override
        public TableDefinition<VoucherInfo> getTableVouchers() {
            return null;
        }
    }

    @Test
    @DisplayName("Should generate first voucher number 00001 when no existing vouchers match prefix")
    void shouldGenerateFirstVoucherNumber() throws BasicException {
        MockVoucherService service = new MockVoucherService();
        service.lastSuffix = null;

        String voucherNumber = service.generateNextVoucherNumber();
        String expectedPrefix = "VO-" + new SimpleDateFormat("MM-yy").format(new Date());
        assertEquals(expectedPrefix + "-00001", voucherNumber);
    }

    @Test
    @DisplayName("Should increment sequence number when last voucher exists")
    void shouldIncrementExistingVoucherNumber() throws BasicException {
        MockVoucherService service = new MockVoucherService();
        service.lastSuffix = "00042";

        String voucherNumber = service.generateNextVoucherNumber();
        String expectedPrefix = "VO-" + new SimpleDateFormat("MM-yy").format(new Date());
        assertEquals(expectedPrefix + "-00043", voucherNumber);
    }

    @Test
    @DisplayName("Should resolve VoucherService in BeanContainer to DataLogicVouchers")
    void shouldResolveVoucherServiceInBeanContainer() {
        AppView appViewProxy = (AppView) Proxy.newProxyInstance(
                AppView.class.getClassLoader(),
                new Class<?>[]{AppView.class},
                (proxy, method, args) -> null
        );

        Object bean = BeanContainer.getBean("com.openbravo.pos.voucher.VoucherService", appViewProxy);
        assertNotNull(bean, "BeanContainer should resolve VoucherService");
        assertInstanceOf(DataLogicVouchers.class, bean, "VoucherService should resolve to DataLogicVouchers");
        assertInstanceOf(VoucherService.class, bean, "DataLogicVouchers should implement VoucherService");
    }

    @Test
    @DisplayName("CreateVoucherCommand should enforce invariants")
    void shouldEnforceCreateVoucherCommandInvariants() {
        assertThrows(NullPointerException.class,
                () -> new CreateVoucherCommand(null, "VO-11-26-00001", "c1", 50.0, "A"),
                "Null id should throw NullPointerException");
        assertThrows(IllegalArgumentException.class,
                () -> new CreateVoucherCommand("", "VO-11-26-00001", "c1", 50.0, "A"),
                "Empty id should throw IllegalArgumentException");
        assertThrows(NullPointerException.class,
                () -> new CreateVoucherCommand("v1", null, "c1", 50.0, "A"),
                "Null voucher number should throw NullPointerException");
        assertThrows(IllegalArgumentException.class,
                () -> new CreateVoucherCommand("v1", "   ", "c1", 50.0, "A"),
                "Blank voucher number should throw IllegalArgumentException");
        assertThrows(NullPointerException.class,
                () -> new CreateVoucherCommand("v1", "VO-11-26-00001", null, 50.0, "A"),
                "Null customer id should throw NullPointerException");
        assertThrows(IllegalArgumentException.class,
                () -> new CreateVoucherCommand("v1", "VO-11-26-00001", "  ", 50.0, "A"),
                "Blank customer id should throw IllegalArgumentException");
        assertThrows(IllegalArgumentException.class,
                () -> new CreateVoucherCommand("v1", "VO-11-26-00001", "c1", 0.0, "A"),
                "Zero amount should throw IllegalArgumentException");
        assertThrows(IllegalArgumentException.class,
                () -> new CreateVoucherCommand("v1", "VO-11-26-00001", "c1", -15.5, "A"),
                "Negative amount should throw IllegalArgumentException");

        CreateVoucherCommand cmd = CreateVoucherCommand.of("v1", "VO-11-26-00001", "c1", 75.0);
        assertEquals("v1", cmd.id());
        assertEquals("VO-11-26-00001", cmd.voucherNumber());
        assertEquals("c1", cmd.customerId());
        assertEquals(75.0, cmd.amount());
        assertEquals("A", cmd.status());
    }

    @Test
    @DisplayName("DeactivateVoucherCommand should enforce invariants")
    void shouldEnforceDeactivateVoucherCommandInvariants() {
        assertThrows(NullPointerException.class,
                () -> new DeactivateVoucherCommand(null),
                "Null voucher number should throw NullPointerException");
        assertThrows(IllegalArgumentException.class,
                () -> new DeactivateVoucherCommand("   "),
                "Blank voucher number should throw IllegalArgumentException");

        DeactivateVoucherCommand cmd = DeactivateVoucherCommand.of("VO-11-26-00002");
        assertEquals("VO-11-26-00002", cmd.voucherNumber());
    }

    @Test
    @DisplayName("Should dispatch voucher commands and delegate legacy deactivation")
    void shouldDispatchVoucherCommands() throws BasicException {
        MockVoucherService service = new MockVoucherService();

        CreateVoucherCommand createCmd = CreateVoucherCommand.of("v-99", "VO-11-26-00099", "cust-9", 150.0);
        service.createVoucher(createCmd);
        assertNotNull(service.capturedCreate);
        assertEquals("v-99", service.capturedCreate.id());
        assertEquals(150.0, service.capturedCreate.amount());

        DeactivateVoucherCommand deactCmd = DeactivateVoucherCommand.of("VO-11-26-00099");
        int count = service.deactivateVoucher(deactCmd);
        assertEquals(1, count);
        assertNotNull(service.capturedDeactivate);
        assertEquals("VO-11-26-00099", service.capturedDeactivate.voucherNumber());

        // Test legacy overload delegation
        service.capturedDeactivate = null;
        int legacyCount = service.deactivateVoucher("VO-11-26-00042");
        assertEquals(1, legacyCount);
        assertNotNull(service.capturedDeactivate);
        assertEquals("VO-11-26-00042", service.capturedDeactivate.voucherNumber());
    }
}
