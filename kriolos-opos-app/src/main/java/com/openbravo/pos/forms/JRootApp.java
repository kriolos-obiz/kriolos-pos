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
//    along with this program.  If not, see <http://www.gnu.org/licenses/>
package com.openbravo.pos.forms;

import com.openbravo.basic.BasicException;
import com.openbravo.data.gui.MessageInf;
import com.openbravo.data.loader.Session;
import com.openbravo.format.Formats;
import com.openbravo.pos.cash.CashManagementService;
import com.openbravo.pos.cash.CashManagementServiceImpl;
import com.openbravo.pos.cash.CashRegister;
import com.openbravo.pos.printer.DeviceTicket;
import com.openbravo.pos.printer.TicketParser;
import com.openbravo.pos.printer.TicketPrinterException;
import com.openbravo.pos.scale.DeviceScale;
import com.openbravo.pos.scanpal2.DeviceScanner;
import com.openbravo.pos.scanpal2.DeviceScannerFactory;
import com.openbravo.pos.scripting.ScriptEngine;
import com.openbravo.pos.scripting.ScriptException;
import com.openbravo.pos.scripting.ScriptFactory;
import java.awt.CardLayout;
import java.awt.ComponentOrientation;
import java.awt.Cursor;
import java.io.*;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.Instant;

/**
 *
 * @author adrianromero
 */
public class JRootApp extends JPanel implements AppView {

    private static final Logger LOGGER = Logger.getLogger(JRootApp.class.getName());
    private static final long serialVersionUID = 1L;

    private final AppProperties appProperties;
    private Session session;
    private DataLogicSystem dlogicSystem;
    private CashManagementService cashManagementService;

    private Properties hostSavedProperties = null;
    private CashRegister activeCash = new CashRegister();
    private String inventoryLocation;

    private DeviceScale deviceScale;
    private DeviceScanner deviceScanner;
    private DeviceTicket deviceTicket;
    private TicketParser ticketParser;

    private JPrincipalApp principalApp = null;
    private JAuthPanel mAuthPanel = null;

    private static final String HOST_PROP_KEY_ACTIVECASH = "activecash";
    private static final String HOST_PROP_KEY_LOCATION = "location";

    private String getLineTimer() {
        return Formats.HOURMIN.formatValue(new Date());
    }

    private String getLineDate() {
        return Formats.DATE.formatValue(new Date());
    }

    public JRootApp(AppProperties props) {
        initComponents();

        appProperties = props;
    }

    public void initApp() throws BasicException {

        applyComponentOrientation(ComponentOrientation.getOrientation(Locale.getDefault()));

        showLoginPanel();
    }

    private void setTitlePanel() {

        String customTile = dlogicSystem.getResourceAsText("Window.Title");

        appTitleLabel.setText(customTile);
        appTitleLabel.repaint();

        /*Timer show Date Hour:min:seg
        javax.swing.Timer clockTimer = new javax.swing.Timer(1000, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent evt) {
                String m_clock = getLineTimer();
                String m_date = getLineDate();
                jLabel2.setText("  " + m_date + " " + m_clock);
            }
        });

        clockTimer.start();*/
    }

    private String getHostPropertyId() {
        return appProperties.getHost() + "/properties";
    }

    private void setInventoryLocation() {
        inventoryLocation = hostSavedProperties.getProperty(HOST_PROP_KEY_LOCATION);
        if (inventoryLocation == null) {
            inventoryLocation = "0";
            hostSavedProperties.setProperty(HOST_PROP_KEY_LOCATION, inventoryLocation);
            dlogicSystem.setResourceAsProperties(getHostPropertyId(), hostSavedProperties);
        }
    }

    private void initPeripheral() {
        deviceTicket = new DeviceTicket(this, appProperties);

        ticketParser = new TicketParser(getDeviceTicket(), dlogicSystem);
        printerStart();

        deviceScale = new DeviceScale(this, appProperties);

        deviceScanner = DeviceScannerFactory.createInstance(appProperties);
    }

