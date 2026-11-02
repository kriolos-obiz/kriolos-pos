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
package com.openbravo.pos.reports;

import com.openbravo.basic.BasicException;
import com.openbravo.format.Formats;
import com.openbravo.basic.AppLocal;
import com.openbravo.pos.panels.AppView;
import com.openbravo.util.StringUtils;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.openbravo.pos.reports.CategorySalesLine;
import com.openbravo.pos.reports.DrawerOpenedLines;
import com.openbravo.pos.reports.FinancialReport;
import com.openbravo.pos.reports.FinancialReportService;
import com.openbravo.pos.reports.FinancialReportServiceImpl;
import com.openbravo.pos.reports.PaymentsListLine;
import com.openbravo.pos.reports.ProductSalesLine;
import com.openbravo.pos.reports.RemovedProductLines;
import com.openbravo.pos.reports.SalesLine;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @authors adrianromero, jackgerrard, janarnagel
 *
 */
public class CashReport {

    private String machineName;
    private String username;
    private int sequenceNumber;
    private Date startDate;
    private Date endDate;
    private Date generatedDate;
    private Date printDate;

    private Integer paymentsCount;
    private Double paymentsTotal;
    private java.util.List<PaymentsListLine> paymentLines;

    // JG 9 Nov 12
    private Integer categorySalesRows;
    private Double categorySalesTotalUnits;
    private Double categorySalesTotal;
    private java.util.List<CategorySalesLine> categorySalesLines;
    // end

    // by janar153 @ 01.12.2013
    private Integer productSalesCount;
    private Double productSalesTotalUnits;
    private Double productSalesTotal;
    private java.util.List<ProductSalesLine> productSalesLines;
    // end

    // added by janar153 @ 29.12.2013
    private java.util.List<RemovedProductLines> removedProductLines;

    private java.util.List<DrawerOpenedLines> drawerOpenedLines;

    private final static String[] PAYMENTHEADERS = { "label.Payment", "label.paymenttotal", "label.qty" };

    private Integer salesCount;
    private Double salesBaseValue;
    private Double salesTaxes;
    private Double salesTaxNet;
    private java.util.List<SalesLine> salesLines;

    private final static String[] SALEHEADERS = { "label.taxcategory", "label.totaltax", "label.totalnet" };

    private CashReport() {
    }

    /**
     *
     * @return
     */
    public static CashReport emptyInstance() {

        CashReport p = new CashReport();

        p.paymentsCount = 0;
        p.paymentsTotal = 0.0;
        // JG 16 May 2013 use diamond inference
        p.paymentLines = new ArrayList<>();

        // JG 9 Nov 12
        p.categorySalesRows = 0;
        p.categorySalesTotalUnits = 0.0;
        p.categorySalesTotal = 0.0;
        p.categorySalesLines = new ArrayList<>();
        // end
        p.salesCount = null;
        p.salesBaseValue = null;
        p.salesTaxes = null;
        p.salesTaxNet = null;

        // JG 16 May 2013 use diamond inference
        // by janar153 @ 01.12.2013
        p.productSalesCount = 0;
        p.productSalesTotalUnits = 0.0;
        p.productSalesTotal = 0.0;
        p.productSalesLines = new ArrayList<>();
        p.removedProductLines = new ArrayList<>();

        p.salesLines = new ArrayList<>();

        return p;
    }

