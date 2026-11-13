package com.openbravo.pos.sales.restaurant;

import com.openbravo.basic.BasicException;
import com.openbravo.data.user.DirtyManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test suite verifying type-safe lifecycle, two-way data binding,
 * and validation of PlacesEditor.
 *
 * @author KriolOS
 */
@DisplayName("Places Editor Type-Safe Lifecycle & Validation Test Suite")
class PlacesEditorTest {

    @Test
    @DisplayName("Should handle Place lifecycle type-safely without Object[] arrays")
    void shouldHandlePlacesEditorLifecycle() throws BasicException {
        DirtyManager dirty = new DirtyManager();
        PlacesEditor view = new PlacesEditor(dirty);

        // 1. Initial EOF
        view.writeValueEOF();
        Place eof = view.createValue();
        assertNull(eof.getId());
        assertEquals("", eof.getName());
        assertEquals("", eof.getSeats());
        assertEquals(0, eof.getX());
        assertEquals(0, eof.getY());

        // 2. Insert
        view.writeValueInsert();
        Place insertValue = view.createValue();
        assertNotNull(insertValue.getId());

        // 3. Edit
        Place table = new Place("place-1", "Table 12", "4", 120, 240, "floor-main");
        view.writeValueEdit(table);

        Place created = view.createValue();
        assertEquals("place-1", created.getId());
        assertEquals("Table 12", created.getName());
        assertEquals("4", created.getSeats());
        assertEquals(120, created.getX());
        assertEquals(240, created.getY());
        assertEquals("floor-main", created.getFloor());

        // 4. Delete
        view.writeValueDelete(table);
        Place deletedState = view.createValue();
        assertEquals("place-1", deletedState.getId());
        assertEquals("Table 12", deletedState.getName());
        assertEquals("4", deletedState.getSeats());
        assertEquals(120, deletedState.getX());
        assertEquals(240, deletedState.getY());
        assertEquals("floor-main", deletedState.getFloor());

        // 5. EOF reset
        view.writeValueEOF();
        Place resetState = view.createValue();
        assertNull(resetState.getId());
        assertEquals("", resetState.getName());
    }

    @Test
    @DisplayName("Should validate valid table successfully with SwingBinder")
    void shouldValidateValidTableWithSwingBinder() throws BasicException {
        DirtyManager dirty = new DirtyManager();
        PlacesEditor view = new PlacesEditor(dirty);

        Place table = new Place("place-vip", "VIP Booth A", "6", 50, 100, "floor-vip");
        view.writeValueEdit(table);

        assertTrue(view.isValid(), "Valid table entity must pass SwingBinder validation");
        assertNotNull(view.getBinder(), "SwingBinder instance must be present");
        assertTrue(view.getBinder().getInvalidFields().isEmpty(), "No fields should be flagged as invalid");

        Place created = view.createValue();
        assertEquals("place-vip", created.getId());
        assertEquals("VIP Booth A", created.getName());
        assertEquals("6", created.getSeats());
        assertEquals(50, created.getX());
        assertEquals(100, created.getY());
        assertEquals("floor-vip", created.getFloor());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    @DisplayName("Should fail SwingBinder validation when required table name is missing or blank")
    void shouldFailValidationWhenRequiredNameIsMissingOrBlank(String invalidName) throws BasicException {
        DirtyManager dirty = new DirtyManager();
        PlacesEditor view = new PlacesEditor(dirty);

        Place table = new Place("place-invalid", invalidName, "2", 10, 10, "floor-1");
        view.writeValueEdit(table);

        assertFalse(view.isValid(), "Table with missing/blank name must fail validation");
        assertFalse(view.getBinder().getInvalidFields().isEmpty(), "Name field must be marked as invalid");
        assertTrue(view.getBinder().getInvalidFields().contains(view.getNameField()),
                "The invalid fields collection must contain m_jName component");
    }

    @Test
    @DisplayName("Should track dirty state when user edits bound table fields")
    void shouldTrackDirtyStateWhenBoundFieldIsModified() {
        DirtyManager dirty = new DirtyManager();
        PlacesEditor view = new PlacesEditor(dirty);

        view.writeValueInsert();
        dirty.setDirty(false);
        assertFalse(dirty.isDirty(), "Dirty flag should be cleared after manual reset");

        // Simulate typing in table name
        view.getNameField().setText("Patio 4");
        assertTrue(dirty.isDirty(), "Modifying name field must trigger dirty state");

        // Clear dirty and modify seats
        dirty.setDirty(false);
        view.getSeatsField().setText("8");
        assertTrue(dirty.isDirty(), "Modifying seats field must trigger dirty state");

        // Clear dirty and modify coordinates
        dirty.setDirty(false);
        view.getXField().setText("250");
        assertTrue(dirty.isDirty(), "Modifying X coordinate must trigger dirty state");

        dirty.setDirty(false);
        view.getYField().setText("300");
        assertTrue(dirty.isDirty(), "Modifying Y coordinate must trigger dirty state");
    }

    @Test
    @DisplayName("Should handle null guards gracefully in writeValueEdit and writeValueDelete")
    void shouldHandleNullGuardsInLifecycle() throws BasicException {
        DirtyManager dirty = new DirtyManager();
        PlacesEditor view = new PlacesEditor(dirty);

        view.writeValueEdit(null);
        Place editNull = view.createValue();
        assertNull(editNull.getId());
        assertEquals("", editNull.getName());

        view.writeValueDelete(null);
        Place deleteNull = view.createValue();
        assertNull(deleteNull.getId());
        assertEquals("", deleteNull.getName());
    }
}
