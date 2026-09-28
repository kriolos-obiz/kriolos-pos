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
import com.openbravo.data.gui.modal.PosUIModal;
import com.openbravo.data.loader.QBFCompareEnum;
import com.openbravo.data.user.EditorCreator;
import com.openbravo.data.user.ListProvider;
import com.openbravo.data.user.ListProviderCreator;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.AppView;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.LayoutStyle;

public class JSupplierFinderPanel extends JPanel implements EditorCreator {

    private static final long serialVersionUID = 1L;

    private SupplierInfo m_ReturnSupplier;
    private ListProvider lpr;
    private AppView appView;
    private PosUIModal modalContext;

    public JSupplierFinderPanel() {
        initComponents();
        initDomainAdapters();
    }

    public JSupplierFinderPanel(DataLogicSuppliers dlSuppliers) {
        init(dlSuppliers);
        initDomainAdapters();
    }

    private void initDomainAdapters() {
        setName("kriolos:suppliers:finder-panel");
        jbtnExecute.setName("kriolos:suppliers:btn-execute");
        jbtnReset.setName("kriolos:suppliers:btn-reset");
        jcmdOK.setName("kriolos:suppliers:btn-ok");
        jcmdCancel.setName("kriolos:suppliers:btn-cancel");
        jListSuppliers.setName("kriolos:suppliers:list-suppliers");
    }

    public void setModalContext(PosUIModal modalContext) {
        this.modalContext = modalContext;
    }

    public static SupplierInfo show(Component parent, DataLogicSuppliers dlSuppliers) {
        return show(parent, dlSuppliers, null);
    }

    public static SupplierInfo show(Component parent, DataLogicSuppliers dlSuppliers, SupplierInfo initialSupplier) {
        JSupplierFinderPanel panel = new JSupplierFinderPanel(dlSuppliers);
        if (initialSupplier != null) {
            panel.search(initialSupplier);
        }
        PosUIModal modal = PosUIModal.create(parent, panel)
                .setTitle(AppLocal.getIntString("form.customertitle"))
                .setModal(true)
                .setResizable(true);
        panel.setModalContext(modal);
        modal.show();
        return panel.getSelectedSupplier();
    }

    public void searchKey() {
        jbtnExecute.setMnemonic(KeyEvent.VK_E);
        executeSearch();
    }

    public void resetKey() {
        jbtnReset.setMnemonic(KeyEvent.VK_R);
        m_jtxtTaxID.reset();
        m_jtxtSearchKey.reset();
        m_jtxtName.reset();
        m_jtxtPostal.reset();
        m_jtxtPhone.reset();
        m_jtxtName2.reset();

        m_jtxtTaxID.activate();

        cleanSearch();
    }

    public void setAppView(AppView appView) {
        this.appView = appView;
    }

    public SupplierInfo getSelectedSupplier() {
        return m_ReturnSupplier;
    }

    private void init(DataLogicSuppliers dlSuppliers) {
        initComponents();

        jImageViewerSupplier.setVisible(false);

        jScrollPane1.getVerticalScrollBar().setPreferredSize(new Dimension(35, 35));

        m_jtxtTaxID.addEditorKeys(m_jKeys);
        m_jtxtSearchKey.addEditorKeys(m_jKeys);
        m_jtxtName.addEditorKeys(m_jKeys);
        m_jtxtPostal.addEditorKeys(m_jKeys);
        m_jtxtPhone.addEditorKeys(m_jKeys);
        m_jtxtName2.addEditorKeys(m_jKeys);

        m_jtxtTaxID.reset();
        m_jtxtSearchKey.reset();
        m_jtxtName.reset();
        m_jtxtPostal.reset();
        m_jtxtPhone.reset();
        m_jtxtName2.reset();

        m_jtxtTaxID.activate();

        lpr = new ListProviderCreator(dlSuppliers.getSupplierList(), this);

        jListSuppliers.setCellRenderer(new SupplierRenderer());

        if (getRootPane() != null) {
            getRootPane().setDefaultButton(jcmdOK);
        }

        m_ReturnSupplier = null;
    }

