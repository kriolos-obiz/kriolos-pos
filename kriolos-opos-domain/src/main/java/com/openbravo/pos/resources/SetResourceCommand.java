/*
 * Copyright (C) 2026 KriolOS POS
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
package com.openbravo.pos.resources;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

/**
 * Immutable command record representing a request to save or update an application resource.
 * Enforces non-blank validation invariants and defensive byte array copying.
 *
 * @param name The unique resource identifier/name (mandatory)
 * @param type The resource type indicator (0 = text/xml, 1 = image, 2 = binary)
 * @param data The raw resource payload bytes (defensively copied, optional)
 */
public record SetResourceCommand(
        String name,
        int type,
        byte[] data
) {
    public SetResourceCommand {
        Objects.requireNonNull(name, "name must not be null");
        if (name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        name = name.trim();

        data = data != null ? Arrays.copyOf(data, data.length) : null;
    }

    @Override
    public byte[] data() {
        return data != null ? Arrays.copyOf(data, data.length) : null;
    }

    /**
     * Factory method creating a command with explicit name, type, and byte array payload.
     */
    public static SetResourceCommand of(String name, int type, byte[] data) {
        return new SetResourceCommand(name, type, data);
    }

    /**
     * Factory method creating a binary resource command (type = 2).
     */
    public static SetResourceCommand ofBinary(String name, byte[] data) {
        return new SetResourceCommand(name, 2, data);
    }

    /**
     * Factory method creating a text/xml resource command (type = 0).
     */
    public static SetResourceCommand ofText(String name, String text) {
        byte[] bytes = text != null ? text.getBytes(StandardCharsets.UTF_8) : null;
        return new SetResourceCommand(name, 0, bytes);
    }
}

