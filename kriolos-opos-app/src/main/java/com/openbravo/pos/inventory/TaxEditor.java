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
import com.openbravo.data.gui.ComboBoxValModel;
import com.openbravo.data.gui.MessageInf;
import com.openbravo.data.loader.SentenceList;
import com.openbravo.data.user.DirtyManager;
import com.openbravo.data.user.EditorRecord;
import com.openbravo.format.Formats;
import com.openbravo.basic.AppLocal;
import com.openbravo.pos.panels.AppView;
import com.openbravo.pos.sales.TaxService;
import com.openbravo.pos.ticket.TaxInfo;
import com.openbravo.pos.util.FormatsIntegerConverter;
import com.openbravo.pos.util.FormatsPercentConverter;
import com.openbravo.ui.data.binding.Converter;
import com.openbravo.ui.data.binding.SwingBinder;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *
 * @author JG uniCenta
 * @author KriolOS
 */
public class TaxEditor extends JPanel implements EditorRecord<TaxInfo> {
    
    private static final long serialVersionUID = 1L;
    private static final System.Logger LOGGER = System.getLogger(TaxEditor.class.getName());

    private String m_oId;
    private DirtyManager dirty;
    private SwingBinder<TaxInfo> binder;
    
    private SentenceList<TaxCategoryInfo> taxcatsent;
    private ComboBoxValModel<TaxCategoryInfo> taxcatmodel;    
    
    private SentenceList<TaxCustCategoryInfo> taxcustcatsent;
    private ComboBoxValModel<TaxCustCategoryInfo> taxcustcatmodel;   
    
    private SentenceList<TaxInfo> taxparentsent;
    private ComboBoxValModel<TaxInfo> taxparentmodel;    
    
    /**
     * Headless constructor for testing without active AppView session or services.
     *
     * @param dirty dirty state manager
     */
    public TaxEditor(DirtyManager dirty) {
        this((AppView) null, dirty, (TaxService) null);
    }

    /**
     * Headless constructor with explicit TaxService dependency.
     *
     * @param dirty dirty state manager
     * @param taxService tax service port
     */
    public TaxEditor(DirtyManager dirty, TaxService taxService) {
        this((AppView) null, dirty, taxService);
    }

    /**
     * Creates new form taxEditor.
     *
     * @param app application view context
     * @param dirty dirty state manager
     */
    public TaxEditor(AppView app, DirtyManager dirty) {
        this(app, dirty, app != null ? app.getBean(TaxService.class) : null);
    }

    /**
     * Full dependency-injected constructor.
     *
     * @param app application view context
     * @param dirty dirty state manager
     * @param taxService tax domain service
     */
    public TaxEditor(AppView app, DirtyManager dirty, TaxService taxService) {
        initComponents();
        this.dirty = dirty;
        
        taxcatsent = taxService != null ? taxService.getTaxCategoriesList() : null;
        taxcatmodel = new ComboBoxValModel<>();        
        
        taxcustcatsent = taxService != null ? taxService.getTaxCustCategoriesList() : null;
        taxcustcatmodel = new ComboBoxValModel<>();    
        
        taxparentsent = taxService != null ? taxService.getTaxList() : null;
        taxparentmodel = new ComboBoxValModel<>();    

        if (dirty != null) {
            m_jName.getDocument().addDocumentListener(dirty);
            m_jTaxCategory.addActionListener(dirty);
            m_jCustTaxCategory.addActionListener(dirty);
            m_jTaxParent.addActionListener(dirty);
            m_jRate.getDocument().addDocumentListener(dirty);
            jCascade.addActionListener(dirty);
            jOrder.getDocument().addDocumentListener(dirty);
        }

        initValidator();
        writeValueEOF();
    }

    private void initValidator() {
        binder = new SwingBinder<>(new TaxInfo());

        // Mandatory Tax Name
        binder.forField(m_jName)
                .asRequired(AppLocal.getIntString("validation.required", AppLocal.getIntString("label.name")))
                .bind(TaxInfo::getName, TaxInfo::setName);

        // Tax Rate (Percentage)
        binder.forField(m_jRate)
                .withConverter(new FormatsPercentConverter())
                .withValidator(v -> v == null || v >= 0.0, AppLocal.getIntString("validation.number.positive", AppLocal.getIntString("label.dutyrate")))
                .bind(TaxInfo::getRate, TaxInfo::setRate);

        // Cascade Flag
        binder.forField(jCascade)
                .bind(TaxInfo::isCascade, TaxInfo::setCascade);

        // Order
        binder.forField(jOrder)
                .withConverter(new FormatsIntegerConverter())
                .bind(TaxInfo::getOrder, TaxInfo::setOrder);

        binder.addStatusChangeListener(() -> {
            if (dirty != null) {
                dirty.setDirty(true);
            }
        });
    }

    public boolean isValid() {
        return binder == null || binder.validate();
    }

    public SwingBinder<TaxInfo> getBinder() {
        return binder;
    }

    JTextField getNameField() {
        return m_jName;
    }

    JTextField getRateField() {
        return m_jRate;
    }

