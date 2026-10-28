package com.openbravo.pos.payment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Component;
import java.awt.ComponentOrientation;
import javax.swing.JPanel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link JPaymentSelect} and subclasses.
 *
 * @author KriolOS
 */
class JPaymentSelectTest {

    @Test
    @DisplayName("Should create JPaymentSelectReceipt as JPanel with URN naming")
    void testReceiptDialogCreation() {
        JPanel parent = new JPanel();
        JPaymentSelect receipt = JPaymentSelectReceipt.getDialog(parent);

        assertNotNull(receipt);
        assertTrue(receipt instanceof JPanel, "JPaymentSelect must be a JPanel");
        assertEquals("kriolos:payment:select-panel", receipt.getName());
    }

    @Test
    @DisplayName("Should create JPaymentSelectRefund as JPanel with URN naming")
    void testRefundDialogCreation() {
        JPanel parent = new JPanel();
        JPaymentSelect refund = JPaymentSelectRefund.getDialog(parent);

        assertNotNull(refund);
        assertTrue(refund instanceof JPanel, "JPaymentSelectRefund must be a JPanel");
        assertEquals("kriolos:payment:select-panel", refund.getName());
    }

    @Test
    @DisplayName("Should create JPaymentSelectCustomer as JPanel with URN naming")
    void testCustomerDialogCreation() {
        JPanel parent = new JPanel();
        JPaymentSelect customer = JPaymentSelectCustomer.getDialog(parent);

        assertNotNull(customer);
        assertTrue(customer instanceof JPanel, "JPaymentSelectCustomer must be a JPanel");
        assertEquals("kriolos:payment:select-panel", customer.getName());
    }

    @Test
    @DisplayName("Should support constructors with ComponentOrientation")
    void testComponentOrientationConstructors() {
        JPaymentSelectReceipt receipt = new JPaymentSelectReceipt(new JPanel(), ComponentOrientation.RIGHT_TO_LEFT);
        assertEquals(ComponentOrientation.RIGHT_TO_LEFT, receipt.getComponentOrientation());

        JPaymentSelectRefund refund = new JPaymentSelectRefund(new JPanel(), ComponentOrientation.LEFT_TO_RIGHT);
        assertEquals(ComponentOrientation.LEFT_TO_RIGHT, refund.getComponentOrientation());

        JPaymentSelectCustomer customer = new JPaymentSelectCustomer(new JPanel(), ComponentOrientation.LEFT_TO_RIGHT);
        assertEquals(ComponentOrientation.LEFT_TO_RIGHT, customer.getComponentOrientation());
    }

    @Test
    @DisplayName("Should toggle printer selection state correctly")
    void testPrinterToggle() {
        JPaymentSelectReceipt receipt = new JPaymentSelectReceipt();
        receipt.setPrintSelected(true);
        assertTrue(receipt.isPrintSelected());

        receipt.setPrintSelected(false);
        assertFalse(receipt.isPrintSelected());
    }
}
