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
package com.openbravo.pos.payment;

import com.openbravo.basic.BasicException;
import com.openbravo.data.user.SaveProvider;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.BeanContainer;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TreasuryService Port Test Suite")
class TreasuryServiceTest {

    private static class MockTreasuryService implements TreasuryService {
        final List<PaymentMovement> recordedMovements = new ArrayList<>();
        final List<String> deletedMovements = new ArrayList<>();

        @Override
        public void recordPaymentMovement(
                String receiptId,
                String activeCashIndex,
                Date date,
                String paymentId,
                String reason,
                double total,
                String notes
        ) throws BasicException {
            recordedMovements.add(new PaymentMovement(receiptId, activeCashIndex, date, paymentId, reason, total, notes));
        }

        @Override
        public void deletePaymentMovement(String receiptId, String paymentId) throws BasicException {
            deletedMovements.add(receiptId + ":" + paymentId);
        }

        @Override
        public SaveProvider getPaymentMovementSaveProvider() {
            return null;
        }

        @Override
        public Integer getNextTicketPaymentIndex() throws BasicException {
            return 42;
        }
    }

    @Test
    @DisplayName("Should delegate recordPaymentMovement with domain record")
    void shouldDelegateRecordPaymentMovementRecord() throws BasicException {
        MockTreasuryService service = new MockTreasuryService();
        PaymentMovement movement = new PaymentMovement(
                "rec-123",
                "cash-1",
                new Date(),
                "pay-456",
                "cashin",
                200.0,
                "Float"
        );

        service.recordPaymentMovement(movement);

        assertEquals(1, service.recordedMovements.size());
        assertEquals("rec-123", service.recordedMovements.get(0).receiptId());
        assertEquals(200.0, service.recordedMovements.get(0).total(), 0.001);
    }

    @Test
    @DisplayName("Should ignore null PaymentMovement in default method")
    void shouldIgnoreNullPaymentMovement() throws BasicException {
        MockTreasuryService service = new MockTreasuryService();
        service.recordPaymentMovement((PaymentMovement) null);
        assertTrue(service.recordedMovements.isEmpty());
    }

    @Test
    @DisplayName("Should resolve TreasuryService in BeanContainer to DataLogicPayments")
    void shouldResolveTreasuryServiceInBeanContainer() {
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

        Object bean = BeanContainer.getBean("com.openbravo.pos.payment.TreasuryService", appViewProxy);
        assertNotNull(bean, "BeanContainer should resolve TreasuryService");
        assertInstanceOf(DataLogicPayments.class, bean, "TreasuryService should resolve to DataLogicPayments");
        assertInstanceOf(TreasuryService.class, bean, "DataLogicPayments should implement TreasuryService");
    }
}
