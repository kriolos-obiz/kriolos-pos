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
 * Immutable configuration descriptor for instantiating a ticket/receipt printer.
 *
 * @param protocol   Target printer protocol.
 * @param param1     First parameter (e.g. port name, service name, or interface type "serial"/"rxtx").
 * @param param2     Second parameter (e.g. baud rate, paper profile "receipt"/"standard", or parity).
 * @param properties Key-value properties map.
 *
 * @author KriolOS Team
 * @since 1.0.0
 */
public record PrinterConfig(
        PrinterProtocol protocol,
        String param1,
        String param2,
        Map<String, String> properties
) implements DeviceConfig {

    public PrinterConfig(PrinterProtocol protocol, String param1, String param2) {
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
     * Parses a printer property line.
     * Examples:
     * - "screen"
     * - "epson:COM1,9600"
     * - "serial:/dev/ttyS0,9600"
     * - "printer:receipt,standard"
     *
     * @param raw Raw printer configuration string.
     * @return Fully hydrated {@link PrinterConfig}.
     */
    public static PrinterConfig parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return new PrinterConfig(PrinterProtocol.NONE, "", "");
        }

        String trimmed = raw.trim();
        int colonIdx = trimmed.indexOf(':');

        if (colonIdx < 0) {
            PrinterProtocol protocol = PrinterProtocol.fromToken(trimmed);
            return new PrinterConfig(protocol, "", "");
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

        // Legacy normalization: serial / rxtx / file alias
        String cleanType = typeToken.toLowerCase();
        if ("serial".equals(cleanType) || "rxtx".equals(cleanType) || "file".equals(cleanType)) {
            p2 = p1;
            p1 = typeToken;
            typeToken = "epson";
        }

        PrinterProtocol protocol = PrinterProtocol.fromToken(typeToken);
        return new PrinterConfig(protocol, p1, p2);
    }
}
