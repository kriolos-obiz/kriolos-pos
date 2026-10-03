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
package com.openbravo.pos.scanpal2;

import com.openbravo.pos.forms.AppProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Characterization test suite for Barcode Scanner protocol factory.
 * Locks down existing DeviceScannerFactory behavior.
 */
public class DeviceScannerRegressionTest {

    @Test
    @DisplayName("DeviceScannerFactory should create DeviceScannerComm when configured with 'scanpal2'")
    void testCreateScanpal2Scanner() {
        AppProperties props = createMockProperties("machine.scanner", "scanpal2:/dev/ttyS0,9600");
        DeviceScanner scanner = DeviceScannerFactory.createInstance(props);

        assertNotNull(scanner, "Scanner must be instantiated for scanpal2");
        assertInstanceOf(DeviceScannerComm.class, scanner, "Scanner must be an instance of DeviceScannerComm");
    }

    @Test
    @DisplayName("DeviceScannerFactory should return null when scanner type is unknown or unconfigured")
    void testUnknownOrNullScanner() {
        AppProperties propsUnknown = createMockProperties("machine.scanner", "unknown_device:/dev/ttyS0");
        assertNull(DeviceScannerFactory.createInstance(propsUnknown), "Unknown scanner type should return null");

        AppProperties propsEmpty = createMockProperties("machine.scanner", "");
        assertNull(DeviceScannerFactory.createInstance(propsEmpty), "Empty scanner config should return null");

        AppProperties propsNull = createMockProperties("machine.scanner", null);
        assertNull(DeviceScannerFactory.createInstance(propsNull), "Null scanner config should return null");
    }

    private static AppProperties createMockProperties(String key, String value) {
        Properties p = new Properties();
        if (value != null) {
            p.setProperty(key, value);
        }
        return new AppProperties() {
            @Override
            public String getProperty(String sKey) {
                return p.getProperty(sKey);
            }

            @Override
            public String getProperty(String sKey, String defaultValue) {
                return p.getProperty(sKey, defaultValue);
            }

            @Override
            public String getHost() {
                return "localhost";
            }

            @Override
            public File getConfigFile() {
                return null;
            }

            @Override
            public List<DatabaseConfig> getAll() {
                return Collections.emptyList();
            }

            @Override
            public DatabaseConfig getPrimary() {
                return null;
            }
        };
    }
}
