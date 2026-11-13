package com.openbravo.pos.inventory;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.DataWrite;
import com.openbravo.data.loader.IKeyed;
import com.openbravo.data.loader.SerializerWrite;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 * Domain record representing an entry in the Stock Diary ledger.
 *
 * @param id                        unique movement identifier
 * @param date                      timestamp of movement
 * @param reason                    movement reason code (e.g. IN_PURCHASE, OUT_SALE, OUT_BREAK)
 * @param locationId                warehouse location identifier
 * @param productId                 product identifier
 * @param attributeSetInstanceId    attribute set instance identifier (optional)
 * @param units                     quantity units moved (signed)
 * @param price                     unit price or cost of the movement
 * @param appUser                   user who performed or authorized the movement
 * @param productRef                product reference/SKU
 * @param productCode               product barcode/code
 * @param productName               product display name
 * @param attributeSetId            product attribute set ID
 * @param attributeSetInstanceDesc  attribute set instance description
 * @param supplierId                supplier identifier (optional)
 * @author KriolOS
 */
public record StockDiaryInfo(
        String id,
        Date date,
        int reason,
        String locationId,
        String productId,
        String attributeSetInstanceId,
        double units,
        double price,
        String appUser,
        String productRef,
        String productCode,
        String productName,
        String attributeSetId,
        String attributeSetInstanceDesc,
        String supplierId
) implements IKeyed<String>, Serializable {

    public StockDiaryInfo {
        if (id == null || id.isBlank()) {
            id = UUID.randomUUID().toString();
        }
        if (date == null) {
            date = new Date();
        }
        if (locationId == null) {
            locationId = "";
        }
        if (productId == null) {
            productId = "";
        }
        if (appUser == null) {
            appUser = "";
        }
        if (productRef == null) {
            productRef = "";
        }
        if (productCode == null) {
            productCode = "";
        }
        if (productName == null) {
            productName = "";
        }
        if (attributeSetId == null) {
            attributeSetId = "";
        }
        if (attributeSetInstanceDesc == null) {
            attributeSetInstanceDesc = "";
        }
    }

    /**
     * Simplified constructor for inventory ledger movements without catalog metadata.
     *
     * @param id                     unique movement ID
     * @param date                   movement timestamp
     * @param reason                 movement reason code
     * @param locationId             warehouse location ID
     * @param productId              product ID
     * @param attributeSetInstanceId attribute set instance ID
     * @param units                  quantity units
     * @param price                  unit price
     * @param appUser                user identifier
     */
    public StockDiaryInfo(
            String id,
            Date date,
            int reason,
            String locationId,
            String productId,
            String attributeSetInstanceId,
            double units,
            double price,
            String appUser
    ) {
        this(id, date, reason, locationId, productId, attributeSetInstanceId, units, price, appUser, "", "", "", "", "", null);
    }

    @Override
    public String getKey() {
        return id;
    }

    /**
     * Serializer for inserting a stockdiary table row.
     *
     * @return SerializerWrite for stockdiary INSERT
     */
    public static SerializerWrite<StockDiaryInfo> getSerializerInsert() {
        return (DataWrite dp, StockDiaryInfo diary) -> {
            dp.setString(1, diary.id());
            dp.setTimestamp(2, diary.date());
            dp.setInt(3, diary.reason());
            dp.setString(4, diary.locationId());
            dp.setString(5, diary.productId());
            dp.setString(6, diary.attributeSetInstanceId());
            dp.setDouble(7, diary.units());
            dp.setDouble(8, diary.price());
            dp.setString(9, diary.appUser());
        };
    }

    /**
     * Serializer for deleting a stockdiary table row by ID.
     *
     * @return SerializerWrite for stockdiary DELETE
     */
    public static SerializerWrite<StockDiaryInfo> getSerializerDelete() {
        return (DataWrite dp, StockDiaryInfo diary) -> {
            dp.setString(1, diary.id());
        };
    }
}
