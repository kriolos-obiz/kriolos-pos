package com.openbravo.pos.pim;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.DataRead;
import com.openbravo.data.loader.Datas;
import com.openbravo.data.loader.ImageUtils;
import com.openbravo.data.loader.PreparedSentence;
import com.openbravo.data.loader.PreparedSentenceExec;
import com.openbravo.data.loader.QBFBuilder;
import com.openbravo.data.loader.SentenceExec;
import com.openbravo.data.loader.SentenceExecTransaction;
import com.openbravo.data.loader.SentenceFind;
import com.openbravo.data.loader.SentenceList;
import com.openbravo.data.loader.SerializerWriteBasic;
import com.openbravo.data.loader.SerializerWriteBasicExt;
import com.openbravo.data.loader.SerializerWriteString;
import com.openbravo.data.loader.Session;
import com.openbravo.data.loader.StaticSentence;
import com.openbravo.data.model.Field;
import com.openbravo.data.model.Row;
import com.openbravo.data.user.DefaultSaveProvider;
import com.openbravo.data.user.EditorCreator;
import com.openbravo.data.user.ListProvider;
import com.openbravo.data.user.ListProviderCreator;
import com.openbravo.data.user.SaveProvider;
import com.openbravo.format.Formats;
import com.openbravo.pos.catalog.CategoryStock;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.BeanFactoryDataSingle;
import com.openbravo.pos.inventory.AttributeSetInfo;
import com.openbravo.pos.inventory.ProductsBundleInfo;
import com.openbravo.pos.ticket.ProductInfo;
import com.openbravo.pos.ticket.ProductInfoExt;
import com.openbravo.pos.ticket.ProductInfoExtA;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data access logic component specifically for Products, Pricing, Catalog and Bundles.
 * Completely decoupled from other DataLogics and operates directly against the database session.
 *
 * @author KriolOS
 */
public class DataLogicProducts extends BeanFactoryDataSingle {

    private static final Logger LOGGER = Logger.getLogger(DataLogicProducts.class.getName());

    private Session sessionDB;

    private static final Row PRODUCTS_ROW = new Row(
            new Field("ID", Datas.STRING, Formats.STRING),
            new Field(AppLocal.getIntString("label.prodref"), Datas.STRING, Formats.STRING, true, true, true),
            new Field(AppLocal.getIntString("label.prodbarcode"), Datas.STRING, Formats.STRING, false, true, true),
            new Field(AppLocal.getIntString("label.prodbarcodetype"), Datas.STRING, Formats.STRING, false, true, true),
            new Field(AppLocal.getIntString("label.prodname"), Datas.STRING, Formats.STRING, true, true, true),
            new Field(AppLocal.getIntString("label.prodpricebuy"), Datas.DOUBLE, Formats.CURRENCY, false, true, true),
            new Field(AppLocal.getIntString("label.prodpricesell"), Datas.DOUBLE, Formats.CURRENCY, false, true, true),
            new Field(AppLocal.getIntString("label.prodcategory"), Datas.STRING, Formats.STRING, false, false, true),
            new Field(AppLocal.getIntString("label.taxcategory"), Datas.STRING, Formats.STRING, false, false, true),
            new Field(AppLocal.getIntString("label.attributeset"), Datas.STRING, Formats.STRING, false, false, true),
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
            new Field(AppLocal.getIntString("label.display"), Datas.STRING, Formats.STRING, false, true, true),
            new Field("ISVPRICE", Datas.BOOLEAN, Formats.BOOLEAN),
            new Field("ISVERPATRIB", Datas.BOOLEAN, Formats.BOOLEAN),
            new Field("TEXTTIP", Datas.STRING, Formats.STRING),
            new Field("WARRANTY", Datas.BOOLEAN, Formats.BOOLEAN),
            new Field(AppLocal.getIntString("label.stockunits"), Datas.DOUBLE, Formats.DOUBLE),
            new Field("PRINTTO", Datas.STRING, Formats.STRING),
            new Field(AppLocal.getIntString("label.prodsupplier"), Datas.STRING, Formats.STRING, false, false, true),
            new Field(AppLocal.getIntString("label.UOM"), Datas.STRING, Formats.STRING),
            new Field("MEMODATE", Datas.TIMESTAMP, Formats.TIMESTAMP),
            new Field("ISCATALOG", Datas.BOOLEAN, Formats.BOOLEAN),
            new Field("CATORDER", Datas.INT, Formats.INT)
    );