    private boolean checkActiveCash() {
        try {
            String activeCash = hostSavedProperties.getProperty(HOST_PROP_KEY_ACTIVECASH);
            CashRegister cash = null;

            if (activeCash != null) {
                cash = cashManagementService.getCloseCashByMoney(activeCash);
            }

            if (cash != null && appProperties.getHost().equals(cash.getHost()) && cash.getEndDate() == null) {
                setActiveCash(activeCash, cash.getHostsequence(), cash.getStartDate(), cash.getEndDate());

            } else {

                int currentSequence = cashManagementService.getCloseCashSequenceByHost(appProperties.getHost());

                setActiveCash(UUID.randomUUID().toString(), currentSequence + 1, new Date(), null);

                cash = new CashRegister();
                cash.setMoney(getActiveCashIndex());
                cash.setHost(appProperties.getHost());
                cash.setHostsequence(getActiveCashSequence());
                cash.setStartDate(getActiveCashDateStart());
                cash.setEndDate(getActiveCashDateEnd());

                cashManagementService.addCloseCash(cash);
            }
        }
        catch (BasicException e) {
            MessageInf msg = new MessageInf(MessageInf.SGN_NOTICE,
                    AppLocal.getIntString("message.cannotclosecash"), e);
            msg.show(this);
            return true;
        }
        return false;
    }

    private void logStartup() {
        // create the filename
        String sUserPath = AppConfig.getInstance().getAppDataDirectory();

        Instant machineTimestamp = Instant.now();
        String sContent = ""
                + "timestamp: "+ machineTimestamp 
                + ", appId: " + AppLocal.APP_ID 
                + ", appName: " + AppLocal.APP_NAME 
                + ", appVersion: " + AppLocal.APP_VERSION 
                + ", appDir: "+ sUserPath 
                + ", host: "+ appProperties.getHost() 
                + ", hostCash: "+ getActiveCashIndex()
                + ", hostCashSeq: "+ getActiveCashSequence()
                + ", hostCashStart: "+ getActiveCashDateStart()
                + "\n";

        try {
            Files.write(new File(sUserPath, AppLocal.getLogFileName()).toPath(), sContent.getBytes(),
                    StandardOpenOption.APPEND, StandardOpenOption.CREATE);
        }
        catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    private String readDataBaseVersion() {
        try {
            return dlogicSystem.findVersion();
        }
        catch (BasicException ed) {
            return null;
        }
    }

    @Override
    public DeviceTicket getDeviceTicket() {
        return deviceTicket;
    }

    @Override
    public DeviceScale getDeviceScale() {
        return deviceScale;
    }

    @Override
    public DeviceScanner getDeviceScanner() {
        return deviceScanner;
    }

    @Override
    public Session getSession() {
        return session;
    }

    @Override
    public String getInventoryLocation() {
        return inventoryLocation;
    }

    @Override
    public String getActiveCashIndex() {
        return activeCash.getMoney();
    }

    @Override
    public int getActiveCashSequence() {
        return activeCash.getHostsequence();
    }

    @Override
    public Date getActiveCashDateStart() {
        return activeCash.getStartDate();
    }

    @Override
    public Date getActiveCashDateEnd() {
        return activeCash.getEndDate();
    }

    @Override
    public void setActiveCash(String sIndex, int sequenceNumber, Date startDate, Date endDate) {
        activeCash.setMoney(sIndex);
        activeCash.setHostsequence(sequenceNumber);
        activeCash.setStartDate(startDate);
        activeCash.setEndDate(endDate);

        hostSavedProperties.setProperty(HOST_PROP_KEY_ACTIVECASH, activeCash.getMoney());
        dlogicSystem.setResourceAsProperties(getHostPropertyId(), hostSavedProperties);
    }

    @Override
    public AppProperties getProperties() {
        return appProperties;
    }

    @Override
    public Object getBean(String beanfactory) throws BeanFactoryException {
        return BeanContainer.geBean(beanfactory, this);
    }

    @Override
    public void waitCursorBegin() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    }

