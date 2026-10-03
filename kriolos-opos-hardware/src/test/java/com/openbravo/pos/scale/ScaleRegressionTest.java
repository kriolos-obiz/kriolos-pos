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
package com.openbravo.pos.scale;

import com.openbravo.pos.forms.AppProperties;
import gnu.io.SerialPortEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Characterization and regression test suite for existing Scale protocol implementations.
 * Ensures future SPI refactoring preserves exact byte decoding, parsing, and status transitions.
 */
public class ScaleRegressionTest {

    @Test
    @DisplayName("ScaleFake should generate randomized positive weight readings")
    void testScaleFakeReturnsPositiveWeight() throws ScaleException {
        ScaleFake fake = new ScaleFake();
        Double weight = fake.readWeight();
        assertNotNull(weight, "Weight reading must not be null");
        assertTrue(weight >= 0.0 && weight <= 2.0, "Weight reading must fall within [0.0, 2.0]");
    }

    @Test
    @DisplayName("DeviceScale configured with 'fake' should exist and read weight")
    void testDeviceScaleFake() throws ScaleException {
        AppProperties props = createMockProperties("machine.scale", "fake");
        DeviceScale deviceScale = new DeviceScale(null, props);

        assertTrue(deviceScale.existsScale(), "DeviceScale should exist when configured with 'fake'");
        Double weight = deviceScale.readWeight();
        assertNotNull(weight);
        assertTrue(weight >= 0.0);
    }

    @Test
    @DisplayName("DeviceScale with unconfigured scale should not exist and throw ScaleException")
    void testDeviceScaleUndefined() {
        AppProperties props = createMockProperties("machine.scale", "");
        DeviceScale deviceScale = new DeviceScale(null, props);

        assertFalse(deviceScale.existsScale(), "DeviceScale should not exist for empty configuration");
        assertThrows(ScaleException.class, deviceScale::readWeight,
                "readWeight() must throw ScaleException when scale is not defined");
    }

    @Test
    @DisplayName("DeviceScale with unknown scale type should not exist and throw ScaleException")
    void testDeviceScaleUnknownType() {
        AppProperties props = createMockProperties("machine.scale", "nonexistent_scale:/dev/ttyS0");
        DeviceScale deviceScale = new DeviceScale(null, props);

        assertFalse(deviceScale.existsScale(), "DeviceScale should not exist for unknown scale type");
        assertThrows(ScaleException.class, deviceScale::readWeight);
    }

    @Test
    @DisplayName("ScaleCASPDII should decode standard CAS PD-II weight byte frames")
    void testScaleCASPDIIByteDecoding() throws Exception {
        // CAS PD-II sends weight as integer grams followed by CR (0x0D), e.g. "1450\r" = 1.450 kg
        ScaleCASPDII scale = new ScaleCASPDII("TEST_PORT");

        byte[] frame = "1450\r".getBytes();
        ByteArrayInputStream in = new ByteArrayInputStream(frame);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        Field inField = AbstractSerialScale.class.getDeclaredField("m_in");
        inField.setAccessible(true);
        inField.set(scale, in);

        Field outField = AbstractSerialScale.class.getDeclaredField("m_out");
        outField.setAccessible(true);
        outField.set(scale, out);

        SerialPortEvent event = createDataAvailableEvent();
        scale.serialEvent(event);

        Field statusField = ScaleCASPDII.class.getDeclaredField("m_iStatusScale");
        statusField.setAccessible(true);
        int status = (int) statusField.get(scale);
        assertEquals(0, status, "Scale state should be SCALE_READY (0) after CR terminator");

        Field bufferField = ScaleCASPDII.class.getDeclaredField("m_dWeightBuffer");
        bufferField.setAccessible(true);
        double buffer = (double) bufferField.get(scale);

        Field decimalsField = ScaleCASPDII.class.getDeclaredField("m_dWeightDecimals");
        decimalsField.setAccessible(true);
        double decimals = (double) decimalsField.get(scale);

        double parsedWeight = buffer / decimals;
        assertEquals(1.450, parsedWeight, 0.0001, "Parsed weight must match 1.450 kg");
    }

    @Test
    @DisplayName("ScaleAcomPC100 should decode standard Acom PC-100 weight byte frames")
    void testScaleAcomPC100ByteDecoding() throws Exception {
        ScaleAcomPC100 scale = new ScaleAcomPC100("TEST_PORT");

        // Acom PC-100 protocol frame: starts with LF (10), followed by weight, 'K' (75), and ETX (3)
        byte[] frame = "\n2.350K\u0003".getBytes();
        ByteArrayInputStream in = new ByteArrayInputStream(frame);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        Field inField = AbstractSerialScale.class.getDeclaredField("m_in");
        inField.setAccessible(true);
        inField.set(scale, in);

        Field outField = AbstractSerialScale.class.getDeclaredField("m_out");
        outField.setAccessible(true);
        outField.set(scale, out);

        SerialPortEvent event = createDataAvailableEvent();
        scale.serialEvent(event);

        Field bufferField = ScaleAcomPC100.class.getDeclaredField("m_dWeightBuffer");
        bufferField.setAccessible(true);
        double buffer = (double) bufferField.get(scale);

        assertEquals(2.350, buffer, 0.0001, "Parsed weight buffer must match 2.350 kg");
    }

    private static SerialPortEvent createDataAvailableEvent() throws Exception {
        var constructor = SerialPortEvent.class.getDeclaredConstructor(
                gnu.io.SerialPort.class, int.class, boolean.class, boolean.class);
        constructor.setAccessible(true);
        gnu.io.SerialPort mockPort = org.mockito.Mockito.mock(gnu.io.SerialPort.class);
        return constructor.newInstance(mockPort, SerialPortEvent.DATA_AVAILABLE, false, true);
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
            public java.io.File getConfigFile() {
                return null;
            }

            @Override
            public java.util.List<DatabaseConfig> getAll() {
                return java.util.Collections.emptyList();
            }

            @Override
            public DatabaseConfig getPrimary() {
                return null;
            }
        };
    }
}
