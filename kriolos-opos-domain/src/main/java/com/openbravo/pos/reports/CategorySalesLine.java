package com.openbravo.pos.reports;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.DataRead;
import com.openbravo.data.loader.SerializableRead;
import com.openbravo.format.Formats;
import java.io.Serializable;

public class CategorySalesLine implements SerializableRead, Serializable {

    private String m_CategoryName;
    private Double m_CategoryUnits;
    private Double m_CategorySum;

    @Override
    public void readValues(DataRead dr) throws BasicException {
        m_CategoryName = dr.getString(1);
        m_CategoryUnits = dr.getDouble(2);
        m_CategorySum = dr.getDouble(3);
    }

    public String printCategoryName() {
        return m_CategoryName;
    }

    public String printCategoryUnits() {
        return Formats.DOUBLE.formatValue(m_CategoryUnits);
    }

    public Double getCategoryUnits() {
        return m_CategoryUnits;
    }

    public String printCategorySum() {
        return Formats.CURRENCY.formatValue(m_CategorySum);
    }

    public Double getCategorySum() {
        return m_CategorySum;
    }
}
