package com.openbravo.pos.admin;

import com.openbravo.basic.BasicException;
import com.openbravo.data.user.DirtyManager;
import com.openbravo.pos.panels.AppView;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test suite verifying type-safe lifecycle, two-way data binding,
 * and validation of {@link RolesView}.
 *
 * @author KriolOS
 */
@DisplayName("Roles View Type-Safe Editor Test Suite")
class RolesEditorTest {

    @Test
    @DisplayName("Should handle RoleInfo lifecycle type-safely in headless mode without AppView")
    void shouldHandleRolesViewLifecycleHeadless() throws BasicException {
        DirtyManager dirty = new DirtyManager();
        RolesView view = new RolesView(dirty);

        // 1. Initial EOF
        RoleInfo eof = view.createValue();
        assertNotNull(eof.getID());
        assertEquals("", eof.getName());
        assertNull(eof.getPermissions());

        // 2. Insert
        view.writeValueInsert();
        RoleInfo insertValue = view.createValue();
        assertNotNull(insertValue.getID());

        // 3. Edit
        byte[] testPerm = "<permissions><class name=\"reports\"/></permissions>".getBytes(StandardCharsets.UTF_8);
        RoleInfo role = new RoleInfo("role-manager", "Manager", testPerm);
        view.writeValueEdit(role);

        RoleInfo created = view.createValue();
        assertEquals("role-manager", created.getID());
        assertEquals("Manager", created.getName());
        assertNotNull(created.getPermissions());
        assertArrayEquals(testPerm, created.getPermissions());

        // 4. Delete
        view.writeValueDelete(role);
        RoleInfo deletedState = view.createValue();
        assertEquals("role-manager", deletedState.getID());
        assertEquals("Manager", deletedState.getName());

        // 5. EOF reset
        view.writeValueEOF();
        RoleInfo resetState = view.createValue();
        assertNotNull(resetState.getID());
        assertEquals("", resetState.getName());
    }

    @Test
    @DisplayName("Should handle RoleInfo lifecycle type-safely with AppView session")
    void shouldHandleRolesViewLifecycleWithAppView() throws BasicException {
        AppView appView = (AppView) Proxy.newProxyInstance(
                AppView.class.getClassLoader(),
                new Class<?>[]{AppView.class},
                (proxy, method, args) -> null
        );

        DirtyManager dirty = new DirtyManager();
        RolesView view = new RolesView(appView, dirty);

        // 1. Initial EOF
        assertNotNull(view.createValue().getID());

        // 2. Insert
        view.writeValueInsert();
        assertNotNull(view.createValue().getID());

        // 3. Edit
        byte[] adminPerm = "<permissions><class name=\"*\"/></permissions>".getBytes(StandardCharsets.UTF_8);
        RoleInfo role = new RoleInfo("role-admin", "Administrator", adminPerm);
        view.writeValueEdit(role);

        RoleInfo created = view.createValue();
        assertEquals("role-admin", created.getID());
        assertEquals("Administrator", created.getName());
        assertArrayEquals(adminPerm, created.getPermissions());

        // 4. Delete
        view.writeValueDelete(role);
        assertEquals("role-admin", view.createValue().getID());

        // 5. EOF
        view.writeValueEOF();
        assertNotNull(view.createValue().getID());
        assertEquals("", view.createValue().getName());
    }

    @Test
    @DisplayName("Should validate valid role successfully with SwingBinder")
    void shouldValidateValidRoleWithSwingBinder() throws BasicException {
        DirtyManager dirty = new DirtyManager();
        RolesView view = new RolesView(dirty);

        byte[] supervisorPerm = "<permissions><class name=\"sales\"/></permissions>".getBytes(StandardCharsets.UTF_8);
        RoleInfo role = new RoleInfo("role-supervisor", "Supervisor", supervisorPerm);
        view.writeValueEdit(role);

        assertTrue(view.isValid(), "Valid role must pass SwingBinder validation");
        assertTrue(view.isDataValid(), "isDataValid() must return true for valid role");
        assertNotNull(view.getBinder(), "SwingBinder instance must be present");
        assertTrue(view.getBinder().getInvalidFields().isEmpty(), "No fields should be marked as invalid");

        RoleInfo created = view.createValue();
        assertEquals("role-supervisor", created.getID());
        assertEquals("Supervisor", created.getName());
        assertArrayEquals(supervisorPerm, created.getPermissions());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    @DisplayName("Should fail SwingBinder validation when role name is missing or blank")
    void shouldFailValidationWhenRoleNameIsMissingOrBlank(String invalidName) throws BasicException {
        DirtyManager dirty = new DirtyManager();
        RolesView view = new RolesView(dirty);

        RoleInfo role = new RoleInfo("role-invalid", invalidName);
        view.writeValueEdit(role);

        assertFalse(view.isValid(), "Role with missing or blank name must fail validation");
        assertFalse(view.isDataValid(), "isDataValid() must return false when validation fails");
        assertFalse(view.getBinder().getInvalidFields().isEmpty(), "Name field must be marked as invalid");
        assertTrue(view.getBinder().getInvalidFields().contains(view.getNameField()),
                "The invalid fields collection must contain m_jName component");
    }

    @Test
    @DisplayName("Should track dirty state when user edits bound role name")
    void shouldTrackDirtyStateWhenRoleFieldIsModified() {
        DirtyManager dirty = new DirtyManager();
        RolesView view = new RolesView(dirty);

        view.writeValueInsert();
        dirty.setDirty(false);
        assertFalse(dirty.isDirty(), "Dirty flag should be cleared after manual reset");

        view.getNameField().setText("Inventory Manager");
        assertTrue(dirty.isDirty(), "Dirty flag must be set when bound name field is edited");
    }

    @Test
    @DisplayName("Should track dirty state when user edits permissions text area")
    void shouldTrackDirtyStateWhenPermissionsAreModified() {
        DirtyManager dirty = new DirtyManager();
        RolesView view = new RolesView(dirty);

        view.writeValueInsert();
        dirty.setDirty(false);
        assertFalse(dirty.isDirty(), "Dirty flag should be cleared after manual reset");

        view.getSyntaxTextArea().setText("<permissions><class name=\"stock\"/></permissions>");
        assertTrue(dirty.isDirty(), "Dirty flag must be set when bound permissions area is edited");
    }

    @Test
    @DisplayName("Should safely handle null values during writeValueEdit and writeValueDelete")
    void shouldHandleNullGuardsInRolesLifecycle() {
        DirtyManager dirty = new DirtyManager();
        RolesView view = new RolesView(dirty);

        assertDoesNotThrow(() -> view.writeValueEdit(null),
                "writeValueEdit(null) must safely fallback to EOF without throwing NPE");
        assertDoesNotThrow(() -> view.writeValueDelete(null),
                "writeValueDelete(null) must safely fallback to EOF without throwing NPE");
    }

    @Test
    @DisplayName("Should provide UI component, syntax text area, and handle refresh safely")
    void shouldExposeComponentAndHandleRefresh() {
        DirtyManager dirty = new DirtyManager();
        RolesView view = new RolesView(dirty);

        assertSame(view, view.getComponent(), "getComponent() should return RolesView instance");
        assertNotNull(view.getSyntaxTextArea(), "getSyntaxTextArea() should return RSyntaxTextArea");
        assertDoesNotThrow(view::refresh, "refresh() should execute safely");
    }
}
