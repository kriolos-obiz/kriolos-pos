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

import com.openbravo.pos.sales.SharedTicketInfo;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import com.openbravo.basic.BasicException;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.ticket.TicketInfo;
import com.openbravo.pos.sales.DataLogicReceipts;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;

/**
 *
 * @author JG uniCenta
 */
public class JTicketsBagSharedList extends javax.swing.JDialog {
    
    
    private final static Logger LOGGER = Logger.getLogger(JTicketsBagSharedList.class.getName());

    private String m_sDialogTicket;

    /**
     * Creates new form JTicketsBagSharedList
     */
    private JTicketsBagSharedList(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
    }

    /**
     * Creates new form JTicketsBagSharedList
     */
    private JTicketsBagSharedList(java.awt.Dialog parent, boolean modal) {
        super(parent, modal);
    }

    /**
     *
     * @param atickets
     * @param dlReceipts
     * @return
     */
    public String showTicketsList(java.util.List<SharedTicketInfo> atickets, DataLogicReceipts dlReceipts) {

        m_sDialogTicket = null;

        for (SharedTicketInfo aticket : atickets) {

            String label = aticket.getName() + " : "+aticket.getUserName();
            try {
                TicketInfo ticket2 = dlReceipts.getSharedTicket(aticket.getId());
                if(ticket2 != null){
                    label += " : " + ticket2.printTotal();
                }else{
                    LOGGER.log(Level.SEVERE, "Cannot recovery TicketInfo for ticket Id: "+aticket.getId());
                }
            } catch (BasicException ex) {
                label += "!!!!!";
                LOGGER.log(Level.SEVERE, "Exception on recovery TicketInfo for ticket Id: "+aticket.getId(), ex);
            }

            m_jtickets.add(new JButtonTicket(aticket, label));
        }

        if (atickets.size() > 0) {
            setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this,
                    AppLocal.getIntString("message.nosharedtickets"),
                    AppLocal.getIntString("message.sharedtickettitle"),
                    JOptionPane.OK_OPTION);
        }

        return m_sDialogTicket;
    }
    
    private void creteTableTicket(String[][] data){
    
        // Column Names
        String[] columnNames = { "Ticket Date", "Total", "User", "Action" };

        JTable j = new JTable(data, columnNames);
        //j.setBounds(30, 40, 200, 300);

        JScrollPane sp = new JScrollPane(j);
    }

    /**
     *
     * @param ticketsbagshared
     * @return
     */
    public static JTicketsBagSharedList newJDialog(JTicketsBagShared ticketsbagshared) {

        Window window = getWindow(ticketsbagshared);
        JTicketsBagSharedList mydialog;
        if (window instanceof Frame) {
            mydialog = new JTicketsBagSharedList((Frame) window, true);
        } else {
            mydialog = new JTicketsBagSharedList((Dialog) window, true);
        }

        mydialog.initComponents();

        mydialog.jScrollPane1.getVerticalScrollBar().setPreferredSize(new Dimension(35, 35));
        mydialog.jScrollPane1.getHorizontalScrollBar().setPreferredSize(new Dimension(25, 25));

        return mydialog;
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

    private class JButtonTicket extends JButton {

        private final SharedTicketInfo m_Ticket;

        public JButtonTicket(SharedTicketInfo ticket, String label) {

            super();

            this.m_Ticket = ticket;
            setFocusPainted(false);
            setFocusable(false);
            setRequestFocusEnabled(false);
            setMargin(new Insets(8, 14, 8, 14));
            setFont(new java.awt.Font("Dialog", 0, 14));
            //setBackground(new java.awt.Color(220, 220, 220));
            addActionListener(new ActionListenerImpl());
            setText(label);
        }

        private class ActionListenerImpl implements ActionListener {

            public ActionListenerImpl() {}

            @Override
            public void actionPerformed(ActionEvent evt) {
                m_sDialogTicket = m_Ticket.getId();
                JTicketsBagSharedList.this.setVisible(false);
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel2 = new javax.swing.JPanel();
        m_jtickets = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        m_jButtonCancel = new javax.swing.JButton();

        setTitle(AppLocal.getIntString("caption.tickets")); // NOI18N
        setResizable(false);

        jPanel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jPanel1.setLayout(new java.awt.BorderLayout());

        jPanel2.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jPanel2.setLayout(new java.awt.BorderLayout());

        m_jtickets.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        m_jtickets.setLayout(new java.awt.GridLayout(0, 1, 5, 5));
        jPanel2.add(m_jtickets, java.awt.BorderLayout.NORTH);

        jScrollPane1.setViewportView(jPanel2);

        jPanel1.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        jPanel3.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));
        jPanel3.add(jPanel4);

        m_jButtonCancel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        m_jButtonCancel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/cancel.png"))); // NOI18N
        m_jButtonCancel.setText(AppLocal.getIntString("button.close")); // NOI18N
        m_jButtonCancel.setFocusPainted(false);
        m_jButtonCancel.setFocusable(false);
        m_jButtonCancel.setMargin(new java.awt.Insets(8, 8, 8, 8));
        m_jButtonCancel.setPreferredSize(new java.awt.Dimension(100, 45));
        m_jButtonCancel.setRequestFocusEnabled(false);
        m_jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jButtonCancelActionPerformed(evt);
            }
        });
        jPanel3.add(m_jButtonCancel);

        getContentPane().add(jPanel3, java.awt.BorderLayout.SOUTH);

        setSize(new java.awt.Dimension(458, 334));
        //REMOVED SET LOCATION RELATIVE TO
    }// </editor-fold>//GEN-END:initComponents

    private void m_jButtonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jButtonCancelActionPerformed

        dispose();

    }//GEN-LAST:event_m_jButtonCancelActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton m_jButtonCancel;
    private javax.swing.JPanel m_jtickets;
    // End of variables declaration//GEN-END:variables

}
