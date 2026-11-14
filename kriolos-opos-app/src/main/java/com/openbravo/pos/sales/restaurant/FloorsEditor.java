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

package com.openbravo.pos.sales.restaurant;

import com.openbravo.basic.AppLocal;
import com.openbravo.basic.BasicException;
import com.openbravo.data.gui.JImageEditor;
import com.openbravo.data.user.DirtyManager;
import com.openbravo.data.user.EditorRecord;
import com.openbravo.format.Formats;
import com.openbravo.ui.data.binding.SwingBinder;
import java.awt.Component;
import java.awt.image.BufferedImage;
import java.util.UUID;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Strongly typed editor component for restaurant Floor records,
 * supporting two-way Swing data binding and real-time validation.
 *
 * @author adrianromero
 * @author KriolOS
 */
public final class FloorsEditor extends JPanel implements EditorRecord<FloorsInfo> {
    
    private static final System.Logger LOGGER = System.getLogger(FloorsEditor.class.getName());
    private static final long serialVersionUID = 1L;

    private String m_sID;
    private DirtyManager dirty;
    private SwingBinder<FloorsInfo> binder;

    /**
     * Creates new form FloorsEditor.
     *
     * @param dirty dirty state listener
     */
    public FloorsEditor(DirtyManager dirty) {
        this.dirty = dirty;
        initComponents();
         
        m_jName.getDocument().addDocumentListener(dirty);
        m_jImage.addPropertyChangeListener("image", dirty);
        
        initValidator();
        writeValueEOF();
    }

    private void initValidator() {
        binder = new SwingBinder<>(new FloorsInfo());

        // Mandatory Floor Name
        binder.forField(m_jName)
                .asRequired(AppLocal.getIntString("validation.required", AppLocal.getIntString("Label.Name")))
                .bind(FloorsInfo::getName, FloorsInfo::setName);

        binder.addStatusChangeListener(() -> {
            if (dirty != null) {
                dirty.setDirty(true);
            }
        });
    }

    public boolean isValid() {
        return binder == null || binder.validate();
    }

    public SwingBinder<FloorsInfo> getBinder() {
        return binder;
    }

    JTextField getNameField() {
        return m_jName;
    }

    JImageEditor getImageEditor() {
        return m_jImage;
    }

    @Override
    public void writeValueEOF() {
        m_sID = null;
        if (binder != null) {
            binder.clear();
        } else {
            m_jName.setText(null);
        }
        m_jImage.setImage(null);

        m_jName.setEnabled(false);
        m_jImage.setEnabled(false);
    }  

    @Override
    public void writeValueInsert() {
        m_sID = UUID.randomUUID().toString(); 
        if (binder != null) {
            binder.clear();
        } else {
            m_jName.setText(null);
        }
        m_jImage.setImage(null);

        m_jName.setEnabled(true);
        m_jImage.setEnabled(true);
    }

    @Override
    public void writeValueDelete(FloorsInfo floor) {
        if (floor == null) {
            writeValueEOF();
            return;
        }
        m_sID = floor.getID();
        if (binder != null) {
            binder.readBean(floor);
        } else {
            m_jName.setText(Formats.STRING.formatValue(floor.getName()));
        }
        m_jImage.setImage(floor.getImage());

        m_jName.setEnabled(false);
        m_jImage.setEnabled(false);
    }    

    @Override
    public void writeValueEdit(FloorsInfo floor) {
        if (floor == null) {
            writeValueEOF();
            return;
        }
        m_sID = floor.getID();
        if (binder != null) {
            binder.readBean(floor);
        } else {
            m_jName.setText(Formats.STRING.formatValue(floor.getName()));
        }
        m_jImage.setImage(floor.getImage());

        m_jName.setEnabled(true);
        m_jImage.setEnabled(true);
    }

    @Override
    public FloorsInfo createValue() throws BasicException {
        FloorsInfo floor = new FloorsInfo(
                m_sID,
                m_jName.getText(),
                (BufferedImage) m_jImage.getImage()
        );

        if (binder != null) {
            try {
                binder.writeBean(floor);
            } catch (SwingBinder.ValidationException ex) {
                LOGGER.log(System.Logger.Level.WARNING, "Validation in createValue: " + ex.getMessage());
            }
        }

        if (floor.getName() == null) {
            floor.setName(m_jName.getText());
        }
        if (floor.getImage() == null && m_jImage.getImage() != null) {
            floor.setImage((BufferedImage) m_jImage.getImage());
        }

        return floor;
    }

    @Override
    public Component getComponent() {
        return this;
    }
    
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

        m_jName = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        m_jImage = new com.openbravo.data.gui.JImageEditor();

        setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N

        m_jName.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N

        jLabel2.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jLabel2.setText(AppLocal.getIntString("Label.Name")); // NOI18N

        m_jImage.setMaxDimensions(new java.awt.Dimension(640, 480));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(m_jName, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(m_jImage, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(19, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(m_jName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(m_jImage, javax.swing.GroupLayout.PREFERRED_SIZE, 226, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(31, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel2;
    private com.openbravo.data.gui.JImageEditor m_jImage;
    private javax.swing.JTextField m_jName;
    // End of variables declaration//GEN-END:variables
    
}
