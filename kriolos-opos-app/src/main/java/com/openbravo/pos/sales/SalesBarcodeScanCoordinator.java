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
import com.openbravo.data.gui.MessageInf;
import com.openbravo.pos.catalog.CatalogService;
import com.openbravo.pos.customers.CustomerService;
import com.openbravo.pos.customers.DataLogicCustomers;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.pim.DataLogicPIM;
import com.openbravo.pos.customers.CustomerInfoExt;
import com.openbravo.pos.ticket.ProductInfoExt;
import com.openbravo.pos.ticket.TaxInfo;
import com.openbravo.pos.util.NotifyUtils;
import java.awt.Component;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Coordinates POS barcode scanning resolution, including customer cards,
 * variable weight/price barcodes (EAN-13 and UPC-A), and standard product barcodes.
 */
public class SalesBarcodeScanCoordinator {

    private static final System.Logger LOGGER = System.getLogger(SalesBarcodeScanCoordinator.class.getName());

    @FunctionalInterface
    public interface TriConsumer<T, U, V> {
        void accept(T t, U u, V v);
    }

    @FunctionalInterface
    public interface ProductLookup {
        ProductInfoExt find(String code) throws BasicException;
    }

    @FunctionalInterface
    public interface CustomerLookup {
        CustomerInfoExt find(String card) throws BasicException;
    }

    public sealed interface BarcodeScanResult {
        record CustomerScanned(CustomerInfoExt customer) implements BarcodeScanResult {}
        record VariableProductScanned(ProductInfoExt product, double units, double priceSell) implements BarcodeScanResult {}
        record StandardProductScanned(ProductInfoExt product) implements BarcodeScanResult {}
        record Track2CardSwiped(String cardData) implements BarcodeScanResult {}
        record NotFound(String code, String message) implements BarcodeScanResult {}
        record ScanError(String code, Exception exception) implements BarcodeScanResult {}
    }

    private final ProductLookup byCode;
    private final ProductLookup byShortCode;
    private final ProductLookup byUShortCode;
    private final CustomerLookup byCard;

    public SalesBarcodeScanCoordinator(CatalogService catalogService, CustomerService customerService) {
        this(
            code -> catalogService != null ? catalogService.getProductInfoByCode(code) : null,
            code -> catalogService != null ? catalogService.getProductInfoByShortCode(code) : null,
            code -> catalogService != null ? catalogService.getProductInfoByUShortCode(code) : null,
            card -> customerService != null ? customerService.findCustomerInfoExtByCard(card) : null
        );
    }

    @Deprecated
    public SalesBarcodeScanCoordinator(DataLogicPIM dataLogicPIM, DataLogicCustomers dlCustomers) {
        this((CatalogService) dataLogicPIM, (CustomerService) dlCustomers);
    }

    SalesBarcodeScanCoordinator(ProductLookup byCode, ProductLookup byShortCode,
                               ProductLookup byUShortCode, CustomerLookup byCard) {
        this.byCode = byCode != null ? byCode : code -> null;
        this.byShortCode = byShortCode != null ? byShortCode : code -> null;
        this.byUShortCode = byUShortCode != null ? byUShortCode : code -> null;
        this.byCard = byCard != null ? byCard : card -> null;
    }

