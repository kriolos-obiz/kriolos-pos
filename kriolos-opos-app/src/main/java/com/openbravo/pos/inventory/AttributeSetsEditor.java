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
 * Editor panel for product attribute sets, supporting real-time validation,
 * dirty state tracking, and headless testing via SwingBinder.
 *
 * @author  adrianromero
 * @author  KriolOS
 */
public final class AttributeSetsEditor extends javax.swing.JPanel implements EditorRecord {

    private static final long serialVersionUID = 1L;

    private String id;
    private final DirtyManager m_Dirty;
    private SwingBinder<AttributeSetInfo> binder;

    /** Creates new form AttributesEditor
     * @param dirty */
    public AttributeSetsEditor(DirtyManager dirty) {
        initComponents();
        this.m_Dirty = dirty;

        if (dirty != null) {
            m_jName.getDocument().addDocumentListener(dirty);
        }

        initValidator();
        writeValueEOF();
    }

    private void initValidator() {
        binder = new SwingBinder<>(new AttributeSetInfo());

        binder.forField(m_jName)
                .asRequired(AppLocal.getIntString("validation.required", AppLocal.getIntString("label.name")))
                .bind(AttributeSetInfo::getName, AttributeSetInfo::setName);

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

    javax.swing.JTextField getNameField() {
        return m_jName;
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
        m_jName.setText(null);
        m_jName.setEnabled(false);
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
        m_jName.setText(null);
        m_jName.setEnabled(true);
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
        if (value instanceof Object[] attrset) {
            id = (String) attrset[0];
            m_jName.setText(Formats.STRING.formatValue((String) attrset[1]));
        } else if (value instanceof AttributeSetInfo info) {
            id = info.getId();
            m_jName.setText(info.getName());
        }
        m_jName.setEnabled(false);
        if (binder != null) {
            binder.readBean(new AttributeSetInfo(id, m_jName.getText()));
        }
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
        if (value instanceof Object[] attrset) {
            id = (String) attrset[0];
            m_jName.setText(Formats.STRING.formatValue((String) attrset[1]));
        } else if (value instanceof AttributeSetInfo info) {
            id = info.getId();
            m_jName.setText(info.getName());
        }
        m_jName.setEnabled(true);
        if (binder != null) {
            binder.readBean(new AttributeSetInfo(id, m_jName.getText()));
        }
    }

    /**
     *
     * @return
     * @throws BasicException
     */
    @Override
    public Object createValue() throws BasicException {
        AttributeSetInfo info = new AttributeSetInfo(id, m_jName.getText());
        if (binder != null) {
            try {
                binder.writeBean(info);
            } catch (SwingBinder.ValidationException ex) {
                // validation status surfaced via isValid()
            }
        }

        Object[] attrset = new Object[2];
        attrset[0] = id;
        attrset[1] = m_jName.getText();
        return attrset;
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
     */
    @Override
    public void refresh() {
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
        m_jName = new javax.swing.JTextField();

        jLabel2.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel2.setText(AppLocal.getIntString("label.namem")); // NOI18N
        jLabel2.setPreferredSize(new java.awt.Dimension(110, 30));

        m_jName.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jName.setPreferredSize(new java.awt.Dimension(220, 30));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(m_jName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_jName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel2;
    private javax.swing.JTextField m_jName;
    // End of variables declaration//GEN-END:variables

}
