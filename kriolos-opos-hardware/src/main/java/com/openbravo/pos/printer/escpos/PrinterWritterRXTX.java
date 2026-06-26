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
package com.openbravo.pos.printer.escpos;

import com.openbravo.pos.printer.TicketPrinterException;
import gnu.io.*;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Writer implementation for driving legacy receipt printers over serial ports
 * using the RXTX (gnu.io) native communications library.
 * 
 * @author JG uniCenta
 */
public class PrinterWritterRXTX extends PrinterWritter {

    private static final Logger LOGGER = Logger.getLogger(PrinterWritterRXTX.class.getName());
    private static final int DEFAULT_BAUD_RATE = 9600;

    private CommPort commPort;
    private OutputStream outstream;

    private final String serialPortName;
    private final int serialBaudRate;
    private final int serialDataBits;
    private final int serialStopBits;
    private final int serialParity;
    
    // Dedicated lock object to isolate serial hardware mutations across asynchronous threads
    private final Object lock = new Object();

    public PrinterWritterRXTX(String serialPortName) throws TicketPrinterException {
        this(serialPortName, DEFAULT_BAUD_RATE);
    }

    public PrinterWritterRXTX(String serialPortName, int serialBaudRate) throws TicketPrinterException {
        this(serialPortName, serialBaudRate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
    }

    public PrinterWritterRXTX(String serialPortName, int serialBaudRate, int serialDataBits, int serialStopBits, int serialParity) throws TicketPrinterException {
        this.serialPortName = serialPortName;
        this.serialBaudRate = serialBaudRate;
        this.serialDataBits = serialDataBits;
        this.serialStopBits = serialStopBits;
        this.serialParity = serialParity;
        this.outstream = null;
    }

    /**
     * Initializes the serial interface, configures hardware parameters, and sends raw byte sequences.
     */
    @Override
    protected void internalWrite(byte[] data) {
        if (data == null || data.length == 0) {
            return;
        }

        synchronized (lock) {
            try {
                if (outstream == null) {
                    LOGGER.log(Level.INFO, "Opening serial port - Name: {0} | BaudRate: {1} | DataBits: {2} | StopBits: {3} | Parity: {4}",
                            new Object[]{serialPortName, serialBaudRate, serialDataBits, serialStopBits, serialParity});
                    
                    CommPortIdentifier commPortIdentifier = CommPortIdentifier.getPortIdentifier(serialPortName);
                    
                    // Attempts to open the port claiming exclusive app ownership. Waits up to 2 seconds if busy.
                    commPort = commPortIdentifier.open("PrinterWritterRXTX", 2000);

                    if (commPort instanceof SerialPort serialPort) {
                        serialPort.setSerialPortParams(serialBaudRate, serialDataBits, serialStopBits, serialParity);

                        // Fixes hardware freezing issues on legacy Epson TM-U220 matrix printers.
                        // Automatically regulates hardware flow control using native RTS/CTS lines.
                        serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN);
                    }
                    
                    outstream = commPort.getOutputStream();
                }
                outstream.write(data);
            } catch (NoSuchPortException | PortInUseException | UnsupportedCommOperationException | IOException e) {
                LOGGER.log(Level.SEVERE, "Hardware exception occurred while writing to serial port: " + serialPortName, e);
                // Hard reset states to prevent permanent port locks in the OS kernel
                forceDisconnect();
            }
        }
    }

    /**
     * Flushes buffered downstream streams into the native serial hardware pipeline.
     */
    @Override
    protected void internalFlush() {
        synchronized (lock) {
            try {
                if (outstream != null) {
                    outstream.flush();
                }
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Exception occurred while flushing serial stream: " + serialPortName, e);
                forceDisconnect();
            }
        }
    }

    /**
     * Flushes variables, shuts down streams, and explicitly releases operating system COM locks.
     */
    @Override
    protected void internalClose() {
        synchronized (lock) {
            forceDisconnect();
        }
    }

    /**
     * Unconditionally teardown connection handles, flushes structures, and releases native system resources.
     * Must be invoked safely within a synchronized block.
     */
    private void forceDisconnect() {
        if (outstream != null) {
            try {
                outstream.flush();
                outstream.close();
            } catch (IOException e) {
                LOGGER.log(Level.FINEST, "Failed to close serial output stream", e);
            } finally {
                outstream = null;
            }
        }
        
        if (commPort != null) {
            try {
                commPort.close(); // Crucial to allow other software to reuse the COM port
            } catch (Exception e) {
                LOGGER.log(Level.FINEST, "Failed to release serial hardware port handle", e);
            } finally {
                commPort = null;
            }
        }
    }
}
