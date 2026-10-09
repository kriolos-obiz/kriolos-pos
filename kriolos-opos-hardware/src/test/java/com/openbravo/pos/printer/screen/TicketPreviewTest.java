/*
 * Copyright (C) 2026 KriolOS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.openbravo.pos.printer.screen;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Standalone test harness for the KriolOS Screen Printer features. Provides
 * centralized monetary scale, rounding mode, currency formatting, and tax
 * presentation configuration (tax inclusive vs. tax exclusive).
 *
 * @author KriolOS
 */
public class TicketPreviewTest {

    // =========================================================================
    // MONETARY PRECISION, ROUNDING & FORMATTING CONFIGURATION
    // =========================================================================
    /**
     * Placement of the currency symbol relative to the numeric amount.
     */
    public enum SymbolPosition {
        PREFIX, // Example: $ 10.00 or $10.00
        SUFFIX  // Example: 1000 CVE or 10.00 €
    }

    /**
     * Determines whether catalog and line item prices include tax or exclude
     * tax.
     */
    public enum TaxDisplayMode {
        TAX_EXCLUSIVE, // Prices exclude tax; tax is added on top of the subtotal
        TAX_INCLUSIVE  // Prices include tax; tax is extracted from the total
    }

    /**
     * Active tax presentation mode. How It Works: TAX_EXCLUSIVE (US / B2B
     * style): Catalog unit prices are treated as net (before tax). Line item
     * amounts show the net price. Subtotal shows the sum of net amounts. Tax is
     * added at the bottom: $\text{Total} = \text{Subtotal} + \text{Tax}$.
     * TAX_INCLUSIVE (Europe / Cabo Verde / Retail style): Catalog unit prices
     * are treated as gross (tax already included). Line item amounts show the
     * gross price. Total is the exact sum of line items. Subtotal and tax are
     * extracted from the total:
     *
     *
     *
     * $$\text{Subtotal} = \frac{\text{Total}}{1 + \text{Tax Rate}}$$
     * $$\text{Tax} = \text{Total} - \text{Subtotal}$$
     */
    public static final TaxDisplayMode TAX_DISPLAY_MODE = TaxDisplayMode.TAX_INCLUSIVE;

    /**
     * Number of fractional decimal places (e.g., 0 for CVE/JPY, 2 for USD/EUR,
     * 3 for BHD/KWD).
     */
    public static final int CURRENCY_DECIMALS = 0;

    /**
     * Standard financial rounding strategy applied to calculations.
     */
    public static final RoundingMode FINANCIAL_ROUNDING = RoundingMode.HALF_UP;

    /**
     * Currency symbol or code displayed on formatted lines (e.g., "$", "€",
     * "CVE").
     */
    public static final String CURRENCY_SYMBOL = "$";

    /**
     * Determines whether the currency symbol appears before or after the
     * number.
     */
    public static final SymbolPosition CURRENCY_SYMBOL_POSITION = SymbolPosition.SUFFIX;

    /**
     * Whether to insert a space between the number and the symbol.
     */
    public static final boolean CURRENCY_USE_SPACE = false;

    // Standard 15% tax rate
    public static final BigDecimal TAX_RATE = new BigDecimal("0.15");

    // =========================================================================
    // LAYOUT CONSTANTS & CATALOG
    // =========================================================================
    private static final int RECEIPT_LINE_WIDTH = 40;
    private static int receiptSequence = 1;
    private static final Random RANDOM = new Random();

    // Sample inventory catalog
    private static final TestItem[] CATALOG = {
        new TestItem("Coffee", new BigDecimal("100")),
        new TestItem("Pastel de Nata", new BigDecimal("150")),
        new TestItem("Toasted Sandwich", new BigDecimal("220")),
        new TestItem("Orange Juice", new BigDecimal("300")),
        new TestItem("Mineral Water", new BigDecimal("120")),
        new TestItem("Espresso Double", new BigDecimal("150")),
        new TestItem("Croissant", new BigDecimal("110")),
        new TestItem("Tea (Earl Grey)", new BigDecimal("140"))
    };

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("KriolOS - Ticket Preview Test");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(950, 750);
            frame.setLayout(new BorderLayout());

            // 1. Initialize printer panel component
            DevicePrinterPanel screenPrinter = new DevicePrinterPanel();
            frame.add(screenPrinter, BorderLayout.CENTER);

