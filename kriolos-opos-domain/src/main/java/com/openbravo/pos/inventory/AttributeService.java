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
package com.openbravo.pos.inventory;

import com.openbravo.basic.BasicException;
import java.util.List;

/**
 * Service port interface for product attributes, attribute sets, and attribute instances.
 */
public interface AttributeService {

    /** SQL template for attribute use list. */
    String SQL_ATTRIBUTE_USE_LIST = DataLogicAttribute.SQL_ATTRIBUTE_USE_LIST;

    /** SQL template for attribute value list. */
    String SQL_ATTRIBUTE_VALUE_LIST = DataLogicAttribute.SQL_ATTRIBUTE_VALUE_LIST;

    /**
     * Retrieves all configured attributes ordered by name.
     *
     * @return list of AttributeInfo
     * @throws BasicException on persistence error
     */
    List<AttributeInfo> getAttributeList() throws BasicException;

    /**
     * Retrieves all configured attribute sets ordered by name.
     *
     * @return list of AttributeSetInfo
     * @throws BasicException on persistence error
     */
    List<AttributeSetInfo> getAttributeSetList() throws BasicException;

    /**
     * Finds an attribute set by its ID.
     *
     * @param attributeSetId attribute set ID
     * @return AttributeSetInfo or null if not found
     * @throws BasicException on persistence error
     */
    AttributeSetInfo findAttributeSet(String attributeSetId) throws BasicException;

    /**
     * Retrieves attribute instance definition information for a set,
     * optionally populated with existing values for an instance.
     *
     * @param attributeSetId         attribute set ID
     * @param attributeSetInstanceId optional instance ID
     * @return list of AttributeInstInfo
     * @throws BasicException on persistence error
     */
    List<AttributeInstInfo> getAttributeInstList(String attributeSetId, String attributeSetInstanceId) throws BasicException;

    /**
     * Retrieves allowed/configured string values for a specific attribute ID.
     *
     * @param attributeId attribute ID
     * @return list of value strings
     * @throws BasicException on persistence error
     */
    List<String> getAttributeValues(String attributeId) throws BasicException;

    /**
     * Checks if an attribute set instance with the exact set ID and description exists.
     *
     * @param attributeSetId attribute set ID
     * @param description    combined description string
     * @return instance ID if found, or null
     * @throws BasicException on persistence error
     */
    String findAttributeSetInstanceId(String attributeSetId, String description) throws BasicException;

    /**
     * Finds an existing attribute set instance or creates a new one with its associated attribute value entries.
     *
     * @param attributeSetId attribute set ID
     * @param description    combined description string
     * @param entries        list of attribute values
     * @return attribute set instance ID, or null if description is empty
     * @throws BasicException on persistence error
     */
    String findOrCreateAttributeSetInstance(String attributeSetId, String description, List<AttributeInstEntry> entries) throws BasicException;
}
