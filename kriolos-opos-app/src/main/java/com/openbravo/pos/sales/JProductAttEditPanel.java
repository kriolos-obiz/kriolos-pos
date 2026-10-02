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
import com.openbravo.data.gui.modal.PosUIModal;
import com.openbravo.data.loader.Session;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.inventory.AttributeInstInfo;
import com.openbravo.pos.inventory.AttributeSetInfo;
import com.openbravo.pos.inventory.DataLogicAttribute;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;

public class JProductAttEditPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private List<JProductAttEditI> itemslist;
    private String attsetid;
    private String attInstanceId;
    private String attInstanceDescription;
    private String title;
    private boolean ok;
    private DataLogicAttribute dlProdAttribute;
    private PosUIModal modalContext;

    public JProductAttEditPanel() {
        initComponents();
        initDomainAdapters();
    }

    public JProductAttEditPanel(Session s) {
        initComponents();
        initDomainAdapters();
        init(s);
    }

    private void initDomainAdapters() {
        setName("kriolos:sales:product-att-edit-panel");
        m_jButtonOK.setName("kriolos:sales:btn-ok");
        m_jButtonCancel.setName("kriolos:sales:btn-cancel");
    }

    public void setModalContext(PosUIModal modalContext) {
        this.modalContext = modalContext;
    }

    public void init(Session s) {
        dlProdAttribute = new DataLogicAttribute();
        dlProdAttribute.init(s);

        if (getRootPane() != null) {
            getRootPane().setDefaultButton(m_jButtonOK);
        }
    }

    public String getTitle() {
        return title != null ? title : AppLocal.getIntString("label.attributes");
    }

    public void editAttributes(String attsetid, String attsetinstid) throws BasicException {
        if (attsetid == null) {
            throw new BasicException(AppLocal.getIntString("message.cannotfindattributes"));
        } else {
            this.attsetid = attsetid;
            this.attInstanceId = null;
            this.attInstanceDescription = null;
            this.ok = false;

            AttributeSetInfo asi = (AttributeSetInfo) dlProdAttribute.attsetSent.find(new Object[]{attsetid});
            if (asi == null) {
                throw new BasicException(AppLocal.getIntString("message.cannotfindattributes"));
            }

            this.title = asi.getName();

            List<AttributeInstInfo> attinstinfo = attsetinstid == null
                    ? dlProdAttribute.attinstSent.list(new Object[]{attsetid})
                    : dlProdAttribute.attinstSent2.list(new Object[]{attsetid, attsetinstid});

            jPanel2.removeAll();
            itemslist = new ArrayList<>();

            for (AttributeInstInfo aii : attinstinfo) {
                JProductAttEditI item;
                List<String> values = dlProdAttribute.attvaluesSent.list(new Object[]{aii.getAttid()});
                if (values.isEmpty()) {
                    item = new JProductAttEditItem(aii.getAttid(), aii.getAttname(), aii.getValue(), m_jKeys);
                } else {
                    item = new JProductAttListItem(aii.getAttid(), aii.getAttname(), aii.getValue(), values);
                }
                itemslist.add(item);
                jPanel2.add(item.getComponent());
            }

            if (itemslist.size() > 0) {
                itemslist.get(0).assignSelection();
            }

            revalidate();
            repaint();
        }
    }

    public boolean isOK() {
        return ok;
    }

    public String getAttributeSetInst() {
        return attInstanceId;
    }

    public String getAttributeSetInstDescription() {
        return attInstanceDescription;
    }

    public static boolean show(Component parent, Session s, String attsetid, String attsetinstid, String[] outResult) throws BasicException {
        JProductAttEditPanel panel = new JProductAttEditPanel(s);
        panel.editAttributes(attsetid, attsetinstid);
        PosUIModal modal = PosUIModal.create(parent, panel)
                .setTitle(panel.getTitle())
                .setModal(true)
                .setResizable(true);
        panel.setModalContext(modal);
        modal.show();

        if (panel.isOK()) {
            if (outResult != null) {
                if (outResult.length > 0) {
                    outResult[0] = panel.getAttributeSetInst();
                }
                if (outResult.length > 1) {
                    outResult[1] = panel.getAttributeSetInstDescription();
                }
            }
            return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel5 = new JPanel();
        jPanel2 = new JPanel();
        jPanel3 = new JPanel();
        jPanel4 = new JPanel();
        m_jKeys = new com.openbravo.editor.JEditorKeys();
        jPanel1 = new JPanel();
        m_jButtonCancel = new JButton();
        m_jButtonOK = new JButton();

        setLayout(new BorderLayout());

        jPanel5.setFont(new Font("Arial", 0, 12)); // NOI18N
        jPanel5.setLayout(new BorderLayout());

        jPanel2.setLayout(new BoxLayout(jPanel2, BoxLayout.PAGE_AXIS));
        jPanel5.add(jPanel2, BorderLayout.NORTH);

        add(jPanel5, BorderLayout.CENTER);

        jPanel3.setFont(new Font("Arial", 0, 12)); // NOI18N
        jPanel3.setLayout(new BorderLayout());

        jPanel4.setLayout(new BoxLayout(jPanel4, BoxLayout.Y_AXIS));
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

        jPanel3.add(jPanel1, BorderLayout.PAGE_END);

        add(jPanel3, BorderLayout.EAST);
    }// </editor-fold>//GEN-END:initComponents

    private void m_jButtonOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jButtonOKActionPerformed
        StringBuilder description = new StringBuilder();

        for (JProductAttEditI item : itemslist) {
            String value = item.getValue();
            if (value != null && value.length() > 0) {
                if (description.length() > 0) {
                    description.append(", ");
                }
                description.append(value);
            }
        }

        String id = null;

        if (description.length() == 0) {
            id = null;
        } else {
            try {
                id = (String) dlProdAttribute.attsetinstExistsSent.find(new Object[]{attsetid, description.toString()});
            } catch (BasicException ex) {
            }

            if (id == null) {
                id = UUID.randomUUID().toString();
                try {
                    dlProdAttribute.attsetSave.exec(new Object[]{id, attsetid, description.toString()});
                    for (JProductAttEditI item : itemslist) {
                        dlProdAttribute.attinstSave.exec(new Object[]{UUID.randomUUID().toString(), id, item.getAttribute(), item.getValue()});
                    }
                } catch (Exception ex) {
                }
            }
        }

        attInstanceId = id;
        attInstanceDescription = description.toString();

        ok = true;
        if (modalContext != null) {
            modalContext.setResult(Boolean.TRUE);
            modalContext.close();
        }
    }//GEN-LAST:event_m_jButtonOKActionPerformed

    private void m_jButtonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jButtonCancelActionPerformed
        ok = false;
        if (modalContext != null) {
            modalContext.close();
        }
    }//GEN-LAST:event_m_jButtonCancelActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JPanel jPanel1;
    private JPanel jPanel2;
    private JPanel jPanel3;
    private JPanel jPanel4;
    private JPanel jPanel5;
    private JButton m_jButtonCancel;
    private JButton m_jButtonOK;
    private com.openbravo.editor.JEditorKeys m_jKeys;
    // End of variables declaration//GEN-END:variables
}
