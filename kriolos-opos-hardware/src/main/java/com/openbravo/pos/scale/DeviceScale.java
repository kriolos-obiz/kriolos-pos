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

import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.AppProperties;
import com.openbravo.pos.spi.hardware.PeripheralManager;
import com.openbravo.pos.spi.hardware.scale.ScaleConfig;
import com.openbravo.pos.spi.hardware.scale.ScaleDevice;
import com.openbravo.pos.spi.hardware.scale.ScaleProtocol;
import java.awt.Component;

/**
 * Legacy facade maintaining backward-compatibility while delegating peripheral
 * resolution to the unified {@link PeripheralManager} SPI.
 *
 * @author JG uniCenta / KriolOS Team
 */
public class DeviceScale {
    
    private Scale m_scale;
    
    /**
     * 
     * @param parent
     * @param props 
     */
    public DeviceScale(Component parent, AppProperties props) {
        String raw = props != null ? props.getProperty("machine.scale") : null;
        if (raw == null || raw.isBlank()) {
            m_scale = null;
            return;
        }
        ScaleConfig config = ScaleConfig.parse(raw);
        if (config.protocol() == ScaleProtocol.SCREEN) {
            m_scale = new ScaleDialog(parent);
        } else {
            ScaleDevice device = PeripheralManager.getScale(config);
            if (device instanceof Scale) {
                m_scale = (Scale) device;
            } else if (device != null && device.isConnected() && device.getProtocol() != ScaleProtocol.NONE) {
                m_scale = new Scale() {
                    @Override
                    public Double readWeight() throws ScaleException {
                        try {
                            return device.readWeight();
                        } catch (com.openbravo.pos.spi.hardware.scale.ScaleException e) {
                            throw new ScaleException(e.getMessage(), e);
                        }
                    }

                    @Override
                    public ScaleProtocol getProtocol() {
                        return device.getProtocol();
                    }

                    @Override
                    public boolean isConnected() {
                        return device.isConnected();
                    }
                };
            } else {
                m_scale = null;
            }
        }
    }

    /**
     * Returns the underlying SPI scale device contract.
     *
     * @return {@link ScaleDevice} instance or null.
     */
    public ScaleDevice getDevice() {
        return m_scale;
    }
    
    /**
     *
     * @return
     */
    public boolean existsScale() {
        return m_scale != null;
    }
    
    /**
     *
     * @return
     * @throws ScaleException
     */
    public Double readWeight() throws ScaleException {
        
        if (m_scale == null) {
            throw new ScaleException(AppLocal.getIntString("scale.notdefined"));
        } else {
            Double result = m_scale.readWeight();
            if (result == null) {
                return null; // Canceled by the user / scale
            } else if (result < 0.002) {
                // invalid result. nothing on the scale
                throw new ScaleException(AppLocal.getIntString("scale.invalidvalue"));                
            } else {
                // valid result
                return result;
            }
        }
    }    
}
