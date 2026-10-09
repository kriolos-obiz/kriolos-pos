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
package com.openbravo.pos.suppliers;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.*;
import com.openbravo.format.Formats;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.BeanFactoryDataSingle;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author JG uniCenta
 */
public class DataLogicSuppliers extends BeanFactoryDataSingle implements SupplierService {

    private static final Logger LOGGER = Logger.getLogger(DataLogicSuppliers.class.getName());
    /**
     * Main Method for supplier object
     */
    protected Session s;
    private TableDefinition tsuppliers;
    private static final Datas[] supplierdatas = new Datas[]{
        Datas.STRING,
        Datas.STRING,
        Datas.STRING,
        Datas.STRING,
        Datas.STRING,
        Datas.STRING,
        Datas.STRING};

    /**
     *
     * @param s
     */
    @Override
    public void init(Session s) {
        this.s = s;
        tsuppliers = new TableDefinition(s,
                "suppliers",
                new String[]{
                    "ID",
                    "SEARCHKEY",
                    "TAXID",
                    "NAME",
                    "MAXDEBT",
                    "ADDRESS",
                    "ADDRESS2",
                    "POSTAL",
                    "CITY",
                    "REGION",
                    "COUNTRY",
                    "FIRSTNAME",
                    "LASTNAME",
                    "EMAIL",
                    "PHONE",
                    "PHONE2",
                    "FAX",
                    "NOTES",
                    "VISIBLE",
                    "CURDATE",
                    "CURDEBT",
                    "VATID"},
                new String[]{
                    "ID",
                    AppLocal.getIntString("label.searchkey"),
                    AppLocal.getIntString("label.taxid"),
                    AppLocal.getIntString("label.name"),
                    AppLocal.getIntString("label.maxdebt"),
                    AppLocal.getIntString("label.address"),
                    AppLocal.getIntString("label.address2"),
                    AppLocal.getIntString("label.postal"),
                    AppLocal.getIntString("label.city"),
                    AppLocal.getIntString("label.region"),
                    AppLocal.getIntString("label.country"),
                    AppLocal.getIntString("label.firstname"),
                    AppLocal.getIntString("label.lastname"),
                    AppLocal.getIntString("label.email"),
                    AppLocal.getIntString("label.phone"),
                    AppLocal.getIntString("label.phone2"),
                    AppLocal.getIntString("label.fax"),
                    AppLocal.getIntString("label.notes"),
                    "VISIBLE",
                    AppLocal.getIntString("label.curdate"),
                    AppLocal.getIntString("label.curdebt"),
                    AppLocal.getIntString("label.suppliervatid")},
                new Datas[]{
                    Datas.STRING, Datas.STRING, Datas.STRING, Datas.STRING,
                    Datas.DOUBLE, Datas.STRING, Datas.STRING, Datas.STRING,
                    Datas.STRING, Datas.STRING, Datas.STRING, Datas.STRING,
                    Datas.STRING, Datas.STRING, Datas.STRING, Datas.STRING,
                    Datas.STRING, Datas.STRING, Datas.BOOLEAN, Datas.TIMESTAMP,
                    Datas.DOUBLE, Datas.STRING},
                new Formats[]{
                    Formats.STRING, Formats.STRING, Formats.STRING, Formats.STRING,
                    Formats.CURRENCY, Formats.STRING, Formats.STRING, Formats.STRING,
                    Formats.STRING, Formats.STRING, Formats.STRING, Formats.STRING,
                    Formats.STRING, Formats.STRING, Formats.STRING, Formats.STRING,
                    Formats.STRING, Formats.STRING, Formats.BOOLEAN, Formats.TIMESTAMP,
                    Formats.CURRENCY, Formats.STRING},
                new int[]{0}
        );
    }

    /**
     *
     * @return supplier data
     */
    public SentenceList<SupplierInfo> getSupplierList() {
        return new StaticSentence(s,
                new QBFBuilder("SELECT "
                        + "ID, SEARCHKEY, TAXID, NAME, "
                        + "POSTAL, PHONE, EMAIL "
                        + "FROM suppliers "
                        + "WHERE VISIBLE = " + s.DB.TRUE() + " AND ?(QBF_FILTER) ORDER BY NAME",
                        new String[]{"SEARCHKEY", "TAXID", "NAME", "POSTAL", "PHONE", "EMAIL"}),
                new SerializerWriteBasic(new Datas[]{
            Datas.OBJECT, Datas.STRING,
            Datas.OBJECT, Datas.STRING,
            Datas.OBJECT, Datas.STRING,
            Datas.OBJECT, Datas.STRING,
            Datas.OBJECT, Datas.STRING,
            Datas.OBJECT, Datas.STRING}),
                (DataRead dr) -> {
                    SupplierInfo s1 = new SupplierInfo(dr.getString(1));
                    s1.setSearchkey(dr.getString(2));
                    s1.setTaxid(dr.getString(3));
                    s1.setName(dr.getString(4));
                    s1.setPostal(dr.getString(5));
                    s1.setPhone(dr.getString(6));
                    s1.setEmail(dr.getString(7));
                    return s1;
                });
    }

    public List<SupplierInfo> getSupplierListAll() {

        List<SupplierInfo> list = null;
        try {
            list = this.getSupplierList().list();
        } catch (BasicException ex) {
            LOGGER.log(Level.WARNING, "Cannot get SupplierInfo list", ex);
        }
        return list;
    }

