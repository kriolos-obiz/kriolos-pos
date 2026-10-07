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

package com.openbravo.pos.forms;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.*;
import com.openbravo.pos.sales.RemoteOrder;
import com.openbravo.pos.sales.RemoteOrderService;

/**
 *
 * @author uniCenta
 */
public class DataLogicOrders extends BeanFactoryDataSingle implements RemoteOrderService {
    private SentenceExec m_addOrder;
    private SentenceExec m_updateOrder;
    private SentenceExec m_deleteOrder;

    public DataLogicOrders() {            
    }
    
    /**
     *
     * @param s
     */
    @Override
    public void init(Session s) {
        m_addOrder = new StaticSentence(s,
                "INSERT INTO orders (ORDERID, QTY, DETAILS, ATTRIBUTES, "
                + "NOTES, TICKETID, ORDERTIME, DISPLAYID, AUXILIARY, "
                + "COMPLETETIME) "
                + "VALUES (?, ?, ?, ?, ?, "
                + "?, ?, ?, ?, ? ) ",
                new SerializerWriteBasic(new Datas[]{
                    Datas.STRING,   // OrderId
                    Datas.DOUBLE,   // Qty
                    Datas.STRING,   // Details
                    Datas.STRING,   // Attributes
                    Datas.STRING,   // Notes
                    Datas.STRING,   // TicketId
                    Datas.TIMESTAMP,// OrderTime
                    Datas.STRING,   // DisplayId
                    Datas.INT,      // Auxiliary
                    Datas.TIMESTAMP // CompleteTime
                }));

        m_updateOrder = new StaticSentence(s,
                "UPDATE orders SET "
                + "ORDERID = ?, "
                + "QTY = ?, "
                + "DETAILS = ?, "
                + "ATTRIBUTES = ?, "
                + "NOTES = ?, "
                + "TICKETID = ?, "
                + "ORDERTIME = ?, "
                + "DISPLAYID = ?, "
                + "AUXILIARY = ?, "
                + "COMPLETETIME = ? "
                + "WHERE ORDERID = ? ",
                new SerializerWriteBasic(new Datas[]{
                    Datas.STRING, // OrderId
                    Datas.DOUBLE, // Qty
                    Datas.STRING, // Details
                    Datas.STRING, // Attributes
                    Datas.STRING, // Notes
                    Datas.STRING, // TicketId
                    Datas.STRING, // OrderTime
                    Datas.STRING, // DisplayId
                    Datas.INT,    // Auxiliary
                    Datas.STRING  // CompleteTime
                }));

        m_deleteOrder = new StaticSentence(s,
                "DELETE FROM orders WHERE ORDERID = ?",
                SerializerWriteString.INSTANCE);
    }

    @Override
    public final void addOrder(RemoteOrder order) throws BasicException {
        if (order == null) {
            return;
        }
        String ordertime = order.ordertime();
        if (ordertime == null) {
            ordertime = Long.toString(new java.util.Date().getTime());
        }
        m_addOrder.exec(new Object[]{
            order.orderId(),
            order.qty(),
            order.details(),
            order.attributes(),
            order.notes(),
            order.ticketId(),
            ordertime,
            order.displayId(),
            order.auxiliary(),
            order.completetime()
        });
    }

    @Override
    public final void updateOrder(RemoteOrder order) throws BasicException {
        if (order == null) {
            return;
        }
        m_updateOrder.exec(new Object[]{
            order.orderId(),
            order.qty(),
            order.details(),
            order.attributes(),
            order.notes(),
            order.ticketId(),
            order.ordertime(),
            order.displayId(),
            order.auxiliary(),
            order.completetime()
        });
    }

    /**
     * @deprecated Use {@link #addOrder(RemoteOrder)} instead.
     */
    @Override
    @Deprecated
    public final void addOrder(String orderId, Double qty, 
            String details, String attributes, String notes, String ticketId, 
            String ordertime, String displayId, String auxiliary, String completetime
    ) throws BasicException {
        addOrder(new RemoteOrder(orderId, qty, details, attributes, notes, ticketId,
                ordertime, displayId, auxiliary, completetime));
    }

    /**
     * @deprecated Use {@link #addOrder(RemoteOrder)} instead.
     */
    @Override
    @Deprecated
    public final void addOrder(String orderId, Integer qty, 
            String details, String attributes, String notes, String ticketId, 
            String ordertime, Integer displayId, String auxiliary, String completetime
    ) throws BasicException {
        addOrder(orderId, qty != null ? qty.doubleValue() : null, details, attributes, notes, ticketId,
                ordertime, displayId != null ? displayId.toString() : null, auxiliary, completetime);
    }

    /**
     * @deprecated Use {@link #updateOrder(RemoteOrder)} instead.
     */
    @Override
    @Deprecated
    public final void updateOrder(String orderId, Double qty, 
            String details, String attributes, String notes, String ticketId, 
            String ordertime, String displayId, String auxiliary, String completetime
    ) throws BasicException {
        updateOrder(new RemoteOrder(orderId, qty, details, attributes, notes, ticketId,
                ordertime, displayId, auxiliary, completetime));
    }

    /**
     * @deprecated Use {@link #updateOrder(RemoteOrder)} instead.
     */
    @Override
    @Deprecated
    public final void updateOrder(String orderId, Integer qty,
            String details, String attributes, String notes, String ticketId,
            String ordertime, Integer displayId, String auxiliary, String completetime
    ) throws BasicException {
        updateOrder(orderId, qty != null ? qty.doubleValue() : null, details, attributes, notes, ticketId,
                ordertime, displayId != null ? displayId.toString() : null, auxiliary, completetime);
    }

    @Override
    public void deleteOrder(String orderId) throws BasicException {
        m_deleteOrder.exec(orderId);
    }
}
