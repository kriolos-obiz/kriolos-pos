package com.openbravo.pos.inventory;

import com.openbravo.basic.BasicException;
import com.openbravo.data.user.DirtyManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test suite verifying type-safe lifecycle of LocationsView.
 *
 * @author KriolOS
 */
@DisplayName("Locations View Type-Safe Editor Test Suite")
class LocationsEditorTest {

    @Test
    @DisplayName("Should handle LocationInfo lifecycle type-safely without Object[] arrays")
    void shouldHandleLocationsViewLifecycle() throws BasicException {
        DirtyManager dirty = new DirtyManager();
        LocationsView view = new LocationsView(dirty);

        // 1. Initial EOF
        view.writeValueEOF();
        LocationInfo eof = view.createValue();
        assertNotNull(eof.getId());
        assertEquals("", eof.getName());
        assertEquals("", eof.getAddress());

        // 2. Insert
        view.writeValueInsert();
        LocationInfo insertValue = view.createValue();
        assertNotNull(insertValue.getId());

        // 3. Edit
        LocationInfo location = new LocationInfo("loc-store", "Downtown Store", "Market Street 45");
        view.writeValueEdit(location);

        LocationInfo created = view.createValue();
        assertEquals("loc-store", created.getId());
        assertEquals("Downtown Store", created.getName());
        assertEquals("Market Street 45", created.getAddress());

        // 4. Delete
        view.writeValueDelete(location);
        LocationInfo deletedState = view.createValue();
        assertEquals("loc-store", deletedState.getId());
        assertEquals("Downtown Store", deletedState.getName());
        assertEquals("Market Street 45", deletedState.getAddress());

        // 5. EOF reset
        view.writeValueEOF();
        LocationInfo resetState = view.createValue();
        assertNotNull(resetState.getId());
        assertEquals("", resetState.getName());
    }
}

