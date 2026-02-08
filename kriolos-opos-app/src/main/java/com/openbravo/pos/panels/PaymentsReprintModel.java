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
import com.openbravo.format.Formats;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.util.StringUtils;
import java.util.ArrayList;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import com.openbravo.pos.forms.BeanFactoryException;
import com.openbravo.pos.forms.DataLogicSystem;
import com.openbravo.pos.payment.CashManagementService;
import com.openbravo.pos.payment.CashManagementServiceImpl;
import com.openbravo.pos.payment.CloseCash;
import com.openbravo.pos.reports.CategorySalesLine;
import com.openbravo.pos.reports.DrawerOpenedLines;
import com.openbravo.pos.reports.FinancialReport;
import com.openbravo.pos.reports.FinancialReportService;
import com.openbravo.pos.reports.FinancialReportServiceImpl;
import com.openbravo.pos.reports.PaymentsListLine;
import com.openbravo.pos.reports.ProductSalesLine;
import com.openbravo.pos.reports.RemovedProductLines;
import com.openbravo.pos.reports.SalesLine;

public class PaymentsReprintModel {

    private static final Logger LOGGER = Logger.getLogger(PaymentsReprintModel.class.getName());
    private String host;
    private String user;
    private int hostSequence;

    private Date startDate;
    private Date endDate;

    private Integer payments;
    private Double paymentsTotal;
    private List<PaymentsListLine> paymentsLines;

    private Integer categorySalesRows;
    private Double categorySalesTotalUnits;
    private Double categorySalesTotal;
    private List<CategorySalesLine> CategorySalesLines;

    private Integer productSalesRows;
    private Double productSalesTotalUnits;
    private Double productSalesTotal;
    private List<ProductSalesLine> productSalesLines;

    private List<RemovedProductLines> removedSalesLines;

    private List<DrawerOpenedLines> drawerOpenedLines;

    private final static String[] PAYMENTHEADERS = { "label.Payment", "label.money" };

    private Integer salesNum;
    private Double salesBase;
    private Double salesTaxes;
    private Double salesTaxNet;
    private List<SalesLine> salesLines;

    private final static String[] SALEHEADERS = { "label.taxcategory", "label.totaltax", "label.totalnet" };

    private PaymentsReprintModel() {
    }

    public static PaymentsReprintModel emptyInstance() {

        PaymentsReprintModel p = new PaymentsReprintModel();

        p.payments = 0;
        p.paymentsTotal = 0.0;
        p.paymentsLines = new ArrayList<>();

        p.categorySalesRows = 0;
        p.categorySalesTotalUnits = 0.0;
        p.categorySalesTotal = 0.0;
        p.CategorySalesLines = new ArrayList<>();

        p.salesNum = 0;
        p.salesBase = 0.0;
        p.salesTaxes = 0.0;
        p.salesTaxNet = 0.0;

        p.productSalesRows = 0;
        p.productSalesTotalUnits = 0.0;
        p.productSalesTotal = 0.0;
        p.productSalesLines = new ArrayList<>();
        p.removedSalesLines = new ArrayList<>();

        p.salesLines = new ArrayList<>();

        return p;
    }

    /**
     *
     * @param app
     * @return
     * @throws BasicException
     */
    public static PaymentsReprintModel loadInstance(AppView app) throws BasicException {

        PaymentsReprintModel p = new PaymentsReprintModel();

        p.user = app.getAppUserView().getUser().getName();
        p.host = app.getProperties().getHost();

        JFrame frame = new JFrame("Sequence");
        String sequenceStr = JOptionPane.showInputDialog(frame,
                AppLocal.getIntString("message.ccentersequence"),
                JOptionPane.INFORMATION_MESSAGE);

        int sequence = -1;
        if (sequenceStr != null) {
            try {
                sequence = Integer.parseInt(sequenceStr);
                p.hostSequence = sequence;
            } catch (NumberFormatException e) {
                // Ignore or handle
            }
        } else {
            // Cancelled
            return null;
        }

        if (sequence != -1) {
            DataLogicSystem dlSystem = null;
            try {
                dlSystem = (DataLogicSystem) app.getBean("com.openbravo.pos.forms.DataLogicSystem");
            } catch (BeanFactoryException e) {
                // Return null or throw BasicException. Since we are just reprinting, logging
                // might be enough,
                // but CashManagementService might need it.
                // For now, we proceed, as getCloseCashBySequence doesn't strictly need it, but
                // it is better to have it.
                LOGGER.warning("Could not load DataLogicSystem bean.");
            }

            CashManagementService cashService = new CashManagementServiceImpl(app.getSession(), dlSystem);
            CloseCash ccash = cashService.getCloseCashBySequence(p.host, sequence);

            if (ccash == null) {
                JOptionPane.showMessageDialog(frame,
                        AppLocal.getIntString("message.ccsequencenotfound"),
                        "",
                        JOptionPane.WARNING_MESSAGE);
                return null;
            } else {
                p.startDate = ccash.getDatestart();
                p.endDate = ccash.getDateend();

                FinancialReportService reportService = new FinancialReportServiceImpl(app.getSession());
                FinancialReport report = reportService.getFinancialReport(ccash.getMoney(), p.startDate, p.endDate);

                // Map FinancialReport to PaymentsReprintModel
                p.payments = report.getPaymentCount();
                p.paymentsTotal = report.getPaymentTotal();
                p.paymentsLines = report.getPaymentLines();

                p.categorySalesRows = report.getCategorySalesRows();
                p.categorySalesTotalUnits = report.getCategorySalesTotalUnits();
                p.categorySalesTotal = report.getCategorySalesTotal();
                p.CategorySalesLines = report.getCategorySalesLines();

                p.salesNum = report.getSalesCount();
                p.salesBase = report.getSalesBase();
                p.salesTaxes = report.getSalesTaxes();

                p.salesLines = report.getSalesLines();
                p.removedSalesLines = report.getRemovedProductLines();
                p.drawerOpenedLines = report.getDrawerOpenedLines();

                p.productSalesRows = report.getProductSalesRows();
                p.productSalesTotalUnits = report.getProductSalesTotalUnits();
                p.productSalesTotal = report.getProductSalesTotal();
                p.productSalesLines = report.getProductSalesLines();

                return p;
            }
        }
        return null;
    }

