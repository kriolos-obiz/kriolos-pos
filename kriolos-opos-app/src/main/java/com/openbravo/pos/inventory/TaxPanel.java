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
import com.openbravo.format.Formats;
import com.openbravo.basic.AppLocal;
import com.openbravo.pos.sales.TaxService;
import com.openbravo.pos.panels.JPanelTable;
import com.openbravo.pos.ticket.TaxInfo;
import javax.swing.ListCellRenderer;

/**
 * Type-safe JPanelTable implementation for Taxes maintenance.
 *
 * @author adrianromero
 * @author KriolOS
 */
public class TaxPanel extends JPanelTable {

    private static final long serialVersionUID = 1L;

    private TaxService taxService;
    private TableDefinition<TaxInfo> ttaxes;
    private TaxEditor jeditor;
    
    /** Creates a new instance of JPanelDuty */
    public TaxPanel() {
    }
    
    /**
     *
     */
    @Override
    protected void init() {
        taxService = app.getBean(TaxService.class);        
        ttaxes = taxService.getTableTaxes();
        jeditor = new TaxEditor(app, dirty, taxService);
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
    public ListProvider<TaxInfo> getListProvider() {
        return new ListProviderCreator<>(ttaxes);
    }
    
    /**
     *
     * @return
     */
    @Override
    public SaveProvider<TaxInfo> getSaveProvider() {
        return taxService.getTaxSaveProvider();      
    }
    
    /**
     *
     * @return
     */
    @Override
    public Vectorer<TaxInfo> getVectorer() {
        return new Vectorer<>() {
            @Override
            public String[] getHeaders() throws BasicException {
                return new String[]{
                    AppLocal.getIntString("label.name"),
                    AppLocal.getIntString("label.dutyrate"),
                    AppLocal.getIntString("label.order")
                };
            }

            @Override
            public String[] getValues(TaxInfo t) throws BasicException {
                return new String[]{
                    t != null && t.getName() != null ? t.getName() : "",
                    t != null ? Formats.PERCENT.formatValue(t.getRate()) : "",
                    t != null && t.getOrder() != null ? Formats.INT.formatValue(t.getOrder()) : ""
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
        return ttaxes.getComparatorCreator(new int[] {1, 5, 7});
    }
    
    /**
     *
     * @return
     */
    @Override
    public ListCellRenderer<?> getListCellRenderer() {
        return new ListCellRendererBasic(v -> v instanceof TaxInfo t ? t.getName() : (v != null ? v.toString() : ""));
    }
    
    /**
     *
     * @return
     */
    @Override
    public EditorRecord<TaxInfo> getEditor() {
        return jeditor;
    }
        
    /**
     *
     * @return
     */
    @Override
    public String getTitle() {
        return AppLocal.getIntString("Menu.Taxes");
    }     
}
