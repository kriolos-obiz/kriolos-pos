/*
 * Copyright (C) 2026 KriolOS
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
package com.openbravo.pos.admin;

import com.openbravo.data.loader.SentenceList;
import com.openbravo.data.loader.TableDefinition;
import com.openbravo.data.user.SaveProvider;

/**
 * Domain service port interface for user and role administration.
 *
 * <p>Decouples presentation controllers and editors (such as {@code PeoplePanel},
 * {@code RolesPanel}, and {@code JPeopleFinderPanel}) from concrete data access
 * logic implementations.</p>
 *
 * @author KriolOS
 */
public interface PeopleService {

    /**
     * Gets the table definition metadata and column mappings for people/users.
     *
     * @return TableDefinition for {@link PeopleInfo}
     */
    TableDefinition<PeopleInfo> getTablePeople();

    /**
     * Gets the table definition metadata and column mappings for roles.
     *
     * @return TableDefinition for {@link RoleInfo}
     */
    TableDefinition<RoleInfo> getTableRoles();

    /**
     * Gets the record save provider for inserting, updating, and deleting people.
     *
     * @return SaveProvider for people record data arrays
     */
    SaveProvider<Object[]> getPeopleSaveProvider();

    /**
     * Gets the query sentence for retrieving the list of users ordered by name.
     *
     * @return SentenceList yielding {@link PeopleInfo}
     */
    SentenceList<PeopleInfo> getPeopleList();

    /**
     * Gets the query sentence for retrieving the list of roles ordered by name.
     *
     * @return SentenceList yielding {@link RoleInfo}
     */
    SentenceList<RoleInfo> getRolesList();

    /**
     * Gets the query sentence for retrieving the list of resources.
     *
     * @return SentenceList yielding {@link ResourceInfo}
     */
    SentenceList<ResourceInfo> getResourceList();
}
