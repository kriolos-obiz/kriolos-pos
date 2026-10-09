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
package com.openbravo.pos.printer.screen;

import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.printer.DevicePrinter;
import com.openbravo.pos.printer.ticket.BasicTicket;
import com.openbravo.pos.printer.ticket.BasicTicketForScreen;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

/**
 * Visual panel to preview printed tickets on screen.
 * 
 * @author JG uniCenta
 * @author KriolOS
 */
public class DevicePrinterPanel extends javax.swing.JPanel implements DevicePrinter {
    
    private static final long serialVersionUID = 1L;

    private final String printerName;
    private final JTicketContainer ticketContainer;    
    private BasicTicket currentTicket;
    private javax.swing.JScrollPane m_jScrollView;
    
    /** 
     * Creates new form DevicePrinterPanel 
     */
    public DevicePrinterPanel() {
        ticketContainer = new JTicketContainer();
        initComponents();
        
        printerName = AppLocal.getIntString("printer.screen");
        currentTicket = null;
    }
    
    // =========================================================================
    // ZOOM & PRINT INTEGRATION
    // =========================================================================
    
    /**
     * Triggers the OS print dialog for the current preview.
     */
    public void printTickets() {
        ticketContainer.printTickets();
    }
    
    /**
     * Sets an absolute zoom scale.
     * @param zoom 1.0 is 100%, 1.5 is 150%, etc.
     */
    public void setZoom(double zoom) {
        ticketContainer.setZoom(zoom);
    }
    
    public double getZoom() {
        return ticketContainer.getZoom();
    }
    
    /**
     * Helper to zoom in by 10%
     */
    public void zoomIn() {
        setZoom(getZoom() + 0.1);
    }
    
    /**
     * Helper to zoom out by 10%
     */
    public void zoomOut() {
        setZoom(getZoom() - 0.1);
    }

    // =========================================================================
    // DEVICE PRINTER IMPLEMENTATION
    // =========================================================================

    public void clearAllTickets() {
        reset();
    }
    
    @Override
    public String getPrinterName() {
        return printerName;
    }
    
    @Override
    public void printLogo() {   
        // No logo implementation needed for screen preview
    }

    @Override
    public String getPrinterDescription() {
        return null;
    }       

    @Override
    public JComponent getPrinterComponent() {
        return this;
    }

    @Override
    public void reset() {
        currentTicket = null;
        ticketContainer.removeAllTickets();
    }
    
    @Override
    public void beginReceipt() {
        currentTicket = new BasicTicketForScreen();
    }

    @Override
    public void printImage(BufferedImage image) {
        if (currentTicket != null) {
            currentTicket.printImage(image);
        }
    }

    @Override
    public void printBarCode(String type, String position, String code) {
        if (currentTicket != null) {
            currentTicket.printBarCode(type, position, code);
        }
    }

    @Override
    public void printQRCode(String code, int size, char errorCorrection) {
        if (currentTicket != null) {
            currentTicket.printQRCode(code, size, errorCorrection);
        }
    }

    @Override
    public void beginLine(int textSize) {
        if (currentTicket != null) {
            currentTicket.beginLine(textSize);
        }
    }

    @Override
    public void printText(int style, String text) {
        if (currentTicket != null) {
            currentTicket.printText(style, text);
        }
    }

    @Override
    public void endLine() {
        if (currentTicket != null) {
            currentTicket.endLine();
        }
    } 

    @Override
    public void endReceipt() {
        if (currentTicket != null) {
            final BasicTicket ticketToRender = currentTicket;
            currentTicket = null;
            
            // Garante que o componente de interface é criado e adicionado na EDT
            if (SwingUtilities.isEventDispatchThread()) {
                ticketContainer.addTicket(new JTicket(ticketToRender));
            } else {
                SwingUtilities.invokeLater(() -> ticketContainer.addTicket(new JTicket(ticketToRender)));
            }
        }
    }
    
    @Override
    public void openDrawer() {
        Toolkit.getDefaultToolkit().beep();
    }     
       
    /** 
     * Initializes the form components.
     */
    private void initComponents() {
        setLayout(new java.awt.BorderLayout());

        // Inicializa o ScrollPane passando logo o ticketContainer como Viewport
        m_jScrollView = new javax.swing.JScrollPane(ticketContainer);
        m_jScrollView.setFont(new java.awt.Font("Arial", 0, 12));
        
        // 1. Desativa scrollbar horizontal (força wrap vertical)
        m_jScrollView.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        // 2. Ativa scrollbar vertical automática quando o conteúdo ultrapassar a janela
        m_jScrollView.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        // 3. Velocidade suave de scroll
        m_jScrollView.getVerticalScrollBar().setUnitIncrement(16);
        
        add(m_jScrollView, java.awt.BorderLayout.CENTER);
    }
}