package com.openbravo.pos.catalog;

import com.openbravo.basic.BasicException;
import com.openbravo.basic.AppLocal;
import com.openbravo.pos.spi.config.AppProperties;
import com.openbravo.pos.panels.AppView;
import com.openbravo.pos.pim.CategoryInfo;
import com.openbravo.pos.sales.TaxService;
import com.openbravo.pos.ticket.ProductInfoExt;
import com.openbravo.pos.ticket.TaxInfo;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * Modernized touch-friendly catalog selector component. Integrates categorized
 * product navigation and dynamic grid layouts.
 *
 * @author adrianromero
 * @author poolborges
 * @author KriolOS
 */
public class JCatalog extends JPanel implements ListSelectionListener, CatalogSelector {

    private static final long serialVersionUID = 1L;
    private static final int DEFAULT_CATALOG_PANEL_HEIGHT = 245;

    protected final EventListenerList listeners = new EventListenerList();

    private final boolean priceVisible;
    private final boolean taxesIncluded;

    private final Map<String, ProductInfoExt> productsCached = new HashMap<>();
    private final Set<String> categoriesCached = new HashSet<>();

    private CategoryInfo showingCategory = null;
    private final CatalogController controller;

    // UI Components
    private JPanel categoriesContainer;
    private JPanel rootCategoriesPanel;
    private JPanel subCategoriesPanel;
    private JScrollPane categoriesScrollPane;
    private JList<CategoryInfo> categoriesList;

    private JLabel subCatIndicatorLabel;
    private JButton subCatBackButton;
    private JPanel productsGridPane;

    private AppProperties config;

    public JCatalog(AppView app) {
        this(app, true, true);
    }

    public JCatalog(AppView app, boolean priceVisible, boolean taxesIncluded) {
        //this.priceVisible = priceVisible;
        //this.taxesIncluded = taxesIncluded;

        config = app.getProperties();

        int panelHeight = getCatalogPanelHeight();
        int catWidth = getCatalogCategoriesWidth();
        boolean showPrice = getBoolean("catalog.price.visible", true);
        boolean taxInc = getBoolean("catalog.taxes.included", true);

        this.priceVisible = showPrice;
        this.taxesIncluded = taxInc;

        TaxService taxService = app.getBean(TaxService.class);
        CatalogService catalogService = app.getBean(CatalogService.class);
        this.controller = new CatalogController(taxService, catalogService);

        initComponents();
        setPreferredSize(new Dimension(0, panelHeight));
        categoriesContainer.setPreferredSize(new Dimension(catWidth, 0));
        categoriesContainer.setMinimumSize(new Dimension(catWidth - 40, 0));
    }
    
    
    private boolean getBoolean(String configKey, boolean defaultValue) {
        return Boolean.parseBoolean(config.getProperty(configKey, Boolean.toString(defaultValue)));
    }

    private int getCatalogPanelHeight() {
        return Integer.parseInt(config.getProperty("catalog.panel.height", "300"));
    }

    private int getCatalogCategoriesWidth() {
        return Integer.parseInt(config.getProperty("catalog.categories.width", "240"));
    }

    private ProductCardV2.ViewMode getCatalogCardViewMode() {
        String mode = config.getProperty("catalog.card.viewmode", "DETAILED");
        return "COMPACT".equalsIgnoreCase(mode)
                ? ProductCardV2.ViewMode.COMPACT
                : ProductCardV2.ViewMode.DETAILED;
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
        productsGridPane.removeAll();
        productsCached.clear();
        categoriesCached.clear();
        showingCategory = null;

        List<CategoryInfo> categories = controller.getRootCategories();
        categoriesList.setModel(new CategoriesListModel(categories));

        if (categoriesList.getModel().getSize() == 0) {
            categoriesContainer.setVisible(false);
        } else {
            categoriesContainer.setVisible(true);
            categoriesList.setSelectedIndex(0);
        }

        showRootCategoriesPanel();
    }

