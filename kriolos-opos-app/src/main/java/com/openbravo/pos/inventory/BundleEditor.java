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

import com.openbravo.basic.AppLocal;
import com.openbravo.basic.BasicException;
import com.openbravo.data.gui.MessageInf;
import com.openbravo.data.user.DirtyManager;
import com.openbravo.data.user.EditorRecord;
import com.openbravo.format.Formats;
import com.openbravo.pos.catalog.CatalogService;
import com.openbravo.pos.panels.AppView;
import com.openbravo.pos.panels.JProductFinderPanel;
import com.openbravo.pos.pim.DataLogicPIM;
import com.openbravo.pos.ticket.ProductInfoExt;
import com.openbravo.pos.util.FormatsDoubleConverter;
import com.openbravo.ui.data.binding.Converter;
import com.openbravo.ui.data.binding.SwingBinder;
import java.awt.Component;
import java.util.UUID;

/**
 * Editor panel for product bundle items, supporting real-time validation,
 * positive quantity checks, dirty state tracking, and headless testing via SwingBinder.
 *
 * @author jaroslawwozniak
 * @author KriolOS
 */
public class BundleEditor extends javax.swing.JPanel implements EditorRecord {

    private static final long serialVersionUID = 1L;

    private CatalogService catalogService;
    private DataLogicPIM dataLogicPIM;
    
    private String id;
    private String product;
    private String productBundle;
    private String name;
    private Double quantity;
    private String insertproduct;

    private AppView appView;
    private final DirtyManager m_Dirty;
    private SwingBinder<BundleModel> binder;

    /**
     * Mutable model adapting product bundle items for data binding.
     */
    public static final class BundleModel {
        private String id;
        private String productId;
        private String bundleProductId;
        private Double quantity;
        private String reference;
        private String barcode;
        private String name;
        private String productDisplay;

        public BundleModel() {
            this(null, null, null, null, null, null, null, null);
        }

