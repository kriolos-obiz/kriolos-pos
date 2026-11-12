package com.openbravo.pos.panels;

import com.openbravo.basic.BasicException;
import com.openbravo.data.user.DirtyManager;
import com.openbravo.pos.payment.PaymentMovement;
import java.util.Date;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test suite verifying type-safe lifecycle of PaymentsEditor.
 *
 * @author KriolOS
 */
@DisplayName("Payments Editor Type-Safe Lifecycle Test Suite")
class PaymentsEditorTest {

    @Test
    @DisplayName("Should handle PaymentMovement lifecycle type-safely without Object[] arrays")
    void shouldHandlePaymentsEditorLifecycle() throws BasicException {
        DirtyManager dirty = new DirtyManager();
        PaymentsEditor editor = new PaymentsEditor(dirty);

        // 1. Initial EOF
        editor.writeValueEOF();
        PaymentMovement eof = editor.createValue();
        assertNotNull(eof.receiptId());
        assertNotNull(eof.paymentId());
        assertNotNull(eof.date());
        assertEquals("", eof.notes());

        // 2. Insert
        editor.writeValueInsert();
        PaymentMovement inserted = editor.createValue();
        assertNotNull(inserted.receiptId());
        assertNotNull(inserted.paymentId());
        assertEquals("cashin", inserted.reason());

        // 3. Edit Cash In
        Date now = new Date();
        PaymentMovement movement = new PaymentMovement(
                "rec-101",
                "seq-1",
                now,
                "pay-101",
                "cashin",
                250.0,
                "Morning Float"
        );

        editor.writeValueEdit(movement);
        PaymentMovement edited = editor.createValue();
        assertEquals("rec-101", edited.receiptId());
        assertEquals("pay-101", edited.paymentId());
        assertEquals(now, edited.date());
        assertEquals("cashin", edited.reason());
        assertEquals(250.0, edited.total(), 0.001);
        assertEquals("Morning Float", edited.notes());

        // 4. Delete Cash In
        editor.writeValueDelete(movement);
        PaymentMovement deleted = editor.createValue();
        assertEquals("rec-101", deleted.receiptId());
        assertEquals("pay-101", deleted.paymentId());
        assertEquals("Morning Float", deleted.notes());

        // 5. Null guards
        editor.writeValueEdit(null);
        assertNotNull(editor.createValue().receiptId());
        editor.writeValueDelete(null);
        assertNotNull(editor.createValue().receiptId());
    }

    @Test
    @DisplayName("Should preserve cashout negative signum in PaymentsEditor")
    void shouldPreserveCashoutNegativeSignum() throws BasicException {
        DirtyManager dirty = new DirtyManager();
        PaymentsEditor editor = new PaymentsEditor(dirty);

        Date now = new Date();
        PaymentMovement cashout = new PaymentMovement(
                "rec-202",
                "seq-2",
                now,
                "pay-202",
                "cashout",
                -75.50,
                "Office Supplies"
        );

        editor.writeValueEdit(cashout);
        PaymentMovement result = editor.createValue();
        assertEquals("rec-202", result.receiptId());
        assertEquals("pay-202", result.paymentId());
        assertEquals("cashout", result.reason());
        assertEquals(-75.50, result.total(), 0.001);
        assertEquals("Office Supplies", result.notes());
    }
}
