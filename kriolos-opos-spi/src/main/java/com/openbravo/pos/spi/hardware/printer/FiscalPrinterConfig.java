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

import com.openbravo.pos.spi.hardware.DeviceConfig;

import java.util.Collections;
import java.util.Map;

/**
 * Immutable configuration descriptor for fiscal memory printers.
 *
 * @param protocol   Fiscal protocol type (e.g. "javapos", "null").
 * @param target     Logical name or connection descriptor.
 * @param properties Key-value properties map.
 *
 * @author KriolOS Team
 * @since 1.0.0
 */
public record FiscalPrinterConfig(
        String protocol,
        String target,
        Map<String, String> properties
) implements DeviceConfig {

    public FiscalPrinterConfig(String protocol, String target) {
        this(protocol, target, Collections.emptyMap());
    }

    @Override
    public String getPortOrTarget() {
        return target;
    }

    @Override
    public Map<String, String> getProperties() {
        return properties != null ? properties : Collections.emptyMap();
    }

    public static FiscalPrinterConfig parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return new FiscalPrinterConfig("none", "");
        }
        String trimmed = raw.trim();
        int colonIdx = trimmed.indexOf(':');
        if (colonIdx < 0) {
            return new FiscalPrinterConfig(trimmed.toLowerCase(), "");
        }
        String type = trimmed.substring(0, colonIdx).trim().toLowerCase();
        String target = trimmed.substring(colonIdx + 1).trim();
        return new FiscalPrinterConfig(type, target);
    }
}
