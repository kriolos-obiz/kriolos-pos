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
package com.openbravo.pos.catalog;

import com.openbravo.basic.BasicException;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.DataLogicSales;
import com.openbravo.pos.pim.CategoryInfo;
import com.openbravo.pos.pim.DataLogicPIM;
import com.openbravo.pos.sales.TaxService;
import com.openbravo.pos.ticket.ProductInfoExt;
import com.openbravo.pos.ticket.TaxInfo;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.*;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 *
 * @author adrianromero
 */
public class JCatalog extends JPanel implements ListSelectionListener, CatalogSelector {

    private static final int DEFAULT_CATALOG_PANEL_HEIGHT = 245;

    protected EventListenerList listeners = new EventListenerList();

    private boolean pricevisible;
    private boolean taxesincluded;

    // Set of Products panels
    private final Map<String, ProductInfoExt> productsCached = new HashMap<>();
    private final Set<String> categoriesCached = new HashSet<>();

    private CategoryInfo showingcategory = null;
    private CatalogController controller;

    public JCatalog(AppView app) {
        this(app, true, true);
    }

    private JCatalog(AppView app, boolean pricevisible, boolean taxesincluded) {

        this.pricevisible = pricevisible;
        this.taxesincluded = taxesincluded;

        initComponents();
        setPreferredSize(new Dimension(0, DEFAULT_CATALOG_PANEL_HEIGHT));

        categoriesJList.addListSelectionListener(this);
        categoriesScrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(35, 35));
        
        
        TaxService taxService = app.getBean(TaxService.class);
        CatalogService catalogService = app.getBean(CatalogService.class);

