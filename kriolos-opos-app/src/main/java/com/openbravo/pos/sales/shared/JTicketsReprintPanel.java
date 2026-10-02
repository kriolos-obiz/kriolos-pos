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

package com.openbravo.pos.sales.shared;

import com.openbravo.basic.BasicException;
import com.openbravo.data.gui.MessageInf;
import com.openbravo.data.gui.modal.PosUIModal;
import com.openbravo.data.gui.ListKeyed;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.DataLogicSales;
import com.openbravo.pos.forms.DataLogicSystem;
import com.openbravo.pos.printer.DeviceTicket;
import com.openbravo.pos.printer.TicketParser;
import com.openbravo.pos.printer.TicketPrinterException;
import com.openbravo.pos.sales.ReprintTicketInfo;
import com.openbravo.pos.sales.TaxesException;
import com.openbravo.pos.sales.TaxesLogic;
import com.openbravo.pos.scripting.ScriptEngine;
import com.openbravo.pos.scripting.ScriptException;
import com.openbravo.pos.scripting.ScriptFactory;
import com.openbravo.pos.ticket.TicketInfo;
import com.openbravo.pos.ticket.TicketTaxInfo;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class JTicketsReprintPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(JTicketsReprintPanel.class.getName());

    private String m_sDialogTicket;
    private final DeviceTicket m_TP;
    private final TicketParser m_TTP;
    private TaxesLogic taxeslogic;
    private ListKeyed taxcollection;

    private TicketInfo m_ticket;
    private TicketInfo m_ticketCopy;
    private AppView m_App;

    private DataLogicSystem dlSystem = null;
    private DataLogicSales dlSales = null;
    private PosUIModal modalContext;

    public JTicketsReprintPanel() {
        this(null);
    }

    public JTicketsReprintPanel(AppView app) {
        this.m_App = app;
        this.m_TP = new DeviceTicket();
        this.m_TTP = new TicketParser(m_TP, dlSystem);

        initComponents();
        initDomainAdapters();

        jScrollPane1.getVerticalScrollBar().setPreferredSize(new Dimension(35, 35));
        jScrollPane1.getHorizontalScrollBar().setPreferredSize(new Dimension(25, 25));
    }

    private void initDomainAdapters() {
        setName("kriolos:sales:tickets-reprint-panel");
        m_jButtonCancel.setName("kriolos:sales:btn-cancel");
    }

    public void setModalContext(PosUIModal modalContext) {
        this.modalContext = modalContext;
    }

    public String getSelectedTicketId() {
        return m_sDialogTicket;
    }

    public void loadTickets(List<ReprintTicketInfo> atickets, DataLogicSales dlSales) {
        this.dlSales = dlSales;
        m_ticket = null;
        m_ticketCopy = null;
        m_sDialogTicket = null;
        m_jtickets.removeAll();

        for (ReprintTicketInfo aticket : atickets) {
            m_jtickets.add(new JButtonTicket(aticket, dlSales));
        }

        revalidate();
        repaint();
    }

    public static String show(Component parent, List<ReprintTicketInfo> atickets, DataLogicSales dlSales, AppView app) {
        if (atickets == null || atickets.isEmpty()) {
            JOptionPane.showMessageDialog(parent,
                    AppLocal.getIntString("message.nosharedtickets"),
                    AppLocal.getIntString("message.sharedtickettitle"),
                    JOptionPane.OK_OPTION);
            return null;
        }

        JTicketsReprintPanel panel = new JTicketsReprintPanel(app);
        panel.loadTickets(atickets, dlSales);
        PosUIModal modal = PosUIModal.create(parent, panel)
                .setTitle(AppLocal.getIntString("caption.tickets"))
                .setModal(true)
                .setResizable(false);
        panel.setModalContext(modal);
        modal.show();
        return panel.getSelectedTicketId();
    }

    private class JButtonTicket extends JButton {

        private final ReprintTicketInfo m_Ticket;

        public JButtonTicket(ReprintTicketInfo ticket, DataLogicSales dlSales) {
            super();
            this.m_Ticket = ticket;
            setFocusPainted(false);
            setFocusable(false);
            setRequestFocusEnabled(false);
            setMargin(new Insets(8, 14, 8, 14));
            setFont(new Font("Arial", Font.PLAIN, 14));
            addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent evt) {
                    try {
                        m_sDialogTicket = m_Ticket.getId();
                        if (modalContext != null) {
                            modalContext.setResult(m_sDialogTicket);
                            modalContext.close();
                        }

                        int iTkt = Integer.parseInt(m_sDialogTicket);
                        int iTt = 0;
                        TicketInfo ticketLoaded = dlSales.loadTicket(iTt, iTkt);

                        if (ticketLoaded == null) {
                            JFrame frame = new JFrame();
                            JOptionPane.showMessageDialog(frame, AppLocal.getIntString("message.notexiststicket"), AppLocal.getIntString("message.notexiststickettitle"), JOptionPane.WARNING_MESSAGE);
                        } else {
                            m_ticket = ticketLoaded;
                            m_ticketCopy = null;
                            try {
                                if (taxeslogic != null) {
                                    taxeslogic.calculateTaxes(m_ticket);
                                    TicketTaxInfo[] taxlist = m_ticket.getTaxLines();
                                }
                            } catch (TaxesException ex) {
                            }
                            printTicket("Printer.ReprintLastTicket", m_ticket, null);
                        }
                    } catch (BasicException ex) {
                        LOGGER.log(Level.SEVERE, null, ex);
                    }
                }
            });

            setText(ticket.getId() + " - " + ticket.getTicketDate() + " - " + ticket.getUserName());
        }
    }

    private void printTicket(String sresourcename, TicketInfo ticket, Object ticketext) {
        if (dlSystem == null) {
            return;
        }
        String sresource = dlSystem.getResourceAsXML(sresourcename);
        if (sresource == null) {
            MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotprintticket"));
        } else {
            if (ticket.getPickupId() == 0) {
                try {
                    ticket.setPickupId(dlSales.getNextPickupIndex());
                } catch (BasicException e) {
                    ticket.setPickupId(0);
                }
            }
            try {
                ScriptEngine script = ScriptFactory.getScriptEngine(ScriptFactory.VELOCITY);
                if (m_App != null && Boolean.parseBoolean(m_App.getProperties().getProperty("receipt.newlayout"))) {
                    script.put("taxes", ticket.getTaxLines());
                } else {
                    script.put("taxes", taxcollection);
                }
                script.put("taxeslogic", taxeslogic);
                script.put("ticket", ticket);
                script.put("place", ticketext);

                m_TTP.printTicket(script.eval(sresource).toString(), ticket);
            } catch (ScriptException | TicketPrinterException e) {
                MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotprintticket"), e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new JPanel();
        jScrollPane1 = new JScrollPane();
        jPanel2 = new JPanel();
        m_jtickets = new JPanel();
        jPanel3 = new JPanel();
        jPanel4 = new JPanel();
        m_jButtonCancel = new JButton();

        setLayout(new BorderLayout());

        jPanel1.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jPanel1.setLayout(new BorderLayout());

        jPanel2.setFont(new Font("Arial", 0, 14)); // NOI18N
        jPanel2.setLayout(new BorderLayout());

        m_jtickets.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        m_jtickets.setLayout(new GridLayout(0, 1, 5, 5));
        jPanel2.add(m_jtickets, BorderLayout.NORTH);

        jScrollPane1.setViewportView(jPanel2);

        jPanel1.add(jScrollPane1, BorderLayout.CENTER);

        add(jPanel1, BorderLayout.CENTER);

        jPanel3.setLayout(new FlowLayout(FlowLayout.RIGHT));
        jPanel3.add(jPanel4);

        m_jButtonCancel.setFont(new Font("Arial", 0, 12)); // NOI18N
        m_jButtonCancel.setIcon(new ImageIcon(getClass().getResource("/com/openbravo/images/cancel.png"))); // NOI18N
        m_jButtonCancel.setText(AppLocal.getIntString("button.close")); // NOI18N
        m_jButtonCancel.setFocusPainted(false);
        m_jButtonCancel.setFocusable(false);
        m_jButtonCancel.setMargin(new Insets(8, 8, 8, 8));
        m_jButtonCancel.setPreferredSize(new Dimension(100, 45));
        m_jButtonCancel.setRequestFocusEnabled(false);
        m_jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jButtonCancelActionPerformed(evt);
            }
        });
        jPanel3.add(m_jButtonCancel);

        add(jPanel3, BorderLayout.SOUTH);
    }// </editor-fold>//GEN-END:initComponents

    private void m_jButtonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jButtonCancelActionPerformed
        if (modalContext != null) {
            modalContext.close();
        }
    }//GEN-LAST:event_m_jButtonCancelActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JPanel jPanel1;
    private JPanel jPanel2;
    private JPanel jPanel3;
    private JPanel jPanel4;
    private JScrollPane jScrollPane1;
    private JButton m_jButtonCancel;
    private JPanel m_jtickets;
    // End of variables declaration//GEN-END:variables
}
