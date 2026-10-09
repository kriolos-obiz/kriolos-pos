/*
 * Copyright (C) 2022 KriolOS
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

/**
 *
 * @author adrianromero
 */
public class ScrollAnimator extends BaseAnimator {

    private int msglength;

    /**
     *
     * @param line1
     * @param line2
     */
    public ScrollAnimator(String line1, String line2) {
        msglength = Math.max(line1.length(), line2.length());
        baseLine1 = PrinterTextUtils.alignLeft(line1, msglength);
        baseLine2 = PrinterTextUtils.alignLeft(line2, msglength);
    }

    /**
     *
     * @param i
     */
    @Override
    public void setTiming(int i) {
        int j = (i / 2) % (msglength + 20);
        if (j < 20) {
            currentLine1 = PrinterTextUtils.alignLeft(PrinterTextUtils.getPaddingString(20 - j) + baseLine1, 20);
            currentLine2 = PrinterTextUtils.alignLeft(PrinterTextUtils.getPaddingString(20 - j) + baseLine2, 20);
        } else {
            currentLine1 = PrinterTextUtils.alignLeft(baseLine1.substring(j - 20), 20);
            currentLine2 = PrinterTextUtils.alignLeft(baseLine2.substring(j - 20), 20);
        }
    }
}
