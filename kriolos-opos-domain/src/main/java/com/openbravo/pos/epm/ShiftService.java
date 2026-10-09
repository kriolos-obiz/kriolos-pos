/*
 * Copyright (C) 2026 KriolOS POS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.openbravo.pos.epm;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.TableDefinition;
import com.openbravo.data.user.EditorCreator;
import com.openbravo.data.user.ListProvider;
import java.util.Date;
import java.util.List;

/**
 * Domain service port interface for employee shift tracking, presence management,
 * breaks, leaves, and time clock operations.
 */
public interface ShiftService {

    /**
     * Checks in an employee, opening a new active shift.
     *
     * @param userId employee identifier
     * @throws BasicException if database operation fails
     */
    void checkIn(String userId) throws BasicException;

    /**
     * Checks out an employee, closing their current active shift.
     *
     * @param userId employee identifier
     * @throws BasicException if database operation fails
     */
    void checkOut(String userId) throws BasicException;

    /**
     * Verifies if an employee currently has an open (active) shift.
     *
     * @param userId employee identifier
     * @return {@code true} if checked in, {@code false} otherwise
     * @throws BasicException if database query fails
     */
    boolean isCheckedIn(String userId) throws BasicException;

    /**
     * Starts a break for the employee's current active shift.
     *
     * @param userId employee identifier
     * @param breakId break type identifier
     * @throws BasicException if database operation fails
     */
    void startBreak(String userId, String breakId) throws BasicException;

    /**
     * Ends the active break for the employee's current shift.
     *
     * @param userId employee identifier
     * @throws BasicException if database operation fails
     */
    void endBreak(String userId) throws BasicException;

    /**
     * Verifies if an employee is currently on break.
     *
     * @param userId employee identifier
     * @return {@code true} if currently on break, {@code false} otherwise
     * @throws BasicException if database query fails
     */
    boolean isOnBreak(String userId) throws BasicException;

    /**
     * Retrieves the ID of the employee's current open shift.
     *
     * @param userId employee identifier
     * @return shift ID or {@code null} if no active shift
     * @throws BasicException if database query fails
     */
    String getShiftId(String userId) throws BasicException;

    /**
     * Retrieves the start timestamp of the employee's current active shift.
     *
     * @param userId employee identifier
     * @return check-in timestamp or {@code null}
     * @throws BasicException if database query fails
     */
    Date getLastCheckIn(String userId) throws BasicException;

    /**
     * Retrieves the end timestamp of the employee's most recent closed shift.
     *
     * @param userId employee identifier
     * @return check-out timestamp or {@code null}
     * @throws BasicException if database query fails
     */
    Date getLastCheckOut(String userId) throws BasicException;

    /**
     * Retrieves the start timestamp of the active break for the specified shift.
     *
     * @param shiftId shift identifier
     * @return break start timestamp or {@code null}
     * @throws BasicException if database query fails
     */
    Date getStartBreakTime(String shiftId) throws BasicException;

    /**
     * Retrieves the break ID for the active break on the specified shift.
     *
     * @param shiftId shift identifier
     * @return break ID or {@code null}
     * @throws BasicException if database query fails
     */
    String getLastBreakId(String shiftId) throws BasicException;

    /**
     * Retrieves the break name for the active break on the specified shift.
     *
     * @param shiftId shift identifier
     * @return break name or {@code null}
     * @throws BasicException if database query fails
     */
    String getLastBreakName(String shiftId) throws BasicException;

    /**
     * Retrieves the active break activity (name and start time) for the specified employee.
     *
     * @param userId employee identifier
     * @return break activity details or {@code null} if not on break
     * @throws BasicException if database query fails
     */
    ShiftBreakActivity getLastBreakActivity(String userId) throws BasicException;

    /**
     * Legacy adapter returning an Object array {@code [breakName, startBreakTime]}.
     *
     * @param userId employee identifier
     * @return object array with break details
     * @throws BasicException if database query fails
     */
    Object[] getLastBreak(String userId) throws BasicException;

    /**
     * Verifies if an employee is currently on an active leave.
     *
     * @param userId employee identifier
     * @return {@code true} if on leave, {@code false} otherwise
     * @throws BasicException if database query fails
     */
    boolean isOnLeave(String userId) throws BasicException;

    /**
     * Blocks an employee by ending any ongoing break and checking out.
     *
     * @param userId employee identifier
     * @throws BasicException if database operation fails
     */
    void blockEmployee(String userId) throws BasicException;

    /**
     * Lists all visible break types available for employee selection.
     *
     * @return list of visible break types
     * @throws BasicException if database query fails
     */
    List<Break> listBreaksVisible() throws BasicException;

    /**
     * Retrieves all break definitions ordered by name.
     *
     * @return list of breaks
     * @throws BasicException if database query fails
     */
    List<BreaksInfo> getBreaksListAll() throws BasicException;

    /**
     * Retrieves all leave records ordered by employee name.
     *
     * @return list of leaves
     * @throws BasicException if database query fails
     */
    List<LeavesInfo> getLeavesListAll() throws BasicException;

    /**
     * Loads extended employee details by ID.
     *
     * @param id employee identifier
     * @return extended employee details or {@code null}
     * @throws BasicException if database query fails
     */
    EmployeeInfoExt loadEmployeeExt(String id) throws BasicException;

    /**
     * Returns the table definition for breaks.
     *
     * @return breaks table definition
     */
    TableDefinition getTableBreaks();

    /**
     * Returns the table definition for leaves.
     *
     * @return leaves table definition
     */
    TableDefinition getTableLeaves();

    /**
     * Creates a list provider for employee lookup and filtering.
     *
     * @param filter editor creator containing search parameters
     * @return list provider
     */
    ListProvider getEmployeeListProvider(EditorCreator filter);
}
