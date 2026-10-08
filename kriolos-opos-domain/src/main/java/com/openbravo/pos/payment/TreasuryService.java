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
import com.openbravo.data.user.SaveProvider;
import java.util.Date;

/**
 * Domain service port interface for treasury, cash drawer movements, and payment sequence numbering.
 */
public interface TreasuryService {

    /**
     * Records a cash drawer movement (cash in / cash out) using domain record {@link PaymentMovement}.
     *
     * @param movement the payment movement domain record
     * @throws BasicException on persistence error
     */
    default void recordPaymentMovement(PaymentMovement movement) throws BasicException {
        if (movement != null) {
            recordPaymentMovement(
                    movement.receiptId(),
                    movement.activeCashIndex(),
                    movement.date(),
                    movement.paymentId(),
                    movement.reason(),
                    movement.total(),
                    movement.notes()
            );
        }
    }

    /**
     * Records a cash drawer movement with individual fields.
     *
     * @param receiptId       unique identifier for receipt
     * @param activeCashIndex active cash/session index (MONEY field)
     * @param date            timestamp
     * @param paymentId       unique identifier for payment
     * @param reason          reason or type (e.g. "cashin", "cashout")
     * @param total           amount moved (signed)
     * @param notes           optional notes
     * @throws BasicException on persistence error
     */
    void recordPaymentMovement(
            String receiptId,
            String activeCashIndex,
            Date date,
            String paymentId,
            String reason,
            double total,
            String notes
    ) throws BasicException;

    /**
     * Deletes or reverts a cash drawer movement by its receipt and payment identifiers.
     *
     * @param receiptId unique receipt identifier
     * @param paymentId unique payment identifier
     * @throws BasicException on persistence error
     */
    void deletePaymentMovement(String receiptId, String paymentId) throws BasicException;

    /**
     * Returns the SaveProvider for table/panel persistence in cash drawer movements.
     *
     * @return SaveProvider for cash drawer movements
     */
    SaveProvider getPaymentMovementSaveProvider();

    /**
     * Generates and retrieves the next ticket payment index sequence number.
     *
     * @return next sequence integer
     * @throws BasicException on sequence generation error
     */
    Integer getNextTicketPaymentIndex() throws BasicException;
}
