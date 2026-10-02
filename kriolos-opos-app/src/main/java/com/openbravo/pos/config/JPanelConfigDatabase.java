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

package com.openbravo.pos.config;

import com.openbravo.data.gui.JMessagePanel;
import com.openbravo.data.gui.MessageInf;
import com.openbravo.data.loader.Session;
import com.openbravo.data.user.DirtyManager;
import com.openbravo.pos.forms.AppConfig;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.util.AltEncrypter;
import java.awt.Component;
import java.awt.HeadlessException;
import java.sql.Connection;
import java.sql.SQLException;
import javax.swing.JOptionPane;

/**
 * @author Jack Gerrard
 * @author adrianromero
 */
public class JPanelConfigDatabase extends javax.swing.JPanel implements PanelConfig {
    
    private final DirtyManager dirty = new DirtyManager();
    
    /** Creates new form JPanelConfigDatabase */
    public JPanelConfigDatabase() {
        
        initComponents();
        jcboDBDriver.addActionListener(dirty);
        jcboDBDriver.addItem("MariaDB");
        jcboDBDriver.addItem("MySQL");
        jcboDBDriver.addItem("HSQLDB");
        jcboDBDriver.setSelectedIndex(0);
        multiDB.addActionListener(dirty);        
        
// primary DB        
        jtxtDbName.getDocument().addDocumentListener(dirty);
        jtxtDbURL.getDocument().addDocumentListener(dirty);
        jtxtDbSchema.getDocument().addDocumentListener(dirty);        
        jtxtDbOptions.getDocument().addDocumentListener(dirty);
        jtxtDbPassword.getDocument().addDocumentListener(dirty);
        jtxtDbUser.getDocument().addDocumentListener(dirty);        

// secondary DB        
        jtxtDbName1.getDocument().addDocumentListener(dirty);        
        jtxtDbURL1.getDocument().addDocumentListener(dirty);
        jtxtDbSchema1.getDocument().addDocumentListener(dirty);                
        jtxtDbOptions1.getDocument().addDocumentListener(dirty);
        jtxtDbPassword1.getDocument().addDocumentListener(dirty);
        jtxtDbUser1.getDocument().addDocumentListener(dirty);        
        
        checkMulBD();

    }

    /**
     *
     * @return
     */
    @Override
    public boolean hasChanged() {
        return dirty.isDirty();
    }
    
    /**
     *
     * @return
     */
    @Override
    public Component getConfigComponent() {
        return this;
    }
   
    /**
     *
     * @param config
     */
    @Override
    public void loadProperties(AppConfig config) {
        
        multiDB.setSelected(Boolean.parseBoolean(config.getProperty("db.multi")));                

        jcboDBDriver.setSelectedItem(config.getProperty("db.engine"));

// primary DB              
        jtxtDbName.setText(config.getProperty("db.name"));
        jtxtDbURL.setText(config.getProperty("db.URL"));
        jtxtDbSchema.setText(config.getProperty("db.schema"));        
        jtxtDbOptions.setText(config.getProperty("db.options"));
        String sDBUser = config.getProperty("db.user");
        String sDBPassword = config.getProperty("db.password");        

        if (sDBUser != null && sDBPassword != null && sDBPassword.startsWith("crypt:")) {
            AltEncrypter cypher = new AltEncrypter("cypherkey" + sDBUser);
            sDBPassword = cypher.decrypt(sDBPassword.substring(6));
        }        
        jtxtDbUser.setText(sDBUser);
        jtxtDbPassword.setText(sDBPassword);   

// secondary DB        
        jtxtDbName1.setText(config.getProperty("db1.name"));
        jtxtDbURL1.setText(config.getProperty("db1.URL"));
        jtxtDbSchema1.setText(config.getProperty("db1.schema"));                
        jtxtDbOptions1.setText(config.getProperty("db1.options"));        
        String sDBUser1 = config.getProperty("db1.user");
        String sDBPassword1 = config.getProperty("db1.password");        

        if (sDBUser1 != null && sDBPassword1 != null && sDBPassword1.startsWith("crypt:")) {
            AltEncrypter cypher = new AltEncrypter("cypherkey" + sDBUser1);
            sDBPassword1 = cypher.decrypt(sDBPassword1.substring(6));
        }        
        jtxtDbUser1.setText(sDBUser1);
        jtxtDbPassword1.setText(sDBPassword1);          
        
        //dirty.setDirty(false);
        
        checkMulBD();
    }
   