    JTextField getOrderField() {
        return jOrder;
    }

    /**
     *
     * @throws BasicException
     */
    public void activate() throws BasicException {
        if (taxcatsent != null) {
            List<TaxCategoryInfo> a = taxcatsent.list();
            taxcatmodel = new ComboBoxValModel<>(a);
            m_jTaxCategory.setModel(taxcatmodel);
        }
        
        List<TaxCustCategoryInfo> b = new ArrayList<>();
        b.add(null);
        if (taxcustcatsent != null) {
            List<TaxCustCategoryInfo> custCats = taxcustcatsent.list();
            if (custCats != null) {
                b.addAll(custCats);
            }
        }
        taxcustcatmodel = new ComboBoxValModel<>(b);
        m_jCustTaxCategory.setModel(taxcustcatmodel);    
    }
    
    /**
     *
     */
    @Override
    public void refresh() {
        List<TaxInfo> a = new ArrayList<>();
        a.add(null); // The null item
        
        try {
            if (taxparentsent != null) {
                List<TaxInfo> parents = taxparentsent.list();
                if (parents != null) {
                    a.addAll(parents);
                }
            }
        } catch (BasicException eD) {
            MessageInf msg = new MessageInf(MessageInf.SGN_NOTICE, AppLocal.getIntString("message.cannotloadlists"), eD);
            msg.show(this);
        }
        
        taxparentmodel = new ComboBoxValModel<>(a);
        m_jTaxParent.setModel(taxparentmodel);    
    }
    
    /**
     *
     */
    @Override
    public void writeValueEOF() {
        m_oId = null;
        if (binder != null) {
            binder.clear();
        } else {
            m_jName.setText(null);
            m_jRate.setText(null);
            jCascade.setSelected(false);
            jOrder.setText(null);
        }
        taxcatmodel.setSelectedKey(null);
        taxcustcatmodel.setSelectedKey(null);
        taxparentmodel.setSelectedKey(null);
      
        m_jName.setEnabled(false);
        m_jTaxCategory.setEnabled(false);
        m_jCustTaxCategory.setEnabled(false);
        m_jTaxParent.setEnabled(false);
        m_jRate.setEnabled(false);
        jCascade.setEnabled(false);
        jOrder.setEnabled(false);
    }

    /**
     *
     */
    @Override
    public void writeValueInsert() {
        m_oId = UUID.randomUUID().toString();
        if (binder != null) {
            binder.clear();
        } else {
            m_jName.setText(null);
            m_jRate.setText(null);
            jCascade.setSelected(false);
            jOrder.setText(null);
        }
        taxcatmodel.setSelectedKey(null);
        taxcustcatmodel.setSelectedKey(null);
        taxparentmodel.setSelectedKey(null);

        m_jName.setEnabled(true);
        m_jTaxCategory.setEnabled(true);
        m_jCustTaxCategory.setEnabled(true);
        m_jTaxParent.setEnabled(true);        
        m_jRate.setEnabled(true);
        jCascade.setEnabled(true);    
        jOrder.setEnabled(true);
    }

    /**
     *
     * @param value
     */
    @Override
    public void writeValueDelete(TaxInfo value) {
        if (value == null) {
            writeValueEOF();
            return;
        }

        m_oId = value.getId();
        if (binder != null) {
            binder.readBean(value);
        } else {
            m_jName.setText(Formats.STRING.formatValue(value.getName()));
            m_jRate.setText(Formats.PERCENT.formatValue(value.getRate()));
            jCascade.setSelected(value.isCascade());
            jOrder.setText(Formats.INT.formatValue(value.getOrder()));
        }
        taxcatmodel.setSelectedKey(value.getTaxCategoryID());
        taxcustcatmodel.setSelectedKey(value.getTaxCustCategoryID());
        taxparentmodel.setSelectedKey(value.getParentID());        
        
        m_jName.setEnabled(false);
        m_jTaxCategory.setEnabled(false);
        m_jCustTaxCategory.setEnabled(false);
        m_jTaxParent.setEnabled(false);
        m_jRate.setEnabled(false);
        jCascade.setEnabled(false);
        jOrder.setEnabled(false);
    }    

    /**
     *
     * @param value
     */
    @Override
    public void writeValueEdit(TaxInfo value) {
        if (value == null) {
            writeValueEOF();
            return;
        }

        m_oId = value.getId();
        if (binder != null) {
            binder.readBean(value);
        } else {
            m_jName.setText(Formats.STRING.formatValue(value.getName()));
            m_jRate.setText(Formats.PERCENT.formatValue(value.getRate()));
            jCascade.setSelected(value.isCascade());
            jOrder.setText(Formats.INT.formatValue(value.getOrder()));
        }
        taxcatmodel.setSelectedKey(value.getTaxCategoryID());
        taxcustcatmodel.setSelectedKey(value.getTaxCustCategoryID());
        taxparentmodel.setSelectedKey(value.getParentID());        
      
        m_jName.setEnabled(true);
        m_jTaxCategory.setEnabled(true);
        m_jCustTaxCategory.setEnabled(true);
        m_jTaxParent.setEnabled(true);        
        m_jRate.setEnabled(true);
        jCascade.setEnabled(true);
        jOrder.setEnabled(true);
    }

