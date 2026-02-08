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
package com.openbravo.pos.inventory;

import com.openbravo.basic.BasicException;
import com.openbravo.data.gui.ComboBoxValModel;
import com.openbravo.data.gui.JMessageDialog;
import com.openbravo.data.gui.MessageInf;
import com.openbravo.data.loader.SentenceList;
import com.openbravo.data.user.DirtyManager;
import com.openbravo.data.user.EditorRecord;
import com.openbravo.format.Formats;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.DataLogicSales;
import java.awt.Component;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.swing.JPanel;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;
import com.openbravo.pos.catalog.CategoryStock;
import com.openbravo.pos.ticket.CategoryInfo;
import com.openbravo.pos.util.DataTypeUtils;
import java.awt.Font;
import javax.swing.table.JTableHeader;

/**
 *
 * @author adrianromero
 */
public final class CategoriesEditor extends JPanel implements EditorRecord {

    private final static Logger LOGGER = Logger.getLogger(CategoriesEditor.class.getName());

    private String categoryId;

    private ComboBoxValModel categoryParentModel;
    private List<CategoryStock> categoryStockList;
    private CategoriesEditor.StockTableModel stockModel;

    private final DataLogicSales dlSales;

    /**
     * Creates new form JPanelCategories
     *
     * @param app
     * @param dirty
     */
    public CategoriesEditor(AppView app, DirtyManager dirty) {

        initComponents();

        dlSales = (DataLogicSales) app.getBean("com.openbravo.pos.forms.DataLogicSales");
        categoryParentModel = new ComboBoxValModel(); 

        categoryNameText.getDocument().addDocumentListener(dirty);
        categoryParentCombox.addActionListener(dirty);
        categoryImage.addPropertyChangeListener("image", dirty);
        categoryShowName.addActionListener(dirty);
        categoryTipText.getDocument().addDocumentListener(dirty);
        categoryCatalogOrder.getDocument().addDocumentListener(dirty);
        categoryShowInCatalog.addActionListener(dirty);
        categoryCatalogOrder.getDocument().addDocumentListener(dirty);
        categoryColorPicker.addPropertyChangeListener("color", dirty);

        writeValueEOF();
    }

    /**
     *
     */
    @Override
    public void refresh() {

        List categories = dlSales.getCategoriesListAll();
        categories.add(0, null);
        categoryParentModel = new ComboBoxValModel(categories);
        categoryParentCombox.setModel(categoryParentModel);

        jLblProdCount.setText(null);
    }

    /**
     *
     */
    @Override
    public void writeValueEOF() {
        categoryId = null;
        categoryNameText.setText(null);
        categoryParentModel.setSelectedKey(null);
        categoryImage.setImage(null);
        categoryNameText.setEnabled(false);
        categoryParentCombox.setEnabled(false);
        categoryImage.setEnabled(false);
        categoryShowInCatalog.isSelected();
        categoryTipText.setText(null);
        categoryTipText.setEnabled(false);
        categoryShowName.setSelected(false);
        categoryShowName.setEnabled(false);
        categoryCatalogOrder.setText(null);
        categoryCatalogOrder.setEnabled(false);
        categoryColorPicker.reset();
        categoryColorPicker.setEnabled(false);

    }

    /**
     *
     */
    @Override
    public void writeValueInsert() {
        categoryId = UUID.randomUUID().toString();
        categoryNameText.setText(null);
        categoryParentModel.setSelectedKey(null);
        categoryImage.setImage(null);
        categoryNameText.setEnabled(true);
        categoryParentCombox.setEnabled(true);
        categoryImage.setEnabled(true);
        categoryShowInCatalog.setSelected(true);
        categoryShowInCatalog.setEnabled(true);
        categoryTipText.setText(null);
        categoryTipText.setEnabled(true);
        categoryShowName.setSelected(true);
        categoryShowName.setEnabled(true);
        categoryCatalogOrder.setText(null);
        categoryCatalogOrder.setEnabled(true);
        categoryColorPicker.reset();
        categoryColorPicker.setEnabled(true);

    }

