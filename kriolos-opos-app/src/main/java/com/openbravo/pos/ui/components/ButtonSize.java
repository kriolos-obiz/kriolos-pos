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


public enum ButtonSize {
    MEDIUM(32, 12, 14),
    LARGE(40, 14, 15),
    EXTRA_LARGE(48, 16, 16), // Ideal for Touch / POS
    MASSIVE(56, 20, 18);     // Massive hero/checkout CTA

    // Campos alterados para private para respeitar o encapsulamento
    private final int height;
    private final int paddingX;
    private final int fontSize;

    ButtonSize(int height, int paddingX, int fontSize) {
        this.height = height;
        this.paddingX = paddingX;
        this.fontSize = fontSize;
    }

    // Getter público para extrair a altura numérica
    public int getHeight() {
        return this.height;
    }

    public int getPaddingX() {
        return this.paddingX;
    }

    public int getFontSize() {
        return this.fontSize;
    }
}
