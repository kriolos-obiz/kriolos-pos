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
import com.openbravo.data.loader.DataRead;
import com.openbravo.data.loader.DataWrite;
import com.openbravo.data.loader.IKeyed;
import com.openbravo.data.loader.SerializableRead;
import com.openbravo.data.loader.SerializerRead;
import com.openbravo.data.loader.SerializerWrite;
import java.io.Serializable;

/**
 * Domain entity representing a restaurant table/place.
 * UI components (buttons, rendering, layouts) are decoupled and managed by the presentation layer.
 *
 * @author JG uniCenta
 * @author KriolOS
 */
public class Place implements SerializableRead, Serializable, IKeyed {

    private static final long serialVersionUID = 8652254694281L;

    private String m_sId;
    private String m_sName;
    private String m_sSeats;
    private int m_ix;
    private int m_iy;
    private String m_sfloor;
    private String m_customer;
    private String m_waiter;
    private String m_ticketId;
    private Boolean m_tableMoved;
    private Boolean m_changed = false;
    private boolean m_bPeople;

    /**
     * Creates a new instance of Place.
     */
    public Place() {
    }

    /**
     * Constructs a Place with its primary persistence attributes.
     *
     * @param id identifier
     * @param name place/table label
     * @param seats seat count description
     * @param x floor X coordinate
     * @param y floor Y coordinate
     * @param floor parent floor identifier
     */
    public Place(String id, String name, String seats, int x, int y, String floor) {
        this.m_sId = id;
        this.m_sName = name;
        this.m_sSeats = seats;
        this.m_ix = x;
        this.m_iy = y;
        this.m_sfloor = floor;
    }

    @Override
    public Object getKey() {
        return m_sId;
    }

    @Override
    public void readValues(DataRead dr) throws BasicException {
        m_sId = dr.getString(1);
        m_sName = dr.getString(2);
        m_sSeats = dr.getString(3);
        m_ix = dr.getInt(4);
        m_iy = dr.getInt(5);
        m_sfloor = dr.getString(6);
        m_customer = dr.getString(7);
        m_waiter = dr.getString(8);
        m_ticketId = dr.getString(9);
        m_tableMoved = dr.getBoolean(10);
        m_bPeople = false;
    }

    public String getId() {
        return m_sId;
    }

    public void setId(String id) {
        this.m_sId = id;
    }

    public String getTicketID() {
        return m_ticketId;
    }

    public void setTicketID(String ticketId) {
        this.m_ticketId = ticketId;
    }

    public String getName() {
        return m_sName;
    }

    public void setName(String name) {
        this.m_sName = name;
    }

    public String getSeats() {
        return m_sSeats;
    }

    public void setSeats(String seats) {
        this.m_sSeats = seats;
    }

    public int getX() {
        return m_ix;
    }

    public void setX(int x) {
        this.m_ix = x;
    }

    public int getY() {
        return m_iy;
    }

    public void setY(int y) {
        this.m_iy = y;
    }

    public Boolean getChanged() {
        return m_changed;
    }

    public void setChanged(Boolean changed) {
        this.m_changed = changed;
    }

    public String getFloor() {
        return m_sfloor;
    }

    public void setFloor(String floor) {
        this.m_sfloor = floor;
    }

    public String getCustomer() {
        return m_customer;
    }

    public void setCustomer(String customer) {
        this.m_customer = customer;
    }

    public String getWaiter() {
        return m_waiter;
    }

    public void setWaiter(String waiter) {
        this.m_waiter = waiter;
    }

    public boolean hasPeople() {
        return m_bPeople;
    }

    public void setPeople(boolean bValue) {
        this.m_bPeople = bValue;
    }

    @Override
    public String toString() {
        return m_sName;
    }

    /**
     * Returns a {@link SerializerRead} for deserializing Place records from JDBC queries.
     * Binding: 1: ID, 2: NAME, 3: SEATS, 4: X, 5: Y, 6: FLOOR
     *
     * @return row serializer for {@link Place}
     */
    public static SerializerRead<Place> getSerializerRead() {
        return (DataRead dr) -> new Place(
                dr.getString(1),
                dr.getString(2),
                dr.getString(3),
                dr.getInt(4) != null ? dr.getInt(4) : 0,
                dr.getInt(5) != null ? dr.getInt(5) : 0,
                dr.getString(6)
        );
    }

    /**
     * Returns a {@link SerializerWrite} for inserting Place records.
     * Binding: 1: ID, 2: NAME, 3: SEATS, 4: X, 5: Y, 6: FLOOR
     *
     * @return serializer for INSERT statement
     */
    public static SerializerWrite<Place> getSerializerInsert() {
        return (DataWrite dp, Place p) -> {
            dp.setString(1, p.getId());
            dp.setString(2, p.getName());
            dp.setString(3, p.getSeats());
            dp.setInt(4, p.getX());
            dp.setInt(5, p.getY());
            dp.setString(6, p.getFloor());
        };
    }

    /**
     * Returns a {@link SerializerWrite} for updating Place records.
     * Binding: 1: ID, 2: NAME, 3: SEATS, 4: X, 5: Y, 6: FLOOR, 7: ID (WHERE id = ?)
     *
     * @return serializer for UPDATE statement
     */
    public static SerializerWrite<Place> getSerializerUpdate() {
        return (DataWrite dp, Place p) -> {
            dp.setString(1, p.getId());
            dp.setString(2, p.getName());
            dp.setString(3, p.getSeats());
            dp.setInt(4, p.getX());
            dp.setInt(5, p.getY());
            dp.setString(6, p.getFloor());
            dp.setString(7, p.getId());
        };
    }

    /**
     * Returns a {@link SerializerWrite} for deleting Place records.
     * Binding: 1: ID (WHERE id = ?)
     *
     * @return serializer for DELETE statement
     */
    public static SerializerWrite<Place> getSerializerDelete() {
        return (DataWrite dp, Place p) -> {
            dp.setString(1, p.getId());
        };
    }
}
