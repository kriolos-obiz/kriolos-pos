//    KriolOS POS
//    Copyright (c) 2019-2026 KriolOS
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

package com.openbravo.pos.sales;

import com.openbravo.basic.BasicException;
import com.openbravo.pos.customers.CustomerInfoExt;
import com.openbravo.pos.ticket.ProductInfoExt;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SalesBarcodeScanCoordinator Test Suite")
class SalesBarcodeScanCoordinatorTest {

    @Test
    @DisplayName("resolveBarcode resolves customer loyalty card when card exists")
    void testResolveBarcode_CustomerCardFound() {
        CustomerInfoExt customer = new CustomerInfoExt("CUST-1");
        customer.setName("John Doe");

        SalesBarcodeScanCoordinator coordinator = new SalesBarcodeScanCoordinator(
                code -> null,
                code -> null,
                code -> null,
                card -> "C12345".equals(card) ? customer : null
        );

        SalesBarcodeScanCoordinator.BarcodeScanResult result = coordinator.resolveBarcode(
                "C12345", false, null, null, false);

        assertTrue(result instanceof SalesBarcodeScanCoordinator.BarcodeScanResult.CustomerScanned);
        SalesBarcodeScanCoordinator.BarcodeScanResult.CustomerScanned cs =
                (SalesBarcodeScanCoordinator.BarcodeScanResult.CustomerScanned) result;
        assertEquals("John Doe", cs.customer().getName());
    }

    @Test
    @DisplayName("resolveBarcode returns NotFound when customer loyalty card is not found")
    void testResolveBarcode_CustomerCardNotFound() {
        SalesBarcodeScanCoordinator coordinator = new SalesBarcodeScanCoordinator(
                code -> null,
                code -> null,
                code -> null,
                card -> null
        );

        SalesBarcodeScanCoordinator.BarcodeScanResult result = coordinator.resolveBarcode(
                "C99999", false, null, null, false);

        assertTrue(result instanceof SalesBarcodeScanCoordinator.BarcodeScanResult.NotFound);
    }

    @Test
    @DisplayName("resolveBarcode returns Track2CardSwiped for magnetic stripe prefix")
    void testResolveBarcode_Track2Swipe() {
        SalesBarcodeScanCoordinator coordinator = new SalesBarcodeScanCoordinator(
                code -> null,
                code -> null,
                code -> null,
                card -> null
        );

        SalesBarcodeScanCoordinator.BarcodeScanResult result = coordinator.resolveBarcode(
                ";411111111111?", false, null, null, false);

        assertTrue(result instanceof SalesBarcodeScanCoordinator.BarcodeScanResult.Track2CardSwiped);
    }

    @Test
    @DisplayName("resolveBarcode resolves standard product barcode")
    void testResolveBarcode_StandardProduct() {
        ProductInfoExt product = new ProductInfoExt();
        product.setID("PROD-1");
        product.setCode("5601234567890");
        product.setName("Soda 33cl");
        product.setPriceSell(1.50);

        SalesBarcodeScanCoordinator coordinator = new SalesBarcodeScanCoordinator(
                code -> "5601234567890".equals(code) ? product : null,
                code -> null,
                code -> null,
                card -> null
        );

        SalesBarcodeScanCoordinator.BarcodeScanResult result = coordinator.resolveBarcode(
                "5601234567890", false, null, null, false);

        assertTrue(result instanceof SalesBarcodeScanCoordinator.BarcodeScanResult.StandardProductScanned);
        SalesBarcodeScanCoordinator.BarcodeScanResult.StandardProductScanned sp =
                (SalesBarcodeScanCoordinator.BarcodeScanResult.StandardProductScanned) result;
        assertEquals("Soda 33cl", sp.product().getName());
    }

    @Test
    @DisplayName("resolveBarcode returns NotFound for unknown barcode")
    void testResolveBarcode_UnknownBarcode() {
        SalesBarcodeScanCoordinator coordinator = new SalesBarcodeScanCoordinator(
                code -> null,
                code -> null,
                code -> null,
                card -> null
        );

        SalesBarcodeScanCoordinator.BarcodeScanResult result = coordinator.resolveBarcode(
                "0000000000000", false, null, null, false);

        assertTrue(result instanceof SalesBarcodeScanCoordinator.BarcodeScanResult.NotFound);
    }

    @Test
    @DisplayName("processBarcode invokes customerConsumer and resets state for card scan")
    void testProcessBarcode_CustomerDispatch() {
        CustomerInfoExt customer = new CustomerInfoExt("CUST-1");
        customer.setName("Maria Silva");

        SalesBarcodeScanCoordinator coordinator = new SalesBarcodeScanCoordinator(
                code -> null,
                code -> null,
                code -> null,
                card -> customer
        );

        AtomicReference<CustomerInfoExt> receivedCustomer = new AtomicReference<>();
        AtomicBoolean resetCalled = new AtomicBoolean(false);

        coordinator.processBarcode(
                null,
                "c98765",
                false,
                null,
                null,
                false,
                receivedCustomer::set,
                (p, u, pr) -> fail("Variable product consumer should not be called"),
                p -> fail("Standard product consumer should not be called"),
                () -> resetCalled.set(true)
        );

        assertNotNull(receivedCustomer.get());
        assertEquals("Maria Silva", receivedCustomer.get().getName());
        assertTrue(resetCalled.get());
    }

