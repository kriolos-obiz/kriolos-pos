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
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.panels.JPanelTable;
import javax.swing.ListCellRenderer;

/**
 *
 * @author Jack Gerrard
 */
public class SuppliersPanel extends JPanelTable {

    private static final long serialVersionUID = 1L;
    
    private TableDefinition tsuppliers;
    private SuppliersView jeditor;
    
    /** Creates a new instance of SuppliersPanel */
    public SuppliersPanel() {}
    
    /**
     *
     */
    @Override
    protected void init() {        
        SupplierService dlSuppliers = app.getBean(SupplierService.class);
        tsuppliers = dlSuppliers.getTableSuppliers();        
        jeditor = new SuppliersView(app, dirty);    
        
    }
    
    /**
     *
     * @throws BasicException
     */
    @Override
    public void activate() throws BasicException { 
        jeditor.activate();         
        super.activate();
    }
    
    /**
     *
     * @return
     */
    @Override
    public ListProvider getListProvider() {
        return new ListProviderCreator(tsuppliers);
    }
    
    /**
     *
     * @return
     */
    @Override
    public DefaultSaveProvider getSaveProvider() {
        return new DefaultSaveProvider(tsuppliers, new int[] {
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 
            11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21});      
    }
    
    /**
     *
     * @return
     */
    @Override
    public Vectorer getVectorer() {
        return tsuppliers.getVectorerBasic(new int[]{1, 2, 3, 4});
    }
    
    /**
     *
     * @return
     */
    @Override
    public ComparatorCreator getComparatorCreator() {
        return tsuppliers.getComparatorCreator(new int[] {1, 2, 3, 4});
    }
    
    /**
     *
     * @return
     */
    @Override
    public ListCellRenderer getListCellRenderer() {
        return new ListCellRendererBasic(tsuppliers.getRenderStringBasic(new int[]{3}));
    }
    
    /**
     *
     * @return
     */
    @Override
    public EditorRecord getEditor() {
        return jeditor;
    }

    /**
     *
     * @return
     */
    @Override
    public String getTitle() {
        return AppLocal.getIntString("Menu.SuppliersManagement");
    }    
}