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
package com.openbravo.pos.forms;

import com.openbravo.basic.BasicException;
import java.util.List;

/**
 * Domain service port interface for runtime authentication, user lookup, and authorization.
 *
 * <p>Decouples login, user verification, card scanning, and permissions loading
 * from legacy {@code DataLogicSystem} god-object implementations.</p>
 *
 * @author KriolOS
 */
public interface SecurityService {

    /**
     * Lists all users flagged as visible for POS login.
     *
     * @return List of visible {@link AppUser} entities
     * @throws BasicException if a database error occurs
     */
    List<AppUser> listPeopleVisible() throws BasicException;

    /**
     * Finds a user entity associated with an encoded card key or badge swipe.
     *
     * @param card Scanned card value
     * @return Matching {@link AppUser}, or {@code null} if not found
     * @throws BasicException if a database error occurs
     */
    AppUser findPeopleByCard(String card) throws BasicException;

    /**
     * Finds raw XML permission definitions configured for a role identifier.
     *
     * @param sRole Role ID
     * @return XML string defining permission classes, or empty string
     */
    String findRolePermissions(String sRole);

    /**
     * Retrieves specific custom permission strings granted to a role.
     *
     * @param role Role ID
     * @return List of permission string keys
     * @throws BasicException if a database error occurs
     */
    List<String> getPermissions(String role) throws BasicException;

    /**
     * Executes a password update for a user.
     *
     * @param userdata Array containing [newPasswordHash, userId]
     * @throws BasicException if update fails
     */
    void execChangePassword(Object[] userdata) throws BasicException;

    /**
     * Inserts or updates custom role permission entries.
     *
     * @param permissions Array containing [roleId, permissionsXml]
     * @throws BasicException if update fails
     */
    void execUpdatePermissions(Object[] permissions) throws BasicException;
}
