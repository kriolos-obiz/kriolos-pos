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
import com.openbravo.pos.ticket.TicketInfo;
import java.util.List;

/**
 * Domain service port interface for shared / parked tickets and table receipts.
 */
public interface SharedTicketService {

    /**
     * Retrieves the serialized ticket associated with the shared ticket identifier.
     *
     * @param id Shared ticket identifier
     * @return TicketInfo or null if not found
     * @throws BasicException on persistence error
     */
    TicketInfo getSharedTicket(String id) throws BasicException;

    /**
     * Lists all active shared tickets.
     *
     * @return List of shared ticket descriptors
     * @throws BasicException on persistence error
     */
    List<SharedTicketInfo> getSharedTicketList() throws BasicException;

    /**
     * Lists shared tickets owned by a specific POS application user.
     *
     * @param appuser Application user identifier
     * @return List of shared ticket descriptors
     * @throws BasicException on persistence error
     */
    List<SharedTicketInfo> getUserSharedTicketList(String appuser) throws BasicException;

    /**
     * Retrieves shared ticket metadata and content for a given shared ID.
     *
     * @param sharedId Shared ticket identifier
     * @return SharedTicketInfo descriptor
     * @throws BasicException on persistence error
     */
    SharedTicketInfo getSharedTicketInfo(String sharedId) throws BasicException;

    /**
     * Inserts a new shared ticket.
     *
     * @param id Shared ticket identifier
     * @param ticket Ticket information
     * @param pickupid Pickup order sequence ID
     * @throws BasicException on persistence error
     */
    void insertSharedTicket(String id, TicketInfo ticket, int pickupid) throws BasicException;

    /**
     * Updates an existing shared ticket.
     *
     * @param id Shared ticket identifier
     * @param ticket Ticket information
     * @param pickupid Pickup order sequence ID
     * @throws BasicException on persistence error
     */
    void updateSharedTicket(String id, TicketInfo ticket, int pickupid) throws BasicException;

    /**
     * Updates an existing restaurant shared ticket.
     *
     * @param id Shared ticket identifier
     * @param ticket Ticket information
     * @param pickupid Pickup order sequence ID
     * @throws BasicException on persistence error
     */
    void updateRSharedTicket(String id, TicketInfo ticket, int pickupid) throws BasicException;

    /**
     * Locks a shared ticket to prevent concurrent modification across terminals.
     *
     * @param id Shared ticket identifier
     * @param locked Lock token / user
     * @throws BasicException on persistence error
     */
    void lockSharedTicket(String id, String locked) throws BasicException;

    /**
     * Unlocks a shared ticket.
     *
     * @param id Shared ticket identifier
     * @param unlocked Unlock state value
     * @throws BasicException on persistence error
     */
    void unlockSharedTicket(String id, String unlocked) throws BasicException;

    /**
     * Inserts a restaurant shared ticket.
     *
     * @param id Shared ticket identifier
     * @param ticket Ticket information
     * @param pickupid Pickup order sequence ID
     * @throws BasicException on persistence error
     */
    void insertRSharedTicket(String id, TicketInfo ticket, int pickupid) throws BasicException;

    /**
     * Deletes a shared ticket by ID.
     *
     * @param id Shared ticket identifier
     * @throws BasicException on persistence error
     */
    void deleteSharedTicket(String id) throws BasicException;

    /**
     * Retrieves pickup identifier for a shared ticket.
     *
     * @param sharedTicketId Shared ticket identifier
     * @return Pickup ID or 0 if not found
     * @throws BasicException on persistence error
     */
    Integer getPickupId(String sharedTicketId) throws BasicException;

    /**
     * Retrieves the owning user name of a shared ticket.
     *
     * @param sharedTicketId Shared ticket identifier
     * @return User name or null if not found
     * @throws BasicException on persistence error
     */
    String getUserId(String sharedTicketId) throws BasicException;

    /**
     * Retrieves the lock state of a shared ticket.
     *
     * @param sharedTicketId Shared ticket identifier
     * @param lockState Optional fallback / filter
     * @return Lock state string
     * @throws BasicException on persistence error
     */
    String getLockState(String sharedTicketId, String lockState) throws BasicException;
}
