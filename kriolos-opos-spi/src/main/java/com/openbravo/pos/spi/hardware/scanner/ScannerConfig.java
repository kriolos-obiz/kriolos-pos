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
package com.openbravo.pos.spi.hardware.scanner;

import com.openbravo.pos.spi.hardware.DeviceConfig;

import java.util.Collections;
import java.util.Map;

/**
 * Immutable configuration descriptor for instantiating a barcode scanner.
 *
 * @param protocol   Target scanner protocol.
 * @param port       Serial COM port or target descriptor (e.g. "/dev/ttyS0", "COM3").
 * @param properties Key-value properties map.
 *
 * @author KriolOS Team
 * @since 1.0.0
 */
public record ScannerConfig(
        ScannerProtocol protocol,
        String port,
        Map<String, String> properties
) implements DeviceConfig {

    public ScannerConfig(ScannerProtocol protocol, String port) {
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
     * Parses a scanner property line.
     * Examples:
     * - "scanpal2:/dev/ttyS0,9600"
     * - "scanpal2:COM1"
     *
     * @param raw Raw scanner configuration string.
     * @return Fully parsed {@link ScannerConfig}.
     */
    public static ScannerConfig parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return new ScannerConfig(ScannerProtocol.NONE, "");
        }

        String trimmed = raw.trim();
        int colonIdx = trimmed.indexOf(':');

        if (colonIdx < 0) {
            ScannerProtocol protocol = ScannerProtocol.fromToken(trimmed);
            return new ScannerConfig(protocol, "");
        }

        String typeToken = trimmed.substring(0, colonIdx).trim();
        String params = trimmed.substring(colonIdx + 1).trim();

        int commaIdx = params.indexOf(',');
        String port;
        Map<String, String> props = Collections.emptyMap();

        if (commaIdx >= 0) {
            port = params.substring(0, commaIdx).trim();
            String param2 = params.substring(commaIdx + 1).trim();
            props = Map.of("param2", param2);
        } else {
            port = params;
        }

        ScannerProtocol protocol = ScannerProtocol.fromToken(typeToken);
        return new ScannerConfig(protocol, port, props);
    }
}
