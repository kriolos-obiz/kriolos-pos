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

package com.openbravo.pos.sales;

import com.openbravo.data.gui.modal.PosUIModal;
import com.openbravo.pos.customers.CustomerService;
import com.openbravo.pos.customers.DataLogicCustomers;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.DataLogicSales;
import com.openbravo.pos.ticket.TicketInfo;
import com.openbravo.pos.ticket.TicketLineInfo;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;

public class ReceiptSplitPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private boolean accepted;
    private SimpleReceipt receiptone;
    private SimpleReceipt receipttwo;
    private PosUIModal modalContext;

    public ReceiptSplitPanel() {
        initComponents();
        initDomainAdapters();
    }

    public ReceiptSplitPanel(String ticketline, CustomerService customerService, TaxesLogic taxeslogic) {
        initComponents();
        initDomainAdapters();
        if (getRootPane() != null) {
            getRootPane().setDefaultButton(m_jButtonOK);
        }

        receiptone = new SimpleReceipt(ticketline, customerService, taxeslogic);
        receiptone.setCustomerEnabled(false);
        jPanel5.add(receiptone, BorderLayout.CENTER);

        receipttwo = new SimpleReceipt(ticketline, customerService, taxeslogic);
        jPanel3.add(receipttwo, BorderLayout.CENTER);
    }

    @Deprecated
    public ReceiptSplitPanel(String ticketline, DataLogicCustomers dlCustomers, TaxesLogic taxeslogic) {
        this(ticketline, (CustomerService) dlCustomers, taxeslogic);
    }

    @Deprecated
    public ReceiptSplitPanel(String ticketline, DataLogicSales dlSales, DataLogicCustomers dlCustomers, TaxesLogic taxeslogic) {
        this(ticketline, (CustomerService) dlCustomers, taxeslogic);
    }

    private void initDomainAdapters() {
        setName("kriolos:sales:receipt-split-panel");
        m_jButtonOK.setName("kriolos:sales:btn-ok");
        m_jButtonCancel.setName("kriolos:sales:btn-cancel");
        jBtnToRightAll.setName("kriolos:sales:btn-to-right-all");
        jBtnToRightOne.setName("kriolos:sales:btn-to-right-one");
        jBtnToLeftOne.setName("kriolos:sales:btn-to-left-one");
        jBtnToLeftAll.setName("kriolos:sales:btn-to-left-all");
    }

    public void setModalContext(PosUIModal modalContext) {
        this.modalContext = modalContext;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public SimpleReceipt getReceiptOne() {
        return receiptone;
    }

    public SimpleReceipt getReceiptTwo() {
        return receipttwo;
    }

    public void setTickets(TicketInfo ticket, TicketInfo ticket2, String ticketext) {
        receiptone.setTicket(ticket, ticketext);
        receipttwo.setTicket(ticket2, ticketext);
    }

    public static boolean show(Component parent, String ticketline, CustomerService customerService, TaxesLogic taxeslogic, TicketInfo ticket, TicketInfo ticket2, String ticketext) {
        ReceiptSplitPanel panel = new ReceiptSplitPanel(ticketline, customerService, taxeslogic);
        panel.setTickets(ticket, ticket2, ticketext);
        PosUIModal modal = PosUIModal.create(parent, panel)
                .setTitle(AppLocal.getIntString("caption.split"))
                .setModal(true)
                .setResizable(false);
        panel.setModalContext(modal);
        modal.show();
        return panel.isAccepted();
    }

    @Deprecated
    public static boolean show(Component parent, String ticketline, DataLogicCustomers dlCustomers, TaxesLogic taxeslogic, TicketInfo ticket, TicketInfo ticket2, String ticketext) {
        return show(parent, ticketline, (CustomerService) dlCustomers, taxeslogic, ticket, ticket2, ticketext);
    }

    @Deprecated
    public static boolean show(Component parent, String ticketline, DataLogicSales dlSales, DataLogicCustomers dlCustomers, TaxesLogic taxeslogic, TicketInfo ticket, TicketInfo ticket2, String ticketext) {
        return show(parent, ticketline, (CustomerService) dlCustomers, taxeslogic, ticket, ticket2, ticketext);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        GridBagConstraints gridBagConstraints;

        jPanel2 = new JPanel();
        m_jButtonCancel = new JButton();
        m_jButtonOK = new JButton();
        jPanel1 = new JPanel();
        jPanel5 = new JPanel();
        jPanel4 = new JPanel();
        jBtnToRightAll = new JButton();
        jBtnToRightOne = new JButton();
        jBtnToLeftOne = new JButton();
        jBtnToLeftAll = new JButton();
        jPanel3 = new JPanel();

        setLayout(new BorderLayout());

        jPanel2.setLayout(new FlowLayout(FlowLayout.RIGHT));

        m_jButtonCancel.setFont(new Font("Arial", 0, 12)); // NOI18N
        m_jButtonCancel.setIcon(new ImageIcon(getClass().getResource("/com/openbravo/images/cancel.png"))); // NOI18N
        m_jButtonCancel.setText(AppLocal.getIntString("button.cancel")); // NOI18N
        m_jButtonCancel.setFocusPainted(false);
        m_jButtonCancel.setFocusable(false);
        m_jButtonCancel.setMargin(new Insets(8, 16, 8, 16));
        m_jButtonCancel.setPreferredSize(new Dimension(110, 45));
        m_jButtonCancel.setRequestFocusEnabled(false);
        m_jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jButtonCancelActionPerformed(evt);
            }
        });
        jPanel2.add(m_jButtonCancel);

        m_jButtonOK.setFont(new Font("Arial", 0, 12)); // NOI18N
        m_jButtonOK.setIcon(new ImageIcon(getClass().getResource("/com/openbravo/images/ok.png"))); // NOI18N
        m_jButtonOK.setText(AppLocal.getIntString("button.ok")); // NOI18N
        m_jButtonOK.setFocusPainted(false);
        m_jButtonOK.setFocusable(false);
        m_jButtonOK.setMargin(new Insets(8, 16, 8, 16));
        m_jButtonOK.setPreferredSize(new Dimension(110, 45));
        m_jButtonOK.setRequestFocusEnabled(false);
        m_jButtonOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jButtonOKActionPerformed(evt);
            }
        });
        jPanel2.add(m_jButtonOK);

        add(jPanel2, BorderLayout.SOUTH);

        jPanel1.setLayout(new BoxLayout(jPanel1, BoxLayout.LINE_AXIS));

        jPanel5.setFont(new Font("Arial", 0, 12)); // NOI18N
        jPanel5.setLayout(new BorderLayout());
        jPanel1.add(jPanel5);

        jPanel4.setFont(new Font("Arial", 0, 12)); // NOI18N
        jPanel4.setLayout(new GridBagLayout());

        jBtnToRightAll.setIcon(new ImageIcon(getClass().getResource("/com/openbravo/images/2rightarrow.png"))); // NOI18N
        jBtnToRightAll.setToolTipText("Split All Line Items");
        jBtnToRightAll.setFocusPainted(false);
        jBtnToRightAll.setFocusable(false);
        jBtnToRightAll.setMargin(new Insets(8, 14, 8, 14));
        jBtnToRightAll.setPreferredSize(new Dimension(80, 45));
        jBtnToRightAll.setRequestFocusEnabled(false);
        jBtnToRightAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnToRightAllActionPerformed(evt);
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        jPanel4.add(jBtnToRightAll, gridBagConstraints);

        jBtnToRightOne.setIcon(new ImageIcon(getClass().getResource("/com/openbravo/images/1rightarrow.png"))); // NOI18N
        jBtnToRightOne.setToolTipText("Split Selected Line Items");
        jBtnToRightOne.setFocusPainted(false);
        jBtnToRightOne.setFocusable(false);
        jBtnToRightOne.setMargin(new Insets(8, 14, 8, 14));
        jBtnToRightOne.setPreferredSize(new Dimension(80, 45));
        jBtnToRightOne.setRequestFocusEnabled(false);
        jBtnToRightOne.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnToRightOneActionPerformed(evt);
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new Insets(5, 0, 0, 0);
        jPanel4.add(jBtnToRightOne, gridBagConstraints);

        jBtnToLeftOne.setIcon(new ImageIcon(getClass().getResource("/com/openbravo/images/1leftarrow.png"))); // NOI18N
        jBtnToLeftOne.setToolTipText("Return Selected Line Items");
        jBtnToLeftOne.setFocusPainted(false);
        jBtnToLeftOne.setFocusable(false);
        jBtnToLeftOne.setMargin(new Insets(8, 14, 8, 14));
        jBtnToLeftOne.setPreferredSize(new Dimension(80, 45));
        jBtnToLeftOne.setRequestFocusEnabled(false);
        jBtnToLeftOne.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnToLeftOneActionPerformed(evt);
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new Insets(5, 0, 0, 0);
        jPanel4.add(jBtnToLeftOne, gridBagConstraints);

        jBtnToLeftAll.setIcon(new ImageIcon(getClass().getResource("/com/openbravo/images/2leftarrow.png"))); // NOI18N
        jBtnToLeftAll.setToolTipText("Return All Line Items");
        jBtnToLeftAll.setFocusPainted(false);
        jBtnToLeftAll.setFocusable(false);
        jBtnToLeftAll.setMargin(new Insets(8, 14, 8, 14));
        jBtnToLeftAll.setPreferredSize(new Dimension(80, 45));
        jBtnToLeftAll.setRequestFocusEnabled(false);
        jBtnToLeftAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnToLeftAllActionPerformed(evt);
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new Insets(5, 0, 0, 0);
        jPanel4.add(jBtnToLeftAll, gridBagConstraints);

        jPanel1.add(jPanel4);

        jPanel3.setFont(new Font("Arial", 0, 12)); // NOI18N
        jPanel3.setLayout(new BorderLayout());
        jPanel1.add(jPanel3);

        add(jPanel1, BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void m_jButtonOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jButtonOKActionPerformed
        if (receipttwo.getTicket().getLinesCount() > 0) {
            accepted = true;
            if (modalContext != null) {
                modalContext.setResult(Boolean.TRUE);
                modalContext.close();
            }
        }
    }//GEN-LAST:event_m_jButtonOKActionPerformed

    private void m_jButtonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jButtonCancelActionPerformed
        accepted = false;
        if (modalContext != null) {
            modalContext.close();
        }
    }//GEN-LAST:event_m_jButtonCancelActionPerformed

    private void jBtnToRightAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnToRightAllActionPerformed
        TicketLineInfo[] lines = receiptone.getSelectedLines();
        if (lines != null) {
            receipttwo.addSelectedLines(lines);
        }
    }//GEN-LAST:event_jBtnToRightAllActionPerformed

    private void jBtnToRightOneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnToRightOneActionPerformed
        TicketLineInfo[] lines = receiptone.getSelectedLinesUnit();
        if (lines != null) {
            receipttwo.addSelectedLines(lines);
        }
    }//GEN-LAST:event_jBtnToRightOneActionPerformed

    private void jBtnToLeftOneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnToLeftOneActionPerformed
        TicketLineInfo[] lines = receipttwo.getSelectedLinesUnit();
        if (lines != null) {
            receiptone.addSelectedLines(lines);
        }
    }//GEN-LAST:event_jBtnToLeftOneActionPerformed

    private void jBtnToLeftAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnToLeftAllActionPerformed
        TicketLineInfo[] lines = receipttwo.getSelectedLines();
        if (lines != null) {
            receiptone.addSelectedLines(lines);
        }
    }//GEN-LAST:event_jBtnToLeftAllActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton jBtnToLeftAll;
    private JButton jBtnToLeftOne;
    private JButton jBtnToRightAll;
    private JButton jBtnToRightOne;
    private JPanel jPanel1;
    private JPanel jPanel2;
    private JPanel jPanel3;
    private JPanel jPanel4;
    private JPanel jPanel5;
    private JButton m_jButtonCancel;
    private JButton m_jButtonOK;
    // End of variables declaration//GEN-END:variables
}
