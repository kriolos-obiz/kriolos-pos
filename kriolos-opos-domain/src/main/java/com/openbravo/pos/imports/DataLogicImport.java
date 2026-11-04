package com.openbravo.pos.imports;

import com.openbravo.pos.container.BeanFactoryDataSingle;
import com.openbravo.data.loader.Session;
import com.openbravo.data.loader.SentenceExec;
import com.openbravo.data.loader.SentenceFind;
import com.openbravo.data.loader.StaticSentence;
import com.openbravo.data.loader.PreparedSentence;
import com.openbravo.data.loader.SerializerWriteBasic;
import com.openbravo.data.loader.SerializerWriteString;
import com.openbravo.data.loader.SerializerRead;
import com.openbravo.data.loader.DataRead;
import com.openbravo.data.loader.Datas;
import com.openbravo.basic.BasicException;

public class DataLogicImport extends BeanFactoryDataSingle implements ImportService {

    protected Session sessionDB;

    @Override
    public void init(Session s) {
        sessionDB = s;
    }

    public Session getSession() {
        return sessionDB;
    }

    @Override
    public final void execCSVStockUpdate(StockQuantityImportEntry entry) throws BasicException {
        final SentenceExec m_insertStockUpdateCSVEntry = new StaticSentence(this.sessionDB,
                "INSERT INTO csvimport ( "
                + "ID, ROWNUMBER, CSVERROR, REFERENCE, CODE, PRICEBUY ) "
                + "VALUES (?, ?, ?, ?, ?, ?)",
                new SerializerWriteBasic(new Datas[]{
            Datas.STRING, Datas.STRING, Datas.STRING, Datas.STRING, Datas.STRING, Datas.DOUBLE
        }));
        m_insertStockUpdateCSVEntry.exec(new Object[]{
            entry.id(), entry.rowNumber(), entry.csvError(), entry.locationId(), entry.barcode(), entry.quantity()});
    }

    @Override
    @Deprecated
    public final void execCSVStockUpdate(Object[] csv) throws BasicException {
        execCSVStockUpdate(new StockQuantityImportEntry(
                (String) csv[0],
                (String) csv[1],
                (String) csv[2],
                (String) csv[3],
                (String) csv[4],
                ((Number) csv[5]).doubleValue()));
    }

