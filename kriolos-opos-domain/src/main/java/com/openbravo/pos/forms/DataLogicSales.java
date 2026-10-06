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
package com.openbravo.pos.forms;

import com.openbravo.pos.ticket.TicketTaxInfo;
import com.openbravo.pos.ticket.TicketInfo;
import com.openbravo.pos.ticket.TicketLineInfo;
import com.openbravo.pos.ticket.TaxInfo;
import com.openbravo.pos.ticket.ProductInfoExt;
import com.openbravo.pos.ticket.FindTicketsInfo;
import com.openbravo.pos.inventory.UomInfo;
import com.openbravo.pos.inventory.LocationInfo;
import com.openbravo.pos.inventory.ProductsBundleInfo;
import com.openbravo.pos.inventory.TaxCustCategoryInfo;
import com.openbravo.pos.inventory.TaxCategoryInfo;
import com.openbravo.pos.inventory.AttributeSetInfo;
import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.*;
import com.openbravo.data.model.Field;
import com.openbravo.data.model.Row;
import com.openbravo.format.Formats;
import com.openbravo.pos.customers.CustomerTransaction;
import com.openbravo.pos.customers.DataLogicCustomers;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.BeanFactoryDataSingle;
import com.openbravo.pos.inventory.*;
import com.openbravo.pos.sales.restaurant.FloorsInfo;
import com.openbravo.pos.payment.PaymentInfo;
import com.openbravo.pos.payment.PaymentInfoTicket;
import com.openbravo.pos.pim.DataLogicPIM;
import com.openbravo.pos.sales.ReprintTicketInfo;
import com.openbravo.pos.voucher.DataLogicVouchers;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author adrianromero
 * @author jackgerrard
 */
public class DataLogicSales extends BeanFactoryDataSingle {

    protected Session sessionDB;

    protected Datas[] auxiliarDatas;
    protected Datas[] stockdiaryDatas;
    protected Datas[] paymenttabledatas;
    protected Datas[] stockdatas;
    protected Datas[] stockAdjustDatas;
    protected Row customersRow;

    private static final String PAYMENT_METHOD_DEBT = "debt";
    private static final String PAYMENT_METHOD_DEBTPAID = "debtpaid";
    private static final String PREPAY = "prepay";
    private static final Logger LOGGER = Logger.getLogger("com.openbravo.pos.forms.DataLogicSales");

    // SQL constants for inventory panel queries
    public static final String SQL_BUNDLE_LIST = "SELECT B.ID, B.PRODUCT, B.PRODUCT_BUNDLE, B.QUANTITY, P.REFERENCE, P.CODE, P.NAME "
            + "FROM products_bundle B, products P "
            + "WHERE B.PRODUCT_BUNDLE = P.ID AND B.PRODUCT = ?";

    public static final String SQL_AUXILIAR_LIST = "SELECT COM.ID, COM.PRODUCT, COM.PRODUCT2, P.REFERENCE, P.CODE, P.NAME "
            + "FROM products_com COM, products P "
            + "WHERE COM.PRODUCT2 = P.ID AND COM.PRODUCT = ?";

