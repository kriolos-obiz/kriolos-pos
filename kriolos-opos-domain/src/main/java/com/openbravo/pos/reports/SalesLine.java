package com.openbravo.pos.reports;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.DataRead;
import com.openbravo.data.loader.SerializableRead;
import com.openbravo.format.Formats;
import java.io.Serializable;

public class SalesLine implements SerializableRead, Serializable {

    private String m_SalesTaxName;
    private Double m_SalesTaxes;
    private Double m_SalesTaxNet;
    private Double m_SalesTaxGross;

    @Override
    public void readValues(DataRead dr) throws BasicException {
        m_SalesTaxName = dr.getString(1);
        m_SalesTaxes = dr.getDouble(2);
        m_SalesTaxNet = dr.getDouble(3);
        m_SalesTaxGross = dr.getDouble(4);
    }

    public String printTaxName() {
        return m_SalesTaxName;
    }

    public String printTaxes() {
        return Formats.CURRENCY.formatValue(m_SalesTaxes);
    }

    public String printTaxNet() {
        return Formats.CURRENCY.formatValue(m_SalesTaxNet);
    }

    public String printTaxGross() {
        return Formats.CURRENCY.formatValue(m_SalesTaxes + m_SalesTaxNet);
    }

    public String getTaxName() {
        return m_SalesTaxName;
    }

    public Double getTaxes() {
        return m_SalesTaxes;
    }

    public Double getTaxNet() {
        return m_SalesTaxNet;
    }

    public Double getTaxGross() {
        return m_SalesTaxGross;
    }
}
