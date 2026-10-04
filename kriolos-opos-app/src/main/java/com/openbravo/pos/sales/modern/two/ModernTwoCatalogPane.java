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
package com.openbravo.pos.sales.modern.two;

import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.pim.CategoryInfo;
import com.openbravo.pos.sales.modern.WrapLayout;
import com.openbravo.pos.ticket.ProductInfoExt;
import com.openbravo.pos.ui.components.ButtonSize;
import com.openbravo.pos.ui.components.POSButtonFactory;
import com.openbravo.pos.ui.components.UnicodeIcon;

import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Touch-optimized Catalog Pane for {@link ModernTwoSalesLayout}.
 * Positioned on the {@code LINE_END} side of the screen.
 * <p>
 * Features:
 * <ul>
 * <li>Prominent Global Search & Barcode scanner field at
 * {@code PAGE_START}.</li>
 * <li>Horizontal touch-friendly Category filter bar.</li>
 * <li>Responsive fluid product card grid utilizing {@link WrapLayout}.</li>
 * </ul>
 *
 * @author KriolOS Team
 */
public class ModernTwoCatalogPane extends JPanel {

    private static final long serialVersionUID = 1L;

    private final ModernTwoCategoryBar categoryBar;
    private final JTextField searchField;
    private final JPanel gridContainer;
    private final JScrollPane gridScrollPane;

    private final List<ProductInfoExt> allProducts = new ArrayList<>();
    private final Consumer<ProductInfoExt> onProductSelected;
    private Consumer<String> onBarcodeScanned;
    private CategoryInfo currentCategory = null;
    private String currentSearchFilter = "";

    public ModernTwoCatalogPane(Consumer<ProductInfoExt> onProductSelected) {
        this.onProductSelected = onProductSelected;

        setLayout(new BorderLayout(0, 12));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ----------------------------------------------------
        // TOP CONTROL AREA: Search / Barcode Bar + Category Bar
        // ----------------------------------------------------
        JPanel topArea = new JPanel(new BorderLayout(0, 10));
        topArea.setOpaque(false);
        topArea.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));

        JPanel searchContainer = new JPanel(new BorderLayout(8, 0));
        searchContainer.setOpaque(false);
        searchContainer.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));

        JLabel lblIcon = new JLabel(UnicodeIcon.SEARCH.getCode());
        lblIcon.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblIcon.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));

        searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(0, 42));
        searchField.setFont(searchField.getFont().deriveFont(14f));
        searchField.setMargin(new Insets(4, 10, 4, 10));
        searchField.setName("kriolos:sales:modern-two:search");
        searchField.putClientProperty("JTextField.placeholderText",
                "Pesquisar produto ou ler c\u00f3digo de barras...");
        searchField.putClientProperty("JTextField.showClearButton", true);
        searchField.putClientProperty("JComponent.roundRect", true);

        JButton btnClear = POSButtonFactory.createActionButton(
                AppLocal.getIntString("button.clean"),
                UnicodeIcon.CLEAR,
                ButtonSize.LARGE,
                0,
                AppLocal.getIntString("button.clear.tooltip"),
                e -> {
                    searchField.setText("");
                    searchField.requestFocus();
                }
        );
        btnClear.setPreferredSize(new Dimension(100, 42));

        searchContainer.add(lblIcon, BorderLayout.LINE_START);
        searchContainer.add(searchField, BorderLayout.CENTER);
        searchContainer.add(btnClear, BorderLayout.LINE_END);

        // Filter on text input
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filter();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filter();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filter();
            }

            private void filter() {
                currentSearchFilter = searchField.getText().trim().toLowerCase(Locale.ROOT);
                renderGrid();
            }
        });

        // Enter key for barcode scanner input or exact match
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    String code = searchField.getText().trim();
                    if (!code.isEmpty()) {
                        // Check if an exact product code/barcode matches
                        ProductInfoExt match = findExactMatch(code);
                        if (match != null && onProductSelected != null) {
                            onProductSelected.accept(match);
                            searchField.setText("");
                        } else if (onBarcodeScanned != null) {
                            onBarcodeScanned.accept(code);
                            searchField.setText("");
                        }
                    }
                }
            }
        });

        categoryBar = new ModernTwoCategoryBar(cat -> {
            this.currentCategory = cat;
            renderGrid();
        });

        topArea.add(searchContainer, BorderLayout.PAGE_START);
        topArea.add(categoryBar, BorderLayout.CENTER);
        add(topArea, BorderLayout.PAGE_START);

        // ----------------------------------------------------
        // CENTER: Responsive WrapLayout Product Grid
        // ----------------------------------------------------
        gridContainer = new JPanel(new WrapLayout(FlowLayout.LEADING, 10, 10));
        gridContainer.setOpaque(false);

        gridScrollPane = new JScrollPane(gridContainer);
        gridScrollPane.setBorder(BorderFactory.createEmptyBorder());
        gridScrollPane.setOpaque(false);
        gridScrollPane.getViewport().setOpaque(false);
        gridScrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
        gridScrollPane.getVerticalScrollBar().setUnitIncrement(24);

        add(gridScrollPane, BorderLayout.CENTER);

        applyComponentOrientation(ComponentOrientation.getOrientation(Locale.getDefault()));
        gridContainer.applyComponentOrientation(ComponentOrientation.getOrientation(Locale.getDefault()));
    }

    public void setOnBarcodeScanned(Consumer<String> onBarcodeScanned) {
        this.onBarcodeScanned = onBarcodeScanned;
    }

    public void setCategories(List<CategoryInfo> categories) {
        categoryBar.setCategories(categories);
    }

    public void setProducts(List<ProductInfoExt> products) {
        allProducts.clear();
        if (products != null) {
            allProducts.addAll(products);
        }
        renderGrid();
    }

    private ProductInfoExt findExactMatch(String code) {
        for (ProductInfoExt p : allProducts) {
            if (code.equalsIgnoreCase(p.getCode()) || code.equalsIgnoreCase(p.getReference())) {
                return p;
            }
        }
        return null;
    }

    private void renderGrid() {
        gridContainer.removeAll();

        List<ProductInfoExt> filtered = allProducts.stream()
                .filter(p -> {
                    if (currentCategory != null && currentCategory.getID() != null) {
                        if (!currentCategory.getID().equals(p.getCategoryID())) {
                            return false;
                        }
                    }
                    if (!currentSearchFilter.isEmpty()) {
                        String name = p.getName() != null ? p.getName().toLowerCase(Locale.ROOT) : "";
                        String ref = p.getReference() != null ? p.getReference().toLowerCase(Locale.ROOT) : "";
                        String code = p.getCode() != null ? p.getCode().toLowerCase(Locale.ROOT) : "";
                        return name.contains(currentSearchFilter) || ref.contains(currentSearchFilter)
                                || code.contains(currentSearchFilter);
                    }
                    return true;
                })
                .collect(Collectors.toList());

        for (ProductInfoExt prod : filtered) {
            ModernTwoProductCard card = new ModernTwoProductCard(prod, e -> {
                if (onProductSelected != null) {
                    onProductSelected.accept(prod);
                }
                focusSearch();
            });
            gridContainer.add(card);
        }

        gridContainer.revalidate();
        gridContainer.repaint();
    }

    public void focusSearch() {
        searchField.requestFocusInWindow();
    }

    public void clearSearch() {
        searchField.setText("");
    }

    public void selectCategory(String categoryId) {
        categoryBar.selectCategory(categoryId);
    }
}
