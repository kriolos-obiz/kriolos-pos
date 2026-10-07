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

    int updateCustomerExt(CustomerInfoExt customer) throws BasicException;

    int updateCustomerDebt(String customerId, Double accDebt, Date date) throws BasicException;

    List<CustomerTransaction> getCustomersTransactionList(String customerId) throws BasicException;
}
