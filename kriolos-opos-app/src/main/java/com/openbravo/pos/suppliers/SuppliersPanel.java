//    KriolOS POS
//    Copyright (c) 2019-2023 KriolOS
//    
//
//     
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

package com.openbravo.pos.suppliers;

import com.openbravo.basic.BasicException;
import com.openbravo.data.gui.ListCellRendererBasic;
import com.openbravo.data.loader.ComparatorCreator;
import com.openbravo.data.loader.TableDefinition;
import com.openbravo.data.loader.Vectorer;
import com.openbravo.data.user.EditorRecord;
import com.openbravo.data.user.ListProvider;
import com.openbravo.data.user.ListProviderCreator;
import com.openbravo.data.user.DefaultSaveProvider;
import com.openbravo.format.Formats;
import com.openbravo.basic.AppLocal;
import com.openbravo.pos.panels.JPanelTable;
import javax.swing.ListCellRenderer;

/**
 * Type-safe JPanelTable implementation for Supplier master data management.
 *
 * @author Jack Gerrard
 * @author KriolOS
 */
public class SuppliersPanel extends JPanelTable {

    private static final long serialVersionUID = 1L;
    
    private TableDefinition<SupplierInfo> tsuppliers;
    private SuppliersView jeditor;
    
    /** Creates a new instance of SuppliersPanel */
    public SuppliersPanel() {}
    
    @Override
    protected void init() {        
        SupplierService dlSuppliers = app.getBean(SupplierService.class);
        tsuppliers = dlSuppliers.getTableSuppliers();        
        jeditor = new SuppliersView(app, dirty);    
    }
    
    @Override
    public void activate() throws BasicException { 
        jeditor.activate();         
        super.activate();
    }
    
    @Override
    public ListProvider<SupplierInfo> getListProvider() {
        return new ListProviderCreator<>(tsuppliers);
    }
    
    @Override
    public DefaultSaveProvider<SupplierInfo> getSaveProvider() {
        return new DefaultSaveProvider<>(tsuppliers);      
    }
    
    @Override
    public Vectorer<SupplierInfo> getVectorer() {
        return new Vectorer<>() {
            @Override
            public String[] getHeaders() throws BasicException {
                return new String[]{
                    AppLocal.getIntString("label.searchkey"),
                    AppLocal.getIntString("label.taxid"),
                    AppLocal.getIntString("label.name"),
                    AppLocal.getIntString("label.maxdebt")
                };
            }

            @Override
            public String[] getValues(SupplierInfo obj) throws BasicException {
                if (obj == null) {
                    return new String[]{"", "", "", ""};
                }
                return new String[]{
                    Formats.STRING.formatValue(obj.getSearchkey()),
                    Formats.STRING.formatValue(obj.getTaxid()),
                    Formats.STRING.formatValue(obj.getName()),
                    Formats.CURRENCY.formatValue(obj.getMaxdebt())
                };
            }
        };
    }
    
    @Override
    public ComparatorCreator getComparatorCreator() {
        return tsuppliers.getComparatorCreator(new int[]{1, 2, 3, 4});
    }
    
    @Override
    public ListCellRenderer<?> getListCellRenderer() {
        return new ListCellRendererBasic(v -> v instanceof SupplierInfo s ? s.getName() : (v != null ? v.toString() : ""));
    }
    
    @Override
    public EditorRecord<SupplierInfo> getEditor() {
        return jeditor;
    }

    @Override
    public String getTitle() {
        return AppLocal.getIntString("Menu.SuppliersManagement");
    }    
}
