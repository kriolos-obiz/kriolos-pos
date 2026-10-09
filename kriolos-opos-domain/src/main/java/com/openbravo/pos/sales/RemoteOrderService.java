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
 * Domain service provider port interface for remote kitchen/bar order queue operations.
 *
 * <p>This service is responsible for persisting and synchronizing order lines sent to remote
 * display systems (kitchen video screens, bar displays, expeditor stations).</p>
 *
 * <ul>
 *   <li><b>Default implementation:</b> {@code DataLogicOrders} stores queue entries in the
 *       shared database table ({@code ORDERS}). External kitchen display applications query this
 *       table directly.</li>
 *   <li><b>Pluggable provider:</b> Can be implemented by an HTTP / REST API client to dispatch
 *       order lines to an external microservice or cloud kitchen display service.</li>
 * </ul>
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
    
}