            // 2. Control Toolbar
            JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 6));

            JButton btnAddShortTicket = new JButton("Generate Ticket (Short)");
            JButton btnAddRandomTicket = new JButton("Generate Ticket (Random)");
            JButton btnZoomIn = new JButton("Zoom In (+)");
            JButton btnZoomOut = new JButton("Zoom Out (-)");
            JButton btnZoomReset = new JButton("100%");
            JButton btnPrint = new JButton("Print Layout");
            JButton btnClear = new JButton("Clear All");

            toolBar.add(btnAddShortTicket);
            toolBar.add(btnAddRandomTicket);
            toolBar.add(btnZoomIn);
            toolBar.add(btnZoomOut);
            toolBar.add(btnZoomReset);
            toolBar.add(btnPrint);
            toolBar.add(btnClear);
            frame.add(toolBar, BorderLayout.NORTH);

            // 3. Toolbar actions
            btnZoomIn.addActionListener(e -> screenPrinter.zoomIn());
            btnZoomOut.addActionListener(e -> screenPrinter.zoomOut());
            btnZoomReset.addActionListener(e -> screenPrinter.setZoom(1.0));
            btnPrint.addActionListener(e -> screenPrinter.printTickets());
            btnClear.addActionListener(e -> screenPrinter.clearAllTickets());
            /*
            // 4. Generate standard short ticket
            btnAddShortTicket.addActionListener(e -> {
                List<OrderItem> items = new ArrayList<>();
                items.add(new OrderItem("Coffee", 1, new BigDecimal("2.50")));
                items.add(new OrderItem("Pastel de Nata", 1, new BigDecimal("1.50")));
                buildReceipt(screenPrinter, items);
            });
            */

            /*
            // 4. Generate standard short ticket using items from CATALOG
            btnAddShortTicket.addActionListener(e -> {
                List<OrderItem> items = new ArrayList<>();
                // Select the first two items directly from CATALOG
                items.add(new OrderItem(CATALOG[0].name, 1, CATALOG[0].price));
                items.add(new OrderItem(CATALOG[1].name, 1, CATALOG[1].price));
                buildReceipt(screenPrinter, items);
            });
            */

            // 4. Generate short ticket with 2 random items from CATALOG
            btnAddShortTicket.addActionListener(e -> {
                List<OrderItem> items = new ArrayList<>();
                for (int i = 0; i < 2; i++) {
                    TestItem catalogItem = CATALOG[RANDOM.nextInt(CATALOG.length)];
                    int qty = 1 + RANDOM.nextInt(2);
                    items.add(new OrderItem(catalogItem.name, qty, catalogItem.price));
                }
                buildReceipt(screenPrinter, items);
            });

            // 5. Generate dynamic ticket with 3 to 12 items to test vertical wrap/scroll
            btnAddRandomTicket.addActionListener(e -> {
                List<OrderItem> items = new ArrayList<>();
                int itemCount = 3 + RANDOM.nextInt(10);
                for (int i = 0; i < itemCount; i++) {
                    TestItem catalogItem = CATALOG[RANDOM.nextInt(CATALOG.length)];
                    int qty = 1 + RANDOM.nextInt(3);
                    items.add(new OrderItem(catalogItem.name, qty, catalogItem.price));
                }
                buildReceipt(screenPrinter, items);
            });

            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    /**
     * Builds and appends a formatted receipt calculated with high monetary
     * precision.
     */
    private static void buildReceipt(DevicePrinterPanel printer, List<OrderItem> items) {
        printer.beginReceipt();

        printLine(printer, "");
        printLine(printer, "KriolOS POINT OF SALE");
        printLine(printer, "Receipt #" + (receiptSequence++));
        printLine(printer, repeat("-", RECEIPT_LINE_WIDTH));

        BigDecimal lineSum = roundCurrency(BigDecimal.ZERO);
        int totalItems = 0;

        for (OrderItem item : items) {
            BigDecimal qtyBd = BigDecimal.valueOf(item.qty);
            BigDecimal totalLinePrice = roundCurrency(item.unitPrice.multiply(qtyBd));

            lineSum = lineSum.add(totalLinePrice);
            totalItems += item.qty;

            String leftPart = item.qty + "x " + item.name;
            String rightPart = formatCurrency(totalLinePrice);
            printLine(printer, padLine(leftPart, rightPart, RECEIPT_LINE_WIDTH));
        }

        printLine(printer, repeat("-", RECEIPT_LINE_WIDTH));
        printLine(printer, padLine("Items count: " + totalItems, "", RECEIPT_LINE_WIDTH));

        BigDecimal subtotal;
        BigDecimal tax;
        BigDecimal grandTotal;

        if (TAX_DISPLAY_MODE == TaxDisplayMode.TAX_INCLUSIVE) {
            // Prices already include tax: Grand total equals the sum of lines
            grandTotal = roundCurrency(lineSum);

            // Factor: 1 + TaxRate (e.g., 1.15)
            BigDecimal taxFactor = BigDecimal.ONE.add(TAX_RATE);

            // Subtotal = Grand Total / (1 + TaxRate)
            subtotal = grandTotal.divide(taxFactor, CURRENCY_DECIMALS, FINANCIAL_ROUNDING);

            // Tax = Grand Total - Subtotal
            tax = roundCurrency(grandTotal.subtract(subtotal));

            printLine(printer, padLine("Subtotal (Net)", formatCurrency(subtotal), RECEIPT_LINE_WIDTH));
            printLine(printer, padLine("Included Tax (15%)", formatCurrency(tax), RECEIPT_LINE_WIDTH));
            printLine(printer, padLine("TOTAL:", formatCurrency(grandTotal), RECEIPT_LINE_WIDTH));
        } else {
            // Prices exclude tax: Tax is added on top
            subtotal = roundCurrency(lineSum);
            tax = roundCurrency(subtotal.multiply(TAX_RATE));
            grandTotal = roundCurrency(subtotal.add(tax));

            printLine(printer, padLine("Subtotal", formatCurrency(subtotal), RECEIPT_LINE_WIDTH));
            printLine(printer, padLine("Tax (15%)", formatCurrency(tax), RECEIPT_LINE_WIDTH));
            printLine(printer, padLine("TOTAL:", formatCurrency(grandTotal), RECEIPT_LINE_WIDTH));
        }

        // Calculate realistic customer payment and change
        BigDecimal cashTendered = calculateTenderedCash(grandTotal);
        BigDecimal change = roundCurrency(cashTendered.subtract(grandTotal));

        printLine(printer, "");
        printLine(printer, padLine("Cash Tendered:", formatCurrency(cashTendered), RECEIPT_LINE_WIDTH));

        // Display change if customer provided more than total
        if (change.compareTo(BigDecimal.ZERO) > 0) {
            printLine(printer, padLine("Change:", formatCurrency(change), RECEIPT_LINE_WIDTH));
        }

        printLine(printer, repeat("-", RECEIPT_LINE_WIDTH));

        // Dispatch completed receipt to container
        printer.endReceipt();
    }

    /**
     * Applies the configured global decimal scale and rounding mode to any
     * monetary value.
     */
    public static BigDecimal roundCurrency(BigDecimal amount) {
        if (amount == null) {
            return BigDecimal.ZERO.setScale(CURRENCY_DECIMALS, FINANCIAL_ROUNDING);
        }
        return amount.setScale(CURRENCY_DECIMALS, FINANCIAL_ROUNDING);
    }

    /**
     * Formats monetary values based on decimal scale, symbol, and prefix/suffix
     * placement.
     */
    public static String formatCurrency(BigDecimal amount) {
        BigDecimal rounded = roundCurrency(amount);

        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        DecimalFormat df = new DecimalFormat();
        df.setDecimalFormatSymbols(symbols);
        df.setGroupingUsed(true);
        df.setMinimumFractionDigits(CURRENCY_DECIMALS);
        df.setMaximumFractionDigits(CURRENCY_DECIMALS);

        String numberStr = df.format(rounded);
        String space = CURRENCY_USE_SPACE ? " " : "";

        if (CURRENCY_SYMBOL_POSITION == SymbolPosition.PREFIX) {
            return CURRENCY_SYMBOL + space + numberStr;
        } else {
            return numberStr + space + CURRENCY_SYMBOL;
        }
    }

    /**
     * Determines a realistic cash bill handed over by the customer according to
     * the currency scale.
     */
    private static BigDecimal calculateTenderedCash(BigDecimal grandTotal) {
        BigDecimal[] commonBills = {
            roundCurrency(new BigDecimal("5")),
            roundCurrency(new BigDecimal("10")),
            roundCurrency(new BigDecimal("20")),
            roundCurrency(new BigDecimal("50")),
            roundCurrency(new BigDecimal("100")),
            roundCurrency(new BigDecimal("500")),
            roundCurrency(new BigDecimal("1000"))
        };

        for (BigDecimal bill : commonBills) {
            if (bill.compareTo(grandTotal) >= 0) {
                return bill;
            }
        }

        // Fallback: round up to next 10-unit increment
        BigDecimal step = new BigDecimal("10");
        BigDecimal divided = grandTotal.divide(step, 0, RoundingMode.CEILING);
        return roundCurrency(divided.multiply(step));
    }

    private static void printLine(DevicePrinterPanel printer, String text) {
        printer.beginLine(0);
        printer.printText(0, text);
        printer.endLine();
    }

    private static String padLine(String left, String right, int width) {
        int spaces = width - left.length() - right.length();
        if (spaces <= 0) {
            return left + " " + right;
        }
        return left + repeat(" ", spaces) + right;
    }

    private static String repeat(String str, int count) {
        StringBuilder sb = new StringBuilder(count * str.length());
        for (int i = 0; i < count; i++) {
            sb.append(str);
        }
        return sb.toString();
    }

    private static class TestItem {

        final String name;
        final BigDecimal price;

        TestItem(String name, BigDecimal price) {
            this.name = name;
            this.price = price;
        }
    }

    private static class OrderItem {

        final String name;
        final int qty;
        final BigDecimal unitPrice;

        OrderItem(String name, int qty, BigDecimal unitPrice) {
            this.name = name;
            this.qty = qty;
            this.unitPrice = unitPrice;
        }
    }
}
