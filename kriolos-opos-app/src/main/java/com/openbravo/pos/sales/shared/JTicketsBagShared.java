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
package com.openbravo.pos.sales.shared;

import com.openbravo.basic.BasicException;
import com.openbravo.beans.JIntegerDialog;
import com.openbravo.beans.JPasswordDialog;
import com.openbravo.data.gui.MessageInf;
import com.openbravo.pos.forms.*;
import com.openbravo.pos.sales.DataLogicReceipts;
import com.openbravo.pos.sales.JTicketsBag;
import com.openbravo.pos.sales.ReprintTicketInfo;
import com.openbravo.pos.sales.SharedTicketInfo;
import com.openbravo.pos.sales.TicketsEditor;
import com.openbravo.pos.ticket.TicketInfo;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * A shared, multi-ticket implementation of the {@link JTicketsBag} controller.
 * <p>
 * This class manages a "layaway" or suspended ticket workflow where multiple active sales receipts
 * can be temporarily stored in the database (as shared tickets) and resumed later. This is
 * particularly useful in retail and bar environments, allowing an operator to put a customer's
 * transaction on hold to serve someone else without losing progress.
 * </p>
 *
 * <h3>Core Features & Business Logic:</h3>
 * <ul>
 *   <li><b>Permission-Based Visibility:</b> Filters the list of shared tickets based on user roles.
 *       Managers or users with override rights see all global tickets, while standard operators
 *       are restricted to their own active entries.</li>
 *   <li><b>Data Persistence Safety:</b> Automatically saves work-in-progress carts to the temporary database
 *       ({@link DataLogicReceipts}) during screen transitions or new ticket instantiation, provided
 *       the cart contains at least one line item.</li>
 *   <li><b>Secure Transaction Voiding:</b> Enforces supervisor validation checks (PIN entry via
 *       {@link JPasswordDialog}) before allowing terminal users to delete or void a running invoice.</li>
 *   <li><b>Asynchronous Lookups:</b> Dispatches heavy database lookups—such as fetching shared lists
 *       or closed historical tickets for reprinting—safely onto the Swing Event Dispatch Thread (EDT)
 *       to keep the point-of-sale layout smooth and responsive.</li>
 * </ul>
 *
 * @author KriolOS / JG uniCenta
 * @version 1.0
 * @see com.openbravo.pos.sales.JTicketsBag
 * @see com.openbravo.pos.sales.shared.JTicketsBagSharedList
 * @see javax.swing.JPanel
 */

public class JTicketsBagShared extends JTicketsBag {

    private DataLogicReceipts dlReceipts = null;
    private DataLogicSales dlSales = null;
    private DataLogicSystem dlSystem;
    private Boolean hasPermissionShowSharedList;

    /**
     * Creates new form JTicketsBagShared
     *
     * @param app
     * @param panelticket
     */
    public JTicketsBagShared(AppView app, TicketsEditor panelticket) {

        super(app, panelticket);

        dlReceipts = (DataLogicReceipts) app.getBean("com.openbravo.pos.sales.DataLogicReceipts");
        dlSales = (DataLogicSales) app.getBean("com.openbravo.pos.forms.DataLogicSales");
        dlSystem = (DataLogicSystem) m_App.getBean("com.openbravo.pos.forms.DataLogicSystem");

        hasPermissionShowSharedList = m_App.hasPermission("sales.ShowList");

        initComponents();

        m_jReprintTickets.setVisible(true);

        LOGGER.log(System.Logger.Level.INFO, "JTicketsBagShared ");
    }

    /**
     *
     */
    @Override
    public void activate() {
        LOGGER.log(System.Logger.Level.INFO, "Active panel");
        updateCount();
        m_jDelTicket.setEnabled(m_App.hasPermission("com.openbravo.pos.sales.JPanelTicketEdits"));
        m_jDelTicket.setEnabled(m_App.hasPermission("sales.DeleteTicket"));

        TicketInfo ticket = new TicketInfo();
        LOGGER.log(System.Logger.Level.INFO, "newTicket has id: " + ticket.getId());
        m_panelticket.setActiveTicket(ticket, null);
    }

    /**
     *
     * @return
     */
    @Override
    public boolean deactivate() {
        saveCurrentTicket();
        m_panelticket.setActiveTicket(null, null);

        return true;
    }

    @Override
    public void deleteTicket() {

        dlSystem.execTicketRemoved(
                new Object[]{
                    m_App.getAppUserView().getUser().getName(),
                    "Void",
                    "Ticket Deleted",
                    0.0
                });
        updateCount();
    }

