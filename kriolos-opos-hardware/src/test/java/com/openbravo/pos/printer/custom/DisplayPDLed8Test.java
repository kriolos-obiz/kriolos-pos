package com.openbravo.pos.printer.custom;

/**
 *
 * @author Administrator
 */
public class DisplayPDLed8Test {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InterruptedException {
        // TODO code application logic here
        DisplayPDLed8 display = new DisplayPDLed8("COM2");

        
        System.out.println("Iniciar...");
// Quando inicia uma nova venda
        display.limpar();
        Thread.sleep(5000);

// Quando passa um produto
        System.out.println("Quando passa um produto...");
        display.atualizarDisplay(DisplayPDLed8.STATUS_PRECO, "500.00");
        Thread.sleep(2000);
// Quando passa um produtodisplay.limpar();
        System.out.println("A limpar...");
        display.limpar();
        Thread.sleep(100);
        display.atualizarDisplay(DisplayPDLed8.STATUS_PRECO, "300.00");
        Thread.sleep(2000);
// Quando passa um produto
        System.out.println("Quando passa um produto...");

        display.limpar();
        Thread.sleep(100);
        display.atualizarDisplay(DisplayPDLed8.STATUS_PRECO, "200.00");
        Thread.sleep(2000);
// Quando passa um produto
        System.out.println("Quando passa um produto...");

        display.limpar();
        Thread.sleep(100);
        display.atualizarDisplay(DisplayPDLed8.STATUS_PRECO, "500.00");
        Thread.sleep(2000);
// Total a apagar
        System.out.println("Total a apagar...");
        display.limpar();
        Thread.sleep(100);
        display.atualizarDisplay(DisplayPDLed8.STATUS_TOTAL, "1500.00");
        Thread.sleep(3000);
// Total recebido
        System.out.println("Total recebido...");
        display.limpar();
        Thread.sleep(100);
        display.atualizarDisplay(DisplayPDLed8.STATUS_RECEBIDO, "2000.00");
        Thread.sleep(3000);

// No fecho da venda ao calcular o troco
        System.out.println("calcular o troco...");

        display.limpar();
        Thread.sleep(100);
        display.atualizarDisplay(DisplayPDLed8.STATUS_TROCO, "500.00");
        Thread.sleep(3000);

        System.out.println("A disparar abertura da caixa de dinheiro...");
        //display.abrirGaveta();
        Thread.sleep(200); // Aguarda o fim da transmissão física antes do close()

    }

}