    public int getPayments() {
        return payments;
    }

    public double getTotal() {
        return paymentsTotal;
    }

    public String getHost() {
        return host;
    }

    public String getUser() {
        return user;
    }

    public int getSequence() {
        return hostSequence;
    }

    public Date getDateStart() {
        return startDate;
    }

    public void setDateEnd(Date dValue) {
        endDate = dValue;
    }

    public Date getDateEnd() {
        return endDate;
    }

    public String printHost() {
        return StringUtils.encodeXML(host);
    }

    public String printUser() {
        return StringUtils.encodeXML(user);
    }

    public String printSequence() {
        return Formats.INT.formatValue(hostSequence);
    }

    public String printDateStart() {
        return Formats.TIMESTAMP.formatValue(startDate);
    }

    public String printDateEnd() {
        return Formats.TIMESTAMP.formatValue(endDate);
    }

    public String printPayments() {
        return Formats.INT.formatValue(payments);
    }

    public String printPaymentsTotal() {
        return Formats.CURRENCY.formatValue(paymentsTotal);
    }

    public List<PaymentsListLine> getPaymentLines() {
        return paymentsLines;
    }

    public int getSales() {
        return salesNum == null ? 0 : salesNum;
    }

    public String printSales() {
        return Formats.INT.formatValue(salesNum);
    }

    public String printSalesBase() {
        return Formats.CURRENCY.formatValue(salesBase);
    }

    public String printSalesTaxes() {
        return Formats.CURRENCY.formatValue(salesTaxes);
    }

    public String printSalesTotal() {
        return Formats.CURRENCY.formatValue((salesBase == null || salesTaxes == null)
                ? null
                : salesBase + salesTaxes);
    }

    public List<SalesLine> getSaleLines() {
        return salesLines;
    }

    public double getCategorySalesRows() {
        return categorySalesRows;
    }

    public String printCategorySalesRows() {
        return Formats.INT.formatValue(categorySalesRows);
    }

    public double getCategorySalesTotalUnits() {
        return categorySalesTotalUnits;
    }

    public String printCategorySalesTotalUnits() {
        return Formats.DOUBLE.formatValue(categorySalesTotalUnits);
    }

    public double getCategorySalesTotal() {
        return categorySalesTotal;
    }

    public String printCategorySalesTotal() {
        return Formats.CURRENCY.formatValue(categorySalesTotal);
    }

    public List<CategorySalesLine> getCategorySalesLines() {
        return CategorySalesLines;
    }

    public double getProductSalesRows() {
        return productSalesRows;
    }

    public String printProductSalesRows() {
        return Formats.INT.formatValue(productSalesRows);
    }

    public double getProductSalesTotalUnits() {
        return productSalesTotalUnits;
    }

    public String printProductSalesTotalUnits() {
        return Formats.DOUBLE.formatValue(productSalesTotalUnits);
    }

    public double getProductSalesTotal() {
        return productSalesTotal;
    }

    public String printProductSalesTotal() {
        return Formats.CURRENCY.formatValue(productSalesTotal);
    }

    public List<ProductSalesLine> getProductSalesLines() {
        return productSalesLines;
    }

    public List<RemovedProductLines> getRemovedProductLines() {
        return removedSalesLines;
    }

    public List<DrawerOpenedLines> getDrawerOpenedLines() {
        return drawerOpenedLines;
    }

    public AbstractTableModel getPaymentsReprintModel() {
        return new AbstractTableModel() {
            @Override
            public String getColumnName(int column) {
                return AppLocal.getIntString(PAYMENTHEADERS[column]);
            }

            @Override
            public int getRowCount() {
                return paymentsLines.size();
            }

            @Override
            public int getColumnCount() {
                return PAYMENTHEADERS.length;
            }

            @Override
            public Object getValueAt(int row, int column) {
                PaymentsListLine l = paymentsLines.get(row);
                switch (column) {
                    case 0:
                        return l.getType();
                    case 1:
                        return l.getValue();
                    default:
                        return null;
                }
            }
        };
    }

    public AbstractTableModel getSalesModel() {
        return new AbstractTableModel() {
            @Override
            public String getColumnName(int column) {
                return AppLocal.getIntString(SALEHEADERS[column]);
            }

            @Override
            public int getRowCount() {
                return salesLines.size();
            }

            @Override
            public int getColumnCount() {
                return SALEHEADERS.length;
            }

            @Override
            public Object getValueAt(int row, int column) {
                SalesLine l = salesLines.get(row);
                switch (column) {
                    case 0:
                        return l.getTaxName();
                    case 1:
                        return l.getTaxes();
                    case 2:
                        return l.getTaxNet();
                    default:
                        return null;
                }
            }
        };
    }
}
