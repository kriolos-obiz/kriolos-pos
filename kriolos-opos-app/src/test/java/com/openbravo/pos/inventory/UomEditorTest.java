package com.openbravo.pos.inventory;

import com.openbravo.basic.BasicException;
import com.openbravo.data.user.DirtyManager;
import com.openbravo.pos.panels.AppView;
import com.openbravo.pos.pim.UomInfo;
import java.lang.reflect.Proxy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test suite verifying type-safe lifecycle, two-way data binding,
 * and validation of {@link UomEditor}.
 *
 * @author KriolOS
 */
@DisplayName("UomEditor Type-Safe Editor Test Suite")
class UomEditorTest {

    @Test
    @DisplayName("Should create, edit, and read strongly typed UomInfo records with AppView session")
    void shouldHandleUomInfoLifecycleWithAppView() throws BasicException {
        AppView appView = (AppView) Proxy.newProxyInstance(
                AppView.class.getClassLoader(),
                new Class<?>[]{AppView.class},
                (proxy, method, args) -> null
        );

        DirtyManager dirty = new DirtyManager();
        UomEditor editor = new UomEditor(appView, dirty);

        // 1. Initial EOF state
        assertNull(editor.createValue().getId());
        assertEquals("", editor.createValue().getName());

        // 2. Insert state assigns UUID
        editor.writeValueInsert();
        assertNotNull(editor.createValue().getId());

        // 3. Edit state updates model
        UomInfo testUom = new UomInfo("uom-liter", "Liter");
        editor.writeValueEdit(testUom);

        UomInfo created = editor.createValue();
        assertEquals("uom-liter", created.getId());
        assertEquals("Liter", created.getName());

        // 4. Delete state preserves value in disabled state
        editor.writeValueDelete(testUom);
        assertEquals("uom-liter", editor.createValue().getId());
        assertEquals("Liter", editor.createValue().getName());

        // 5. EOF resets
        editor.writeValueEOF();
        assertNull(editor.createValue().getId());
    }

    @Test
    @DisplayName("Should handle UomInfo lifecycle type-safely in headless mode without AppView")
    void shouldHandleUomInfoLifecycleHeadless() throws BasicException {
        DirtyManager dirty = new DirtyManager();
        UomEditor editor = new UomEditor(dirty);

        // 1. Initial EOF
        assertNull(editor.createValue().getId());
        assertEquals("", editor.createValue().getName());

        // 2. Insert
        editor.writeValueInsert();
        assertNotNull(editor.createValue().getId());

        // 3. Edit
        UomInfo testUom = new UomInfo("uom-kg", "Kilogram");
        editor.writeValueEdit(testUom);

        UomInfo created = editor.createValue();
        assertEquals("uom-kg", created.getId());
        assertEquals("Kilogram", created.getName());

        // 4. Delete
        editor.writeValueDelete(testUom);
        assertEquals("uom-kg", editor.createValue().getId());
        assertEquals("Kilogram", editor.createValue().getName());

        // 5. EOF reset
        editor.writeValueEOF();
        assertNull(editor.createValue().getId());
    }

    @Test
    @DisplayName("Should validate valid UOM successfully with SwingBinder")
    void shouldValidateValidUomWithSwingBinder() throws BasicException {
        DirtyManager dirty = new DirtyManager();
        UomEditor editor = new UomEditor(dirty);

        UomInfo uom = new UomInfo("uom-meter", "Meter");
        editor.writeValueEdit(uom);

        assertTrue(editor.isValid(), "Valid UOM must pass SwingBinder validation");
        assertTrue(editor.isDataValid(), "isDataValid() must return true for valid UOM");
        assertNotNull(editor.getBinder(), "SwingBinder instance must be present");
        assertTrue(editor.getBinder().getInvalidFields().isEmpty(), "No fields should be marked as invalid");

        UomInfo created = editor.createValue();
        assertEquals("uom-meter", created.getId());
        assertEquals("Meter", created.getName());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    @DisplayName("Should fail SwingBinder validation when UOM name is missing or blank")
    void shouldFailValidationWhenUomNameIsMissingOrBlank(String invalidName) throws BasicException {
        DirtyManager dirty = new DirtyManager();
        UomEditor editor = new UomEditor(dirty);

        UomInfo uom = new UomInfo("uom-invalid", invalidName);
        editor.writeValueEdit(uom);

        assertFalse(editor.isValid(), "UOM with missing or blank name must fail validation");
        assertFalse(editor.isDataValid(), "isDataValid() must return false when validation fails");
        assertFalse(editor.getBinder().getInvalidFields().isEmpty(), "Name field must be marked as invalid");
        assertTrue(editor.getBinder().getInvalidFields().contains(editor.getNameField()),
                "The invalid fields collection must contain m_jName component");
    }

    @Test
    @DisplayName("Should track dirty state when user edits bound UOM name")
    void shouldTrackDirtyStateWhenUomNameIsModified() {
        DirtyManager dirty = new DirtyManager();
        UomEditor editor = new UomEditor(dirty);

        editor.writeValueInsert();
        dirty.setDirty(false);
        assertFalse(dirty.isDirty(), "Dirty flag should be cleared after manual reset");

        editor.getNameField().setText("Gallon");
        assertTrue(dirty.isDirty(), "Dirty flag must be set when bound name field is edited");
    }

    @Test
    @DisplayName("Should safely handle null values during writeValueEdit and writeValueDelete")
    void shouldHandleNullGuardsInUomLifecycle() {
        DirtyManager dirty = new DirtyManager();
        UomEditor editor = new UomEditor(dirty);

        assertDoesNotThrow(() -> editor.writeValueEdit(null),
                "writeValueEdit(null) must safely fallback to EOF without throwing NPE");
        assertDoesNotThrow(() -> editor.writeValueDelete(null),
                "writeValueDelete(null) must safely fallback to EOF without throwing NPE");
    }

    @Test
    @DisplayName("Should provide UI component and refresh safely")
    void shouldExposeComponentAndHandleRefresh() {
        DirtyManager dirty = new DirtyManager();
        UomEditor editor = new UomEditor(dirty);

        assertSame(editor, editor.getComponent(), "getComponent() should return editor instance");
        assertDoesNotThrow(editor::refresh, "refresh() should execute safely");
    }
}
