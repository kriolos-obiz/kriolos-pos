package com.openbravo.pos.reports;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import com.openbravo.pos.payment.PaymentInfo;

/**
 * Domain object representing a financial report (e.g. for closing cash).
 */
public class FinancialReport implements Serializable {

    private String host;
    private String user;
    private int sequence;
    private Date dateStart;
    private Date dateEnd;

    private int paymentCount;
    private double paymentTotal;
    private List<PaymentsListLine> paymentLines;

    private int categorySalesRows;
    private double categorySalesTotalUnits;
    private double categorySalesTotal;
    private List<CategorySalesLine> categorySalesLines;

    private int productSalesRows;
    private double productSalesTotalUnits;
    private double productSalesTotal;
    private List<ProductSalesLine> productSalesLines;

    private List<RemovedProductLines> removedProductLines;
    private List<DrawerOpenedLines> drawerOpenedLines;

    private int salesCount;
    private double salesBase;
    private double salesTaxes;
    private List<SalesLine> salesLines;

    // We will likely need to move the inner classes from PaymentsModel to here or
    // separate files.
    // For now, let's start with the basics.

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public Date getDateStart() {
        return dateStart;
    }

    public void setDateStart(Date dateStart) {
        this.dateStart = dateStart;
    }

    public Date getDateEnd() {
        return dateEnd;
    }

    public void setDateEnd(Date dateEnd) {
        this.dateEnd = dateEnd;
    }

    public int getPaymentCount() {
        return paymentCount;
    }

    public void setPaymentCount(int paymentCount) {
        this.paymentCount = paymentCount;
    }

    public double getPaymentTotal() {
        return paymentTotal;
    }

    public void setPaymentTotal(double paymentTotal) {
        this.paymentTotal = paymentTotal;
    }

    // Getters and Setters
    public List<PaymentsListLine> getPaymentLines() {
        return paymentLines;
    }

    public void setPaymentLines(List<PaymentsListLine> paymentLines) {
        this.paymentLines = paymentLines;
    }

    public int getCategorySalesRows() {
        return categorySalesRows;
    }

    public void setCategorySalesRows(int categorySalesRows) {
        this.categorySalesRows = categorySalesRows;
    }

    public double getCategorySalesTotalUnits() {
        return categorySalesTotalUnits;
    }

    public void setCategorySalesTotalUnits(double categorySalesTotalUnits) {
        this.categorySalesTotalUnits = categorySalesTotalUnits;
    }

    public double getCategorySalesTotal() {
        return categorySalesTotal;
    }

    public void setCategorySalesTotal(double categorySalesTotal) {
        this.categorySalesTotal = categorySalesTotal;
    }

    public List<CategorySalesLine> getCategorySalesLines() {
        return categorySalesLines;
    }

    public void setCategorySalesLines(List<CategorySalesLine> categorySalesLines) {
        this.categorySalesLines = categorySalesLines;
    }

    public int getProductSalesRows() {
        return productSalesRows;
    }

    public void setProductSalesRows(int productSalesRows) {
        this.productSalesRows = productSalesRows;
    }

    public double getProductSalesTotalUnits() {
        return productSalesTotalUnits;
    }

    public void setProductSalesTotalUnits(double productSalesTotalUnits) {
        this.productSalesTotalUnits = productSalesTotalUnits;
    }

    public double getProductSalesTotal() {
        return productSalesTotal;
    }

    public void setProductSalesTotal(double productSalesTotal) {
        this.productSalesTotal = productSalesTotal;
    }

    public List<ProductSalesLine> getProductSalesLines() {
        return productSalesLines;
    }

    public void setProductSalesLines(List<ProductSalesLine> productSalesLines) {
        this.productSalesLines = productSalesLines;
    }

    public List<RemovedProductLines> getRemovedProductLines() {
        return removedProductLines;
    }

    public void setRemovedProductLines(List<RemovedProductLines> removedProductLines) {
        this.removedProductLines = removedProductLines;
    }

    public List<DrawerOpenedLines> getDrawerOpenedLines() {
        return drawerOpenedLines;
    }

    public void setDrawerOpenedLines(List<DrawerOpenedLines> drawerOpenedLines) {
        this.drawerOpenedLines = drawerOpenedLines;
    }

    public int getSalesCount() {
        return salesCount;
    }

    public void setSalesCount(int salesCount) {
        this.salesCount = salesCount;
    }

    public double getSalesBase() {
        return salesBase;
    }

    public void setSalesBase(double salesBase) {
        this.salesBase = salesBase;
    }

    public double getSalesTaxes() {
        return salesTaxes;
    }

    public void setSalesTaxes(double salesTaxes) {
        this.salesTaxes = salesTaxes;
    }

    public List<SalesLine> getSalesLines() {
        return salesLines;
    }

    public void setSalesLines(List<SalesLine> salesLines) {
        this.salesLines = salesLines;
    }
}
