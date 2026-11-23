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

package com.openbravo.pos.suppliers;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.DataRead;
import com.openbravo.data.loader.DataWrite;
import com.openbravo.data.loader.SerializerRead;
import com.openbravo.data.loader.SerializerWrite;
import com.openbravo.pos.businesspartner.BusinessPartner;
import java.util.Date;

/**
 * Domain entity representing a Supplier in KriolOS POS.
 *
 * @author Jack Gerrard
 * @author KriolOS
 */
public class SupplierInfo extends BusinessPartner {
    
    private static final long serialVersionUID = 9093257536541L;

    protected Double maxdebt;
    protected String address;
    protected String address2;
    protected String city;
    protected String region;
    protected String country;
    protected String firstname;
    protected String lastname;
    protected String phone2;
    protected String fax;
    protected String notes;
    protected boolean visible = true;
    protected Date curdate;
    protected Double curdebt;
    protected String vatid;

    public SupplierInfo() {
        this(null);
    }

    public SupplierInfo(String id) {
        super(id);
    }

    public SupplierInfo(String id, String searchkey, String name) {
        super(id, searchkey, name);
    }

    public Double getMaxdebt() {
        return maxdebt;
    }

    public void setMaxdebt(Double maxdebt) {
        this.maxdebt = maxdebt;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAddress2() {
        return address2;
    }

    public void setAddress2(String address2) {
        this.address2 = address2;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getPhone2() {
        return phone2;
    }

    public void setPhone2(String phone2) {
        this.phone2 = phone2;
    }

    public String getFax() {
        return fax;
    }

    public void setFax(String fax) {
        this.fax = fax;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public Date getCurdate() {
        return curdate;
    }

    public void setCurdate(Date curdate) {
        this.curdate = curdate;
    }

    public Double getCurdebt() {
        return curdebt;
    }

    public void setCurdebt(Double curdebt) {
        this.curdebt = curdebt;
    }

    public String getVatid() {
        return vatid;
    }

    public void setVatid(String vatid) {
        this.vatid = vatid;
    }

    public String getSupplierVATID() {
        return vatid;
    }

    public void setSupplierVATID(String vatid) {
        this.vatid = vatid;
    }

    /**
     * Serializer for reading SupplierInfo from JDBC DataRead.
     * Columns:
     * 1: ID, 2: SEARCHKEY, 3: TAXID, 4: NAME, 5: MAXDEBT, 6: ADDRESS, 7: ADDRESS2,
     * 8: POSTAL, 9: CITY, 10: REGION, 11: COUNTRY, 12: FIRSTNAME, 13: LASTNAME,
     * 14: EMAIL, 15: PHONE, 16: PHONE2, 17: FAX, 18: NOTES, 19: VISIBLE,
     * 20: CURDATE, 21: CURDEBT, 22: VATID
     */
    public static SerializerRead<SupplierInfo> getSerializerRead() {
        return (DataRead dr) -> {
            SupplierInfo s = new SupplierInfo(dr.getString(1));
            s.setSearchkey(dr.getString(2));
            s.setTaxid(dr.getString(3));
            s.setName(dr.getString(4));
            s.setMaxdebt(dr.getDouble(5));
            s.setAddress(dr.getString(6));
            s.setAddress2(dr.getString(7));
            s.setPostal(dr.getString(8));
            s.setCity(dr.getString(9));
            s.setRegion(dr.getString(10));
            s.setCountry(dr.getString(11));
            s.setFirstname(dr.getString(12));
            s.setLastname(dr.getString(13));
            s.setEmail(dr.getString(14));
            s.setPhone(dr.getString(15));
            s.setPhone2(dr.getString(16));
            s.setFax(dr.getString(17));
            s.setNotes(dr.getString(18));
            Boolean vis = dr.getBoolean(19);
            s.setVisible(vis != null ? vis : true);
            s.setCurdate(dr.getTimestamp(20));
            s.setCurdebt(dr.getDouble(21));
            s.setVatid(dr.getString(22));
            return s;
        };
    }

    /**
     * Serializer for inserting SupplierInfo.
     */
    public static SerializerWrite<SupplierInfo> getSerializerInsert() {
        return (DataWrite dp, SupplierInfo s) -> {
            dp.setString(1, s.getId());
            dp.setString(2, s.getSearchkey());
            dp.setString(3, s.getTaxid());
            dp.setString(4, s.getName());
            dp.setDouble(5, s.getMaxdebt());
            dp.setString(6, s.getAddress());
            dp.setString(7, s.getAddress2());
            dp.setString(8, s.getPostal());
            dp.setString(9, s.getCity());
            dp.setString(10, s.getRegion());
            dp.setString(11, s.getCountry());
            dp.setString(12, s.getFirstname());
            dp.setString(13, s.getLastname());
            dp.setString(14, s.getEmail());
            dp.setString(15, s.getPhone());
            dp.setString(16, s.getPhone2());
            dp.setString(17, s.getFax());
            dp.setString(18, s.getNotes());
            dp.setBoolean(19, s.isVisible());
            dp.setTimestamp(20, s.getCurdate());
            dp.setDouble(21, s.getCurdebt());
            dp.setString(22, s.getVatid());
        };
    }

    /**
     * Serializer for updating SupplierInfo.
     * Indices 1-22 are SET columns, index 23 is WHERE id = ?
     */
    public static SerializerWrite<SupplierInfo> getSerializerUpdate() {
        return (DataWrite dp, SupplierInfo s) -> {
            dp.setString(1, s.getId());
            dp.setString(2, s.getSearchkey());
            dp.setString(3, s.getTaxid());
            dp.setString(4, s.getName());
            dp.setDouble(5, s.getMaxdebt());
            dp.setString(6, s.getAddress());
            dp.setString(7, s.getAddress2());
            dp.setString(8, s.getPostal());
            dp.setString(9, s.getCity());
            dp.setString(10, s.getRegion());
            dp.setString(11, s.getCountry());
            dp.setString(12, s.getFirstname());
            dp.setString(13, s.getLastname());
            dp.setString(14, s.getEmail());
            dp.setString(15, s.getPhone());
            dp.setString(16, s.getPhone2());
            dp.setString(17, s.getFax());
            dp.setString(18, s.getNotes());
            dp.setBoolean(19, s.isVisible());
            dp.setTimestamp(20, s.getCurdate());
            dp.setDouble(21, s.getCurdebt());
            dp.setString(22, s.getVatid());
            dp.setString(23, s.getId());
        };
    }

    /**
     * Serializer for deleting SupplierInfo by ID.
     */
    public static SerializerWrite<SupplierInfo> getSerializerDelete() {
        return (DataWrite dp, SupplierInfo s) -> {
            dp.setString(1, s.getId());
        };
    }
}
