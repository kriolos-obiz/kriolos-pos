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

/**
 * Type-safe enumeration of barcode scanner and batch handheld protocols.
 *
 * @author KriolOS Team
 * @since 1.0.0
 */
public enum ScannerProtocol {
    SCANPAL2("scanpal2", "CipherLab ScanPal 2 Batch Terminal", true),
    SERIAL("serial", "Generic Serial Barcode Scanner", true),
    KEYBOARD("keyboard", "Keyboard Wedge Barcode Scanner", false),
    NONE("none", "Disabled / No Scanner", false);

    private final String token;
    private final String displayName;
    private final boolean serial;

    ScannerProtocol(String token, String displayName, boolean serial) {
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

    public static ScannerProtocol fromToken(String token) {
        if (token == null || token.isBlank()) {
            return NONE;
        }
        String clean = token.trim().toLowerCase();
        for (ScannerProtocol p : values()) {
            if (p.token.equalsIgnoreCase(clean)) {
                return p;
            }
        }
        return NONE;
    }
}