    private static final Datas[] PRODUCT_TABLE = new Datas[]{
        Datas.STRING, Datas.STRING, Datas.STRING, Datas.STRING, Datas.STRING,
        Datas.DOUBLE, Datas.DOUBLE, Datas.STRING, Datas.STRING, Datas.STRING,
        Datas.DOUBLE, Datas.DOUBLE, Datas.IMAGE, Datas.BOOLEAN, Datas.BOOLEAN,
        Datas.BOOLEAN, Datas.BOOLEAN, Datas.BOOLEAN, Datas.BOOLEAN, Datas.BYTES,
        Datas.STRING, Datas.BOOLEAN, Datas.BOOLEAN, Datas.STRING, Datas.BOOLEAN,
        Datas.DOUBLE, Datas.STRING, Datas.STRING, Datas.STRING, Datas.TIMESTAMP
    };

    /**
     * Default constructor for BeanFactory initialization.
     */
    public DataLogicProducts() {
    }

    /**
     * Initializes this component with the active database session.
     *
     * @param sessionDB active database session
     */
    @Override
    public void init(Session sessionDB) {
        this.sessionDB = sessionDB;
    }

    /**
     * Returns the underlying database session.
     *
     * @return active {@link Session}
     */
    public Session getSession() {
        return sessionDB;
    }

    // =========================================================================
    // Save & List Providers
    // =========================================================================

    /**
     * Returns the product save provider combining insert, update, and delete sentences.
     *
     * @return {@link SaveProvider} for products
     */
    public SaveProvider getProductSaveProvider() {
        return new DefaultSaveProvider(productUpdate(), productInsert(), getProductCatDelete());
    }

    /**
     * Returns the product list provider without filters.
     *
     * @return {@link ListProvider} for products
     */
    public ListProvider getProductListProvider() {
        return new ListProviderCreator(getProductCatQBF());
    }

    /**
     * Returns the product list provider with editor filter criteria.
     *
     * @param filter editor filter
     * @return filtered {@link ListProvider} for products
     */
    public ListProvider getProductListProvider(EditorCreator filter) {
        return new ListProviderCreator(getProductCatQBF(), filter);
    }

    /**
     * Returns the metadata definition of product table rows.
     *
     * @return {@link Row} definition
     */
    public Row getProductsRow() {
        return PRODUCTS_ROW;
    }

    // =========================================================================
    // Product CRUD & QBF Sentences
    // =========================================================================

    /**
     * Returns the transactional sentence for inserting a product and its catalog order entry.
     *
     * @return {@link SentenceExec} for inserting products
     */
    public SentenceExec productInsert() {
        return new SentenceExecTransaction(sessionDB) {
            @Override
            public int execInTransaction(Object[] params) throws BasicException {
                int inserted = new PreparedSentenceExec(sessionDB,
                        "INSERT INTO products ("
                        + "ID, REFERENCE, CODE, CODETYPE, NAME, PRICEBUY, "
                        + "PRICESELL, CATEGORY, TAXCAT, ATTRIBUTESET_ID, STOCKCOST, STOCKVOLUME, "
                        + "IMAGE, ISCOM, ISSCALE, ISCONSTANT, PRINTKB, SENDSTATUS, "
                        + "ISSERVICE, ATTRIBUTES, DISPLAY, ISVPRICE, ISVERPATRIB, TEXTTIP, "
                        + "WARRANTY, STOCKUNITS, PRINTTO, SUPPLIER, UOM, MEMODATE ) "
                        + "VALUES ("
                        + "?, ?, ?, ?, ?, ?, "
                        + "?, ?, ?, ?, ?, ?, "
                        + "?, ?, ?, ?, ?, ?, "
                        + "?, ?, ?, ?, ?, ?, "
                        + "?, ?, ?, ?, ?, ?)",
                        PRODUCTS_ROW.getDatas(),
                        new int[]{0,
                            1, 2, 3, 4, 5, 6,
                            7, 8, 9, 10, 11, 12,
                            13, 14, 15, 16, 17, 18,
                            19, 20, 21, 22, 23, 24,
                            25, 26, 27, 28, 29})
                        .exec(params);

                if (inserted > 0 && ((Boolean) params[30])) {
                    return new PreparedSentence(sessionDB,
                            "INSERT INTO products_cat (PRODUCT, CATORDER) VALUES (?, ?)",
                            new SerializerWriteBasicExt(PRODUCTS_ROW.getDatas(), new int[]{0, 31}))
                            .exec(params);
                } else {
                    return inserted;
                }
            }
        };
    }

