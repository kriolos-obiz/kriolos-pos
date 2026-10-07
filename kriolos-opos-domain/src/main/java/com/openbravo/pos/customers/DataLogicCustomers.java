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
package com.openbravo.pos.customers;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.*;
import com.openbravo.data.user.DefaultSaveProvider;
import com.openbravo.data.user.SaveProvider;
import com.openbravo.data.model.Field;
import com.openbravo.data.model.Row;
import com.openbravo.format.Formats;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.BeanFactoryDataSingle;
import com.openbravo.pos.forms.BeanFactoryException;
import com.openbravo.pos.sales.restaurant.DataLogicRestaurant;
import java.util.Date;
import java.util.List;

/**
 * @author JG uniCenta
 * @author adrianromero
 */
public class DataLogicCustomers extends BeanFactoryDataSingle implements CustomerService {

    protected Session s;
    private Row customersRow;

    private static final Datas[] CUSTOMER_DATA = new Datas[]{
        Datas.OBJECT, Datas.STRING, //TAXID
        Datas.OBJECT, Datas.STRING, //SEARCHKEY
        Datas.OBJECT, Datas.STRING, //NAME
        Datas.OBJECT, Datas.STRING, //POSTAL
        Datas.OBJECT, Datas.STRING, //PHONE
        Datas.OBJECT, Datas.STRING //EMAIL
    };

    @Override
    public void init(Session s) {
        this.s = s;
        initCustomersRow();
    }

    private void initCustomersRow() {
        customersRow = new Row(
                new Field("ID", Datas.STRING, Formats.STRING),
                new Field("SEARCHKEY", Datas.STRING, Formats.STRING),
                new Field("TAXID", Datas.STRING, Formats.STRING),
                new Field("NAME", Datas.STRING, Formats.STRING),
                new Field("TAXCATEGORY", Datas.STRING, Formats.STRING),
                new Field("CARD", Datas.STRING, Formats.STRING),
                new Field("MAXDEBT", Datas.DOUBLE, Formats.CURRENCY),
                new Field("ADDRESS", Datas.STRING, Formats.STRING),
                new Field("ADDRESS2", Datas.STRING, Formats.STRING),
                new Field("POSTAL", Datas.STRING, Formats.STRING),
                new Field("CITY", Datas.STRING, Formats.STRING),
                new Field("REGION", Datas.STRING, Formats.STRING),
                new Field("COUNTRY", Datas.STRING, Formats.STRING),
                new Field("FIRSTNAME", Datas.STRING, Formats.STRING),
                new Field("LASTNAME", Datas.STRING, Formats.STRING),
                new Field("EMAIL", Datas.STRING, Formats.STRING),
                new Field("PHONE", Datas.STRING, Formats.STRING),
                new Field("PHONE2", Datas.STRING, Formats.STRING),
                new Field("FAX", Datas.STRING, Formats.STRING),
                new Field("NOTES", Datas.STRING, Formats.STRING),
                new Field("VISIBLE", Datas.BOOLEAN, Formats.BOOLEAN),
                new Field("CURDATE", Datas.TIMESTAMP, Formats.TIMESTAMP),
                new Field("CURDEBT", Datas.DOUBLE, Formats.CURRENCY),
                new Field("IMAGE", Datas.IMAGE, Formats.NULL),
                new Field("ISVIP", Datas.BOOLEAN, Formats.BOOLEAN),
                new Field("DISCOUNT", Datas.DOUBLE, Formats.PERCENT),
                new Field("MEMODATE", Datas.TIMESTAMP, Formats.TIMESTAMP)
        );
    }

    public Row getCustomersRow() {
        if (customersRow == null) {
            initCustomersRow();
        }
        return customersRow;
    }

    public SentenceList<CustomerInfo> getCustomerList() {
        return new StaticSentence(s,
                new QBFBuilder("SELECT "
                        + "ID, TAXID, SEARCHKEY, NAME, "
                        + "POSTAL, EMAIL, PHONE, IMAGE "
                        + "FROM customers "
                        + "WHERE VISIBLE = " + s.DB.TRUE() + " AND ?(QBF_FILTER) ORDER BY NAME",
                        new String[]{"TAXID", "SEARCHKEY", "NAME", "POSTAL", "PHONE", "EMAIL"}),
                new SerializerWriteBasic(CUSTOMER_DATA),
                new CustomerInfoRead());
    }

