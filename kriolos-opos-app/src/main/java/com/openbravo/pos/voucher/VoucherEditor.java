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

package com.openbravo.pos.voucher;

import com.openbravo.basic.AppLocal;
import com.openbravo.basic.BasicException;
import com.openbravo.data.user.DirtyManager;
import com.openbravo.data.user.EditorRecord;
import com.openbravo.format.Formats;
import com.openbravo.pos.admin.DataLogicSystem;
import com.openbravo.pos.customers.CustomerInfo;
import com.openbravo.pos.customers.CustomerService;
import com.openbravo.pos.customers.DataLogicCustomers;
import com.openbravo.pos.customers.JCustomerFinder;
import com.openbravo.pos.customers.JCustomerFinderPanel;
import com.openbravo.pos.customers.JDialogNewCustomer;
import com.openbravo.pos.panels.AppView;
import com.openbravo.pos.resources.ResourceService;
import com.openbravo.pos.util.FormatsDoubleConverter;
import com.openbravo.ui.data.binding.SwingBinder;
import java.awt.Component;
import java.awt.image.BufferedImage;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

/**
 * Modernized editor for Vouchers with SwingBinder two-way binding,
 * real-time validation, and headless testability.
 *
 * @author KiolOS
 * @author KriolOS
 */
public final class VoucherEditor extends javax.swing.JPanel implements EditorRecord {

    private static final Logger LOGGER = Logger.getLogger(VoucherEditor.class.getName());
    private static final long serialVersionUID = 1L;

    /**
     * Presentation model for two-way Swing data binding.
     */
    public static class VoucherModel {
        private String id;
        private String voucherNumber;
        private String customerId;
        private String customerName;
        private Double amount;
        private String status;

        public VoucherModel() {
        }

