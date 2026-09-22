/*
 * Copyright (C) 2026 KriolOS
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
package com.openbravo.pos.forms;

import com.openbravo.basic.BasicException;
import com.openbravo.data.gui.MessageInf;
import com.openbravo.pos.config.JFrmConfig;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * DatabaseSelector provides an interactive UI panel for choosing and activating
 * a configured database instance, with dynamic listener notification.
 *
 * If no databases are configured in the properties, an action button is displayed
 * to open the application configuration dialog.
 */
public class DatabaseSelector extends JPanel {

    private static final Logger LOGGER = Logger.getLogger(DatabaseSelector.class.getName());
    private static final long serialVersionUID = 1L;

    public interface DatabaseSelectListener {
        void onDatabaseSelected(String dbKey, String dbName) throws BasicException;
    }

    public static class DatabaseItem {
        private final String key;
        private final String displayName;

        public DatabaseItem(String key, String displayName) {
            this.key = key;
            this.displayName = displayName;
        }

        public String getKey() {
            return key;
        }

        public String getDisplayName() {
            return displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    private final AppProperties properties;
    private final List<DatabaseSelectListener> listeners = new ArrayList<>();

    private JComboBox<DatabaseItem> comboDatabases;
    private JButton btnSelect;
    private JButton btnConfigure;
    private JLabel lblTitle;
    private JPanel controlsPanel;

    public DatabaseSelector(AppProperties props) {
        this.properties = props;
        initComponents();
        loadDatabases();
    }

    private void initComponents() {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createEtchedBorder(),
                        AppLocal.getIntString("label.database") != null ? AppLocal.getIntString("label.database") : "Base de Dados"),
                BorderFactory.createEmptyBorder(2, 4, 4, 4)
        ));

        controlsPanel = new JPanel();
        controlsPanel.setLayout(new javax.swing.BoxLayout(controlsPanel, javax.swing.BoxLayout.Y_AXIS));

        lblTitle = new JLabel();
        lblTitle.setName("lblDatabaseSelector");

        comboDatabases = new JComboBox<>();
        comboDatabases.setName("comboDatabases");
        comboDatabases.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        comboDatabases.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        comboDatabases.setPreferredSize(new Dimension(260, 30));

