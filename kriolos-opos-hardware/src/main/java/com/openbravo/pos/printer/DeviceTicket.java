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
package com.openbravo.pos.printer;

import com.openbravo.pos.forms.AppProperties;
import com.openbravo.pos.printer.escpos.*;
import com.openbravo.pos.printer.javapos.DeviceDisplayJavaPOS;
import com.openbravo.pos.printer.javapos.DeviceFiscalPrinterJavaPOS;
import com.openbravo.pos.printer.javapos.DevicePrinterJavaPOS;
import com.openbravo.pos.printer.printer.DevicePrinterPrinter;
import com.openbravo.pos.printer.screen.DeviceDisplayWindowDualScreen;
import com.openbravo.pos.printer.screen.DeviceDisplayPanel;
import com.openbravo.pos.printer.screen.DeviceDisplayWindow;
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
 *
 * @author JG uniCenta
 */
public class DeviceTicket {

    private static final Logger logger = Logger.getLogger("com.openbravo.pos.printer.DeviceTicket");

    private DevicePrinter n_devicePrinterPanel;
    private DeviceFiscalPrinter m_deviceFiscal;
    private DeviceDisplay m_devicedisplay;
    private DevicePrinter m_nullprinter;
    private Map<String, DevicePrinter> m_deviceprinters;

    /**
     *
     * Creates a new instance of DeviceTicket
     */
    public DeviceTicket() {

        n_devicePrinterPanel = new DevicePrinterPanel();
        m_deviceFiscal = new DeviceFiscalPrinterNull();
        m_devicedisplay = new DeviceDisplayNull();
        m_nullprinter = new DevicePrinterNull();
        m_deviceprinters = new HashMap<>();
        addPrinter("1", n_devicePrinterPanel);
    }

    /**
     *
     * @param parent
     * @param props
     */
    public DeviceTicket(Component parent, AppProperties props) {

        PrinterWritterPool pws = new PrinterWritterPool();

        m_nullprinter = new DevicePrinterNull();
        m_deviceprinters = new HashMap<>();

        initDeviceFiscalPrinter(props);
        initDeviceDisplay(props, pws);
        initDevicePrinter(parent, props, pws);

    }

    /**
     * Init Fiscal Printer
     *
     * @param props
     * @param pws
     */
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

    /**
     * Init/Register all printer
     *
     * @param parent
     * @param props
     * @param pws
     */
    private void initDevicePrinter(Component parent, AppProperties props, PrinterWritterPool pws) {
        // Empezamos a iterar por las impresoras...
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

            //Special Case for Epson ( [serial|rxtx|file]:param1,param2
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
                        // backward compatibility
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
        String[] devices = {"screen", "window", "dual", "epson", "surepos", "ld200", "javapos", "led8"};
        return devices;
    }

    public static String[] getPrinterDeviceTypes() {
        String[] devices = {"screen", "window", "dual", "epson", "surepos", "ld200", "javapos", "led8"};
        return devices;
    }

    private void addPrinter(String sPrinterIndex, DevicePrinter p) {
        m_deviceprinters.put(sPrinterIndex, p);
    }

    /**
     * PrinterWritterPool
     *
     * Class to avoid two device (serial/file/rxtx) to open the same COM port
     * Avoid colision on Computer COM port


    /**
     *
     * @return Fiscal printer
     */
    public DeviceFiscalPrinter getFiscalPrinter() {
        return m_deviceFiscal;
    }

    /**
     *
     * @return Device display
     */
    public DeviceDisplay getDeviceDisplay() {
        return m_devicedisplay;
    }

    /**
     *
     * @param key
     * @return Device printer
     */
    public DevicePrinter getDevicePrinter(String key) {
        DevicePrinter printer = m_deviceprinters.get(key);
        return printer == null ? m_nullprinter : printer;
    }

    /**
     *
     * @return Device printer list
     */
    public List<DevicePrinter> getDevicePrinterAll() {
        return new ArrayList<>(m_deviceprinters.values());
    }

    /**
     * Generates a string consisting of a specific character repeated a given
     * number of times.
     *
     * @param size The desired length of the string
     * @param paddingChar The character used to fill the string
     * @return A string composed of the repeated character
     */
    public static String getPaddingString(int size, char paddingChar) {
        if (size <= 0) {
            return "";
        }
        return String.valueOf(paddingChar).repeat(size);
    }

    /**
     * Generates a string consisting of spaces repeated a given number of times.
     *
     * @param size The desired length of the string
     * @return A string composed of spaces
     */
    public static String getPaddingString(int size) {
        return getPaddingString(size, ' ');
    }