    /**
     * Returns the transactional sentence for updating a product and its catalog order entry.
     *
     * @return {@link SentenceExec} for updating products
     */
    public SentenceExec productUpdate() {
        return new SentenceExecTransaction(sessionDB) {
            @Override
            public int execInTransaction(Object[] params) throws BasicException {
                int updated = new PreparedSentenceExec(sessionDB,
                        "UPDATE products SET "
                        + "ID = ?, REFERENCE = ?, CODE = ?, CODETYPE = ?, NAME = ?, PRICEBUY = ?, "
                        + "PRICESELL = ?, CATEGORY = ?, TAXCAT = ?, ATTRIBUTESET_ID = ?, STOCKCOST = ?, "
                        + "STOCKVOLUME = ?, IMAGE = ?, ISCOM = ?, ISSCALE = ?, ISCONSTANT = ?, "
                        + "PRINTKB = ?, SENDSTATUS = ?, ISSERVICE = ?, ATTRIBUTES = ?, DISPLAY = ?, "
                        + "ISVPRICE = ?, ISVERPATRIB = ?, TEXTTIP = ?, WARRANTY = ?, STOCKUNITS = ?, "
                        + "PRINTTO = ?, SUPPLIER = ?, UOM = ?, MEMODATE = ? "
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

                if (updated > 0) {
                    if (((Boolean) params[30])) {
                        if (new PreparedSentence(sessionDB,
                                "UPDATE products_cat SET CATORDER = ? WHERE PRODUCT = ?",
                                new SerializerWriteBasicExt(PRODUCTS_ROW.getDatas(), new int[]{31, 0}))
                                .exec(params) == 0) {
                            new PreparedSentence(sessionDB,
                                    "INSERT INTO products_cat (PRODUCT, CATORDER) VALUES (?, ?)",
                                    new SerializerWriteBasicExt(PRODUCTS_ROW.getDatas(), new int[]{0, 31}))
                                    .exec(params);
                        }
                    } else {
                        new PreparedSentence(sessionDB,
                                "DELETE FROM products_cat WHERE PRODUCT = ?",
                                new SerializerWriteBasicExt(PRODUCTS_ROW.getDatas(), new int[]{0}))
                                .exec(params);
                    }
                }
                return updated;
            }
        };
    }

    /**
     * Returns the transactional sentence for deleting a product and its catalog order entries.
     *
     * @return {@link SentenceExec} for deleting products
     */
    public SentenceExec getProductCatDelete() {
        return new SentenceExecTransaction(sessionDB) {
            @Override
            public int execInTransaction(Object[] params) throws BasicException {
                new PreparedSentence(sessionDB,
                        "DELETE FROM products_cat WHERE PRODUCT = ?",
                        new SerializerWriteBasicExt(PRODUCTS_ROW.getDatas(), new int[]{0}))
                        .exec(params);
                return new PreparedSentence(sessionDB,
                        "DELETE FROM products WHERE ID = ?",
                        new SerializerWriteBasicExt(PRODUCTS_ROW.getDatas(), new int[]{0}))
                        .exec(params);
            }
        };
    }

    /**
     * Returns the query-by-filter sentence for products listing in maintenance panels.
     *
     * @return {@link SentenceList} of product records
     */
    public SentenceList getProductCatQBF() {
        return new StaticSentence(sessionDB,
                new QBFBuilder(
                        "SELECT "
                        + "P.ID, P.REFERENCE, P.CODE, P.CODETYPE, P.NAME, P.PRICEBUY, P.PRICESELL, "
                        + "P.CATEGORY, P.TAXCAT, P.ATTRIBUTESET_ID, P.STOCKCOST, P.STOCKVOLUME, "
                        + sessionDB.DB.CHAR_NULL() + ", "
                        + "P.ISCOM, P.ISSCALE, P.ISCONSTANT, P.PRINTKB, P.SENDSTATUS, P.ISSERVICE, "
                        + "P.ATTRIBUTES, P.DISPLAY, P.ISVPRICE, P.ISVERPATRIB, P.TEXTTIP, P.WARRANTY, "
                        + "P.STOCKUNITS, P.PRINTTO, P.SUPPLIER, P.UOM, P.MEMODATE, "
                        + "CASE WHEN C.PRODUCT IS NULL THEN " + sessionDB.DB.FALSE()
                        + " ELSE " + sessionDB.DB.TRUE() + " END, "
                        + "C.CATORDER "
                        + "FROM products P LEFT OUTER JOIN products_cat C "
                        + "ON P.ID = C.PRODUCT "
                        + "WHERE ?(QBF_FILTER) "
                        + "ORDER BY P.REFERENCE",
                        new String[]{"P.NAME", "P.PRICEBUY", "P.PRICESELL", "P.CATEGORY", "P.CODE"}),
                new SerializerWriteBasic(new Datas[]{
                    Datas.OBJECT, Datas.STRING, Datas.OBJECT, Datas.DOUBLE, Datas.OBJECT,
                    Datas.DOUBLE, Datas.OBJECT, Datas.STRING, Datas.OBJECT, Datas.STRING}),
                PRODUCTS_ROW.getSerializerRead());
    }

    // =========================================================================
    // Product Price & Lookups
    // =========================================================================

    /**
     * Updates the sell price of a specific product.
     *
     * @param productId product identifier
     * @param newPrice  new sell price
     * @throws BasicException on database failure
     */
    public void updateProductPrice(String productId, double newPrice) throws BasicException {
        new PreparedSentence(sessionDB,
                "UPDATE PRODUCTS SET PRICESELL = ? WHERE ID = ?",
                new SerializerWriteBasic(new Datas[]{Datas.DOUBLE, Datas.STRING}))
                .exec(new Object[]{newPrice, productId});
    }

    /**
     * Static helper for looking up full product details by identifier using an arbitrary session.
     *
     * @param productId product identifier
     * @param sessionDB active database session
     * @return {@link ProductInfoExt} or null
     * @throws BasicException on database failure
     */
    public static ProductInfoExt getProductInfoExtById(String productId, Session sessionDB) throws BasicException {
        return new PreparedSentence<String, ProductInfoExt>(sessionDB,
                "SELECT "
                + "ID, REFERENCE, CODE, CODETYPE, NAME, PRICEBUY, PRICESELL, "
                + "CATEGORY, TAXCAT, ATTRIBUTESET_ID, STOCKCOST, STOCKVOLUME, "
                + "IMAGE, ISCOM, ISSCALE, ISCONSTANT, PRINTKB, SENDSTATUS, "
                + "ISSERVICE, ATTRIBUTES, DISPLAY, ISVPRICE, ISVERPATRIB, TEXTTIP, "
                + "WARRANTY, STOCKUNITS, PRINTTO, SUPPLIER, UOM, MEMODATE "
                + "FROM products WHERE ID = ?",
                SerializerWriteString.INSTANCE,
                ProductInfoExt.getSerializerRead()).find(productId);
    }

    /**
     * Retrieves full product details by identifier.
     *
     * @param id product identifier
     * @return {@link ProductInfoExt} or null
     * @throws BasicException on database failure
     */
    public ProductInfoExt getProductInfo(String id) throws BasicException {
        return getProductInfoExtById(id, sessionDB);
    }

    /**
     * Retrieves full product details by exact barcode match.
     *
     * @param code barcode string
     * @return {@link ProductInfoExt} or null
     * @throws BasicException on database failure
     */
    public ProductInfoExt getProductInfoByCode(String code) throws BasicException {
        return new PreparedSentence<String, ProductInfoExt>(sessionDB,
                "SELECT "
                + "ID, REFERENCE, CODE, CODETYPE, NAME, PRICEBUY, PRICESELL, "
                + "CATEGORY, TAXCAT, ATTRIBUTESET_ID, STOCKCOST, STOCKVOLUME, "
                + "IMAGE, ISCOM, ISSCALE, ISCONSTANT, PRINTKB, SENDSTATUS, "
                + "ISSERVICE, ATTRIBUTES, DISPLAY, ISVPRICE, ISVERPATRIB, TEXTTIP, "
                + "WARRANTY, STOCKUNITS, PRINTTO, SUPPLIER, UOM, MEMODATE "
                + "FROM products WHERE CODE = ?",
                SerializerWriteString.INSTANCE,
                ProductInfoExt.getSerializerRead()).find(code);
    }

    /**
     * Retrieves product details matching the short barcode substring (characters 3 to 8).
     *
     * @param code raw barcode string
     * @return {@link ProductInfoExt} or null
     * @throws BasicException on database failure
     */
    public ProductInfoExt getProductInfoByShortCode(String code) throws BasicException {
        return new PreparedSentence<String, ProductInfoExt>(sessionDB,
                "SELECT "
                + "ID, REFERENCE, CODE, CODETYPE, NAME, PRICEBUY, PRICESELL, "
                + "CATEGORY, TAXCAT, ATTRIBUTESET_ID, STOCKCOST, STOCKVOLUME, "
                + "IMAGE, ISCOM, ISSCALE, ISCONSTANT, PRINTKB, SENDSTATUS, "
                + "ISSERVICE, ATTRIBUTES, DISPLAY, ISVPRICE, ISVERPATRIB, TEXTTIP, "
                + "WARRANTY, STOCKUNITS, PRINTTO, SUPPLIER, UOM, MEMODATE "
                + "FROM products "
                + "WHERE SUBSTRING( CODE, 3, 6 ) = ?",
                SerializerWriteString.INSTANCE,
                ProductInfoExt.getSerializerRead()).find(code.substring(2, 8));
    }

    /**
     * Retrieves product details matching the first 7 characters of a UPC-A barcode.
     *
     * @param code raw barcode string
     * @return {@link ProductInfoExt} or null
     * @throws BasicException on database failure
     */
    public ProductInfoExt getProductInfoByUShortCode(String code) throws BasicException {
        return new PreparedSentence<String, ProductInfoExt>(sessionDB,
                "SELECT "
                + "ID, REFERENCE, CODE, CODETYPE, NAME, PRICEBUY, PRICESELL, "
                + "CATEGORY, TAXCAT, ATTRIBUTESET_ID, STOCKCOST, STOCKVOLUME, "
                + "IMAGE, ISCOM, ISSCALE, ISCONSTANT, PRINTKB, SENDSTATUS, "
                + "ISSERVICE, ATTRIBUTES, DISPLAY, ISVPRICE, ISVERPATRIB, TEXTTIP, "
                + "WARRANTY, STOCKUNITS, PRINTTO, SUPPLIER, UOM, MEMODATE "
                + "FROM products "
                + "WHERE LEFT( CODE, 7 ) = ? AND CODETYPE = 'UPC-A' ",
                SerializerWriteString.INSTANCE,
                ProductInfoExt.getSerializerRead()).find(code.substring(0, 7));
    }

    /**
     * Retrieves product details by exact product reference.
     *
     * @param reference product reference code
     * @return {@link ProductInfoExt} or null
     * @throws BasicException on database failure
     */
    public ProductInfoExt getProductInfoByReference(String reference) throws BasicException {
        return new PreparedSentence<String, ProductInfoExt>(sessionDB,
                "SELECT "
                + "ID, REFERENCE, CODE, CODETYPE, NAME, PRICEBUY, PRICESELL, "
                + "CATEGORY, TAXCAT, ATTRIBUTESET_ID, STOCKCOST, STOCKVOLUME, "
                + "IMAGE, ISCOM, ISSCALE, ISCONSTANT, PRINTKB, SENDSTATUS, "
                + "ISSERVICE, ATTRIBUTES, DISPLAY, ISVPRICE, ISVERPATRIB, TEXTTIP, "
                + "WARRANTY, STOCKUNITS, PRINTTO, SUPPLIER, UOM, MEMODATE "
                + "FROM products WHERE REFERENCE = ?",
                SerializerWriteString.INSTANCE,
                ProductInfoExt.getSerializerRead()).find(reference);
    }

    /**
     * Retrieves products configured as constant items.
     *
     * @return list of constant {@link ProductInfoExt}
     * @throws BasicException on database failure
     */
    public List<ProductInfoExt> getProductConstant() throws BasicException {
        return new PreparedSentence<Void, ProductInfoExt>(sessionDB,
                "SELECT "
                + "products.ID, products.REFERENCE, products.CODE, products.CODETYPE, products.NAME, "
                + "products.PRICEBUY, products.PRICESELL, products.CATEGORY, products.TAXCAT, "
                + "products.ATTRIBUTESET_ID, products.STOCKCOST, products.STOCKVOLUME, products.IMAGE, "
                + "products.ISCOM, products.ISSCALE, products.ISCONSTANT, products.PRINTKB, "
                + "products.SENDSTATUS, products.ISSERVICE, products.ATTRIBUTES, products.DISPLAY, "
                + "products.ISVPRICE, products.ISVERPATRIB, products.TEXTTIP, products.WARRANTY, "
                + "products.STOCKUNITS, products.PRINTTO, products.SUPPLIER, products.UOM, products.MEMODATE "
                + "FROM categories INNER JOIN products ON (products.CATEGORY = categories.ID) "
                + "WHERE products.ISCONSTANT = " + sessionDB.DB.TRUE() + " "
                + "ORDER BY categories.NAME, products.NAME",
                null,
                ProductInfoExt.getSerializerRead()).list();
    }

    /**
     * Retrieves basic product identifiers and barcodes belonging to a category.
     *
     * @param categoryId category identifier
     * @return list of {@link CategoryStock}
     * @throws BasicException on database failure
     */
    public List<CategoryStock> getCategorysProductList(String categoryId) throws BasicException {
        return new PreparedSentence<String, CategoryStock>(sessionDB,
                "SELECT products.ID, products.NAME AS Name, products.CODE AS Barcode, categories.ID AS Category "
                + "FROM products products "
                + "INNER JOIN categories categories ON (products.CATEGORY = categories.ID) "
                + "WHERE products.category = ? "
                + "ORDER BY products.NAME ASC",
                SerializerWriteString.INSTANCE,
                CategoryStock.getSerializerRead()).list(categoryId);
    }

    /**
     * Retrieves catalog products for a given category.
     *
     * @param category category identifier
     * @return list of {@link ProductInfoExt}
     * @throws BasicException on database failure
     */
    public List<ProductInfoExt> getProductCatalog(String category) throws BasicException {
        return new PreparedSentence<String, ProductInfoExt>(sessionDB,
                "SELECT "
                + "P.ID, P.REFERENCE, P.CODE, P.CODETYPE, P.NAME, P.PRICEBUY, P.PRICESELL, "
                + "P.CATEGORY, P.TAXCAT, P.ATTRIBUTESET_ID, P.STOCKCOST, P.STOCKVOLUME, "
                + "P.IMAGE, P.ISCOM, P.ISSCALE, P.ISCONSTANT, P.PRINTKB, P.SENDSTATUS, "
                + "P.ISSERVICE, P.ATTRIBUTES, P.DISPLAY, P.ISVPRICE, P.ISVERPATRIB, P.TEXTTIP, "
                + "P.WARRANTY, P.STOCKUNITS, P.PRINTTO, P.SUPPLIER, P.UOM, P.MEMODATE "
                + "FROM products P, products_cat O "
                + "WHERE P.ID = O.PRODUCT AND P.CATEGORY = ? "
                + "ORDER BY O.CATORDER, P.NAME ",
                SerializerWriteString.INSTANCE,
                ProductInfoExt.getSerializerRead()).list(category);
    }

    /**
     * Retrieves composite child products associated with a parent composite product.
     *
     * @param id parent product ID
     * @return list of composite child {@link ProductInfoExt}
     * @throws BasicException on database failure
     */
    public List<ProductInfoExt> getProductComposite(String id) throws BasicException {
        return new PreparedSentence<String, ProductInfoExt>(sessionDB,
                "SELECT "
                + "P.ID, P.REFERENCE, P.CODE, P.CODETYPE, P.NAME, P.PRICEBUY, P.PRICESELL, "
                + "P.CATEGORY, P.TAXCAT, P.ATTRIBUTESET_ID, P.STOCKCOST, P.STOCKVOLUME, "
                + "P.IMAGE, P.ISCOM, P.ISSCALE, P.ISCONSTANT, P.PRINTKB, P.SENDSTATUS, "
                + "P.ISSERVICE, P.ATTRIBUTES, P.DISPLAY, P.ISVPRICE, P.ISVERPATRIB, P.TEXTTIP, "
                + "P.WARRANTY, P.STOCKUNITS, P.PRINTTO, P.SUPPLIER, P.UOM, P.MEMODATE "
                + "FROM products P, products_cat O, products_com M "
                + "WHERE P.ID = O.PRODUCT AND P.ID = M.PRODUCT2 AND M.PRODUCT = ? "
                + "AND P.ISCOM = " + sessionDB.DB.TRUE() + " "
                + "ORDER BY O.CATORDER, P.NAME",
                SerializerWriteString.INSTANCE,
                ProductInfoExt.getSerializerRead()).list(id);
    }

    /**
     * Returns sentence query for products matching QBF filters.
     *
     * @return {@link SentenceList} of {@link ProductInfoExt}
     */
    public SentenceList<ProductInfoExt> getProductList() {
        return new StaticSentence(sessionDB,
                new QBFBuilder(
                        "SELECT "
                        + "ID, REFERENCE, CODE, CODETYPE, NAME, PRICEBUY, PRICESELL, "
                        + "CATEGORY, TAXCAT, ATTRIBUTESET_ID, STOCKCOST, STOCKVOLUME, "
                        + "IMAGE, ISCOM, ISSCALE, ISCONSTANT, PRINTKB, SENDSTATUS, "
                        + "ISSERVICE, ATTRIBUTES, DISPLAY, ISVPRICE, ISVERPATRIB, TEXTTIP, "
                        + "WARRANTY, STOCKUNITS, PRINTTO, SUPPLIER, UOM, MEMODATE "
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
     * Returns sentence query for normal (non-composite) products matching QBF filters.
     *
     * @return {@link SentenceList} of {@link ProductInfoExt}
     */
    public SentenceList<ProductInfoExt> getProductListNormal() {
        return new StaticSentence(sessionDB,
                new QBFBuilder(
                        "SELECT "
                        + "ID, REFERENCE, CODE, CODETYPE, NAME, PRICEBUY, PRICESELL, "
                        + "CATEGORY, TAXCAT, ATTRIBUTESET_ID, STOCKCOST, STOCKVOLUME, "
                        + "IMAGE, ISCOM, ISSCALE, ISCONSTANT, PRINTKB, SENDSTATUS, "
                        + "ISSERVICE, ATTRIBUTES, DISPLAY, ISVPRICE, ISVERPATRIB, TEXTTIP, "
                        + "WARRANTY, STOCKUNITS, PRINTTO, SUPPLIER, UOM, MEMODATE "
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
     * Returns sentence query for all products ordered by name.
     *
     * @return {@link SentenceList} of {@link ProductInfo}
     */
    public SentenceList<ProductInfo> getProductsList() {
        return new StaticSentence(sessionDB,
                "SELECT "
                + "ID, REFERENCE, CODE, CODETYPE, NAME, PRICEBUY, PRICESELL, "
                + "CATEGORY, TAXCAT, ATTRIBUTESET_ID, STOCKCOST, STOCKVOLUME, "
                + "IMAGE, ISCOM, ISSCALE, ISCONSTANT, PRINTKB, SENDSTATUS, "
                + "ISSERVICE, ATTRIBUTES, DISPLAY, ISVPRICE, ISVERPATRIB, TEXTTIP, "
                + "WARRANTY, STOCKUNITS, PRINTTO, SUPPLIER, UOM, MEMODATE "
                + "FROM products "
                + "ORDER BY NAME",
                ProductInfo.getSerializerRead());
    }

    /**
     * Returns total product count.
     *
     * @return product count
     * @throws BasicException on database failure
     */
    public int getProductsCount() throws BasicException {
        Object result = new StaticSentence(sessionDB,
                "SELECT COUNT(*) as count FROM products",
                com.openbravo.data.loader.SerializerReadInteger.INSTANCE)
                .find();
        return result == null ? 0 : ((Number) result).intValue();
    }

    /**
     * Returns sentence query for product stock summary listing with taxes and locations.
     *
     * @return {@link SentenceList} of {@link ProductInfoExtA}
     */
    public SentenceList<ProductInfoExtA> getProductList2() {
        return new StaticSentence(sessionDB,
                new QBFBuilder(
                        "SELECT "
                        + "products.id, products.name, stockcurrent.units, locations.name, "
                        + "products.pricesell, taxes.rate, "
                        + "products.pricesell + (products.pricesell * taxes.rate) AS SellIncTax "
                        + "products.category"
                        + "products.ISCOM"
                        + "products.ISSCALE"
                        + "products.ISCONSTANT"
                        + "products.ISSERVICE"
                        + " FROM (((stockcurrent stockcurrent "
                        + "INNER JOIN locations locations ON (stockcurrent.location = locations.id)) "
                        + "INNER JOIN products products ON (stockcurrent.product = products.id)) "
                        + "INNER JOIN taxcategories taxcategories ON (products.taxcat = taxcategories.id)) "
                        + "INNER JOIN taxes taxes ON (taxes.category = taxcategories.id) "
                        + "WHERE ?(QBF_FILTER) "
                        + "GROUP BY products.name ",
                        new String[]{"NAME", "UNITS", "SellIncTax", "LOCATION"}),
                new SerializerWriteBasic(new Datas[]{
                    Datas.OBJECT, Datas.STRING,
                    Datas.OBJECT, Datas.DOUBLE,
                    Datas.OBJECT, Datas.DOUBLE,
                    Datas.OBJECT, Datas.STRING}),
                ProductInfoExtA.getSerializerRead());
    }

    /**
     * Returns sentence query for auxiliary (composite modifier) products matching QBF filters.
     *
     * @return {@link SentenceList} of {@link ProductInfoExt}
     */
    public SentenceList<ProductInfoExt> getProductListAuxiliar() {
        return new StaticSentence(sessionDB,
                new QBFBuilder(
                        "SELECT "
                        + "ID, REFERENCE, CODE, CODETYPE, NAME, PRICEBUY, PRICESELL, "
                        + "CATEGORY, TAXCAT, ATTRIBUTESET_ID, STOCKCOST, STOCKVOLUME, "
                        + "IMAGE, ISCOM, ISSCALE, ISCONSTANT, PRINTKB, SENDSTATUS, "
                        + "ISSERVICE, ATTRIBUTES, DISPLAY, ISVPRICE, ISVERPATRIB, TEXTTIP, "
                        + "WARRANTY, STOCKUNITS, PRINTTO, SUPPLIER, UOM, MEMODATE "
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

    /**
     * Static helper for retrieving bundled products for a given parent product.
     *
     * @param productId parent product ID
     * @param sessionDB active database session
     * @return list of {@link ProductsBundleInfo}
     * @throws BasicException on database failure
     */
    public static List<ProductsBundleInfo> getProductsBundle(String productId, Session sessionDB) throws BasicException {
        return new PreparedSentence(sessionDB,
                "SELECT ID, PRODUCT, PRODUCT_BUNDLE, QUANTITY "
                + "FROM products_bundle WHERE PRODUCT = ?",
                SerializerWriteString.INSTANCE,
                ProductsBundleInfo.getSerializerRead()).list(productId);
    }

    // =========================================================================
    // Attribute Set Operations
    // =========================================================================

    /**
     * Returns sentence query for all attribute sets ordered by name.
     *
     * @return {@link SentenceList} of {@link AttributeSetInfo}
     */
    public SentenceList<AttributeSetInfo> getAttributeSetList() {
        return new StaticSentence<>(sessionDB,
                "SELECT ID, NAME FROM attributeset ORDER BY NAME",
                null,
                (DataRead dr) -> new AttributeSetInfo(dr.getString(1), dr.getString(2)));
    }

    /**
     * Retrieves all attribute sets as an in-memory list.
     *
     * @return list of {@link AttributeSetInfo}
     */
    public List<AttributeSetInfo> getAttributeSetListAll() {
        List<AttributeSetInfo> list = null;
        try {
            list = this.getAttributeSetList().list();
        } catch (BasicException ex) {
            LOGGER.log(Level.WARNING, "Cannot get AttributeSetInfo list", ex);
        }
        return list;
    }

    // =========================================================================
    // Product Image & Catalog Operations
    // =========================================================================

    /**
     * Returns sentence finder for product images by product ID.
     *
     * @return {@link SentenceFind} returning {@link BufferedImage}
     */
    public SentenceFind getProductImage() {
        return new PreparedSentence(sessionDB,
                "SELECT IMAGE FROM products WHERE ID = ?",
                SerializerWriteString.INSTANCE,
                (DataRead dr) -> ImageUtils.readImage(dr.getBytes(1)));
    }

    /**
     * Retrieves the image of a product by identifier.
     *
     * @param imageId product ID
     * @return {@link BufferedImage} or null
     */
    public BufferedImage getProductImage(String imageId) {
        try {
            return (BufferedImage) getProductImage().find(imageId);
        } catch (BasicException e) {
            return null;
        }
    }

    /**
     * Adds all products under a specific category to the sales touch catalog.
     *
     * @param categoryId category identifier
     * @return number of affected rows
     * @throws BasicException on database failure
     */
    public int addProductsToCatalogWithCategoryId(String categoryId) throws BasicException {
        StaticSentence sentence = new StaticSentence(sessionDB,
                "INSERT INTO products_cat(PRODUCT, CATORDER) SELECT ID, " + sessionDB.DB.INTEGER_NULL()
                + " FROM products WHERE CATEGORY = ?",
                SerializerWriteString.INSTANCE);
        return sentence.exec(categoryId);
    }

    /**
     * Removes all products under a specific category from the sales touch catalog.
     *
     * @param categoryId category identifier
     * @return number of affected rows
     * @throws BasicException on database failure
     */
    public int removeProductsFromCatalogWithCategoryId(String categoryId) throws BasicException {
        StaticSentence sentence = new StaticSentence(sessionDB,
                "DELETE FROM products_cat WHERE PRODUCT IN (SELECT ID FROM products WHERE CATEGORY = ?)",
                SerializerWriteString.INSTANCE);
        return sentence.exec(categoryId);
    }
}
