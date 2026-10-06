package com.openbravo.pos.sales.restaurant;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.SerializerReadClass;
import com.openbravo.data.loader.Session;
import com.openbravo.data.loader.StaticSentence;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PlaceServiceImpl implements PlaceService {

    private static final Logger LOGGER = Logger.getLogger(PlaceServiceImpl.class.getName());

    private final Session dbSession;

    public PlaceServiceImpl(Session session) {
        this.dbSession = session;
    }

    @Override
    public List<Floor> getFloors() throws BasicException {
        return new StaticSentence(
                dbSession,
                "SELECT ID, NAME, IMAGE FROM floors ORDER BY NAME",
                null,
                new SerializerReadClass(Floor.class))
                .list();
    }

    @Override
    public List<Place> getPlaces() throws BasicException {
        return new StaticSentence(
                dbSession,
                "SELECT ID, NAME, SEATS, X, Y, FLOOR, CUSTOMER, WAITER, TICKETID, TABLEMOVED FROM places ORDER BY FLOOR",
                null,
                new SerializerReadClass(Place.class))
                .list();
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
        String sqlQuery = "UPDATE places SET WAITER=null WHERE ID=?";
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
        String sqlQuery = "UPDATE places SET TICKETID=? WHERE NAME=?";
        try (Connection con = dbSession.getConnection(); PreparedStatement pstmt = con.prepareStatement(sqlQuery);) {
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
        String sqlQuery = "UPDATE places SET TICKETID=null WHERE NAME=?";
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
    public void clearTicketIdInTableById(String tableID) {
        String sqlQuery = "UPDATE places SET TICKETID=null WHERE ID=?";
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
        String sqlQuery = "UPDATE places SET TABLEMOVED='true' WHERE ID=?";
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
        String sqlQuery = "UPDATE places SET TABLEMOVED='false' WHERE NAME=?";
        try (Connection con = dbSession.getConnection(); PreparedStatement pstmt = con.prepareStatement(sqlQuery);) {
            pstmt.setString(1, tableID);
            pstmt.executeUpdate();
        }
        catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "", e);
        }
    }
}
