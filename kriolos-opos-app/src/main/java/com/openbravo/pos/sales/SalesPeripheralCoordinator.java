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
import com.openbravo.pos.forms.DataLogicSales;
import com.openbravo.pos.forms.DataLogicSystem;
import com.openbravo.pos.hardware.PosHardwareManager;
import com.openbravo.pos.printer.TicketParser;
import com.openbravo.pos.printer.TicketPrinterException;
import com.openbravo.pos.reports.PrintReportUtils;
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
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
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
    private final Function<String, String> textResourceResolver;
    private final Function<String, String> xmlResourceResolver;

    public SalesPeripheralCoordinator(AppView app, DataLogicSystem dlSystem) {
        this(app, dlSystem,
                app != null ? app.createTicketParser() : null,
                dlSystem != null ? dlSystem::getResourceAsText : key -> null,
                dlSystem != null ? dlSystem::getResourceAsXML : key -> null);
    }

    SalesPeripheralCoordinator(AppView app, DataLogicSystem dlSystem, TicketParser ticketParser,
                               Function<String, String> textResourceResolver,
                               Function<String, String> xmlResourceResolver) {
        this.app = Objects.requireNonNull(app, "AppView cannot be null");
        this.dlSystem = dlSystem;
        this.ticketParser = ticketParser;
        this.textResourceResolver = textResourceResolver != null ? textResourceResolver : (dlSystem != null ? dlSystem::getResourceAsText : key -> null);
        this.xmlResourceResolver = xmlResourceResolver != null ? xmlResourceResolver : (dlSystem != null ? dlSystem::getResourceAsXML : key -> null);
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
                String resourcePrintTemplate = xmlResourceResolver.apply(resourceName);
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
        String sresource = xmlResourceResolver.apply(resourceName);
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
     * Executes the remote order printing script (script.SendOrder) to dispatch items to kitchen/bar printers.
     *
     * @param ticket the ticket whose order is being sent
     * @param place the ticket extension / table name
     * @param taxeslogic taxes logic for tax calculations
     * @param taxesIncluded whether taxes are included in prices
     * @param warrantyPrint whether warranty is included
     * @param pickupId human-readable pickup ID
     * @param salesContext sales UI facade for script execution
     * @return true if script executed without error
     */
    public boolean sendRemoteOrder(TicketInfo ticket, String place, TaxesLogic taxeslogic,
                                  boolean taxesIncluded, boolean warrantyPrint, String pickupId,
                                  Object salesContext) {
        
        /* Remote Orders Display */
        displayRemoteOrder(ticket, place, null);
        
        /* Remote Orders Display */
        String scriptId = "script.SendOrder";
        try {
            String rScript = textResourceResolver.apply(scriptId);
            if (rScript == null) {
                LOGGER.log(Level.WARNING, "Resource not found for script: " + scriptId);
                return false;
            }
            ScriptEngine scriptEngine = ScriptFactory.getScriptEngine(ScriptFactory.BEANSHELL);
            scriptEngine.put("ticket", ticket);
            scriptEngine.put("place", place);
            scriptEngine.put("taxes", ticket != null ? ticket.getTaxLines() : null);
            scriptEngine.put("taxeslogic", taxeslogic);
            scriptEngine.put("user", app.getAppUserView() != null ? app.getAppUserView().getUser() : null);
            scriptEngine.put("sales", salesContext);
            scriptEngine.put("taxesinc", taxesIncluded);
            scriptEngine.put("warranty", warrantyPrint);
            scriptEngine.put("pickupid", pickupId);

            scriptEngine.eval(rScript);
            return true;
        } catch (ScriptException ex) {
            LOGGER.log(Level.WARNING, "Exception on executing script: " + scriptId, ex);
            return false;
        }
    }

    /**
     * Resolves the remote order identifier prioritizing customer name, ticketExt, then pickupString.
     */
    public String resolveRemoteOrderId(TicketInfo ticket, String ticketExt, String pickupString) {
        if (ticket != null && ticket.getCustomer() != null && ticket.getCustomer().getName() != null) {
            return ticket.getCustomer().getName();
        } else if (ticketExt != null) {
            return ticketExt;
        } else if (pickupString != null) {
            return pickupString;
        } else {
            return ticket != null ? Integer.toString(ticket.getPickupId()) : "";
        }
    }

    /**
     * Dispatches remote order display updates to Kitchen/Bar video display systems.
     */
    private void displayRemoteOrder(TicketInfo ticket, String ticketExt, String display) {
        createRemoteOrderDisplay(ticket, ticketExt).remoteOrderDisplay(display);
    }

    /**
     * Formats a ticket's pickup ID string according to the configured till pickup size.
     *
     * @param ticket the ticket
     * @param pickupSizeConfig configured till pickup size
     * @return padded pickup ID string, or "0" if ticket is null
     */
    public static String formatPickupId(TicketInfo ticket, String pickupSizeConfig) {
        if (ticket == null) {
            return "0";
        }
        String tmpPickupId = Integer.toString(ticket.getPickupId());
        if (pickupSizeConfig != null && !pickupSizeConfig.isBlank()) {
            try {
                int size = Integer.parseInt(pickupSizeConfig.trim());
                if (size >= tmpPickupId.length()) {
                    return String.format("%0" + size + "d", ticket.getPickupId());
                }
            } catch (NumberFormatException ignored) {
                // Return unpadded pickup ID if configuration is invalid
            }
        }
        return tmpPickupId;
    }

    private RemoteOrderDisplayService createRemoteOrderDisplay(TicketInfo ticket, String ticketExt) {
        return new RemoteOrderDisplayService(app, ticket, ticketExt);
    }

    /**
     * Renders and dispatches a Jasper report for the given ticket.
     *
     * @param printerName printer device name
     * @param resourcefile Jasper report resource file
     * @param ticket the active ticket
     * @param ticketext ticket place / table extension
     * @param taxeslogic active taxes calculation logic
     * @param parent parent UI component for alerts
     * @return true if report was dispatched successfully
     */
    public boolean printReport(String printerName, String resourcefile, TicketInfo ticket, String ticketext,
                               TaxesLogic taxeslogic, Component parent) {
        try {
            Map<String, Object> reportParams = new HashMap<>();
            String reportBundleName = resourcefile + ".properties";
            try {
                reportParams.put("REPORT_RESOURCE_BUNDLE", ResourceBundle.getBundle(reportBundleName));
            } catch (MissingResourceException ex) {
                LOGGER.log(Level.WARNING, "Exception on set report bundle file: " + reportBundleName, ex);
            }
            reportParams.put("TAXESLOGIC", taxeslogic);

            Map<String, Object> reportFields = new HashMap<>();
            reportFields.put("TICKET", ticket);
            reportFields.put("PLACE", ticketext);

            PrintReportUtils.printReport(printerName, resourcefile, reportParams, reportFields);
            return true;
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Exception on print report with resource file: " + resourcefile, ex);
            if (parent != null) {
                new MessageInf(MessageInf.SGN_WARNING,
                        AppLocal.getIntString("message.cannotloadreport") + "\n" + resourcefile, ex).show(parent);
            }
            return false;
        }
    }

    @FunctionalInterface
    public interface TicketLoader {
        TicketInfo load(int ticketType) throws BasicException;
    }

    /**
     * Loads the last completed ticket, recalculates taxes, and dispatches it to the receipt printer.
     *
    /**
     * Loads the last completed ticket using {@link TicketLifecycleService}, recalculates taxes, and dispatches it to the receipt printer.
     *
     * @param parent UI component for alerts
     * @param ticketLifecycleService service for ticket retrieval
     * @param taxeslogic active taxes calculation logic
     * @param ticketPrinter consumer to print the retrieved ticket
     * @param notifier consumer to notify user
     * @return an {@link Optional} containing the reprinted ticket if successful
     */
    public Optional<TicketInfo> reprintLastTicket(Component parent, TicketLifecycleService ticketLifecycleService, TaxesLogic taxeslogic,
                                                  BiConsumer<String, TicketInfo> ticketPrinter,
                                                  Consumer<String> notifier) {
        return reprintLastTicket(parent, ticketLifecycleService != null ? ticketLifecycleService::loadLastTicket : null, taxeslogic, ticketPrinter, notifier);
    }

    /**
     * @deprecated Use {@link #reprintLastTicket(Component, TicketLifecycleService, TaxesLogic, BiConsumer, Consumer)} instead.
     */
    @Deprecated
    public Optional<TicketInfo> reprintLastTicket(Component parent, DataLogicSales dlSales, TaxesLogic taxeslogic,
                                                  BiConsumer<String, TicketInfo> ticketPrinter,
                                                  Consumer<String> notifier) {
        return reprintLastTicket(parent, dlSales != null ? dlSales::loadLastTicket : null, taxeslogic, ticketPrinter, notifier);
    }

    /**
     * Loads the last completed ticket using the provided loader, recalculates taxes, and dispatches it to the receipt printer.
     *
     * @param parent parent UI component for alerts
     * @param ticketLoader loader for ticket retrieval
     * @param taxeslogic active taxes calculation logic
     * @param ticketPrinter consumer to print the retrieved ticket
     * @param notifier consumer to notify user
     * @return an {@link Optional} containing the reprinted ticket if successful
     */
    public Optional<TicketInfo> reprintLastTicket(Component parent, TicketLoader ticketLoader, TaxesLogic taxeslogic,
                                                  BiConsumer<String, TicketInfo> ticketPrinter,
                                                  Consumer<String> notifier) {
        if (ticketLoader == null) {
            return Optional.empty();
        }
        try {
            int ticketType = 0;
            TicketInfo ticketInfo = ticketLoader.load(ticketType);
            if (ticketInfo == null) {
                if (parent != null) {
                    new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.notexiststicket")).show(parent);
                }
                return Optional.empty();
            }

            if (taxeslogic != null) {
                try {
                    taxeslogic.calculateTaxes(ticketInfo);
                } catch (TaxesException ex) {
                    LOGGER.log(Level.WARNING, "Exception on calculate taxes for reprint: ", ex);
                }
            }

            if (ticketPrinter != null) {
                ticketPrinter.accept("Printer.ReprintTicket", ticketInfo);
            }
            if (notifier != null) {
                notifier.accept("'Printer.reprint.last.ticket'");
            }
            return Optional.of(ticketInfo);
        } catch (BasicException ex) {
            LOGGER.log(Level.WARNING, "Exception on load last ticket for reprint: ", ex);
            if (parent != null) {
                new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotloadticket"), ex).show(parent);
            }
            return Optional.empty();
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
