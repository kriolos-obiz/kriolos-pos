//    KriolOS POS
//    Copyright (c) 2019-2023 KriolOS
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
import com.openbravo.data.gui.ListCellRendererBasic;
import com.openbravo.data.loader.ComparatorCreator;
import com.openbravo.data.loader.Vectorer;
import com.openbravo.data.user.EditorRecord;
import com.openbravo.data.user.ListProvider;
import com.openbravo.data.user.ListProviderCreator;
import com.openbravo.data.user.SaveProvider;
import com.openbravo.basic.AppLocal;
import com.openbravo.pos.panels.JPanelTable;
import javax.swing.ListCellRenderer;

/**
 * Type-safe JPanelTable implementation for Customer master management.
 *
 * @author adrianromero
 * @author KriolOS
 */
public class CustomersPanel extends JPanelTable {

    private static final long serialVersionUID = 1L;
    
    private CustomerService customerService;
    private CustomersView jeditor;
    
    public CustomersPanel() {}

    @Override
    protected void init() {
        this.customerService = app.getBean(CustomerService.class);
        this.jeditor = new CustomersView(app, dirty);
    }

    @Override
    public void activate() throws BasicException {     
        super.activate();
        jeditor.activate();     
    }

    @Override
    public ListProvider<CustomerInfoExt> getListProvider() {
        return new ListProviderCreator<>(customerService.getTableCustomers());
    }

    @Override
    public SaveProvider<CustomerInfoExt> getSaveProvider() {
        return customerService.getCustomerSaveProvider();
    }

    @Override
    public Vectorer<CustomerInfoExt> getVectorer() {
        return new Vectorer<>() {
            @Override
            public String[] getHeaders() throws BasicException {
                return new String[]{
                    AppLocal.getIntString("label.searchkey"),
                    AppLocal.getIntString("label.taxid"),
                    AppLocal.getIntString("label.name")
                };
            }

            @Override
            public String[] getValues(CustomerInfoExt c) throws BasicException {
                return new String[]{
                    c != null && c.getSearchkey() != null ? c.getSearchkey() : "",
                    c != null && c.getTaxid() != null ? c.getTaxid() : "",
                    c != null && c.getName() != null ? c.getName() : ""
                };
            }
        };
    }

    @Override
    public ComparatorCreator getComparatorCreator() {
        return customerService.getTableCustomers().getComparatorCreator(new int[] {1, 2, 3});
    }

    @Override
    public ListCellRenderer<?> getListCellRenderer() {
        return new ListCellRendererBasic(v -> v instanceof CustomerInfoExt c ? c.getName() : (v != null ? v.toString() : ""));
    }

    @Override
    public EditorRecord<CustomerInfoExt> getEditor() {
        return jeditor;
    }

    @Override
    public String getTitle() {
        return AppLocal.getIntString("Menu.CustomersManagement");
    }    
}