    @Override
    public final CustomerInfo getCustomerInfo(String id) throws BasicException {
        return (CustomerInfo) new PreparedSentence(s,
                "SELECT "
                + "ID, TAXID, SEARCHKEY, NAME, "
                + "POSTAL, EMAIL, PHONE, IMAGE "
                + "FROM customers WHERE VISIBLE = " + s.DB.TRUE() + " "
                + "AND ID = ?",
                SerializerWriteString.INSTANCE,
                new CustomerInfoRead()).find(id);
    }

    @Override
    public int updateCustomerExt(final CustomerInfoExt customer) throws BasicException {

        return new PreparedSentence(s,
                "UPDATE customers SET NOTES = ? WHERE ID = ?",
                SerializerWriteParams.INSTANCE
        ).exec(new DataParams() {
            @Override
            public void writeValues() throws BasicException {
                setString(1, customer.getNotes());
                setString(2, customer.getId());
            }
        });
    }

    public DataLogicRestaurant getDataLogicRestaurant() {
        if (app != null) {
            try {
                return app.getBean(DataLogicRestaurant.class);
            } catch (BeanFactoryException ignored) {
            }
        }
        DataLogicRestaurant fallback = new DataLogicRestaurant();
        fallback.init(s);
        return fallback;
    }

    // <editor-fold defaultstate="collapsed" desc="Reservation">
    /**
     * @deprecated Use {@link DataLogicRestaurant#getReservationsList()} instead.
     */
    @Deprecated
    public final SentenceList getReservationsList() {
        return getDataLogicRestaurant().getReservationsList();
    }

    /**
     * @deprecated Use {@link DataLogicRestaurant#getReservationsUpdate()} instead.
     */
    @Deprecated
    public final SentenceExec getReservationsUpdate() {
        return getDataLogicRestaurant().getReservationsUpdate();
    }

    /**
     * @deprecated Use {@link DataLogicRestaurant#getReservationsDelete()} instead.
     */
    @Deprecated
    public final SentenceExec getReservationsDelete() {
        return getDataLogicRestaurant().getReservationsDelete();
    }

    /**
     * @deprecated Use {@link DataLogicRestaurant#getReservationsInsert()} instead.
     */
    @Deprecated
    public final SentenceExec getReservationsInsert() {
        return getDataLogicRestaurant().getReservationsInsert();
    }
    // </editor-fold>

    public final TableDefinition getTableCustomers() {
        TableDefinition tcustomers = new TableDefinition(s,
                "customers",
                new String[]{
                    "ID",
                    "SEARCHKEY",
                    "TAXID",
                    "NAME",
                    "TAXCATEGORY",
                    "CARD",
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
                    "IMAGE",
                    "ISVIP",
                    "DISCOUNT",
                    "MEMODATE"
                },
                new String[]{
                    "ID",
                    AppLocal.getIntString("label.searchkey"),
                    AppLocal.getIntString("label.taxid"),
                    AppLocal.getIntString("label.name"),
                    "TAXCATEGORY",
                    "CARD",
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
                    "IMAGE",
                    "ISVIP",
                    "DISCOUNT",
                    "MEMODATE"
                },
                new Datas[]{
                    Datas.STRING, //id
                    Datas.STRING, //searchkey
                    Datas.STRING, //taxid
                    Datas.STRING, //name
                    Datas.STRING, //taxcat
                    Datas.STRING, //card
                    Datas.DOUBLE, //maxdebt
                    Datas.STRING, //add
                    Datas.STRING, //add2
                    Datas.STRING, //postal
                    Datas.STRING, //city
                    Datas.STRING, //region
                    Datas.STRING, //cntry
                    Datas.STRING, //fname
                    Datas.STRING, //lname
                    Datas.STRING, //email
                    Datas.STRING, //phone
                    Datas.STRING, //phone2
                    Datas.STRING, //fax
                    Datas.STRING, //notes
                    Datas.BOOLEAN, //visible
                    Datas.TIMESTAMP, //curdate
                    Datas.DOUBLE, //curdebt
                    Datas.IMAGE, //image
                    Datas.BOOLEAN, //isvip
                    Datas.DOUBLE, //discount
                    Datas.TIMESTAMP //memodate
                },
                new Formats[]{
                    Formats.STRING, //id
                    Formats.STRING, //searchkey
                    Formats.STRING, //taxid
                    Formats.STRING, //name
                    Formats.STRING, //taxcat
                    Formats.STRING, //card
                    Formats.CURRENCY, //maxdebt
                    Formats.STRING, //add
                    Formats.STRING, //add2
                    Formats.STRING, //postal
                    Formats.STRING, //city
                    Formats.STRING, //region
                    Formats.STRING, //cntry
                    Formats.STRING, //fname
                    Formats.STRING, //lname
                    Formats.STRING, //email
                    Formats.STRING, //phone
                    Formats.STRING, //phone2
                    Formats.STRING, //fax
                    Formats.STRING, //notes
                    Formats.BOOLEAN, //visible
                    Formats.TIMESTAMP, //curdate
                    Formats.CURRENCY, //curdebt
                    Formats.NULL, //image
                    Formats.BOOLEAN, //isvip
                    Formats.DOUBLE, //discount
                    Formats.TIMESTAMP //memodate
                },
                new int[]{0}
        );
        return tcustomers;
    }

    

