/*
 * Copyright (C) 2022-2026 KriolOS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.openbravo.pos.forms.components;

import com.openbravo.basic.BasicException;
import com.openbravo.beans.JFlowPanel;
import com.openbravo.beans.JPasswordPanel;
import com.openbravo.data.gui.MessageInf;
import com.openbravo.pos.config.JFrmConfig;
import com.openbravo.pos.forms.AppConfig;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.AppProperties;
import com.openbravo.pos.forms.AppProperties.DatabaseConfig;
import com.openbravo.pos.forms.AppUser;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.ApplicationShell;
import com.openbravo.pos.forms.DataLogicSystem;
import com.openbravo.pos.forms.DatabaseActivationCallback;
import com.openbravo.pos.ui.components.ButtonSize;
import com.openbravo.pos.ui.components.POSButtonFactory;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/**
 * Integrated Authentication and Database Selection view.
 *
 * <p>
 * Consolidates tenant/database switching directly into the authentication
 * experience, providing non-blocking asynchronous database activation, a
 * persistent status badge for the currently active database, real-time
 * migration progress feedback, and user credential entry.</p>
 *
 * @author poolborges
 */
public class AuthenticationPanel extends javax.swing.JPanel {

    private static final Logger LOGGER = Logger.getLogger(AuthenticationPanel.class.getName());
    private static final long serialVersionUID = 1L;

    public interface AuthListener {

        void onSucess(AppUser user);
    }

    private final AppView appView;
    private final AppProperties appProperties;
    private final AuthListener authListener;
    private DataLogicSystem m_dlSystem;

    private DatabaseConfig activeDbConfig = null;
    private StringBuilder inputtext;

    // Integrated Database Selector UI components (with Rule 1 compliant URNs and test anchors)
    private JComboBox<DatabaseConfig> comboDatabases;
    private JButton btnSelectDatabase;
    private JButton btnConfigureDatabase;
    private JLabel lblActiveDatabaseBadge;
    private JLabel lblStatus;
    private JProgressBar progressBar;
    private JPanel dbControlsPanel;

    /**
     * Constructs a fully-wired AuthenticationPanel.
     *
     * @param app Outer application view / shell.
     * @param dlSystem DataLogicSystem for querying users (may be null if no
     * database is active yet).
     * @param props System configuration properties.
     * @param authcListener Listener receiving successful authentication events.
     */
    public AuthenticationPanel(AppView app, DataLogicSystem dlSystem, AppProperties props, AuthListener authcListener) {
        this.appView = app;
        this.authListener = authcListener;
        this.appProperties = props;
        this.m_dlSystem = dlSystem;

        initComponents();
        initIntegratedDbSelector();
        initPanel();
        
    }

    public AuthenticationPanel(DataLogicSystem dlSystem, AppProperties props, AuthListener authcListener) {
        this(null, dlSystem, props, authcListener);
    }

    public AuthenticationPanel(DataLogicSystem dlSystem, AuthListener authcListener) {
        this(null, dlSystem, AppConfig.getInstance(), authcListener);
    }

