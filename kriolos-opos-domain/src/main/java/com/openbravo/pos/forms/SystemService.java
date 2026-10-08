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
import java.util.Date;

/**
 * Domain service port interface for application runtime lifecycle, database metadata,
 * versioning, and physical cash drawer access events.
 *
 * <p>Extends {@link ResourceService} and {@link SecurityService} to provide a consolidated
 * port contract while enabling pure hexagonal decoupling from {@code DataLogicSystem}.</p>
 *
 * @author KriolOS
 */
public interface SystemService extends ResourceService, SecurityService {

    /**
     * Gets the database engine version or dialect name.
     *
     * @return Database version string
     */
    String getDBVersion();

    /**
     * Finds the application database schema version from the applications table.
     *
     * @return Schema version string
     * @throws BasicException if a database query error occurs
     */
    String findVersion() throws BasicException;

    /**
     * Retrieves the current system user name.
     *
     * @return User name string
     * @throws BasicException if an error occurs
     */
    String getUser() throws BasicException;

    /**
     * Records a physical cash drawer open audit event in the database.
     *
     * @param name User or terminal identifier opening the drawer
     * @param action Event description or associated receipt ID
     * @param openDate Timestamp of drawer opening
     * @throws BasicException if a database persistence error occurs
     */
    void execDrawerOpened(String name, String action, Date openDate) throws BasicException;
}
