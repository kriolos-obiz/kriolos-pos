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
package com.openbravo.pos.scale.javapos;

import com.openbravo.pos.scale.Scale;
import com.openbravo.pos.scale.ScaleException;
import java.util.logging.Level;
import java.util.logging.Logger;
import jpos.JposException;

/**
 *
 * @author poolborges
 */
public class ScaleJavaPOS implements Scale {

    private final static Logger LOGGER = Logger.getLogger(ScaleJavaPOS.class.getName());

    private String m_sName;
    private jpos.Scale m_scale;

    /**
     *
     * @param sDeviceName
     */
    public ScaleJavaPOS(String sDeviceName) {
        this.m_sName = sDeviceName;

        try {
            m_scale = new jpos.Scale();
            m_scale.open(sDeviceName);
            m_scale.claim(10000);
            m_scale.setDeviceEnabled(true);
        } catch (JposException e) {
            LOGGER.log(Level.SEVERE, "Excepion on init POSPrinter: ", e);
            //throw new ScaleException(e.getMessage(), e);
        }
    }

    /**
     * Read Net weight
     * @return net weight
     * @throws ScaleException 
     */
    @Override
    public Double readWeight() throws ScaleException {
        int[] weightData = readAllWeights();

        // [0] - net weight
        // [1] - gross weight
        // [2] - tare weight
        Double result = (1.0 * weightData[0]) / 1000.0;
        return result;
    }

    /**
     * Read all weights (Net, Gross, Tare)
     * 
     * @return  array[3] of weight
     * [0] - net weight
     * [1] - gross weight
     * [2] - tare weight
     * @throws ScaleException 
     */
    public int[] readAllWeights() throws ScaleException {
        int[] weightData = new int[3];

        try {
            m_scale.readWeight(weightData, 2000);
        } catch (JposException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            throw new ScaleException(ex.getMessage(), ex);
        }

        return weightData;
    }

    /**
     * Get human redable Weight unit (e.g: pound, kg, oz)
     * @return
     * @throws ScaleException 
     */
    public String getWeightUnit() throws ScaleException {
        String result = "";

        int unitType = -1;
        try {
            unitType = m_scale.getWeightUnit();
        } catch (JposException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            throw new ScaleException(ex.getMessage(), ex);
        }

        /*
            1 pound is equal to 0.45359237 kilograms in the International System of Units (SI)
            The pound is abbreviated as "lb"
         */
        switch (unitType) {
            case 0:
                result = "pound 1/8";
                break;
            case 1:
                result = "pound 1/10";
                break;
            case 2:
                result = "pound 1/500";
                break;
            case 3:
                result = "kg";
                break;
            case 4:
                result = "pound";
                break;
            case 5:
                result = "oz";
                break;
        }
        LOGGER.log(Level.FINE, " WeightUnit: {}", result);
        return result;

    }

    @Override
    public String getDeviceId() {
        return m_sName;
    }

    @Override
    public boolean isConnected() {
        return m_scale != null && m_scale.getState() != jpos.JposConst.JPOS_S_CLOSED;
    }

    @Override
    public void close() {
        if (m_scale != null) {
            try {
                m_scale.setDeviceEnabled(false);
                m_scale.release();
                m_scale.close();
            } catch (JposException e) {
                LOGGER.log(Level.WARNING, "Error closing JavaPOS scale", e);
            }
        }
    }

    @Override
    public com.openbravo.pos.spi.hardware.scale.ScaleProtocol getProtocol() {
        return com.openbravo.pos.spi.hardware.scale.ScaleProtocol.JAVAPOS;
    }
}
