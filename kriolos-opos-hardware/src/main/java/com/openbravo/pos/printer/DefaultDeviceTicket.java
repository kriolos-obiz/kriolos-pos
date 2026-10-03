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
package com.openbravo.pos.printer;

import com.openbravo.pos.forms.AppProperties;
import com.openbravo.pos.printer.escpos.*;
import com.openbravo.pos.printer.javapos.DevicePrinterJavaPOS;
import com.openbravo.pos.printer.printer.DevicePrinterPrinter;
import com.openbravo.pos.printer.screen.DevicePrinterPanel;
import com.openbravo.pos.spi.hardware.PeripheralManager;
import com.openbravo.pos.spi.hardware.display.DisplayConfig;
import com.openbravo.pos.spi.hardware.display.DisplayDevice;
import com.openbravo.pos.spi.hardware.printer.FiscalPrinterConfig;
import com.openbravo.pos.spi.hardware.printer.FiscalPrinterDevice;
import com.openbravo.pos.util.StringParser;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Default hardware-backed implementation of {@link DeviceTicket}.
 *
 * @author JG uniCenta / KriolOS Team
 */
public class DefaultDeviceTicket implements DeviceTicket {

    private static final Logger logger = Logger.getLogger("com.openbravo.pos.printer.DefaultDeviceTicket");

    private DevicePrinter n_devicePrinterPanel;
    private DeviceFiscalPrinter m_deviceFiscal;
    private DeviceDisplay m_devicedisplay;
    private DevicePrinter m_nullprinter;
    private Map<String, DevicePrinter> m_deviceprinters;

    /**
     * Creates a new mock/panel-backed instance of DefaultDeviceTicket.
     */
    public DefaultDeviceTicket() {
        n_devicePrinterPanel = new DevicePrinterPanel();
        m_deviceFiscal = new DeviceFiscalPrinterNull();
        m_devicedisplay = new DeviceDisplayNull();
        m_nullprinter = new DevicePrinterNull();
        m_deviceprinters = new HashMap<>();
        addPrinter("1", n_devicePrinterPanel);
    }

    /**
     * Creates a new instance of DefaultDeviceTicket wired to configured peripherals.
     *
     * @param parent UI component parent
     * @param props  Application properties
     */
    public DefaultDeviceTicket(Component parent, AppProperties props) {
        PrinterWritterPool pws = new PrinterWritterPool();

        m_nullprinter = new DevicePrinterNull();
        m_deviceprinters = new HashMap<>();

        initDeviceFiscalPrinter(props);
        initDeviceDisplay(props, pws);
        initDevicePrinter(parent, props, pws);
    }

    private void initDeviceFiscalPrinter(AppProperties props) {
        String raw = props != null ? props.getProperty("machine.fiscalprinter") : null;
        FiscalPrinterConfig config = FiscalPrinterConfig.parse(raw);
        FiscalPrinterDevice device = PeripheralManager.getFiscalPrinter(config);
        if (device instanceof DeviceFiscalPrinter) {
            m_deviceFiscal = (DeviceFiscalPrinter) device;
        } else {
            m_deviceFiscal = new DeviceFiscalPrinterNull();
        }
    }