    /**
     *
     * @param value
     */
    @Override
    public void writeValueDelete(Object value) {
        Object[] cat = (Object[]) value;
        categoryId = (String) cat[0];
        categoryNameText.setText(Formats.STRING.formatValue((String) cat[1]));
        categoryParentModel.setSelectedKey(cat[2]);
        categoryImage.setImage((BufferedImage) cat[3]);
        categoryTipText.setText(Formats.STRING.formatValue((String) cat[4]));
        categoryShowName.setSelected(DataTypeUtils.toBoolean(cat[5]));
        categoryCatalogOrder.setText(Formats.STRING.formatValue((String) cat[6]));
        categoryColorPicker.setColor((String) cat[7]);
        categoryShowInCatalog.setSelected(DataTypeUtils.toBoolean(cat[8]));

        categoryNameText.setEnabled(false);
        categoryParentCombox.setEnabled(false);
        categoryImage.setEnabled(false);
        categoryShowInCatalog.setEnabled(false);
        categoryTipText.setEnabled(false);
        categoryShowName.setEnabled(false);
        categoryCatalogOrder.setEnabled(false);
        categoryColorPicker.setEnabled(false);

        stockModel = new CategoriesEditor.StockTableModel(getProductsByCategoryId(categoryId));

    }

    /**
     *
     * @param value
     */
    @Override
    public void writeValueEdit(Object value) {
        Object[] cat = (Object[]) value;
        categoryId = (String) cat[0];
        categoryNameText.setText(Formats.STRING.formatValue((String) cat[1]));
        categoryParentModel.setSelectedKey(cat[2]);
        categoryImage.setImage((BufferedImage) cat[3]);
        categoryTipText.setText(Formats.STRING.formatValue((String) cat[4]));
        categoryShowName.setSelected(DataTypeUtils.toBoolean(cat[5]));
        categoryCatalogOrder.setText(Formats.STRING.formatValue((String) cat[6]));
        categoryColorPicker.setColor((String) cat[7]);
        categoryShowInCatalog.setSelected(DataTypeUtils.toBoolean(cat[8]));

        if (categoryCatalogOrder.getText().isBlank()) {
            categoryCatalogOrder.setText(null);
        }

        categoryNameText.setEnabled(true);
        categoryParentCombox.setEnabled(true);
        categoryImage.setEnabled(true);
        categoryShowInCatalog.setEnabled(true);
        categoryTipText.setEnabled(true);
        categoryShowName.setEnabled(true);
        categoryCatalogOrder.setEnabled(true);
        categoryColorPicker.setEnabled(true);

        resetTranxTable();
    }
    
    
    
    /**
     *
     * @return @throws BasicException
     */
    @Override
    public Object createValue() throws BasicException {

        Object[] cat = new Object[10];

        cat[0] = categoryId;
        cat[1] = categoryNameText.getText();
        cat[2] = categoryParentModel.getSelectedKey();
        cat[3] = categoryImage.getImage();
        cat[4] = categoryTipText.getText();
        cat[5] = categoryShowName.isSelected();
        if (categoryCatalogOrder.getText().isBlank()) {
            categoryCatalogOrder.setText(null);
        }
        cat[6] = categoryCatalogOrder.getText();
        cat[7] = categoryColorPicker.getHexColor();
        cat[8] = categoryShowInCatalog.isSelected();

        return cat;
    }

    /**
     *
     * @return
     */
    @Override
    public Component getComponent() {
        return this;
    }

    public void resetTranxTable() {

        jTableCategoryStock.getColumnModel().getColumn(0).setPreferredWidth(250);

        // set font for headers
        Font f = new Font("Arial", Font.BOLD, 14);
        JTableHeader header = jTableCategoryStock.getTableHeader();
        header.setFont(f);

        jTableCategoryStock.getTableHeader().setReorderingAllowed(true);
        jTableCategoryStock.setAutoCreateRowSorter(true);
        jTableCategoryStock.repaint();
    }

