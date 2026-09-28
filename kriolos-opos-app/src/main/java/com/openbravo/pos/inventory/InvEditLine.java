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

package com.openbravo.pos.inventory;

import java.awt.Component;

/**
 * Backward-compatible adapter delegating to {@link InvEditLinePanel}.
 *
 * @deprecated Use {@link InvEditLinePanel#show(Component, String, String)} instead.
 */
@Deprecated
public class InvEditLine {

    private InvEditLine() {
    }

    public static String show(Component parent, String title, String initialPrice) {
        return InvEditLinePanel.show(parent, title, initialPrice);
    }
}
