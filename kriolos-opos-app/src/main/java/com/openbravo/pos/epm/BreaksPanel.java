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
public class BreaksPanel extends JPanelTable {

    private TableDefinition<BreaksInfo> tbreaks;
    private BreaksView jeditor;

    /**
     *
     */
    public BreaksPanel() {
    }

    /**
     *
     */
    @Override
    protected void init() {
        ShiftService shiftService = app.getBean(ShiftService.class);
        tbreaks = shiftService.getTableBreaks();
        jeditor = new BreaksView(app, dirty, shiftService);
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
    public ListProvider<BreaksInfo> getListProvider() {
        return new ListProviderCreator<>(tbreaks);
    }

    @Override
    public SaveProvider<BreaksInfo> getSaveProvider() {
        return new DefaultSaveProvider<>(tbreaks);
    }

    @Override
    public Vectorer<BreaksInfo> getVectorer() {
        return new Vectorer<>() {
            @Override
            public String[] getHeaders() throws BasicException {
                return new String[]{
                    AppLocal.getIntString("label.name"),
                    AppLocal.getIntString("label.notes")
                };
            }

            @Override
            public String[] getValues(BreaksInfo breaks) throws BasicException {
                return new String[]{
                    breaks != null && breaks.getName() != null ? breaks.getName() : "",
                    breaks != null && breaks.getNotes() != null ? breaks.getNotes() : ""
                };
            }
        };
    }

    @Override
    public ComparatorCreator getComparatorCreator() {
        return tbreaks.getComparatorCreator(new int[] {1, 2});
    }

    @Override
    public ListCellRenderer<?> getListCellRenderer() {
        return new ListCellRendererBasic(v -> v instanceof BreaksInfo b ? b.getName() : (v != null ? v.toString() : ""));
    }

    @Override
    public EditorRecord<BreaksInfo> getEditor() {
        return jeditor;
    }

    /**
     *
     * @return
     */
    @Override
    public String getTitle() {
        return AppLocal.getIntString("Menu.Breaks");
    }
}