    /**
     *
     * @param app
     * @return
     * @throws BasicException
     */
    public static CashReport loadInstance(AppView app) throws BasicException {

        CashReport p = CashReport.emptyInstance();

        // Global Properties
        p.machineName = app.getProperties().getHost();
        p.username = app.getAppUserView().getUser().getName();
        p.sequenceNumber = app.getActiveCashSequence();
        p.startDate = app.getActiveCashDateStart();
        p.endDate = null;

        FinancialReportService reportService = new FinancialReportServiceImpl(app.getSession());
        FinancialReport report = reportService.getFinancialReport(app.getActiveCashIndex(), p.startDate, p.endDate);

        // Map FinancialReport to PaymentsModel
        p.paymentsCount = report.getPaymentCount();
        p.paymentsTotal = report.getPaymentTotal();
        p.paymentLines = report.getPaymentLines();

        p.categorySalesRows = report.getCategorySalesRows();
        p.categorySalesTotalUnits = report.getCategorySalesTotalUnits();
        p.categorySalesTotal = report.getCategorySalesTotal();
        p.categorySalesLines = report.getCategorySalesLines();

        p.salesCount = report.getSalesCount();
        p.salesBaseValue = report.getSalesBase();

        p.salesTaxes = report.getSalesTaxes();
        // Recalculate TaxNet if likely not in report or add to report.
        // For now we trust the service/report to provide what's needed or calculate it.
        // Original logic had it from a query.
        // Let's assume for now we use the report data.

        p.salesLines = report.getSalesLines();
        p.removedProductLines = report.getRemovedProductLines();
        p.drawerOpenedLines = report.getDrawerOpenedLines();

        p.productSalesCount = report.getProductSalesRows();
        p.productSalesTotalUnits = report.getProductSalesTotalUnits();
        p.productSalesTotal = report.getProductSalesTotal();
        p.productSalesLines = report.getProductSalesLines();

        return p;
    }

    /**
     * Converts this UI report representation into an immutable domain {@link FinancialReport}.
     *
     * @return domain FinancialReport instance
     */
    public FinancialReport toFinancialReport() {
        FinancialReport fr = new FinancialReport();
        fr.setHost(machineName);
        fr.setUser(username);
        fr.setSequence(sequenceNumber);
        fr.setDateStart(startDate);
        fr.setDateEnd(endDate);
        fr.setPaymentCount(paymentsCount != null ? paymentsCount : 0);
        fr.setPaymentTotal(paymentsTotal != null ? paymentsTotal : 0.0);
        fr.setPaymentLines(paymentLines);
        fr.setSalesCount(salesCount != null ? salesCount : 0);
        fr.setSalesBase(salesBaseValue != null ? salesBaseValue : 0.0);
        fr.setSalesTaxes(salesTaxes != null ? salesTaxes : 0.0);
        fr.setSalesLines(salesLines);
        return fr;
    }

    /**
     *
     * @return
     */
    public int getPayments() {
        return paymentsCount;
    }

    /**
     *
     * @return
     */
    public double getTotal() {
        return paymentsTotal;
    }

    /**
     *
     * @return
     */
    public String getHost() {
        return machineName;
    }

    /**
     *
     * @return
     */
    public String getUser() {
        return username;
    }

    /**
     *
     * @return
     */
    public int getSequence() {
        return sequenceNumber;
    }

    public String getPrintDate() {
        Date m_dPrintDate = new Date();
        return Formats.TIMESTAMP.formatValue(m_dPrintDate);
    }

    /**
     *
     * @return
     */
    public Date getDateStart() {
        return startDate;
    }

    /**
     *
     * @param dValue
     */
    public void setDateEnd(Date dValue) {
        endDate = dValue;
    }

    /**
     *
     * @return
     */
    public Date getDateEnd() {
        return endDate;
    }

    /**
     *
     * @return
     */
    public String printHost() {
        return StringUtils.encodeXML(machineName);
    }

    /**
     *
     * @return
     */
    public String printUser() {
        return StringUtils.encodeXML(username);
    }

    /**
     *
     * @return
     */
    public String printSequence() {
        return Formats.INT.formatValue(sequenceNumber);
    }

    public String printDate() {
        Date m_dPrintDate = new Date();
        return Formats.TIMESTAMP.formatValue(m_dPrintDate);
    }

    /**
     *
     * @return
     */
    public String printDateStart() {
        return Formats.TIMESTAMP.formatValue(startDate);
    }

    /**
     *
     * @return
     */
    public String printDateEnd() {
        return Formats.TIMESTAMP.formatValue(endDate);
    }

    /**
     *
     * @return
     */
    public String printPayments() {
        return Formats.INT.formatValue(paymentsCount);
    }

    /**
     *
     * @return
     */
    public String printPaymentsTotal() {
        return Formats.CURRENCY.formatValue(paymentsTotal);
    }

    /**
     *
     * @return
     */
    public List<PaymentsListLine> getPaymentLines() {
        return paymentLines;
    }

