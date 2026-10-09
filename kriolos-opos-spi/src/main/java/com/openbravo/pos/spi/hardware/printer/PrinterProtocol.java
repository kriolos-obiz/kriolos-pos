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
 * Type-safe enumeration of receipt and kitchen ticket printer protocols.
 *
 * @author KriolOS Team
 * @since 1.0.0
 */
public enum PrinterProtocol {
    SCREEN("screen", "Virtual Screen Receipt Panel", false),
    PRINTER("printer", "Standard Java Print Service", false),
    EPSON("epson", "Epson ESC/POS", true),
    TMU220("tmu220", "Epson TM-U220", true),
    STAR("star", "Star Micronics", true),
    ITHACA("ithaca", "Ithaca POS", true),
    SUREPOS("surepos", "IBM SurePOS", true),
    PLAIN("plain", "Plain Text Raw Stream", true),
    JAVAPOS("javapos", "JavaPOS Receipt Printer", false),
    NONE("none", "Disabled / No Printer", false);

    private final String token;
    private final String displayName;
    private final boolean serialOrComm;

    PrinterProtocol(String token, String displayName, boolean serialOrComm) {
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
     * Resolves a PrinterProtocol strictly from string tokens.
     *
     * @param token String identifier from configuration.
     * @return The matching {@link PrinterProtocol}, or {@link #NONE} if unrecognized.
     */
    public static PrinterProtocol fromToken(String token) {
        if (token == null || token.isBlank()) {
            return NONE;
        }
        String clean = token.trim().toLowerCase();

        for (PrinterProtocol p : values()) {
            if (p.token.equalsIgnoreCase(clean)) {
                return p;
            }
        }
        return NONE;
    }
}
