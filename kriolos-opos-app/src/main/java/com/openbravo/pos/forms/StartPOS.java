//    KriolOS POS
//    Copyright (c) 2019-2023 KriolOS
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
//    along with this program.  If not, see <http://www.gnu.org/licenses/>
package com.openbravo.pos.forms;

/**
 * Legacy entry point. Deprecated in favor of {@link BootstrapPOS}.
 *
 * @deprecated Use {@link BootstrapPOS} instead.
 */
@Deprecated(since = "1.0.0", forRemoval = true)
public class StartPOS {

    public static void setExitAction(Runnable action) {
        BootstrapPOS.setExitAction(action);
    }

    public static void resetExitAction() {
        BootstrapPOS.resetExitAction();
    }

    public static void main(final String args[]) {
        BootstrapPOS.main(args);
    }

    static boolean checkSingletonInstance(WindowShell rootFrame, AppConfig config) {
        return BootstrapPOS.checkSingletonInstance(rootFrame, config);
    }
}
