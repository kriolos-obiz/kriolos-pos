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

package com.openbravo.pos.inventory;

import com.openbravo.data.gui.MessageInf;
import com.openbravo.data.gui.modal.PosUIModal;
import com.openbravo.data.user.BrowsableEditableData;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.scanpal2.DeviceScanner;
import com.openbravo.pos.scanpal2.DeviceScannerException;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.ListModel;
import javax.swing.SwingConstants;

public class JUploadProductsPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private DeviceScanner m_scanner;
    private BrowsableEditableData m_bd;
    private PosUIModal modalContext;

    public JUploadProductsPanel() {
        initComponents();
        initDomainAdapters();
    }

    public JUploadProductsPanel(DeviceScanner scanner, BrowsableEditableData bd) {
        initComponents();
        initDomainAdapters();
        this.m_scanner = scanner;
        this.m_bd = bd;
        if (getRootPane() != null) {
            getRootPane().setDefaultButton(jcmdOK);
        }
    }

    private void initDomainAdapters() {
        setName("kriolos:inventory:upload-products-panel");
        jcmdOK.setName("kriolos:inventory:btn-ok");
        jcmdCancel.setName("kriolos:inventory:btn-cancel");
    }

    public void setModalContext(PosUIModal modalContext) {
        this.modalContext = modalContext;
    }

    public static void showMessage(Component parent, DeviceScanner scanner, BrowsableEditableData bd) {
        JUploadProductsPanel panel = new JUploadProductsPanel(scanner, bd);
        PosUIModal modal = PosUIModal.create(parent, panel)
                .setTitle(AppLocal.getIntString("caption.upload"))
                .setModal(true)
                .setResizable(false);
        panel.setModalContext(modal);
        modal.show();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new JPanel();
        jcmdCancel = new JButton();
        jcmdOK = new JButton();
        jPanel1 = new JPanel();
        jLabel1 = new JLabel();

        setLayout(new BorderLayout());

        jPanel2.setLayout(new FlowLayout(FlowLayout.RIGHT));

        jcmdCancel.setFont(new Font("Arial", 0, 12)); // NOI18N
        jcmdCancel.setIcon(new ImageIcon(getClass().getResource("/com/openbravo/images/cancel.png"))); // NOI18N
        jcmdCancel.setText(AppLocal.getIntString("button.cancel")); // NOI18N
        jcmdCancel.setPreferredSize(new Dimension(110, 45));
        jcmdCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jcmdCancelActionPerformed(evt);
            }
        });
        jPanel2.add(jcmdCancel);

        jcmdOK.setFont(new Font("Arial", 0, 12)); // NOI18N
        jcmdOK.setIcon(new ImageIcon(getClass().getResource("/com/openbravo/images/ok.png"))); // NOI18N
        jcmdOK.setText(AppLocal.getIntString("button.ok")); // NOI18N
        jcmdOK.setPreferredSize(new Dimension(110, 45));
        jcmdOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jcmdOKActionPerformed(evt);
            }
        });
        jPanel2.add(jcmdOK);

        add(jPanel2, BorderLayout.SOUTH);

        jPanel1.setLayout(null);

        jLabel1.setFont(new Font("Arial", 0, 14)); // NOI18N
        jLabel1.setHorizontalAlignment(SwingConstants.CENTER);
        jLabel1.setText(AppLocal.getIntString("message.preparescanner")); // NOI18N
        jLabel1.setPreferredSize(new Dimension(450, 30));
        jPanel1.add(jLabel1);
        jLabel1.setBounds(0, 20, 460, 30);

        add(jPanel1, BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void jcmdCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jcmdCancelActionPerformed
        if (modalContext != null) {
            modalContext.close();
        }
    }//GEN-LAST:event_jcmdCancelActionPerformed

    private void jcmdOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jcmdOKActionPerformed
        String stext = jLabel1.getText();
        jLabel1.setText(AppLocal.getIntString("label.uploadingproducts"));
        jcmdOK.setEnabled(false);
        jcmdCancel.setEnabled(false);

        try {
            m_scanner.connectDevice();
            m_scanner.startUploadProduct();

            ListModel l = m_bd.getListModel();
            for (int i = 0; i < l.getSize(); i++) {
                Object[] myprod = (Object[]) l.getElementAt(i);
                m_scanner.sendProduct(
                        (String) myprod[3],
                        (String) myprod[2],
                        (Double) myprod[6]
                );
            }
            m_scanner.stopUploadProduct();
            MessageInf msg = new MessageInf(MessageInf.SGN_SUCCESS, AppLocal.getIntString("message.scannerok"));
            msg.show(this);
        } catch (DeviceScannerException e) {
            MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.scannerfail"), e);
            msg.show(this);
        } finally {
            m_scanner.disconnectDevice();
        }

        jLabel1.setText(stext);
        jcmdOK.setEnabled(true);
        jcmdCancel.setEnabled(true);

        if (modalContext != null) {
            modalContext.close();
        }
    }//GEN-LAST:event_jcmdOKActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JLabel jLabel1;
    private JPanel jPanel1;
    private JPanel jPanel2;
    private JButton jcmdCancel;
    private JButton jcmdOK;
    // End of variables declaration//GEN-END:variables
}