        btnSelect = new JButton(AppLocal.getIntString("button.activate"));
        btnSelect.setName("btnSelectDatabase");
        btnSelect.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        btnSelect.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        btnSelect.setPreferredSize(new Dimension(260, 32));
        btnSelect.setToolTipText(AppLocal.getIntString("button.activate.tooltip"));
        try {
            btnSelect.setIcon(new ImageIcon(getClass().getResource("/com/openbravo/images/apply.png")));
        } catch (Exception ignored) {
        }
        btnSelect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                notifySelection();
            }
        });

        btnConfigure = new JButton(AppLocal.getIntString("button.configuration"));
        btnConfigure.setName("btnOpenConfiguration");
        btnConfigure.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        btnConfigure.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        btnConfigure.setPreferredSize(new Dimension(260, 32));
        btnConfigure.setToolTipText(AppLocal.getIntString("button.configuration.tooltip"));
        try {
            btnConfigure.setIcon(new ImageIcon(getClass().getResource("/com/openbravo/images/configuration.png")));
        } catch (Exception ignored) {
        }
        btnConfigure.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openConfiguration();
            }
        });

        controlsPanel.add(comboDatabases);
        controlsPanel.add(javax.swing.Box.createVerticalStrut(6));
        controlsPanel.add(btnSelect);
        controlsPanel.add(javax.swing.Box.createVerticalStrut(4));
        controlsPanel.add(btnConfigure);

        add(controlsPanel, BorderLayout.CENTER);
    }

    public void loadDatabases() {
        List<String> rawDbs = AppViewConnection.findAllDB(properties);
        DefaultComboBoxModel<DatabaseItem> model = new DefaultComboBoxModel<>();

        if (rawDbs != null) {
            for (String entry : rawDbs) {
                // entry format: db.name=Main DB or db1.name=DBSecond
                String[] parts = entry.split("=", 2);
                String prefix = "db";
                String name = entry;
                if (parts.length >= 2) {
                    name = parts[1];
                    String[] keyParts = parts[0].split("[.]", 2);
                    if (keyParts.length >= 1) {
                        prefix = keyParts[0];
                    }
                }
                model.addElement(new DatabaseItem(prefix, name));
            }
        }

        comboDatabases.setModel(model);

        boolean hasDbs = model.getSize() > 0;
        comboDatabases.setVisible(hasDbs);
        btnSelect.setVisible(hasDbs);
        btnConfigure.setVisible(!hasDbs);

        if (hasDbs) {
            comboDatabases.setSelectedIndex(0);
        }

        revalidate();
        repaint();
    }

    public void addDatabaseSelectListener(DatabaseSelectListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeDatabaseSelectListener(DatabaseSelectListener listener) {
        listeners.remove(listener);
    }

    public DatabaseItem getSelectedItem() {
        return (DatabaseItem) comboDatabases.getSelectedItem();
    }

    public String getSelectedDbKey() {
        DatabaseItem item = getSelectedItem();
        return item != null ? item.getKey() : AppViewConnection.getDefaultDB();
    }

    public String getSelectedDbName() {
        DatabaseItem item = getSelectedItem();
        return item != null ? item.getDisplayName() : AppLocal.getIntString("label.defaultdatabase");
    }

    public void setSelectedDbKey(String dbKey) {
        if (dbKey == null) {
            return;
        }
        for (int i = 0; i < comboDatabases.getItemCount(); i++) {
            DatabaseItem item = comboDatabases.getItemAt(i);
            if (dbKey.equalsIgnoreCase(item.getKey())) {
                comboDatabases.setSelectedIndex(i);
                break;
            }
        }
    }

    public int getDatabaseCount() {
        return comboDatabases.getItemCount();
    }

    public void autoSelectIfSingle() {
        if (comboDatabases.getItemCount() == 1) {
            LOGGER.log(Level.INFO, "Single database configured, auto-activating: {0}", getSelectedDbKey());
            notifySelection();
        }
    }

    private void notifySelection() {
        DatabaseItem item = getSelectedItem();
        if (item != null) {
            LOGGER.log(Level.INFO, "Database selected from selector: {0} ({1})", new Object[]{item.getDisplayName(), item.getKey()});
            for (DatabaseSelectListener listener : new ArrayList<>(listeners)) {
                try {
                    listener.onDatabaseSelected(item.getKey(), item.getDisplayName());
                } catch (BasicException ex) {
                    LOGGER.log(Level.WARNING, "Error activating database: " + item.getKey(), ex);
                    MessageInf msg = new MessageInf(MessageInf.SGN_WARNING,
                            AppLocal.getIntString("message.databaseconnectionerror"), ex);
                    msg.show(this);
                } catch (Exception ex) {
                    LOGGER.log(Level.WARNING, "Error activating database", ex);
                    MessageInf msg = new MessageInf(MessageInf.SGN_WARNING,
                            AppLocal.getIntString("message.databaseconnectionerror"), ex);
                    msg.show(this);
                }
            }
        }
    }

    public void openConfiguration() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    JFrmConfig frmConfig = new JFrmConfig(properties);
                    frmConfig.setLocationRelativeTo(DatabaseSelector.this);
                    frmConfig.setVisible(true);
                } catch (Exception ex) {
                    LOGGER.log(Level.WARNING, "Could not open configuration frame", ex);
                }
            }
        });
    }

    public JComboBox<DatabaseItem> getComboDatabases() {
        return comboDatabases;
    }

    public JButton getBtnSelect() {
        return btnSelect;
    }

    public JButton getBtnConfigure() {
        return btnConfigure;
    }
}
