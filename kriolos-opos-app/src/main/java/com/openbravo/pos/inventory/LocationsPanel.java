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

import com.openbravo.basic.BasicException;
import com.openbravo.data.gui.ListCellRendererBasic;
import com.openbravo.data.loader.ComparatorCreator;
import com.openbravo.data.loader.TableDefinition;
import com.openbravo.data.loader.Vectorer;
import com.openbravo.data.user.EditorRecord;
import com.openbravo.data.user.ListProvider;
import com.openbravo.data.user.ListProviderCreator;
import com.openbravo.data.user.SaveProvider;
import com.openbravo.data.user.DefaultSaveProvider;
import com.openbravo.basic.AppLocal;
import com.openbravo.pos.panels.JPanelTable;
import javax.swing.ListCellRenderer;

/**
 *
 * @author adrianromero
 * @author KriolOS
 */
public class LocationsPanel extends JPanelTable {
    
    private TableDefinition<LocationInfo> tlocations;
    private LocationsView jeditor;
    
    /** Creates a new instance of LocationsPanel */
    public LocationsPanel() {
    }
    
    /**
     *
     */
    @Override
    protected void init() {   
        InventoryService inventoryService = app.getBean(InventoryService.class);          
        tlocations = inventoryService.getTableLocations();
        jeditor = new LocationsView(dirty);
    }
    
    /**
     *
     * @return
     */
    @Override
    public ListProvider<LocationInfo> getListProvider() {
        return new ListProviderCreator<>(tlocations);
    }
    
    /**
     *
     * @return
     */
    @Override
    public SaveProvider<LocationInfo> getSaveProvider() {
        return new DefaultSaveProvider<>(tlocations);        
    }
    
    /**
     *
     * @return
     */
    @Override
    public Vectorer<LocationInfo> getVectorer() {
        return new Vectorer<>() {
            @Override
            public String[] getHeaders() throws BasicException {
                return new String[]{
                    AppLocal.getIntString("label.locationname"),
                    AppLocal.getIntString("label.locationaddress")
                };
            }

            @Override
            public String[] getValues(LocationInfo loc) throws BasicException {
                return new String[]{
                    loc != null && loc.getName() != null ? loc.getName() : "",
                    loc != null && loc.getAddress() != null ? loc.getAddress() : ""
                };
            }
        };
    }
    
    /**
     *
     * @return
     */
    @Override
    public ComparatorCreator getComparatorCreator() {
        return tlocations.getComparatorCreator(new int[] {1, 2});
    }
    
    /**
     *
     * @return
     */
    @Override
    public ListCellRenderer<?> getListCellRenderer() {
        return new ListCellRendererBasic(v -> v instanceof LocationInfo loc ? loc.getName() : (v != null ? v.toString() : ""));
    }
    
    /**
     *
     * @return
     */
    @Override
    public EditorRecord<LocationInfo> getEditor() {
        return jeditor;
    }
    
    /**
     *
     * @return
     */
    @Override
    public String getTitle() {
        return AppLocal.getIntString("Menu.Locations");
    }      
}
