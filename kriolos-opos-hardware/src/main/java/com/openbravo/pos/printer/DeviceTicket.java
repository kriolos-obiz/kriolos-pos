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
        StringParser sf = new StringParser(props.getProperty("machine.fiscalprinter"));
        String sFiscalType = sf.nextToken(':');
        String sFiscalParam1 = sf.nextToken(',');
        try {
            if ("javapos".equals(sFiscalType)) {
                m_deviceFiscal = new DeviceFiscalPrinterJavaPOS(sFiscalParam1);
            } else {
                m_deviceFiscal = new DeviceFiscalPrinterNull();
            }
        } catch (TicketPrinterException e) {
            m_deviceFiscal = new DeviceFiscalPrinterNull(e.getMessage());
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
        
        List<String> serialNamesAlternative = Arrays.asList( "serial", "rxtx", "file");

        while (sprinter != null && !"".equals(sprinter)) {

            StringParser sp = new StringParser(sprinter);
            String sPrinterType = sp.nextToken(':');
            String sPrinterParam1 = sp.nextToken(',');
            String sPrinterParam2 = sp.nextToken(',');
            
            
            logger.log(Level.WARNING, "Printer device: "+sprinter);

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
        String deviceUri = props.getProperty("machine.display");
        StringParser sd = new StringParser(deviceUri);
        String sDisplayType = sd.nextToken(':');
        String sDisplayParam1 = sd.nextToken(',');
        String sDisplayParam2 = sd.nextToken(',');

        if ("serial".equals(sDisplayType)
                || "rxtx".equals(sDisplayType)
                || "file".equals(sDisplayType)) {
            sDisplayParam2 = sDisplayParam1;
            sDisplayParam1 = sDisplayType;
            sDisplayType = "epson";
        }

        try {

            switch (sDisplayType) {
                case "screen":
                    m_devicedisplay = new DeviceDisplayPanel();
                    break;
                case "window":
                    m_devicedisplay = new DeviceDisplayWindow();
                    break;
                case "dual":
                    m_devicedisplay = new DeviceDisplayWindowDualScreen();
                    break;
                case "epson":
                    m_devicedisplay = new DeviceDisplayESCPOS(
                            pws.getPrinterWritter(sDisplayParam1, sDisplayParam2),
                            new UnicodeTranslatorInt());
                    break;
                case "surepos":
                    m_devicedisplay = new DeviceDisplaySurePOS(
                            pws.getPrinterWritter(sDisplayParam1, sDisplayParam2));
                    break;
                case "ld200":
                    m_devicedisplay = new DeviceDisplayESCPOS(
                            pws.getPrinterWritter(sDisplayParam1, sDisplayParam2),
                            new UnicodeTranslatorEur());
                    break;
                case "javapos":
                    m_devicedisplay = new DeviceDisplayJavaPOS(sDisplayParam1);
                    break;
                case "led8":
                    this.m_devicedisplay = new DeviceDisplayLED8(
                            pws.getPrinterWritter(sDisplayParam1, sDisplayParam2));
                    break;
                case "pdled8":
                    this.m_devicedisplay = new DeviceDisplayLED8(
                            pws.getDisplayPrinterWritter(sDisplayParam1, sDisplayParam2, 2400)
                    );
                    break;
                default:
                    m_devicedisplay = new DeviceDisplayNull();
                    break;
            }
        } catch (TicketPrinterException e) {
            logger.log(Level.WARNING, "Exception init device display " + deviceUri, e);
            m_devicedisplay = new DeviceDisplayNull(e.getMessage());
        }
    }
    
    public static String[] getDisplayDeviceTypes(){
        String[] devices = {"screen", "window", "dual", "epson", "surepos", "ld200", "javapos", "led8"};
        return devices;
    }
    
    public static String[] getPrinterDeviceTypes(){
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
     *
     */
    private static class PrinterWritterPool {

        private final Map<String, PrinterWritter> m_apool = new HashMap<>();
        
        private String genUniqueKey(String connector, String port){
            return connector + "-->" + port;
        }
        
        public PrinterWritter getDisplayPrinterWritter(String con, String port, int baud) throws TicketPrinterException {

            String skey = genUniqueKey(con,port);
            PrinterWritter pw = m_apool.get(skey);
            if (pw == null) {

                switch (con) {
                    case "serial":
                    case "rxtx":
                        pw = new PrinterWritterRXTX(port, baud);
                        m_apool.put(skey, pw);
                        break;                
                }
            }
            return pw;
        }

        public PrinterWritter getPrinterWritter(String con, String port) throws TicketPrinterException {

            String skey = genUniqueKey(con,port);
            PrinterWritter pw = m_apool.get(skey);
            if (pw == null) {

                switch (con) {
                    case "serial":
                    case "rxtx":
                        pw = new PrinterWritterRXTX(port);
                        m_apool.put(skey, pw);
                        break;
                    case "file":
                        pw = new PrinterWritterFile(port);
                        m_apool.put(skey, pw);
                        break;
                    case "usb":
                        pw = new PrinterWritterRaw(port);
                        this.m_apool.put(skey, pw);
                        break;
                    case "network":
                        String[] str = port.split("\\:");
                        String hostAddr = str[0];
                        int portAddr = (str.length == 2) ? Integer.parseInt(str[1]) : 9100;
                        if (hostAddr != null && !hostAddr.isBlank()) {
                            pw = new PrinterWritterNetwork(hostAddr, portAddr);
                            this.m_apool.put(skey, pw);
                            return pw;
                        } else {
                            throw new TicketPrinterException("Invalid host addr: " + hostAddr + "; connection string: " + skey);
                        }
                    default:
                        throw new TicketPrinterException("Not supported protocol with connection string: " + skey);
                }
            }
            return pw;
        }
    }

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
     *
     * @param iSize
     * @param cWhiteChar
     * @return Spacing string length
     */
    public static String getWhiteString(int iSize, char cWhiteChar) {

        char[] cFill = new char[iSize];
        for (int i = 0; i < iSize; i++) {
            cFill[i] = cWhiteChar;
        }
        return new String(cFill);
    }

    /**
     *
     * @param iSize
     * @return Space sizing
     */
    public static String getWhiteString(int iSize) {

        return getWhiteString(iSize, ' ');
    }

    /**
     *
     * @param sLine
     * @param iSize
     * @return Barcode bar inter-spacing
     */
    public static String alignBarCode(String sLine, int iSize) {

        if (sLine.length() > iSize) {
            return sLine.substring(sLine.length() - iSize);
        } else {
            return getWhiteString(iSize - sLine.length(), '0') + sLine;
        }
    }

    public static String alignText(int textAlignment, String text, int textLength) {
          return switch (textAlignment) {
            case DevicePrinter.ALIGN_RIGHT ->
                alignRight(text, textLength);
            case DevicePrinter.ALIGN_CENTER ->
                alignCenter(text, textLength);
            default ->
                //DevicePrinter.ALIGN_LEFT
                alignLeft(text, textLength);
        };
    }
    /**
     *
     * @param sLine
     * @param iSize Line maximun size
     * 
     * @return Reduce spacing
     */
    public static String alignLeft(String sLine, int iSize) {

        if (sLine.length() > iSize) {
            return sLine.substring(0, iSize);
        } else {
            return sLine + getWhiteString(iSize - sLine.length());
        }
    }

    /**
     *
     * @param sLine
     * @param iSize
     * @return Add spacing
     */
    public static String alignRight(String sLine, int iSize) {

        if (sLine.length() > iSize) {
            return sLine.substring(sLine.length() - iSize);
        } else {
            return getWhiteString(iSize - sLine.length()) + sLine;
        }
    }

    /**
     *
     * @param sLine
     * @param iSize
     * @return Adjusts Left/Right spacing
     */
    public static String alignCenter(String sLine, int iSize) {

        if (sLine.length() > iSize) {
            return alignRight(sLine.substring(0, (sLine.length() + iSize) / 2), iSize);
        } else {
            return alignRight(sLine + getWhiteString((iSize - sLine.length()) / 2), iSize);
        }
    }

    /**
     * Text Aligment 'Center' with default text length of 42
     * @param sLine
     * @return Equalise Left/Right spacing
     */
    public static String alignCenter(String sLine) {
        return alignCenter(sLine, 42);
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
