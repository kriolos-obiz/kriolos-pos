package com.openbravo.pos.inventory;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.Datas;
import com.openbravo.data.loader.PreparedSentence;
import com.openbravo.data.loader.SentenceExec;
import com.openbravo.data.loader.SentenceExecTransaction;
import com.openbravo.data.loader.SentenceList;
import com.openbravo.data.loader.SerializerRead;
import com.openbravo.data.loader.SerializerReadDouble;
import com.openbravo.data.loader.SerializerWriteBasic;
import com.openbravo.data.loader.SerializerWriteBasicExt;
import com.openbravo.data.loader.SerializerWriteString;
import com.openbravo.data.loader.Session;
import com.openbravo.pos.forms.BeanFactoryDataSingle;
import com.openbravo.pos.pim.DataLogicPIM;
import com.openbravo.format.Formats;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.data.loader.TableDefinition;
import com.openbravo.data.loader.StaticSentence;
import com.openbravo.data.loader.SerializerReadClass;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DataLogicInventory extends BeanFactoryDataSingle {
    
    private static final Logger LOGGER = Logger.getLogger(DataLogicInventory.class.getName());
    protected Session sessionDB;

    public static final Datas[] STOCK_DIARY_DATAS = new Datas[]{
        Datas.STRING, Datas.TIMESTAMP, Datas.INT, Datas.STRING, Datas.STRING,
        Datas.STRING, Datas.DOUBLE, Datas.DOUBLE, Datas.STRING, Datas.STRING, Datas.STRING
    };

    public static final Datas[] STOCK_ADJUST_DATAS = new Datas[]{
        Datas.STRING, Datas.STRING, Datas.STRING, Datas.DOUBLE
    };

    public static final String SQL_WAREHOUSE_STOCK_LIST = """
        SELECT 
            L.ID, 
            P.ID, 
            P.REFERENCE, 
            P.NAME,
            L.STOCKSECURITY, 
            L.STOCKMAXIMUM, 
            COALESCE(S.SUMUNITS, 0) 
        FROM products P 
        LEFT OUTER JOIN (
            SELECT ID, PRODUCT, LOCATION, STOCKSECURITY, STOCKMAXIMUM 
            FROM stocklevel 
            WHERE LOCATION = ?
        ) L ON P.ID = L.PRODUCT 
        LEFT OUTER JOIN (
            SELECT PRODUCT, SUM(UNITS) AS SUMUNITS 
            FROM stockcurrent 
            WHERE LOCATION = ? 
            GROUP BY PRODUCT
        ) S ON P.ID = S.PRODUCT 
        ORDER BY P.NAME
        """;

    public static final String SQL_STOCKLEVEL_INSERT = "INSERT INTO stocklevel (ID, LOCATION, PRODUCT, STOCKSECURITY, STOCKMAXIMUM) VALUES (?, ?, ?, ?, ?)";
    public static final String SQL_STOCKLEVEL_UPDATE = "UPDATE stocklevel SET STOCKSECURITY = ?, STOCKMAXIMUM = ? WHERE ID = ?";

    @Override
    public void init(Session s) {
        sessionDB = s;
    }

    public final SentenceList getWarehouseStockList(SerializerRead sr) {
        return new PreparedSentence(sessionDB,
                SQL_WAREHOUSE_STOCK_LIST,
                new SerializerWriteBasicExt(new Datas[] { Datas.OBJECT, Datas.STRING }, new int[] { 1, 1 }),
                sr);
    }

    public final SentenceExec getStockLevelInsert() {
        return new PreparedSentence(sessionDB, SQL_STOCKLEVEL_INSERT,
                new SerializerWriteBasic(new Datas[] {Datas.STRING, Datas.STRING, Datas.STRING, Datas.DOUBLE, Datas.DOUBLE}));
    }

    public final SentenceExec getStockLevelUpdate() {
        return new PreparedSentence(sessionDB, SQL_STOCKLEVEL_UPDATE,
                new SerializerWriteBasic(new Datas[] {Datas.DOUBLE, Datas.DOUBLE, Datas.STRING}));
    }

    public final SentenceExec getStockCurrentInsert() {
        return new PreparedSentence(sessionDB,
                "INSERT INTO stockcurrent ( LOCATION, PRODUCT, UNITS) VALUES (?, ?, ?)",
                new SerializerWriteBasic(new Datas[]{Datas.STRING, Datas.STRING, Datas.DOUBLE}));
    }

    public final SentenceList<LocationInfo> getLocationsList() {
        return new StaticSentence(sessionDB,
                "SELECT ID, "
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
            LOGGER.log(Level.WARNING, "Cannot get LocationInfo list", ex);
        }
        return list;
    }

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

        return (ProductStock) preparedSentence.find(pId, location);
    }

    public final List<ProductStock> getProductStockList(String pId) throws BasicException {
        String SQL_STOCK = """
                                SELECT
                                    P.ID AS product_id,
                                    L.name AS location_name,
                                    COALESCE(MAX(SC.units), 0) AS current_stock,
                                    MAX(SL.stocksecurity) AS minimum_stock,
                                    MAX(SL.stockmaximum) AS maximum_stock,
                                    ROUND(P.pricebuy, 2) AS price_buy,
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

    public final SentenceExec getStockDiaryInsert() {
        return new SentenceExecTransaction(sessionDB) {
            @Override
            public int execInTransaction(Object[] params) throws BasicException {
                Object[] adjustParams = new Object[4];
                Object[] paramsArray = (Object[]) params;
                adjustParams[0] = paramsArray[4];
                adjustParams[1] = paramsArray[3];
                adjustParams[2] = paramsArray[5];
                adjustParams[3] = paramsArray[6];
                adjustStock(adjustParams);

                return new PreparedSentence(sessionDB,
                        "INSERT INTO stockdiary (ID, DATENEW, REASON, LOCATION, "
                        + "PRODUCT, ATTRIBUTESETINSTANCE_ID, "
                        + "UNITS, PRICE, AppUser) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        new SerializerWriteBasicExt(STOCK_DIARY_DATAS,
                                new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8}))
                        .exec(params);
            }
        };
    }

    public final SentenceExec getStockDiaryInsert1() {
        return new SentenceExecTransaction(sessionDB) {
            @Override
            public int execInTransaction(Object[] params) throws BasicException {
                int updateresult = params[5] == null
                        ? new PreparedSentence(sessionDB,
                                "UPDATE stockcurrent SET UNITS = (UNITS + ?) "
                                + "WHERE LOCATION = ? AND PRODUCT = ? "
                                + "AND ATTRIBUTESETINSTANCE_ID IS NULL",
                                new SerializerWriteBasicExt(STOCK_DIARY_DATAS,
                                        new int[]{6, 3, 4}))
                                .exec(params)
                        : new PreparedSentence(sessionDB,
                                "UPDATE stockcurrent SET UNITS = (UNITS + ?) "
                                + "WHERE LOCATION = ? AND PRODUCT = ? "
                                + "AND ATTRIBUTESETINSTANCE_ID = ?",
                                new SerializerWriteBasicExt(STOCK_DIARY_DATAS,
                                        new int[]{6, 3, 4, 5}))
                                .exec(params);

                if (updateresult == 0) {
                    new PreparedSentence(sessionDB,
                            "INSERT INTO stockcurrent (LOCATION, PRODUCT, "
                            + "ATTRIBUTESETINSTANCE_ID, UNITS) "
                            + "VALUES (?, ?, ?, ?)",
                            new SerializerWriteBasicExt(STOCK_DIARY_DATAS,
                                    new int[]{3, 4, 5, 6}))
                            .exec(params);
                }
                return new PreparedSentence(sessionDB,
                        "INSERT INTO stockdiary (ID, DATENEW, REASON, LOCATION, PRODUCT, "
                        + "ATTRIBUTESETINSTANCE_ID, UNITS, PRICE, AppUser, "
                        + "SUPPLIER, SUPPLIERDOC) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        new SerializerWriteBasicExt(STOCK_DIARY_DATAS,
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

    public final SentenceExec getStockDiaryDelete() {
        return new SentenceExecTransaction(sessionDB) {
            @Override
            public int execInTransaction(Object[] params) throws BasicException {
                int updateresult = ((Object[]) params)[5] == null
                        ? new PreparedSentence(sessionDB,
                                "UPDATE stockcurrent SET UNITS = (UNITS - ?) "
                                + "WHERE LOCATION = ? AND PRODUCT = ? "
                                + "AND ATTRIBUTESETINSTANCE_ID IS NULL",
                                new SerializerWriteBasicExt(STOCK_DIARY_DATAS,
                                        new int[]{6, 3, 4}))
                                .exec(params)
                        : new PreparedSentence(sessionDB,
                                "UPDATE stockcurrent SET UNITS = (UNITS - ?) "
                                + "WHERE LOCATION = ? AND PRODUCT = ? "
                                + "AND ATTRIBUTESETINSTANCE_ID = ?",
                                new SerializerWriteBasicExt(STOCK_DIARY_DATAS,
                                        new int[]{6, 3, 4, 5}))
                                .exec(params);

                if (updateresult == 0) {
                    new PreparedSentence(sessionDB,
                            "INSERT INTO stockcurrent (LOCATION, PRODUCT, "
                            + "ATTRIBUTESETINSTANCE_ID, UNITS) "
                            + "VALUES (?, ?, ?, -(?))",
                            new SerializerWriteBasicExt(STOCK_DIARY_DATAS,
                                    new int[]{3, 4, 5, 6}))
                            .exec(params);
                }
                return new PreparedSentence(sessionDB,
                        "DELETE FROM stockdiary WHERE ID = ?",
                        new SerializerWriteBasicExt(STOCK_DIARY_DATAS, new int[]{0}))
                        .exec(params);
            }
        };
    }

    public void adjustStock(Object[] params) throws BasicException {
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
                            new SerializerWriteBasicExt(STOCK_ADJUST_DATAS,
                                    new int[]{3, 1, 0}))
                            .exec(params)
                    : new PreparedSentence(sessionDB,
                            "UPDATE stockcurrent SET UNITS = (UNITS + ?) "
                            + "WHERE LOCATION = ? AND PRODUCT = ? "
                            + "AND ATTRIBUTESETINSTANCE_ID = ?",
                            new SerializerWriteBasicExt(STOCK_ADJUST_DATAS,
                                    new int[]{3, 1, 0, 2}))
                            .exec(params);

            if (updateresult == 0) {
                new PreparedSentence(sessionDB,
                        "INSERT INTO stockcurrent (LOCATION, PRODUCT, "
                        + "ATTRIBUTESETINSTANCE_ID, UNITS) "
                        + "VALUES (?, ?, ?, ?)",
                        new SerializerWriteBasicExt(STOCK_ADJUST_DATAS,
                                new int[]{1, 0, 2, 3}))
                        .exec(params);
            }
        }
    }

    private List<ProductsBundleInfo> getProductsBundle(String productId) throws BasicException {
        return DataLogicPIM.getProductsBundle(productId, sessionDB);
    }

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
}
