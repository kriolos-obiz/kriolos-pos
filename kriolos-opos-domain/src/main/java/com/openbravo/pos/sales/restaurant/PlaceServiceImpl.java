/*
 * Copyright (C) 2026 KriolOS POS
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
package com.openbravo.pos.sales.restaurant;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.Datas;
import com.openbravo.data.loader.PreparedSentence;
import com.openbravo.data.loader.SerializerReadClass;
import com.openbravo.data.loader.SerializerReadInteger;
import com.openbravo.data.loader.SerializerReadString;
import com.openbravo.data.loader.SerializerWriteBasic;
import com.openbravo.data.loader.SerializerWriteString;
import com.openbravo.data.loader.Session;
import com.openbravo.data.loader.StaticSentence;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of PlaceService providing SQL-safe dining table floor plan management.
 */
public class PlaceServiceImpl implements PlaceService {

    private static final Logger LOGGER = Logger.getLogger(PlaceServiceImpl.class.getName());

    private final Session dbSession;

    public PlaceServiceImpl(Session session) {
        this.dbSession = session;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Floor> getFloors() throws BasicException {
        return (List<Floor>) (List<?>) new StaticSentence(
                dbSession,
                "SELECT ID, NAME, IMAGE FROM floors ORDER BY NAME",
                null,
                new SerializerReadClass<>(Floor.class))
                .list();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Place> getPlaces() throws BasicException {
        return (List<Place>) (List<?>) new StaticSentence(
                dbSession,
                "SELECT ID, NAME, SEATS, X, Y, FLOOR, CUSTOMER, WAITER, TICKETID, TABLEMOVED FROM places ORDER BY FLOOR",
                null,
                new SerializerReadClass<>(Place.class))
                .list();
    }

    @Override
    public void moveCustomer(MoveCustomerCommand command) {
        Objects.requireNonNull(command, "command must not be null");
        String newTable = command.newTable();
        String ticketID = command.ticketId();

        String oldTable = getTableDetails(ticketID);

        if (countTicketIdInTable(ticketID) > 1) {
            setCustomerNameInTable(getCustomerNameInTable(oldTable), newTable);
            setWaiterNameInTable(getWaiterNameInTable(oldTable), newTable);
            setTicketIdInTable(ticketID, newTable);

            oldTable = getTableMovedName(ticketID);
            boolean hasUpdated = oldTable == null ? newTable != null : !oldTable.equals(newTable);
            if ((oldTable != null) && (hasUpdated)) {
                clearCustomerNameInTable(oldTable);
                clearWaiterNameInTable(oldTable);
                clearTicketIdInTable(oldTable);
                clearTableMovedFlag(oldTable);
            } else {
                oldTable = getTableMovedName(ticketID);
                clearTableMovedFlag(oldTable);
            }
        }
    }

    @Override
    public void setCustomerNameInTable(String custName, String tableName) {
        try {
            new PreparedSentence(dbSession,
                    "UPDATE places SET CUSTOMER = ? WHERE NAME = ?",
                    new SerializerWriteBasic(new Datas[]{Datas.STRING, Datas.STRING}))
                    .exec(new Object[]{custName, tableName});
        } catch (BasicException e) {
            LOGGER.log(Level.SEVERE, "Exception setting customer name in table: " + tableName, e);
        }
    }

    @Override
    public void setCustomerNameInTableById(String custName, String tableID) {
        try {
            new PreparedSentence(dbSession,
                    "UPDATE places SET CUSTOMER = ? WHERE ID = ?",
                    new SerializerWriteBasic(new Datas[]{Datas.STRING, Datas.STRING}))
                    .exec(new Object[]{custName, tableID});
        } catch (BasicException e) {
            LOGGER.log(Level.SEVERE, "Exception setting customer name in table by ID: " + tableID, e);
        }
    }

    @Override
    public void setCustomerNameInTableByTicketId(String custName, String ticketID) {
        try {
            new PreparedSentence(dbSession,
                    "UPDATE places SET CUSTOMER = ? WHERE TICKETID = ?",
                    new SerializerWriteBasic(new Datas[]{Datas.STRING, Datas.STRING}))
                    .exec(new Object[]{custName, ticketID});
        } catch (BasicException e) {
            LOGGER.log(Level.SEVERE, "Exception setting customer name in table by ticketId: " + ticketID, e);
        }
    }

    @Override
    public String getCustomerNameInTable(String tableName) {
        try {
            String customerName = (String) new PreparedSentence(dbSession,
                    "SELECT customer FROM places WHERE NAME = ?",
                    SerializerWriteString.INSTANCE,
                    SerializerReadString.INSTANCE)
                    .find(tableName);
            return customerName != null ? customerName : "";
        } catch (BasicException e) {
            LOGGER.log(Level.SEVERE, "Exception getting customer name in table: " + tableName, e);
            return "";
        }
    }

    @Override
    public String getCustomerNameInTableById(String tableId) {
        try {
            String customer = (String) new PreparedSentence(dbSession,
                    "SELECT customer FROM places WHERE ID = ?",
                    SerializerWriteString.INSTANCE,
                    SerializerReadString.INSTANCE)
                    .find(tableId);
            return customer != null ? customer : "";
        } catch (BasicException e) {
            LOGGER.log(Level.SEVERE, "Exception getting customer name in table by id: " + tableId, e);
            return "";
        }
    }

    @Override
    public void clearCustomerNameInTable(String tableName) {
        try {
            new PreparedSentence(dbSession,
                    "UPDATE places SET CUSTOMER = null WHERE NAME = ?",
                    SerializerWriteString.INSTANCE)
                    .exec(tableName);
        } catch (BasicException e) {
            LOGGER.log(Level.SEVERE, "Exception clearing customer name in table: " + tableName, e);
        }
    }

    @Override
    public void clearCustomerNameInTableById(String tableID) {
        try {
            new PreparedSentence(dbSession,
                    "UPDATE places SET CUSTOMER = null WHERE ID = ?",
                    SerializerWriteString.INSTANCE)
                    .exec(tableID);
        } catch (BasicException e) {
            LOGGER.log(Level.SEVERE, "Exception clearing customer name in table by id: " + tableID, e);
        }
    }

    @Override
    public void setWaiterNameInTable(String waiterName, String tableName) {
        try {
            new PreparedSentence(dbSession,
                    "UPDATE places SET WAITER = ? WHERE NAME = ?",
                    new SerializerWriteBasic(new Datas[]{Datas.STRING, Datas.STRING}))
                    .exec(new Object[]{waiterName, tableName});
        } catch (BasicException e) {
            LOGGER.log(Level.SEVERE, "Exception setting waiter in table: " + tableName, e);
        }
    }

    @Override
    public void setWaiterNameInTableById(String waiterName, String tableID) {
        try {
            new PreparedSentence(dbSession,
                    "UPDATE places SET WAITER = ? WHERE ID = ?",
                    new SerializerWriteBasic(new Datas[]{Datas.STRING, Datas.STRING}))
                    .exec(new Object[]{waiterName, tableID});
        } catch (BasicException e) {
            LOGGER.log(Level.SEVERE, "Exception setting waiter in table by id: " + tableID, e);
        }
    }

    @Override
    public String getWaiterNameInTable(String tableName) {
        try {
            String waiter = (String) new PreparedSentence(dbSession,
                    "SELECT waiter FROM places WHERE NAME = ?",
                    SerializerWriteString.INSTANCE,
                    SerializerReadString.INSTANCE)
                    .find(tableName);
            return waiter != null ? waiter : "";
        } catch (BasicException e) {
            LOGGER.log(Level.SEVERE, "Exception getting waiter in table: " + tableName, e);
            return "";
        }
    }

    @Override
    public String getWaiterNameInTableById(String tableID) {
        try {
            String waiter = (String) new PreparedSentence(dbSession,
                    "SELECT waiter FROM places WHERE ID = ?",
                    SerializerWriteString.INSTANCE,
                    SerializerReadString.INSTANCE)
                    .find(tableID);
            return waiter != null ? waiter : "";
        } catch (BasicException e) {
            LOGGER.log(Level.SEVERE, "Exception getting waiter in table by id: " + tableID, e);
            return "";
        }
    }

    @Override
    public void clearWaiterNameInTable(String tableName) {
        try {
            new PreparedSentence(dbSession,
                    "UPDATE places SET WAITER = null WHERE NAME = ?",
                    SerializerWriteString.INSTANCE)
                    .exec(tableName);
        } catch (BasicException e) {
            LOGGER.log(Level.SEVERE, "Exception clearing waiter in table: " + tableName, e);
        }
    }

    @Override
    public void clearWaiterNameInTableById(String tableID) {
        try {
            new PreparedSentence(dbSession,
                    "UPDATE places SET WAITER = null WHERE ID = ?",
                    SerializerWriteString.INSTANCE)
                    .exec(tableID);
        } catch (BasicException e) {
            LOGGER.log(Level.SEVERE, "Exception clearing waiter in table by id: " + tableID, e);
        }
    }

    @Override
    public String getTicketIdInTable(String tableID) {
        try {
            String ticket = (String) new PreparedSentence(dbSession,
                    "SELECT TICKETID FROM places WHERE ID = ?",
                    SerializerWriteString.INSTANCE,
                    SerializerReadString.INSTANCE)
                    .find(tableID);
            return ticket != null ? ticket : "";
        } catch (BasicException e) {
            LOGGER.log(Level.SEVERE, "Exception getting ticketId in table by id: " + tableID, e);
            return "";
        }
    }

    @Override
    public void setTicketIdInTable(String ticketId, String tableName) {
        try {
            new PreparedSentence(dbSession,
                    "UPDATE places SET TICKETID = ? WHERE NAME = ?",
                    new SerializerWriteBasic(new Datas[]{Datas.STRING, Datas.STRING}))
                    .exec(new Object[]{ticketId, tableName});
        } catch (BasicException e) {
            LOGGER.log(Level.SEVERE, "Exception setting ticketId in table: " + tableName, e);
        }
    }

    @Override
    public void clearTicketIdInTable(String tableName) {
        try {
            new PreparedSentence(dbSession,
                    "UPDATE places SET TICKETID = null WHERE NAME = ?",
                    SerializerWriteString.INSTANCE)
                    .exec(tableName);
        } catch (BasicException e) {
            LOGGER.log(Level.SEVERE, "Exception clearing ticketId in table: " + tableName, e);
        }
    }

    @Override
    public void clearTicketIdInTableById(String tableID) {
        try {
            new PreparedSentence(dbSession,
                    "UPDATE places SET TICKETID = null WHERE ID = ?",
                    SerializerWriteString.INSTANCE)
                    .exec(tableID);
        } catch (BasicException e) {
            LOGGER.log(Level.SEVERE, "Exception clearing ticketId in table by id: " + tableID, e);
        }
    }

    @Override
    public Integer countTicketIdInTable(String ticketID) {
        try {
            Integer count = (Integer) new PreparedSentence(dbSession,
                    "SELECT COUNT(*) FROM places WHERE TICKETID = ?",
                    SerializerWriteString.INSTANCE,
                    SerializerReadInteger.INSTANCE)
                    .find(ticketID);
            return count != null ? count : 0;
        } catch (BasicException e) {
            LOGGER.log(Level.SEVERE, "Exception counting ticketId in places: " + ticketID, e);
            return 0;
        }
    }

    @Override
    public String getTableDetails(String ticketID) {
        try {
            String name = (String) new PreparedSentence(dbSession,
                    "SELECT NAME FROM places WHERE TICKETID = ?",
                    SerializerWriteString.INSTANCE,
                    SerializerReadString.INSTANCE)
                    .find(ticketID);
            return name != null ? name : "";
        } catch (BasicException e) {
            LOGGER.log(Level.SEVERE, "Exception getting table details for ticketId: " + ticketID, e);
            return "";
        }
    }

    @Override
    public void setTableMovedFlag(String tableID) {
        try {
            new PreparedSentence(dbSession,
                    "UPDATE places SET TABLEMOVED = 'true' WHERE ID = ?",
                    SerializerWriteString.INSTANCE)
                    .exec(tableID);
        } catch (BasicException e) {
            LOGGER.log(Level.SEVERE, "Exception setting table moved flag: " + tableID, e);
        }
    }

    @Override
    public String getTableMovedName(String ticketID) {
        try {
            return (String) new PreparedSentence(dbSession,
                    "SELECT NAME FROM places WHERE TICKETID = ? AND TABLEMOVED = 'true'",
                    SerializerWriteString.INSTANCE,
                    SerializerReadString.INSTANCE)
                    .find(ticketID);
        } catch (BasicException e) {
            LOGGER.log(Level.SEVERE, "Exception getting table moved name for ticketId: " + ticketID, e);
            return null;
        }
    }

    @Override
    public Boolean getTableMovedFlag(String ticketID) {
        try {
            String moved = (String) new PreparedSentence(dbSession,
                    "SELECT TABLEMOVED FROM places WHERE TICKETID = ?",
                    SerializerWriteString.INSTANCE,
                    SerializerReadString.INSTANCE)
                    .find(ticketID);
            return moved != null && ("true".equalsIgnoreCase(moved) || "1".equals(moved));
        } catch (BasicException e) {
            LOGGER.log(Level.SEVERE, "Exception getting table moved flag for ticketId: " + ticketID, e);
            return false;
        }
    }

    @Override
    public void clearTableMovedFlag(String tableID) {
        try {
            new PreparedSentence(dbSession,
                    "UPDATE places SET TABLEMOVED = 'false' WHERE NAME = ?",
                    SerializerWriteString.INSTANCE)
                    .exec(tableID);
        } catch (BasicException e) {
            LOGGER.log(Level.SEVERE, "Exception clearing table moved flag: " + tableID, e);
        }
    }

    @Override
    public void updatePlaces(UpdatePlacePositionCommand command) throws BasicException {
        Objects.requireNonNull(command, "command must not be null");
        new StaticSentence<>(this.dbSession,
                "UPDATE PLACES SET X = ?, Y = ? WHERE ID = ?",
                new SerializerWriteBasic(new Datas[]{Datas.INT, Datas.INT, Datas.STRING}))
                .exec(new Object[]{command.x(), command.y(), command.id()});
    }
}
