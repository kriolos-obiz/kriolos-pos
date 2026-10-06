//    KriolOS POS
//    Copyright (c) 2019-2026 KriolOS
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

import com.openbravo.pos.forms.AppLocal;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.util.function.Consumer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.UIManager;

/**
 * Top header toolbar pane for ticket views.
 * Encapsulates ticket bags, common sales actions (scripts toggle, scale, customer, split,
 * remote order print, and reprint), and the secondary script extension toolbar.
 * Pure Java Swing layout with zero NetBeans Matisse form dependencies.
 */
public class TicketHeaderPane extends JPanel {

    private final JPanel topBar;
    private final JPanel bagContainer;
    private final JToggleButton btnToggleScripts;
    private final JButton btnScale;
    private final JPanel actionButtonsPanel;
    private final JButton btnCustomer;
    private final JButton btnSplit;
    private final JButton btnRemoteOrder;
    private final JButton btnReprint;
    private final JPanel scriptsPanel;
    private final JPanel bagExtPanel;

    public TicketHeaderPane() {
        super(new BorderLayout());
        setOpaque(false);

        Font baseFont = UIManager.getFont("Button.font");
        if (baseFont == null) {
            baseFont = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
        }

        // Top toolbar
        topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        topBar.setOpaque(false);
        topBar.setPreferredSize(new Dimension(0, 55));

        bagContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        bagContainer.setOpaque(false);
        topBar.add(bagContainer);

        // Scripts/tools toggle
        btnToggleScripts = new JToggleButton();
        btnToggleScripts.setFont(baseFont.deriveFont(14f));
        btnToggleScripts.setPreferredSize(new Dimension(80, 45));
        btnToggleScripts.setFocusable(false);
        btnToggleScripts.setRequestFocusEnabled(false);
        btnToggleScripts.setIcon(loadIcon("/com/openbravo/images/resources.png"));
        topBar.add(btnToggleScripts);

        // Scale
        btnScale = new JButton(AppLocal.getIntString("button.scale"));
        btnScale.setFont(baseFont);
        btnScale.setToolTipText(AppLocal.getIntString("tooltip.scale"));
        btnScale.setPreferredSize(new Dimension(85, 45));
        btnScale.setMargin(new Insets(8, 14, 8, 14));
        btnScale.setFocusPainted(false);
        btnScale.setFocusable(false);
        btnScale.setRequestFocusEnabled(false);
        btnScale.setIcon(loadIcon("/com/openbravo/images/scale.png"));
        topBar.add(btnScale);

        // Action buttons
        actionButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        actionButtonsPanel.setOpaque(false);

        btnCustomer = new JButton();
        btnCustomer.setFont(baseFont.deriveFont(14f));
        btnCustomer.setToolTipText(AppLocal.getIntString("tooltip.salescustomer"));
        btnCustomer.setPreferredSize(new Dimension(80, 45));
        btnCustomer.setFocusable(false);
        btnCustomer.setRequestFocusEnabled(false);
        btnCustomer.setIcon(loadIcon("/com/openbravo/images/customer.png"));
        actionButtonsPanel.add(btnCustomer);

        btnSplit = new JButton();
        btnSplit.setToolTipText(AppLocal.getIntString("tooltip.salesplit"));
        btnSplit.setPreferredSize(new Dimension(80, 45));
        btnSplit.setMargin(new Insets(8, 14, 8, 14));
        btnSplit.setFocusPainted(false);
        btnSplit.setFocusable(false);
        btnSplit.setRequestFocusEnabled(false);
        btnSplit.setEnabled(false);
        btnSplit.setIcon(loadIcon("/com/openbravo/images/sale_split_sml.png"));
        actionButtonsPanel.add(btnSplit);

        btnRemoteOrder = new JButton(AppLocal.getIntString("button.sendorder"));
        btnRemoteOrder.setFont(baseFont);
        btnRemoteOrder.setToolTipText(AppLocal.getIntString("tooltip.printtoremote"));
        btnRemoteOrder.setPreferredSize(new Dimension(80, 45));
        btnRemoteOrder.setMargin(new Insets(0, 4, 0, 4));
        btnRemoteOrder.setFocusable(false);
        btnRemoteOrder.setRequestFocusEnabled(false);
        btnRemoteOrder.setIcon(loadIcon("/com/openbravo/images/remote_print.png"));
        actionButtonsPanel.add(btnRemoteOrder);

        btnReprint = new JButton();
        btnReprint.setFont(baseFont);
        btnReprint.setToolTipText(AppLocal.getIntString("tooltip.reprintLastTicket"));
        btnReprint.setPreferredSize(new Dimension(80, 45));
        btnReprint.setMargin(new Insets(8, 14, 8, 14));
        btnReprint.setFocusPainted(false);
        btnReprint.setFocusable(false);
        btnReprint.setRequestFocusEnabled(false);
        btnReprint.setIcon(loadIcon("/com/openbravo/images/reprint24.png"));
        actionButtonsPanel.add(btnReprint);

        topBar.add(actionButtonsPanel);
        add(topBar, BorderLayout.PAGE_START);

        // Scripts / Extension panel
        scriptsPanel = new JPanel(new BorderLayout());
        scriptsPanel.setOpaque(false);
        scriptsPanel.setPreferredSize(new Dimension(200, 60));
        scriptsPanel.setVisible(false);

        bagExtPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        bagExtPanel.setOpaque(false);
        bagExtPanel.setPreferredSize(new Dimension(20, 60));
        bagExtPanel.setVisible(false);
        scriptsPanel.add(bagExtPanel, BorderLayout.PAGE_START);

        add(scriptsPanel, BorderLayout.CENTER);
    }

