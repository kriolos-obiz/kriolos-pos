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
import com.openbravo.format.Formats;
import java.io.Serializable;
import java.util.Date;

/**
 * Domain entity representing an employee leave record.
 *
 * @author Ali Safdar and Aneeqa Baber
 * @author KriolOS
 */
public class LeavesInfo implements Serializable, IKeyed {

    private static final long serialVersionUID = 8936482715929L;

    private String m_sID;
    private String m_sPplId;
    private String m_sName;
    private Date m_dStartDate;
    private Date m_dEndDate;
    private String m_sNotes;

    public LeavesInfo() {
        this(null, null, null, (Date) null, (Date) null, null);
    }

    public LeavesInfo(String id, String pplid, String name, Date startDate, Date endDate, String notes) {
        this.m_sID = id;
        this.m_sPplId = pplid;
        this.m_sName = name;
        this.m_dStartDate = startDate;
        this.m_dEndDate = endDate;
        this.m_sNotes = notes;
    }

    private static Date parseDateSafe(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        try {
            return (Date) Formats.TIMESTAMP.parseValue(dateStr);
        } catch (BasicException e) {
            return null;
        }
    }

    /**
     * Backwards-compatible constructor supporting string-formatted dates.
     */
    public LeavesInfo(String id, String name, String employeeid, String startdate, String enddate, String notes) {
        this.m_sID = id;
        this.m_sName = name;
        this.m_sPplId = employeeid;
        this.m_dStartDate = parseDateSafe(startdate);
        this.m_dEndDate = parseDateSafe(enddate);
        this.m_sNotes = notes;
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

    public String getPplId() {
        return m_sPplId;
    }

    public void setPplId(String pplId) {
        this.m_sPplId = pplId;
    }

    public String getEmployeeID() {
        return m_sPplId;
    }

    public void setEmployeeID(String employeeID) {
        this.m_sPplId = employeeID;
    }

    public String getName() {
        return m_sName;
    }

    public void setName(String sName) {
        m_sName = sName;
    }

    public Date getStartDate() {
        return m_dStartDate;
    }

    public void setStartDate(Date startDate) {
        this.m_dStartDate = startDate;
    }

    public void setStartDate(String startDate) {
        this.m_dStartDate = parseDateSafe(startDate);
    }

    public Date getEndDate() {
        return m_dEndDate;
    }

    public void setEndDate(Date endDate) {
        this.m_dEndDate = endDate;
    }

    public void setEndDate(String endDate) {
        this.m_dEndDate = parseDateSafe(endDate);
    }

    public String getNotes() {
        return m_sNotes;
    }

    public void setNotes(String notes) {
        this.m_sNotes = notes;
    }

    public String printStartDate() {
        return Formats.TIMESTAMP.formatValue(m_dStartDate);
    }

    public String printEndDate() {
        return Formats.TIMESTAMP.formatValue(m_dEndDate);
    }

    @Override
    public String toString() {
        return m_sName;
    }

    /**
     * Serializer for reading LeavesInfo from JDBC queries.
     * Columns: 1: ID, 2: PPLID, 3: NAME, 4: STARTDATE, 5: ENDDATE, 6: NOTES
     */
    public static SerializerRead<LeavesInfo> getSerializerRead() {
        return (DataRead dr) -> new LeavesInfo(
                dr.getString(1),
                dr.getString(2),
                dr.getString(3),
                dr.getTimestamp(4),
                dr.getTimestamp(5),
                dr.getString(6)
        );
    }

    /**
     * Serializer for inserting LeavesInfo.
     */
    public static SerializerWrite<LeavesInfo> getSerializerInsert() {
        return (DataWrite dp, LeavesInfo leaf) -> {
            dp.setString(1, leaf.getID());
            dp.setString(2, leaf.getPplId());
            dp.setString(3, leaf.getName());
            dp.setTimestamp(4, leaf.getStartDate());
            dp.setTimestamp(5, leaf.getEndDate());
            dp.setString(6, leaf.getNotes());
        };
    }

    /**
     * Serializer for updating LeavesInfo.
     * Columns: 1: ID, 2: PPLID, 3: NAME, 4: STARTDATE, 5: ENDDATE, 6: NOTES, 7: ID (WHERE id = ?)
     */
    public static SerializerWrite<LeavesInfo> getSerializerUpdate() {
        return (DataWrite dp, LeavesInfo leaf) -> {
            dp.setString(1, leaf.getID());
            dp.setString(2, leaf.getPplId());
            dp.setString(3, leaf.getName());
            dp.setTimestamp(4, leaf.getStartDate());
            dp.setTimestamp(5, leaf.getEndDate());
            dp.setString(6, leaf.getNotes());
            dp.setString(7, leaf.getID());
        };
    }

    /**
     * Serializer for deleting LeavesInfo by ID.
     */
    public static SerializerWrite<LeavesInfo> getSerializerDelete() {
        return (DataWrite dp, LeavesInfo leaf) -> {
            dp.setString(1, leaf.getID());
        };
    }
}
