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

package com.openbravo.pos.admin;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.DataRead;
import com.openbravo.data.loader.DataWrite;
import com.openbravo.data.loader.IKeyed;
import com.openbravo.data.loader.SerializableRead;
import com.openbravo.data.loader.SerializerRead;
import com.openbravo.data.loader.SerializerWrite;
import java.io.Serializable;

/**
 * Domain entity representing a Role in KriolOS POS security model.
 *
 * @author adrianromero
 * @author KriolOS
 */
public class RoleInfo implements SerializableRead, Serializable, IKeyed {
    
    private static final long serialVersionUID = 9110127845966L;

    private String m_sID;
    protected String m_sName;
    protected byte[] m_aPermissions;
    
    public RoleInfo() {
        this(null, null, null);
    }

    public RoleInfo(String id, String name) {
        this(id, name, null);
    }

    public RoleInfo(String id, String name, byte[] permissions) {
        this.m_sID = id;
        this.m_sName = name;
        this.m_aPermissions = permissions;
    }
    
    @Override
    public Object getKey() {
        return m_sID;
    }
    
    @Override
    public void readValues(DataRead dr) throws BasicException {
        m_sID = dr.getString(1);
        m_sName = dr.getString(2);
    }
    
    public void setID(String sID) {
        m_sID = sID;
    }
    
    public String getID() {
        return m_sID;
    }

    public void setId(String id) {
        m_sID = id;
    }

    public String getId() {
        return m_sID;
    }
    
    public String getName() {
        return m_sName;
    }
    
    public void setName(String sValue) {
        m_sName = sValue;
    }

    public byte[] getPermissions() {
        return m_aPermissions;
    }

    public void setPermissions(byte[] permissions) {
        this.m_aPermissions = permissions;
    }
    
    @Override
    public String toString() {
        return m_sName;
    }

    /**
     * Serializer for reading RoleInfo from JDBC queries.
     * Columns: 1: ID, 2: NAME, 3: PERMISSIONS
     */
    public static SerializerRead<RoleInfo> getSerializerRead() {
        return (DataRead dr) -> new RoleInfo(dr.getString(1), dr.getString(2), dr.getBytes(3));
    }

    /**
     * Serializer for inserting RoleInfo.
     */
    public static SerializerWrite<RoleInfo> getSerializerInsert() {
        return (DataWrite dp, RoleInfo role) -> {
            dp.setString(1, role.getID());
            dp.setString(2, role.getName());
            dp.setBytes(3, role.getPermissions());
        };
    }

    /**
     * Serializer for updating RoleInfo.
     * Columns: 1: ID, 2: NAME, 3: PERMISSIONS, 4: ID (WHERE id = ?)
     */
    public static SerializerWrite<RoleInfo> getSerializerUpdate() {
        return (DataWrite dp, RoleInfo role) -> {
            dp.setString(1, role.getID());
            dp.setString(2, role.getName());
            dp.setBytes(3, role.getPermissions());
            dp.setString(4, role.getID());
        };
    }

    /**
     * Serializer for deleting RoleInfo by ID.
     */
    public static SerializerWrite<RoleInfo> getSerializerDelete() {
        return (DataWrite dp, RoleInfo role) -> {
            dp.setString(1, role.getID());
        };
    }
}
