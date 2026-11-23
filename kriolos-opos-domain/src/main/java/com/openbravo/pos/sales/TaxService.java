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

package com.openbravo.pos.sales;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.SentenceList;
import com.openbravo.data.loader.TableDefinition;
import com.openbravo.data.user.SaveProvider;
import com.openbravo.pos.inventory.TaxCategoryInfo;
import com.openbravo.pos.inventory.TaxCustCategoryInfo;
import com.openbravo.pos.ticket.TaxInfo;
import java.util.List;

/**
 * Service port interface for Tax definitions, categories, and rates.
 */
public interface TaxService {

    SentenceList<TaxInfo> getTaxList();

    List<TaxInfo> getTaxListAll();

    SentenceList<TaxCustCategoryInfo> getTaxCustCategoriesList();

    List<TaxCustCategoryInfo> getTaxCustCategoriesListAll();

    SentenceList<TaxCategoryInfo> getTaxCategoriesList();

    List<TaxCategoryInfo> getTaxCategoriesListAll();

    SentenceList<TaxCategoryInfo> getTaxCategoryInfoList();

    /**
     * Creates a product tax category from a typed command.
     *
     * @param command create tax category command
     * @throws BasicException on persistence error
     */
    void createTaxCategory(CreateTaxCategoryCommand command) throws BasicException;

    /**
     * Creates a customer tax category from a typed command.
     *
     * @param command create customer tax category command
     * @throws BasicException on persistence error
     */
    void createTaxCustCategory(CreateTaxCustCategoryCommand command) throws BasicException;

    /**
     * Creates a tax definition from a typed command.
     *
     * @param command create tax command
     * @throws BasicException on persistence error
     */
    void createTax(CreateTaxCommand command) throws BasicException;

    TableDefinition<TaxInfo> getTableTaxes();

    default SaveProvider<TaxInfo> getTaxSaveProvider() {
        return null;
    }

    TableDefinition<TaxCustCategoryInfo> getTableTaxCustCategories();

    TableDefinition<TaxCategoryInfo> getTableTaxCategories();
}

