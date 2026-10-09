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

import com.openbravo.basic.BasicException;
import com.openbravo.data.gui.JMessagePanel;
import com.openbravo.data.gui.MessageInf;
import com.openbravo.data.gui.modal.PosUIModal;
import com.openbravo.format.Formats;
import com.openbravo.pos.forms.AppConfig;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.catalog.CatalogService;
import com.openbravo.pos.ticket.TicketLineInfo;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LayoutStyle;

public class JProductLineEditPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(JProductLineEditPanel.class.getName());

    private TicketLineInfo returnLine;
    private TicketLineInfo m_oLine;
    private boolean m_bunitsok;
    private boolean m_bpriceok;
    private String productID;
    private AppView appView;
    private PosUIModal modalContext;
    private boolean accepted = false;

    public JProductLineEditPanel() {
        initComponents();
        initDomainAdapters();
    }

    private void initDomainAdapters() {
        setName("kriolos:sales:product-line-edit-panel");
        m_jButtonOK.setName("kriolos:sales:btn-ok");
        m_jButtonCancel.setName("kriolos:sales:btn-cancel");
        m_jBtnPriceUpdate.setName("kriolos:sales:btn-price-update");
    }

    public void setModalContext(PosUIModal modalContext) {
        this.modalContext = modalContext;
    }

    public TicketLineInfo getReturnLine() {
        return accepted ? returnLine : null;
    }

    private void init(AppView appView, TicketLineInfo oLine) throws BasicException {
        this.appView = appView;

        productID = oLine.getProductID();

        if (oLine.getTaxInfo() == null) {
            throw new BasicException(AppLocal.getIntString("message.cannotcalculatetaxes"));
        }

        m_jBtnPriceUpdate.setVisible(AppConfig.getInstance().getBoolean("db.prodpriceupdate"));
        m_jBtnPriceUpdate.setEnabled(false);

        m_oLine = new TicketLineInfo(oLine);
        m_bunitsok = true;
        m_bpriceok = true;

        m_jUnits.setEnabled(this.appView.hasPermission("com.openbravo.pos.sales.JPanelTicketEdits"));
        m_jPrice.setEnabled(this.appView.hasPermission("com.openbravo.pos.sales.JPanelTicketEdits"));
        m_jPriceTax.setEnabled(this.appView.hasPermission("com.openbravo.pos.sales.JPanelTicketEdits"));

        m_jName.setText(oLine.getProductName());
        m_jUnits.setDoubleValue(oLine.getMultiply());
        m_jPrice.setDoubleValue(oLine.getPrice());
        m_jPriceTax.setDoubleValue(oLine.getPriceWithTax());

        jLblTaxName.setText(oLine.getTaxInfo().getName());
        m_jTaxrate.setText(Formats.PERCENT.formatValue(oLine.getTaxInfo().getRate()));

        m_jName.addPropertyChangeListener("Edition", new RecalculateName());
        m_jUnits.addPropertyChangeListener("Edition", new RecalculateUnits());
        m_jPrice.addPropertyChangeListener("Edition", new RecalculatePrice());
        m_jPriceTax.addPropertyChangeListener("Edition", new RecalculatePriceTax());

        printTotals();

        m_jUnits.addEditorKeys(m_jKeys);
        m_jPrice.addEditorKeys(m_jKeys);
        m_jPriceTax.addEditorKeys(m_jKeys);

        m_jUnits.activate();

        if (getRootPane() != null) {
            getRootPane().setDefaultButton(m_jButtonOK);
        }

        accepted = false;
        returnLine = null;
    }

    public static TicketLineInfo showMessage(Component parent, AppView app, TicketLineInfo oLine) throws BasicException {
        JProductLineEditPanel panel = new JProductLineEditPanel();
        panel.init(app, oLine);
        PosUIModal modal = PosUIModal.create(parent, panel)
                .setTitle(AppLocal.getIntString("label.editline"))
                .setModal(true)
                .setResizable(false);
        panel.setModalContext(modal);
        modal.show();
        return panel.getReturnLine();
    }

    private void printTotals() {
        if (m_bunitsok && m_bpriceok) {
            m_jSubtotal.setText(m_oLine.printSubValue());
            m_jTotal.setText(m_oLine.printValue());
        } else {
            m_jSubtotal.setText(null);
            m_jTotal.setText(null);
        }
    }

    private class RecalculateUnits implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            Double value = m_jUnits.getValue();
            if (value == null || value == 0.0) {
                m_bunitsok = false;
            } else {
                m_oLine.setMultiply(value);
                m_bunitsok = true;
            }
            printTotals();
        }
    }

    private class RecalculatePrice implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            Double value = m_jPrice.getValue();
            if (value == null) {
                m_bpriceok = false;
            } else {
                m_oLine.setPrice(value);
                m_jPriceTax.setDoubleValue(m_oLine.getPriceWithTax());
                m_bpriceok = true;
            }
            printTotals();
        }
    }

    private class RecalculatePriceTax implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            Double value = m_jPriceTax.getValue();
            if (value == null) {
                m_bpriceok = false;
            } else {
                m_oLine.setPriceTax(value);
                m_jPrice.setDoubleValue(m_oLine.getPrice());
                m_bpriceok = true;
            }
            printTotals();
        }
    }

    private class RecalculateName implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            m_oLine.setProperty("product.name", m_jName.getText());
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel5 = new JPanel();
        jPanel2 = new JPanel();
        jLabel1 = new JLabel();
        jLabel2 = new JLabel();
        jLabel3 = new JLabel();
        jLabel4 = new JLabel();
        m_jName = new com.openbravo.editor.JEditorString();
        m_jUnits = new com.openbravo.editor.JEditorDoublePositive();
        m_jPrice = new com.openbravo.editor.JEditorCurrency();
        m_jPriceTax = new com.openbravo.editor.JEditorCurrency();
        jLabel5 = new JLabel();
        jLblTaxName = new JLabel();
        m_jTaxrate = new JLabel();
        jLabel7 = new JLabel();
        m_jSubtotal = new JLabel();
        jLabel6 = new JLabel();
        m_jTotal = new JLabel();
        m_jBtnPriceUpdate = new JButton();
        jPanel3 = new JPanel();
        jPanel4 = new JPanel();
        m_jKeys = new com.openbravo.editor.JEditorKeys();
        jPanel1 = new JPanel();
        m_jButtonCancel = new JButton();
        m_jButtonOK = new JButton();

        setLayout(new BorderLayout());

        jPanel5.setLayout(new BorderLayout());

        jLabel1.setFont(new Font("Arial", 0, 14)); // NOI18N
        jLabel1.setText(AppLocal.getIntString("label.price")); // NOI18N
        jLabel1.setPreferredSize(new Dimension(100, 30));

        jLabel2.setFont(new Font("Arial", 0, 14)); // NOI18N
        jLabel2.setText(AppLocal.getIntString("label.item")); // NOI18N
        jLabel2.setPreferredSize(new Dimension(100, 30));

        jLabel3.setFont(new Font("Arial", 0, 14)); // NOI18N
        jLabel3.setText(AppLocal.getIntString("label.pricetax")); // NOI18N
        jLabel3.setPreferredSize(new Dimension(100, 30));

        jLabel4.setFont(new Font("Arial", 0, 14)); // NOI18N
        jLabel4.setText(AppLocal.getIntString("label.units")); // NOI18N
        jLabel4.setPreferredSize(new Dimension(100, 30));

        m_jName.setEnabled(false);
        m_jName.setFont(new Font("Arial", 0, 14)); // NOI18N
        m_jName.setPreferredSize(new Dimension(250, 30));

        m_jUnits.setFont(new Font("Arial", 0, 14)); // NOI18N
        m_jUnits.setPreferredSize(new Dimension(250, 30));

        m_jPrice.setFont(new Font("Arial", 0, 14)); // NOI18N
        m_jPrice.setPreferredSize(new Dimension(250, 30));

        m_jPriceTax.setFont(new Font("Arial", 0, 14)); // NOI18N
        m_jPriceTax.setPreferredSize(new Dimension(250, 30));

        jLabel5.setFont(new Font("Arial", 0, 14)); // NOI18N
        jLabel5.setText(AppLocal.getIntString("label.tax")); // NOI18N
        jLabel5.setPreferredSize(new Dimension(100, 30));

        jLblTaxName.setFont(new Font("Arial", 0, 14)); // NOI18N
        jLblTaxName.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLblTaxName.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(javax.swing.UIManager.getDefaults().getColor("Button.darkShadow")), BorderFactory.createEmptyBorder(1, 4, 1, 4)));
        jLblTaxName.setOpaque(true);
        jLblTaxName.setPreferredSize(new Dimension(150, 30));
        jLblTaxName.setRequestFocusEnabled(false);

        m_jTaxrate.setFont(new Font("Arial", 0, 14)); // NOI18N
        m_jTaxrate.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        m_jTaxrate.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(javax.swing.UIManager.getDefaults().getColor("Button.darkShadow")), BorderFactory.createEmptyBorder(1, 4, 1, 4)));
        m_jTaxrate.setOpaque(true);
        m_jTaxrate.setPreferredSize(new Dimension(100, 30));
        m_jTaxrate.setRequestFocusEnabled(false);

        jLabel7.setFont(new Font("Arial", 0, 14)); // NOI18N
        jLabel7.setText(AppLocal.getIntString("label.subtotal")); // NOI18N
        jLabel7.setPreferredSize(new Dimension(100, 30));

        m_jSubtotal.setFont(new Font("Arial", 0, 14)); // NOI18N
        m_jSubtotal.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        m_jSubtotal.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(javax.swing.UIManager.getDefaults().getColor("Button.darkShadow")), BorderFactory.createEmptyBorder(1, 4, 1, 4)));
        m_jSubtotal.setOpaque(true);
        m_jSubtotal.setPreferredSize(new Dimension(150, 30));
        m_jSubtotal.setRequestFocusEnabled(false);

        jLabel6.setFont(new Font("Arial", 0, 14)); // NOI18N
        jLabel6.setText(AppLocal.getIntString("label.totalcash")); // NOI18N
        jLabel6.setPreferredSize(new Dimension(100, 30));

        m_jTotal.setFont(new Font("Arial", 0, 14)); // NOI18N
        m_jTotal.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        m_jTotal.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(javax.swing.UIManager.getDefaults().getColor("Button.darkShadow")), BorderFactory.createEmptyBorder(1, 4, 1, 4)));
        m_jTotal.setOpaque(true);
        m_jTotal.setPreferredSize(new Dimension(150, 30));
        m_jTotal.setRequestFocusEnabled(false);

        m_jBtnPriceUpdate.setFont(new Font("Arial", 0, 12)); // NOI18N
        m_jBtnPriceUpdate.setIcon(new ImageIcon(getClass().getResource("/com/openbravo/images/filesave.png"))); // NOI18N
        m_jBtnPriceUpdate.setText(AppLocal.getIntString("button.prodpriceupdate")); // NOI18N
        m_jBtnPriceUpdate.setToolTipText("Update Product Price in database");
        m_jBtnPriceUpdate.setFocusPainted(false);
        m_jBtnPriceUpdate.setFocusable(false);
        m_jBtnPriceUpdate.setMargin(new Insets(8, 16, 8, 16));
        m_jBtnPriceUpdate.setPreferredSize(new Dimension(110, 45));
        m_jBtnPriceUpdate.setRequestFocusEnabled(false);
        m_jBtnPriceUpdate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jBtnPriceUpdateActionPerformed(evt);
            }
        });

        GroupLayout jPanel2Layout = new GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel4, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel3, GroupLayout.Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                    .addComponent(m_jUnits, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(m_jPrice, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(m_jPriceTax, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addComponent(jLblTaxName, GroupLayout.PREFERRED_SIZE, 136, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(m_jTaxrate, GroupLayout.PREFERRED_SIZE, 107, GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(m_jName, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel6, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                    .addComponent(m_jBtnPriceUpdate, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                    .addComponent(m_jTotal, GroupLayout.PREFERRED_SIZE, 249, GroupLayout.PREFERRED_SIZE)))
                            .addComponent(jLabel5, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel7, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(m_jSubtotal, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(m_jName, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(m_jUnits, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(m_jPrice, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_jPriceTax, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addGap(9, 9, 9)
                .addGroup(jPanel2Layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                    .addComponent(m_jTaxrate, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel2Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel5, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLblTaxName, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel7, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_jSubtotal, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(m_jTotal, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 51, Short.MAX_VALUE)
                .addComponent(m_jBtnPriceUpdate, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jPanel5.add(jPanel2, BorderLayout.CENTER);

        add(jPanel5, BorderLayout.CENTER);

        jPanel3.setLayout(new BorderLayout());

        jPanel4.setLayout(new BoxLayout(jPanel4, BoxLayout.Y_AXIS));

        m_jKeys.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jKeysActionPerformed(evt);
            }
        });
        jPanel4.add(m_jKeys);

        jPanel3.add(jPanel4, BorderLayout.NORTH);

        jPanel1.setLayout(new FlowLayout(FlowLayout.RIGHT));

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
        jPanel1.add(m_jButtonCancel);

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
        jPanel1.add(m_jButtonOK);

        jPanel3.add(jPanel1, BorderLayout.SOUTH);

        add(jPanel3, BorderLayout.EAST);
    }// </editor-fold>//GEN-END:initComponents

    private void m_jButtonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jButtonCancelActionPerformed
        accepted = false;
        returnLine = null;
        if (modalContext != null) {
            modalContext.close();
        }
    }//GEN-LAST:event_m_jButtonCancelActionPerformed

    private void m_jButtonOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jButtonOKActionPerformed
        if (m_bunitsok && m_bpriceok) {
            returnLine = new TicketLineInfo(m_oLine);
            accepted = true;
            if (modalContext != null) {
                modalContext.setResult(returnLine);
                modalContext.close();
            }
        }
    }//GEN-LAST:event_m_jButtonOKActionPerformed

    private void m_jKeysActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jKeysActionPerformed
    }//GEN-LAST:event_m_jKeysActionPerformed

    private void m_jBtnPriceUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jBtnPriceUpdateActionPerformed
        try {
            CatalogService catalogService = appView.getBean(CatalogService.class); 
            catalogService.updateProductPrice(productID, m_jPrice.getValue());
            m_jBtnPriceUpdate.setEnabled(false);
        } catch (BasicException ex) {
            LOGGER.log(Level.WARNING, "Exception update products pricesell for ID: " + productID, ex);
            JMessagePanel.showMessage(this,
                    new MessageInf(MessageInf.SGN_DANGER, "Unable to update product sell price. ", ex));
        }

        m_oLine.setUpdated(true);
    }//GEN-LAST:event_m_jBtnPriceUpdateActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JLabel jLabel1;
    private JLabel jLabel2;
    private JLabel jLabel3;
    private JLabel jLabel4;
    private JLabel jLabel5;
    private JLabel jLabel6;
    private JLabel jLabel7;
    private JLabel jLblTaxName;
    private JPanel jPanel1;
    private JPanel jPanel2;
    private JPanel jPanel3;
    private JPanel jPanel4;
    private JPanel jPanel5;
    private JButton m_jBtnPriceUpdate;
    private JButton m_jButtonCancel;
    private JButton m_jButtonOK;
    private com.openbravo.editor.JEditorKeys m_jKeys;
    private com.openbravo.editor.JEditorString m_jName;
    private com.openbravo.editor.JEditorCurrency m_jPrice;
    private com.openbravo.editor.JEditorCurrency m_jPriceTax;
    private JLabel m_jSubtotal;
    private JLabel m_jTaxrate;
    private JLabel m_jTotal;
    private com.openbravo.editor.JEditorDoublePositive m_jUnits;
    // End of variables declaration//GEN-END:variables
}