    /**
     * Resolves the scanned barcode text to a strongly-typed {@link BarcodeScanResult}
     * with automatic format identification (Customer card, EAN-13 variable, UPC-A variable,
     * Track-2 magnetic swipe, or standard product code) without requiring configuration flags.
     *
     * @param barcode the scanned raw barcode string
     * @param taxeslogic taxes calculation logic
     * @param customer current ticket customer (for tax rate resolution)
     * @param taxesIncluded whether taxes are included in prices
     * @return the resolved {@link BarcodeScanResult}
     */
    public BarcodeScanResult resolveBarcode(String barcode, TaxesLogic taxeslogic,
                                            CustomerInfoExt customer, boolean taxesIncluded) {
        if (barcode == null || barcode.isEmpty()) {
            return new BarcodeScanResult.NotFound("", AppLocal.getIntString("message.noproduct"));
        }

        // 1. Customer Loyalty Card Scan (Prefix C or c)
        if (barcode.startsWith("C") || barcode.startsWith("c")) {
            try {
                CustomerInfoExt newCustomer = byCard.find(barcode);
                if (newCustomer != null) {
                    return new BarcodeScanResult.CustomerScanned(newCustomer);
                } else {
                    return new BarcodeScanResult.NotFound(barcode, AppLocal.getIntString("message.nocustomer"));
                }
            } catch (BasicException ex) {
                LOGGER.log(System.Logger.Level.WARNING, "Exception looking up customer by card: " + barcode, ex);
                return new BarcodeScanResult.ScanError(barcode, ex);
            }
        }

        // 2. Magnetic Stripe Track 2 swipe (Prefix ;)
        if (barcode.startsWith(";")) {
            return new BarcodeScanResult.Track2CardSwiped(barcode);
        }

        // 3. Variable Weight / Price Barcode (Auto-detection by length, prefix, and product codetype)
        if (EmbeddedBarcodeDecoder.isEanVariableBarcode(barcode) || EmbeddedBarcodeDecoder.isUpcVariableBarcode(barcode)) {
            // EAN-13: 13 digits or starting with "02"
            if (barcode.length() == 13 || barcode.startsWith("02")) {
                try {
                    ProductInfoExt oProduct = byShortCode.find(barcode);
                    if (oProduct != null && "EAN-13".equalsIgnoreCase(oProduct.getCodetype())) {
                        TaxInfo tax = taxeslogic != null ? taxeslogic.getTaxInfo(oProduct.getTaxCategoryID(), customer) : null;
                        EmbeddedBarcodeDecoder.DecodedBarcode decoded = EmbeddedBarcodeDecoder.decodeEan(barcode, oProduct, tax, taxesIncluded);
                        return new BarcodeScanResult.VariableProductScanned(oProduct, decoded.getUnits(), decoded.getPriceSell());
                    }
                } catch (BasicException ex) {
                    LOGGER.log(System.Logger.Level.WARNING, "Exception processing EAN barcode: " + barcode, ex);
                    return new BarcodeScanResult.ScanError(barcode, ex);
                }
            }

            // 12 digits: check UPC-A first, then 12-digit EAN short code
            if (barcode.length() == 12) {
                try {
                    ProductInfoExt upcProduct = byUShortCode.find(barcode);
                    if (upcProduct != null && "UPC-A".equalsIgnoreCase(upcProduct.getCodetype())) {
                        TaxInfo tax = taxeslogic != null ? taxeslogic.getTaxInfo(upcProduct.getTaxCategoryID(), customer) : null;
                        EmbeddedBarcodeDecoder.DecodedBarcode decoded = EmbeddedBarcodeDecoder.decodeUpcA(barcode, upcProduct, tax, taxesIncluded);
                        return new BarcodeScanResult.VariableProductScanned(upcProduct, decoded.getUnits(), decoded.getPriceSell());
                    }

                    ProductInfoExt eanProduct = byShortCode.find(barcode);
                    if (eanProduct != null && "EAN-13".equalsIgnoreCase(eanProduct.getCodetype())) {
                        TaxInfo tax = taxeslogic != null ? taxeslogic.getTaxInfo(eanProduct.getTaxCategoryID(), customer) : null;
                        EmbeddedBarcodeDecoder.DecodedBarcode decoded = EmbeddedBarcodeDecoder.decodeEan(barcode, eanProduct, tax, taxesIncluded);
                        return new BarcodeScanResult.VariableProductScanned(eanProduct, decoded.getUnits(), decoded.getPriceSell());
                    }
                } catch (BasicException ex) {
                    LOGGER.log(System.Logger.Level.WARNING, "Exception processing variable barcode: " + barcode, ex);
                    return new BarcodeScanResult.ScanError(barcode, ex);
                }
            }
        }

        // 4. Standard Product Barcode (fallback for any non-variable code)
        try {
            ProductInfoExt oProduct = byCode.find(barcode);
            if (oProduct != null) {
                return new BarcodeScanResult.StandardProductScanned(oProduct);
            } else {
                return new BarcodeScanResult.NotFound(barcode, barcode + " - " + AppLocal.getIntString("message.noproduct"));
            }
        } catch (BasicException ex) {
            LOGGER.log(System.Logger.Level.WARNING, "Exception processing standard barcode: " + barcode, ex);
            return new BarcodeScanResult.ScanError(barcode, ex);
        }
    }

