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

package com.openbravo.pos.admin;

import com.openbravo.basic.BasicException;
import com.openbravo.data.gui.modal.PosUIModal;
import com.openbravo.data.loader.QBFCompareEnum;
import com.openbravo.data.user.EditorCreator;
import com.openbravo.data.user.ListProvider;
import com.openbravo.data.user.ListProviderCreator;
import com.openbravo.pos.forms.AppLocal;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.LayoutStyle;

public class JPeopleFinderPanel extends JPanel implements EditorCreator {

    private static final long serialVersionUID = 1L;

    private PeopleInfo selectedPeople;
    private ListProvider<PeopleInfo> lpr;
    private PosUIModal modalContext;

    public JPeopleFinderPanel() {
        initComponents();
        initDomainAdapters();
    }

    public JPeopleFinderPanel(DataLogicAdmin dlPeople) {
        init(dlPeople);
        initDomainAdapters();
    }

    private void initDomainAdapters() {
        setName("kriolos:admin:people-finder-panel");
        jButton3.setName("kriolos:admin:btn-execute");
        jButton1.setName("kriolos:admin:btn-reset");
        jcmdOK.setName("kriolos:admin:btn-ok");
        jcmdCancel.setName("kriolos:admin:btn-cancel");
        jListPeople.setName("kriolos:admin:list-people");
    }

    public void setModalContext(PosUIModal modalContext) {
        this.modalContext = modalContext;
    }

    public static PeopleInfo show(Component parent, DataLogicAdmin dlPeople) {
        return show(parent, dlPeople, null);
    }

    public static PeopleInfo show(Component parent, DataLogicAdmin dlPeople, PeopleInfo initialPeople) {
        JPeopleFinderPanel panel = new JPeopleFinderPanel(dlPeople);
        if (initialPeople != null) {
            panel.search(initialPeople);
        }
        PosUIModal modal = PosUIModal.create(parent, panel)
                .setTitle(AppLocal.getIntString("form.usertitle"))
                .setModal(true)
                .setResizable(true);
        panel.setModalContext(modal);
        modal.show();
        return panel.getSelectedPeople();
    }

    public PeopleInfo getSelectedPeople() {
        return selectedPeople;
    }

    private void init(DataLogicAdmin dlPeople) {
        initComponents();

        jScrollPane1.getVerticalScrollBar().setPreferredSize(new Dimension(35, 35));
        m_jtxtName.addEditorKeys(m_jKeys);
        m_jtxtName.reset();
        lpr = new ListProviderCreator<>(dlPeople.getPeopleList(), this);
        jListPeople.setCellRenderer(new PeopleRenderer());

        if (getRootPane() != null) {
            getRootPane().setDefaultButton(jcmdOK);
        }

        selectedPeople = null;
    }

    public void search(PeopleInfo people) {
        if (people == null || people.getName() == null || people.getName().equals("")) {
            m_jtxtName.reset();
            cleanSearch();
        } else {
            m_jtxtName.setText(people.getName());
            executeSearch();
        }
    }

    private void cleanSearch() {
        jListPeople.setModel(new PeopleListModel(new ArrayList<>()));
    }

    public void executeSearch() {
        try {
            jListPeople.setModel(new PeopleListModel(lpr.loadData()));
            if (jListPeople.getModel().getSize() > 0) {
                jListPeople.setSelectedIndex(0);
            }
        } catch (BasicException e) {
        }
    }

    @Override
    public Object createValue() throws BasicException {
        Object[] afilter = new Object[12];

        if (m_jtxtName.getText() == null || m_jtxtName.getText().equals("")) {
            afilter[0] = QBFCompareEnum.COMP_NONE;
            afilter[1] = null;
        } else {
            afilter[0] = QBFCompareEnum.COMP_RE;
            afilter[1] = "%" + m_jtxtName.getText() + "%";
        }

        return afilter;
    }

    private static class PeopleListModel extends AbstractListModel<PeopleInfo> {
        private final List<PeopleInfo> m_data;

        public PeopleListModel(List<PeopleInfo> data) {
            m_data = data;
        }

