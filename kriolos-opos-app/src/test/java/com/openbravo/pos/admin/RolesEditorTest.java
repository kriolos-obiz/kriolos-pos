package com.openbravo.pos.admin;

import com.openbravo.basic.BasicException;
import com.openbravo.data.user.DirtyManager;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test suite verifying type-safe lifecycle of RolesView.
 *
 * @author KriolOS
 */
@DisplayName("Roles View Type-Safe Editor Test Suite")
class RolesEditorTest {

    @Test
    @DisplayName("Should handle RoleInfo lifecycle type-safely without Object[] arrays")
    void shouldHandleRolesViewLifecycle() throws BasicException {
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
}

