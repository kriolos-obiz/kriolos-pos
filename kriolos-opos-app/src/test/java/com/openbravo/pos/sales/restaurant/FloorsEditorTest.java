package com.openbravo.pos.sales.restaurant;

import com.openbravo.basic.BasicException;
import com.openbravo.data.user.DirtyManager;
import java.awt.image.BufferedImage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test suite verifying type-safe lifecycle, two-way data binding,
 * and validation of FloorsEditor.
 *
 * @author KriolOS
 */
@DisplayName("Floors Editor Type-Safe Lifecycle & Validation Test Suite")
class FloorsEditorTest {

    @Test
    @DisplayName("Should handle FloorsInfo lifecycle type-safely without Object[] arrays")
    void shouldHandleFloorsEditorLifecycle() throws BasicException {
        DirtyManager dirty = new DirtyManager();
        FloorsEditor view = new FloorsEditor(dirty);

        // 1. Initial EOF
        view.writeValueEOF();
        FloorsInfo eof = view.createValue();
        assertNull(eof.getID());
        assertEquals("", eof.getName());
        assertNull(eof.getImage());

        // 2. Insert
        view.writeValueInsert();
        FloorsInfo insertValue = view.createValue();
        assertNotNull(insertValue.getID());

        // 3. Edit
        BufferedImage image = new BufferedImage(32, 32, BufferedImage.TYPE_INT_RGB);
        FloorsInfo floor = new FloorsInfo("floor-patio", "Patio Garden", image);
        view.writeValueEdit(floor);

        FloorsInfo created = view.createValue();
        assertEquals("floor-patio", created.getID());
        assertEquals("Patio Garden", created.getName());
        assertNotNull(created.getImage());

        // 4. Delete
        view.writeValueDelete(floor);
        FloorsInfo deletedState = view.createValue();
        assertEquals("floor-patio", deletedState.getID());
        assertEquals("Patio Garden", deletedState.getName());
        assertNotNull(deletedState.getImage());

        // 5. EOF reset
        view.writeValueEOF();
        FloorsInfo resetState = view.createValue();
        assertNull(resetState.getID());
        assertEquals("", resetState.getName());
        assertNull(resetState.getImage());
    }

    @Test
    @DisplayName("Should validate valid floor successfully with SwingBinder")
    void shouldValidateValidFloorWithSwingBinder() throws BasicException {
        DirtyManager dirty = new DirtyManager();
        FloorsEditor view = new FloorsEditor(dirty);

        FloorsInfo floor = new FloorsInfo("floor-rooftop", "Rooftop Lounge");
        view.writeValueEdit(floor);

        assertTrue(view.isValid(), "Valid floor entity must pass SwingBinder validation");
        assertNotNull(view.getBinder(), "SwingBinder instance must be present");
        assertTrue(view.getBinder().getInvalidFields().isEmpty(), "No fields should be flagged as invalid");

        FloorsInfo created = view.createValue();
        assertEquals("floor-rooftop", created.getID());
        assertEquals("Rooftop Lounge", created.getName());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    @DisplayName("Should fail SwingBinder validation when required floor name is missing or blank")
    void shouldFailValidationWhenRequiredNameIsMissingOrBlank(String invalidName) throws BasicException {
        DirtyManager dirty = new DirtyManager();
        FloorsEditor view = new FloorsEditor(dirty);

        FloorsInfo floor = new FloorsInfo("floor-invalid", invalidName);
        view.writeValueEdit(floor);

        assertFalse(view.isValid(), "Floor with missing/blank name must fail validation");
        assertFalse(view.getBinder().getInvalidFields().isEmpty(), "Name field must be marked as invalid");
        assertTrue(view.getBinder().getInvalidFields().contains(view.getNameField()),
                "The invalid fields collection must contain m_jName component");
    }

    @Test
    @DisplayName("Should track dirty state when user edits bound floor name field")
    void shouldTrackDirtyStateWhenBoundFieldIsModified() {
        DirtyManager dirty = new DirtyManager();
        FloorsEditor view = new FloorsEditor(dirty);

        view.writeValueInsert();
        dirty.setDirty(false);
        assertFalse(dirty.isDirty(), "Dirty flag should be cleared after manual reset");

        // Simulate typing in floor name
        view.getNameField().setText("Mezzanine Terrace");
        assertTrue(dirty.isDirty(), "Modifying name field must trigger dirty state");
    }

    @Test
    @DisplayName("Should handle null guards gracefully in writeValueEdit and writeValueDelete")
    void shouldHandleNullGuardsInLifecycle() throws BasicException {
        DirtyManager dirty = new DirtyManager();
        FloorsEditor view = new FloorsEditor(dirty);

        view.writeValueEdit(null);
        FloorsInfo editNull = view.createValue();
        assertNull(editNull.getID());
        assertEquals("", editNull.getName());

        view.writeValueDelete(null);
        FloorsInfo deleteNull = view.createValue();
        assertNull(deleteNull.getID());
        assertEquals("", deleteNull.getName());
    }
}
