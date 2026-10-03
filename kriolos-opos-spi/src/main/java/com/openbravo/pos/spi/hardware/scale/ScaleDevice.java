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

import com.openbravo.pos.spi.hardware.DeviceType;
import com.openbravo.pos.spi.hardware.HardwareDevice;

import java.util.Optional;

/**
 * Contract representing an operational weight scale peripheral.
 *
 * @author KriolOS Team
 * @since 1.0.0
 */
public interface ScaleDevice extends HardwareDevice {

    @Override
    default DeviceType getDeviceType() {
        return DeviceType.SCALE;
    }

    @Override
    default String getDeviceId() {
        return getDeviceType().getCode();
    }

    /**
     * Reads the current weight from the scale.
     *
     * @return Double weight value (in kilograms by default).
     * @throws ScaleException if reading fails, scale is not ready, or communication times out.
     */
    Double readWeight() throws ScaleException;

    /**
     * Reads comprehensive weight measurement including unit and stability flag.
     *
     * @return Optional containing the {@link WeightReading}, or empty if not supported.
     * @throws ScaleException if reading fails.
     */
    default Optional<WeightReading> readWeightReading() throws ScaleException {
        Double w = readWeight();
        return w != null ? Optional.of(new WeightReading(w)) : Optional.empty();
    }

    /**
     * Protocol implemented by this scale device.
     *
     * @return {@link ScaleProtocol} enum.
     */
    ScaleProtocol getProtocol();
}
