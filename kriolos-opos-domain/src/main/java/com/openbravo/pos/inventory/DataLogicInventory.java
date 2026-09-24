package com.openbravo.pos.inventory;

import com.openbravo.pos.forms.BeanFactoryDataSingle;
import com.openbravo.data.loader.Session;
import com.openbravo.data.loader.SentenceExec;
import com.openbravo.data.loader.SentenceList;
import com.openbravo.data.loader.SerializerRead;
import com.openbravo.data.loader.SerializerWriteBasicExt;
import com.openbravo.data.loader.SerializerWriteBasic;
import com.openbravo.data.loader.PreparedSentence;
import com.openbravo.data.loader.Datas;
import com.openbravo.basic.BasicException;

public class DataLogicInventory extends BeanFactoryDataSingle {
    
    protected Session sessionDB;

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
}
