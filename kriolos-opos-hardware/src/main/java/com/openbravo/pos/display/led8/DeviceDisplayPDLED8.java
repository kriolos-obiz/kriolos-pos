/*
 * Copyright (C) 2026 Paulo Borges
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
import com.openbravo.pos.printer.escpos.PrinterWritter;
import com.openbravo.pos.spi.hardware.display.DisplayProtocol;

/**
 * PD-LED8 represents a Pole Display LED8 Device (Posiflex protocol).
 *  First Line: NdNdNdNdNdNdNdN (ex: "8.8.8.8.8.8.8.8" for 8 DIGIT) 
 *  Second Line: Fixed Status/Icon/Word (Unit Price, Total Amount, Tendered, Change)
 *
 * @author Paulo Borges
 * @since 1.0.0
 */
public class DeviceDisplayPDLED8 extends DeviceDisplaySerial {
    
    private static final int PD_LED8_MAX_NUM_OF_DIGIT = 8;

    private final AZ09Translator trans;

    public DeviceDisplayPDLED8(PrinterWritter pWritter) {
        init(pWritter);
        this.trans = new AZ09Translator();
    }

    @Override
    public DisplayProtocol getProtocol() {
        return DisplayProtocol.PDLED8;
    }

    @Override
    public void initVisor() {
        this.display.init(CODE.CMD_VISOR_CLEAR);
        this.display.flush();
    }

    @Override
    public void repaintLines() {
        this.display.write(CODE.CMD_VISOR_CLEAR);
        this.display.write(CODE.CMD_HEADER);
        this.display.write(this.trans.translateString(DeviceTicket.alignRight(this.baseDeviceDisplay.getLine1(), PD_LED8_MAX_NUM_OF_DIGIT)));
        this.display.write(CODE.CMD_TERMINATOR);
        this.display.flush();
    }

    public void changeStatus(int status) {
        switch (status) {
            case 0 -> {
                this.display.write(CODE.CMD_STATUS_CELAR);
                return;
            }
            case 1 -> {
                this.display.write(CODE.CMD_STATUS_PRICE);
                return;
            }
            case 2 -> {
                this.display.write(CODE.CMD_STATUS_TOTAL_AMOUNT);
                return;
            }
            case 3 -> {
                this.display.write(CODE.CMD_STATUS_TOTAL_TENDERED);
                return;
            }
            case 4 -> {
                this.display.write(CODE.CMD_STATUS_TOTAL_CHANGE);
                return;
            }
            default -> {
                this.display.write(CODE.CMD_STATUS_CELAR);
            }
        }
    }
    
    private static class CODE {
        public static final byte ASCII_ESC = 0x1B;
        public static final byte ASCII_CR = 0x0D;
        public static final byte ASCII_Q = 0x51;
        public static final byte ASCII_A = 0x41;
        public static final byte ASCII_S_LOWER = 0x73;
        public static final byte ASCII_NUM_0 = 0x30;
        public static final byte ASCII_NUM_1 = 0x31;
        public static final byte ASCII_NUM_2 = 0x32;
        public static final byte ASCII_NUM_3 = 0x33;
        public static final byte ASCII_NUM_4 = 0x34;
        
        public static final byte[] CMD_HEADER = {ASCII_ESC, ASCII_Q, ASCII_A};
        public static final byte[] CMD_TERMINATOR = {ASCII_CR};
        
        public static final byte[] CMD_VISOR_CLEAR = {
            ASCII_ESC, ASCII_S_LOWER, ASCII_NUM_0, // ESC s 0
            ASCII_ESC, ASCII_Q, ASCII_A, ASCII_CR  // ESC Q A CR
        };
        
        public static final byte[] CMD_STATUS_CELAR = {ASCII_ESC, ASCII_S_LOWER, ASCII_NUM_0}; // ESC s 0
        public static final byte[] CMD_STATUS_PRICE = {ASCII_ESC, ASCII_S_LOWER, ASCII_NUM_1}; // ESC s 1
        public static final byte[] CMD_STATUS_TOTAL_AMOUNT = {ASCII_ESC, ASCII_S_LOWER, ASCII_NUM_2}; // ESC s 2
        public static final byte[] CMD_STATUS_TOTAL_TENDERED = {ASCII_ESC, ASCII_S_LOWER, ASCII_NUM_3}; // ESC s 3
        public static final byte[] CMD_STATUS_TOTAL_CHANGE = {ASCII_ESC, ASCII_S_LOWER, ASCII_NUM_4}; // ESC s 4
    }
}
