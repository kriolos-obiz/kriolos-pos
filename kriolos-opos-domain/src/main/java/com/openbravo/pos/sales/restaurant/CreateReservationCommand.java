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
package com.openbravo.pos.sales.restaurant;

import java.util.Date;
import java.util.Objects;

/**
 * Immutable command to create a dining table reservation.
 *
 * @param id              unique reservation identifier
 * @param createdDate     creation timestamp
 * @param reservationDate target date and time for the reservation
 * @param customerId      optional customer identifier
 * @param title           contact or group title for the reservation
 * @param chairs          number of seats requested (must be &ge; 1)
 * @param isDone          whether the reservation has been fulfilled
 * @param description     optional notes or special requests
 */
public record CreateReservationCommand(
        String id,
        Date createdDate,
        Date reservationDate,
        String customerId,
        String title,
        int chairs,
        boolean isDone,
        String description
) {

    /**
     * Compact constructor validating business invariants.
     */
    public CreateReservationCommand {
        Objects.requireNonNull(id, "Reservation ID must not be null");
        if (id.isBlank()) {
            throw new IllegalArgumentException("Reservation ID must not be blank");
        }
        Objects.requireNonNull(reservationDate, "Reservation date must not be null");
        if (createdDate == null) {
            createdDate = new Date();
        } else {
            createdDate = new Date(createdDate.getTime());
        }
        reservationDate = new Date(reservationDate.getTime());
        if (title == null) {
            title = "";
        }
        if (chairs < 1) {
            throw new IllegalArgumentException("Reservation chairs must be at least 1, got: " + chairs);
        }
    }

    @Override
    public Date createdDate() {
        return new Date(createdDate.getTime());
    }

    @Override
    public Date reservationDate() {
        return new Date(reservationDate.getTime());
    }

    /**
     * Factory method for creating a reservation with default created timestamp and initial status.
     *
     * @param id              unique identifier
     * @param reservationDate target reservation date and time
     * @param customerId      optional customer ID
     * @param title           group or contact title
     * @param chairs          number of chairs (&ge; 1)
     * @param description     optional notes
     * @return a new valid {@link CreateReservationCommand} instance
     */
    public static CreateReservationCommand of(
            String id,
            Date reservationDate,
            String customerId,
            String title,
            int chairs,
            String description
    ) {
        return new CreateReservationCommand(id, new Date(), reservationDate, customerId, title, chairs, false, description);
    }
}

