//    KriolOS POS
//    Copyright (c) 2019-2026 KriolOS
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

package com.openbravo.pos.sales;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.Datas;
import com.openbravo.data.loader.SentenceExec;
import com.openbravo.data.loader.SerializerWriteBasic;
import com.openbravo.data.loader.Session;
import com.openbravo.data.loader.StaticSentence;
import com.openbravo.pos.forms.BeanFactoryDataSingle;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data access logic for security, void tracking, and ticket line removal audit logging.
 */
public class DataLogicAudit extends BeanFactoryDataSingle implements AuditService {

    private static final Logger LOGGER = Logger.getLogger(DataLogicAudit.class.getName());

    private Session sessionDB;

    @Override
    public void init(Session sessionDB) {
        this.sessionDB = sessionDB;
    }

    @Override
    public final void addTicketLineRemoved(String username, String ticketId, String productId, String productName, double quantity) {
        addTicketLineRemoved(username, ticketId, productId, productName, quantity, new Date());
    }

    @Override

    public final void addTicketLineRemoved(String username, String ticketId, String productId, String productName, double quantity, Date date) {
        final SentenceExec m_lineremoved = new StaticSentence(this.sessionDB,
                """
                INSERT INTO lineremoved (NAME, TICKETID, PRODUCTID, PRODUCTNAME, UNITS, REMOVEDDATE)
                    VALUES (?, ?, ?, ?, ?, ?)
                """,
                new SerializerWriteBasic(new Datas[]{
                    Datas.STRING, Datas.STRING,
                    Datas.STRING, Datas.STRING,
                    Datas.DOUBLE, Datas.TIMESTAMP
                }));

        try {
            Object[] line = new Object[]{username, ticketId, productId, productName, quantity, date};
            m_lineremoved.exec(line);
        } catch (BasicException e) {
            LOGGER.log(Level.SEVERE, "Exception on execute line removed: ", e);
        }
    }

    @Override
    public final void addTicketDeleted(String username) {
        addTicketDeleted(username, new Date());
    }

    @Override
    public final void addTicketDeleted(String username, Date date) {
        final SentenceExec m_ticketremoved = new StaticSentence(this.sessionDB,
                """
                INSERT INTO lineremoved (NAME, TICKETID, PRODUCTNAME, UNITS, REMOVEDDATE)
                    VALUES (?, ?, ?, ?, ?)
                """,
                new SerializerWriteBasic(new Datas[]{
                    Datas.STRING, Datas.STRING,
                    Datas.STRING, Datas.DOUBLE, Datas.TIMESTAMP
                }));
        try {
            Object[] ticketDeleted = new Object[]{username, "Void", "Ticket Deleted", 0.0, date};
            m_ticketremoved.exec(ticketDeleted);
        } catch (BasicException e) {
            LOGGER.log(Level.SEVERE, "Exception on execute ticket removed: ", e);
        }
    }
}
