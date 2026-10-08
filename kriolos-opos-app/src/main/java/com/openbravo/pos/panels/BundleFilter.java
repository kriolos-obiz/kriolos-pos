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

package com.openbravo.pos.panels;

import com.openbravo.basic.BasicException;
import com.openbravo.data.gui.MessageInf;
import com.openbravo.data.loader.SerializerWrite;
import com.openbravo.data.loader.SerializerWriteString;
import com.openbravo.pos.catalog.CatalogService;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.pim.DataLogicPIM;
import com.openbravo.pos.reports.ReportEditorCreator;
import com.openbravo.pos.ticket.ProductInfoExt;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EventListener;
import javax.swing.event.EventListenerList;

/**
 *
 * @author Jack
 */
public class BundleFilter extends javax.swing.JPanel implements ReportEditorCreator {

    private ProductInfoExt product;
    private CatalogService catalogService;
    private DataLogicPIM dataLogicPIM;
    private AppView appView;
    
    protected EventListenerList listeners = new EventListenerList();

    /** Creates new form BundleFilter */
    public BundleFilter() {
        initComponents();
    }

    @Override
    public void init(AppView app) {   
           
        this.appView = app;
        this.catalogService = app.getBean(CatalogService.class);
        this.dataLogicPIM = (catalogService instanceof DataLogicPIM) ? (DataLogicPIM) catalogService : null;
    }

    @Override
    public void activate() throws BasicException {
        product = null;
        m_jSearch.setText(null);
        m_jBarcode1.setText(null);
        m_jReference1.setText(null);        
    }

    @Override
    public SerializerWrite getSerializerWrite() {
        return SerializerWriteString.INSTANCE;
    }

    public void addActionListener(ActionListener l){
        listeners.add(ActionListener.class, l);
    }
    
    public void removeActionListener(ActionListener l) {
        listeners.remove(ActionListener.class, l);
    }
    
    @Override
    public Component getComponent() {
        return this;
    }

    @Override
    public Object createValue() throws BasicException {
        return product == null ? null : product.getID();
    }
    
    public ProductInfoExt getProductInfoExt() {
        return product;
    }

    private void assignProduct(ProductInfoExt prod) {

        product = prod;
        if (product == null) {
            m_jSearch.setText(null);
            m_jBarcode1.setText(null);
            m_jReference1.setText(null);
        } else {
            m_jSearch.setText(product.getReference() + " - " + product.getName());
            m_jBarcode1.setText(product.getCode());
            m_jReference1.setText(product.getReference());
        } 
        
        fireSelectedProduct();
    }
    
    
    
    protected void fireSelectedProduct() {
        EventListener[] l = listeners.getListeners(ActionListener.class);
        ActionEvent e = null;
        for (int i = 0; i < l.length; i++) {
            if (e == null) {
                e = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "SELECTED");
            }
            ((ActionListener) l[i]).actionPerformed(e);	       
        }
    }     

    private void assignProductByCode() {
        try {
            ProductInfoExt prod = catalogService.getProductInfoByCode(m_jBarcode1.getText());
            if (prod == null) {
                com.openbravo.pos.util.NotifyUtils.beep();
            }
            assignProduct(prod);
        } catch (BasicException eData) {
            MessageInf msg = new MessageInf(eData);
            msg.show(this);
            assignProduct(null);
        }
    }

    private void assignProductByReference() {
        try {
            ProductInfoExt prod = catalogService.getProductInfoByReference(m_jReference1.getText());
            if (prod == null) {
                com.openbravo.pos.util.NotifyUtils.beep();
            }
            assignProduct(prod);
        } catch (BasicException eData) {
            MessageInf msg = new MessageInf(eData);
            msg.show(this);
            assignProduct(null);
        }
    }
   
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel6 = new javax.swing.JLabel();
        m_jReference1 = new javax.swing.JTextField();
        Enter1 = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        m_jBarcode1 = new javax.swing.JTextField();
        Enter2 = new javax.swing.JButton();
        m_jSearch = new javax.swing.JTextField();
        search = new javax.swing.JButton();

        setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N

        jLabel6.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel6.setText(AppLocal.getIntString("label.prodref")); // NOI18N
        jLabel6.setMaximumSize(new java.awt.Dimension(50, 20));
        jLabel6.setMinimumSize(new java.awt.Dimension(50, 20));
        jLabel6.setPreferredSize(new java.awt.Dimension(100, 30));

        m_jReference1.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jReference1.setPreferredSize(new java.awt.Dimension(150, 30));
        m_jReference1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jReference1ActionPerformed(evt);
            }
        });

        Enter1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/products24.png"))); // NOI18N
        Enter1.setToolTipText("Enter Product ID");
        Enter1.setPreferredSize(new java.awt.Dimension(80, 45));
        Enter1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Enter1ActionPerformed(evt);
            }
        });

        jLabel7.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel7.setText(AppLocal.getIntString("label.prodbarcode")); // NOI18N
        jLabel7.setPreferredSize(new java.awt.Dimension(100, 30));

        m_jBarcode1.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jBarcode1.setPreferredSize(new java.awt.Dimension(150, 30));
        m_jBarcode1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jBarcode1ActionPerformed(evt);
            }
        });

        Enter2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/barcode.png"))); // NOI18N
        Enter2.setToolTipText("Get Barcode");
        Enter2.setPreferredSize(new java.awt.Dimension(80, 45));
        Enter2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Enter2ActionPerformed(evt);
            }
        });

        m_jSearch.setEditable(false);
        m_jSearch.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jSearch.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        m_jSearch.setFocusable(false);
        m_jSearch.setPreferredSize(new java.awt.Dimension(200, 30));
        m_jSearch.setRequestFocusEnabled(false);

        search.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/search24.png"))); // NOI18N
        search.setToolTipText("Search Products");
        search.setPreferredSize(new java.awt.Dimension(80, 45));
        search.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(m_jBarcode1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(Enter2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(m_jReference1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(Enter1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(m_jSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(search, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_jReference1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Enter1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_jSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(search, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_jBarcode1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Enter2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        getAccessibleContext().setAccessibleName("By product");
    }// </editor-fold>//GEN-END:initComponents

    private void m_jReference1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jReference1ActionPerformed
        this.assignProductByReference();
    }//GEN-LAST:event_m_jReference1ActionPerformed

    private void searchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchActionPerformed
        
        assignProduct(JProductFinderPanel.showMessage(this, this.appView, JProductFinderPanel.PRODUCT_NORMAL));       
        
}//GEN-LAST:event_searchActionPerformed

    private void Enter2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Enter2ActionPerformed
        this.assignProductByCode();
    }//GEN-LAST:event_Enter2ActionPerformed

    private void Enter1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Enter1ActionPerformed
        this.assignProductByReference();
    }//GEN-LAST:event_Enter1ActionPerformed

    private void m_jBarcode1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jBarcode1ActionPerformed
        this.assignProductByCode();
    }//GEN-LAST:event_m_jBarcode1ActionPerformed

    public CatalogService getCatalogService() {
        return catalogService;
    }

    @Deprecated
    public DataLogicPIM getDataLogicPIM() {
        return dataLogicPIM;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton Enter1;
    private javax.swing.JButton Enter2;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JTextField m_jBarcode1;
    private javax.swing.JTextField m_jReference1;
    private javax.swing.JTextField m_jSearch;
    private javax.swing.JButton search;
    // End of variables declaration//GEN-END:variables

}