    public void search(SupplierInfo supplier) {
        if (supplier == null || supplier.getName() == null || supplier.getName().equals("")) {
            m_jtxtTaxID.reset();
            m_jtxtSearchKey.reset();
            m_jtxtName.reset();
            m_jtxtPostal.reset();
            m_jtxtPhone.reset();
            m_jtxtName2.reset();

            m_jtxtTaxID.activate();

            cleanSearch();
        } else {
            m_jtxtTaxID.setText(supplier.getTaxid());
            m_jtxtSearchKey.setText(supplier.getSearchkey());
            m_jtxtName.setText(supplier.getName());
            m_jtxtPostal.setText(supplier.getPostal());
            m_jtxtPhone.setText(supplier.getPhone());
            m_jtxtName2.setText(supplier.getEmail());

            m_jtxtTaxID.activate();

            executeSearch();
        }
    }

    private void cleanSearch() {
        m_jtxtTaxID.setText("");
        m_jtxtSearchKey.setText("");
        m_jtxtName.setText("");
        m_jtxtPostal.setText("");
        m_jtxtPhone.setText("");
        m_jtxtName2.setText("");

        jListSuppliers.setModel(new MyListData(new ArrayList()));
    }

    public void executeSearch() {
        try {
            jListSuppliers.setModel(new MyListData(lpr.loadData()));
            if (jListSuppliers.getModel().getSize() > 0) {
                jListSuppliers.setSelectedIndex(0);
            } else {
                if (!m_jtxtName.getText().equals("")) {
                    int n = JOptionPane.showConfirmDialog(
                            null,
                            AppLocal.getIntString("message.suppliernotfound"),
                            AppLocal.getIntString("title.editor"),
                            JOptionPane.YES_NO_OPTION);

                    if (n != 1) {
                        if (modalContext != null) {
                            modalContext.close();
                        }
                        if (appView != null && appView.getAppUserView() != null) {
                            appView.getAppUserView().showTask("com.openbravo.pos.suppliers.SuppliersPanel");
                        }
                        JOptionPane.showMessageDialog(this,
                                "You must complete Account and Search Key Then Save to add to Ticket",
                                "Create Supplier", JOptionPane.OK_OPTION);
                    }
                }
            }
        } catch (BasicException e) {
        }
    }

    @Override
    public Object createValue() throws BasicException {
        Object[] afilter = new Object[12];

        // TaxID
        if (m_jtxtTaxID.getText() == null || m_jtxtTaxID.getText().equals("")) {
            afilter[0] = QBFCompareEnum.COMP_NONE;
            afilter[1] = null;
        } else {
            afilter[0] = QBFCompareEnum.COMP_RE;
            afilter[1] = "%" + m_jtxtTaxID.getText() + "%";
        }

        // SearchKey
        if (m_jtxtSearchKey.getText() == null || m_jtxtSearchKey.getText().equals("")) {
            afilter[2] = QBFCompareEnum.COMP_NONE;
            afilter[3] = null;
        } else {
            afilter[2] = QBFCompareEnum.COMP_RE;
            afilter[3] = "%" + m_jtxtSearchKey.getText() + "%";
        }

        // Name
        if (m_jtxtName.getText() == null || m_jtxtName.getText().equals("")) {
            afilter[4] = QBFCompareEnum.COMP_NONE;
            afilter[5] = null;
        } else {
            afilter[4] = QBFCompareEnum.COMP_RE;
            afilter[5] = "%" + m_jtxtName.getText() + "%";
        }

        // Postal
        if (m_jtxtPostal.getText() == null || m_jtxtPostal.getText().equals("")) {
            afilter[6] = QBFCompareEnum.COMP_NONE;
            afilter[7] = null;
        } else {
            afilter[6] = QBFCompareEnum.COMP_RE;
            afilter[7] = "%" + m_jtxtPostal.getText() + "%";
        }

        // Phone
        if (m_jtxtPhone.getText() == null || m_jtxtPhone.getText().equals("")) {
            afilter[8] = QBFCompareEnum.COMP_NONE;
            afilter[9] = null;
        } else {
            afilter[8] = QBFCompareEnum.COMP_RE;
            afilter[9] = "%" + m_jtxtPhone.getText() + "%";
        }

        // Email
        if (m_jtxtName2.getText() == null || m_jtxtName2.getText().equals("")) {
            afilter[10] = QBFCompareEnum.COMP_NONE;
            afilter[11] = null;
        } else {
            afilter[10] = QBFCompareEnum.COMP_RE;
            afilter[11] = "%" + m_jtxtName2.getText() + "%";
        }

        return afilter;
    }

