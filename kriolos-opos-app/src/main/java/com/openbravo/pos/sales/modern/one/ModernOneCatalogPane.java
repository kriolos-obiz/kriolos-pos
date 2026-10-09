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
package com.openbravo.pos.sales.modern.one;

import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.sales.modern.WrapLayout;
import com.openbravo.pos.pim.CategoryInfo;
import com.openbravo.pos.ticket.ProductInfoExt;
import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Touch-optimized modern Catalog Pane featuring a fluid product card grid and category filter bar.
 * Built with {@link WrapLayout} to adapt seamlessly to varying screen widths and resolutions.
 *
 * @author KriolOS Team
 */
public class ModernOneCatalogPane extends JPanel {

    private static final long serialVersionUID = 1L;

    private final ModernOneCategoryBar categoryBar;
    private final JTextField searchField;
    private final JPanel gridContainer;
    private final JScrollPane gridScrollPane;

    private final List<ProductInfoExt> allProducts = new ArrayList<>();
    private final Consumer<ProductInfoExt> onProductSelected;
    private CategoryInfo currentCategory = null;
    private String currentSearchFilter = "";

    public ModernOneCatalogPane(Consumer<ProductInfoExt> onProductSelected) {
        this.onProductSelected = onProductSelected;

        setLayout(new BorderLayout(0, 18));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // Top Control Area: Search Bar + Category Bar (with proper spacing)
        JPanel topArea = new JPanel(new BorderLayout(0, 12));
        topArea.setOpaque(false);
        topArea.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));

        searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(0, 44));
        searchField.setFont(searchField.getFont().deriveFont(14f));
        searchField.setMargin(new Insets(4, 12, 4, 12));
        searchField.setName("kriolos:sales:modern:catalog-search");

        // Modern FlatLaf text field properties (gracefully degrades)
        searchField.putClientProperty("JTextField.placeholderText", AppLocal.getIntString("label.searchproductbarcode"));
        searchField.putClientProperty("JTextField.showClearButton", true);
        searchField.putClientProperty("JComponent.roundRect", true);

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { filter(); }
            @Override
            public void removeUpdate(DocumentEvent e) { filter(); }
            @Override
            public void changedUpdate(DocumentEvent e) { filter(); }

            private void filter() {
                currentSearchFilter = searchField.getText().trim().toLowerCase(Locale.ROOT);
                renderGrid();
            }
        });

        categoryBar = new ModernOneCategoryBar(category -> {
            this.currentCategory = category;
            renderGrid();
        });

        topArea.add(searchField, BorderLayout.PAGE_START);
        topArea.add(categoryBar, BorderLayout.CENTER);
        add(topArea, BorderLayout.PAGE_START);

        // Center Product Grid (WrapLayout with clean 12px gutters)
        gridContainer = new JPanel(new WrapLayout(FlowLayout.LEADING, 12, 12));
        gridContainer.setOpaque(false);

        gridScrollPane = new JScrollPane(gridContainer);
        gridScrollPane.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
        gridScrollPane.setOpaque(false);
        gridScrollPane.getViewport().setOpaque(false);
        gridScrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(10, 0));
        gridScrollPane.getVerticalScrollBar().setUnitIncrement(24);

        add(gridScrollPane, BorderLayout.CENTER);

        // Apply RTL/LTR orientation
        applyComponentOrientation(ComponentOrientation.getOrientation(Locale.getDefault()));
        gridContainer.applyComponentOrientation(ComponentOrientation.getOrientation(Locale.getDefault()));
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

    private void renderGrid() {
        gridContainer.removeAll();

        List<ProductInfoExt> filtered = allProducts.stream()
                .filter(p -> {
                    // Category filter
                    if (currentCategory != null && currentCategory.getID() != null) {
                        if (!currentCategory.getID().equals(p.getCategoryID())) {
                            return false;
                        }
                    }
                    // Text search filter
                    if (!currentSearchFilter.isEmpty()) {
                        String name = p.getName() != null ? p.getName().toLowerCase(Locale.ROOT) : "";
                        String ref = p.getReference() != null ? p.getReference().toLowerCase(Locale.ROOT) : "";
                        String code = p.getCode() != null ? p.getCode().toLowerCase(Locale.ROOT) : "";
                        return name.contains(currentSearchFilter) || ref.contains(currentSearchFilter) || code.contains(currentSearchFilter);
                    }
                    return true;
                })
                .collect(Collectors.toList());

        for (ProductInfoExt prod : filtered) {
            ModernOneProductCard card = new ModernOneProductCard(prod, e -> {
                if (onProductSelected != null) {
                    onProductSelected.accept(prod);
                }
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
}
