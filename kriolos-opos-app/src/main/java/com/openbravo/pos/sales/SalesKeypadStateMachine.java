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
package com.openbravo.pos.sales;

/**
 * State machine managing sales keypad input, quantities, prices, and barcode buffer.
 */
public class SalesKeypadStateMachine {

    public static final int NUMBERZERO = 0;
    public static final int NUMBERVALID = 1;

    public static final int NUMBER_INPUTZERO = 0;
    public static final int NUMBER_INPUTZERODEC = 1;
    public static final int NUMBER_INPUTINT = 2;
    public static final int NUMBER_INPUTDEC = 3;
    public static final int NUMBER_PORZERO = 4;
    public static final int NUMBER_PORZERODEC = 5;
    public static final int NUMBER_PORINT = 6;
    public static final int NUMBER_PORDEC = 7;

    private int numberStatus = NUMBER_INPUTZERO;
    private int numberStatusInput = NUMBERZERO;
    private int numberStatusPor = NUMBERZERO;

    private String priceText = "";
    private String porText = "";
    private final StringBuilder barcodeBuffer = new StringBuilder();

    private boolean priceWith00 = false;

    public SalesKeypadStateMachine() {
        reset();
    }

    public void setPriceWith00(boolean priceWith00) {
        this.priceWith00 = priceWith00;
    }

    public boolean isPriceWith00() {
        return priceWith00;
    }

    public void reset() {
        priceText = "";
        porText = "";
        barcodeBuffer.setLength(0);

        numberStatus = NUMBER_INPUTZERO;
        numberStatusInput = NUMBERZERO;
        numberStatusPor = NUMBERZERO;
    }

    /**
     * Feeds a keypad character into the state machine.
     *
     * @param cTrans the character (0-9, ., *, \u007f)
     * @return true if the character was handled as a keypad/buffer transition
     */
    public boolean processKeypadChar(char cTrans) {
        barcodeBuffer.append(cTrans);

        if (cTrans == '\u007f') {
            reset();
            return true;
        }

        if (cTrans >= '0' && cTrans <= '9') {
            handleDigit(cTrans);
            return true;
        } else if (cTrans == '.') {
            handleDot();
            return true;
        } else if (cTrans == '*') {
            handleAsterisk();
            return true;
        }

        return false;
    }

    private void handleDigit(char cTrans) {
        if (numberStatus == NUMBER_INPUTZERO) {
            if (cTrans == '0') {
                priceText = "0";
            } else {
                if (!priceWith00) {
                    priceText = priceText + cTrans;
                } else {
                    priceText = formatTempPrice(priceText + cTrans);
                }
                numberStatus = NUMBER_INPUTINT;
                numberStatusInput = NUMBERVALID;
            }
        } else if (numberStatus == NUMBER_INPUTINT) {
            if (!priceWith00) {
                priceText = priceText + cTrans;
            } else {
                priceText = formatTempPrice(priceText + cTrans);
            }
        } else if (numberStatus == NUMBER_INPUTZERODEC || numberStatus == NUMBER_INPUTDEC) {
            if (cTrans == '0') {
                if (!priceWith00) {
                    priceText = priceText + cTrans;
                } else {
                    priceText = formatTempPrice(priceText + cTrans);
                }
            } else {
                priceText = priceText + cTrans;
                numberStatus = NUMBER_INPUTDEC;
                numberStatusInput = NUMBERVALID;
            }
        } else if (numberStatus == NUMBER_PORZERO) {
            if (cTrans == '0') {
                porText = "x0";
            } else {
                porText = "x" + cTrans;
                numberStatus = NUMBER_PORINT;
                numberStatusPor = NUMBERVALID;
            }
        } else if (numberStatus == NUMBER_PORINT) {
            porText = porText + cTrans;
        } else if (numberStatus == NUMBER_PORZERODEC || numberStatus == NUMBER_PORDEC) {
            porText = porText + cTrans;
            if (cTrans != '0') {
                numberStatus = NUMBER_PORDEC;
                numberStatusPor = NUMBERVALID;
            }
        }
    }

    private void handleDot() {
        if (numberStatus == NUMBER_INPUTZERO) {
            if (!priceWith00) {
                priceText = "0.";
                numberStatus = NUMBER_INPUTZERODEC;
            } else {
                priceText = "";
                numberStatus = NUMBER_INPUTZERO;
            }
        } else if (numberStatus == NUMBER_INPUTINT) {
            if (!priceWith00) {
                priceText = priceText + ".";
                numberStatus = NUMBER_INPUTDEC;
            } else {
                priceText = formatTempPrice(priceText + "00");
                numberStatus = NUMBER_INPUTINT;
            }
        } else if (numberStatus == NUMBER_PORZERO) {
            if (!priceWith00) {
                porText = "x0.";
                numberStatus = NUMBER_PORZERODEC;
            } else {
                porText = "x";
                numberStatus = NUMBERVALID;
            }
        } else if (numberStatus == NUMBER_PORINT) {
            if (!priceWith00) {
                porText = porText + ".";
                numberStatus = NUMBER_PORDEC;
            } else {
                porText = porText + "00";
                numberStatus = NUMBERVALID;
            }
        }
    }

    private void handleAsterisk() {
        if (numberStatus == NUMBER_INPUTINT || numberStatus == NUMBER_INPUTDEC) {
            porText = "x";
            numberStatus = NUMBER_PORZERO;
        } else if (numberStatus == NUMBER_INPUTZERO || numberStatus == NUMBER_INPUTZERODEC) {
            priceText = "0";
            porText = "x";
            numberStatus = NUMBER_PORZERO;
        }
    }

    private String formatTempPrice(String jPrice) {
        String clean = jPrice.replace(".", "");
        try {
            long tempL = Long.parseLong(clean);
            clean = Long.toString(tempL);
        }
        catch (NumberFormatException e) {
            return jPrice;
        }

        while (clean.length() < 3) {
            clean = "0" + clean;
        }
        return (clean.length() <= 2) ? clean : (new StringBuilder(clean).insert(clean.length() - 2, ".").toString());
    }

    public double getInputValue() {
        try {
            return Double.parseDouble(priceText);
        }
        catch (NumberFormatException ex) {
            return 0.0;
        }
    }

    public double getPorValue() {
        try {
            return Double.parseDouble(porText.substring(1));
        }
        catch (NumberFormatException | IndexOutOfBoundsException ex) {
            return 1.0;
        }
    }

    public boolean isInputValid() {
        return numberStatusInput == NUMBERVALID;
    }

    public boolean isInputZero() {
        return numberStatusInput == NUMBERZERO;
    }

    public boolean isPorValid() {
        return numberStatusPor == NUMBERVALID;
    }

    public boolean isPorZero() {
        return numberStatusPor == NUMBERZERO;
    }

    public String getPriceText() {
        return priceText;
    }

    public String getPorText() {
        return porText;
    }

    public String getBarcode() {
        return barcodeBuffer.toString();
    }

    public void clearBarcode() {
        barcodeBuffer.setLength(0);
    }

    public int getNumberStatus() {
        return numberStatus;
    }

    public int getNumberStatusInput() {
        return numberStatusInput;
    }

    public int getNumberStatusPor() {
        return numberStatusPor;
    }
}
