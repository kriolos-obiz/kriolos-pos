//    KriolOS POS
//    Copyright (c) 2019-2026 KriolOS
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
import com.openbravo.data.loader.*;
import com.openbravo.data.user.EditorCreator;
import com.openbravo.data.user.ListProvider;
import com.openbravo.data.user.ListProviderCreator;
import com.openbravo.format.Formats;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.BeanFactoryDataSingle;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Legacy DataLogic provider implementing {@link ShiftService}.
 *
 * @author Ali Safdar and Aneeqa Baber
 */
public class DataLogicPresenceManagement extends BeanFactoryDataSingle implements ShiftService {

    protected Session s;

    private SentenceExec m_checkin;
    private SentenceExec m_checkout;
    private SentenceFind m_checkdate;

    private SentenceList m_breaksvisible;
    private SentenceExec m_startbreak;
    private SentenceExec m_endbreak;

    private SentenceFind m_isonbreak;
    private SentenceFind m_shiftid;

    private SentenceFind m_lastcheckin;
    private SentenceFind m_lastcheckout;
    private SentenceFind m_startbreaktime;

    private SentenceFind m_lastbreakid;
    private SentenceFind m_breakname;
    
    private SerializerRead breakread;
    private TableDefinition tbreaks;
    private TableDefinition tleaves;

    public DataLogicPresenceManagement() {
    }

    public Session getSession() {
        return s;
    }
    
    @Override
    public void init(Session s){
        this.s = s;
        String dbTrue = (s != null && s.DB != null) ? s.DB.TRUE() : "1";

        breakread = new SerializerRead() {
            @Override
            public Object readValues(DataRead dr) throws BasicException {
                return new Break(
                        dr.getString(1),
                        dr.getString(2),
                        dr.getString(3),
                        dr.getBoolean(4));
            }
        };

        tbreaks = new TableDefinition(s
            , "breaks"
            , new String[] { "ID", "NAME", "NOTES", "VISIBLE"}
            , new String[] { "ID", AppLocal.getIntString("label.epm.employee"), AppLocal.getIntString("label.epm.notes"), "VISIBLE"}
            , new Datas[] { Datas.STRING, Datas.STRING, Datas.STRING, Datas.BOOLEAN}
            , new Formats[] { Formats.STRING, Formats.STRING, Formats.STRING, Formats.BOOLEAN}
            , new int[] {0}
        );

        tleaves = new TableDefinition(s
            , "leaves"
            , new String[] { "ID", "PPLID", "NAME", "STARTDATE", "ENDDATE", "NOTES"}
            , new String[] { "ID", AppLocal.getIntString("label.epm.employee.id"), AppLocal.getIntString("label.epm.employee"), AppLocal.getIntString("label.StartDate"), AppLocal.getIntString("label.EndDate"), AppLocal.getIntString("label.notes")}
            , new Datas[] { Datas.STRING, Datas.STRING, Datas.STRING, Datas.TIMESTAMP, Datas.TIMESTAMP, Datas.STRING}
            , new Formats[] { Formats.STRING, Formats.STRING, Formats.STRING, Formats.TIMESTAMP, Formats.TIMESTAMP, Formats.STRING}
            , new int[] {0}
        );

        m_breaksvisible = new StaticSentence(s
            , "SELECT ID, NAME, NOTES, VISIBLE FROM breaks WHERE VISIBLE = " + dbTrue
            , null
            , breakread);

        m_checkin = new PreparedSentence(s
                , "INSERT INTO shifts(ID, STARTSHIFT, PPLID) VALUES (?, ?, ?)"
                , new SerializerWriteBasic(new Datas[] {Datas.STRING, Datas.TIMESTAMP, Datas.STRING}));

        m_checkout = new StaticSentence(s
                , "UPDATE shifts SET ENDSHIFT = ? WHERE ENDSHIFT IS NULL AND PPLID = ?"
                , new SerializerWriteBasic(new Datas[] {Datas.TIMESTAMP, Datas.STRING}));

        m_checkdate = new StaticSentence(s
            , "SELECT COUNT(*) FROM shifts WHERE ENDSHIFT IS NULL AND PPLID = ?"
            , SerializerWriteString.INSTANCE
            , SerializerReadString.INSTANCE);

        m_startbreak = new PreparedSentence(s
                , "INSERT INTO shift_breaks(ID, SHIFTID, BREAKID, STARTTIME) VALUES (?, ?, ?, ?)"
                , new SerializerWriteBasic(new Datas[] {Datas.STRING, Datas.STRING, Datas.STRING, Datas.TIMESTAMP}));

        m_endbreak = new StaticSentence(s
                , "UPDATE shift_breaks SET ENDTIME = ? WHERE ENDTIME IS NULL AND SHIFTID = ?"
                , new SerializerWriteBasic(new Datas[] {Datas.TIMESTAMP, Datas.STRING}));

        m_isonbreak = new StaticSentence(s
            , "SELECT COUNT(*) FROM shift_breaks WHERE ENDTIME IS NULL"                
            , SerializerWriteString.INSTANCE
            , SerializerReadString.INSTANCE);

        m_shiftid = new StaticSentence(s
            , "SELECT ID FROM shifts WHERE ENDSHIFT IS NULL AND PPLID = ?"
            , SerializerWriteString.INSTANCE
            , SerializerReadString.INSTANCE);

        m_lastcheckin = new StaticSentence(s
            , "SELECT STARTSHIFT FROM shifts WHERE ENDSHIFT IS NULL AND PPLID = ?"
            , SerializerWriteString.INSTANCE
            , SerializerReadDate.INSTANCE);

        m_lastcheckout = new StaticSentence(s
            , "SELECT MAX(ENDSHIFT) FROM shifts WHERE PPLID = ?"
            , SerializerWriteString.INSTANCE
            , SerializerReadDate.INSTANCE);

        m_startbreaktime = new StaticSentence(s
            , "SELECT STARTTIME FROM shift_breaks WHERE ENDTIME IS NULL AND SHIFTID = ?"
            , SerializerWriteString.INSTANCE
            , SerializerReadDate.INSTANCE);

        m_lastbreakid = new StaticSentence(s
            , "SELECT BREAKID FROM shift_breaks WHERE ENDTIME IS NULL AND SHIFTID = ?"
            , SerializerWriteString.INSTANCE
            , SerializerReadString.INSTANCE);

        m_breakname = new StaticSentence(s
            , "SELECT NAME FROM breaks WHERE ID = ?"
            , SerializerWriteString.INSTANCE
            , SerializerReadString.INSTANCE);
    }

