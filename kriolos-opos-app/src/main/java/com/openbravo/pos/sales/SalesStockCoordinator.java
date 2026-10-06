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
import com.openbravo.pos.forms.DataLogicSales;
import com.openbravo.pos.inventory.InventoryService;
import com.openbravo.pos.inventory.LocationInfo;
import com.openbravo.pos.inventory.ProductStock;
import com.openbravo.pos.pim.DataLogicPIM;
import com.openbravo.pos.pim.CategoryInfo;
import com.openbravo.pos.ticket.ProductInfoExt;
import com.openbravo.pos.ticket.TicketInfo;
import com.openbravo.pos.ticket.TicketLineInfo;
import com.openbravo.pos.util.NotifyUtils;
import java.awt.Component;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Coordinates product stock inspection, stock availability verification, and stock metadata
 * resolution for POS ticket lines.
 */
public class SalesStockCoordinator {

    private static final System.Logger LOGGER = System.getLogger(SalesStockCoordinator.class.getName());

    private final InventoryService inventoryService;
    private final DataLogicPIM dataLogicPIM;
    private final DataLogicSales dlSales;

    public SalesStockCoordinator(InventoryService inventoryService, DataLogicPIM dataLogicPIM, DataLogicSales dlSales) {
        this.inventoryService = inventoryService;
        this.dataLogicPIM = dataLogicPIM;
        this.dlSales = dlSales;
    }

    /**
     * Checks whether the given ticket line currently has positive units in stock at the specified location.
     *
     * @param line the ticket line to inspect
     * @param location the active warehouse/inventory location ID
     * @return true if stock record exists for the location and units > 0; false otherwise
     */
    public boolean isStockAvailable(TicketLineInfo line, String location) {
        if (line == null || line.getProductID() == null || location == null || inventoryService == null) {
            return false;
        }
        try {
            ProductStock productStock = inventoryService.getStock(line.getProductID(), location);
            if (productStock != null && location.equals(productStock.getLocation())) {
                Double units = productStock.getUnits();
                return units != null && units > 0;
            }
        } catch (BasicException ex) {
            LOGGER.log(System.Logger.Level.WARNING, "Error checking stock availability for product " + line.getProductID(), ex);
        }
        return false;
    }

    /**
     * Resolves comprehensive stock metrics and product details for the given ticket line.
     *
     * @param line the ticket line to inspect
     * @param location the active warehouse/inventory location ID
     * @return an {@link Optional} containing {@link ProductStockDetails} if stock exists for the location,
     *         or empty if no valid record exists
     */
    public Optional<ProductStockDetails> inspectStock(TicketLineInfo line, String location) {
        if (line == null || line.getProductID() == null || location == null || inventoryService == null) {
            return Optional.empty();
        }

        try {
            ProductStock productStock = inventoryService.getStock(line.getProductID(), location);
            if (productStock == null || !location.equals(productStock.getLocation())) {
                return Optional.empty();
            }

            Double pMin = productStock.getMinimum() != null ? productStock.getMinimum() : 0.0;
            Double pMax = productStock.getMaximum() != null ? productStock.getMaximum() : 0.0;
            Double pUnits = productStock.getUnits() != null ? productStock.getUnits() : 0.0;
            Date pMemoDate = productStock.getMemoDate();
            Double pPriceSell = (productStock.getPriceSell() != null && productStock.getPriceSell() > 0)
                    ? productStock.getPriceSell()
                    : line.getPrice();

            String productName = line.getProductName();
            String categoryName = null;
            String reference = null;
            String barcode = null;
            String locationName = location;

            if (dataLogicPIM != null) {
                try {
                    ProductInfoExt prod = dataLogicPIM.getProductInfo(line.getProductID());
                    if (prod != null) {
                        if (productName == null || productName.isBlank()) {
                            productName = prod.getName();
                        }
                        reference = prod.getReference();
                        barcode = prod.getCode();
                        if (prod.getCategoryID() != null) {
                            CategoryInfo cat = dataLogicPIM.getCategoryInfo(prod.getCategoryID());
                            if (cat != null) {
                                categoryName = cat.getName();
                            }
                        }
                    }
                } catch (BasicException ignored) {
                }

                if (categoryName == null && line.getProductCategoryID() != null) {
                    try {
                        CategoryInfo cat = dataLogicPIM.getCategoryInfo(line.getProductCategoryID());
                        if (cat != null) {
                            categoryName = cat.getName();
                        }
                    } catch (BasicException ignored) {
                    }
                }
            }

            if (dlSales != null) {
                try {
                    List<LocationInfo> locs = dlSales.getLocationsListAll();
                    if (locs != null) {
                        for (LocationInfo loc : locs) {
                            if (location.equals(loc.getID())) {
                                locationName = loc.getName();
                                break;
                            }
                        }
                    }
                } catch (Exception ignored) {
                }
            }

            return Optional.of(new ProductStockDetails(
                    productName,
                    categoryName,
                    reference,
                    barcode,
                    locationName,
                    pUnits,
                    pMin,
                    pMax,
                    pPriceSell,
                    pMemoDate
            ));

        } catch (BasicException ex) {
            LOGGER.log(System.Logger.Level.WARNING, "Error inspecting stock details for product " + line.getProductID(), ex);
            return Optional.empty();
        }
    }

    /**
     * Inspects stock for the given ticket line and displays either the stock details modal or
     * a notice message if the inventory location has no valid stock record.
     *
     * @param parent the parent Swing component
     * @param line the ticket line to inspect
     * @param location the active warehouse/inventory location ID
     * @return an {@link Optional} containing {@link ProductStockDetails} if resolved
     */
    public Optional<ProductStockDetails> showStockDetails(Component parent, TicketLineInfo line, String location) {
        Optional<ProductStockDetails> details = inspectStock(line, location);
        if (parent != null) {
            if (details.isPresent()) {
                ProductStockInfoPanel.show(parent, details.get());
            } else {
                new MessageInf(MessageInf.SGN_NOTICE, AppLocal.getIntString("message.location.current")).show(parent);
            }
        }
        return details;
    }

    /**
     * Checks stock availability for the given ticket line index and optionally displays stock details.
     * Triggers an audible alert if the ticket or line index is invalid.
     *
     * @param parent the parent Swing component (for modals/dialogs, null-safe for tests)
     * @param ticket the ticket containing lines
     * @param lineNumber the zero-based index of the line
     * @param location the active inventory location ID
     * @param showDialog whether to display the stock details modal or warning dialog
     * @return an {@link Optional} containing {@code true} if stock is available, {@code false} if out of stock,
     *         or {@code Optional.empty()} if the line index is invalid
     */
    public Optional<Boolean> checkAndShowStock(Component parent, TicketInfo ticket, int lineNumber, String location, boolean showDialog) {
        if (ticket == null || lineNumber < 0 || lineNumber >= ticket.getLinesCount()) {
            NotifyUtils.beep();
            return Optional.empty();
        }

        TicketLineInfo line = ticket.getLine(lineNumber);
        boolean inStock = isStockAvailable(line, location);

        if (showDialog) {
            showStockDetails(parent, line, location);
        }

        return Optional.of(inStock);
    }

    /**
     * Overload for checking stock availability for a ticket line by index.
     *
     * @param showDialog whether to display the stock details dialog
     * @param ticket the ticket
     * @param lineNumber the index of the line
     * @param location the inventory location ID
     * @return an {@link Optional} containing stock availability
     */
    public Optional<Boolean> isStockAvailable(boolean showDialog, TicketInfo ticket, int lineNumber, String location) {
        return checkAndShowStock(null, ticket, lineNumber, location, showDialog);
    }
}
