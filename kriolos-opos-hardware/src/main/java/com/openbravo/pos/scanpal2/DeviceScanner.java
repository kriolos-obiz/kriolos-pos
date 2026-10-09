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

import com.openbravo.pos.spi.hardware.scanner.ScannerDevice;
import com.openbravo.pos.spi.hardware.scanner.ScannerProtocol;

/**
 *
 * @author JG uniCenta
 */
public interface DeviceScanner extends ScannerDevice {

    @Override
    default ScannerProtocol getProtocol() {
        return ScannerProtocol.SCANPAL2;
    }

    @Override
    default boolean isConnected() {
        return true;
    }

    @Override
    default void start() {
        try {
            connectDevice();
        } catch (DeviceScannerException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    default void stop() {
        disconnectDevice();
    }


    /**
     *
     * @throws DeviceScannerException
     */
    public void connectDevice() throws DeviceScannerException;

    /**
     *
     */
    public void disconnectDevice();

    /**
     *
     * @throws DeviceScannerException
     */
    public void startDownloadProduct() throws DeviceScannerException;

    /**
     *
     * @return
     * @throws DeviceScannerException
     */
    public ProductDownloaded recieveProduct() throws DeviceScannerException;
    
    /**
     *
     * @throws DeviceScannerException
     */
    public void startUploadProduct() throws DeviceScannerException;

    /**
     *
     * @param sName
     * @param sCode
     * @param dPrice
     * @throws DeviceScannerException
     */
    public void sendProduct(String sName, String sCode, Double dPrice) throws DeviceScannerException;

    /**
     *
     * @throws DeviceScannerException
     */
    public void stopUploadProduct() throws DeviceScannerException;    
}