    @Override
    public void waitCursorEnd() {
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    @Override
    public AppUserView getAppUserView() {
        return principalApp;
    }

    @Override
    public boolean hasPermission(String permission) {
        return Optional.ofNullable(this.getAppUserView())
                .map(i -> i.getUser())
                .map(u -> u.hasPermission(permission))
                .orElse(false);
    }

    private void printerStart() {

        String sresource = dlogicSystem.getResourceAsXML("Printer.Start");

        deviceTicket.getDeviceDisplay().writeVisor(AppLocal.APP_NAME, AppLocal.APP_VERSION);

        if (sresource != null) {

            try {
                ScriptEngine script = ScriptFactory.getScriptEngine(ScriptFactory.VELOCITY);
                script.put("appname", AppLocal.APP_NAME);
                script.put("appShortDescription", AppLocal.APP_SHORT_DESCRIPTION);
                String xmlContent = script.eval(sresource).toString();
                ticketParser.printTicket(xmlContent);

            }
            catch (TicketPrinterException | ScriptException ex) {
                LOGGER.log(Level.WARNING, "Print start: ", ex);
            }
        }
    }

    private void showView(String view) {
        CardLayout cl = (CardLayout) (contentContainerPanel.getLayout());
        cl.show(contentContainerPanel, view);
    }

    private void openAppView(AppUser user) {

        LOGGER.log(Level.WARNING, "INFO :: showMainAppPanel");
        if (closeAppView()) {

            principalApp = new JPrincipalApp(this, user);

            statusBarSecondPanel.add(principalApp.getNotificator());
            statusBarSecondPanel.revalidate();

            String viewID = "_" + principalApp.getUser().getId();
            contentContainerPanel.add(principalApp, viewID);
            showView(viewID);

            principalApp.activate();
        }
    }

    //Release hardware,files,...
    private void releaseResources() {
        if (deviceTicket != null) {
            deviceTicket.getDeviceDisplay().clearVisor();
        }
    }

    public void tryToClose() {

        if (closeAppView()) {
            releaseResources();
            if (session != null) {
                try {
                    session.close();
                }
                catch (SQLException ex) {
                    LOGGER.log(Level.WARNING, "", ex);
                }
            }
            java.awt.Window parent = SwingUtilities.getWindowAncestor(this);
            if (parent != null) {
                parent.dispose();
            } else {
                this.setVisible(false);
                this.setEnabled(false);
            }
        }
    }

    @Override
    public boolean closeAppView() {

        if (principalApp == null) {
            return true;
        } else if (!principalApp.deactivate()) {
            return false;
        } else {
            statusBarSecondPanel.remove(principalApp.getNotificator());
            statusBarSecondPanel.revalidate();
            statusBarSecondPanel.repaint();

            contentContainerPanel.remove(principalApp);
            principalApp = null;

            //showLoginPanel();
            return true;
        }
    }

    private void showLoginPanel() {
        LOGGER.log(Level.WARNING, "INFO :: showLoginPanel");
        if (mAuthPanel == null) {
            mAuthPanel = new JAuthPanel(this, dlogicSystem, appProperties, new JAuthPanel.AuthListener() {
                @Override
                public void onSucess(AppUser user) {
                    openAppView(user);
                }
            });
            contentContainerPanel.add(mAuthPanel, "login");
            if (mAuthPanel.getDatabaseSelector() != null) {
                mAuthPanel.getDatabaseSelector().autoSelectIfSingle();
            }
        }
        showView("login");
    }

    public void switchDatabase() throws BasicException {
        String dbKey = (mAuthPanel != null && mAuthPanel.getDatabaseSelector() != null)
                ? mAuthPanel.getDatabaseSelector().getSelectedDbKey()
                : null;

        try {
            waitCursorBegin();
            LOGGER.log(Level.INFO, "Switching database: {0}", dbKey);

            if (session != null) {
                try {
                    session.close();
                } catch (SQLException ex) {
                    LOGGER.log(Level.WARNING, "Error closing previous session: ", ex);
                }
            }

            session = AppViewConnection.createSession(appProperties, dbKey);

            LOGGER.log(Level.INFO, "DB Migration execution Starting");
            try {
                com.openbravo.pos.data.DBMigrator.execDBMigration(session);
                LOGGER.log(Level.INFO, "Database verification or migration done successfully");
            } catch (BasicException ex) {
                throw new BasicException("Database verification fail", ex);
            }

            dlogicSystem = (DataLogicSystem) getBean("com.openbravo.pos.forms.DataLogicSystem");
            dlogicSystem.init(session);

            cashManagementService = new CashManagementServiceImpl(session);

            hostSavedProperties = dlogicSystem.getResourceAsProperties(getHostPropertyId());

            if (checkActiveCash()) {
                LOGGER.log(Level.WARNING, "Fail on verify ActiveCash");
                throw new BasicException("Fail on verify ActiveCash");
            }

            setInventoryLocation();
            setTitlePanel();
            setStatusBarPanel();

            if (deviceTicket == null) {
                initPeripheral();
                logStartup();
            } else {
                ticketParser = new TicketParser(getDeviceTicket(), dlogicSystem);
            }

            if (mAuthPanel != null) {
                mAuthPanel.updateDataLogicSystem(dlogicSystem);
            }

            LOGGER.log(Level.INFO, "Successfully completed switch to database");

        } finally {
            waitCursorEnd();
        }
    }

    private void setStatusBarPanel() {
        String sWareHouse;

        try {
            sWareHouse = dlogicSystem.findLocationName(inventoryLocation);
        }
        catch (BasicException e) {
            sWareHouse = "";
        }

        String url;
        try {
            url = session.getURL();
        }
        catch (SQLException e) {
            url = "";
        }
        appInfoLabel.setText("<html>" + appProperties.getHost() + " ;<b>WareHouse<b>: " + sWareHouse + "<br>" + url + "</html>");
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the FormEditor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        topPanel = new javax.swing.JPanel();
        appTitleLabel = new javax.swing.JLabel();
        appPowerByLabel = new javax.swing.JLabel();
        appTopLabel = new javax.swing.JLabel();
        contentContainerPanel = new javax.swing.JPanel();
        statusBarPanel = new javax.swing.JPanel();
        statusBarFirstPanel = new javax.swing.JPanel();
        appInfoLabel = new javax.swing.JLabel();
        statusBarSecondPanel = new javax.swing.JPanel();
        appExitButton = new javax.swing.JButton();

        setEnabled(false);
        setPreferredSize(new java.awt.Dimension(1024, 768));
        setLayout(new java.awt.BorderLayout());

        topPanel.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, javax.swing.UIManager.getDefaults().getColor("Button.darkShadow")));
        topPanel.setPreferredSize(new java.awt.Dimension(449, 40));
        topPanel.setLayout(new java.awt.BorderLayout());

