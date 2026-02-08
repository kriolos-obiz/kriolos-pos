package com.openbravo.pos.reports;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.DataRead;
import com.openbravo.data.loader.SerializableRead;
import com.openbravo.format.Formats;
import com.openbravo.pos.util.StringUtils;
import java.io.Serializable;

public class RemovedProductLines implements SerializableRead, Serializable {

    private String m_Name;
    private String m_TicketId;
    private String m_ProductName;
    private Double m_TotalUnits;

    @Override
    public void readValues(DataRead dr) throws BasicException {
        m_Name = dr.getString(1);
        m_TicketId = dr.getString(2);
        m_ProductName = dr.getString(3);
        m_TotalUnits = dr.getDouble(4);
    }

    public String printWorkerName() {
        return StringUtils.encodeXML(m_Name);
    }

    public String printTicketId() {
        return StringUtils.encodeXML(m_TicketId);
    }

    public String printProductName() {
        return StringUtils.encodeXML(m_ProductName);
    }

    public String printTotalUnits() {
        return Formats.DOUBLE.formatValue(m_TotalUnits);
    }
}
