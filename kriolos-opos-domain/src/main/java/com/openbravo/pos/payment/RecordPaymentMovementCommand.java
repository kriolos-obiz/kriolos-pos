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

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * Immutable command record for recording a cash drawer treasury movement (cash in / cash out).
 *
 * @param receiptId       unique receipt identifier (non-null, non-blank)
 * @param activeCashIndex active cash/session sequence identifier (non-null, non-blank)
 * @param date            timestamp of movement (defensively copied, defaults to current time if null)
 * @param paymentId       unique payment identifier (non-null, non-blank)
 * @param reason          movement reason or type (e.g. "cashin", "cashout", non-null, non-blank)
 * @param total           amount moved (finite signed number)
 * @param notes           optional notes or remarks (normalized to non-null string)
 */
public record RecordPaymentMovementCommand(
        String receiptId,
        String activeCashIndex,
        Date date,
        String paymentId,
        String reason,
        double total,
        String notes
) implements Serializable {

    public RecordPaymentMovementCommand {
        Objects.requireNonNull(receiptId, "receiptId cannot be null");
        if (receiptId.isBlank()) {
            throw new IllegalArgumentException("receiptId cannot be blank");
        }
        Objects.requireNonNull(activeCashIndex, "activeCashIndex cannot be null");
        if (activeCashIndex.isBlank()) {
            throw new IllegalArgumentException("activeCashIndex cannot be blank");
        }
        Objects.requireNonNull(paymentId, "paymentId cannot be null");
        if (paymentId.isBlank()) {
            throw new IllegalArgumentException("paymentId cannot be blank");
        }
        Objects.requireNonNull(reason, "reason cannot be null");
        if (reason.isBlank()) {
            throw new IllegalArgumentException("reason cannot be blank");
        }
        if (!Double.isFinite(total)) {
            throw new IllegalArgumentException("total must be a finite number");
        }

        receiptId = receiptId.trim();
        activeCashIndex = activeCashIndex.trim();
        paymentId = paymentId.trim();
        reason = reason.trim();
        date = date != null ? new Date(date.getTime()) : new Date();
        notes = notes != null ? notes.trim() : "";
    }

    @Override
    public Date date() {
        return new Date(date.getTime());
    }

    /**
     * Creates a {@link RecordPaymentMovementCommand} with all properties.
     *
     * @param receiptId       receipt identifier
     * @param activeCashIndex active cash session identifier
     * @param date            movement timestamp
     * @param paymentId       payment identifier
     * @param reason          movement reason
     * @param total           amount moved
     * @param notes           notes
     * @return validated RecordPaymentMovementCommand
     */
    public static RecordPaymentMovementCommand of(
            String receiptId,
            String activeCashIndex,
            Date date,
            String paymentId,
            String reason,
            double total,
            String notes
    ) {
        return new RecordPaymentMovementCommand(receiptId, activeCashIndex, date, paymentId, reason, total, notes);
    }

    /**
     * Creates a {@link RecordPaymentMovementCommand} defaulting to current timestamp.
     *
     * @param receiptId       receipt identifier
     * @param activeCashIndex active cash session identifier
     * @param paymentId       payment identifier
     * @param reason          movement reason
     * @param total           amount moved
     * @param notes           notes
     * @return validated RecordPaymentMovementCommand
     */
    public static RecordPaymentMovementCommand of(
            String receiptId,
            String activeCashIndex,
            String paymentId,
            String reason,
            double total,
            String notes
    ) {
        return new RecordPaymentMovementCommand(receiptId, activeCashIndex, new Date(), paymentId, reason, total, notes);
    }

    /**
     * Converts a {@link PaymentMovement} domain record to a {@link RecordPaymentMovementCommand}.
     *
     * @param movement existing domain record
     * @return validated RecordPaymentMovementCommand
     */
    public static RecordPaymentMovementCommand from(PaymentMovement movement) {
        Objects.requireNonNull(movement, "movement cannot be null");
        return new RecordPaymentMovementCommand(
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
