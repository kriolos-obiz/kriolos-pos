/*
 * Copyright (C) 2022 KriolOS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.openbravo.pos.forms;

import com.openbravo.basic.BasicException;
import com.openbravo.beans.JFlowPanel;
import com.openbravo.beans.JPasswordDialog;
import com.openbravo.data.gui.MessageInf;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;

/**
 *
 * @author poolborges
 */
public class JAuthPanel extends javax.swing.JPanel {

    private static final Logger LOGGER = Logger.getLogger(JAuthPanel.class.getName());
    private static final long serialVersionUID = 1L;

    private StringBuilder inputtext;
    
    private final DataLogicSystem m_dlSystem;
    private final AuthListener authListener;

    public JAuthPanel(DataLogicSystem dlSystem, AuthListener authcListener) {
        
        authListener = authcListener;
        m_dlSystem = dlSystem;
        
        initComponents();
        initPanel();
    }

    private void initPanel() {
       
        usersLisScrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(30, 30));
        showListPeople();

        inputtext = new StringBuilder();
        m_txtKeys.setText(null);
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                m_txtKeys.requestFocus();
            }
        });
    }

    private void showListPeople() {

        try {

            usersLisScrollPane.getViewport().setView(null);

            JFlowPanel jPeople = new JFlowPanel();
            jPeople.applyComponentOrientation(getComponentOrientation());

            java.util.List<AppUser> peoples = m_dlSystem.listPeopleVisible();
            
            LOGGER.log(Level.INFO, "Number of Peoples found is: "+peoples.size());

            for (AppUser user : peoples) {
                
                JButton btn = new JButton(new AppUserAction(user));
                btn.applyComponentOrientation(getComponentOrientation());
                btn.setFocusPainted(false);
                btn.setFocusable(false);
                btn.setRequestFocusEnabled(false);
                btn.setMaximumSize(new Dimension(110, 60));
                btn.setPreferredSize(new Dimension(110, 60));
                btn.setMinimumSize(new Dimension(110, 60));
                btn.setHorizontalAlignment(SwingConstants.CENTER);
                btn.setHorizontalTextPosition(AbstractButton.CENTER);
                btn.setVerticalTextPosition(AbstractButton.BOTTOM);
                jPeople.add(btn);
            }

            usersLisScrollPane.getViewport().setView(jPeople);

        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Exception on listPeople: ", ex);
        }
    }

    private void processKey(char c) {

        if ((c == '\n') || (c == '?')) {
            AppUser user = null;
            try {
                user = m_dlSystem.findPeopleByCard(inputtext.toString());
            } catch (BasicException ex) {
                LOGGER.log(Level.WARNING, "Exception on findPeopleByCard: ", ex);
            }

            if (user == null) {
                MessageInf msg = new MessageInf(MessageInf.SGN_WARNING,
                        AppLocal.getIntString("message.nocard"));
                msg.show(this);
            } else {
                authListener.onSucess(user);
            }

            inputtext = new StringBuilder();
        } else {
            inputtext.append(c);
        }
    }

    public void showPanel() {

        
    }

    class AppUserAction extends AbstractAction {

        private static final long serialVersionUID = 1L;
        private final AppUser m_actionuser;

        public AppUserAction(AppUser user) {
            m_actionuser = user;
            putValue(Action.SMALL_ICON, m_actionuser.getIcon());
            putValue(Action.NAME, m_actionuser.getName());
            putValue(Action.SELECTED_KEY, "USER_ID_" + m_actionuser.getName());
        }

        public AppUser getUser() {
            return m_actionuser;
        }

        @Override
        public void actionPerformed(ActionEvent evt) {

            try {
                if (m_actionuser.authenticate()) {
                    LOGGER.log(Level.INFO, "User is already Logged");
                    authListener.onSucess(m_actionuser);
                } else {
                    // Using getWindowAncestor guarantees finding the root top-level Frame/Dialog window.
                    String sPassword = JPasswordDialog.showEditor(
                            SwingUtilities.getWindowAncestor(JAuthPanel.this),
                            AppLocal.getIntString("label.Password"),
                            m_actionuser.getName(),
                            m_actionuser.getIcon());
                    if (sPassword != null) {

                        if (m_actionuser.authenticate(sPassword)) {
                            LOGGER.log(Level.INFO, "Login Success");
                            authListener.onSucess(m_actionuser);
                        } else {
                            LOGGER.log(Level.INFO, "Login failed");
                            MessageInf msg = new MessageInf(MessageInf.SGN_WARNING,
                                    AppLocal.getIntString("message.BadPassword"));
                            msg.show(JAuthPanel.this);
                        }
                    }
                }
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, "Exception on LOGIN: ", ex);
            }
        }
    }
    
    public interface AuthListener {
        public void onSucess(AppUser user);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        m_vendorImageLabel = new javax.swing.JLabel();
        mainScrollPanel = new javax.swing.JScrollPane();
        jCopyRightPanel1 = new com.openbravo.pos.forms.JCopyRightPanel();
        filler2 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 10), new java.awt.Dimension(32767, 0));
        leftPanel = new javax.swing.JPanel();
        leftHeaderPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        usersLisScrollPane = new javax.swing.JScrollPane();
        leftFooterPanel = new javax.swing.JPanel();
        m_txtKeys = new javax.swing.JTextField();

        setLayout(new java.awt.BorderLayout());

        mainPanel.setLayout(new java.awt.BorderLayout());

        m_vendorImageLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        m_vendorImageLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/logo_100×100.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(m_vendorImageLabel, org.openide.util.NbBundle.getMessage(JAuthPanel.class, "JAuthPanel.m_vendorImageLabel.text")); // NOI18N
        m_vendorImageLabel.setToolTipText(org.openide.util.NbBundle.getMessage(JAuthPanel.class, "JAuthPanel.m_vendorImageLabel.toolTipText")); // NOI18N
        m_vendorImageLabel.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        m_vendorImageLabel.setName("m_vendorImageLabel"); // NOI18N
        mainPanel.add(m_vendorImageLabel, java.awt.BorderLayout.NORTH);
        m_vendorImageLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JAuthPanel.class, "JAuthPanel.m_vendorImageLabel.AccessibleContext.accessibleName")); // NOI18N

        mainScrollPanel.setViewportView(jCopyRightPanel1);

        mainPanel.add(mainScrollPanel, java.awt.BorderLayout.CENTER);
        mainPanel.add(filler2, java.awt.BorderLayout.SOUTH);

        add(mainPanel, java.awt.BorderLayout.CENTER);

        leftPanel.setPreferredSize(new java.awt.Dimension(300, 400));
        leftPanel.setLayout(new java.awt.BorderLayout());

        leftHeaderPanel.setMinimumSize(new java.awt.Dimension(300, 40));
        leftHeaderPanel.setName(""); // NOI18N
        leftHeaderPanel.setPreferredSize(new java.awt.Dimension(300, 40));
        leftHeaderPanel.setLayout(new java.awt.BorderLayout());

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(JAuthPanel.class, "JAuthPanel.m_LoginLabel.text")); // NOI18N
        jLabel1.setName("m_LoginLabel"); // NOI18N
        leftHeaderPanel.add(jLabel1, java.awt.BorderLayout.CENTER);
        jLabel1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(JAuthPanel.class, "JAuthPanel.m_LoginLabel.AccessibleContext.accessibleName")); // NOI18N

        leftPanel.add(leftHeaderPanel, java.awt.BorderLayout.NORTH);

        usersLisScrollPane.setBorder(null);
        usersLisScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        usersLisScrollPane.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        usersLisScrollPane.setMinimumSize(new java.awt.Dimension(21, 40));
        usersLisScrollPane.setPreferredSize(new java.awt.Dimension(300, 40));
        leftPanel.add(usersLisScrollPane, java.awt.BorderLayout.CENTER);

        m_txtKeys.setPreferredSize(new java.awt.Dimension(0, 0));
        m_txtKeys.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                m_txtKeysKeyTyped(evt);
            }
        });

        javax.swing.GroupLayout leftFooterPanelLayout = new javax.swing.GroupLayout(leftFooterPanel);
        leftFooterPanel.setLayout(leftFooterPanelLayout);
        leftFooterPanelLayout.setHorizontalGroup(
            leftFooterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(leftFooterPanelLayout.createSequentialGroup()
                .addComponent(m_txtKeys, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(300, Short.MAX_VALUE))
        );
        leftFooterPanelLayout.setVerticalGroup(
            leftFooterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(leftFooterPanelLayout.createSequentialGroup()
                .addComponent(m_txtKeys, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(57, 57, 57))
        );

        leftPanel.add(leftFooterPanel, java.awt.BorderLayout.SOUTH);

        add(leftPanel, java.awt.BorderLayout.EAST);
    }// </editor-fold>//GEN-END:initComponents

    private void m_txtKeysKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_m_txtKeysKeyTyped

        m_txtKeys.setText("0");
        processKey(evt.getKeyChar());
    }//GEN-LAST:event_m_txtKeysKeyTyped


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.Box.Filler filler2;
    private com.openbravo.pos.forms.JCopyRightPanel jCopyRightPanel1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel leftFooterPanel;
    private javax.swing.JPanel leftHeaderPanel;
    private javax.swing.JPanel leftPanel;
    private javax.swing.JTextField m_txtKeys;
    private javax.swing.JLabel m_vendorImageLabel;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JScrollPane mainScrollPanel;
    private javax.swing.JScrollPane usersLisScrollPane;
    // End of variables declaration//GEN-END:variables
}
