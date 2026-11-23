package com.openbravo.pos.inventory;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.SentenceList;
import com.openbravo.data.loader.TableDefinition;
import com.openbravo.data.user.DirtyManager;
import com.openbravo.pos.sales.CreateTaxCategoryCommand;
import com.openbravo.pos.sales.CreateTaxCommand;
import com.openbravo.pos.sales.CreateTaxCustCategoryCommand;
import com.openbravo.pos.sales.TaxService;
import com.openbravo.pos.ticket.TaxInfo;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test suite verifying type-safe lifecycle, two-way data binding,
 * and validation of TaxEditor.
 *
 * @author KriolOS
 */
@DisplayName("Tax Editor Type-Safe Lifecycle & Validation Test Suite")
class TaxEditorTest {

    private static class SimpleSentenceList<T> implements SentenceList<T> {
        private final List<T> items;

        SimpleSentenceList(List<T> items) {
            this.items = items;
        }

        @Override
        public List<T> list() throws BasicException {
            return new ArrayList<>(items);
        }

        @Override
        public List<T> list(Object params) throws BasicException {
            return new ArrayList<>(items);
        }

        @Override
        public List<T> listPage(int offset, int length) throws BasicException {
            return new ArrayList<>(items);
        }

        @Override
        public List<T> listPage(Object params, int offset, int length) throws BasicException {
            return new ArrayList<>(items);
        }
    }

    private static class StubTaxService implements TaxService {
        private final List<TaxCategoryInfo> taxCats = List.of(new TaxCategoryInfo("tc-001", "Standard"));
        private final List<TaxCustCategoryInfo> custCats = List.of(new TaxCustCategoryInfo("tcc-001", "Retail"));
        private final List<TaxInfo> taxes = List.of(new TaxInfo("tax-parent", "Parent Tax", "tc-001", null, null, 0.05, false, 1));

        @Override
        public SentenceList<TaxInfo> getTaxList() {
            return new SimpleSentenceList<>(taxes);
        }

        @Override
        public List<TaxInfo> getTaxListAll() {
            return taxes;
        }

        @Override
        public SentenceList<TaxCustCategoryInfo> getTaxCustCategoriesList() {
            return new SimpleSentenceList<>(custCats);
        }

        @Override
        public List<TaxCustCategoryInfo> getTaxCustCategoriesListAll() {
            return custCats;
        }

        @Override
        public SentenceList<TaxCategoryInfo> getTaxCategoriesList() {
            return new SimpleSentenceList<>(taxCats);
        }

        @Override
        public List<TaxCategoryInfo> getTaxCategoriesListAll() {
            return taxCats;
        }

        @Override
        public SentenceList<TaxCategoryInfo> getTaxCategoryInfoList() {
            return new SimpleSentenceList<>(taxCats);
        }

        @Override
        public void createTaxCategory(CreateTaxCategoryCommand command) throws BasicException {
        }

        @Override
        public void createTaxCustCategory(CreateTaxCustCategoryCommand command) throws BasicException {
        }

        @Override
        public void createTax(CreateTaxCommand command) throws BasicException {
        }

        @Override
        public TableDefinition<TaxInfo> getTableTaxes() {
            return null;
        }

        @Override
        public TableDefinition<TaxCustCategoryInfo> getTableTaxCustCategories() {
            return null;
        }

        @Override
        public TableDefinition<TaxCategoryInfo> getTableTaxCategories() {
            return null;
        }
    }