        @Override
        public PeopleInfo getElementAt(int index) {
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

        jPanel3 = new JPanel();
        jPanel5 = new JPanel();
        jPanel7 = new JPanel();
        jLblName = new JLabel();
        m_jtxtName = new com.openbravo.editor.JEditorString();
        jPanel4 = new JPanel();
        jScrollPane1 = new JScrollPane();
        jListPeople = new JList();
        jPanel6 = new JPanel();
        jButton1 = new JButton();
        jButton3 = new JButton();
        jPanel2 = new JPanel();
        m_jKeys = new com.openbravo.editor.JEditorKeys();
        jPanel8 = new JPanel();
        jPanel1 = new JPanel();
        jcmdCancel = new JButton();
        jcmdOK = new JButton();

        setLayout(new BorderLayout());

        jPanel3.setPreferredSize(new Dimension(450, 231));
        jPanel3.setLayout(new BorderLayout());

        jPanel5.setLayout(new BorderLayout());

        jLblName.setFont(new Font("Arial", 0, 14)); // NOI18N
        jLblName.setText(AppLocal.getIntString("label.people")); // NOI18N
        jLblName.setPreferredSize(new Dimension(100, 30));

        m_jtxtName.setFont(new Font("Arial", 0, 14)); // NOI18N
        m_jtxtName.setPreferredSize(new Dimension(300, 30));

        GroupLayout jPanel7Layout = new GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLblName, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(m_jtxtName, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(jLblName, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_jtxtName, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel5.add(jPanel7, BorderLayout.CENTER);

        jPanel3.add(jPanel5, BorderLayout.PAGE_START);

        jPanel4.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jPanel4.setLayout(new BorderLayout());

        jScrollPane1.setPreferredSize(new Dimension(400, 147));

        jListPeople.setFont(new Font("Arial", 0, 14)); // NOI18N
        jListPeople.setFocusable(false);
        jListPeople.setRequestFocusEnabled(false);
        jListPeople.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jListPeopleMouseClicked(evt);
            }
        });
        jListPeople.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jListPeopleValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(jListPeople);

        jPanel4.add(jScrollPane1, BorderLayout.CENTER);

        jButton1.setFont(new Font("Arial", 0, 12)); // NOI18N
        jButton1.setIcon(new ImageIcon(getClass().getResource("/com/openbravo/images/reload.png"))); // NOI18N
        ResourceBundle bundle = ResourceBundle.getBundle("pos_messages"); // NOI18N
        jButton1.setText(bundle.getString("button.reset")); // NOI18N
        jButton1.setToolTipText("Clear Filter");
        jButton1.setActionCommand("Reset ");
        jButton1.setFocusable(false);
        jButton1.setPreferredSize(new Dimension(110, 45));
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jPanel6.add(jButton1);

        jButton3.setFont(new Font("Arial", 0, 12)); // NOI18N
        jButton3.setIcon(new ImageIcon(getClass().getResource("/com/openbravo/images/ok.png"))); // NOI18N
        jButton3.setText(AppLocal.getIntString("button.executefilter")); // NOI18N
        jButton3.setToolTipText("Execute Filter");
        jButton3.setFocusPainted(false);
        jButton3.setPreferredSize(new Dimension(110, 45));
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });
        jPanel6.add(jButton3);

        jPanel4.add(jPanel6, BorderLayout.PAGE_START);

        jPanel3.add(jPanel4, BorderLayout.CENTER);

        add(jPanel3, BorderLayout.CENTER);

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

        jPanel2.add(jPanel8, BorderLayout.SOUTH);

        add(jPanel2, BorderLayout.LINE_END);
    }// </editor-fold>//GEN-END:initComponents

    private void jcmdOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jcmdOKActionPerformed
        selectedPeople = (PeopleInfo) jListPeople.getSelectedValue();
        if (modalContext != null) {
            modalContext.setResult(selectedPeople);
            modalContext.close();
        }
    }//GEN-LAST:event_jcmdOKActionPerformed

    private void jcmdCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jcmdCancelActionPerformed
        selectedPeople = null;
        if (modalContext != null) {
            modalContext.close();
        }
    }//GEN-LAST:event_jcmdCancelActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        executeSearch();
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jListPeopleValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jListPeopleValueChanged
        jcmdOK.setEnabled(jListPeople.getSelectedValue() != null);
    }//GEN-LAST:event_jListPeopleValueChanged

    private void jListPeopleMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jListPeopleMouseClicked
        if (evt.getClickCount() == 2 && jListPeople.getSelectedValue() != null) {
            selectedPeople = (PeopleInfo) jListPeople.getSelectedValue();
            if (modalContext != null) {
                modalContext.setResult(selectedPeople);
                modalContext.close();
            }
        }
    }//GEN-LAST:event_jListPeopleMouseClicked

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        m_jtxtName.reset();
        cleanSearch();
    }//GEN-LAST:event_jButton1ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton jButton1;
    private JButton jButton3;
    private JLabel jLblName;
    private JList jListPeople;
    private JPanel jPanel1;
    private JPanel jPanel2;
    private JPanel jPanel3;
    private JPanel jPanel4;
    private JPanel jPanel5;
    private JPanel jPanel6;
    private JPanel jPanel7;
    private JPanel jPanel8;
    private JScrollPane jScrollPane1;
    private JButton jcmdCancel;
    private JButton jcmdOK;
    private com.openbravo.editor.JEditorKeys m_jKeys;
    private com.openbravo.editor.JEditorString m_jtxtName;
    // End of variables declaration//GEN-END:variables
}
