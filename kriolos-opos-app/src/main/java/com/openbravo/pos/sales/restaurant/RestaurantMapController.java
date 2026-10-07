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
//    along with this program.  If not, see <http://www.gnu.org/licenses/>.
package com.openbravo.pos.sales.restaurant;

import com.openbravo.basic.BasicException;
import com.openbravo.data.gui.MessageInf;
import com.openbravo.pos.customers.CustomerInfo;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.DataLogicSystem;
import com.openbravo.pos.sales.DataLogicReceipts;
import com.openbravo.pos.sales.SharedTicketService;
import com.openbravo.pos.sales.TicketsEditor;
import com.openbravo.pos.ticket.TicketInfo;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 * Coordinates restaurant table state, tickets, persistence operations, and concurrency locks.
 */
public class RestaurantMapController {

    private static final System.Logger LOGGER = System.getLogger(RestaurantMapController.class.getName());
    public static final String LOCKED_STATE = "locked";

    private final AppView appView;
    private final TicketsEditor panelTicket;
    private final SharedTicketService sharedTicketService;
    private final DataLogicSystem dlSystem;
    private final PlaceService placeService;

    private List<Floor> floorList = Collections.emptyList();
    private List<Place> placeList = Collections.emptyList();

    private Place placeCurrent;
    private Place placeClipboard;
    private CustomerInfo customerInfo;

    public RestaurantMapController(AppView appView, TicketsEditor panelTicket) {
        this(appView, panelTicket,
                appView != null ? appView.getBean(SharedTicketService.class) : null,
                appView != null ? appView.getBean(DataLogicSystem.class) : null,
                appView != null && appView.getSession() != null ? new PlaceServiceImpl(appView.getSession()) : null);
    }

    /**
     * @deprecated Use {@link #RestaurantMapController(AppView, TicketsEditor, SharedTicketService, DataLogicSystem, PlaceService)} instead.
     */
    @Deprecated
    public RestaurantMapController(AppView appView, TicketsEditor panelTicket,
                            DataLogicReceipts dlReceipts, DataLogicSystem dlSystem,
                            PlaceService placeService) {
        this(appView, panelTicket, (SharedTicketService) dlReceipts, dlSystem, placeService);
    }

    public RestaurantMapController(AppView appView, TicketsEditor panelTicket,
                            SharedTicketService sharedTicketService, DataLogicSystem dlSystem,
                            PlaceService placeService) {
        this.appView = appView;
        this.panelTicket = panelTicket;
        this.sharedTicketService = sharedTicketService;
        this.dlSystem = dlSystem;
        this.placeService = placeService;
    }

    public PlaceService getPlaceService() {
        return placeService;
    }

    public SharedTicketService getSharedTicketService() {
        return sharedTicketService;
    }

    /**
     * @deprecated Use {@link #getSharedTicketService()} instead.
     */
    @Deprecated
    public DataLogicReceipts getDataLogicReceipts() {
        return sharedTicketService instanceof DataLogicReceipts ? (DataLogicReceipts) sharedTicketService : null;
    }

    public List<Floor> getFloorList() {
        return floorList;
    }

    public List<Place> getPlaceList() {
        return placeList;
    }

    public Place getPlaceCurrent() {
        return placeCurrent;
    }

    public void setPlaceCurrent(Place placeCurrent) {
        this.placeCurrent = placeCurrent;
    }

    public Place getPlaceClipboard() {
        return placeClipboard;
    }

    public void setPlaceClipboard(Place placeClipboard) {
        this.placeClipboard = placeClipboard;
    }

    public CustomerInfo getCustomerInfo() {
        return customerInfo;
    }

    public void setCustomerInfo(CustomerInfo customerInfo) {
        this.customerInfo = customerInfo;
    }

