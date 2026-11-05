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
package com.openbravo.pos.voucher;

import java.util.Objects;

/**
 * Immutable command to issue or create a customer voucher.
 *
 * @param id            unique identifier for the voucher entity
 * @param voucherNumber user-visible voucher sequence number (e.g., "VO-11-26-00001")
 * @param customerId    associated customer identifier
 * @param amount        monetary value of the voucher (must be &gt; 0.0)
 * @param status        voucher status flag ("A" for Active, "D" for Deactivated / Redeemed)
 */
public record CreateVoucherCommand(
        String id,
        String voucherNumber,
        String customerId,
        double amount,
        String status
) {

    /**
     * Compact constructor validating business invariants.
     */
    public CreateVoucherCommand {
        Objects.requireNonNull(id, "Voucher ID must not be null");
        if (id.isBlank()) {
            throw new IllegalArgumentException("Voucher ID must not be blank");
        }
        Objects.requireNonNull(voucherNumber, "Voucher number must not be null");
        if (voucherNumber.isBlank()) {
            throw new IllegalArgumentException("Voucher number must not be blank");
        }
        Objects.requireNonNull(customerId, "Customer ID must not be null");
        if (customerId.isBlank()) {
            throw new IllegalArgumentException("Customer ID must not be blank");
        }
        if (amount <= 0.0) {
            throw new IllegalArgumentException("Voucher amount must be positive, got: " + amount);
        }
        if (status == null || status.isBlank()) {
            status = "A";
        }
    }

    /**
     * Factory method creating an active voucher command.
     *
     * @param id            voucher identifier
     * @param voucherNumber sequence voucher number
     * @param customerId    customer identifier
     * @param amount        voucher amount (&gt; 0.0)
     * @return a new valid {@link CreateVoucherCommand}
     */
    public static CreateVoucherCommand of(String id, String voucherNumber, String customerId, double amount) {
        return new CreateVoucherCommand(id, voucherNumber, customerId, amount, "A");
    }
}

