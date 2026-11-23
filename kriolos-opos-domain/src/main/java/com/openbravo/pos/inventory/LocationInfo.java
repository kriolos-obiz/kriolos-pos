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

package com.openbravo.pos.inventory;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.DataRead;
import com.openbravo.data.loader.DataWrite;
import com.openbravo.data.loader.IKeyed;
import com.openbravo.data.loader.SerializableRead;
import com.openbravo.data.loader.SerializerRead;
import com.openbravo.data.loader.SerializerWrite;
import java.io.Serializable;

/**
 * Domain entity representing an inventory warehouse/location record.
 *
 * @author adrianromero
 * @author KriolOS
 */
public class LocationInfo implements SerializableRead, Serializable, IKeyed {
    
    private static final long serialVersionUID = 9032683595230L;
    private String m_sID;
    private String m_sName;
    private String m_sAddress;
    
    /** Creates a new instance of LocationInfo */
    public LocationInfo() {
        this(null, null, null);
    }

    public LocationInfo(String id, String name) {
        this(id, name, null);
    }

    public LocationInfo(String id, String name, String address) {
        this.m_sID = id;
        this.m_sName = name;
        this.m_sAddress = address;
    }
    
    /**
     *
     * @return
     */
    @Override
    public Object getKey() {
        return m_sID;
    }

    /**
     *
     * @param dr
     * @throws BasicException
     */
    @Override
    public void readValues(DataRead dr) throws BasicException {
        m_sID = dr.getString(1);
        m_sName = dr.getString(2);
        m_sAddress = dr.getString(3);
    }

    public String getId() {
        return m_sID;
    }

    public void setId(String id) {
        this.m_sID = id;
    }

    /**
     *
     * @param sID
     */
    public void setID(String sID) {
        m_sID = sID;
    }
    
    /**
     *
     * @return
     */
    public String getID() {
        return m_sID;
    }

    /**
     *
     * @return
     */
    public String getName() {
        return m_sName;
    }
    
    /**
     *
     * @param sName
     */
    public void setName(String sName) {
        m_sName = sName;
    }  

    /**
     *
     * @return
     */
    public String getAddress() {
        return m_sAddress;
    }
    
    /**
     *
     * @param sAddress
     */
    public void setAddress(String sAddress) {
        m_sAddress = sAddress;
    } 
    
    @Override
    public String toString(){
        return m_sName;
    }

    /**
     * Serializer for reading LocationInfo from JDBC queries.
     * Columns: 1: ID, 2: NAME, 3: ADDRESS
     */
    public static SerializerRead<LocationInfo> getSerializerRead() {
        return (DataRead dr) -> new LocationInfo(
                dr.getString(1),
                dr.getString(2),
                dr.getString(3)
        );
    }

    /**
     * Serializer for inserting LocationInfo.
     */
    public static SerializerWrite<LocationInfo> getSerializerInsert() {
        return (DataWrite dp, LocationInfo loc) -> {
            dp.setString(1, loc.getId());
            dp.setString(2, loc.getName());
            dp.setString(3, loc.getAddress());
        };
    }

    /**
     * Serializer for updating LocationInfo.
     * Columns: 1: ID, 2: NAME, 3: ADDRESS, 4: ID (WHERE id = ?)
     */
    public static SerializerWrite<LocationInfo> getSerializerUpdate() {
        return (DataWrite dp, LocationInfo loc) -> {
            dp.setString(1, loc.getId());
            dp.setString(2, loc.getName());
            dp.setString(3, loc.getAddress());
            dp.setString(4, loc.getId());
        };
    }

    /**
     * Serializer for deleting LocationInfo by ID.
     */
    public static SerializerWrite<LocationInfo> getSerializerDelete() {
        return (DataWrite dp, LocationInfo loc) -> {
            dp.setString(1, loc.getId());
        };
    }
}

