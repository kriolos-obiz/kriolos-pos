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


import com.openbravo.pos.sales.ReprintTicketInfo;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import com.openbravo.pos.printer.TicketPrinterException;
import javax.swing.JOptionPane;
import com.openbravo.pos.sales.TaxesException;
import com.openbravo.pos.sales.TaxesLogic;

import com.openbravo.basic.BasicException;
import com.openbravo.data.gui.ListKeyed;
import com.openbravo.data.gui.MessageInf;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.AppProperties;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.DataLogicSales;
import com.openbravo.pos.forms.DataLogicSystem;
import com.openbravo.pos.printer.DeviceTicket;
import com.openbravo.pos.printer.TicketParser;
import com.openbravo.pos.scripting.ScriptEngine;
import com.openbravo.pos.scripting.ScriptException;
import com.openbravo.pos.scripting.ScriptFactory;
import com.openbravo.pos.ticket.TicketInfo;
import com.openbravo.pos.ticket.TicketTaxInfo;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author JG uniCenta
 */
public class JTicketsReprintList extends javax.swing.JDialog {
    
    private String m_sDialogTicket;
    private final DeviceTicket m_TP;    
    private final TicketParser m_TTP;    
//    private final TicketParser m_TTP2;     
    private TaxesLogic taxeslogic;
    private ListKeyed taxcollection;

    private TicketInfo m_ticket;
    private TicketInfo m_ticketCopy;
    private AppView m_App;    
    
    private DataLogicSystem dlSystem = null;
    private DataLogicSales dlSales = null;   
    
    /** Creates new form JTicketsReprintList */
    private JTicketsReprintList(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        AppView app = null;
        m_App = app;
        AppProperties props = null;
        
        m_TP = new DeviceTicket();        

        m_TTP = new TicketParser(m_TP, dlSystem);
//        m_TTP2 = new TicketParser(m_App.getDeviceTicket(), dlSystem);          
    }
    /** Creates new form JTicketsReprintList */
    private JTicketsReprintList(java.awt.Dialog parent, boolean modal) {
        super(parent, modal);
        AppProperties props = null;

        m_TP = new DeviceTicket();        
        m_TTP = new TicketParser(m_TP, dlSystem);
    }

    /**
     *
     * @param atickets
     * @param dlSales
     * @return
     */
    public String showTicketsList(java.util.List<ReprintTicketInfo> atickets, DataLogicSales dlSales) {
        
        m_ticket = null;
        m_ticketCopy = null;        

ReprintTicketInfo m_Ticket = null;
        
        for (ReprintTicketInfo aticket : atickets) {
            m_jtickets.add(new JButtonTicket(aticket, dlSales));
        }  
     
        m_sDialogTicket = null;

        int lsize = atickets.size();
        if (lsize > 0) {
            setVisible(true);
        }else{
            JOptionPane.showMessageDialog(this,
                AppLocal.getIntString("message.nosharedtickets"), 
                AppLocal.getIntString("message.sharedtickettitle"), 
                JOptionPane.OK_OPTION);            
        }

        return m_sDialogTicket;
    }

