package com.openbravo.pos.inventory;

import com.openbravo.basic.BasicException;
import com.openbravo.data.user.DirtyManager;
import com.openbravo.pos.pim.CategoryInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test suite verifying type-safe lifecycle, two-way data binding,
 * and validation of CategoriesEditor.
 *
 * @author KriolOS
 */
@DisplayName("Categories View Type-Safe Editor Test Suite")
class CategoriesEditorTest {

    @Test
    @DisplayName("Should handle CategoryInfo lifecycle type-safely without Object[] arrays")
    void shouldHandleCategoriesEditorLifecycle() throws BasicException {
        DirtyManager dirty = new DirtyManager();
        CategoriesEditor editor = new CategoriesEditor(dirty);

        // 1. Initial EOF
        editor.writeValueEOF();
        CategoryInfo eof = editor.createValue();
        assertNotNull(eof.getId());
        assertEquals("", eof.getName());
        assertNull(eof.getParentId());
        assertFalse(eof.isCatShowName());

        // 2. Insert
        editor.writeValueInsert();
        CategoryInfo insertValue = editor.createValue();
        assertNotNull(insertValue.getId());
        assertTrue(insertValue.isCatShowName());
        assertTrue(insertValue.isCatalogEnabled());

        // 3. Edit
        CategoryInfo cat = new CategoryInfo("cat-bakery", "Bakery", null, "Fresh Bread & Pastries", true);
        cat.setCatalogOrder("10");
        cat.setCatalogColor("#FFAA00");
        cat.setCatalogEnabled(true);
        editor.writeValueEdit(cat);

        CategoryInfo created = editor.createValue();
        assertEquals("cat-bakery", created.getId());
        assertEquals("Bakery", created.getName());
        assertEquals("Fresh Bread & Pastries", created.getTextTip());
        assertEquals("10", created.getCatalogOrder());
        assertTrue("#FFAA00".equalsIgnoreCase(created.getCatalogColor()));
        assertTrue(created.isCatShowName());
        assertTrue(created.isCatalogEnabled());

        // 4. Delete
        editor.writeValueDelete(cat);
        CategoryInfo deletedState = editor.createValue();
        assertEquals("cat-bakery", deletedState.getId());
        assertEquals("Bakery", deletedState.getName());

        // 5. EOF reset
        editor.writeValueEOF();
        CategoryInfo resetState = editor.createValue();
        assertNotNull(resetState.getId());
        assertEquals("", resetState.getName());
    }

    @Test
    @DisplayName("Should validate valid category successfully with SwingBinder")
    void shouldValidateValidCategoryWithSwingBinder() throws BasicException {
        DirtyManager dirty = new DirtyManager();
        CategoriesEditor editor = new CategoriesEditor(dirty);

        CategoryInfo cat = new CategoryInfo("cat-beverages", "Beverages & Drinks", null, "Cold sodas and juices", true);
        cat.setCatalogOrder("5");
        cat.setCatalogColor("#33AAFF");
        cat.setCatalogEnabled(true);
        editor.writeValueEdit(cat);

        assertTrue(editor.isValid(), "Valid category entity must pass SwingBinder validation");
        assertNotNull(editor.getBinder(), "SwingBinder instance must be present");
        assertTrue(editor.getBinder().getInvalidFields().isEmpty(), "No fields should be flagged as invalid");

        CategoryInfo created = editor.createValue();
        assertEquals("cat-beverages", created.getId());
        assertEquals("Beverages & Drinks", created.getName());
        assertEquals("Cold sodas and juices", created.getTextTip());
        assertEquals("5", created.getCatalogOrder());
        assertTrue("#33AAFF".equalsIgnoreCase(created.getCatalogColor()));
        assertTrue(created.isCatShowName());
        assertTrue(created.isCatalogEnabled());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    @DisplayName("Should fail SwingBinder validation when required category name is missing or blank")
    void shouldFailValidationWhenRequiredNameIsMissingOrBlank(String invalidName) throws BasicException {
        DirtyManager dirty = new DirtyManager();
        CategoriesEditor editor = new CategoriesEditor(dirty);

        CategoryInfo cat = new CategoryInfo("cat-invalid", invalidName, null, "Tip", true);
        editor.writeValueEdit(cat);

        assertFalse(editor.isValid(), "Category with missing or blank name must fail validation");
        assertFalse(editor.getBinder().getInvalidFields().isEmpty(), "Name field must be marked as invalid");
        assertTrue(editor.getBinder().getInvalidFields().contains(editor.getNameField()),
                "The invalid fields collection must contain categoryNameText component");
    }

    @Test
    @DisplayName("Should track dirty state when user edits bound category fields")
    void shouldTrackDirtyStateWhenBoundFieldIsModified() {
        DirtyManager dirty = new DirtyManager();
        CategoriesEditor editor = new CategoriesEditor(dirty);

        editor.writeValueInsert();
        dirty.setDirty(false);
        assertFalse(dirty.isDirty(), "Dirty flag should be cleared after manual reset");

        // Simulate typing in category name
        editor.getNameField().setText("New Organic Produce");
        assertTrue(dirty.isDirty(), "Dirty flag must be set when bound name field is edited");
    }

    @Test
    @DisplayName("Should safely handle null values during writeValueEdit and writeValueDelete")
    void shouldHandleNullGuardsInLifecycle() {
        DirtyManager dirty = new DirtyManager();
        CategoriesEditor editor = new CategoriesEditor(dirty);

        assertDoesNotThrow(() -> editor.writeValueEdit(null),
                "writeValueEdit(null) must safely fallback to EOF without throwing NPE");
        assertDoesNotThrow(() -> editor.writeValueDelete(null),
                "writeValueDelete(null) must safely fallback to EOF without throwing NPE");
    }
}