    @Test
    @DisplayName("processBarcode invokes standardProductConsumer for standard products")
    void testProcessBarcode_StandardProductDispatch() {
        ProductInfoExt product = new ProductInfoExt();
        product.setName("Bread");

        SalesBarcodeScanCoordinator coordinator = new SalesBarcodeScanCoordinator(
                code -> product,
                code -> null,
                code -> null,
                card -> null
        );

        AtomicReference<ProductInfoExt> receivedProduct = new AtomicReference<>();

        coordinator.processBarcode(
                null,
                "12345678",
                false,
                null,
                null,
                false,
                c -> fail("Customer consumer should not be called"),
                (p, u, pr) -> fail("Variable product consumer should not be called"),
                receivedProduct::set,
                () -> {}
        );

        assertNotNull(receivedProduct.get());
        assertEquals("Bread", receivedProduct.get().getName());
    }

    @Test
    @DisplayName("resolveBarcode automatically identifies EAN-13 variable barcode without config flag")
    void testResolveBarcode_AutoDetectEanVariable() {
        ProductInfoExt eanProduct = new ProductInfoExt();
        eanProduct.setID("EAN-PROD");
        eanProduct.setCodetype("EAN-13");
        eanProduct.setName("Apples KG");
        eanProduct.setPriceSell(2.50);

        SalesBarcodeScanCoordinator coordinator = new SalesBarcodeScanCoordinator(
                code -> null,
                code -> eanProduct,
                code -> null,
                card -> null
        );

        // 13-digit barcode starting with 20: 2012345005001
        SalesBarcodeScanCoordinator.BarcodeScanResult result = coordinator.resolveBarcode(
                "2012345005001", null, null, false);

        assertTrue(result instanceof SalesBarcodeScanCoordinator.BarcodeScanResult.VariableProductScanned);
        SalesBarcodeScanCoordinator.BarcodeScanResult.VariableProductScanned vps =
                (SalesBarcodeScanCoordinator.BarcodeScanResult.VariableProductScanned) result;
        assertEquals("Apples KG", vps.product().getName());
        assertEquals(2.50, vps.priceSell(), 0.001);
    }

    @Test
    @DisplayName("resolveBarcode automatically identifies UPC-A variable barcode without config flag")
    void testResolveBarcode_AutoDetectUpcVariable() {
        ProductInfoExt upcProduct = new ProductInfoExt();
        upcProduct.setID("UPC-PROD");
        upcProduct.setCodetype("UPC-A");
        upcProduct.setName("Pears Pack");
        upcProduct.setPriceSell(3.00);

        SalesBarcodeScanCoordinator coordinator = new SalesBarcodeScanCoordinator(
                code -> null,
                code -> null,
                code -> upcProduct,
                card -> null
        );

        // 12-digit barcode starting with 2: 212345601509
        SalesBarcodeScanCoordinator.BarcodeScanResult result = coordinator.resolveBarcode(
                "212345601509", null, null, false);

        assertTrue(result instanceof SalesBarcodeScanCoordinator.BarcodeScanResult.VariableProductScanned);
        SalesBarcodeScanCoordinator.BarcodeScanResult.VariableProductScanned vps =
                (SalesBarcodeScanCoordinator.BarcodeScanResult.VariableProductScanned) result;
        assertEquals("Pears Pack", vps.product().getName());
    }

    @Test
    @DisplayName("resolveBarcode falls back to standard product when 12-digit code is not variable")
    void testResolveBarcode_AutoFallbackToStandard() {
        ProductInfoExt stdProduct = new ProductInfoExt();
        stdProduct.setID("STD-PROD");
        stdProduct.setCode("212345678901");
        stdProduct.setName("Standard Canned Item");
        stdProduct.setPriceSell(4.20);

        SalesBarcodeScanCoordinator coordinator = new SalesBarcodeScanCoordinator(
                code -> "212345678901".equals(code) ? stdProduct : null,
                code -> null, // No EAN variable short code
                code -> null, // No UPC variable short code
                card -> null
        );

        SalesBarcodeScanCoordinator.BarcodeScanResult result = coordinator.resolveBarcode(
                "212345678901", null, null, false);

        assertTrue(result instanceof SalesBarcodeScanCoordinator.BarcodeScanResult.StandardProductScanned);
        SalesBarcodeScanCoordinator.BarcodeScanResult.StandardProductScanned sp =
                (SalesBarcodeScanCoordinator.BarcodeScanResult.StandardProductScanned) result;
        assertEquals("Standard Canned Item", sp.product().getName());
    }
}
