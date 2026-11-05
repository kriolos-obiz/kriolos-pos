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
package com.openbravo.pos.imports;

/**
 * Values recorded for a stock-quantity CSV import row.
 *
 * @param id import row identifier
 * @param rowNumber source file row number
 * @param csvError import status or error description
 * @param locationId inventory location identifier
 * @param barcode product barcode from the source row
 * @param quantity imported quantity
 * @author KriolOS
 */
public record StockQuantityImportEntry(
        String id,
        String rowNumber,
        String csvError,
        String locationId,
        String barcode,
        double quantity) {
}