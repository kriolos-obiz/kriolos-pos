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

import java.util.Date;

/**
 * Service port interface for security, void tracking, and ticket line removal audit logging.
 */
public interface AuditService {

    /**
     * Records an audit log entry for a removed ticket line.
     *
     * @param command The validated command containing line removal audit details
     */
    void recordLineRemoved(RecordAuditLineRemovedCommand command);

    /**
     * Records an audit log entry for a voided/deleted ticket.
     *
     * @param command The validated command containing ticket deletion audit details
     */
    void recordTicketDeleted(RecordAuditTicketDeletedCommand command);

    /**
     * @deprecated Use {@link #recordLineRemoved(RecordAuditLineRemovedCommand)} instead.
     */
    @Deprecated
    default void addTicketLineRemoved(String username, String ticketId, String productId, String productName, double quantity) {
        recordLineRemoved(RecordAuditLineRemovedCommand.of(username, ticketId, productId, productName, quantity));
    }

    /**
     * @deprecated Use {@link #recordLineRemoved(RecordAuditLineRemovedCommand)} instead.
     */
    @Deprecated
    default void addTicketLineRemoved(String username, String ticketId, String productId, String productName, double quantity, Date date) {
        recordLineRemoved(RecordAuditLineRemovedCommand.of(username, ticketId, productId, productName, quantity, date));
    }

    /**
     * @deprecated Use {@link #recordTicketDeleted(RecordAuditTicketDeletedCommand)} instead.
     */
    @Deprecated
    default void addTicketDeleted(String username) {
        recordTicketDeleted(RecordAuditTicketDeletedCommand.of(username));
    }

    /**
     * @deprecated Use {@link #recordTicketDeleted(RecordAuditTicketDeletedCommand)} instead.
     */
    @Deprecated
    default void addTicketDeleted(String username, Date date) {
        recordTicketDeleted(RecordAuditTicketDeletedCommand.of(username, date));
    }
}
