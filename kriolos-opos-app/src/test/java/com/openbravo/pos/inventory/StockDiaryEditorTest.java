package com.openbravo.pos.inventory;

import com.openbravo.basic.BasicException;
import com.openbravo.data.user.DirtyManager;
import java.util.Date;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test suite verifying type-safe lifecycle, validation, and data binding of StockDiaryEditor.
 *
 * @author KriolOS
 */
@DisplayName("Stock Diary Editor Type-Safe Lifecycle Test Suite")
class StockDiaryEditorTest {

    @Test
    @DisplayName("Should handle StockDiaryInfo lifecycle type-safely without Object[] arrays")
    void shouldHandleStockDiaryEditorLifecycle() throws BasicException {
        DirtyManager dirty = new DirtyManager();
        StockDiaryEditor editor = new StockDiaryEditor(dirty);

        // 1. Initial EOF
        editor.writeValueEOF();
        StockDiaryInfo eof = editor.createValue();
        assertNotNull(eof.id());
        assertNotNull(eof.date());

        // 2. Insert
        editor.writeValueInsert();
        StockDiaryInfo inserted = editor.createValue();
        assertNotNull(inserted.id());
        assertNotNull(inserted.date());

        // 3. Edit Stock Movement
        Date now = new Date();
        StockDiaryInfo entry = new StockDiaryInfo(
                "diary-101",
                now,
                1,
                "loc-main",
                "prod-100",
                "att-inst-01",
                50.0,
                15.75,
                "admin",
                "SKU-100",
                "BAR-100",
                "Premium Widget",
                "att-01",
                "Color: Blue",
                "supp-99"
        );

        editor.writeValueEdit(entry);
        StockDiaryInfo edited = editor.createValue();
        assertEquals("diary-101", edited.id());
        assertEquals("loc-main", edited.locationId());
        assertEquals(1, edited.reason());
        assertEquals(50.0, edited.units(), 0.001);
        assertEquals(15.75, edited.price(), 0.001);
        assertEquals("Premium Widget", edited.productName());
        assertEquals("SKU-100", edited.productRef());

        // 4. Delete Stock Movement
        editor.writeValueDelete(entry);
        StockDiaryInfo deleted = editor.createValue();
        assertEquals("diary-101", deleted.id());
        assertEquals("Premium Widget", deleted.productName());

        // 5. Null guards
        editor.writeValueEdit(null);
        assertNotNull(editor.createValue().id());
        editor.writeValueDelete(null);
        assertNotNull(editor.createValue().id());
    }

    @Test
    @DisplayName("Should handle outgoing movement signum correctly")
    void shouldHandleOutgoingMovementSignum() throws BasicException {
        DirtyManager dirty = new DirtyManager();
        StockDiaryEditor editor = new StockDiaryEditor(dirty);

        Date now = new Date();
        StockDiaryInfo outMovement = new StockDiaryInfo(
                "diary-202",
                now,
                -1,
                "loc-secondary",
                "prod-200",
                null,
                -10.0,
                20.0,
                "manager",
                "SKU-200",
                "BAR-200",
                "Standard Part",
                "",
                "",
                null
        );

        editor.writeValueEdit(outMovement);
        StockDiaryInfo result = editor.createValue();
        assertEquals("diary-202", result.id());
        assertEquals(-1, result.reason());
        assertEquals(-10.0, result.units(), 0.001);
        assertEquals(20.0, result.price(), 0.001);
    }

    @Test
    @DisplayName("Should validate mandatory product and units in StockDiaryEditor")
    void shouldValidateMandatoryProductAndUnits() {
        StockDiaryEditor editor = new StockDiaryEditor(new DirtyManager());
        editor.writeValueInsert();

        // 1. Initial insert state (product is empty, units is empty) -> invalid
        assertFalse(editor.isValid());
        assertFalse(editor.isDataValid());

        // 2. Set product but leave units empty -> invalid
        editor.getProductField().setText("Steel Bolt M8");
        assertFalse(editor.isValid());

        // 3. Set units to zero -> invalid
        editor.getUnitsField().setText("0.0");
        assertFalse(editor.isValid());

        // 4. Set valid units -> valid
        editor.getUnitsField().setText("25.0");
        assertTrue(editor.isValid());
        assertTrue(editor.isDataValid());
    }

    @Test
    @DisplayName("Should track dirty state when StockDiaryEditor fields change")
    void shouldTrackDirtyStateWhenFieldsChange() {
        DirtyManager dirty = new DirtyManager();
        StockDiaryEditor editor = new StockDiaryEditor(dirty);
        editor.writeValueInsert();
        dirty.setDirty(false);
        assertFalse(dirty.isDirty());

        // Modifying product triggers dirty
        editor.getProductField().setText("Copper Wire");
        assertTrue(dirty.isDirty());

        // Reset and test units
        dirty.setDirty(false);
        assertFalse(dirty.isDirty());
        editor.getUnitsField().setText("100.0");
        assertTrue(dirty.isDirty());

        // Reset and test price
        dirty.setDirty(false);
        assertFalse(dirty.isDirty());
        editor.getPriceField().setText("5.25");
        assertTrue(dirty.isDirty());
    }

    @Test
    @DisplayName("Should safely handle null edit and delete in StockDiaryEditor")
    void shouldSafelyHandleNullEditAndDelete() {
        StockDiaryEditor editor = new StockDiaryEditor(new DirtyManager());
        assertDoesNotThrow(() -> editor.writeValueEdit(null));
        assertDoesNotThrow(() -> editor.writeValueDelete(null));
    }

    @Test
    @DisplayName("Should expose component accessors for headless testing")
    void shouldExposeComponentAccessors() {
        StockDiaryEditor editor = new StockDiaryEditor(new DirtyManager());
        assertNotNull(editor.getDateField());
        assertNotNull(editor.getUnitsField());
        assertNotNull(editor.getPriceField());
        assertNotNull(editor.getProductField());
        assertNotNull(editor.getReferenceField());
        assertNotNull(editor.getCodebarField());
        assertNotNull(editor.getReasonCombo());
        assertNotNull(editor.getLocationCombo());
        assertNotNull(editor.getComponent());
    }
}