    private static class MyListData extends AbstractListModel {
        private final List m_data;

        public MyListData(List data) {
            m_data = data;
        }

        @Override
        public Object getElementAt(int index) {
            return m_data.get(index);
        }

        @Override
        public int getSize() {
            return m_data.size();
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new JPanel();
        m_jKeys = new com.openbravo.editor.JEditorKeys();
        jPanel8 = new JPanel();
        jPanel1 = new JPanel();
        jcmdCancel = new JButton();
        jcmdOK = new JButton();
        jImageViewerSupplier = new com.openbravo.data.gui.JImageViewer();
        jPanel3 = new JPanel();
        jPanel5 = new JPanel();
        jPanel7 = new JPanel();
        jLblTaxID = new JLabel();
        m_jtxtTaxID = new com.openbravo.editor.JEditorString();
        jLblSearchKey = new JLabel();
        m_jtxtSearchKey = new com.openbravo.editor.JEditorString();
        jLblPostal = new JLabel();
        m_jtxtPostal = new com.openbravo.editor.JEditorString();
        jLblName = new JLabel();
        m_jtxtName = new com.openbravo.editor.JEditorString();
        jLblPhone = new JLabel();
        jLblEmail = new JLabel();
        m_jtxtName2 = new com.openbravo.editor.JEditorString();
        m_jtxtPhone = new com.openbravo.editor.JEditorString();
        jPanel4 = new JPanel();
        jScrollPane1 = new JScrollPane();
        jListSuppliers = new JList();
        jPanel6 = new JPanel();
        jbtnReset = new JButton();
        jbtnExecute = new JButton();

        setLayout(new BorderLayout());

        jPanel2.setLayout(new BorderLayout());
        jPanel2.add(m_jKeys, BorderLayout.NORTH);

        jPanel8.setLayout(new BorderLayout());

        jcmdCancel.setFont(new Font("Arial", 0, 12)); // NOI18N
        jcmdCancel.setIcon(new ImageIcon(getClass().getResource("/com/openbravo/images/cancel.png"))); // NOI18N
        jcmdCancel.setText(AppLocal.getIntString("button.cancel")); // NOI18N
        jcmdCancel.setFocusPainted(false);
        jcmdCancel.setFocusable(false);
        jcmdCancel.setMargin(new Insets(8, 16, 8, 16));
        jcmdCancel.setPreferredSize(new Dimension(110, 45));
        jcmdCancel.setRequestFocusEnabled(false);
        jcmdCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jcmdCancelActionPerformed(evt);
            }
        });
        jPanel1.add(jcmdCancel);

        jcmdOK.setIcon(new ImageIcon(getClass().getResource("/com/openbravo/images/ok.png"))); // NOI18N
        jcmdOK.setText(AppLocal.getIntString("button.ok")); // NOI18N
        jcmdOK.setEnabled(false);
        jcmdOK.setFocusPainted(false);
        jcmdOK.setFocusable(false);
        jcmdOK.setMargin(new Insets(8, 16, 8, 16));
        jcmdOK.setMaximumSize(new Dimension(103, 44));
        jcmdOK.setMinimumSize(new Dimension(103, 44));
        jcmdOK.setPreferredSize(new Dimension(110, 45));
        jcmdOK.setRequestFocusEnabled(false);
        jcmdOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jcmdOKActionPerformed(evt);
            }
        });
        jPanel1.add(jcmdOK);

        jPanel8.add(jPanel1, BorderLayout.LINE_END);

        jPanel2.add(jPanel8, BorderLayout.PAGE_END);
        jPanel2.add(jImageViewerSupplier, BorderLayout.CENTER);

        add(jPanel2, BorderLayout.LINE_END);

        jPanel3.setPreferredSize(new Dimension(450, 0));
        jPanel3.setLayout(new BorderLayout());

        jPanel5.setLayout(new BorderLayout());

        jLblTaxID.setFont(new Font("Arial", 0, 14)); // NOI18N
        jLblTaxID.setText(AppLocal.getIntString("label.taxid")); // NOI18N
        jLblTaxID.setPreferredSize(new Dimension(100, 30));

        m_jtxtTaxID.setFont(new Font("Arial", 0, 14)); // NOI18N
        m_jtxtTaxID.setPreferredSize(new Dimension(300, 30));

        jLblSearchKey.setFont(new Font("Arial", 0, 14)); // NOI18N
        jLblSearchKey.setText(AppLocal.getIntString("label.searchkey")); // NOI18N
        jLblSearchKey.setPreferredSize(new Dimension(100, 30));

        m_jtxtSearchKey.setFont(new Font("Arial", 0, 14)); // NOI18N
        m_jtxtSearchKey.setPreferredSize(new Dimension(300, 30));

        jLblPostal.setFont(new Font("Arial", 0, 14)); // NOI18N
        jLblPostal.setText(AppLocal.getIntString("label.postal")); // NOI18N
        jLblPostal.setPreferredSize(new Dimension(100, 30));

        m_jtxtPostal.setFont(new Font("Arial", 0, 14)); // NOI18N
        m_jtxtPostal.setPreferredSize(new Dimension(300, 30));

        jLblName.setFont(new Font("Arial", 0, 14)); // NOI18N
        jLblName.setText(AppLocal.getIntString("label.name")); // NOI18N
        jLblName.setPreferredSize(new Dimension(100, 30));

        m_jtxtName.setFont(new Font("Arial", 0, 14)); // NOI18N
        m_jtxtName.setPreferredSize(new Dimension(300, 30));

        jLblPhone.setFont(new Font("Arial", 0, 14)); // NOI18N
        jLblPhone.setText(AppLocal.getIntString("label.phone")); // NOI18N
        jLblPhone.setPreferredSize(new Dimension(100, 30));

        jLblEmail.setFont(new Font("Arial", 0, 14)); // NOI18N
        jLblEmail.setText(AppLocal.getIntString("label.email")); // NOI18N
        jLblEmail.setPreferredSize(new Dimension(100, 30));

        m_jtxtName2.setFont(new Font("Arial", 0, 14)); // NOI18N
        m_jtxtName2.setPreferredSize(new Dimension(300, 30));

        m_jtxtPhone.setFont(new Font("Arial", 0, 14)); // NOI18N
        m_jtxtPhone.setPreferredSize(new Dimension(300, 30));

        GroupLayout jPanel7Layout = new GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addComponent(jLblTaxID, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(m_jtxtTaxID, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addComponent(jLblSearchKey, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(m_jtxtSearchKey, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addComponent(jLblPostal, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(m_jtxtPostal, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addComponent(jLblName, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(m_jtxtName, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addComponent(jLblPhone, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(m_jtxtPhone, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addComponent(jLblEmail, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(m_jtxtName2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(jLblTaxID, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_jtxtTaxID, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(jLblSearchKey, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_jtxtSearchKey, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(jLblPostal, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_jtxtPostal, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(m_jtxtName, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLblName, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(m_jtxtPhone, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLblPhone, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(jLblEmail, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_jtxtName2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jPanel5.add(jPanel7, BorderLayout.CENTER);

        jPanel3.add(jPanel5, BorderLayout.PAGE_START);

        jPanel4.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jPanel4.setPreferredSize(new Dimension(450, 140));
        jPanel4.setLayout(new BorderLayout());

        jScrollPane1.setPreferredSize(new Dimension(400, 147));

        jListSuppliers.setFont(new Font("Arial", 0, 14)); // NOI18N
        jListSuppliers.setFocusable(false);
        jListSuppliers.setRequestFocusEnabled(false);
        jListSuppliers.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jListSuppliersMouseClicked(evt);
            }
        });
        jListSuppliers.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jListSuppliersValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(jListSuppliers);

        jPanel4.add(jScrollPane1, BorderLayout.CENTER);

        jbtnReset.setFont(new Font("Arial", 0, 12)); // NOI18N
        jbtnReset.setIcon(new ImageIcon(getClass().getResource("/com/openbravo/images/reload.png"))); // NOI18N
        jbtnReset.setText(AppLocal.getIntString("button.reset")); // NOI18N
        jbtnReset.setToolTipText("Clear Filter");
        jbtnReset.setActionCommand("Reset ");
        jbtnReset.setFocusable(false);
        jbtnReset.setPreferredSize(new Dimension(110, 45));
        jbtnReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbtnResetActionPerformed(evt);
            }
        });
        jPanel6.add(jbtnReset);

        jbtnExecute.setFont(new Font("Arial", 0, 12)); // NOI18N
        jbtnExecute.setIcon(new ImageIcon(getClass().getResource("/com/openbravo/images/ok.png"))); // NOI18N
        jbtnExecute.setText(AppLocal.getIntString("button.executefilter")); // NOI18N
        jbtnExecute.setToolTipText("Execute Filter");
        jbtnExecute.setFocusPainted(false);
        jbtnExecute.setPreferredSize(new Dimension(110, 45));
        jbtnExecute.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbtnExecuteActionPerformed(evt);
            }
        });
        jPanel6.add(jbtnExecute);

        jPanel4.add(jPanel6, BorderLayout.PAGE_START);

        jPanel3.add(jPanel4, BorderLayout.CENTER);

        add(jPanel3, BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void jcmdOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jcmdOKActionPerformed
        m_ReturnSupplier = (SupplierInfo) jListSuppliers.getSelectedValue();
        if (modalContext != null) {
            modalContext.setResult(m_ReturnSupplier);
            modalContext.close();
        }
    }//GEN-LAST:event_jcmdOKActionPerformed

    private void jcmdCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jcmdCancelActionPerformed
        m_ReturnSupplier = null;
        if (modalContext != null) {
            modalContext.close();
        }
    }//GEN-LAST:event_jcmdCancelActionPerformed

    private void jbtnExecuteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbtnExecuteActionPerformed
        m_ReturnSupplier = null;
        executeSearch();
    }//GEN-LAST:event_jbtnExecuteActionPerformed

    private void jListSuppliersValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jListSuppliersValueChanged
        m_ReturnSupplier = (SupplierInfo) jListSuppliers.getSelectedValue();

        if (m_ReturnSupplier != null) {
            jImageViewerSupplier.setImage(m_ReturnSupplier.getImage());
        }

        jcmdOK.setEnabled(jListSuppliers.getSelectedValue() != null);
    }//GEN-LAST:event_jListSuppliersValueChanged

    private void jListSuppliersMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jListSuppliersMouseClicked
        m_ReturnSupplier = (SupplierInfo) jListSuppliers.getSelectedValue();

        if (m_ReturnSupplier != null) {
            jImageViewerSupplier.setImage(m_ReturnSupplier.getImage());
        }

        if (evt.getClickCount() == 2 && jListSuppliers.getSelectedValue() != null) {
            jcmdOKActionPerformed(null);
        }
    }//GEN-LAST:event_jListSuppliersMouseClicked

    private void jbtnResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbtnResetActionPerformed
        m_jtxtTaxID.reset();
        m_jtxtSearchKey.reset();
        m_jtxtName.reset();
        m_jtxtPostal.reset();
        m_jtxtPhone.reset();
        m_jtxtName2.reset();

        m_jtxtTaxID.activate();

        cleanSearch();
    }//GEN-LAST:event_jbtnResetActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.openbravo.data.gui.JImageViewer jImageViewerSupplier;
    private JLabel jLblEmail;
    private JLabel jLblName;
    private JLabel jLblPhone;
    private JLabel jLblPostal;
    private JLabel jLblSearchKey;
    private JLabel jLblTaxID;
    private JList jListSuppliers;
    private JPanel jPanel1;
    private JPanel jPanel2;
    private JPanel jPanel3;
    private JPanel jPanel4;
    private JPanel jPanel5;
    private JPanel jPanel6;
    private JPanel jPanel7;
    private JPanel jPanel8;
    private JScrollPane jScrollPane1;
    private JButton jbtnExecute;
    private JButton jbtnReset;
    private JButton jcmdCancel;
    private JButton jcmdOK;
    private com.openbravo.editor.JEditorKeys m_jKeys;
    private com.openbravo.editor.JEditorString m_jtxtName;
    private com.openbravo.editor.JEditorString m_jtxtName2;
    private com.openbravo.editor.JEditorString m_jtxtPhone;
    private com.openbravo.editor.JEditorString m_jtxtPostal;
    private com.openbravo.editor.JEditorString m_jtxtSearchKey;
    private com.openbravo.editor.JEditorString m_jtxtTaxID;
    // End of variables declaration//GEN-END:variables
}
