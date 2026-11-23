//    KriolOS POS
//    Copyright (c) 2019-2023 KriolOS
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

package com.openbravo.pos.sales.restaurant;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.DataRead;
import com.openbravo.data.loader.DataWrite;
import com.openbravo.data.loader.IKeyed;
import com.openbravo.data.loader.ImageUtils;
import com.openbravo.data.loader.SerializableRead;
import com.openbravo.data.loader.SerializerRead;
import com.openbravo.data.loader.SerializerWrite;
import java.awt.image.BufferedImage;
import java.io.Serializable;

/**
 * Floor metadata and layout model for restaurant hospitality management.
 *
 * @author adrianromero
 * @author KriolOS
 */
public class FloorsInfo implements SerializableRead, Serializable, IKeyed {
    
    private static final long serialVersionUID = 8906929819402L;
    
    private String id;
    private String name;
    private BufferedImage image;
    
    /** Creates a new instance of FloorsInfo */
    public FloorsInfo() {
        this(null, null, null);
    }

    /**
     * Constructs a FloorsInfo with id and name.
     *
     * @param id floor identifier
     * @param name floor display name
     */
    public FloorsInfo(String id, String name) {
        this(id, name, null);
    }

    /**
     * Constructs a FloorsInfo with id, name, and floor layout image.
     *
     * @param id floor identifier
     * @param name floor display name
     * @param image floor layout image
     */
    public FloorsInfo(String id, String name, BufferedImage image) {
        this.id = id;
        this.name = name;
        this.image = image;
    }
   
    @Override
    public Object getKey() {
        return id;
    }

    @Override
    public void readValues(DataRead dr) throws BasicException {
        id = dr.getString(1);
        name = dr.getString(2);
    }

    public void setID(String sID) {
        id = sID;
    }

    public String getID() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String sName) {
        name = sName;
    } 

    public BufferedImage getImage() {
        return image;
    }

    public void setImage(BufferedImage image) {
        this.image = image;
    }
    
    @Override
    public String toString() {
        return name;
    }

    /**
     * Returns a {@link SerializerRead} for deserializing FloorsInfo from JDBC queries.
     *
     * @return row serializer for {@link FloorsInfo}
     */
    public static SerializerRead<FloorsInfo> getSerializerRead() {
        return (DataRead dr) -> new FloorsInfo(dr.getString(1), dr.getString(2), ImageUtils.readImage(dr.getBytes(3)));
    }

    /**
     * Returns a {@link SerializerWrite} for inserting FloorsInfo records.
     * Binding: 1: ID, 2: NAME, 3: IMAGE
     *
     * @return serializer for INSERT statement
     */
    public static SerializerWrite<FloorsInfo> getSerializerInsert() {
        return (DataWrite dp, FloorsInfo info) -> {
            dp.setString(1, info.getID());
            dp.setString(2, info.getName());
            dp.setBytes(3, ImageUtils.writeImage(info.getImage()));
        };
    }

    /**
     * Returns a {@link SerializerWrite} for updating FloorsInfo records.
     * Binding: 1: ID, 2: NAME, 3: IMAGE, 4: ID (WHERE id = ?)
     *
     * @return serializer for UPDATE statement
     */
    public static SerializerWrite<FloorsInfo> getSerializerUpdate() {
        return (DataWrite dp, FloorsInfo info) -> {
            dp.setString(1, info.getID());
            dp.setString(2, info.getName());
            dp.setBytes(3, ImageUtils.writeImage(info.getImage()));
            dp.setString(4, info.getID());
        };
    }

    /**
     * Returns a {@link SerializerWrite} for deleting FloorsInfo records.
     * Binding: 1: ID (WHERE id = ?)
     *
     * @return serializer for DELETE statement
     */
    public static SerializerWrite<FloorsInfo> getSerializerDelete() {
        return (DataWrite dp, FloorsInfo info) -> {
            dp.setString(1, info.getID());
        };
    }
}
