package com.openbravo.pos.inventory;

import com.openbravo.basic.BasicException;
import com.openbravo.data.user.DirtyManager;
import com.openbravo.pos.pim.CategoryInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test suite verifying type-safe lifecycle of CategoriesEditor.
 *
 * @author KriolOS
 */
@DisplayName("Categories View Type-Safe Editor Test Suite")
class CategoriesEditorTest {

    @Test
    @DisplayName("Should handle CategoryInfo lifecycle type-safely without Object[] arrays")
    void shouldHandleCategoriesEditorLifecycle() throws BasicException {
        DirtyManager dirty = new DirtyManager();
        CategoriesEditor editor = new CategoriesEditor(null, dirty);

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
}