        controller = new CatalogController(taxService, catalogService);
    }

    @Override
    public Component getComponent() {
        return this;
    }

    @Override
    public void showCatalogPanel(String id) {

        if (id == null) {
            showRootCategoriesPanel();
        } else {
            showProductPanel(id);
        }
    }

    @Override
    public void loadCatalog() throws BasicException {

        // delete all categories panel
        productsGridPane.removeAll();

        productsCached.clear();
        categoriesCached.clear();

        showingcategory = null;

        // Load all categories.
        List<CategoryInfo> categories = controller.getRootCategories();

        // Select the first category
        categoriesJList.setCellRenderer(new SmallCategoryRenderer());
        categoriesJList.setModel(new CategoriesListModel(categories));

        if (categoriesJList.getModel().getSize() == 0) {
            categoriesScrollPane.setVisible(false);
            categoriesVerticalSeparatorPane.setVisible(false);
        } else {
            categoriesScrollPane.setVisible(true);
            categoriesVerticalSeparatorPane.setVisible(true);
            categoriesJList.setSelectedIndex(0);
        }

        showRootCategoriesPanel();
    }

    @Override
    public void setComponentEnabled(boolean value) {

        categoriesJList.setEnabled(value);
        categoriesScrollPane.setEnabled(value);
        subCatlIndicatorJLabel.setEnabled(value);
        subCatBackJButton.setEnabled(value);
        productsGridPane.setEnabled(value);

        synchronized (productsGridPane.getTreeLock()) {
            int compCount = productsGridPane.getComponentCount();
            for (int i = 0; i < compCount; i++) {
                productsGridPane.getComponent(i).setEnabled(value);
            }
        }

        this.setEnabled(value);
    }

    @Override
    public void addActionListener(ActionListener l) {
        listeners.add(ActionListener.class, l);
    }

    @Override
    public void removeActionListener(ActionListener l) {
        listeners.remove(ActionListener.class, l);
    }

    @Override
    public void valueChanged(ListSelectionEvent evt) {

        if (!evt.getValueIsAdjusting()) {
            int i = categoriesJList.getSelectedIndex();
            if (i >= 0) {
                Rectangle oRect = categoriesJList.getCellBounds(i, i);
                categoriesJList.scrollRectToVisible(oRect);
            }
        }
    }

    protected void fireSelectedProduct(ProductInfoExt prod) {
        EventListener[] l = listeners.getListeners(ActionListener.class);
        ActionEvent e = null;
        for (EventListener l1 : l) {
            if (e == null) {
                e = new ActionEvent(prod, ActionEvent.ACTION_PERFORMED, prod.getID());
            }
            ((ActionListener) l1).actionPerformed(e);
        }
    }

    private void showProductGridByCat(String categoryId) {
        if (!categoriesCached.contains(categoryId)) {

            JCatalogTab catalogTab = new JCatalogTab();
            catalogTab.applyComponentOrientation(getComponentOrientation());
            productsGridPane.add(catalogTab, categoryId);
            categoriesCached.add(categoryId);

            // 1 - SHOW ALL PRODUCT (SET AS CONSTANT) IN CATEGORIY
            List<ProductInfoExt> prods = controller.getProductConstant();
            for (ProductInfoExt prod : prods) {

                Image imageIcon = controller.getThumbNailOrDefault(prod.getImage());

                String tooltip = (prod.getTextTip() != null && !prod.getTextTip().isBlank()) ? prod.getTextTip() : null;

                catalogTab.addButton(
                        new ImageIcon(imageIcon),
                        new SelectProductListener(prod),
                        getProductLabel(prod, true),
                        tooltip);

            }

            // 2 - SHOW ALL SUB-CATEGORIES
            List<CategoryInfo> categories = controller.getSubcategories(categoryId);
            for (CategoryInfo cat : categories) {
                String catName = "";
                if (cat.getCatShowName()) {
                    catName = cat.getName();
                }

                Image imageIcons = controller.getThumbNailOrDefaultSubCat(cat.getImage());

                catalogTab.addButton(
                        new ImageIcon(imageIcons),
                        new SelectCategoryListener(cat),
                        catName,
                        null);
            }

            // 3 - SHOW ALL PRODUCT IN SELECTED CATEGORIY
            List<ProductInfoExt> products = controller.findProductByCategory(categoryId);
            for (ProductInfoExt prod : products) {
                catalogTab.addCatalogItem(buildCatalogItem(prod), new SelectProductListener(prod));
            }
        }
        CardLayout cl = (CardLayout) (productsGridPane.getLayout());
        cl.show(productsGridPane, categoryId);
    }

    private CatalogItem buildCatalogItem(ProductInfoExt product) {

        String produtTitle;

        if (product.getDisplay() != null && !product.getDisplay().isBlank()) {
            produtTitle = product.getDisplay();
        } else {
            produtTitle = product.getName();
        }

        CatalogItem item = new CatalogItem(produtTitle);
        
        String tooltip = (product.getTextTip() != null && !product.getTextTip().isBlank()) ? product.getTextTip() : null;
        item.setTextTip(tooltip);

        if (product.getImage() != null) {
            Image imageIcon = controller.getThumbNailOrDefault(product.getImage());

            if (imageIcon != null) {
                item.setImage(imageIcon);
            }
        }else {
             item.setImage(null);
        }

        if (pricevisible) {
            String produtprice;
            if (taxesincluded) {
                TaxInfo tax = controller.getTaxInfo(product.getTaxCategoryID());
                produtprice = product.printPriceSellTax(tax);

            } else {
                produtprice = product.printPriceSell();
            }

            item.setPrice(produtprice);
        } else {
            item.setPrice(null);
        }

        return item;
    }

    private String getProductLabel(ProductInfoExt product) {
        return getProductLabel(product, false);
    }

    private String getProductLabel(ProductInfoExt product, boolean isHtml) {

        String productLabel = "";
        String produtTitle = "";
        String produtprice = "";

        if (!"".equals(product.getDisplay())) {
            produtTitle = product.getDisplay();
        } else {
            produtTitle = product.getName();
        }

        if (pricevisible) {
            if (taxesincluded) {
                TaxInfo tax = controller.getTaxInfo(product.getTaxCategoryID());
                produtprice = product.printPriceSellTax(tax);

            } else {
                produtprice = product.printPriceSell();
            }

            if (isHtml) {
                productLabel = "<html><center>"
                        + produtTitle
                        + "<br>"
                        + produtprice
                        + "</center></html>";
            } else {
                productLabel = produtprice;
            }

        } else {
            productLabel = produtTitle;
        }

        return productLabel;
    }

    private void setSubCatlIndicator(Icon icon, String label, String texttip) {

        subCatlIndicatorJLabel.setText(label);
        subCatlIndicatorJLabel.setIcon(icon);

        // Show subcategories panel
        CardLayout cl = (CardLayout) (categoriesPane.getLayout());
        cl.show(categoriesPane, "subcategories");
    }

    private void selectRootCategories() {
        // Show root categories panel
        CardLayout cl = (CardLayout) (categoriesPane.getLayout());
        cl.show(categoriesPane, "rootcategories");
    }

    private void showRootCategoriesPanel() {

        selectRootCategories();
        // Show selected root category
        CategoryInfo cat = (CategoryInfo) categoriesJList.getSelectedValue();

        if (cat != null) {
            showProductGridByCat(cat.getID());
        }
        showingcategory = null;
    }

    private void showSubcategoryPanel(CategoryInfo category) {

        // this is the new panel that displays when a sub catergory is selected mouse does not work here 
        Image imageIcons = controller.getThumbNailOrDefaultCat(category.getImage());

        setSubCatlIndicator(
                new ImageIcon(imageIcons),
                category.getName(),
                category.getTextTip());

        showProductGridByCat(category.getID());
        showingcategory = category;
    }

    private void showProductPanel(String id) {

        ProductInfoExt product = productsCached.get(id);

        if (product == null) {
            if (productsCached.containsKey(id)) {
                // It is an empty panel
                if (showingcategory == null) {
                    showRootCategoriesPanel();
                } else {
                    showSubcategoryPanel(showingcategory);
                }
            } else {
                // Create  products panel
                List<ProductInfoExt> products = controller.getProductCompanion(id);
                if (products.isEmpty()) {
                    productsCached.put(id, null);

                    if (showingcategory == null) {
                        showRootCategoriesPanel();
                    } else {
                        showSubcategoryPanel(showingcategory);
                    }
                } else {

                    product = controller.getProductInfo(id);
                    if (product != null) {
                        productsCached.put(id, product);

                        JCatalogTab jcurrTab = new JCatalogTab();
                        jcurrTab.applyComponentOrientation(getComponentOrientation());
                        productsGridPane.add(jcurrTab, "PRODUCT." + id);

                        // Add products
                        for (ProductInfoExt prod : products) {
                            jcurrTab.addButton(
                                    new ImageIcon(controller.getThumbNailOrDefault(prod.getImage())),
                                    new SelectProductListener(prod),
                                    getProductLabel(prod),
                                    prod.getTextTip()
                            );
                        }
                        setSubCatlIndicator(new ImageIcon(controller.getThumbNailOrDefault(product.getImage())),
                                product.getDisplay(), product.getTextTip());

                        CardLayout cl = (CardLayout) (productsGridPane.getLayout());
                        cl.show(productsGridPane, "PRODUCT." + id);
                    } else {
                        productsCached.put(id, null);
                        if (showingcategory == null) {
                            showRootCategoriesPanel();
                        } else {
                            showSubcategoryPanel(showingcategory);
                        }
                    }
                }
            }
        } else {
            setSubCatlIndicator(
                    new ImageIcon(controller.getThumbNailOrDefault(product.getImage())),
                    product.getName(), product.getTextTip());

            CardLayout cl = (CardLayout) (productsGridPane.getLayout());
            cl.show(productsGridPane, "PRODUCT." + id);
        }
    }

    private class SelectProductListener implements ActionListener {

        private final ProductInfoExt prod;

        public SelectProductListener(ProductInfoExt prod) {
            this.prod = prod;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            fireSelectedProduct(prod);
        }
    }

    private class SelectCategoryListener implements ActionListener {

        private final CategoryInfo category;

        public SelectCategoryListener(CategoryInfo category) {
            this.category = category;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            showSubcategoryPanel(category);
        }
    }

    private class CategoriesListModel extends AbstractListModel<CategoryInfo> {

        private final List<CategoryInfo> m_aCategories;

        public CategoriesListModel(List<CategoryInfo> aCategories) {
            m_aCategories = aCategories;
        }

        @Override
        public int getSize() {
            return m_aCategories.size();
        }

        @Override
        public CategoryInfo getElementAt(int i) {
            return m_aCategories.get(i);
        }
    }

    private class SmallCategoryRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {

            super.getListCellRendererComponent(list, null, index, isSelected, cellHasFocus);
            CategoryInfo cat = (CategoryInfo) value;
            setText(cat.getName());
            Image imageIcons = controller.getThumbNailOrDefaultCat(cat.getImage());
            setIcon(new ImageIcon(imageIcons));

            return this;
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        categoriesPane = new javax.swing.JPanel();
        rootCategoriesPane = new javax.swing.JPanel();
        categoriesScrollPane = new javax.swing.JScrollPane();
        categoriesJList = new javax.swing.JList();
        categoriesVerticalSeparatorPane = new javax.swing.JPanel();
        categoriesVSEndPane = new javax.swing.JPanel();
        subCategoriesPane = new javax.swing.JPanel();
        subCatLeftJPanel = new javax.swing.JPanel();
        subCatlIndicatorJLabel = new javax.swing.JLabel();
        subCatRigthJPanel = new javax.swing.JPanel();
        subCatActionsJPanel = new javax.swing.JPanel();
        subCatBackJButton = new javax.swing.JButton();
        productsGridPane = new javax.swing.JPanel();

        setLayout(new java.awt.BorderLayout());

        categoriesPane.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        categoriesPane.setMaximumSize(new java.awt.Dimension(275, 600));
        categoriesPane.setPreferredSize(new java.awt.Dimension(265, 0));
        categoriesPane.setLayout(new java.awt.CardLayout());

        rootCategoriesPane.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        rootCategoriesPane.setMinimumSize(new java.awt.Dimension(200, 100));
        rootCategoriesPane.setPreferredSize(new java.awt.Dimension(275, 130));
        rootCategoriesPane.setLayout(new java.awt.BorderLayout());

        categoriesScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        categoriesScrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        categoriesScrollPane.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        categoriesScrollPane.setPreferredSize(new java.awt.Dimension(265, 130));

        categoriesJList.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        categoriesJList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        categoriesJList.setFocusable(false);
        categoriesJList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                categoriesJListValueChanged(evt);
            }
        });
        categoriesScrollPane.setViewportView(categoriesJList);

        rootCategoriesPane.add(categoriesScrollPane, java.awt.BorderLayout.CENTER);

        categoriesVerticalSeparatorPane.setLayout(new java.awt.BorderLayout());

        categoriesVSEndPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 5, 0, 5));
        categoriesVSEndPane.setLayout(new java.awt.GridLayout(0, 1, 0, 5));
        categoriesVerticalSeparatorPane.add(categoriesVSEndPane, java.awt.BorderLayout.NORTH);

        rootCategoriesPane.add(categoriesVerticalSeparatorPane, java.awt.BorderLayout.LINE_END);

        categoriesPane.add(rootCategoriesPane, "rootcategories");

        subCategoriesPane.setLayout(new java.awt.BorderLayout());

        subCatLeftJPanel.setLayout(new java.awt.BorderLayout());

        subCatlIndicatorJLabel.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        subCatlIndicatorJLabel.setText("Sub Category Indicator");
        subCatLeftJPanel.add(subCatlIndicatorJLabel, java.awt.BorderLayout.NORTH);

        subCategoriesPane.add(subCatLeftJPanel, java.awt.BorderLayout.WEST);

        subCatRigthJPanel.setLayout(new java.awt.BorderLayout());

        subCatActionsJPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 5, 0, 5));
        subCatActionsJPanel.setLayout(new java.awt.GridLayout(0, 1, 0, 5));

        subCatBackJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/2uparrow.png"))); // NOI18N
        subCatBackJButton.setFocusPainted(false);
        subCatBackJButton.setFocusable(false);
        subCatBackJButton.setMargin(new java.awt.Insets(8, 14, 8, 14));
        subCatBackJButton.setPreferredSize(new java.awt.Dimension(60, 45));
        subCatBackJButton.setRequestFocusEnabled(false);
        subCatBackJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                subCatBackJButtonActionPerformed(evt);
            }
        });
        subCatActionsJPanel.add(subCatBackJButton);

        subCatRigthJPanel.add(subCatActionsJPanel, java.awt.BorderLayout.NORTH);

        subCategoriesPane.add(subCatRigthJPanel, java.awt.BorderLayout.LINE_END);

        categoriesPane.add(subCategoriesPane, "subcategories");

        add(categoriesPane, java.awt.BorderLayout.LINE_START);

        productsGridPane.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        productsGridPane.setLayout(new java.awt.CardLayout());
        add(productsGridPane, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void categoriesJListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_categoriesJListValueChanged

        if (!evt.getValueIsAdjusting()) {
            CategoryInfo cat = (CategoryInfo) categoriesJList.getSelectedValue();
            if (cat != null) {
                showProductGridByCat(cat.getID());
            }
        }

    }//GEN-LAST:event_categoriesJListValueChanged

    private void subCatBackJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_subCatBackJButtonActionPerformed

        showRootCategoriesPanel();

    }//GEN-LAST:event_subCatBackJButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JList categoriesJList;
    private javax.swing.JPanel categoriesPane;
    private javax.swing.JScrollPane categoriesScrollPane;
    private javax.swing.JPanel categoriesVSEndPane;
    private javax.swing.JPanel categoriesVerticalSeparatorPane;
    private javax.swing.JPanel productsGridPane;
    private javax.swing.JPanel rootCategoriesPane;
    private javax.swing.JPanel subCatActionsJPanel;
    private javax.swing.JButton subCatBackJButton;
    private javax.swing.JPanel subCatLeftJPanel;
    private javax.swing.JPanel subCatRigthJPanel;
    private javax.swing.JPanel subCategoriesPane;
    private javax.swing.JLabel subCatlIndicatorJLabel;
    // End of variables declaration//GEN-END:variables

}

