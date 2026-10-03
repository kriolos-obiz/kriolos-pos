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
 * @author JG uniCenta
 */
public class TicketPrinterException extends com.openbravo.pos.spi.hardware.printer.PrinterException {

    private static final long serialVersionUID = 1L;

    public TicketPrinterException() {
        super();
    }

    public TicketPrinterException(String msg) {
        super(msg);
    }
    public TicketPrinterException(String msg, Throwable cause) {
        super(msg, cause);
    }
}


