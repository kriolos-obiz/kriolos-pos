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
 * @author uniCenta + H Singh
 */
public class ScaleAcomPC100 extends AbstractSerialScale implements Scale, SerialPortEventListener {

    private static final int SCALE_READY = 0;
    private static final int SCALE_READING = 1;
    private static final int SCALE_READINGDECIMALS = 2;

    private double m_dWeightBuffer;
    private double m_dWeightDecimals;
    private int m_iStatusScale;
    private String m_sScaleReading;

    /**
     * Creates a new instance of ScaleComm
     *
     * @param sPortPrinter
     */
    public ScaleAcomPC100(String sPortPrinter) {
        super(sPortPrinter);
        m_iStatusScale = SCALE_READY;
        m_dWeightBuffer = 0.0;
        m_dWeightDecimals = 1.0;
    }

    /**
     *
     * @return
     */
    @Override
    public Double readWeight() throws ScaleException {

        synchronized (this) {

            if (m_iStatusScale != SCALE_READY) {
                waitFor(200);
            }

            m_dWeightBuffer = 0.0;
            m_dWeightDecimals = 1.0;
            write(new byte[]{0x57, 0X0D}); // $
            flush();

            waitFor(200);

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

    /**
     *
     * @param e
     */
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
                int i;
                i = m_in.available();
                byte[] readBuffer = new byte[i];
                if (i > 0) {
                    m_in.read(readBuffer);
                }

                m_sScaleReading = m_sScaleReading + new String(readBuffer);

                int start = m_sScaleReading.indexOf((char) 10);
                int end = m_sScaleReading.indexOf((char) 3);

                if (start >= 0 && end >= 0) {

                    start = m_sScaleReading.indexOf((char) 10);
                    end = m_sScaleReading.indexOf((char) 75);
                    m_dWeightBuffer = Double.parseDouble(m_sScaleReading.substring(start + 1, end));
                    m_sScaleReading = "";
                }

            } catch (IOException | IndexOutOfBoundsException ex) {
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
        return com.openbravo.pos.spi.hardware.scale.ScaleProtocol.ACOM_PC100;
    }
}