    /**
     * @deprecated Use {@link #getBreaksListAll()} instead.
     */
    @Deprecated
    public final SentenceList getBreaksList() {
        return new StaticSentence(s
            , "SELECT ID, NAME FROM breaks ORDER BY NAME"
            , null
            , new SerializerRead() {
                @Override
                public Object readValues(DataRead dr) throws BasicException {
                    return new BreaksInfo(dr.getString(1), dr.getString(2));
                }
            });
    }

    /**
     * @deprecated Use {@link #getLeavesListAll()} instead.
     */
    @Deprecated
    public final SentenceList getLeavesList() {
        return new StaticSentence(s
            , "SELECT ID, PPLID, NAME, STARTDATE, ENDDATE, NOTES FROM leaves ORDER BY NAME"
            , null
            , new SerializerRead() {
                @Override
                public Object readValues(DataRead dr) throws BasicException {
                    return new LeavesInfo(dr.getString(1), dr.getString(2), dr.getString(3), dr.getString(4), dr.getString(5), dr.getString(6));
                }
            });
    }

    @Override
    @SuppressWarnings("unchecked")
    public final List<BreaksInfo> getBreaksListAll() throws BasicException {
        return (List<BreaksInfo>) getBreaksList().list();
    }

    @Override
    @SuppressWarnings("unchecked")
    public final List<LeavesInfo> getLeavesListAll() throws BasicException {
        return (List<LeavesInfo>) getLeavesList().list();
    }

    @Override
    @SuppressWarnings("unchecked")
    public final List<Break> listBreaksVisible() throws BasicException {
        return m_breaksvisible.list();
    }      

    @Override
    public final void checkIn(String user) throws BasicException {
        Object[] value = new Object[] {UUID.randomUUID().toString(), new Date(), user};
        m_checkin.exec(value);
    }

    @Deprecated
    public final void CheckIn(String user) throws BasicException {
        checkIn(user);
    }

    @Override
    public final void checkOut(String user) throws BasicException {
        Object[] value = new Object[] {new Date(), user};
        m_checkout.exec(value);
    }

    @Deprecated
    public final void CheckOut(String user) throws BasicException {
        checkOut(user);
    }

    @Override
    public final boolean isCheckedIn(String user) throws BasicException {
        String data = (String) m_checkdate.find(user);
        return data != null && !data.equals("0");
    }

    @Deprecated
    public final boolean IsCheckedIn(String user) throws BasicException {
        return isCheckedIn(user);
    }

    @Override
    public final void startBreak(String userId, String breakId) throws BasicException {
        String shiftId = getShiftId(userId);
        Object[] value = new Object[] {UUID.randomUUID().toString(), shiftId, breakId, new Date()};
        m_startbreak.exec(value);
    }

    @Deprecated
    public final void StartBreak(String userId, String breakId) throws BasicException {
        startBreak(userId, breakId);
    }

    @Override
    public final void endBreak(String userId) throws BasicException {
        String shiftId = getShiftId(userId);
        Object[] value = new Object[] {new Date(), shiftId};
        m_endbreak.exec(value);
    }

    @Deprecated
    public final void EndBreak(String userId) throws BasicException {
        endBreak(userId);
    }

    @Override
    public final boolean isOnBreak(String user) throws BasicException {
        String shiftId = getShiftId(user);
        String data = (String) m_isonbreak.find(shiftId);
        return data != null && !data.equals("0");
    }

    @Deprecated
    public final boolean IsOnBreak(String user) throws BasicException {
        return isOnBreak(user);
    }

