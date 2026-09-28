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
package com.openbravo.pos.sales;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.Datas;
import com.openbravo.data.loader.PreparedSentence;
import com.openbravo.data.loader.SerializerReadBasic;
import com.openbravo.data.loader.SerializerWriteBasicExt;
import com.openbravo.data.loader.SerializerWriteString;
import com.openbravo.data.loader.Session;
import com.openbravo.data.loader.StaticSentence;
import com.openbravo.pos.forms.BeanFactoryDataSingle;
import com.openbravo.pos.ticket.TicketInfo;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author adrianromero
 */
public class DataLogicReceipts extends BeanFactoryDataSingle {

    private final static System.Logger LOGGER = System.getLogger(DataLogicReceipts.class.getName());

    private Session sessionDB;

    /**
     * Creates a new instance of DataLogicReceipts
     */
    public DataLogicReceipts() {
    }

    /**
     *
     * @param s
     */
    @Override
    public void init(Session s) {
        this.sessionDB = s;
    }

    /**
     *
     * @param Id
     * @return
     * @throws BasicException
     */
    public final TicketInfo getSharedTicket(String id) throws BasicException {
        TicketInfo ticketInfo = null;

        try {
            if (id != null) {
                Object[] record = (Object[]) new StaticSentence(sessionDB,
                        "SELECT CONTENT FROM sharedtickets WHERE ID = ?",
                        SerializerWriteString.INSTANCE,
                        new SerializerReadBasic(new Datas[]{Datas.SERIALIZABLE})).find(id);

                if (record != null && record[0] != null) {
                    ticketInfo = (TicketInfo) record[0];
                }
            } else {
                LOGGER.log(System.Logger.Level.ERROR, "Fail getSharedTicket id is null");
            }
        }
        catch (Exception ex) {
            LOGGER.log(System.Logger.Level.ERROR, "Exception getSharedTicket: " + id, ex);
            throw new BasicException("Exception getSharedTicket id: " + id, ex);
        }

        return ticketInfo;
    }

    /**
     * JG Dec 14 Administrator and Manager Roles always have access to ALL
     * SHARED tickets
     *
     * @return
     * @throws BasicException
     */
    public final List<SharedTicketInfo> getSharedTicketList() throws BasicException {

        List<SharedTicketInfo> list = new ArrayList<>();
        String SQL = "SELECT ID, NAME, APPUSER, LOCKED, PICKUPID, CONTENT FROM sharedtickets ORDER BY ID";

        try (Statement pstmt = sessionDB.getConnection().createStatement(); ResultSet rs = pstmt.executeQuery(SQL)) {
            while (rs.next()) {
                SharedTicketInfo sTicketInfo = new SharedTicketInfo();

                sTicketInfo.setId(rs.getString(1));
                sTicketInfo.setName(rs.getString(2));
                sTicketInfo.setUserName(rs.getString(3));
                sTicketInfo.setStatus(rs.getString(4));
                sTicketInfo.setPickupId(rs.getInt(5));

                InputStream bStream = rs.getBinaryStream(6);
                ObjectInputStream bis = new ObjectInputStream(bStream);
                TicketInfo tInfo = (TicketInfo) bis.readObject();
                sTicketInfo.setTicketInfo(tInfo);

                list.add(sTicketInfo);
            }
        }
        catch (SQLException | IOException | ClassNotFoundException ex) {
            LOGGER.log(System.Logger.Level.ERROR, "Exception getSharedTicketList ", ex);
            throw new BasicException("Exception getSharedTicketList", ex);
        }

        return list;
    }

