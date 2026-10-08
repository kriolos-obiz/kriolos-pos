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

package com.openbravo.pos.admin;

import com.openbravo.data.gui.ListCellRendererBasic;
import com.openbravo.data.loader.ComparatorCreator;
import com.openbravo.data.loader.TableDefinition;
import com.openbravo.data.loader.Vectorer;
import com.openbravo.data.user.EditorRecord;
import com.openbravo.data.user.ListProvider;
import com.openbravo.data.user.ListProviderCreator;
import com.openbravo.data.user.DefaultSaveProvider;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.ResourceService;
import com.openbravo.pos.panels.JPanelTable;
import javax.swing.ListCellRenderer;

/**
 *
 * @author adrianromero
 */
public class ResourcesPanel extends JPanelTable {

    private static final long serialVersionUID = 1L;

    private TableDefinition<ResourceInfo> tresources;
    private ResourcesView jeditor;

    public ResourcesPanel() {}

    @Override
    protected void init() {
        ResourceService resourceService = app.getBean(ResourceService.class);
        tresources = resourceService.getTableResources();         
        jeditor = new ResourcesView(dirty);           
    }

    @Override
    public boolean deactivate() {
        return super.deactivate();    
    }

    @Override
    public ListProvider<ResourceInfo> getListProvider() {
        return new ListProviderCreator(tresources);
    }

    @Override
    public DefaultSaveProvider getSaveProvider() {
        return new DefaultSaveProvider(tresources);        
    }

    @Override
    public Vectorer getVectorer() {
        return tresources.getVectorerBasic(new int[] {1});
    }

    @Override
    public ComparatorCreator getComparatorCreator() {
        return tresources.getComparatorCreator(new int[] {1, 2});
    }

    @Override
    public ListCellRenderer getListCellRenderer() {
        return new ListCellRendererBasic(tresources.getRenderStringBasic(new int[] {1}));
    }

    public EditorRecord getEditor() {
        return jeditor;
    }
 
    @Override
    public String getTitle() {
        return AppLocal.getIntString("Menu.Resources");
    }        
}
