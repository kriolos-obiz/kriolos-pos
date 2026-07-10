package com.openbravo.pos.util;

import javax.swing.JOptionPane;
import java.awt.Toolkit;

public class NotificationHelper {

    public static void beep() {
        try {
            Toolkit.getDefaultToolkit().beep();
        } catch (Exception e) {
            System.err.println("Erro no beep: " + e.getMessage());
        }
    }

    public static void showWarning(String message) {
        JOptionPane.showMessageDialog(null, message, "WARNING", JOptionPane.WARNING_MESSAGE);
    }

    public static void showError(String message) {
        JOptionPane.showMessageDialog(
                null,
                message,
                "Erro Crítico",
                JOptionPane.ERROR_MESSAGE
        );
    }

    public static void beepAndLog(String message) {
        beep();
        System.out.println("[POS NOTIFICATION]: " + message);
        // Aqui poderia integrar uma biblioteca de logs ou uma barra de notificações discreta
    }
}

