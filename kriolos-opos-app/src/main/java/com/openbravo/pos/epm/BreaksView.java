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

package com.openbravo.pos.epm;

import com.openbravo.basic.AppLocal;
import com.openbravo.basic.BasicException;
import com.openbravo.data.user.DirtyManager;
import com.openbravo.data.user.EditorRecord;
import com.openbravo.pos.panels.AppView;
import com.openbravo.ui.data.binding.SwingBinder;
import java.awt.Component;
import java.util.List;
import java.util.UUID;

/**
 * Strongly typed editor panel for employee break configurations, supporting
 * two-way Swing data binding, headless testing, and real-time validation.
 *
 * @author Ali Safdar and Aneeqa Baber
 * @author KriolOS
 */
public final class BreaksView extends javax.swing.JPanel implements EditorRecord<BreaksInfo> {

    private static final long serialVersionUID = 1L;

    private String m_oId;
    private final ShiftService shiftService;
    private DirtyManager m_Dirty;
    private SwingBinder<BreaksInfo> binder;

    /**
     * Headless constructor for testing without AppView.
     *
     * @param dirty DirtyManager listener
     */
    public BreaksView(DirtyManager dirty) {
        this(null, dirty, null);
    }

    /**
     * Creates new form BreaksView
     *
     * @param app
     * @param dirty
     */
    public BreaksView(AppView app, DirtyManager dirty) {
        this(app, dirty, null);
    }

    public BreaksView(AppView app, DirtyManager dirty, ShiftService shiftService) {
        this.shiftService = shiftService != null ? shiftService : (app != null ? app.getBean(ShiftService.class) : null);
        initComponents();
        this.m_Dirty = dirty;
        if (dirty != null) {
            m_jBreakName.getDocument().addDocumentListener(dirty);
            m_jVisible.addActionListener(dirty);
            m_jBreakDescription.getDocument().addDocumentListener(dirty);
        }
        initValidator();
        writeValueEOF();
    }

    private void initValidator() {
        binder = new SwingBinder<>(new BreaksInfo());

        binder.forField(m_jBreakName)
                .asRequired(AppLocal.getIntString("validation.required", AppLocal.getIntString("label.name")))
                .bind(BreaksInfo::getName, BreaksInfo::setName);

        binder.forField(m_jBreakDescription)
                .bind(b -> b != null && b.getNotes() != null ? b.getNotes() : "", BreaksInfo::setNotes);

        binder.forField(m_jVisible)
                .bind(BreaksInfo::isVisible, BreaksInfo::setVisible);

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

    javax.swing.JTextField getBreakNameField() {
        return m_jBreakName;
    }

    javax.swing.JTextArea getBreakDescriptionArea() {
        return m_jBreakDescription;
    }

    javax.swing.JCheckBox getVisibleCheckBox() {
        return m_jVisible;
    }

    void activate() throws BasicException {
        if (shiftService != null) {
            List<BreaksInfo> a = shiftService.getBreaksListAll();
            a.add(0, null);
        }
    }

    /**
     *
     */
    @Override
    public void writeValueEOF() {
        if (binder != null) {
            binder.clear();
        }
        m_oId = null;
        m_jBreakName.setText(null);
        m_jBreakDescription.setText(null);
        m_jVisible.setSelected(false);
        m_jBreakName.setEditable(false);
        m_jBreakDescription.setEnabled(false);
        m_jVisible.setEnabled(false);
    }

    /**
     *
     */
    @Override
    public void writeValueInsert() {
        if (binder != null) {
            binder.clear();
        }
        m_oId = null;
        m_jBreakName.setText(null);
        m_jBreakDescription.setText(null);
        m_jVisible.setSelected(true);
        m_jBreakName.setEditable(true);
        m_jBreakDescription.setEnabled(true);
        m_jVisible.setEnabled(true);
    }

    /**
     *
     * @param breaks
     */
    @Override
    public void writeValueEdit(BreaksInfo breaks) {
        if (breaks == null) {
            writeValueEOF();
            return;
        }
        m_oId = breaks.getId();
        m_jBreakName.setText(breaks.getName());
        m_jBreakDescription.setText(breaks.getNotes());
        m_jVisible.setSelected(breaks.isVisible());
        m_jBreakName.setEditable(true);
        m_jBreakDescription.setEnabled(true);
        m_jVisible.setEnabled(true);
        if (binder != null) {
            binder.readBean(breaks);
        }
    }

    /**
     *
     * @param breaks
     */
    @Override
    public void writeValueDelete(BreaksInfo breaks) {
        if (breaks == null) {
            writeValueEOF();
            return;
        }
        m_oId = breaks.getId();
        m_jBreakName.setText(breaks.getName());
        m_jBreakDescription.setText(breaks.getNotes());
        m_jVisible.setSelected(breaks.isVisible());
        m_jBreakName.setEditable(false);
        m_jBreakDescription.setEnabled(false);
        m_jVisible.setEnabled(false);
        if (binder != null) {
            binder.readBean(breaks);
        }
    }

    /**
     *
     */
    @Override
    public void refresh() {
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
    public BreaksInfo createValue() throws BasicException {
        BreaksInfo info = new BreaksInfo(
            m_oId == null ? UUID.randomUUID().toString() : m_oId,
            m_jBreakName.getText(),
            m_jBreakDescription.getText(),
            m_jVisible.isSelected()
        );
        if (binder != null) {
            try {
                binder.writeBean(info);
            } catch (SwingBinder.ValidationException ex) {
                // validation state is surfaced by binder.validate() / isValid()
            }
        }
        return info;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        m_jBreakName = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        m_jBreakDescription = new javax.swing.JTextArea();
        m_jVisible = new javax.swing.JCheckBox();
        m_Ncx = new javax.swing.JLabel();
        m_Name3 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();

        m_jBreakName.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N

        m_jBreakDescription.setColumns(20);
        m_jBreakDescription.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        m_jBreakDescription.setLineWrap(true);
        m_jBreakDescription.setRows(5);
        jScrollPane1.setViewportView(m_jBreakDescription);

        m_jVisible.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N

        m_Ncx.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        m_Ncx.setText(AppLocal.getIntString("label.epm.visible")); // NOI18N

        m_Name3.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        m_Name3.setText(AppLocal.getIntString("label.epm.notes")); // NOI18N

        jLabel1.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("pos_messages"); // NOI18N
        jLabel1.setText(bundle.getString("label.epm.employee")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 126, Short.MAX_VALUE)
                    .addComponent(m_Name3, javax.swing.GroupLayout.DEFAULT_SIZE, 126, Short.MAX_VALUE)
                    .addComponent(m_Ncx, javax.swing.GroupLayout.DEFAULT_SIZE, 126, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(m_jVisible)
                    .addComponent(m_jBreakName)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 223, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(214, 214, 214))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(m_jBreakName, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(m_Ncx, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(m_Name3, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(m_jVisible, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(35, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel m_Name3;
    private javax.swing.JLabel m_Ncx;
    private javax.swing.JTextArea m_jBreakDescription;
    private javax.swing.JTextField m_jBreakName;
    private javax.swing.JCheckBox m_jVisible;
    // End of variables declaration//GEN-END:variables
}
