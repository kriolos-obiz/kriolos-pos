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

import com.openbravo.basic.BasicException;
import com.openbravo.data.gui.ListCellRendererBasic;
import com.openbravo.data.loader.ComparatorCreator;
import com.openbravo.data.loader.TableDefinition;
import com.openbravo.data.loader.Vectorer;
import com.openbravo.data.user.EditorRecord;
import com.openbravo.data.user.ListProvider;
import com.openbravo.data.user.ListProviderCreator;
import com.openbravo.data.user.SaveProvider;
import com.openbravo.basic.AppLocal;
import com.openbravo.pos.resources.ResourceService;
import com.openbravo.pos.panels.JPanelTable;
import javax.swing.ListCellRenderer;

/**
 * Type-safe JPanelTable implementation for Application Resources maintenance.
 *
 * @author adrianromero
 * @author KriolOS
 */
public class ResourcesPanel extends JPanelTable {

    private static final long serialVersionUID = 1L;

    private ResourceService resourceService;
    private TableDefinition<ResourceInfo> tresources;
    private ResourcesView jeditor;

    public ResourcesPanel() {}

    @Override
    protected void init() {
        resourceService = app.getBean(ResourceService.class);
        tresources = resourceService.getTableResources();         
        jeditor = new ResourcesView(dirty);           
    }

    @Override
    public boolean deactivate() {
        return super.deactivate();    
    }

    @Override
    public ListProvider<ResourceInfo> getListProvider() {
        return new ListProviderCreator<>(tresources);
    }

    @Override
    public SaveProvider<ResourceInfo> getSaveProvider() {
        return resourceService.getResourceSaveProvider();        
    }

    @Override
    public Vectorer<ResourceInfo> getVectorer() {
        return new Vectorer<>() {
            @Override
            public String[] getHeaders() throws BasicException {
                return new String[]{AppLocal.getIntString("label.name")};
            }

            @Override
            public String[] getValues(ResourceInfo r) throws BasicException {
                return new String[]{r != null && r.getName() != null ? r.getName() : ""};
            }
        };
    }

    @Override
    public ComparatorCreator getComparatorCreator() {
        return tresources.getComparatorCreator(new int[] {1, 2});
    }

    @Override
    public ListCellRenderer<?> getListCellRenderer() {
        return new ListCellRendererBasic(v -> v instanceof ResourceInfo r ? r.getName() : (v != null ? v.toString() : ""));
    }

    @Override
    public EditorRecord<ResourceInfo> getEditor() {
        return jeditor;
    }
 
    @Override
    public String getTitle() {
        return AppLocal.getIntString("Menu.Resources");
    }        
}
