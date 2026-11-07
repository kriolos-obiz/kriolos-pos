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

package com.openbravo.pos.sales.restaurant;

import com.openbravo.basic.AppLocal;
import com.openbravo.basic.BasicException;
import com.openbravo.data.gui.ListCellRendererBasic;
import com.openbravo.data.loader.TableDefinition;
import com.openbravo.data.loader.Vectorer;
import com.openbravo.data.user.DefaultSaveProvider;
import com.openbravo.data.user.EditorRecord;
import com.openbravo.data.user.ListProvider;
import com.openbravo.data.user.ListProviderCreator;
import com.openbravo.pos.panels.JPanelTable;
import javax.swing.ListCellRenderer;

/**
 * Strongly typed maintenance panel for restaurant floors.
 *
 * @author adrianromero
 * @author KriolOS
 */
public class JPanelFloors extends JPanelTable {
    
    private static final long serialVersionUID = 1L;

    private TableDefinition<FloorsInfo> tfloors;
    private FloorsEditor jeditor;
    
    public JPanelFloors() {
    }
    
    @Override
    protected void init() {
        RestaurantService restaurantService = app.getBean(RestaurantService.class);
        tfloors = restaurantService.getTableFloors();
        jeditor = new FloorsEditor(dirty); 
    }
    
    @Override
    public ListProvider<FloorsInfo> getListProvider() {
        return new ListProviderCreator<>(tfloors);
    }
    
    @Override
    public Vectorer<FloorsInfo> getVectorer() {
        return new Vectorer<>() {
            @Override
            public String[] getHeaders() throws BasicException {
                return new String[]{AppLocal.getIntString("label.name")};
            }

            @Override
            public String[] getValues(FloorsInfo obj) throws BasicException {
                return new String[]{obj != null ? obj.getName() : ""};
            }
        };
    }
    
    @Override
    public ListCellRenderer<?> getListCellRenderer() {
        return new ListCellRendererBasic(v -> v instanceof FloorsInfo f ? f.getName() : (v != null ? v.toString() : ""));
    }
    
    @Override
    public DefaultSaveProvider<FloorsInfo> getSaveProvider() {
        return new DefaultSaveProvider<>(tfloors);      
    }
    
    @Override
    public EditorRecord<FloorsInfo> getEditor() {
        return jeditor;
    }
    
    @Override
    public String getTitle() {
        return AppLocal.getIntString("Menu.Floors");
    }     
}
