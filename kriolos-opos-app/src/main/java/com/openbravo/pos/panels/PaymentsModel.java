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
public class PaymentsModel {

    private String m_sHost;
    private String m_sUser;
    private int m_iSeq;
    private Date m_dDateStart;
    private Date m_dDateEnd;
    private Date rDate;
    private Date m_dPrintDate;

    private Integer m_iPayments;
    private Double m_dPaymentsTotal;
    private java.util.List<PaymentsListLine> m_lpayments;

    // JG 9 Nov 12
    private Integer m_iCategorySalesRows;
    private Double m_dCategorySalesTotalUnits;
    private Double m_dCategorySalesTotal;
    private java.util.List<CategorySalesLine> m_lcategorysales;
    // end

    // by janar153 @ 01.12.2013
    private Integer m_iProductSalesRows;
    private Double m_dProductSalesTotalUnits;
    private Double m_dProductSalesTotal;
    private java.util.List<ProductSalesLine> m_lproductsales;
    // end

    // added by janar153 @ 29.12.2013
    private java.util.List<RemovedProductLines> m_lremovedlines;

    private java.util.List<DrawerOpenedLines> m_ldraweropenedlines;

    private final static String[] PAYMENTHEADERS = { "label.Payment", "label.paymenttotal", "label.qty" };

    private Integer m_iSales;
    private Double m_dSalesBase;
    private Double m_dSalesTaxes;
    private Double m_dSalesTaxNet;
    private java.util.List<SalesLine> m_lsales;

    private final static String[] SALEHEADERS = { "label.taxcategory", "label.totaltax", "label.totalnet" };

    private PaymentsModel() {
    }

    /**
     *
     * @return
     */
    public static PaymentsModel emptyInstance() {

        PaymentsModel p = new PaymentsModel();

        p.m_iPayments = 0;
        p.m_dPaymentsTotal = 0.0;
        // JG 16 May 2013 use diamond inference
        p.m_lpayments = new ArrayList<>();

        // JG 9 Nov 12
        p.m_iCategorySalesRows = 0;
        p.m_dCategorySalesTotalUnits = 0.0;
        p.m_dCategorySalesTotal = 0.0;
        p.m_lcategorysales = new ArrayList<>();
        // end
        p.m_iSales = null;
        p.m_dSalesBase = null;
        p.m_dSalesTaxes = null;
        p.m_dSalesTaxNet = null;

        // JG 16 May 2013 use diamond inference
        // by janar153 @ 01.12.2013
        p.m_iProductSalesRows = 0;
        p.m_dProductSalesTotalUnits = 0.0;
        p.m_dProductSalesTotal = 0.0;
        p.m_lproductsales = new ArrayList<>();
        p.m_lremovedlines = new ArrayList<>();

        p.m_lsales = new ArrayList<>();

        return p;
    }

    /**
     *
     * @param app
     * @return
     * @throws BasicException
     */
    public static PaymentsModel loadInstance(AppView app) throws BasicException {

        PaymentsModel p = new PaymentsModel();

        // Global Properties
        p.m_sHost = app.getProperties().getHost();
        p.m_sUser = app.getAppUserView().getUser().getName();
        p.m_iSeq = app.getActiveCashSequence();
        p.m_dDateStart = app.getActiveCashDateStart();
        p.m_dDateEnd = null;

        FinancialReportService reportService = new FinancialReportServiceImpl(app.getSession());
        FinancialReport report = reportService.getFinancialReport(app.getActiveCashIndex(), p.m_dDateStart,
                p.m_dDateEnd);

        // Map FinancialReport to PaymentsModel
        p.m_iPayments = report.getPaymentCount();
        p.m_dPaymentsTotal = report.getPaymentTotal();
        p.m_lpayments = report.getPaymentLines();

        p.m_iCategorySalesRows = report.getCategorySalesRows();
        p.m_dCategorySalesTotalUnits = report.getCategorySalesTotalUnits();
        p.m_dCategorySalesTotal = report.getCategorySalesTotal();
        p.m_lcategorysales = report.getCategorySalesLines();

        p.m_iSales = report.getSalesCount();
        p.m_dSalesBase = report.getSalesBase();

        p.m_dSalesTaxes = report.getSalesTaxes();
        // Recalculate TaxNet if likely not in report or add to report.
        // For now we trust the service/report to provide what's needed or calculate it.
        // Original logic had it from a query.
        // Let's assume for now we use the report data.

        p.m_lsales = report.getSalesLines();
        p.m_lremovedlines = report.getRemovedProductLines();
        p.m_ldraweropenedlines = report.getDrawerOpenedLines();

        p.m_iProductSalesRows = report.getProductSalesRows();
        p.m_dProductSalesTotalUnits = report.getProductSalesTotalUnits();
        p.m_dProductSalesTotal = report.getProductSalesTotal();
        p.m_lproductsales = report.getProductSalesLines();

        return p;
    }

    /**
     *
     * @return
     */
    public int getPayments() {
        return m_iPayments;
    }

    /**
     *
     * @return
     */
    public double getTotal() {
        return m_dPaymentsTotal;
    }

    /**
     *
     * @return
     */
    public String getHost() {
        return m_sHost;
    }

    /**
     *
     * @return
     */
    public String getUser() {
        return m_sUser;
    }

