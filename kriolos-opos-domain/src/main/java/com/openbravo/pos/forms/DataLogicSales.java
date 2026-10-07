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
import com.openbravo.pos.sales.DataLogicTax;
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
import com.openbravo.pos.sales.restaurant.DataLogicRestaurant;
import com.openbravo.pos.sales.DataLogicAudit;
import com.openbravo.pos.payment.DataLogicPayments;
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

        paymenttabledatas = null;

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

        customersRow = null;
    }

    /**
     *
     * @param s session
     */
    @Override
    public void init(Session s) {
        this.sessionDB = s;
    }

    /**
     * @deprecated Use {@link DataLogicCustomers#getCustomersRow()} instead.
     */
    @Deprecated
    public final Row getCustomersRow() {
        return getCustomerDataLogic().getCustomersRow();
    }

    public DataLogicInventory getDataLogicInventory() {
        if (app != null) {
            try {
                return app.getBean(DataLogicInventory.class);
            } catch (BeanFactoryException ignored) {
            }
        }
        DataLogicInventory fallback = new DataLogicInventory();
        fallback.init(sessionDB);
        return fallback;
    }

    public DataLogicTax getDataLogicTax() {
        if (app != null) {
            try {
                return app.getBean(DataLogicTax.class);
            } catch (BeanFactoryException ignored) {
            }
        }
        DataLogicTax fallback = new DataLogicTax();
        fallback.init(sessionDB);
        return fallback;
    }

    public DataLogicRestaurant getDataLogicRestaurant() {
        if (app != null) {
            try {
                return app.getBean(DataLogicRestaurant.class);
            } catch (BeanFactoryException ignored) {
            }
        }
        DataLogicRestaurant fallback = new DataLogicRestaurant();
        fallback.init(sessionDB);
        return fallback;
    }

    public DataLogicPIM getDataLogicPIM() {
        if (app != null) {
            try {
                return app.getBean(DataLogicPIM.class);
            } catch (BeanFactoryException ignored) {
            }
        }
        DataLogicPIM fallback = new DataLogicPIM();
        fallback.init(sessionDB);
        return fallback;
    }

    public DataLogicPayments getDataLogicPayments() {
        if (app != null) {
            try {
                return app.getBean(DataLogicPayments.class);
            } catch (BeanFactoryException ignored) {
            }
        }
        DataLogicPayments fallback = new DataLogicPayments();
        fallback.init(sessionDB);
        return fallback;
    }

    public DataLogicAudit getDataLogicAudit() {
        if (app != null) {
            try {
                return app.getBean(DataLogicAudit.class);
            } catch (BeanFactoryException ignored) {
            }
        }
        DataLogicAudit fallback = new DataLogicAudit();
        fallback.init(sessionDB);
        return fallback;
    }

    /**
     * @deprecated Use {@link DataLogicInventory#getProductStockState(String, String)} instead.
     */
    @Deprecated
    public final ProductStock getProductStockState(String pId, String location) throws BasicException {
        return getDataLogicInventory().getProductStockState(pId, location);
    }

    /**
     * @deprecated Use {@link DataLogicInventory#getProductStockList(String)} instead.
     */
    @Deprecated
    public final List<ProductStock> getProductStockList(String pId) throws BasicException {
        return getDataLogicInventory().getProductStockList(pId);
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

    /**
     * @deprecated Use {@link DataLogicTax#getTaxCategoryInfoList()} instead.
     */
    @Deprecated
    public final SentenceList<TaxCategoryInfo> getTaxCategoryInfoList() {
        return getDataLogicTax().getTaxCategoryInfoList();
    }

    /**
     * @deprecated Use {@link DataLogicTax#getTaxList()} instead.
     */
    @Deprecated
    public final SentenceList<TaxInfo> getTaxList() {
        return getDataLogicTax().getTaxList();
    }

    /**
     * @deprecated Use {@link DataLogicTax#getTaxListAll()} instead.
     */
    @Deprecated
    public final List<TaxInfo> getTaxListAll() {
        return getDataLogicTax().getTaxListAll();
    }

    /**
     * @deprecated Use {@link DataLogicTax#getTaxCustCategoriesList()} instead.
     */
    @Deprecated
    public final SentenceList<TaxCustCategoryInfo> getTaxCustCategoriesList() {
        return getDataLogicTax().getTaxCustCategoriesList();
    }

    /**
     * JG Apr 2017 - Revised to return Customer Id - cId param
     *
     * @param cId
     * @return
     * @throws BasicException
     * @deprecated Use {@link DataLogicCustomers#getCustomersTransactionList(String)} instead.
     */
    @Deprecated
    public final List<CustomerTransaction> getCustomersTransactionList(String cId) throws BasicException {
        return getDataLogicCustomers().getCustomersTransactionList(cId);
    }

    /**
     * @deprecated Use {@link DataLogicTax#getTaxCategoriesList()} instead.
     */
    @Deprecated
    public final SentenceList<TaxCategoryInfo> getTaxCategoriesList() {
        return getDataLogicTax().getTaxCategoriesList();
    }

    /**
     * @deprecated Use {@link DataLogicTax#getTaxCategoriesListAll()} instead.
     */
    @Deprecated
    public final List<TaxCategoryInfo> getTaxCategoriesListAll() {
        return getDataLogicTax().getTaxCategoriesListAll();
    }

    /**
     * @deprecated Use {@link DataLogicPIM#getAttributeSetList()} instead.
     * @return
     */
    @Deprecated
    public final SentenceList<AttributeSetInfo> getAttributeSetList() {
        return getDataLogicPIM().getAttributeSetList();
    }

    /**
     * @deprecated Use {@link DataLogicPIM#getAttributeSetListAll()} instead.
     * @return
     */
    @Deprecated
    public final List<AttributeSetInfo> getAttributeSetListAll() {
        return getDataLogicPIM().getAttributeSetListAll();
    }

    /**
     * @deprecated Since Nov/2025
     * @return
    /**
     * @deprecated Use {@link DataLogicInventory#getLocationsList()} instead.
     */
    @Deprecated
    public final SentenceList<LocationInfo> getLocationsList() {
        return getDataLogicInventory().getLocationsList();
    }

    /**
     * @deprecated Use {@link DataLogicInventory#getLocationsListAll()} instead.
     */
    @Deprecated
    public final List<LocationInfo> getLocationsListAll() {
        return getDataLogicInventory().getLocationsListAll();
    }

    /**
     * @deprecated Use {@link DataLogicRestaurant#getFloorsList()} instead.
     */
    @Deprecated
    public final SentenceList<FloorsInfo> getFloorsList() {
        return getDataLogicRestaurant().getFloorsList();
    }

    /**
     * @deprecated Use {@link DataLogicRestaurant#getFloorTablesList()} instead.
     */
    @Deprecated
    public final SentenceList<FloorsInfo> getFloorTablesList() {
        return getDataLogicRestaurant().getFloorTablesList();
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

    public DataLogicCustomers getDataLogicCustomers() {
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

    public DataLogicCustomers getCustomerDataLogic() {
        return getDataLogicCustomers();
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
     * @deprecated Use {@link DataLogicPayments#getNextTicketPaymentIndex()} instead.
     */
    @Deprecated
    public final Integer getNextTicketPaymentIndex() throws BasicException {
        return getDataLogicPayments().getNextTicketPaymentIndex();
    }

    // JG 3 Feb 16 - Product load speedup
    /**
     * @deprecated Use {@link DataLogicPIM#getProductImage()} instead.
     */
    @Deprecated
    public final SentenceFind getProductImage() {
        return getDataLogicPIM().getProductImage();
    }

    /**
     * @deprecated Use {@link DataLogicPIM#getProductImage(String)} instead.
     */
    @Deprecated
    public final BufferedImage getProductImage(String imageId) {
        return getDataLogicPIM().getProductImage(imageId);
    }

    /**
     *
     * @return
     * @deprecated Use {@link DataLogicCustomers#updateCustomerDebt(String, Double, Date)} instead.
     */
    @Deprecated
    public final int updateCustomerDebt(String customerId, Double accDebt, Date date) throws BasicException {
        return getDataLogicCustomers().updateCustomerDebt(customerId, accDebt, date);
    }

    /**
     * ProductBundle version
     *
     * @return
     */
    /**
     * @deprecated Use {@link DataLogicInventory#getStockDiaryInsert()} instead.
     */
    @Deprecated
    public final SentenceExec getStockDiaryInsert() {
        return getDataLogicInventory().getStockDiaryInsert();
    }

    /**
     * @deprecated Use {@link DataLogicInventory#getStockDiaryInsert1()} instead.
     */
    @Deprecated
    public final SentenceExec getStockDiaryInsert1() {
        return getDataLogicInventory().getStockDiaryInsert1();
    }

    /**
     * @deprecated Use {@link DataLogicInventory#saveStockDiary(ProductStockTransaction)} instead.
     */
    @Deprecated
    public final void saveStockDiary(ProductStockTransaction prodStock) throws BasicException {
        getDataLogicInventory().saveStockDiary(prodStock);
    }

    /**
     * @deprecated Use {@link DataLogicInventory#getStockDiaryDelete()} instead.
     */
    @Deprecated
    public final SentenceExec getStockDiaryDelete() {
        return getDataLogicInventory().getStockDiaryDelete();
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
     * @deprecated Use {@link DataLogicPayments#getPaymentMovementInsert()} instead.
     */
    @Deprecated
    public final SentenceExec getPaymentMovementInsert() {
        return getDataLogicPayments().getPaymentMovementInsert();
    }

    /**
     *
     * @return
     * @deprecated Use {@link DataLogicPayments#getPaymentMovementDelete()} instead.
     */
    @Deprecated
    public final SentenceExec getPaymentMovementDelete() {
        return getDataLogicPayments().getPaymentMovementDelete();
    }

    /**
     * @deprecated Use {@link DataLogicInventory#findProductStock(String, String, String)} instead.
     */
    @Deprecated
    public final double findProductStock(String warehouse, String id, String attsetinstid) throws BasicException {
        return getDataLogicInventory().findProductStock(warehouse, id, attsetinstid);
    }

    /**
     * Add all product from a category to Catalog
     *
     * @param categoryId
     * @return num added of products
     * @deprecated Use {@link DataLogicPIM#addProductsToCatalogWithCategoryId(String)} instead.
     */
    @Deprecated
    public final int addProductsToCatalogWithCategoryId(String categoryId) throws BasicException {
        return getDataLogicPIM().addProductsToCatalogWithCategoryId(categoryId);
    }

    /**
     *
     * @param categoryId
     * @return number of removed products
     * @deprecated Use {@link DataLogicPIM#removeProductsFromCatalogWithCategoryId(String)} instead.
     */
    @Deprecated
    public final int removeProductsFromCatalogWithCategoryId(String categoryId) throws BasicException {
        return getDataLogicPIM().removeProductsFromCatalogWithCategoryId(categoryId);
    }

    /**
     * @deprecated Use {@link DataLogicTax#getTableTaxes()} instead.
     */
    @Deprecated
    public final TableDefinition getTableTaxes() {
        return getDataLogicTax().getTableTaxes();
    }

    /**
     * @deprecated Use {@link DataLogicTax#getTableTaxCustCategories()} instead.
     */
    @Deprecated
    public final TableDefinition getTableTaxCustCategories() {
        return getDataLogicTax().getTableTaxCustCategories();
    }

    /**
     * @deprecated Use {@link DataLogicTax#getTableTaxCategories()} instead.
     */
    @Deprecated
    public final TableDefinition getTableTaxCategories() {
        return getDataLogicTax().getTableTaxCategories();
    }

    /**
     * @deprecated Use {@link DataLogicInventory#getTableLocations()} instead.
     */
    @Deprecated
    public final TableDefinition getTableLocations() {
        return getDataLogicInventory().getTableLocations();
    }

    /**
     * @deprecated Use {@link DataLogicPIM#getUomInfoById(String)} instead.
     */
    @Deprecated
    public final UomInfo getUomInfoById(String id) throws BasicException {
        return getDataLogicPIM().getUomInfoById(id);
    }

    /**
     * @deprecated Use {@link DataLogicPIM#getTableUom()} instead.
     */
    @Deprecated
    public final TableDefinition getTableUom() {
        return getDataLogicPIM().getTableUom();
    }

    /**
     * @deprecated Use {@link DataLogicPIM#getUomList()} instead.
     */
    @Deprecated
    public final SentenceList<UomInfo> getUomList() {
        return getDataLogicPIM().getUomList();
    }

    /**
     * @deprecated Use {@link DataLogicPIM#getUomListAll()} instead.
     */
    @Deprecated
    public final List<UomInfo> getUomListAll() {
        return getDataLogicPIM().getUomListAll();
    }

    /**
     *
     * @return
     * @deprecated Use {@link DataLogicCustomers#getCustomerInsert()} instead.
     */
    @Deprecated
    public final SentenceExec getCustomerInsert() {
        return getDataLogicCustomers().getCustomerInsert();
    }

    /**
     *
     * @return
     * @deprecated Use {@link DataLogicCustomers#getCustomerUpdate()} instead.
     */
    @Deprecated
    public final SentenceExec getCustomerUpdate() {
        return getDataLogicCustomers().getCustomerUpdate();
    }

    /**
     * @deprecated Use {@link DataLogicCustomers#getCustomerDelete()} instead.
     */
    @Deprecated
    public final SentenceExec getCustomerDelete() {
        return getDataLogicCustomers().getCustomerDelete();
    }

    /**
     * @deprecated Use {@link DataLogicAudit#addTicketLineRemoved(String, String, String, String, double)} instead.
     */
    @Deprecated
    public final void addTicketLineRemoved(String username, String ticketId, String productId, String productName, double quantity) {
        getDataLogicAudit().addTicketLineRemoved(username, ticketId, productId, productName, quantity);
    }

    /**
     * @deprecated Use {@link DataLogicAudit#addTicketDeleted(String)} instead.
     */
    @Deprecated
    public final void addTicketDeleted(String username) {
        getDataLogicAudit().addTicketDeleted(username);
    }

}
