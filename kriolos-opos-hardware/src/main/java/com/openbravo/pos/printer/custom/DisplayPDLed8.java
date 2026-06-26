/*
 * Copyright (C) 2026 Paulo Borges
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
package com.openbravo.pos.printer.custom;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author psb
 */
public class DisplayPDLed8 {
    
    private static final Logger LOGGER = Logger.getLogger(DisplayPDLed8.class.getName());

    private final String nomePorta;
    private SerialPort serialPort;

    // Constantes para as Luzes de Status
    public static final int STATUS_ESCURO = 0;
    public static final int STATUS_PRECO = 1;
    public static final int STATUS_TOTAL = 2;
    public static final int STATUS_RECEBIDO = 3;
    public static final int STATUS_TROCO = 4;

    public DisplayPDLed8(String nomePorta) {
        this.nomePorta = nomePorta;
    }

    /**
     * Envia um valor numérico e define a luz de status correspondente.
     * 
     * @param statusOpcao
     * @param valor
     */
    public void atualizarDisplay(int statusOpcao, String valor) {
        OutputStream out = null;
        try {
            CommPortIdentifier portId = CommPortIdentifier.getPortIdentifier(nomePorta);
            CommPort commPort = portId.open("DisplayPDLed8", 2000);

            if (!(commPort instanceof SerialPort)) {
                LOGGER.log(Level.WARNING, "Not a valid Serial/COM port: " + nomePorta);
                if(commPort != null){
                    commPort.close();
                }
                return;
            }

            serialPort = (SerialPort) commPort;
            serialPort.setSerialPortParams(2400, 
                                           SerialPort.DATABITS_8, 
                                           SerialPort.STOPBITS_1, 
                                           SerialPort.PARITY_NONE);
            
            // Configura os pinos de controle de fluxo de hardware
            // serialPort.setDTR(true);
            // serialPort.setRTS(true);
            
            serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN);

            out = serialPort.getOutputStream();

            // Formatação estável automática de dízimas e espaços
            String textoValidado = valor;
            byte[] textBytes = textoValidado.getBytes(StandardCharsets.UTF_8);

            byte[] payload = new byte[3 + 3 + textBytes.length + 1];
            byte statusByte = (byte) (0x30 + statusOpcao);

            // Bloco .sX
            payload[0] = 0x1B;
            payload[1] = 0x73;
            payload[2] = statusByte;
            
            // Bloco .QA
            payload[3] = 0x1B; //ESC
            payload[4] = 0x51;
            payload[5] = 0x41;

            System.arraycopy(textBytes, 0, payload, 6, textBytes.length);
            payload[payload.length - 1] = 0x0D; // CR

            // Envia os dados através da Stream de saída
            out.write(payload);
            out.flush();

            Thread.sleep(150);
            return;

        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Exception on PDLed8: ", ex);
        } finally {
            // Fecha a stream e a porta com segurança
            try { if (out != null) out.close(); } catch (Exception ignored) {}
            try { if (serialPort != null) serialPort.close(); } catch (Exception ignored) {}
        }
    }

    /**
     * Limpa totalmente o ecrã enviando o comando corrido vazio.
     */
    public void limpar() {
        atualizarDisplay(STATUS_ESCURO, "");
    }
}