    private void initDevicePrinter(Component parent, AppProperties props, PrinterWritterPool pws) {
        int iPrinterIndex = 1;
        String sPrinterIndex = Integer.toString(iPrinterIndex);
        String sprinter = props.getProperty("machine.printer");

        List<String> serialNamesAlternative = Arrays.asList("serial", "rxtx", "file");

        while (sprinter != null && !"".equals(sprinter)) {
            StringParser sp = new StringParser(sprinter);
            String sPrinterType = sp.nextToken(':');
            String sPrinterParam1 = sp.nextToken(',');
            String sPrinterParam2 = sp.nextToken(',');

            logger.log(Level.WARNING, "Printer device: " + sprinter);

            if (serialNamesAlternative.contains(sPrinterType)) {
                sPrinterParam2 = sPrinterParam1;
                sPrinterParam1 = sPrinterType;
                sPrinterType = "epson";
            }

            try {
                switch (sPrinterType) {
                    case "screen":
                        addPrinter(sPrinterIndex, new DevicePrinterPanel());
                        break;
                    case "printer":
                        if (sPrinterParam2 == null || sPrinterParam2.equals("")
                                || sPrinterParam2.equals("true")) {
                            sPrinterParam2 = "receipt";
                        } else if (sPrinterParam2.equals("false")) {
                            sPrinterParam2 = "standard";
                        }
                        addPrinter(sPrinterIndex, new DevicePrinterPrinter(parent, sPrinterParam1,
                                Integer.parseInt(props.getProperty("paper." + sPrinterParam2 + ".x")),
                                Integer.parseInt(props.getProperty("paper." + sPrinterParam2 + ".y")),
                                Integer.parseInt(props.getProperty("paper." + sPrinterParam2 + ".width")),
                                Integer.parseInt(props.getProperty("paper." + sPrinterParam2 + ".height")),
                                props.getProperty("paper." + sPrinterParam2 + ".mediasizename")
                        ));
                        break;
                    case "epson":
                        addPrinter(sPrinterIndex, new DevicePrinterESCPOS(
                                pws.getPrinterWritter(sPrinterParam1, sPrinterParam2),
                                new CodesEpson(), new UnicodeTranslatorInt()));
                        break;
                    case "tmu220":
                        addPrinter(sPrinterIndex, new DevicePrinterESCPOS(
                                pws.getPrinterWritter(sPrinterParam1, sPrinterParam2),
                                new CodesTMU220(), new UnicodeTranslatorInt()));
                        break;
                    case "star":
                        addPrinter(sPrinterIndex, new DevicePrinterESCPOS(
                                pws.getPrinterWritter(sPrinterParam1, sPrinterParam2),
                                new CodesStar(), new UnicodeTranslatorStar()));
                        break;
                    case "ithaca":
                        addPrinter(sPrinterIndex, new DevicePrinterESCPOS(
                                pws.getPrinterWritter(sPrinterParam1, sPrinterParam2),
                                new CodesIthaca(), new UnicodeTranslatorInt()));
                        break;
                    case "surepos":
                        addPrinter(sPrinterIndex, new DevicePrinterESCPOS(
                                pws.getPrinterWritter(sPrinterParam1, sPrinterParam2),
                                new CodesSurePOS(), new UnicodeTranslatorSurePOS()));
                        break;
                    case "plain":
                        addPrinter(sPrinterIndex, new DevicePrinterPlain(
                                pws.getPrinterWritter(sPrinterParam1, sPrinterParam2)));
                        break;
                    case "javapos":
                        addPrinter(sPrinterIndex, new DevicePrinterJavaPOS(
                                sPrinterParam1, sPrinterParam2));
                        break;
                }
            } catch (TicketPrinterException e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }

            iPrinterIndex++;
            sPrinterIndex = Integer.toString(iPrinterIndex);
            sprinter = props.getProperty("machine.printer." + sPrinterIndex);
        }
    }

    private void initDeviceDisplay(AppProperties props, PrinterWritterPool pws) {
        String deviceUri = props != null ? props.getProperty("machine.display") : null;
        DisplayConfig config = DisplayConfig.parse(deviceUri);
        DisplayDevice device = PeripheralManager.getDisplay(config);
        if (device instanceof DeviceDisplay) {
            m_devicedisplay = (DeviceDisplay) device;
        } else {
            m_devicedisplay = new DeviceDisplayNull();
        }
    }

    public static String[] getDisplayDeviceTypes() {
        return new String[]{"screen", "window", "dual", "epson", "surepos", "ld200", "javapos", "led8"};
    }

    public static String[] getPrinterDeviceTypes() {
        return new String[]{"screen", "window", "dual", "epson", "surepos", "ld200", "javapos", "led8"};
    }

    private void addPrinter(String sPrinterIndex, DevicePrinter p) {
        m_deviceprinters.put(sPrinterIndex, p);
    }

    @Override
    public DeviceFiscalPrinter getFiscalPrinter() {
        return m_deviceFiscal;
    }

    @Override
    public DeviceDisplay getDeviceDisplay() {
        return m_devicedisplay;
    }

    @Override
    public DevicePrinter getDevicePrinter(String key) {
        DevicePrinter printer = m_deviceprinters.get(key);
        return printer == null ? m_nullprinter : printer;
    }

    @Override
    public List<DevicePrinter> getDevicePrinterAll() {
        return new ArrayList<>(m_deviceprinters.values());
    }
}
