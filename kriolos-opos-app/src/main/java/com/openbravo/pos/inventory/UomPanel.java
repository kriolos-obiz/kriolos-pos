package com.openbravo.pos.inventory;

import com.openbravo.basic.AppLocal;
import com.openbravo.basic.BasicException;
import com.openbravo.data.gui.ListCellRendererBasic;
import com.openbravo.data.loader.TableDefinition;
import com.openbravo.data.loader.Vectorer;
import com.openbravo.data.user.DefaultSaveProvider;
import com.openbravo.data.user.EditorRecord;
import com.openbravo.data.user.ListProvider;
import com.openbravo.data.user.ListProviderCreator;
import com.openbravo.data.user.SaveProvider;
import com.openbravo.pos.catalog.CatalogService;
import com.openbravo.pos.panels.JPanelTable;
import com.openbravo.pos.pim.UomInfo;
import javax.swing.ListCellRenderer;

/**
 * Strongly typed maintenance panel for Unit of Measure (UOM) configurations.
 *
 * @author JG uniCenta
 * @author KriolOS
 */
public class UomPanel extends JPanelTable {

    private static final long serialVersionUID = 1L;

    private TableDefinition<UomInfo> tuom;
    private UomEditor jeditor;
     
    @Override
    protected void init() {
        CatalogService catalogService = app.getBean(CatalogService.class);           
        tuom = catalogService.getTableUom();
        jeditor = new UomEditor(app, dirty);   
    }

    @Override
    public EditorRecord<UomInfo> getEditor() {
        return jeditor;
    }

    @Override
    public ListProvider<UomInfo> getListProvider() {
        return new ListProviderCreator<>(tuom);
    }

    @Override
    public SaveProvider<UomInfo> getSaveProvider() {
        return new DefaultSaveProvider<>(tuom);  
    }
    
    @Override
    public Vectorer<UomInfo> getVectorer() {
        return new Vectorer<>() {
            @Override
            public String[] getHeaders() throws BasicException {
                return new String[]{AppLocal.getIntString("label.name")};
            }

            @Override
            public String[] getValues(UomInfo obj) throws BasicException {
                return new String[]{obj != null ? obj.getName() : ""};
            }
        };
    }

    @Override
    public ListCellRenderer<?> getListCellRenderer() {
        return new ListCellRendererBasic(value -> value instanceof UomInfo u ? u.getName() : (value != null ? value.toString() : ""));
    }
     
    @Override
    public String getTitle() {
        return AppLocal.getIntString("Menu.Uom");
    }
}
