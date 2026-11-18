package com.openbravo.pos.admin;

import com.openbravo.basic.BasicException;
import com.openbravo.data.user.DirtyManager;
import com.openbravo.pos.panels.AppView;
import java.awt.image.BufferedImage;
import java.lang.reflect.Proxy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test suite verifying type-safe lifecycle, two-way data binding,
 * and validation of {@link PeopleView}.
 *
 * @author KriolOS
 */
@DisplayName("People View Type-Safe Editor Test Suite")
class PeopleEditorTest {

    @Test
    @DisplayName("Should handle PeopleInfo lifecycle type-safely in headless mode without AppView")
    void shouldHandlePeopleViewLifecycleHeadless() throws BasicException {
        DirtyManager dirty = new DirtyManager();
        PeopleView view = new PeopleView(dirty);

        // 1. Initial EOF
        PeopleInfo eof = view.createValue();
        assertNotNull(eof.getId());
        assertEquals("", eof.getName());
        assertNull(eof.getPassword());
        assertFalse(eof.isVisible());
        assertEquals("", eof.getCard());
        assertNull(eof.getImage());

        // 2. Insert
        view.writeValueInsert();
        PeopleInfo insertValue = view.createValue();
        assertNotNull(insertValue.getId());
        assertTrue(insertValue.isVisible());

        // 3. Edit with 16-digit card
        BufferedImage img = new BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB);
        PeopleInfo person = new PeopleInfo("p-100", "Bob Cashier", "secretpass", "role-cashier", true, "1234567890123456", img);
        view.writeValueEdit(person);

        PeopleInfo created = view.createValue();
        assertEquals("p-100", created.getId());
        assertEquals("Bob Cashier", created.getName());
        assertEquals("secretpass", created.getPassword());
        assertTrue(created.isVisible());
        assertEquals("1234567890123456", created.getCard());
        assertNotNull(created.getImage());

        // 3b. Edit with short card (non-ibutton)
        person.setCard("CARD99");
        view.writeValueEdit(person);
        PeopleInfo shortCardPerson = view.createValue();
        assertEquals("CARD99", shortCardPerson.getCard());

        // 4. Delete
        view.writeValueDelete(person);
        PeopleInfo deletedState = view.createValue();
        assertEquals("p-100", deletedState.getId());
        assertEquals("Bob Cashier", deletedState.getName());

