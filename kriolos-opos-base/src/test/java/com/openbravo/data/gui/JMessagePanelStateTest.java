/*
 * Copyright (C) 2026 KriolOS
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

/**
 *
 * @author dev
 */
package com.openbravo.data.gui;

import com.openbravo.data.gui.modal.PosUIModal;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Classe de teste para simular a mudança dinâmica de estados do JMessagePanel
 * usando as constantes de bits reais da classe MessageInf.
 */
public class JMessagePanelStateTest {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Força o Look and Feel nativo do sistema para garantir o agnosticismo visual
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                // Avança com o padrão caso falhe
            }

            // Frame principal que simula o ecrã do POS
            JFrame mainFrame = new JFrame("KriolOS POS - Painel de Testes Tátil");
            mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            mainFrame.setSize(800, 600);
            mainFrame.setLocationRelativeTo(null);
            mainFrame.setLayout(new BorderLayout());

            // Painel central onde a mensagem ativa será renderizada
            JPanel containerPanel = new JPanel(new GridBagLayout());
            containerPanel.setBackground(new Color(45, 45, 48));
            mainFrame.add(containerPanel, BorderLayout.CENTER);

            // Barra de ferramentas inferior para alternar entre os estados do POS em tempo real
            JPanel controlBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
            controlBar.setBackground(new Color(28, 28, 28));

            // Ações para mudar o estado do painel
            JButton btnDanger = new JButton("Simular DANGER (Erro)");
            JButton btnWarning = new JButton("Simular WARNING (Aviso)");
            JButton btnSuccess = new JButton("Simular SUCCESS (Sucesso)");
            JButton btnNotice = new JButton("Simular NOTICE (Info)");

            controlBar.add(btnDanger);
            controlBar.add(btnWarning);
            controlBar.add(btnSuccess);
            controlBar.add(btnNotice);
            mainFrame.add(controlBar, BorderLayout.SOUTH);

            // Ouvinte comum para atualizar o painel injetado no centro
            ActionListener stateSwitcher = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    containerPanel.removeAll();
                    
                    MessageInf mockInf;
                    String cmd = e.getActionCommand();
                    
                    if (cmd.equals("DANGER")) {
                        Exception ex = new java.sql.SQLException("Access denied for user 'kriolos_pos'@'192.168.1.50'");
                        mockInf = new MessageInf(MessageInf.SGN_DANGER, 
                            "Erro Crítico: A gaveta de dinheiro ou o banco de dados falhou em responder à transação atual.", ex);
                    } else if (cmd.equals("SUCCESS")) {
                        mockInf = new MessageInf(MessageInf.SGN_SUCCESS, 
                            "Venda finalizada com sucesso! O inventário foi atualizado e o recibo impresso corretamente.");
                    } else if (cmd.equals("NOTICE")) {
                        mockInf = new MessageInf(MessageInf.SGN_NOTICE, 
                            "Notificação: O fecho de caixa agendado para o operador atual será iniciado em 15 minutos.");
                    } else {
                        // Padrão: WARNING
                        mockInf = new MessageInf(MessageInf.SGN_WARNING, 
                            "Aviso: O papel da impressora térmica está no fim. Substitua o rolo para evitar interrupções.");
                    }

                    // Instancia o componente real atualizado com o Adobe Spectrum
                    JMessagePanel testPanel = new JMessagePanel(mockInf, true);
                    
                    // Mock do comportamento de fecho da janela modal
                   // 1. Criação limpa da instância do Builder Modal do KriolOS
                    PosUIModal modalInstance = PosUIModal.create(mainFrame, testPanel)
                            .setTitle(testPanel.getTitle())
                            .setModal(true)
                            .setResizable(false)
                            .onClosed(() -> {
                                // Callback oficial acionado automaticamente quando o painel invocar o close()
                                JOptionPane.showMessageDialog(mainFrame, 
                                    "Ação processada no POS! Código retornado: " + testPanel.getOptionChosed(),
                                    "Evento do Botão", JOptionPane.INFORMATION_MESSAGE);
                            });

                    // 2. Vincula a instância ao painel de mensagens para permitir o controle interno
                    testPanel.setModalContext(modalInstance);

                    GridBagConstraints gbc = new GridBagConstraints();
                    gbc.insets = new Insets(20, 20, 20, 20);
                    containerPanel.add(testPanel, gbc);
                    
                    containerPanel.revalidate();
                    containerPanel.repaint();
                }
            };

            // Mapeamento dos botões de controle
            btnDanger.setActionCommand("DANGER");
            btnWarning.setActionCommand("WARNING");
            btnSuccess.setActionCommand("SUCCESS");
            btnNotice.setActionCommand("NOTICE");

            btnDanger.addActionListener(stateSwitcher);
            btnWarning.addActionListener(stateSwitcher);
            btnSuccess.addActionListener(stateSwitcher);
            btnNotice.addActionListener(stateSwitcher);

            // Aciona o estado inicial (Warning) para a janela não abrir vazia
            btnWarning.doClick();

            mainFrame.setVisible(true);
        });
    }
}

/**
 * Classe Mock temporária para permitir a compilação e o isolamento do teste 
 * em ambientes onde o ficheiro MessageInf ou AppLocal nativos ainda não foram integrados.
 */
class MessageInf {
    public final static int SGN_DANGER = 0xFF000000;
    public final static int SGN_WARNING = 0xFE000000;
    public final static int SGN_CAUTION = 0xFD000000;
    public final static int SGN_NOTICE = 0xFC000000;
    public final static int SGN_IMPORTANT = 0xFA000000;
    public final static int SGN_SUCCESS = 0xFB000000;

    private final int code;
    private final String message;
    private final Object cause;

    public MessageInf(int code, String message, Object cause) {
        this.code = code;
        this.message = message;
        this.cause = cause;
    }

    public MessageInf(int code, String message) {
        this(code, message, null);
    }

    public int getMessageCode() { return code; }
    public String getCode() { return "KRIOL-" + Integer.toHexString(code & 0x00FFFFFF).toUpperCase(); }
    public String getErrorCodeMsg() { return "STATUS_CODE"; }
    public String getMessage() { return message; }
    public Object getCause() { return cause; }
}

/** Mock complementar do ficheiro de propriedades nativo */
class AppLocal {
    public static String getIntString(String key) {
        if ("button.details".equals(key)) return "Detalhes";
        if ("button.cancel".equals(key)) return "CANCELAR";
        if ("button.ok".equals(key)) return "OK";
        return "Mensagem";
    }
}
