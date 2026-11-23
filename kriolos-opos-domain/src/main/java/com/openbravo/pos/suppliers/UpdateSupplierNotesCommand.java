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
package com.openbravo.pos.suppliers;

import java.util.Objects;

/**
 * Immutable command to update notes for a supplier.
 *
 * @param supplierId unique supplier identifier
 * @param notes additional notes or annotations for the supplier
 * @author KriolOS
 */
public record UpdateSupplierNotesCommand(String supplierId, String notes) {

    public UpdateSupplierNotesCommand {
        Objects.requireNonNull(supplierId, "supplierId must not be null");
        if (supplierId.isBlank()) {
            throw new IllegalArgumentException("supplierId must not be blank");
        }
        supplierId = supplierId.trim();
        notes = (notes == null) ? "" : notes;
    }

    public static UpdateSupplierNotesCommand of(String supplierId, String notes) {
        return new UpdateSupplierNotesCommand(supplierId, notes);
    }
}
