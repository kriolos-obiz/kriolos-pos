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

package com.openbravo.pos.epm;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.DataRead;
import com.openbravo.data.loader.DataWrite;
import com.openbravo.data.loader.IKeyed;
import com.openbravo.data.loader.SerializerRead;
import com.openbravo.data.loader.SerializerWrite;
import java.io.Serializable;

/**
 * Domain entity representing an employee break definition.
 *
 * @author Ali Safdar and Aneeqa Baber
 * @author KriolOS
 */
public class BreaksInfo implements Serializable, IKeyed {

    private static final long serialVersionUID = 8936482715929L;

    private String m_sID;
    private String m_sName;
    private String m_sNotes;
    private boolean m_bVisible = true;

    public BreaksInfo() {
        this(null, null, null, true);
    }

    public BreaksInfo(String id, String name) {
        this(id, name, null, true);
    }

    public BreaksInfo(String id, String name, String notes, boolean visible) {
        this.m_sID = id;
        this.m_sName = name;
        this.m_sNotes = notes;
        this.m_bVisible = visible;
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

    public String getId() {
        return m_sID;
    }

    public void setId(String id) {
        this.m_sID = id;
    }

    public String getName() {
        return m_sName;
    }

    public void setName(String sName) {
        m_sName = sName;
    }

    public String getNotes() {
        return m_sNotes;
    }

    public void setNotes(String notes) {
        this.m_sNotes = notes;
    }

    public boolean isVisible() {
        return m_bVisible;
    }

    public void setVisible(boolean visible) {
        this.m_bVisible = visible;
    }

    @Override
    public String toString() {
        return m_sName;
    }

    /**
     * Serializer for reading BreaksInfo from JDBC queries.
     * Columns: 1: ID, 2: NAME, 3: NOTES, 4: VISIBLE
     */
    public static SerializerRead<BreaksInfo> getSerializerRead() {
        return (DataRead dr) -> {
            Boolean vis = dr.getBoolean(4);
            return new BreaksInfo(
                    dr.getString(1),
                    dr.getString(2),
                    dr.getString(3),
                    vis != null ? vis : true
            );
        };
    }

    /**
     * Serializer for inserting BreaksInfo.
     */
    public static SerializerWrite<BreaksInfo> getSerializerInsert() {
        return (DataWrite dp, BreaksInfo b) -> {
            dp.setString(1, b.getId());
            dp.setString(2, b.getName());
            dp.setString(3, b.getNotes());
            dp.setBoolean(4, b.isVisible());
        };
    }

    /**
     * Serializer for updating BreaksInfo.
     * Columns: 1: ID, 2: NAME, 3: NOTES, 4: VISIBLE, 5: ID (WHERE id = ?)
     */
    public static SerializerWrite<BreaksInfo> getSerializerUpdate() {
        return (DataWrite dp, BreaksInfo b) -> {
            dp.setString(1, b.getId());
            dp.setString(2, b.getName());
            dp.setString(3, b.getNotes());
            dp.setBoolean(4, b.isVisible());
            dp.setString(5, b.getId());
        };
    }

    /**
     * Serializer for deleting BreaksInfo by ID.
     */
    public static SerializerWrite<BreaksInfo> getSerializerDelete() {
        return (DataWrite dp, BreaksInfo b) -> {
            dp.setString(1, b.getId());
        };
    }
}