    @Test
    @DisplayName("Should handle TaxInfo lifecycle type-safely without Object[] arrays")
    void shouldHandleTaxEditorLifecycle() throws BasicException {
        DirtyManager dirty = new DirtyManager();
        TaxService taxService = new StubTaxService();
        TaxEditor editor = new TaxEditor(null, dirty, taxService);

        // 1. Activate and refresh models
        editor.activate();
        editor.refresh();

        // 2. Initial EOF
        editor.writeValueEOF();
        TaxInfo eof = editor.createValue();
        assertNull(eof.getId());
        assertEquals("", eof.getName());
        assertEquals(0.0, eof.getRate());
        assertFalse(eof.isCascade());
        assertNull(eof.getOrder());

        // 3. Insert
        editor.writeValueInsert();
        TaxInfo insertValue = editor.createValue();
        assertNotNull(insertValue.getId());
        assertEquals("", insertValue.getName());
        assertEquals(0.0, insertValue.getRate());
        assertFalse(insertValue.isCascade());

        // 4. Edit
        TaxInfo tax = new TaxInfo("tax-001", "Standard VAT 20%", "tc-001", "tcc-001", "tax-parent", 0.20, true, 5);
        editor.writeValueEdit(tax);

        TaxInfo created = editor.createValue();
        assertEquals("tax-001", created.getId());
        assertEquals("Standard VAT 20%", created.getName());
        assertEquals("tc-001", created.getTaxCategoryID());
        assertEquals("tcc-001", created.getTaxCustCategoryID());
        assertEquals("tax-parent", created.getParentID());
        assertEquals(0.20, created.getRate(), 0.0001);
        assertTrue(created.isCascade());
        assertEquals(5, created.getOrder());

        // 5. Delete
        editor.writeValueDelete(tax);
        TaxInfo deletedState = editor.createValue();
        assertEquals("tax-001", deletedState.getId());
        assertEquals("Standard VAT 20%", deletedState.getName());

        // 6. Null guard on edit/delete
        editor.writeValueEdit(null);
        assertNull(editor.createValue().getId());
        editor.writeValueDelete(null);
        assertNull(editor.createValue().getId());
    }

    @Test
    @DisplayName("Should validate valid tax successfully with SwingBinder")
    void shouldValidateValidTaxWithSwingBinder() throws BasicException {
        DirtyManager dirty = new DirtyManager();
        TaxService taxService = new StubTaxService();
        TaxEditor editor = new TaxEditor(dirty, taxService);
        editor.activate();
        editor.refresh();

        TaxInfo tax = new TaxInfo("tax-reduced", "Reduced Tax 8%", "tc-001", "tcc-001", null, 0.08, false, 2);
        editor.writeValueEdit(tax);

        assertTrue(editor.isValid(), "Valid tax entity must pass SwingBinder validation");
        assertNotNull(editor.getBinder(), "SwingBinder instance must be present");
        assertTrue(editor.getBinder().getInvalidFields().isEmpty(), "No fields should be marked as invalid");

        TaxInfo created = editor.createValue();
        assertEquals("tax-reduced", created.getId());
        assertEquals("Reduced Tax 8%", created.getName());
        assertEquals(0.08, created.getRate(), 0.0001);
        assertFalse(created.isCascade());
        assertEquals(2, created.getOrder());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    @DisplayName("Should fail SwingBinder validation when tax name is missing or blank")
    void shouldFailValidationWhenTaxNameIsMissingOrBlank(String invalidName) throws BasicException {
        DirtyManager dirty = new DirtyManager();
        TaxEditor editor = new TaxEditor(dirty);

        TaxInfo tax = new TaxInfo("tax-invalid", invalidName, null, null, null, 0.15, false, 1);
        editor.writeValueEdit(tax);

        assertFalse(editor.isValid(), "Tax with missing/blank name must fail validation");
        assertFalse(editor.getBinder().getInvalidFields().isEmpty(), "Name field must be marked as invalid");
        assertTrue(editor.getBinder().getInvalidFields().contains(editor.getNameField()),
                "The invalid fields collection must contain m_jName component");
    }

    @Test
    @DisplayName("Should track dirty state when user edits bound tax fields")
    void shouldTrackDirtyStateWhenBoundFieldIsModified() {
        DirtyManager dirty = new DirtyManager();
        TaxEditor editor = new TaxEditor(dirty);

        editor.writeValueInsert();
        dirty.setDirty(false);
        assertFalse(dirty.isDirty(), "Dirty flag should be cleared after manual reset");

        editor.getNameField().setText("Eco Tax 5%");
        assertTrue(dirty.isDirty(), "Dirty flag must be set when bound name field is edited");
    }

    @Test
    @DisplayName("Should instantiate headlessly without active AppView or services")
    void shouldInstantiateHeadlesslyWithoutServices() throws BasicException {
        DirtyManager dirty = new DirtyManager();
        TaxEditor editor = new TaxEditor(dirty);

        assertNotNull(editor.getComponent(), "Component must be instantiated");
        assertDoesNotThrow(editor::activate, "activate() on headless instance should not throw NPE");
        assertDoesNotThrow(editor::refresh, "refresh() on headless instance should not throw NPE");

        editor.writeValueInsert();
        TaxInfo inserted = editor.createValue();
        assertNotNull(inserted.getId(), "Insert state should generate an ID");
    }
}
