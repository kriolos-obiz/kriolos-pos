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
package com.openbravo.pos.spi.hardware.printer;

import com.openbravo.pos.spi.hardware.DeviceConfig;

import java.util.Collections;
import java.util.Map;

/**
 * Immutable configuration descriptor for instantiating a customer visor display.
 *
 * @param protocol   Target display protocol.
 * @param param1     First parameter (port name, window bounds, or interface).
 * @param param2     Second parameter (baud rate, parity, or secondary display index).
 * @param properties Key-value properties map.
 *
 * @author KriolOS Team
 * @since 1.0.0
 */
public record DisplayConfig(
        DisplayProtocol protocol,
        String param1,
        String param2,
        Map<String, String> properties
) implements DeviceConfig {

    public DisplayConfig(DisplayProtocol protocol, String param1, String param2) {
        this(protocol, param1, param2, Collections.emptyMap());
    }

    @Override
    public String getPortOrTarget() {
        return param1;
    }

    @Override
    public Map<String, String> getProperties() {
        return properties != null ? properties : Collections.emptyMap();
    }

    /**
     * Parses a display property line.
     * Examples:
     * - "screen"
     * - "window"
     * - "dual"
     * - "epson:COM2,9600"
     * - "led8:/dev/ttyUSB0,2400"
     *
     * @param raw Raw display configuration string.
     * @return Fully parsed {@link DisplayConfig}.
     */
    public static DisplayConfig parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return new DisplayConfig(DisplayProtocol.NONE, "", "");
        }

        String trimmed = raw.trim();
        int colonIdx = trimmed.indexOf(':');

        if (colonIdx < 0) {
            DisplayProtocol protocol = DisplayProtocol.fromToken(trimmed);
            return new DisplayConfig(protocol, "", "");
        }

        String typeToken = trimmed.substring(0, colonIdx).trim();
        String params = trimmed.substring(colonIdx + 1).trim();

        String p1 = "";
        String p2 = "";

        int commaIdx = params.indexOf(',');
        if (commaIdx >= 0) {
            p1 = params.substring(0, commaIdx).trim();
            p2 = params.substring(commaIdx + 1).trim();
        } else {
            p1 = params;
        }

        String cleanType = typeToken.toLowerCase();
        if ("serial".equals(cleanType) || "rxtx".equals(cleanType) || "file".equals(cleanType)) {
            p2 = p1;
            p1 = typeToken;
            typeToken = "epson";
        }

        DisplayProtocol protocol = DisplayProtocol.fromToken(typeToken);
        return new DisplayConfig(protocol, p1, p2);
    }
}
