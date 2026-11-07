package com.openbravo.pos.inventory;

import com.openbravo.basic.BasicException;
import com.openbravo.data.user.DirtyManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test suite verifying type-safe lifecycle of TaxCategoriesEditor and TaxCustCategoriesEditor.
 *
 * @author KriolOS
 */
@DisplayName("Tax Categories Type-Safe Editors Test Suite")
class TaxCategoriesEditorTest {

    @Test
    @DisplayName("Should handle TaxCategoryInfo lifecycle type-safely without Object[] arrays")
    void shouldHandleTaxCategoryEditorLifecycle() throws BasicException {
        DirtyManager dirty = new DirtyManager();
        TaxCategoriesEditor editor = new TaxCategoriesEditor(dirty);

        // 1. Initial EOF
        assertNull(editor.createValue().getID());
        assertEquals("", editor.createValue().getName());

        // 2. Insert
        editor.writeValueInsert();
        assertNotNull(editor.createValue().getID());

        // 3. Edit
        TaxCategoryInfo cat = new TaxCategoryInfo("tc-general", "General Goods");
        editor.writeValueEdit(cat);

        TaxCategoryInfo created = editor.createValue();
        assertEquals("tc-general", created.getID());
        assertEquals("General Goods", created.getName());

        // 4. Delete
        editor.writeValueDelete(cat);
        assertEquals("tc-general", editor.createValue().getID());
        assertEquals("General Goods", editor.createValue().getName());

        // 5. EOF reset
        editor.writeValueEOF();
        assertNull(editor.createValue().getID());
    }

    @Test
    @DisplayName("Should handle TaxCustCategoryInfo lifecycle type-safely without Object[] arrays")
    void shouldHandleTaxCustCategoryEditorLifecycle() throws BasicException {
        DirtyManager dirty = new DirtyManager();
        TaxCustCategoriesEditor editor = new TaxCustCategoriesEditor(dirty);

        // 1. Initial EOF
        assertNull(editor.createValue().getID());
        assertEquals("", editor.createValue().getName());

        // 2. Insert
        editor.writeValueInsert();
        assertNotNull(editor.createValue().getID());

        // 3. Edit
        TaxCustCategoryInfo custCat = new TaxCustCategoryInfo("tcc-wholesale", "Wholesale Buyer");
        editor.writeValueEdit(custCat);

        TaxCustCategoryInfo created = editor.createValue();
        assertEquals("tcc-wholesale", created.getID());
        assertEquals("Wholesale Buyer", created.getName());

        // 4. Delete
        editor.writeValueDelete(custCat);
        assertEquals("tcc-wholesale", editor.createValue().getID());
        assertEquals("Wholesale Buyer", editor.createValue().getName());

        // 5. EOF reset
        editor.writeValueEOF();
        assertNull(editor.createValue().getID());
    }
}

