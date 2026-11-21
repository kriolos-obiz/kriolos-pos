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

package com.openbravo.pos.admin;

import com.openbravo.basic.BasicException;
import com.openbravo.data.gui.ComboBoxValModel;
import com.openbravo.data.loader.ImageUtils;
import com.openbravo.data.user.DirtyManager;
import com.openbravo.data.user.EditorRecord;
import com.openbravo.format.Formats;
import com.openbravo.basic.AppLocal;
import com.openbravo.util.Base64Encoder;
import com.openbravo.ui.data.binding.SwingBinder;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

/**
 * Strongly typed editor panel for application resources (templates, images, scripts),
 * supporting two-way Swing data binding, headless testing, and real-time validation.
 *
 * @author adrianromero
 * @author KriolOS
 */
public final class ResourcesView extends JPanel implements EditorRecord<ResourceInfo> {
    
    private static final Logger LOGGER = Logger.getLogger(ResourcesView.class.getName());
    
    private static final long serialVersionUID = 1L;
    private String m_oId;
    private final ComboBoxValModel<ResourceType> m_ResourceModel;
    
    private final RSyntaxTextArea m_RSyntaxTextArea;
    private final RTextScrollPane m_RTextScrollPane;
    private final DirtyManager m_Dirty;
    private SwingBinder<ResourceInfo> binder;
            
    /** Creates new form ResourcesEditor
     * @param dirty */
    public ResourcesView(DirtyManager dirty) {
        initComponents();
        this.m_Dirty = dirty;
        
        JPanel cp = new JPanel(new BorderLayout());
        m_RSyntaxTextArea = new RSyntaxTextArea(20, 60);
        m_RSyntaxTextArea.setCodeFoldingEnabled(true);
        m_RTextScrollPane = new RTextScrollPane(m_RSyntaxTextArea);
        cp.add(m_RTextScrollPane);
        
        //Overview Editor with on CardLayout with "text" Identifier
        m_jContainer.add(cp, "text");
        
        m_ResourceModel = new ComboBoxValModel<>();
        m_ResourceModel.add(ResourceType.TEXT);
        m_ResourceModel.add(ResourceType.IMAGE);
        m_ResourceModel.add(ResourceType.BINARY);
        m_jType.setModel(m_ResourceModel);
        
        if (dirty != null) {
            m_jName.getDocument().addDocumentListener(dirty);
            m_jType.addActionListener(dirty);
            m_RSyntaxTextArea.getDocument().addDocumentListener(dirty);
            m_jImage.addPropertyChangeListener("image", dirty);
        }
        
        initValidator();
        writeValueEOF();        
    }

    private void initValidator() {
        binder = new SwingBinder<>(new ResourceInfo());

        // Mandatory Resource Name
        binder.forField(m_jName)
                .asRequired(AppLocal.getIntString("validation.required", AppLocal.getIntString("label.resname")))
                .bind(ResourceInfo::getName, ResourceInfo::setName);

        // Content binding for text area
        binder.forField(m_RSyntaxTextArea)
                .bind(
                        res -> res != null && res.getContent() != null && res.getRestype() == 0
                                ? Formats.BYTEA.formatValue(res.getContent())
                                : "",
                        (res, text) -> {
                            if (res != null && res.getRestype() == 0) {
                                byte[] bytes = null;
                                if (text != null && !text.trim().isEmpty()) {
                                    try {
                                        bytes = (byte[]) Formats.BYTEA.parseValue(text);
                                    } catch (BasicException ex) {
                                        bytes = text.getBytes(StandardCharsets.UTF_8);
                                    }
                                }
                                res.setContent(bytes);
                            }
                        }
                );

        binder.addStatusChangeListener(() -> {
            if (m_Dirty != null) {
                m_Dirty.setDirty(true);
            }
        });
    }

    public boolean isValid() {
        return binder == null || binder.validate();
    }

    public boolean isDataValid() {
        return isValid();
    }

    javax.swing.JTextField getNameField() {
        return m_jName;
    }

    javax.swing.JComboBox<ResourceType> getTypeCombo() {
        return m_jType;
    }

    RSyntaxTextArea getSyntaxTextArea() {
        return m_RSyntaxTextArea;
    }

    com.openbravo.data.gui.JImageEditor getImageEditor() {
        return m_jImage;
    }

    /**
     *
     */
    @Override
    public void writeValueEOF() {
        if (binder != null) {
            binder.clear();
        }
        m_oId = null;
        m_jName.setText(null);
        m_ResourceModel.setSelectedItem(null);
        m_RSyntaxTextArea.setText(null);
        m_jImage.setImage(null);     
        m_jName.setEnabled(false);
        m_jType.setEnabled(false);
        m_RSyntaxTextArea.setEnabled(false);
        m_jImage.setEnabled(false);
        m_jBtnReadScript.setEnabled(false);  
        
        m_RSyntaxTextArea.setText(null);
        m_RSyntaxTextArea.setEnabled(false);
    }

