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

import com.openbravo.pos.spi.hardware.ConnectorType;
import com.openbravo.pos.spi.hardware.DeviceConfig;
import com.openbravo.pos.spi.hardware.TransportType;

import java.util.Collections;
import java.util.Map;

/**
 * Immutable configuration descriptor for instantiating a ticket/receipt printer.
 *
 * @param protocol   Target printer command set / protocol dialect.
 * @param connector  Communication transport scheme / connector ({@link ConnectorType}).
 * @param target     Target address, port, endpoint (e.g. "/dev/ttyUSB0", "COM1", "192.168.1.100:9100"), or drawer name.
 * @param properties Key-value properties map.
 *
 * @author KriolOS Team
 * @since 1.0.0
 */
public record PrinterConfig(
        PrinterProtocol protocol,
        ConnectorType connector,
        String target,
        Map<String, String> properties
) implements DeviceConfig {

    public PrinterConfig(PrinterProtocol protocol, ConnectorType connector, String target) {
        this(protocol, connector, target, Collections.emptyMap());
    }

    public PrinterConfig(PrinterProtocol protocol, TransportType transport, String target) {
        this(protocol, transport != null ? transport.toConnectorType() : ConnectorType.NONE, target, Collections.emptyMap());
    }

    /** Transport scheme alias for connector. */
    public ConnectorType transport() {
        return connector;
    }

    /** Scheme alias for connector. */
    public ConnectorType scheme() {
        return connector;
    }

    @Override
    public String getPortOrTarget() {
        return target != null && !target.isBlank() ? target : (connector != null ? connector.getCode() : "");
    }

    @Override
    public Map<String, String> getProperties() {
        return properties != null ? properties : Collections.emptyMap();
    }

    /**
     * Parses a printer property configuration line strictly according to current syntax.
     * Format:
     * - {@code "<protocol>"} (e.g. {@code "screen"})
     * - {@code "<protocol>:<connector>,<target>"} (e.g. {@code "epson:serial,/dev/ttyUSB0"}, {@code "epson:network,192.168.1.100:9100"})
     * - {@code "<protocol>:<target>"} (e.g. {@code "printer:ReceiptPrinter"}, {@code "javapos:jposDrawer"})
     *
     * @param raw Raw printer configuration string.
     * @return Fully parsed {@link PrinterConfig}.
     */
    public static PrinterConfig parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return new PrinterConfig(PrinterProtocol.NONE, ConnectorType.NONE, "");
        }

        String trimmed = raw.trim();
        int colonIdx = trimmed.indexOf(':');

        if (colonIdx < 0) {
            PrinterProtocol protocol = PrinterProtocol.fromToken(trimmed);
            ConnectorType connector = protocol == PrinterProtocol.SCREEN ? ConnectorType.SCREEN : ConnectorType.NONE;
            return new PrinterConfig(protocol, connector, "");
        }

        String typeToken = trimmed.substring(0, colonIdx).trim();
        String params = trimmed.substring(colonIdx + 1).trim();

        PrinterProtocol protocol = PrinterProtocol.fromToken(typeToken);

        int commaIdx = params.indexOf(',');
        if (commaIdx >= 0) {
            String p1 = params.substring(0, commaIdx).trim();
            String p2 = params.substring(commaIdx + 1).trim();
            ConnectorType connector = ConnectorType.fromCode(p1);
            return new PrinterConfig(protocol, connector, p2);
        }

        if (protocol == PrinterProtocol.PRINTER) {
            return new PrinterConfig(protocol, ConnectorType.SYSTEM, params);
        } else if (protocol == PrinterProtocol.JAVAPOS) {
            return new PrinterConfig(protocol, ConnectorType.JAVAPOS, params);
        }

        ConnectorType connector = ConnectorType.fromCode(params);
        String target = connector != ConnectorType.NONE ? "" : params;
        return new PrinterConfig(protocol, connector, target);
    }
}
