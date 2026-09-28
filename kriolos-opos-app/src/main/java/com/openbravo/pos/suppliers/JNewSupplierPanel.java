//    KriolOS POS
//    Copyright (c) 2019-2026 KriolOS
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

package com.openbravo.pos.suppliers;

import com.openbravo.basic.BasicException;
import com.openbravo.data.gui.MessageInf;
import com.openbravo.data.gui.modal.PosUIModal;
import com.openbravo.data.loader.LocalRes;
import com.openbravo.data.loader.TableDefinition;
import com.openbravo.data.user.DirtyManager;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.AppView;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;

public class JNewSupplierPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private DataLogicSuppliers dlSupplier;
    private TableDefinition tsuppliers;
    private SupplierInfoExt selectedSupplier;
    private SuppliersView suppliersView;
    private PosUIModal modalContext;

    public JNewSupplierPanel() {
        initComponents();
        initDomainAdapters();
    }

    public JNewSupplierPanel(AppView app) {
        init(app);
        initDomainAdapters();
    }

    private void initDomainAdapters() {
        setName("kriolos:suppliers:new-supplier-panel");
        m_jBtnOK.setName("kriolos:suppliers:btn-ok");
        m_jBtnCancel.setName("kriolos:suppliers:btn-cancel");
    }

    public void setModalContext(PosUIModal modalContext) {
        this.modalContext = modalContext;
    }

    public static SupplierInfoExt show(Component parent, AppView app) {
        JNewSupplierPanel panel = new JNewSupplierPanel(app);
        PosUIModal modal = PosUIModal.create(parent, panel)
                .setTitle(AppLocal.getIntString("label.supplier"))
                .setModal(true)
                .setResizable(false);
        panel.setModalContext(modal);
        modal.show();
        return panel.getSelectedSupplier();
    }

    private void init(AppView app) {
        dlSupplier = (DataLogicSuppliers) app.getBean("com.openbravo.pos.suppliers.DataLogicSuppliers");
        tsuppliers = dlSupplier.getTableSuppliers();

        initComponents();

        DirtyManager dirty = new DirtyManager();
        suppliersView = new SuppliersView(app, dirty);
        suppliersView.writeValueInsert();
        jFormPanel.setLayout(new BorderLayout());
        jFormPanel.add(suppliersView, BorderLayout.CENTER);

        if (getRootPane() != null) {
            getRootPane().setDefaultButton(m_jBtnOK);
        }
    }

    public Object createValue() throws BasicException {
        return suppliersView != null ? suppliersView.createValue() : null;
    }

    public SupplierInfoExt getSelectedSupplier() {
        return selectedSupplier;
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jFormPanel = new JPanel();
        jPanel2 = new JPanel();
        m_jBtnCancel = new JButton();
        m_jBtnOK = new JButton();

        setLayout(new BorderLayout());

        jFormPanel.setFont(new Font("Arial", 0, 14)); // NOI18N
        jFormPanel.setPreferredSize(new Dimension(660, 310));
        jFormPanel.setLayout(new BorderLayout());
        add(jFormPanel, BorderLayout.NORTH);
        jFormPanel.getAccessibleContext().setAccessibleName("");

        jPanel2.setLayout(new FlowLayout(FlowLayout.RIGHT));

        m_jBtnCancel.setFont(new Font("Arial", 0, 14)); // NOI18N
        m_jBtnCancel.setIcon(new ImageIcon(getClass().getResource("/com/openbravo/images/cancel.png"))); // NOI18N
        m_jBtnCancel.setText(AppLocal.getIntString("button.cancel")); // NOI18N
        m_jBtnCancel.setFocusPainted(false);
        m_jBtnCancel.setFocusable(false);
        m_jBtnCancel.setMargin(new Insets(8, 16, 8, 16));
        m_jBtnCancel.setPreferredSize(new Dimension(80, 45));
        m_jBtnCancel.setRequestFocusEnabled(false);
        m_jBtnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jBtnCancelActionPerformed(evt);
            }
        });
        jPanel2.add(m_jBtnCancel);

        m_jBtnOK.setFont(new Font("Arial", 0, 14)); // NOI18N
        m_jBtnOK.setIcon(new ImageIcon(getClass().getResource("/com/openbravo/images/ok.png"))); // NOI18N
        m_jBtnOK.setText(AppLocal.getIntString("button.ok")); // NOI18N
        m_jBtnOK.setFocusPainted(false);
        m_jBtnOK.setFocusable(false);
        m_jBtnOK.setMargin(new Insets(8, 16, 8, 16));
        m_jBtnOK.setPreferredSize(new Dimension(80, 45));
        m_jBtnOK.setRequestFocusEnabled(false);
        m_jBtnOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jBtnOKActionPerformed(evt);
            }
        });
        jPanel2.add(m_jBtnOK);

        add(jPanel2, BorderLayout.SOUTH);
    }// </editor-fold>//GEN-END:initComponents

    private void m_jBtnOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jBtnOKActionPerformed
        try {
            Object supplier = createValue();
            String m_oId = ((Object[]) supplier)[0].toString();

            int status = tsuppliers.getInsertSentence().exec(supplier);

            if (status > 0) {
                selectedSupplier = dlSupplier.loadSupplierExt(m_oId);
                if (modalContext != null) {
                    modalContext.setResult(selectedSupplier);
                    modalContext.close();
                }
            } else {
                MessageInf msg = new MessageInf(MessageInf.SGN_NOTICE,
                        LocalRes.getIntString("message.nosave"), "Error save");
                msg.show(this);
            }
        } catch (BasicException ex) {
            MessageInf msg = new MessageInf(MessageInf.SGN_NOTICE,
                    LocalRes.getIntString("message.nosave"), ex);
            msg.show(this);
        }
    }//GEN-LAST:event_m_jBtnOKActionPerformed

    private void m_jBtnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jBtnCancelActionPerformed
        selectedSupplier = null;
        if (modalContext != null) {
            modalContext.close();
        }
    }//GEN-LAST:event_m_jBtnCancelActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JPanel jFormPanel;
    private JPanel jPanel2;
    private JButton m_jBtnCancel;
    private JButton m_jBtnOK;
    // End of variables declaration//GEN-END:variables
}
