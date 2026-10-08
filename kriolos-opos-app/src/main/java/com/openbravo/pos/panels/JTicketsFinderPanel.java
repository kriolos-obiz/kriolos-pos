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
import com.openbravo.beans.JCalendarPanel2;
import com.openbravo.data.gui.ComboBoxValModel;
import com.openbravo.data.gui.ListQBFModelNumber;
import com.openbravo.data.gui.MessageInf;
import com.openbravo.data.gui.modal.PosUIModal;
import com.openbravo.data.loader.QBFCompareEnum;
import com.openbravo.data.user.EditorCreator;
import com.openbravo.data.user.ListProvider;
import com.openbravo.data.user.ListProviderCreator;
import com.openbravo.format.Formats;
import com.openbravo.pos.customers.CustomerInfo;
import com.openbravo.pos.customers.CustomerService;
import com.openbravo.pos.customers.DataLogicCustomers;
import com.openbravo.pos.customers.JCustomerFinder;
import com.openbravo.pos.customers.JCustomerFinderPanel;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.DataLogicSales;
import com.openbravo.pos.inventory.TaxCategoryInfo;
import com.openbravo.pos.sales.TaxService;
import com.openbravo.pos.sales.TicketLifecycleService;
import com.openbravo.pos.ticket.FindTicketsInfo;
import com.openbravo.pos.ticket.FindTicketsRenderer;
import java.awt.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JPanel;

/**
 * Panel-First modal for finding tickets, managed via {@link PosUIModal}.
 *
 * @author KriolOS
 */
public class JTicketsFinderPanel extends JPanel implements EditorCreator {

    private static final long serialVersionUID = 1L;

    private ListProvider lpr;
    private ComboBoxValModel<TaxCategoryInfo> m_CategoryModel;
    private TicketLifecycleService ticketLifecycleService;
    private TaxService taxService;
    private CustomerService customerService;
    private DataLogicCustomers dlCustomers;
    private FindTicketsInfo selectedTicket;
    private PosUIModal modalContext;

    public JTicketsFinderPanel() {
        initComponents();
        initDomainAdapters();
    }

    public JTicketsFinderPanel(TicketLifecycleService ticketLifecycleService, TaxService taxService, CustomerService customerService) {
        initComponents();
        init(ticketLifecycleService, taxService, customerService);
        initDomainAdapters();
    }

    @Deprecated
    public JTicketsFinderPanel(TicketLifecycleService ticketLifecycleService, TaxService taxService, DataLogicCustomers dlCustomers) {
        this(ticketLifecycleService, taxService, (CustomerService) dlCustomers);
    }

    @Deprecated
    public JTicketsFinderPanel(DataLogicSales dlSales, DataLogicCustomers dlCustomers) {
        this((TicketLifecycleService) dlSales, (TaxService) dlSales, (CustomerService) dlCustomers);
    }

    private void initDomainAdapters() {
        setName("kriolos:tickets:finder-panel");
        jbtnExecute.setName("kriolos:tickets:btn-execute");
        jbtnReset.setName("kriolos:tickets:btn-reset");
        jcmdOK.setName("kriolos:tickets:btn-ok");
        jcmdCancel.setName("kriolos:tickets:btn-cancel");
        jListTickets.setName("kriolos:tickets:list-tickets");
    }

    public void setModalContext(PosUIModal modalContext) {
        this.modalContext = modalContext;
    }

    public static FindTicketsInfo show(Component parent, TicketLifecycleService ticketLifecycleService, TaxService taxService, CustomerService customerService) {
        JTicketsFinderPanel panel = new JTicketsFinderPanel(ticketLifecycleService, taxService, customerService);
        PosUIModal modal = PosUIModal.create(parent, panel)
                .setTitle(AppLocal.getIntString("form.tickettitle"))
                .setModal(true)
                .setResizable(true);
        panel.setModalContext(modal);
        modal.show();
        return panel.getSelectedTicket();
    }

    @Deprecated
    public static FindTicketsInfo show(Component parent, TicketLifecycleService ticketLifecycleService, TaxService taxService, DataLogicCustomers dlCustomers) {
        return show(parent, ticketLifecycleService, taxService, (CustomerService) dlCustomers);
    }

