package com.openbravo.pos.inventory;

import com.openbravo.data.gui.ListCellRendererBasic;
import com.openbravo.data.loader.ComparatorCreator;
import com.openbravo.data.loader.TableDefinition;
import com.openbravo.data.loader.Vectorer;
import com.openbravo.data.user.EditorRecord;
import com.openbravo.data.user.ListProvider;
import com.openbravo.data.user.ListProviderCreator;
import com.openbravo.data.user.DefaultSaveProvider;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.DataLogicSales;
import com.openbravo.pos.panels.JPanelTable;
import javax.swing.ListCellRenderer;

public class UomPanel extends JPanelTable {

     private TableDefinition tuom;
     private UomEditor jeditor;
     
    @Override
    protected void init() {
        DataLogicSales dlSales = app.getBean(DataLogicSales.class);           
        tuom = dlSales.getTableUom();
        jeditor = new UomEditor(app, dirty);   
    }

    @Override
    public EditorRecord getEditor() {
        return jeditor;
    }

    @Override
    public ListProvider getListProvider() {
         return new ListProviderCreator(tuom);
    }

    @Override
    public DefaultSaveProvider getSaveProvider() {
        return new DefaultSaveProvider(tuom);  
    }
    
    @Override
    public Vectorer getVectorer() {
        return tuom.getVectorerBasic(new int[]{1});
    }
    

     @Override
    public ListCellRenderer getListCellRenderer() {
        return new ListCellRendererBasic(tuom.getRenderStringBasic(new int[]{1}));
    }
     
    @Override
    public String getTitle() {
        return AppLocal.getIntString("Menu.Uom");
    }
    
}