    /**
     *
     * @return
     */
    public int getSales() {
        return salesCount == null ? 0 : salesCount;
    }

    /**
     *
     * @return
     */
    public String printSales() {
        return Formats.INT.formatValue(salesCount);
    }

    /**
     *
     * @return
     */
    public String printSalesBase() {
        return Formats.CURRENCY.formatValue(salesBaseValue);
    }

    /**
     *
     * @return
     */
    public String printSalesTaxes() {
        return Formats.CURRENCY.formatValue(salesTaxes);
    }

    /**
     *
     * @return
     */
    public String printSalesTotal() {
        return Formats.CURRENCY.formatValue((salesBaseValue == null || salesTaxes == null)
                ? null
                : salesBaseValue + salesTaxes);
    }

    /**
     *
     * @return
     */
    public List<SalesLine> getSaleLines() {
        return salesLines;
    }

    // JG 9 Nov 12
    /**
     *
     * @return
     */
    public double getCategorySalesRows() {
        return categorySalesRows;
    }

    /**
     *
     * @return
     */
    public String printCategorySalesRows() {
        return Formats.INT.formatValue(categorySalesRows);
    }

    /**
     *
     * @return
     */
    public double getCategorySalesTotalUnits() {
        return categorySalesTotalUnits;
    }

    /**
     *
     * @return
     */
    public String printCategorySalesTotalUnits() {
        return Formats.DOUBLE.formatValue(categorySalesTotalUnits);
    }

    /**
     *
     * @return
     */
    public double getCategorySalesTotal() {
        return categorySalesTotal;
    }

    /**
     *
     * @return
     */
    public String printCategorySalesTotal() {
        return Formats.CURRENCY.formatValue(categorySalesTotal);
    }

    /**
     *
     * @return
     */
    public List<CategorySalesLine> getCategorySalesLines() {
        return categorySalesLines;
    }
    // end

    // by janar153 @ 01.12.2013
    /**
     *
     * @return
     */
    public double getProductSalesRows() {
        return productSalesCount;
    }

    /**
     *
     * @return
     */
    public String printProductSalesRows() {
        return Formats.INT.formatValue(productSalesCount);
    }

    /**
     *
     * @return
     */
    public double getProductSalesTotalUnits() {
        return productSalesTotalUnits;
    }

    /**
     *
     * @return
     */
    public String printProductSalesTotalUnits() {
        return Formats.DOUBLE.formatValue(productSalesTotalUnits);
    }

    /**
     *
     * @return
     */
    public double getProductSalesTotal() {
        return productSalesTotal;
    }

    /**
     *
     * @return
     */
    public String printProductSalesTotal() {
        return Formats.CURRENCY.formatValue(productSalesTotal);
    }

    /**
     *
     * @return
     */
    public List<ProductSalesLine> getProductSalesLines() {
        return productSalesLines;
    }
    // end

    /**
     * janar153 @ 29.12.2013
     *
     * @return
     */
    public List<RemovedProductLines> getRemovedProductLines() {
        return removedProductLines;
    }

    /**
     * JG Dec 14
     *
     * @return
     */
    public List<DrawerOpenedLines> getDrawerOpenedLines() {
        return drawerOpenedLines;
    }

    /**
     *
     * @return
     */
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
                        return l.getTaxNet(); // JG June 2014
                    default:
                        return null;
                }
            }
        };
    }

    /**
     *
     * @return
     */
    public AbstractTableModel getPaymentsModel() {
        return new AbstractTableModel() {
            @Override
            public String getColumnName(int column) {
                return AppLocal.getIntString(PAYMENTHEADERS[column]);
            }

            @Override
            public int getRowCount() {
                return paymentLines.size();
            }

            @Override
            public int getColumnCount() {
                return PAYMENTHEADERS.length;
            }

            @Override
            public Object getValueAt(int row, int column) {
                PaymentsListLine l = paymentLines.get(row);
                switch (column) {
                    case 0:
                        return l.getType();
                    case 1:
                        return l.getValue();
                    case 2:
                        return l.getNumberOfEntries();
                    default:
                        return null;
                }
            }
        };
    }

    // End of Inner Classes (Moved to Domain)
}