        public VoucherModel(String id, String voucherNumber, String customerId, String customerName, Double amount, String status) {
            this.id = id;
            this.voucherNumber = voucherNumber;
            this.customerId = customerId;
            this.customerName = customerName;
            this.amount = amount;
            this.status = status;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getVoucherNumber() {
            return voucherNumber;
        }

        public void setVoucherNumber(String voucherNumber) {
            this.voucherNumber = voucherNumber;
        }

        public String getCustomerId() {
            return customerId;
        }

        public void setCustomerId(String customerId) {
            this.customerId = customerId;
        }

        public String getCustomerName() {
            return customerName;
        }

        public void setCustomerName(String customerName) {
            this.customerName = customerName;
        }

        public Double getAmount() {
            return amount;
        }

        public void setAmount(Double amount) {
            this.amount = amount;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

    private String voucherId;
    private final CustomerService customerService;
    @Deprecated
    private final DataLogicCustomers dlCustomers;
    private final ResourceService resourceService;
    @Deprecated
    private final DataLogicSystem dlSystem;
    private final VoucherService voucherService;
    private CustomerInfo customerInfo;
    private final AppView m_app;
    private DirtyManager dirty;
    private SwingBinder<VoucherModel> binder;

    /**
     * Headless constructor for testing without active GUI/database session.
     *
     * @param dirty dirty state manager
     */
    public VoucherEditor(DirtyManager dirty) {
        this(dirty, null, null, null, null);
    }

    public VoucherEditor(DirtyManager dirty, AppView app) {
        this(dirty, app, null, null, null);
    }

    public VoucherEditor(DirtyManager dirty, AppView app, VoucherService voucherService) {
        this(dirty, app, voucherService, null, null);
    }

    public VoucherEditor(DirtyManager dirty, AppView app, VoucherService voucherService, CustomerService customerService) {
        this(dirty, app, voucherService, customerService, null);
    }

    public VoucherEditor(DirtyManager dirty, AppView app, VoucherService voucherService, CustomerService customerService, ResourceService resourceService) {
        this.m_app = app;
        this.dirty = dirty;

        initComponents();

        this.customerService = customerService != null ? customerService : (app != null ? app.getBean(CustomerService.class) : null);
        this.dlCustomers = (this.customerService instanceof DataLogicCustomers) ? (DataLogicCustomers) this.customerService : (app != null ? app.getBean(DataLogicCustomers.class) : null);
        this.resourceService = resourceService != null ? resourceService : (app != null ? app.getBean(ResourceService.class) : null);
        this.dlSystem = (this.resourceService instanceof DataLogicSystem) ? (DataLogicSystem) this.resourceService : null;
        this.voucherService = voucherService != null ? voucherService : (app != null ? app.getBean(VoucherService.class) : null);

        if (dirty != null) {
            voucherNumberTField.getDocument().addDocumentListener(dirty);
            voucherCustomerTField.getDocument().addDocumentListener(dirty);
            voucherAmountTField.getDocument().addDocumentListener(dirty);
            voucherStatusTField.getDocument().addDocumentListener(dirty);
        }

        printBtn.setVisible(true);
        jLblStatus.setIcon(null);
        statusIcon.setIcon(null);

        initValidator();
        writeValueEOF();
    }

    private void initValidator() {
        binder = new SwingBinder<>(new VoucherModel());

        // Mandatory Voucher Number
        binder.forField(voucherNumberTField)
                .asRequired(AppLocal.getIntString("validation.required", AppLocal.getIntString("label.Number")))
                .bind(VoucherModel::getVoucherNumber, VoucherModel::setVoucherNumber);

        // Mandatory Customer Name
        binder.forField(voucherCustomerTField)
                .asRequired(AppLocal.getIntString("validation.required", AppLocal.getIntString("label.customer")))
                .bind(VoucherModel::getCustomerName, VoucherModel::setCustomerName);

        // Mandatory Positive Amount
        binder.forField(voucherAmountTField)
                .withConverter(new FormatsDoubleConverter())
                .withValidator(v -> v != null && v > 0.0, AppLocal.getIntString("validation.number.positive", AppLocal.getIntString("label.paymenttotal")))
                .bind(VoucherModel::getAmount, VoucherModel::setAmount);

        // Mandatory Status
        binder.forField(voucherStatusTField)
                .asRequired(AppLocal.getIntString("validation.required", AppLocal.getIntString("label.Status")))
                .bind(VoucherModel::getStatus, VoucherModel::setStatus);

        binder.addStatusChangeListener(() -> {
            if (dirty != null) {
                dirty.setDirty(true);
            }
        });
    }

    public boolean isValid() {
        return binder == null || binder.validate();
    }

    VoucherModel toModel() {
        Double amt = null;
        try {
            amt = (Double) Formats.DOUBLE.parseValue(voucherAmountTField.getText());
        } catch (BasicException e) {
            // keep null
        }
        return new VoucherModel(
                voucherId,
                voucherNumberTField.getText(),
                customerInfo != null ? customerInfo.getId() : null,
                voucherCustomerTField.getText(),
                amt,
                voucherStatusTField.getText()
        );
    }

    private void updateStatusIcon(String status) {
        if (status == null) {
            jLblStatus.setIcon(null);
            statusIcon.setIcon(null);
            return;
        }
        switch (status) {
            case VoucherInfo.VOUCHER_STATUS_AVAILABLE -> {
                var url = getClass().getResource("/com/openbravo/images/ok.png");
                if (url != null) {
                    statusIcon.setIcon(new javax.swing.ImageIcon(url));
                }
                statusIcon.setToolTipText("Available");
            }
            case VoucherInfo.VOUCHER_STATUS_REDEEMED -> {
                var url = getClass().getResource("/com/openbravo/images/refundit.png");
                if (url != null) {
                    statusIcon.setIcon(new javax.swing.ImageIcon(url));
                }
                statusIcon.setToolTipText("Redeemed");
            }
            default -> {
                jLblStatus.setIcon(null);
                statusIcon.setIcon(null);
            }
        }
    }

    @Deprecated
    public DataLogicVouchers getDataLogicVouchers() {
        return voucherService instanceof DataLogicVouchers dl ? dl : null;
    }

    @Override
    public void writeValueEOF() {
        voucherId = null;
        customerInfo = null;
        if (binder != null) {
            binder.clear();
        } else {
            voucherNumberTField.setText(null);
            voucherCustomerTField.setText(null);
            voucherAmountTField.setText(null);
            voucherStatusTField.setText(null);
        }
        voucherNumberTField.setEnabled(false);
        voucherCustomerTField.setEnabled(false);
        voucherAmountTField.setEnabled(false);
        voucherStatusTField.setEnabled(false);
        customerSelectorBtn.setEnabled(false);
        printBtn.setEnabled(false);
        jLblStatus.setIcon(null);
        statusIcon.setIcon(null);
    }

    @Override
    public void writeValueInsert() {
        voucherId = UUID.randomUUID().toString();
        customerInfo = null;
        voucherNumberTField.setText(generateVoucherNumber());
        voucherNumberTField.setEnabled(true);
        voucherCustomerTField.setText(null);
        voucherCustomerTField.setEnabled(true);
        voucherAmountTField.setText(null);
        voucherAmountTField.setEnabled(true);
        voucherStatusTField.setText(VoucherInfo.VOUCHER_STATUS_AVAILABLE);
        voucherStatusTField.setEnabled(false);
        customerSelectorBtn.setEnabled(true);
        printBtn.setEnabled(true);
        updateStatusIcon(VoucherInfo.VOUCHER_STATUS_AVAILABLE);

        if (binder != null) {
            binder.readBean(toModel());
        }
    }

    @Override
    public void writeValueDelete(Object value) {
        if (value == null) {
            writeValueEOF();
            return;
        }

        String status = null;
        if (value instanceof Object[] attr) {
            voucherId = (String) attr[0];
            voucherNumberTField.setText(Formats.STRING.formatValue((String) attr[1]));
            String custId = attr[2] != null ? attr[2].toString() : null;
            if (customerService != null && custId != null) {
                try {
                    customerInfo = customerService.getCustomerInfo(custId);
                } catch (BasicException ex) {
                    LOGGER.log(Level.WARNING, "Exception fetching customer info", ex);
                }
            }
            voucherCustomerTField.setText(customerInfo != null ? customerInfo.getName() : (custId != null ? custId : ""));
            voucherAmountTField.setText(Formats.DOUBLE.formatValue((Double) attr[3]));
            status = Formats.STRING.formatValue((String) attr[4]);
            voucherStatusTField.setText(status);
        } else if (value instanceof VoucherInfo vi) {
            voucherId = vi.getId();
            voucherNumberTField.setText(Formats.STRING.formatValue(vi.getVoucherNumber()));
            voucherCustomerTField.setText(Formats.STRING.formatValue(vi.getCustomerName()));
            voucherAmountTField.setText(Formats.DOUBLE.formatValue(vi.getAmount()));
            status = Formats.STRING.formatValue(vi.getStatus());
            voucherStatusTField.setText(status);
        } else if (value instanceof VoucherModel vm) {
            voucherId = vm.getId();
            voucherNumberTField.setText(Formats.STRING.formatValue(vm.getVoucherNumber()));
            voucherCustomerTField.setText(Formats.STRING.formatValue(vm.getCustomerName()));
            voucherAmountTField.setText(Formats.DOUBLE.formatValue(vm.getAmount()));
            status = Formats.STRING.formatValue(vm.getStatus());
            voucherStatusTField.setText(status);
        }

        if (VoucherInfo.VOUCHER_STATUS_AVAILABLE.equals(status)) {
            voucherNumberTField.setEnabled(false);
            voucherCustomerTField.setEnabled(false);
            voucherAmountTField.setEnabled(false);
            voucherStatusTField.setEnabled(false);
            customerSelectorBtn.setEnabled(false);
            printBtn.setEnabled(false);
            updateStatusIcon(status);
        } else {
            JOptionPane.showMessageDialog(this,
                    AppLocal.getIntString("message.voucherdelete"),
                    AppLocal.getIntString("Check"),
                    JOptionPane.WARNING_MESSAGE);
        }

        if (binder != null) {
            binder.readBean(toModel());
        }
    }

    @Override
    public void writeValueEdit(Object value) {
        if (value == null) {
            writeValueEOF();
            return;
        }

        String status = null;
        if (value instanceof Object[] attr) {
            voucherId = (String) attr[0];
            voucherNumberTField.setText(Formats.STRING.formatValue((String) attr[1]));
            String custId = attr[2] != null ? attr[2].toString() : null;
            if (customerService != null && custId != null) {
                try {
                    customerInfo = customerService.getCustomerInfo(custId);
                } catch (BasicException ex) {
                    LOGGER.log(Level.WARNING, "Exception fetching customer info", ex);
                }
            }
            voucherCustomerTField.setText(customerInfo != null ? customerInfo.getName() : (custId != null ? custId : ""));
            voucherAmountTField.setText(Formats.DOUBLE.formatValue((Double) attr[3]));
            status = Formats.STRING.formatValue((String) attr[4]);
            voucherStatusTField.setText(status);
        } else if (value instanceof VoucherInfo vi) {
            voucherId = vi.getId();
            voucherNumberTField.setText(Formats.STRING.formatValue(vi.getVoucherNumber()));
            voucherCustomerTField.setText(Formats.STRING.formatValue(vi.getCustomerName()));
            voucherAmountTField.setText(Formats.DOUBLE.formatValue(vi.getAmount()));
            status = Formats.STRING.formatValue(vi.getStatus());
            voucherStatusTField.setText(status);
        } else if (value instanceof VoucherModel vm) {
            voucherId = vm.getId();
            voucherNumberTField.setText(Formats.STRING.formatValue(vm.getVoucherNumber()));
            voucherCustomerTField.setText(Formats.STRING.formatValue(vm.getCustomerName()));
            voucherAmountTField.setText(Formats.DOUBLE.formatValue(vm.getAmount()));
            status = Formats.STRING.formatValue(vm.getStatus());
            voucherStatusTField.setText(status);
        }

        printBtn.setEnabled(true);

        if (VoucherInfo.VOUCHER_STATUS_REDEEMED.equals(status)) {
            voucherNumberTField.setEnabled(false);
            voucherCustomerTField.setEnabled(false);
            voucherAmountTField.setEnabled(false);
            voucherStatusTField.setEnabled(false);
            customerSelectorBtn.setEnabled(false);
        } else {
            voucherNumberTField.setEnabled(true);
            voucherCustomerTField.setEnabled(true);
            voucherAmountTField.setEnabled(true);
            voucherStatusTField.setEnabled(false);
            customerSelectorBtn.setEnabled(true);
        }

        updateStatusIcon(status);

        if (binder != null) {
            binder.readBean(toModel());
        }
    }

    @Override
    public Object createValue() throws BasicException {
        if (binder != null) {
            try {
                binder.writeBean(toModel());
            } catch (SwingBinder.ValidationException ex) {
                // validation status surfaced via isValid()
            }
        }

        Double amount = null;
        try {
            amount = (Double) Formats.DOUBLE.parseValue(voucherAmountTField.getText());
        } catch (BasicException ex) {
            // fallback
        }

        Object[] attr = new Object[5];
        attr[0] = voucherId;
        attr[1] = voucherNumberTField.getText();
        attr[2] = customerInfo != null ? customerInfo.getId() : null;
        attr[3] = amount;
        attr[4] = voucherStatusTField.getText();

        return attr;
    }

    @Override
    public Component getComponent() {
        return this;
    }

    @Override
    public void refresh() {
    }

    JTextField getVoucherNumberField() {
        return voucherNumberTField;
    }

    JTextField getCustomerField() {
        return voucherCustomerTField;
    }

    JTextField getAmountField() {
        return voucherAmountTField;
    }

    JTextField getStatusField() {
        return voucherStatusTField;
    }

    JButton getPrintButton() {
        return printBtn;
    }

    JButton getCustomerSelectorButton() {
        return customerSelectorBtn;
    }

    JLabel getStatusIcon() {
        return statusIcon;
    }

    SwingBinder<VoucherModel> getBinder() {
        return binder;
    }

    void setCustomerInfo(CustomerInfo customerInfo) {
        this.customerInfo = customerInfo;
        if (customerInfo != null) {
            voucherCustomerTField.setText(customerInfo.getName());
        }
    }

    CustomerInfo getCustomerInfo() {
        return customerInfo;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel2 = new javax.swing.JLabel();
        voucherNumberTField = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        voucherCustomerTField = new javax.swing.JTextField();
        voucherAmountTField = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        printBtn = new javax.swing.JButton();
        jLblStatus = new javax.swing.JLabel();
        voucherStatusTField = new javax.swing.JTextField();
        customerSelectorBtn = new javax.swing.JButton();
        statusIcon = new javax.swing.JLabel();

        setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N

        jLabel2.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel2.setText(AppLocal.getIntString("label.Number")); // NOI18N
        jLabel2.setPreferredSize(new java.awt.Dimension(100, 30));

        voucherNumberTField.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        voucherNumberTField.setPreferredSize(new java.awt.Dimension(240, 30));

        jLabel3.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel3.setText(AppLocal.getIntString("label.customer")); // NOI18N
        jLabel3.setPreferredSize(new java.awt.Dimension(100, 30));

        voucherCustomerTField.setEditable(false);
        voucherCustomerTField.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        voucherCustomerTField.setPreferredSize(new java.awt.Dimension(240, 30));

        voucherAmountTField.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        voucherAmountTField.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        voucherAmountTField.setPreferredSize(new java.awt.Dimension(240, 30));

        jLabel5.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel5.setText(AppLocal.getIntString("label.paymenttotal")); // NOI18N
        jLabel5.setPreferredSize(new java.awt.Dimension(100, 30));

        printBtn.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        printBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/printer24.png"))); // NOI18N
        printBtn.setText(AppLocal.getIntString("button.print")); // NOI18N
        printBtn.setToolTipText(AppLocal.getIntString("button.print")); // NOI18N
        printBtn.setFocusPainted(false);
        printBtn.setFocusable(false);
        printBtn.setMargin(new java.awt.Insets(8, 14, 8, 14));
        printBtn.setPreferredSize(new java.awt.Dimension(80, 45));
        printBtn.setRequestFocusEnabled(false);
        printBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                printBtnActionPerformed(evt);
            }
        });

