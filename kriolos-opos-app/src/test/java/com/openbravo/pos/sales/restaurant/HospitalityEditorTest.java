package com.openbravo.pos.sales.restaurant;

import com.openbravo.basic.BasicException;
import com.openbravo.data.user.DirtyManager;
import java.awt.image.BufferedImage;
import java.lang.reflect.Proxy;
import java.util.Collections;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test suite verifying type-safe lifecycle of FloorsEditor and PlacesEditor.
 *
 * @author KriolOS
 */
@DisplayName("Hospitality Type-Safe Editors Test Suite")
class HospitalityEditorTest {

    @Test
    @DisplayName("Should handle FloorsInfo lifecycle type-safely without Object[] arrays")
    void shouldHandleFloorsEditorLifecycle() throws BasicException {
        DirtyManager dirty = new DirtyManager();
        FloorsEditor editor = new FloorsEditor(dirty);

        // 1. Initial EOF
        assertNull(editor.createValue().getID());
        assertEquals("", editor.createValue().getName());
        assertNull(editor.createValue().getImage());

        // 2. Insert
        editor.writeValueInsert();
        assertNotNull(editor.createValue().getID());

        // 3. Edit
        BufferedImage testImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        FloorsInfo floor = new FloorsInfo("floor-patio", "Patio", testImg);
        editor.writeValueEdit(floor);

        FloorsInfo created = editor.createValue();
        assertEquals("floor-patio", created.getID());
        assertEquals("Patio", created.getName());
        assertNotNull(created.getImage());

        // 4. Delete
        editor.writeValueDelete(floor);
        assertEquals("floor-patio", editor.createValue().getID());
        assertEquals("Patio", editor.createValue().getName());

        // 5. EOF reset
        editor.writeValueEOF();
        assertNull(editor.createValue().getID());
    }

    @Test
    @DisplayName("Should handle Place lifecycle type-safely without Object[] arrays")
    void shouldHandlePlacesEditorLifecycle() throws BasicException {
        RestaurantService restaurantService = (RestaurantService) Proxy.newProxyInstance(
                RestaurantService.class.getClassLoader(),
                new Class<?>[]{RestaurantService.class},
                (proxy, method, args) -> {
                    if ("getFloorsListAll".equals(method.getName())) {
                        return Collections.singletonList(new FloorsInfo("floor-1", "Main"));
                    }
                    return null;
                }
        );

        DirtyManager dirty = new DirtyManager();
        PlacesEditor editor = new PlacesEditor(restaurantService, dirty);
        editor.activate();

        // 1. Initial EOF
        assertNull(editor.createValue().getId());
        assertEquals("", editor.createValue().getName());

        // 2. Insert
        editor.writeValueInsert();
        assertNotNull(editor.createValue().getId());

        // 3. Edit
        Place place = new Place("place-10", "Table 10", "6", 50, 75, "floor-1");
        editor.writeValueEdit(place);

        Place created = editor.createValue();
        assertEquals("place-10", created.getId());
        assertEquals("Table 10", created.getName());
        assertEquals("6", created.getSeats());
        assertEquals(50, created.getX());
        assertEquals(75, created.getY());
        assertEquals("floor-1", created.getFloor());

        // 4. Delete
        editor.writeValueDelete(place);
        assertEquals("place-10", editor.createValue().getId());
        assertEquals("Table 10", editor.createValue().getName());

        // 5. EOF reset
        editor.writeValueEOF();
        assertNull(editor.createValue().getId());
    }
}

