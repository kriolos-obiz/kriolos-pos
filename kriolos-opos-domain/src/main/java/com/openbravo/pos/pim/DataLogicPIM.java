/*
 * Copyright (C) 2026 KriolOS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.openbravo.pos.pim;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.Datas;
import com.openbravo.data.loader.PreparedSentence;
import com.openbravo.data.loader.PreparedSentenceExec;
import com.openbravo.data.loader.QBFBuilder;
import com.openbravo.data.loader.SentenceExec;
import com.openbravo.data.loader.SentenceExecTransaction;
import com.openbravo.data.loader.SentenceList;
import com.openbravo.data.loader.SerializerWriteBasic;
import com.openbravo.data.loader.SerializerWriteBasicExt;
import com.openbravo.data.loader.SerializerWriteString;
import com.openbravo.data.loader.Session;
import com.openbravo.data.loader.StaticSentence;
import com.openbravo.data.loader.TableDefinition;
import com.openbravo.data.model.Field;
import com.openbravo.data.model.Row;
import com.openbravo.format.Formats;
import com.openbravo.pos.catalog.CategoryStock;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.BeanFactoryDataSingle;
import com.openbravo.pos.inventory.ProductsBundleInfo;
import com.openbravo.pos.ticket.ProductInfo;
import com.openbravo.pos.ticket.ProductInfoExt;
import com.openbravo.pos.ticket.ProductInfoExtA;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author poolborges
 */
public class DataLogicPIM extends BeanFactoryDataSingle {

    private static final Logger LOGGER = Logger.getLogger(BeanFactoryDataSingle.class.getName());

    private Session sessionDB;

    @Override
    public void init(Session sessionDB) {
        this.sessionDB = sessionDB;
    }

    // <editor-fold defaultstate="collapsed" desc="CATEGORY MANAGEMENT"> 
    public final TableDefinition getTableCategories() {
        return new TableDefinition(sessionDB,
                "categories",
                new String[]{"ID", "NAME", "PARENTID", "IMAGE", "TEXTTIP", "CATSHOWNAME", "CATORDER",
                    "CATALOGCOLOR",
                    "CATALOGENABLED"},
                new String[]{"ID", AppLocal.getIntString("label.name"), "",
                    AppLocal.getIntString("label.image"), "",
                    "", "", "", ""},
                new Datas[]{Datas.STRING, Datas.STRING, Datas.STRING, Datas.IMAGE, Datas.STRING,
                    Datas.BOOLEAN,
                    Datas.STRING, Datas.STRING, Datas.BOOLEAN},
                new Formats[]{Formats.STRING, Formats.STRING, Formats.STRING, Formats.NULL,
                    Formats.STRING,
                    Formats.BOOLEAN, Formats.STRING, Formats.STRING, Formats.BOOLEAN},
                new int[]{0});
    }

