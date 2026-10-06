//    KriolOS POS
//    Copyright (c) 2019-2023 KriolOS
//    Portions Contributed by: John D L 2013
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
import com.openbravo.data.user.EditorListener;
import com.openbravo.data.user.EditorRecord;
import com.openbravo.data.user.ListProviderCreator;
import com.openbravo.data.user.DefaultSaveProvider;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.panels.JPanelTable2;
import com.openbravo.pos.pim.DataLogicPIM;
import com.openbravo.pos.scanpal2.DeviceScanner;
import com.openbravo.pos.ticket.ProductFilter;
import java.awt.Component;
import javax.swing.JButton;

/**
 *
 * @author adrianromero
 * Created on 1 de marzo de 2007, 22:15
 *
 */
public class PriceImportPanel extends JPanelTable2 implements EditorListener {

    private ProductsEditor jeditor;
    private ProductFilter jproductfilter;    
    
    private DataLogicPIM dataLogicPIM = null;
    
    /** Creates a new instance of ProductsPanel2 */
    public PriceImportPanel() {
    }
    
    /**
     *
     */
    @Override
    protected void init() {   
        dataLogicPIM = app.getBean(DataLogicPIM.class);
        
        // el panel del filtro
        jproductfilter = new ProductFilter();
        jproductfilter.init(app);

        row = dataLogicPIM.getProductsRow();

        lpr =  new ListProviderCreator(dataLogicPIM.getProductCatQBF(), jproductfilter);

        spr = new DefaultSaveProvider(
            dataLogicPIM.productUpdate(),
            dataLogicPIM.productInsert(),
            dataLogicPIM.getProductCatDelete());
        
        // el panel del editor
        jeditor = new ProductsEditor(app, dirty);       
    }
    
    /**
     *
     * @return
     */
    @Override
    public EditorRecord getEditor() {
        return jeditor;
    }
    
    /**
     *
     * @return
     */
    @Override
    public Component getFilter() {
        return jproductfilter.getComponent();
    }

    /**
     *
     * @return
     */
    @Override
    public Component getToolbarExtras() {
        
        JButton btnScanPal = new JButton();
        btnScanPal.setText("ScanPal");
        btnScanPal.setVisible(app.hasScanner());
        btnScanPal.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnScanPalActionPerformed(evt);
            }
        });      
        
        return btnScanPal;
    }
    
    private void btnScanPalActionPerformed(java.awt.event.ActionEvent evt) {                                           
  
        JUploadProductsPanel.showMessage(this,  bd);
    }

    /**
     *
     * @return
     */
    @Override
    public String getTitle() {
        return AppLocal.getIntString("Menu.StockImport");
    }

    /**
     *
     * @throws BasicException
     */
    @Override
    public void activate() throws BasicException {
        
        jeditor.activate(); 
        jproductfilter.activate();
        
        super.activate();
    }

    /**
     *
     * @param value
     */
    @Override
    public void updateValue(Object value) {
    }    
}