    /**
     * Loads floors, places, and current shared ticket occupancy from database.
     */
    public void loadData(Component parent) {
        Set<String> ticketPlaceIds = new HashSet<>();
        try {
            sharedTicketService.getSharedTicketList().forEach(ticket -> ticketPlaceIds.add(ticket.getId()));
        }
        catch (BasicException ex) {
            LOGGER.log(System.Logger.Level.WARNING, "Exception getting shared tickets: ", ex);
        }

        try {
            floorList = placeService.getFloors();
        }
        catch (Exception e) {
            MessageInf msg = new MessageInf(MessageInf.SGN_NOTICE, AppLocal.getIntString("message.cannotloadfloors"), e);
            msg.show(parent);
            floorList = new ArrayList<>();
        }

        try {
            placeList = placeService.getPlaces();
        }
        catch (BasicException ex) {
            LOGGER.log(System.Logger.Level.WARNING, "Exception getting places: ", ex);
            placeList = new ArrayList<>();
        }

        for (Place pl : placeList) {
            pl.setPeople(ticketPlaceIds.contains(pl.getId()));
        }
    }

    public TicketInfo getTicketInfo(Place place) {
        if (place == null) {
            return null;
        }
        try {
            return sharedTicketService.getSharedTicket(place.getId());
        }
        catch (BasicException ex) {
            LOGGER.log(System.Logger.Level.WARNING, "Exception fetching shared ticket: ", ex);
            return null;
        }
    }

