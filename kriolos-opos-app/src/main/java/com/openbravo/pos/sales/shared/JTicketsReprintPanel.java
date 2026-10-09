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
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.DataLogicSystem;
import com.openbravo.pos.forms.ResourceService;
import com.openbravo.pos.sales.TicketLifecycleService;
import com.openbravo.pos.hardware.PosHardwareManager;
import com.openbravo.pos.printer.TicketParser;
import com.openbravo.pos.printer.TicketPrinterException;
import com.openbravo.pos.sales.ReprintTicketInfo;
import com.openbravo.pos.scripting.ScriptEngine;
import com.openbravo.pos.scripting.ScriptException;
import com.openbravo.pos.scripting.ScriptFactory;
import com.openbravo.pos.ticket.TicketInfo;
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
import org.openide.util.Exceptions;

public class JTicketsReprintPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(JTicketsReprintPanel.class.getName());

    private String currentTicketId;
    private AppView appView;

    private final ResourceService resourceService;
    @Deprecated
    private final DataLogicSystem dlSystem;
    private final TicketLifecycleService ticketLifecycleService;
    private PosUIModal modalContext;

    public JTicketsReprintPanel(AppView app) {
        this.appView = app;
        this.resourceService = appView.getBean(ResourceService.class);
        this.dlSystem = (resourceService instanceof DataLogicSystem) ? (DataLogicSystem) resourceService : null;
        this.ticketLifecycleService = appView.getBean(TicketLifecycleService.class);
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
        return currentTicketId;
    }

    public void loadTickets() {
        try {
            currentTicketId = null;
            m_jtickets.removeAll();

            List<ReprintTicketInfo> atickets = ticketLifecycleService.getReprintTicketList();

            for (ReprintTicketInfo aticket : atickets) {
                m_jtickets.add(new JButtonTicket(aticket));
            }

            revalidate();
            repaint();
        }
        catch (BasicException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public static String show(Component parent, AppView app) {
        /*
        if (atickets == null || atickets.isEmpty()) {
            JOptionPane.showMessageDialog(parent,
                    AppLocal.getIntString("message.nosharedtickets"),
                    AppLocal.getIntString("message.sharedtickettitle"),
                    JOptionPane.OK_OPTION);
            return null;
        }
         */
        JTicketsReprintPanel panel = new JTicketsReprintPanel(app);
        panel.loadTickets();
        PosUIModal modal = PosUIModal.create(parent, panel)
                .setTitle(AppLocal.getIntString("caption.tickets"))
                .setModal(true)
                .setResizable(false);
        panel.setModalContext(modal);
        modal.show();
        return panel.getSelectedTicketId();
    }

    private class JButtonTicket extends JButton {

        private final ReprintTicketInfo ticketInfo;

        public JButtonTicket(ReprintTicketInfo ticket) {
            super();
            this.ticketInfo = ticket;
            setFocusPainted(false);
            setFocusable(false);
            setRequestFocusEnabled(false);
            setMargin(new Insets(8, 14, 8, 14));
            setFont(new Font("Arial", Font.PLAIN, 14));
            addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent evt) {
                    try {
                        currentTicketId = ticketInfo.getId();
                        if (modalContext != null) {
                            modalContext.setResult(currentTicketId);
                            modalContext.close();
                        }

                        int ticketId = Integer.parseInt(currentTicketId);
                        int ticketType = 0;
                        TicketInfo ticketInfoOriginal = ticketLifecycleService.loadTicket(ticketType, ticketId);

                        if (ticketInfoOriginal != null) {
                            printTicket(ticketInfoOriginal, null);
                        } else {
                            JFrame frame = new JFrame();
                            JOptionPane.showMessageDialog(frame, AppLocal.getIntString("message.notexiststicket"), AppLocal.getIntString("message.notexiststickettitle"), JOptionPane.WARNING_MESSAGE);

                        }
                    }
                    catch (BasicException ex) {
                        LOGGER.log(Level.SEVERE, "Exception on ", ex);
                    }
                }
            });

            setText(ticket.getId() + " - " + ticket.getTicketDate() + " - " + ticket.getUserName());
        }
    }

    private void printTicket(TicketInfo ticket, Object ticketext) {

        String sresourcename = "Printer.ReprintLastTicket";
        String sresource = (resourceService != null) ? resourceService.getResourceAsXML(sresourcename) : null;
        try {
            ScriptEngine script = ScriptFactory.getScriptEngine(ScriptFactory.VELOCITY);
            script.put("taxes", ticket.getTaxLines());
            script.put("ticket", ticket);
            script.put("place", ticketext);

            TicketParser ticketParser = this.appView.createTicketParser(PosHardwareManager.createPreviewTicketDevice());
            ticketParser.printTicket(script.eval(sresource).toString(), ticket);
        }
        catch (ScriptException | TicketPrinterException e) {
            MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotprintticket"), e);
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