    private List<CategoryStock> getProductsByCategoryId(String categoryId) {

        try {
            categoryStockList = dlSales.getCategorysProductList(categoryId);
        }
        catch (BasicException ex) {
            LOGGER.log(Level.SEVERE, "Exception get products by category id: " + categoryId, ex);
        }

        List<CategoryStock> categoryList = new ArrayList<>();

        for (CategoryStock categoryStock : categoryStockList) {
            String categoryStockId = categoryStock.getCategoryId();
            if (categoryStockId.equals(categoryId)) {
                categoryList.add(categoryStock);
            }
        }

        repaint();
        refresh();

        return categoryList;
    }

    class StockTableModel extends AbstractTableModel {

        String nam = AppLocal.getIntString("label.prodname");
        String cod = AppLocal.getIntString("label.prodbarcode");

        List<CategoryStock> stockList;
        String[] columnNames = {nam, cod};

        public StockTableModel(List<CategoryStock> list) {
            stockList = list;
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public int getRowCount() {
            return stockList.size();
        }

        @Override
        public Object getValueAt(int row, int column) {
            CategoryStock categoryStock = stockList.get(row);

            switch (column) {
                case 0:
                    return categoryStock.getProductName();
                case 1:
                    return categoryStock.getProductCode();
                case 2:
                    return categoryStock.getProductId();
                default:
                    return "";
            }
        }

        @Override
        public String getColumnName(int col) {
            return columnNames[col];
        }
    }

