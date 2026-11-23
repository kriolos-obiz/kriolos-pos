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
package com.openbravo.pos.sales;

import com.openbravo.pos.ticket.TaxInfo;
import java.util.Objects;

/**
 * Immutable command to create a tax definition.
 *
 * @param id unique identifier of the tax
 * @param name display name of the tax
 * @param taxCategoryId target product tax category identifier
 * @param taxCustCategoryId target customer tax category identifier (optional)
 * @param parentId parent tax identifier for compound rates (optional)
 * @param rate tax percentage rate (0.0 to 1.0 or higher)
 * @param cascade whether tax applies cascade calculation
 * @param order display or calculation sequence order
 * @author KriolOS
 */
public record CreateTaxCommand(
        String id,
        String name,
        String taxCategoryId,
        String taxCustCategoryId,
        String parentId,
        double rate,
        boolean cascade,
        Integer order
) {

    public CreateTaxCommand {
        Objects.requireNonNull(id, "id must not be null");
        if (id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
        Objects.requireNonNull(name, "name must not be null");
        if (name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        Objects.requireNonNull(taxCategoryId, "taxCategoryId must not be null");
        if (taxCategoryId.isBlank()) {
            throw new IllegalArgumentException("taxCategoryId must not be blank");
        }
        if (!Double.isFinite(rate) || rate < 0.0) {
            throw new IllegalArgumentException("rate must be a non-negative finite number: " + rate);
        }

        id = id.trim();
        name = name.trim();
        taxCategoryId = taxCategoryId.trim();
        taxCustCategoryId = (taxCustCategoryId != null && !taxCustCategoryId.isBlank()) ? taxCustCategoryId.trim() : null;
        parentId = (parentId != null && !parentId.isBlank()) ? parentId.trim() : null;
        order = (order != null) ? order : 0;
    }

    public static CreateTaxCommand of(
            String id,
            String name,
            String taxCategoryId,
            String taxCustCategoryId,
            String parentId,
            double rate,
            boolean cascade,
            Integer order
    ) {
        return new CreateTaxCommand(id, name, taxCategoryId, taxCustCategoryId, parentId, rate, cascade, order);
    }

    public static CreateTaxCommand from(TaxInfo taxInfo) {
        Objects.requireNonNull(taxInfo, "taxInfo must not be null");
        return new CreateTaxCommand(
                taxInfo.getId(),
                taxInfo.getName(),
                taxInfo.getTaxCategoryID(),
                taxInfo.getTaxCustCategoryID(),
                taxInfo.getParentID(),
                taxInfo.getRate(),
                taxInfo.isCascade(),
                taxInfo.getOrder()
        );
    }
}