    public DataLogicSales() {
        stockdiaryDatas = new Datas[]{
            Datas.STRING, Datas.TIMESTAMP, Datas.INT, Datas.STRING,
            Datas.STRING, Datas.STRING, Datas.DOUBLE, Datas.DOUBLE,
            Datas.STRING, Datas.STRING, Datas.STRING};

        paymenttabledatas = new Datas[]{
            Datas.STRING, Datas.STRING, Datas.TIMESTAMP,
            Datas.STRING, Datas.STRING, Datas.DOUBLE,
            Datas.STRING};

        stockdatas = new Datas[]{
            Datas.STRING, Datas.STRING, Datas.STRING,
            Datas.DOUBLE, Datas.DOUBLE, Datas.DOUBLE};

        stockAdjustDatas = new Datas[]{
            Datas.STRING,
            Datas.STRING,
            Datas.STRING,
            Datas.DOUBLE};

        auxiliarDatas = new Datas[]{
            Datas.STRING, Datas.STRING, Datas.STRING,
            Datas.STRING, Datas.STRING, Datas.STRING};

        // creating customers object here for now for future global reuse
        // LOYALTY, MEMBERSHIP & etc as will be more system centric than customer
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
                new Field("CURDATE", Datas.STRING, Formats.TIMESTAMP),
                new Field("CURDEBT", Datas.DOUBLE, Formats.CURRENCY),
                new Field("IMAGE", Datas.BYTES, Formats.NULL),
                new Field("ISVIP", Datas.BOOLEAN, Formats.BOOLEAN),
                new Field("DISCOUNT", Datas.DOUBLE, Formats.CURRENCY),
                new Field("MEMODATE", Datas.STRING, Formats.TIMESTAMP));

    }

    /**
     *
     * @param s session
     */
    @Override
    public void init(Session s) {
        this.sessionDB = s;
    }

    // End Import Creates
    public final Row getCustomersRow() {
        return customersRow;
    }

    /**
     * JG Oct 2016 Called from JPanelTicket
     *
     * @param pId
     * @param location
     * @return
     * @throws BasicException
     */
    public final ProductStock getProductStockState(String pId, String location) throws BasicException {

        PreparedSentence preparedSentence = new PreparedSentence(sessionDB,
                "SELECT "
                + "products.id, "
                + "locations.id as Location, "
                + "stockcurrent.units AS Current, "
                + "stocklevel.stocksecurity AS Minimum, "
                + "stocklevel.stockmaximum AS Maximum, "
                + "products.pricebuy, "
                + "products.pricesell, "
                + "products.memodate "
                + "FROM locations "
                + "INNER JOIN ((products "
                + "INNER JOIN stockcurrent "
                + "ON products.id = stockcurrent.product) "
                + "LEFT JOIN stocklevel ON products.id = stocklevel.product) "
                + "ON locations.id = stockcurrent.location "
                + "WHERE products.id = ? "
                + "AND locations.id = ?",
                SerializerWriteString.INSTANCE,
                ProductStock.getSerializerRead());

        ProductStock productStock = (ProductStock) preparedSentence.find(pId, location);

        return productStock;
    }

    /**
     * JG May 2016 Called from StockManagement
     *
     * @param pId
     * @return
     * @throws BasicException
     */
    public final List<ProductStock> getProductStockList(String pId) throws BasicException {

        String SQL_STOCK = """
                                SELECT
                                    P.ID AS product_id,
                                    L.name AS location_name,
                                    COALESCE(MAX(SC.units), 0) AS current_stock,
                                    MAX(SL.stocksecurity) AS minimum_stock,
                                    MAX(SL.stockmaximum) AS maximum_stock,
                                    ROUND(P.pricebuy, 2) AS price_buy,
                                    -- Standard calculation for price sell + tax
                                    ROUND((P.pricesell * MAX(T.rate)) + P.pricesell, 2) AS price_sell,
                                    P.memodate
                                FROM
                                    products P
                                INNER JOIN
                                    taxcategories TC ON P.TAXCAT = TC.ID
                                INNER JOIN
                                    taxes T ON TC.ID = T.category
                                LEFT OUTER JOIN
                                    stocklevel SL ON SL.product = P.ID
                                LEFT OUTER JOIN
                                    stockcurrent SC ON P.ID = SC.product
                                INNER JOIN
                                    locations L ON SC.location = L.ID
                                WHERE
                                    P.ID = ?
                                GROUP BY
                                    P.ID, L.name, P.pricebuy, P.pricesell, P.memodate;
                                """;
        return new PreparedSentence(sessionDB,
                SQL_STOCK,
                SerializerWriteString.INSTANCE,
                ProductStock.getSerializerRead()).list(pId);
    }

    /**
     * JG Sept 2017
     *
     * @return
     * @throws BasicException
     */
    public final List<ReprintTicketInfo> getReprintTicketList() throws BasicException {
        return (List<ReprintTicketInfo>) new StaticSentence(sessionDB, """
            SELECT 
                T.TICKETID, 
                T.TICKETTYPE, 
                R.DATENEW, 
                P.NAME, 
                C.NAME, 
                SUM(PM.TOTAL), 
                T.STATUS 
            FROM receipts R 
            JOIN tickets T ON R.ID = T.ID 
            LEFT OUTER JOIN payments PM ON R.ID = PM.RECEIPT 
            LEFT OUTER JOIN customers C ON C.ID = T.CUSTOMER 
            LEFT OUTER JOIN people P ON T.PERSON = P.ID 
            GROUP BY 
                T.ID, 
                T.TICKETID, 
                T.TICKETTYPE, 
                R.DATENEW, 
                P.NAME, 
                C.NAME,
                T.STATUS 
            ORDER BY R.DATENEW DESC, T.TICKETID 
            LIMIT 10
            """,
                null,
                new SerializerReadClass(ReprintTicketInfo.class)).list();
    }

    /**
     *
     * @param Id
     * @return
     * @throws BasicException
     */
    public final TicketInfo getReprintTicket(String Id) throws BasicException {

        if (Id == null) {
            return null;
        } else {
            Object[] ticketInfoObjArray = (Object[]) new StaticSentence(sessionDB,
                    "SELECT "
                    + "T.TICKETID, "
                    + "SUM(PM.TOTAL), "
                    + "R.DATENEW, "
                    + "P.NAME, "
                    + "T.TICKETTYPE, "
                    + "C.NAME, "
                    + "T.STATUS "
                    + "FROM receipts "
                    + "R JOIN tickets T ON R.ID = T.ID LEFT OUTER JOIN payments PM "
                    + "ON R.ID = PM.RECEIPT LEFT OUTER JOIN customers C "
                    + "ON C.ID = T.CUSTOMER LEFT OUTER JOIN people P ON T.PERSON = P.ID "
                    + "WHERE T.TICKETID = ?",
                    SerializerWriteString.INSTANCE,
                    new SerializerReadBasic(new Datas[]{Datas.SERIALIZABLE})).find(Id);
            return ticketInfoObjArray == null ? null : (TicketInfo) ticketInfoObjArray[0];
        }
    }

    // Tickets and Receipt list
    public SentenceList<FindTicketsInfo> getTicketsList() {
        return new StaticSentence(sessionDB,
                new QBFBuilder("""
                        SELECT 
                            T.TICKETID, 
                            T.TICKETTYPE, 
                            R.DATENEW, 
                            P.NAME, 
                            C.NAME, 
                            SUM(PM.TOTAL), 
                            T.STATUS 
                        FROM receipts R 
                        JOIN tickets T ON R.ID = T.ID 
                        LEFT OUTER JOIN payments PM ON R.ID = PM.RECEIPT 
                        LEFT OUTER JOIN customers C ON C.ID = T.CUSTOMER 
                        LEFT OUTER JOIN people P ON T.PERSON = P.ID 
                        WHERE ?(QBF_FILTER) 
                        GROUP BY 
                            T.ID, 
                            T.TICKETID, 
                            T.TICKETTYPE, 
                            R.DATENEW, 
                            P.NAME, 
                            C.NAME,
                            T.STATUS
                        ORDER BY R.DATENEW DESC, T.TICKETID
                        """,
                        new String[]{
                            "T.TICKETID", "T.TICKETTYPE", "PM.TOTAL", "R.DATENEW",
                            "R.DATENEW", "P.NAME", "C.NAME"
                        }),
                new SerializerWriteBasic(new Datas[]{
            Datas.OBJECT, Datas.INT,
            Datas.OBJECT, Datas.INT,
            Datas.OBJECT, Datas.DOUBLE,
            Datas.OBJECT, Datas.TIMESTAMP,
            Datas.OBJECT, Datas.TIMESTAMP,
            Datas.OBJECT, Datas.STRING,
            Datas.OBJECT, Datas.STRING
        }),
                new SerializerReadClass(FindTicketsInfo.class));
    }

    // User list
    /**
     *
     * @return
     */
    public final SentenceList<TaxCategoryInfo> getTaxCategoryInfoList() {
        return getTaxCategoriesList();
    }

    /**
     * @deprecated since Nov/2025
     * @return
     */
    public final SentenceList<TaxInfo> getTaxList() {
        return new StaticSentence(sessionDB,
                "SELECT "
                + "ID, "
                + "NAME, "
                + "CATEGORY, "
                + "CUSTCATEGORY, "
                + "PARENTID, "
                + "RATE, "
                + "RATECASCADE, "
                + "RATEORDER "
                + "FROM taxes "
                + "ORDER BY NAME",
                null,
                (DataRead dr) -> new TaxInfo(
                        dr.getString(1),
                        dr.getString(2),
                        dr.getString(3),
                        dr.getString(4),
                        dr.getString(5),
                        dr.getDouble(6),
                        dr.getBoolean(7),
                        dr.getInt(8)));
    }

    public final List<TaxInfo> getTaxListAll() {
        List<TaxInfo> list = null;
        try {
            list = this.getTaxList().list();
        }
        catch (BasicException ex) {
            LOGGER.log(Level.WARNING, "Cannot get Tax list", ex);
        }
        return list;
    }

    /**
     *
     * @return
     */
    public final SentenceList<TaxCustCategoryInfo> getTaxCustCategoriesList() {
        return new StaticSentence<>(sessionDB,
                "SELECT "
                + "ID, "
                + "NAME "
                + "FROM taxcustcategories "
                + "ORDER BY NAME",
                null,
                (DataRead dr) -> new TaxCustCategoryInfo(
                        dr.getString(1),
                        dr.getString(2)));
    }

    /**
     * JG Apr 2017 - Revised to return Customer Id - cId param
     *
     * @param cId
     * @return
     * @throws BasicException
     */
    public final List<CustomerTransaction> getCustomersTransactionList(String cId) throws BasicException {

        // TODO: TICKETLINE MUST STORE: _tax_value, _line_amount(Qty x price)
        // _line_total (Price x Qty x Tax), line_prod_name
        // TODO: CALCULATION MUST BE DONE Java using BigDecimal
        return new PreparedSentence<>(sessionDB, """
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

    /**
     * @deprecated Since Nov/2025
     * @return
     */
    public final SentenceList<TaxCategoryInfo> getTaxCategoriesList() {
        return new StaticSentence<>(sessionDB,
                "SELECT "
                + "ID, "
                + "NAME "
                + "FROM taxcategories "
                + "ORDER BY NAME",
                null,
                (DataRead dr) -> new TaxCategoryInfo(dr.getString(1), dr.getString(2)));
    }

    /**
     *
     * @return
     */
    public final List<TaxCategoryInfo> getTaxCategoriesListAll() {
        List<TaxCategoryInfo> list = null;
        try {
            list = this.getTaxCategoriesList().list();
        }
        catch (BasicException ex) {
            LOGGER.log(Level.WARNING, "Cannot get TaxCategoryInfo list", ex);
        }
        return list;
    }

    /**
     * @deprecated Since Nov/2025
     * @return
     */
    public final SentenceList<AttributeSetInfo> getAttributeSetList() {
        return new StaticSentence(sessionDB,
                "SELECT "
                + "ID, "
                + "NAME "
                + "FROM attributeset "
                + "ORDER BY NAME",
                null,
                (DataRead dr) -> new AttributeSetInfo(dr.getString(1), dr.getString(2)));
    }

    public final List<AttributeSetInfo> getAttributeSetListAll() {
        List<AttributeSetInfo> list = null;
        try {
            list = this.getAttributeSetList().list();
        }
        catch (BasicException ex) {
            LOGGER.log(Level.WARNING, "Cannot get AttributeSetInfo list", ex);
        }
        return list;
    }

    /**
     * @deprecated Since Nov/2025
     * @return
     */
    public final SentenceList<LocationInfo> getLocationsList() {
        return new StaticSentence(sessionDB,
                "SELECT "
                + "ID, "
                + "NAME, "
                + "ADDRESS FROM locations "
                + "ORDER BY NAME",
                null,
                new SerializerReadClass(LocationInfo.class));
    }

    public final List<LocationInfo> getLocationsListAll() {
        List<LocationInfo> list = null;
        try {
            list = this.getLocationsList().list();
        }
        catch (BasicException ex) {
            LOGGER.log(Level.WARNING, "Cannot get AttributeSetInfo list", ex);
        }
        return list;
    }

    /**
     *
     * @return
     */
    public final SentenceList<FloorsInfo> getFloorsList() {
        return new StaticSentence(sessionDB,
                "SELECT ID, NAME FROM floors ORDER BY NAME",
                null,
                new SerializerReadClass(FloorsInfo.class));
    }

    /**
     *
     * @return
     */
    public final SentenceList<FloorsInfo> getFloorTablesList() {
        return new StaticSentence(sessionDB,
                "SELECT ID, NAME, SEATS FROM places ORDER BY NAME",
                null,
                new SerializerReadClass(FloorsInfo.class));
    }

    /**
     *
     * @param tickettype
     * @param ticketid
     * @return
     * @throws BasicException
     */
    public final TicketInfo loadTicket(final int tickettype, final int ticketid) throws BasicException {

        SerializerWrite<Object[]> sw = new SerializerWriteBasicExt(new Datas[]{Datas.INT, Datas.INT}, new int[]{0, 1});
        Object[] params = new Object[]{tickettype, ticketid};

        TicketInfo ticket = (TicketInfo) new PreparedSentence(sessionDB,
                "SELECT "
                + "T.ID, "
                + "T.TICKETTYPE, "
                + "T.TICKETID, "
                + "R.DATENEW, "
                + "R.MONEY, "
                + "R.ATTRIBUTES, "
                + "P.ID, "
                + "P.NAME, "
                + "T.CUSTOMER, "
                + "T.STATUS "
                + "FROM receipts R "
                + "JOIN tickets T ON R.ID = T.ID "
                + "LEFT OUTER JOIN people P ON T.PERSON = P.ID "
                + "WHERE T.TICKETTYPE = ? AND T.TICKETID = ? "
                + "ORDER BY R.DATENEW DESC",
                sw,
                new SerializerReadClass(TicketInfo.class))
                .find(params);

        setTicketData(ticket);

        return ticket;
    }
    
    /**
     * 
     * @param ticketType
     * @return
     * @throws BasicException 
     */
    public final TicketInfo loadLastTicket(final int ticketType) throws BasicException {

        SerializerWrite<Object[]> serialWriter = new SerializerWriteBasicExt(new Datas[]{Datas.INT}, new int[]{0});
        Object[] params = new Object[]{ticketType};

        TicketInfo ticket = (TicketInfo) new PreparedSentence(sessionDB,
                "SELECT "
                + "T.ID, "
                + "T.TICKETTYPE, "
                + "T.TICKETID, "
                + "R.DATENEW, "
                + "R.MONEY, "
                + "R.ATTRIBUTES, "
                + "P.ID, "
                + "P.NAME, "
                + "T.CUSTOMER, "
                + "T.STATUS "
                + "FROM receipts R "
                + "JOIN tickets T ON R.ID = T.ID "
                + "LEFT OUTER JOIN people P ON T.PERSON = P.ID "
                + "WHERE T.TICKETTYPE = ?  "
                + "ORDER BY R.DATENEW DESC LIMIT 1",
                serialWriter,
                new SerializerReadClass(TicketInfo.class))
                .find(params);

        setTicketData(ticket);

        return ticket;
    }

    private DataLogicCustomers getCustomerDataLogic() {
        if (app != null) {
            try {
                return app.getBean(DataLogicCustomers.class);
            } catch (BeanFactoryException ignored) {
            }
        }
        DataLogicCustomers fallback = new DataLogicCustomers();
        fallback.init(sessionDB);
        return fallback;
    }

    private void setTicketData(TicketInfo ticket) throws BasicException {
        if (ticket != null) {

            String customerid = ticket.getCustomerId();

            if (customerid != null) {
                ticket.setCustomer(getCustomerDataLogic().findCustomerInfoExtById(customerid));
            }

            ticket.setLines(new PreparedSentence(sessionDB,
                    "SELECT L.TICKET, L.LINE, L.PRODUCT, L.ATTRIBUTESETINSTANCE_ID, "
                    + "L.UNITS, L.PRICE, T.ID, T.NAME, T.CATEGORY, T.CUSTCATEGORY, "
                    + "T.PARENTID, T.RATE, T.RATECASCADE, T.RATEORDER, L.ATTRIBUTES "
                    + "FROM ticketlines L, taxes T "
                    + "WHERE L.TAXID = T.ID AND L.TICKET = ? ORDER BY L.LINE",
                    SerializerWriteString.INSTANCE,
                    new SerializerReadClass(TicketLineInfo.class)).list(ticket.getId()));

            ticket.setPayments(new PreparedSentence(sessionDB,
                    "SELECT PAYMENT, TOTAL, TRANSID, TENDERED, CARDNAME FROM payments WHERE RECEIPT = ?",
                    SerializerWriteString.INSTANCE,
                    new SerializerReadClass(PaymentInfoTicket.class)).list(ticket.getId()));
        }
    }

    /**
     * Save Ticket information (Receipt, Payments, Ticket, TaxLine, TicketLine,
     * Customer debt, Voucher)
     *
     * @param ticket
     * @param location
     * @throws BasicException
     */
    public final void saveTicket(final TicketInfo ticket, final String location) throws BasicException {

        Transaction t = new Transaction(sessionDB) {
            @Override
            public Object transact() throws BasicException {

                // Set Receipt Id
                if (ticket.getTicketId() == 0) {
                    switch (ticket.getTicketType()) {
                        case TicketInfo.RECEIPT_NORMAL:
                            ticket.setTicketId(getNextTicketIndex());
                            break;
                        case TicketInfo.RECEIPT_REFUND:
                            ticket.setTicketId(getNextTicketRefundIndex());
                            break;
                        case TicketInfo.RECEIPT_PAYMENT:
                            ticket.setTicketId(getNextTicketPaymentIndex());
                            break;
                        case TicketInfo.RECEIPT_NOSALE:
                            ticket.setTicketId(getNextTicketPaymentIndex());
                            break;
                        default:
                            throw new BasicException(
                                    "Ticket with unsupported TicketType. TicketType is: "
                                    + ticket.getTicketType());
                    }
                }

                // Ticket Properties
                byte[] properties = null;
                try {
                    ByteArrayOutputStream o = new ByteArrayOutputStream();
                    ticket.getProperties().storeToXML(o, AppLocal.APP_NAME, "UTF-8");
                    properties = o.toByteArray();
                }
                catch (IOException e) {
                    LOGGER.log(Level.WARNING, "Cannot convert ticket properties to XML ", e);
                }

                // Receipt Writer
                SerializerWrite<Object[]> sw = new SerializerWriteBasicExt(
                        new Datas[]{Datas.STRING, Datas.STRING, Datas.TIMESTAMP, Datas.BYTES,
                            Datas.STRING},
                        new int[]{0, 1, 2, 3, 4});
                Object[] params = new Object[]{
                    ticket.getId(),
                    ticket.getActiveCash(),
                    ticket.getDate(),
                    properties,
                    ticket.getProperty("person")};

                // Receipt Prepared
                new PreparedSentence(sessionDB,
                        "INSERT INTO receipts (ID, MONEY, DATENEW, ATTRIBUTES, PERSON) VALUES (?, ?, ?, ?, ?)",
                        sw)
                        .exec(params);

                // new ticket
                sw = new SerializerWriteBasicExt(
                        new Datas[]{Datas.STRING, Datas.INT, Datas.INT, Datas.STRING,
                            Datas.STRING, Datas.INT},
                        new int[]{0, 1, 2, 3, 4, 5});
                params = new Object[]{
                    ticket.getId(),
                    ticket.getTicketType(),
                    ticket.getTicketId(),
                    ticket.getUser().getId(),
                    ticket.getCustomerId(),
                    ticket.getTicketStatus()
                };
                new PreparedSentence(sessionDB,
                        "INSERT INTO tickets (ID, TICKETTYPE, TICKETID, PERSON, CUSTOMER, STATUS) "
                        + "VALUES (?, ?, ?, ?, ?, ?)",
                        sw)
                        .exec(params);

                // Ticket: Update status (This is Receipt or TicketType: 0)
                new PreparedSentence(sessionDB,
                        "UPDATE tickets SET STATUS = ? "
                        + "WHERE TICKETTYPE = 0 AND TICKETID = ?",
                        SerializerWriteParams.INSTANCE)
                        .exec(new DataParams() {

                            @Override
                            public void writeValues() throws BasicException {
                                setInt(1, ticket.getTicketId());
                                setInt(2, ticket.getTicketStatus());
                            }
                        });

                // Ticket Lines
                SentenceExec ticketlineinsert = new PreparedSentenceExec(sessionDB,
                        "INSERT INTO ticketlines (TICKET, LINE, "
                        + "PRODUCT, ATTRIBUTESETINSTANCE_ID, "
                        + "UNITS, PRICE, TAXID, ATTRIBUTES) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                        SerializerWriteBuilder.INSTANCE);

                for (TicketLineInfo l : ticket.getLines()) {
                    ticketlineinsert.exec(l);

                    if (l.getProductID() != null && l.isProductService() != true) {
                        getStockDiaryInsert().exec(new Object[]{
                            UUID.randomUUID().toString(),
                            ticket.getDate(),
                            l.getMultiply() < 0.0
                            ? MovementReason.IN_REFUND.getKey()
                            : MovementReason.OUT_SALE.getKey(),
                            location,
                            l.getProductID(),
                            l.getProductAttSetInstId(), -l.getMultiply(),
                            l.getPrice(),
                            ticket.getUser().getName()
                        });
                    }
                }

                // Native-style workflow approximation for Openbravo POS database persistence
                SentenceExec paymentinsert = new PreparedSentence(sessionDB,
                        "INSERT INTO payments (ID, RECEIPT, PAYMENT, TOTAL, TRANSID, RETURNMSG, TENDERED, CARDNAME, VOUCHER) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        SerializerWriteParams.INSTANCE);

                // Direct iteration over the ticket's native payment list
                for (PaymentInfo p : ticket.getPayments()) {

                    final String paymentMethod = p.getName();
                    final double paymentTotal = p.getTotal();
                    final double paymentTendered = p.getPaid(); // Or getTendered() depending on version fork
                    final String paymentCardName = p.getCardName();
                    final String paymentVoucherNumber = p.getVoucher();
                    final String paymentReturnMsg = ticket.getReturnMessage();

                    // Directly execute SQL Insert for each individual payment line item
                    paymentinsert.exec(new DataParams() {
                        @Override
                        public void writeValues() throws BasicException {
                            setString(1, UUID.randomUUID().toString());
                            setString(2, ticket.getId());
                            setString(3, paymentMethod);         // Stores "ccard", "ccard", "voucherin" on separate lines
                            setDouble(4, paymentTotal);          // Individual value for each respective card/voucher
                            setString(5, ticket.getTransactionID());
                            setBytes(6, Formats.BYTEA.parseValue(paymentReturnMsg));
                            setDouble(7, paymentTendered);
                            setString(8, paymentCardName);       // "Visa" on line 1, "Mastercard" on line 2
                            setString(9, paymentVoucherNumber);  // Voucher A on line 1, Voucher B on line 2
                        }
                    });

                    // Voucher Deactivation Logic (Executed on an isolated per-line basis)
                    if (paymentVoucherNumber != null) {
                        updateVoucherNonActive(paymentVoucherNumber);
                    }

                    // Customer Debt / Account Receivable Ledger Logic
                    if (isPaymentMethodCustomerDebt(paymentMethod)) {
                        ticket.getCustomer().updateCurDebt(paymentTotal, ticket.getDate());

                        updateCustomerDebt(
                                ticket.getCustomer().getId(),
                                ticket.getCustomer().getAccdebt(),
                                ticket.getCustomer().getCurdate()
                        );
                    }
                }

                if (ticket.getTaxes() != null) {
                    for (final TicketTaxInfo tickettax : ticket.getTaxes()) {
                        insertTicketTaxLine(ticket.getId(), tickettax.getTaxInfo().getId(), tickettax.getSubTotal(), tickettax.getTax());
                    }
                }
                return null;
            }
        };

        t.execute();
    }

    private int insertTicketTaxLine(String ticketId, String taxId, Double taxableAmount, Double taxAmount) throws BasicException {

        // TAX Lines
        SentenceExec taxlinesinsert = new PreparedSentence(sessionDB,
                "INSERT INTO taxlines (ID, RECEIPT, TAXID, BASE, AMOUNT) VALUES (?, ?, ?, ?, ?)",
                SerializerWriteParams.INSTANCE);

        return taxlinesinsert.exec(new DataParams() {
            @Override
            public void writeValues() throws BasicException {
                setString(1, UUID.randomUUID().toString());
                setString(2, ticketId);
                setString(3, taxId);
                setDouble(4, taxableAmount);
                setDouble(5, taxAmount);
            }
        });
    }

    private int updateVoucherNonActive(String voucherNumber) throws BasicException {
        return DataLogicVouchers.updateVoucherNonActive(voucherNumber, sessionDB);
    }

    private boolean isPaymentMethodCustomerDebt(String paymentMethod) {
        return PAYMENT_METHOD_DEBT.equals(paymentMethod) || PAYMENT_METHOD_DEBTPAID.equals(paymentMethod);
    }

    /**
     *
     * @param ticket
     * @param location
     * @throws BasicException
     */
    public final void deleteTicket(final TicketInfo ticket, final String location) throws BasicException {

        Transaction t;
        t = new Transaction(sessionDB) {
            @Override
            public Object transact() throws BasicException {

                // update the inventory
                Date nowDate = new Date();
                for (int ticketLineNumber = 0; ticketLineNumber < ticket.getLinesCount(); ticketLineNumber++) {

                    if (ticket.getLine(ticketLineNumber).getProductID() != null) {
                        getStockDiaryInsert().exec(new Object[]{
                            UUID.randomUUID().toString(),
                            nowDate,
                            ticket.getLine(ticketLineNumber).getMultiply() >= 0.0
                            ? MovementReason.IN_REFUND.getKey()
                            : MovementReason.OUT_SALE.getKey(),
                            location,
                            ticket.getLine(ticketLineNumber).getProductID(),
                            ticket.getLine(ticketLineNumber).getProductAttSetInstId(),
                            ticket.getLine(ticketLineNumber).getMultiply(),
                            ticket.getLine(ticketLineNumber).getPrice(),
                            ticket.getUser().getName()
                        });
                    }
                    // For productBundle
                    List<ProductsBundleInfo> bundle = getProductsBundle((String) ticket.getLine(ticketLineNumber).getProductID());

                    if (bundle.size() > 0) {
                        for (ProductsBundleInfo bundleComponent : bundle) {
                            ProductInfoExt bundleProduct = getProductInfoExtById(
                                    bundleComponent.getProductBundleId());

                            getStockDiaryInsert().exec(new Object[]{
                                UUID.randomUUID().toString(),
                                nowDate,
                                ticket.getLine(ticketLineNumber).getMultiply()
                                * bundleComponent
                                .getQuantity() >= 0.0
                                ? MovementReason.IN_REFUND
                                .getKey()
                                : MovementReason.OUT_SALE
                                .getKey(),
                                location,
                                bundleComponent.getProductBundleId(),
                                null,
                                ticket.getLine(ticketLineNumber).getMultiply()
                                * bundleComponent.getQuantity(),
                                bundleProduct.getPriceSell(),
                                ticket.getUser().getName()});
                        }
                    }
                }

                // update customer debts
                for (PaymentInfo p : ticket.getPayments()) {
                    if (isPaymentMethodCustomerDebt(p.getName())) {

                        // udate customer fields...
                        ticket.getCustomer().updateCurDebt(-p.getTotal(), ticket.getDate());

                        // save customer fields...
                        updateCustomerDebt(
                                ticket.getCustomer().getId(),
                                ticket.getCustomer().getAccdebt(),
                                ticket.getCustomer().getCurdate()
                        );
                    }
                }

                // and delete the receipt
                new StaticSentence(sessionDB,
                        "DELETE FROM taxlines WHERE RECEIPT = ?",
                        SerializerWriteString.INSTANCE).exec(ticket.getId());
                new StaticSentence(sessionDB,
                        "DELETE FROM payments WHERE RECEIPT = ?",
                        SerializerWriteString.INSTANCE).exec(ticket.getId());
                new StaticSentence(sessionDB,
                        "DELETE FROM ticketlines WHERE TICKET = ?",
                        SerializerWriteString.INSTANCE).exec(ticket.getId());
                new StaticSentence(sessionDB,
                        "DELETE FROM tickets WHERE ID = ?",
                        SerializerWriteString.INSTANCE).exec(ticket.getId());
                new StaticSentence(sessionDB,
                        "DELETE FROM receipts WHERE ID = ?",
                        SerializerWriteString.INSTANCE).exec(ticket.getId());
                return null;
            }
        };
        t.execute();
    }

    /**
     *
     * @throws BasicException
     */
    public final void resetPickup() throws BasicException {

        sessionDB.DB.resetSequenceSentence(sessionDB, "pickup_number").exec(0);
    }

    /**
     *
     * @return @throws BasicException
     */
    public final Integer getNextPickupIndex() throws BasicException {
        return (Integer) sessionDB.DB.getSequenceSentence(sessionDB, "pickup_number").find();
    }

    /**
     *
     * @return @throws BasicException
     */
    public final Integer getNextTicketIndex() throws BasicException {
        return (Integer) sessionDB.DB.getSequenceSentence(sessionDB, "ticketsnum").find();
    }

    /**
     *
     * @return @throws BasicException
     */
    public final Integer getNextTicketRefundIndex() throws BasicException {
        return (Integer) sessionDB.DB.getSequenceSentence(sessionDB, "ticketsnum_refund").find();
    }

    /**
     *
     * @return @throws BasicException
     */
    public final Integer getNextTicketPaymentIndex() throws BasicException {
        return (Integer) sessionDB.DB.getSequenceSentence(sessionDB, "ticketsnum_payment").find();
    }

    // JG 3 Feb 16 - Product load speedup
    public final SentenceFind getProductImage() {
        return new PreparedSentence(sessionDB,
                "SELECT IMAGE FROM products WHERE ID = ?",
                SerializerWriteString.INSTANCE,
                (DataRead dr) -> ImageUtils.readImage(dr.getBytes(1)));
    }

    public final BufferedImage getProductImage(String imageId) {

        try {
            return (BufferedImage) getProductImage().find(imageId);
        }
        catch (BasicException e) {
            return null;
        }
    }

    /**
     *
     * @return
     */
    public final int updateCustomerDebt(String customerId, Double accDebt, Date date) throws BasicException {

        return new PreparedSentence(sessionDB,
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

    /**
     * ProductBundle version
     *
     * @return
     */
    public final SentenceExec getStockDiaryInsert() {
        return new SentenceExecTransaction(sessionDB) {
            @Override
            /**
             * @param params[0] String STOCKDIARY.ID
             * @param params[1] Date Timestamp
             * @param params[2] Integer Reason
             * @param params[3] String Location
             * @param params[4] String Product ID
             * @param params[5] String Attribute instance ID
             * @param params[6] Double Units
             * @param params[7] Double Price
             * @param params[8] String Application User
             */
            public int execInTransaction(Object[] params) throws BasicException {

                Object[] adjustParams = new Object[4];
                Object[] paramsArray = (Object[]) params;
                adjustParams[0] = paramsArray[4]; // product ->Location
                adjustParams[1] = paramsArray[3]; // location -> Product
                adjustParams[2] = paramsArray[5]; // attributesetinstance
                adjustParams[3] = paramsArray[6]; // units
                adjustStock(adjustParams);

                return new PreparedSentence(sessionDB,
                        "INSERT INTO stockdiary (ID, DATENEW, REASON, LOCATION, "
                        + "PRODUCT, ATTRIBUTESETINSTANCE_ID, "
                        + "UNITS, PRICE, AppUser) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        new SerializerWriteBasicExt(stockdiaryDatas,
                                new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8}))
                        .exec(params);
            }
        };
    }

    /**
     *
     * @return
     */
    public final SentenceExec getStockDiaryInsert1() {
        return new SentenceExecTransaction(sessionDB) {
            @Override
            public int execInTransaction(Object[] params) throws BasicException {
                int updateresult = params[5] == null
                        ? new PreparedSentence(sessionDB,
                                "UPDATE stockcurrent SET UNITS = (UNITS + ?) "
                                + "WHERE LOCATION = ? AND PRODUCT = ? "
                                + "AND ATTRIBUTESETINSTANCE_ID IS NULL",
                                new SerializerWriteBasicExt(stockdiaryDatas,
                                        new int[]{6, 3, 4}))
                                .exec(params)
                        : new PreparedSentence(sessionDB,
                                "UPDATE stockcurrent SET UNITS = (UNITS + ?) "
                                + "WHERE LOCATION = ? AND PRODUCT = ? "
                                + "AND ATTRIBUTESETINSTANCE_ID = ?",
                                new SerializerWriteBasicExt(stockdiaryDatas,
                                        new int[]{6, 3, 4, 5}))
                                .exec(params);

                if (updateresult == 0) {
                    new PreparedSentence(sessionDB,
                            "INSERT INTO stockcurrent (LOCATION, PRODUCT, "
                            + "ATTRIBUTESETINSTANCE_ID, UNITS) "
                            + "VALUES (?, ?, ?, ?)",
                            new SerializerWriteBasicExt(stockdiaryDatas,
                                    new int[]{3, 4, 5, 6}))
                            .exec(params);
                }
                return new PreparedSentence(sessionDB,
                        "INSERT INTO stockdiary (ID, DATENEW, REASON, LOCATION, PRODUCT, "
                        + "ATTRIBUTESETINSTANCE_ID, UNITS, PRICE, AppUser, "
                        + "SUPPLIER, SUPPLIERDOC) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        new SerializerWriteBasicExt(stockdiaryDatas,
                                new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10}))
                        .exec(params);

            }
        };
    }

    public final void saveStockDiary(ProductStockTransaction prodStock) throws BasicException {

        getStockDiaryInsert1().exec(new Object[]{
            prodStock.getId(),
            prodStock.getTransactionDate(),
            prodStock.getReasonId(),
            prodStock.getLocationId(),
            prodStock.getProductId(),
            prodStock.getProductAttribSetId(),
            prodStock.getUnits(),
            prodStock.getPrice(),
            prodStock.getUserId(),
            prodStock.getSupplierId(),
            prodStock.getSupplierDoc()
        });
    }

    /**
     *
     * @return
     */
    public final SentenceExec getStockDiaryDelete() {
        return new SentenceExecTransaction(sessionDB) {
            @Override
            public int execInTransaction(Object[] params) throws BasicException {
                int updateresult = ((Object[]) params)[5] == null // if ATTRIBUTESETINSTANCE_ID is null
                        ? new PreparedSentence(sessionDB,
                                "UPDATE stockcurrent SET UNITS = (UNITS - ?) "
                                + "WHERE LOCATION = ? AND PRODUCT = ? "
                                + "AND ATTRIBUTESETINSTANCE_ID IS NULL",
                                new SerializerWriteBasicExt(stockdiaryDatas,
                                        new int[]{6, 3, 4}))
                                .exec(params)
                        : new PreparedSentence(sessionDB,
                                "UPDATE stockcurrent SET UNITS = (UNITS - ?) "
                                + "WHERE LOCATION = ? AND PRODUCT = ? "
                                + "AND ATTRIBUTESETINSTANCE_ID = ?",
                                new SerializerWriteBasicExt(stockdiaryDatas,
                                        new int[]{6, 3, 4, 5}))
                                .exec(params);

                if (updateresult == 0) {
                    new PreparedSentence(sessionDB,
                            "INSERT INTO stockcurrent (LOCATION, PRODUCT, "
                            + "ATTRIBUTESETINSTANCE_ID, UNITS) "
                            + "VALUES (?, ?, ?, -(?))",
                            new SerializerWriteBasicExt(stockdiaryDatas,
                                    new int[]{3, 4, 5, 6}))
                            .exec(params);
                }
                return new PreparedSentence(sessionDB,
                        "DELETE FROM stockdiary WHERE ID = ?",
                        new SerializerWriteBasicExt(stockdiaryDatas, new int[]{0}))
                        .exec(params);
            }
        };
    }

    private void adjustStock(Object[] params) throws BasicException {

        List<ProductsBundleInfo> bundle = getProductsBundle((String) params[0]);

        if (bundle.size() > 0) {

            for (ProductsBundleInfo component : bundle) {
                Object[] adjustParams = new Object[4];
                adjustParams[0] = component.getProductBundleId();
                adjustParams[1] = ((Object[]) params)[1];
                adjustParams[2] = ((Object[]) params)[2];
                adjustParams[3] = ((Double) ((Object[]) params)[3]) * component.getQuantity();
                adjustStock(adjustParams);
            }
        } else {

            int updateresult = ((Object[]) params)[2] == null
                    ? new PreparedSentence(sessionDB,
                            "UPDATE stockcurrent SET UNITS = (UNITS + ?) "
                            + "WHERE LOCATION = ? AND PRODUCT = ? "
                            + "AND ATTRIBUTESETINSTANCE_ID IS NULL",
                            new SerializerWriteBasicExt(stockAdjustDatas,
                                    new int[]{3, 1, 0}))
                            .exec(params)
                    : new PreparedSentence(sessionDB,
                            "UPDATE stockcurrent SET UNITS = (UNITS + ?) "
                            + "WHERE LOCATION = ? AND PRODUCT = ? "
                            + "AND ATTRIBUTESETINSTANCE_ID = ?",
                            new SerializerWriteBasicExt(stockAdjustDatas,
                                    new int[]{3, 1, 0, 2}))
                            .exec(params);

            if (updateresult == 0) {

                new PreparedSentence(sessionDB,
                        "INSERT INTO stockcurrent (LOCATION, PRODUCT, "
                        + "ATTRIBUTESETINSTANCE_ID, UNITS) "
                        + "VALUES (?, ?, ?, ?)",
                        new SerializerWriteBasicExt(stockAdjustDatas,
                                new int[]{1, 0, 2, 3}))
                        .exec(params);
            }
        }
    }

    /**
     *
     */
    private List<ProductsBundleInfo> getProductsBundle(String productId) throws BasicException {
        return DataLogicPIM.getProductsBundle(productId, sessionDB);
    }

    private ProductInfoExt getProductInfoExtById(String productId) throws BasicException {
        return DataLogicPIM.getProductInfoExtById(productId, sessionDB);
    }

    /**
     *
     * @return
     */
    public final SentenceExec getPaymentMovementInsert() {
        return new SentenceExecTransaction(sessionDB) {
            @Override
            public int execInTransaction(Object[] params) throws BasicException {
                new PreparedSentence(sessionDB,
                        "INSERT INTO receipts (ID, MONEY, DATENEW) "
                        + "VALUES (?, ?, ?)",
                        new SerializerWriteBasicExt(paymenttabledatas,
                                new int[]{0, 1, 2}))
                        .exec(params);
                return new PreparedSentence(sessionDB,
                        "INSERT INTO payments (ID, RECEIPT, PAYMENT, TOTAL, NOTES) "
                        + "VALUES (?, ?, ?, ?, ?)",
                        new SerializerWriteBasicExt(paymenttabledatas,
                                new int[]{3, 0, 4, 5, 6}))
                        .exec(params);
            }
        };
    }

    /**
     *
     * @return
     */
    public final SentenceExec getPaymentMovementDelete() {
        return new SentenceExecTransaction(sessionDB) {
            @Override
            public int execInTransaction(Object[] params) throws BasicException {
                new PreparedSentence(sessionDB,
                        "DELETE FROM payments WHERE ID = ?",
                        new SerializerWriteBasicExt(paymenttabledatas, new int[]{3}))
                        .exec(params);
                return new PreparedSentence(sessionDB,
                        "DELETE FROM receipts WHERE ID = ?",
                        new SerializerWriteBasicExt(paymenttabledatas, new int[]{0}))
                        .exec(params);
            }
        };
    }

    /**
     *
     * @param warehouse
     * @param id
     * @param attsetinstid
     * @return
     * @throws BasicException
     */
    public final double findProductStock(String warehouse, String id, String attsetinstid) throws BasicException {

        PreparedSentence p = attsetinstid == null
                ? new PreparedSentence(sessionDB, "SELECT UNITS FROM stockcurrent "
                        + "WHERE LOCATION = ? AND PRODUCT = ? AND ATTRIBUTESETINSTANCE_ID IS NULL",
                        new SerializerWriteBasic(Datas.STRING, Datas.STRING),
                        SerializerReadDouble.INSTANCE)
                : new PreparedSentence(sessionDB, "SELECT UNITS FROM stockcurrent "
                        + "WHERE LOCATION = ? AND PRODUCT = ? AND ATTRIBUTESETINSTANCE_ID = ?",
                        new SerializerWriteBasic(Datas.STRING, Datas.STRING, Datas.STRING),
                        SerializerReadDouble.INSTANCE);

        Double d = (Double) p.find(warehouse, id, attsetinstid);
        return d == null ? 0.0 : d;
    }

    /**
     * Add all product from a category to Catalog
     *
     * @param categoryId
     * @return num added of products
     */
    public final int addProductsToCatalogWithCategoryId(String categoryId) throws BasicException {
        StaticSentence sentence = new StaticSentence(sessionDB,
                "INSERT INTO products_cat(PRODUCT, CATORDER) SELECT ID, " + sessionDB.DB.INTEGER_NULL()
                + " FROM products WHERE CATEGORY = ?",
                SerializerWriteString.INSTANCE);

        return sentence.exec(categoryId);
    }

    /**
     *
     * @param categoryId
     * @return number of removed products
     */
    public final int removeProductsFromCatalogWithCategoryId(String categoryId) throws BasicException {
        StaticSentence sentence = new StaticSentence(sessionDB,
                "DELETE FROM products_cat WHERE PRODUCT IN (SELECT ID "
                + "FROM products WHERE CATEGORY = ?)",
                SerializerWriteString.INSTANCE);

        return sentence.exec(categoryId);
    }

    public final TableDefinition getTableTaxes() {
        return new TableDefinition(sessionDB,
                "taxes",
                new String[]{"ID", "NAME", "CATEGORY", "CUSTCATEGORY", "PARENTID", "RATE",
                    "RATECASCADE",
                    "RATEORDER"},
                new String[]{"ID", AppLocal.getIntString("label.name"),
                    AppLocal.getIntString("label.taxcategory"),
                    AppLocal.getIntString("label.custtaxcategory"),
                    AppLocal.getIntString("label.taxparent"),
                    AppLocal.getIntString("label.dutyrate"),
                    AppLocal.getIntString("label.cascade"),
                    AppLocal.getIntString("label.order")},
                new Datas[]{Datas.STRING, Datas.STRING, Datas.STRING, Datas.STRING, Datas.STRING,
                    Datas.DOUBLE,
                    Datas.BOOLEAN, Datas.INT},
                new Formats[]{Formats.STRING, Formats.STRING, Formats.STRING, Formats.STRING,
                    Formats.STRING,
                    Formats.PERCENT, Formats.BOOLEAN, Formats.INT},
                new int[]{0});
    }

    public final TableDefinition getTableTaxCustCategories() {
        return new TableDefinition(sessionDB,
                "taxcustcategories",
                new String[]{"ID", "NAME"},
                new String[]{"ID", AppLocal.getIntString("label.name")},
                new Datas[]{Datas.STRING, Datas.STRING},
                new Formats[]{Formats.STRING, Formats.STRING},
                new int[]{0});
    }

    /**
     *
     * @return
     */
    public final TableDefinition getTableTaxCategories() {
        return new TableDefinition(sessionDB,
                "taxcategories",
                new String[]{"ID", "NAME"},
                new String[]{"ID", AppLocal.getIntString("label.name")},
                new Datas[]{Datas.STRING, Datas.STRING},
                new Formats[]{Formats.STRING, Formats.STRING},
                new int[]{0});
    }

    /**
     *
     * @return
     */
    public final TableDefinition getTableLocations() {
        return new TableDefinition(sessionDB,
                "locations",
                new String[]{"ID", "NAME", "ADDRESS"},
                new String[]{"ID", AppLocal.getIntString("label.locationname"),
                    AppLocal.getIntString("label.locationaddress")},
                new Datas[]{Datas.STRING, Datas.STRING, Datas.STRING},
                new Formats[]{Formats.STRING, Formats.STRING, Formats.STRING},
                new int[]{0});
    }

    public final UomInfo getUomInfoById(String id) throws BasicException {
        return (UomInfo) new PreparedSentence(sessionDB,
                "SELECT "
                + "id, name "
                + "FROM uom "
                + "WHERE id = ?",
                SerializerWriteString.INSTANCE, UomInfo.getSerializerRead()).find(id);
    }

    public final TableDefinition getTableUom() {
        return new TableDefinition(sessionDB,
                "uom",
                new String[]{"id", "name"},
                new String[]{"id",
                    AppLocal.getIntString("label.name")},
                new Datas[]{
                    Datas.STRING, Datas.STRING},
                new Formats[]{
                    Formats.STRING, Formats.STRING},
                new int[]{0});
    }

    public final SentenceList<UomInfo> getUomList() {
        return new StaticSentence(sessionDB, "SELECT ID, NAME  FROM uom ORDER BY NAME", null,
                UomInfo.getSerializerRead());
    }

    public final List<UomInfo> getUomListAll() {
        List<UomInfo> list = null;
        try {
            list = this.getUomList().list();
        }
        catch (BasicException ex) {
            LOGGER.log(Level.WARNING, "Cannot get UomInfo list", ex);
        }
        return list;
    }

    /**
     *
     * @return
     */
    public final SentenceExec getCustomerInsert() {
        return new SentenceExecTransaction(sessionDB) {
            @Override
            public int execInTransaction(Object[] params) throws BasicException {
                int i = new PreparedSentence(sessionDB,
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
                        new SerializerWriteBasicExt(customersRow.getDatas(),
                                new int[]{0,
                                    1, 2, 3, 4, 5, 6,
                                    7, 8, 9, 10, 11, 12,
                                    13, 14, 15, 16, 17, 18,
                                    19, 20, 21, 22, 23, 24,
                                    25, 26}))
                        .exec(params);
                return i;
            }
        };
    }

    /**
     *
     * @return
     */
    public final SentenceExec getCustomerUpdate() {
        return new SentenceExecTransaction(sessionDB) {
            @Override
            public int execInTransaction(Object[] params) throws BasicException {

                int i = new PreparedSentence(sessionDB,
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
                        new SerializerWriteBasicExt(customersRow.getDatas(),
                                new int[]{0,
                                    1, 2, 3, 4, 5,
                                    6, 7, 8, 9, 10,
                                    11, 12, 13, 14, 15,
                                    16, 17, 18, 19, 20,
                                    21, 22, 23, 24, 25,
                                    26, 0}))
                        .exec(params);
                return i;
            }
        };
    }

    public final SentenceExec getCustomerDelete() {
        return new SentenceExecTransaction(sessionDB) {
            @Override
            public int execInTransaction(Object[] params) throws BasicException {
                return new PreparedSentence(sessionDB,
                        "DELETE FROM customers WHERE ID = ?",
                        new SerializerWriteBasicExt(customersRow.getDatas(),
                                new int[]{0}))
                        .exec(params);
            }
        };
    }

    public final void addTicketLineRemoved(String username, String ticketId, String productId, String productName, double quantity) {

        final SentenceExec m_lineremoved = new StaticSentence(this.sessionDB,
                """
                INSERT INTO lineremoved (NAME, TICKETID, PRODUCTID, PRODUCTNAME, UNITS, REMOVEDDATE)
                    VALUES (?, ?, ?, ?, ?, ?)
                """,
                new SerializerWriteBasic(new Datas[]{
            Datas.STRING, Datas.STRING,
            Datas.STRING, Datas.STRING,
            Datas.DOUBLE, Datas.TIMESTAMP
        }));

        try {
            Object[] line = new Object[]{username, ticketId, productId, productName, quantity, new Date()};

            m_lineremoved.exec(line);
        }
        catch (BasicException e) {
            LOGGER.log(Level.SEVERE, "Exception on execute line removed: ", e);
        }
    }

    public final void addTicketDeleted(String username) {
        final SentenceExec m_ticketremoved = new StaticSentence(this.sessionDB,
                """
                INSERT INTO lineremoved (NAME, TICKETID, PRODUCTNAME, UNITS, REMOVEDDATE)
                    VALUES (?, ?, ?, ?, ?)
                """,
                new SerializerWriteBasic(new Datas[]{
            Datas.STRING, Datas.STRING,
            Datas.STRING, Datas.DOUBLE, Datas.TIMESTAMP
        }));
        try {
            Object[] ticketDeleted = new Object[]{username, "Void", "Ticket Deleted", 0.0, new Date()};
            m_ticketremoved.exec(ticketDeleted);
        }
        catch (BasicException e) {
            LOGGER.log(Level.SEVERE, "Exception on execute ticket removed: ", e);
        }
    }

}
