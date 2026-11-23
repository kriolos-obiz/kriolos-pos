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

package com.openbravo.pos.admin;

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
 * Domain entity representing a user/person in the POS security model.
 *
 * @author adrianromero
 * @author KriolOS
 */
public class PeopleInfo implements SerializableRead, Serializable, IKeyed<String> {
    
    private static final long serialVersionUID = 9110127845966L;
    private String id;
    private String name;
    private String appPassword;
    private String role;
    private boolean visible = true;
    private String card;
    private BufferedImage image;
    
    public PeopleInfo() {
        this(null, null, null, null, true, null, null);
    }

    public PeopleInfo(String id, String name) {
        this(id, name, null, null, true, null, null);
    }

    public PeopleInfo(String id, String name, String appPassword, String role, boolean visible, String card, BufferedImage image) {
        this.id = id;
        this.name = name;
        this.appPassword = appPassword;
        this.role = role;
        this.visible = visible;
        this.card = card;
        this.image = image;
    }

    @Override
    public String getKey() {
        return id;
    }

    @Override
    public void readValues(DataRead dr) throws BasicException {
        id = dr.getString(1);
        name = dr.getString(2);
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

    public void setID(String sID) {
        this.id = sID;
    }

    public String getName() {
        return name;
    }

    public void setName(String sValue) {
        this.name = sValue;
    }

    public String getAppPassword() {
        return appPassword;
    }

    public void setAppPassword(String appPassword) {
        this.appPassword = appPassword;
    }

    public String getPassword() {
        return appPassword;
    }

    public void setPassword(String password) {
        this.appPassword = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public String getCard() {
        return card;
    }

    public void setCard(String card) {
        this.card = card;
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
     * Serializer for reading PeopleInfo from JDBC queries.
     * Columns: 1: ID, 2: NAME, 3: APPPASSWORD, 4: ROLE, 5: VISIBLE, 6: CARD, 7: IMAGE
     */
    public static SerializerRead<PeopleInfo> getSerializerRead() {
        return (DataRead dr) -> {
            Boolean vis = dr.getBoolean(5);
            return new PeopleInfo(
                dr.getString(1),
                dr.getString(2),
                dr.getString(3),
                dr.getString(4),
                vis != null ? vis : false,
                dr.getString(6),
                ImageUtils.readImage(dr.getBytes(7))
            );
        };
    }

    /**
     * Serializer for inserting PeopleInfo.
     */
    public static SerializerWrite<PeopleInfo> getSerializerInsert() {
        return (DataWrite dp, PeopleInfo p) -> {
            dp.setString(1, p.getId());
            dp.setString(2, p.getName());
            dp.setString(3, p.getAppPassword());
            dp.setString(4, p.getRole());
            dp.setBoolean(5, p.isVisible());
            dp.setString(6, p.getCard());
            dp.setBytes(7, ImageUtils.writeImage(p.getImage()));
        };
    }

    /**
     * Serializer for updating PeopleInfo.
     * Columns 1-7 in SET clause, Column 8: ID in WHERE clause.
     */
    public static SerializerWrite<PeopleInfo> getSerializerUpdate() {
        return (DataWrite dp, PeopleInfo p) -> {
            dp.setString(1, p.getId());
            dp.setString(2, p.getName());
            dp.setString(3, p.getAppPassword());
            dp.setString(4, p.getRole());
            dp.setBoolean(5, p.isVisible());
            dp.setString(6, p.getCard());
            dp.setBytes(7, ImageUtils.writeImage(p.getImage()));
            dp.setString(8, p.getId());
        };
    }

    /**
     * Serializer for deleting PeopleInfo by ID.
     */
    public static SerializerWrite<PeopleInfo> getSerializerDelete() {
        return (DataWrite dp, PeopleInfo p) -> {
            dp.setString(1, p.getId());
        };
    }
}

