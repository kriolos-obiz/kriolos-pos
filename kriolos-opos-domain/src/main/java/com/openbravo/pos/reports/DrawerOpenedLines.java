package com.openbravo.pos.reports;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.DataRead;
import com.openbravo.data.loader.SerializableRead;
import com.openbravo.pos.util.StringUtils;
import java.io.Serializable;

public class DrawerOpenedLines implements SerializableRead, Serializable {

    private String m_DrawerOpened;
    private String m_Name;
    private String m_TicketId;

    @Override
    public void readValues(DataRead dr) throws BasicException {
        m_DrawerOpened = dr.getString(1);
        m_Name = dr.getString(2);
        m_TicketId = dr.getString(3);
    }

    public String printDrawerOpened() {
        return StringUtils.encodeXML(m_DrawerOpened);
    }

    public String printUserName() {
        return StringUtils.encodeXML(m_Name);
    }

    public String printTicketId() {
        return StringUtils.encodeXML(m_TicketId);
    }
}