    public void Notify(String msg) {

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jInternalFrame1 = new javax.swing.JInternalFrame();
        jLblCatName = new javax.swing.JLabel();
        categoryNameText = new javax.swing.JTextField();
        jLblCatParent = new javax.swing.JLabel();
        categoryParentCombox = new javax.swing.JComboBox();
        jLblCatTextTip = new javax.swing.JLabel();
        categoryTipText = new javax.swing.JTextField();
        jLblCatShowName = new javax.swing.JLabel();
        categoryShowName = new javax.swing.JCheckBox();
        jLblCatOrder = new javax.swing.JLabel();
        categoryCatalogOrder = new javax.swing.JTextField();
        jLblInCatalog = new javax.swing.JLabel();
        categoryShowInCatalog = new javax.swing.JCheckBox();
        categoryImage = new com.openbravo.data.gui.JImageEditor();
        jBtnShowTransactions = new javax.swing.JButton();
        jLblProdCount = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTableCategoryStock = new javax.swing.JTable();
        categoryColorPicker = new com.openbravo.data.gui.JColorPicker();
        jLblCatColor = new javax.swing.JLabel();

        jInternalFrame1.setVisible(true);

        setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        setPreferredSize(new java.awt.Dimension(700, 500));

        jLblCatName.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLblCatName.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/info.png"))); // NOI18N
        jLblCatName.setText(AppLocal.getIntString("label.namem")); // NOI18N
        jLblCatName.setPreferredSize(new java.awt.Dimension(125, 30));
        jLblCatName.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLblCatNameMouseClicked(evt);
            }
        });

        categoryNameText.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        categoryNameText.setPreferredSize(new java.awt.Dimension(250, 30));

        jLblCatParent.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLblCatParent.setText(AppLocal.getIntString("label.prodcategory")); // NOI18N
        jLblCatParent.setPreferredSize(new java.awt.Dimension(125, 30));

        categoryParentCombox.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        categoryParentCombox.setPreferredSize(new java.awt.Dimension(250, 30));

        jLblCatTextTip.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("pos_messages"); // NOI18N
        jLblCatTextTip.setText(bundle.getString("label.texttip")); // NOI18N
        jLblCatTextTip.setPreferredSize(new java.awt.Dimension(125, 30));

        categoryTipText.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        categoryTipText.setPreferredSize(new java.awt.Dimension(250, 30));

        jLblCatShowName.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLblCatShowName.setText(bundle.getString("label.subcategorytitle")); // NOI18N
        jLblCatShowName.setPreferredSize(new java.awt.Dimension(150, 30));

        categoryShowName.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        categoryShowName.setSelected(true);
        categoryShowName.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        categoryShowName.setPreferredSize(new java.awt.Dimension(30, 30));

        jLblCatOrder.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLblCatOrder.setText(bundle.getString("label.ccatorder")); // NOI18N
        jLblCatOrder.setInheritsPopupMenu(false);
        jLblCatOrder.setPreferredSize(new java.awt.Dimension(60, 30));

        categoryCatalogOrder.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        categoryCatalogOrder.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        categoryCatalogOrder.setMaximumSize(new java.awt.Dimension(60, 30));
        categoryCatalogOrder.setMinimumSize(new java.awt.Dimension(60, 30));
        categoryCatalogOrder.setPreferredSize(new java.awt.Dimension(60, 30));

        jLblInCatalog.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLblInCatalog.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLblInCatalog.setText(AppLocal.getIntString("label.CatalogueStatusYes")); // NOI18N
        jLblInCatalog.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        jLblInCatalog.setPreferredSize(new java.awt.Dimension(125, 30));

        categoryShowInCatalog.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        categoryShowInCatalog.setMaximumSize(new java.awt.Dimension(30, 30));
        categoryShowInCatalog.setPreferredSize(new java.awt.Dimension(30, 30));
        categoryShowInCatalog.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                categoryShowInCatalogActionPerformed(evt);
            }
        });

        jBtnShowTransactions.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jBtnShowTransactions.setText(bundle.getString("button.CatProds")); // NOI18N
        jBtnShowTransactions.setToolTipText("");
        jBtnShowTransactions.setPreferredSize(new java.awt.Dimension(140, 30));
        jBtnShowTransactions.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnShowTransactionsActionPerformed(evt);
            }
        });

        jLblProdCount.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLblProdCount.setText("...");
        jLblProdCount.setOpaque(true);
        jLblProdCount.setPreferredSize(new java.awt.Dimension(237, 30));

        jScrollPane2.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jScrollPane2.setPreferredSize(new java.awt.Dimension(340, 502));

        jTableCategoryStock.setAutoCreateRowSorter(true);
        jTableCategoryStock.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jTableCategoryStock.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null}
            },
            new String [] {
                "Name", "Barcode"
            }
        ));
        jTableCategoryStock.setGridColor(new java.awt.Color(102, 204, 255));
        jTableCategoryStock.setRowHeight(25);
        jScrollPane2.setViewportView(jTableCategoryStock);
        if (jTableCategoryStock.getColumnModel().getColumnCount() > 0) {
            jTableCategoryStock.getColumnModel().getColumn(0).setPreferredWidth(250);
        }

        categoryColorPicker.setBackground(new java.awt.Color(255, 255, 255));
        categoryColorPicker.setText("color");
        categoryColorPicker.setMaximumSize(new java.awt.Dimension(100, 30));
        categoryColorPicker.setMinimumSize(new java.awt.Dimension(100, 30));
        categoryColorPicker.setPreferredSize(new java.awt.Dimension(60, 30));

        jLblCatColor.setText("Category Color");
        jLblCatColor.setMaximumSize(new java.awt.Dimension(60, 30));
        jLblCatColor.setMinimumSize(new java.awt.Dimension(60, 30));
        jLblCatColor.setPreferredSize(new java.awt.Dimension(60, 30));
        jLblCatColor.setRequestFocusEnabled(false);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(jLblCatParent, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLblCatTextTip, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLblCatName, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(categoryNameText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(categoryTipText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(categoryParentCombox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(jLblCatOrder, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLblInCatalog, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLblCatShowName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLblCatColor, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(0, 0, 0)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(categoryShowName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(categoryShowInCatalog, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                        .addComponent(categoryCatalogOrder, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(215, 215, 215))
                                    .addComponent(categoryColorPicker, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(categoryImage, javax.swing.GroupLayout.PREFERRED_SIZE, 263, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(jLblProdCount, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 311, Short.MAX_VALUE)
                                .addComponent(jBtnShowTransactions, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(categoryNameText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLblCatName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(categoryParentCombox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLblCatParent, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(categoryTipText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLblCatTextTip, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLblInCatalog, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(categoryShowInCatalog, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLblCatShowName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(categoryShowName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLblCatOrder, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(categoryCatalogOrder, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(8, 8, 8)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(categoryColorPicker, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLblCatColor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(categoryImage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jBtnShowTransactions, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLblProdCount, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 179, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jLblCatColor.getAccessibleContext().setAccessibleName("Catalog Color");
    }// </editor-fold>//GEN-END:initComponents

    private void categoryShowInCatalogActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_categoryShowInCatalogActionPerformed

        try {
            if (categoryShowInCatalog.isSelected()) {
                int del = dlSales.removeProductsFromCatalogWithCategoryId(categoryId);
                int add = dlSales.addProductsToCatalogWithCategoryId(categoryId);
                jLblInCatalog.setText(AppLocal.getIntString("label.CatalogueStatusYes"));
                //Notify(AppLocal.getIntString("notify.added"));   
                LOGGER.log(Level.INFO, "Number of products added: " + add + ", deleted: " + del + ", by categoryId: " + categoryId);
            } else {
                int del = dlSales.removeProductsFromCatalogWithCategoryId(categoryId);
                jLblInCatalog.setText(AppLocal.getIntString("label.CatalogueStatusNo"));
                //Notify(AppLocal.getIntString("notify.removed"));
                LOGGER.log(Level.INFO, "Number of products deleted: " + del + ", by categoryId: " + categoryId);
            }
        }
        catch (BasicException ex) {
            LOGGER.log(Level.SEVERE, "Exception adding/delete products to catalog categoryI: " + categoryId, ex);
            JMessageDialog.showMessage(this, new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotexecute"), ex));
        }


    }//GEN-LAST:event_categoryShowInCatalogActionPerformed

    private void jLblCatNameMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLblCatNameMouseClicked

        if (evt.getClickCount() == 2) {
            String uuidString = categoryId;
            StringSelection stringSelection = new StringSelection(uuidString);
            Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
            clpbrd.setContents(stringSelection, null);

            JOptionPane.showMessageDialog(this,
                    AppLocal.getIntString("message.uuidcopy"),
                    uuidString, JOptionPane.INFORMATION_MESSAGE);

            /*
            JMessageDialog.showMessage(this, 
                    new MessageInf(MessageInf.SGN_SUCCESS, AppLocal.getIntString("message.cannotexecute"), uuidString));
             */
        }
    }//GEN-LAST:event_jLblCatNameMouseClicked

    private void jBtnShowTransactionsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnShowTransactionsActionPerformed
        String pId = categoryId;
        if (pId != null) {
            stockModel = new CategoriesEditor.StockTableModel(getProductsByCategoryId(pId));
            jTableCategoryStock.setModel(stockModel);
            jTableCategoryStock.setVisible(false);
            jLblProdCount.setText(null);

            if (stockModel.getRowCount() > 0) {
                jTableCategoryStock.setVisible(true);
                String ProdCount = String.valueOf(stockModel.getRowCount());
                jLblProdCount.setText(ProdCount);
            } else {
                JOptionPane.showMessageDialog(this,
                        "No Products for this Category", "Products", JOptionPane.INFORMATION_MESSAGE);
            }
            resetTranxTable();
        }
    }//GEN-LAST:event_jBtnShowTransactionsActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField categoryCatalogOrder;
    private com.openbravo.data.gui.JColorPicker categoryColorPicker;
    private com.openbravo.data.gui.JImageEditor categoryImage;
    private javax.swing.JTextField categoryNameText;
    private javax.swing.JComboBox categoryParentCombox;
    private javax.swing.JCheckBox categoryShowInCatalog;
    private javax.swing.JCheckBox categoryShowName;
    private javax.swing.JTextField categoryTipText;
    private javax.swing.JButton jBtnShowTransactions;
    private javax.swing.JInternalFrame jInternalFrame1;
    private javax.swing.JLabel jLblCatColor;
    private javax.swing.JLabel jLblCatName;
    private javax.swing.JLabel jLblCatOrder;
    private javax.swing.JLabel jLblCatParent;
    private javax.swing.JLabel jLblCatShowName;
    private javax.swing.JLabel jLblCatTextTip;
    private javax.swing.JLabel jLblInCatalog;
    private javax.swing.JLabel jLblProdCount;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable jTableCategoryStock;
    // End of variables declaration//GEN-END:variables

}
