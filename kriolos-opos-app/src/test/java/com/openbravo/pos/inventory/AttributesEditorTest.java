package com.openbravo.pos.inventory;

import com.openbravo.basic.BasicException;
import com.openbravo.data.user.DirtyManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test suite verifying type-safe lifecycle, validation, and dirty tracking
 * across the entire product attributes editor family:
 * - AttributesEditor
 * - AttributeValuesEditor
 * - AttributeSetsEditor
 * - AttributeUseEditor
 *
 * @author KriolOS
 */
@DisplayName("Product Attributes Family Type-Safe Editor Test Suite")
class AttributesEditorTest {

    @Nested
    @DisplayName("AttributesEditor Tests")
    class AttributesEditorTests {

        @Test
        @DisplayName("Should instantiate AttributesEditor headlessly with DirtyManager")
        void shouldInstantiateHeadless() {
            DirtyManager dirty = new DirtyManager();
            AttributesEditor editor = new AttributesEditor(dirty);
            assertNotNull(editor);
            assertNotNull(editor.getComponent());
            assertNotNull(editor.getNameField());
        }

        @Test
        @DisplayName("Should handle AttributesEditor lifecycle")
        void shouldHandleLifecycle() throws BasicException {
            DirtyManager dirty = new DirtyManager();
            AttributesEditor editor = new AttributesEditor(dirty);

            // 1. Initial EOF
            editor.writeValueEOF();
            Object[] eof = (Object[]) editor.createValue();
            assertNull(eof[0]);
            assertEquals("", eof[1]);

            // 2. Insert
            editor.writeValueInsert();
            Object[] inserted = (Object[]) editor.createValue();
            assertNotNull(inserted[0]);
            assertEquals("", inserted[1]);

            // 3. Edit with Object[]
            Object[] record = new Object[]{"attr-001", "Color"};
            editor.writeValueEdit(record);
            Object[] edited = (Object[]) editor.createValue();
            assertEquals("attr-001", edited[0]);
            assertEquals("Color", edited[1]);

            // 4. Delete
            editor.writeValueDelete(record);
            Object[] deleted = (Object[]) editor.createValue();
            assertEquals("attr-001", deleted[0]);
            assertEquals("Color", deleted[1]);

            // 5. Edit with domain bean AttributeInfo
            AttributeInfo info = new AttributeInfo("attr-002", "Size");
            editor.writeValueEdit(info);
            Object[] fromBean = (Object[]) editor.createValue();
            assertEquals("attr-002", fromBean[0]);
            assertEquals("Size", fromBean[1]);

            // 6. Null guards
            editor.writeValueEdit(null);
            assertNull(((Object[]) editor.createValue())[0]);
            editor.writeValueDelete(null);
            assertNull(((Object[]) editor.createValue())[0]);
        }

        @Test
        @DisplayName("Should validate mandatory attribute name")
        void shouldValidateMandatoryName() {
            AttributesEditor editor = new AttributesEditor(new DirtyManager());
            editor.writeValueInsert();

            // Empty name -> invalid
            editor.getNameField().setText("");
            assertFalse(editor.isValid());
            assertFalse(editor.isDataValid());

            // Whitespace -> invalid
            editor.getNameField().setText("   ");
            assertFalse(editor.isValid());

            // Valid name -> valid
            editor.getNameField().setText("Brand");
            assertTrue(editor.isValid());
            assertTrue(editor.isDataValid());
        }

        @Test
        @DisplayName("Should track dirty state on attribute name change")
        void shouldTrackDirtyState() {
            DirtyManager dirty = new DirtyManager();
            AttributesEditor editor = new AttributesEditor(dirty);
            editor.writeValueInsert();
            dirty.setDirty(false);
            assertFalse(dirty.isDirty());

            editor.getNameField().setText("Material");
            assertTrue(dirty.isDirty());
        }
    }

    @Nested
    @DisplayName("AttributeValuesEditor Tests")
    class AttributeValuesEditorTests {

        @Test
        @DisplayName("Should instantiate AttributeValuesEditor headlessly with DirtyManager")
        void shouldInstantiateHeadless() {
            DirtyManager dirty = new DirtyManager();
            AttributeValuesEditor editor = new AttributeValuesEditor(dirty);
            assertNotNull(editor);
            assertNotNull(editor.getComponent());
            assertNotNull(editor.getValueField());
        }

