package com.openbravo.pos.admin;

import com.openbravo.basic.BasicException;
import com.openbravo.data.user.DirtyManager;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test suite verifying type-safe lifecycle of ResourcesView.
 *
 * @author KriolOS
 */
@DisplayName("Resources View Type-Safe Editor Test Suite")
class ResourcesEditorTest {

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
}
