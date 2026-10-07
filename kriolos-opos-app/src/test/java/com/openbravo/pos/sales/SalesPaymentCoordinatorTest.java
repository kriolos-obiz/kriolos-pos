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
import com.openbravo.pos.payment.JPaymentSelect;
import com.openbravo.pos.payment.JPaymentSelectReceipt;
import com.openbravo.pos.payment.JPaymentSelectRefund;
import com.openbravo.pos.ticket.TaxInfo;
import com.openbravo.pos.ticket.TicketInfo;
import com.openbravo.pos.ticket.TicketLineInfo;
import java.lang.reflect.Proxy;
import javax.swing.JPanel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SalesPaymentCoordinator Unit Tests")
public class SalesPaymentCoordinatorTest {

    private SalesPaymentCoordinator coordinator;
    private TicketInfo ticket;

    @BeforeEach
    void setUp() {
        AppView app = (AppView) Proxy.newProxyInstance(
                AppView.class.getClassLoader(),
                new Class<?>[]{AppView.class},
                (proxy, method, args) -> null);

        SalesService salesService = new SalesServiceImpl(new TaxesLogic(java.util.Collections.emptyList()));
        TicketLifecycleService ticketLifecycleService = (TicketLifecycleService) Proxy.newProxyInstance(
                TicketLifecycleService.class.getClassLoader(),
                new Class<?>[]{TicketLifecycleService.class},
                (proxy, method, args) -> null);

        coordinator = new SalesPaymentCoordinator(app, ticketLifecycleService, salesService);
        ticket = new TicketInfo();
    }

    @Test
    @DisplayName("Should detect warranty products on ticket lines")
    void testHasWarrantyProduct() {
        assertFalse(coordinator.hasWarrantyProduct(null));
        assertFalse(coordinator.hasWarrantyProduct(ticket));

        TaxInfo tax = new TaxInfo("tax-01", "Standard", "1", "cat-01", null, 0.10, false, 0);
        TicketLineInfo lineNormal = new TicketLineInfo("Item 1", "cat-01", 1.0, 10.0, tax);
        lineNormal.setProperty("product.warranty", "false");
        ticket.addLine(lineNormal);

        assertFalse(coordinator.hasWarrantyProduct(ticket));

        TicketLineInfo lineWarranty = new TicketLineInfo("Item 2", "cat-01", 1.0, 50.0, tax);
        lineWarranty.setProperty("product.warranty", "true");
        ticket.addLine(lineWarranty);

        assertTrue(coordinator.hasWarrantyProduct(ticket));
    }

    @Test
    @DisplayName("Should resolve correct payment dialog based on ticket receipt type")
    void testResolvePaymentDialog() {
        JPanel parent = new JPanel();
        JPaymentSelect receiptDlg = JPaymentSelectReceipt.getDialog(parent);
        JPaymentSelect refundDlg = JPaymentSelectRefund.getDialog(parent);

        ticket.setTicketType(TicketInfo.RECEIPT_NORMAL);
        assertSame(receiptDlg, coordinator.resolvePaymentDialog(ticket, receiptDlg, refundDlg));

        ticket.setTicketType(TicketInfo.RECEIPT_REFUND);
        assertSame(refundDlg, coordinator.resolvePaymentDialog(ticket, receiptDlg, refundDlg));

        assertNull(coordinator.resolvePaymentDialog(null, receiptDlg, refundDlg));
    }

    @Test
    @DisplayName("Should expose injected TicketLifecycleService port")
    void testTicketLifecycleServiceAccess() {
        assertNotNull(coordinator.getTicketLifecycleService());
    }
}