        // 5. EOF reset
        view.writeValueEOF();
        PeopleInfo resetState = view.createValue();
        assertNotNull(resetState.getId());
        assertEquals("", resetState.getName());
    }

    @Test
    @DisplayName("Should handle PeopleInfo lifecycle type-safely with AppView session")
    void shouldHandlePeopleViewLifecycleWithAppView() throws BasicException {
        AppView appView = (AppView) Proxy.newProxyInstance(
                AppView.class.getClassLoader(),
                new Class<?>[]{AppView.class},
                (proxy, method, args) -> null
        );

        DirtyManager dirty = new DirtyManager();
        PeopleView view = new PeopleView(appView, dirty);

        // 1. Initial EOF
        assertNotNull(view.createValue().getId());

        // 2. Insert
        view.writeValueInsert();
        assertNotNull(view.createValue().getId());
        assertTrue(view.createValue().isVisible());

        // 3. Edit
        PeopleInfo person = new PeopleInfo("p-admin", "Alice Admin", "admin123", "role-admin", true, "CARD001", null);
        view.writeValueEdit(person);

        PeopleInfo created = view.createValue();
        assertEquals("p-admin", created.getId());
        assertEquals("Alice Admin", created.getName());
        assertTrue(created.isVisible());
        assertEquals("CARD001", created.getCard());

        // 4. Delete
        view.writeValueDelete(person);
        assertEquals("p-admin", view.createValue().getId());

        // 5. EOF
        view.writeValueEOF();
        assertNotNull(view.createValue().getId());
        assertEquals("", view.createValue().getName());
    }

    @Test
    @DisplayName("Should validate valid person successfully with SwingBinder")
    void shouldValidateValidPersonWithSwingBinder() throws BasicException {
        DirtyManager dirty = new DirtyManager();
        PeopleView view = new PeopleView(dirty);

        PeopleInfo person = new PeopleInfo("p-mgr", "Maria Manager", "pwd456", "role-mgr", true, "CARD88", null);
        view.writeValueEdit(person);

        assertTrue(view.isValid(), "Valid person must pass SwingBinder validation");
        assertTrue(view.isDataValid(), "isDataValid() must return true for valid person");
        assertNotNull(view.getBinder(), "SwingBinder instance must be present");
        assertTrue(view.getBinder().getInvalidFields().isEmpty(), "No fields should be marked as invalid");

        PeopleInfo created = view.createValue();
        assertEquals("p-mgr", created.getId());
        assertEquals("Maria Manager", created.getName());
        assertTrue(created.isVisible());
        assertEquals("CARD88", created.getCard());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    @DisplayName("Should fail SwingBinder validation when person name is missing or blank")
    void shouldFailValidationWhenPersonNameIsMissingOrBlank(String invalidName) throws BasicException {
        DirtyManager dirty = new DirtyManager();
        PeopleView view = new PeopleView(dirty);

        PeopleInfo person = new PeopleInfo("p-invalid", invalidName);
        view.writeValueEdit(person);

        assertFalse(view.isValid(), "Person with missing or blank name must fail validation");
        assertFalse(view.isDataValid(), "isDataValid() must return false when validation fails");
        assertFalse(view.getBinder().getInvalidFields().isEmpty(), "Name field must be marked as invalid");
        assertTrue(view.getBinder().getInvalidFields().contains(view.getNameField()),
                "The invalid fields collection must contain m_jName component");
    }

    @Test
    @DisplayName("Should track dirty state when user edits bound person fields")
    void shouldTrackDirtyStateWhenPersonFieldIsModified() {
        DirtyManager dirty = new DirtyManager();
        PeopleView view = new PeopleView(dirty);

        view.writeValueInsert();
        dirty.setDirty(false);
        assertFalse(dirty.isDirty(), "Dirty flag should be cleared after manual reset");

        view.getNameField().setText("Charlie Clerk");
        assertTrue(dirty.isDirty(), "Dirty flag must be set when bound name field is edited");

        dirty.setDirty(false);
        view.getCardField().setText("CARD777");
        assertTrue(dirty.isDirty(), "Dirty flag must be set when bound card field is edited");

        dirty.setDirty(false);
        view.getVisibleCheckBox().doClick();
        assertTrue(dirty.isDirty(), "Dirty flag must be set when visible checkbox is clicked");
    }

    @Test
    @DisplayName("Should safely handle null values during writeValueEdit and writeValueDelete")
    void shouldHandleNullGuardsInPeopleLifecycle() {
        DirtyManager dirty = new DirtyManager();
        PeopleView view = new PeopleView(dirty);

        assertDoesNotThrow(() -> view.writeValueEdit(null),
                "writeValueEdit(null) must safely fallback to EOF without throwing NPE");
        assertDoesNotThrow(() -> view.writeValueDelete(null),
                "writeValueDelete(null) must safely fallback to EOF without throwing NPE");
    }

    @Test
    @DisplayName("Should provide UI component, accessors, and handle refresh safely")
    void shouldExposeComponentAndHandleRefresh() {
        DirtyManager dirty = new DirtyManager();
        PeopleView view = new PeopleView(dirty);

        assertSame(view, view.getComponent(), "getComponent() should return PeopleView instance");
        assertNotNull(view.getNameField(), "getNameField() should return JTextField");
        assertNotNull(view.getCardField(), "getCardField() should return JTextField");
        assertNotNull(view.getVisibleCheckBox(), "getVisibleCheckBox() should return JCheckBox");
        assertNotNull(view.getRoleComboBox(), "getRoleComboBox() should return JComboBox");
        assertDoesNotThrow(view::refresh, "refresh() should execute safely");
    }
}
