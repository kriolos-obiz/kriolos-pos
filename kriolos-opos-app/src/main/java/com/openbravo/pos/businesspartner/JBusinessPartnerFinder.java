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

package com.openbravo.pos.businesspartner;

import com.openbravo.pos.customers.DataLogicCustomers;
import com.openbravo.pos.customers.CustomerInfo;
import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.QBFCompareEnum;
import com.openbravo.data.user.EditorCreator;
import com.openbravo.data.user.ListProvider;
import com.openbravo.data.user.ListProviderCreator;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.AppView;
import java.awt.*;
import java.util.ArrayList;
import javax.swing.JFrame;
import javax.swing.*;

import java.awt.event.KeyEvent;

/**
 *
 * @author adrianromero
 */
public class JBusinessPartnerFinder extends javax.swing.JDialog implements EditorCreator {

    private static final long serialVersionUID = 1L;

    private BusinessPartner m_BPartner;
    private ListProvider lpr;
    private AppView appView;

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
        m_jtxtEmail.reset();
        m_jtxtTaxID.activate();

        cleanSearch();
    }

    public void setAppView(AppView appView) {
        this.appView = appView;
    }

    private JBusinessPartnerFinder(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
    }

    private JBusinessPartnerFinder(java.awt.Dialog parent, boolean modal) {
        super(parent, modal);
    }

    public static JBusinessPartnerFinder getBusinessPartnerFinder(Component parent, DataLogicCustomers dlCustomers) {
        Window window = getWindow(parent);

        JBusinessPartnerFinder myMsg;
        if (window instanceof Frame) {
            myMsg = new JBusinessPartnerFinder((Frame) window, true);
        } else {
            myMsg = new JBusinessPartnerFinder((Dialog) window, true);
        }
        myMsg.init(dlCustomers);
        myMsg.applyComponentOrientation(parent.getComponentOrientation());

        return myMsg;
    }

    public BusinessPartner getSelectedCustomer() {
        return m_BPartner;
    }

    private void init(DataLogicCustomers dlCustomers) {

        initComponents();

        jScrollPane1.getVerticalScrollBar().setPreferredSize(new Dimension(35, 35));

        m_jtxtTaxID.addEditorKeys(m_jKeys);
        m_jtxtSearchKey.addEditorKeys(m_jKeys);
        m_jtxtName.addEditorKeys(m_jKeys);
        m_jtxtPostal.addEditorKeys(m_jKeys);
        m_jtxtPhone.addEditorKeys(m_jKeys);
        m_jtxtEmail.addEditorKeys(m_jKeys);

        m_jtxtTaxID.reset();
        m_jtxtSearchKey.reset();
        m_jtxtName.reset();
        m_jtxtPostal.reset();
        m_jtxtPhone.reset();
        m_jtxtEmail.reset();

        m_jtxtTaxID.activate();

        lpr = new ListProviderCreator(dlCustomers.getCustomerList(), this);

        jListBPartner.setCellRenderer(new BusinessPartnerListCellRenderer());

        getRootPane().setDefaultButton(jcmdOK);

        m_BPartner = null;

    }

    public void search(BusinessPartner bpartner) {

        if (bpartner == null || bpartner.getName() == null || bpartner.getName().equals("")) {

            m_jtxtTaxID.reset();
            m_jtxtSearchKey.reset();
            m_jtxtName.reset();
            m_jtxtPostal.reset();
            m_jtxtPhone.reset();
            m_jtxtEmail.reset();

            m_jtxtTaxID.activate();

            cleanSearch();
        } else {

            m_jtxtTaxID.setText(bpartner.getTaxid());
            m_jtxtSearchKey.setText(bpartner.getSearchkey());
            m_jtxtName.setText(bpartner.getName());
            m_jtxtPostal.setText(bpartner.getPostal());
            m_jtxtPhone.setText(bpartner.getPhone());
            m_jtxtEmail.setText(bpartner.getEmail());

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
        m_jtxtEmail.setText("");
        jListBPartner.setModel(new BPListModel(new ArrayList()));
    }

    public void executeSearch() {
        
        try {
            jListBPartner.setModel(new BPListModel(lpr.loadData()));
            if (jListBPartner.getModel().getSize() > 0) {
                jListBPartner.setSelectedIndex(0);
            } else {
                if(!m_jtxtName.getText().equals("")) {
                    
                    int n = JOptionPane.showConfirmDialog(
                        null,
                        AppLocal.getIntString("message.bpartnernotfound"),
                        AppLocal.getIntString("title.editor"),
                        JOptionPane.YES_NO_OPTION);

                    if (n != 1) {
                        this.setVisible(false);
                        appView.getAppUserView().showTask("com.openbravo.pos.customers.CustomersPanel");
                        JOptionPane.showMessageDialog(this, 
                            "You must complete Account and Search Key Then Save to add to Ticket",
                            "Create Customer",JOptionPane.OK_OPTION);
                    }
                }
            }
        } catch (BasicException e) {
        }
    }

    @Override
    public Object createValue() throws BasicException {

        //T businessPartener = new T();
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

        // EMail
        if (m_jtxtEmail.getText() == null || m_jtxtEmail.getText().equals("")) {
            afilter[10] = QBFCompareEnum.COMP_NONE;
            afilter[11] = null;
        } else {
            afilter[10] = QBFCompareEnum.COMP_RE;
            afilter[11] = "%" + m_jtxtEmail.getText() + "%";
        }

        return afilter;
    }

    private static Window getWindow(Component parent) {
        if (parent == null) {
            return new JFrame();
        } else if (parent instanceof Frame || parent instanceof Dialog) {
            return (Window) parent;
        } else {
            return getWindow(parent.getParent());
        }
    }

    private static class BPListModel extends javax.swing.AbstractListModel {

        private final java.util.List m_data;

        public BPListModel(java.util.List data) {
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

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        m_jKeys = new com.openbravo.editor.JEditorKeys();
        jPanel8 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jcmdCancel = new javax.swing.JButton();
        jcmdOK = new javax.swing.JButton();
        jImageViewerBP = new com.openbravo.data.gui.JImageViewer();
        jPanel3 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        jLblTaxID = new javax.swing.JLabel();
        m_jtxtTaxID = new com.openbravo.editor.JEditorString();
        jLblSearchKey = new javax.swing.JLabel();
        m_jtxtSearchKey = new com.openbravo.editor.JEditorString();
        jLblPostal = new javax.swing.JLabel();
        m_jtxtPostal = new com.openbravo.editor.JEditorString();
        jLblName = new javax.swing.JLabel();
        m_jtxtName = new com.openbravo.editor.JEditorString();
        jLblPhone = new javax.swing.JLabel();
        jLblEmail = new javax.swing.JLabel();
        m_jtxtPhone = new com.openbravo.editor.JEditorString();
        m_jtxtEmail = new com.openbravo.editor.JEditorString();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jListBPartner = new javax.swing.JList();
        jPanel6 = new javax.swing.JPanel();
        jbtnReset = new javax.swing.JButton();
        jbtnExecute = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(AppLocal.getIntString("form.customertitle")); // NOI18N
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        jPanel2.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jPanel2.setLayout(new java.awt.BorderLayout());
        jPanel2.add(m_jKeys, java.awt.BorderLayout.NORTH);

        jPanel8.setLayout(new java.awt.BorderLayout());

        jcmdCancel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jcmdCancel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/cancel.png"))); // NOI18N
        jcmdCancel.setText(AppLocal.getIntString("button.cancel")); // NOI18N
        jcmdCancel.setFocusPainted(false);
        jcmdCancel.setFocusable(false);
        jcmdCancel.setMargin(new java.awt.Insets(8, 16, 8, 16));
        jcmdCancel.setPreferredSize(new java.awt.Dimension(110, 45));
        jcmdCancel.setRequestFocusEnabled(false);
        jcmdCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jcmdCancelActionPerformed(evt);
            }
        });
        jPanel1.add(jcmdCancel);

        jcmdOK.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/ok.png"))); // NOI18N
        jcmdOK.setText(AppLocal.getIntString("button.ok")); // NOI18N
        jcmdOK.setEnabled(false);
        jcmdOK.setFocusPainted(false);
        jcmdOK.setFocusable(false);
        jcmdOK.setMargin(new java.awt.Insets(8, 16, 8, 16));
        jcmdOK.setMaximumSize(new java.awt.Dimension(103, 44));
        jcmdOK.setMinimumSize(new java.awt.Dimension(103, 44));
        jcmdOK.setPreferredSize(new java.awt.Dimension(110, 45));
        jcmdOK.setRequestFocusEnabled(false);
        jcmdOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jcmdOKActionPerformed(evt);
            }
        });
        jPanel1.add(jcmdOK);

        jPanel8.add(jPanel1, java.awt.BorderLayout.LINE_END);

        jPanel2.add(jPanel8, java.awt.BorderLayout.PAGE_END);
        jPanel2.add(jImageViewerBP, java.awt.BorderLayout.CENTER);

        getContentPane().add(jPanel2, java.awt.BorderLayout.LINE_END);

        jPanel3.setPreferredSize(new java.awt.Dimension(450, 0));
        jPanel3.setLayout(new java.awt.BorderLayout());

        jPanel5.setLayout(new java.awt.BorderLayout());

        jLblTaxID.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLblTaxID.setText(AppLocal.getIntString("label.taxid")); // NOI18N
        jLblTaxID.setMaximumSize(new java.awt.Dimension(60, 15));
        jLblTaxID.setMinimumSize(new java.awt.Dimension(60, 15));
        jLblTaxID.setPreferredSize(new java.awt.Dimension(110, 30));

        m_jtxtTaxID.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jtxtTaxID.setPreferredSize(new java.awt.Dimension(200, 30));

        jLblSearchKey.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLblSearchKey.setText(AppLocal.getIntString("label.searchkey")); // NOI18N
        jLblSearchKey.setMaximumSize(new java.awt.Dimension(60, 15));
        jLblSearchKey.setMinimumSize(new java.awt.Dimension(60, 15));
        jLblSearchKey.setPreferredSize(new java.awt.Dimension(110, 30));

        m_jtxtSearchKey.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jtxtSearchKey.setPreferredSize(new java.awt.Dimension(250, 30));

        jLblPostal.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLblPostal.setText("Postal");
        jLblPostal.setMaximumSize(new java.awt.Dimension(60, 15));
        jLblPostal.setMinimumSize(new java.awt.Dimension(60, 15));
        jLblPostal.setPreferredSize(new java.awt.Dimension(110, 30));

        m_jtxtPostal.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jtxtPostal.setPreferredSize(new java.awt.Dimension(250, 30));

        jLblName.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLblName.setText(AppLocal.getIntString("label.prodname")); // NOI18N
        jLblName.setMaximumSize(new java.awt.Dimension(60, 15));
        jLblName.setMinimumSize(new java.awt.Dimension(60, 15));
        jLblName.setPreferredSize(new java.awt.Dimension(110, 30));

        m_jtxtName.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jtxtName.setPreferredSize(new java.awt.Dimension(250, 30));

        jLblPhone.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("pos_messages"); // NOI18N
        jLblPhone.setText(bundle.getString("label.companytelephone")); // NOI18N
        jLblPhone.setPreferredSize(new java.awt.Dimension(110, 30));

        jLblEmail.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLblEmail.setText(bundle.getString("label.companyemail")); // NOI18N
        jLblEmail.setPreferredSize(new java.awt.Dimension(110, 30));

        m_jtxtPhone.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jtxtPhone.setMinimumSize(new java.awt.Dimension(150, 30));
        m_jtxtPhone.setPreferredSize(new java.awt.Dimension(250, 30));

        m_jtxtEmail.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jtxtEmail.setMinimumSize(new java.awt.Dimension(150, 30));
        m_jtxtEmail.setPreferredSize(new java.awt.Dimension(250, 30));

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLblName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLblSearchKey, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLblPostal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(m_jtxtSearchKey, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(m_jtxtPostal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(m_jtxtName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addComponent(jLblTaxID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(m_jtxtTaxID, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jLblEmail, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLblPhone, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(m_jtxtPhone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(m_jtxtEmail, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(m_jtxtTaxID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLblTaxID, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(m_jtxtSearchKey, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLblSearchKey, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLblPostal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(m_jtxtPostal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(m_jtxtName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLblName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(m_jtxtPhone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLblPhone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLblEmail, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_jtxtEmail, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        m_jtxtName.getAccessibleContext().setAccessibleName("");

        jPanel5.add(jPanel7, java.awt.BorderLayout.CENTER);

        jPanel3.add(jPanel5, java.awt.BorderLayout.PAGE_START);

        jPanel4.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jPanel4.setPreferredSize(new java.awt.Dimension(450, 140));
        jPanel4.setLayout(new java.awt.BorderLayout());

        jScrollPane1.setPreferredSize(new java.awt.Dimension(400, 147));

        jListBPartner.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jListBPartner.setFocusable(false);
        jListBPartner.setRequestFocusEnabled(false);
        jListBPartner.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jListBPartnerMouseClicked(evt);
            }
        });
        jListBPartner.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jListBPartnerValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(jListBPartner);

        jPanel4.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jbtnReset.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jbtnReset.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/reload.png"))); // NOI18N
        jbtnReset.setText(bundle.getString("button.reset")); // NOI18N
        jbtnReset.setToolTipText("Clear Filter");
        jbtnReset.setActionCommand("Reset ");
        jbtnReset.setFocusable(false);
        jbtnReset.setPreferredSize(new java.awt.Dimension(110, 45));
        jbtnReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbtnResetActionPerformed(evt);
            }
        });
        jPanel6.add(jbtnReset);
        jbtnReset.getAccessibleContext().setAccessibleDescription("");

        jbtnExecute.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jbtnExecute.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/ok.png"))); // NOI18N
        jbtnExecute.setText(AppLocal.getIntString("button.executefilter")); // NOI18N
        jbtnExecute.setToolTipText("Execute Filter");
        jbtnExecute.setFocusPainted(false);
        jbtnExecute.setPreferredSize(new java.awt.Dimension(110, 45));
        jbtnExecute.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbtnExecuteActionPerformed(evt);
            }
        });
        jPanel6.add(jbtnExecute);
        jbtnExecute.getAccessibleContext().setAccessibleDescription("");

        jPanel4.add(jPanel6, java.awt.BorderLayout.PAGE_START);

        jPanel3.add(jPanel4, java.awt.BorderLayout.CENTER);

        getContentPane().add(jPanel3, java.awt.BorderLayout.CENTER);

        setSize(new java.awt.Dimension(758, 634));
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents
    private void jcmdOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jcmdOKActionPerformed

        m_BPartner = (BusinessPartner) jListBPartner.getSelectedValue();
        dispose();

    }//GEN-LAST:event_jcmdOKActionPerformed

    private void jcmdCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jcmdCancelActionPerformed
        
        dispose();

    }//GEN-LAST:event_jcmdCancelActionPerformed

    private void jbtnExecuteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbtnExecuteActionPerformed

        m_BPartner=null;
        executeSearch();
        
    }//GEN-LAST:event_jbtnExecuteActionPerformed

    private void jListBPartnerValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jListBPartnerValueChanged

        m_BPartner = (CustomerInfo) jListBPartner.getSelectedValue();
        
        if (m_BPartner != null) {
                jImageViewerBP.setImage(m_BPartner.getImage());
        }
        
        jcmdOK.setEnabled(jListBPartner.getSelectedValue() != null);

    }//GEN-LAST:event_jListBPartnerValueChanged

    private void jListBPartnerMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jListBPartnerMouseClicked

        m_BPartner = (BusinessPartner) jListBPartner.getSelectedValue();
        
        if (m_BPartner != null) {
                jImageViewerBP.setImage(m_BPartner.getImage());
        } 

    }//GEN-LAST:event_jListBPartnerMouseClicked

    private void jbtnResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbtnResetActionPerformed
 
        m_jtxtTaxID.reset();
        m_jtxtSearchKey.reset();
        m_jtxtName.reset();
        m_jtxtPostal.reset();
        m_jtxtPhone.reset();
        m_jtxtEmail.reset();

        m_jtxtTaxID.activate(); 
        
        cleanSearch();
}//GEN-LAST:event_jbtnResetActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.openbravo.data.gui.JImageViewer jImageViewerBP;
    private javax.swing.JLabel jLblEmail;
    private javax.swing.JLabel jLblName;
    private javax.swing.JLabel jLblPhone;
    private javax.swing.JLabel jLblPostal;
    private javax.swing.JLabel jLblSearchKey;
    private javax.swing.JLabel jLblTaxID;
    private javax.swing.JList jListBPartner;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton jbtnExecute;
    private javax.swing.JButton jbtnReset;
    private javax.swing.JButton jcmdCancel;
    private javax.swing.JButton jcmdOK;
    private com.openbravo.editor.JEditorKeys m_jKeys;
    private com.openbravo.editor.JEditorString m_jtxtEmail;
    private com.openbravo.editor.JEditorString m_jtxtName;
    private com.openbravo.editor.JEditorString m_jtxtPhone;
    private com.openbravo.editor.JEditorString m_jtxtPostal;
    private com.openbravo.editor.JEditorString m_jtxtSearchKey;
    private com.openbravo.editor.JEditorString m_jtxtTaxID;
    // End of variables declaration//GEN-END:variables
}
