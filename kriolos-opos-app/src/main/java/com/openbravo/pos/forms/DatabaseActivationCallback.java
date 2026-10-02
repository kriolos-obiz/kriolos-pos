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

import com.openbravo.pos.forms.AppProperties.DatabaseConfig;

/**
 * Asynchronous callback contract for database activation and migration tasks.
 *
 * <p>Notifies callers of real-time progress steps, successful completion,
 * and error conditions during background database connection and initialization.</p>
 */
public interface DatabaseActivationCallback {

    /**
     * Invoked on the Event Dispatch Thread (EDT) when a progress milestone occurs.
     *
     * @param statusMessage Localized human-readable status description of the current task.
     */
    void onProgress(String statusMessage);

    /**
     * Invoked on the Event Dispatch Thread (EDT) when database initialization and migrations succeed.
     *
     * @param activatedConfig The database configuration that was activated.
     * @param dlSystem The newly initialized DataLogicSystem.
     */
    void onSuccess(DatabaseConfig activatedConfig, DataLogicSystem dlSystem);

    /**
     * Invoked on the Event Dispatch Thread (EDT) if an error occurs during connection, migration, or setup.
     *
     * @param error The root cause exception.
     */
    void onError(Throwable error);
}