    public void updateCount() {

        List<SharedTicketInfo> l = getSharedTickets();
        int count = l != null ? l.size() : 0;

        if (count > 0) {
            m_jListTickets.setText(Integer.toString(count));
        } else {
            m_jListTickets.setText("");
        }

        LOGGER.log(System.Logger.Level.DEBUG, "Count shared ticket: " + count);

    }

    /**
     *
     * @return
     */
    @Override
    protected JComponent getBagComponent() {
        return this;
    }

    /**
     *
     * @return
     */
    @Override
    protected JComponent getNullComponent() {
        return new JPanel();
    }

    private void saveCurrentTicket() {

        TicketInfo ticketInfo = m_panelticket.getActiveTicket();
        if (ticketInfo != null) {
            String ticketID = m_panelticket.getActiveTicket().getId();
            int pickupId = m_panelticket.getActiveTicket().getPickupId();
            try {
                //SAVE Ticket with at less One line
                if (ticketInfo.getLinesCount() >= 1) {
                    dlReceipts.insertSharedTicket(ticketID, m_panelticket.getActiveTicket(), pickupId);

                    m_jListTickets.setText("*");
                    LOGGER.log(System.Logger.Level.INFO, "SAVED Current Ticket ID: " + ticketID);
                } else {
                    LOGGER.log(System.Logger.Level.INFO, "NOT SAVED Current Ticket because has no line/item, Ticket ID: " + ticketID);
                    //new MessageInf(new BasicException("Cannot save current Ticket because has no line/item")).show(this);
                }
            }
            catch (BasicException e) {
                LOGGER.log(System.Logger.Level.WARNING, "Exception saveCurrentTicket: " + ticketID, e);
                new MessageInf(e).show(this);
            }
        } else {
            LOGGER.log(System.Logger.Level.INFO, "NOT SAVED Current Ticket because ActiveTicket is NULL");
        }

        updateCount();
    }

    private void setActiveTicket(String id) throws BasicException {

        TicketInfo ticket = dlReceipts.getSharedTicket(id);
        if (ticket == null) {
            m_jListTickets.setText("");
            LOGGER.log(System.Logger.Level.WARNING, "NOT FOUND Shared TICKET for id: " + id);
            throw new BasicException(AppLocal.getIntString("message.noticket"));
        } else {
            Integer pickUp = dlReceipts.getPickupId(id);
            dlReceipts.deleteSharedTicket(id);
            m_panelticket.setActiveTicket(ticket, null);
            ticket.setPickupId(pickUp);
        }

        updateCount();
    }

    private void setActiveReprintTicket(String id) throws BasicException {
        TicketInfo ticket = dlSales.getReprintTicket(id);
        JOptionPane.showMessageDialog(this, AppLocal.getIntString("NOT IMPLEMENTED YET"));
        LOGGER.log(System.Logger.Level.WARNING, "NOT IMPLEMENTED setActiveReprintTicket");
    }

    private void newTicket() {

        try {
            //IMPORTANT TO SAVE CURRENT TICKET IF
            saveCurrentTicket();

            //CREATE NEW
            TicketInfo ticket = new TicketInfo();
            LOGGER.log(System.Logger.Level.DEBUG, "newTicket has id: " + ticket.getId());
            m_panelticket.setActiveTicket(ticket, null);
            updateCount();

        }
        catch (Exception ex) {
            LOGGER.log(System.Logger.Level.WARNING, "Exception while exec newTicket: ", ex);
        }
    }

    private List<SharedTicketInfo> getSharedTickets() {
        List<SharedTicketInfo> listSharedTicket = new ArrayList<>();
        try {
            AppUser appuser = m_App.getAppUserView().getUser();
            //String roleId = m_App.getAppUserView().getUser().getRole();
            if (m_App.hasPermission("sales.ViewSharedTicket")
                    || m_App.hasPermission("sales.Override")) {
                listSharedTicket = dlReceipts.getSharedTicketList();

            } else {
                listSharedTicket = dlReceipts.getUserSharedTicketList(appuser.getId());
            }
            
            LOGGER.log(System.Logger.Level.DEBUG, "Shared ticket found " + listSharedTicket.size() + " tickets for  user: " + appuser.getName());
        }
        catch (BasicException e) {
            LOGGER.log(System.Logger.Level.WARNING, "Exception get Shared Tickets ", e);
        }
        return listSharedTicket;
    }

