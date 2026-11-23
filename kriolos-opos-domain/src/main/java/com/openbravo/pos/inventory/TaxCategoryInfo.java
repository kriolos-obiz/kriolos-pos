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
import com.openbravo.data.loader.SerializerRead;
import com.openbravo.data.loader.SerializerWrite;
import java.io.Serializable;

/**
 * Tax Category domain model.
 *
 * @author adrianromero
 * @author KriolOS
 */
public class TaxCategoryInfo implements Serializable, IKeyed {
    
    private static final long serialVersionUID = 8959679342805L;
    private String m_sID;
    private String m_sName;
       
    /**
     * Constructs a new TaxCategoryInfo.
     *
     * @param sID identifier
     * @param sName display name
     */
    public TaxCategoryInfo(String sID, String sName) {
        m_sID = sID;
        m_sName = sName;      
    }
    
    @Override
    public Object getKey() {
        return m_sID;
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
    
    public void setName(String sName) {
        m_sName = sName;
    }
   
    @Override
    public String toString(){
        return m_sName;
    }

    /**
     * Returns a {@link SerializerRead} for deserializing TaxCategoryInfo from JDBC result sets.
     *
     * @return row serializer for {@link TaxCategoryInfo}
     */
    public static SerializerRead<TaxCategoryInfo> getSerializerRead() {
        return (DataRead dr) -> new TaxCategoryInfo(dr.getString(1), dr.getString(2));
    }

    /**
     * Returns a {@link SerializerWrite} for inserting TaxCategoryInfo records.
     * Binding: 1: ID, 2: NAME
     *
     * @return serializer for INSERT statement
     */
    public static SerializerWrite<TaxCategoryInfo> getSerializerInsert() {
        return (DataWrite dp, TaxCategoryInfo info) -> {
            dp.setString(1, info.getID());
            dp.setString(2, info.getName());
        };
    }

    /**
     * Returns a {@link SerializerWrite} for updating TaxCategoryInfo records.
     * Binding: 1: ID, 2: NAME, 3: ID (WHERE id = ?)
     *
     * @return serializer for UPDATE statement
     */
    public static SerializerWrite<TaxCategoryInfo> getSerializerUpdate() {
        return (DataWrite dp, TaxCategoryInfo info) -> {
            dp.setString(1, info.getID());
            dp.setString(2, info.getName());
            dp.setString(3, info.getID());
        };
    }

    /**
     * Returns a {@link SerializerWrite} for deleting TaxCategoryInfo records.
     * Binding: 1: ID (WHERE id = ?)
     *
     * @return serializer for DELETE statement
     */
    public static SerializerWrite<TaxCategoryInfo> getSerializerDelete() {
        return (DataWrite dp, TaxCategoryInfo info) -> {
            dp.setString(1, info.getID());
        };
    }
}
