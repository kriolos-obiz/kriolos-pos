package com.openbravo.pos.epm;

import com.openbravo.basic.BasicException;
import com.openbravo.data.user.DirtyManager;
import java.util.Date;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test suite verifying type-safe lifecycle, two-way data binding, and validation
 * for BreaksView and LeavesView.
 *
 * @author KriolOS
 */
@DisplayName("EPM Views Type-Safe Editor Test Suite")
class EpmEditorTest {

    @Test
    @DisplayName("Should instantiate headless BreaksView and LeavesView using DirtyManager only")
    void shouldInstantiateHeadlessWithoutAppView() {
        DirtyManager dirty = new DirtyManager();
        BreaksView breaksView = new BreaksView(dirty);
        assertNotNull(breaksView);
        assertNotNull(breaksView.getComponent());

        LeavesView leavesView = new LeavesView(dirty);
        assertNotNull(leavesView);
        assertNotNull(leavesView.getComponent());
    }

    @Test
    @DisplayName("Should handle BreaksView lifecycle type-safely without Object[] arrays")
    void shouldHandleBreaksViewLifecycle() throws BasicException {
        DirtyManager dirty = new DirtyManager();
        BreaksView view = new BreaksView(dirty);

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
    @DisplayName("Should validate mandatory break name in BreaksView")
    void shouldValidateMandatoryBreakName() {
        BreaksView view = new BreaksView(new DirtyManager());
        view.writeValueInsert();

        // 1. Empty name should fail validation
        view.getBreakNameField().setText("");
        assertFalse(view.isValid());
        assertFalse(view.isDataValid());

        // 2. Whitespace-only name should fail validation
        view.getBreakNameField().setText("   ");
        assertFalse(view.isValid());

        // 3. Valid name should pass validation
        view.getBreakNameField().setText("Lunch Break");
        assertTrue(view.isValid());
        assertTrue(view.isDataValid());
    }

    @Test
    @DisplayName("Should track dirty state when BreaksView fields change")
    void shouldTrackDirtyStateInBreaksView() {
        DirtyManager dirty = new DirtyManager();
        BreaksView view = new BreaksView(dirty);
        view.writeValueInsert();
        dirty.setDirty(false);
        assertFalse(dirty.isDirty());

        // Text field edit triggers dirty
        view.getBreakNameField().setText("Morning Break");
        assertTrue(dirty.isDirty());

        // Reset dirty
        dirty.setDirty(false);
        assertFalse(dirty.isDirty());

        // Checkbox click triggers dirty
        view.getVisibleCheckBox().doClick();
        assertTrue(dirty.isDirty());
    }

    @Test
    @DisplayName("Should safely handle null edit and delete in BreaksView")
    void shouldSafelyHandleNullEditAndDeleteInBreaksView() {
        BreaksView view = new BreaksView(new DirtyManager());
        assertDoesNotThrow(() -> view.writeValueEdit(null));
        assertDoesNotThrow(() -> view.writeValueDelete(null));
    }

    @Test
    @DisplayName("Should expose component accessors for BreaksView")
    void shouldExposeComponentAccessorsForBreaksView() {
        BreaksView view = new BreaksView(new DirtyManager());
        assertNotNull(view.getBreakNameField());
        assertNotNull(view.getBreakDescriptionArea());
        assertNotNull(view.getVisibleCheckBox());
    }

    @Test
    @DisplayName("Should handle LeavesView lifecycle type-safely without Object[] arrays")
    void shouldHandleLeavesViewLifecycle() throws BasicException {
        DirtyManager dirty = new DirtyManager();
        LeavesView view = new LeavesView(dirty);

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

    @Test
    @DisplayName("Should validate mandatory employee name in LeavesView")
    void shouldValidateMandatoryEmployeeNameInLeavesView() {
        LeavesView view = new LeavesView(new DirtyManager());
        view.writeValueInsert();

        // 1. Empty name fails validation
        view.getEmployeeNameField().setText("");
        assertFalse(view.isValid());
        assertFalse(view.isDataValid());

        // 2. Whitespace-only name fails validation
        view.getEmployeeNameField().setText("   ");
        assertFalse(view.isValid());

        // 3. Valid employee name passes validation
        view.getEmployeeNameField().setText("John Doe");
        assertTrue(view.isValid());
        assertTrue(view.isDataValid());
    }

    @Test
    @DisplayName("Should track dirty state when LeavesView fields change")
    void shouldTrackDirtyStateInLeavesView() {
        DirtyManager dirty = new DirtyManager();
        LeavesView view = new LeavesView(dirty);
        view.writeValueInsert();
        dirty.setDirty(false);
        assertFalse(dirty.isDirty());

        // Edit text field
        view.getEmployeeNameField().setText("Jane Smith");
        assertTrue(dirty.isDirty());

        // Reset and test notes area
        dirty.setDirty(false);
        assertFalse(dirty.isDirty());
        view.getLeaveNoteArea().setText("Annual vacation leave");
        assertTrue(dirty.isDirty());
    }

    @Test
    @DisplayName("Should safely handle null edit and delete in LeavesView")
    void shouldSafelyHandleNullEditAndDeleteInLeavesView() {
        LeavesView view = new LeavesView(new DirtyManager());
        assertDoesNotThrow(() -> view.writeValueEdit(null));
        assertDoesNotThrow(() -> view.writeValueDelete(null));
    }

    @Test
    @DisplayName("Should expose component accessors for LeavesView")
    void shouldExposeComponentAccessorsForLeavesView() {
        LeavesView view = new LeavesView(new DirtyManager());
        assertNotNull(view.getEmployeeNameField());
        assertNotNull(view.getStartDateField());
        assertNotNull(view.getEndDateField());
        assertNotNull(view.getLeaveNoteArea());
        assertNotNull(view.getBtnEmployee());
    }
}
