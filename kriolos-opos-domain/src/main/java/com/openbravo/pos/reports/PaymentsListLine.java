package com.openbravo.pos.reports;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.DataRead;
import com.openbravo.data.loader.SerializableRead;
import com.openbravo.format.Formats;
import com.openbravo.pos.forms.AppLocal;
import java.io.Serializable;

// Renamed from PaymentsLine to avoid confusion with PaymentInfo or similar
public class PaymentsListLine implements SerializableRead, Serializable {

    private String m_PaymentType;
    private Double m_PaymentValue;
    private String s_PaymentReason;
    private int numberOfEntries;

    @Override
    public void readValues(DataRead dr) throws BasicException {
        m_PaymentType = dr.getString(1);
        m_PaymentValue = dr.getDouble(2);
        s_PaymentReason = dr.getString(3) == null ? "" : dr.getString(3);
        numberOfEntries = dr.getInt(4) != null ? dr.getInt(4) : -1;
    }

    public String printType() {
        return AppLocal.getIntString("transpayment." + m_PaymentType);
    }

    public String getType() {
        return m_PaymentType;
    }

    public String printValue() {
        return Formats.CURRENCY.formatValue(m_PaymentValue);
    }

    public Double getValue() {
        return m_PaymentValue;
    }

    public String printReason() {
        return s_PaymentReason;
    }

    public String getReason() {
        return s_PaymentReason;
    }

    public int getNumberOfEntries() {
        return numberOfEntries;
    }

    public void setNumberOfEntries(int numberOfEntries) {
        this.numberOfEntries = numberOfEntries;
    }
}
