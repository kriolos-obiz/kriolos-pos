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
import java.util.Objects;

/**
 * Immutable command record for deleting or reverting a cash drawer treasury movement.
 *
 * @param receiptId unique receipt identifier (non-null, non-blank)
 * @param paymentId unique payment identifier (non-null, non-blank)
 */
public record DeletePaymentMovementCommand(
        String receiptId,
        String paymentId
) implements Serializable {

    public DeletePaymentMovementCommand {
        Objects.requireNonNull(receiptId, "receiptId cannot be null");
        if (receiptId.isBlank()) {
            throw new IllegalArgumentException("receiptId cannot be blank");
        }
        Objects.requireNonNull(paymentId, "paymentId cannot be null");
        if (paymentId.isBlank()) {
            throw new IllegalArgumentException("paymentId cannot be blank");
        }

        receiptId = receiptId.trim();
        paymentId = paymentId.trim();
    }

    /**
     * Creates a {@link DeletePaymentMovementCommand}.
     *
     * @param receiptId unique receipt identifier
     * @param paymentId unique payment identifier
     * @return validated DeletePaymentMovementCommand
     */
    public static DeletePaymentMovementCommand of(String receiptId, String paymentId) {
        return new DeletePaymentMovementCommand(receiptId, paymentId);
    }
}
