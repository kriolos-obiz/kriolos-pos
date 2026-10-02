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
package com.openbravo.pos.ui.components;

/**
 * Standard zero-dependency font-based Unicode vector icons for touch POS
 * components,
 * action panels, toolbars, and buttons.
 * <p>
 * Implemented as an {@link Enum} allowing iteration, enumeration, and discovery
 * via {@link #values()}, {@link #getCode()}, and {@link #getName()}.
 *
 * @author KriolOS Team
 */
public enum UnicodeIcon {

    // People & Identification
    CUSTOMER("\uD83D\uDC64", "Customer"), // 👤
    USER("\uD83D\uDC64", "User"), // 👤

    // Orders & Tickets
    ORDERS("\uD83D\uDCCB", "Orders"), // 📋
    HOLD("\u23F8", "Hold"), // ⏸
    PARKED("\u23F8", "Parked"), // ⏸

    // Cash Operations
    CASH_IN("\uD83D\uDCE5", "Cash In"), // 📥
    CASH_OUT("\uD83D\uDCE4", "Cash Out"), // 📤
    CLOSE_CASH("\uD83D\uDD12", "Close Cash"), // 🔒

    // Line & Quantity Modifiers
    MINUS("\u2796", "Minus"), // ➖
    PLUS("\u2795", "Plus"), // ➕
    EDIT("\u270F", "Edit"), // ✏️
    DELETE("\uD83D\uDDD1", "Delete"), // 🗑️
    CLEAR("\u232B", "Clear"), // ⌫

    // Sales & Financials
    DISCOUNT("\uD83C\uDFF7", "Discount"), // 🏷️
    PAY("\uD83D\uDCB3", "Pay"), // 💳

    // Navigation, Status & Common Controls
    SEARCH("\uD83D\uDD0D", "Search"), // 🔍
    CHECK("\u2714", "Check"), // ✔
    CLOSE("\u2716", "Close"), // ✖
    SETTINGS("\u2699", "Settings"), // ⚙
    PRINTER("\uD83D\uDDA8", "Printer"), // 🖨
    REFRESH("\uD83D\uDD04", "Refresh"), // 🔄
    BARCODE("\uD83D\uDCF6", "Barcode"), // 📶
    NOTE("\uD83D\uDCDD", "Note"); // 📝

    private final String code;
    private final String name;

    UnicodeIcon(String code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * Gets the raw Unicode string representation of the icon glyph.
     *
     * @return Unicode character string (e.g. "\uD83D\uDC64")
     */
    public String getCode() {
        return code;
    }

    /**
     * Gets the human-readable display name of the icon.
     *
     * @return Descriptive name (e.g. "Customer", "Pay")
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the icon's Unicode character sequence.
     * Overriding toString enables seamless string concatenation (e.g. icon + " " +
     * label).
     */
    @Override
    public String toString() {
        return code;
    }

    /**
     * Finds a {@link UnicodeIcon} by its raw Unicode character sequence.
     *
     * @param code Unicode code sequence to match
     * @return Matching UnicodeIcon, or null if not found
     */
    public static UnicodeIcon fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (UnicodeIcon icon : values()) {
            if (icon.code.equals(code)) {
                return icon;
            }
        }
        return null;
    }
}
