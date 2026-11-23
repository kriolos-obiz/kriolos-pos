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
//    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-130

package com.openbravo.pos.inventory;

import com.openbravo.basic.AppLocal;
import com.openbravo.basic.BasicException;
import com.openbravo.data.user.DirtyManager;
import com.openbravo.data.user.EditorRecord;
import com.openbravo.format.Formats;
import com.openbravo.pos.util.FormatsDoubleConverter;
import com.openbravo.ui.data.binding.SwingBinder;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTextField;

/**
 * Modernized editor for Warehouse stock limits with SwingBinder two-way binding,
 * real-time validation, and headless testability.
 *
 * @author adrianromero
 * @author KriolOS
 */
public class ProductsWarehouseEditor extends javax.swing.JPanel implements EditorRecord {

    private static final long serialVersionUID = 1L;

    /**
     * Presentation model for two-way Swing data binding.
     */
    public static class ProductsWarehouseModel {
        private Object id;
        private Object productId;
        private Object productRef;
        private Object productName;
        private Object location;
        private Double minimum;
        private Double maximum;
        private Double quantity;

        public ProductsWarehouseModel() {
        }

        public ProductsWarehouseModel(Object id, Object productId, Object productRef, Object productName, Object location, Double minimum, Double maximum, Double quantity) {
            this.id = id;
            this.productId = productId;
            this.productRef = productRef;
            this.productName = productName;
            this.location = location;
            this.minimum = minimum;
            this.maximum = maximum;
            this.quantity = quantity;
        }

        public Object getId() {
            return id;
        }

        public void setId(Object id) {
            this.id = id;
        }

        public Object getProductId() {
            return productId;
        }

        public void setProductId(Object productId) {
            this.productId = productId;
        }

        public Object getProductRef() {
            return productRef;
        }

        public void setProductRef(Object productRef) {
            this.productRef = productRef;
        }

        public Object getProductName() {
            return productName;
        }

        public void setProductName(Object productName) {
            this.productName = productName;
        }

        public Object getLocation() {
            return location;
        }

        public void setLocation(Object location) {
            this.location = location;
        }

        public Double getMinimum() {
            return minimum;
        }

        public void setMinimum(Double minimum) {
            this.minimum = minimum;
        }

        public Double getMaximum() {
            return maximum;
        }

        public void setMaximum(Double maximum) {
            this.maximum = maximum;
        }

        public Double getQuantity() {
            return quantity;
        }

        public void setQuantity(Double quantity) {
            this.quantity = quantity;
        }
    }

    public Object id;
    public Object prodid;
    public Object prodref;
    public Object prodname;
    public Object location;

    private DirtyManager dirty;
    private SwingBinder<ProductsWarehouseModel> binder;

    /**
     * Default constructor.
     */
    public ProductsWarehouseEditor() {
        this(new DirtyManager());
    }

    /**
     * Creates new form ProductsWarehouseEditor.
     *
     * @param dirty dirty state manager
     */
    public ProductsWarehouseEditor(DirtyManager dirty) {
        this.dirty = dirty;
        initComponents();

        if (dirty != null) {
            m_jMinimum.getDocument().addDocumentListener(dirty);
            m_jMaximum.getDocument().addDocumentListener(dirty);
        }

        initValidator();
        writeValueEOF();
    }

    private void initValidator() {
        binder = new SwingBinder<>(new ProductsWarehouseModel());

        // Minimum Stock (>= 0.0)
        binder.forField(m_jMinimum)
                .withConverter(new FormatsDoubleConverter())
                .withValidator(v -> v == null || v >= 0.0,
                        AppLocal.getIntString("validation.number.positive", AppLocal.getIntString("label.minimum")))
                .bind(ProductsWarehouseModel::getMinimum, ProductsWarehouseModel::setMinimum);

        // Maximum Stock (>= 0.0)
        binder.forField(m_jMaximum)
                .withConverter(new FormatsDoubleConverter())
                .withValidator(v -> v == null || v >= 0.0,
                        AppLocal.getIntString("validation.number.positive", AppLocal.getIntString("label.maximum")))
                .bind(ProductsWarehouseModel::getMaximum, ProductsWarehouseModel::setMaximum);

        binder.addStatusChangeListener(() -> {
            if (dirty != null) {
                dirty.setDirty(true);
            }
        });
    }

