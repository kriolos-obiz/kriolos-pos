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

import com.openbravo.basic.BasicException;
import com.openbravo.data.gui.MessageInf;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.DataLogicSystem;
import com.openbravo.pos.hardware.PosHardwareManager;
import com.openbravo.pos.printer.TicketParser;
import com.openbravo.pos.printer.TicketPrinterException;
import com.openbravo.pos.scripting.ScriptEngine;
import com.openbravo.pos.scripting.ScriptException;
import com.openbravo.pos.scripting.ScriptFactory;
import com.openbravo.pos.ticket.ProductInfoExt;
import com.openbravo.pos.ticket.TicketInfo;
import com.openbravo.pos.ticket.TicketLineInfo;
import com.openbravo.pos.util.NotifyUtils;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Font;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.swing.event.ListSelectionEvent;

/**
 * Coordinates peripheral hardware interactions for sales screens, including
 * customer displays (pole display / advanced secondary monitor), weighing scales,
 * and ticket printers.
 */
public class SalesPeripheralCoordinator {

    private static final Logger LOGGER = System.getLogger(SalesPeripheralCoordinator.class.getName());

    private final AppView app;
    private final DataLogicSystem dlSystem;
    private final TicketParser ticketParser;

    public SalesPeripheralCoordinator(AppView app, DataLogicSystem dlSystem) {
        this.app = Objects.requireNonNull(app, "AppView cannot be null");
        this.dlSystem = Objects.requireNonNull(dlSystem, "DataLogicSystem cannot be null");
        this.ticketParser = app.createTicketParser();
    }

    /**
     * Reads weight from the connected weighing scale.
     *
     * @param parent UI component for displaying warning dialogs on failure
     * @return the measured weight, or {@code null} if scale reading was aborted/empty
     */
    public Double readWeight(Component parent) {
        if (!app.hasScale()) {
            NotifyUtils.beep();
            return null;
        }

        try {
            return app.readWeight();
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Exception on read product SCALE: ", ex);
            NotifyUtils.beep();
            new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.noweight"), ex).show(parent);
            return null;
        }
    }

    /**
     * Updates the pole / customer display with the current ticket line, or clears it if null.
     *
     * @param oLine the active line, or {@code null} to clear the display
     * @param parent UI parent for error messages
     */
    public void updateCustomerDisplay(TicketLineInfo oLine, Component parent) {
        String resourceName = "Printer.TicketLine";
        if (oLine == null) {
            var deviceTicket = app.getDeviceTicket();
            if (deviceTicket != null && deviceTicket.getDeviceDisplay() != null) {
                deviceTicket.getDeviceDisplay().clearVisor();
            }
        } else {
            try {
                ScriptEngine script = ScriptFactory.getScriptEngine(ScriptFactory.VELOCITY);
                script.put("ticketline", oLine);
                String resourcePrintTemplate = dlSystem.getResourceAsXML(resourceName);
                if (resourcePrintTemplate != null) {
                    String generatedPrintContent = script.eval(resourcePrintTemplate).toString();
                    ticketParser.printTicket(generatedPrintContent);
                }
            } catch (ScriptException | TicketPrinterException ex) {
                LOGGER.log(Level.WARNING, "Exception execute visor ticket line with resource name: " + resourceName, ex);
                new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotprintline"), ex).show(parent);
            }
        }
    }

