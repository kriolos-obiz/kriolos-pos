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

/**
 * Type-safe enumeration of customer-facing visor and screen display protocols.
 *
 * @author KriolOS Team
 * @since 1.0.0
 */
public enum DisplayProtocol {
    SCREEN("screen", "In-App Virtual Display Panel", false),
    WINDOW("window", "Customer Display Window", false),
    DUAL("dual", "Dual Screen Customer Display", false),
    EPSON("epson", "Epson ESC/POS 2x20 VFD Line Display", true),
    SUREPOS("surepos", "IBM SurePOS Customer Display", true),
    LD200("ld200", "Logic Controls LD200 Display", true),
    JAVAPOS("javapos", "JavaPOS Customer Display", false),
    LED8("led8", "Generic LED 8-Digit Display", true),
    PDLED8("pdled8", "Posiflex PD LED 8-Digit Display", true),
    NONE("none", "Disabled / No Display", false);

    private final String token;
    private final String displayName;
    private final boolean serialOrComm;

    DisplayProtocol(String token, String displayName, boolean serialOrComm) {
        this.token = token;
        this.displayName = displayName;
        this.serialOrComm = serialOrComm;
    }

    public String getToken() {
        return token;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isSerialOrComm() {
        return serialOrComm;
    }

    /**
     * Resolves a DisplayProtocol from string tokens with legacy alias normalization.
     *
     * @param token String identifier from configuration.
     * @return The matching {@link DisplayProtocol}, or {@link #NONE} if unrecognized.
     */
    public static DisplayProtocol fromToken(String token) {
        if (token == null || token.isBlank()) {
            return NONE;
        }
        String clean = token.trim().toLowerCase();

        // Legacy serial aliases: "serial", "rxtx", "file" resolve to EPSON ESC/POS by default
        if ("serial".equals(clean) || "rxtx".equals(clean) || "file".equals(clean)) {
            return EPSON;
        }

        for (DisplayProtocol p : values()) {
            if (p.token.equalsIgnoreCase(clean)) {
                return p;
            }
        }
        return NONE;
    }
}
