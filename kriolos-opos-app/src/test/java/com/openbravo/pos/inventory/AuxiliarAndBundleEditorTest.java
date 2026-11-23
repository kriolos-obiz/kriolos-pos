//    KriolOS POS
//    Copyright (c) 2019-2023 KriolOS
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program.  If not, see <http://www.gnu.org/licenses/>.

package com.openbravo.pos.inventory;

import com.openbravo.basic.BasicException;
import com.openbravo.data.user.DirtyManager;
import com.openbravo.format.Formats;
import com.openbravo.pos.util.FormatsDoubleConverter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Auxiliary & Product Bundle Type-Safe Editor Test Suite")
class AuxiliarAndBundleEditorTest {

    @Nested
    @DisplayName("AuxiliarEditor Tests")
    class AuxiliarEditorTests {

        @Test
        @DisplayName("Should instantiate AuxiliarEditor headlessly with DirtyManager")
        void shouldInstantiateHeadless() {
            DirtyManager dirty = new DirtyManager();
            AuxiliarEditor editor = new AuxiliarEditor(dirty);
            assertNotNull(editor);
            assertNotNull(editor.getComponent());
            assertNotNull(editor.getProductField());
            assertNotNull(editor.getReferenceField());
            assertNotNull(editor.getBarcodeField());
        }

        @Test
        @DisplayName("Should handle AuxiliarEditor lifecycle")
        void shouldHandleLifecycle() throws BasicException {
            DirtyManager dirty = new DirtyManager();
            AuxiliarEditor editor = new AuxiliarEditor(dirty);

            // 1. Initial EOF
            editor.writeValueEOF();
            Object[] eof = (Object[]) editor.createValue();
            assertNull(eof[0]);
            assertNull(eof[1]);
            assertNull(eof[2]);
            assertEquals("", eof[3]);
            assertEquals("", eof[4]);
            assertNull(eof[5]);

            // 2. Insert
            editor.writeValueInsert();
            Object[] inserted = (Object[]) editor.createValue();
            assertNotNull(inserted[0]);
            assertNull(inserted[1]);
            assertNull(inserted[2]);
            assertEquals("", inserted[3]);
            assertEquals("", inserted[4]);
            assertNull(inserted[5]);

            // 3. Edit with Object[]
            Object[] record = new Object[]{"aux-001", "prod-parent-01", "prod-child-02", "REF-A", "BAR-A", "Extra Cheese"};
            editor.writeValueEdit(record);
            Object[] edited = (Object[]) editor.createValue();
            assertEquals("aux-001", edited[0]);
            assertEquals("prod-parent-01", edited[1]);
            assertEquals("prod-child-02", edited[2]);
            assertEquals("REF-A", edited[3]);
            assertEquals("BAR-A", edited[4]);
            assertEquals("Extra Cheese", edited[5]);

            // 4. Delete
            editor.writeValueDelete(record);
            Object[] deleted = (Object[]) editor.createValue();
            assertEquals("aux-001", deleted[0]);
            assertEquals("prod-parent-01", deleted[1]);

            // 5. Edit with AuxiliarModel
            AuxiliarEditor.AuxiliarModel model = new AuxiliarEditor.AuxiliarModel(
                    "aux-002", "prod-parent-01", "prod-child-03", "REF-B", "BAR-B", "Bacon", "REF-B - Bacon");
            editor.writeValueEdit(model);
            Object[] fromModel = (Object[]) editor.createValue();
            assertEquals("aux-002", fromModel[0]);
            assertEquals("prod-child-03", fromModel[2]);
            assertEquals("REF-B", fromModel[3]);
            assertEquals("Bacon", fromModel[5]);

            // 6. Null guards
            editor.writeValueEdit(null);
            assertNull(((Object[]) editor.createValue())[0]);
            editor.writeValueDelete(null);
            assertNull(((Object[]) editor.createValue())[0]);
        }

        @Test
        @DisplayName("Should validate mandatory auxiliary product selection")
        void shouldValidateMandatoryProduct() {
            DirtyManager dirty = new DirtyManager();
            AuxiliarEditor editor = new AuxiliarEditor(dirty);
            editor.writeValueInsert();

            // Default insert has empty product -> invalid
            assertFalse(editor.isValid());
            assertFalse(editor.isDataValid());

            // Assign valid product text -> valid
            editor.getProductField().setText("REF-100 - Spicy Sauce");
            assertTrue(editor.isValid());
            assertTrue(editor.isDataValid());

            // Clear -> invalid
            editor.getProductField().setText("");
            assertFalse(editor.isValid());
        }

        @Test
        @DisplayName("Should track dirty state on field updates")
        void shouldTrackDirtyState() {
            DirtyManager dirty = new DirtyManager();
            AuxiliarEditor editor = new AuxiliarEditor(dirty);
            editor.writeValueInsert();
            dirty.setDirty(false);
            assertFalse(dirty.isDirty());

            editor.getProductField().setText("REF-200 - Garlic Butter");
            assertTrue(dirty.isDirty());
        }
    }

