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

import gnu.io.*;
import java.io.*;
import java.util.logging.Level;

/**
 *
 * @author Ryan Airey, Feb 2017.
 */
public class ScaleAvery extends AbstractSerialScale implements Scale, SerialPortEventListener {

    private static final int SCALE_READY = 0;
    private static final int SCALE_READING = 1;
    private static final int SCALE_READINGDECIMALS = 2;
    private static int SCALE_NOMORE = 1;

    private double m_dWeightBuffer;
    private double m_dWeightDecimals;
    private int m_iStatusScale;

    public ScaleAvery(String sPortPrinter) {
        super(sPortPrinter);

        m_iStatusScale = SCALE_READY;
        m_dWeightBuffer = 0.0;
        m_dWeightDecimals = 1.0;

    }

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
            m_dWeightDecimals = 1.0;

            write(new byte[]{0x0057});
            flush();
            write(new byte[]{0x000D});
            waitFor(1000);

            if (m_iStatusScale == SCALE_READY) {
                double dWeight = m_dWeightBuffer / m_dWeightDecimals;
                m_dWeightBuffer = 0.0;
                m_dWeightDecimals = 1.0;
                return dWeight;
            } else {
                m_iStatusScale = SCALE_READY;
                m_dWeightBuffer = 0.0;
                m_dWeightDecimals = 1.0;
                return 0.0;
            }
        }
    }

    @Override
    public void serialEvent(SerialPortEvent e) {

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
                    if (b == 0x0003 || b == 3) {
                        synchronized (this) {
                            SCALE_NOMORE = 1;
                            m_iStatusScale = SCALE_READY;
                            notifyAll();
                        }
                    } else if (b == 0x004C || b == 76) {
                        synchronized (this) {
                            SCALE_NOMORE = 0;
                        }
                    } else if (SCALE_NOMORE == 0) {
                        m_iStatusScale = SCALE_READY;
                    } else if (b > 0x002F
                            && b < 0x003A
                            && SCALE_NOMORE == 1
                            || b == 0x002E) {
                        synchronized (this) {
                            if (m_iStatusScale == SCALE_READY) {
                                m_dWeightBuffer = 0.0;
                                m_dWeightDecimals = 1.0;
                                m_iStatusScale = SCALE_READING;
                            }
                            if (b == 0x002E) {
                                m_iStatusScale = SCALE_READINGDECIMALS;
                            } else {
                                m_dWeightBuffer = m_dWeightBuffer * 10.0 + b - 0x0030;
                                if (m_iStatusScale == SCALE_READINGDECIMALS) {
                                    m_dWeightDecimals *= 10.0;
                                }
                            }
                        }
                    } else {
                        m_dWeightBuffer = 0.0;
                        m_dWeightDecimals = 1.0;
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
    protected AbstractSerialScale.SerialPortParams getSerialPortParams() {
        return new AbstractSerialScale.SerialPortParams(9600,
                SerialPort.DATABITS_7,
                SerialPort.STOPBITS_1,
                SerialPort.PARITY_EVEN);
    }

    @Override
    public com.openbravo.pos.spi.hardware.scale.ScaleProtocol getProtocol() {
        return com.openbravo.pos.spi.hardware.scale.ScaleProtocol.AVERY_BERKEL_6720;
    }
}

