//    KrOS POS
//    Copyright (c) 2019-2023 KriolOS
//    
//
//     
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
//    along with KrOS POS.  If not, see <http://www.gnu.org/licenses/>.

package com.openbravo.pos.pim;

import com.openbravo.data.loader.DataRead;
import com.openbravo.data.loader.DataWrite;
import com.openbravo.data.loader.IKeyed;
import com.openbravo.data.loader.ImageUtils;
import com.openbravo.data.loader.SerializerRead;
import com.openbravo.data.loader.SerializerWrite;
import java.awt.image.BufferedImage;
import java.io.Serializable;

/**
 * Domain entity representing a product category.
 *
 * @author Adrian
 * @author KriolOS
 */
public class CategoryInfo implements Serializable, IKeyed {

    private static final long serialVersionUID = 8612449444103L;
    private String id;
    private String parentId;
    private String name;
    private String textTip;
    private BufferedImage image;
    private Boolean catShowName = true;
    private String catalogOrder;
    private String catalogColor;
    private Boolean catalogEnabled = true;

    public CategoryInfo() {
        this(null, null, null, null, true);
    }

    public CategoryInfo(String id, String name) {
        this(id, name, null, null, true);
    }

    public CategoryInfo(String id, String name, BufferedImage image) {
        this(id, name, image, null, true);
    }

    public CategoryInfo(String id, String name, BufferedImage image, String textTip, Boolean showCatName) {
        this.id = id;
        this.name = name;
        this.image = image;
        this.textTip = textTip;
        this.catShowName = showCatName;
    }

    public CategoryInfo(String id, String name, String parentId, BufferedImage image, String textTip,
                        Boolean showCatName, String catalogOrder, String catalogColor, Boolean catalogEnabled) {
        this.id = id;
        this.name = name;
        this.parentId = parentId;
        this.image = image;
        this.textTip = textTip;
        this.catShowName = showCatName;
        this.catalogOrder = catalogOrder;
        this.catalogColor = catalogColor;
        this.catalogEnabled = catalogEnabled;
    }

    @Override
    public Object getKey() {
        return id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setID(String sID) {
        id = sID;
    }

    public String getID() {
        return id;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String sName) {
        name = sName;
    }

    public String getTextTip() {
        return textTip;
    }

    public void setTextTip(String sName) {
        textTip = sName;
    }

    public Boolean getCatShowName() {
        return catShowName;
    }

    public void setCatShowName(Boolean bcatshowname) {
        catShowName = bcatshowname;
    }

    public boolean isCatShowName() {
        return Boolean.TRUE.equals(catShowName);
    }

    public BufferedImage getImage() {
        return image;
    }

    public void setImage(BufferedImage img) {
        image = img;
    }

    public String getCatalogOrder() {
        return catalogOrder;
    }

    public void setCatalogOrder(String catalogOrder) {
        this.catalogOrder = catalogOrder;
    }
    
    public String getCatalogColor() {
        return catalogColor;
    }

    public void setCatalogColor(String color) {
        this.catalogColor = color;
    }
    
    public Boolean getCatalogEnabled() {
        return catalogEnabled;
    }

    public void setCatalogEnabled(Boolean catalogEnabled) {
        this.catalogEnabled = catalogEnabled;
    }

    public boolean isCatalogEnabled() {
        return Boolean.TRUE.equals(catalogEnabled);
    }

    @Override
    public String toString() {
        return name;
    }

    /**
     * Serializer for reading CategoryInfo from JDBC queries.
     * Columns: 1: ID, 2: NAME, 3: IMAGE, 4: TEXTTIP, 5: CATSHOWNAME, 6: CATORDER, 7: CATALOGCOLOR, 8: CATALOGENABLED, 9: PARENTID
     */
    public static SerializerRead<CategoryInfo> getSerializerRead() {
        return (DataRead dr) -> {
            CategoryInfo catInfo = new CategoryInfo(
                dr.getString(1),
                dr.getString(2),
                ImageUtils.readImage(dr.getBytes(3)),
                dr.getString(4),
                dr.getBoolean(5));
             
            catInfo.setCatalogOrder(dr.getString(6));
            catInfo.setCatalogColor(dr.getString(7));
            catInfo.setCatalogEnabled(dr.getBoolean(8));
            catInfo.setParentId(dr.getString(9));
            
            return catInfo;
        };
    }

    /**
     * Serializer for inserting CategoryInfo.
     */
    public static SerializerWrite<CategoryInfo> getSerializerInsert() {
        return (DataWrite dp, CategoryInfo cat) -> {
            dp.setString(1, cat.getId());
            dp.setString(2, cat.getName());
            dp.setBytes(3, ImageUtils.writeImage(cat.getImage()));
            dp.setString(4, cat.getTextTip());
            dp.setBoolean(5, cat.getCatShowName());
            dp.setString(6, cat.getCatalogOrder());
            dp.setString(7, cat.getCatalogColor());
            dp.setBoolean(8, cat.getCatalogEnabled());
            dp.setString(9, cat.getParentId());
        };
    }

    /**
     * Serializer for updating CategoryInfo.
     * Columns 1-9 in SET clause, Column 10: ID in WHERE clause.
     */
    public static SerializerWrite<CategoryInfo> getSerializerUpdate() {
        return (DataWrite dp, CategoryInfo cat) -> {
            dp.setString(1, cat.getId());
            dp.setString(2, cat.getName());
            dp.setBytes(3, ImageUtils.writeImage(cat.getImage()));
            dp.setString(4, cat.getTextTip());
            dp.setBoolean(5, cat.getCatShowName());
            dp.setString(6, cat.getCatalogOrder());
            dp.setString(7, cat.getCatalogColor());
            dp.setBoolean(8, cat.getCatalogEnabled());
            dp.setString(9, cat.getParentId());
            dp.setString(10, cat.getId());
        };
    }

    /**
     * Serializer for deleting CategoryInfo by ID.
     */
    public static SerializerWrite<CategoryInfo> getSerializerDelete() {
        return (DataWrite dp, CategoryInfo cat) -> {
            dp.setString(1, cat.getId());
        };
    }
}