    @Override
    public final String getShiftId(String user) throws BasicException {
        return (String) m_shiftid.find(user);
    }

    @Deprecated
    public final String GetShiftID(String user) throws BasicException {
        return getShiftId(user);
    }

    @Override
    public final Date getLastCheckIn(String user) throws BasicException {
        return (Date) m_lastcheckin.find(user);
    }

    @Deprecated
    public final Date GetLastCheckIn(String user) throws BasicException {
        return getLastCheckIn(user);
    }

    @Override
    public final Date getLastCheckOut(String user) throws BasicException {
        return (Date) m_lastcheckout.find(user);
    }

    @Deprecated
    public final Date GetLastCheckOut(String user) throws BasicException {
        return getLastCheckOut(user);
    }

    @Override
    public final Date getStartBreakTime(String shiftId) throws BasicException {
        return (Date) m_startbreaktime.find(shiftId);
    }

    @Deprecated
    public final Date GetStartBreakTime(String shiftId) throws BasicException {
        return getStartBreakTime(shiftId);
    }

    @Override
    public final String getLastBreakId(String shiftId) throws BasicException {
        return (String) m_lastbreakid.find(shiftId);
    }

    @Deprecated
    public final String GetLastBreakID(String shiftId) throws BasicException {
        return getLastBreakId(shiftId);
    }

    @Override
    public final String getLastBreakName(String shiftId) throws BasicException {
        String breakId = getLastBreakId(shiftId);
        return (String) m_breakname.find(breakId);
    }

    @Deprecated
    public final String GetLastBreakName(String shiftId) throws BasicException {
        return getLastBreakName(shiftId);
    }

    @Override
    public final ShiftBreakActivity getLastBreakActivity(String userId) throws BasicException {
        String shiftId = getShiftId(userId);
        Date startBreakTime = getStartBreakTime(shiftId);
        String breakName = getLastBreakName(shiftId);
        return new ShiftBreakActivity(breakName, startBreakTime);
    }

    @Override
    public final Object[] getLastBreak(String user) throws BasicException {
        ShiftBreakActivity activity = getLastBreakActivity(user);
        return new Object[] {activity.breakName(), activity.startTime()};
    }

    @Deprecated
    public final Object[] GetLastBreak(String user) throws BasicException {
        return getLastBreak(user);
    }

    @Override
    public final boolean isOnLeave(String user) throws BasicException {
        Object[] value = new Object[] {new Date(), new Date(), user};
        
        SentenceFind m_isonleave = new StaticSentence(s
            , "SELECT COUNT(*) FROM leaves WHERE STARTDATE < ? AND ENDDATE > ? AND PPLID = ?"
            , new SerializerWriteBasic(new Datas[] {Datas.TIMESTAMP, Datas.TIMESTAMP, Datas.STRING})
            , SerializerReadInteger.INSTANCE);
        Integer data = (Integer) m_isonleave.find(value);
        return data != null && data > 0;
    }

    @Deprecated
    public final boolean IsOnLeave(String user) throws BasicException {
        return isOnLeave(user);
    }

    public SentenceList getEmployeeList() {
        String dbTrue = (s != null && s.DB != null) ? s.DB.TRUE() : "1";
        return new StaticSentence(s
            , new QBFBuilder("SELECT ID, NAME FROM people WHERE ROLE != '0' AND VISIBLE = " + dbTrue + " AND ?(QBF_FILTER) ORDER BY NAME", new String[] {"NAME"})
            , new SerializerWriteBasic(new Datas[] {Datas.OBJECT, Datas.STRING})
            , new SerializerRead() {
                @Override
                public Object readValues(DataRead dr) throws BasicException {
                    EmployeeInfo c = new EmployeeInfo(dr.getString(1));
                    c.setName(dr.getString(2));
                    return c;
                }
            });
    }

    @Override
    public ListProvider getEmployeeListProvider(EditorCreator filter) {
        return new ListProviderCreator(getEmployeeList(), filter);
    }

    @Override
    public void blockEmployee(String user) throws BasicException {
        if (isOnBreak(user)) {
            endBreak(user);
        }
        checkOut(user);
    }

    @Deprecated
    public void BlockEmployee(String user) throws BasicException {
        blockEmployee(user);
    }

    @Override
    public TableDefinition getTableBreaks() {
        return tbreaks;
    }

    @Override
    public TableDefinition getTableLeaves() {
        return tleaves;
    }

    @Override
    public EmployeeInfoExt loadEmployeeExt(String id) throws BasicException {
        return (EmployeeInfoExt) new PreparedSentence(s
                , "SELECT ID, NAME FROM people WHERE ID = ?"
                , SerializerWriteString.INSTANCE
                , new EmployeeExtRead()).find(id);
    }

    protected static class EmployeeExtRead implements SerializerRead {
        @Override
        public Object readValues(DataRead dr) throws BasicException {
            EmployeeInfoExt c = new EmployeeInfoExt(dr.getString(1));
            c.setName(dr.getString(2));
            return c;
        }
    }
}
