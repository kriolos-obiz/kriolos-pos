/*
 * Copyright (C) 2022-2026 KriolOS
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
package com.openbravo.pos.hardware;

import com.openbravo.data.user.BrowsableEditableData;
import com.openbravo.pos.forms.AppProperties;
import com.openbravo.pos.printer.DefaultDeviceTicket;
import com.openbravo.pos.printer.DeviceDisplay;
import com.openbravo.pos.printer.DeviceTicket;
import com.openbravo.pos.printer.screen.DeviceDisplayAdvance;
import com.openbravo.pos.printer.screen.DeviceDisplayPanel;
import com.openbravo.pos.scale.DeviceScale;
import com.openbravo.pos.scanpal2.DeviceScanner;
import com.openbravo.pos.scanpal2.DeviceScannerFactory;
import com.openbravo.pos.spi.hardware.scale.ScaleDevice;
import com.openbravo.pos.spi.hardware.scanner.ScannerDevice;

import java.awt.Component;
import java.awt.image.BufferedImage;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.ListModel;

/**
 * Single gateway and facade in {@code kriolos-opos-app} for all direct interactions with
 * concrete hardware drivers and classes from {@code kriolos-opos-hardware}.
 * <p>
 * This is the ONLY class in {@code kriolos-opos-app} permitted to import
 * classes from {@code kriolos-opos-hardware}.
 *
 * @author KriolOS Team
 */
public final class PosHardwareManager {
    
    private static volatile ScannerDevice scanner;

    private PosHardwareManager() {
    }

    // --- Peripheral Instantiation ---
    
    public static void setScannerDevice(ScannerDevice scannerConfigured){
        scanner = scannerConfigured;
    }

    public static DeviceTicket createDeviceTicket(Component parent, AppProperties props) {
        return new DefaultDeviceTicket(parent, props);
    }

    public static DeviceTicket createPreviewTicketDevice() {
        return new DefaultDeviceTicket();
    }

    public static ScaleDevice createDeviceScale(Component parent, AppProperties props) {
        return new DeviceScale(parent, props);
    }

    public static ScannerDevice createDeviceScanner(AppProperties props) {
        return DeviceScannerFactory.createInstance(props);
    }

    // --- Display & Screen Peripheral Operations ---

    public static JComponent getDisplayComponent(DeviceDisplay display) {
        if (display instanceof DeviceDisplayPanel panel) {
            return panel;
        }
        return null;
    }

    public static boolean isAdvanceDisplay(DeviceDisplay display) {
        return display instanceof DeviceDisplayAdvance;
    }

    public static boolean hasFeature(DeviceDisplay display, int feature) {
        return display instanceof DeviceDisplayAdvance adv && adv.hasFeature(feature);
    }

    public static boolean setProductImage(DeviceDisplay display, BufferedImage image) {
        if (display instanceof DeviceDisplayAdvance adv) {
            return adv.setProductImage(image);
        }
        return false;
    }

    public static boolean setTicketLines(DeviceDisplay display, JPanel panel) {
        if (display instanceof DeviceDisplayAdvance adv) {
            return adv.setTicketLines(panel);
        }
        return false;
    }

    // --- Handheld Scanner Operations ---

    public static void uploadProducts(BrowsableEditableData bd) throws Exception {
        
        if (!(scanner instanceof DeviceScanner deviceScanner)) {
            throw new IllegalStateException("Configured scanner does not support ScanPal2 product upload.");
        }
        try {
            deviceScanner.connectDevice();
            deviceScanner.startUploadProduct();

            ListModel l = bd.getListModel();
            for (int i = 0; i < l.getSize(); i++) {
                Object[] myprod = (Object[]) l.getElementAt(i);
                deviceScanner.sendProduct(
                        (String) myprod[3],
                        (String) myprod[2],
                        (Double) myprod[6]
                );
            }
            deviceScanner.stopUploadProduct();
        } finally {
            deviceScanner.disconnectDevice();
        }
    }
}
