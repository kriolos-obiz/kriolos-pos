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
import com.openbravo.data.gui.ComboBoxValModel;
import com.openbravo.data.user.DirtyManager;
import com.openbravo.data.user.EditorRecord;
import com.openbravo.format.Formats;
import com.openbravo.pos.panels.AppView;
import com.openbravo.ui.data.binding.SwingBinder;
import java.awt.Component;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Editor panel for associating attributes to attribute sets, supporting real-time
 * validation, dirty state tracking, and headless testing via SwingBinder.
 *
 * @author adrianromero
 * @author KriolOS
 */
public class AttributeUseEditor extends javax.swing.JPanel implements EditorRecord {

    private static final long serialVersionUID = 1L;

    private AttributeService attributeService;
    private ComboBoxValModel attributemodel;

    private String id;
    private String attuseid;
    private String insertid;

    private final DirtyManager m_Dirty;
    private SwingBinder<AttributeUseModel> binder;

    /**
     * Mutable model adapting attribute use records for data binding.
     */
    public static final class AttributeUseModel {
        private String id;
        private String attributeSetId;
        private String attributeId;
        private String lineno;

        public AttributeUseModel() {
            this(null, null, null, null);
        }

        public AttributeUseModel(String id, String attributeSetId, String attributeId, String lineno) {
            this.id = id;
            this.attributeSetId = attributeSetId;
            this.attributeId = attributeId;
            this.lineno = lineno;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getAttributeSetId() {
            return attributeSetId;
        }

        public void setAttributeSetId(String attributeSetId) {
            this.attributeSetId = attributeSetId;
        }

        public String getAttributeId() {
            return attributeId;
        }

        public void setAttributeId(String attributeId) {
            this.attributeId = attributeId;
        }

        public String getLineno() {
            return lineno;
        }

        public void setLineno(String lineno) {
            this.lineno = lineno;
        }
    }

    /**
     * Headless constructor for testing without live AppView.
     *
     * @param dirty DirtyManager listener
     */
    public AttributeUseEditor(DirtyManager dirty) {
        this(null, dirty);
    }

    /**
     * Creates new form AttributeSetEditor
     * 
     * @param app
     * @param dirty
     */
    public AttributeUseEditor(AppView app, DirtyManager dirty) {
        this.m_Dirty = dirty;
        if (app != null) {
            attributeService = app.getBean(AttributeService.class);
            if (attributeService == null) {
                attributeService = app.getBean(DataLogicAttribute.class);
            }
        }
        attributemodel = new ComboBoxValModel();

        initComponents();

        if (dirty != null) {
            jLineno.getDocument().addDocumentListener(dirty);
            jAttribute.addActionListener(dirty);
        }

        initValidator();
        writeValueEOF();
    }

    private void initValidator() {
        binder = new SwingBinder<>(new AttributeUseModel());

        binder.forField(jLineno)
                .asRequired(AppLocal.getIntString("validation.required", AppLocal.getIntString("label.order")))
                .withValidator(s -> {
                    if (s == null || s.trim().isEmpty()) {
                        return false;
                    }
                    try {
                        Integer val = (Integer) Formats.INT.parseValue(s.trim());
                        return val != null && val >= 0;
                    } catch (Exception e) {
                        return false;
                    }
                }, AppLocal.getIntString("validation.number.invalid", AppLocal.getIntString("label.order")))
                .bind(AttributeUseModel::getLineno, AttributeUseModel::setLineno);

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

    javax.swing.JTextField getLinenoField() {
        return jLineno;
    }

    javax.swing.JComboBox getAttributeCombo() {
        return jAttribute;
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
     * @throws BasicException
     */
    public void activate() throws BasicException {
        List<AttributeInfo> list = attributeService != null ? attributeService.getAttributeList() : Collections.emptyList();
        attributemodel = new ComboBoxValModel(list);
        jAttribute.setModel(attributemodel);
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
        attuseid = null;
        attributemodel.setSelectedKey(null);
        jLineno.setText(null);

        jAttribute.setEnabled(false);
        jLineno.setEnabled(false);
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
        attuseid = (String) insertid;
        attributemodel.setSelectedKey(null);
        jLineno.setText(null);

        jAttribute.setEnabled(true);
        jLineno.setEnabled(true);
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
            attuseid = (String) obj[1];
            attributemodel.setSelectedKey(obj[2]);
            jLineno.setText(Formats.INT.formatValue((Integer) obj[3]));
        }

        jAttribute.setEnabled(true);
        jLineno.setEnabled(true);
        if (binder != null) {
            binder.readBean(new AttributeUseModel(id, attuseid, (String) attributemodel.getSelectedKey(), jLineno.getText()));
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
            attuseid = (String) obj[1];
            attributemodel.setSelectedKey(obj[2]);
            jLineno.setText(Formats.INT.formatValue((Integer) obj[3]));
        }

        jAttribute.setEnabled(false);
        jLineno.setEnabled(false);
        if (binder != null) {
            binder.readBean(new AttributeUseModel(id, attuseid, (String) attributemodel.getSelectedKey(), jLineno.getText()));
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
        AttributeUseModel model = new AttributeUseModel(id, attuseid, (String) attributemodel.getSelectedKey(), jLineno.getText());
        if (binder != null) {
            try {
                binder.writeBean(model);
            } catch (SwingBinder.ValidationException ex) {
                // validation state is surfaced via isValid()
            }
        }

        Object[] value = new Object[5];

        value[0] = id;
        value[1] = attuseid;
        value[2] = attributemodel.getSelectedKey();
        String lineText = jLineno.getText();
        if (lineText == null || lineText.trim().isEmpty()) {
            value[3] = null;
        } else {
            try {
                value[3] = Formats.INT.parseValue(lineText.trim());
            } catch (Exception ex) {
                try {
                    value[3] = Integer.parseInt(lineText.trim());
                } catch (Exception nfe) {
                    value[3] = null;
                }
            }
        }
        value[4] = attributemodel.getSelectedText();

        return value;
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated
    // Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel3 = new javax.swing.JLabel();
        jLineno = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jAttribute = new javax.swing.JComboBox();

        setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N

        jLabel3.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel3.setText(AppLocal.getIntString("label.order")); // NOI18N
        jLabel3.setPreferredSize(new java.awt.Dimension(110, 30));

        jLineno.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLineno.setPreferredSize(new java.awt.Dimension(110, 30));

        jLabel4.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel4.setText(AppLocal.getIntString("label.attribute")); // NOI18N
        jLabel4.setPreferredSize(new java.awt.Dimension(110, 30));

        jAttribute.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jAttribute.setPreferredSize(new java.awt.Dimension(0, 30));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(jLineno, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(jAttribute, javax.swing.GroupLayout.PREFERRED_SIZE, 170,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addContainerGap()));
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 25,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLineno, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jAttribute, javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap()));
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox jAttribute;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JTextField jLineno;
    // End of variables declaration//GEN-END:variables

}
