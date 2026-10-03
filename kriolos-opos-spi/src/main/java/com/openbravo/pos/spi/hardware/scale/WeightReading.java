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
package com.openbravo.pos.spi.hardware.scale;

/**
 * Immutable value representation of a weight measurement acquired from a scale peripheral.
 *
 * @param weight     Numeric measured weight.
 * @param unit       Unit of measurement (e.g. "kg", "g", "lb").
 * @param isStable   Whether the scale indicates the measurement is stable.
 * @param timestamp  System epoch millisecond timestamp when reading was captured.
 *
 * @author KriolOS Team
 * @since 1.0.0
 */
public record WeightReading(
        Double weight,
        String unit,
        boolean isStable,
        long timestamp
) {

    public WeightReading(Double weight) {
        this(weight, "kg", true, System.currentTimeMillis());
    }

    public WeightReading(Double weight, String unit) {
        this(weight, unit, true, System.currentTimeMillis());
    }
}
