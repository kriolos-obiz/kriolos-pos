/*
 * Copyright (C) 2022-2026 KriolOS
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
package com.openbravo.pos.printer;

import com.openbravo.pos.spi.hardware.display.DisplayDevice;
import com.openbravo.pos.spi.hardware.display.DisplayProtocol;

/**
 * High-level contract for customer visor and display operations.
 *
 * @author JG uniCenta / KriolOS Team
 */
public interface DeviceDisplay extends DisplayDevice {

    public static final int ANIMATION_NULL = 0;
    public static final int ANIMATION_FLYER = 1;
    public static final int ANIMATION_SCROLL = 2;
    public static final int ANIMATION_BLINK = 3;
    public static final int ANIMATION_CURTAIN = 4;

    @Override
    default DisplayProtocol getProtocol() {
        return DisplayProtocol.SUREPOS;
    }

    @Override
    default boolean isConnected() {
        return true;
    }

    String getDisplayName();

    String getDisplayDescription();

    void writeVisor(int animation, String sLine1, String sLine2);

    void writeVisor(String sLine1, String sLine2);

    void clearVisor();

    void repaintLines();

    /**
     * Changes display light style (for LED8 pole displays).
     *
     * @param iStyle Style index.
     */
    default void displayLight(int iStyle) {
        // Optional capability
    }

    /**
     * Changes display status line (for PD-LED8 pole displays).
     *
     * @param status Status index.
     */
    default void changeStatus(int status) {
        // Optional capability
    }
}
