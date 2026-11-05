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
 * Immutable command to deactivate or redeem a voucher by its sequence number.
 *
 * @param voucherNumber the voucher sequence number to mark as redeemed
 */
public record DeactivateVoucherCommand(String voucherNumber) {

    /**
     * Compact constructor validating business invariants.
     */
    public DeactivateVoucherCommand {
        Objects.requireNonNull(voucherNumber, "Voucher number must not be null");
        if (voucherNumber.isBlank()) {
            throw new IllegalArgumentException("Voucher number must not be blank");
        }
    }

    /**
     * Static factory method to create a {@link DeactivateVoucherCommand}.
     *
     * @param voucherNumber the voucher sequence number
     * @return a new valid {@link DeactivateVoucherCommand} instance
     */
    public static DeactivateVoucherCommand of(String voucherNumber) {
        return new DeactivateVoucherCommand(voucherNumber);
    }
}

