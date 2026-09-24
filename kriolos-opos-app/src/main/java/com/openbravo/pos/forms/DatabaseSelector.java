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
import com.openbravo.pos.forms.AppProperties.DatabaseConfig;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
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
        void onDatabaseSelected(DatabaseConfig dbConfig) throws BasicException;
    }

    private final AppProperties properties;
    private final List<DatabaseSelectListener> listeners = new ArrayList<>();

    private JComboBox<DatabaseConfig> comboDatabases;
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
        lblTitle.setName("kriolos:dbselector:lblDatabaseSelector");

        comboDatabases = new JComboBox<>();
        comboDatabases.setName("kriolos:dbselector:comboDatabases");
        comboDatabases.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        comboDatabases.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        comboDatabases.setPreferredSize(new Dimension(260, 30));
        comboDatabases.setRenderer(new DatabaseConfigRenderer());

        btnSelect = new JButton(AppLocal.getIntString("button.activate"));
        btnSelect.setName("kriolos:dbselector:btnSelectDatabase");
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
        List<DatabaseConfig> rawDbs = properties.getAll();
        DefaultComboBoxModel<DatabaseConfig> model = new DefaultComboBoxModel<>();

        if (rawDbs != null) {
            for (DatabaseConfig entry : rawDbs) {
                model.addElement(entry);
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

    public DatabaseConfig getSelectedItem() {
        return (DatabaseConfig) comboDatabases.getSelectedItem();
    }


    public String getSelectedDbName() {
        DatabaseConfig item = getSelectedItem();
        return item != null ? item.name(): AppLocal.getIntString("label.defaultdatabase");
    }

    public void setSelectedDbKey(DatabaseConfig dbConfig) {
        if (dbConfig == null) {
            return;
        }
        for (int i = 0; i < comboDatabases.getItemCount(); i++) {
            DatabaseConfig item = comboDatabases.getItemAt(i);
            if (dbConfig == item) {
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
            LOGGER.log(Level.INFO, "Single database configured, auto-activating: {0}", getSelectedItem().name());
            notifySelection();
        }
    }

    private void notifySelection() {
        DatabaseConfig item = getSelectedItem();
        if (item != null) {
            LOGGER.log(Level.INFO, "Database selected from selector: {0} ({1})", new Object[]{item.name(), item.url()});
            for (DatabaseSelectListener listener : new ArrayList<>(listeners)) {
                try {
                    listener.onDatabaseSelected(item);
                } catch (BasicException ex) {
                    LOGGER.log(Level.WARNING, "Error activating database: " + item.name(), ex);
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

    public JComboBox<DatabaseConfig> getComboDatabases() {
        return comboDatabases;
    }

    public JButton getBtnSelect() {
        return btnSelect;
    }

    public JButton getBtnConfigure() {
        return btnConfigure;
    }
}


class DatabaseConfigRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, 
                                                  boolean isSelected, boolean cellHasFocus) {
        
        // Permite que o Swing trate o background, cores e seleção automaticamente
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        
        if (value instanceof DatabaseConfig db) {
            String label = db.name() != null ? db.name() : db.url();
            setText(label); 
        }
        
        return this;
    }
}