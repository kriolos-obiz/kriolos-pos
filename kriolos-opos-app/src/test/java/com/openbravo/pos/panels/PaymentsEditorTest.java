package com.openbravo.pos.panels;

import com.openbravo.basic.BasicException;
import com.openbravo.data.user.DirtyManager;
import com.openbravo.pos.payment.PaymentMovement;
import java.util.Date;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test suite verifying type-safe lifecycle, two-way data binding,
 * and validation of {@link PaymentsEditor}.
 *
 * @author KriolOS
 */
@DisplayName("Payments Editor Type-Safe Lifecycle & Validation Test Suite")
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

    @Test
    @DisplayName("Should validate valid payment successfully with SwingBinder")
    void shouldValidateValidPaymentWithSwingBinder() throws BasicException {
        DirtyManager dirty = new DirtyManager();
        PaymentsEditor editor = new PaymentsEditor(dirty);

        Date now = new Date();
        PaymentMovement validCashin = new PaymentMovement(
                "rec-303",
                "seq-3",
                now,
                "pay-303",
                "cashin",
                120.00,
                "Mid-day Top-up"
        );

        editor.writeValueEdit(validCashin);

        assertTrue(editor.isValid(), "Valid payment movement must pass validation");
        assertTrue(editor.isDataValid(), "isDataValid() must return true for valid payment");
        assertNotNull(editor.getBinder(), "SwingBinder instance must be present");

        PaymentMovement created = editor.createValue();
        assertEquals(120.00, created.total(), 0.001);
        assertEquals("cashin", created.reason());
        assertEquals("Mid-day Top-up", created.notes());
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.0, -0.0})
    @DisplayName("Should fail validation when payment total is zero")
    void shouldFailValidationWhenTotalIsZero(double zeroAmount) throws BasicException {
        DirtyManager dirty = new DirtyManager();
        PaymentsEditor editor = new PaymentsEditor(dirty);

        PaymentMovement invalidPayment = new PaymentMovement(
                "rec-404",
                "seq-4",
                new Date(),
                "pay-404",
                "cashin",
                zeroAmount,
                "Zero amount"
        );

        editor.writeValueEdit(invalidPayment);

        assertFalse(editor.isValid(), "Payment movement with zero total must fail validation");
        assertFalse(editor.isDataValid(), "isDataValid() must return false when validation fails");
    }

    @Test
    @DisplayName("Should fail validation when payment total is null or not entered")
    void shouldFailValidationWhenTotalIsNull() {
        DirtyManager dirty = new DirtyManager();
        PaymentsEditor editor = new PaymentsEditor(dirty);

        editor.writeValueInsert();

        assertFalse(editor.isValid(), "Payment without amount must fail validation");
        assertFalse(editor.isDataValid(), "isDataValid() must return false for empty amount");
    }

    @Test
    @DisplayName("Should track dirty state when payment total is modified")
    void shouldTrackDirtyStateWhenTotalIsModified() {
        DirtyManager dirty = new DirtyManager();
        PaymentsEditor editor = new PaymentsEditor(dirty);

        editor.writeValueInsert();
        dirty.setDirty(false);
        assertFalse(dirty.isDirty(), "Dirty flag should be cleared after manual reset");

        editor.getTotalEditor().setDoubleValue(85.0);
        assertTrue(dirty.isDirty(), "Modifying total editor must mark dirty state");
    }

    @Test
    @DisplayName("Should track dirty state when payment notes are modified")
    void shouldTrackDirtyStateWhenNotesAreModified() {
        DirtyManager dirty = new DirtyManager();
        PaymentsEditor editor = new PaymentsEditor(dirty);

        editor.writeValueInsert();
        dirty.setDirty(false);
        assertFalse(dirty.isDirty(), "Dirty flag should be cleared after manual reset");

        editor.getNotesEditor().setText("Updated reason note");
        assertTrue(dirty.isDirty(), "Modifying notes editor must mark dirty state");
    }

    @Test
    @DisplayName("Should safely handle null values during writeValueEdit and writeValueDelete")
    void shouldHandleNullGuardsInPaymentsLifecycle() {
        DirtyManager dirty = new DirtyManager();
        PaymentsEditor editor = new PaymentsEditor(dirty);

        assertDoesNotThrow(() -> editor.writeValueEdit(null),
                "writeValueEdit(null) must safely fallback to EOF without throwing NPE");
        assertDoesNotThrow(() -> editor.writeValueDelete(null),
                "writeValueDelete(null) must safely fallback to EOF without throwing NPE");
    }

    @Test
    @DisplayName("Should expose component, accessors, and handle refresh safely")
    void shouldExposeComponentAndAccessors() {
        DirtyManager dirty = new DirtyManager();
        PaymentsEditor editor = new PaymentsEditor(dirty);

        assertSame(editor, editor.getComponent(), "getComponent() should return editor instance");
        assertNotNull(editor.getTotalEditor(), "getTotalEditor() should return JEditorCurrency");
        assertNotNull(editor.getNotesEditor(), "getNotesEditor() should return JEditorString");
        assertNotNull(editor.getReasonComboBox(), "getReasonComboBox() should return JComboBox");
        assertNotNull(editor.getBinder(), "getBinder() should return SwingBinder");
        assertDoesNotThrow(editor::refresh, "refresh() should execute safely");
    }
}