    public void setActivePlace(Place place, TicketInfo ticket) {
        this.placeCurrent = place;
        syncTableDetails(place, ticket);
        panelTicket.setActiveTicket(ticket, placeCurrent.getName());

        try {
            sharedTicketService.lockSharedTicket(placeCurrent.getId(), LOCKED_STATE);
        }
        catch (BasicException ex) {
            Logger.getLogger(RestaurantMapController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void syncTableDetails(Place place, TicketInfo ticket) {
        if (place == null || ticket == null) {
            return;
        }
        String tableName = place.getName();
        if (!ticket.getOldTicket()) {
            placeService.setTicketIdInTable(ticket.getId(), tableName);
        }

        if (Boolean.parseBoolean(appView.getProperties().getProperty("table.showcustomerdetails"))) {
            String custname = placeService.getCustomerNameInTable(tableName);
            if (ticket.getCustomer() != null && (custname == null || custname.isBlank())) {
                placeService.setCustomerNameInTable(ticket.getCustomer().getName(), tableName);
            }
        }

        if (Boolean.parseBoolean(appView.getProperties().getProperty("table.showwaiterdetails"))) {
            String waiter = placeService.getWaiterNameInTable(tableName);
            if (waiter == null || waiter.isBlank()) {
                if (appView.getAppUserView() != null && appView.getAppUserView().getUser() != null) {
                    placeService.setWaiterNameInTable(appView.getAppUserView().getUser().getName(), tableName);
                }
            }
        }

        if (Boolean.TRUE.equals(placeService.getTableMovedFlag(ticket.getId()))) {
            placeService.moveCustomer(tableName, ticket.getId());
        }
    }

    public void syncCustomerInTable(String customerName, String ticketId) {
        if (ticketId != null) {
            placeService.setCustomerNameInTableByTicketId(customerName, ticketId);
        }
    }

    public void clearTable(String tableName) {
        if (tableName != null) {
            placeService.clearCustomerNameInTable(tableName);
            placeService.clearWaiterNameInTable(tableName);
            placeService.clearTicketIdInTable(tableName);
        }
    }

    /**
     * Handles table selection, unlocking/locking, merge, or move.
     *
     * @return true if a table was opened/made active, false otherwise.
     */
    public boolean handleTableSelection(Place place, Component parent) {
        if (placeClipboard == null) {
            return handleDirectTableSelection(place, parent);
        } else {
            return handleClipboardMergeOrMove(place, parent);
        }
    }

    private boolean handleDirectTableSelection(Place place, Component parent) {
        TicketInfo ticket = getTicketInfo(place);
        if (ticket == null) {
            ticket = new TicketInfo();
            ensureTicketUser(ticket);
            try {
                sharedTicketService.insertSharedTicket(place.getId(), ticket, ticket.getPickupId());
            }
            catch (BasicException ex) {
                LOGGER.log(System.Logger.Level.WARNING, "Exception creating ticket: ", ex);
                new MessageInf(ex).show(parent);
            }
            place.setPeople(true);
            setActivePlace(place, ticket);
            return true;
        }

        String lockState = null;
        try {
            lockState = sharedTicketService.getLockState(place.getId(), lockState);
            if (LOCKED_STATE.equals(lockState)) {
                JOptionPane.showMessageDialog(parent, AppLocal.getIntString("message.sharedticketlock"));
                if (appView.hasPermission("sales.Override")) {
                    int res = JOptionPane.showConfirmDialog(parent,
                            AppLocal.getIntString("message.sharedticketlockoverride"),
                            AppLocal.getIntString("title.editor"),
                            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    if (res == JOptionPane.YES_OPTION) {
                        place.setPeople(true);
                        placeClipboard = null;
                        setActivePlace(place, ticket);
                        return true;
                    }
                }
                return false;
            }

            String currentUser = appView.getAppUserView().getUser().getName();
            String ticketUser = place.getWaiter();
            if (currentUser.equals(ticketUser) || appView.hasPermission("sales.Override")) {
                place.setPeople(true);
                placeClipboard = null;
                setActivePlace(place, ticket);
                return true;
            } else {
                JOptionPane.showMessageDialog(parent,
                        AppLocal.getIntString("message.sharedticket"),
                        AppLocal.getIntString("title.editor"),
                        JOptionPane.OK_OPTION);
                return false;
            }
        }
        catch (BasicException ex) {
            LOGGER.log(System.Logger.Level.WARNING, "Exception checking lock: ", ex);
            return false;
        }
    }

    private boolean handleClipboardMergeOrMove(Place place, Component parent) {
        TicketInfo ticketClip = getTicketInfo(placeClipboard);
        if (ticketClip == null) {
            new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.tableempty")).show(parent);
            placeClipboard.setPeople(false);
            placeClipboard = null;
            customerInfo = null;
            return false;
        }

        if (placeClipboard == place) {
            Place placeClip = placeClipboard;
            placeClipboard = null;
            customerInfo = null;
            setActivePlace(placeClip, ticketClip);
            return true;
        }

        if (place.hasPeople()) {
            return handleTableMerge(place, ticketClip, parent);
        } else {
            return handleTableMove(place, ticketClip, parent);
        }
    }

    private boolean handleTableMerge(Place place, TicketInfo ticketClip, Component parent) {
        TicketInfo targetTicket = getTicketInfo(place);
        if (targetTicket == null) {
            new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.tableempty")).show(parent);
            place.setPeople(false);
            return false;
        }

        int confirm = JOptionPane.showConfirmDialog(parent,
                AppLocal.getIntString("message.mergetablequestion"),
                AppLocal.getIntString("message.mergetable"),
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                placeClipboard.setPeople(false);
                if (targetTicket.getCustomer() == null) {
                    targetTicket.setCustomer(ticketClip.getCustomer());
                }
                ticketClip.getLines().forEach(targetTicket::addLine);
                sharedTicketService.updateRSharedTicket(place.getId(), targetTicket, targetTicket.getPickupId());
                sharedTicketService.deleteSharedTicket(placeClipboard.getId());
            }
            catch (BasicException ex) {
                LOGGER.log(System.Logger.Level.WARNING, "Exception merging tables: ", ex);
                new MessageInf(ex).show(parent);
            }

            String clipTicketId = ticketClip.getId();
            placeClipboard = null;
            customerInfo = null;
            placeService.clearCustomerNameInTable(placeService.getTableDetails(clipTicketId));
            placeService.clearWaiterNameInTable(placeService.getTableDetails(clipTicketId));
            placeService.clearTableMovedFlag(placeService.getTableDetails(clipTicketId));
            placeService.clearTicketIdInTable(placeService.getTableDetails(clipTicketId));

            setActivePlace(place, targetTicket);
            return true;
        } else {
            Place placeClip = placeClipboard;
            placeClipboard = null;
            customerInfo = null;
            setActivePlace(placeClip, ticketClip);
            return true;
        }
    }

    private boolean handleTableMove(Place place, TicketInfo ticketClip, Component parent) {
        TicketInfo existing = getTicketInfo(place);
        if (existing == null) {
            try {
                sharedTicketService.insertRSharedTicket(place.getId(), ticketClip, ticketClip.getPickupId());
                place.setPeople(true);
                sharedTicketService.deleteSharedTicket(placeClipboard.getId());
                placeClipboard.setPeople(false);
            }
            catch (BasicException ex) {
                LOGGER.log(System.Logger.Level.WARNING, "Exception moving ticket: ", ex);
                new MessageInf(ex).show(parent);
            }
            placeClipboard = null;
            customerInfo = null;
            setActivePlace(place, ticketClip);
            return true;
        } else {
            new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.tablefull")).show(parent);
            placeClipboard.setPeople(true);
            return false;
        }
    }

    /**
     * Releases active table ticket, updating and unlocking it in database.
     */
    public void releaseCurrentTicket(Component parent) {
        if (placeCurrent != null) {
            try {
                String lockState = null;
                lockState = sharedTicketService.getLockState(placeCurrent.getId(), lockState);
                sharedTicketService.getSharedTicket(placeCurrent.getId());

                if ("override".equals(lockState) || LOCKED_STATE.equals(lockState)) {
                    sharedTicketService.updateSharedTicket(placeCurrent.getId(),
                            panelTicket.getActiveTicket(),
                            panelTicket.getActiveTicket().getPickupId());
                    sharedTicketService.unlockSharedTicket(placeCurrent.getId(), null);
                    placeCurrent = null;
                } else {
                    JOptionPane.showMessageDialog(parent,
                            AppLocal.getIntString("message.sharedticketlockoverriden"),
                            AppLocal.getIntString("title.editor"),
                            JOptionPane.INFORMATION_MESSAGE);
                }
            }
            catch (BasicException ex) {
                LOGGER.log(System.Logger.Level.WARNING, "Exception releasing ticket: ", ex);
            }
        }
        panelTicket.setActiveTicket(null, null);
    }

    /**
     * Deletes active ticket and frees the place.
     */
    public void deleteCurrentTicket(Component parent) {
        if (placeCurrent != null) {
            String id = placeCurrent.getId();
            try {
                sharedTicketService.deleteSharedTicket(id);
            }
            catch (BasicException ex) {
                LOGGER.log(System.Logger.Level.WARNING, "Exception deleting ticket: ", ex);
                new MessageInf(ex).show(parent);
            }
            placeCurrent.setPeople(false);
            placeCurrent = null;
        }
        panelTicket.setActiveTicket(null, null);
    }

    /**
     * Prepares ticket to move, placing current table in clipboard.
     */
    public void moveCurrentTicket(Component parent) {
        if (placeCurrent != null) {
            try {
                sharedTicketService.updateRSharedTicket(placeCurrent.getId(),
                        panelTicket.getActiveTicket(), panelTicket.getActiveTicket().getPickupId());
            }
            catch (BasicException ex) {
                LOGGER.log(System.Logger.Level.WARNING, "Exception moving ticket: ", ex);
                new MessageInf(ex).show(parent);
            }
            placeClipboard = placeCurrent;
            customerInfo = null;
            placeCurrent = null;
        }
        panelTicket.setActiveTicket(null, null);
    }

    /**
     * Updates place coordinates in database for layout design mode.
     */
    public void savePlacesLayout() {
        for (Place pl : placeList) {
            try {
                dlSystem.updatePlaces(pl.getX(), pl.getY(), pl.getId());
            }
            catch (BasicException ex) {
                LOGGER.log(System.Logger.Level.WARNING, "Exception updating place layout: ", ex);
            }
        }
    }

    private void ensureTicketUser(TicketInfo ticket) {
        if (ticket != null && ticket.getUser() == null) {
            if (appView != null && appView.getAppUserView() != null && appView.getAppUserView().getUser() != null) {
                ticket.setUser(appView.getAppUserView().getUser().getUserInfo());
            }
        }
    }
}
