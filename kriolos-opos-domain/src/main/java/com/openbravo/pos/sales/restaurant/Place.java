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
import com.openbravo.data.loader.SerializableRead;

/**
 * Domain entity representing a restaurant table/place.
 * UI components (buttons, rendering, layouts) are decoupled and managed by the presentation layer.
 *
 * @author JG uniCenta
 */
public class Place implements SerializableRead, java.io.Serializable {

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

    public String getTicketID() {
        return m_ticketId;
    }

    public String getName() {
        return m_sName;
    }

    public String getSeats() {
        return m_sSeats;
    }

    public int getX() {
        return m_ix;
    }

    public int getY() {
        return m_iy;
    }

    public void setX(int x) {
        this.m_ix = x;
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

    public String getCustomer() {
        return m_customer;
    }

    public String getWaiter() {
        return m_waiter;
    }

    public boolean hasPeople() {
        return m_bPeople;
    }

    public void setPeople(boolean bValue) {
        this.m_bPeople = bValue;
    }
}