        public BundleModel(String id, String productId, String bundleProductId, Double quantity,
                           String reference, String barcode, String name, String productDisplay) {
            this.id = id;
            this.productId = productId;
            this.bundleProductId = bundleProductId;
            this.quantity = quantity;
            this.reference = reference;
            this.barcode = barcode;
            this.name = name;
            this.productDisplay = productDisplay;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getProductId() {
            return productId;
        }

        public void setProductId(String productId) {
            this.productId = productId;
        }

        public String getBundleProductId() {
            return bundleProductId;
        }

        public void setBundleProductId(String bundleProductId) {
            this.bundleProductId = bundleProductId;
        }

        public Double getQuantity() {
            return quantity;
        }

        public void setQuantity(Double quantity) {
            this.quantity = quantity;
        }

        public String getReference() {
            return reference;
        }

        public void setReference(String reference) {
            this.reference = reference;
        }

        public String getBarcode() {
            return barcode;
        }

        public void setBarcode(String barcode) {
            this.barcode = barcode;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getProductDisplay() {
            return productDisplay;
        }

        public void setProductDisplay(String productDisplay) {
            this.productDisplay = productDisplay;
        }
    }

    /**
     * Creates new form BundleEditor for headless testing.
     *
     * @param dirty Dirty manager
     */
    public BundleEditor(DirtyManager dirty) {
        this(null, dirty);
    }

    /**
     * Creates new form BundleEditor.
     *
     * @param app App view
     * @param dirty Dirty manager
     */
    public BundleEditor(AppView app, DirtyManager dirty) {
        this.appView = app;
        this.catalogService = (app != null) ? app.getBean(CatalogService.class) : null;
        this.dataLogicPIM = (catalogService instanceof DataLogicPIM) ? (DataLogicPIM) catalogService : null;
        this.m_Dirty = dirty;

        initComponents();

        if (dirty != null) {
            m_jProduct.getDocument().addDocumentListener(dirty);
            m_jQuantity.getDocument().addDocumentListener(dirty);
            m_jReference.getDocument().addDocumentListener(dirty);
            m_jBarcode.getDocument().addDocumentListener(dirty);
        }

        initValidator();
        writeValueEOF();
    }

    private void initValidator() {
        binder = new SwingBinder<>(new BundleModel());

        binder.forField(m_jProduct)
                .asRequired(AppLocal.getIntString("validation.required", AppLocal.getIntString("label.prodname")))
                .bind(BundleModel::getProductDisplay, BundleModel::setProductDisplay);

        binder.forField(m_jQuantity)
                .withConverter(new FormatsDoubleConverter())
                .asRequired(AppLocal.getIntString("validation.required", "Quantity"))
                .withValidator(qty -> qty != null && qty > 0.0, "Quantity must be greater than zero")
                .bind(BundleModel::getQuantity, BundleModel::setQuantity);

        binder.addStatusChangeListener(() -> {
            if (m_Dirty != null) {
                m_Dirty.setDirty(true);
            }
        });
    }

    public boolean isValid() {
        return binder == null || binder.validate();
    }

    public boolean isDataValid() {
        return isValid();
    }

    javax.swing.JTextField getProductField() {
        return m_jProduct;
    }

    javax.swing.JTextField getQuantityField() {
        return m_jQuantity;
    }

    javax.swing.JTextField getReferenceField() {
        return m_jReference;
    }

    javax.swing.JTextField getBarcodeField() {
        return m_jBarcode;
    }

    private static Double parseDouble(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        String trimmed = text.trim();
        try {
            return (Double) Formats.DOUBLE.parseValue(trimmed);
        } catch (Exception e) {
            try {
                return Double.parseDouble(trimmed.replace(',', '.'));
            } catch (Exception nfe) {
                return null;
            }
        }
    }

    private BundleModel toModel() {
        Double qtyVal = parseDouble(m_jQuantity.getText());
        return new BundleModel(id, product, productBundle, qtyVal, m_jReference.getText(),
                m_jBarcode.getText(), name, m_jProduct.getText());
    }
    
    public void setInsertProduct(ProductInfoExt prod) {
        
        if (prod == null) {
            insertproduct = null;
        } else {
            insertproduct = prod.getID();
        }
    }

    @Override
    public void refresh() {
    }

    @Override
    public void writeValueEOF() {
        if (binder != null) {
            binder.clear();
        }
        id = null;
        product = null;
        productBundle = null;
        quantity = null;
        name = null;
        m_jReference.setText(null);
        m_jBarcode.setText(null);
        m_jProduct.setText(null);
        m_jQuantity.setText(null);

        m_jReference.setEnabled(false);
        m_jBarcode.setEnabled(false);
        m_jProduct.setEnabled(false);
        m_jQuantity.setEnabled(false);
        m_jEnter1.setEnabled(false);
        m_jEnter2.setEnabled(false);
        m_jSearch.setEnabled(false);
    }

    @Override
    public void writeValueInsert() {
        if (binder != null) {
            binder.clear();
        }
        id = UUID.randomUUID().toString();
        product = insertproduct;
        productBundle = null;
        name = null;
        quantity = null;
        m_jReference.setText(null);
        m_jBarcode.setText(null);
        m_jProduct.setText(null);
        m_jQuantity.setText(null);

        m_jReference.setEnabled(true);
        m_jBarcode.setEnabled(true);
        m_jProduct.setEnabled(true);
        m_jQuantity.setEnabled(true);
        m_jEnter1.setEnabled(true);
        m_jEnter2.setEnabled(true);
        m_jSearch.setEnabled(true);
    }

    @Override
    public void writeValueEdit(Object value) {
        if (value == null) {
            writeValueEOF();
            return;
        }
        if (value instanceof Object[] obj) {
            id = (String) obj[0];
            product = (String) obj[1];
            productBundle = (String) obj[2];
            quantity = (obj[3] instanceof Double d) ? d : (obj[3] != null ? Double.valueOf(obj[3].toString()) : null);
            name = (String) obj[6];
            m_jReference.setText(Formats.STRING.formatValue((String) obj[4]));
            m_jBarcode.setText(Formats.STRING.formatValue((String) obj[5]));
            m_jProduct.setText(Formats.STRING.formatValue((String) obj[4]) + " - " + Formats.STRING.formatValue((String) obj[6]));
            m_jQuantity.setText(quantity != null ? Formats.DOUBLE.formatValue(quantity) : "");
        } else if (value instanceof BundleModel model) {
            id = model.getId();
            product = model.getProductId();
            productBundle = model.getBundleProductId();
            quantity = model.getQuantity();
            name = model.getName();
            m_jReference.setText(model.getReference());
            m_jBarcode.setText(model.getBarcode());
            m_jProduct.setText(model.getProductDisplay());
            m_jQuantity.setText(quantity != null ? Formats.DOUBLE.formatValue(quantity) : "");
        }

        m_jReference.setEnabled(true);
        m_jBarcode.setEnabled(true);
        m_jProduct.setEnabled(true);
        m_jQuantity.setEnabled(true);
        m_jEnter1.setEnabled(true);
        m_jEnter2.setEnabled(true);
        m_jSearch.setEnabled(true);

        if (binder != null) {
            binder.readBean(toModel());
        }
    }

    @Override
    public void writeValueDelete(Object value) {
        if (value == null) {
            writeValueEOF();
            return;
        }
        if (value instanceof Object[] obj) {
            id = (String) obj[0];
            product = (String) obj[1];
            productBundle = (String) obj[2];
            quantity = (obj[3] instanceof Double d) ? d : (obj[3] != null ? Double.valueOf(obj[3].toString()) : null);
            name = (String) obj[6];
            m_jReference.setText(Formats.STRING.formatValue((String) obj[4]));
            m_jBarcode.setText(Formats.STRING.formatValue((String) obj[5]));
            m_jProduct.setText(Formats.STRING.formatValue((String) obj[4]) + " - " + Formats.STRING.formatValue((String) obj[6]));
            m_jQuantity.setText(quantity != null ? Formats.DOUBLE.formatValue(quantity) : "");
        } else if (value instanceof BundleModel model) {
            id = model.getId();
            product = model.getProductId();
            productBundle = model.getBundleProductId();
            quantity = model.getQuantity();
            name = model.getName();
            m_jReference.setText(model.getReference());
            m_jBarcode.setText(model.getBarcode());
            m_jProduct.setText(model.getProductDisplay());
            m_jQuantity.setText(quantity != null ? Formats.DOUBLE.formatValue(quantity) : "");
        }

        m_jReference.setEnabled(false);
        m_jBarcode.setEnabled(false);
        m_jProduct.setEnabled(false);
        m_jQuantity.setEnabled(false);
        m_jEnter1.setEnabled(false);
        m_jEnter2.setEnabled(false);
        m_jSearch.setEnabled(false);

        if (binder != null) {
            binder.readBean(toModel());
        }
    }

    @Override
    public Object createValue() throws BasicException {
        if (binder != null) {
            try {
                binder.writeBean(toModel());
            } catch (SwingBinder.ValidationException ex) {
                // validation status surfaced via isValid()
            }
        }
        return new Object[] {
            id, 
            product, 
            productBundle,
            parseDouble(m_jQuantity.getText()),
            m_jReference.getText(),
            m_jBarcode.getText(),
            name,
        };
    }

    @Override
    public Component getComponent() {
        return this;
    }

    private void assignProduct(ProductInfoExt prod) {
        if (m_jSearch.isEnabled()) {
            if (prod == null) {
                productBundle = null;
                quantity = null;
                m_jReference.setText(null);
                m_jBarcode.setText(null);
                m_jProduct.setText(null);
                name = null;
            } else {
                productBundle = prod.getID();
                quantity = null;
                m_jReference.setText(prod.getReference());
                m_jBarcode.setText(prod.getCode());
                m_jProduct.setText(prod.getReference() + " - " + prod.getName());
                name = prod.getName();
            }
            if (binder != null) {
                binder.readBean(toModel());
            }
        }
    }

    private void assignProductByCode() {
        if (catalogService == null) {
            return;
        }
        try {
            ProductInfoExt prod = catalogService.getProductInfoByCode(m_jBarcode.getText());
            assignProduct(prod);
            if (prod == null) {
                com.openbravo.util.NotifyUtils.beep();       
            }
        } catch (BasicException eData) {
            assignProduct(null);
            MessageInf msg = new MessageInf(eData);
            msg.show(this);
        }
    }

    private void assignProductByReference() {
        if (catalogService == null) {
            return;
        }
        try {
            ProductInfoExt prod = catalogService.getProductInfoByReference(m_jReference.getText());
            assignProduct(prod);
            if (prod == null) {
                com.openbravo.util.NotifyUtils.beep();       
            }
        } catch (BasicException eData) {
            assignProduct(null);
            MessageInf msg = new MessageInf(eData);
            msg.show(this);
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel3 = new javax.swing.JLabel();
        m_jReference = new javax.swing.JTextField();
        m_jEnter1 = new javax.swing.JButton();
        m_jEnter2 = new javax.swing.JButton();
        m_jSearch = new javax.swing.JButton();
        m_jProduct = new javax.swing.JTextField();
        m_jBarcode = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        m_jQuantity = new javax.swing.JTextField();

        setPreferredSize(new java.awt.Dimension(700, 120));

        jLabel3.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel3.setText(AppLocal.getIntString("label.prodref")); // NOI18N
        jLabel3.setPreferredSize(new java.awt.Dimension(100, 30));

        m_jReference.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jReference.setPreferredSize(new java.awt.Dimension(150, 30));
        m_jReference.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jReferenceActionPerformed(evt);
            }
        });

