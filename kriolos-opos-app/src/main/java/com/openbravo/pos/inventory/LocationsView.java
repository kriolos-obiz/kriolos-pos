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

package com.openbravo.pos.inventory;

import com.openbravo.basic.AppLocal;
import com.openbravo.basic.BasicException;
import com.openbravo.data.user.DirtyManager;
import com.openbravo.data.user.EditorRecord;
import com.openbravo.ui.data.binding.SwingBinder;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.UUID;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

/**
 *
 * @author adrianromero
 * @author KriolOS
 */
public final class LocationsView extends javax.swing.JPanel implements EditorRecord<LocationInfo> {
    
    private static final System.Logger LOGGER = System.getLogger(LocationsView.class.getName());
    private static final long serialVersionUID = 1L;

    private String m_sID;
    private DirtyManager m_Dirty;
    private SwingBinder<LocationInfo> binder;

    /** Creates new form LocationsEditor
     * @param dirty */
    public LocationsView(DirtyManager dirty) {
        this.m_Dirty = dirty;
        initComponents();
        
        m_jName.getDocument().addDocumentListener(dirty);
        m_jAddress.getDocument().addDocumentListener(dirty);
        
        initValidator();
        writeValueEOF();    
    }

    private void initValidator() {
        binder = new SwingBinder<>(new LocationInfo());

        // Mandatory Location Name
        binder.forField(m_jName)
                .asRequired(AppLocal.getIntString("validation.required", AppLocal.getIntString("label.locationname")))
                .bind(LocationInfo::getName, LocationInfo::setName);

        // Optional Location Address
        binder.forField(m_jAddress)
                .bind(LocationInfo::getAddress, LocationInfo::setAddress);

        binder.addStatusChangeListener(() -> {
            if (m_Dirty != null) {
                m_Dirty.setDirty(true);
            }
        });
    }

    public boolean isValid() {
        return binder == null || binder.validate();
    }

    public SwingBinder<LocationInfo> getBinder() {
        return binder;
    }

    JTextField getNameField() {
        return m_jName;
    }

    JTextField getAddressField() {
        return m_jAddress;
    }

    /**
     *
     */
    @Override
    public void writeValueEOF() {
        m_sID = null;
        if (binder != null) {
            binder.clear();
        } else {
            m_jName.setText(null);
            m_jAddress.setText(null);
        }

        m_jName.setEnabled(false);
        m_jAddress.setEnabled(false);
    }    

    /**
     *
     */
    @Override
    public void writeValueInsert() {
        m_sID = UUID.randomUUID().toString(); 
        if (binder != null) {
            binder.clear();
        } else {
            m_jName.setText(null);
            m_jAddress.setText(null);
        }

        m_jName.setEnabled(true);
        m_jAddress.setEnabled(true);
    }    

    /**
     *
     * @param location
     */
    @Override
    public void writeValueDelete(LocationInfo location) {
        if (location == null) {
            writeValueEOF();
            return;
        }
        m_sID = location.getId();
        if (binder != null) {
            binder.readBean(location);
        } else {
            m_jName.setText(location.getName());
            m_jAddress.setText(location.getAddress());
        }

        m_jName.setEnabled(false);
        m_jAddress.setEnabled(false);
    }    

    /**
     *
     * @param location
     */
    @Override
    public void writeValueEdit(LocationInfo location) {
        if (location == null) {
            writeValueEOF();
            return;
        }
        m_sID = location.getId();
        if (binder != null) {
            binder.readBean(location);
        } else {
            m_jName.setText(location.getName());
            m_jAddress.setText(location.getAddress());
        }

        m_jName.setEnabled(true);
        m_jAddress.setEnabled(true);
    }    

    /**
     *
     * @return
     * @throws BasicException
     */
    @Override
    public LocationInfo createValue() throws BasicException {
        LocationInfo location = new LocationInfo(
                m_sID == null ? UUID.randomUUID().toString() : m_sID,
                m_jName.getText(),
                m_jAddress.getText()
        );

        if (binder != null) {
            try {
                binder.writeBean(location);
            } catch (SwingBinder.ValidationException ex) {
                LOGGER.log(System.Logger.Level.WARNING, "Validation in createValue: " + ex.getMessage());
            }
        }

        if (location.getName() == null) {
            location.setName(m_jName.getText());
        }
        if (location.getAddress() == null) {
            location.setAddress(m_jAddress.getText());
        }

        return location;
    }

    /**
     *
     * @return
     */
    @Override
    public Component getComponent() {
        return this;
    }
    
    /**
     *
     */
    @Override
    public void refresh() {
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel2 = new javax.swing.JLabel();
        m_jName = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        m_jAddress = new javax.swing.JTextField();

        jLabel2.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/info.png"))); // NOI18N
        jLabel2.setText(AppLocal.getIntString("label.locationname")); // NOI18N
        jLabel2.setPreferredSize(new java.awt.Dimension(110, 30));
        jLabel2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel2MouseClicked(evt);
            }
        });

        m_jName.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jName.setPreferredSize(new java.awt.Dimension(220, 30));

        jLabel3.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel3.setText(AppLocal.getIntString("label.locationaddress")); // NOI18N
        jLabel3.setPreferredSize(new java.awt.Dimension(110, 30));

        m_jAddress.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jAddress.setPreferredSize(new java.awt.Dimension(220, 30));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(m_jAddress, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_jName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(m_jName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(m_jAddress, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jLabel2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel2MouseClicked

        if (evt.getClickCount() == 2) {
            String uuidString = m_sID;
            StringSelection stringSelection = new StringSelection(uuidString);
            Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
            clpbrd.setContents(stringSelection, null);
        
            JOptionPane.showMessageDialog(this, 
                AppLocal.getIntString("message.uuidcopy"));
        }
    }//GEN-LAST:event_jLabel2MouseClicked
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JTextField m_jAddress;
    private javax.swing.JTextField m_jName;
    // End of variables declaration//GEN-END:variables
    
}