    public final void createCategory(Object[] category) throws BasicException {
        SentenceExec m_createCat = new StaticSentence(this.sessionDB,
                "INSERT INTO categories ( ID, NAME, CATSHOWNAME ) "
                + "VALUES (?, ?, ?)",
                new SerializerWriteBasic(new Datas[]{Datas.STRING, Datas.STRING, Datas.BOOLEAN}));
        m_createCat.exec(category);
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
                + "CATALOGENABLED, "
                + "PARENTID "
                + "FROM categories "
                + "WHERE ID = ? "
                + "ORDER BY CATORDER, NAME",
                SerializerWriteString.INSTANCE,
                CategoryInfo.getSerializerRead()).find(id);
    }

    /**
     * @deprecated since Nov/2025
     * @return
     */
    private final SentenceList<CategoryInfo> getCategoriesList() {
        return new StaticSentence(sessionDB,
                "SELECT "
                + "ID, "
                + "NAME, "
                + "IMAGE, "
                + "TEXTTIP, "
                + "CATSHOWNAME, "
                + "CATORDER, "
                + "CATALOGCOLOR, "
                + "CATALOGENABLED, "
                + "PARENTID "
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
                + "CATALOGENABLED, "
                + "PARENTID "
                + "FROM categories "
                + "WHERE PARENTID IS NULL "
                + "ORDER BY NAME",
                null,
                CategoryInfo.getSerializerRead());
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
                + "CATALOGENABLED, "
                + "PARENTID "
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
                + "CATALOGENABLED, "
                + "PARENTID "
                + "FROM categories "
                + "WHERE PARENTID = ? "
                + "ORDER BY CATORDER, NAME",
                SerializerWriteString.INSTANCE,
                CategoryInfo.getSerializerRead()).list(category);
    }

    //// </editor-fold>   END CATEGORY MANAGEMENT


    // <editor-fold defaultstate="collapsed" desc="PRODUCT MANAGEMENT">  
    
    
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
                new SerializerWriteBasic(new Datas[]{Datas.DOUBLE, Datas.STRING}))
                .exec(new Object[]{newPrice, productId});
    }

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

    public final SentenceExec productInsert() {
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
                        new int[]{0,
                            1, 2, 3, 4, 5, 6,
                            7, 8, 9, 10, 11, 12,
                            13, 14, 15, 16, 17, 18,
                            19, 20, 21, 22, 23, 24,
                            25, 26, 27, 28, 29})
                        .exec(params);

                if (i > 0 && ((Boolean) params[30])) {
                    return new PreparedSentence(sessionDB,
                            "INSERT INTO products_cat (PRODUCT, CATORDER) VALUES (?, ?)",
                            new SerializerWriteBasicExt(productsRow.getDatas(),
                                    new int[]{0, 31}))
                            .exec(params);
                } else {
                    return i;
                }
            }
        };
    }

    Datas[] PRODUCT_TABLE = new Datas[]{
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
        Datas.TIMESTAMP};

    /**
     *
     * @return
     */
    public final SentenceExec productUpdate() {
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
                        new int[]{0,
                            1, 2, 3, 4, 5,
                            6, 7, 8, 9, 10,
                            11, 12, 13, 14, 15,
                            16, 17, 18, 19, 20,
                            21, 22, 23, 24, 25,
                            26, 27, 28, 29, 0})
                        .exec(params);
                if (i > 0) {
                    if (((Boolean) params[30])) {
                        if (new PreparedSentence(sessionDB,
                                "UPDATE products_cat SET CATORDER = ? WHERE PRODUCT = ?",
                                new SerializerWriteBasicExt(productsRow.getDatas(),
                                        new int[]{31, 0}))
                                .exec(params) == 0) {
                            new PreparedSentence(sessionDB,
                                    "INSERT INTO products_cat (PRODUCT, CATORDER) VALUES (?, ?)",
                                    new SerializerWriteBasicExt(
                                            productsRow.getDatas(),
                                            new int[]{0, 31}))
                                    .exec(params);
                        }
                    } else {
                        new PreparedSentence(sessionDB,
                                "DELETE FROM products_cat WHERE PRODUCT = ?",
                                new SerializerWriteBasicExt(productsRow.getDatas(),
                                        new int[]{0}))
                                .exec(params);
                    }
                }
                return i;
            }
        };
    }

    private final static Row productsRow = new Row(
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
            new Field("MEMODATE", Datas.TIMESTAMP, Formats.TIMESTAMP),
            new Field("ISCATALOG", Datas.BOOLEAN, Formats.BOOLEAN),
            new Field("CATORDER", Datas.INT, Formats.INT)
    );
    

    public final Row getProductsRow() {
        return productsRow;
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
                        new String[]{
                            "P.NAME", "P.PRICEBUY", "P.PRICESELL", "P.CATEGORY",
                            "P.CODE"}),
                new SerializerWriteBasic(new Datas[]{
            Datas.OBJECT, Datas.STRING, Datas.OBJECT, Datas.DOUBLE, Datas.OBJECT,
            Datas.DOUBLE,
            Datas.OBJECT, Datas.STRING, Datas.OBJECT, Datas.STRING}),
                productsRow.getSerializerRead());
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
                        new SerializerWriteBasicExt(productsRow.getDatas(), new int[]{0}))
                        .exec(params);
                return new PreparedSentence(sessionDB,
                        "DELETE FROM products WHERE ID = ?",
                        new SerializerWriteBasicExt(productsRow.getDatas(), new int[]{0}))
                        .exec(params);
            }
        };
    }
    
    public static ProductInfoExt getProductInfoExtById (String productId, Session sessionDB)  throws BasicException{
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
                                ProductInfoExt.getSerializerRead()).find(productId);
    }

    /**
     *
     * @param id
     * @return
     * @throws BasicException
     */
    public final ProductInfoExt getProductInfo(String id) throws BasicException {
        return getProductInfoExtById(id, sessionDB);
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
                        new String[]{"NAME", "PRICEBUY", "PRICESELL", "CATEGORY", "CODE"}),
                new SerializerWriteBasic(new Datas[]{
            Datas.OBJECT, Datas.STRING,
            Datas.OBJECT, Datas.DOUBLE,
            Datas.OBJECT, Datas.DOUBLE,
            Datas.OBJECT, Datas.STRING,
            Datas.OBJECT, Datas.STRING}),
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
                        new String[]{"NAME", "PRICEBUY", "PRICESELL", "CATEGORY", "CODE"}),
                new SerializerWriteBasic(new Datas[]{
            Datas.OBJECT, Datas.STRING,
            Datas.OBJECT, Datas.DOUBLE,
            Datas.OBJECT, Datas.DOUBLE,
            Datas.OBJECT, Datas.STRING,
            Datas.OBJECT, Datas.STRING}),
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
                        new String[]{"NAME", "UNITS", "SellIncTax", "LOCATION",}),
                new SerializerWriteBasic(new Datas[]{
            Datas.OBJECT, Datas.STRING,
            Datas.OBJECT, Datas.DOUBLE,
            Datas.OBJECT, Datas.DOUBLE,
            Datas.OBJECT, Datas.STRING}),
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
                        new String[]{"NAME", "PRICEBUY", "PRICESELL", "CATEGORY", "CODE"}),
                new SerializerWriteBasic(new Datas[]{
            Datas.OBJECT, Datas.STRING,
            Datas.OBJECT, Datas.DOUBLE,
            Datas.OBJECT, Datas.DOUBLE,
            Datas.OBJECT, Datas.STRING,
            Datas.OBJECT, Datas.STRING}),
                ProductInfoExt.getSerializerRead());
    }
    
    public static final List<ProductsBundleInfo> getProductsBundle(String productId, Session sessionDB) throws BasicException {
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


//// </editor-fold>   PRODUCT MANAGEMENT

}