    private ImageIcon loadIcon(String path) {
        try {
            java.net.URL url = getClass().getResource(path);
            if (url != null) {
                return new ImageIcon(url);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    public void setBagComponent(Component component) {
        bagContainer.removeAll();
        if (component != null) {
            bagContainer.add(component);
        }
        bagContainer.revalidate();
        bagContainer.repaint();
    }

    public void addScriptComponent(Component component) {
        if (component != null) {
            bagExtPanel.add(component);
            bagExtPanel.revalidate();
            bagExtPanel.repaint();
        }
    }

    public void setScriptsVisible(boolean visible) {
        scriptsPanel.setVisible(visible);
        bagExtPanel.setVisible(visible);
        revalidate();
        repaint();
    }

    public boolean isScriptsVisible() {
        return scriptsPanel.isVisible();
    }

    public void setToggleScriptsSelected(boolean selected) {
        btnToggleScripts.setSelected(selected);
    }

    public boolean isToggleScriptsSelected() {
        return btnToggleScripts.isSelected();
    }

    public void setScaleVisible(boolean visible) {
        btnScale.setVisible(visible);
    }

    public void setCustomerVisible(boolean visible) {
        btnCustomer.setVisible(visible);
    }

    public void setCustomerEnabled(boolean enabled) {
        btnCustomer.setEnabled(enabled);
    }

    public void setSplitVisible(boolean visible) {
        btnSplit.setVisible(visible);
    }

    public void setSplitEnabled(boolean enabled) {
        btnSplit.setEnabled(enabled);
    }

    public void setRemoteOrderVisible(boolean visible) {
        btnRemoteOrder.setVisible(visible);
    }

    public void setRemoteOrderEnabled(boolean enabled) {
        btnRemoteOrder.setEnabled(enabled);
    }

    public void setReprintVisible(boolean visible) {
        btnReprint.setVisible(visible);
    }

    public void setReprintEnabled(boolean enabled) {
        btnReprint.setEnabled(enabled);
    }

    public void setOnToggleScripts(Consumer<Boolean> listener) {
        btnToggleScripts.addActionListener(e -> {
            if (listener != null) {
                listener.accept(btnToggleScripts.isSelected());
            }
        });
    }

    public void setOnScaleAction(Runnable action) {
        btnScale.addActionListener(e -> {
            if (action != null) {
                action.run();
            }
        });
    }

    public void setOnCustomerAction(Runnable action) {
        btnCustomer.addActionListener(e -> {
            if (action != null) {
                action.run();
            }
        });
    }

    public void setOnSplitAction(Runnable action) {
        btnSplit.addActionListener(e -> {
            if (action != null) {
                action.run();
            }
        });
    }

    public void setOnRemoteOrderAction(Runnable action) {
        btnRemoteOrder.addActionListener(e -> {
            if (action != null) {
                action.run();
            }
        });
    }

    public void setOnReprintAction(Runnable action) {
        btnReprint.addActionListener(e -> {
            if (action != null) {
                action.run();
            }
        });
    }

    public JPanel getBagContainer() {
        return bagContainer;
    }

    public JToggleButton getBtnToggleScripts() {
        return btnToggleScripts;
    }

    public JButton getBtnScale() {
        return btnScale;
    }

    public JButton getBtnCustomer() {
        return btnCustomer;
    }

    public JButton getBtnSplit() {
        return btnSplit;
    }

    public JButton getBtnRemoteOrder() {
        return btnRemoteOrder;
    }

    public JButton getBtnReprint() {
        return btnReprint;
    }

    public JPanel getScriptsPanel() {
        return scriptsPanel;
    }

    public JPanel getBagExtPanel() {
        return bagExtPanel;
    }
}