    /**
     *
     */
    @Override
    public void writeValueInsert() {
        if (binder != null) {
            binder.clear();
        }
        m_oId = UUID.randomUUID().toString();
        m_jName.setText(null);
        m_ResourceModel.setSelectedItem(ResourceType.TEXT);
        m_RSyntaxTextArea.setText(null);
        m_jImage.setImage(null);     
        m_jName.setEnabled(true);
        m_jType.setEnabled(true);
        m_RSyntaxTextArea.setEnabled(true);
        m_jImage.setEnabled(true);
        m_jBtnReadScript.setEnabled(true);  
        
        m_RSyntaxTextArea.setText(null);
        m_RSyntaxTextArea.setEnabled(true);
    }
    
    /**
     *
     * @param value
     */
    @Override
    public void writeValueDelete(ResourceInfo resource) {
        if (resource == null) {
            writeValueEOF();
            return;
        }
        m_oId = resource.getId();
        m_jName.setText(resource.getName());
        m_ResourceModel.setSelectedKey(resource.getRestype());
        
        ResourceType restype = (ResourceType) m_ResourceModel.getSelectedItem();
        if (restype == ResourceType.TEXT) {
            m_RSyntaxTextArea.setText(Formats.BYTEA.formatValue(resource.getContent()));
            m_RSyntaxTextArea.setSyntaxEditingStyle(getResourceSyntaxStyle(m_RSyntaxTextArea.getText()));
            m_RSyntaxTextArea.setCaretPosition(0);
            
            m_jImage.setImage(null);
        } else if (restype == ResourceType.IMAGE) {
            m_RSyntaxTextArea.setText(null);
            m_jImage.setImage(ImageUtils.readImage(resource.getContent()));
        } else if (restype == ResourceType.BINARY) {
            m_RSyntaxTextArea.setText(resource.getContent() == null
                    ? null
                    : Base64Encoder.encodeChunked(resource.getContent()));
            m_RSyntaxTextArea.setCaretPosition(0);
            
            m_jImage.setImage(null);
        } else {
            m_RSyntaxTextArea.setText(null);
            m_jImage.setImage(null);
        }
        m_jName.setEnabled(false);
        m_jType.setEnabled(false);
        m_jImage.setEnabled(false);
        m_jBtnReadScript.setEnabled(false);    
        m_RSyntaxTextArea.setEnabled(false);
        if (binder != null) {
            binder.readBean(resource);
        }
    }

    /**
     *
     * @param resource
     */
    @Override
    public void writeValueEdit(ResourceInfo resource) {
        if (resource == null) {
            writeValueEOF();
            return;
        }
        m_oId = resource.getId();
        m_jName.setText(resource.getName());
        m_ResourceModel.setSelectedKey(resource.getRestype());
        
        ResourceType restype = (ResourceType) m_ResourceModel.getSelectedItem();
        if (restype == ResourceType.TEXT) {
            
            m_RSyntaxTextArea.setText(Formats.BYTEA.formatValue(resource.getContent()));
            m_RSyntaxTextArea.setSyntaxEditingStyle(getResourceSyntaxStyle(m_RSyntaxTextArea.getText()));
            m_RSyntaxTextArea.setCaretPosition(0);
            
            m_jImage.setImage(null);
        } else if (restype == ResourceType.IMAGE) {
            
            m_RSyntaxTextArea.setText(null);
            m_jBtnReadScript.setVisible(false);       
            
            m_jImage.setImage(ImageUtils.readImage(resource.getContent()));     
        } else if (restype == ResourceType.BINARY) {
            m_RSyntaxTextArea.setText(resource.getContent() == null
                    ? null
                    : Base64Encoder.encodeChunked(resource.getContent()));
            m_RSyntaxTextArea.setCaretPosition(0); 
            
            m_jImage.setImage(null);          
        } else {
            m_RSyntaxTextArea.setText(null);
            m_jImage.setImage(null);           
        }
        m_jName.setEnabled(true);
        m_jType.setEnabled(true);
        
        m_jImage.setEnabled(true);
        m_RSyntaxTextArea.setEnabled(true);
        if (binder != null) {
            binder.readBean(resource);
        }
    }
    
