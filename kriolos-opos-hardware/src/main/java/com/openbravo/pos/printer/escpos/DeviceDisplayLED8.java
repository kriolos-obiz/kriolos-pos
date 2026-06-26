package com.openbravo.pos.printer.escpos;

import com.openbravo.pos.printer.DeviceTicket;

public class DeviceDisplayLED8 extends DeviceDisplaySerial {

    private final UnicodeTranslator trans;

    public DeviceDisplayLED8(PrinterWritter display) {
        this.trans = new UnicodeTranslatorInt();
        init(display);
    }

    @Override
    public void initVisor() {
        this.display.init(ESCPOS.INIT);
        this.display.write(ESCPOS.SELECT_DISPLAY);
        this.display.write(this.trans.getCodeTable());
        this.display.write(ESCPOS.VISOR_HIDE_CURSOR);
        this.display.write(ESCPOS.VISOR_CLEAR);
        this.display.write(ESCPOS.VISOR_HOME);
        this.display.flush();
    }

    @Override
    public void repaintLines() {
        this.display.write(ESCPOS.SELECT_DISPLAY);
        this.display.write(ESCPOS.VISOR_CLEAR);
        this.display.write(ESCPOS.VISOR_HOME);
        this.display.write(new byte[]{27, 81, 65});
        this.display.write(this.trans.transString(DeviceTicket.alignLeft(this.baseDeviceDisplay.getLine1(), 8)));
        this.display.write(new byte[]{13});
        this.display.flush();
    }

    /**
     * Change Display Ligth Style
     * @param iStyle 
     */
    public void displayLight(int iStyle) {
        this.display.write(ESCPOS.SELECT_DISPLAY);
        switch (iStyle) {
            case 1:
                this.display.write(new byte[]{27, 115, 49});
                return;
            case 2:
                this.display.write(new byte[]{27, 115, 50});
                return;
            case 3:
                this.display.write(new byte[]{27, 115, 51});
                return;
            case 4:
                this.display.write(new byte[]{27, 115, 52});
                return;
        }
        this.display.write(new byte[]{27, 115, 48});
    }
}
