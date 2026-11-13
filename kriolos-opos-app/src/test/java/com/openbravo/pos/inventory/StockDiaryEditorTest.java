package com.openbravo.pos.inventory;

import com.openbravo.basic.BasicException;
import com.openbravo.data.user.DirtyManager;
import java.util.Date;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test suite verifying type-safe lifecycle of StockDiaryEditor.
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
}
