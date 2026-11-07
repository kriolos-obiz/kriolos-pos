package com.openbravo.pos.inventory;

import com.openbravo.basic.BasicException;
import com.openbravo.data.user.DirtyManager;
import com.openbravo.pos.panels.AppView;
import com.openbravo.pos.pim.UomInfo;
import java.lang.reflect.Proxy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test suite for {@link UomEditor} strongly typed lifecycle.
 *
 * @author KriolOS
 */
@DisplayName("UomEditor Type-Safe Editor Test Suite")
class UomEditorTest {

    @Test
    @DisplayName("Should create, edit, and read strongly typed UomInfo records without Object[] arrays")
    void shouldHandleUomInfoLifecycleTypeSafely() throws BasicException {
        AppView appView = (AppView) Proxy.newProxyInstance(
                AppView.class.getClassLoader(),
                new Class<?>[]{AppView.class},
                (proxy, method, args) -> null
        );

        DirtyManager dirty = new DirtyManager();
        UomEditor editor = new UomEditor(appView, dirty);

        // 1. Initial EOF state
        assertNull(editor.createValue().getId());
        assertEquals("", editor.createValue().getName());

        // 2. Insert state assigns UUID
        editor.writeValueInsert();
        assertNotNull(editor.createValue().getId());

        // 3. Edit state updates model
        UomInfo testUom = new UomInfo("uom-liter", "Liter");
        editor.writeValueEdit(testUom);

        UomInfo created = editor.createValue();
        assertEquals("uom-liter", created.getId());
        assertEquals("Liter", created.getName());

        // 4. Delete state preserves value in disabled state
        editor.writeValueDelete(testUom);
        assertEquals("uom-liter", editor.createValue().getId());
        assertEquals("Liter", editor.createValue().getName());

        // 5. EOF resets
        editor.writeValueEOF();
        assertNull(editor.createValue().getId());
    }
}