    /**
     * Initializes the integrated Database Selector UI panel inside the
     * leftHeaderPanel container.
     */
    private void initIntegratedDbSelector() {
        leftPanel.remove(leftHeaderPanel);

        JPanel headerContainer = new JPanel();
        headerContainer.setLayout(new BoxLayout(headerContainer, BoxLayout.Y_AXIS));
        headerContainer.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        // 1. Title
        headerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        headerContainer.add(headerLabel);
        headerContainer.add(Box.createVerticalStrut(4));

        // 2. Active Database Status Badge
        lblActiveDatabaseBadge = new JLabel();
        lblActiveDatabaseBadge.setName("kriolos:auth:active-database-badge");
        lblActiveDatabaseBadge.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblActiveDatabaseBadge.setFont(lblActiveDatabaseBadge.getFont().deriveFont(Font.BOLD, 12f));
        lblActiveDatabaseBadge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        headerContainer.add(lblActiveDatabaseBadge);
        headerContainer.add(Box.createVerticalStrut(6));

        // 3. Database Controls Panel
        dbControlsPanel = new JPanel();
        dbControlsPanel.setName("kriolos:auth:db-controls-panel");
        dbControlsPanel.setLayout(new BoxLayout(dbControlsPanel, BoxLayout.Y_AXIS));
        dbControlsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createEtchedBorder(),
                        AppLocal.getIntString("label.database") != null ? AppLocal.getIntString("label.database") : "Base de Dados"),
                BorderFactory.createEmptyBorder(4, 6, 6, 6)
        ));

        comboDatabases = new JComboBox<>();
        comboDatabases.setName("kriolos:auth:combo-databases"); // Rule 7.2 URN & robot test anchor
        comboDatabases.setAlignmentX(Component.CENTER_ALIGNMENT);
        comboDatabases.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        comboDatabases.setPreferredSize(new Dimension(270, 30));
        comboDatabases.setRenderer(new DatabaseConfigRenderer());
        comboDatabases.addActionListener(e -> onDatabaseSelectionChanged());

        btnSelectDatabase = new JButton(AppLocal.getIntString("button.activate"));
        btnSelectDatabase.setName("kriolos:auth:btn-select-database"); // Rule 7.2 URN & robot test anchor
        btnSelectDatabase.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnSelectDatabase.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        btnSelectDatabase.setPreferredSize(new Dimension(270, 48));
        btnSelectDatabase.setIcon(new ImageIcon(getClass().getResource("/com/openbravo/images/database.png")));
        btnSelectDatabase.addActionListener(e -> activateSelectedDatabase());

        btnConfigureDatabase = new JButton("Configurar");
        btnConfigureDatabase.setName("kriolos:auth:btn-configure-database"); // Rule 7.2 URN & robot test anchor
        btnConfigureDatabase.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnConfigureDatabase.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        btnConfigureDatabase.setPreferredSize(new Dimension(270, 48));
        btnConfigureDatabase.setIcon(new ImageIcon(getClass().getResource("/com/openbravo/images/configuration.png")));
        btnConfigureDatabase.addActionListener(e -> openConfiguration());

        // 4. Progress Bar & Real-time Status Label
        progressBar = new JProgressBar();
        progressBar.setName("kriolos:auth:progress-bar");
        progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);
        progressBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 8));
        progressBar.setPreferredSize(new Dimension(270, 8));
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);

        lblStatus = new JLabel(" ");
        lblStatus.setName("kriolos:auth:status-label");
        lblStatus.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblStatus.setFont(lblStatus.getFont().deriveFont(Font.PLAIN, 11f));
        lblStatus.setForeground(new Color(100, 100, 100));

        dbControlsPanel.add(comboDatabases);
        dbControlsPanel.add(Box.createVerticalStrut(4));
        dbControlsPanel.add(btnSelectDatabase);
        dbControlsPanel.add(Box.createVerticalStrut(4));
        dbControlsPanel.add(btnConfigureDatabase);
        dbControlsPanel.add(Box.createVerticalStrut(6));
        dbControlsPanel.add(progressBar);
        dbControlsPanel.add(Box.createVerticalStrut(2));
        dbControlsPanel.add(lblStatus);

        headerContainer.add(dbControlsPanel);
        leftPanel.add(headerContainer, BorderLayout.NORTH);

        loadDatabases();
    }

    /**
     * Populates database options from configuration properties.
     */
    private void loadDatabases() {
        if (appProperties == null) {
            showNoDatabasesState();
            return;
        }

        List<DatabaseConfig> dbs = appProperties.getAll();
        if (dbs == null || dbs.isEmpty()) {
            showNoDatabasesState();
            return;
        }

        DefaultComboBoxModel<DatabaseConfig> model = new DefaultComboBoxModel<>();
        for (DatabaseConfig db : dbs) {
            model.addElement(db);
        }
        comboDatabases.setModel(model);

        String defaultDb = appProperties.getProperty("db.default");
        DatabaseConfig toSelect = null;
        if (defaultDb != null && !defaultDb.isBlank()) {
            for (DatabaseConfig db : dbs) {
                if (defaultDb.equalsIgnoreCase(db.name())) {
                    toSelect = db;
                    break;
                }
            }
        }
        if (toSelect == null && !dbs.isEmpty()) {
            toSelect = dbs.get(0);
        }
        if (toSelect != null) {
            comboDatabases.setSelectedItem(toSelect);
        }

        comboDatabases.setVisible(true);
        btnSelectDatabase.setVisible(true);
        btnConfigureDatabase.setVisible(false);

        updateBadgeAndButtons();
    }

    private void showNoDatabasesState() {
        comboDatabases.setVisible(false);
        btnSelectDatabase.setVisible(false);
        btnConfigureDatabase.setVisible(true);
        lblActiveDatabaseBadge.setText("⚠️ Nenhuma Base de Dados Configurada");
        lblActiveDatabaseBadge.setForeground(new Color(180, 50, 50));
        lblStatus.setText("Clique em Configurar para adicionar.");
    }

    /**
     * Invoked when user selects a different database item in the combo box.
     */
    private void onDatabaseSelectionChanged() {
        updateBadgeAndButtons();
    }

    /**
     * Updates the active badge, status text, and activate button label based on
     * selection.
     */
    private void updateBadgeAndButtons() {
        DatabaseConfig selected = getSelectedDatabase();
        if (selected == null) {
            lblActiveDatabaseBadge.setText("⚠️ Nenhuma Base de Dados Selecionada");
            lblActiveDatabaseBadge.setForeground(new Color(150, 150, 150));
            btnSelectDatabase.setEnabled(false);
            return;
        }

        if (activeDbConfig != null && activeDbConfig.equals(selected)) {
            lblActiveDatabaseBadge.setText("🟢 Base de Dados Ativa: " + activeDbConfig.name());
            lblActiveDatabaseBadge.setForeground(new Color(25, 135, 48));
            lblActiveDatabaseBadge.setToolTipText(activeDbConfig.url());
            btnSelectDatabase.setText("Reconectar");
            btnSelectDatabase.setEnabled(true);
            lblStatus.setText("● Base de dados selecionada com sucesso");
        } else {
            if (activeDbConfig != null) {
                lblActiveDatabaseBadge.setText("🟡 Ativa: " + activeDbConfig.name());
                lblActiveDatabaseBadge.setForeground(new Color(180, 120, 20));
            } else {
                lblActiveDatabaseBadge.setText("⚪ Nenhuma Base de Dados Conectada");
                lblActiveDatabaseBadge.setForeground(new Color(100, 100, 100));
            }
            btnSelectDatabase.setText("Ativar " + selected.name());
            btnSelectDatabase.setEnabled(true);
            lblStatus.setText("⚠️ " + selected.name() + " selecionada (Não aplicada)");
        }
    }

    /**
     * Initiates non-blocking asynchronous activation of the selected database.
     */
    public void activateSelectedDatabase() {
        final DatabaseConfig selected = getSelectedDatabase();
        if (selected == null) {
            return;
        }

        setBusy(true, "A verificar ligação...");
        showLoadingUsersList("A carregar utilizadores de " + selected.name() + "...");

        if (appView instanceof ApplicationShell shell) {
            shell.activateDatabase(selected, new DatabaseActivationCallback() {
                @Override
                public void onProgress(String statusMessage) {
                    setBusy(true, statusMessage);
                }

                @Override
                public void onSuccess(DatabaseConfig activatedConfig, DataLogicSystem dlSystem) {
                    activeDbConfig = activatedConfig;
                    updateDataLogicSystem(dlSystem);
                    setBusy(false, "Ligado com sucesso a " + activatedConfig.name());
                    updateBadgeAndButtons();
                    m_txtKeys.requestFocus();
                }

                @Override
                public void onError(Throwable error) {
                    LOGGER.log(Level.WARNING, "Failed activating database: " + selected.name(), error);
                    setBusy(false, "Erro ao conectar");
                    updateBadgeAndButtons();
                    showEmptyUserList();

                    MessageInf msg = new MessageInf(MessageInf.SGN_WARNING,
                            AppLocal.getIntString("message.databaseconnectionerror"), error);
                    msg.show(AuthenticationPanel.this);
                }
            });
        } else if (appView != null) {
            try {
                appView.switchDatabase();
                activeDbConfig = selected;
                updateBadgeAndButtons();
                setBusy(false, "Ligado a " + selected.name());
            }
            catch (BasicException ex) {
                LOGGER.log(Level.WARNING, "Error activating database", ex);
                setBusy(false, "Erro ao conectar");
                updateBadgeAndButtons();
            }
        }
    }

    /**
     * Controls the busy/loading state across UI components during database
     * migrations and connections.
     *
     * @param busy True while connection and migrations are running.
     * @param message Current progress or completion message.
     */
    public void setBusy(boolean busy, String message) {
        comboDatabases.setEnabled(!busy);
        btnSelectDatabase.setEnabled(!busy);
        btnConfigureDatabase.setEnabled(!busy);
        progressBar.setVisible(busy);
        if (message != null) {
            lblStatus.setText(message);
        }
    }

    private void openConfiguration() {
        SwingUtilities.invokeLater(() -> {
            try {
                JFrmConfig frmConfig = new JFrmConfig(appProperties);
                frmConfig.setLocationRelativeTo(AuthenticationPanel.this);
                frmConfig.setVisible(true);
            }
            catch (Exception ex) {
                LOGGER.log(Level.WARNING, "Could not open configuration frame", ex);
            }
        });
    }

    private void initPanel() {
        // Rule 7.2 Component Identification — setName() URN Anchoring
        setName("kriolos:auth:panel");
        headerLabel.setName("kriolos:auth:lbl-login-header");
        vendorImageLabel.setName("kriolos:auth:lbl-vendor-image");
        usersLisScrollPane.setName("kriolos:auth:scroll-users");
        m_txtKeys.setName("kriolos:auth:txt-barcode-keys");

        mainScrollPanel.setName("kriolos:auth:scroll-main");
        mainScrollPanel.setViewportView(new CopyrightPanel());
        mainScrollPanel.setBorder(BorderFactory.createEmptyBorder());
        usersLisScrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(30, 30));
        showListPeople();

        inputtext = new StringBuilder();
        m_txtKeys.setText(null);
        java.awt.EventQueue.invokeLater(() -> m_txtKeys.requestFocus());
    }

    public DatabaseConfig getSelectedDatabase() {
        return (DatabaseConfig) comboDatabases.getSelectedItem();
    }

    public DatabaseConfig getActiveDatabase() {
        return activeDbConfig;
    }

    public JComboBox<DatabaseConfig> getComboDatabases() {
        return comboDatabases;
    }

    public JButton getBtnSelect() {
        return btnSelectDatabase;
    }

    public JButton getBtnConfigure() {
        return btnConfigureDatabase;
    }

    public void updateDataLogicSystem(DataLogicSystem dlSystem) {
        this.m_dlSystem = dlSystem;
        showListPeople();
        revalidate();
        repaint();
    }

    public void refreshUsers() {
        showListPeople();
    }

    private void showListPeople() {
        List<AppUser> people = null;

        if (m_dlSystem != null) {
            try {
                people = m_dlSystem.listPeopleVisible();
            }
            catch (BasicException ee) {
                LOGGER.log(Level.WARNING, "Error listing visible users", ee);
            }
        }

        if (people == null || people.isEmpty()) {
            showEmptyUserList();
            return;
        }

        LOGGER.log(Level.INFO, "Found visible users: {0}", people.size());

        // 1. Choose your baseline size variant
        ButtonSize variant = ButtonSize.EXTRA_LARGE;

        JFlowPanel jPeople = new JFlowPanel();
        for (AppUser user : people) {
            // 2. Instantiate using the native Factory
            JButton btn = POSButtonFactory.createButton(variant);
            btn.setAction(new SetUserAction(user));
            
            // 3. Apply the specific layout width (280px) and get the dynamic height from the variant
            btn.setPreferredSize(new Dimension(280, variant.getHeight()));

            jPeople.add(btn);
        }

        usersLisScrollPane.setViewportView(jPeople);
    }

    private void showLoadingUsersList(String message) {
        JPanel loadingPanel = new JPanel(new GridBagLayout());
        JLabel label = new JLabel(message != null ? message : "A carregar utilizadores...");
        label.setHorizontalAlignment(SwingConstants.CENTER);
        loadingPanel.add(label);
        usersLisScrollPane.setViewportView(loadingPanel);
    }

    private void showEmptyUserList() {
        JPanel emptyPanel = new JPanel(new GridBagLayout());
        JLabel emptyLabel = new JLabel("Nenhum utilizador disponível");
        emptyLabel.setHorizontalAlignment(SwingConstants.CENTER);
        emptyPanel.add(emptyLabel);
        usersLisScrollPane.setViewportView(emptyPanel);
    }

    private void processKey(char c) {
        if (c == '\n') {
            AppUser user = null;
            try {
                if (m_dlSystem != null) {
                    user = m_dlSystem.findPeopleByCard(inputtext.toString());
                }
            }
            catch (BasicException ee) {
                user = null;
            }

            if (user == null) {
                LOGGER.log(Level.INFO, "Card not found: {0}", inputtext);
                MessageInf msg = new MessageInf(MessageInf.SGN_WARNING,
                        AppLocal.getIntString("message.nocard"));
                msg.show(this);
            } else {
                LOGGER.log(Level.INFO, "User found by card: {0}", user.getName());
                authListener.onSucess(user);
            }
            inputtext = new StringBuilder();
        } else {
            inputtext.append(c);
        }
    }

    private class SetUserAction extends AbstractAction {

        private static final long serialVersionUID = 1L;
        private final AppUser m_actionuser;

        public SetUserAction(AppUser user) {
            m_actionuser = user;
            putValue(Action.NAME, m_actionuser.getName());
            putValue(Action.SMALL_ICON, m_actionuser.getIcon());
        }

        @Override
        public void actionPerformed(ActionEvent evt) {
            LOGGER.log(Level.INFO, "User button clicked: {0}", m_actionuser.getName());

            try {
                if (m_actionuser.authenticate()) {
                    LOGGER.log(Level.INFO, "Login Success without password");
                    authListener.onSucess(m_actionuser);
                } else {
                    String sPassword = JPasswordPanel.show(SwingUtilities.getWindowAncestor(AuthenticationPanel.this),
                            AppLocal.getIntString("label.Password"),
                            m_actionuser.getName(),
                            m_actionuser.getIcon());
                    if (sPassword != null) {
                        if (m_actionuser.authenticate(sPassword)) {
                            LOGGER.log(Level.INFO, "Login Success");
                            authListener.onSucess(m_actionuser);
                        } else {
                            LOGGER.log(Level.INFO, "Login failed");
                            MessageInf msg = new MessageInf(MessageInf.SGN_WARNING,
                                    AppLocal.getIntString("message.BadPassword"));
                            msg.show(AuthenticationPanel.this);
                        }
                    }
                }
            }
            catch (Exception ex) {
                LOGGER.log(Level.WARNING, "Exception on LOGIN: ", ex);
            }
        }
    }

    private static class DatabaseConfigRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof DatabaseConfig db) {
                String label = db.name() != null ? db.name() : db.url();
                setText(label);
            }
            return this;
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        vendorImageLabel = new javax.swing.JLabel();
        mainScrollPanel = new javax.swing.JScrollPane();
        filler2 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 10), new java.awt.Dimension(32767, 0));
        leftPanel = new javax.swing.JPanel();
        leftHeaderPanel = new javax.swing.JPanel();
        headerLabel = new javax.swing.JLabel();
        usersLisScrollPane = new javax.swing.JScrollPane();
        leftFooterPanel = new javax.swing.JPanel();
        m_txtKeys = new javax.swing.JTextField();

        setLayout(new java.awt.BorderLayout());

        mainPanel.setLayout(new java.awt.BorderLayout());

        vendorImageLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        vendorImageLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/logo_100×100.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(vendorImageLabel, AppLocal.getIntString("JAuthPanel.m_vendorImageLabel.text")); // NOI18N
        vendorImageLabel.setToolTipText(AppLocal.getIntString("JAuthPanel.m_vendorImageLabel.toolTipText")); // NOI18N
        vendorImageLabel.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        vendorImageLabel.setName("vendorImageLabel"); // NOI18N
        mainPanel.add(vendorImageLabel, java.awt.BorderLayout.NORTH);
        vendorImageLabel.getAccessibleContext().setAccessibleName("");

        mainPanel.add(mainScrollPanel, java.awt.BorderLayout.CENTER);
        mainPanel.add(filler2, java.awt.BorderLayout.SOUTH);

        add(mainPanel, java.awt.BorderLayout.CENTER);

        leftPanel.setPreferredSize(new java.awt.Dimension(300, 400));
        leftPanel.setLayout(new java.awt.BorderLayout());

        leftHeaderPanel.setMinimumSize(new java.awt.Dimension(300, 40));
        leftHeaderPanel.setName(""); // NOI18N
        leftHeaderPanel.setPreferredSize(new java.awt.Dimension(300, 40));
        leftHeaderPanel.setLayout(new java.awt.BorderLayout());

        headerLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(headerLabel, AppLocal.getIntString("JAuthPanel.m_LoginLabel.text")); // NOI18N
        headerLabel.setName("m_LoginLabel"); // NOI18N
        leftHeaderPanel.add(headerLabel, java.awt.BorderLayout.CENTER);
        headerLabel.getAccessibleContext().setAccessibleName(AppLocal.getIntString("AuthenticationPanel.headerLabel.AccessibleContext.accessibleName")); // NOI18N

        leftPanel.add(leftHeaderPanel, java.awt.BorderLayout.NORTH);

        usersLisScrollPane.setBorder(null);
        usersLisScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        usersLisScrollPane.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        usersLisScrollPane.setMinimumSize(new java.awt.Dimension(21, 40));
        usersLisScrollPane.setPreferredSize(new java.awt.Dimension(300, 40));
        leftPanel.add(usersLisScrollPane, java.awt.BorderLayout.CENTER);

        m_txtKeys.setPreferredSize(new java.awt.Dimension(0, 0));
        m_txtKeys.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                m_txtKeysKeyTyped(evt);
            }
        });

        javax.swing.GroupLayout leftFooterPanelLayout = new javax.swing.GroupLayout(leftFooterPanel);
        leftFooterPanel.setLayout(leftFooterPanelLayout);
        leftFooterPanelLayout.setHorizontalGroup(
            leftFooterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(leftFooterPanelLayout.createSequentialGroup()
                .addComponent(m_txtKeys, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(300, Short.MAX_VALUE))
        );
        leftFooterPanelLayout.setVerticalGroup(
            leftFooterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(leftFooterPanelLayout.createSequentialGroup()
                .addComponent(m_txtKeys, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(57, 57, 57))
        );

        leftPanel.add(leftFooterPanel, java.awt.BorderLayout.SOUTH);

        add(leftPanel, java.awt.BorderLayout.EAST);
    }// </editor-fold>//GEN-END:initComponents

    private void m_txtKeysKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_m_txtKeysKeyTyped
        m_txtKeys.setText("0");
        processKey(evt.getKeyChar());
    }//GEN-LAST:event_m_txtKeysKeyTyped

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.Box.Filler filler2;
    private javax.swing.JLabel headerLabel;
    private javax.swing.JPanel leftFooterPanel;
    private javax.swing.JPanel leftHeaderPanel;
    private javax.swing.JPanel leftPanel;
    private javax.swing.JTextField m_txtKeys;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JScrollPane mainScrollPanel;
    private javax.swing.JScrollPane usersLisScrollPane;
    private javax.swing.JLabel vendorImageLabel;
    // End of variables declaration//GEN-END:variables
}
