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
package com.openbravo.pos.scale;

import static com.openbravo.pos.scale.AbstractSerialScale.LOGGER;
import gnu.io.*;
import java.io.*;
import java.util.logging.Level;

/**
 *
 * @author JG uniCenta
 */
public class ScaleComm extends AbstractSerialScale implements Scale, SerialPortEventListener {

    private static final int SCALE_READY = 0;
    private static final int SCALE_READING = 1;

    private double m_dWeightBuffer;
    private int m_iStatusScale;

    public ScaleComm(String sPortPrinter) {
        super(sPortPrinter);

        m_iStatusScale = SCALE_READY;
        m_dWeightBuffer = 0.0;
    }

    /**
     *
     * @return
     */
    @Override
    public Double readWeight() throws ScaleException {

        synchronized (this) {

            if (m_iStatusScale != SCALE_READY) {
                waitFor(1000);
                if (m_iStatusScale != SCALE_READY) {
                    m_iStatusScale = SCALE_READY;
                }
            }

            m_dWeightBuffer = 0.0;
            write(new byte[]{0x05});
            flush();

            waitFor(1000);

            if (m_iStatusScale == SCALE_READY) {
                double dWeight = m_dWeightBuffer / 1000.0;
                m_dWeightBuffer = 0.0;
                return dWeight;
            } else {
                m_iStatusScale = SCALE_READY;
                m_dWeightBuffer = 0.0;
                return 0.0;
            }
        }
    }

    /**
     *
     * @param e
     */
    @Override
    public void serialEvent(SerialPortEvent e) {

        // Determine type of event.
        switch (e.getEventType()) {
            case SerialPortEvent.BI:
            case SerialPortEvent.OE:
            case SerialPortEvent.FE:
            case SerialPortEvent.PE:
            case SerialPortEvent.CD:
            case SerialPortEvent.CTS:
            case SerialPortEvent.DSR:
            case SerialPortEvent.RI:
            case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
                break;
            case SerialPortEvent.DATA_AVAILABLE:
                try {
                while (m_in.available() > 0) {
                    int b = m_in.read();

                    if (b == 0x001E) { // RS ASCII
                        synchronized (this) {
                            m_iStatusScale = SCALE_READY;
                            notifyAll();
                        }
                    } else if (b > 0x002F && b < 0x003A) {
                        synchronized (this) {
                            if (m_iStatusScale == SCALE_READY) {
                                m_dWeightBuffer = 0.0;
                                m_iStatusScale = SCALE_READING;
                            }
                            m_dWeightBuffer = m_dWeightBuffer * 10.0 + b - 0x0030;
                        }
                    } else {
                        m_dWeightBuffer = 0.0;
                        m_iStatusScale = SCALE_READY;
                    }
                }

            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, "Exception on serialEvent", ex);
            }
            break;
        }

    }

    @Override
    protected SerialPortParams getSerialPortParams() {
        return new AbstractSerialScale.SerialPortParams(4800,
                SerialPort.DATABITS_8,
                SerialPort.STOPBITS_1,
                SerialPort.PARITY_ODD);
    }

    @Override
    public com.openbravo.pos.spi.hardware.scale.ScaleProtocol getProtocol() {
        return com.openbravo.pos.spi.hardware.scale.ScaleProtocol.DIALOG_1;
    }
}