class TextPaneWithBackground extends JTextPane {

    private BufferedImage background;

    public TextPaneWithBackground(BufferedImage background) {
        this.background = background;
        setForeground(Color.WHITE);
        setOpaque(false);
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return background == null ? super.getPreferredScrollableViewportSize() : new Dimension(background.getWidth(), background.getHeight());
    }

    @Override
    public Dimension getPreferredSize() {
        return background == null ? super.getPreferredSize() : new Dimension(background.getWidth(), background.getHeight());
    }

    @Override
    protected void paintComponent(Graphics g) {

        Graphics2D g2d = (Graphics2D) g.create();

        if (isOpaque()) {
            g2d.setColor(getBackground());
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }

        if (background != null) {
            int x = (getWidth() - background.getWidth()) / 2;
            int y = (getHeight() - background.getHeight()) / 2;
            g2d.drawImage(background, x, y, this);
        }

        getUI().paint(g2d, this);
        g2d.dispose();
    }
}

class ButtonWithBackground extends JButton {

    private BufferedImage background;

    public ButtonWithBackground(BufferedImage background) {
        this.background = background;
        setForeground(Color.WHITE);
        setOpaque(false);
    }

    @Override
    public Dimension getPreferredSize() {
        return background == null ? super.getPreferredSize() : new Dimension(background.getWidth(), background.getHeight());
    }

    @Override
    protected void paintComponent(Graphics g) {

        Graphics2D g2d = (Graphics2D) g.create();

        if (isOpaque()) {
            g2d.setColor(getBackground());
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }

        if (background != null) {
            int x = (getWidth() - background.getWidth()) / 2;
            int y = (getHeight() - background.getHeight()) / 2;
            g2d.drawImage(background, x, y, this);
        }

        getUI().paint(g2d, this);
        g2d.dispose();
    }
}
