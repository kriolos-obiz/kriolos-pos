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
) {
    public PaymentMovement {
        if (date == null) {
            date = new Date();
        }
        if (notes == null) {
            notes = "";
        }
    }
}
