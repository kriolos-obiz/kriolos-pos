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
import com.openbravo.pos.ticket.CategoryInfo;
import com.openbravo.pos.ticket.ProductInfoExt;
import com.openbravo.pos.ticket.FindTicketsInfo;
import com.openbravo.pos.ticket.ProductInfo;
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
import com.openbravo.pos.catalog.CategoryStock;
import com.openbravo.pos.customers.CustomerInfoExt;
import com.openbravo.pos.customers.CustomerTransaction;
import com.openbravo.pos.forms.AppConfig;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.BeanFactoryDataSingle;
import com.openbravo.pos.forms.Payments;
import com.openbravo.pos.inventory.*;
import com.openbravo.pos.sales.restaurant.FloorsInfo;
import com.openbravo.pos.payment.PaymentInfo;
import com.openbravo.pos.payment.PaymentInfoTicket;
import com.openbravo.pos.sales.ReprintTicketInfo;
import com.openbravo.pos.suppliers.SupplierInfo;
import com.openbravo.pos.suppliers.SupplierInfoExt;
import com.openbravo.pos.ticket.ProductInfoExtA;
import com.openbravo.pos.voucher.VoucherInfo;
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

        protected Row productsRow;
        protected Row customersRow;

        private String pName;
        private Double getTotal;
        private Double getTendered;
        private String getRetMsg;
        private String getVoucher;

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

        public static final String SQL_WAREHOUSE_STOCK_LIST = "SELECT L.ID, P.ID, P.REFERENCE, P.NAME,"
                        + "L.STOCKSECURITY, L.STOCKMAXIMUM, COALESCE(S.SUMUNITS, 0) "
                        + "FROM products P "
                        + "LEFT OUTER JOIN (SELECT ID, PRODUCT, LOCATION, STOCKSECURITY, STOCKMAXIMUM "
                        + "FROM stocklevel WHERE LOCATION = ?) L ON P.ID = L.PRODUCT "
                        + "LEFT OUTER JOIN (SELECT PRODUCT, SUM(UNITS) AS SUMUNITS "
                        + "FROM stockcurrent WHERE LOCATION = ? GROUP BY PRODUCT) S ON P.ID = S.PRODUCT "
                        + "ORDER BY P.NAME";

        public static final String SQL_STOCKLEVEL_INSERT = "INSERT INTO stocklevel (ID, LOCATION, PRODUCT, STOCKSECURITY, STOCKMAXIMUM) VALUES (?, ?, ?, ?, ?)";

        public static final String SQL_STOCKLEVEL_UPDATE = "UPDATE stocklevel SET STOCKSECURITY = ?, STOCKMAXIMUM = ? WHERE ID = ?";

        private String getCardName;

        public DataLogicSales() {
                stockdiaryDatas = new Datas[] {
                                Datas.STRING, Datas.TIMESTAMP, Datas.INT, Datas.STRING,
                                Datas.STRING, Datas.STRING, Datas.DOUBLE, Datas.DOUBLE,
                                Datas.STRING, Datas.STRING, Datas.STRING };

                paymenttabledatas = new Datas[] {
                                Datas.STRING, Datas.STRING, Datas.TIMESTAMP,
                                Datas.STRING, Datas.STRING, Datas.DOUBLE,
                                Datas.STRING };

                stockdatas = new Datas[] {
                                Datas.STRING, Datas.STRING, Datas.STRING,
                                Datas.DOUBLE, Datas.DOUBLE, Datas.DOUBLE };

                stockAdjustDatas = new Datas[] {
                                Datas.STRING,
                                Datas.STRING,
                                Datas.STRING,
                                Datas.DOUBLE };

                auxiliarDatas = new Datas[] {
                                Datas.STRING, Datas.STRING, Datas.STRING,
                                Datas.STRING, Datas.STRING, Datas.STRING };

                productsRow = new Row(
                                new Field("ID", Datas.STRING, Formats.STRING),
                                new Field(AppLocal.getIntString("label.prodref"), Datas.STRING, Formats.STRING, true,
                                                true, true),
                                new Field(AppLocal.getIntString("label.prodbarcode"), Datas.STRING, Formats.STRING,
                                                false, true, true),
                                new Field(AppLocal.getIntString("label.prodbarcodetype"), Datas.STRING, Formats.STRING,
                                                false, true,
                                                true),
                                new Field(AppLocal.getIntString("label.prodname"), Datas.STRING, Formats.STRING, true,
                                                true, true),
                                new Field(AppLocal.getIntString("label.prodpricebuy"), Datas.DOUBLE, Formats.CURRENCY,
                                                false, true,
                                                true),
                                new Field(AppLocal.getIntString("label.prodpricesell"), Datas.DOUBLE, Formats.CURRENCY,
                                                false, true,
                                                true),
                                new Field(AppLocal.getIntString("label.prodcategory"), Datas.STRING, Formats.STRING,
                                                false, false,
                                                true),
                                new Field(AppLocal.getIntString("label.taxcategory"), Datas.STRING, Formats.STRING,
                                                false, false, true),
                                new Field(AppLocal.getIntString("label.attributeset"), Datas.STRING, Formats.STRING,
                                                false, false,
                                                true),
                                new Field("STOCKCOST", Datas.DOUBLE, Formats.CURRENCY),
                                new Field("STOCKVOLUME", Datas.DOUBLE, Formats.DOUBLE),
                                new Field("IMAGE", Datas.IMAGE, Formats.NULL),
                                new Field("ISCOM", Datas.BOOLEAN, Formats.BOOLEAN),
                                new Field("ISSCALE", Datas.BOOLEAN, Formats.BOOLEAN),
                                new Field("ISCONSTANT", Datas.BOOLEAN, Formats.BOOLEAN),
                                new Field("PRINTKB", Datas.BOOLEAN, Formats.BOOLEAN),
                                new Field("SENDSTATUS", Datas.BOOLEAN, Formats.BOOLEAN),
                                new Field("ISSERVICE", Datas.BOOLEAN, Formats.BOOLEAN),
                                new Field("PROPERTIES", Datas.BYTES, Formats.NULL),
                                new Field(AppLocal.getIntString("label.display"), Datas.STRING, Formats.STRING, false,
                                                true, true),
                                new Field("ISVPRICE", Datas.BOOLEAN, Formats.BOOLEAN),
                                new Field("ISVERPATRIB", Datas.BOOLEAN, Formats.BOOLEAN),
                                new Field("TEXTTIP", Datas.STRING, Formats.STRING),
                                new Field("WARRANTY", Datas.BOOLEAN, Formats.BOOLEAN),
                                new Field(AppLocal.getIntString("label.stockunits"), Datas.DOUBLE, Formats.DOUBLE),
                                new Field("PRINTTO", Datas.STRING, Formats.STRING),
                                new Field(AppLocal.getIntString("label.prodsupplier"), Datas.STRING, Formats.STRING,
                                                false, false,
                                                true),
                                new Field(AppLocal.getIntString("label.UOM"), Datas.STRING, Formats.STRING),
                                new Field("MEMODATE", Datas.TIMESTAMP, Formats.DATE),
                                new Field("ISCATALOG", Datas.BOOLEAN, Formats.BOOLEAN),
                                new Field("CATORDER", Datas.INT, Formats.INT));

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

        // Import Creates
        public final void createCategory(Object[] category) throws BasicException {
                SentenceExec m_createCat = new StaticSentence(this.sessionDB,
                                "INSERT INTO categories ( ID, NAME, CATSHOWNAME ) "
                                                + "VALUES (?, ?, ?)",
                                new SerializerWriteBasic(new Datas[] { Datas.STRING, Datas.STRING, Datas.BOOLEAN }));
                m_createCat.exec(category);
        }

        public final void createSupplier(Object[] supplier) throws BasicException {
                SentenceExec m_createSupp = new StaticSentence(this.sessionDB,
                                "INSERT INTO suppliers ( ID, NAME, SEARCHKEY, VISIBLE ) "
                                                + "VALUES (?, ?, ?, ?)",
                                new SerializerWriteBasic(new Datas[] {
                                                Datas.STRING,
                                                Datas.STRING,
                                                Datas.STRING,
                                                Datas.BOOLEAN }));
                m_createSupp.exec(supplier);
        }
        // End Import Creates

        public final Row getProductsRow() {
                return productsRow;
        }

        public final Row getCustomersRow() {
                return customersRow;
        }

        /**
         *
         * @param id
         * @return
         * @throws BasicException
         */
        public final ProductInfoExt getProductInfo(String id) throws BasicException {
                return new PreparedSentence<String, ProductInfoExt>(sessionDB,
                                "SELECT "
                                                + "ID, "
                                                + "REFERENCE, "
                                                + "CODE, "
                                                + "CODETYPE, "
                                                + "NAME, "
                                                + "PRICEBUY, "
                                                + "PRICESELL, "
                                                + "CATEGORY, "
                                                + "TAXCAT, "
                                                + "ATTRIBUTESET_ID, "
                                                + "STOCKCOST, "
                                                + "STOCKVOLUME, "
                                                + "IMAGE, "
                                                + "ISCOM, "
                                                + "ISSCALE, "
                                                + "ISCONSTANT, "
                                                + "PRINTKB, "
                                                + "SENDSTATUS, "
                                                + "ISSERVICE, "
                                                + "ATTRIBUTES, "
                                                + "DISPLAY, "
                                                + "ISVPRICE, "
                                                + "ISVERPATRIB, "
                                                + "TEXTTIP, "
                                                + "WARRANTY, "
                                                + "STOCKUNITS, "
                                                + "PRINTTO, "
                                                + "SUPPLIER, "
                                                + "UOM, "
                                                + "MEMODATE "
                                                + "FROM products WHERE ID = ?",
                                SerializerWriteString.INSTANCE,
                                ProductInfoExt.getSerializerRead()).find(id);
        }

        /**
         *
         * @param sCode (Example BarCode)
         * @return
         * @throws BasicException
         */
        public final ProductInfoExt getProductInfoByCode(String sCode) throws BasicException {
                return new PreparedSentence<String, ProductInfoExt>(sessionDB,
                                "SELECT "
                                                + "ID, "
                                                + "REFERENCE, "
                                                + "CODE, "
                                                + "CODETYPE, "
                                                + "NAME, "
                                                + "PRICEBUY, "
                                                + "PRICESELL, "
                                                + "CATEGORY, "
                                                + "TAXCAT, "
                                                + "ATTRIBUTESET_ID, "
                                                + "STOCKCOST, "
                                                + "STOCKVOLUME, "
                                                + "IMAGE, "
                                                + "ISCOM, "
                                                + "ISSCALE, "
                                                + "ISCONSTANT, "
                                                + "PRINTKB, "
                                                + "SENDSTATUS, "
                                                + "ISSERVICE, "
                                                + "ATTRIBUTES, "
                                                + "DISPLAY, "
                                                + "ISVPRICE, "
                                                + "ISVERPATRIB, "
                                                + "TEXTTIP, "
                                                + "WARRANTY, "
                                                + "STOCKUNITS, "
                                                + "PRINTTO, "
                                                + "SUPPLIER, "
                                                + "UOM, "
                                                + "MEMODATE "
                                                + "FROM products WHERE CODE = ?",
                                SerializerWriteString.INSTANCE,
                                ProductInfoExt.getSerializerRead()).find(sCode);
        }

        /**
         *
         * @param sCode (short code, Example: Barcode)
         * @return
         * @throws BasicException
         */
        public final ProductInfoExt getProductInfoByShortCode(String sCode) throws BasicException {

                return new PreparedSentence<String, ProductInfoExt>(sessionDB,
                                "SELECT "
                                                + "ID, "
                                                + "REFERENCE, "
                                                + "CODE, "
                                                + "CODETYPE, "
                                                + "NAME, "
                                                + "PRICEBUY, "
                                                + "PRICESELL, "
                                                + "CATEGORY, "
                                                + "TAXCAT, "
                                                + "ATTRIBUTESET_ID, "
                                                + "STOCKCOST, "
                                                + "STOCKVOLUME, "
                                                + "IMAGE, "
                                                + "ISCOM, "
                                                + "ISSCALE, "
                                                + "ISCONSTANT, "
                                                + "PRINTKB, "
                                                + "SENDSTATUS, "
                                                + "ISSERVICE, "
                                                + "ATTRIBUTES, "
                                                + "DISPLAY, "
                                                + "ISVPRICE, "
                                                + "ISVERPATRIB, "
                                                + "TEXTTIP, "
                                                + "WARRANTY, "
                                                + "STOCKUNITS, "
                                                + "PRINTTO, "
                                                + "SUPPLIER, "
                                                + "UOM, "
                                                + "MEMODATE "
                                                + "FROM products "
                                                + "WHERE SUBSTRING( CODE, 3, 6 ) = ?",
                                SerializerWriteString.INSTANCE,
                                ProductInfoExt.getSerializerRead()).find(sCode.substring(2, 8));
        }

        /**
         * Important Note: Deliberately extracted from other code to force strict
         * UPC-A (full 12 digits) Why? Because other manf' or in-store codes may
         * exist and we just need a single record returned. Also, handling things
         * this way will allow use (future) of a COUPON code (5 or 9 normally used)
         * in-store
         *
         */
        public final ProductInfoExt getProductInfoByUShortCode(String sCode) throws BasicException {

                /*
                 * selection of 7 digits ie: 2123456
                 * specific to allow for other 12 digit codes that may be in use at positions
                 * 234567
                 * last digit (position 7) can be used to identify COUPON (5 or 9) - FUTURE
                 */
                return new PreparedSentence<String, ProductInfoExt>(sessionDB,
                                "SELECT "
                                                + "ID, "
                                                + "REFERENCE, "
                                                + "CODE, "
                                                + "CODETYPE, "
                                                + "NAME, "
                                                + "PRICEBUY, "
                                                + "PRICESELL, "
                                                + "CATEGORY, "
                                                + "TAXCAT, "
                                                + "ATTRIBUTESET_ID, "
                                                + "STOCKCOST, "
                                                + "STOCKVOLUME, "
                                                + "IMAGE, "
                                                + "ISCOM, "
                                                + "ISSCALE, "
                                                + "ISCONSTANT, "
                                                + "PRINTKB, "
                                                + "SENDSTATUS, "
                                                + "ISSERVICE, "
                                                + "ATTRIBUTES, "
                                                + "DISPLAY, "
                                                + "ISVPRICE, "
                                                + "ISVERPATRIB, "
                                                + "TEXTTIP, "
                                                + "WARRANTY, "
                                                + "STOCKUNITS, "
                                                + "PRINTTO, "
                                                + "SUPPLIER, "
                                                + "UOM, "
                                                + "MEMODATE "
                                                + "FROM products "
                                                + "WHERE LEFT( CODE, 7 ) = ? AND CODETYPE = 'UPC-A' ",
                                SerializerWriteString.INSTANCE,
                                ProductInfoExt.getSerializerRead()).find(sCode.substring(0, 7));
        }

        /**
         *
         * @param sReference
         * @return
         * @throws BasicException
         */
        public final ProductInfoExt getProductInfoByReference(String sReference) throws BasicException {
                return new PreparedSentence<String, ProductInfoExt>(sessionDB,
                                "SELECT "
                                                + "ID, "
                                                + "REFERENCE, "
                                                + "CODE, "
                                                + "CODETYPE, "
                                                + "NAME, "
                                                + "PRICEBUY, "
                                                + "PRICESELL, "
                                                + "CATEGORY, "
                                                + "TAXCAT, "
                                                + "ATTRIBUTESET_ID, "
                                                + "STOCKCOST, "
                                                + "STOCKVOLUME, "
                                                + "IMAGE, "
                                                + "ISCOM, "
                                                + "ISSCALE, "
                                                + "ISCONSTANT, "
                                                + "PRINTKB, "
                                                + "SENDSTATUS, "
                                                + "ISSERVICE, "
                                                + "ATTRIBUTES, "
                                                + "DISPLAY, "
                                                + "ISVPRICE, "
                                                + "ISVERPATRIB, "
                                                + "TEXTTIP, "
                                                + "WARRANTY, "
                                                + "STOCKUNITS, "
                                                + "PRINTTO, "
                                                + "SUPPLIER, "
                                                + "UOM, "
                                                + "MEMODATE "
                                                + "FROM products WHERE REFERENCE = ?",
                                SerializerWriteString.INSTANCE,
                                ProductInfoExt.getSerializerRead()).find(sReference);
        }

        /**
         *
         * @return @throws BasicException
         */
        public final List<CategoryInfo> getRootCategories() throws BasicException {
                return new PreparedSentence<Void, CategoryInfo>(sessionDB,
                                "SELECT "
                                                + "ID, "
                                                + "NAME, "
                                                + "IMAGE, "
                                                + "TEXTTIP, "
                                                + "CATSHOWNAME, "
                                                + "CATORDER, "
                                                + "CATALOGCOLOR, "
                                                + "CATALOGENABLED "
                                                + "FROM categories "
                                                + "WHERE PARENTID IS NULL AND CATSHOWNAME = " + sessionDB.DB.TRUE()
                                                + " "
                                                + "ORDER BY CATORDER, NAME",
                                null,
                                CategoryInfo.getSerializerRead()).list();
        }

        /**
         *
         * @param category ID
         * @return
         * @throws BasicException
         */
        public final List<CategoryInfo> getSubcategories(String category) throws BasicException {
                return new PreparedSentence<String, CategoryInfo>(sessionDB,
                                "SELECT "
                                                + "ID, "
                                                + "NAME, "
                                                + "IMAGE, "
                                                + "TEXTTIP, "
                                                + "CATSHOWNAME, "
                                                + "CATORDER, "
                                                + "CATALOGCOLOR, "
                                                + "CATALOGENABLED "
                                                + "FROM categories "
                                                + "WHERE PARENTID = ? "
                                                + "ORDER BY CATORDER, NAME",
                                SerializerWriteString.INSTANCE,
                                CategoryInfo.getSerializerRead()).list(category);
        }

        /**
         *
         * @param category ID
         * @return
         * @throws BasicException
         */
        public List<ProductInfoExt> getProductCatalog(String category) throws BasicException {
                return new PreparedSentence<String, ProductInfoExt>(sessionDB,
                                "SELECT "
                                                + "P.ID, "
                                                + "P.REFERENCE, "
                                                + "P.CODE, "
                                                + "P.CODETYPE, "
                                                + "P.NAME, "
                                                + "P.PRICEBUY, "
                                                + "P.PRICESELL, "
                                                + "P.CATEGORY, "
                                                + "P.TAXCAT, "
                                                + "P.ATTRIBUTESET_ID, "
                                                + "P.STOCKCOST, "
                                                + "P.STOCKVOLUME, "
                                                + "P.IMAGE, "
                                                + "P.ISCOM, "
                                                + "P.ISSCALE, "
                                                + "P.ISCONSTANT, "
                                                + "P.PRINTKB, "
                                                + "P.SENDSTATUS, "
                                                + "P.ISSERVICE, "
                                                + "P.ATTRIBUTES, "
                                                + "P.DISPLAY, "
                                                + "P.ISVPRICE, "
                                                + "P.ISVERPATRIB, "
                                                + "P.TEXTTIP, "
                                                + "P.WARRANTY, "
                                                + "P.STOCKUNITS, "
                                                + "P.PRINTTO, "
                                                + "P.SUPPLIER, "
                                                + "P.UOM, "
                                                + "P.MEMODATE "
                                                + "FROM products P, products_cat O "
                                                + "WHERE P.ID = O.PRODUCT AND P.CATEGORY = ? "
                                                + "ORDER BY O.CATORDER, P.NAME ",
                                SerializerWriteString.INSTANCE,
                                ProductInfoExt.getSerializerRead()).list(category);
        }

        /**
         *
         * @param id Product ID
         * @return List of ProductInfoExt
         * @throws BasicException
         */
        public List<ProductInfoExt> getProductComposite(String id) throws BasicException {
                return new PreparedSentence<String, ProductInfoExt>(sessionDB,
                                "SELECT "
                                                + "P.ID, "
                                                + "P.REFERENCE, "
                                                + "P.CODE, "
                                                + "P.CODETYPE, "
                                                + "P.NAME, "
                                                + "P.PRICEBUY, "
                                                + "P.PRICESELL, "
                                                + "P.CATEGORY, "
                                                + "P.TAXCAT, "
                                                + "P.ATTRIBUTESET_ID, "
                                                + "P.STOCKCOST, "
                                                + "P.STOCKVOLUME, "
                                                + "P.IMAGE, "
                                                + "P.ISCOM, "
                                                + "P.ISSCALE, "
                                                + "P.ISCONSTANT, "
                                                + "P.PRINTKB, "
                                                + "P.SENDSTATUS, "
                                                + "P.ISSERVICE, "
                                                + "P.ATTRIBUTES, "
                                                + "P.DISPLAY, "
                                                + "P.ISVPRICE, "
                                                + "P.ISVERPATRIB, "
                                                + "P.TEXTTIP, "
                                                + "P.WARRANTY, "
                                                + "P.STOCKUNITS, "
                                                + "P.PRINTTO, "
                                                + "P.SUPPLIER, "
                                                + "P.UOM, "
                                                + "P.MEMODATE "
                                                + "FROM products P, "
                                                + "products_cat O, products_com M "
                                                + "WHERE P.ID = O.PRODUCT AND P.ID = M.PRODUCT2 AND M.PRODUCT = ? "
                                                + "AND P.ISCOM = " + sessionDB.DB.TRUE() + " "
                                                + "ORDER BY O.CATORDER, P.NAME",
                                SerializerWriteString.INSTANCE,
                                ProductInfoExt.getSerializerRead()).list(id);
        }

        /**
         *
         * @return @throws BasicException
         */
        public List<ProductInfoExt> getProductConstant() throws BasicException {
                return new PreparedSentence<Void, ProductInfoExt>(sessionDB,
                                "SELECT "
                                                + "products.ID, "
                                                + "products.REFERENCE, "
                                                + "products.CODE, "
                                                + "products.CODETYPE, "
                                                + "products.NAME, "
                                                + "products.PRICEBUY, "
                                                + "products.PRICESELL, "
                                                + "products.CATEGORY, "
                                                + "products.TAXCAT, "
                                                + "products.ATTRIBUTESET_ID, "
                                                + "products.STOCKCOST, "
                                                + "products.STOCKVOLUME, "
                                                + "products.IMAGE, "
                                                + "products.ISCOM, "
                                                + "products.ISSCALE, "
                                                + "products.ISCONSTANT, "
                                                + "products.PRINTKB, "
                                                + "products.SENDSTATUS, "
                                                + "products.ISSERVICE, "
                                                + "products.ATTRIBUTES, "
                                                + "products.DISPLAY, "
                                                + "products.ISVPRICE, "
                                                + "products.ISVERPATRIB, "
                                                + "products.TEXTTIP, "
                                                + "products.WARRANTY, "
                                                + "products.STOCKUNITS, "
                                                + "products.PRINTTO, "
                                                + "products.SUPPLIER, "
                                                + "products.UOM, "
                                                + "products.MEMODATE "
                                                + "FROM categories INNER JOIN products ON (products.CATEGORY = categories.ID) "
                                                + "WHERE products.ISCONSTANT = " + sessionDB.DB.TRUE() + " "
                                                + "ORDER BY categories.NAME, products.NAME",
                                null,
                                ProductInfoExt.getSerializerRead()).list();

        }

        /**
         *
         * @param id
         * @return
         * @throws BasicException
         */
        public final CategoryInfo getCategoryInfo(String id) throws BasicException {
                return new PreparedSentence<String, CategoryInfo>(sessionDB,
                                "SELECT "
                                                + "ID, "
                                                + "NAME, "
                                                + "IMAGE, "
                                                + "TEXTTIP, "
                                                + "CATSHOWNAME, "
                                                + "CATORDER, "
                                                + "CATALOGCOLOR, "
                                                + "CATALOGENABLED "
                                                + "FROM categories "
                                                + "WHERE ID = ? "
                                                + "ORDER BY CATORDER, NAME",
                                SerializerWriteString.INSTANCE,
                                CategoryInfo.getSerializerRead()).find(id);
        }

        /**
         * JG Dec 2017
         *
         * @param pId
         * @return
         * @throws BasicException
         */
        public final List<CategoryStock> getCategorysProductList(String pId) throws BasicException {
                return new PreparedSentence<String, CategoryStock>(sessionDB,
                                "SELECT products.ID, "
                                                + "products.NAME AS Name, "
                                                + "products.CODE AS Barcode, "
                                                + "categories.ID AS Category "
                                                + "FROM products products "
                                                + "INNER JOIN categories categories ON (products.CATEGORY = categories.ID) "
                                                + "WHERE products.category = ? "
                                                + "ORDER BY products.NAME ASC",
                                SerializerWriteString.INSTANCE,
                                CategoryStock.getSerializerRead()).list(pId);
        }

        /**
         *
         * @return
         */
        public final SentenceList<ProductInfoExt> getProductList() {
                return new StaticSentence(sessionDB,
                                new QBFBuilder(
                                                "SELECT "
                                                                + "ID, "
                                                                + "REFERENCE, "
                                                                + "CODE, "
                                                                + "CODETYPE, "
                                                                + "NAME, "
                                                                + "PRICEBUY, "
                                                                + "PRICESELL, "
                                                                + "CATEGORY, "
                                                                + "TAXCAT, "
                                                                + "ATTRIBUTESET_ID, "
                                                                + "STOCKCOST, "
                                                                + "STOCKVOLUME, "
                                                                + "IMAGE, "
                                                                + "ISCOM, "
                                                                + "ISSCALE, "
                                                                + "ISCONSTANT, "
                                                                + "PRINTKB, "
                                                                + "SENDSTATUS, "
                                                                + "ISSERVICE, "
                                                                + "ATTRIBUTES, "
                                                                + "DISPLAY, "
                                                                + "ISVPRICE, "
                                                                + "ISVERPATRIB, "
                                                                + "TEXTTIP, "
                                                                + "WARRANTY, "
                                                                + "STOCKUNITS, "
                                                                + "PRINTTO, "
                                                                + "SUPPLIER, "
                                                                + "UOM, "
                                                                + "MEMODATE "
                                                                + "FROM products "
                                                                + "WHERE ?(QBF_FILTER) "
                                                                + "ORDER BY REFERENCE",
                                                new String[] { "NAME", "PRICEBUY", "PRICESELL", "CATEGORY", "CODE" }),
                                new SerializerWriteBasic(new Datas[] {
                                                Datas.OBJECT, Datas.STRING,
                                                Datas.OBJECT, Datas.DOUBLE,
                                                Datas.OBJECT, Datas.DOUBLE,
                                                Datas.OBJECT, Datas.STRING,
                                                Datas.OBJECT, Datas.STRING }),
                                ProductInfoExt.getSerializerRead());
        }

        /**
         *
         * @return
         */
        public SentenceList<ProductInfoExt> getProductListNormal() {
                return new StaticSentence(sessionDB,
                                new QBFBuilder(
                                                "SELECT "
                                                                + "ID, "
                                                                + "REFERENCE, "
                                                                + "CODE, "
                                                                + "CODETYPE, "
                                                                + "NAME, "
                                                                + "PRICEBUY, "
                                                                + "PRICESELL, "
                                                                + "CATEGORY, "
                                                                + "TAXCAT, "
                                                                + "ATTRIBUTESET_ID, "
                                                                + "STOCKCOST, "
                                                                + "STOCKVOLUME, "
                                                                + "IMAGE, "
                                                                + "ISCOM, "
                                                                + "ISSCALE, "
                                                                + "ISCONSTANT, "
                                                                + "PRINTKB, "
                                                                + "SENDSTATUS, "
                                                                + "ISSERVICE, "
                                                                + "ATTRIBUTES, "
                                                                + "DISPLAY, "
                                                                + "ISVPRICE, "
                                                                + "ISVERPATRIB, "
                                                                + "TEXTTIP, "
                                                                + "WARRANTY, "
                                                                + "STOCKUNITS, "
                                                                + "PRINTTO, "
                                                                + "SUPPLIER, "
                                                                + "UOM, "
                                                                + "MEMODATE "
                                                                + "FROM products "
                                                                + "WHERE ISCOM = " + sessionDB.DB.FALSE()
                                                                + " AND ?(QBF_FILTER) ORDER BY REFERENCE",
                                                new String[] { "NAME", "PRICEBUY", "PRICESELL", "CATEGORY", "CODE" }),
                                new SerializerWriteBasic(new Datas[] {
                                                Datas.OBJECT, Datas.STRING,
                                                Datas.OBJECT, Datas.DOUBLE,
                                                Datas.OBJECT, Datas.DOUBLE,
                                                Datas.OBJECT, Datas.STRING,
                                                Datas.OBJECT, Datas.STRING }),
                                ProductInfoExt.getSerializerRead());
        }

        /**
         *
         * @return
         */
        public SentenceList<ProductInfo> getProductsList() {
                return new StaticSentence(sessionDB,
                                "SELECT "
                                                + "ID, "
                                                + "REFERENCE, "
                                                + "CODE, "
                                                + "CODETYPE, "
                                                + "NAME, "
                                                + "PRICEBUY, "
                                                + "PRICESELL, "
                                                + "CATEGORY, "
                                                + "TAXCAT, "
                                                + "ATTRIBUTESET_ID, "
                                                + "STOCKCOST, "
                                                + "STOCKVOLUME, "
                                                + "IMAGE, "
                                                + "ISCOM, "
                                                + "ISSCALE, "
                                                + "ISCONSTANT, "
                                                + "PRINTKB, "
                                                + "SENDSTATUS, "
                                                + "ISSERVICE, "
                                                + "ATTRIBUTES, "
                                                + "DISPLAY, "
                                                + "ISVPRICE, "
                                                + "ISVERPATRIB, "
                                                + "TEXTTIP, "
                                                + "WARRANTY, "
                                                + "STOCKUNITS, "
                                                + "PRINTTO, "
                                                + "SUPPLIER, "
                                                + "UOM, "
                                                + "MEMODATE "
                                                + "FROM products "
                                                + "ORDER BY NAME",
                                ProductInfo.getSerializerRead());
        }

        public SentenceList<ProductInfoExtA> getProductList2() {
                return new StaticSentence(sessionDB,
                                new QBFBuilder(
                                                "SELECT "
                                                                + "products.id, "
                                                                + "products.name, "
                                                                + "stockcurrent.units, "
                                                                + "locations.name, "
                                                                + "products.pricesell, "
                                                                + "taxes.rate, "
                                                                + "products.pricesell + (products.pricesell * taxes.rate) AS SellIncTax "
                                                                + "products.category"
                                                                + "products.ISCOM"
                                                                + "products.ISSCALE"
                                                                + "products.ISCONSTANT"
                                                                + "products.ISSERVICE"
                                                                + " FROM (((stockcurrent stockcurrent "
                                                                + "INNER JOIN locations locations "
                                                                + "ON (stockcurrent.location = locations.id)) "
                                                                + "INNER JOIN products products "
                                                                + "ON (stockcurrent.product = products.id)) "
                                                                + "INNER JOIN taxcategories taxcategories "
                                                                + "ON (products.taxcat = taxcategories.id)) "
                                                                + "INNER JOIN taxes taxes "
                                                                + "ON (taxes.category = taxcategories.id) "
                                                                + "WHERE ?(QBF_FILTER) "
                                                                + "GROUP BY products.name ",
                                                new String[] { "NAME", "UNITS", "SellIncTax", "LOCATION", }),
                                new SerializerWriteBasic(new Datas[] {
                                                Datas.OBJECT, Datas.STRING,
                                                Datas.OBJECT, Datas.DOUBLE,
                                                Datas.OBJECT, Datas.DOUBLE,
                                                Datas.OBJECT, Datas.STRING }),
                                ProductInfoExtA.getSerializerRead());
        }

        /**
         *
         * @return
         */
        public SentenceList<ProductInfoExt> getProductListAuxiliar() {
                return new StaticSentence(sessionDB,
                                new QBFBuilder(
                                                "SELECT "
                                                                + "ID, "
                                                                + "REFERENCE, "
                                                                + "CODE, "
                                                                + "CODETYPE, "
                                                                + "NAME, "
                                                                + "PRICEBUY, "
                                                                + "PRICESELL, "
                                                                + "CATEGORY, "
                                                                + "TAXCAT, "
                                                                + "ATTRIBUTESET_ID, "
                                                                + "STOCKCOST, "
                                                                + "STOCKVOLUME, "
                                                                + "IMAGE, "
                                                                + "ISCOM, "
                                                                + "ISSCALE, "
                                                                + "ISCONSTANT, "
                                                                + "PRINTKB, "
                                                                + "SENDSTATUS, "
                                                                + "ISSERVICE, "
                                                                + "ATTRIBUTES, "
                                                                + "DISPLAY, "
                                                                + "ISVPRICE, "
                                                                + "ISVERPATRIB, "
                                                                + "TEXTTIP, "
                                                                + "WARRANTY, "
                                                                + "STOCKUNITS, "
                                                                + "PRINTTO, "
                                                                + "SUPPLIER, "
                                                                + "UOM, "
                                                                + "MEMODATE "
                                                                + "FROM products "
                                                                + "WHERE ISCOM = " + sessionDB.DB.TRUE()
                                                                + " AND ?(QBF_FILTER) "
                                                                + "ORDER BY REFERENCE",
                                                new String[] { "NAME", "PRICEBUY", "PRICESELL", "CATEGORY", "CODE" }),
                                new SerializerWriteBasic(new Datas[] {
                                                Datas.OBJECT, Datas.STRING,
                                                Datas.OBJECT, Datas.DOUBLE,
                                                Datas.OBJECT, Datas.DOUBLE,
                                                Datas.OBJECT, Datas.STRING,
                                                Datas.OBJECT, Datas.STRING }),
                                ProductInfoExt.getSerializerRead());
        }

        /**
         *
         * @param productId The product id to look for bundle
         * @return List of products part of the searched product
         * @throws BasicException
         */
        public final List<ProductsBundleInfo> getProductsBundle(String productId) throws BasicException {
                return new PreparedSentence(sessionDB,
                                "SELECT "
                                                + "ID, "
                                                + "PRODUCT, "
                                                + "PRODUCT_BUNDLE, "
                                                + "QUANTITY "
                                                + "FROM products_bundle WHERE PRODUCT = ?",
                                SerializerWriteString.INSTANCE,
                                ProductsBundleInfo.getSerializerRead()).list(productId);
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
        @SuppressWarnings("unchecked")
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
        @SuppressWarnings("unchecked")
        public final List<ReprintTicketInfo> getReprintTicketList() throws BasicException {
                return (List<ReprintTicketInfo>) new StaticSentence(sessionDB,
                                "SELECT "
                                                + "T.TICKETID, "
                                                + "T.TICKETTYPE, "
                                                + "R.DATENEW, "
                                                + "P.NAME, "
                                                + "C.NAME, "
                                                + "SUM(PM.TOTAL), "
                                                + "T.STATUS "
                                                + "FROM receipts "
                                                + "R JOIN tickets T ON R.ID = T.ID LEFT OUTER JOIN payments PM "
                                                + "ON R.ID = PM.RECEIPT LEFT OUTER JOIN customers C "
                                                + "ON C.ID = T.CUSTOMER LEFT OUTER JOIN people P ON T.PERSON = P.ID "
                                                + "GROUP BY "
                                                + "T.ID, "
                                                + "T.TICKETID, "
                                                + "T.TICKETTYPE, "
                                                + "R.DATENEW, "
                                                + "P.NAME, "
                                                + "C.NAME "
                                                + "ORDER BY R.DATENEW DESC, T.TICKETID "
                                                + "LIMIT 10 ",
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
                                        new SerializerReadBasic(new Datas[] { Datas.SERIALIZABLE })).find(Id);
                        return ticketInfoObjArray == null ? null : (TicketInfo) ticketInfoObjArray[0];
                }
        }

        // Tickets and Receipt list
        public SentenceList<FindTicketsInfo> getTicketsList() {
                return new StaticSentence(sessionDB,
                                new QBFBuilder(
                                                "SELECT "
                                                                + "T.TICKETID, "
                                                                + "T.TICKETTYPE, "
                                                                + "R.DATENEW, "
                                                                + "P.NAME, "
                                                                + "C.NAME, "
                                                                + "SUM(PM.TOTAL), "
                                                                + "T.STATUS "
                                                                + "FROM receipts "
                                                                + "R JOIN tickets T ON R.ID = T.ID LEFT OUTER JOIN payments PM "
                                                                + "ON R.ID = PM.RECEIPT LEFT OUTER JOIN customers C "
                                                                + "ON C.ID = T.CUSTOMER LEFT OUTER JOIN people P ON T.PERSON = P.ID "
                                                                + "WHERE ?(QBF_FILTER) "
                                                                + "GROUP BY "
                                                                + "T.ID, "
                                                                + "T.TICKETID, "
                                                                + "T.TICKETTYPE, "
                                                                + "R.DATENEW, "
                                                                + "P.NAME, "
                                                                + "C.NAME "
                                                                + "ORDER BY R.DATENEW DESC, T.TICKETID",
                                                new String[] {
                                                                "T.TICKETID", "T.TICKETTYPE", "PM.TOTAL", "R.DATENEW",
                                                                "R.DATENEW", "P.NAME",
                                                                "C.NAME" }),
                                new SerializerWriteBasic(new Datas[] {
                                                Datas.OBJECT, Datas.INT,
                                                Datas.OBJECT, Datas.INT,
                                                Datas.OBJECT, Datas.DOUBLE,
                                                Datas.OBJECT, Datas.TIMESTAMP,
                                                Datas.OBJECT, Datas.TIMESTAMP,
                                                Datas.OBJECT, Datas.STRING,
                                                Datas.OBJECT, Datas.STRING }),
                                new SerializerReadClass(FindTicketsInfo.class));
        }

        // User list
        /**
         *
         * @return
         */
        public final SentenceList<TaxCategoryInfo> getUserList() {
                return new StaticSentence(sessionDB,
                                "SELECT "
                                                + "ID, "
                                                + "NAME "
                                                + "FROM people "
                                                + "ORDER BY NAME",
                                null,
                                (DataRead dr) -> new TaxCategoryInfo(
                                                dr.getString(1),
                                                dr.getString(2)));
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
                } catch (BasicException ex) {
                        LOGGER.log(Level.WARNING, "Cannot get Tax list", ex);
                }
                return list;
        }

        /**
         * @deprecated since Nov/2025
         * @return
         */
        public final SentenceList<CategoryInfo> getCategoriesList() {
                return new StaticSentence(sessionDB,
                                "SELECT "
                                                + "ID, "
                                                + "NAME, "
                                                + "IMAGE, "
                                                + "TEXTTIP, "
                                                + "CATSHOWNAME, "
                                                + "CATORDER, "
                                                + "CATALOGCOLOR, "
                                                + "CATALOGENABLED "
                                                + "FROM categories "
                                                + "ORDER BY NAME",
                                null,
                                CategoryInfo.getSerializerRead());
        }

        public final List<CategoryInfo> getCategoriesListAll() {
                List<CategoryInfo> list = null;
                try {
                        list = this.getCategoriesList().list();
                } catch (BasicException ex) {
                        LOGGER.log(Level.WARNING, "Cannot get categories list", ex);
                }
                return list;
        }

        /**
         * JG Feb 2017 Returns all PARENT categories
         *
         * @return
         */
        public final SentenceList<CategoryInfo> getCategoriesList_1() {
                return new StaticSentence(sessionDB,
                                "SELECT "
                                                + "ID, "
                                                + "NAME, "
                                                + "IMAGE, "
                                                + "TEXTTIP, "
                                                + "CATSHOWNAME, "
                                                + "CATORDER, "
                                                + "CATALOGCOLOR, "
                                                + "CATALOGENABLED "
                                                + "FROM categories "
                                                + "WHERE PARENTID IS NULL "
                                                + "ORDER BY NAME",
                                null,
                                CategoryInfo.getSerializerRead());
        }

        /**
         *
         * @return
         */
        public final SentenceList<SupplierInfo> getSuppList() {
                return new StaticSentence(sessionDB,
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
         *
         * @param id
         * @return
         * @throws BasicException
         */
        public final CustomerInfoExt getCustomerInfo(String id) throws BasicException {
                return new PreparedSentence<String, CustomerInfoExt>(sessionDB,
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
                                new CustomerExtRead()).find(id);
        }

        /**
         * JG Apr 2017 - Revised to return Customer Id - cId param
         *
         * @param cId
         * @return
         * @throws BasicException
         */
        @SuppressWarnings("unchecked")
        public final List<CustomerTransaction> getCustomersTransactionList(String cId) throws BasicException {

                // TODO: TICKETLINE MUST STORE: _tax_value, _line_amount(Qty x price)
                // _line_total (Price x Qty x Tax), line_prod_name
                return new PreparedSentence<>(sessionDB,
                                "SELECT tickets.TICKETID, "
                                                + "products.NAME AS PNAME, "
                                                + "SUM(ticketlines.UNITS) AS UNITS, "
                                                + "SUM(ticketlines.UNITS * ticketlines.PRICE) AS AMOUNT, "
                                                + "SUM(ticketlines.UNITS * ticketlines.PRICE * (1.0 + taxes.RATE)) AS TOTAL, " // TODO:
                                                                                                                               // CALCULATION
                                                                                                                               // MUST
                                                                                                                               // NOT
                                                                                                                               // BE
                                                                                                                               // DONE
                                                                                                                               // ON
                                                                                                                               // DB
                                                                                                                               // (Use
                                                                                                                               // Java
                                                                                                                               // BigDecimal)
                                                + "receipts.DATENEW, "
                                                + "customers.ID AS CID "
                                                + "FROM ((((ticketlines ticketlines "
                                                + "CROSS JOIN taxes taxes ON (ticketlines.TAXID = taxes.ID)) "
                                                + "INNER JOIN tickets tickets ON (tickets.ID = ticketlines.TICKET)) "
                                                + "INNER JOIN customers customers ON (customers.ID = tickets.CUSTOMER)) "
                                                + "INNER JOIN receipts receipts ON (tickets.ID = receipts.ID)) "
                                                + "LEFT OUTER JOIN products products ON (ticketlines.PRODUCT = products.ID) "
                                                + "WHERE tickets.CUSTOMER = ? "
                                                + "GROUP BY customers.ID, receipts.DATENEW, tickets.TICKETID, "
                                                + "products.NAME, tickets.TICKETTYPE "
                                                + "ORDER BY receipts.DATENEW DESC",
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
                } catch (BasicException ex) {
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
                } catch (BasicException ex) {
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
                } catch (BasicException ex) {
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
         * @param card
         * @return
         * @throws BasicException
         */
        public CustomerInfoExt findCustomerExt(String card) throws BasicException {
                return (CustomerInfoExt) new PreparedSentence(sessionDB,
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
                                                + "WHERE CARD = ? AND VISIBLE = " + sessionDB.DB.TRUE() + " "
                                                + "ORDER BY NAME",
                                SerializerWriteString.INSTANCE,
                                new CustomerExtRead()).find(card);
        }

        /**
         *
         * @param name
         * @return
         * @throws BasicException
         */
        public CustomerInfoExt findCustomerName(String name) throws BasicException {
                return (CustomerInfoExt) new PreparedSentence(sessionDB,
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
                                                + "WHERE NAME = ? AND VISIBLE = " + sessionDB.DB.TRUE() + " "
                                                + "ORDER BY NAME",
                                SerializerWriteString.INSTANCE,
                                new CustomerExtRead()).find(name);
        }

        /**
         *
         * @param id
         * @return
         * @throws BasicException
         */
        public CustomerInfoExt loadCustomerExt(String id) throws BasicException {
                return (CustomerInfoExt) new PreparedSentence(sessionDB,
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
                                new CustomerExtRead()).find(id);
        }

        /**
         * Quick Customer create
         *
         * @param id
         * @return
         * @throws BasicException
         */
        public CustomerInfoExt qCustomerExt(String id) throws BasicException {
                return (CustomerInfoExt) new PreparedSentence(sessionDB,
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
                                new CustomerExtRead()).find(id);
        }

        /**
         *
         * @param tickettype
         * @param ticketid
         * @return
         * @throws BasicException
         */
        public final TicketInfo loadTicket(final int tickettype, final int ticketid) throws BasicException {

                SerializerWrite<Object[]> sw = new SerializerWriteBasicExt(new Datas[] { Datas.INT, Datas.INT },
                                new int[] { 0, 1 });
                Object[] params = new Object[] { tickettype, ticketid };

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

                if (ticket != null) {

                        String customerid = ticket.getCustomerId();
                        ticket.setCustomer(customerid == null
                                        ? null
                                        : loadCustomerExt(customerid));

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
                return ticket;
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
                                } catch (IOException e) {
                                        LOGGER.log(Level.WARNING, "Cannot convert ticket properties to XML ", e);
                                }

                                // Receipt Writer
                                SerializerWrite<Object[]> sw = new SerializerWriteBasicExt(
                                                new Datas[] { Datas.STRING, Datas.STRING, Datas.TIMESTAMP, Datas.BYTES,
                                                                Datas.STRING },
                                                new int[] { 0, 1, 2, 3, 4 });
                                Object[] params = new Object[] {
                                                ticket.getId(),
                                                ticket.getActiveCash(),
                                                ticket.getDate(),
                                                properties,
                                                ticket.getProperty("person") };

                                // Receipt Prepared
                                new PreparedSentence(sessionDB,
                                                "INSERT INTO receipts (ID, MONEY, DATENEW, ATTRIBUTES, PERSON) VALUES (?, ?, ?, ?, ?)",
                                                sw)
                                                .exec(params);

                                // new ticket
                                sw = new SerializerWriteBasicExt(
                                                new Datas[] { Datas.STRING, Datas.INT, Datas.INT, Datas.STRING,
                                                                Datas.STRING, Datas.INT },
                                                new int[] { 0, 1, 2, 3, 4, 5 });
                                params = new Object[] {
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
                                                getStockDiaryInsert().exec(new Object[] {
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

                                // Payments
                                final Payments payments = new Payments();
                                SentenceExec paymentinsert = new PreparedSentence(sessionDB,
                                                "INSERT INTO payments (ID, RECEIPT, PAYMENT, TOTAL, TRANSID, RETURNMSG, "
                                                                + "TENDERED, CARDNAME, VOUCHER) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                                                SerializerWriteParams.INSTANCE);

                                ticket.getPayments().forEach((p) -> {
                                        payments.addPayment(p.getName(), p.getTotal(), p.getPaid(),
                                                        ticket.getReturnMessage(),
                                                        p.getVoucher());
                                });
                                while (payments.getSize() >= 1) {

                                        pName = payments.getFirstElement();
                                        getTotal = payments.getPaidAmount(pName);
                                        getTendered = payments.getTendered(pName);
                                        getRetMsg = payments.getRtnMessage(pName);
                                        getVoucher = payments.getVoucher(pName);
                                        payments.removeFirst(pName);

                                        paymentinsert.exec(new DataParams() {

                                                @Override
                                                public void writeValues() throws BasicException {

                                                        setString(1, UUID.randomUUID().toString());
                                                        setString(2, ticket.getId());
                                                        setString(3, pName);
                                                        setDouble(4, getTotal);
                                                        setString(5, ticket.getTransactionID());
                                                        setBytes(6, Formats.BYTEA.parseValue(getRetMsg));
                                                        setDouble(7, getTendered);
                                                        setString(8, getCardName);
                                                        setString(9, getVoucher);
                                                        payments.removeFirst(pName);
                                                }
                                        });

                                        // Payment method: Disable that Voucher
                                        if (payments.getVoucher(pName) != null) {
                                                getVoucherNonActive().exec(payments.getVoucher(pName));
                                        }

                                        // Payment method: Add debt to Customer Account
                                        if (PAYMENT_METHOD_DEBT.equals(pName)
                                                        || PAYMENT_METHOD_DEBTPAID.equals(pName)) {
                                                ticket.getCustomer().updateCurDebt(getTotal, ticket.getDate());
                                                getDebtUpdate().exec(new DataParams() {

                                                        @Override
                                                        public void writeValues() throws BasicException {
                                                                setDouble(1, ticket.getCustomer().getAccdebt());
                                                                setTimestamp(2, ticket.getCustomer().getCurdate());
                                                                setString(3, ticket.getCustomer().getId());
                                                        }
                                                });
                                        }
                                }

                                // TAX Lines
                                SentenceExec taxlinesinsert = new PreparedSentence(sessionDB,
                                                "INSERT INTO taxlines (ID, RECEIPT, TAXID, BASE, AMOUNT)  "
                                                                + "VALUES (?, ?, ?, ?, ?)",
                                                SerializerWriteParams.INSTANCE);

                                if (ticket.getTaxes() != null) {
                                        for (final TicketTaxInfo tickettax : ticket.getTaxes()) {
                                                taxlinesinsert.exec(new DataParams() {
                                                        @Override
                                                        public void writeValues() throws BasicException {
                                                                setString(1, UUID.randomUUID().toString());
                                                                setString(2, ticket.getId());
                                                                setString(3, tickettax.getTaxInfo().getId());
                                                                setDouble(4, tickettax.getSubTotal());
                                                                setDouble(5, tickettax.getTax());
                                                        }
                                                });
                                        }
                                }
                                return null;
                        }
                };

                t.execute();
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
                                Date d = new Date();
                                for (int i = 0; i < ticket.getLinesCount(); i++) {
                                        if (ticket.getLine(i).getProductID() != null) {
                                                // Hay que actualizar el stock si el hay producto
                                                getStockDiaryInsert().exec(new Object[] {
                                                                UUID.randomUUID().toString(),
                                                                d,
                                                                ticket.getLine(i).getMultiply() >= 0.0
                                                                                ? MovementReason.IN_REFUND.getKey()
                                                                                : MovementReason.OUT_SALE.getKey(),
                                                                location,
                                                                ticket.getLine(i).getProductID(),
                                                                ticket.getLine(i).getProductAttSetInstId(),
                                                                ticket.getLine(i).getMultiply(),
                                                                ticket.getLine(i).getPrice(),
                                                                ticket.getUser().getName()
                                                });
                                        }
                                        // For productBundle
                                        List<ProductsBundleInfo> bundle = getProductsBundle(
                                                        (String) ticket.getLine(i).getProductID());

                                        if (bundle.size() > 0) {
                                                for (ProductsBundleInfo bundleComponent : bundle) {
                                                        ProductInfoExt bundleProduct = getProductInfo(
                                                                        bundleComponent.getProductBundleId());

                                                        getStockDiaryInsert().exec(new Object[] {
                                                                        UUID.randomUUID().toString(),
                                                                        d,
                                                                        ticket.getLine(i).getMultiply()
                                                                                        * bundleComponent
                                                                                                        .getQuantity() >= 0.0
                                                                                                                        ? MovementReason.IN_REFUND
                                                                                                                                        .getKey()
                                                                                                                        : MovementReason.OUT_SALE
                                                                                                                                        .getKey(),
                                                                        location,
                                                                        bundleComponent.getProductBundleId(),
                                                                        null,
                                                                        ticket.getLine(i).getMultiply()
                                                                                        * bundleComponent.getQuantity(),
                                                                        bundleProduct.getPriceSell(),
                                                                        ticket.getUser().getName() });
                                                }
                                        }
                                }

                                // update customer debts
                                for (PaymentInfo p : ticket.getPayments()) {
                                        if (PAYMENT_METHOD_DEBT.equals(p.getName())
                                                        || PAYMENT_METHOD_DEBTPAID.equals(p.getName())) {

                                                // udate customer fields...
                                                ticket.getCustomer().updateCurDebt(-p.getTotal(), ticket.getDate());

                                                // save customer fields...
                                                getDebtUpdate().exec(new DataParams() {
                                                        @Override
                                                        public void writeValues() throws BasicException {
                                                                setDouble(1, ticket.getCustomer().getAccdebt());
                                                                setTimestamp(2, ticket.getCustomer().getCurdate());
                                                                setString(3, ticket.getCustomer().getId());
                                                        }
                                                });
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
                } catch (BasicException e) {
                        return null;
                }
        }

        /**
         * Loads on ProductsEditor
         *
         * @return
         */
        public final SentenceList getProductCatQBF() {
                return new StaticSentence(sessionDB,
                                new QBFBuilder(
                                                "SELECT "
                                                                + "P.ID, "
                                                                + "P.REFERENCE, "
                                                                + "P.CODE, "
                                                                + "P.CODETYPE, "
                                                                + "P.NAME, "
                                                                + "P.PRICEBUY, "
                                                                + "P.PRICESELL, "
                                                                + "P.CATEGORY, "
                                                                + "P.TAXCAT, "
                                                                + "P.ATTRIBUTESET_ID, "
                                                                + "P.STOCKCOST, "
                                                                + "P.STOCKVOLUME, "
                                                                // JG 3 feb 16 speedup + "P.IMAGE, "
                                                                + sessionDB.DB.CHAR_NULL() + ","
                                                                + "P.ISCOM, "
                                                                + "P.ISSCALE, "
                                                                + "P.ISCONSTANT, "
                                                                + "P.PRINTKB, "
                                                                + "P.SENDSTATUS, "
                                                                + "P.ISSERVICE, "
                                                                + "P.ATTRIBUTES, "
                                                                + "P.DISPLAY, "
                                                                + "P.ISVPRICE, "
                                                                + "P.ISVERPATRIB, "
                                                                + "P.TEXTTIP, "
                                                                + "P.WARRANTY, "
                                                                + "P.STOCKUNITS, "
                                                                + "P.PRINTTO, "
                                                                + "P.SUPPLIER, "
                                                                + "P.UOM, "
                                                                + "P.MEMODATE, "
                                                                + "CASE WHEN "
                                                                + "C.PRODUCT IS NULL "
                                                                + "THEN " + sessionDB.DB.FALSE()
                                                                + " ELSE " + sessionDB.DB.TRUE()
                                                                + " END, "
                                                                + "C.CATORDER "
                                                                + "FROM products P LEFT OUTER JOIN products_cat C "
                                                                + "ON P.ID = C.PRODUCT "
                                                                + "WHERE ?(QBF_FILTER) "
                                                                + "ORDER BY P.REFERENCE",
                                                new String[] {
                                                                "P.NAME", "P.PRICEBUY", "P.PRICESELL", "P.CATEGORY",
                                                                "P.CODE" }),
                                new SerializerWriteBasic(new Datas[] {
                                                Datas.OBJECT, Datas.STRING, Datas.OBJECT, Datas.DOUBLE, Datas.OBJECT,
                                                Datas.DOUBLE,
                                                Datas.OBJECT, Datas.STRING, Datas.OBJECT, Datas.STRING }),
                                productsRow.getSerializerRead());
        }

        /**
         *
         * @return
         */
        public final SentenceExec getProductCatInsert() {
                return new SentenceExecTransaction(sessionDB) {
                        @Override
                        public int execInTransaction(Object[] params) throws BasicException {
                                int i = new PreparedSentenceExec(sessionDB,
                                                "INSERT INTO products ("
                                                                + "ID, "
                                                                + "REFERENCE, "
                                                                + "CODE, "
                                                                + "CODETYPE, "
                                                                + "NAME, "
                                                                + "PRICEBUY, "
                                                                + "PRICESELL, "
                                                                + "CATEGORY, "
                                                                + "TAXCAT, "
                                                                + "ATTRIBUTESET_ID, "
                                                                + "STOCKCOST, "
                                                                + "STOCKVOLUME, "
                                                                + "IMAGE, "
                                                                + "ISCOM, "
                                                                + "ISSCALE, "
                                                                + "ISCONSTANT, "
                                                                + "PRINTKB, "
                                                                + "SENDSTATUS, "
                                                                + "ISSERVICE, "
                                                                + "ATTRIBUTES, "
                                                                + "DISPLAY, "
                                                                + "ISVPRICE, "
                                                                + "ISVERPATRIB, "
                                                                + "TEXTTIP, "
                                                                + "WARRANTY, "
                                                                + "STOCKUNITS, "
                                                                + "PRINTTO, "
                                                                + "SUPPLIER, "
                                                                + "UOM, "
                                                                + "MEMODATE ) "
                                                                + "VALUES ("
                                                                + "?, ?, ?, ?, ?, ?, "
                                                                + "?, ?, ?, ?, ?, ?, "
                                                                + "?, ?, ?, ?, ?, ?, "
                                                                + "?, ?, ?, ?, ?, ?, "
                                                                + "?, ?, ?, ?, ?, ?)",
                                                productsRow.getDatas(),
                                                new int[] { 0,
                                                                1, 2, 3, 4, 5, 6,
                                                                7, 8, 9, 10, 11, 12,
                                                                13, 14, 15, 16, 17, 18,
                                                                19, 20, 21, 22, 23, 24,
                                                                25, 26, 27, 28, 29 })
                                                .exec(params);

                                if (i > 0 && ((Boolean) params[30])) {
                                        return new PreparedSentence(sessionDB,
                                                        "INSERT INTO products_cat (PRODUCT, CATORDER) VALUES (?, ?)",
                                                        new SerializerWriteBasicExt(productsRow.getDatas(),
                                                                        new int[] { 0, 31 }))
                                                        .exec(params);
                                } else {
                                        return i;
                                }
                        }
                };
        }

        Datas[] PRODUCT_TABLE = new Datas[] {
                        Datas.STRING,
                        Datas.STRING,
                        Datas.STRING,
                        Datas.STRING,
                        Datas.STRING,
                        Datas.DOUBLE,
                        Datas.DOUBLE,
                        Datas.STRING,
                        Datas.STRING,
                        Datas.STRING,
                        Datas.DOUBLE,
                        Datas.DOUBLE,
                        Datas.IMAGE,
                        Datas.BOOLEAN,
                        Datas.BOOLEAN,
                        Datas.BOOLEAN,
                        Datas.BOOLEAN,
                        Datas.BOOLEAN,
                        Datas.BOOLEAN,
                        Datas.BYTES,
                        Datas.STRING,
                        Datas.BOOLEAN,
                        Datas.BOOLEAN,
                        Datas.STRING,
                        Datas.BOOLEAN,
                        Datas.DOUBLE,
                        Datas.STRING,
                        Datas.STRING,
                        Datas.STRING,
                        Datas.TIMESTAMP };

        /**
         *
         * @return
         */
        public final SentenceExec getProductCatUpdate() {
                return new SentenceExecTransaction(sessionDB) {
                        @Override
                        public int execInTransaction(Object[] params) throws BasicException {

                                int i = new PreparedSentenceExec(sessionDB,
                                                "UPDATE products SET "
                                                                + "ID = ?, "
                                                                + "REFERENCE = ?, "
                                                                + "CODE = ?, "
                                                                + "CODETYPE = ?, "
                                                                + "NAME = ?, "
                                                                + "PRICEBUY = ?, "
                                                                + "PRICESELL = ?, "
                                                                + "CATEGORY = ?, "
                                                                + "TAXCAT = ?, "
                                                                + "ATTRIBUTESET_ID = ?, "
                                                                + "STOCKCOST = ?, "
                                                                + "STOCKVOLUME = ?, "
                                                                + "IMAGE = ?, "
                                                                + "ISCOM = ?, "
                                                                + "ISSCALE = ?, "
                                                                + "ISCONSTANT = ?, "
                                                                + "PRINTKB = ?, "
                                                                + "SENDSTATUS = ?, "
                                                                + "ISSERVICE = ?,  "
                                                                + "ATTRIBUTES = ?,"
                                                                + "DISPLAY = ?, "
                                                                + "ISVPRICE = ?, "
                                                                + "ISVERPATRIB = ?, "
                                                                + "TEXTTIP = ?, "
                                                                + "WARRANTY = ?, "
                                                                + "STOCKUNITS = ?, "
                                                                + "PRINTTO = ?, "
                                                                + "SUPPLIER = ?, "
                                                                + "UOM = ?, "
                                                                + "MEMODATE = ? "
                                                                + "WHERE ID = ?",
                                                PRODUCT_TABLE,
                                                new int[] { 0,
                                                                1, 2, 3, 4, 5,
                                                                6, 7, 8, 9, 10,
                                                                11, 12, 13, 14, 15,
                                                                16, 17, 18, 19, 20,
                                                                21, 22, 23, 24, 25,
                                                                26, 27, 28, 29, 0 })
                                                .exec(params);
                                if (i > 0) {
                                        if (((Boolean) params[30])) {
                                                if (new PreparedSentence(sessionDB,
                                                                "UPDATE products_cat SET CATORDER = ? WHERE PRODUCT = ?",
                                                                new SerializerWriteBasicExt(productsRow.getDatas(),
                                                                                new int[] { 31, 0 }))
                                                                .exec(params) == 0) {
                                                        new PreparedSentence(sessionDB,
                                                                        "INSERT INTO products_cat (PRODUCT, CATORDER) VALUES (?, ?)",
                                                                        new SerializerWriteBasicExt(
                                                                                        productsRow.getDatas(),
                                                                                        new int[] { 0, 31 }))
                                                                        .exec(params);
                                                }
                                        } else {
                                                new PreparedSentence(sessionDB,
                                                                "DELETE FROM products_cat WHERE PRODUCT = ?",
                                                                new SerializerWriteBasicExt(productsRow.getDatas(),
                                                                                new int[] { 0 }))
                                                                .exec(params);
                                        }
                                }
                                return i;
                        }
                };
        }

        /**
         *
         * @return
         */
        public final SentenceExec getProductCatDelete() {
                return new SentenceExecTransaction(sessionDB) {
                        @Override
                        public int execInTransaction(Object[] params) throws BasicException {
                                new PreparedSentence(sessionDB,
                                                "DELETE FROM products_cat WHERE PRODUCT = ?",
                                                new SerializerWriteBasicExt(productsRow.getDatas(), new int[] { 0 }))
                                                .exec(params);
                                return new PreparedSentence(sessionDB,
                                                "DELETE FROM products WHERE ID = ?",
                                                new SerializerWriteBasicExt(productsRow.getDatas(), new int[] { 0 }))
                                                .exec(params);
                        }
                };
        }

        /**
         *
         * @return
         */
        public final SentenceExec getDebtUpdate() {

                return new PreparedSentence(sessionDB,
                                "UPDATE customers SET CURDEBT = ?, CURDATE = ? WHERE ID = ?",
                                SerializerWriteParams.INSTANCE);
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
                                                                new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8 }))
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
                                                                                new int[] { 6, 3, 4 }))
                                                                .exec(params)
                                                : new PreparedSentence(sessionDB,
                                                                "UPDATE stockcurrent SET UNITS = (UNITS + ?) "
                                                                                + "WHERE LOCATION = ? AND PRODUCT = ? "
                                                                                + "AND ATTRIBUTESETINSTANCE_ID = ?",
                                                                new SerializerWriteBasicExt(stockdiaryDatas,
                                                                                new int[] { 6, 3, 4, 5 }))
                                                                .exec(params);

                                if (updateresult == 0) {
                                        new PreparedSentence(sessionDB,
                                                        "INSERT INTO stockcurrent (LOCATION, PRODUCT, "
                                                                        + "ATTRIBUTESETINSTANCE_ID, UNITS) "
                                                                        + "VALUES (?, ?, ?, ?)",
                                                        new SerializerWriteBasicExt(stockdiaryDatas,
                                                                        new int[] { 3, 4, 5, 6 }))
                                                        .exec(params);
                                }
                                return new PreparedSentence(sessionDB,
                                                "INSERT INTO stockdiary (ID, DATENEW, REASON, LOCATION, PRODUCT, "
                                                                + "ATTRIBUTESETINSTANCE_ID, UNITS, PRICE, AppUser, "
                                                                + "SUPPLIER, SUPPLIERDOC) "
                                                                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                                                new SerializerWriteBasicExt(stockdiaryDatas,
                                                                new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 }))
                                                .exec(params);

                        }
                };
        }

        public final void saveStockDiary(ProductStockTransaction prodStock) throws BasicException {

                getStockDiaryInsert1().exec(new Object[] {
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
                                                                                new int[] { 6, 3, 4 }))
                                                                .exec(params)
                                                : new PreparedSentence(sessionDB,
                                                                "UPDATE stockcurrent SET UNITS = (UNITS - ?) "
                                                                                + "WHERE LOCATION = ? AND PRODUCT = ? "
                                                                                + "AND ATTRIBUTESETINSTANCE_ID = ?",
                                                                new SerializerWriteBasicExt(stockdiaryDatas,
                                                                                new int[] { 6, 3, 4, 5 }))
                                                                .exec(params);

                                if (updateresult == 0) {
                                        new PreparedSentence(sessionDB,
                                                        "INSERT INTO stockcurrent (LOCATION, PRODUCT, "
                                                                        + "ATTRIBUTESETINSTANCE_ID, UNITS) "
                                                                        + "VALUES (?, ?, ?, -(?))",
                                                        new SerializerWriteBasicExt(stockdiaryDatas,
                                                                        new int[] { 3, 4, 5, 6 }))
                                                        .exec(params);
                                }
                                return new PreparedSentence(sessionDB,
                                                "DELETE FROM stockdiary WHERE ID = ?",
                                                new SerializerWriteBasicExt(stockdiaryDatas, new int[] { 0 }))
                                                .exec(params);
                        }
                };
        }

        private void adjustStock(Object params) throws BasicException {

                List<ProductsBundleInfo> bundle = getProductsBundle((String) ((Object[]) params)[0]);

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
                                                                        new int[] { 3, 1, 0 }))
                                                        .exec(params)
                                        : new PreparedSentence(sessionDB,
                                                        "UPDATE stockcurrent SET UNITS = (UNITS + ?) "
                                                                        + "WHERE LOCATION = ? AND PRODUCT = ? "
                                                                        + "AND ATTRIBUTESETINSTANCE_ID = ?",
                                                        new SerializerWriteBasicExt(stockAdjustDatas,
                                                                        new int[] { 3, 1, 0, 2 }))
                                                        .exec(params);

                        if (updateresult == 0) {

                                new PreparedSentence(sessionDB,
                                                "INSERT INTO stockcurrent (LOCATION, PRODUCT, "
                                                                + "ATTRIBUTESETINSTANCE_ID, UNITS) "
                                                                + "VALUES (?, ?, ?, ?)",
                                                new SerializerWriteBasicExt(stockAdjustDatas,
                                                                new int[] { 1, 0, 2, 3 }))
                                                .exec(params);
                        }
                }
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
                                                                new int[] { 0, 1, 2 }))
                                                .exec(params);
                                return new PreparedSentence(sessionDB,
                                                "INSERT INTO payments (ID, RECEIPT, PAYMENT, TOTAL, NOTES) "
                                                                + "VALUES (?, ?, ?, ?, ?)",
                                                new SerializerWriteBasicExt(paymenttabledatas,
                                                                new int[] { 3, 0, 4, 5, 6 }))
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
                                                new SerializerWriteBasicExt(paymenttabledatas, new int[] { 3 }))
                                                .exec(params);
                                return new PreparedSentence(sessionDB,
                                                "DELETE FROM receipts WHERE ID = ?",
                                                new SerializerWriteBasicExt(paymenttabledatas, new int[] { 0 }))
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

        public final TableDefinition getTableCategories() {
                return new TableDefinition(sessionDB,
                                "categories",
                                new String[] { "ID", "NAME", "PARENTID", "IMAGE", "TEXTTIP", "CATSHOWNAME", "CATORDER",
                                                "CATALOGCOLOR",
                                                "CATALOGENABLED" },
                                new String[] { "ID", AppLocal.getIntString("label.name"), "",
                                                AppLocal.getIntString("label.image"), "",
                                                "", "", "", "" },
                                new Datas[] { Datas.STRING, Datas.STRING, Datas.STRING, Datas.IMAGE, Datas.STRING,
                                                Datas.BOOLEAN,
                                                Datas.STRING, Datas.STRING, Datas.BOOLEAN },
                                new Formats[] { Formats.STRING, Formats.STRING, Formats.STRING, Formats.NULL,
                                                Formats.STRING,
                                                Formats.BOOLEAN, Formats.STRING, Formats.STRING, Formats.BOOLEAN },
                                new int[] { 0 });
        }

        public final TableDefinition getTableTaxes() {
                return new TableDefinition(sessionDB,
                                "taxes",
                                new String[] { "ID", "NAME", "CATEGORY", "CUSTCATEGORY", "PARENTID", "RATE",
                                                "RATECASCADE",
                                                "RATEORDER" },
                                new String[] { "ID", AppLocal.getIntString("label.name"),
                                                AppLocal.getIntString("label.taxcategory"),
                                                AppLocal.getIntString("label.custtaxcategory"),
                                                AppLocal.getIntString("label.taxparent"),
                                                AppLocal.getIntString("label.dutyrate"),
                                                AppLocal.getIntString("label.cascade"),
                                                AppLocal.getIntString("label.order") },
                                new Datas[] { Datas.STRING, Datas.STRING, Datas.STRING, Datas.STRING, Datas.STRING,
                                                Datas.DOUBLE,
                                                Datas.BOOLEAN, Datas.INT },
                                new Formats[] { Formats.STRING, Formats.STRING, Formats.STRING, Formats.STRING,
                                                Formats.STRING,
                                                Formats.PERCENT, Formats.BOOLEAN, Formats.INT },
                                new int[] { 0 });
        }

        public final TableDefinition getTableTaxCustCategories() {
                return new TableDefinition(sessionDB,
                                "taxcustcategories",
                                new String[] { "ID", "NAME" },
                                new String[] { "ID", AppLocal.getIntString("label.name") },
                                new Datas[] { Datas.STRING, Datas.STRING },
                                new Formats[] { Formats.STRING, Formats.STRING },
                                new int[] { 0 });
        }

        /**
         *
         * @return
         */
        public final TableDefinition getTableTaxCategories() {
                return new TableDefinition(sessionDB,
                                "taxcategories",
                                new String[] { "ID", "NAME" },
                                new String[] { "ID", AppLocal.getIntString("label.name") },
                                new Datas[] { Datas.STRING, Datas.STRING },
                                new Formats[] { Formats.STRING, Formats.STRING },
                                new int[] { 0 });
        }

        /**
         *
         * @return
         */
        public final TableDefinition getTableLocations() {
                return new TableDefinition(sessionDB,
                                "locations",
                                new String[] { "ID", "NAME", "ADDRESS" },
                                new String[] { "ID", AppLocal.getIntString("label.locationname"),
                                                AppLocal.getIntString("label.locationaddress") },
                                new Datas[] { Datas.STRING, Datas.STRING, Datas.STRING },
                                new Formats[] { Formats.STRING, Formats.STRING, Formats.STRING },
                                new int[] { 0 });
        }

        protected static class CustomerExtRead implements SerializerRead<CustomerInfoExt> {

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
                                new String[] { "id", "name" },
                                new String[] { "id",
                                                AppLocal.getIntString("label.name") },
                                new Datas[] {
                                                Datas.STRING, Datas.STRING },
                                new Formats[] {
                                                Formats.STRING, Formats.STRING },
                                new int[] { 0 });
        }

        public final SentenceList<UomInfo> getUomList() {
                return new StaticSentence(sessionDB, "SELECT ID, NAME  FROM uom ORDER BY NAME", null,
                                UomInfo.getSerializerRead());
        }

        public final List<UomInfo> getUomListAll() {
                List<UomInfo> list = null;
                try {
                        list = this.getUomList().list();
                } catch (BasicException ex) {
                        LOGGER.log(Level.WARNING, "Cannot get UomInfo list", ex);
                }
                return list;
        }

        public final SentenceList<VoucherInfo> getVoucherList() {
                return new StaticSentence(sessionDB,
                                "SELECT "
                                                + "vouchers.ID,vouchers.VOUCHER_NUMBER,vouchers.CUSTOMER, "
                                                + "customers.NAME,AMOUNT, STATUS "
                                                + "FROM vouchers   "
                                                + "JOIN customers ON customers.id = vouchers.CUSTOMER  "
                                                + "WHERE STATUS='A' "
                                                + "ORDER BY vouchers.VOUCHER_NUMBER ASC",
                                null, VoucherInfo.getSerializerRead());
        }

        public final SentenceExec getVoucherNonActive() {
                return new PreparedSentence(sessionDB,
                                "UPDATE vouchers SET STATUS = 'D' "
                                                + "WHERE VOUCHER_NUMBER = ?",
                                SerializerWriteString.INSTANCE);
        }

        public final SentenceExec resetPickupId() {

                return new PreparedSentence(sessionDB,
                                "UPDATE pickup_number SET ID=1 ",
                                SerializerWriteString.INSTANCE);

        }

        /**
         *
         * @param id
         * @return
         * @throws BasicException
         */
        public SupplierInfoExt loadSupplierExt(String id) throws BasicException {
                return (SupplierInfoExt) new PreparedSentence(sessionDB,
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
                                                                new int[] { 0,
                                                                                1, 2, 3, 4, 5, 6,
                                                                                7, 8, 9, 10, 11, 12,
                                                                                13, 14, 15, 16, 17, 18,
                                                                                19, 20, 21, 22, 23, 24,
                                                                                25, 26 }))
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
                                                                new int[] { 0,
                                                                                1, 2, 3, 4, 5,
                                                                                6, 7, 8, 9, 10,
                                                                                11, 12, 13, 14, 15,
                                                                                16, 17, 18, 19, 20,
                                                                                21, 22, 23, 24, 25,
                                                                                26, 0 }))
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
                                                                new int[] { 0 }))
                                                .exec(params);
                        }
                };
        }

        /**
         * Updates the sell price of a product.
         *
         * @param productId the product ID
         * @param newPrice  the new sell price
         * @throws BasicException if the update fails
         */
        public void updateProductPrice(String productId, double newPrice) throws BasicException {
                new PreparedSentence(sessionDB,
                                "UPDATE PRODUCTS SET PRICESELL = ? WHERE ID = ?",
                                new SerializerWriteBasic(new Datas[] { Datas.DOUBLE, Datas.STRING }))
                                .exec(new Object[] { newPrice, productId });
        }

}
