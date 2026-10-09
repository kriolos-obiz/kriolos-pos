/*
 * Copyright (C) 2026 KriolOS POS
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
package com.openbravo.pos.inventory;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.Datas;
import com.openbravo.data.loader.PreparedSentence;
import com.openbravo.data.loader.SentenceList;
import com.openbravo.data.loader.SerializerRead;
import com.openbravo.data.loader.SerializerWriteBasic;
import com.openbravo.data.loader.SerializerWriteBasicExt;
import com.openbravo.data.loader.Session;
import com.openbravo.data.loader.TableDefinition;
import com.openbravo.data.user.EditorCreator;
import com.openbravo.data.user.ListProvider;
import com.openbravo.data.user.SaveProvider;
import com.openbravo.pos.forms.DataLogicSales;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of InventoryService.
 */
public class InventoryServiceImpl implements InventoryService {

    private final DataLogicInventory dlInventory;
    private final Session session;

    public InventoryServiceImpl(DataLogicInventory dlInventory, Session session) {
        this.dlInventory = dlInventory;
        this.session = session;
    }

    /**
     * @deprecated Use {@link #InventoryServiceImpl(DataLogicInventory, Session)} instead.
     */
    @Deprecated
    public InventoryServiceImpl(DataLogicSales dlSales, Session session) {
        this(dlSales != null ? dlSales.getDataLogicInventory() : null, session);
    }

    @Override
    public ProductStock getStock(String productId, String locationId) throws BasicException {
        if (productId == null || locationId == null || dlInventory == null) {
            return null;
        }
        return dlInventory.getProductStockState(productId, locationId);
    }

    @Override
    public void addStockEntry(StockEntry entry) throws BasicException {
        if (dlInventory != null) {
            dlInventory.addStockEntry(entry);
            return;
        }
        if (entry != null) {
            addStockEntry(entry.locationId(), entry.productId(), entry.attributeSetInstanceId(), entry.units());
        }
    }

    @Override
    public void addStockEntry(String locationId, String productId, double units) throws BasicException {
        addStockEntry(locationId, productId, null, units);
    }

    @Override
    public void addStockEntry(String locationId, String productId, String attributeSetInstanceId, double units) throws BasicException {
        if (dlInventory != null) {
            dlInventory.addStockEntry(locationId, productId, attributeSetInstanceId, units);
            return;
        }
        if (attributeSetInstanceId == null) {
            createStock(locationId, productId, units);
        } else {
            new PreparedSentence(session,
                    "INSERT INTO stockcurrent (LOCATION, PRODUCT, ATTRIBUTESETINSTANCE_ID, UNITS) VALUES (?, ?, ?, ?)",
                    new SerializerWriteBasic(new Datas[]{Datas.STRING, Datas.STRING, Datas.STRING, Datas.DOUBLE}))
                    .exec(new Object[]{locationId, productId, attributeSetInstanceId, units});
        }
    }

    @Override
    public void createStock(String locationId, String productId, double units) throws BasicException {
        if (dlInventory != null) {
            dlInventory.createStock(locationId, productId, units);
            return;
        }
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
        if (dlInventory != null) {
            dlInventory.updateStock(locationId, productId, units);
            return;
        }
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

    @Override
    public TableDefinition getTableLocations() {
        return dlInventory != null ? dlInventory.getTableLocations() : null;
    }

    @Override
    public List<LocationInfo> getLocationsList() throws BasicException {
        return dlInventory != null ? dlInventory.getLocationsList() : Collections.emptyList();
    }

    @Override
    public List<LocationInfo> getLocationsListAll() {
        return dlInventory != null ? dlInventory.getLocationsListAll() : Collections.emptyList();
    }

    @Override
    public String findLocationName(String iLocation) throws BasicException {
        return dlInventory != null ? dlInventory.findLocationName(iLocation) : null;
    }

    @Override
    public ListProvider getWarehouseStockListProvider(EditorCreator filter) {
        return dlInventory != null ? dlInventory.getWarehouseStockListProvider(filter) : null;
    }

    @Override
    public SentenceList getWarehouseStockList(SerializerRead sr) {
        return dlInventory != null ? dlInventory.getWarehouseStockList(sr) : null;
    }

    @Override
    public void insertStockLevel(String id, String locationId, String productId, Double stockSecurity, Double stockMaximum) throws BasicException {
        if (dlInventory != null) {
            dlInventory.insertStockLevel(id, locationId, productId, stockSecurity, stockMaximum);
        }
    }

    @Override
    public void updateStockLevel(String id, Double stockSecurity, Double stockMaximum) throws BasicException {
        if (dlInventory != null) {
            dlInventory.updateStockLevel(id, stockSecurity, stockMaximum);
        }
    }

    @Override
    public SaveProvider getWarehouseStockSaveProvider() {
        return dlInventory != null ? dlInventory.getWarehouseStockSaveProvider() : null;
    }
}
