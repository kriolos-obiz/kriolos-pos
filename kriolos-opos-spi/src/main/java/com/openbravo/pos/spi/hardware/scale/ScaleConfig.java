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

import com.openbravo.pos.spi.hardware.DeviceConfig;

import java.util.Collections;
import java.util.Map;

/**
 * Immutable configuration descriptor for instantiating and connecting a scale device.
 *
 * @param protocol   Target scale protocol.
 * @param port       Serial COM port or target descriptor (e.g. "/dev/ttyUSB0", "COM1").
 * @param properties Key-value properties for baud rate, databits, etc.
 *
 * @author KriolOS Team
 * @since 1.0.0
 */
public record ScaleConfig(
        ScaleProtocol protocol,
        String port,
        Map<String, String> properties
) implements DeviceConfig {

    public ScaleConfig(ScaleProtocol protocol, String port) {
        this(protocol, port, Collections.emptyMap());
    }

    @Override
    public String getPortOrTarget() {
        return port;
    }

    @Override
    public Map<String, String> getProperties() {
        return properties != null ? properties : Collections.emptyMap();
    }

    /**
     * Parses legacy or modern scale configuration strings.
     * Examples:
     * - "caspdii:/dev/ttyS0"
     * - "acompc100:COM1,9600"
     * - "fake"
     * - "screen"
     *
     * @param raw Raw property value string.
     * @return Fully parsed {@link ScaleConfig}.
     */
    public static ScaleConfig parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return new ScaleConfig(ScaleProtocol.NONE, "");
        }

        String trimmed = raw.trim();
        int colonIdx = trimmed.indexOf(':');

        if (colonIdx < 0) {
            // No port specified (e.g. "fake", "screen")
            ScaleProtocol protocol = ScaleProtocol.fromToken(trimmed);
            return new ScaleConfig(protocol, "");
        }

        String typeToken = trimmed.substring(0, colonIdx).trim();
        String paramStr = trimmed.substring(colonIdx + 1).trim();

        ScaleProtocol protocol = ScaleProtocol.fromToken(typeToken);

        int commaIdx = paramStr.indexOf(',');
        String port;
        Map<String, String> props = Collections.emptyMap();

        if (commaIdx >= 0) {
            port = paramStr.substring(0, commaIdx).trim();
            String param2 = paramStr.substring(commaIdx + 1).trim();
            props = Map.of("param2", param2);
        } else {
            port = paramStr;
        }

        return new ScaleConfig(protocol, port, props);
    }
}
