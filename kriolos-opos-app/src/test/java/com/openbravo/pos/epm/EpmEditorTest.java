package com.openbravo.pos.epm;

import com.openbravo.basic.BasicException;
import com.openbravo.data.user.DirtyManager;
import java.util.Date;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test suite verifying type-safe lifecycle of BreaksView and LeavesView.
 *
 * @author KriolOS
 */
@DisplayName("EPM Views Type-Safe Editor Test Suite")
class EpmEditorTest {

    @Test
    @DisplayName("Should handle BreaksView lifecycle type-safely without Object[] arrays")
    void shouldHandleBreaksViewLifecycle() throws BasicException {
        DirtyManager dirty = new DirtyManager();
        BreaksView view = new BreaksView(null, dirty, null);

        // 1. Initial EOF state
        view.writeValueEOF();
        BreaksInfo eofState = view.createValue();
        assertNotNull(eofState.getId());
        assertEquals("", eofState.getName());
        assertEquals("", eofState.getNotes());
        assertFalse(eofState.isVisible());

        // 2. Insert state
        view.writeValueInsert();
        BreaksInfo insertState = view.createValue();
        assertNotNull(insertState.getId());
        assertTrue(insertState.isVisible());

        // 3. Edit state
        BreaksInfo breakItem = new BreaksInfo("b-coffee", "Coffee Break", "15 minute coffee break", true);
        view.writeValueEdit(breakItem);

        BreaksInfo editedState = view.createValue();
        assertEquals("b-coffee", editedState.getId());
        assertEquals("Coffee Break", editedState.getName());
        assertEquals("15 minute coffee break", editedState.getNotes());
        assertTrue(editedState.isVisible());

        // 4. Delete state
        view.writeValueDelete(breakItem);
        BreaksInfo deletedState = view.createValue();
        assertEquals("b-coffee", deletedState.getId());
        assertEquals("Coffee Break", deletedState.getName());

        // 5. Reset to EOF
        view.writeValueEOF();
        BreaksInfo resetState = view.createValue();
        assertNotNull(resetState.getId());
        assertEquals("", resetState.getName());
    }

    @Test
    @DisplayName("Should handle LeavesView lifecycle type-safely without Object[] arrays")
    void shouldHandleLeavesViewLifecycle() throws BasicException {
        DirtyManager dirty = new DirtyManager();
        LeavesView view = new LeavesView(null, dirty, null);

        // 1. Initial EOF state
        view.writeValueEOF();
        LeavesInfo eofState = view.createValue();
        assertNotNull(eofState.getID());
        assertEquals("", eofState.getName());
        assertNull(eofState.getStartDate());
        assertNull(eofState.getEndDate());

        // 2. Insert state
        view.writeValueInsert();
        LeavesInfo insertState = view.createValue();
        assertNotNull(insertState.getID());

        // 3. Edit state
        long nowMs = System.currentTimeMillis();
        Date start = new Date(nowMs);
        Date end = new Date(nowMs + 86400000L);
        LeavesInfo leave = new LeavesInfo("leave-medical", "emp-42", "Alice Smith", start, end, "Medical leave");
        view.writeValueEdit(leave);

        LeavesInfo editedState = view.createValue();
        assertEquals("leave-medical", editedState.getID());
        assertEquals("Alice Smith", editedState.getName());
        assertEquals("Medical leave", editedState.getNotes());
        assertEquals("emp-42", editedState.getPplId());

        // 4. Delete state
        view.writeValueDelete(leave);
        LeavesInfo deletedState = view.createValue();
        assertEquals("leave-medical", deletedState.getID());
        assertEquals("Alice Smith", deletedState.getName());

        // 5. Reset to EOF
        view.writeValueEOF();
        LeavesInfo resetState = view.createValue();
        assertNotNull(resetState.getID());
        assertEquals("", resetState.getName());
    }
}