    protected static class CustomerInfoRead implements SerializerRead<CustomerInfo> {

        @Override
        public CustomerInfo readValues(DataRead dr) throws BasicException {
            CustomerInfo c = new CustomerInfo(dr.getString(1));
            c.setTaxid(dr.getString(2));
            c.setSearchkey(dr.getString(3));
            c.setName(dr.getString(4));
            c.setPostal(dr.getString(5));
            c.setPhone(dr.getString(6));
            c.setEmail(dr.getString(7));
            c.setImage(ImageUtils.readImage(dr.getBytes(8)));
            return c;
        }
    }

    Datas[] customerData = new Datas[]{
        Datas.STRING, //id
        Datas.STRING, //searchkey
        Datas.STRING, //taxid
        Datas.STRING, //name
        Datas.STRING, //taxcat
        Datas.STRING, //card
        Datas.DOUBLE, //maxdebt
        Datas.STRING, //add
        Datas.STRING, //add2
        Datas.STRING, //postal
        Datas.STRING, //city
        Datas.STRING, //region
        Datas.STRING, //cntry
        Datas.STRING, //fname
        Datas.STRING, //lname
        Datas.STRING, //email
        Datas.STRING, //phone
        Datas.STRING, //phone2
        Datas.STRING, //fax
        Datas.STRING, //notes
        Datas.BOOLEAN, //visible
        Datas.TIMESTAMP, //curdate
        Datas.DOUBLE, //curdebt
        Datas.IMAGE, //image
        Datas.BOOLEAN, //isvip
        Datas.DOUBLE, //discount
        Datas.TIMESTAMP //memodate
    };

    private SentenceExec customerSentenceExecUpdate() {
        SentenceExec sentupdate = new PreparedSentenceExec(this.s,
                "update customers set ID = ?, SEARCHKEY = ?, TAXID = ?, NAME = ?, TAXCATEGORY = ?, CARD = ?, MAXDEBT = ?, ADDRESS = ?, ADDRESS2 = ?, POSTAL = ?, CITY = ?, REGION = ?, COUNTRY = ?, FIRSTNAME = ?, LASTNAME = ?, EMAIL = ?, PHONE = ?, PHONE2 = ?, FAX = ?, NOTES = ?, VISIBLE = ?, CURDATE = ?, CURDEBT = ?, IMAGE = ?, ISVIP = ?, DISCOUNT = ?, MEMODATE = ? where ID = ?",
                customerData, new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 0});

