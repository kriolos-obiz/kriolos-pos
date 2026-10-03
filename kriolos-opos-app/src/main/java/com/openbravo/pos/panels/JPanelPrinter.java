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
package com.openbravo.pos.panels;

import com.openbravo.basic.BasicException;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.JPanelView;
import com.openbravo.pos.hardware.PosHardwareManager;
import com.openbravo.pos.printer.DeviceFiscalPrinter;
import com.openbravo.pos.printer.DevicePrinter;
import com.openbravo.pos.printer.screen.DevicePrinterPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * Panel to manage and preview printer tickets on screen.
 * Provides controls for zooming, printing, and resetting previews.
 * 
 * @author adrianromero
 * @author poolborges
 * @author KriolOS
 */
public class JPanelPrinter extends JPanel implements JPanelView {

    private static final long serialVersionUID = 1L;
    private static final Font BUTTON_FONT = new Font("Arial", Font.BOLD, 12);

    private final AppView appView;

    public JPanelPrinter(AppView appView) {
        this.appView = appView;
        initComponents();
        initComponentExtras();
    }

    /**
     * Executes post-constructor UI assemblies, components bindings, and custom
     * layout actions.
     */
    private void initComponentExtras() {
        JComponent displayComp = PosHardwareManager.getDisplayComponent(appView.getDeviceTicket().getDeviceDisplay());
        if (displayComp != null) {
            displayJPanel.add(displayComp);
        }

        List<DevicePrinter> printers = appView.getDeviceTicket().getDevicePrinterAll();
        for (int i = 0; i < printers.size(); i++) {
            DevicePrinter printer = printers.get(i);
            if (printer.getPrinterComponent() != null) {
                printersJTabbedPane.add(printer.getPrinterName(), printer.getPrinterComponent());
            }
        }

        DeviceFiscalPrinter fiscalPrinter = appView.getDeviceTicket().getFiscalPrinter();
        if (fiscalPrinter.getFiscalComponent() != null) {
            printersJTabbedPane.add(fiscalPrinter.getFiscalName(), fiscalPrinter.getFiscalComponent());
        }

        // Action controls toolbar positioned at the bottom
        JPanel actionContainerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 5));

        // 1. Zoom In (+)
        JButton zoomInButton = new JButton("+");
        zoomInButton.setFont(BUTTON_FONT);
        zoomInButton.setToolTipText("Zoom In");
        zoomInButton.addActionListener(e -> {
            DevicePrinterPanel panel = getSelectedPrinterPanel();
            if (panel != null) {
                panel.zoomIn();
            }
        });

        // 2. Zoom Out (-)
        JButton zoomOutButton = new JButton("-");
        zoomOutButton.setFont(BUTTON_FONT);
        zoomOutButton.setToolTipText("Zoom Out");
        zoomOutButton.addActionListener(e -> {
            DevicePrinterPanel panel = getSelectedPrinterPanel();
            if (panel != null) {
                panel.zoomOut();
            }
        });

        // 3. Reset Zoom (100%)
        JButton zoomResetButton = new JButton("100%");
        zoomResetButton.setFont(BUTTON_FONT);
        zoomResetButton.setToolTipText("Reset Zoom");
        zoomResetButton.addActionListener(e -> {
            DevicePrinterPanel panel = getSelectedPrinterPanel();
            if (panel != null) {
                panel.setZoom(1.0);
            }
        });

        // 4. Print layout
        JButton printButton = new JButton(AppLocal.getIntString("button.print"));
        printButton.setFont(BUTTON_FONT);
        printButton.addActionListener(e -> {
            DevicePrinterPanel panel = getSelectedPrinterPanel();
            if (panel != null) {
                panel.printTickets();
            }
        });

        // 5. Clear all screens
        JButton clearScreensButton = new JButton(AppLocal.getIntString("button.clearscreens"));
        clearScreensButton.setFont(BUTTON_FONT);
        clearScreensButton.addActionListener(e -> handleClearScreensAction());

        // Add controls in logical operational sequence
        actionContainerPanel.add(zoomInButton);
        actionContainerPanel.add(zoomOutButton);
        actionContainerPanel.add(zoomResetButton);
        actionContainerPanel.add(printButton);
        actionContainerPanel.add(clearScreensButton);

        add(actionContainerPanel, BorderLayout.SOUTH);
    }

    /**
     * Resolves the DevicePrinterPanel inside the currently active tab.
     * 
     * @return the active DevicePrinterPanel or null if not selected or applicable
     */
    private DevicePrinterPanel getSelectedPrinterPanel() {
        Component selected = printersJTabbedPane.getSelectedComponent();
        if (selected instanceof DevicePrinterPanel devicePrinterPanel) {
            return devicePrinterPanel;
        }
        return null;
    }

    /**
     * Scans all tabs in printersJTabbedPane and resets ticket history.
     */
    private void handleClearScreensAction() {
        int tabCount = printersJTabbedPane.getTabCount();
        for (int i = 0; i < tabCount; i++) {
            Component tabComponent = printersJTabbedPane.getComponentAt(i);
            if (tabComponent instanceof DevicePrinterPanel devicePrinterPanel) {
                devicePrinterPanel.reset();
            }
        }
    }

    @Override
    public JComponent getComponent() {
        return this;
    }

    @Override
    public String getTitle() {
        return AppLocal.getIntString("Menu.Printer");
    }

    @Override
    public void activate() throws BasicException {
    }

    @Override
    public boolean deactivate() {
        return true;
    }

    private void initComponents() {
        displayJPanel = new javax.swing.JPanel();
        printersJPanel = new javax.swing.JPanel();
        printersJTabbedPane = new javax.swing.JTabbedPane();

        setBackground(Color.red);
        setPreferredSize(new java.awt.Dimension(480, 600));
        setRequestFocusEnabled(false);
        setLayout(new java.awt.BorderLayout());
        add(displayJPanel, java.awt.BorderLayout.NORTH);

        printersJPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        printersJPanel.setLayout(new java.awt.BorderLayout());

        printersJTabbedPane.setFont(new java.awt.Font("Arial", 0, 12));
        printersJPanel.add(printersJTabbedPane, java.awt.BorderLayout.CENTER);

        add(printersJPanel, java.awt.BorderLayout.CENTER);
    }

    private javax.swing.JPanel displayJPanel;
    private javax.swing.JPanel printersJPanel;
    private javax.swing.JTabbedPane printersJTabbedPane;
}