    /**
     *
     * @return
     * @throws BasicException
     */
    @Override
    public ResourceInfo createValue() throws BasicException {
        String id = m_oId == null ? UUID.randomUUID().toString() : m_oId;
        String name = m_jName.getText();
        
        ResourceType restype = (ResourceType) m_ResourceModel.getSelectedItem();
        int type = restype != null && restype.getKey() != null ? (Integer) restype.getKey() : 0;
        byte[] content;
        if (restype == ResourceType.TEXT) {
            content = (byte[]) Formats.BYTEA.parseValue(m_RSyntaxTextArea.getText());
        } else if (restype == ResourceType.IMAGE) {
            content = ImageUtils.writeImage(m_jImage.getImage());
        } else if (restype == ResourceType.BINARY) {
            content = Base64Encoder.decode(m_RSyntaxTextArea.getText());
        } else {
            content = null;
        }

        ResourceInfo resource = new ResourceInfo(id, name, type, content);
        if (binder != null) {
            try {
                binder.writeBean(resource);
            } catch (SwingBinder.ValidationException ex) {
                // validation state is surfaced by binder.validate() / isValid()
            }
        }

        return resource;
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
    
    private String readFileAsString() { 
        JFileChooser chooser = new JFileChooser(); 
        chooser.showOpenDialog(null);
        File file = chooser.getSelectedFile();
        String data = null;
        //Only if file was selected
        if(file != null){
            String filename = file.getAbsolutePath();
            try {
                data = new String(Files.readAllBytes(Paths.get(filename)));
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Exception reading content: "+file.getAbsolutePath(), e);
            } 
        }
	return data; 
    }
    
    private String getResourceSyntaxStyle(String resource) {

        if (resource.contains("<?xml")) {
            return SyntaxConstants.SYNTAX_STYLE_XML;
        } else if (resource.contains("INSERT") || resource.contains("SELECT") 
                || resource.contains("UPDATE") || resource.contains("DELETE")) {
            return SyntaxConstants.SYNTAX_STYLE_SQL;
        } else {
            return SyntaxConstants.SYNTAX_STYLE_JAVA;
        }

    }

    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        m_jGroupType = new javax.swing.ButtonGroup();
        jPanel3 = new javax.swing.JPanel();
        m_jContainer = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        m_jText = new javax.swing.JTextArea();
        jPanel1 = new javax.swing.JPanel();
        m_jImage = new com.openbravo.data.gui.JImageEditor();
        jLabel2 = new javax.swing.JLabel();
        m_jName = new javax.swing.JTextField();
        m_jType = new javax.swing.JComboBox();
        m_jBtnReadScript = new javax.swing.JButton();

        jPanel3.setLayout(new java.awt.BorderLayout());

        m_jContainer.setLayout(new java.awt.CardLayout());

        m_jText.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jScrollPane1.setViewportView(m_jText);

        m_jContainer.add(jScrollPane1, "text");
        m_jContainer.add(jPanel1, "null");
        m_jContainer.add(m_jImage, "image");

        jPanel3.add(m_jContainer, java.awt.BorderLayout.CENTER);

        jLabel2.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel2.setText(AppLocal.getIntString("label.resname")); // NOI18N
        jLabel2.setPreferredSize(new java.awt.Dimension(0, 30));

        m_jName.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jName.setPreferredSize(new java.awt.Dimension(0, 30));

        m_jType.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jType.setPreferredSize(new java.awt.Dimension(150, 30));
        m_jType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jTypeActionPerformed(evt);
            }
        });

        m_jBtnReadScript.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("pos_messages"); // NOI18N
        m_jBtnReadScript.setText(bundle.getString("button.readscript")); // NOI18N
        m_jBtnReadScript.setPreferredSize(new java.awt.Dimension(80, 30));
        m_jBtnReadScript.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jBtnReadScriptActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, 549, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(m_jName, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(m_jType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(m_jBtnReadScript, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_jName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_jType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_jBtnReadScript, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, 328, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void m_jTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jTypeActionPerformed

        ResourceType restype = (ResourceType) m_ResourceModel.getSelectedItem();
        String viewType = "null";
        
        if (restype == ResourceType.TEXT) {
            viewType = "text";
            m_jBtnReadScript.setVisible(true);            
        } else if (restype == ResourceType.IMAGE) {
            viewType = "image";
            m_jBtnReadScript.setVisible(false);                        
        } else if (restype == ResourceType.BINARY) {
            viewType = "text";
            m_jBtnReadScript.setVisible(true);                        
        } else {
            viewType = "null";
            m_jBtnReadScript.setVisible(true);                        
        }

        CardLayout cl = (CardLayout)(m_jContainer.getLayout());
        cl.show(m_jContainer, viewType);  
      
    }//GEN-LAST:event_m_jTypeActionPerformed

    private void m_jBtnReadScriptActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jBtnReadScriptActionPerformed

        String data = readFileAsString();
        if (data != null) {
           m_RSyntaxTextArea.setText(data);            
        }
    }//GEN-LAST:event_m_jBtnReadScriptActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton m_jBtnReadScript;
    private javax.swing.JPanel m_jContainer;
    private javax.swing.ButtonGroup m_jGroupType;
    private com.openbravo.data.gui.JImageEditor m_jImage;
    private javax.swing.JTextField m_jName;
    private javax.swing.JTextArea m_jText;
    private javax.swing.JComboBox m_jType;
    // End of variables declaration//GEN-END:variables

    
}
