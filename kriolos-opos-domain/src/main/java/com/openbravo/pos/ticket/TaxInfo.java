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

package com.openbravo.pos.ticket;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.DataRead;
import com.openbravo.data.loader.DataWrite;
import com.openbravo.data.loader.IKeyed;
import com.openbravo.data.loader.SerializerRead;
import com.openbravo.data.loader.SerializerWrite;
import java.io.Serializable;

/**
 *
 * @author adrianromero
 */
public class TaxInfo implements Serializable, IKeyed<String> {
    
    private static final long serialVersionUID = -2705212098856473043L;
    private String id;
    private String name;
    private String taxCategoryId; // Product Tax Category ID
    private String taxCustCategoryId; // Customer Tax Category ID
    private String parentid;
    
    private double rate;
    private boolean isCascade;
    private Integer order;
    
    /**
     * Default no-arg constructor.
     */
    public TaxInfo() {
    }

    /** 
     * Creates new TaxInfo
     * 
     * @param id Tax ID
     * @param name Tax name
     * @param taxcategoryid Product Tax Category ID
     * @param taxcustcategoryid Customer Tax Category ID
     * @param rate Tax rate
     * @param cascade if tax calculation is cascade
     * @param parentid Parent Tax ID
     * @param order (Order/Position)
     */
    public TaxInfo(String id, String name, String taxcategoryid, String taxcustcategoryid, 
            String parentid, double rate, boolean cascade, Integer order) {
        this.id = id;
        this.name = name;
        this.taxCategoryId = taxcategoryid;
        this.taxCustCategoryId = taxcustcategoryid;
        this.parentid = parentid;
        
        this.rate = rate;
        this.isCascade = cascade;
        this.order = order;
    }
    
    /**
     * Get Tax ID
     * 
     * @return Tax ID
     */
    @Override
    public String getKey() {
        return id;
    }
    
    /**
     * Set Tax ID
     * @param value
     */
    public void setID(String value) {
        id = value;
    }
    
    /**
     *  Get Tax ID
     * 
     * @return
     */
    public String getId() {
        return id;
    }

    /**
     *
     * @return
     */
    public String getName() {
        return name;
    }
    
    /**
     *
     * @param value
     */
    public void setName(String value) {
        name = value;
    }

    /**
     * Get Product Tax Category ID
     * 
     * @return
     */
    public String getTaxCategoryID() {
        return taxCategoryId;
    }
    
    /**
     *
     * @param value
     */
    public void setTaxCategoryID(String value) {
        taxCategoryId = value;
    }

    /**
     * Get Customer Tax Category ID
     * @return
     */
    public String getTaxCustCategoryID() {
        return taxCustCategoryId;
    }
    
    /**
     *
     * @param value
     */
    public void setTaxCustCategoryID(String value) {
        taxCustCategoryId = value;
    }    

    /**
     *
     * @return
     */
    public String getParentID() {
        return parentid;
    }
    
    /**
     *
     * @param value
     */
    public void setParentID(String value) {
        parentid = value;
    }
    
    /**
     *
     * @return
     */
    public double getRate() {
        return rate;
    }
    
    /**
     *
     * @param value
     */
    public void setRate(double value) {
        rate = value;
    }

    /**
     *
     * @return
     */
    public boolean isCascade() {
        return isCascade;
    }
    
    /**
     *
     * @param value
     */
    public void setCascade(boolean value) {
        isCascade = value;
    }
    
    /**
     * Get Tax Order/Position
     * 
     * @return
     */
    public Integer getOrder() {
        return order;
    }
    
    /**
     *
     * @return
     */
    public Integer getApplicationOrder() {
        return order != null ?  order: Integer.MAX_VALUE;
    }

    /**
     *
     * @param value
     */
    public void setOrder(Integer value) {
        order = value;
    }

    public String getTaxCategoryId() {
        return taxCategoryId;
    }

    public void setTaxCategoryId(String taxCategoryId) {
        this.taxCategoryId = taxCategoryId;
    }

    public String getTaxCustCategoryId() {
        return taxCustCategoryId;
    }

    public void setTaxCustCategoryId(String taxCustCategoryId) {
        this.taxCustCategoryId = taxCustCategoryId;
    }

    public String getParentId() {
        return parentid;
    }

    public void setParentId(String parentId) {
        this.parentid = parentId;
    }

    public boolean getIsCascade() {
        return isCascade;
    }

    public void setIsCascade(boolean isCascade) {
        this.isCascade = isCascade;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString(){
        return name;
    }

    /**
     * Row mapper deserializing a {@link TaxInfo} instance from a JDBC result set.
     *
     * @return deserializer for {@link TaxInfo}
     */
    public static SerializerRead<TaxInfo> getSerializerRead() {
        return (DataRead dr) -> new TaxInfo(
                dr.getString(1),
                dr.getString(2),
                dr.getString(3),
                dr.getString(4),
                dr.getString(5),
                dr.getDouble(6) != null ? dr.getDouble(6) : 0.0,
                dr.getBoolean(7) != null && dr.getBoolean(7),
                dr.getInt(8));
    }

    /**
     * Parameter serializer for inserting a new {@link TaxInfo} record.
     *
     * @return serializer for INSERT statements
     */
    public static SerializerWrite<TaxInfo> getSerializerInsert() {
        return (DataWrite dp, TaxInfo info) -> {
            dp.setString(1, info.getId());
            dp.setString(2, info.getName());
            dp.setString(3, info.getTaxCategoryID());
            dp.setString(4, info.getTaxCustCategoryID());
            dp.setString(5, info.getParentID());
            dp.setDouble(6, info.getRate());
            dp.setBoolean(7, info.isCascade());
            dp.setInt(8, info.getOrder());
        };
    }

    /**
     * Parameter serializer for updating an existing {@link TaxInfo} record.
     *
     * @return serializer for UPDATE statements
     */
    public static SerializerWrite<TaxInfo> getSerializerUpdate() {
        return (DataWrite dp, TaxInfo info) -> {
            dp.setString(1, info.getId());
            dp.setString(2, info.getName());
            dp.setString(3, info.getTaxCategoryID());
            dp.setString(4, info.getTaxCustCategoryID());
            dp.setString(5, info.getParentID());
            dp.setDouble(6, info.getRate());
            dp.setBoolean(7, info.isCascade());
            dp.setInt(8, info.getOrder());
            dp.setString(9, info.getId());
        };
    }

    /**
     * Parameter serializer for deleting an existing {@link TaxInfo} record.
     *
     * @return serializer for DELETE statements
     */
    public static SerializerWrite<TaxInfo> getSerializerDelete() {
        return (DataWrite dp, TaxInfo info) -> {
            dp.setString(1, info.getId());
        };
    }
}
