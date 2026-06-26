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

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Writer implementation for sending raw ESC/POS commands over TCP/IP network sockets.
 * Designed with thread safety and automatic network connection recovery.
 */
public class PrinterWritterNetwork extends PrinterWritter {

    private static final Logger LOGGER = Logger.getLogger(PrinterWritterNetwork.class.getName());
    
    private final String hostAddress;
    private final int m_iPort;
    private Socket clientSocket;
    private OutputStream outStream;
    
    // Dedicated lock object to ensure network states remain completely atomic
    private final Object lock = new Object();
    
    // Maximum connection timeout in milliseconds (3 seconds) to prevent infinite queue blocking
    private static final int CONNECT_TIMEOUT_MS = 3000;

    /**
     * Initializes the network printer writer settings.
     * 
     * @param hostAddress Target IP address of the network printer.
     * @param hostPort    Target network port number (typically 9100 for RAW printing).
     */
    public PrinterWritterNetwork(String hostAddress, int hostPort) {
        this.hostAddress = hostAddress;
        this.m_iPort = hostPort;
        this.clientSocket = null;
        this.outStream = null;
    }

    /**
     * Connects to the printer socket if offline and transmits raw byte payloads.
     */
    @Override
    protected void internalWrite(byte[] data) {
        if (data == null || data.length == 0) {
            return;
        }

        synchronized (lock) {
            try {
                if (this.outStream == null || this.clientSocket == null || this.clientSocket.isClosed()) {
                    LOGGER.log(Level.INFO, "Connecting to printer network host: {0}, port: {1}", new Object[]{this.hostAddress, this.m_iPort});
                    
                    this.clientSocket = new Socket();
                    // Connects using a strict timeout instead of infinite system block
                    this.clientSocket.connect(new InetSocketAddress(this.hostAddress, this.m_iPort), CONNECT_TIMEOUT_MS);
                    this.outStream = new DataOutputStream(this.clientSocket.getOutputStream());
                }
                this.outStream.write(data);
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Network write exception occurred at host: " + this.hostAddress + ", port: " + this.m_iPort, e);
                // Hard reset connection states so the next execution can try a fresh reconnect
                forceDisconnect();
            }
        }
    }

    /**
     * Flushes buffers and dispatches data down the network pipe.
     */
    @Override
    protected void internalFlush() {
        synchronized (lock) {
            try {
                if (this.outStream != null) {
                    this.outStream.flush();
                }
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Network flush exception occurred at host: " + this.hostAddress + ", port: " + this.m_iPort, e);
            } finally {
                forceDisconnect();
            }
        }
    }

    /**
     * Gracefully closes network channels and clears system socket descriptors.
     */
    @Override
    protected void internalClose() {
        synchronized (lock) {
            if (this.clientSocket != null && !this.clientSocket.isClosed()) {
                LOGGER.log(Level.INFO, "Closing printer network socket connection to host: {0}, port: {1}", 
                        new Object[]{this.clientSocket.getInetAddress().getHostAddress(), this.clientSocket.getPort()});
            }
            forceDisconnect();
        }
    }

    /**
     * Unconditionally closes active socket handles and releases memory states.
     * Must be invoked safely within a synchronized block.
     */
    private void forceDisconnect() {
        if (this.outStream != null) {
            try {
                this.outStream.close();
            } catch (IOException e) {
                LOGGER.log(Level.FINEST, "Failed to close network output stream", e);
            } finally {
                this.outStream = null;
            }
        }
        
        if (this.clientSocket != null) {
            try {
                this.clientSocket.close();
            } catch (IOException e) {
                LOGGER.log(Level.FINEST, "Failed to close network client socket", e);
            } finally {
                this.clientSocket = null;
            }
        }
    }
}