    @Deprecated
    public static FindTicketsInfo show(Component parent, DataLogicSales dlSales, DataLogicCustomers dlCustomers) {
        return show(parent, (TicketLifecycleService) dlSales, (TaxService) dlSales, (CustomerService) dlCustomers);
    }

    public FindTicketsInfo getSelectedTicket() {
        return selectedTicket;
    }

    public FindTicketsInfo getSelectedCustomer() {
        return selectedTicket;
    }

    public void init(TicketLifecycleService ticketLifecycleService, TaxService taxService, CustomerService customerService) {

        this.ticketLifecycleService = ticketLifecycleService;
        this.taxService = taxService;
        this.customerService = customerService;
        if (customerService instanceof DataLogicCustomers) {
            this.dlCustomers = (DataLogicCustomers) customerService;
        }

        jScrollPane1.getVerticalScrollBar().setPreferredSize(new Dimension(35, 35));

        jtxtTicketID.addEditorKeys(m_jKeys);
        jtxtMoney.addEditorKeys(m_jKeys);

        lpr = new ListProviderCreator(ticketLifecycleService != null ? ticketLifecycleService.getTicketsList() : null, this);

        jListTickets.setCellRenderer(new FindTicketsRenderer());

        if (getRootPane() != null) {
            getRootPane().setDefaultButton(jcmdOK);
        }

        initCombos();

        defaultValues();

        selectedTicket = null;
    }

    public void executeSearch() {

        jLblTicketCount.setVisible(false);
        jLblReturnCount.setVisible(false);
        jLblTicketCount.setText(null);

        try {
            jListTickets.setModel(new MyListData(lpr.loadData()));
            if (jListTickets.getModel().getSize() > 0) {
                String count = String.valueOf(jListTickets.getModel().getSize());
                jLblTicketCount.setVisible(true);
                jLblReturnCount.setVisible(true);
                jListTickets.setSelectedIndex(0);
                jLblTicketCount.setText(count);
            }
        } catch (BasicException e) {
        }
    }

    private void initCombos() {
        String[] values = new String[] {AppLocal.getIntString("label.sales"),
            AppLocal.getIntString("label.refunds"), AppLocal.getIntString("label.all")};
        jComboBoxTicket.setModel(new DefaultComboBoxModel(values));

        jcboMoney.setModel(ListQBFModelNumber.getMandatoryNumber());

        List<TaxCategoryInfo> taxCategoryList = taxService != null
                ? new ArrayList<>(taxService.getTaxCategoriesListAll())
                : new ArrayList<>();
        taxCategoryList.add(0, null);

        m_CategoryModel = new ComboBoxValModel(taxCategoryList);
        jcboUser.setModel(m_CategoryModel);
    }

    private void defaultValues() {

        jListTickets.setModel(new MyListData(new ArrayList()));
        jcboUser.setSelectedItem(null);
        jtxtTicketID.reset();
        jtxtTicketID.activate();
        jTxtStartDate.setText(null);
        jTxtEndDate.setText(null);
        jtxtCustomer.setText(null);
        jComboBoxTicket.setSelectedIndex(0);
        jcboUser.setSelectedItem(null);

        jcboMoney.setSelectedItem(((ListQBFModelNumber) jcboMoney.getModel()).getElementAt(0));
        jcboMoney.revalidate();
        jcboMoney.repaint();
        jtxtMoney.reset();
        jTxtStartDate.setText(null);
        jTxtEndDate.setText(null);
        jtxtCustomer.setText(null);

        jLblTicketCount.setVisible(false);
        jLblReturnCount.setVisible(false);
        jLblTicketCount.setText(null);

        Calendar now = Calendar.getInstance();
        now.set(Calendar.HOUR, 0);
        now.set(Calendar.MINUTE, 0);
        now.set(Calendar.SECOND, 0);
        now.set(Calendar.HOUR_OF_DAY, 0);
        jTxtStartDate.setText(Formats.DATETIME.formatValue(now.getTime()));
        repaint();
    }