    /**
     * Backward-compatible overload accepting isUpcMode flag (delegates to automatic detection).
     */
    public BarcodeScanResult resolveBarcode(String barcode, boolean isUpcMode, TaxesLogic taxeslogic,
                                            CustomerInfoExt customer, boolean taxesIncluded) {
        return resolveBarcode(barcode, taxeslogic, customer, taxesIncluded);
    }

    /**
     * Processes a scanned barcode with automatic format identification without requiring a configuration flag.
     */
    public void processBarcode(Component parent, String barcode, TaxesLogic taxeslogic,
                               CustomerInfoExt customer, boolean taxesIncluded,
                               Consumer<CustomerInfoExt> customerConsumer,
                               TriConsumer<ProductInfoExt, Double, Double> variableProductConsumer,
                               Consumer<ProductInfoExt> standardProductConsumer,
                               Runnable stateReset) {
        processBarcode(parent, barcode, false, taxeslogic, customer, taxesIncluded,
                customerConsumer, variableProductConsumer, standardProductConsumer, stateReset);
    }

    /**
     * Processes a scanned barcode and invokes the corresponding success callback, or notifies the user on error/missing item.
     *
     * @param parent parent UI component for alerts/dialogs
     * @param barcode raw scanned barcode
     * @param isUpcMode true if UPC mode is enabled
     * @param taxeslogic active taxes logic
     * @param customer current ticket customer
     * @param taxesIncluded whether taxes are included
     * @param customerConsumer callback when a customer card is scanned
     * @param variableProductConsumer callback when a variable EAN/UPC product is decoded
     * @param standardProductConsumer callback when a standard product is scanned
     * @param stateReset callback to reset keypad state
     */
    public void processBarcode(Component parent, String barcode, boolean isUpcMode, TaxesLogic taxeslogic,
                               CustomerInfoExt customer, boolean taxesIncluded,
                               Consumer<CustomerInfoExt> customerConsumer,
                               TriConsumer<ProductInfoExt, Double, Double> variableProductConsumer,
                               Consumer<ProductInfoExt> standardProductConsumer,
                               Runnable stateReset) {
        BarcodeScanResult result = resolveBarcode(barcode, isUpcMode, taxeslogic, customer, taxesIncluded);

        if (result instanceof BarcodeScanResult.CustomerScanned cs) {
            if (customerConsumer != null) {
                customerConsumer.accept(cs.customer());
            }
            if (stateReset != null) {
                stateReset.run();
            }
        } else if (result instanceof BarcodeScanResult.VariableProductScanned vps) {
            if (variableProductConsumer != null) {
                variableProductConsumer.accept(vps.product(), vps.units(), vps.priceSell());
            }
        } else if (result instanceof BarcodeScanResult.StandardProductScanned sps) {
            if (standardProductConsumer != null) {
                standardProductConsumer.accept(sps.product());
            }
        } else if (result instanceof BarcodeScanResult.Track2CardSwiped) {
            if (stateReset != null) {
                stateReset.run();
            }
        } else if (result instanceof BarcodeScanResult.NotFound nf) {
            NotifyUtils.beep();
            if (parent != null) {
                new MessageInf(MessageInf.SGN_WARNING, nf.message()).show(parent);
            }
            if (stateReset != null) {
                stateReset.run();
            }
        } else if (result instanceof BarcodeScanResult.ScanError se) {
            NotifyUtils.beep();
            if (parent != null) {
                new MessageInf(se.exception()).show(parent);
            }
            if (stateReset != null) {
                stateReset.run();
            }
        }
    }
}
