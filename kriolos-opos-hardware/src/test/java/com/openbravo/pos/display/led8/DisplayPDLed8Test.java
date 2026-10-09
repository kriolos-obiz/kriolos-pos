package com.openbravo.pos.display.led8;

/**
 * Manual test runner for DisplayPDLed8 hardware.
 *
 * @author Administrator
 */
public class DisplayPDLed8Test {

    public static void main(String[] args) throws InterruptedException {
        DisplayPDLed8 display = new DisplayPDLed8("COM2");

        System.out.println("Iniciar...");
        display.limpar();
        Thread.sleep(5000);

        System.out.println("Quando passa um produto...");
        display.atualizarDisplay(DisplayPDLed8.STATUS_PRECO, "500.00");
        Thread.sleep(2000);

        display.atualizarDisplay(DisplayPDLed8.STATUS_PRECO, "300.00");
        Thread.sleep(2000);

        display.atualizarDisplay(DisplayPDLed8.STATUS_PRECO, "200.00");
        Thread.sleep(2000);

        display.atualizarDisplay(DisplayPDLed8.STATUS_PRECO, "500.00");
        Thread.sleep(2000);

        System.out.println("Total da compra...");
        display.atualizarDisplay(DisplayPDLed8.STATUS_TOTAL, "1500.00");
        Thread.sleep(5000);

        System.out.println("Recebido...");
        display.atualizarDisplay(DisplayPDLed8.STATUS_RECEBIDO, "2000.00");
        Thread.sleep(5000);

        System.out.println("Troco...");
        display.atualizarDisplay(DisplayPDLed8.STATUS_TROCO, "500.00");
        Thread.sleep(5000);

        display.limpar();
    }
}
