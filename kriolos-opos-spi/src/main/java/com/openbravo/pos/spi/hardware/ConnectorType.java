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
 * Physical or logical connector transport scheme used to interface with a hardware peripheral.
 * Defines canonical URNs and transport scheme identifiers for URI/endpoint resolution.
 *
 * @author KriolOS Team
 * @since 1.0.0
 */
public enum ConnectorType {
    SERIAL("serial", "urn:kriolos:hardware:connector:serial", "RS-232 Serial Port (RXTX / Comm / TTY)"),
    NETWORK("network", "urn:kriolos:hardware:connector:network", "TCP/IP Raw Socket Network Connection"),
    USB("usb", "urn:kriolos:hardware:connector:usb", "Raw USB Direct Endpoint"),
    FILE("file", "urn:kriolos:hardware:connector:file", "File System Path / Character Device Node"),
    PIPE("pipe", "urn:kriolos:hardware:connector:pipe", "Named Pipe / OS Stream"),
    JAVAPOS("javapos", "urn:kriolos:hardware:connector:javapos", "JavaPOS Subsystem Endpoint"),
    SYSTEM("system", "urn:kriolos:hardware:connector:system", "OS Print Spooler / Java Print Service"),
    SCREEN("screen", "urn:kriolos:hardware:connector:screen", "Virtual Screen / GUI Panel"),
    NONE("none", "urn:kriolos:hardware:connector:none", "Virtual / Disconnected / No Connector");

    private final String code;
    private final String urn;
    private final String description;

    ConnectorType(String code, String urn, String description) {
        this.code = code;
        this.urn = urn;
        this.description = description;
    }

    /**
     * Canonical short identifier code for configuration parsing and serialization.
     *
     * @return code string (e.g. "serial", "network").
     */
    public String getCode() {
        return code;
    }

    /**
     * Canonical URI transport scheme (e.g. "serial", "network").
     *
     * @return scheme string.
     */
    public String getScheme() {
        return code;
    }

    /**
     * Canonical URN representation (e.g. "urn:kriolos:hardware:connector:serial").
     *
     * @return URN string.
     */
    public String getUrn() {
        return urn;
    }

    /**
     * Human-readable description of this connector mechanism.
     *
     * @return description string.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Standard URI selector (e.g. "connector:serial").
     *
     * @return URI selector string.
     */
    public String getSelector() {
        return "connector:" + code;
    }

    /**
     * Resolves a {@link ConnectorType} from a configuration string, scheme, URN, or transport scheme alias.
     *
     * @param token String representation (e.g. "serial", "rxtx", "tcp", "network", "file", "usb").
     * @return Corresponding {@link ConnectorType}, defaulting to {@link #NONE} if unrecognized.
     */
    public static ConnectorType fromCode(String token) {
        if (token == null || token.isBlank()) {
            return NONE;
        }
        String clean = token.trim().toLowerCase();

        for (ConnectorType c : values()) {
            if (c.code.equalsIgnoreCase(clean)
                    || c.name().equalsIgnoreCase(clean)
                    || c.urn.equalsIgnoreCase(clean)
                    || c.getSelector().equalsIgnoreCase(clean)) {
                return c;
            }
        }

        // Scheme aliases
        switch (clean) {
            case "rxtx":
            case "comm":
            case "rs232":
            case "tty":
                return SERIAL;
            case "tcp":
            case "socket":
            case "ethernet":
            case "ip":
            case "lan":
                return NETWORK;
            case "hid":
            case "rawusb":
                return USB;
            case "dev":
            case "device":
                return FILE;
            case "fifo":
                return PIPE;
            case "jpos":
                return JAVAPOS;
            case "printer":
            case "spooler":
            case "cups":
            case "os":
                return SYSTEM;
            case "panel":
            case "window":
            case "dual":
                return SCREEN;
            case "null":
            case "disabled":
                return NONE;
            default:
                return NONE;
        }
    }

    /**
     * Resolves a {@link ConnectorType} from a URI scheme alias.
     *
     * @param scheme URI scheme token.
     * @return Matching {@link ConnectorType}.
     */
    public static ConnectorType fromScheme(String scheme) {
        return fromCode(scheme);
    }
}
