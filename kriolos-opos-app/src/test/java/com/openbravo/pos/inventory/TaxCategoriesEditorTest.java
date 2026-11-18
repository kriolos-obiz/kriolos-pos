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
 * and validation of TaxCategoriesEditor and TaxCustCategoriesEditor.
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

    @Test
    @DisplayName("Should validate valid tax category successfully with SwingBinder")
    void shouldValidateValidTaxCategoryWithSwingBinder() throws BasicException {
        DirtyManager dirty = new DirtyManager();
        TaxCategoriesEditor editor = new TaxCategoriesEditor(dirty);

        TaxCategoryInfo cat = new TaxCategoryInfo("tc-reduced", "Reduced VAT Rate");
        editor.writeValueEdit(cat);

        assertTrue(editor.isValid(), "Valid tax category must pass SwingBinder validation");
        assertNotNull(editor.getBinder(), "SwingBinder instance must be present");
        assertTrue(editor.getBinder().getInvalidFields().isEmpty(), "No fields should be marked as invalid");

        TaxCategoryInfo created = editor.createValue();
        assertEquals("tc-reduced", created.getID());
        assertEquals("Reduced VAT Rate", created.getName());
    }

    @Test
    @DisplayName("Should validate valid customer tax category successfully with SwingBinder")
    void shouldValidateValidTaxCustCategoryWithSwingBinder() throws BasicException {
        DirtyManager dirty = new DirtyManager();
        TaxCustCategoriesEditor editor = new TaxCustCategoriesEditor(dirty);

        TaxCustCategoryInfo custCat = new TaxCustCategoryInfo("tcc-exempt", "Diplomatic Exempt");
        editor.writeValueEdit(custCat);

        assertTrue(editor.isValid(), "Valid customer tax category must pass SwingBinder validation");
        assertNotNull(editor.getBinder(), "SwingBinder instance must be present");
        assertTrue(editor.getBinder().getInvalidFields().isEmpty(), "No fields should be marked as invalid");

        TaxCustCategoryInfo created = editor.createValue();
        assertEquals("tcc-exempt", created.getID());
        assertEquals("Diplomatic Exempt", created.getName());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    @DisplayName("Should fail SwingBinder validation when tax category name is missing or blank")
    void shouldFailValidationWhenTaxCategoryNameIsMissingOrBlank(String invalidName) throws BasicException {
        DirtyManager dirty = new DirtyManager();
        TaxCategoriesEditor editor = new TaxCategoriesEditor(dirty);

        TaxCategoryInfo cat = new TaxCategoryInfo("tc-invalid", invalidName);
        editor.writeValueEdit(cat);

        assertFalse(editor.isValid(), "Tax category with missing or blank name must fail validation");
        assertFalse(editor.getBinder().getInvalidFields().isEmpty(), "Name field must be marked as invalid");
        assertTrue(editor.getBinder().getInvalidFields().contains(editor.getNameField()),
                "The invalid fields collection must contain m_jName component");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    @DisplayName("Should fail SwingBinder validation when customer tax category name is missing or blank")
    void shouldFailValidationWhenTaxCustCategoryNameIsMissingOrBlank(String invalidName) throws BasicException {
        DirtyManager dirty = new DirtyManager();
        TaxCustCategoriesEditor editor = new TaxCustCategoriesEditor(dirty);

        TaxCustCategoryInfo custCat = new TaxCustCategoryInfo("tcc-invalid", invalidName);
        editor.writeValueEdit(custCat);

        assertFalse(editor.isValid(), "Customer tax category with missing or blank name must fail validation");
        assertFalse(editor.getBinder().getInvalidFields().isEmpty(), "Name field must be marked as invalid");
        assertTrue(editor.getBinder().getInvalidFields().contains(editor.getNameField()),
                "The invalid fields collection must contain m_jName component");
    }

    @Test
    @DisplayName("Should track dirty state when user edits bound tax category name")
    void shouldTrackDirtyStateWhenTaxCategoryFieldIsModified() {
        DirtyManager dirty = new DirtyManager();
        TaxCategoriesEditor editor = new TaxCategoriesEditor(dirty);

        editor.writeValueInsert();
        dirty.setDirty(false);
        assertFalse(dirty.isDirty(), "Dirty flag should be cleared after manual reset");

        editor.getNameField().setText("Luxury Goods VAT");
        assertTrue(dirty.isDirty(), "Dirty flag must be set when bound name field is edited");
    }

    @Test
    @DisplayName("Should track dirty state when user edits bound customer tax category name")
    void shouldTrackDirtyStateWhenTaxCustCategoryFieldIsModified() {
        DirtyManager dirty = new DirtyManager();
        TaxCustCategoriesEditor editor = new TaxCustCategoriesEditor(dirty);

        editor.writeValueInsert();
        dirty.setDirty(false);
        assertFalse(dirty.isDirty(), "Dirty flag should be cleared after manual reset");

        editor.getNameField().setText("Non-Profit Entity");
        assertTrue(dirty.isDirty(), "Dirty flag must be set when bound name field is edited");
    }

    @Test
    @DisplayName("Should safely handle null values during tax category writeValueEdit and writeValueDelete")
    void shouldHandleNullGuardsInTaxCategoriesLifecycle() {
        DirtyManager dirty = new DirtyManager();
        TaxCategoriesEditor editor = new TaxCategoriesEditor(dirty);

        assertDoesNotThrow(() -> editor.writeValueEdit(null),
                "writeValueEdit(null) must safely fallback to EOF without throwing NPE");
        assertDoesNotThrow(() -> editor.writeValueDelete(null),
                "writeValueDelete(null) must safely fallback to EOF without throwing NPE");
    }

    @Test
    @DisplayName("Should safely handle null values during customer tax category writeValueEdit and writeValueDelete")
    void shouldHandleNullGuardsInTaxCustCategoriesLifecycle() {
        DirtyManager dirty = new DirtyManager();
        TaxCustCategoriesEditor editor = new TaxCustCategoriesEditor(dirty);

        assertDoesNotThrow(() -> editor.writeValueEdit(null),
                "writeValueEdit(null) must safely fallback to EOF without throwing NPE");
        assertDoesNotThrow(() -> editor.writeValueDelete(null),
                "writeValueDelete(null) must safely fallback to EOF without throwing NPE");
    }
}
