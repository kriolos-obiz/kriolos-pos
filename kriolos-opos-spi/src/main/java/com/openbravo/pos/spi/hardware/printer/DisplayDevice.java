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

import com.openbravo.pos.spi.hardware.DeviceType;
import com.openbravo.pos.spi.hardware.HardwareDevice;

/**
 * Contract representing an operational customer-facing display or visor.
 *
 * @author KriolOS Team
 * @since 1.0.0
 */
public interface DisplayDevice extends HardwareDevice {

    @Override
    default DeviceType getDeviceType() {
        return DeviceType.DISPLAY;
    }

    /**
     * Clears all content from the customer display.
     */
    void clearVisor();

    /**
     * Writes 2 lines of text to the visor.
     *
     * @param sLine1 First line content.
     * @param sLine2 Second line content.
     */
    void writeVisor(String sLine1, String sLine2);

    /**
     * Writes 2 lines of text to the visor with an optional animation mode.
     *
     * @param animation Animation effect index.
     * @param sLine1    First line content.
     * @param sLine2    Second line content.
     */
    default void writeVisor(int animation, String sLine1, String sLine2) {
        writeVisor(sLine1, sLine2);
    }

    /**
     * The hardware protocol implemented by this display.
     *
     * @return {@link DisplayProtocol} enum.
     */
    DisplayProtocol getProtocol();
}
