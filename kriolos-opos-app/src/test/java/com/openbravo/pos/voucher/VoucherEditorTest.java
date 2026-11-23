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

package com.openbravo.pos.voucher;

import com.openbravo.basic.BasicException;
import com.openbravo.data.user.DirtyManager;
import com.openbravo.format.Formats;
import com.openbravo.pos.customers.CustomerInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Voucher Editor Type-Safe Lifecycle & Validation Test Suite")
class VoucherEditorTest {

    private DirtyManager dirty;
    private VoucherEditor editor;

    @BeforeEach
    void setUp() {
        dirty = new DirtyManager();
        editor = new VoucherEditor(dirty);
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

            assertFalse(editor.getVoucherNumberField().isEnabled());
            assertFalse(editor.getCustomerField().isEnabled());
            assertFalse(editor.getAmountField().isEnabled());
            assertFalse(editor.getStatusField().isEnabled());
            assertFalse(editor.getCustomerSelectorButton().isEnabled());
            assertFalse(editor.getPrintButton().isEnabled());

            assertEquals("", editor.getVoucherNumberField().getText());
            assertEquals("", editor.getCustomerField().getText());
            assertEquals("", editor.getAmountField().getText());
            assertEquals("", editor.getStatusField().getText());
        }
    }

    @Nested
    @DisplayName("Lifecycle Operations")
    class LifecycleTests {

        @Test
        @DisplayName("Should initialize fields and enable inputs on writeValueInsert")
        void shouldHandleInsert() {
            editor.writeValueInsert();

            assertTrue(editor.getVoucherNumberField().isEnabled());
            assertTrue(editor.getCustomerField().isEnabled());
            assertTrue(editor.getAmountField().isEnabled());
            assertFalse(editor.getStatusField().isEnabled());
            assertTrue(editor.getCustomerSelectorButton().isEnabled());
            assertTrue(editor.getPrintButton().isEnabled());

            assertNotNull(editor.getVoucherNumberField().getText());
            assertFalse(editor.getVoucherNumberField().getText().isBlank());
            assertEquals(VoucherInfo.VOUCHER_STATUS_AVAILABLE, editor.getStatusField().getText());
        }

        @Test
        @DisplayName("Should populate fields and enable editing on writeValueEdit")
        void shouldPopulateFieldsOnEdit() throws BasicException {
            Object[] data = new Object[]{
                "v-001",
                "VO-2026-001",
                "cust-123",
                75.50,
                VoucherInfo.VOUCHER_STATUS_AVAILABLE
            };

            editor.writeValueEdit(data);

            assertEquals("VO-2026-001", editor.getVoucherNumberField().getText());
            assertEquals(Formats.DOUBLE.formatValue(75.50), editor.getAmountField().getText());
            assertEquals(VoucherInfo.VOUCHER_STATUS_AVAILABLE, editor.getStatusField().getText());
            assertTrue(editor.getVoucherNumberField().isEnabled());
            assertTrue(editor.getAmountField().isEnabled());
            assertTrue(editor.getPrintButton().isEnabled());

            Object result = editor.createValue();
            assertTrue(result instanceof Object[]);
            Object[] created = (Object[]) result;
            assertEquals("v-001", created[0]);
            assertEquals("VO-2026-001", created[1]);
            assertEquals(75.50, (Double) created[3], 0.001);
            assertEquals(VoucherInfo.VOUCHER_STATUS_AVAILABLE, created[4]);
        }

        @Test
        @DisplayName("Should disable editing when voucher is redeemed on writeValueEdit")
        void shouldDisableEditingWhenRedeemed() {
            Object[] data = new Object[]{
                "v-002",
                "VO-2026-002",
                "cust-456",
                100.00,
                VoucherInfo.VOUCHER_STATUS_REDEEMED
            };

            editor.writeValueEdit(data);

            assertEquals("VO-2026-002", editor.getVoucherNumberField().getText());
            assertEquals(VoucherInfo.VOUCHER_STATUS_REDEEMED, editor.getStatusField().getText());
            assertFalse(editor.getVoucherNumberField().isEnabled());
            assertFalse(editor.getAmountField().isEnabled());
            assertFalse(editor.getCustomerSelectorButton().isEnabled());
        }

        @Test
        @DisplayName("Should disable all fields on writeValueDelete")
        void shouldDisableFieldsOnDelete() {
            Object[] data = new Object[]{
                "v-003",
                "VO-2026-003",
                "cust-789",
                50.00,
                VoucherInfo.VOUCHER_STATUS_AVAILABLE
            };

            editor.writeValueDelete(data);

            assertFalse(editor.getVoucherNumberField().isEnabled());
            assertFalse(editor.getCustomerField().isEnabled());
            assertFalse(editor.getAmountField().isEnabled());
            assertFalse(editor.getStatusField().isEnabled());
            assertFalse(editor.getCustomerSelectorButton().isEnabled());
            assertFalse(editor.getPrintButton().isEnabled());
        }
    }

    @Nested
    @DisplayName("Validation & Two-Way Binding")
    class ValidationTests {

        @Test
        @DisplayName("Should validate successfully when all required fields and positive amount are provided")
        void shouldValidateWhenValid() {
            editor.writeValueInsert();

            editor.getVoucherNumberField().setText("VO-TEST-123");
            CustomerInfo customer = new CustomerInfo("c-1");
            customer.setName("John Doe");
            editor.setCustomerInfo(customer);
            editor.getAmountField().setText(Formats.DOUBLE.formatValue(25.00));

            assertTrue(editor.isValid());
            assertTrue(editor.isDataValid());
        }

        @Test
        @DisplayName("Should fail validation when voucher number is empty")
        void shouldFailWhenVoucherNumberEmpty() {
            editor.writeValueInsert();

            editor.getVoucherNumberField().setText("");
            CustomerInfo customer = new CustomerInfo("c-1");
            customer.setName("John Doe");
            editor.setCustomerInfo(customer);
            editor.getAmountField().setText(Formats.DOUBLE.formatValue(25.00));

            assertFalse(editor.isValid());
        }

        @Test
        @DisplayName("Should fail validation when customer name is empty")
        void shouldFailWhenCustomerEmpty() {
            editor.writeValueInsert();

            editor.getVoucherNumberField().setText("VO-TEST-123");
            editor.getCustomerField().setText("");
            editor.getAmountField().setText(Formats.DOUBLE.formatValue(25.00));

            assertFalse(editor.isValid());
        }

        @ParameterizedTest
        @ValueSource(strings = {"0", "0.0", "-10.0", "-0.01", "invalid_amount", ""})
        @DisplayName("Should fail validation when amount is non-positive or invalid")
        void shouldFailWhenAmountInvalid(String amountInput) {
            editor.writeValueInsert();

            editor.getVoucherNumberField().setText("VO-TEST-123");
            CustomerInfo customer = new CustomerInfo("c-1");
            customer.setName("John Doe");
            editor.setCustomerInfo(customer);
            editor.getAmountField().setText(amountInput);

            assertFalse(editor.isValid());
        }

        @Test
        @DisplayName("Should track dirty state on text field modification")
        void shouldTrackDirtyState() {
            editor.writeValueInsert();
            dirty.setDirty(false);

            editor.getAmountField().setText(Formats.DOUBLE.formatValue(150.0));

            assertTrue(dirty.isDirty());
        }
    }
}
