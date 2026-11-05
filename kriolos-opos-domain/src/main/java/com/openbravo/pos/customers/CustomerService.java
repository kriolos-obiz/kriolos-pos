//    KriolOS POS
//    Copyright (c) 2019-2026 KriolOS
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program.  If not, see <http://www.gnu.org/licenses/>.

package com.openbravo.pos.customers;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.SentenceList;
import com.openbravo.data.loader.TableDefinition;
import com.openbravo.data.model.Row;
import com.openbravo.data.user.EditorCreator;
import com.openbravo.data.user.ListProvider;
import com.openbravo.data.user.SaveProvider;
import java.util.Date;
import java.util.List;

/**
 * Service port interface for Customer profiles, lookups, debts, and transaction ledgers.
 */
public interface CustomerService {

    CustomerInfo getCustomerInfo(String id) throws BasicException;

    CustomerInfoExt findCustomerInfoExtById(String id) throws BasicException;

    CustomerInfoExt findCustomerInfoExtByCard(String card) throws BasicException;

    CustomerInfoExt findCustomerInfoExtByName(String name) throws BasicException;

    CustomerInfoExt findCustomerInfoExtBySearchKey(String searchKey) throws BasicException;

    CustomerInfoExt findCustomerInfoExtByTaxId(String taxId) throws BasicException;

    int updateCustomerExt(CustomerInfoExt customer) throws BasicException;

    /**
     * Updates customer accumulated debt and timestamp using a typed command record.
     *
     * @param command customer debt update details
     * @return number of affected rows
     * @throws BasicException if a database access error occurs
     */
    int updateCustomerDebt(CustomerDebtUpdateCommand command) throws BasicException;

    /**
     * Updates customer debt using individual parameters.
     *
     * @param customerId unique identifier of the customer
     * @param accDebt    new accumulated debt balance
     * @param date       update timestamp
     * @return number of affected rows
     * @throws BasicException if a database access error occurs
     * @deprecated Use {@link #updateCustomerDebt(CustomerDebtUpdateCommand)} instead.
     */
    @Deprecated(since = "1.0.0")
    default int updateCustomerDebt(String customerId, Double accDebt, Date date) throws BasicException {
        return updateCustomerDebt(new CustomerDebtUpdateCommand(customerId, accDebt, date));
    }

    /**
     * Retrieves all active customers in the system.
     *
     * @return list of active customer profiles
     * @throws BasicException if a database access error occurs
     */
    List<CustomerInfo> findAllCustomers() throws BasicException;

    List<CustomerTransaction> getCustomersTransactionList(String customerId) throws BasicException;

    /**
     * @deprecated Direct access to persistence TableDefinition leaks data loader internals into the domain.
     */
    @Deprecated(since = "1.0.0")
    TableDefinition getTableCustomers();

    /**
     * @deprecated Direct access to Row metadata leaks data loader internals into the domain.
     */
    @Deprecated(since = "1.0.0")
    Row getCustomersRow();

    /**
     * @deprecated Direct access to SaveProvider leaks data loader internals into the domain.
     */
    @Deprecated(since = "1.0.0")
    SaveProvider getCustomerSaveProvider();

    /**
     * @deprecated Use {@link #findAllCustomers()} or dedicated finder queries instead of raw SentenceList.
     */
    @Deprecated(since = "1.0.0")
    SentenceList<CustomerInfo> getCustomerList();

    /**
     * @deprecated ListProvider couples the UI and data layer directly. Use domain-level filtered queries.
     */
    @Deprecated(since = "1.0.0")
    ListProvider<CustomerInfo> getCustomerListProvider(EditorCreator filter);
}
