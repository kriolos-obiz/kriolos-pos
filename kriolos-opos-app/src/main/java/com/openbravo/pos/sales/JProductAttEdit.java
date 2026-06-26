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

package com.openbravo.pos.sales;

import com.openbravo.pos.inventory.DataLogicAttribute;
import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.DataRead;
import com.openbravo.data.loader.Datas;
import com.openbravo.data.loader.PreparedSentence;
import com.openbravo.data.loader.SentenceExec;
import com.openbravo.data.loader.SentenceFind;
import com.openbravo.data.loader.SentenceList;
import com.openbravo.data.loader.SerializerRead;
import com.openbravo.data.loader.SerializerReadString;
import com.openbravo.data.loader.SerializerWriteBasic;
import com.openbravo.data.loader.SerializerWriteString;
import com.openbravo.data.loader.Session;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.inventory.AttributeSetInfo;
import com.openbravo.pos.inventory.AttributeInstInfo;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.swing.SwingUtilities;

/**
 *
 * @author adrianromero
 */
public class JProductAttEdit extends javax.swing.JDialog {

    private List<JProductAttEditI> itemslist;
    private String attsetid;
    private String attInstanceId;
    private String attInstanceDescription;

    private boolean ok;
    
    private DataLogicAttribute dlProdAttribute;

    /** Creates new form JProductAttEdit */
    private JProductAttEdit(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
    }

    /** Creates new form JProductAttEdit */
    private JProductAttEdit(java.awt.Dialog parent, boolean modal) {
        super(parent, modal);
    }

    private void init(Session s) {

        initComponents();
        getRootPane().setDefaultButton(m_jButtonOK);
        
        dlProdAttribute = new DataLogicAttribute();
        dlProdAttribute.init(s);
    }

    /**
     *
     * @param parent
     * @param s
     * @return
     */
    public static JProductAttEdit getAttributesEditor(Component parent, Session s) {

        Window window = SwingUtilities.getWindowAncestor(parent);

        JProductAttEdit myMsg;
        if (window instanceof Frame) {
            myMsg = new JProductAttEdit((Frame) window, true);
        } else {
            myMsg = new JProductAttEdit((Dialog) window, true);
        }
        myMsg.init(s);
        myMsg.applyComponentOrientation(parent.getComponentOrientation());
        return myMsg;
    }

    /**
     *
     * @param attsetid
     * @param attsetinstid
     * @throws BasicException
     */
    public void editAttributes(String attsetid, String attsetinstid) throws BasicException {

        if (attsetid == null) {
//            throw new BasicException(AppLocal.getIntString("message.attsetnotexists"));
            throw new BasicException(AppLocal.getIntString("message.cannotfindattributes"));
        } else {

            this.attsetid = attsetid;
            this.attInstanceId = null;
            this.attInstanceDescription = null;

            this.ok = false;

            // get attsetinst values
            AttributeSetInfo asi = (AttributeSetInfo) dlProdAttribute.attsetSent.find(new Object[]{attsetid});

            if (asi == null) {
//                throw new BasicException(AppLocal.getIntString("message.attsetnotexists"));
                throw new BasicException(AppLocal.getIntString("message.cannotfindattributes"));
            }

            setTitle(asi.getName());

            List<AttributeInstInfo> attinstinfo = attsetinstid == null
                    ? dlProdAttribute.attinstSent.list(new Object[]{attsetid})
                    : dlProdAttribute.attinstSent2.list(new Object[]{attsetid, attsetinstid});

            itemslist = new ArrayList<>();

            for (AttributeInstInfo aii : attinstinfo) {

                JProductAttEditI item;

                List<String> values = dlProdAttribute.attvaluesSent.list(new Object[]{aii.getAttid()});
                if (values.isEmpty()) {
                    // Does not exist a list of values then a textfield
                    item = new JProductAttEditItem(aii.getAttid(),  aii.getAttname(), aii.getValue(), m_jKeys);
                } else {
                    // Does exist a list with the values
                    item = new JProductAttListItem(aii.getAttid(),  aii.getAttname(), aii.getValue(), values);
                }

                itemslist.add(item);
                jPanel2.add(item.getComponent());
            }

            if (itemslist.size() > 0) {
                itemslist.get(0).assignSelection();
            }
        }
    }

    /**
     *
     * @return
     */
    public boolean isOK() {
        return ok;
    }

    /**
     *
     * @return
     */
    public String getAttributeSetInst() {
        return attInstanceId;
    }

