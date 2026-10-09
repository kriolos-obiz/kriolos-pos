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

import java.util.Map;

/**
 * Base configuration contract for hardware peripherals.
 *
 * @author KriolOS Team
 * @since 1.0.0
 */
public interface DeviceConfig {

    /**
     * Primary port, connection address, or device descriptor (e.g. "/dev/ttyUSB0", "COM1", "192.168.1.100").
     *
     * @return port or target identifier.
     */
    String getPortOrTarget();

    /**
     * Unmodifiable map of auxiliary operational parameters (baudrate, parity, timeout, etc.).
     *
     * @return properties map.
     */
    Map<String, String> getProperties();
}
