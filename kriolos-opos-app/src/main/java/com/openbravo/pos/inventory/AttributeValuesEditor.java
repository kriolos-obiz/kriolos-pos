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
import com.openbravo.data.user.DirtyManager;
import com.openbravo.data.user.EditorRecord;
import com.openbravo.format.Formats;
import com.openbravo.ui.data.binding.SwingBinder;
import java.awt.Component;
import java.util.UUID;

/**
 * Editor panel for product attribute values, supporting real-time validation,
 * dirty state tracking, and headless testing via SwingBinder.
 *
 * @author adrian
 * @author KriolOS
 */
public class AttributeValuesEditor extends javax.swing.JPanel implements EditorRecord {

    private static final long serialVersionUID = 1L;

    private String id;
    private String attid;
    private String insertid;

    private final DirtyManager m_Dirty;
    private SwingBinder<AttributeValueModel> binder;

    /**
     * Mutable model adapting attribute value records for data binding.
     */
    public static final class AttributeValueModel {
        private String id;
        private String attributeId;
        private String value;

        public AttributeValueModel() {
            this(null, null, null);
        }

        public AttributeValueModel(String id, String attributeId, String value) {
            this.id = id;
            this.attributeId = attributeId;
            this.value = value;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getAttributeId() {
            return attributeId;
        }

        public void setAttributeId(String attributeId) {
            this.attributeId = attributeId;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    /** Creates new form AttributesValuesEditor
     * @param dirty */
    public AttributeValuesEditor(DirtyManager dirty) {
        initComponents();
        this.m_Dirty = dirty;

        if (dirty != null) {
            jValue.getDocument().addDocumentListener(dirty);
        }

        initValidator();
        writeValueEOF();
    }

    private void initValidator() {
        binder = new SwingBinder<>(new AttributeValueModel());

        binder.forField(jValue)
                .asRequired(AppLocal.getIntString("validation.required", AppLocal.getIntString("label.value")))
                .bind(AttributeValueModel::getValue, AttributeValueModel::setValue);

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

    javax.swing.JTextField getValueField() {
        return jValue;
    }

    /**
     *
     * @param insertid
     */
    public void setInsertId(String insertid) {
        this.insertid = insertid;
    }

    /**
     *
     */
    @Override
    public void refresh() {
    }

    /**
     *
     */
    @Override
    public void writeValueEOF() {
        if (binder != null) {
            binder.clear();
        }
        id = null;
        attid = null;
        jValue.setText(null);
        jValue.setEnabled(false);
    }

    /**
     *
     */
    @Override
    public void writeValueInsert() {
        if (binder != null) {
            binder.clear();
        }
        id = UUID.randomUUID().toString();
        attid = insertid;
        jValue.setText(null);
        jValue.setEnabled(true);
    }

    /**
     *
     * @param value
     */
    @Override
    public void writeValueEdit(Object value) {
        if (value == null) {
            writeValueEOF();
            return;
        }
        if (value instanceof Object[] obj) {
            id = (String) obj[0];
            attid = (String) obj[1];
            jValue.setText(Formats.STRING.formatValue((String) obj[2]));
        }
        jValue.setEnabled(true);
        if (binder != null) {
            binder.readBean(new AttributeValueModel(id, attid, jValue.getText()));
        }
    }

    /**
     *
     * @param value
     */
    @Override
    public void writeValueDelete(Object value) {
        if (value == null) {
            writeValueEOF();
            return;
        }
        if (value instanceof Object[] obj) {
            id = (String) obj[0];
            attid = (String) obj[1];
            jValue.setText(Formats.STRING.formatValue((String) obj[2]));
        }
        jValue.setEnabled(false);
        if (binder != null) {
            binder.readBean(new AttributeValueModel(id, attid, jValue.getText()));
        }
    }

    /**
     *
     * @return
     */
    @Override
    public Component getComponent() {
        return this;
    }

    /**
     *
     * @return
     * @throws BasicException
     */
    @Override
    public Object createValue() throws BasicException {
        AttributeValueModel model = new AttributeValueModel(id, attid, jValue.getText());
        if (binder != null) {
            try {
                binder.writeBean(model);
            } catch (SwingBinder.ValidationException ex) {
                // validation status surfaced via isValid()
            }
        }
        return new Object[] {
            id,
            attid,
            Formats.STRING.formatValue(jValue.getText())
        };
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel2 = new javax.swing.JLabel();
        jValue = new javax.swing.JTextField();

        jLabel2.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel2.setText(AppLocal.getIntString("label.value")); // NOI18N
        jLabel2.setPreferredSize(new java.awt.Dimension(110, 30));

        jValue.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jValue.setPreferredSize(new java.awt.Dimension(220, 30));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jValue, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel2;
    private javax.swing.JTextField jValue;
    // End of variables declaration//GEN-END:variables


}
