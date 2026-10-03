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
package com.openbravo.pos.display.led8;

import com.openbravo.pos.printer.DeviceTicket;
import com.openbravo.pos.printer.escpos.DeviceDisplaySerial;
import com.openbravo.pos.printer.escpos.ESCPOS;
import com.openbravo.pos.printer.escpos.PrinterWritter;
import com.openbravo.pos.printer.escpos.UnicodeTranslator;
import com.openbravo.pos.printer.escpos.UnicodeTranslatorInt;
import com.openbravo.pos.spi.hardware.display.DisplayProtocol;

/**
 * Generic ESC/POS 8-digit customer pole display driver.
 *
 * @author KriolOS Team
 * @since 1.0.0
 */
public class DeviceDisplayLED8 extends DeviceDisplaySerial {

    private final UnicodeTranslator trans;

    public DeviceDisplayLED8(PrinterWritter display) {
        this.trans = new UnicodeTranslatorInt();
        init(display);
    }

    @Override
    public DisplayProtocol getProtocol() {
        return DisplayProtocol.LED8;
    }

    @Override
    public void initVisor() {
        this.display.init(ESCPOS.INIT);
        this.display.write(ESCPOS.SELECT_DISPLAY);
        this.display.write(this.trans.getCodeTable());
        this.display.write(ESCPOS.VISOR_HIDE_CURSOR);
        this.display.write(ESCPOS.VISOR_CLEAR);
        this.display.write(ESCPOS.VISOR_HOME);
        this.display.flush();
    }

    @Override
    public void repaintLines() {
        this.display.write(ESCPOS.SELECT_DISPLAY);
        this.display.write(ESCPOS.VISOR_CLEAR);
        this.display.write(ESCPOS.VISOR_HOME);
        this.display.write(new byte[]{27, 81, 65});
        this.display.write(this.trans.transString(DeviceTicket.alignLeft(this.baseDeviceDisplay.getLine1(), 8)));
        this.display.write(new byte[]{13});
        this.display.flush();
    }

    /**
     * Change Display Light Style.
     *
     * @param iStyle Style index (1 to 4).
     */
    public void displayLight(int iStyle) {
        this.display.write(ESCPOS.SELECT_DISPLAY);
        switch (iStyle) {
            case 1:
                this.display.write(new byte[]{27, 115, 49});
                return;
            case 2:
                this.display.write(new byte[]{27, 115, 50});
                return;
            case 3:
                this.display.write(new byte[]{27, 115, 51});
                return;
            case 4:
                this.display.write(new byte[]{27, 115, 52});
                return;
            default:
                this.display.write(new byte[]{27, 115, 48});
        }
    }
}