    @Nested
    @DisplayName("BundleEditor Tests")
    class BundleEditorTests {

        @Test
        @DisplayName("Should instantiate BundleEditor headlessly with DirtyManager")
        void shouldInstantiateHeadless() {
            DirtyManager dirty = new DirtyManager();
            BundleEditor editor = new BundleEditor(dirty);
            assertNotNull(editor);
            assertNotNull(editor.getComponent());
            assertNotNull(editor.getProductField());
            assertNotNull(editor.getQuantityField());
            assertNotNull(editor.getReferenceField());
            assertNotNull(editor.getBarcodeField());
        }

        @Test
        @DisplayName("Should handle BundleEditor lifecycle")
        void shouldHandleLifecycle() throws BasicException {
            DirtyManager dirty = new DirtyManager();
            BundleEditor editor = new BundleEditor(dirty);

            // 1. Initial EOF
            editor.writeValueEOF();
            Object[] eof = (Object[]) editor.createValue();
            assertNull(eof[0]);
            assertNull(eof[1]);
            assertNull(eof[2]);
            assertNull(eof[3]); // Quantity
            assertEquals("", eof[4]);
            assertEquals("", eof[5]);
            assertNull(eof[6]);

            // 2. Insert
            editor.writeValueInsert();
            Object[] inserted = (Object[]) editor.createValue();
            assertNotNull(inserted[0]);
            assertNull(inserted[1]);
            assertNull(inserted[2]);
            assertNull(inserted[3]);

            // 3. Edit with Object[]
            Object[] record = new Object[]{"bnd-001", "bundle-parent-01", "bundle-item-01", 3.0, "REF-B1", "BAR-B1", "Soft Drink"};
            editor.writeValueEdit(record);
            Object[] edited = (Object[]) editor.createValue();
            assertEquals("bnd-001", edited[0]);
            assertEquals("bundle-parent-01", edited[1]);
            assertEquals("bundle-item-01", edited[2]);
            assertEquals(3.0, (Double) edited[3], 0.0001);
            assertEquals("REF-B1", edited[4]);
            assertEquals("BAR-B1", edited[5]);
            assertEquals("Soft Drink", edited[6]);

            // 4. Delete
            editor.writeValueDelete(record);
            Object[] deleted = (Object[]) editor.createValue();
            assertEquals("bnd-001", deleted[0]);
            assertEquals("bundle-parent-01", deleted[1]);

            // 5. Edit with BundleModel
            BundleEditor.BundleModel model = new BundleEditor.BundleModel(
                    "bnd-002", "bundle-parent-01", "bundle-item-02", 5.0, "REF-B2", "BAR-B2", "Potato Chips", "REF-B2 - Potato Chips");
            editor.writeValueEdit(model);
            Object[] fromModel = (Object[]) editor.createValue();
            assertEquals("bnd-002", fromModel[0]);
            assertEquals("bundle-item-02", fromModel[2]);
            assertEquals(5.0, (Double) fromModel[3], 0.0001);
            assertEquals("Potato Chips", fromModel[6]);

            // 6. Null guards
            editor.writeValueEdit(null);
            assertNull(((Object[]) editor.createValue())[0]);
            editor.writeValueDelete(null);
            assertNull(((Object[]) editor.createValue())[0]);
        }

        @Test
        @DisplayName("Should validate mandatory product and strictly positive quantity via Formats.DOUBLE")
        void shouldValidateProductAndPositiveQuantity() {
            DirtyManager dirty = new DirtyManager();
            BundleEditor editor = new BundleEditor(dirty);
            editor.writeValueInsert();

            // Missing both product and quantity
            assertFalse(editor.isValid());
            assertFalse(editor.isDataValid());

            // Set product only
            editor.getProductField().setText("REF-B10 - Energy Bar");
            assertFalse(editor.isValid());

            // Set zero quantity -> invalid
            editor.getQuantityField().setText(Formats.DOUBLE.formatValue(0.0));
            assertFalse(editor.isValid());

            // Set negative quantity -> invalid
            editor.getQuantityField().setText(Formats.DOUBLE.formatValue(-2.0));
            assertFalse(editor.isValid());

            // Set positive valid quantity -> valid
            editor.getQuantityField().setText(Formats.DOUBLE.formatValue(2.5));
            assertTrue(editor.isValid());
            assertTrue(editor.isDataValid());

            // Clear product -> invalid
            editor.getProductField().setText("");
            assertFalse(editor.isValid());
        }

        @Test
        @DisplayName("Should track dirty state on quantity and product mutations")
        void shouldTrackDirtyState() {
            DirtyManager dirty = new DirtyManager();
            BundleEditor editor = new BundleEditor(dirty);
            editor.writeValueInsert();
            dirty.setDirty(false);
            assertFalse(dirty.isDirty());

            editor.getQuantityField().setText(Formats.DOUBLE.formatValue(4.0));
            assertTrue(dirty.isDirty());
        }
    }
}
