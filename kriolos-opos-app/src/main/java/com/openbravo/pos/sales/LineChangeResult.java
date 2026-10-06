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

import com.openbravo.pos.ticket.TicketLineInfo;

/**
 * Result of a ticket line quantity modification or removal operation.
 */
public record LineChangeResult(Status status, TicketLineInfo updatedLine) {

    public enum Status {
        UPDATED,
        REMOVED,
        DENIED,
        NOOP
    }

    public static LineChangeResult updated(TicketLineInfo line) {
        return new LineChangeResult(Status.UPDATED, line);
    }

    public static LineChangeResult removed() {
        return new LineChangeResult(Status.REMOVED, null);
    }

    public static LineChangeResult denied() {
        return new LineChangeResult(Status.DENIED, null);
    }

    public static LineChangeResult noop() {
        return new LineChangeResult(Status.NOOP, null);
    }
}