        return sentupdate;
    }

    private SentenceExec customerSentenceExecDelete() {
        Datas[] resourcedata = new Datas[]{Datas.STRING};
        SentenceExec sentdelete = new PreparedSentenceExec(this.s,
                "DELETE FROM customers WHERE ID = ?",
                resourcedata, new int[]{0});

        return sentdelete;
    }

    private SentenceExec customerSentenceExecInsert() {
        SentenceExec sentinsert = new PreparedSentenceExec(this.s,
                "insert into customers (ID, SEARCHKEY, TAXID, NAME, TAXCATEGORY, CARD, MAXDEBT, ADDRESS, ADDRESS2, POSTAL, CITY, REGION, COUNTRY, FIRSTNAME, LASTNAME, EMAIL, PHONE, PHONE2, FAX, NOTES, VISIBLE, CURDATE, CURDEBT, IMAGE, ISVIP, DISCOUNT, MEMODATE) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                customerData, new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26});

        return sentinsert;
    }

    public SaveProvider<Object[]> getCustomerSaveProvider() {
        return new DefaultSaveProvider(
                customerSentenceExecUpdate(),
                customerSentenceExecInsert(),
                customerSentenceExecDelete());
    }

    /**
     *
     * @param card
     * @return
     * @throws BasicException
     */
    @Override
    public CustomerInfoExt findCustomerInfoExtByCard(String card) throws BasicException {
        return (CustomerInfoExt) new PreparedSentence(this.s,
                "SELECT "
                + "ID, "
                + "TAXID, "
                + "SEARCHKEY, "
                + "NAME, "
                + "TAXCATEGORY, "
                + "CARD, "
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
                + "IMAGE, "
                + "ISVIP, "
                + "DISCOUNT, "
                + "MEMODATE "
                + "FROM customers "
                + "WHERE CARD = ? AND VISIBLE = " + this.s.DB.TRUE() + " "
                + "ORDER BY NAME",
                SerializerWriteString.INSTANCE,
                new CustomerInfoExtRead()).find(card);
    }

    /**
     *
     * @param name
     * @return
     * @throws BasicException
     */
    @Override
    public CustomerInfoExt findCustomerInfoExtByName(String name) throws BasicException {
        return (CustomerInfoExt) new PreparedSentence(this.s,
                "SELECT "
                + "ID, "
                + "SEARCHKEY, "
                + "TAXID, "
                + "NAME, "
                + "TAXCATEGORY, "
                + "CARD, "
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
                + "IMAGE, "
                + "ISVIP, "
                + "DISCOUNT, "
                + "MEMODATE "
                + "FROM customers "
                + "WHERE NAME = ? AND VISIBLE = " + this.s.DB.TRUE() + " "
                + "ORDER BY NAME",
                SerializerWriteString.INSTANCE,
                new CustomerInfoExtRead()).find(name);
    }

    /**
     *
     * @param id
     * @return
     * @throws BasicException
     */
    @Override
    public final CustomerInfoExt findCustomerInfoExtById(String id) throws BasicException {
        return new PreparedSentence<String, CustomerInfoExt>(this.s,
                "SELECT "
                + "ID, "
                + "SEARCHKEY, "
                + "TAXID, "
                + "NAME, "
                + "TAXCATEGORY, "
                + "CARD, "
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
                + "IMAGE, "
                + "ISVIP, "
                + "DISCOUNT, "
                + "MEMODATE "
                + "FROM customers WHERE ID = ?",
                SerializerWriteString.INSTANCE,
                new CustomerInfoExtRead()).find(id);
    }

    protected static class CustomerInfoExtRead implements SerializerRead<CustomerInfoExt> {

        @Override
        public CustomerInfoExt readValues(DataRead dr) throws BasicException {
            CustomerInfoExt c = new CustomerInfoExt(dr.getString(1));
            c.setSearchkey(dr.getString(2));
            c.setTaxid(dr.getString(3));
            c.setTaxCustomerID(dr.getString(3));
            c.setName(dr.getString(4));
            c.setTaxCustCategoryID(dr.getString(5));
            c.setCard(dr.getString(6));
            c.setMaxdebt(dr.getDouble(7));
            c.setAddress(dr.getString(8));
            c.setAddress2(dr.getString(9));
            c.setPostal(dr.getString(10));
            c.setCity(dr.getString(11));
            c.setRegion(dr.getString(12));
            c.setCountry(dr.getString(13));
            c.setFirstname(dr.getString(14));
            c.setLastname(dr.getString(15));
            c.setEmail(dr.getString(16));
            c.setPhone1(dr.getString(17));
            c.setPhone2(dr.getString(18));
            c.setFax(dr.getString(19));
            c.setNotes(dr.getString(20));
            c.setVisible(dr.getBoolean(21));
            c.setCurdate(dr.getTimestamp(22));
            c.setAccdebt(dr.getDouble(23));
            c.setImage(ImageUtils.readImage(dr.getBytes(24)));
            c.setisVIP(dr.getBoolean(25));
            c.setDiscount(dr.getDouble(26));
            c.setMemoDate(dr.getString(27));

            return c;
        }
    }

    @Override
    public final List<CustomerTransaction> getCustomersTransactionList(String cId) throws BasicException {
        return new PreparedSentence<>(s, """
            SELECT 
                tickets.TICKETID, 
                products.NAME AS PNAME, 
                SUM(ticketlines.UNITS) AS UNITS, 
                SUM(ticketlines.UNITS * ticketlines.PRICE) AS AMOUNT, 
                SUM(ticketlines.UNITS * ticketlines.PRICE * (1.0 + taxes.RATE)) AS TOTAL, 
                receipts.DATENEW, 
                customers.ID AS CID 
            FROM ticketlines ticketlines 
            INNER JOIN taxes taxes ON ticketlines.TAXID = taxes.ID 
            INNER JOIN tickets tickets ON tickets.ID = ticketlines.TICKET 
            INNER JOIN customers customers ON customers.ID = tickets.CUSTOMER 
            INNER JOIN receipts receipts ON tickets.ID = receipts.ID 
            LEFT OUTER JOIN products products ON ticketlines.PRODUCT = products.ID 
            WHERE tickets.CUSTOMER = ? 
            GROUP BY 
                customers.ID, 
                receipts.DATENEW, 
                tickets.TICKETID, 
                products.NAME
            ORDER BY receipts.DATENEW DESC
            """,
                SerializerWriteString.INSTANCE,
                CustomerTransaction.getSerializerRead()).list(cId);
    }

    @Override
    public final int updateCustomerDebt(String customerId, Double accDebt, Date date) throws BasicException {
        return new PreparedSentence(s,
                "UPDATE customers SET CURDEBT = ?, CURDATE = ? WHERE ID = ?",
                SerializerWriteParams.INSTANCE).exec(new DataParams() {
            @Override
            public void writeValues() throws BasicException {
                setDouble(1, accDebt);
                setTimestamp(2, date);
                setString(3, customerId);
            }
        });
    }

    public final SentenceExec getCustomerInsert() {
        return new SentenceExecTransaction(s) {
            @Override
            public int execInTransaction(Object[] params) throws BasicException {
                return new PreparedSentence(s,
                        "INSERT INTO customers ("
                        + "ID, "
                        + "SEARCHKEY, "
                        + "TAXID, "
                        + "NAME, "
                        + "TAXCATEGORY, "
                        + "CARD, "
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
                        + "IMAGE, "
                        + "ISVIP, "
                        + "DISCOUNT, "
                        + "MEMODATE ) "
                        + "VALUES ("
                        + "?, ?, ?, ?, ?, ?, "
                        + "?, ?, ?, ?, ?, ?, "
                        + "?, ?, ?, ?, ?, ?, "
                        + "?, ?, ?, ?, ?, ?, "
                        + "?, ?, ?)",
                        new SerializerWriteBasicExt(getCustomersRow().getDatas(),
                                new int[]{0,
                                    1, 2, 3, 4, 5, 6,
                                    7, 8, 9, 10, 11, 12,
                                    13, 14, 15, 16, 17, 18,
                                    19, 20, 21, 22, 23, 24,
                                    25, 26}))
                        .exec(params);
            }
        };
    }

    public final SentenceExec getCustomerUpdate() {
        return new SentenceExecTransaction(s) {
            @Override
            public int execInTransaction(Object[] params) throws BasicException {
                return new PreparedSentence(s,
                        "UPDATE customers SET "
                        + "ID = ?, "
                        + "SEARCHKEY = ?, "
                        + "TAXID = ?, "
                        + "NAME = ?, "
                        + "TAXCATEGORY = ?, "
                        + "CARD = ?, "
                        + "MAXDEBT = ?, "
                        + "ADDRESS = ?, "
                        + "ADDRESS2 = ?, "
                        + "POSTAL = ?, "
                        + "CITY = ?, "
                        + "REGION = ?, "
                        + "COUNTRY = ?, "
                        + "FIRSTNAME = ?, "
                        + "LASTNAME = ?, "
                        + "EMAIL = ?, "
                        + "PHONE = ?, "
                        + "PHONE2 = ?, "
                        + "FAX = ?,  "
                        + "NOTES = ?,"
                        + "VISIBLE = ?, "
                        + "CURDATE = ?, "
                        + "CURDEBT = ?, "
                        + "IMAGE = ?, "
                        + "ISVIP = ?, "
                        + "DISCOUNT = ?, "
                        + "MEMODATE = ? "
                        + "WHERE ID = ?",
                        new SerializerWriteBasicExt(getCustomersRow().getDatas(),
                                new int[]{0,
                                    1, 2, 3, 4, 5,
                                    6, 7, 8, 9, 10,
                                    11, 12, 13, 14, 15,
                                    16, 17, 18, 19, 20,
                                    21, 22, 23, 24, 25,
                                    26, 0}))
                        .exec(params);
            }
        };
    }

    public final SentenceExec getCustomerDelete() {
        return new SentenceExecTransaction(s) {
            @Override
            public int execInTransaction(Object[] params) throws BasicException {
                return new PreparedSentence(s,
                        "DELETE FROM customers WHERE ID = ?",
                        new SerializerWriteBasicExt(getCustomersRow().getDatas(),
                                new int[]{0}))
                        .exec(params);
            }
        };
    }
}