    @Override
    public Object createValue() throws BasicException {

        Object[] afilter = new Object[14];

        if (jtxtTicketID.getText() == null || jtxtTicketID.getText().equals("")) {
            afilter[0] = QBFCompareEnum.COMP_NONE;
            afilter[1] = null;
        } else {
            afilter[0] = QBFCompareEnum.COMP_EQUALS;
            afilter[1] = jtxtTicketID.getValue();
        }

        switch (jComboBoxTicket.getSelectedIndex()) {
            case 2:
                afilter[2] = QBFCompareEnum.COMP_DISTINCT;
                afilter[3] = 2;
                break;
            case 0:
                afilter[2] = QBFCompareEnum.COMP_EQUALS;
                afilter[3] = 0;
                break;
            case 1:
                afilter[2] = QBFCompareEnum.COMP_EQUALS;
                afilter[3] = 1;
                break;
            default:
                break;
        }

        afilter[5] = jtxtMoney.getValue();
        afilter[4] = afilter[5] == null ? QBFCompareEnum.COMP_NONE : jcboMoney.getSelectedItem();

        Object startdate = Formats.TIMESTAMP.parseValue(jTxtStartDate.getText());
        Object enddate = Formats.TIMESTAMP.parseValue(jTxtEndDate.getText());

        afilter[6] = (startdate == null) ? QBFCompareEnum.COMP_NONE : QBFCompareEnum.COMP_GREATEROREQUALS;
        afilter[7] = startdate;
        afilter[8] = (enddate == null) ? QBFCompareEnum.COMP_NONE : QBFCompareEnum.COMP_LESS;
        afilter[9] = enddate;

        if (jcboUser.getSelectedItem() == null) {
            afilter[10] = QBFCompareEnum.COMP_NONE;
            afilter[11] = null;
        } else {
            afilter[10] = QBFCompareEnum.COMP_EQUALS;
            afilter[11] = ((TaxCategoryInfo) jcboUser.getSelectedItem()).getName();
        }

        if (jtxtCustomer.getText() == null || jtxtCustomer.getText().equals("")) {
            afilter[12] = QBFCompareEnum.COMP_NONE;
            afilter[13] = null;
        } else {
            afilter[12] = QBFCompareEnum.COMP_RE;
            afilter[13] = "%" + jtxtCustomer.getText() + "%";
        }

        return afilter;
    }

    private static class MyListData extends javax.swing.AbstractListModel {

        private final java.util.List m_data;

        public MyListData(java.util.List data) {
            m_data = data;
        }

        @Override
        public Object getElementAt(int index) {
            return m_data.get(index);
        }

        @Override
        public int getSize() {
            return m_data.size();
        }
    }

    public void setStartDate(Date d) {
        jTxtStartDate.setText(Formats.TIMESTAMP.formatValue(d));
    }

