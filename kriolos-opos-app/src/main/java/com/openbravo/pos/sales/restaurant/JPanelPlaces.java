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

import com.openbravo.basic.BasicException;
import com.openbravo.data.gui.ListCellRendererBasic;
import com.openbravo.data.loader.Datas;
import com.openbravo.data.loader.TableDefinition;
import com.openbravo.data.loader.Vectorer;
import com.openbravo.data.user.EditorRecord;
import com.openbravo.data.user.ListProvider;
import com.openbravo.data.user.ListProviderCreator;
import com.openbravo.data.user.DefaultSaveProvider;
import com.openbravo.format.Formats;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.panels.JPanelTable;
import javax.swing.ListCellRenderer;

/**
 *
 * @author adrianromero
 */
public class JPanelPlaces extends JPanelTable {

    private static final long serialVersionUID = 1L;

    private TableDefinition tplaces;
    private PlacesEditor jeditor;

    public JPanelPlaces() {}

    @Override
    protected void init() {
        RestaurantService restaurantService = app.getBean(RestaurantService.class);
        tplaces = restaurantService.getTablePlaces();
        jeditor = new PlacesEditor(restaurantService, dirty);
    }

    @Override
    public ListProvider getListProvider() {
        return new ListProviderCreator(tplaces);
    }

    @Override
    public DefaultSaveProvider getSaveProvider() {
        return new DefaultSaveProvider(tplaces);
    }

    @Override
    public Vectorer getVectorer() {
        return tplaces.getVectorerBasic(new int[]{1});
    }

    @Override
    public ListCellRenderer getListCellRenderer() {
        return new ListCellRendererBasic(tplaces.getRenderStringBasic(new int[]{1}));
    }

    @Override
    public EditorRecord getEditor() {
        return jeditor;
    }

    @Override
    public String getTitle() {
        return AppLocal.getIntString("Menu.Tables");
    }

    @Override
    public void activate() throws BasicException {
        jeditor.activate();
        super.activate();
    }
}