    /**
     *
     * @param config
     */
    @Override
    public void saveProperties(AppConfig config) {

// multi-db        
        config.setProperty("db.multi",Boolean.toString(multiDB.isSelected()));
        
        config.setProperty("db.engine", comboValue(jcboDBDriver.getSelectedItem()));

// primary DB
        config.setProperty("db.name", jtxtDbName.getText());
        config.setProperty("db.URL", jtxtDbURL.getText());
        config.setProperty("db.schema", jtxtDbSchema.getText());        
        config.setProperty("db.options", jtxtDbOptions.getText());
        config.setProperty("db.user", jtxtDbUser.getText());
        AltEncrypter cypher = new AltEncrypter("cypherkey" + jtxtDbUser.getText());       
        config.setProperty("db.password", "crypt:" + 
                cypher.encrypt(new String(jtxtDbPassword.getPassword())));

// secondary DB        
        config.setProperty("db1.name", jtxtDbName1.getText());        
        config.setProperty("db1.URL", jtxtDbURL1.getText());
        config.setProperty("db1.schema", jtxtDbSchema1.getText());                
        config.setProperty("db1.options", jtxtDbOptions1.getText());
        config.setProperty("db1.user", jtxtDbUser1.getText());
        cypher = new AltEncrypter("cypherkey" + jtxtDbUser1.getText());       
        config.setProperty("db1.password", "crypt:" + 
                cypher.encrypt(new String(jtxtDbPassword1.getPassword())));        

        dirty.setDirty(false);
    }

    private String comboValue(Object value) {
        return value == null ? "" : value.toString();
    }
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel6 = new javax.swing.JLabel();
        jcboDBDriver = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        jtxtDbURL = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jtxtDbUser = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jtxtDbPassword = new javax.swing.JPasswordField();
        jButtonTest = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel5 = new javax.swing.JLabel();
        jButtonTest1 = new javax.swing.JButton();
        jtxtDbPassword1 = new javax.swing.JPasswordField();
        jtxtDbUser1 = new javax.swing.JTextField();
        jtxtDbURL1 = new javax.swing.JTextField();
        jLblDbURL1 = new javax.swing.JLabel();
        jLblDbUser1 = new javax.swing.JLabel();
        jLblDbPassword1 = new javax.swing.JLabel();
        jLblDBName = new javax.swing.JLabel();
        jtxtDbName = new javax.swing.JTextField();
        jLblDbName1 = new javax.swing.JLabel();
        jtxtDbName1 = new javax.swing.JTextField();
        multiDB = new javax.swing.JCheckBox();
        jLabel7 = new javax.swing.JLabel();
        jtxtDbSchema = new javax.swing.JTextField();
        jLblDbSchema1 = new javax.swing.JLabel();
        jtxtDbSchema1 = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        jtxtDbOptions = new javax.swing.JTextField();
        jLblDbOptions1 = new javax.swing.JLabel();
        jtxtDbOptions1 = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();

        setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        setOpaque(false);
        setPreferredSize(new java.awt.Dimension(900, 500));

        jLabel6.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("pos_messages"); // NOI18N
        jLabel6.setText(bundle.getString("label.database")); // NOI18N
        jLabel6.setPreferredSize(new java.awt.Dimension(125, 30));

        jcboDBDriver.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jcboDBDriver.setPreferredSize(new java.awt.Dimension(150, 30));
        jcboDBDriver.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jcboDBDriverActionPerformed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel2.setText(AppLocal.getIntString("label.DbURL")); // NOI18N
        jLabel2.setPreferredSize(new java.awt.Dimension(125, 30));

