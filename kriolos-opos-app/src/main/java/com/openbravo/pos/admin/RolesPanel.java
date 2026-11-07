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
import com.openbravo.data.user.DefaultSaveProvider;
import com.openbravo.basic.AppLocal;
import com.openbravo.pos.panels.JPanelTable;
import javax.swing.ListCellRenderer;

/**
 * Type-safe JPanelTable implementation for Role security maintenance.
 *
 * @author adrianromero
 * @author KriolOS
 */
public class RolesPanel extends JPanelTable {
    
    private TableDefinition<RoleInfo> troles;
    private RolesView jeditor;

    public RolesPanel() {}

    @Override
    protected void init() {
        PeopleService peopleService = app.getBean(PeopleService.class);        
        troles = peopleService.getTableRoles();         
        jeditor = new RolesView(dirty);    
    }

    @Override
    public ListProvider<RoleInfo> getListProvider() {
        return new ListProviderCreator<>(troles);
    }

    @Override
    public DefaultSaveProvider<RoleInfo> getSaveProvider() {
        return new DefaultSaveProvider<>(troles);        
    }

    @Override
    public Vectorer<RoleInfo> getVectorer() {
        return new Vectorer<>() {
            @Override
            public String[] getHeaders() throws BasicException {
                return new String[]{AppLocal.getIntString("label.name")};
            }

            @Override
            public String[] getValues(RoleInfo role) throws BasicException {
                return new String[]{role != null && role.getName() != null ? role.getName() : ""};
            }
        };
    }

    @Override
    public ComparatorCreator getComparatorCreator() {
        return troles.getComparatorCreator(new int[] {1});
    }

    @Override
    public ListCellRenderer<?> getListCellRenderer() {
        return new ListCellRendererBasic(v -> v instanceof RoleInfo r ? r.getName() : (v != null ? v.toString() : ""));
    }

    @Override
    public EditorRecord<RoleInfo> getEditor() {
        return jeditor;
    }
 
    @Override
    public String getTitle() {
        return AppLocal.getIntString("Menu.Roles");
    }        
}
