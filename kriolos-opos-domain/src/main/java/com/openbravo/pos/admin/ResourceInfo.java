/*
 * Copyright (C) 2022-2026 KriolOS
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

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.DataRead;
import com.openbravo.data.loader.DataWrite;
import com.openbravo.data.loader.IKeyed;
import com.openbravo.data.loader.SerializableRead;
import com.openbravo.data.loader.SerializerRead;
import com.openbravo.data.loader.SerializerWrite;

/**
 * Domain entity representing an application resource (template, image, script, properties).
 *
 * @author poolborges
 * @author KriolOS
 */
public class ResourceInfo implements SerializableRead, IKeyed<String> {
    
    private static final long serialVersionUID = 9110127845966L;
    
    private String id;
    private String name;
    private int restype;
    private byte[] content;

    /**
     * Default constructor for serialization and reflection.
     */
    public ResourceInfo() {
    }

    /**
     * Creates a new fully initialized ResourceInfo entity.
     *
     * @param id Unique resource identifier
     * @param name Unique resource name
     * @param restype Resource type code (0 = text/xml, 1 = image, 2 = binary)
     * @param content Binary payload content
     */
    public ResourceInfo(String id, String name, int restype, byte[] content) {
        this.id = id;
        this.name = name;
        this.restype = restype;
        this.content = content;
    }

    @Override
    public void readValues(DataRead dr) throws BasicException {
        id = dr.getString(1);
        name = dr.getString(2);
        restype = dr.getInt(3);
        content = dr.getBytes(4);
    }

    @Override
    public String getKey() {
        return id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getID() {
        return id;
    }

    public void setID(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getRestype() {
        return restype;
    }

    public void setRestype(int restype) {
        this.restype = restype;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return name;
    }

    /**
     * Serializer for reading ResourceInfo from JDBC queries.
     * Columns: 1: ID, 2: NAME, 3: RESTYPE, 4: CONTENT
     */
    public static SerializerRead<ResourceInfo> getSerializerRead() {
        return (DataRead dr) -> new ResourceInfo(
                dr.getString(1),
                dr.getString(2),
                dr.getInt(3),
                dr.getBytes(4)
        );
    }

    /**
     * Serializer for inserting ResourceInfo.
     */
    public static SerializerWrite<ResourceInfo> getSerializerInsert() {
        return (DataWrite dp, ResourceInfo r) -> {
            dp.setString(1, r.getId());
            dp.setString(2, r.getName());
            dp.setInt(3, r.getRestype());
            dp.setBytes(4, r.getContent());
        };
    }

    /**
     * Serializer for updating ResourceInfo.
     * Columns 1-4 in SET clause, Column 5: ID in WHERE clause.
     */
    public static SerializerWrite<ResourceInfo> getSerializerUpdate() {
        return (DataWrite dp, ResourceInfo r) -> {
            dp.setString(1, r.getId());
            dp.setString(2, r.getName());
            dp.setInt(3, r.getRestype());
            dp.setBytes(4, r.getContent());
            dp.setString(5, r.getId());
        };
    }

    /**
     * Serializer for deleting ResourceInfo by ID.
     */
    public static SerializerWrite<ResourceInfo> getSerializerDelete() {
        return (DataWrite dp, ResourceInfo r) -> {
            dp.setString(1, r.getId());
        };
    }
}