        jLblStatus.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLblStatus.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/ok.png"))); // NOI18N
        jLblStatus.setText(AppLocal.getIntString("label.Status")); // NOI18N
        jLblStatus.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        jLblStatus.setPreferredSize(new java.awt.Dimension(100, 30));

        voucherStatusTField.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        voucherStatusTField.setPreferredSize(new java.awt.Dimension(240, 30));

        customerSelectorBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/customer_sml.png"))); // NOI18N
        customerSelectorBtn.setToolTipText("Open Customers");
        customerSelectorBtn.setFocusPainted(false);
        customerSelectorBtn.setFocusable(false);
        customerSelectorBtn.setMargin(new java.awt.Insets(8, 14, 8, 14));
        customerSelectorBtn.setPreferredSize(new java.awt.Dimension(100, 30));
        customerSelectorBtn.setRequestFocusEnabled(false);
        customerSelectorBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                customerSelectorBtnActionPerformed(evt);
            }
        });

        statusIcon.setToolTipText("");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(voucherNumberTField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(voucherCustomerTField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(26, 26, 26)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(statusIcon, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(customerSelectorBtn, javax.swing.GroupLayout.DEFAULT_SIZE, 62, Short.MAX_VALUE)))))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(voucherAmountTField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLblStatus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(voucherStatusTField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(printBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(72, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(voucherNumberTField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(voucherCustomerTField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(voucherAmountTField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLblStatus, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(voucherStatusTField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(statusIcon, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addComponent(customerSelectorBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 7, Short.MAX_VALUE)
                .addComponent(printBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12))
        );
    }// </editor-fold>//GEN-END:initComponents

private void printBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_printBtnActionPerformed

    try {
        VoucherInfo voucherInfo = voucherService.getVoucherAll(voucherId);
        BufferedImage image = (resourceService != null) ? resourceService.getResourceAsImage("Window.Logo") : null;
        if (voucherInfo != null) {
            JVoucherReportPanel.show(this, m_app, voucherInfo, image);
        }

    } catch (BasicException ex) {
        LOGGER.log(Level.WARNING, "Exception Create voucher printer dialog", ex);
    }
}//GEN-LAST:event_printBtnActionPerformed

    private void customerSelectorBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_customerSelectorBtnActionPerformed
        if (customerService != null) {
            CustomerInfo selected = JCustomerFinderPanel.show(this, customerService);
            if (selected != null) {
                customerInfo = selected;
                voucherCustomerTField.setText(customerInfo.getName());
                if (binder != null) {
                    binder.readBean(toModel());
                }
            }
        }
    }//GEN-LAST:event_customerSelectorBtnActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton customerSelectorBtn;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLblStatus;
    private javax.swing.JButton printBtn;
    private javax.swing.JLabel statusIcon;
    private javax.swing.JTextField voucherAmountTField;
    private javax.swing.JTextField voucherCustomerTField;
    private javax.swing.JTextField voucherNumberTField;
    private javax.swing.JTextField voucherStatusTField;
    // End of variables declaration//GEN-END:variables

    public boolean isDataValid() {
        return isValid();
    }

    /** 
     * Voucher Number generator
     * 
     * @return voucher number ("VO-{MM-yy}-{SEQ}" e.g. "VO-10-26-00001")
     */
    public String generateVoucherNumber() {
        if (voucherService == null) {
            return "VO-AUTO-00001";
        }
        try {
            return voucherService.generateNextVoucherNumber();
        } catch (BasicException ex) {
            LOGGER.log(Level.WARNING, "Exception generate voucher number", ex);
            return "VO-AUTO-00001";
        }
    }

}
