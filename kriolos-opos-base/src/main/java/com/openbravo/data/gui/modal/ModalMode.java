//    KriolOS POS
//    Copyright (c) 2019-2026 KriolOS
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program.  If not, see <http://www.gnu.org/licenses/>.
package com.openbravo.data.gui.modal;

/**
 * Defines the display presentation modes supported by {@link PosUIModal}.
 *
 * @author KriolOS
 */
public enum ModalMode {

    /**
     * Standard top-level OS/Swing {@link javax.swing.JDialog}.
     * Employs window owner hierarchy resolution and Wayland/FlatLaf focus & buffer validation.
     */
    NATIVE_DIALOG,

    /**
     * In-frame single window overlay rendered onto the parent {@link javax.swing.JLayeredPane}
     * or {@link javax.swing.JRootPane}.
     * Employs a backdrop overlay and {@link java.awt.SecondaryLoop} without spawning a separate OS window.
     */
    IN_FRAME_OVERLAY
}