    /**
     * Renders and dispatches a ticket print job using a Velocity resource template.
     *
     * @param resourceName the XML resource name (e.g. Printer.Ticket, Printer.TicketTotal)
     * @param ticket the ticket to print
     * @param ticketext additional place/table or context text
     * @param taxeslogic active taxes calculation logic
     * @param warrantyPrint true if warranty slip should be appended
     * @param pickupId human-readable pickup identifier
     * @param parent parent component for UI notifications
     * @return true if printing completed without error
     */
    public boolean printTicket(String resourceName, TicketInfo ticket, String ticketext,
                               TaxesLogic taxeslogic, boolean warrantyPrint, String pickupId, Component parent) {
        LOGGER.log(Level.INFO, "Reading resource id: " + resourceName);
        String sresource = dlSystem.getResourceAsXML(resourceName);
        if (sresource == null) {
            LOGGER.log(Level.WARNING, "NOTFOUND content for resource id: " + resourceName);
            new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotprintticket")).show(parent);
            return false;
        }

        String processTemplated = "";
        try {
            ScriptEngine script = ScriptFactory.getScriptEngine(ScriptFactory.VELOCITY);
            script.put("taxes", ticket != null ? ticket.getTaxLines() : null);
            script.put("taxeslogic", taxeslogic);
            script.put("ticket", ticket);
            script.put("place", ticketext);
            script.put("warranty", warrantyPrint);
            script.put("pickupid", pickupId);

            processTemplated = script.eval(sresource).toString();
            ticketParser.printTicket(processTemplated, ticket);
            return true;
        } catch (ScriptException | TicketPrinterException ex) {
            LOGGER.log(Level.WARNING, "Exception on processing/Print resource id: " + resourceName, ex);
            LOGGER.log(Level.DEBUG, "Exception PROCESSED TEMPLATE: \n\r+++++++++++++\n\r "
                    + processTemplated + "\n\r+++++++++++++\n\r");
            new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotprintticket"), ex).show(parent);
            return false;
        }
    }

    /**
     * Initializes advanced customer display features (line list replication & product image preview).
     */
    public void setupAdvancedDisplayListener(JTicketLines sourceLines,
                                             Supplier<TicketInfo> ticketSupplier,
                                             Function<String, ProductInfoExt> productResolver) {
        var deviceTicket = app.getDeviceTicket();
        if (deviceTicket == null) {
            return;
        }
        var deviceDisplay = deviceTicket.getDeviceDisplay();
        if (deviceDisplay != null && PosHardwareManager.isAdvanceDisplay(deviceDisplay)) {
            JTicketLines secondaryLines = new JTicketLines(dlSystem.getResourceAsXML(TicketConstants.RES_TICKET_LINES));
            secondaryLines.setTicketTableFont(new Font("Arial", Font.PLAIN, 18));

            sourceLines.addListSelectionListener((ListSelectionEvent e) -> {
                EventQueue.invokeLater(() -> {
                    var currentTicketDevice = app.getDeviceTicket();
                    if (currentTicketDevice == null) {
                        return;
                    }
                    var currentDisplay = currentTicketDevice.getDeviceDisplay();
                    int ticketLineIndex = sourceLines.getSelectedIndex();
                    TicketInfo currentTicket = ticketSupplier.get();

                    // Feature 1: Product Image display
                    if (PosHardwareManager.hasFeature(currentDisplay, 1) && !e.getValueIsAdjusting()) {
                        if (ticketLineIndex >= 0 && currentTicket != null && ticketLineIndex < currentTicket.getLinesCount()) {
                            try {
                                String sProductId = currentTicket.getLine(ticketLineIndex).getProductID();
                                if (sProductId != null) {
                                    ProductInfoExt prod = productResolver.apply(sProductId);
                                    if (prod != null) {
                                        PosHardwareManager.setProductImage(currentDisplay, prod.getImage());
                                    }
                                }
                            } catch (Exception ex) {
                                LOGGER.log(Level.WARNING, "Error setting product image on customer display: ", ex);
                            }
                        }
                    }

                    // Feature 2: Line Table display
                    if (PosHardwareManager.hasFeature(deviceDisplay, 2)) {
                        secondaryLines.clearTicketLines();
                        if (currentTicket != null) {
                            for (int j = 0; j < currentTicket.getLinesCount(); j++) {
                                secondaryLines.insertTicketLine(j, currentTicket.getLine(j));
                            }
                        }
                        secondaryLines.setSelectedIndex(ticketLineIndex);
                        PosHardwareManager.setTicketLines(deviceDisplay, secondaryLines);
                    }
                });
            });
        }
    }

    public TicketParser getTicketParser() {
        return ticketParser;
    }
}