    public final List<SharedTicketInfo> getUserSharedTicketList(String appuser) throws BasicException {
        String sql = "SELECT ID, NAME, APPUSER, LOCKED, PICKUPID, CONTENT FROM sharedtickets WHERE APPUSER =? ORDER BY ID";

        List<SharedTicketInfo> list = new ArrayList<>();

        try (PreparedStatement pstmt = sessionDB.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, appuser);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    SharedTicketInfo sTicketInfo = new SharedTicketInfo();

                    sTicketInfo.setId(rs.getString(1));
                    sTicketInfo.setName(rs.getString(2));
                    sTicketInfo.setUserName(rs.getString(3));
                    sTicketInfo.setStatus(rs.getString(4));
                    sTicketInfo.setPickupId(rs.getInt(5));
                    InputStream bStream = rs.getBinaryStream(6);
                    ObjectInputStream bis = new ObjectInputStream(bStream);
                    TicketInfo tInfo = (TicketInfo) bis.readObject();
                    sTicketInfo.setTicketInfo(tInfo);

                    list.add(sTicketInfo);
                }
            }
            catch (IOException | ClassNotFoundException ex) {
                LOGGER.log(System.Logger.Level.ERROR, "Exception get SharedTicket for user: " + appuser, ex);
                throw new BasicException("Exception get SharedTicket for user: " + appuser, ex);
            }

        }
        catch (SQLException ex) {
            LOGGER.log(System.Logger.Level.ERROR, "Exception get SharedTicket for user: " + appuser, ex);
            throw new BasicException("Exception get SharedTicket for user: " + appuser, ex);
        }
        return list;
    }

    public final SharedTicketInfo getSharedTicketInfo(String sharedId) throws BasicException {
        String sql = "SELECT ID, NAME, APPUSER, LOCKED, PICKUPID, CONTENT FROM sharedtickets WHERE ID =? LIMIT 1";

        SharedTicketInfo sTicketInfo;
        try (PreparedStatement pstmt = sessionDB.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, sharedId);

            try (ResultSet rs = pstmt.executeQuery()) {
                rs.next();

                sTicketInfo = new SharedTicketInfo();

                sTicketInfo.setId(rs.getString(1));
                sTicketInfo.setName(rs.getString(2));
                sTicketInfo.setUserName(rs.getString(3));
                sTicketInfo.setStatus(rs.getString(4));
                sTicketInfo.setPickupId(rs.getInt(5));
                InputStream bStream = rs.getBinaryStream(6);
                ObjectInputStream bis = new ObjectInputStream(bStream);
                TicketInfo tInfo = (TicketInfo) bis.readObject();
                sTicketInfo.setTicketInfo(tInfo);

            }
            catch (IOException | ClassNotFoundException ex) {
                LOGGER.log(System.Logger.Level.ERROR, "Exception get SharedTicket id: " + sharedId, ex);
                throw new BasicException("Exception get SharedTicket for user: " + sharedId, ex);
            }

        }
        catch (SQLException ex) {
            LOGGER.log(System.Logger.Level.ERROR, "Exception get SharedTicket id: " + sharedId, ex);
            throw new BasicException("Exception get SharedTicket id: " + sharedId, ex);
        }
        return sTicketInfo;
    }

    public final void insertSharedTicket(final String id, final TicketInfo ticket, int pickupid) throws BasicException {
        int rowsAffected;
        String SQL = "INSERT INTO sharedtickets (ID, NAME, CONTENT, APPUSER, PICKUPID) VALUES (?, ?, ?, ?, ?) ";
        try {
            Connection conDB = sessionDB.getConnection();
            try (PreparedStatement pstmt = conDB.prepareStatement(SQL)) {
                pstmt.getConnection().setAutoCommit(false);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream bos = new ObjectOutputStream(baos);
                bos.writeObject(ticket);

                byte[] saves = baos.toByteArray();
                ByteArrayInputStream bais = new ByteArrayInputStream(saves);

                pstmt.setString(1, id);
                pstmt.setString(2, ticket.getName());
                pstmt.setBinaryStream(3, bais);
                pstmt.setString(4, ticket.getUser() != null ? ticket.getUser().getId() : null);
                pstmt.setInt(5, pickupid);
                rowsAffected = pstmt.executeUpdate();

                pstmt.getConnection().commit();

                LOGGER.log(System.Logger.Level.DEBUG, "Insert SharedTicket id: " + id + ", affected row: " + rowsAffected);

            }
            catch (SQLException | IOException ex) {

                try {
                    if (conDB != null) {
                        conDB.rollback();
                    }
                }
                catch (SQLException exRoolB) {
                    LOGGER.log(System.Logger.Level.ERROR, "Exception roolback SharedTicket id: " + id, exRoolB);
                }

                LOGGER.log(System.Logger.Level.ERROR, "Exception Insert SharedTicket id: " + id, ex);
                throw new BasicException("Exception Insert SharedTicket id: " + id, ex);
            }
        }
        catch (SQLException ex) {
            LOGGER.log(System.Logger.Level.ERROR, "Exception get DB Connection. Insert into  SharedTicket table with ID: " + id, ex);
        }
    }

    /**
     *
     * @param id
     * @param ticket
     * @param pickupid
     * @throws BasicException
     */
    public final void updateSharedTicket(final String id, final TicketInfo ticket, int pickupid) throws BasicException {

        String SQL = "UPDATE sharedtickets SET NAME = ?, CONTENT = ?, APPUSER = ?, PICKUPID = ? WHERE ID = ?";
        try (PreparedStatement pstmt = sessionDB.getConnection().prepareStatement(SQL)) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream bos = new ObjectOutputStream(baos);
            bos.writeObject(ticket);

            byte[] saves = baos.toByteArray();
            ByteArrayInputStream bais = new ByteArrayInputStream(saves);

            pstmt.setString(1, ticket.getName());
            pstmt.setBinaryStream(2, bais);
            pstmt.setString(3, ticket.getUser() != null ? ticket.getUser().getId() : null);
            pstmt.setInt(4, pickupid);
            pstmt.setString(5, id);
            pstmt.executeUpdate();
        }
        catch (SQLException | IOException ex) {
            LOGGER.log(System.Logger.Level.ERROR, "Exception UPDATE SharedTicket id: " + id, ex);
            throw new BasicException("Exception UPDATE SharedTicket id: " + id, ex);
        }
    }

    /**
     *
     * @param id
     * @param ticket
     * @param pickupid
     * @throws BasicException
     */
    public final void updateRSharedTicket(final String id, final TicketInfo ticket, int pickupid) throws BasicException {
        updateSharedTicket(id, ticket, pickupid);
    }

    /**
     * In place for multi-purposing like containing data from elsewhere and/or
     * using Place and User for Notifications
     *
     * @param id
     * @param locked
     * @throws BasicException
     */
    public final void lockSharedTicket(final String id, final String locked) throws BasicException {

        try {
            Object[] values = new Object[]{id, locked};
            Datas[] datas = new Datas[]{
                Datas.STRING,
                Datas.STRING
            };
            new PreparedSentence(sessionDB,
                    "UPDATE sharedtickets SET "
                    + "LOCKED = ? "
                    + "WHERE ID = ?",
                    new SerializerWriteBasicExt(datas, new int[]{1, 0})).exec(values);
        }
        catch (Exception ex) {
            LOGGER.log(System.Logger.Level.ERROR, "Exception lockSharedTicket id: " + id, ex);
            throw new BasicException("Exception lockSharedTicket id: " + id, ex);
        }
    }

    /**
     * In place for multi-purposing like flushing locks from elsewhere and/or
     * using Place and User for Notifications
     *
     * @param id
     * @param unlocked
     * @throws BasicException
     */
    public final void unlockSharedTicket(final String id, final String unlocked) throws BasicException {

        try {
            Object[] values = new Object[]{
                id,
                unlocked
            };
            Datas[] datas = new Datas[]{
                Datas.STRING,
                Datas.STRING
            };
            new PreparedSentence(sessionDB,
                    "UPDATE sharedtickets SET "
                    + "LOCKED = ? "
                    + "WHERE ID = ?",
                    new SerializerWriteBasicExt(datas, new int[]{1, 0})).exec(values);
        }
        catch (Exception ex) {
            LOGGER.log(System.Logger.Level.ERROR, "Exception unlockSharedTicket id: " + id, ex);
            throw new BasicException("Exception unlockSharedTicket id: " + id, ex);
        }
    }

    /**
     * For Restaurant View
     *
     * @param id
     * @param ticket
     * @param pickupid
     * @throws BasicException
     */
    public final void insertRSharedTicket(final String id, final TicketInfo ticket, int pickupid) throws BasicException {
        insertSharedTicket(id, ticket, pickupid);
    }

    /**
     *
     * @param id
     * @throws BasicException
     */
    public final void deleteSharedTicket(final String id) throws BasicException {

        try {
            new StaticSentence(sessionDB,
                    "DELETE FROM sharedtickets WHERE ID = ?",
                    SerializerWriteString.INSTANCE).exec(id);
        }
        catch (Exception ex) {
            LOGGER.log(System.Logger.Level.ERROR, "Exception Delete SharedTicket id: " + id, ex);
            throw new BasicException("Exception Delete SharedTicket id: " + id, ex);
        }
    }

    public final Integer getPickupId(final String sharedTicketId) throws BasicException {
        Integer pickupId = 0;

        SharedTicketInfo sht = getSharedTicketInfo(sharedTicketId);
        if (sht != null) {
            pickupId = sht.getPickupId();
        }

        return pickupId;
    }

    public final String getUserId(final String sharedTicketId) throws BasicException {
        String userId = null;

        SharedTicketInfo sht = getSharedTicketInfo(sharedTicketId);
        if (sht != null) {
            userId = sht.getUserName();
        }

        return userId;
    }

    public final String getLockState(final String sharedTicketId, String lockState) throws BasicException {
        String state = null;

        SharedTicketInfo sht = getSharedTicketInfo(sharedTicketId);
        if (sht != null) {
            state = sht.getStatus();
        }

        return state;
    }
}