        @Test
        @DisplayName("Should handle AttributeValuesEditor lifecycle")
        void shouldHandleLifecycle() throws BasicException {
            DirtyManager dirty = new DirtyManager();
            AttributeValuesEditor editor = new AttributeValuesEditor(dirty);
            editor.setInsertId("attr-parent-01");

            // 1. Initial EOF
            editor.writeValueEOF();
            Object[] eof = (Object[]) editor.createValue();
            assertNull(eof[0]);
            assertNull(eof[1]);
            assertEquals("", eof[2]);

            // 2. Insert
            editor.writeValueInsert();
            Object[] inserted = (Object[]) editor.createValue();
            assertNotNull(inserted[0]);
            assertEquals("attr-parent-01", inserted[1]);
            assertEquals("", inserted[2]);

            // 3. Edit
            Object[] record = new Object[]{"val-001", "attr-parent-01", "Red"};
            editor.writeValueEdit(record);
            Object[] edited = (Object[]) editor.createValue();
            assertEquals("val-001", edited[0]);
            assertEquals("attr-parent-01", edited[1]);
            assertEquals("Red", edited[2]);

            // 4. Delete
            editor.writeValueDelete(record);
            Object[] deleted = (Object[]) editor.createValue();
            assertEquals("val-001", deleted[0]);
            assertEquals("Red", deleted[2]);

            // 5. Null guards
            editor.writeValueEdit(null);
            assertNull(((Object[]) editor.createValue())[0]);
            editor.writeValueDelete(null);
            assertNull(((Object[]) editor.createValue())[0]);
        }

        @Test
        @DisplayName("Should validate mandatory attribute value")
        void shouldValidateMandatoryValue() {
            AttributeValuesEditor editor = new AttributeValuesEditor(new DirtyManager());
            editor.writeValueInsert();

            // Empty value -> invalid
            editor.getValueField().setText("");
            assertFalse(editor.isValid());
            assertFalse(editor.isDataValid());

            // Whitespace -> invalid
            editor.getValueField().setText("   ");
            assertFalse(editor.isValid());

            // Valid value -> valid
            editor.getValueField().setText("Extra Large");
            assertTrue(editor.isValid());
            assertTrue(editor.isDataValid());
        }

        @Test
        @DisplayName("Should track dirty state on attribute value change")
        void shouldTrackDirtyState() {
            DirtyManager dirty = new DirtyManager();
            AttributeValuesEditor editor = new AttributeValuesEditor(dirty);
            editor.writeValueInsert();
            dirty.setDirty(false);
            assertFalse(dirty.isDirty());

            editor.getValueField().setText("Blue");
            assertTrue(dirty.isDirty());
        }
    }

    @Nested
    @DisplayName("AttributeSetsEditor Tests")
    class AttributeSetsEditorTests {

        @Test
        @DisplayName("Should instantiate AttributeSetsEditor headlessly with DirtyManager")
        void shouldInstantiateHeadless() {
            DirtyManager dirty = new DirtyManager();
            AttributeSetsEditor editor = new AttributeSetsEditor(dirty);
            assertNotNull(editor);
            assertNotNull(editor.getComponent());
            assertNotNull(editor.getNameField());
        }

        @Test
        @DisplayName("Should handle AttributeSetsEditor lifecycle")
        void shouldHandleLifecycle() throws BasicException {
            DirtyManager dirty = new DirtyManager();
            AttributeSetsEditor editor = new AttributeSetsEditor(dirty);

            // 1. Initial EOF
            editor.writeValueEOF();
            Object[] eof = (Object[]) editor.createValue();
            assertNull(eof[0]);
            assertEquals("", eof[1]);

            // 2. Insert
            editor.writeValueInsert();
            Object[] inserted = (Object[]) editor.createValue();
            assertNotNull(inserted[0]);
            assertEquals("", inserted[1]);

            // 3. Edit with Object[]
            Object[] record = new Object[]{"set-001", "Apparel Sizes"};
            editor.writeValueEdit(record);
            Object[] edited = (Object[]) editor.createValue();
            assertEquals("set-001", edited[0]);
            assertEquals("Apparel Sizes", edited[1]);

            // 4. Delete
            editor.writeValueDelete(record);
            Object[] deleted = (Object[]) editor.createValue();
            assertEquals("set-001", deleted[0]);
            assertEquals("Apparel Sizes", deleted[1]);

            // 5. Edit with domain bean AttributeSetInfo
            AttributeSetInfo info = new AttributeSetInfo("set-002", "Electronics Variants");
            editor.writeValueEdit(info);
            Object[] fromBean = (Object[]) editor.createValue();
            assertEquals("set-002", fromBean[0]);
            assertEquals("Electronics Variants", fromBean[1]);

            // 6. Null guards
            editor.writeValueEdit(null);
            assertNull(((Object[]) editor.createValue())[0]);
            editor.writeValueDelete(null);
            assertNull(((Object[]) editor.createValue())[0]);
        }

