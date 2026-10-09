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
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.BeanContainer;
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
        public int deactivateVoucher(String voucherNumber) throws BasicException {
            return 1;
        }

        @Override
        public TableDefinition getTableVouchers() {
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
}
