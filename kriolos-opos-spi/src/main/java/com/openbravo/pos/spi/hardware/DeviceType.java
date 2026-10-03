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
 * Categorization of hardware peripherals supported by the KriolOS POS platform.
 * Defines standard URNs and well-known code identifiers for seamless parsing,
 * configuration, and integration with {@link com.openbravo.pos.spi.annotation.PluginMetadata}.
 *
 * @author KriolOS Team
 * @since 1.0.0
 */
public enum DeviceType {
    SCALE("scale", "urn:kriolos:hardware:scale", "Weight Scale"),
    PRINTER("printer", "urn:kriolos:hardware:printer", "Receipt / Kitchen Printer"),
    DISPLAY("display", "urn:kriolos:hardware:display", "Customer Facing Display"),
    SCANNER("scanner", "urn:kriolos:hardware:scanner", "Barcode Scanner / Mobile Terminal"),
    FISCAL_PRINTER("fiscal_printer", "urn:kriolos:hardware:fiscal_printer", "Fiscal Memory Printer"),
    CASH_DRAWER("cash_drawer", "urn:kriolos:hardware:cash_drawer", "Cash Drawer Trigger"),
    PAYMENT_TERMINAL("payment_terminal", "urn:kriolos:hardware:payment_terminal", "EFT / Payment Terminal");

    private final String code;
    private final String urn;
    private final String description;

    DeviceType(String code, String urn, String description) {
        this.code = code;
        this.urn = urn;
        this.description = description;
    }

    /**
     * Short well-known identifier code (e.g. "scale", "printer").
     *
     * @return short code string.
     */
    public String getCode() {
        return code;
    }

    /**
     * Canonical URN representation (e.g. "urn:kriolos:hardware:scale").
     *
     * @return URN string.
     */
    public String getUrn() {
        return urn;
    }

    /**
     * Human-readable description of this hardware category.
     *
     * @return description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Standard URI selector (e.g. "hardware:scale") aligned with package and {@link com.openbravo.pos.spi.annotation.PluginMetadata#selectors()}.
     *
     * @return URI selector string.
     */
    public String getSelector() {
        return "hardware:" + code;
    }

    /**
     * Resolves a {@link DeviceType} from any valid representation:
     * - Short code ("scale", "printer")
     * - Enum constant name ("SCALE", "PRINTER")
     * - Hardware selector ("hardware:scale")
     * - Legacy selector ("device:scale")
     * - Canonical URN ("urn:kriolos:hardware:scale")
     *
     * @param input Input token or URN.
     * @return The matching {@link DeviceType}, or {@code null} if unrecognized.
     */
    public static DeviceType fromCode(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }
        String clean = input.trim().toLowerCase();

        for (DeviceType type : values()) {
            if (type.code.equalsIgnoreCase(clean)
                    || type.name().equalsIgnoreCase(clean)
                    || type.urn.equalsIgnoreCase(clean)
                    || type.getSelector().equalsIgnoreCase(clean)
                    || ("device:" + type.code).equalsIgnoreCase(clean)) {
                return type;
            }
        }
        return null;
    }
}
