package com.openbravo.pos.admin;

import com.openbravo.basic.BasicException;
import com.openbravo.data.user.DirtyManager;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test suite verifying type-safe lifecycle, two-way data binding, and validation
 * of ResourcesView.
 *
 * @author KriolOS
 */
@DisplayName("Resources View Type-Safe Editor Test Suite")
class ResourcesEditorTest {

    @Test
    @DisplayName("Should instantiate ResourcesView headlessly with DirtyManager")
    void shouldInstantiateHeadlessWithoutAppView() {
        DirtyManager dirty = new DirtyManager();
        ResourcesView view = new ResourcesView(dirty);
        assertNotNull(view);
        assertNotNull(view.getComponent());
    }

    @Test
    @DisplayName("Should handle ResourceInfo lifecycle type-safely without Object[] arrays")
    void shouldHandleResourcesViewLifecycle() throws BasicException {
        DirtyManager dirty = new DirtyManager();
        ResourcesView view = new ResourcesView(dirty);

        // 1. Initial EOF
        ResourceInfo eof = view.createValue();
        assertNotNull(eof.getId());
        assertEquals("", eof.getName());
        assertNull(eof.getContent());

        // 2. Insert
        view.writeValueInsert();
        ResourceInfo insertValue = view.createValue();
        assertNotNull(insertValue.getId());
        assertEquals("", insertValue.getName());
        assertEquals(0, insertValue.getRestype());

        // 3. Edit Text Resource
        byte[] textBytes = "<print>Template Content</print>".getBytes(StandardCharsets.UTF_8);
        ResourceInfo res = new ResourceInfo("res-999", "Printer.TicketPreview", 0, textBytes);
        view.writeValueEdit(res);

        ResourceInfo created = view.createValue();
        assertEquals("res-999", created.getId());
        assertEquals("Printer.TicketPreview", created.getName());
        assertEquals(0, created.getRestype());
        assertNotNull(created.getContent());
        assertArrayEquals(textBytes, created.getContent());

        // 4. Delete
        view.writeValueDelete(res);
        ResourceInfo deletedState = view.createValue();
        assertEquals("res-999", deletedState.getId());
        assertEquals("Printer.TicketPreview", deletedState.getName());

        // 5. EOF reset
        view.writeValueEOF();
        ResourceInfo resetState = view.createValue();
        assertNotNull(resetState.getId());
        assertEquals("", resetState.getName());

        // 6. Null guard safety on edit/delete
        view.writeValueEdit(null);
        assertNotNull(view.createValue().getId());
        view.writeValueDelete(null);
        assertNotNull(view.createValue().getId());
    }

    @Test
    @DisplayName("Should validate mandatory resource name in ResourcesView")
    void shouldValidateMandatoryResourceName() {
        ResourcesView view = new ResourcesView(new DirtyManager());
        view.writeValueInsert();

        // 1. Initial insert with empty name -> invalid
        view.getNameField().setText("");
        assertFalse(view.isValid());
        assertFalse(view.isDataValid());

        // 2. Whitespace-only name -> invalid
        view.getNameField().setText("   ");
        assertFalse(view.isValid());

        // 3. Valid name -> valid
        view.getNameField().setText("Printer.Ticket");
        assertTrue(view.isValid());
        assertTrue(view.isDataValid());
    }

    @Test
    @DisplayName("Should track dirty state when ResourcesView fields change")
    void shouldTrackDirtyStateWhenFieldsChange() {
        DirtyManager dirty = new DirtyManager();
        ResourcesView view = new ResourcesView(dirty);
        view.writeValueInsert();
        dirty.setDirty(false);
        assertFalse(dirty.isDirty());

        // Changing name triggers dirty
        view.getNameField().setText("payment.ticket");
        assertTrue(dirty.isDirty());

        // Reset and test syntax text area
        dirty.setDirty(false);
        assertFalse(dirty.isDirty());
        view.getSyntaxTextArea().setText("<ticket><line>Total: 100</line></ticket>");
        assertTrue(dirty.isDirty());
    }

    @Test
    @DisplayName("Should safely handle null edit and delete in ResourcesView")
    void shouldSafelyHandleNullEditAndDelete() {
        ResourcesView view = new ResourcesView(new DirtyManager());
        assertDoesNotThrow(() -> view.writeValueEdit(null));
        assertDoesNotThrow(() -> view.writeValueDelete(null));
    }

    @Test
    @DisplayName("Should expose component accessors for headless testing")
    void shouldExposeComponentAccessors() {
        ResourcesView view = new ResourcesView(new DirtyManager());
        assertNotNull(view.getNameField());
        assertNotNull(view.getTypeCombo());
        assertNotNull(view.getSyntaxTextArea());
        assertNotNull(view.getImageEditor());
        assertNotNull(view.getComponent());
    }
}