        jtxtDbURL.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jtxtDbURL.setPreferredSize(new java.awt.Dimension(275, 30));
        jtxtDbURL.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jtxtDbURLActionPerformed(evt);
            }
        });

        jLabel3.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel3.setText(AppLocal.getIntString("label.DbUser")); // NOI18N
        jLabel3.setPreferredSize(new java.awt.Dimension(125, 30));

        jtxtDbUser.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jtxtDbUser.setPreferredSize(new java.awt.Dimension(150, 30));

        jLabel4.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel4.setText(AppLocal.getIntString("label.DbPassword")); // NOI18N
        jLabel4.setPreferredSize(new java.awt.Dimension(125, 30));

        jtxtDbPassword.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jtxtDbPassword.setPreferredSize(new java.awt.Dimension(150, 30));

        jButtonTest.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jButtonTest.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/database.png"))); // NOI18N
        jButtonTest.setText(bundle.getString("button.test")); // NOI18N
        jButtonTest.setPreferredSize(new java.awt.Dimension(110, 45));
        jButtonTest.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonTestActionPerformed(evt);
            }
        });

        jLabel5.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/logo.png"))); // NOI18N
        jLabel5.setText(bundle.getString("message.DBDefault")); // NOI18N
        jLabel5.setToolTipText("");
        jLabel5.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        jLabel5.setPreferredSize(new java.awt.Dimension(889, 120));

        jButtonTest1.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jButtonTest1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/database.png"))); // NOI18N
        jButtonTest1.setText(bundle.getString("button.test")); // NOI18N
        jButtonTest1.setEnabled(false);
        jButtonTest1.setPreferredSize(new java.awt.Dimension(110, 45));
        jButtonTest1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonTest1ActionPerformed(evt);
            }
        });

        jtxtDbPassword1.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jtxtDbPassword1.setEnabled(false);
        jtxtDbPassword1.setPreferredSize(new java.awt.Dimension(150, 30));

        jtxtDbUser1.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jtxtDbUser1.setEnabled(false);
        jtxtDbUser1.setPreferredSize(new java.awt.Dimension(150, 30));

        jtxtDbURL1.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jtxtDbURL1.setEnabled(false);
        jtxtDbURL1.setPreferredSize(new java.awt.Dimension(275, 30));

        jLblDbURL1.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLblDbURL1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLblDbURL1.setText(AppLocal.getIntString("label.DbURL")); // NOI18N
        jLblDbURL1.setEnabled(false);
        jLblDbURL1.setPreferredSize(new java.awt.Dimension(125, 30));

        jLblDbUser1.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLblDbUser1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLblDbUser1.setText(AppLocal.getIntString("label.DbUser")); // NOI18N
        jLblDbUser1.setEnabled(false);
        jLblDbUser1.setPreferredSize(new java.awt.Dimension(100, 30));

        jLblDbPassword1.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLblDbPassword1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLblDbPassword1.setText(AppLocal.getIntString("label.DbPassword")); // NOI18N
        jLblDbPassword1.setEnabled(false);
        jLblDbPassword1.setPreferredSize(new java.awt.Dimension(100, 30));

        jLblDBName.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        jLblDBName.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLblDBName.setText(AppLocal.getIntString("label.DbName")); // NOI18N
        jLblDBName.setPreferredSize(new java.awt.Dimension(125, 30));

        jtxtDbName.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jtxtDbName.setPreferredSize(new java.awt.Dimension(275, 30));

        jLblDbName1.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        jLblDbName1.setText(AppLocal.getIntString("label.DbName1")); // NOI18N
        jLblDbName1.setEnabled(false);
        jLblDbName1.setPreferredSize(new java.awt.Dimension(125, 30));

        jtxtDbName1.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jtxtDbName1.setEnabled(false);
        jtxtDbName1.setPreferredSize(new java.awt.Dimension(275, 30));

        multiDB.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        multiDB.setText(AppLocal.getIntString("label.multidb")); // NOI18N
        multiDB.setToolTipText(AppLocal.getIntString("label.multidb")); // NOI18N
        multiDB.setPreferredSize(new java.awt.Dimension(80, 30));
        multiDB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                multiDBActionPerformed(evt);
            }
        });

        jLabel7.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel7.setText(AppLocal.getIntString("label.DbSchema")); // NOI18N
        jLabel7.setPreferredSize(new java.awt.Dimension(125, 30));

        jtxtDbSchema.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jtxtDbSchema.setPreferredSize(new java.awt.Dimension(275, 30));

        jLblDbSchema1.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLblDbSchema1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLblDbSchema1.setText(AppLocal.getIntString("label.DbSchema")); // NOI18N
        jLblDbSchema1.setEnabled(false);
        jLblDbSchema1.setPreferredSize(new java.awt.Dimension(100, 30));

        jtxtDbSchema1.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jtxtDbSchema1.setEnabled(false);
        jtxtDbSchema1.setPreferredSize(new java.awt.Dimension(275, 30));

        jLabel8.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel8.setText(AppLocal.getIntString("label.DbOptions")); // NOI18N
        jLabel8.setPreferredSize(new java.awt.Dimension(125, 30));

        jtxtDbOptions.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jtxtDbOptions.setPreferredSize(new java.awt.Dimension(275, 30));

        jLblDbOptions1.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLblDbOptions1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLblDbOptions1.setText(AppLocal.getIntString("label.DbOptions")); // NOI18N
        jLblDbOptions1.setEnabled(false);
        jLblDbOptions1.setPreferredSize(new java.awt.Dimension(100, 30));

        jtxtDbOptions1.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jtxtDbOptions1.setEnabled(false);
        jtxtDbOptions1.setPreferredSize(new java.awt.Dimension(275, 30));

        jLabel9.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(51, 204, 255));
        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel9.setText(bundle.getString("jpanelconfigdatabase.msg.info")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 880, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 880, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jtxtDbOptions, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)
                                    .addComponent(jtxtDbSchema, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)
                                    .addComponent(jtxtDbUser, javax.swing.GroupLayout.DEFAULT_SIZE, 261, Short.MAX_VALUE)
                                    .addComponent(jtxtDbPassword, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jButtonTest, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jLblDBName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(18, 18, 18)
                                        .addComponent(jtxtDbURL, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(18, 18, 18)
                                        .addComponent(jtxtDbName, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)))))
                        .addGap(40, 40, 40)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLblDbName1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLblDbURL1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLblDbSchema1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLblDbOptions1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLblDbUser1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLblDbPassword1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jtxtDbName1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(jtxtDbURL1, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE))
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                    .addGap(11, 11, 11)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(jtxtDbOptions1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jtxtDbSchema1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jButtonTest1, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jtxtDbUser1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jtxtDbPassword1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jcboDBDriver, javax.swing.GroupLayout.PREFERRED_SIZE, 188, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(multiDB, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 501, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(10, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jcboDBDriver, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(multiDB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLblDBName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLblDbName1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jtxtDbName1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jtxtDbName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLblDbURL1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jtxtDbURL1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jtxtDbURL, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLblDbSchema1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jtxtDbSchema1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jtxtDbSchema, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLblDbOptions1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jtxtDbOptions, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jtxtDbOptions1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jtxtDbUser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jtxtDbUser1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLblDbUser1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jtxtDbPassword1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLblDbPassword1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jtxtDbPassword, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButtonTest, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonTest1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(38, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jcboDBDriverActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jcboDBDriverActionPerformed
           
        String selected = (String)jcboDBDriver.getSelectedItem();
        
        switch(selected){
            case "MySQL":                   
                //jtxtDbOptions.setText("?characterEncoding=utf8&zeroDateTimeBehavior=convertToNull");
                break;
            case "MariaDB":            
                //jtxtDbOptions.setText("?characterEncoding=utf8");
                break;
            case "PostgreSQL":
                break;
            case "Derby":
                //jtxtDbOptions.setText(";create=true");
                break;
            case "HSQLDB":
                //jtxtDbOptions.setText(";shutdown=true");
                break;
        }    
    }//GEN-LAST:event_jcboDBDriverActionPerformed

    private void jButtonTestActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonTestActionPerformed
        try {
            String url = jtxtDbURL.getText() + jtxtDbSchema.getText() + jtxtDbOptions.getText();
            String user = jtxtDbUser.getText();
            String password = new String(jtxtDbPassword.getPassword());

            Session session =  new Session(url, user, password);
            Connection connection = session.getConnection();
            boolean isValid;
            isValid = (connection == null) ? false : connection.isValid(1000);
            
            if (isValid) {
                JOptionPane.showMessageDialog(this, 
                        AppLocal.getIntString("message.databasesuccess"), 
                        "Connection Test", JOptionPane.INFORMATION_MESSAGE);
              
            } else {
                JMessagePanel.showMessage(this, 
                        new MessageInf(MessageInf.SGN_WARNING, "Connection Error"));
            }
        } catch (SQLException e) {
            JMessagePanel.showMessage(this, 
                    new MessageInf(MessageInf.SGN_WARNING, 
                            AppLocal.getIntString("message.databaseconnectionerror"), e));            
        } catch (HeadlessException e) {
            JMessagePanel.showMessage(this, 
                    new MessageInf(MessageInf.SGN_WARNING, "Unknown exception", e));
        }
    }//GEN-LAST:event_jButtonTestActionPerformed

    private void jButtonTest1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonTest1ActionPerformed
        try {
            String url = jtxtDbURL1.getText() + jtxtDbSchema1.getText() + jtxtDbOptions1.getText();
            String user = jtxtDbUser1.getText();
            String password = new String(jtxtDbPassword1.getPassword());

            Session session =  new Session(url, user, password);
            Connection connection = session.getConnection();
            boolean isValid;
            isValid = (connection == null) ? false : connection.isValid(1000);

            if (isValid) {
                JOptionPane.showMessageDialog(this, 
                        AppLocal.getIntString("message.databasesuccess"), 
                        "Connection Test", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JMessagePanel.showMessage(this, 
                        new MessageInf(MessageInf.SGN_WARNING, "Connection Error"));
            }
        } catch (SQLException e) {
            JMessagePanel.showMessage(this, 
                    new MessageInf(MessageInf.SGN_WARNING, 
                            AppLocal.getIntString("message.databaseconnectionerror"), e));
        } catch (HeadlessException e) {
            JMessagePanel.showMessage(this, 
                    new MessageInf(MessageInf.SGN_WARNING, "Unknown exception", e));
        }
    }//GEN-LAST:event_jButtonTest1ActionPerformed

    private void checkMulBD(){

        if (multiDB.isSelected()) {
            jLblDbName1.setEnabled(true);
            jtxtDbName1.setEnabled(true);
            jLblDbOptions1.setEnabled(true);
            jtxtDbOptions1.setEnabled(true);  
//            jLblDBPort1.setEnabled(true);
//            jtxtDbPort1.setEnabled(true);            
            jLblDbURL1.setEnabled(true);
            jtxtDbURL1.setEnabled(true);            
            jLblDbSchema1.setEnabled(true);
            jtxtDbSchema1.setEnabled(true);            
            jLblDbUser1.setEnabled(true);
            jtxtDbUser1.setEnabled(true);
            jLblDbPassword1.setEnabled(true);
            jtxtDbPassword1.setEnabled(true);
            jButtonTest1.setEnabled(true);
        } else {
            jLblDbName1.setEnabled(false);
            jtxtDbName1.setEnabled(false);
            jLblDbOptions1.setEnabled(false);
            jtxtDbOptions1.setEnabled(false);  
//            jLblDBPort1.setEnabled(false);
//            jtxtDbPort1.setEnabled(false);            
            jLblDbURL1.setEnabled(false);
            jtxtDbURL1.setEnabled(false);
            jLblDbSchema1.setEnabled(false);
            jtxtDbSchema1.setEnabled(false);              
            jLblDbUser1.setEnabled(false);
            jtxtDbUser1.setEnabled(false);
            jLblDbPassword1.setEnabled(false);
            jtxtDbPassword1.setEnabled(false);            
            jButtonTest1.setEnabled(false);            
        }
    }
    private void multiDBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_multiDBActionPerformed
        checkMulBD();
    }//GEN-LAST:event_multiDBActionPerformed

    private void jtxtDbURLActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jtxtDbURLActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jtxtDbURLActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonTest;
    private javax.swing.JButton jButtonTest1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLblDBName;
    private javax.swing.JLabel jLblDbName1;
    private javax.swing.JLabel jLblDbOptions1;
    private javax.swing.JLabel jLblDbPassword1;
    private javax.swing.JLabel jLblDbSchema1;
    private javax.swing.JLabel jLblDbURL1;
    private javax.swing.JLabel jLblDbUser1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JComboBox jcboDBDriver;
    private javax.swing.JTextField jtxtDbName;
    private javax.swing.JTextField jtxtDbName1;
    private javax.swing.JTextField jtxtDbOptions;
    private javax.swing.JTextField jtxtDbOptions1;
    private javax.swing.JPasswordField jtxtDbPassword;
    private javax.swing.JPasswordField jtxtDbPassword1;
    private javax.swing.JTextField jtxtDbSchema;
    private javax.swing.JTextField jtxtDbSchema1;
    private javax.swing.JTextField jtxtDbURL;
    private javax.swing.JTextField jtxtDbURL1;
    private javax.swing.JTextField jtxtDbUser;
    private javax.swing.JTextField jtxtDbUser1;
    private javax.swing.JCheckBox multiDB;
    // End of variables declaration//GEN-END:variables

     
}