    /**
     *
     * @param ticketsbagshared
     * @return
     */
    public static JTicketsReprintList newJDialog(JTicketsBagShared ticketsbagshared) {
        
        Window window = getWindow(ticketsbagshared);
        JTicketsReprintList mydialog;
        if (window instanceof Frame) { 
            mydialog = new JTicketsReprintList((Frame) window, true);
        } else {
            mydialog = new JTicketsReprintList((Dialog) window, true);
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
        
        private final ReprintTicketInfo m_Ticket;
        
        public JButtonTicket(ReprintTicketInfo ticket, DataLogicSales dlSales){
            
            super();
            
            m_Ticket = ticket;
            setFocusPainted(false);
            setFocusable(false);
            setRequestFocusEnabled(false);
            setMargin(new Insets(8, 14, 8, 14));
            setFont(new java.awt.Font ("Dialog", 0, 14));
            setBackground(new java.awt.Color (220, 220, 220));
            addActionListener(new ActionListenerImpl());
            
            setText(ticket.getId() + " - " +
                    ticket.getTicketDate() + " - " +
                    ticket.getUserName());               
        }

        private class ActionListenerImpl implements ActionListener {

            public ActionListenerImpl() {
            }

            @Override
            public void actionPerformed(ActionEvent evt) {
                        
                try {
                    m_sDialogTicket = m_Ticket.getId();
                    
                    JTicketsReprintList.this.setVisible(false);
                    int iTkt=Integer.valueOf(m_sDialogTicket);
                    int iTt = 0;
//            readTicket(iTkt, iTt);
// readTicket(m_sDialogTicket);
                    
                    TicketInfo ticket = dlSales.loadTicket(iTt, iTkt);

                    if (ticket == null) {
                        JFrame frame = new JFrame();
                        JOptionPane.showMessageDialog(frame, AppLocal.getIntString("message.notexiststicket"), AppLocal.getIntString("message.notexiststickettitle"), JOptionPane.WARNING_MESSAGE);
                    } else {
                        m_ticket = ticket;
                        m_ticketCopy = null;
                        try {
                            taxeslogic.calculateTaxes(m_ticket);
                            TicketTaxInfo[] taxlist = m_ticket.getTaxLines();
                        } catch (TaxesException ex) {
                        }
                        printTicket("Printer.ReprintLastTicket", m_ticket, null);
                    }
//            } catch (BasicException e) {
//                MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotloadticket"), e);
//                msg.show(this);
//            }
//        }                        
                } catch (BasicException ex) {
                    Logger.getLogger(JTicketsReprintList.class.getName()).log(Level.SEVERE, null, ex);
                }
                        
                        
            }
        }
    }
    
//    private void readTicket(int iTicketid, int iTickettype) {
    private void readTicket(String Id) {    
//        Integer findTicket=Integer.valueOf(m_sDialogTicket);    

        try {
        //TicketInfo ticket = new TicketInfo();             
   
//            TicketInfo ticket = dlSales.loadTicket(iTickettype, iTicketid);

TicketInfo ticket = dlSales.getReprintTicket(Id);
            if (ticket == null) {
                JFrame frame = new JFrame();
                JOptionPane.showMessageDialog(frame,
                    AppLocal.getIntString("message.notexiststicket"),
                    AppLocal.getIntString("message.notexiststickettitle"),
                    JOptionPane.WARNING_MESSAGE);
                
            } else {
                m_ticket = ticket;
                m_ticketCopy = null;

                    if(m_ticket.getTicketType()== 1 
                        || m_ticket.getTicketStatus()> 0) {
                        JFrame frame = new JFrame();
                            JOptionPane.showMessageDialog(frame,
                            AppLocal.getIntString("message.ticketrefunded"),
                            AppLocal.getIntString("message.ticketrefundedtitle"),
                            JOptionPane.WARNING_MESSAGE);
//                        m_jEdit.setEnabled(false);
//                        m_jRefund.setEnabled(false);                            
                    }else{
//                        m_jEdit.setEnabled(true);
//                        m_jRefund.setEnabled(true);
                    }
                try {
                    taxeslogic.calculateTaxes(m_ticket);
                    TicketTaxInfo[] taxlist = m_ticket.getTaxLines();
                } catch (TaxesException ex) {}

//                printTicket();
            }
            
        } catch (BasicException e) {
            MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotloadticket"), e);
            msg.show(this);
        }
     
    }
    
/*    private void printTicket() {
         
        if (m_ticket != null
                && (m_ticket.getTicketType() == TicketInfo.RECEIPT_NORMAL
                    &&  m_ticket.getTicketStatus() == 0)) {
         }

        m_TP.getDevicePrinter("1").reset();
        
        if (m_ticket == null) {
//            m_jTicketId.setText(m_ticket.getName());
            
            try {
                ScriptEngine script = ScriptFactory.getScriptEngine(ScriptFactory.VELOCITY);
                script.put("ticket", m_ticket);
                script.put("taxes", m_ticket.getTaxLines());                
                m_TTP.printTicket(script.eval(dlSystem.getResourceAsXML("Printer.TicketPreview")).toString());
            } catch (    ScriptException | TicketPrinterException e) {
                MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotprintticket"), e);
                msg.show(this);
            }
        }
    }
*/
    
    private void printTicket(String sresourcename, TicketInfo ticket, Object ticketext) {

        String sresource = dlSystem.getResourceAsXML(sresourcename);
        if (sresource == null) {
            MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotprintticket"));
//            msg.show(JPanelTicket.this);
        } else {

// if this is ticket does not have a pickup code assign on now            
            if (ticket.getPickupId() == 0) {
                try {
                    ticket.setPickupId(dlSales.getNextPickupIndex());
                } catch (BasicException e) {
                    ticket.setPickupId(0);
                }
            }
            try {
                ScriptEngine script = ScriptFactory.getScriptEngine(ScriptFactory.VELOCITY);
// JG 19 Feb 14 unnecessary boolean parse - if (Boolean.valueOf(m_App.getProperties().getProperty("receipt.newlayout")).booleanValue()){
                if (Boolean.parseBoolean(m_App.getProperties().getProperty("receipt.newlayout"))) {
                    script.put("taxes", ticket.getTaxLines());
                } else {
                    script.put("taxes", taxcollection);
                }
                script.put("taxeslogic", taxeslogic);
                script.put("ticket", ticket);
                script.put("place", ticketext);
                Object warrantyPrint;
//                script.put("warranty", warrantyPrint);
//                script.put("pickupid", getPickupString(ticket));

// JG Aug 2014
//                refreshTicket();

                m_TTP.printTicket(script.eval(sresource).toString(), ticket);

// JG May 2013 replaced with Multicatch            
            } catch (ScriptException | TicketPrinterException e) {
                MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotprintticket"), e);
//                msg.show(JPanelTicket.this);
            }
        }
    }    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
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

        setSize(new java.awt.Dimension(519, 399));
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