    @Override
    public void setComponentEnabled(boolean value) {
        categoriesList.setEnabled(value);
        categoriesScrollPane.setEnabled(value);
        subCatIndicatorLabel.setEnabled(value);
        subCatBackButton.setEnabled(value);
        productsGridPane.setEnabled(value);

        synchronized (productsGridPane.getTreeLock()) {
            for (Component comp : productsGridPane.getComponents()) {
                comp.setEnabled(value);
            }
        }
        super.setEnabled(value);
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
            int selectedIndex = categoriesList.getSelectedIndex();
            if (selectedIndex >= 0) {
                Rectangle cellBounds = categoriesList.getCellBounds(selectedIndex, selectedIndex);
                if (cellBounds != null) {
                    categoriesList.scrollRectToVisible(cellBounds);
                }
                CategoryInfo selected = categoriesList.getSelectedValue();
                if (selected != null) {
                    showProductGridByCat(selected.getID());
                }
            }
        }
    }

    protected void fireSelectedProduct(ProductInfoExt prod) {
        EventListener[] list = listeners.getListeners(ActionListener.class);
        ActionEvent event = null;
        for (EventListener listener : list) {
            if (event == null) {
                event = new ActionEvent(prod, ActionEvent.ACTION_PERFORMED, prod.getID());
            }
            ((ActionListener) listener).actionPerformed(event);
        }
    }

    private void showProductGridByCat(String categoryId) {
        if (!categoriesCached.contains(categoryId)) {
            JCatalogTab catalogTab = new JCatalogTab();
            catalogTab.applyComponentOrientation(getComponentOrientation());
            productsGridPane.add(catalogTab, categoryId);
            categoriesCached.add(categoryId);

            // 1. Show constant products
            List<ProductInfoExt> constantProducts = controller.getProductConstant();
            for (ProductInfoExt prod : constantProducts) {
                catalogTab.addCatalogItem(buildCatalogItem(prod), new SelectProductListener(prod));
            }

            // 2. Show subcategories
            List<CategoryInfo> subCategories = controller.getSubcategories(categoryId);
            for (CategoryInfo subCat : subCategories) {
                String catName = subCat.getCatShowName() ? subCat.getName() : "";
                Image img = controller.getThumbNailOrDefaultSubCat(subCat.getImage());

                CatalogItem catItem = new CatalogItem(catName);
                catItem.setImage(img);
                catItem.setTextTip(subCat.getTextTip());
                catalogTab.addCatalogItem(catItem, new SelectCategoryListener(subCat));
            }

            // 3. Show category products
            List<ProductInfoExt> products = controller.findProductByCategory(categoryId);
            for (ProductInfoExt prod : products) {
                catalogTab.addCatalogItem(buildCatalogItem(prod), new SelectProductListener(prod));
            }
        }

        CardLayout cl = (CardLayout) productsGridPane.getLayout();
        cl.show(productsGridPane, categoryId);
    }

    private CatalogItem buildCatalogItem(ProductInfoExt product) {
        String title = (product.getDisplay() != null && !product.getDisplay().isBlank())
                ? product.getDisplay()
                : product.getName();

        CatalogItem item = new CatalogItem(title);
        item.setTextTip(product.getTextTip());

        if (product.getImage() != null) {
            Image icon = controller.getThumbNailOrDefault(product.getImage());
            item.setImage(icon);
        } else {
            item.setImage(null);
        }

        if (priceVisible) {
            String priceStr;
            if (taxesIncluded) {
                TaxInfo tax = controller.getTaxInfo(product.getTaxCategoryID());
                priceStr = product.printPriceSellTax(tax);
            } else {
                priceStr = product.printPriceSell();
            }
            item.setPrice(priceStr);
        } else {
            item.setPrice(null);
        }

        return item;
    }

    private void setSubCatIndicator(Icon icon, String label) {
        subCatIndicatorLabel.setText(label);
        subCatIndicatorLabel.setIcon(icon);

        CardLayout cl = (CardLayout) categoriesContainer.getLayout();
        cl.show(categoriesContainer, "subcategories");
    }

    private void showRootCategoriesPanel() {
        CardLayout cl = (CardLayout) categoriesContainer.getLayout();
        cl.show(categoriesContainer, "rootcategories");

        CategoryInfo selected = categoriesList.getSelectedValue();
        if (selected != null) {
            showProductGridByCat(selected.getID());
        }
        showingCategory = null;
    }

    private void showSubcategoryPanel(CategoryInfo category) {
        Image img = controller.getThumbNailOrDefaultCat(category.getImage());
        setSubCatIndicator(new ImageIcon(img), category.getName());
        showProductGridByCat(category.getID());
        showingCategory = category;
    }

    private void showProductPanel(String id) {
        ProductInfoExt product = productsCached.get(id);

        if (product == null) {
            if (productsCached.containsKey(id)) {
                restorePreviousCategoryView();
            } else {
                List<ProductInfoExt> companionProducts = controller.getProductCompanion(id);
                if (companionProducts.isEmpty()) {
                    productsCached.put(id, null);
                    restorePreviousCategoryView();
                } else {
                    product = controller.getProductInfo(id);
                    if (product != null) {
                        productsCached.put(id, product);

                        JCatalogTab productTab = new JCatalogTab();
                        productTab.applyComponentOrientation(getComponentOrientation());
                        productsGridPane.add(productTab, "PRODUCT." + id);

                        for (ProductInfoExt prod : companionProducts) {
                            productTab.addCatalogItem(buildCatalogItem(prod), new SelectProductListener(prod));
                        }

                        setSubCatIndicator(
                                new ImageIcon(controller.getThumbNailOrDefault(product.getImage())),
                                product.getDisplay()
                        );

                        CardLayout cl = (CardLayout) productsGridPane.getLayout();
                        cl.show(productsGridPane, "PRODUCT." + id);
                    } else {
                        productsCached.put(id, null);
                        restorePreviousCategoryView();
                    }
                }
            }
        } else {
            setSubCatIndicator(
                    new ImageIcon(controller.getThumbNailOrDefault(product.getImage())),
                    product.getName()
            );

            CardLayout cl = (CardLayout) productsGridPane.getLayout();
            cl.show(productsGridPane, "PRODUCT." + id);
        }
    }

    private void restorePreviousCategoryView() {
        if (showingCategory == null) {
            showRootCategoriesPanel();
        } else {
            showSubcategoryPanel(showingCategory);
        }
    }

    private void initComponents() {
        setLayout(new BorderLayout(8, 0));
        setBorder(new EmptyBorder(4, 4, 4, 4));

        // --- 1. Left Categories Container (Root vs Subcategory Switcher) ---
        categoriesContainer = new JPanel(new CardLayout());
        categoriesContainer.setPreferredSize(new Dimension(240, 0));
        categoriesContainer.setMinimumSize(new Dimension(200, 0));

        // 1.1 Root Categories View
        rootCategoriesPanel = new JPanel(new BorderLayout());
        rootCategoriesPanel.setOpaque(false);

        categoriesList = new JList<>();
        categoriesList.setFont(new Font("Segoe UI", Font.BOLD, 13));
        categoriesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        categoriesList.setFixedCellHeight(46);
        categoriesList.setCellRenderer(new ModernCategoryRenderer());
        categoriesList.addListSelectionListener(this);

        categoriesScrollPane = new JScrollPane(categoriesList);
        categoriesScrollPane.setBorder(BorderFactory.createLineBorder(
                UIManager.getColor("Component.borderColor") != null
                ? UIManager.getColor("Component.borderColor")
                : new Color(220, 224, 230), 1));
        categoriesScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        categoriesScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        categoriesScrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(28, 28));

        rootCategoriesPanel.add(categoriesScrollPane, BorderLayout.CENTER);
        categoriesContainer.add(rootCategoriesPanel, "rootcategories");

        // 1.2 Subcategory Breadcrumb & Navigation Header
        subCategoriesPanel = new JPanel(new BorderLayout(5, 5));
        subCategoriesPanel.setOpaque(false);
        subCategoriesPanel.setBorder(new EmptyBorder(6, 6, 6, 6));

        subCatIndicatorLabel = new JLabel("Category", SwingConstants.LEFT);
        subCatIndicatorLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        subCatIndicatorLabel.setIconTextGap(8);

        subCatBackButton = new JButton(AppLocal.getIntString("button.back") != null
                ? AppLocal.getIntString("button.back") : "Voltar");
        try {
            subCatBackButton.setIcon(new ImageIcon(getClass().getResource("/com/openbravo/images/2uparrow.png")));
        }
        catch (Exception ignored) {
        }
        subCatBackButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        subCatBackButton.setFocusPainted(false);
        subCatBackButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        subCatBackButton.putClientProperty("JButton.buttonType", "roundRect");
        subCatBackButton.setMargin(new Insets(6, 12, 6, 12));
        subCatBackButton.addActionListener(e -> showRootCategoriesPanel());

        subCategoriesPanel.add(subCatIndicatorLabel, BorderLayout.CENTER);
        subCategoriesPanel.add(subCatBackButton, BorderLayout.EAST);

        categoriesContainer.add(subCategoriesPanel, "subcategories");

        add(categoriesContainer, BorderLayout.LINE_START);

        // --- 2. Products Grid Panel (CardLayout) ---
        productsGridPane = new JPanel(new CardLayout());
        productsGridPane.setOpaque(false);
        add(productsGridPane, BorderLayout.CENTER);
    }


    // --- Action Listeners & List Models ---
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

    private static class CategoriesListModel extends AbstractListModel<CategoryInfo> {

        private static final long serialVersionUID = 1L;
        private final List<CategoryInfo> categories;

        public CategoriesListModel(List<CategoryInfo> categories) {
            this.categories = categories;
        }

        @Override
        public int getSize() {
            return categories != null ? categories.size() : 0;
        }

        @Override
        public CategoryInfo getElementAt(int index) {
            return categories.get(index);
        }
    }

    /**
     * Touch-optimized list cell renderer for category items with scaled icons.
     * Prevents vertical image overflow and layout overlapping on fixed-height
     * cells.
     */
    private class ModernCategoryRenderer extends DefaultListCellRenderer {

        private static final long serialVersionUID = 1L;
        private static final int ICON_SIZE = 24; // Fixed icon dimension to prevent vertical overlap

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {

            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value instanceof CategoryInfo cat) {
                setText(cat.getName());
                Image rawIcon = controller.getThumbNailOrDefaultCat(cat.getImage());

                if (rawIcon != null) {
                    // Smoothly scale the category image to fit within the cell bounds
                    Image scaled = rawIcon.getScaledInstance(ICON_SIZE, ICON_SIZE, Image.SCALE_SMOOTH);
                    setIcon(new ImageIcon(scaled));
                } else {
                    setIcon(null);
                }
            }

            // Cell styling and padding configuration
            setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            setIconTextGap(12);
            setVerticalAlignment(SwingConstants.CENTER);

            return this;
        }
    }
}
