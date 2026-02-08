package com.openbravo.pos.reports;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.DataRead;
import com.openbravo.data.loader.SerializableRead;
import com.openbravo.format.Formats;
import java.io.Serializable;

public class ProductSalesLine implements SerializableRead, Serializable {

    private String m_ProductName;
    private Double m_ProductUnits;
    private Double m_ProductPrice;
    private Double m_TaxRate;
    private Double m_ProductPriceTax;
    private Double m_ProductPriceNet;

    @Override
    public void readValues(DataRead dr) throws BasicException {
        m_ProductName = dr.getString(1);
        m_ProductUnits = dr.getDouble(2);
        m_ProductPrice = dr.getDouble(3);
        m_TaxRate = dr.getDouble(4);

        m_ProductPriceTax = m_ProductPrice + m_ProductPrice * m_TaxRate;
        m_ProductPriceNet = m_ProductPrice * m_TaxRate;
    }

    public String printProductName() {
        return m_ProductName;
    }

    public String printProductUnits() {
        return Formats.DOUBLE.formatValue(m_ProductUnits);
    }

    public Double getProductUnits() {
        return m_ProductUnits;
    }

    public String printProductPrice() {
        return Formats.CURRENCY.formatValue(m_ProductPrice);
    }

    public Double getProductPrice() {
        return m_ProductPrice;
    }

    public String printTaxRate() {
        return Formats.PERCENT.formatValue(m_TaxRate);
    }

    public Double getTaxRate() {
        return m_TaxRate;
    }

    public String printProductPriceTax() {
        return Formats.CURRENCY.formatValue(m_ProductPriceTax);
    }

    public String printProductSubValue() {
        return Formats.CURRENCY.formatValue(m_ProductPriceTax * m_ProductUnits);
    }

    public String printProductPriceNet() {
        return Formats.CURRENCY.formatValue(m_ProductPrice * m_ProductUnits);
    }
}
