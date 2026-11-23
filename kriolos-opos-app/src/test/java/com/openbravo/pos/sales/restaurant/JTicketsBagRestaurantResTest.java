//    KriolOS POS
//    Copyright (c) 2019-2023 KriolOS
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program.  If not, see <http://www.gnu.org/licenses/>.

package com.openbravo.pos.sales.restaurant;

import com.openbravo.basic.BasicException;
import com.openbravo.data.user.DirtyManager;
import com.openbravo.pos.customers.CustomerInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JTicketsBagRestaurantRes Type-Safe Lifecycle & Validation Test Suite")
class JTicketsBagRestaurantResTest {

    private DirtyManager dirty;
    private JTicketsBagRestaurantRes editor;

    @BeforeEach
    void setUp() {
        dirty = new DirtyManager();
        editor = new JTicketsBagRestaurantRes(dirty);
    }

    @Nested
    @DisplayName("Headless Instantiation & Basic Contract")
    class InstantiationTests {

        @Test
        @DisplayName("Should instantiate headlessly without active GUI or database")
        void shouldInstantiateHeadless() {
            assertNotNull(editor);
            assertNotNull(editor.getComponent());
            assertNotNull(editor.getBinder());
        }

        @Test
        @DisplayName("Should initialize in EOF state with disabled controls")
        void shouldInitializeInEofState() {
            editor.writeValueEOF();

            assertFalse(editor.getCustomerField().isEnabled());
            assertFalse(editor.getChairsField().isEnabled());
            assertFalse(editor.getDescriptionField().isEnabled());
            assertFalse(editor.getTimeReservationPanel().isEnabled());

            assertTrue(editor.getCustomerField().getText() == null || editor.getCustomerField().getText().isEmpty());
            assertTrue(editor.getDescriptionField().getText() == null || editor.getDescriptionField().getText().isEmpty());
        }
    }

    @Nested
    @DisplayName("Lifecycle Operations")
    class LifecycleTests {

        @Test
        @DisplayName("Should initialize fields and enable inputs on writeValueInsert")
        void shouldHandleInsert() {
            editor.writeValueInsert();

            assertTrue(editor.getCustomerField().isEnabled());
            assertTrue(editor.getChairsField().isEnabled());
            assertTrue(editor.getDescriptionField().isEnabled());
            assertTrue(editor.getTimeReservationPanel().isEnabled());

            assertEquals(2, editor.getChairsField().getValue());
            assertNotNull(editor.getTimeReservationPanel().getDate());
        }

        @Test
        @DisplayName("Should populate fields and enable editing on writeValueEdit")
        void shouldPopulateFieldsOnEdit() throws BasicException {
            Date now = new Date();
            Object[] data = new Object[]{
                "res-001",
                now,
                now,
                "cust-001",
                "TAX-123",
                "KEY-123",
                "Alice Smith",
                4,
                false,
                "Window table requested"
            };

            editor.writeValueEdit(data);

            assertEquals("Alice Smith", editor.getCustomerField().getText());
            assertEquals(4, editor.getChairsField().getValue());
            assertEquals("Window table requested", editor.getDescriptionField().getText());
            assertTrue(editor.getCustomerField().isEnabled());
            assertTrue(editor.getChairsField().isEnabled());

            Object result = editor.createValue();
            assertTrue(result instanceof Object[]);
            Object[] created = (Object[]) result;
            assertEquals("res-001", created[0]);
            assertEquals("cust-001", created[3]);
            assertEquals("TAX-123", created[4]);
            assertEquals("KEY-123", created[5]);
            assertEquals("Alice Smith", created[6]);
            assertEquals(4, created[7]);
            assertEquals(false, created[8]);
            assertEquals("Window table requested", created[9]);
        }

        @Test
        @DisplayName("Should disable all fields on writeValueDelete")
        void shouldDisableFieldsOnDelete() {
            Date now = new Date();
            Object[] data = new Object[]{
                "res-002",
                now,
                now,
                "cust-002",
                "TAX-456",
                "KEY-456",
                "Bob Jones",
                2,
                false,
                "Standard table"
            };

            editor.writeValueDelete(data);

            assertFalse(editor.getCustomerField().isEnabled());
            assertFalse(editor.getChairsField().isEnabled());
            assertFalse(editor.getDescriptionField().isEnabled());
            assertFalse(editor.getTimeReservationPanel().isEnabled());
        }
    }

    @Nested
    @DisplayName("Validation & Two-Way Binding")
    class ValidationTests {

        @Test
        @DisplayName("Should validate successfully when customer and positive chairs are present")
        void shouldValidateWhenValid() {
            editor.writeValueInsert();

            CustomerInfo customer = new CustomerInfo("c-100");
            customer.setName("Maria Silva");
            editor.setCustomer(customer);
            editor.getChairsField().setValueInteger(4);

            assertTrue(editor.isValid());
            assertTrue(editor.isDataValid());
        }

        @Test
        @DisplayName("Should fail validation when customer name is empty")
        void shouldFailWhenCustomerEmpty() {
            editor.writeValueInsert();

            editor.setCustomer(new CustomerInfo(null));
            editor.getCustomerField().setText("");
            editor.getChairsField().setValueInteger(4);

            assertFalse(editor.isValid());
            assertFalse(editor.isDataValid());
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1, -5})
        @DisplayName("Should fail validation when chairs is non-positive")
        void shouldFailWhenChairsNonPositive(int invalidChairs) {
            editor.writeValueInsert();

            CustomerInfo customer = new CustomerInfo("c-200");
            customer.setName("Carlos Gomez");
            editor.setCustomer(customer);
            editor.getChairsField().setValueInteger(invalidChairs);

            assertFalse(editor.isValid());
        }

        @Test
        @DisplayName("Should track dirty state on component modification")
        void shouldTrackDirtyState() {
            editor.writeValueInsert();
            dirty.setDirty(false);

            editor.getCustomerField().setText("New Customer Name");

            assertTrue(dirty.isDirty());
        }
    }
}