    /**
     *
     * @return supplier data
     */
    public SentenceList<SupplierInfo> getSuppList() {
        return new StaticSentence(s,
                new QBFBuilder("SELECT "
                        + "ID, SEARCHKEY, TAXID, NAME, "
                        + "POSTAL, PHONE, EMAIL "
                        + "FROM suppliers "
                        + "WHERE VISIBLE = " + s.DB.TRUE() + " ORDER BY NAME",
                        new String[]{"SEARCHKEY", "TAXID", "NAME", "POSTAL", "PHONE", "EMAIL"}),
                new SerializerWriteBasic(new Datas[]{
            Datas.OBJECT, Datas.STRING,
            Datas.OBJECT, Datas.STRING,
            Datas.OBJECT, Datas.STRING,
            Datas.OBJECT, Datas.STRING,
            Datas.OBJECT, Datas.STRING,
            Datas.OBJECT, Datas.STRING}),
                (DataRead dr) -> {
                    SupplierInfo s1 = new SupplierInfo(dr.getString(1));
                    s1.setSearchkey(dr.getString(2));
                    s1.setTaxid(dr.getString(3));
                    s1.setName(dr.getString(4));
                    s1.setPostal(dr.getString(5));
                    s1.setPhone(dr.getString(6));
                    s1.setEmail(dr.getString(7));
                    return s1;
                });
    }

    /**
     *
     * @return
     */
    public final SentenceList<SupplierInfo> getSuppListExt() {
        return new StaticSentence(s,
                "SELECT "
                + "ID, "
                + "SEARCHKEY, "
                + "NAME "
                + "FROM suppliers "
                + "ORDER BY NAME",
                null,
                (DataRead dr) -> new SupplierInfo(
                        dr.getString(1),
                        dr.getString(2),
                        dr.getString(3)));
    }

    /**
     *
     * @param supplier
     * @return
     * @throws BasicException
     */
    public int updateSupplierExt(final SupplierInfoExt supplier) throws BasicException {

        return new PreparedSentence(s,
                "UPDATE suppliers SET NOTES = ? WHERE ID = ?",
                SerializerWriteParams.INSTANCE)
                .exec(new DataParams() {
                    @Override
                    public void writeValues() throws BasicException {
                        setString(1, supplier.getNotes());
                        setString(2, supplier.getId());
                    }
                });
    }

    public final TableDefinition getTableSuppliers() {
        return tsuppliers;
    }

    /**
     * JG Aug 2017 - Return Supplier Id - sId param
     *
     * @param sId
     * @return
     * @throws BasicException
     */
    public final List<SupplierTransaction> getSuppliersTransactionList(String sId) throws BasicException {
        return new PreparedSentence(s, """
            SELECT 
                stockdiary.datenew, 
                products.NAME, 
                stockdiary.units, 
                stockdiary.price, 
                stockdiary.reason, 
                suppliers.id 
            FROM stockdiary stockdiary 
            INNER JOIN suppliers suppliers ON (stockdiary.supplier = suppliers.id) 
            INNER JOIN products products ON (stockdiary.product = products.ID) 
            WHERE suppliers.id = ? 
            ORDER BY stockdiary.datenew DESC
            """,
            SerializerWriteString.INSTANCE,
            SupplierTransaction.getSerializerRead()
        ).list(sId);
    }

    public final void createSupplier(Object[] supplier) throws BasicException {
        SentenceExec m_createSupp = new StaticSentence(this.s,
                "INSERT INTO suppliers ( ID, NAME, SEARCHKEY, VISIBLE ) "
                + "VALUES (?, ?, ?, ?)",
                new SerializerWriteBasic(new Datas[]{
            Datas.STRING,
            Datas.STRING,
            Datas.STRING,
            Datas.BOOLEAN}));
        m_createSupp.exec(supplier);
    }

    /**
     *
     * @param id
     * @return
     * @throws BasicException
     */
    public SupplierInfoExt loadSupplierExt(String id) throws BasicException {
        return (SupplierInfoExt) new PreparedSentence(s,
                "SELECT "
                + "ID, "
                + "SEARCHKEY, "
                + "TAXID, "
                + "NAME, "
                + "MAXDEBT, "
                + "ADDRESS, "
                + "ADDRESS2, "
                + "POSTAL, "
                + "CITY, "
                + "REGION, "
                + "COUNTRY, "
                + "FIRSTNAME, "
                + "LASTNAME, "
                + "EMAIL, "
                + "PHONE, "
                + "PHONE2, "
                + "FAX, "
                + "NOTES, "
                + "VISIBLE, "
                + "CURDATE, "
                + "CURDEBT, "
                + "VATID "
                + "FROM suppliers WHERE ID = ?",
                SerializerWriteString.INSTANCE,
                new SupplierExtRead()).find(id);
    }

    /**
     *
     */
    protected static class SupplierExtRead implements SerializerRead {

        /**
         *
         * @param dr
         * @return
         * @throws BasicException
         */
        @Override
        public Object readValues(DataRead dr) throws BasicException {
            SupplierInfoExt s = new SupplierInfoExt(dr.getString(1));
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
            s.setVisible(dr.getBoolean(19));
            s.setCurdate(dr.getTimestamp(20));
            s.setCurdebt(dr.getDouble(21));
            s.setSupplierVATID(dr.getString(22));

            return s;
        }
    }

}