    /**
     *
     * @return
     * @throws BasicException
     */
    @Override
    public TaxInfo createValue() throws BasicException {
        Double rate = null;
        try {
            rate = (Double) Formats.PERCENT.parseValue(m_jRate.getText());
        } catch (BasicException ex) {
            // fallback
        }
        Integer order = null;
        try {
            order = (Integer) Formats.INT.parseValue(jOrder.getText());
        } catch (BasicException ex) {
            // fallback
        }
        TaxInfo tax = new TaxInfo(
            m_oId,
            m_jName.getText() != null ? m_jName.getText() : "",
            (String) taxcatmodel.getSelectedKey(),
            (String) taxcustcatmodel.getSelectedKey(),
            (String) taxparentmodel.getSelectedKey(),
            rate != null ? rate : 0.0,
            jCascade.isSelected(),
            order
        );
        if (binder != null) {
            try {
                binder.writeBean(tax);
            } catch (SwingBinder.ValidationException ex) {
                LOGGER.log(System.Logger.Level.WARNING, "Validation in createValue: " + ex.getMessage());
            }
        }
        if (tax.getName() == null) {
            tax.setName(m_jName.getText() != null ? m_jName.getText() : "");
        }
        return tax;
    }

    /**
     *
     * @return
     */
    @Override
    public Component getComponent() {
        return this;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        m_jName = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        m_jRate = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jCascade = new javax.swing.JCheckBox();
        m_jTaxCategory = new javax.swing.JComboBox();
        m_jTaxParent = new javax.swing.JComboBox();
        m_jCustTaxCategory = new javax.swing.JComboBox();
        jLabel6 = new javax.swing.JLabel();
        jOrder = new javax.swing.JTextField();

        m_jName.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jName.setPreferredSize(new java.awt.Dimension(0, 30));

        jLabel2.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/info.png"))); // NOI18N
        jLabel2.setText(AppLocal.getIntString("label.name")); // NOI18N
        jLabel2.setPreferredSize(new java.awt.Dimension(170, 30));
        jLabel2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel2MouseClicked(evt);
            }
        });

        jLabel3.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel3.setText(AppLocal.getIntString("label.dutyrate")); // NOI18N
        jLabel3.setPreferredSize(new java.awt.Dimension(170, 30));

        m_jRate.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jRate.setPreferredSize(new java.awt.Dimension(0, 30));

        jLabel1.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel1.setText(AppLocal.getIntString("label.taxcategory")); // NOI18N
        jLabel1.setPreferredSize(new java.awt.Dimension(170, 30));

        jLabel4.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel4.setText(AppLocal.getIntString("label.custtaxcategory")); // NOI18N
        jLabel4.setPreferredSize(new java.awt.Dimension(170, 30));

        jLabel5.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel5.setText(AppLocal.getIntString("label.taxparent")); // NOI18N
        jLabel5.setPreferredSize(new java.awt.Dimension(170, 30));

        jCascade.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jCascade.setText(AppLocal.getIntString("label.cascade")); // NOI18N
        jCascade.setPreferredSize(new java.awt.Dimension(0, 30));

        m_jTaxCategory.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jTaxCategory.setPreferredSize(new java.awt.Dimension(0, 30));

        m_jTaxParent.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jTaxParent.setPreferredSize(new java.awt.Dimension(0, 30));

        m_jCustTaxCategory.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jCustTaxCategory.setPreferredSize(new java.awt.Dimension(0, 30));

        jLabel6.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel6.setText(AppLocal.getIntString("label.order")); // NOI18N
        jLabel6.setPreferredSize(new java.awt.Dimension(170, 30));

        jOrder.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jOrder.setPreferredSize(new java.awt.Dimension(0, 30));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(m_jTaxCategory, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(m_jCustTaxCategory, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(m_jTaxParent, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(m_jRate, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(16, 16, 16)
                        .addComponent(jCascade, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(m_jName, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jOrder, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(50, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_jName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_jTaxCategory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_jCustTaxCategory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_jTaxParent, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(m_jRate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jCascade, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jOrder, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jLabel2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel2MouseClicked
        
        if (evt.getClickCount() == 2) {
            String uuidString = m_oId.toString();
            StringSelection stringSelection = new StringSelection(uuidString);
            Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
            clpbrd.setContents(stringSelection, null);
        
            JOptionPane.showMessageDialog(this, 
                AppLocal.getIntString("message.uuidcopy"));
        }
    }//GEN-LAST:event_jLabel2MouseClicked
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox jCascade;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JTextField jOrder;
    private javax.swing.JComboBox m_jCustTaxCategory;
    private javax.swing.JTextField m_jName;
    private javax.swing.JTextField m_jRate;
    private javax.swing.JComboBox m_jTaxCategory;
    private javax.swing.JComboBox m_jTaxParent;
    // End of variables declaration//GEN-END:variables
    
}
