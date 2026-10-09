/*
 * Copyright (C) 2026 KriolOS POS
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
import com.openbravo.data.loader.SentenceList;
import com.openbravo.pos.ticket.FindTicketsInfo;
import com.openbravo.pos.ticket.TicketInfo;
import java.util.List;

/**
 * Domain port interface for Ticket persistence, reprint recovery, and sequence numbering.
 */
public interface TicketLifecycleService {

    /**
     * Loads an existing ticket by its type and numeric identifier.
     *
     * @param ticketType Ticket receipt type
     * @param ticketId   Numeric ticket sequence ID
     * @return Retrieved TicketInfo
     * @throws BasicException on persistence error
     */
    TicketInfo loadTicket(int ticketType, int ticketId) throws BasicException;

    /**
     * Loads the last processed ticket of a given type.
     *
     * @param ticketType Ticket receipt type
     * @return Last processed TicketInfo
     * @throws BasicException on persistence error
     */
    TicketInfo loadLastTicket(int ticketType) throws BasicException;

    /**
     * Saves and commits a finalized ticket into database records (receipts, tickets, lines, taxes, payments).
     *
     * @param ticket   Ticket to persist
     * @param location Inventory warehouse location
     * @throws BasicException on persistence error
     */
    void saveTicket(TicketInfo ticket, String location) throws BasicException;

    /**
     * Deletes a ticket and reverses stock/payment side-effects.
     *
     * @param ticket   Ticket to delete
     * @param location Inventory warehouse location
     * @throws BasicException on persistence error
     */
    void deleteTicket(TicketInfo ticket, String location) throws BasicException;

    /**
     * Returns sentence list for query-by-filter ticket search.
     *
     * @return SentenceList providing FindTicketsInfo records
     */
    SentenceList<FindTicketsInfo> getTicketsList();

    /**
     * Retrieves a past ticket by its ticket UUID for reprint purposes.
     *
     * @param id Ticket UUID
     * @return TicketInfo for reprint
     * @throws BasicException on persistence error
     */
    TicketInfo getReprintTicket(String id) throws BasicException;

    /**
     * Lists active tickets available for reprinting.
     *
     * @return List of ReprintTicketInfo descriptors
     * @throws BasicException on persistence error
     */
    List<ReprintTicketInfo> getReprintTicketList() throws BasicException;

    /**
     * Retrieves the next sequential receipt number.
     *
     * @return Next ticket sequence index
     * @throws BasicException on persistence error
     */
    Integer getNextTicketIndex() throws BasicException;

    /**
     * Retrieves the next sequential refund receipt number.
     *
     * @return Next refund sequence index
     * @throws BasicException on persistence error
     */
    Integer getNextTicketRefundIndex() throws BasicException;

    /**
     * Retrieves the next sequential pickup sequence index.
     *
     * @return Next pickup sequence index
     * @throws BasicException on persistence error
     */
    Integer getNextPickupIndex() throws BasicException;

    /**
     * Resets the daily/shift pickup sequence counter.
     *
     * @throws BasicException on persistence error
     */
    void resetPickup() throws BasicException;
}