    public final void execAddCSVEntry(Object[] csv) throws BasicException {
        final SentenceExec m_insertCSVEntry = new StaticSentence(this.sessionDB,
                "INSERT INTO csvimport ( "
                + "ID, ROWNUMBER, CSVERROR, REFERENCE, "
                + "CODE, NAME, PRICEBUY, PRICESELL, "
                + "PREVIOUSBUY, PREVIOUSSELL, CATEGORY, TAX) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                new SerializerWriteBasic(new Datas[]{
            Datas.STRING, Datas.STRING, Datas.STRING, Datas.STRING,
            Datas.STRING, Datas.STRING, Datas.DOUBLE, Datas.DOUBLE,
            Datas.DOUBLE, Datas.DOUBLE, Datas.STRING, Datas.STRING
        }));
        m_insertCSVEntry.exec(csv);
    }

    @Override
    public final void execCustomerAddCSVEntry(CustomerCsvImportEntry entry) throws BasicException {
        final SentenceExec m_insertCustomerCSVEntry = new StaticSentence(this.sessionDB,
                "INSERT INTO csvimport ( "
                + "ID, ROWNUMBER, CSVERROR, SEARCHKEY, NAME) "
                + "VALUES (?, ?, ?, ?, ?)",
                new SerializerWriteBasic(new Datas[]{
            Datas.STRING, Datas.STRING, Datas.STRING, Datas.STRING, Datas.STRING
        }));
        m_insertCustomerCSVEntry.exec(new Object[]{
            entry.id(), entry.rowNumber(), entry.csvError(), entry.searchKey(), entry.name()});
    }

    @Override
    @Deprecated
    public final void execCustomerAddCSVEntry(Object[] csv) throws BasicException {
        execCustomerAddCSVEntry(new CustomerCsvImportEntry(
                (String) csv[0],
                (String) csv[1],
                (String) csv[2],
                (String) csv[3],
                (String) csv[4]));
    }

    public final String getProductRecordType(Object[] myProduct) throws BasicException {
        final SentenceFind m_getProductAllFields = new PreparedSentence(this.sessionDB,
                "SELECT ID FROM products WHERE REFERENCE=? AND CODE=? AND NAME=? ",
                new SerializerWriteBasic(new Datas[]{Datas.STRING, Datas.STRING, Datas.STRING}),
                new ProductIdRead()
        );

        final SentenceFind m_getProductRefAndCode = new PreparedSentence(this.sessionDB,
                "SELECT ID FROM products WHERE REFERENCE=? AND CODE=?",
                new SerializerWriteBasic(new Datas[]{Datas.STRING, Datas.STRING}),
                new ProductIdRead()
        );

        final SentenceFind m_getProductRefAndName = new PreparedSentence(this.sessionDB,
                "SELECT ID FROM products WHERE REFERENCE=? AND NAME=? ",
                new SerializerWriteBasic(new Datas[]{Datas.STRING, Datas.STRING}),
                new ProductIdRead()
        );

        final SentenceFind m_getProductCodeAndName = new PreparedSentence(this.sessionDB,
                "SELECT ID FROM products WHERE CODE=? AND NAME=? ",
                new SerializerWriteBasic(new Datas[]{Datas.STRING, Datas.STRING}),
                new ProductIdRead()
        );

        final SentenceFind m_getProductByReference = new PreparedSentence(this.sessionDB,
                "SELECT ID FROM products WHERE REFERENCE=? ",
                SerializerWriteString.INSTANCE,
                new ProductIdRead()
        );

        final SentenceFind m_getProductByCode = new PreparedSentence(this.sessionDB,
                "SELECT ID FROM products WHERE CODE=? ",
                SerializerWriteString.INSTANCE,
                new ProductIdRead()
        );

        final SentenceFind m_getProductByName = new PreparedSentence(this.sessionDB,
                "SELECT ID FROM products WHERE NAME=? ",
                SerializerWriteString.INSTANCE,
                new ProductIdRead()
        );

        if (m_getProductAllFields.find(myProduct) != null) {
            return m_getProductAllFields.find(myProduct).toString();
        }
        if (m_getProductRefAndCode.find(myProduct[0], myProduct[1]) != null) {
            return "Name change";
        }
        if (m_getProductRefAndName.find(myProduct[0], myProduct[2]) != null) {
            return "Barcode change";
        }
        if (m_getProductCodeAndName.find(myProduct[1], myProduct[2]) != null) {
            return "Reference change";
        }
        if (m_getProductByReference.find(myProduct[0]) != null) {
            return "Duplicate Reference found.";
        }
        if (m_getProductByCode.find(myProduct[1]) != null) {
            return "Duplicate Barcode found.";
        }
        if (m_getProductByName.find(myProduct[2]) != null) {
            return "Duplicate Description found.";
        }
        return "new";
    }

    public final String getCustomerRecordType(Object[] myCustomer) throws BasicException {
        final SerializerRead customerIdRead = (DataRead dr) -> (dr.getString(1));

        final SentenceFind m_getCustomerAllFields = new PreparedSentence(this.sessionDB,
                "SELECT ID FROM customers WHERE SEARCHKEY=? AND NAME=? ",
                new SerializerWriteBasic(new Datas[]{Datas.STRING, Datas.STRING}),
                customerIdRead
        );

        final SentenceFind m_getCustomerSearchKeyAndName = new PreparedSentence(this.sessionDB,
                "SELECT ID FROM customers WHERE SEARCHKEY=? AND NAME=? ",
                new SerializerWriteBasic(new Datas[]{Datas.STRING, Datas.STRING}),
                customerIdRead
        );

        final SentenceFind m_getCustomerBySearchKey = new PreparedSentence(this.sessionDB,
                "SELECT ID FROM customers WHERE SEARCHKEY=? ",
                SerializerWriteString.INSTANCE,
                customerIdRead
        );

        final SentenceFind m_getCustomerByName = new PreparedSentence(this.sessionDB,
                "SELECT ID FROM customers WHERE NAME=? ",
                SerializerWriteString.INSTANCE,
                customerIdRead
        );

        if (m_getCustomerAllFields.find(myCustomer) != null) {
            return m_getCustomerAllFields.find(myCustomer).toString();
        }
        if (m_getCustomerSearchKeyAndName.find(myCustomer[0], myCustomer[1]) != null) {
            return "reference error";
        }
        if (m_getCustomerBySearchKey.find(myCustomer[0]) != null) {
            return "Duplicate Search Key found.";
        }
        if (m_getCustomerByName.find(myCustomer[1]) != null) {
            return "Duplicate Name found.";
        }
        return "new";
    }

    private final static class ProductIdRead implements SerializerRead<String> {
        @Override
        public String readValues(DataRead dr) throws BasicException {
            return dr.getString(1);
        }
    };
}
