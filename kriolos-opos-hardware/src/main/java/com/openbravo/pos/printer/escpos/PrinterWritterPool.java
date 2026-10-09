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
package com.openbravo.pos.printer.escpos;

import com.openbravo.pos.printer.TicketPrinterException;
import com.openbravo.pos.spi.hardware.ConnectorType;
import com.openbravo.pos.spi.hardware.TransportType;

import java.util.HashMap;
import java.util.Map;

/**
 * Connection pool managing shared physical I/O writters across printer and display peripherals.
 *
 * @author JG uniCenta / KriolOS Team
 * @since 1.0.0
 */
public class PrinterWritterPool {

    private final Map<String, PrinterWritter> m_apool = new HashMap<>();

    private String genUniqueKey(String connector, String port) {
        return connector + "-->" + port;
    }

    public PrinterWritter getDisplayPrinterWritter(ConnectorType connector, String port, int baud) throws TicketPrinterException {
        if (connector == null || connector == ConnectorType.NONE) {
            throw new TicketPrinterException("No valid connector specified for display");
        }
        return getDisplayPrinterWritter(connector.getCode(), port, baud);
    }

    public PrinterWritter getDisplayPrinterWritter(TransportType transport, String port, int baud) throws TicketPrinterException {
        if (transport == null || transport == TransportType.NONE) {
            throw new TicketPrinterException("No valid transport specified for display");
        }
        return getDisplayPrinterWritter(transport.getCode(), port, baud);
    }

    public PrinterWritter getDisplayPrinterWritter(String con, String port, int baud) throws TicketPrinterException {
        String skey = genUniqueKey(con, port);
        PrinterWritter pw = m_apool.get(skey);
        if (pw == null) {
            switch (con) {
                case "serial":
                case "rxtx":
                    pw = new PrinterWritterRXTX(port, baud);
                    m_apool.put(skey, pw);
                    break;
                default:
                    pw = getPrinterWritter(con, port);
                    break;
            }
        }
        return pw;
    }

    public PrinterWritter getPrinterWritter(ConnectorType connector, String port) throws TicketPrinterException {
        if (connector == null || connector == ConnectorType.NONE) {
            throw new TicketPrinterException("No valid connector specified for printer");
        }
        return getPrinterWritter(connector.getCode(), port);
    }

    public PrinterWritter getPrinterWritter(TransportType transport, String port) throws TicketPrinterException {
        if (transport == null || transport == TransportType.NONE) {
            throw new TicketPrinterException("No valid transport specified for printer");
        }
        return getPrinterWritter(transport.getCode(), port);
    }

    public PrinterWritter getPrinterWritter(String con, String port) throws TicketPrinterException {
        String skey = genUniqueKey(con, port);
        PrinterWritter pw = m_apool.get(skey);
        if (pw == null) {
            switch (con) {
                case "serial":
                case "rxtx":
                    pw = new PrinterWritterRXTX(port);
                    m_apool.put(skey, pw);
                    break;
                case "file":
                    pw = new PrinterWritterFile(port);
                    m_apool.put(skey, pw);
                    break;
                case "usb":
                    pw = new PrinterWritterRaw(port);
                    this.m_apool.put(skey, pw);
                    break;
                case "network":
                    String[] str = port != null ? port.split("\\:") : new String[]{""};
                    String hostAddr = str[0];
                    int portAddr = (str.length == 2) ? Integer.parseInt(str[1]) : 9100;
                    if (hostAddr != null && !hostAddr.isBlank()) {
                        pw = new PrinterWritterNetwork(hostAddr, portAddr);
                        this.m_apool.put(skey, pw);
                        return pw;
                    } else {
                        throw new TicketPrinterException("Invalid host addr: " + hostAddr + "; connection string: " + skey);
                    }
                default:
                    throw new TicketPrinterException("Not supported protocol with connection string: " + skey);
            }
        }
        return pw;
    }
}
