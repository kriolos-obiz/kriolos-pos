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
package com.openbravo.pos.payment;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.DataWrite;
import com.openbravo.data.loader.IKeyed;
import com.openbravo.data.loader.SerializerWrite;
import java.io.Serializable;
import java.util.Date;

/**
 * Domain record representing a cash drawer treasury movement (cash in / cash out).
 *
 * @param receiptId       unique receipt identifier
 * @param activeCashIndex active cash/session sequence identifier (MONEY field)
 * @param date            timestamp of movement
 * @param paymentId       unique payment identifier
 * @param reason          movement reason/type (e.g. "cashin", "cashout")
 * @param total           amount moved (signed)
 * @param notes           optional notes or remarks
 */
public record PaymentMovement(
        String receiptId,
        String activeCashIndex,
        Date date,
        String paymentId,
        String reason,
        double total,
        String notes
) implements IKeyed<String>, Serializable {

    public PaymentMovement {
        if (date == null) {
            date = new Date();
        }
        if (notes == null) {
            notes = "";
        }
    }

    @Override
    public String getKey() {
        return paymentId;
    }

    /**
     * Serializer for inserting the receipt record component of a cash movement.
     *
     * @return SerializerWrite for receipts INSERT statement
     */
    public static SerializerWrite<PaymentMovement> getSerializerReceiptInsert() {
        return (DataWrite dp, PaymentMovement movement) -> {
            dp.setString(1, movement.receiptId());
            dp.setString(2, movement.activeCashIndex());
            dp.setTimestamp(3, movement.date());
        };
    }

    /**
     * Serializer for inserting the payment record component of a cash movement.
     *
     * @return SerializerWrite for payments INSERT statement
     */
    public static SerializerWrite<PaymentMovement> getSerializerPaymentInsert() {
        return (DataWrite dp, PaymentMovement movement) -> {
            dp.setString(1, movement.paymentId());
            dp.setString(2, movement.receiptId());
            dp.setString(3, movement.reason());
            dp.setDouble(4, movement.total());
            dp.setString(5, movement.notes());
        };
    }

    /**
     * Serializer for deleting the payment record by its unique payment ID.
     *
     * @return SerializerWrite for payments DELETE statement
     */
    public static SerializerWrite<PaymentMovement> getSerializerPaymentDelete() {
        return (DataWrite dp, PaymentMovement movement) -> {
            dp.setString(1, movement.paymentId());
        };
    }

    /**
     * Serializer for deleting the receipt record by its unique receipt ID.
     *
     * @return SerializerWrite for receipts DELETE statement
     */
    public static SerializerWrite<PaymentMovement> getSerializerReceiptDelete() {
        return (DataWrite dp, PaymentMovement movement) -> {
            dp.setString(1, movement.receiptId());
        };
    }
}
