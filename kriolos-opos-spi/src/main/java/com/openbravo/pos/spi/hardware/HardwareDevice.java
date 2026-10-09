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
package com.openbravo.pos.spi.hardware;

import com.openbravo.pos.spi.provider.Provider;

/**
 * Base abstraction for all connected hardware devices and virtual peripherals.
 * Extends {@link Provider} to ensure clean resource teardown (closing serial ports, socket handles, etc.).
 *
 * @author KriolOS Team
 * @since 1.0.0
 */
public interface HardwareDevice extends Provider {

    /**
     * Unique identifier or logical name assigned to this peripheral device.
     *
     * @return device identifier string.
     */
    String getDeviceId();

    /**
     * The hardware category classification for this peripheral.
     *
     * @return {@link DeviceType} enum value.
     */
    DeviceType getDeviceType();

    /**
     * Checks if the physical or virtual peripheral is currently connected and operational.
     *
     * @return {@code true} if operational, {@code false} otherwise.
     */
    boolean isConnected();

    /**
     * Graceful teardown of device connection and underlying I/O resources.
     * Default implementation does nothing if no cleanup is necessary.
     */
    @Override
    default void close() {
        // Default no-op
    }
}
