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

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.TableDefinition;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Domain service port interface for voucher issuance, validation, lookup, and redemption.
 */
public interface VoucherService {

    /**
     * Retrieves all active vouchers (STATUS = 'A') ordered by voucher number ascending.
     *
     * @return list of active vouchers
     * @throws BasicException if database access fails
     */
    List<VoucherInfo> getVoucherList() throws BasicException;

    /**
     * Finds an active voucher by its unique identifier.
     *
     * @param id the voucher identifier
     * @return the voucher details or {@code null} if not found or inactive
     * @throws BasicException if database access fails
     */
    VoucherInfo getVoucher(String id) throws BasicException;

    /**
     * Finds a voucher by its unique identifier regardless of status.
     *
     * @param id the voucher identifier
     * @return the voucher details or {@code null} if not found
     * @throws BasicException if database access fails
     */
    VoucherInfo getVoucherAll(String id) throws BasicException;

    /**
     * Finds the last sequence suffix number for vouchers matching the given prefix.
     *
     * @param prefix the prefix formatted as "VO-MM-yy" (e.g. "VO-10-26")
     * @return the last assigned sequence suffix or {@code null}
     * @throws BasicException if database access fails
     */
    String findLastVoucherNumber(String prefix) throws BasicException;

    /**
     * Generates the next sequential voucher number for the current month and year.
     * Format: "VO-{MM-yy}-{SEQ}" (e.g. "VO-10-26-00001").
     *
     * @return generated voucher number
     * @throws BasicException if database access fails
     */
    default String generateNextVoucherNumber() throws BasicException {
        DateFormat df = new SimpleDateFormat("MM-yy");
        String prefix = "VO-" + df.format(new Date());
        String lastNumber = findLastVoucherNumber(prefix);
        int newNumber = 1;
        if (lastNumber != null && !lastNumber.trim().isEmpty()) {
            try {
                newNumber = Integer.parseInt(lastNumber.trim()) + 1;
            } catch (NumberFormatException ignored) {
            }
        }
        return prefix + "-" + String.format("%05d", newNumber);
    }

    /**
     * Deactivates (marks as redeemed / 'D') a voucher by its voucher number.
     *
     * @param voucherNumber the voucher number to deactivate
     * @return number of records affected
     * @throws BasicException if database access fails
     */
    int deactivateVoucher(String voucherNumber) throws BasicException;

    /**
     * Returns the table definition metadata for the vouchers entity.
     *
     * @return table definition for vouchers
     */
    TableDefinition getTableVouchers();
}
