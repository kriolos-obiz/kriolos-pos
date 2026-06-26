package com.openbravo.pos.printer.custom;

import com.openbravo.pos.printer.escpos.*;
import com.openbravo.pos.printer.DeviceTicket;

/**
 * PD-LE8 represent a Pole Display LED8 Device 
 *  First Line: NdNdNdNdNdNdNdN (ex: "8.8.8.8.8.8.8.8" for 8 DIGIT) 
 *  Second Line: Fixed Status/Icon/Word (Unit Price, Total Amount, Tendered, Change)
 * @author psb
 */
public class DeviceDisplayPDLED8 extends DeviceDisplaySerial {
    
    private static final int PD_LED8_MAX_NUM_OF_DIGIT = 8;

    private final AZ09Translator trans;

    public DeviceDisplayPDLED8(PrinterWritter pWritter) {
        init(pWritter);
        this.trans = new AZ09Translator();
    }

    @Override
    public void initVisor() {
        this.display.init(CODE.CMD_VISOR_CLEAR);
        this.display.flush();
    }

    @Override
    public void repaintLines() {
        this.display.write(CODE.CMD_VISOR_CLEAR);
        this.display.write(CODE.CMD_HEADER);
        this.display.write(this.trans.translateString(DeviceTicket.alignRight(this.baseDeviceDisplay.getLine1(), PD_LED8_MAX_NUM_OF_DIGIT)));
        this.display.write(CODE.CMD_TERMINATOR);
        this.display.flush();
    }

    public void changeStatus(int status) {

        switch (status) {

            case 0 -> {
                this.display.write(CODE.CMD_STATUS_CELAR);
                return;
            }
            case 1 -> {
                this.display.write(CODE.CMD_STATUS_PRICE);
                return;
            }
            case 2 -> {
                this.display.write(CODE.CMD_STATUS_TOTAL_AMOUNT);
                return;
            }
            case 3 -> {
                this.display.write(CODE.CMD_STATUS_TOTAL_TENDERED);
                return;
            }
            case 4 -> {
                this.display.write(CODE.CMD_STATUS_TOTAL_CHANGE);
                return;
            }
        }
        this.display.write(CODE.CMD_STATUS_CELAR);
    }
    
    
    private class CODE {
        public static final byte ASCII_ESC = 0x1B;
        public static final byte ASCII_CR = 0x0D; // Carriage return
        public static final byte ASCII_Q = 0x51; //
        public static final byte ASCII_A = 0x41; //
        public static final byte ASCII_S_LOWER = 0x73; //
        public static final byte ASCII_NUM_0 = 0x30; // '0'
        public static final byte ASCII_NUM_1 = 0x31; // '1'
        public static final byte ASCII_NUM_2 = 0x32; // '2'
        public static final byte ASCII_NUM_3 = 0x33; // '3'
        public static final byte ASCII_NUM_4 = 0x34; // '4'
        
        
        public static final byte[] CMD_HEADER = {ASCII_ESC, ASCII_Q, ASCII_A};
        public static final byte[] CMD_TERMINATOR = {ASCII_CR};
        
        public static final byte[] CMD_VISOR_CLEAR = {
            ASCII_ESC, ASCII_S_LOWER, ASCII_NUM_0, //ESC s 0
            ASCII_ESC, ASCII_Q, ASCII_A, ASCII_CR};//ESC Q A CR
        
        public static final byte[] CMD_STATUS_CELAR = {ASCII_ESC, ASCII_S_LOWER, ASCII_NUM_0};//ESC s 0
        public static final byte[] CMD_STATUS_PRICE = {ASCII_ESC, ASCII_S_LOWER, ASCII_NUM_1};//ESC s 1
        public static final byte[] CMD_STATUS_TOTAL_AMOUNT = {ASCII_ESC, ASCII_S_LOWER, ASCII_NUM_2};//ESC s 2
        public static final byte[] CMD_STATUS_TOTAL_TENDERED = {ASCII_ESC, ASCII_S_LOWER, ASCII_NUM_3};//ESC s 3
        public static final byte[] CMD_STATUS_TOTAL_CHANGE = {ASCII_ESC, ASCII_S_LOWER, ASCII_NUM_4};//ESC s 4
        
    }
}