    /**
     *
     * @return
     */
    public String getAttributeSetInstDescription() {
        return attInstanceDescription;
    }

   
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel5 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        m_jKeys = new com.openbravo.editor.JEditorKeys();
        jPanel1 = new javax.swing.JPanel();
        m_jButtonCancel = new javax.swing.JButton();
        m_jButtonOK = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jPanel5.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jPanel5.setLayout(new java.awt.BorderLayout());

        jPanel2.setLayout(new javax.swing.BoxLayout(jPanel2, javax.swing.BoxLayout.PAGE_AXIS));
        jPanel5.add(jPanel2, java.awt.BorderLayout.NORTH);

        getContentPane().add(jPanel5, java.awt.BorderLayout.CENTER);

        jPanel3.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jPanel3.setLayout(new java.awt.BorderLayout());

        jPanel4.setLayout(new javax.swing.BoxLayout(jPanel4, javax.swing.BoxLayout.Y_AXIS));
        jPanel4.add(m_jKeys);

        jPanel3.add(jPanel4, java.awt.BorderLayout.NORTH);

        jPanel1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        m_jButtonCancel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        m_jButtonCancel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/cancel.png"))); // NOI18N
        m_jButtonCancel.setText(AppLocal.getIntString("button.cancel")); // NOI18N
        m_jButtonCancel.setFocusPainted(false);
        m_jButtonCancel.setFocusable(false);
        m_jButtonCancel.setMargin(new java.awt.Insets(8, 16, 8, 16));
        m_jButtonCancel.setPreferredSize(new java.awt.Dimension(110, 45));
        m_jButtonCancel.setRequestFocusEnabled(false);
        m_jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jButtonCancelActionPerformed(evt);
            }
        });
        jPanel1.add(m_jButtonCancel);

        m_jButtonOK.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        m_jButtonOK.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/ok.png"))); // NOI18N
        m_jButtonOK.setText(AppLocal.getIntString("button.OK")); // NOI18N
        m_jButtonOK.setFocusPainted(false);
        m_jButtonOK.setFocusable(false);
        m_jButtonOK.setLabel(AppLocal.getIntString("button.ok")); // NOI18N
        m_jButtonOK.setMargin(new java.awt.Insets(8, 16, 8, 16));
        m_jButtonOK.setPreferredSize(new java.awt.Dimension(110, 45));
        m_jButtonOK.setRequestFocusEnabled(false);
        m_jButtonOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jButtonOKActionPerformed(evt);
            }
        });
        jPanel1.add(m_jButtonOK);

        jPanel3.add(jPanel1, java.awt.BorderLayout.PAGE_END);

        getContentPane().add(jPanel3, java.awt.BorderLayout.EAST);

        setSize(new java.awt.Dimension(658, 388));
        //REMOVED SET LOCATION RELATIVE TO
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

        String id;

        if (description.length() == 0) {
            // No values then id is null
            id = null;
        } else {
            // Some values then an instance should exists.
            try {
                // Exist an attribute set instance with these values for the attributeset selected
                id = (String) dlProdAttribute.attsetinstExistsSent.find(new Object[]{attsetid, description.toString()});
            } catch (Exception ex) {
                // Logger.getLogger(JProductAttEdit.class.getName()).log(Level.SEVERE, null, ex);
                return;
            }


            if (id == null) {
                // No, create a new ATTRIBUTESETINSTANCE and return the ID generated
                // or return null... That means that that product does not exists....
                // Maybe these two modes must be supported one for selection and other for creation....
                id = UUID.randomUUID().toString();
                try {
                    dlProdAttribute.attsetSave.exec(new Object[]{id, attsetid, description.toString()});
                    for (JProductAttEditI item : itemslist) {
                        dlProdAttribute.attinstSave.exec(new Object[]{UUID.randomUUID().toString(), id, item.getAttribute(), item.getValue()});
                    }

                } catch (Exception ex) {
                    // Logger.getLogger(JProductAttEdit.class.getName()).log(Level.SEVERE, null, ex);
                    return;
                }
            }
        }

        ok = true;
        attInstanceId = id;
        attInstanceDescription = description.toString();

        dispose();
    }//GEN-LAST:event_m_jButtonOKActionPerformed

    private void m_jButtonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jButtonCancelActionPerformed

        dispose();
    }//GEN-LAST:event_m_jButtonCancelActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JButton m_jButtonCancel;
    private javax.swing.JButton m_jButtonOK;
    private com.openbravo.editor.JEditorKeys m_jKeys;
    // End of variables declaration//GEN-END:variables

}