    @Override
    public boolean isValid() {
        if (m_jMinimum == null || m_jMaximum == null || binder == null) {
            return super.isValid();
        }
        boolean valid = binder.validate();
        if (!valid) {
            return false;
        }

        String minText = m_jMinimum.getText().trim();
        Double min = parseDouble(minText);
        if (!minText.isEmpty()) {
            if (minText.startsWith("-") || min == null || Double.compare(min, 0.0) < 0) {
                return false;
            }
        }

        String maxText = m_jMaximum.getText().trim();
        Double max = parseDouble(maxText);
        if (!maxText.isEmpty()) {
            if (maxText.startsWith("-") || max == null || Double.compare(max, 0.0) < 0) {
                return false;
            }
        }

        if (min != null && max != null && max < min) {
            m_jMaximum.setToolTipText(AppLocal.getIntString("validation.number.min",
                    AppLocal.getIntString("label.maximum"),
                    AppLocal.getIntString("label.minimum")));
            return false;
        }
        return true;
    }

    public boolean isDataValid() {
        return isValid();
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

    ProductsWarehouseModel toModel() {
        return new ProductsWarehouseModel(
                id,
                prodid,
                prodref,
                prodname,
                location,
                parseDouble(m_jMinimum.getText()),
                parseDouble(m_jMaximum.getText()),
                parseDouble(m_jQuantity.getText())
        );
    }

    @Override
    public void writeValueEOF() {
        m_jTitle.setText(AppLocal.getIntString("label.recordeof"));
        id = null;
        prodid = null;
        prodref = null;
        prodname = null;
        location = null;
        if (binder != null) {
            binder.clear();
        } else {
            m_jMinimum.setText(null);
            m_jMaximum.setText(null);
        }
        m_jQuantity.setText(null);
        m_jMinimum.setEnabled(false);
        m_jMaximum.setEnabled(false);
    }

    @Override
    public void writeValueInsert() {
        m_jTitle.setText(AppLocal.getIntString("label.recordnew"));
        id = null;
        prodid = null;
        prodref = null;
        prodname = null;
        location = null;
        m_jQuantity.setText(null);
        m_jMinimum.setText(null);
        m_jMaximum.setText(null);
        m_jMinimum.setEnabled(true);
        m_jMaximum.setEnabled(true);

        if (binder != null) {
            binder.readBean(toModel());
        }
    }

    @Override
    public void writeValueEdit(Object value) {
        if (value == null) {
            writeValueEOF();
            return;
        }

        if (value instanceof Object[] myprod) {
            id = myprod[0];
            prodid = myprod[1];
            prodref = myprod[2];
            prodname = myprod[3];
            location = myprod[4];
            m_jTitle.setText(Formats.STRING.formatValue((String) myprod[2]) + " - " + Formats.STRING.formatValue((String) myprod[3]));
            m_jQuantity.setText(Formats.DOUBLE.formatValue((Double) myprod[7]));
            m_jMinimum.setText(Formats.DOUBLE.formatValue((Double) myprod[5]));
            m_jMaximum.setText(Formats.DOUBLE.formatValue((Double) myprod[6]));
        } else if (value instanceof ProductsWarehouseModel model) {
            id = model.getId();
            prodid = model.getProductId();
            prodref = model.getProductRef();
            prodname = model.getProductName();
            location = model.getLocation();
            m_jTitle.setText(Formats.STRING.formatValue((String) model.getProductRef()) + " - " + Formats.STRING.formatValue((String) model.getProductName()));
            m_jQuantity.setText(Formats.DOUBLE.formatValue(model.getQuantity()));
            m_jMinimum.setText(Formats.DOUBLE.formatValue(model.getMinimum()));
            m_jMaximum.setText(Formats.DOUBLE.formatValue(model.getMaximum()));
        }

        m_jMinimum.setEnabled(true);
        m_jMaximum.setEnabled(true);

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

        if (value instanceof Object[] myprod) {
            id = myprod[0];
            prodid = myprod[1];
            prodref = myprod[2];
            prodname = myprod[3];
            location = myprod[4];
            m_jTitle.setText(Formats.STRING.formatValue((String) myprod[2]) + " - " + Formats.STRING.formatValue((String) myprod[3]));
            m_jQuantity.setText(Formats.DOUBLE.formatValue((Double) myprod[7]));
            m_jMinimum.setText(Formats.DOUBLE.formatValue((Double) myprod[5]));
            m_jMaximum.setText(Formats.DOUBLE.formatValue((Double) myprod[6]));
        } else if (value instanceof ProductsWarehouseModel model) {
            id = model.getId();
            prodid = model.getProductId();
            prodref = model.getProductRef();
            prodname = model.getProductName();
            location = model.getLocation();
            m_jTitle.setText(Formats.STRING.formatValue((String) model.getProductRef()) + " - " + Formats.STRING.formatValue((String) model.getProductName()));
            m_jQuantity.setText(Formats.DOUBLE.formatValue(model.getQuantity()));
            m_jMinimum.setText(Formats.DOUBLE.formatValue(model.getMinimum()));
            m_jMaximum.setText(Formats.DOUBLE.formatValue(model.getMaximum()));
        }

        m_jMinimum.setEnabled(false);
        m_jMaximum.setEnabled(false);

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

        return new Object[]{
            id,
            prodid,
            prodref,
            prodname,
            location,
            parseDouble(m_jMinimum.getText()),
            parseDouble(m_jMaximum.getText()),
            parseDouble(m_jQuantity.getText())
        };
    }

    @Override
    public Component getComponent() {
        return this;
    }

    @Override
    public void refresh() {
    }

    JTextField getMinimumField() {
        return m_jMinimum;
    }

    JTextField getMaximumField() {
        return m_jMaximum;
    }

    JTextField getQuantityField() {
        return m_jQuantity;
    }

    JLabel getTitleLabel() {
        return m_jTitle;
    }

    SwingBinder<ProductsWarehouseModel> getBinder() {
        return binder;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        m_jTitle = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        m_jQuantity = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        m_jMinimum = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        m_jMaximum = new javax.swing.JTextField();

        m_jTitle.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jTitle.setPreferredSize(new java.awt.Dimension(300, 30));

        jLabel3.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel3.setText(AppLocal.getIntString("label.units")); // NOI18N
        jLabel3.setPreferredSize(new java.awt.Dimension(110, 30));

        m_jQuantity.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jQuantity.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        m_jQuantity.setEnabled(false);
        m_jQuantity.setPreferredSize(new java.awt.Dimension(0, 30));

        jLabel4.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel4.setText(AppLocal.getIntString("label.minimum")); // NOI18N
        jLabel4.setPreferredSize(new java.awt.Dimension(110, 30));

        m_jMinimum.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jMinimum.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        m_jMinimum.setPreferredSize(new java.awt.Dimension(0, 30));

        jLabel5.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel5.setText(AppLocal.getIntString("label.maximum")); // NOI18N
        jLabel5.setPreferredSize(new java.awt.Dimension(110, 30));

        m_jMaximum.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jMaximum.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        m_jMaximum.setPreferredSize(new java.awt.Dimension(0, 30));

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
                        .addComponent(m_jMinimum, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(m_jMaximum, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(m_jQuantity, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(m_jTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 314, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(m_jTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(m_jQuantity, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(m_jMinimum, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(m_jMaximum, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JTextField m_jMaximum;
    private javax.swing.JTextField m_jMinimum;
    private javax.swing.JTextField m_jQuantity;
    private javax.swing.JLabel m_jTitle;
    // End of variables declaration//GEN-END:variables
    
}
