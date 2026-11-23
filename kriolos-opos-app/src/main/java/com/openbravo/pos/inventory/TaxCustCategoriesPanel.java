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

package com.openbravo.pos.inventory;

import com.openbravo.basic.AppLocal;
import com.openbravo.basic.BasicException;
import com.openbravo.data.gui.ListCellRendererBasic;
import com.openbravo.data.loader.ComparatorCreator;
import com.openbravo.data.loader.TableDefinition;
import com.openbravo.data.loader.Vectorer;
import com.openbravo.data.user.DefaultSaveProvider;
import com.openbravo.data.user.EditorRecord;
import com.openbravo.data.user.ListProvider;
import com.openbravo.data.user.ListProviderCreator;
import com.openbravo.pos.panels.JPanelTable;
import com.openbravo.pos.sales.TaxService;
import javax.swing.ListCellRenderer;

/**
 * Strongly typed maintenance panel for Customer Tax Categories.
 *
 * @author adrianromero
 * @author KriolOS
 */
public class TaxCustCategoriesPanel extends JPanelTable {

    private static final long serialVersionUID = 1L;

    private TableDefinition<TaxCustCategoryInfo> ttaxcategories;
    private TaxCustCategoriesEditor jeditor;
    
    public TaxCustCategoriesPanel() {
    }
    
    @Override
    protected void init() {
        TaxService taxService = app.getBean(TaxService.class);        
        ttaxcategories = taxService.getTableTaxCustCategories();
        jeditor = new TaxCustCategoriesEditor(dirty);
    }
    
    @Override
    public ListProvider<TaxCustCategoryInfo> getListProvider() {
        return new ListProviderCreator<>(ttaxcategories);
    }
    
    @Override
    public DefaultSaveProvider<TaxCustCategoryInfo> getSaveProvider() {
        return new DefaultSaveProvider<>(ttaxcategories);      
    }
    
    @Override
    public Vectorer<TaxCustCategoryInfo> getVectorer() {
        return new Vectorer<>() {
            @Override
            public String[] getHeaders() throws BasicException {
                return new String[]{AppLocal.getIntString("label.name")};
            }

            @Override
            public String[] getValues(TaxCustCategoryInfo obj) throws BasicException {
                return new String[]{obj != null ? obj.getName() : ""};
            }
        };
    }
    
    @Override
    public ComparatorCreator getComparatorCreator() {
        return ttaxcategories.getComparatorCreator(new int[]{1});
    }
    
    @Override
    public ListCellRenderer<?> getListCellRenderer() {
        return new ListCellRendererBasic(v -> v instanceof TaxCustCategoryInfo c ? c.getName() : (v != null ? v.toString() : ""));
    }
    
    @Override
    public EditorRecord<TaxCustCategoryInfo> getEditor() {
        return jeditor;
    }
        
    @Override
    public String getTitle() {
        return AppLocal.getIntString("Menu.TaxCustCategories");
    }     
}