    private void fullfillSharedList() {

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {

                try {
                    List<SharedTicketInfo> listSharedTicket = getSharedTickets();

                    JTicketsBagSharedList listDialog = JTicketsBagSharedList.newJDialog(JTicketsBagShared.this);
                    String id = listDialog.showTicketsList(listSharedTicket, dlReceipts);

                    if (id != null) {
                        saveCurrentTicket();
                        setActiveTicket(id);
                    }

                }
                catch (BasicException e) {
                    LOGGER.log(System.Logger.Level.WARNING, "Exception to show selected Ticket: ", e);
                    new MessageInf(e).show(JTicketsBagShared.this);
                }
            }
        });
    }

    protected void setEnabledPanel(boolean enabled) {
        jPanel1.setEnabled(enabled);
    }

    protected void disableAllButtons() {
        m_jDelTicket.setEnabled(false);
        m_jNewTicket.setEnabled(false);
        m_jReprintTickets.setEnabled(false);
        m_jListTickets.setEnabled(false);
        m_jHold.setEnabled(false);
    }

    protected void setEnabledButtonDel(boolean enabled) {
        m_jDelTicket.setEnabled(enabled);
    }

    protected void setEnabledButtonNew(boolean enabled) {
        m_jNewTicket.setEnabled(enabled);
    }

    protected void setEnabledButtonReprint(boolean enabled) {
        m_jReprintTickets.setEnabled(enabled);
    }

    protected void setEnabledButtonList(boolean enabled) {
        m_jListTickets.setEnabled(enabled);
    }

    protected void setEnabledButtonExpand(boolean enabled) {
        m_jHold.setEnabled(enabled);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        m_jNewTicket = new javax.swing.JButton();
        m_jDelTicket = new javax.swing.JButton();
        m_jListTickets = new javax.swing.JButton();
        m_jReprintTickets = new javax.swing.JButton();
        m_jHold = new javax.swing.JButton();

        setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        setLayout(new java.awt.BorderLayout());

        m_jNewTicket.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/sale_new.png"))); // NOI18N
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("pos_messages"); // NOI18N
        m_jNewTicket.setToolTipText(bundle.getString("tooltip.addnewticket")); // NOI18N
        m_jNewTicket.setFocusPainted(false);
        m_jNewTicket.setFocusable(false);
        m_jNewTicket.setMargin(new java.awt.Insets(0, 4, 0, 4));
        m_jNewTicket.setMaximumSize(new java.awt.Dimension(50, 40));
        m_jNewTicket.setMinimumSize(new java.awt.Dimension(50, 40));
        m_jNewTicket.setPreferredSize(new java.awt.Dimension(80, 45));
        m_jNewTicket.setRequestFocusEnabled(false);
        m_jNewTicket.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jNewTicketActionPerformed(evt);
            }
        });
        jPanel1.add(m_jNewTicket);

        m_jDelTicket.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/sale_delete.png"))); // NOI18N
        m_jDelTicket.setToolTipText(bundle.getString("tooltip.delete")); // NOI18N
        m_jDelTicket.setFocusPainted(false);
        m_jDelTicket.setFocusable(false);
        m_jDelTicket.setMargin(new java.awt.Insets(0, 4, 0, 4));
        m_jDelTicket.setMaximumSize(new java.awt.Dimension(50, 40));
        m_jDelTicket.setMinimumSize(new java.awt.Dimension(50, 40));
        m_jDelTicket.setPreferredSize(new java.awt.Dimension(80, 45));
        m_jDelTicket.setRequestFocusEnabled(false);
        m_jDelTicket.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jDelTicketActionPerformed(evt);
            }
        });
        jPanel1.add(m_jDelTicket);

        m_jListTickets.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        m_jListTickets.setForeground(new java.awt.Color(255, 0, 153));
        m_jListTickets.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/sale_pending.png"))); // NOI18N
        m_jListTickets.setToolTipText(bundle.getString("tooltip.layaway")); // NOI18N
        m_jListTickets.setFocusPainted(false);
        m_jListTickets.setFocusable(false);
        m_jListTickets.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        m_jListTickets.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        m_jListTickets.setIconTextGap(1);
        m_jListTickets.setMargin(new java.awt.Insets(0, 2, 0, 2));
        m_jListTickets.setMaximumSize(new java.awt.Dimension(50, 40));
        m_jListTickets.setMinimumSize(new java.awt.Dimension(50, 40));
        m_jListTickets.setPreferredSize(new java.awt.Dimension(80, 45));
        m_jListTickets.setRequestFocusEnabled(false);
        m_jListTickets.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        m_jListTickets.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jListTicketsActionPerformed(evt);
            }
        });
        jPanel1.add(m_jListTickets);

        m_jReprintTickets.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        m_jReprintTickets.setForeground(new java.awt.Color(255, 0, 153));
        m_jReprintTickets.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/reprint24.png"))); // NOI18N
        m_jReprintTickets.setToolTipText(bundle.getString("tooltip.reprint")); // NOI18N
        m_jReprintTickets.setFocusPainted(false);
        m_jReprintTickets.setFocusable(false);
        m_jReprintTickets.setIconTextGap(1);
        m_jReprintTickets.setMargin(new java.awt.Insets(0, 2, 0, 2));
        m_jReprintTickets.setMaximumSize(new java.awt.Dimension(50, 40));
        m_jReprintTickets.setMinimumSize(new java.awt.Dimension(50, 40));
        m_jReprintTickets.setPreferredSize(new java.awt.Dimension(80, 45));
        m_jReprintTickets.setRequestFocusEnabled(false);
        m_jReprintTickets.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        m_jReprintTickets.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jReprintTicketsActionPerformed(evt);
            }
        });
        jPanel1.add(m_jReprintTickets);

        m_jHold.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        m_jHold.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/logout.png"))); // NOI18N
        m_jHold.setToolTipText(bundle.getString("tooltip.quicklogoff")); // NOI18N
        m_jHold.setFocusPainted(false);
        m_jHold.setFocusable(false);
        m_jHold.setMargin(new java.awt.Insets(0, 4, 0, 4));
        m_jHold.setMaximumSize(new java.awt.Dimension(50, 40));
        m_jHold.setMinimumSize(new java.awt.Dimension(50, 40));
        m_jHold.setPreferredSize(new java.awt.Dimension(80, 45));
        m_jHold.setRequestFocusEnabled(false);
        m_jHold.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jHoldActionPerformed(evt);
            }
        });
        jPanel1.add(m_jHold);

        add(jPanel1, java.awt.BorderLayout.WEST);
    }// </editor-fold>//GEN-END:initComponents

    private void m_jListTicketsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jListTicketsActionPerformed

        if (hasPermissionShowSharedList) {
            fullfillSharedList();
        }

    }//GEN-LAST:event_m_jListTicketsActionPerformed

    private void m_jDelTicketActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jDelTicketActionPerformed

        boolean pinOK = false;

        if (m_panelticket.getActiveTicket() != null) {

            if (m_App.getProperties().getProperty("override.check").equals("true")) {
                String pin = m_App.getProperties().getProperty("override.pin");
                String iValue = JPasswordDialog.showEditor(this, AppLocal.getIntString("title.override.enterpin"));

                if (iValue != null && iValue.equals(pin)) {
                    pinOK = true;
                    int res = JOptionPane.showConfirmDialog(this,
                            AppLocal.getIntString("message.wannadelete"),
                            AppLocal.getIntString("title.editor"),
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE);

                    if (res == JOptionPane.YES_OPTION) {
                        deleteTicket();
                    }
                } else {
                    pinOK = false;
                    JOptionPane.showMessageDialog(this, AppLocal.getIntString("message.override.badpin"));
                }
            }
        }

    }//GEN-LAST:event_m_jDelTicketActionPerformed

    private void m_jNewTicketActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jNewTicketActionPerformed

        newTicket();

    }//GEN-LAST:event_m_jNewTicketActionPerformed

    private void m_jHoldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jHoldActionPerformed
        deactivate();
        m_App.closeAppView();
    }//GEN-LAST:event_m_jHoldActionPerformed

    private void m_jReprintTicketsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jReprintTicketsActionPerformed
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {

                try {
                    if (!m_App.hasPermission("sales.ReprintTicket")) {
                        JOptionPane.showMessageDialog(JTicketsBagShared.this,
                                AppLocal.getIntString("message.reprintticketnotallow"),
                                AppLocal.getIntString("message.reprintticketnotallowtitle"),
                                JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        /*
                        if ("0".equals(m_App.getAppUserView().getUser().getRole())
                                || "1".equals(m_App.getAppUserView().getUser().getRole())
                                || m_App.hasPermission("sales.ViewSharedTicket")
                                || m_App.hasPermission("sales.Override")) {
                            
                        }
                         */

                        List<ReprintTicketInfo> l = dlSales.getReprintTicketList();
                        JTicketsReprintList listDialog = JTicketsReprintList.newJDialog(JTicketsBagShared.this);
                        String id = listDialog.showTicketsList(l, dlSales);

                        if (id != null) {
                            saveCurrentTicket();
                            setActiveReprintTicket(id);
                        }

                    }
                }
                catch (BasicException e) {
                    LOGGER.log(System.Logger.Level.WARNING, "Exception ReprintTicketsAction: ", e);
                    new MessageInf(e).show(JTicketsBagShared.this);
                }
            }
        });
    }//GEN-LAST:event_m_jReprintTicketsActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private javax.swing.JButton m_jDelTicket;
    private javax.swing.JButton m_jHold;
    private javax.swing.JButton m_jListTickets;
    private javax.swing.JButton m_jNewTicket;
    private javax.swing.JButton m_jReprintTickets;
    // End of variables declaration//GEN-END:variables

}
