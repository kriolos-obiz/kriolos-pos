/*
 * Copyright (C) 2023 Paulo Borges
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
package com.openbravo.pos.scale;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.TooManyListenersException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author poolb
 */
public abstract class AbstractSerialScale implements Scale, SerialPortEventListener {

    protected final static Logger LOGGER = Logger.getLogger(AbstractSerialScale.class.getName());

    private CommPortIdentifier m_PortIdPrinter;
    private SerialPort m_CommPortPrinter;

    private final String m_sPortScale;
    protected OutputStream m_out;
    protected InputStream m_in;

    public AbstractSerialScale(String sPortPrinter) {
        m_sPortScale = sPortPrinter;
        m_out = null;
        m_in = null;
    }

    private void open() throws ScaleException {

        try {
            m_PortIdPrinter = CommPortIdentifier.getPortIdentifier(m_sPortScale); // Get serial port ID
            m_CommPortPrinter = (SerialPort) m_PortIdPrinter.open("PORTID", 2000); // Open Serial

            m_out = m_CommPortPrinter.getOutputStream();
            m_in = m_CommPortPrinter.getInputStream();

            m_CommPortPrinter.addEventListener(this);
            m_CommPortPrinter.notifyOnDataAvailable(true);

            SerialPortParams serialPortParams = getSerialPortParams();

            m_CommPortPrinter.setSerialPortParams(serialPortParams.getBaudRate(),
                    serialPortParams.getDataBits(),
                    serialPortParams.getStopBits(),
                    serialPortParams.getParity()); // Configuramos el puerto
        } catch (NoSuchPortException | TooManyListenersException | UnsupportedCommOperationException | PortInUseException | IOException ex) {
            LOGGER.log(Level.SEVERE, "Exception open serial port: " + this.m_sPortScale, ex);
            throw new ScaleException("Exception open serial port: " + this.m_sPortScale, ex);
        }
    }

    @Override
    public String getDeviceId() {
        return m_sPortScale;
    }

    @Override
    public boolean isConnected() {
        return m_CommPortPrinter != null;
    }

    @Override
    public void close() {
        if (m_in != null) {
            try {
                m_in.close();
            } catch (IOException ex) {
                //
            }
            m_in = null;
        }

        if (m_out != null) {
            try {
                m_out.close();
            } catch (IOException ex) {
                //
            }
            m_out = null;
        }

        if (m_CommPortPrinter != null) {
            m_CommPortPrinter.close();
            m_CommPortPrinter = null;
        }

        if (m_PortIdPrinter != null) {
            m_PortIdPrinter = null;
        }
    }

    protected void requireConnection() throws ScaleException {
        if (m_in == null) {
            close();
            open();
        }
    }

    protected void flush() {
        try {
            if (this.m_out != null) {
                this.m_out.flush();
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Exception while flush: ", e);
        }
    }

    protected String getSerialPort() {
        return this.m_sPortScale;
    }

    protected void write(byte[] data) throws ScaleException {
        try {
            requireConnection();
            m_out.write(data);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, null, e);
            throw new ScaleException("Cannot write to: " + this.m_sPortScale, e);
        }
    }

    protected void waitFor(int timeToWait) {
        try {
            wait(timeToWait);
        } catch (InterruptedException ex) {
            LOGGER.log(Level.SEVERE, "Exception calling wait: ", ex);
        }
    }

    @Override
    public abstract Double readWeight() throws ScaleException;

    @Override
    public abstract void serialEvent(SerialPortEvent ev);

    protected abstract SerialPortParams getSerialPortParams();

    public static class SerialPortParams {

        private int baudRate;
        private int dataBits;
        private int stopBits;
        private int parity;

        public SerialPortParams(int baudRate, int dataBits, int stopBits, int parity) {
            this.baudRate = baudRate;
            this.dataBits = dataBits;
            this.stopBits = stopBits;
            this.parity = parity;
        }

        public int getBaudRate() {
            return baudRate;
        }

        public void setBaudRate(int baudRate) {
            this.baudRate = baudRate;
        }

        public int getDataBits() {
            return dataBits;
        }

        public void setDataBits(int dataBits) {
            this.dataBits = dataBits;
        }

        public int getStopBits() {
            return stopBits;
        }

        public void setStopBits(int stopBits) {
            this.stopBits = stopBits;
        }

        public int getParity() {
            return parity;
        }

        public void setParity(int parity) {
            this.parity = parity;
        }

    }

}