        @Test
        @DisplayName("Should validate mandatory attribute set name")
        void shouldValidateMandatorySetName() {
            AttributeSetsEditor editor = new AttributeSetsEditor(new DirtyManager());
            editor.writeValueInsert();

            // Empty name -> invalid
            editor.getNameField().setText("");
            assertFalse(editor.isValid());
            assertFalse(editor.isDataValid());

            // Whitespace -> invalid
            editor.getNameField().setText("   ");
            assertFalse(editor.isValid());

            // Valid name -> valid
            editor.getNameField().setText("Footwear Matrix");
            assertTrue(editor.isValid());
            assertTrue(editor.isDataValid());
        }

        @Test
        @DisplayName("Should track dirty state on attribute set name change")
        void shouldTrackDirtyState() {
            DirtyManager dirty = new DirtyManager();
            AttributeSetsEditor editor = new AttributeSetsEditor(dirty);
            editor.writeValueInsert();
            dirty.setDirty(false);
            assertFalse(dirty.isDirty());

            editor.getNameField().setText("Jewelry Variations");
            assertTrue(dirty.isDirty());
        }
    }

    @Nested
    @DisplayName("AttributeUseEditor Tests")
    class AttributeUseEditorTests {

        @Test
        @DisplayName("Should instantiate AttributeUseEditor headlessly with DirtyManager")
        void shouldInstantiateHeadless() {
            DirtyManager dirty = new DirtyManager();
            AttributeUseEditor editor = new AttributeUseEditor(dirty);
            assertNotNull(editor);
            assertNotNull(editor.getComponent());
            assertNotNull(editor.getLinenoField());
            assertNotNull(editor.getAttributeCombo());
        }

        @Test
        @DisplayName("Should handle AttributeUseEditor lifecycle")
        void shouldHandleLifecycle() throws BasicException {
            DirtyManager dirty = new DirtyManager();
            AttributeUseEditor editor = new AttributeUseEditor(dirty);
            editor.setInsertId("set-001");

            // 1. Initial EOF
            editor.writeValueEOF();
            Object[] eof = (Object[]) editor.createValue();
            assertNull(eof[0]);
            assertNull(eof[1]);

            // 2. Insert
            editor.writeValueInsert();
            Object[] inserted = (Object[]) editor.createValue();
            assertNotNull(inserted[0]);
            assertEquals("set-001", inserted[1]);

            // 3. Edit
            Object[] record = new Object[]{"use-001", "set-001", "attr-001", 10};
            editor.writeValueEdit(record);
            Object[] edited = (Object[]) editor.createValue();
            assertEquals("use-001", edited[0]);
            assertEquals("set-001", edited[1]);
            assertEquals(10, edited[3]);

            // 4. Delete
            editor.writeValueDelete(record);
            Object[] deleted = (Object[]) editor.createValue();
            assertEquals("use-001", deleted[0]);
            assertEquals(10, deleted[3]);

            // 5. Null guards
            editor.writeValueEdit(null);
            assertNull(((Object[]) editor.createValue())[0]);
            editor.writeValueDelete(null);
            assertNull(((Object[]) editor.createValue())[0]);
        }

        @Test
        @DisplayName("Should validate mandatory line number in AttributeUseEditor")
        void shouldValidateMandatoryLineNumber() {
            AttributeUseEditor editor = new AttributeUseEditor(new DirtyManager());
            editor.writeValueInsert();

            // Empty line number -> invalid
            editor.getLinenoField().setText("");
            assertFalse(editor.isValid());
            assertFalse(editor.isDataValid());

            // Non-numeric line number -> invalid
            editor.getLinenoField().setText("abc");
            assertFalse(editor.isValid());

            // Negative line number -> invalid
            editor.getLinenoField().setText("-1");
            assertFalse(editor.isValid());

            // Valid positive integer -> valid
            editor.getLinenoField().setText("1");
            assertTrue(editor.isValid());
            assertTrue(editor.isDataValid());
        }

        @Test
        @DisplayName("Should track dirty state on line number change")
        void shouldTrackDirtyState() {
            DirtyManager dirty = new DirtyManager();
            AttributeUseEditor editor = new AttributeUseEditor(dirty);
            editor.writeValueInsert();
            dirty.setDirty(false);
            assertFalse(dirty.isDirty());

            editor.getLinenoField().setText("2");
            assertTrue(dirty.isDirty());
        }
    }
}