    public void setEndDate(Date d) {
        jTxtEndDate.setText(Formats.TIMESTAMP.formatValue(d));
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel3 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jtxtMoney = new com.openbravo.editor.JEditorCurrency();
        jcboUser = new javax.swing.JComboBox();
        jcboMoney = new javax.swing.JComboBox();
        jtxtTicketID = new com.openbravo.editor.JEditorIntegerPositive();
        labelCustomer = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jTxtStartDate = new javax.swing.JTextField();
        jTxtEndDate = new javax.swing.JTextField();
        btnDateStart = new javax.swing.JButton();
        btnDateEnd = new javax.swing.JButton();
        jtxtCustomer = new javax.swing.JTextField();
        btnCustomer = new javax.swing.JButton();
        jComboBoxTicket = new javax.swing.JComboBox();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jListTickets = new javax.swing.JList();
        jPanel6 = new javax.swing.JPanel();
        jbtnReset = new javax.swing.JButton();
        jbtnExecute = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jcmdCancel = new javax.swing.JButton();
        jcmdOK = new javax.swing.JButton();
        m_jKeys = new com.openbravo.editor.JEditorKeys();
        jPanel8 = new javax.swing.JPanel();
        jLblTicketCount = new javax.swing.JLabel();
        jLblReturnCount = new javax.swing.JLabel();

        setLayout(new java.awt.BorderLayout());

        jPanel3.setLayout(new java.awt.BorderLayout());

        jPanel5.setLayout(new java.awt.BorderLayout());

        jPanel7.setPreferredSize(new java.awt.Dimension(0, 210));

        jLabel1.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel1.setText(AppLocal.getIntString("label.ticketid")); // NOI18N
        jLabel1.setPreferredSize(new java.awt.Dimension(100, 30));

        jLabel6.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel6.setText(AppLocal.getIntString("label.user")); // NOI18N
        jLabel6.setPreferredSize(new java.awt.Dimension(100, 30));

        jLabel7.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel7.setText(AppLocal.getIntString("label.totalcash")); // NOI18N
        jLabel7.setPreferredSize(new java.awt.Dimension(100, 30));

        jtxtMoney.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jtxtMoney.setPreferredSize(new java.awt.Dimension(150, 30));

        jcboUser.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jcboUser.setPreferredSize(new java.awt.Dimension(220, 30));
        jcboUser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jcboUserActionPerformed(evt);
            }
        });

        jcboMoney.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jcboMoney.setPreferredSize(new java.awt.Dimension(150, 30));

        jtxtTicketID.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jtxtTicketID.setPreferredSize(new java.awt.Dimension(150, 30));

        labelCustomer.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        labelCustomer.setText(AppLocal.getIntString("label.customer")); // NOI18N
        labelCustomer.setPreferredSize(new java.awt.Dimension(100, 30));

        jLabel3.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel3.setText(AppLocal.getIntString("label.StartDate")); // NOI18N
        jLabel3.setPreferredSize(new java.awt.Dimension(100, 30));

        jLabel4.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel4.setText(AppLocal.getIntString("label.EndDate")); // NOI18N
        jLabel4.setPreferredSize(new java.awt.Dimension(100, 30));

        jTxtStartDate.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jTxtStartDate.setPreferredSize(new java.awt.Dimension(150, 30));

        jTxtEndDate.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jTxtEndDate.setPreferredSize(new java.awt.Dimension(150, 30));

        btnDateStart.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/date.png"))); // NOI18N
        btnDateStart.setToolTipText("Open Calendar");
        btnDateStart.setPreferredSize(new java.awt.Dimension(100, 30));
        btnDateStart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDateStartActionPerformed(evt);
            }
        });

        btnDateEnd.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/date.png"))); // NOI18N
        btnDateEnd.setToolTipText("Open Calendar");
        btnDateEnd.setPreferredSize(new java.awt.Dimension(100, 30));
        btnDateEnd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDateEndActionPerformed(evt);
            }
        });

        jtxtCustomer.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jtxtCustomer.setPreferredSize(new java.awt.Dimension(150, 30));

        btnCustomer.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/customer_sml.png"))); // NOI18N
        btnCustomer.setToolTipText("Open Customers");
        btnCustomer.setFocusPainted(false);
        btnCustomer.setFocusable(false);
        btnCustomer.setMargin(new java.awt.Insets(8, 14, 8, 14));
        btnCustomer.setPreferredSize(new java.awt.Dimension(100, 30));
        btnCustomer.setRequestFocusEnabled(false);
        btnCustomer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCustomerActionPerformed(evt);
            }
        });

        jComboBoxTicket.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jComboBoxTicket.setPreferredSize(new java.awt.Dimension(150, 30));

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jLabel6, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 115, Short.MAX_VALUE)
                    .addComponent(labelCustomer, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jtxtCustomer, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jTxtStartDate, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jTxtEndDate, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnDateEnd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnDateStart, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnCustomer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addComponent(jcboMoney, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jtxtMoney, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addComponent(jtxtTicketID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jComboBoxTicket, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jcboUser, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(jComboBoxTicket, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jtxtTicketID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnDateStart, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTxtStartDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTxtEndDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnDateEnd, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(labelCustomer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jtxtCustomer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnCustomer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jcboUser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jtxtMoney, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcboMoney, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(19, 19, 19))
        );

        jPanel5.add(jPanel7, java.awt.BorderLayout.CENTER);

        jPanel3.add(jPanel5, java.awt.BorderLayout.PAGE_START);

        jPanel4.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jPanel4.setLayout(new java.awt.BorderLayout());

        jScrollPane1.setPreferredSize(new java.awt.Dimension(258, 60));

        jListTickets.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jListTickets.setFocusable(false);
        jListTickets.setRequestFocusEnabled(false);
        jListTickets.setVisibleRowCount(6);
        jListTickets.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jListTicketsMouseClicked(evt);
            }
        });
        jListTickets.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jListTicketsValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(jListTickets);

        jPanel4.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jbtnReset.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jbtnReset.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/reload.png"))); // NOI18N
        jbtnReset.setText(AppLocal.getIntString("button.clean")); // NOI18N
        jbtnReset.setToolTipText("Clear Filter");
        jbtnReset.setPreferredSize(new java.awt.Dimension(110, 45));
        jbtnReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbtnResetActionPerformed(evt);
            }
        });
        jPanel6.add(jbtnReset);

        jbtnExecute.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jbtnExecute.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/ok.png"))); // NOI18N
        jbtnExecute.setText(AppLocal.getIntString("button.executefilter")); // NOI18N
        jbtnExecute.setToolTipText("Execute Filter");
        jbtnExecute.setFocusPainted(false);
        jbtnExecute.setFocusable(false);
        jbtnExecute.setPreferredSize(new java.awt.Dimension(110, 45));
        jbtnExecute.setRequestFocusEnabled(false);
        jbtnExecute.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbtnExecuteActionPerformed(evt);
            }
        });
        jPanel6.add(jbtnExecute);

        jPanel4.add(jPanel6, java.awt.BorderLayout.PAGE_START);

        jPanel3.add(jPanel4, java.awt.BorderLayout.CENTER);

        add(jPanel3, java.awt.BorderLayout.CENTER);

        jPanel2.setPreferredSize(new java.awt.Dimension(300, 250));

        jPanel1.setPreferredSize(new java.awt.Dimension(290, 55));

        jcmdCancel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jcmdCancel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/cancel.png"))); // NOI18N
        jcmdCancel.setText(AppLocal.getIntString("button.cancel")); // NOI18N
        jcmdCancel.setFocusPainted(false);
        jcmdCancel.setFocusable(false);
        jcmdCancel.setMargin(new java.awt.Insets(8, 16, 8, 16));
        jcmdCancel.setPreferredSize(new java.awt.Dimension(110, 45));
        jcmdCancel.setRequestFocusEnabled(false);
        jcmdCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jcmdCancelActionPerformed(evt);
            }
        });
        jPanel1.add(jcmdCancel);

        jcmdOK.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jcmdOK.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/ok.png"))); // NOI18N
        jcmdOK.setText(AppLocal.getIntString("button.ok")); // NOI18N
        jcmdOK.setEnabled(false);
        jcmdOK.setFocusPainted(false);
        jcmdOK.setFocusable(false);
        jcmdOK.setMargin(new java.awt.Insets(8, 16, 8, 16));
        jcmdOK.setMaximumSize(new java.awt.Dimension(103, 44));
        jcmdOK.setMinimumSize(new java.awt.Dimension(103, 44));
        jcmdOK.setPreferredSize(new java.awt.Dimension(110, 45));
        jcmdOK.setRequestFocusEnabled(false);
        jcmdOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jcmdOKActionPerformed(evt);
            }
        });
        jPanel1.add(jcmdOK);

        jPanel2.add(jPanel1);

        m_jKeys.setPreferredSize(new java.awt.Dimension(290, 300));
        m_jKeys.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jKeysActionPerformed(evt);
            }
        });
        jPanel2.add(m_jKeys);

        jPanel8.setMinimumSize(new java.awt.Dimension(250, 50));
        jPanel8.setPreferredSize(new java.awt.Dimension(290, 60));

        jLblTicketCount.setBackground(new java.awt.Color(51, 204, 255));
        jLblTicketCount.setFont(new java.awt.Font("Century Gothic", 0, 18)); // NOI18N
        jLblTicketCount.setForeground(new java.awt.Color(255, 255, 255));
        jLblTicketCount.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLblTicketCount.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(51, 204, 255)));
        jLblTicketCount.setOpaque(true);
        jLblTicketCount.setPreferredSize(new java.awt.Dimension(80, 50));
        jPanel8.add(jLblTicketCount);

        jLblReturnCount.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("pos_messages"); // NOI18N
        jLblReturnCount.setText(bundle.getString("label.ticketsfound")); // NOI18N
        jLblReturnCount.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 204, 255)));
        jLblReturnCount.setPreferredSize(new java.awt.Dimension(80, 50));
        jPanel8.add(jLblReturnCount);

        jPanel2.add(jPanel8);

        add(jPanel2, java.awt.BorderLayout.LINE_END);
    }// </editor-fold>//GEN-END:initComponents

    private void jcmdOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jcmdOKActionPerformed

        selectedTicket = (FindTicketsInfo) jListTickets.getSelectedValue();
        if (modalContext != null) {
            modalContext.setResult(selectedTicket);
            modalContext.close();
        }

    }//GEN-LAST:event_jcmdOKActionPerformed

    private void jcmdCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jcmdCancelActionPerformed

        selectedTicket = null;
        if (modalContext != null) {
            modalContext.close();
        }

    }//GEN-LAST:event_jcmdCancelActionPerformed

    private void jbtnExecuteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbtnExecuteActionPerformed

        executeSearch();

        jLblTicketCount.setVisible(true);
        jLblReturnCount.setVisible(true);
        jLblReturnCount.setText(" Tickets found");

    }//GEN-LAST:event_jbtnExecuteActionPerformed

    private void jListTicketsValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jListTicketsValueChanged

        jcmdOK.setEnabled(jListTickets.getSelectedValue() != null);

    }//GEN-LAST:event_jListTicketsValueChanged

    private void jListTicketsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jListTicketsMouseClicked

        if (evt.getClickCount() == 2) {
            selectedTicket = (FindTicketsInfo) jListTickets.getSelectedValue();
            if (modalContext != null) {
                modalContext.setResult(selectedTicket);
                modalContext.close();
            }
        }

    }//GEN-LAST:event_jListTicketsMouseClicked

    private void jbtnResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbtnResetActionPerformed

        defaultValues();
    }//GEN-LAST:event_jbtnResetActionPerformed

    private void btnDateStartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDateStartActionPerformed

        jLblTicketCount.setVisible(false);
        jLblReturnCount.setVisible(false);

        Date date;
        try {
            date = (Date) Formats.TIMESTAMP.parseValue(jTxtStartDate.getText());
        } catch (BasicException e) {
            date = null;
        }
        date = JCalendarPanel2.showCalendarTimeHours(this, date);
        if (date != null) {
            jTxtStartDate.setText(Formats.TIMESTAMP.formatValue(date));
        }
    }//GEN-LAST:event_btnDateStartActionPerformed

    private void btnDateEndActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDateEndActionPerformed
        jLblTicketCount.setVisible(false);
        jLblReturnCount.setVisible(false);

        Date date;
        try {
            date = (Date) Formats.TIMESTAMP.parseValue(jTxtEndDate.getText());
        } catch (BasicException e) {
            date = null;
        }
        date = JCalendarPanel2.showCalendarTimeHours(this, date);
        if (date != null) {
            jTxtEndDate.setText(Formats.TIMESTAMP.formatValue(date));
        }
    }//GEN-LAST:event_btnDateEndActionPerformed

    private void btnCustomerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCustomerActionPerformed
        jLblTicketCount.setVisible(false);
        jLblReturnCount.setVisible(false);

        CustomerInfo customer = JCustomerFinderPanel.show(this, customerService);

        try {
            jtxtCustomer.setText(customer == null
                    ? null
                    : customerService.findCustomerInfoExtById(customer.getId()).toString());
        } catch (BasicException e) {
            MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotfindcustomer"), e);
            msg.show(this);
        }
    }//GEN-LAST:event_btnCustomerActionPerformed

    private void jcboUserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jcboUserActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jcboUserActionPerformed

    private void m_jKeysActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jKeysActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_m_jKeysActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCustomer;
    private javax.swing.JButton btnDateEnd;
    private javax.swing.JButton btnDateStart;
    private javax.swing.JComboBox jComboBoxTicket;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel labelCustomer;
    private javax.swing.JLabel jLblReturnCount;
    private javax.swing.JLabel jLblTicketCount;
    private javax.swing.JList jListTickets;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton jbtnExecute;
    private javax.swing.JButton jbtnReset;
    private javax.swing.JComboBox jcboMoney;
    private javax.swing.JComboBox jcboUser;
    private javax.swing.JButton jcmdCancel;
    private javax.swing.JButton jcmdOK;
    private com.openbravo.editor.JEditorKeys m_jKeys;
    private javax.swing.JTextField jTxtEndDate;
    private javax.swing.JTextField jTxtStartDate;
    private javax.swing.JTextField jtxtCustomer;
    private com.openbravo.editor.JEditorCurrency jtxtMoney;
    private com.openbravo.editor.JEditorIntegerPositive jtxtTicketID;
    // End of variables declaration//GEN-END:variables
}
