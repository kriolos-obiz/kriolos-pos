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

import com.openbravo.basic.BasicException;

/**
 * Service port interface for CSV and batch data import operations.
 * Decouples import operations from direct DataLogic implementations.
 */
public interface ImportService {

    /**
     * Inserts an entry into the csvimport table for stock quantity update.
     *
     * @param csv array containing [id, rowNumber, csvError, location, code, units]
     * @throws BasicException on database or execution error
     */
    void execCSVStockUpdate(Object[] csv) throws BasicException;

    /**
     * Inserts an entry into the csvimport table for product creation or update.
     *
     * @param csv array containing CSV entry fields
     * @throws BasicException on database or execution error
     */
    void execAddCSVEntry(Object[] csv) throws BasicException;

    /**
     * Inserts an entry into the csvimport table for customer creation or update.
     *
     * @param csv array containing [id, rowNumber, csvError, searchKey, name]
     * @throws BasicException on database or execution error
     */
    void execCustomerAddCSVEntry(Object[] csv) throws BasicException;

    /**
     * Identifies the product record type / match status based on product fields.
     *
     * @param myProduct array containing [reference, barcode, name]
     * @return product ID if exact match, or change description ("Name change", "Barcode change", "Reference change", duplicate notice, or "new")
     * @throws BasicException on database or execution error
     */
    String getProductRecordType(Object[] myProduct) throws BasicException;

    /**
     * Type-safe convenience method to identify the product record type.
     *
     * @param reference product reference
     * @param code product barcode / code
     * @param name product name
     * @return product ID or change status / "new"
     * @throws BasicException on database error
     */
    default String getProductRecordType(String reference, String code, String name) throws BasicException {
        return getProductRecordType(new Object[]{reference, code, name});
    }

    /**
     * Identifies the customer record type / match status based on searchKey and name.
     *
     * @param myCustomer array containing [searchKey, name]
     * @return customer ID if exact match, or duplicate description, or "new"
     * @throws BasicException on database or execution error
     */
    String getCustomerRecordType(Object[] myCustomer) throws BasicException;

    /**
     * Type-safe convenience method to identify the customer record type.
     *
     * @param searchKey customer search key
     * @param name customer name
     * @return customer ID or error/duplicate status / "new"
     * @throws BasicException on database error
     */
    default String getCustomerRecordType(String searchKey, String name) throws BasicException {
        return getCustomerRecordType(new Object[]{searchKey, name});
    }
}
