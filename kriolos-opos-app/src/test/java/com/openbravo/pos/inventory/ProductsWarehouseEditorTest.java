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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ProductsWarehouseEditor Type-Safe Lifecycle & Validation Test Suite")
class ProductsWarehouseEditorTest {

    private DirtyManager dirty;
    private ProductsWarehouseEditor editor;

    @BeforeEach
    void setUp() {
        dirty = new DirtyManager();
        editor = new ProductsWarehouseEditor(dirty);
    }

    @Nested
    @DisplayName("Headless Instantiation & Basic Contract")
    class InstantiationTests {

        @Test
        @DisplayName("Should instantiate headlessly without active GUI or database")
        void shouldInstantiateHeadless() {
            assertNotNull(editor);
            assertNotNull(editor.getComponent());
            assertNotNull(editor.getBinder());

            ProductsWarehouseEditor defaultEditor = new ProductsWarehouseEditor();
            assertNotNull(defaultEditor);
            assertNotNull(defaultEditor.getBinder());
        }

        @Test
        @DisplayName("Should initialize in EOF state with disabled controls")
        void shouldInitializeInEofState() {
            editor.writeValueEOF();

            assertFalse(editor.getMinimumField().isEnabled());
            assertFalse(editor.getMaximumField().isEnabled());
            assertFalse(editor.getQuantityField().isEnabled());

            assertEquals("", editor.getMinimumField().getText());
            assertEquals("", editor.getMaximumField().getText());
            assertEquals("", editor.getQuantityField().getText());
        }
    }

    @Nested
    @DisplayName("Lifecycle Operations")
    class LifecycleTests {

        @Test
        @DisplayName("Should initialize fields and enable inputs on writeValueInsert")
        void shouldHandleInsert() {
            editor.writeValueInsert();

            assertTrue(editor.getMinimumField().isEnabled());
            assertTrue(editor.getMaximumField().isEnabled());
            assertFalse(editor.getQuantityField().isEnabled());

            assertEquals("", editor.getMinimumField().getText());
            assertEquals("", editor.getMaximumField().getText());
        }

        @Test
        @DisplayName("Should populate fields and enable editing on writeValueEdit")
        void shouldPopulateFieldsOnEdit() throws BasicException {
            Object[] data = new Object[]{
                "wh-001",
                "prod-123",
                "REF-001",
                "Mineral Water 500ml",
                "loc-main",
                10.0,
                50.0,
                25.0
            };

            editor.writeValueEdit(data);

            assertEquals(Formats.DOUBLE.formatValue(10.0), editor.getMinimumField().getText());
            assertEquals(Formats.DOUBLE.formatValue(50.0), editor.getMaximumField().getText());
            assertEquals(Formats.DOUBLE.formatValue(25.0), editor.getQuantityField().getText());
            assertTrue(editor.getMinimumField().isEnabled());
            assertTrue(editor.getMaximumField().isEnabled());

            Object result = editor.createValue();
            assertTrue(result instanceof Object[]);
            Object[] created = (Object[]) result;
            assertEquals("wh-001", created[0]);
            assertEquals("prod-123", created[1]);
            assertEquals("REF-001", created[2]);
            assertEquals("Mineral Water 500ml", created[3]);
            assertEquals("loc-main", created[4]);
            assertEquals(10.0, (Double) created[5], 0.001);
            assertEquals(50.0, (Double) created[6], 0.001);
            assertEquals(25.0, (Double) created[7], 0.001);
        }

        @Test
        @DisplayName("Should disable all fields on writeValueDelete")
        void shouldDisableFieldsOnDelete() {
            Object[] data = new Object[]{
                "wh-002",
                "prod-456",
                "REF-002",
                "Sparkling Soda",
                "loc-main",
                5.0,
                30.0,
                15.0
            };

            editor.writeValueDelete(data);

            assertFalse(editor.getMinimumField().isEnabled());
            assertFalse(editor.getMaximumField().isEnabled());
            assertFalse(editor.getQuantityField().isEnabled());
        }
    }

    @Nested
    @DisplayName("Validation & Two-Way Binding")
    class ValidationTests {

        @Test
        @DisplayName("Should validate successfully when minimum and maximum are valid non-negative values and max >= min")
        void shouldValidateWhenValid() {
            editor.writeValueInsert();

            editor.getMinimumField().setText(Formats.DOUBLE.formatValue(5.0));
            editor.getMaximumField().setText(Formats.DOUBLE.formatValue(20.0));

            assertTrue(editor.isValid());
            assertTrue(editor.isDataValid());
        }

        @Test
        @DisplayName("Should validate successfully when minimum and maximum are equal")
        void shouldValidateWhenEqual() {
            editor.writeValueInsert();

            editor.getMinimumField().setText(Formats.DOUBLE.formatValue(15.0));
            editor.getMaximumField().setText(Formats.DOUBLE.formatValue(15.0));

            assertTrue(editor.isValid());
        }

        @Test
        @DisplayName("Should validate successfully when fields are empty / null")
        void shouldValidateWhenBlank() {
            editor.writeValueInsert();

            editor.getMinimumField().setText("");
            editor.getMaximumField().setText("");

            assertTrue(editor.isValid());
        }

        @ParameterizedTest
        @ValueSource(strings = {"-0.01", "-1.0", "-100.0"})
        @DisplayName("Should fail validation when minimum is negative")
        void shouldFailWhenMinNegative(String negativeMin) {
            editor.writeValueInsert();

            editor.getMinimumField().setText(negativeMin);
            editor.getMaximumField().setText(Formats.DOUBLE.formatValue(50.0));

            assertFalse(editor.isValid());
        }

        @ParameterizedTest
        @ValueSource(strings = {"-0.01", "-5.0"})
        @DisplayName("Should fail validation when maximum is negative")
        void shouldFailWhenMaxNegative(String negativeMax) {
            editor.writeValueInsert();

            editor.getMinimumField().setText(Formats.DOUBLE.formatValue(10.0));
            editor.getMaximumField().setText(negativeMax);

            assertFalse(editor.isValid());
        }

        @Test
        @DisplayName("Should fail cross-field validation when maximum is less than minimum")
        void shouldFailWhenMaxLessThanMin() {
            editor.writeValueInsert();

            editor.getMinimumField().setText(Formats.DOUBLE.formatValue(50.0));
            editor.getMaximumField().setText(Formats.DOUBLE.formatValue(10.0));

            assertFalse(editor.isValid());
        }

        @Test
        @DisplayName("Should track dirty state on text field modification")
        void shouldTrackDirtyState() {
            editor.writeValueInsert();
            dirty.setDirty(false);

            editor.getMinimumField().setText(Formats.DOUBLE.formatValue(8.0));

            assertTrue(dirty.isDirty());
        }
    }
}