    /**
     *
     * @return
     */
    public int getSequence() {
        return m_iSeq;
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
        return m_dDateStart;
    }

    /**
     *
     * @param dValue
     */
    public void setDateEnd(Date dValue) {
        m_dDateEnd = dValue;
    }

    /**
     *
     * @return
     */
    public Date getDateEnd() {
        return m_dDateEnd;
    }

    /**
     *
     * @return
     */
    public String getDateStartDerby() {
        return Formats.DATETIME.formatValue(m_dDateStart);
    }

    /**
     *
     * @return
     */
    public String printHost() {
        return StringUtils.encodeXML(m_sHost);
    }

    /**
     *
     * @return
     */
    public String printUser() {
        return StringUtils.encodeXML(m_sUser);
    }

    /**
     *
     * @return
     */
    public String printSequence() {
        return Formats.INT.formatValue(m_iSeq);
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
        return Formats.TIMESTAMP.formatValue(m_dDateStart);
    }

    /**
     *
     * @return
     */
    public String printDateEnd() {
        return Formats.TIMESTAMP.formatValue(m_dDateEnd);
    }

    /**
     *
     * @return
     */
    public String printPayments() {
        return Formats.INT.formatValue(m_iPayments);
    }

    /**
     *
     * @return
     */
    public String printPaymentsTotal() {
        return Formats.CURRENCY.formatValue(m_dPaymentsTotal);
    }

    /**
     *
     * @return
     */
    public List<PaymentsListLine> getPaymentLines() {
        return m_lpayments;
    }

    /**
     *
     * @return
     */
    public int getSales() {
        return m_iSales == null ? 0 : m_iSales;
    }

    /**
     *
     * @return
     */
    public String printSales() {
        return Formats.INT.formatValue(m_iSales);
    }

    /**
     *
     * @return
     */
    public String printSalesBase() {
        return Formats.CURRENCY.formatValue(m_dSalesBase);
    }

    /**
     *
     * @return
     */
    public String printSalesTaxes() {
        return Formats.CURRENCY.formatValue(m_dSalesTaxes);
    }

    /**
     *
     * @return
     */
    public String printSalesTotal() {
        return Formats.CURRENCY.formatValue((m_dSalesBase == null || m_dSalesTaxes == null)
                ? null
                : m_dSalesBase + m_dSalesTaxes);
    }

    /**
     *
     * @return
     */
    public List<SalesLine> getSaleLines() {
        return m_lsales;
    }

    // JG 9 Nov 12
    /**
     *
     * @return
     */
    public double getCategorySalesRows() {
        return m_iCategorySalesRows;
    }

    /**
     *
     * @return
     */
    public String printCategorySalesRows() {
        return Formats.INT.formatValue(m_iCategorySalesRows);
    }

    /**
     *
     * @return
     */
    public double getCategorySalesTotalUnits() {
        return m_dCategorySalesTotalUnits;
    }

    /**
     *
     * @return
     */
    public String printCategorySalesTotalUnits() {
        return Formats.DOUBLE.formatValue(m_dCategorySalesTotalUnits);
    }

    /**
     *
     * @return
     */
    public double getCategorySalesTotal() {
        return m_dCategorySalesTotal;
    }

    /**
     *
     * @return
     */
    public String printCategorySalesTotal() {
        return Formats.CURRENCY.formatValue(m_dCategorySalesTotal);
    }

    /**
     *
     * @return
     */
    public List<CategorySalesLine> getCategorySalesLines() {
        return m_lcategorysales;
    }
    // end

    // by janar153 @ 01.12.2013
    /**
     *
     * @return
     */
    public double getProductSalesRows() {
        return m_iProductSalesRows;
    }

    /**
     *
     * @return
     */
    public String printProductSalesRows() {
        return Formats.INT.formatValue(m_iProductSalesRows);
    }

    /**
     *
     * @return
     */
    public double getProductSalesTotalUnits() {
        return m_dProductSalesTotalUnits;
    }

    /**
     *
     * @return
     */
    public String printProductSalesTotalUnits() {
        return Formats.DOUBLE.formatValue(m_dProductSalesTotalUnits);
    }

    /**
     *
     * @return
     */
    public double getProductSalesTotal() {
        return m_dProductSalesTotal;
    }

    /**
     *
     * @return
     */
    public String printProductSalesTotal() {
        return Formats.CURRENCY.formatValue(m_dProductSalesTotal);
    }

    /**
     *
     * @return
     */
    public List<ProductSalesLine> getProductSalesLines() {
        return m_lproductsales;
    }
    // end

    /**
     * janar153 @ 29.12.2013
     *
     * @return
     */
    public List<RemovedProductLines> getRemovedProductLines() {
        return m_lremovedlines;
    }

    /**
     * JG Dec 14
     *
     * @return
     */
    public List<DrawerOpenedLines> getDrawerOpenedLines() {
        return m_ldraweropenedlines;
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
                return m_lsales.size();
            }

            @Override
            public int getColumnCount() {
                return SALEHEADERS.length;
            }

            @Override
            public Object getValueAt(int row, int column) {
                SalesLine l = m_lsales.get(row);
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
                return m_lpayments.size();
            }

            @Override
            public int getColumnCount() {
                return PAYMENTHEADERS.length;
            }

            @Override
            public Object getValueAt(int row, int column) {
                PaymentsListLine l = m_lpayments.get(row);
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
