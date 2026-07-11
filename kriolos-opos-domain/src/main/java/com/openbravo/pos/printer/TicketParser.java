//    KriolOS POS
//    Copyright (c) 2019-2023 KriolOS
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program.  If not, see <http://www.gnu.org/licenses/>.
package com.openbravo.pos.printer;

import com.openbravo.basic.BasicException;
import com.openbravo.pos.forms.DataLogicSystem;
import com.openbravo.pos.printer.custom.DeviceDisplayPDLED8;
import com.openbravo.pos.printer.escpos.DeviceDisplayLED8;
import com.openbravo.pos.ticket.TicketInfo;
import com.openbravo.pos.util.AudioUtils;
import com.openbravo.pos.util.SAXParserUtils;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author JG uniCenta
 */
public class TicketParser extends DefaultHandler {

    private static final Logger LOGGER = Logger.getLogger(TicketParser.class.getName());

    private final DeviceTicket printer;
    private final DataLogicSystem dataLogicSystem;

    private StringBuilder currentText;

    private String barcodeType;
    private String barcodePosition;
    private int textAlignment;
    private int textLength;
    private int textStyle;

    private StringBuilder visorLineBuilder;
    private int visorAnimation;
    private String visorLine1;
    private String visorLine2;

    private double fiscalTicketLinePrice;
    private double fiscalTicketLineQty;
    private double fiscalTicketTotalPaid;
    private int fiscalTicketLineTaxInfo;

    private int outputType;
    private static final int OUTPUT_NONE = 0;
    private static final int OUTPUT_DISPLAY = 1;
    private static final int OUTPUT_TICKET = 2;
    private static final int OUTPUT_FISCAL = 3;
    private DevicePrinter outputPrinter;
    private String currentUser;
    private String ticketId;

    //QrCode
    int qrcodeSize = DevicePrinter.QRCODE_DEFAULT_SIZE;
    char qrcodeErrorCode =  DevicePrinter.QRCODE_DEFAULT_ERROR_CODE;

    public TicketParser(DeviceTicket printer, DataLogicSystem system) {
        this.printer = printer;
        this.dataLogicSystem = system;
    }