        appTitleLabel.setFont(new java.awt.Font("Arial", 1, 16)); // NOI18N
        appTitleLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        appTitleLabel.setText("Point of Sales (POS)");
        topPanel.add(appTitleLabel, java.awt.BorderLayout.CENTER);

        appPowerByLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        appPowerByLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 5, 0, 5));
        appPowerByLabel.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        appPowerByLabel.setMaximumSize(new java.awt.Dimension(180, 34));
        appPowerByLabel.setPreferredSize(new java.awt.Dimension(180, 34));
        topPanel.add(appPowerByLabel, java.awt.BorderLayout.LINE_END);

        appTopLabel.setFont(new java.awt.Font("Arial", 1, 16)); // NOI18N
        appTopLabel.setForeground(new java.awt.Color(102, 102, 102));
        appTopLabel.setPreferredSize(new java.awt.Dimension(180, 34));
        topPanel.add(appTopLabel, java.awt.BorderLayout.LINE_START);

        add(topPanel, java.awt.BorderLayout.NORTH);

        contentContainerPanel.setLayout(new java.awt.CardLayout());
        add(contentContainerPanel, java.awt.BorderLayout.CENTER);

        statusBarPanel.setBorder(javax.swing.BorderFactory.createMatteBorder(1, 0, 0, 0, javax.swing.UIManager.getDefaults().getColor("Button.darkShadow")));
        statusBarPanel.setLayout(new javax.swing.BoxLayout(statusBarPanel, javax.swing.BoxLayout.LINE_AXIS));

        statusBarFirstPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        appInfoLabel.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        appInfoLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        appInfoLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/display.png"))); // NOI18N
        appInfoLabel.setText("*Hostname");
        appInfoLabel.setMaximumSize(new java.awt.Dimension(32767, 32767));
        appInfoLabel.setMinimumSize(new java.awt.Dimension(200, 40));
        appInfoLabel.setPreferredSize(new java.awt.Dimension(400, 40));
        statusBarFirstPanel.add(appInfoLabel);

        statusBarPanel.add(statusBarFirstPanel);

        statusBarSecondPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        appExitButton.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        appExitButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/exit.png"))); // NOI18N
        appExitButton.setText(AppLocal.getIntString("button.exit")); // NOI18N
        appExitButton.setFocusPainted(false);
        appExitButton.setFocusable(false);
        appExitButton.setPreferredSize(new java.awt.Dimension(100, 50));
        appExitButton.setRequestFocusEnabled(false);
        appExitButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                appExitButtonActionPerformed(evt);
            }
        });
        statusBarSecondPanel.add(appExitButton);

        statusBarPanel.add(statusBarSecondPanel);

        add(statusBarPanel, java.awt.BorderLayout.SOUTH);
    }// </editor-fold>//GEN-END:initComponents

    private void appExitButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_appExitButtonActionPerformed
        tryToClose();
    }//GEN-LAST:event_appExitButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton appExitButton;
    private javax.swing.JLabel appInfoLabel;
    private javax.swing.JLabel appPowerByLabel;
    private javax.swing.JLabel appTitleLabel;
    private javax.swing.JLabel appTopLabel;
    private javax.swing.JPanel contentContainerPanel;
    private javax.swing.JPanel statusBarFirstPanel;
    private javax.swing.JPanel statusBarPanel;
    private javax.swing.JPanel statusBarSecondPanel;
    private javax.swing.JPanel topPanel;
    // End of variables declaration//GEN-END:variables
}
