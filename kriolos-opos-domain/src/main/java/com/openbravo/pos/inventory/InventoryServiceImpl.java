package com.openbravo.pos.inventory;

import com.openbravo.basic.BasicException;
import com.openbravo.pos.forms.DataLogicSales;
import com.openbravo.data.loader.Datas;
import com.openbravo.data.loader.PreparedSentence;
import com.openbravo.data.loader.SerializerWriteBasicExt;
import com.openbravo.data.loader.Session;

/**
 * Implementation of InventoryService.
 */
public class InventoryServiceImpl implements InventoryService {

    private final DataLogicSales dlSales;
    private final Session session;

    public InventoryServiceImpl(DataLogicSales dlSales, Session session) {
        this.dlSales = dlSales;
        this.session = session;
    }

    @Override
    public ProductStock getStock(String productId, String locationId) throws BasicException {
        if (productId == null || locationId == null) {
            return null;
        }
        return dlSales.getProductStockState(productId, locationId);
    }

    @Override
    public void createStock(String locationId, String productId, double units) throws BasicException {
        Object[] values = new Object[3];
        values[0] = locationId;
        values[1] = productId;
        values[2] = units;

        PreparedSentence sentence = new PreparedSentence(session,
                "INSERT INTO stockcurrent (LOCATION, PRODUCT, UNITS) VALUES (?, ?, ?)",
                new SerializerWriteBasicExt(new Datas[] { Datas.STRING, Datas.STRING, Datas.DOUBLE },
                        new int[] { 0, 1, 2 }));

        sentence.exec(values);
    }

    @Override
    public void updateStock(String locationId, String productId, double units) throws BasicException {
        Object[] newValues = new Object[3];
        newValues[0] = units;
        newValues[1] = locationId;
        newValues[2] = productId;

        PreparedSentence sentence = new PreparedSentence(session,
                "UPDATE stockcurrent SET UNITS = ? WHERE LOCATION = ? AND PRODUCT = ?",
                new SerializerWriteBasicExt(new Datas[] { Datas.DOUBLE, Datas.STRING, Datas.STRING },
                        new int[] { 0, 1, 2 }));

        sentence.exec(newValues);
    }
}
