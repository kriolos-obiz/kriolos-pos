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

package com.openbravo.pos.epm;

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
 * @author Ali Safdar and Aneeqa Baber
 */
public class LeavesPanel extends JPanelTable {

    private TableDefinition<LeavesInfo> tleaves;
    private LeavesView jeditor;

    /** Creates a new instance of LeavesPanel */
    public LeavesPanel() {
    }

    /**
     *
     */
    @Override
    protected void init() {
        ShiftService shiftService = app.getBean(ShiftService.class);
        tleaves = shiftService.getTableLeaves();
        jeditor = new LeavesView(app, dirty, shiftService);
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
    public ListProvider<LeavesInfo> getListProvider() {
        return new ListProviderCreator<>(tleaves);
    }

    @Override
    public SaveProvider<LeavesInfo> getSaveProvider() {
        return new DefaultSaveProvider<>(tleaves);
    }

    @Override
    public Vectorer<LeavesInfo> getVectorer() {
        return new Vectorer<>() {
            @Override
            public String[] getHeaders() throws BasicException {
                return new String[]{
                    AppLocal.getIntString("label.name"),
                    AppLocal.getIntString("label.notes")
                };
            }

            @Override
            public String[] getValues(LeavesInfo leaves) throws BasicException {
                return new String[]{
                    leaves != null && leaves.getName() != null ? leaves.getName() : "",
                    leaves != null && leaves.getNotes() != null ? leaves.getNotes() : ""
                };
            }
        };
    }

    @Override
    public ComparatorCreator getComparatorCreator() {
        return tleaves.getComparatorCreator(new int[] {2, 3, 4, 5});
    }

    @Override
    public ListCellRenderer<?> getListCellRenderer() {
        return new ListCellRendererBasic(v -> v instanceof LeavesInfo l ? l.getName() : (v != null ? v.toString() : ""));
    }

    @Override
    public EditorRecord<LeavesInfo> getEditor() {
        return jeditor;
    }

    /**
     *
     * @return
     */
    @Override
    public String getTitle() {
        return AppLocal.getIntString("Menu.Leaves");
    }
}
