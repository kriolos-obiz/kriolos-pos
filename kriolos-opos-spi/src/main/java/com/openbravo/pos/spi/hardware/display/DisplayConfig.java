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
package com.openbravo.pos.spi.hardware.display;

import com.openbravo.pos.spi.hardware.ConnectorType;
import com.openbravo.pos.spi.hardware.DeviceConfig;
import com.openbravo.pos.spi.hardware.TransportType;

import java.util.Collections;
import java.util.Map;

/**
 * Immutable configuration descriptor for instantiating a customer visor display.
 *
 * @param protocol   Target display protocol dialect.
 * @param connector  Communication transport scheme / connector ({@link ConnectorType}).
 * @param target     Target address, COM port, baud rate, or window descriptor.
 * @param properties Key-value properties map.
 *
 * @author KriolOS Team
 * @since 1.0.0
 */
public record DisplayConfig(
        DisplayProtocol protocol,
        ConnectorType connector,
        String target,
        Map<String, String> properties
) implements DeviceConfig {

    public DisplayConfig(DisplayProtocol protocol, ConnectorType connector, String target) {
        this(protocol, connector, target, Collections.emptyMap());
    }

    public DisplayConfig(DisplayProtocol protocol, TransportType transport, String target) {
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
     * Parses a display property line strictly according to current syntax.
     * Format:
     * - {@code "<protocol>"} (e.g. {@code "screen"}, {@code "window"}, {@code "dual"})
     * - {@code "<protocol>:<connector>,<target>"} (e.g. {@code "epson:serial,COM2,9600"}, {@code "led8:serial,/dev/ttyUSB0,2400"})
     * - {@code "<protocol>:<target>"} (e.g. {@code "javapos:display1"})
     *
     * @param raw Raw display configuration string.
     * @return Fully parsed {@link DisplayConfig}.
     */
    public static DisplayConfig parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return new DisplayConfig(DisplayProtocol.NONE, ConnectorType.NONE, "");
        }

        String trimmed = raw.trim();
        int colonIdx = trimmed.indexOf(':');

        if (colonIdx < 0) {
            DisplayProtocol protocol = DisplayProtocol.fromToken(trimmed);
            ConnectorType connector = protocol == DisplayProtocol.SCREEN
                    || protocol == DisplayProtocol.WINDOW
                    || protocol == DisplayProtocol.DUAL
                    ? ConnectorType.SCREEN
                    : ConnectorType.NONE;
            return new DisplayConfig(protocol, connector, "");
        }

        String typeToken = trimmed.substring(0, colonIdx).trim();
        String params = trimmed.substring(colonIdx + 1).trim();

        DisplayProtocol protocol = DisplayProtocol.fromToken(typeToken);

        int commaIdx = params.indexOf(',');
        if (commaIdx >= 0) {
            String p1 = params.substring(0, commaIdx).trim();
            String p2 = params.substring(commaIdx + 1).trim();
            ConnectorType connector = ConnectorType.fromCode(p1);
            return new DisplayConfig(protocol, connector, p2);
        }

        if (protocol == DisplayProtocol.JAVAPOS) {
            return new DisplayConfig(protocol, ConnectorType.JAVAPOS, params);
        }

        ConnectorType connector = ConnectorType.fromCode(params);
        String target = connector != ConnectorType.NONE ? "" : params;
        return new DisplayConfig(protocol, connector, target);
    }
}
