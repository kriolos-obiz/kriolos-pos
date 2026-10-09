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
package com.openbravo.pos.sales.restaurant;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.SentenceExec;
import com.openbravo.data.loader.SentenceList;
import com.openbravo.data.loader.TableDefinition;
import com.openbravo.data.user.EditorCreator;
import com.openbravo.data.user.ListProvider;
import com.openbravo.data.user.SaveProvider;
import java.util.List;

/**
 * Pure domain service port interface for Restaurant and Hospitality operations
 * (Floors, Places layout, and Table Reservations).
 */
public interface RestaurantService {

    /**
     * Retrieves all configured floors as a clean domain list.
     *
     * @return list of floors
     * @throws BasicException on database access error
     */
    List<FloorsInfo> getFloorsListAll() throws BasicException;

    /**
     * Retrieves all floor tables / places with seating capacities.
     *
     * @return list of floor tables
     * @throws BasicException on database access error
     */
    List<FloorsInfo> getFloorTablesListAll() throws BasicException;

    /**
     * Table definition for floors table editing.
     *
     * @return table definition for floors
     */
    TableDefinition getTableFloors();

    /**
     * Table definition for places table editing.
     *
     * @return table definition for places
     */
    TableDefinition getTablePlaces();

    /**
     * Updates coordinates (x, y) for a dining place/table on the visual floor plan.
     *
     * @param x coordinate X
     * @param y coordinate Y
     * @param id place identifier
     * @throws BasicException on database error
     */
    void updatePlaces(int x, int y, String id) throws BasicException;

    /**
     * Returns a ListProvider for reservations filtered by date range.
     *
     * @param filter editor creator containing date range filter params
     * @return high-level ListProvider
     */
    ListProvider getReservationsListProvider(EditorCreator filter);

    /**
     * Returns a SaveProvider managing insert, update, and delete transactions for reservations.
     *
     * @return high-level SaveProvider
     */
    SaveProvider getReservationsSaveProvider();

    /**
     * @deprecated Low-level SentenceList query for floors. Use {@link #getFloorsListAll()} instead.
     */
    @Deprecated
    SentenceList<FloorsInfo> getFloorsList();

    /**
     * @deprecated Low-level SentenceList query for tables. Use {@link #getFloorTablesListAll()} instead.
     */
    @Deprecated
    SentenceList<FloorsInfo> getFloorTablesList();

    /**
     * @deprecated Low-level SentenceList query for reservations. Use {@link #getReservationsListProvider(EditorCreator)} instead.
     */
    @Deprecated
    SentenceList getReservationsList();

    /**
     * @deprecated Low-level SentenceExec for reservation update. Use {@link #getReservationsSaveProvider()} instead.
     */
    @Deprecated
    SentenceExec getReservationsUpdate();

    /**
     * @deprecated Low-level SentenceExec for reservation delete. Use {@link #getReservationsSaveProvider()} instead.
     */
    @Deprecated
    SentenceExec getReservationsDelete();

    /**
     * @deprecated Low-level SentenceExec for reservation insert. Use {@link #getReservationsSaveProvider()} instead.
     */
    @Deprecated
    SentenceExec getReservationsInsert();
}