        m_jEnter1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/ok.png"))); // NOI18N
        m_jEnter1.setPreferredSize(new java.awt.Dimension(80, 45));
        m_jEnter1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jEnter1ActionPerformed(evt);
            }
        });

        m_jEnter2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/barcode.png"))); // NOI18N
        m_jEnter2.setPreferredSize(new java.awt.Dimension(80, 45));
        m_jEnter2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jEnter2ActionPerformed(evt);
            }
        });

        m_jSearch.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/search24.png"))); // NOI18N
        m_jSearch.setPreferredSize(new java.awt.Dimension(80, 45));
        m_jSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jSearchActionPerformed(evt);
            }
        });

        m_jProduct.setEditable(false);
        m_jProduct.setPreferredSize(new java.awt.Dimension(200, 30));
        m_jProduct.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jProductActionPerformed(evt);
            }
        });

        m_jBarcode.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jBarcode.setPreferredSize(new java.awt.Dimension(150, 30));
        m_jBarcode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jBarcodeActionPerformed(evt);
            }
        });

        jLabel4.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel4.setText(AppLocal.getIntString("label.prodbarcode")); // NOI18N
        jLabel4.setPreferredSize(new java.awt.Dimension(100, 30));

        jLabel1.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel1.setText("Quantity");
        jLabel1.setPreferredSize(new java.awt.Dimension(110, 30));

        m_jQuantity.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jQuantity.setPreferredSize(new java.awt.Dimension(100, 30));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(m_jBarcode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(m_jReference, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(10, 10, 10)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(m_jEnter1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(m_jEnter2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(m_jQuantity, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                    .addComponent(m_jProduct, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(m_jSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_jReference, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_jEnter1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_jProduct, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_jSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(m_jBarcode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_jEnter2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_jQuantity, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void m_jSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jSearchActionPerformed
        if (appView != null) {
            assignProduct(JProductFinderPanel.showMessage(this, appView, JProductFinderPanel.PRODUCT_BUNDLE));
        }
    }//GEN-LAST:event_m_jSearchActionPerformed

    private void m_jReferenceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jReferenceActionPerformed
        this.assignProductByReference();
    }//GEN-LAST:event_m_jReferenceActionPerformed

    private void m_jEnter2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jEnter2ActionPerformed
        this.assignProductByCode();
    }//GEN-LAST:event_m_jEnter2ActionPerformed

    private void m_jEnter1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jEnter1ActionPerformed
        this.assignProductByReference();
    }//GEN-LAST:event_m_jEnter1ActionPerformed

    private void m_jBarcodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jBarcodeActionPerformed
        this.assignProductByCode();
    }//GEN-LAST:event_m_jBarcodeActionPerformed

    private void m_jProductActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jProductActionPerformed

    }//GEN-LAST:event_m_jProductActionPerformed

  


    public CatalogService getCatalogService() {
        return catalogService;
    }

    @Deprecated
    public DataLogicPIM getDataLogicPIM() {
        return dataLogicPIM;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JTextField m_jBarcode;
    private javax.swing.JButton m_jEnter1;
    private javax.swing.JButton m_jEnter2;
    private javax.swing.JTextField m_jProduct;
    private javax.swing.JTextField m_jQuantity;
    private javax.swing.JTextField m_jReference;
    private javax.swing.JButton m_jSearch;
    // End of variables declaration//GEN-END:variables

}
