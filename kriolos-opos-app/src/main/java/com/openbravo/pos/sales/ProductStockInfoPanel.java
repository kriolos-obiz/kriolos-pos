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

import com.openbravo.data.gui.modal.PosUIModal;
import com.openbravo.format.Formats;
import com.openbravo.pos.forms.AppLocal;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Date;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;

/**
 * LAF-agnostic modal panel displaying comprehensive product inventory and stock details
 * using a structured record without HTML string concatenation.
 */
public class ProductStockInfoPanel extends JPanel {

    private final ProductStockDetails details;
    private PosUIModal modalContext;

    public ProductStockInfoPanel(ProductStockDetails details) {
        this.details = details != null ? details : ProductStockDetails.of(0.0, 0.0, 0.0, null);
        initUI();
    }

    public ProductStockInfoPanel(Double units, Double max, Double min, Date memoDate) {
        this(ProductStockDetails.of(units, max, min, memoDate));
    }

    public ProductStockInfoPanel(Double units, Double max, Double min, String memoDate) {
        this(new ProductStockDetails(null, null, null, null, null, units, min, max, null, null));
    }

    public ProductStockInfoPanel(String productName, String categoryName, Double units, Double max, Double min, Date memoDate) {
        this(ProductStockDetails.of(productName, categoryName, units, max, min, memoDate));
    }

    public ProductStockInfoPanel(String productName, String categoryName, Double units, Double max, Double min, String memoDate) {
        this(new ProductStockDetails(productName, categoryName, null, null, null, units, min, max, null, null));
    }

    private void initUI() {
        setName("kriolos:sales:product_stock_info");
        setLayout(new BorderLayout(16, 16));
        setBorder(BorderFactory.createEmptyBorder(18, 22, 16, 22));

        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setName("kriolos:sales:product_stock_content");

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 8, 5, 8);
        gbc.anchor = GridBagConstraints.LINE_START;

        int row = 0;

        // Section 1: Product Identification & Pricing
        boolean hasProductInfo = false;
        if (hasValue(details.productName())) {
            addRow(contentPanel, gbc, row++, AppLocal.getIntString("label.prodname"), details.productName(), true);
            hasProductInfo = true;
        }

        if (hasValue(details.categoryName())) {
            addRow(contentPanel, gbc, row++, AppLocal.getIntString("label.prodcategory"), details.categoryName(), false);
            hasProductInfo = true;
        }

        if (hasValue(details.reference())) {
            addRow(contentPanel, gbc, row++, AppLocal.getIntString("label.prodref"), details.reference(), false);
            hasProductInfo = true;
        }

        if (hasValue(details.barcode())) {
            addRow(contentPanel, gbc, row++, AppLocal.getIntString("label.prodbarcode"), details.barcode(), false);
            hasProductInfo = true;
        }

        if (details.priceSell() != null) {
            addRow(contentPanel, gbc, row++, AppLocal.getIntString("label.price"), Formats.CURRENCY.formatValue(details.priceSell()), false);
            hasProductInfo = true;
        }

        if (hasProductInfo) {
            addSeparator(contentPanel, gbc, row++);
        }

        // Section 2: Location & Stock Metrics
        if (hasValue(details.locationName())) {
            addRow(contentPanel, gbc, row++, AppLocal.getIntString("label.location"), details.locationName(), false);
        }

        // Available Units
        addRow(contentPanel, gbc, row++, AppLocal.getIntString("label.currentstock"), String.valueOf(details.units()), true);

        // Minimum Level
        addRow(contentPanel, gbc, row++, AppLocal.getIntString("label.minimum"), String.valueOf(details.minimum()), false);

        // Maximum Level
        addRow(contentPanel, gbc, row++, AppLocal.getIntString("label.maximum"), String.valueOf(details.maximum()), false);

        // Inventory Date
        String dateStr = details.memoDate() != null ? Formats.DATE.formatValue(details.memoDate()) : "-";
        addRow(contentPanel, gbc, row++, AppLocal.getIntString("label.proddate"), dateStr, false);

        // Section 3: Stock Status Badge / Summary
        addSeparator(contentPanel, gbc, row++);
        String statusText;
        if (details.units() <= 0) {
            statusText = AppLocal.getIntString("label.nostock");
            if (statusText == null || statusText.startsWith("label.")) {
                statusText = "0 Units (Out of Stock)";
            }
        } else if (details.minimum() > 0 && details.units() <= details.minimum()) {
            statusText = AppLocal.getIntString("label.lowstock");
            if (statusText == null || statusText.startsWith("label.")) {
                statusText = details.units() + " Units (Low Stock)";
            }
        } else {
            statusText = AppLocal.getIntString("label.stockunits") != null
                    ? AppLocal.getIntString("label.stockunits")
                    : "In Stock";
        }
        addRow(contentPanel, gbc, row++, AppLocal.getIntString("label.status") != null && !AppLocal.getIntString("label.status").startsWith("label.")
                ? AppLocal.getIntString("label.status") : "Status", statusText, true);

        add(contentPanel, BorderLayout.CENTER);

        // Action Buttons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        JButton btnOk = new JButton(AppLocal.getIntString("button.ok"));
        btnOk.setName("kriolos:sales:product_stock_ok");
        btnOk.addActionListener(e -> {
            if (modalContext != null) {
                modalContext.close();
            }
        });
        actionPanel.add(btnOk);
        add(actionPanel, BorderLayout.SOUTH);
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int row, String title, String value, boolean highlightValue) {
        gbc.gridx = 0;
        gbc.gridy = row;
        JLabel lblTitle = new JLabel(title + ":");
        lblTitle.setFont(lblTitle.getFont().deriveFont(Font.BOLD));
        panel.add(lblTitle, gbc);

        gbc.gridx = 1;
        JLabel lblValue = new JLabel(value);
        if (highlightValue) {
            lblValue.setFont(lblValue.getFont().deriveFont(Font.BOLD));
        }
        panel.add(lblValue, gbc);
    }

    private void addSeparator(JPanel panel, GridBagConstraints gbc, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 4, 8, 4);
        panel.add(new JSeparator(), gbc);

        // Reset constraints
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(5, 8, 5, 8);
    }

    private boolean hasValue(String text) {
        return text != null && !text.isBlank();
    }

    public ProductStockDetails getDetails() {
        return details;
    }

    public void setModalContext(PosUIModal modalContext) {
        this.modalContext = modalContext;
    }

    public static void show(Component parent, ProductStockDetails details) {
        ProductStockInfoPanel panel = new ProductStockInfoPanel(details);
        PosUIModal modal = PosUIModal.create(parent, panel)
                .setTitle(AppLocal.getIntString("message.title.checkstock"))
                .setModal(true)
                .setResizable(false);
        panel.setModalContext(modal);
        modal.show();
    }

    public static void show(Component parent, Double units, Double max, Double min, Date memoDate) {
        show(parent, ProductStockDetails.of(units, max, min, memoDate));
    }

    public static void show(Component parent, Double units, Double max, Double min, String memoDate) {
        show(parent, new ProductStockDetails(null, null, null, null, null, units, min, max, null, null));
    }

    public static void show(Component parent, String productName, String categoryName, Double units, Double max, Double min, Date memoDate) {
        show(parent, ProductStockDetails.of(productName, categoryName, units, max, min, memoDate));
    }

    public static void show(Component parent, String productName, String categoryName, Double units, Double max, Double min, String memoDate) {
        show(parent, new ProductStockDetails(productName, categoryName, null, null, null, units, min, max, null, null));
    }
}
