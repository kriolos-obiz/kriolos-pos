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

/**
 * Type-safe enumeration of weight scale protocols supported by the POS system.
 * Replaces legacy hardcoded magic string switches with strongly-typed protocol definitions.
 *
 * @author KriolOS Team
 * @since 1.0.0
 */
public enum ScaleProtocol {
    ACOM_PC100("acompc100", "Acom PC-100", true),
    AVERY_BERKEL_6720("averyberkel6720", "Avery Berkel 6720", true),
    CASIO_PD1("casiopd1", "Casio PD-1", true),
    DIALOG_1("dialog1", "Mettler Toledo / Dialog 06", true),
    SAMSUNG_ESP("samsungesp", "Samsung ESP", true),
    CAS_PDII("caspdii", "CAS PD-II", true),
    MT_IND221("mtind221", "Mettler Toledo IND221", true),
    JAVAPOS("javapos", "JavaPOS Scale", false),
    SCREEN("screen", "Virtual Screen Scale Dialog", false),
    FAKE("fake", "Mock / Fake Scale for Debugging", false),
    NONE("none", "Disabled / No Scale", false);

    private final String token;
    private final String displayName;
    private final boolean serial;

    ScaleProtocol(String token, String displayName, boolean serial) {
        this.token = token;
        this.displayName = displayName;
        this.serial = serial;
    }

    public String getToken() {
        return token;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isSerial() {
        return serial;
    }

    /**
     * Resolves a ScaleProtocol from a string token (case-insensitive).
     *
     * @param token String identifier from configuration (e.g. "caspdii", "fake").
     * @return The matching {@link ScaleProtocol}, or {@link #NONE} if unrecognized or empty.
     */
    public static ScaleProtocol fromToken(String token) {
        if (token == null || token.isBlank()) {
            return NONE;
        }
        String clean = token.trim().toLowerCase();
        for (ScaleProtocol p : values()) {
            if (p.token.equalsIgnoreCase(clean)) {
                return p;
            }
        }
        return NONE;
    }
}
