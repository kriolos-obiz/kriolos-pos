package com.openbravo.pos.admin;

import com.openbravo.basic.BasicException;
import com.openbravo.data.user.DirtyManager;
import java.awt.image.BufferedImage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test suite verifying type-safe lifecycle of PeopleView.
 *
 * @author KriolOS
 */
@DisplayName("People View Type-Safe Editor Test Suite")
class PeopleEditorTest {

    @Test
    @DisplayName("Should handle PeopleInfo lifecycle type-safely without Object[] arrays")
    void shouldHandlePeopleViewLifecycle() throws BasicException {
        DirtyManager dirty = new DirtyManager();
        PeopleView view = new PeopleView((PeopleService) null, dirty);

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

        // 6. Null guard safety on edit/delete
        view.writeValueEdit(null);
        assertNotNull(view.createValue().getId());
        view.writeValueDelete(null);
        assertNotNull(view.createValue().getId());
    }
}

