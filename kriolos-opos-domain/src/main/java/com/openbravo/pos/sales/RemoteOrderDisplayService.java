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
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.ticket.TicketInfo;

/**
 * Remote Orders Display coordinator.
 *
 * <p>Handles dispatching and queue management for remote kitchen and bar order displays.</p>
 */
public class RemoteOrderDisplayService {

    protected final static System.Logger LOGGER = System.getLogger(RemoteOrderDisplayService.class.getName());

    private final RemoteOrderService remoteOrderService;
    private final TicketLifecycleService ticketLifecycleService;
    private final TicketInfo ticketInfo;
    private final String ticketExternalId;

    public RemoteOrderDisplayService(RemoteOrderService remoteOrderService, TicketLifecycleService ticketLifecycleService,
                              TicketInfo ticketInfo, String ticketExternalId) {
        this.remoteOrderService = remoteOrderService;
        this.ticketLifecycleService = ticketLifecycleService;
        this.ticketInfo = ticketInfo;
        this.ticketExternalId = ticketExternalId;
    }

    public RemoteOrderDisplayService(AppView appView, TicketInfo ticketInfo, String ticketExternalId) {
        this(appView.getBean(RemoteOrderService.class), appView.getBean(TicketLifecycleService.class), ticketInfo, ticketExternalId);
    }

    public void remoteOrderDisplay() {
        remoteOrderDisplay(null);
    }

    private String remoteOrderId() {

        if ((ticketInfo.getCustomer() != null && ticketInfo.getCustomer().getName() != null)) {
            return ticketInfo.getCustomer().getName();
        } else if (ticketExternalId != null) {
            return ticketExternalId;
        } else {
            if (ticketInfo.getPickupId() == 0 && ticketLifecycleService != null) {
                try {
                    ticketInfo.setPickupId(ticketLifecycleService.getNextPickupIndex());
                } catch (BasicException ex) {
                    LOGGER.log(System.Logger.Level.WARNING, "Exception on generate next pickup id: ", ex);
                    ticketInfo.setPickupId(0);
                }
            }
            return "" + ticketInfo.getPickupId();
        }
    }

    public void remoteOrderDisplay(String display) {
        if (remoteOrderService == null) {
            return;
        }

        String orderId = remoteOrderId();
        try {
            remoteOrderService.deleteOrder(orderId);
        } catch (BasicException ex) {
            LOGGER.log(System.Logger.Level.WARNING, "Exception on: ", ex);
        }

        for (int i = 0; i < ticketInfo.getLinesCount(); i++) {
            try {
                String lineDisplay = display;
                if (lineDisplay == null || lineDisplay.isBlank()) {
                    lineDisplay = ticketInfo.getLine(i).getProperty("display");
                    if (lineDisplay == null || lineDisplay.isBlank()) {
                        lineDisplay = "1";
                    }
                }

                RemoteOrder order = RemoteOrder.of(
                        orderId,
                        ticketInfo.getLine(i).getMultiply(),
                        ticketInfo.getLine(i).getProductName(),
                        ticketInfo.getLine(i).getProductAttSetInstDesc(),
                        ticketInfo.getLine(i).getProperty("notes"),
                        ticketInfo.getId(),
                        lineDisplay);
                remoteOrderService.addOrder(order);
            } catch (BasicException ex) {
                LOGGER.log(System.Logger.Level.WARNING, "Exception on: ", ex);
            }
        }
    }
}
