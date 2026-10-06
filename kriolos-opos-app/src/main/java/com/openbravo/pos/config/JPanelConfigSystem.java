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

import com.openbravo.data.user.DirtyManager;
import com.openbravo.pos.forms.AppConfig;
import java.awt.*;


/**
 *
 * @author JG uniCenta
 */
public class JPanelConfigSystem extends javax.swing.JPanel implements PanelConfig {
    
    private DirtyManager dirty = new DirtyManager();
    
    /** Creates new form JPanelConfigDatabase */
    public JPanelConfigSystem() {

        initComponents();

        jTextAutoLogoffTime.getDocument().addDocumentListener(dirty);
        jchkInstance.addActionListener(dirty);
        jchkTextOverlay.addActionListener(dirty);
        jchkAutoLogoff.addActionListener(dirty);
        jchkAutoLogoffToTables.addActionListener(dirty);
        jTaxIncluded.addActionListener(dirty);
        jCheckPrice00.addActionListener(dirty);          
        jMoveAMountBoxToTop.addActionListener(dirty);
        jCloseCashbtn.addActionListener(dirty);
        jchkautoRefreshTableMap.addActionListener(dirty);
        jTxtautoRefreshTimer.getDocument().addDocumentListener(dirty);       
        jchkSCOnOff.addActionListener(dirty);
        jchkSCRestaurant.addActionListener(dirty);        
        jTextSCRate.getDocument().addDocumentListener(dirty);   
        jchkPriceUpdate.addActionListener(dirty);
        jchkBarcodetype.addActionListener(dirty);
        jchkShowCustomerDetails.addActionListener(dirty);
        jchkShowWaiterDetails.addActionListener(dirty);
        CustomerColour.addActionListener(dirty);
        WaiterColour.addActionListener(dirty);
        TableNameColour.addActionListener(dirty);
        jchkTransBtn.addActionListener(dirty);
        jchkOverride.addActionListener(dirty);
        jtxtPIN.getDocument().addDocumentListener(dirty);        
         
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
     
        String timerCheck =(config.getProperty("till.autotimer"));
        if (timerCheck == null){
            config.setProperty("till.autotimer","100");
        } else {                
            jTextAutoLogoffTime.setText(config.getProperty("till.autotimer"));
        }

        String autoRefreshtimerCheck =(config.getProperty("till.autoRefreshTimer"));
        if (autoRefreshtimerCheck == null){
            config.setProperty("till.autoRefreshTableMap","true");                        
            config.setProperty("till.autoRefreshTimer","5");
        }
        jTxtautoRefreshTimer.setText(config.getProperty("till.autoRefreshTimer"));

        jchkInstance.setSelected(Boolean.parseBoolean(config.getProperty("machine.uniqueinstance"))); 

        jchkTextOverlay.setSelected(Boolean.parseBoolean(config.getProperty("payments.textoverlay")));        
        jchkAutoLogoff.setSelected(Boolean.parseBoolean(config.getProperty("till.autoLogoff")));    
        jchkAutoLogoffToTables.setSelected(Boolean.parseBoolean(config.getProperty("till.autoLogoffrestaurant")));           
        jTaxIncluded.setSelected(Boolean.parseBoolean(config.getProperty("till.taxincluded")));
        jCheckPrice00.setSelected(Boolean.parseBoolean(config.getProperty("till.pricewith00")));        
        jMoveAMountBoxToTop.setSelected(Boolean.parseBoolean(config.getProperty("till.amountattop")));  
        jCloseCashbtn.setSelected(Boolean.parseBoolean(config.getProperty("screen.600800")));
        jchkautoRefreshTableMap.setSelected(Boolean.parseBoolean(config.getProperty("till.autoRefreshTableMap")));  
        jchkPriceUpdate.setSelected(AppConfig.getInstance().getBoolean("db.prodpriceupdate"));
        jchkBarcodetype.setSelected(Boolean.parseBoolean(config.getProperty("machine.barcodetype")));  
        
        String txtPIN =(config.getProperty("override.pin"));
        if (txtPIN == null){
            config.setProperty("override.check","true");                        
            config.setProperty("override.pin","1234");
        }       
        jchkOverride.setSelected(Boolean.parseBoolean(config.getProperty("override.check")));
        jtxtPIN.setText(config.getProperty("override.pin"));         
        
/** Added: JG 23 July 13 */      
        String SCCheck =(config.getProperty("till.SCRate"));
        if (SCCheck == null){
            config.setProperty("till.SCRate","0");
        }                
        jTextSCRate.setText(config.getProperty("till.SCRate"));
        jchkSCOnOff.setSelected(Boolean.parseBoolean(config.getProperty("till.SCOnOff")));    
        jchkSCRestaurant.setSelected(Boolean.parseBoolean(config.getProperty("till.SCRestaurant")));
        
        if (jchkSCOnOff.isSelected()){
                jchkSCRestaurant.setVisible(true);
                jLabelSCRate.setVisible(true);
                jTextSCRate.setVisible(true);
                jLabelSCRatePerCent.setVisible(true);
        }else{    
                jchkSCRestaurant.setVisible(false);
                jLabelSCRate.setVisible(false);
                jTextSCRate.setVisible(false);
                jLabelSCRatePerCent.setVisible(false);
        }                       
        
        if (jchkAutoLogoff.isSelected()){
                jchkAutoLogoffToTables.setVisible(true);
                jLabelTimedMessage.setVisible(true);
                jTextAutoLogoffTime.setVisible(true);
        }else{    
                jchkAutoLogoffToTables.setVisible(false);
                jLabelTimedMessage.setVisible(false);
                jTextAutoLogoffTime.setVisible(false);
        }
        
        if (jchkautoRefreshTableMap.isSelected()){
                jLblautoRefresh.setVisible(true);
                jTxtautoRefreshTimer.setVisible(true);
        }else{    
                jLblautoRefresh.setVisible(false);
                jTxtautoRefreshTimer.setVisible(false);
        }

        String customerCheck =(config.getProperty("table.showcustomerdetails"));
        if (customerCheck == null) {
            config.setProperty("table.showcustomerdetails","true");                      
        }
        jchkShowCustomerDetails.setSelected(Boolean.parseBoolean(config.getProperty("table.showcustomerdetails")));
        if (config.getProperty("table.customercolour")==null){
            CustomerColour.reset();
        }else{
            CustomerColour.setColor(config.getProperty("table.customercolour"));
        }

        String waiterCheck =(config.getProperty("table.showwaiterdetails"));
        if (waiterCheck == null) {
            config.setProperty("table.showwaiterdetails","true");                      
        }
        jchkShowWaiterDetails.setSelected(Boolean.parseBoolean(config.getProperty("table.showwaiterdetails")));        
        if (config.getProperty("table.waitercolour")==null){
            WaiterColour.reset();
        }else{
            WaiterColour.setColor(config.getProperty("table.waitercolour"));
        }
        if (config.getProperty("table.tablecolour")==null){                
            TableNameColour.reset();      
        }else{
            TableNameColour.setColor(config.getProperty("table.tablecolour"));  
        }
        
        jchkTransBtn.setSelected(Boolean.parseBoolean(config.getProperty("table.transbtn")));                
       
        dirty.setDirty(false);
    }
   