    public void printTicket(String xmlInput, TicketInfo ticket) throws TicketPrinterException {
        this.currentUser = ticket.getName();
        this.ticketId = (ticket.getTicketId() == 0) ? "No Sale" : Integer.toString(ticket.getTicketId());

        try (Reader in = new StringReader(xmlInput)) {
            printTicket(in);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "I/O error closing StringReader.", e);
        }

    }

    public void printTicket(String xmlInput) throws TicketPrinterException {
        try (Reader in = new StringReader(xmlInput)) {
            printTicket(in);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "I/O error closing StringReader.", e);
        }
    }

    public void printTicket(Reader in) throws TicketPrinterException {
        
        try {
            SAXParserFactory spf = SAXParserUtils.newSecureInstance();
            // A new parser instance for each call ensures thread safety
            SAXParser sp = spf.newSAXParser();
            sp.parse(new InputSource(in), this);

        } catch (ParserConfigurationException ex) {
            LOGGER.log(Level.SEVERE, "Parser configuration error.", ex);
            throw new TicketPrinterException("exception.parserconfig", ex);
        } catch (SAXException ex) {
            LOGGER.log(Level.SEVERE, "XML parsing error.", ex);
            throw new TicketPrinterException("exception.xmlfile", ex);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "I/O error during parsing.", ex);
            throw new TicketPrinterException("exception.iofile", ex);
        }
    }

    @Override
    public void startDocument() throws SAXException {
        currentText = null;
        barcodeType = null;
        barcodePosition = null;
        visorLineBuilder = null;
        visorAnimation = DeviceDisplayEngine.ANIMATION_NULL;
        visorLine1 = null;
        visorLine2 = null;
        outputType = OUTPUT_NONE;
        outputPrinter = null;
    }

    @Override
    public void endDocument() throws SAXException {
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

        switch (outputType) {
            case OUTPUT_NONE:
                handleNoOutputStart(qName, attributes);
                break;
            case OUTPUT_TICKET:
                handleTicketOutputStart(qName, attributes);
                break;
            case OUTPUT_DISPLAY:
                handleDisplayOutputStart(qName, attributes);
                break;
            case OUTPUT_FISCAL:
                handleFiscalOutputStart(qName, attributes);
                break;
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {

        switch (outputType) {
            case OUTPUT_NONE:
                handleNoOutputEnd(qName);
                break;

            case OUTPUT_TICKET:
                handleTicketOutputEnd(qName);
                break;
            case OUTPUT_DISPLAY:
                handleDisplayOutputEnd(qName);
                break;
            case OUTPUT_FISCAL:
                handleFiscalOutputEnd(qName);
                break;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (currentText != null) {
            currentText.append(ch, start, length);
        }
    }

    private void handleNoOutputStart(String qName, Attributes attributes) throws SAXException {
        switch (qName) {
            case "opendrawer":
                printer.getDevicePrinter(readString(attributes.getValue("printer"), "1")).openDrawer();
                // Cashdrawer has been activated record the data in the table
                try {
                    dataLogicSystem.execDrawerOpened(
                            new Object[]{currentUser, ticketId});
                } catch (BasicException ex) {
                    LOGGER.log(Level.SEVERE, "Failed to log drawer opened event.", ex);
                }
                break;
            case "play":
                currentText = new StringBuilder();
                break;
            case "ticket":
                outputType = OUTPUT_TICKET;
                outputPrinter = printer.getDevicePrinter(readString(attributes.getValue("printer"), "1"));
                outputPrinter.beginReceipt();
                break;
            case "display":
                outputType = OUTPUT_DISPLAY;
                String animation = attributes.getValue("animation");

                visorAnimation = parseAnimation(animation);
                visorLine1 = null;
                visorLine2 = null;
                outputPrinter = null;
                break;
            case "fiscalreceipt":
                outputType = OUTPUT_FISCAL;
                printer.getFiscalPrinter().beginReceipt();
                break;
            case "fiscalzreport":
                printer.getFiscalPrinter().printZReport();
                break;
            case "fiscalxreport":
                printer.getFiscalPrinter().printXReport();
                break;
        }
    }

    private void handleNoOutputEnd(String qName) {

        String textContent = (currentText != null) ? currentText.toString() : null;
        if ("play".equals(qName) && textContent != null) {
            AudioUtils.play(textContent);
            currentText = null;
        }
    }

    private void handleTicketOutputStart(String qName, Attributes attributes) throws SAXException {
        switch (qName) {
            
            case "qrcode":
                currentText = new StringBuilder();
                qrcodeSize = parseInt(attributes.getValue("size"), DevicePrinter.QRCODE_DEFAULT_SIZE);
                qrcodeErrorCode = parseChar(attributes.getValue("ec"), DevicePrinter.QRCODE_DEFAULT_ERROR_CODE);
             break;

            case "logo":
                currentText = new StringBuilder();
                break;
            case "image":
                currentText = new StringBuilder();
                break;
            case "barcode":
                currentText = new StringBuilder();
                barcodeType = attributes.getValue("type");
                barcodePosition = attributes.getValue("position");
                break;
            case "line":
                outputPrinter.beginLine(parseInt(attributes.getValue("size"), DevicePrinter.SIZE_0));
                break;
            case "text":
                currentText = new StringBuilder();
                textStyle = getFontStyle(attributes);
                String sAlign = attributes.getValue("align");
                textAlignment = parseTextAlignment(sAlign);
                textLength = parseInt(attributes.getValue("length"), 0);
                break;
            default:
                break;
        }
    }

    private void handleTicketOutputEnd(String qName) throws SAXException {
        //String textContent = (currentText != null) ? currentText.toString() : null;
        switch (qName) {
            case "qrcode":
                outputPrinter.printQRCode(currentText.toString(), qrcodeSize, qrcodeErrorCode);
                currentText = null;
                break;
            case "logo":
                //Star TSP700 to print stored logo image JDL
                outputPrinter.printLogo();
                break;
            case "image":
                try {
                    BufferedImage image = dataLogicSystem.getResourceAsImage(currentText.toString());
                    if (image != null) {
                        outputPrinter.printImage(image);
                    }
                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, "Failed to load image resource.", ex);
                }
                currentText = null;
                break;
            case "barcode":
                outputPrinter.printBarCode(barcodeType,
                        barcodePosition,
                        currentText.toString());
                currentText = null;
                break;
            case "text":
                if (textLength > 0) {
                    outputPrinter.printText(textStyle, DeviceTicket.alignText(textAlignment, currentText.toString(), textLength));
                } else {
                    outputPrinter.printText(textStyle, currentText.toString());
                }
                currentText = null;
                break;
            case "line":
                outputPrinter.endLine();
                break;
            case "ticket":
                outputPrinter.endReceipt();
                outputType = OUTPUT_NONE;
                outputPrinter = null;
                break;
            default:
                break;
        }
    }

    private void handleDisplayOutputStart(String qName, Attributes attributes) {
        switch (qName) {
            case "line":
                // line 1 or 2 of the display
                visorLineBuilder = new StringBuilder();
                break;
            case "line1":
                // linea 1 del visor
                visorLineBuilder = new StringBuilder();
                break;
            case "line2":
                // linea 2 del visor
                visorLineBuilder = new StringBuilder();
                break;
            case "text":
                textStyle = parseInt(attributes.getValue("ligth"));
                currentText = new StringBuilder();
                String sAlign = attributes.getValue("align");
                textAlignment = parseTextAlignment(sAlign);
                textLength = parseInt(attributes.getValue("length"));
                break;
            default:
                break;
        }
    }

    private void handleDisplayOutputEnd(String qName) {
        switch (qName) {
            case "line":
                // line 1 or 2 of the display
                if (visorLine1 == null) {
                    visorLine1 = visorLineBuilder.toString();
                } else {
                    visorLine2 = visorLineBuilder.toString();
                }
                visorLineBuilder = null;
                break;
            case "line1":
                // linea 1 del visor
                visorLine1 = visorLineBuilder.toString();
                visorLineBuilder = null;
                break;
            case "line2":
                // linea 2 del visor
                visorLine2 = visorLineBuilder.toString();
                visorLineBuilder = null;
                break;
            case "text":
                if (textLength > 0) {
                    switch (textAlignment) {
                        case DevicePrinter.ALIGN_RIGHT:
                            visorLineBuilder.append(DeviceTicket.alignRight(currentText.toString(), textLength));
                            break;
                        case DevicePrinter.ALIGN_CENTER:
                            visorLineBuilder.append(DeviceTicket.alignCenter(currentText.toString(), textLength));
                            break;
                        default: // DevicePrinter.ALIGN_LEFT
                            visorLineBuilder.append(DeviceTicket.alignLeft(currentText.toString(), textLength));
                            break;
                    }
                } else {
                    visorLineBuilder.append(currentText);
                }
                
                //Apply Display 'Light style' 
                if(this.printer.getDeviceDisplay() instanceof DeviceDisplayLED8 deviceDisplay){
                    deviceDisplay.displayLight(this.textStyle);
                }

                //Apply Display 'Status line'
                if(this.printer.getDeviceDisplay() instanceof DeviceDisplayPDLED8 deviceDisplay){
                    deviceDisplay.changeStatus(this.textStyle);
                }
                currentText = null;
                break;
            case "display":
                printer.getDeviceDisplay().writeVisor(visorAnimation, visorLine1, visorLine2);
                visorAnimation = DeviceDisplayEngine.ANIMATION_NULL;
                visorLine1 = null;
                visorLine2 = null;
                outputType = OUTPUT_NONE;
                outputPrinter = null;
                break;
            default:
                break;
        }
    }

    private void handleFiscalOutputStart(String qName, Attributes attributes) {

        switch (qName) {
            case "line":
                currentText = new StringBuilder();
                fiscalTicketLinePrice = parseDouble(attributes.getValue("price"));
                fiscalTicketLineQty = parseDouble(attributes.getValue("units"), 1.0);
                fiscalTicketLineTaxInfo = parseInt(attributes.getValue("tax"));
                break;
            case "message":
                currentText = new StringBuilder();
                break;
            case "total":
                currentText = new StringBuilder();
                fiscalTicketTotalPaid = parseDouble(attributes.getValue("paid"));
                break;
            default:
                break;
        }
    }

    private void handleFiscalOutputEnd(String qName) {
        switch (qName) {
            case "fiscalreceipt":
                printer.getFiscalPrinter().endReceipt();
                outputType = OUTPUT_NONE;
                break;
            case "line":
                printer.getFiscalPrinter().printLine(currentText.toString(), fiscalTicketLinePrice, fiscalTicketLineQty, fiscalTicketLineTaxInfo);
                currentText = null;
                break;
            case "message":
                printer.getFiscalPrinter().printMessage(currentText.toString());
                currentText = null;
                break;
            case "total":
                printer.getFiscalPrinter().printTotal(currentText.toString(), fiscalTicketTotalPaid);
                currentText = null;
                break;
            default:
                break;
        }
    }

    /**
     * Helper to parse text alignment attribute.
     */
    private int parseTextAlignment(String align) {
        return switch (readString(align, "left")) {
            case "right" ->
                DevicePrinter.ALIGN_RIGHT;
            case "center" ->
                DevicePrinter.ALIGN_CENTER;
            default ->
                DevicePrinter.ALIGN_LEFT;
        };
    }

    /**
     * Parses the animation attribute string.
     *
     * @param animationString The animation string from XML.
     * @return The corresponding animation constant.
     */
    private int parseAnimation(String animationString) {
        return switch (readString(animationString, "none")) {
            case "scroll" ->
                DeviceDisplayEngine.ANIMATION_SCROLL;
            case "flyer" ->
                DeviceDisplayEngine.ANIMATION_FLYER;
            case "blink" ->
                DeviceDisplayEngine.ANIMATION_BLINK;
            case "curtain" ->
                DeviceDisplayEngine.ANIMATION_CURTAIN;
            default ->
                DeviceDisplayEngine.ANIMATION_NULL;
        };
    }

    private int getFontStyle(Attributes attributes) {
        int fontStyle = DevicePrinter.STYLE_PLAIN;
        if("true".equals(attributes.getValue("bold"))){
            fontStyle = DevicePrinter.STYLE_BOLD;
        }
        if("true".equals(attributes.getValue("underline"))){
            fontStyle = DevicePrinter.STYLE_UNDERLINE;
        }
        return fontStyle;
    }

    private int parseInt(String sValue, int iDefault) {
        try {
            return Integer.parseInt(sValue);
        } catch (NumberFormatException eNF) {
            return iDefault;
        }
    }

    private int parseInt(String sValue) {
        return parseInt(sValue, 0);
    }

    private double parseDouble(String sValue, double ddefault) {
        try {
            return Double.parseDouble(sValue);
        } catch (NumberFormatException eNF) {
            return ddefault;
        }
    }

    private char parseChar(String sValue, char cDefault) {
        char receiveChar = cDefault;
        if (sValue != null && !sValue.isEmpty()) {
            String cleanEc = sValue.trim();
            if (!cleanEc.isEmpty()) {
                receiveChar = cleanEc.charAt(0);
            }
        }
        return receiveChar;
    }

    private double parseDouble(String sValue) {
        return parseDouble(sValue, 0.0);
    }

    private String readString(String value, String defaultValue) {
        return (value == null) ? defaultValue : value;
    }
}
