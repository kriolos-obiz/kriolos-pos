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
package com.openbravo.pos.scanpal2;

import com.openbravo.pos.forms.AppProperties;
import com.openbravo.pos.spi.hardware.PeripheralManager;
import com.openbravo.pos.spi.hardware.scanner.ScannerConfig;
import com.openbravo.pos.spi.hardware.scanner.ScannerDevice;
import com.openbravo.pos.spi.hardware.scanner.ScannerProtocol;

/**
 * Legacy scanner factory delegating to {@link PeripheralManager}.
 *
 * @author JG uniCenta / KriolOS Team
 */
public class DeviceScannerFactory {
    
    private DeviceScannerFactory() {
    }
    
    /**
     * Creates or resolves scanner instance from application properties.
     *
     * @param props Application configuration properties.
     * @return {@link DeviceScanner} or null if disabled.
     */
    public static DeviceScanner createInstance(AppProperties props) {
        String raw = props != null ? props.getProperty("machine.scanner") : null;
        if (raw == null || raw.isBlank()) {
            return null;
        }
        ScannerConfig config = ScannerConfig.parse(raw);
        if (config.protocol() == ScannerProtocol.NONE) {
            return null;
        }
        ScannerDevice device = PeripheralManager.getScanner(config);
        if (device instanceof DeviceScanner) {
            return (DeviceScanner) device;
        }
        return null;
    }  
}
