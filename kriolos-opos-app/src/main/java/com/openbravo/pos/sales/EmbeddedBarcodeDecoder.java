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
package com.openbravo.pos.sales;

import com.openbravo.pos.ticket.ProductInfoExt;
import com.openbravo.pos.ticket.TaxInfo;
import com.openbravo.pos.domain.utils.AmountCalculatorUtil;

/**
 * Decodes embedded price and weight barcodes for EAN and UPC barcode standards.
 */
public final class EmbeddedBarcodeDecoder {

    public static class DecodedBarcode {
        private final double units;
        private final double priceSell;
        private final double weight;

        public DecodedBarcode(double units, double priceSell, double weight) {
            this.units = units;
            this.priceSell = priceSell;
            this.weight = weight;
        }

        public double getUnits() {
            return units;
        }

        public double getPriceSell() {
            return priceSell;
        }

        public double getWeight() {
            return weight;
        }
    }

    private EmbeddedBarcodeDecoder() {
    }

    public static boolean isEanVariableBarcode(String code) {
        if (code == null) {
            return false;
        }
        return (code.startsWith("2") || code.startsWith("02"))
                && (code.length() == 13 || code.length() == 12);
    }

    public static boolean isUpcVariableBarcode(String code) {
        if (code == null) {
            return false;
        }
        return code.startsWith("2") && code.length() == 12;
    }

    /**
     * Decodes an EAN-13 variable barcode with embedded price or weight.
     */
    public static DecodedBarcode decodeEan(String code, ProductInfoExt product, TaxInfo tax, boolean addTax) {
        product.setProperty("product.barcode", code);

        double dPriceSell = product.getPriceSell();
        double weight = 0.0;
        double dUnits = 0.0;

        String variableTypePrefix = code.substring(0, 2);
        String variableNum = (code.length() == 13)
                ? code.substring(8, 12)
                : code.substring(7, 11);

        switch (variableTypePrefix) {
            case "02", "20" -> {
                dPriceSell = AmountCalculatorUtil.calcPriceWithoutTax(product.getPriceSellTax(tax), tax);
                dUnits = (Double.parseDouble(variableNum) / 100.0) / product.getPriceSellTax(tax);
                product.setProperty("product.price", Double.toString(product.getPriceSell()));
            }
            case "21" -> {
                dPriceSell = AmountCalculatorUtil.calcPriceWithoutTax(product.getPriceSellTax(tax), tax);
                dUnits = (Double.parseDouble(variableNum) / 10.0) / product.getPriceSellTax(tax);
                product.setProperty("product.price", Double.toString(product.getPriceSell()));
            }
            case "22" -> {
                dPriceSell = AmountCalculatorUtil.calcPriceWithoutTax(product.getPriceSellTax(tax), tax);
                dUnits = Double.parseDouble(variableNum) / product.getPriceSellTax(tax);
                product.setProperty("product.price", Double.toString(product.getPriceSell()));
            }
            case "23" -> {
                weight = Double.parseDouble(variableNum) / 1000.0;
                dUnits = weight;
                product.setProperty("product.weight", Double.toString(weight));
                product.setProperty("product.price", Double.toString(dPriceSell));
            }
            case "24" -> {
                weight = Double.parseDouble(variableNum) / 100.0;
                dUnits = weight;
                product.setProperty("product.weight", Double.toString(weight));
                product.setProperty("product.price", Double.toString(dPriceSell));
            }
            case "25" -> {
                weight = Double.parseDouble(variableNum) / 10.0;
                dUnits = weight;
                product.setProperty("product.weight", Double.toString(weight));
                product.setProperty("product.price", Double.toString(dPriceSell));
            }
            default -> {
            }
        }

        if (addTax) {
            dPriceSell = product.getPriceSellTax(tax);
        }

        return new DecodedBarcode(dUnits, dPriceSell, weight);
    }

    /**
     * Decodes a UPC-A variable barcode with embedded weight or price.
     */
    public static DecodedBarcode decodeUpcA(String code, ProductInfoExt product, TaxInfo tax, boolean addTax) {
        product.setProperty("product.barcode", code);

        double dPriceSell;
        double weight = 0.0;
        double dUnits;
        String variableNum = code.substring(7, 11);

        if (product.getPriceSell() != 0.0) {
            weight = Double.parseDouble(variableNum) / 100.0;
            product.setProperty("product.weight", Double.toString(weight));
            product.setProperty("product.price", Double.toString(product.getPriceSell()));
            dPriceSell = product.getPriceSellTax(tax);
            dUnits = (Double.parseDouble(variableNum) / 100.0) / product.getPriceSellTax(tax);
        } else {
            dPriceSell = Double.parseDouble(variableNum) / 100.0;
            dUnits = 1.0;
        }

        if (!addTax) {
            dPriceSell = AmountCalculatorUtil.calcPriceWithoutTax(dPriceSell, tax);
        }

        return new DecodedBarcode(dUnits, dPriceSell, weight);
    }
}
