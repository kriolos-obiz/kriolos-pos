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

import com.openbravo.data.loader.Session;
import com.openbravo.pos.forms.AppView;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JDL
 */
public class RestaurantDBUtils {

    private static final Logger LOGGER = Logger.getLogger(RestaurantDBUtils.class.getName());

    private final Session dbSession;

    private final AppView appView;

    public RestaurantDBUtils(AppView oApp) {
        appView = oApp;
        dbSession = appView.getSession();
    }

    /**
     *
     * @param newTable
     * @param ticketID
     */
    public void moveCustomer(String newTable, String ticketID) {
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

    /**
     *
     * @param custName
     * @param tableName
     */
    public void setCustomerNameInTable(String custName, String tableName) {

        String sqlQuery = "UPDATE places SET CUSTOMER=? WHERE NAME=?";
        try (Connection con = dbSession.getConnection(); PreparedStatement pstmt = con.prepareStatement(sqlQuery);) {

            pstmt.setString(1, custName);
            pstmt.setString(2, tableName);
            pstmt.executeUpdate();
        }
        catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "", e);
        }
    }

    /**
     *
     * @param custName
     * @param tableID
     */
    public void setCustomerNameInTableById(String custName, String tableID) {
        String sqlQuery = "UPDATE places SET CUSTOMER=? WHERE ID=?";
        try (Connection con = dbSession.getConnection(); PreparedStatement pstmt = con.prepareStatement(sqlQuery);) {

            pstmt.setString(1, custName);
            pstmt.setString(2, tableID);
            pstmt.executeUpdate();
        }
        catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "", e);
        }
    }

    /**
     *
     * @param custName
     * @param ticketID
     */
    public void setCustomerNameInTableByTicketId(String custName, String ticketID) {
        String sqlQuery = "UPDATE places SET CUSTOMER=? WHERE TICKETID=?";
        try (Connection con = dbSession.getConnection(); PreparedStatement pstmt = con.prepareStatement(sqlQuery);) {
            pstmt.setString(1, custName);
            pstmt.setString(2, ticketID);
            pstmt.executeUpdate();
        }
        catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "", e);
        }
    }

    /**
     *
     * @param tableName
     * @return
     */
    public String getCustomerNameInTable(String tableName) {
        String customerName = "";
        try (Connection con = dbSession.getConnection()) {
            String sqlQuery = "SELECT customer FROM places WHERE NAME=?";
            try (PreparedStatement pstmt = con.prepareStatement(sqlQuery)) {
                pstmt.setString(1, tableName);

                try (ResultSet resultSet = pstmt.executeQuery()) {
                    if (resultSet.next()) {
                        customerName = resultSet.getString("CUSTOMER");
                    }
                }
            }
        }
        catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Exception get customer name in table: " + tableName, e);
        }

        return customerName;
    }

    /**
     *
     * @param tableId
     * @return
     */
    public String getCustomerNameInTableById(String tableId) {
        String sqlQuery = "SELECT customer FROM places WHERE ID=?";
        try (Connection con = dbSession.getConnection(); PreparedStatement pstmt = con.prepareStatement(sqlQuery);) {
            pstmt.setString(1, tableId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String customer = rs.getString("CUSTOMER");
                return (customer);
            }
        }
        catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "", e);
        }

        return "";
    }

    /**
     *
     * @param tableName
     */
    public void clearCustomerNameInTable(String tableName) {
        String sqlQuery = "UPDATE places SET CUSTOMER=null WHERE NAME=?";
        try (Connection con = dbSession.getConnection(); PreparedStatement pstmt = con.prepareStatement(sqlQuery);) {
            pstmt.setString(1, tableName);
            pstmt.executeUpdate();
        }
        catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "", e);
        }
    }

    /**
     *
     * @param tableID
     */
    public void clearCustomerNameInTableById(String tableID) {
        String sqlQuery = "UPDATE places SET CUSTOMER=null WHERE ID=?";
        try (Connection con = dbSession.getConnection(); PreparedStatement pstmt = con.prepareStatement(sqlQuery);) {
            pstmt.setString(1, tableID);
            pstmt.executeUpdate();
        }
        catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "", e);
        }
    }

    /**
     *
     * @param waiterName
     * @param tableName
     */
    public void setWaiterNameInTable(String waiterName, String tableName) {
        String sqlQuery = "UPDATE places SET WAITER=? WHERE NAME=?";
        try (Connection con = dbSession.getConnection(); PreparedStatement pstmt = con.prepareStatement(sqlQuery);) {
            pstmt.setString(1, waiterName);
            pstmt.setString(2, tableName);
            pstmt.executeUpdate();
        }
        catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "", e);
        }
    }

    /**
     *
     * @param waiterName
     * @param tableID
     */
    public void setWaiterNameInTableById(String waiterName, String tableID) {
        String sqlQuery = "UPDATE places SET WAITER=? WHERE ID=?";
        try (Connection con = dbSession.getConnection(); PreparedStatement pstmt = con.prepareStatement(sqlQuery);) {
            pstmt.setString(1, waiterName);
            pstmt.setString(2, tableID);
            pstmt.executeUpdate();
        }
        catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "", e);
        }
    }

    /**
     *
     * @param tableName
     * @return
     */
    public String getWaiterNameInTable(String tableName) {
        String sqlQuery = "SELECT waiter FROM places WHERE NAME=?";
        try (Connection con = dbSession.getConnection(); PreparedStatement pstmt = con.prepareStatement(sqlQuery);) {
            pstmt.setString(1, tableName);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String waiter = rs.getString("WAITER");
                return (waiter);
            }
        }
        catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "", e);
        }

        return "";
    }

    /**
     *
     * @param tableID
     * @return
     */
    public String getWaiterNameInTableById(String tableID) {
        String sqlQuery = "SELECT waiter FROM places WHERE ID=?";
        try (Connection con = dbSession.getConnection(); PreparedStatement pstmt = con.prepareStatement(sqlQuery);) {
            pstmt.setString(1, tableID);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String waiter = rs.getString("WAITER");
                return (waiter);
            }
        }
        catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "", e);
        }

        return "";
    }

    /**
     *
     * @param tableName
     */
    public void clearWaiterNameInTable(String tableName) {
        String sqlQuery = "UPDATE places SET WAITER=null WHERE NAME=?";
        try (Connection con = dbSession.getConnection(); PreparedStatement pstmt = con.prepareStatement(sqlQuery);) {
            pstmt.setString(1, tableName);
            pstmt.executeUpdate();
        }
        catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "", e);
        }
    }

    /**
     *
     * @param tableID
     */
    public void clearWaiterNameInTableById(String tableID) {
        try (Connection con = dbSession.getConnection()) {
            String sqlQuery = "UPDATE places SET WAITER=null WHERE ID=?";
            PreparedStatement pstmt = con.prepareStatement(sqlQuery);
            pstmt.setString(1, tableID);
            pstmt.executeUpdate();
        }
        catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "", e);
        }
    }

    /**
     *
     * @param ID
     * @return
     */
    public String getTicketIdInTable(String ID) {
        try (Connection con = dbSession.getConnection()) {
            String sqlQuery = "SELECT TICKETID FROM places WHERE ID='" + ID + "'";
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sqlQuery);

            if (rs.next()) {
                String customer = rs.getString("TICKETID");
                return (customer);
            }
        }
        catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "", e);
        }

        return "";
    }

    /**
     *
     * @param TicketID
     * @param tableName
     */
    public void setTicketIdInTable(String TicketID, String tableName) {
        try (Connection con = dbSession.getConnection()) {
            String sqlQuery = "UPDATE places SET TICKETID=? WHERE NAME=?";
            PreparedStatement pstmt = con.prepareStatement(sqlQuery);
            pstmt.setString(1, TicketID);
            pstmt.setString(2, tableName);
            pstmt.executeUpdate();
        }
        catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "", e);
        }
    }

    /**
     *
     * @param tableName
     */
    public void clearTicketIdInTable(String tableName) {
        try (Connection con = dbSession.getConnection()) {
            String sqlQuery = "UPDATE places SET TICKETID=null WHERE NAME=?";
            PreparedStatement pstmt = con.prepareStatement(sqlQuery);
            pstmt.setString(1, tableName);
            pstmt.executeUpdate();
        }
        catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "", e);
        }
    }

    /**
     *
     * @param tableID
     */
    public void clearTicketIdInTableById(String tableID) {
        try (Connection con = dbSession.getConnection()) {
            String sqlQuery = "UPDATE places SET TICKETID=null WHERE ID=?";
            PreparedStatement pstmt = con.prepareStatement(sqlQuery);
            pstmt.setString(1, tableID);
            pstmt.executeUpdate();
        }
        catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "", e);
        }
    }

    /**
     *
     * @param ticketID
     * @return
     */
    public Integer countTicketIdInTable(String ticketID) {
        try (Connection con = dbSession.getConnection()) {
            String sqlQuery = "SELECT COUNT(*) AS RECORDCOUNT FROM places WHERE TICKETID='" + ticketID + "'";
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sqlQuery);

            if (rs.next()) {
                Integer count = rs.getInt("RECORDCOUNT");
                return (count);
            }
        }
        catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "", e);
        }

        return 0;
    }

    /**
     *
     * @param ticketID
     * @return
     */
    public String getTableDetails(String ticketID) {
        try (Connection con = dbSession.getConnection()) {
            String sqlQuery = "SELECT NAME FROM places WHERE TICKETID='" + ticketID + "'";
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sqlQuery);

            if (rs.next()) {
                String name = rs.getString("NAME");
                return (name);
            }
        }
        catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "", e);
        }

        return "";
    }

    /**
     *
     * @param tableID
     */
    public void setTableMovedFlag(String tableID) {
        try (Connection con = dbSession.getConnection()) {
            String sqlQuery = "UPDATE places SET TABLEMOVED='true' WHERE ID=?";
            PreparedStatement pstmt = con.prepareStatement(sqlQuery);
            pstmt.setString(1, tableID);
            pstmt.executeUpdate();
        }
        catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "", e);
        }
    }

    /**
     *
     * @param ticketID
     * @return
     */
    public String getTableMovedName(String ticketID) {
        try (Connection con = dbSession.getConnection()) {
            String sqlQuery = "SELECT NAME FROM places WHERE TICKETID='" + ticketID + "' AND TABLEMOVED ='true'";
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sqlQuery);

            if (rs.next()) {
                String name = rs.getString("NAME");
                return (name);
            }
        }
        catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "", e);
        }

        return null;
    }

    /**
     *
     * @param ticketID
     * @return
     */
    public Boolean getTableMovedFlag(String ticketID) {
        try (Connection con = dbSession.getConnection()) {
            String sqlQuery = "SELECT TABLEMOVED FROM places WHERE TICKETID='" + ticketID + "'";
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sqlQuery);

            if (rs.next()) {
                return (rs.getBoolean("TABLEMOVED"));
            }
        }
        catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "", e);
        }

        return false;
    }

    /**
     *
     * @param tableID
     */
    public void clearTableMovedFlag(String tableID) {
        try (Connection con = dbSession.getConnection()) {
            String sqlQuery = "UPDATE places SET TABLEMOVED='false' WHERE NAME=?";
            PreparedStatement pstmt = con.prepareStatement(sqlQuery);
            pstmt.setString(1, tableID);
            pstmt.executeUpdate();
        }
        catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "", e);
        }
    }
}