    /**
     * Aligns a barcode value by padding it with leading zeros to meet the
     * target length. If the barcode length exceeds the target size, it
     * truncates from the left to keep the end.
     *
     * @param barcode The barcode string to align
     * @param targetSize The exact final size needed for the barcode
     * @return Zero-padded barcode string
     */
    public static String alignBarcode(String barcode, int targetSize) {
        if (barcode == null) {
            barcode = "";
        }

        if (barcode.length() > targetSize) {
            return barcode.substring(barcode.length() - targetSize);
        }

        return getPaddingString(targetSize - barcode.length(), '0') + barcode;
    }

    /**
     * Default line character length for standard receipt printer hardware.
     */
    public static final int DEFAULT_LINE_LENGTH = 42;

    /**
     * Routes string tokens to appropriate alignment methods based on target identifier flags.
     * Called directly inside the SAX TicketParser process.
     * 
     * @param textAlignment The target alignment code identifier constant
     * @param text          The input text string sequence to process
     * @param textLength    Maximum text column allocation limit width
     * @return Standard padded aligned output string
     */
    public static String alignText(int textAlignment, String text, int textLength) {
        return switch (textAlignment) {
            case DevicePrinter.ALIGN_RIGHT ->
                alignRight(text, textLength);
            case DevicePrinter.ALIGN_CENTER ->
                alignCenter(text, textLength);
            default ->
                alignLeft(text, textLength); // DevicePrinter.ALIGN_LEFT
        };
    }

    /**
     * Aligns text to the left. If the text exceeds the maximum line size, it is
     * truncated. If it is shorter, spaces are appended to the right.
     *
     * @param line The text string to align
     * @param lineSize Maximum length of the output string
     * @return Left-aligned string padded with spaces
     */
    public static String alignLeft(String line, int lineSize) {
        if (line == null) {
            line = "";
        }
        if (line.length() > lineSize) {
            return line.substring(0, lineSize);
        }
        return line + " ".repeat(lineSize - line.length());
    }

    /**
     * Aligns text to the right. If the text exceeds the maximum line size, it
     * is truncated. If it is shorter, spaces are prepended to the left.
     *
     * @param line The text string to align
     * @param lineSize Maximum length of the output string
     * @return Right-aligned string padded with spaces
     */
    public static String alignRight(String line, int lineSize) {
        if (line == null) {
            line = "";
        }
        if (line.length() > lineSize) {
            return line.substring(0, lineSize);
        }
        return " ".repeat(lineSize - line.length()) + line;
    }

    /**
     * Centers the text within the specified line size. Correctly distributes
     * odd spaces to guarantee the exact total length.
     *
     * @param line The text string to align
     * @param lineSize Maximum length of the output string
     * @return Center-aligned string padded evenly with spaces
     */
    public static String alignCenter(String line, int lineSize) {
        if (line == null) {
            line = "";
        }
        if (line.length() > lineSize) {
            return line.substring(0, lineSize);
        }

        int totalSpaces = lineSize - line.length();
        int leftSpaces = totalSpaces / 2;
        int rightSpaces = totalSpaces - leftSpaces; // Safely handles odd numbers

        return " ".repeat(leftSpaces) + line + " ".repeat(rightSpaces);
    }

    /**
     * Centers the text using the default line length.
     *
     * @param line The text string to align
     * @return Center-aligned string with the default line length
     */
    public static String alignCenter(String line) {
        return alignCenter(line, DEFAULT_LINE_LENGTH);
    }

// JG 16 May 12     public static final byte[] transNumber(String sCad) {
    /**
     *
     * @param sCad
     * @return Convert number to string
     */
    public static byte[] transNumber(String sCad) {

        if (sCad == null) {
            return null;
        } else {
            byte bAux[] = new byte[sCad.length()];
            for (int i = 0; i < sCad.length(); i++) {
                bAux[i] = transNumberChar(sCad.charAt(i));
            }
            return bAux;
        }
    }

    /**
     *
     * @param sChar
     * @return Convert hex to character
     */
    public static byte transNumberChar(char sChar) {
        switch (sChar) {
            case '0':
                return 0x30;
            case '1':
                return 0x31;
            case '2':
                return 0x32;
            case '3':
                return 0x33;
            case '4':
                return 0x34;
            case '5':
                return 0x35;
            case '6':
                return 0x36;
            case '7':
                return 0x37;
            case '8':
                return 0x38;
            case '9':
                return 0x39;
            default:
                return 0x30;
        }
    }
}
