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

import com.openbravo.pos.printer.TicketPrinterException;
import com.openbravo.pos.printer.escpos.PrinterWritterRXTX;

/**
 * Manual test runner for DeviceDisplayPDLED8.
 *
 * @author Administrator
 */
public class DeviceDisplayPDLED8Main {

    public static void main(String[] args) throws TicketPrinterException {
        DeviceDisplayPDLED8 m_devicedisplay = new DeviceDisplayPDLED8(new PrinterWritterRXTX("COM2", 2400));

        System.out.println("initVisor...");
        m_devicedisplay.initVisor(); 
        System.out.println("clearVisor...");       
        m_devicedisplay.clearVisor();  
        System.out.println("changeStatus...");      
        m_devicedisplay.changeStatus(1);
    }
}
