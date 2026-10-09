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
package com.openbravo.data.gui;

import com.openbravo.basic.BasicException;
import com.openbravo.data.gui.modal.PosUIModal;
import com.openbravo.data.loader.ComparatorCreator;
import com.openbravo.pos.forms.AppLocal;
import java.awt.Component;
import java.util.Comparator;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;

/**
 * Multi-column sort selection panel presentable via {@link PosUIModal} or embedded directly into views.
 *
 * @param <T> data type sorted
 * @author KriolOS
 */
public class JSortPanel<T> extends JPanel {

    private static final long serialVersionUID = 1L;

    private ComparatorCreator<T> m_cc;
    private Comparator<T> m_Comparator;
    private PosUIModal modalContext;
    private boolean accepted = false;

    /**
     * Creates new form JSortPanel
     */
    public JSortPanel() {
        initComponents();
        initDomainAdapters();
    }

    /**
     * Creates and initializes form JSortPanel with comparator creator.
     *
     * @param cc comparator creator
     */
    public JSortPanel(ComparatorCreator<T> cc) {
        initComponents();
        init(cc);
        initDomainAdapters();
    }

    private void initDomainAdapters() {
        setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8));

        setName("kriolos:sort:ordering-panel");
        m_jSort1.setName("kriolos:sort:combo-column1");
        m_jSort2.setName("kriolos:sort:combo-column2");
        m_jSort3.setName("kriolos:sort:combo-column3");
        jcmdOK.setName("kriolos:sort:btn-ok");
        jcmdCancel.setName("kriolos:sort:btn-cancel");
    }

    public final void init(ComparatorCreator<T> cc) {
        m_cc = cc;
        if (m_cc != null) {
            String[] sHeaders = m_cc.getHeaders();

            m_jSort1.removeAllItems();
            m_jSort1.addItem("");
            for (String header : sHeaders) {
                m_jSort1.addItem(header);
            }
            m_jSort1.setSelectedIndex(0);

            m_jSort2.removeAllItems();
            m_jSort2.addItem("");
            for (String header : sHeaders) {
                m_jSort2.addItem(header);
            }
            m_jSort2.setSelectedIndex(0);

            m_jSort3.removeAllItems();
            m_jSort3.addItem("");
            for (String header : sHeaders) {
                m_jSort3.addItem(header);
            }
            m_jSort3.setSelectedIndex(0);
        }
        m_Comparator = null;
        accepted = false;
    }

    @Override
    public void addNotify() {
        super.addNotify();
        JRootPane root = SwingUtilities.getRootPane(this);
        if (root != null) {
            root.setDefaultButton(jcmdOK);
        }
    }

    /**
     * Attaches the controlling {@link PosUIModal} context.
     *
     * @param modalContext the modal context
     */
    public void setModalContext(PosUIModal modalContext) {
        this.modalContext = modalContext;
    }

    public String getTitle() {
        return AppLocal.getIntString("caption.sort");
    }

    public JButton getOkButton() {
        return jcmdOK;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public Comparator<T> getComparator() {
        return accepted ? m_Comparator : null;
    }

    private void handleAccept() {
        if (m_cc != null) {
            m_Comparator = m_cc.createComparator(new int[]{
                m_jSort1.getSelectedIndex() - 1,
                m_jSort2.getSelectedIndex() - 1,
                m_jSort3.getSelectedIndex() - 1
            });
        }
        this.accepted = true;

        if (modalContext != null) {
            modalContext.setResult(m_Comparator);
            modalContext.close();
        }
    }

    private void handleCancel() {
        this.m_Comparator = null;
        this.accepted = false;

        if (modalContext != null) {
            modalContext.setResult(null);
            modalContext.close();
        }
    }

    /**
     * Displays the sort panel dialog using {@link PosUIModal}.
     *
     * @param <T> data type
     * @param parent the parent component
     * @param cc the comparator creator
     * @return selected comparator or null if canceled
     * @throws BasicException on error
     */
    public static <T> Comparator<T> showMessage(Component parent, ComparatorCreator<T> cc) throws BasicException {
        JSortPanel<T> panel = new JSortPanel<>(cc);
        PosUIModal modal = PosUIModal.create(parent, panel)
                .setTitle(panel.getTitle())
                .setModal(true)
                .setResizable(false);

        panel.setModalContext(modal);
        modal.show();

        return panel.getComparator();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        m_jSort1 = new javax.swing.JComboBox<>();
        jLabel3 = new javax.swing.JLabel();
        m_jSort2 = new javax.swing.JComboBox<>();
        jLabel4 = new javax.swing.JLabel();
        m_jSort3 = new javax.swing.JComboBox<>();
        jPanel2 = new javax.swing.JPanel();
        jcmdCancel = new javax.swing.JButton();
        jcmdOK = new javax.swing.JButton();

        setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        setLayout(new java.awt.BorderLayout());

        jLabel2.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel2.setText(AppLocal.getIntString("label.sortby")); // NOI18N
        jLabel2.setPreferredSize(new java.awt.Dimension(150, 30));

        m_jSort1.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jSort1.setPreferredSize(new java.awt.Dimension(200, 30));

        jLabel3.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel3.setText(AppLocal.getIntString("label.andby")); // NOI18N
        jLabel3.setPreferredSize(new java.awt.Dimension(150, 30));

        m_jSort2.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jSort2.setPreferredSize(new java.awt.Dimension(200, 30));

        jLabel4.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel4.setText(AppLocal.getIntString("label.andby")); // NOI18N
        jLabel4.setPreferredSize(new java.awt.Dimension(150, 30));

        m_jSort3.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jSort3.setPreferredSize(new java.awt.Dimension(200, 30));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(m_jSort1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(m_jSort2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(m_jSort3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_jSort1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_jSort2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(m_jSort3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        add(jPanel1, java.awt.BorderLayout.CENTER);

        jPanel2.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        jcmdCancel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jcmdCancel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/cancel.png"))); // NOI18N
        jcmdCancel.setText(AppLocal.getIntString("button.cancel")); // NOI18N
        jcmdCancel.setPreferredSize(new java.awt.Dimension(110, 45));
        jcmdCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jcmdCancelActionPerformed(evt);
            }
        });
        jPanel2.add(jcmdCancel);

        jcmdOK.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jcmdOK.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/ok.png"))); // NOI18N
        jcmdOK.setText(AppLocal.getIntString("button.ok")); // NOI18N
        jcmdOK.setMaximumSize(new java.awt.Dimension(65, 33));
        jcmdOK.setMinimumSize(new java.awt.Dimension(65, 33));
        jcmdOK.setPreferredSize(new java.awt.Dimension(110, 45));
        jcmdOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jcmdOKActionPerformed(evt);
            }
        });
        jPanel2.add(jcmdOK);

        add(jPanel2, java.awt.BorderLayout.SOUTH);
    }// </editor-fold>//GEN-END:initComponents

    private void jcmdCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jcmdCancelActionPerformed
        handleCancel();
    }//GEN-LAST:event_jcmdCancelActionPerformed

    private void jcmdOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jcmdOKActionPerformed
        handleAccept();
    }//GEN-LAST:event_jcmdOKActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JButton jcmdCancel;
    private javax.swing.JButton jcmdOK;
    private javax.swing.JComboBox<String> m_jSort1;
    private javax.swing.JComboBox<String> m_jSort2;
    private javax.swing.JComboBox<String> m_jSort3;
    // End of variables declaration//GEN-END:variables
}
