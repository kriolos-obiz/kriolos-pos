/*
 * Copyright (C) 2022 KiolOS<https://github.com/kriolos>
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

package com.openbravo.pos.util;

/**
 *
 * @author JG uniCenta
 */
public class OSValidator {

    private static final String OS = System.getProperty("os.name").toLowerCase();

    private OSValidator() {}

    public static String getOS() {
        if (isWindows()) {
            return ("w");
        } else if (isMac()) {
            return ("m");
        } else if (isUnix()) {
            return ("l");
        } else if (isSolaris()) {
            return ("s");
        } else {
            return ("x");
        }
    }

    public static boolean isWindows() {
        return (OS.contains("win"));
    }


    public static boolean isMac() {
        return (OS.contains("mac"));
    }

    public static boolean isUnix() {
        return (OS.contains("nix") || OS.contains("nux") || OS.contains("aix"));
    }

    public static boolean isSolaris() {
        return (OS.contains("sunos"));
    }

}
