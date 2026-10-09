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
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.data.loader.Vectorer;
import java.awt.Component;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;

/**
 * Search and find query panel presentable via {@link PosUIModal} or embedded directly into views.
 *
 * @param <T> data type searched
 * @author KriolOS
 */
public class JFindPanel<T> extends JPanel {

    private static final long serialVersionUID = 1L;

    private FindInfo<T> m_FindInfo;
    private Vectorer<T> m_vec;
    private PosUIModal modalContext;
    private boolean accepted = false;

    /**
     * Creates new form JFindPanel
     */
    public JFindPanel() {
        initComponents();
        initDomainAdapters();
    }

    /**
     * Creates and initializes form JFindPanel with search metadata.
     *
     * @param lastFindInfo previous search parameters
     */
    public JFindPanel(FindInfo<T> lastFindInfo) throws BasicException {
        initComponents();
        init(lastFindInfo);
        initDomainAdapters();
    }

    private void initDomainAdapters() {
        setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8));

        setName("kriolos:find:search-panel");
        m_jFind.setName("kriolos:find:input-text");
        m_jWhere.setName("kriolos:find:combo-field");
        m_jMatch.setName("kriolos:find:combo-match");
        m_jMatchCase.setName("kriolos:find:chk-matchcase");
        jcmdOK.setName("kriolos:find:btn-ok");
        jcmdCancel.setName("kriolos:find:btn-cancel");
    }

    @SuppressWarnings("unchecked")
    public final void init(FindInfo<T> lastFindInfo) throws BasicException {
        if (lastFindInfo != null) {
            m_jFind.setText(lastFindInfo.getText());
            m_jWhere.removeAllItems();
            for (String header : lastFindInfo.getVectorer().getHeaders()) {
                m_jWhere.addItem(header);
            }
            if (lastFindInfo.getField() >= 0 && lastFindInfo.getField() < m_jWhere.getItemCount()) {
                m_jWhere.setSelectedIndex(lastFindInfo.getField());
            }

            m_jMatch.removeAllItems();
            m_jMatch.addItem(AppLocal.getIntString("list.startfield"));
            m_jMatch.addItem(AppLocal.getIntString("list.wholefield"));
            m_jMatch.addItem(AppLocal.getIntString("list.anypart"));
            m_jMatch.addItem(AppLocal.getIntString("list.re"));
            m_jMatch.setSelectedIndex(lastFindInfo.getMatch());

            m_jMatchCase.setSelected(lastFindInfo.isMatchCase());
            m_vec = lastFindInfo.getVectorer();
        }
        m_FindInfo = null;
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
        return AppLocal.getIntString("title.find");
    }

    public JButton getOkButton() {
        return jcmdOK;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public FindInfo<T> getFindInfo() {
        return accepted ? m_FindInfo : null;
    }

    private void handleAccept() {
        m_FindInfo = new FindInfo<>(
                m_vec,
                m_jFind.getText(),
                m_jWhere.getSelectedIndex(),
                m_jMatchCase.isSelected(),
                m_jMatch.getSelectedIndex()
        );
        this.accepted = true;

        if (modalContext != null) {
            modalContext.setResult(m_FindInfo);
            modalContext.close();
        }
    }

    private void handleCancel() {
        this.m_FindInfo = null;
        this.accepted = false;

        if (modalContext != null) {
            modalContext.setResult(null);
            modalContext.close();
        }
    }

    /**
     * Displays the search dialog using {@link PosUIModal}.
     *
     * @param <T> data type
     * @param parent the parent component
     * @param lastFindInfo the initial search metadata
     * @return updated search parameters or null if canceled
     * @throws BasicException on error
     */
    public static <T> FindInfo<T> showMessage(Component parent, FindInfo<T> lastFindInfo) throws BasicException {
        JFindPanel<T> panel = new JFindPanel<>(lastFindInfo);
        PosUIModal modal = PosUIModal.create(parent, panel)
                .setTitle(panel.getTitle())
                .setModal(true)
                .setResizable(false);

        panel.setModalContext(modal);
        modal.show();

        return panel.getFindInfo();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        m_jFind = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        m_jWhere = new javax.swing.JComboBox<>();
        jLabel3 = new javax.swing.JLabel();
        m_jMatch = new javax.swing.JComboBox<>();
        m_jMatchCase = new javax.swing.JCheckBox();
        jPanel2 = new javax.swing.JPanel();
        jcmdCancel = new javax.swing.JButton();
        jcmdOK = new javax.swing.JButton();

        setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        setLayout(new java.awt.BorderLayout());

        jLabel1.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel1.setText(AppLocal.getIntString("label.findwhat")); // NOI18N
        jLabel1.setPreferredSize(new java.awt.Dimension(150, 30));

        m_jFind.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jFind.setPreferredSize(new java.awt.Dimension(250, 30));

        jLabel2.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel2.setText(AppLocal.getIntString("label.where")); // NOI18N
        jLabel2.setPreferredSize(new java.awt.Dimension(150, 30));

        m_jWhere.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jWhere.setPreferredSize(new java.awt.Dimension(250, 30));

        jLabel3.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel3.setText(AppLocal.getIntString("label.match")); // NOI18N
        jLabel3.setPreferredSize(new java.awt.Dimension(150, 30));

        m_jMatch.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jMatch.setPreferredSize(new java.awt.Dimension(250, 30));

        m_jMatchCase.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jMatchCase.setText(AppLocal.getIntString("label.casesensitive")); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addComponent(m_jFind, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addComponent(m_jWhere, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(m_jMatchCase, javax.swing.GroupLayout.PREFERRED_SIZE, 230, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(m_jMatch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_jFind, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_jWhere, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_jMatch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(m_jMatchCase)
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
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JButton jcmdCancel;
    private javax.swing.JButton jcmdOK;
    private javax.swing.JTextField m_jFind;
    private javax.swing.JComboBox<String> m_jMatch;
    private javax.swing.JCheckBox m_jMatchCase;
    private javax.swing.JComboBox<String> m_jWhere;
    // End of variables declaration//GEN-END:variables
}