    /**
     *
     * @param config
     */
    @Override
    public void saveProperties(AppConfig config) {
        
        config.setProperty("till.autotimer",jTextAutoLogoffTime.getText());
        config.setProperty("machine.uniqueinstance", Boolean.toString(jchkInstance.isSelected()));
        config.setProperty("table.showcustomerdetails", Boolean.toString(jchkShowCustomerDetails.isSelected()));
        config.setProperty("table.showwaiterdetails", Boolean.toString(jchkShowWaiterDetails.isSelected()));        
        config.setProperty("payments.textoverlay", Boolean.toString(jchkTextOverlay.isSelected()));         
        config.setProperty("till.autoLogoff", Boolean.toString(jchkAutoLogoff.isSelected()));                 
        config.setProperty("till.autoLogoffrestaurant", Boolean.toString(jchkAutoLogoffToTables.isSelected()));                        
        config.setProperty("table.customercolour",CustomerColour.getHexColor());
        config.setProperty("table.waitercolour",WaiterColour.getHexColor());
        config.setProperty("table.tablecolour",TableNameColour.getHexColor());
        config.setProperty("till.taxincluded",Boolean.toString(jTaxIncluded.isSelected()));                     
        config.setProperty("till.pricewith00",Boolean.toString(jCheckPrice00.isSelected()));                         
        config.setProperty("till.amountattop",Boolean.toString(jMoveAMountBoxToTop.isSelected()));         
        config.setProperty("screen.600800",Boolean.toString(jCloseCashbtn.isSelected())); 
        config.setProperty("till.autoRefreshTableMap", Boolean.toString(jchkautoRefreshTableMap.isSelected()));                 
        config.setProperty("till.autoRefreshTimer", jTxtautoRefreshTimer.getText());

        config.setProperty("till.SCOnOff",Boolean.toString(jchkSCOnOff.isSelected()));
        config.setProperty("till.SCRate",jTextSCRate.getText());
        config.setProperty("till.SCRestaurant",Boolean.toString(jchkSCRestaurant.isSelected()));

        config.setProperty("db.prodpriceupdate", Boolean.toString(jchkPriceUpdate.isSelected()));        
        config.setProperty("machine.barcodetype", Boolean.toString(jchkBarcodetype.isSelected()));
        
        config.setProperty("table.transbtn", Boolean.toString(jchkTransBtn.isSelected()));
        
        config.setProperty("override.check", Boolean.toString(jchkOverride.isSelected()));
        config.setProperty("override.pin",jtxtPIN.getText());        
        
        dirty.setDirty(false);
    }
       
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jchkInstance = new javax.swing.JCheckBox();
        jTextAutoLogoffTime = new javax.swing.JTextField();
        jLabelTimedMessage = new javax.swing.JLabel();
        jchkAutoLogoff = new javax.swing.JCheckBox();
        jchkAutoLogoffToTables = new javax.swing.JCheckBox();
        jchkShowCustomerDetails = new javax.swing.JCheckBox();
        jchkShowWaiterDetails = new javax.swing.JCheckBox();
        jLabelTableNameTextColour = new javax.swing.JLabel();
        jCheckPrice00 = new javax.swing.JCheckBox();
        jTaxIncluded = new javax.swing.JCheckBox();
        jCloseCashbtn = new javax.swing.JCheckBox();
        jMoveAMountBoxToTop = new javax.swing.JCheckBox();
        jchkTextOverlay = new javax.swing.JCheckBox();
        jchkautoRefreshTableMap = new javax.swing.JCheckBox();
        jTxtautoRefreshTimer = new javax.swing.JTextField();
        jLblautoRefresh = new javax.swing.JLabel();
        jchkSCOnOff = new javax.swing.JCheckBox();
        jLabelSCRate = new javax.swing.JLabel();
        jTextSCRate = new javax.swing.JTextField();
        jLabelSCRatePerCent = new javax.swing.JLabel();
        jchkSCRestaurant = new javax.swing.JCheckBox();
        jchkPriceUpdate = new javax.swing.JCheckBox();
        jchkBarcodetype = new javax.swing.JCheckBox();
        jchkTransBtn = new javax.swing.JCheckBox();
        WaiterColour = new com.openbravo.data.gui.JColorPicker();
        TableNameColour = new com.openbravo.data.gui.JColorPicker();
        CustomerColour = new com.openbravo.data.gui.JColorPicker();
        jchkOverride = new javax.swing.JCheckBox();
        jtxtPIN = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();

        setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        setOpaque(false);
        setPreferredSize(new java.awt.Dimension(800, 600));

        jLabel1.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("pos_messages"); // NOI18N
        jLabel1.setText(bundle.getString("label.configOptionStartup")); // NOI18N
        jLabel1.setPreferredSize(new java.awt.Dimension(250, 30));

        jLabel2.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        jLabel2.setText(bundle.getString("label.configOptionKeypad")); // NOI18N
        jLabel2.setPreferredSize(new java.awt.Dimension(250, 30));

        jLabel3.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        jLabel3.setText(bundle.getString("label.configOptionLogOff")); // NOI18N
        jLabel3.setPreferredSize(new java.awt.Dimension(100, 30));

        jLabel4.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        jLabel4.setText(bundle.getString("label.configOptionRestaurant")); // NOI18N
        jLabel4.setPreferredSize(new java.awt.Dimension(250, 30));

        jchkInstance.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jchkInstance.setSelected(true);
        jchkInstance.setText(bundle.getString("label.instance")); // NOI18N
        jchkInstance.setMaximumSize(new java.awt.Dimension(0, 25));
        jchkInstance.setMinimumSize(new java.awt.Dimension(0, 0));
        jchkInstance.setPreferredSize(new java.awt.Dimension(250, 25));

