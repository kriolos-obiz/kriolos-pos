/*
 * Copyright (C) 2026 KriolOS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.openbravo.pos.sales;

import com.openbravo.basic.BasicException;

/**
 * Domain service port interface for remote kitchen/bar order queue operations.
 *
 * <p>Decouples kitchen order displays and remote order routing from legacy
 * {@code DataLogicOrders} and {@code DataLogicSystem}.</p>
 *
 * @author KriolOS
 */
public interface RemoteOrderService {

    /**
     * Enqueues a new item order line for remote kitchen or bar displays.
     *
     * @param order Encapsulated remote order details
     * @throws BasicException if database insertion fails
     */
    void addOrder(RemoteOrder order) throws BasicException;

    /**
     * Updates an existing order line in the kitchen display queue.
     *
     * @param order Encapsulated remote order details
     * @throws BasicException if database update fails
     */
    void updateOrder(RemoteOrder order) throws BasicException;

    /**
     * Removes an order and its associated queue lines by order identifier.
     *
     * @param orderId Order identifier to purge
     * @throws BasicException if database deletion fails
     */
    void deleteOrder(String orderId) throws BasicException;

    /**
     * @deprecated Use {@link #addOrder(RemoteOrder)} instead.
     */
    @Deprecated
    default void addOrder(String orderId, Double qty,
            String details, String attributes, String notes, String ticketId,
            String ordertime, String displayId, String auxiliary, String completetime
    ) throws BasicException {
        addOrder(new RemoteOrder(orderId, qty, details, attributes, notes, ticketId,
                ordertime, displayId, auxiliary, completetime));
    }

    /**
     * @deprecated Use {@link #addOrder(RemoteOrder)} instead.
     */
    @Deprecated
    default void addOrder(String orderId, Double qty,
            String details, String attributes, String notes, String ticketId,
            String ordertime, Integer displayId, String auxiliary, String completetime
    ) throws BasicException {
        addOrder(orderId, qty, details, attributes, notes, ticketId,
                ordertime, displayId != null ? displayId.toString() : null, auxiliary, completetime);
    }

    /**
     * @deprecated Use {@link #addOrder(RemoteOrder)} instead.
     */
    @Deprecated
    default void addOrder(String orderId, Integer qty,
            String details, String attributes, String notes, String ticketId,
            String ordertime, Integer displayId, String auxiliary, String completetime
    ) throws BasicException {
        addOrder(orderId, qty != null ? qty.doubleValue() : null, details, attributes, notes, ticketId,
                ordertime, displayId != null ? displayId.toString() : null, auxiliary, completetime);
    }

    /**
     * @deprecated Use {@link #updateOrder(RemoteOrder)} instead.
     */
    @Deprecated
    default void updateOrder(String orderId, Double qty,
            String details, String attributes, String notes, String ticketId,
            String ordertime, String displayId, String auxiliary, String completetime
    ) throws BasicException {
        updateOrder(new RemoteOrder(orderId, qty, details, attributes, notes, ticketId,
                ordertime, displayId, auxiliary, completetime));
    }

    /**
     * @deprecated Use {@link #updateOrder(RemoteOrder)} instead.
     */
    @Deprecated
    default void updateOrder(String orderId, Double qty,
            String details, String attributes, String notes, String ticketId,
            String ordertime, Integer displayId, String auxiliary, String completetime
    ) throws BasicException {
        updateOrder(orderId, qty, details, attributes, notes, ticketId,
                ordertime, displayId != null ? displayId.toString() : null, auxiliary, completetime);
    }

    /**
     * @deprecated Use {@link #updateOrder(RemoteOrder)} instead.
     */
    @Deprecated
    default void updateOrder(String orderId, Integer qty,
            String details, String attributes, String notes, String ticketId,
            String ordertime, Integer displayId, String auxiliary, String completetime
    ) throws BasicException {
        updateOrder(orderId, qty != null ? qty.doubleValue() : null, details, attributes, notes, ticketId,
                ordertime, displayId != null ? displayId.toString() : null, auxiliary, completetime);
    }
}
