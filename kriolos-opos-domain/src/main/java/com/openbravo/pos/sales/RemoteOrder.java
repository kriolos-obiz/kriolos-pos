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

import java.io.Serializable;

/**
 * Domain record encapsulating a remote kitchen/bar order line.
 *
 * @param orderId External or generated order identifier
 * @param qty Ordered item quantity as decimal/double (supporting fractional quantities)
 * @param details Product description / line title
 * @param attributes Product attribute instance description
 * @param notes Line notes / preparation instructions
 * @param ticketId Parent receipt ID
 * @param ordertime Order placement timestamp
 * @param displayId Target kitchen display station identifier
 * @param auxiliary Auxiliary bundle flag
 * @param completetime Timestamp when line preparation finished
 * @author KriolOS
 */
public record RemoteOrder(
        String orderId,
        Double qty,
        String details,
        String attributes,
        String notes,
        String ticketId,
        String ordertime,
        String displayId,
        String auxiliary,
        String completetime
) implements Serializable {

    /**
     * Convenience factory for creating a new pending order line with decimal quantity.
     *
     * @param orderId External or generated order identifier
     * @param qty Ordered item quantity (double)
     * @param details Product description / line title
     * @param attributes Product attribute instance description
     * @param notes Line notes / preparation instructions
     * @param ticketId Parent receipt ID
     * @param displayId Target kitchen display station identifier
     * @return New RemoteOrder instance with default null timestamps and auxiliary flag
     */
    public static RemoteOrder of(
            String orderId,
            double qty,
            String details,
            String attributes,
            String notes,
            String ticketId,
            String displayId
    ) {
        return new RemoteOrder(orderId, qty, details, attributes, notes, ticketId, null, displayId, null, null);
    }

    /**
     * Backward-compatible convenience factory accepting an integer displayId.
     *
     * @param orderId External or generated order identifier
     * @param qty Ordered item quantity (double)
     * @param details Product description / line title
     * @param attributes Product attribute instance description
     * @param notes Line notes / preparation instructions
     * @param ticketId Parent receipt ID
     * @param displayId Target kitchen display station index
     * @return New RemoteOrder instance with default null timestamps and auxiliary flag
     */
    public static RemoteOrder of(
            String orderId,
            double qty,
            String details,
            String attributes,
            String notes,
            String ticketId,
            Integer displayId
    ) {
        return of(orderId, qty, details, attributes, notes, ticketId, displayId != null ? displayId.toString() : null);
    }
}
