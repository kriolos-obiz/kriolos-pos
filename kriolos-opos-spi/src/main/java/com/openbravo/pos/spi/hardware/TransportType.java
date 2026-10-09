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

/**
 * Transport type enumeration aligned with {@link ConnectorType}.
 *
 * @author KriolOS Team
 * @since 1.0.0
 */
public enum TransportType {
    SERIAL(ConnectorType.SERIAL),
    NETWORK(ConnectorType.NETWORK),
    USB(ConnectorType.USB),
    FILE(ConnectorType.FILE),
    PIPE(ConnectorType.PIPE),
    JAVAPOS(ConnectorType.JAVAPOS),
    SYSTEM(ConnectorType.SYSTEM),
    SCREEN(ConnectorType.SCREEN),
    NONE(ConnectorType.NONE);

    private final ConnectorType connectorType;

    TransportType(ConnectorType connectorType) {
        this.connectorType = connectorType;
    }

    public String getCode() {
        return connectorType.getCode();
    }

    public String getScheme() {
        return connectorType.getScheme();
    }

    public String getUrn() {
        return connectorType.getUrn();
    }

    public String getDescription() {
        return connectorType.getDescription();
    }

    public ConnectorType toConnectorType() {
        return connectorType;
    }

    public static TransportType fromCode(String token) {
        ConnectorType ct = ConnectorType.fromCode(token);
        for (TransportType t : values()) {
            if (t.connectorType == ct) {
                return t;
            }
        }
        return NONE;
    }

    public static TransportType fromConnectorType(ConnectorType connectorType) {
        if (connectorType == null) {
            return NONE;
        }
        for (TransportType t : values()) {
            if (t.connectorType == connectorType) {
                return t;
            }
        }
        return NONE;
    }
}
