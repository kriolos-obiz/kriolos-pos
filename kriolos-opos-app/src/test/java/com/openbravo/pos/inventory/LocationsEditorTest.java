package com.openbravo.pos.inventory;

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
 * and validation of LocationsView.
 *
 * @author KriolOS
 */
@DisplayName("Locations View Type-Safe Editor Test Suite")
class LocationsEditorTest {

    @Test
    @DisplayName("Should handle LocationInfo lifecycle type-safely without Object[] arrays")
    void shouldHandleLocationsViewLifecycle() throws BasicException {
        DirtyManager dirty = new DirtyManager();
        LocationsView view = new LocationsView(dirty);

        // 1. Initial EOF
        view.writeValueEOF();
        LocationInfo eof = view.createValue();
        assertNotNull(eof.getId());
        assertEquals("", eof.getName());
        assertEquals("", eof.getAddress());

        // 2. Insert
        view.writeValueInsert();
        LocationInfo insertValue = view.createValue();
        assertNotNull(insertValue.getId());

        // 3. Edit
        LocationInfo location = new LocationInfo("loc-store", "Downtown Store", "Market Street 45");
        view.writeValueEdit(location);

        LocationInfo created = view.createValue();
        assertEquals("loc-store", created.getId());
        assertEquals("Downtown Store", created.getName());
        assertEquals("Market Street 45", created.getAddress());

        // 4. Delete
        view.writeValueDelete(location);
        LocationInfo deletedState = view.createValue();
        assertEquals("loc-store", deletedState.getId());
        assertEquals("Downtown Store", deletedState.getName());
        assertEquals("Market Street 45", deletedState.getAddress());

        // 5. EOF reset
        view.writeValueEOF();
        LocationInfo resetState = view.createValue();
        assertNotNull(resetState.getId());
        assertEquals("", resetState.getName());
    }

    @Test
    @DisplayName("Should validate valid location successfully with SwingBinder")
    void shouldValidateValidLocationWithSwingBinder() throws BasicException {
        DirtyManager dirty = new DirtyManager();
        LocationsView view = new LocationsView(dirty);

        LocationInfo location = new LocationInfo("loc-wh-1", "Central Warehouse", "742 Evergreen Terrace");
        view.writeValueEdit(location);

        assertTrue(view.isValid(), "Valid location entity must pass SwingBinder validation");
        assertNotNull(view.getBinder(), "SwingBinder instance must be present");
        assertTrue(view.getBinder().getInvalidFields().isEmpty(), "No fields should be flagged as invalid");

        LocationInfo created = view.createValue();
        assertEquals("loc-wh-1", created.getId());
        assertEquals("Central Warehouse", created.getName());
        assertEquals("742 Evergreen Terrace", created.getAddress());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    @DisplayName("Should fail SwingBinder validation when required location name is missing or blank")
    void shouldFailValidationWhenRequiredNameIsMissingOrBlank(String invalidName) throws BasicException {
        DirtyManager dirty = new DirtyManager();
        LocationsView view = new LocationsView(dirty);

        LocationInfo location = new LocationInfo("loc-invalid", invalidName, "Main Dock 3");
        view.writeValueEdit(location);

        assertFalse(view.isValid(), "Location with missing/blank name must fail validation");
        assertFalse(view.getBinder().getInvalidFields().isEmpty(), "Name field must be marked as invalid");
        assertTrue(view.getBinder().getInvalidFields().contains(view.getNameField()),
                "The invalid fields collection must contain m_jName component");
    }

    @Test
    @DisplayName("Should track dirty state when user edits bound location fields")
    void shouldTrackDirtyStateWhenBoundFieldIsModified() {
        DirtyManager dirty = new DirtyManager();
        LocationsView view = new LocationsView(dirty);

        view.writeValueInsert();
        dirty.setDirty(false);
        assertFalse(dirty.isDirty(), "Dirty flag should be cleared after manual reset");

        // Simulate typing in location name
        view.getNameField().setText("New Annex Building");
        assertTrue(dirty.isDirty(), "Modifying name field must trigger dirty state");

        // Clear dirty and modify address
        dirty.setDirty(false);
        view.getAddressField().setText("Dock 14, East Harbor");
        assertTrue(dirty.isDirty(), "Modifying address field must trigger dirty state");
    }

    @Test
    @DisplayName("Should handle null guards gracefully in writeValueEdit and writeValueDelete")
    void shouldHandleNullGuardsInLifecycle() throws BasicException {
        DirtyManager dirty = new DirtyManager();
        LocationsView view = new LocationsView(dirty);

        view.writeValueEdit(null);
        LocationInfo editNull = view.createValue();
        assertNotNull(editNull.getId());
        assertEquals("", editNull.getName());

        view.writeValueDelete(null);
        LocationInfo deleteNull = view.createValue();
        assertNotNull(deleteNull.getId());
        assertEquals("", deleteNull.getName());
    }
}