        jTextAutoLogoffTime.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jTextAutoLogoffTime.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        jTextAutoLogoffTime.setText("0");
        jTextAutoLogoffTime.setMaximumSize(new java.awt.Dimension(0, 25));
        jTextAutoLogoffTime.setMinimumSize(new java.awt.Dimension(0, 0));
        jTextAutoLogoffTime.setPreferredSize(new java.awt.Dimension(0, 30));

        jLabelTimedMessage.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabelTimedMessage.setText(bundle.getString("label.autologoffzero")); // NOI18N
        jLabelTimedMessage.setMaximumSize(new java.awt.Dimension(0, 25));
        jLabelTimedMessage.setMinimumSize(new java.awt.Dimension(0, 0));
        jLabelTimedMessage.setPreferredSize(new java.awt.Dimension(200, 30));

        jchkAutoLogoff.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jchkAutoLogoff.setText(bundle.getString("label.autologonoff")); // NOI18N
        jchkAutoLogoff.setMaximumSize(new java.awt.Dimension(0, 25));
        jchkAutoLogoff.setMinimumSize(new java.awt.Dimension(0, 0));
        jchkAutoLogoff.setPreferredSize(new java.awt.Dimension(200, 30));
        jchkAutoLogoff.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jchkAutoLogoffActionPerformed(evt);
            }
        });

        jchkAutoLogoffToTables.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jchkAutoLogoffToTables.setText(bundle.getString("label.autoloffrestaurant")); // NOI18N
        jchkAutoLogoffToTables.setMaximumSize(new java.awt.Dimension(0, 25));
        jchkAutoLogoffToTables.setMinimumSize(new java.awt.Dimension(0, 0));
        jchkAutoLogoffToTables.setPreferredSize(new java.awt.Dimension(0, 30));
        jchkAutoLogoffToTables.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jchkAutoLogoffToTablesActionPerformed(evt);
            }
        });

        jchkShowCustomerDetails.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jchkShowCustomerDetails.setSelected(true);
        jchkShowCustomerDetails.setText(bundle.getString("label.tableshowcustomerdetails")); // NOI18N
        jchkShowCustomerDetails.setMaximumSize(new java.awt.Dimension(0, 25));
        jchkShowCustomerDetails.setMinimumSize(new java.awt.Dimension(0, 0));
        jchkShowCustomerDetails.setPreferredSize(new java.awt.Dimension(350, 30));
        jchkShowCustomerDetails.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jchkShowCustomerDetailsActionPerformed(evt);
            }
        });

        jchkShowWaiterDetails.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jchkShowWaiterDetails.setSelected(true);
        jchkShowWaiterDetails.setText(bundle.getString("label.tableshowwaiterdetails")); // NOI18N
        jchkShowWaiterDetails.setMaximumSize(new java.awt.Dimension(0, 25));
        jchkShowWaiterDetails.setMinimumSize(new java.awt.Dimension(0, 0));
        jchkShowWaiterDetails.setPreferredSize(new java.awt.Dimension(350, 30));

        jLabelTableNameTextColour.setBackground(new java.awt.Color(255, 255, 255));
        jLabelTableNameTextColour.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabelTableNameTextColour.setText(bundle.getString("label.textclourtablename")); // NOI18N
        jLabelTableNameTextColour.setMaximumSize(new java.awt.Dimension(0, 25));
        jLabelTableNameTextColour.setMinimumSize(new java.awt.Dimension(0, 0));
        jLabelTableNameTextColour.setPreferredSize(new java.awt.Dimension(350, 30));

        jCheckPrice00.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jCheckPrice00.setText(bundle.getString("label.pricewith00")); // NOI18N
        jCheckPrice00.setToolTipText("");
        jCheckPrice00.setMaximumSize(new java.awt.Dimension(0, 25));
        jCheckPrice00.setMinimumSize(new java.awt.Dimension(0, 0));
        jCheckPrice00.setPreferredSize(new java.awt.Dimension(250, 25));
        jCheckPrice00.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckPrice00ActionPerformed(evt);
            }
        });

        jTaxIncluded.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jTaxIncluded.setText(bundle.getString("label.taxincluded")); // NOI18N
        jTaxIncluded.setMaximumSize(new java.awt.Dimension(0, 25));
        jTaxIncluded.setMinimumSize(new java.awt.Dimension(0, 0));
        jTaxIncluded.setPreferredSize(new java.awt.Dimension(250, 25));

        jCloseCashbtn.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jCloseCashbtn.setText(bundle.getString("message.systemclosecash")); // NOI18N
        jCloseCashbtn.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jCloseCashbtn.setMaximumSize(new java.awt.Dimension(0, 25));
        jCloseCashbtn.setMinimumSize(new java.awt.Dimension(0, 0));
        jCloseCashbtn.setPreferredSize(new java.awt.Dimension(250, 25));

        jMoveAMountBoxToTop.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jMoveAMountBoxToTop.setSelected(true);
        jMoveAMountBoxToTop.setText(bundle.getString("label.inputamount")); // NOI18N
        jMoveAMountBoxToTop.setMaximumSize(new java.awt.Dimension(0, 25));
        jMoveAMountBoxToTop.setMinimumSize(new java.awt.Dimension(0, 0));
        jMoveAMountBoxToTop.setPreferredSize(new java.awt.Dimension(250, 25));

        jchkTextOverlay.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jchkTextOverlay.setText(bundle.getString("label.currencybutton")); // NOI18N
        jchkTextOverlay.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jchkTextOverlay.setMaximumSize(new java.awt.Dimension(0, 25));
        jchkTextOverlay.setMinimumSize(new java.awt.Dimension(0, 0));
        jchkTextOverlay.setPreferredSize(new java.awt.Dimension(250, 25));

        jchkautoRefreshTableMap.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jchkautoRefreshTableMap.setSelected(true);
        jchkautoRefreshTableMap.setText(bundle.getString("label.autoRefreshTableMap")); // NOI18N
        jchkautoRefreshTableMap.setMaximumSize(new java.awt.Dimension(0, 25));
        jchkautoRefreshTableMap.setMinimumSize(new java.awt.Dimension(0, 0));
        jchkautoRefreshTableMap.setPreferredSize(new java.awt.Dimension(200, 30));
        jchkautoRefreshTableMap.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jchkautoRefreshTableMapActionPerformed(evt);
            }
        });

        jTxtautoRefreshTimer.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jTxtautoRefreshTimer.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        jTxtautoRefreshTimer.setText("0");
        jTxtautoRefreshTimer.setMaximumSize(new java.awt.Dimension(0, 25));
        jTxtautoRefreshTimer.setMinimumSize(new java.awt.Dimension(0, 0));
        jTxtautoRefreshTimer.setPreferredSize(new java.awt.Dimension(0, 30));

        jLblautoRefresh.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLblautoRefresh.setText(bundle.getString("label.autoRefreshTableMapTimer")); // NOI18N
        jLblautoRefresh.setMaximumSize(new java.awt.Dimension(0, 25));
        jLblautoRefresh.setMinimumSize(new java.awt.Dimension(0, 0));
        jLblautoRefresh.setPreferredSize(new java.awt.Dimension(200, 30));

        jchkSCOnOff.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jchkSCOnOff.setText(bundle.getString("label.SCOnOff")); // NOI18N
        jchkSCOnOff.setMaximumSize(new java.awt.Dimension(0, 25));
        jchkSCOnOff.setMinimumSize(new java.awt.Dimension(0, 0));
        jchkSCOnOff.setPreferredSize(new java.awt.Dimension(0, 25));
        jchkSCOnOff.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jchkSCOnOffActionPerformed(evt);
            }
        });

        jLabelSCRate.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabelSCRate.setText(bundle.getString("label.SCRate")); // NOI18N
        jLabelSCRate.setMaximumSize(new java.awt.Dimension(0, 25));
        jLabelSCRate.setMinimumSize(new java.awt.Dimension(0, 0));
        jLabelSCRate.setPreferredSize(new java.awt.Dimension(190, 30));

        jTextSCRate.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jTextSCRate.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        jTextSCRate.setText("0");
        jTextSCRate.setMaximumSize(new java.awt.Dimension(0, 25));
        jTextSCRate.setMinimumSize(new java.awt.Dimension(0, 0));
        jTextSCRate.setPreferredSize(new java.awt.Dimension(0, 30));
        jTextSCRate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextSCRateActionPerformed(evt);
            }
        });

        jLabelSCRatePerCent.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabelSCRatePerCent.setText(bundle.getString("label.SCZero")); // NOI18N
        jLabelSCRatePerCent.setMaximumSize(new java.awt.Dimension(0, 25));
        jLabelSCRatePerCent.setMinimumSize(new java.awt.Dimension(0, 0));
        jLabelSCRatePerCent.setPreferredSize(new java.awt.Dimension(0, 30));

        jchkSCRestaurant.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jchkSCRestaurant.setText(bundle.getString("label.SCRestaurant")); // NOI18N
        jchkSCRestaurant.setMaximumSize(new java.awt.Dimension(0, 25));
        jchkSCRestaurant.setMinimumSize(new java.awt.Dimension(0, 0));
        jchkSCRestaurant.setPreferredSize(new java.awt.Dimension(0, 25));

        jchkPriceUpdate.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jchkPriceUpdate.setText(bundle.getString("label.priceupdate")); // NOI18N
        jchkPriceUpdate.setToolTipText(bundle.getString("tooltip.priceupdate")); // NOI18N
        jchkPriceUpdate.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jchkPriceUpdate.setMaximumSize(new java.awt.Dimension(0, 25));
        jchkPriceUpdate.setMinimumSize(new java.awt.Dimension(0, 0));
        jchkPriceUpdate.setPreferredSize(new java.awt.Dimension(250, 25));

        jchkBarcodetype.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jchkBarcodetype.setText(bundle.getString("label.barcodetype")); // NOI18N
        jchkBarcodetype.setToolTipText(bundle.getString("tooltip.barcodetype")); // NOI18N
        jchkBarcodetype.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jchkBarcodetype.setMaximumSize(new java.awt.Dimension(0, 25));
        jchkBarcodetype.setMinimumSize(new java.awt.Dimension(0, 0));
        jchkBarcodetype.setPreferredSize(new java.awt.Dimension(250, 25));

        jchkTransBtn.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jchkTransBtn.setText(bundle.getString("label.tabletransbutton")); // NOI18N
        jchkTransBtn.setMaximumSize(new java.awt.Dimension(0, 25));
        jchkTransBtn.setMinimumSize(new java.awt.Dimension(0, 0));
        jchkTransBtn.setPreferredSize(new java.awt.Dimension(350, 30));
        jchkTransBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jchkTransBtnActionPerformed(evt);
            }
        });

        WaiterColour.setToolTipText(bundle.getString("tooltip.prodhtmldisplayColourChooser")); // NOI18N
        WaiterColour.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        WaiterColour.setMaximumSize(new java.awt.Dimension(71, 30));
        WaiterColour.setMinimumSize(new java.awt.Dimension(71, 30));
        WaiterColour.setPreferredSize(new java.awt.Dimension(71, 30));

        TableNameColour.setToolTipText(bundle.getString("tooltip.prodhtmldisplayColourChooser")); // NOI18N
        TableNameColour.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        TableNameColour.setMaximumSize(new java.awt.Dimension(71, 30));
        TableNameColour.setMinimumSize(new java.awt.Dimension(71, 30));
        TableNameColour.setPreferredSize(new java.awt.Dimension(71, 30));

        CustomerColour.setToolTipText(bundle.getString("tooltip.prodhtmldisplayColourChooser")); // NOI18N
        CustomerColour.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        CustomerColour.setMaximumSize(new java.awt.Dimension(71, 30));
        CustomerColour.setMinimumSize(new java.awt.Dimension(71, 30));
        CustomerColour.setPreferredSize(new java.awt.Dimension(71, 30));

        jchkOverride.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jchkOverride.setText(bundle.getString("label.override")); // NOI18N
        jchkOverride.setMaximumSize(new java.awt.Dimension(0, 25));
        jchkOverride.setMinimumSize(new java.awt.Dimension(0, 0));
        jchkOverride.setPreferredSize(new java.awt.Dimension(200, 30));

        jtxtPIN.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jtxtPIN.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        jtxtPIN.setToolTipText("");
        jtxtPIN.setMaximumSize(new java.awt.Dimension(0, 25));
        jtxtPIN.setMinimumSize(new java.awt.Dimension(0, 0));
        jtxtPIN.setPreferredSize(new java.awt.Dimension(60, 30));

        jLabel5.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel5.setText("PIN");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jchkInstance, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jTaxIncluded, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jchkTextOverlay, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jchkOverride, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel5)
                                        .addGap(20, 20, 20)))
                                .addComponent(jtxtPIN, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jchkPriceUpdate, javax.swing.GroupLayout.PREFERRED_SIZE, 349, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jCheckPrice00, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jMoveAMountBoxToTop, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jCloseCashbtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jchkBarcodetype, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(32, 32, 32)
                                .addComponent(jTextSCRate, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabelSCRatePerCent, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabelSCRate, javax.swing.GroupLayout.PREFERRED_SIZE, 229, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(32, 32, 32)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jchkAutoLogoffToTables, javax.swing.GroupLayout.PREFERRED_SIZE, 306, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jTextAutoLogoffTime, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabelTimedMessage, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                            .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(6, 6, 6)
                                        .addComponent(jchkSCOnOff, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(jchkAutoLogoff, javax.swing.GroupLayout.PREFERRED_SIZE, 327, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 331, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jchkTransBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jchkShowWaiterDetails, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jchkShowCustomerDetails, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabelTableNameTextColour, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(WaiterColour, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
                                            .addComponent(TableNameColour, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 6, Short.MAX_VALUE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(CustomerColour, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 0, Short.MAX_VALUE))))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(21, 21, 21)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jchkSCRestaurant, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jTxtautoRefreshTimer, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLblautoRefresh, javax.swing.GroupLayout.PREFERRED_SIZE, 244, javax.swing.GroupLayout.PREFERRED_SIZE))))
                            .addComponent(jchkautoRefreshTableMap, javax.swing.GroupLayout.PREFERRED_SIZE, 327, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTaxIncluded, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jCheckPrice00, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jchkInstance, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jMoveAMountBoxToTop, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jchkTextOverlay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jCloseCashbtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jchkPriceUpdate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jchkBarcodetype, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jtxtPIN, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jchkOverride, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jchkAutoLogoff, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jchkAutoLogoffToTables, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jTextAutoLogoffTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabelTimedMessage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jchkSCOnOff, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jTextSCRate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabelSCRate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabelSCRatePerCent, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(CustomerColour, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jchkShowCustomerDetails, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jchkShowWaiterDetails, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(WaiterColour, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(TableNameColour, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabelTableNameTextColour, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jchkTransBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jchkautoRefreshTableMap, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addComponent(jchkSCRestaurant, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLblautoRefresh, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTxtautoRefreshTimer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jchkAutoLogoffActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jchkAutoLogoffActionPerformed
        if (jchkAutoLogoff.isSelected()){
                jchkAutoLogoffToTables.setVisible(true);
                jLabelTimedMessage.setVisible(true);
                jTextAutoLogoffTime.setVisible(true);
        }else{    
                jchkAutoLogoffToTables.setVisible(false);
                jLabelTimedMessage.setVisible(false);
                jTextAutoLogoffTime.setVisible(false);
        }
    }//GEN-LAST:event_jchkAutoLogoffActionPerformed

    private void jCheckPrice00ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckPrice00ActionPerformed

    }//GEN-LAST:event_jCheckPrice00ActionPerformed

    private void jchkAutoLogoffToTablesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jchkAutoLogoffToTablesActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jchkAutoLogoffToTablesActionPerformed

    private void jchkShowCustomerDetailsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jchkShowCustomerDetailsActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jchkShowCustomerDetailsActionPerformed

    private void jchkautoRefreshTableMapActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jchkautoRefreshTableMapActionPerformed
        if (jchkautoRefreshTableMap.isSelected()){
            jLblautoRefresh.setVisible(true);
            jTxtautoRefreshTimer.setVisible(true);
        }else{    
            jLblautoRefresh.setVisible(false);
            jTxtautoRefreshTimer.setVisible(false);
        }  
    }//GEN-LAST:event_jchkautoRefreshTableMapActionPerformed

    private void jchkSCOnOffActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jchkSCOnOffActionPerformed
        if (jchkSCOnOff.isSelected()){
            jchkSCRestaurant.setVisible(true);
            jLabelSCRate.setVisible(true);
            jTextSCRate.setVisible(true);
            jLabelSCRatePerCent.setVisible(true);
        }else{
            jchkSCRestaurant.setVisible(false);
            jLabelSCRate.setVisible(false);
            jTextSCRate.setVisible(false);
            jLabelSCRatePerCent.setVisible(false);
        }
    }//GEN-LAST:event_jchkSCOnOffActionPerformed

    private void jTextSCRateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextSCRateActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextSCRateActionPerformed

    private void jchkTransBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jchkTransBtnActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jchkTransBtnActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.openbravo.data.gui.JColorPicker CustomerColour;
    private com.openbravo.data.gui.JColorPicker TableNameColour;
    private com.openbravo.data.gui.JColorPicker WaiterColour;
    private javax.swing.JCheckBox jCheckPrice00;
    private javax.swing.JCheckBox jCloseCashbtn;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabelSCRate;
    private javax.swing.JLabel jLabelSCRatePerCent;
    private javax.swing.JLabel jLabelTableNameTextColour;
    private javax.swing.JLabel jLabelTimedMessage;
    private javax.swing.JLabel jLblautoRefresh;
    private javax.swing.JCheckBox jMoveAMountBoxToTop;
    private javax.swing.JCheckBox jTaxIncluded;
    private javax.swing.JTextField jTextAutoLogoffTime;
    private javax.swing.JTextField jTextSCRate;
    private javax.swing.JTextField jTxtautoRefreshTimer;
    private javax.swing.JCheckBox jchkAutoLogoff;
    private javax.swing.JCheckBox jchkAutoLogoffToTables;
    private javax.swing.JCheckBox jchkBarcodetype;
    private javax.swing.JCheckBox jchkInstance;
    private javax.swing.JCheckBox jchkOverride;
    private javax.swing.JCheckBox jchkPriceUpdate;
    private javax.swing.JCheckBox jchkSCOnOff;
    private javax.swing.JCheckBox jchkSCRestaurant;
    private javax.swing.JCheckBox jchkShowCustomerDetails;
    private javax.swing.JCheckBox jchkShowWaiterDetails;
    private javax.swing.JCheckBox jchkTextOverlay;
    private javax.swing.JCheckBox jchkTransBtn;
    private javax.swing.JCheckBox jchkautoRefreshTableMap;
    private javax.swing.JTextField jtxtPIN;
    // End of variables declaration//GEN-END:variables
    
}