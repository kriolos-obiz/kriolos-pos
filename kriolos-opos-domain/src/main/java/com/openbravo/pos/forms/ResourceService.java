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

import com.openbravo.data.loader.TableDefinition;
import com.openbravo.pos.admin.ResourceInfo;
import java.awt.image.BufferedImage;
import java.util.Properties;

/**
 * Domain service port interface for application templates, XML definitions,
 * images, properties, and database resources.
 *
 * <p>Decouples receipt templates, print scripts, menu definitions, and UI assets
 * from the legacy {@code DataLogicSystem} god-object.</p>
 *
 * @author KriolOS
 */
public interface ResourceService {

    /**
     * Gets the table definition metadata for application resources.
     *
     * @return TableDefinition for {@link ResourceInfo}
     */
    TableDefinition<ResourceInfo> getTableResources();

    /**
     * Retrieves a resource by name as raw binary byte array.
     *
     * @param sName Resource name identifier
     * @return Raw resource bytes, or {@code null} if not found
     */
    byte[] getResourceAsBinary(String sName);

    /**
     * Retrieves a resource by name as a plain text string.
     *
     * @param sName Resource name identifier
     * @return Resource content as text, or {@code null}
     */
    String getResourceAsText(String sName);

    /**
     * Retrieves a resource by name formatted as an XML or script template.
     *
     * @param sName Resource name identifier
     * @return XML / script string content, or {@code null}
     */
    String getResourceAsXML(String sName);

    /**
     * Retrieves a resource by name converted into a {@link BufferedImage}.
     *
     * @param sName Resource name identifier
     * @return Decoded image, or {@code null}
     */
    BufferedImage getResourceAsImage(String sName);

    /**
     * Retrieves a resource by name loaded into a {@link Properties} map.
     *
     * @param sName Resource name identifier
     * @return Loaded Properties object (empty if resource missing or empty)
     */
    Properties getResourceAsProperties(String sName);

    /**
     * Persists or updates a resource entry in the database.
     *
     * @param name Resource name identifier
     * @param type Resource type code (e.g. 0 = text/xml, 1 = image, 2 = binary)
     * @param data Binary payload bytes
     */
    void setResource(String name, int type, byte[] data);

    /**
     * Persists or updates a binary resource entry in the database.
     *
     * @param sName Resource name identifier
     * @param data Binary payload bytes
     */
    void setResourceAsBinary(String sName, byte[] data);

    /**
     * Persists or updates an XML Properties configuration entry in the database.
     *
     * @param sName Resource name identifier
     * @param p Properties configuration to store as XML
     */
    void setResourceAsProperties(String sName, Properties p);
}
