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
package com.openbravo.pos.sales.modern;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

/**
 * Thread-Safe utility to bind Numpad keys without freezing the Swing GUI.
 */
public class POSKeyBinder {

    public static void bindNumpadKey(final JButton button, final int keyCode, final String actionName) {
        if (button == null) return;

        // FORÇA A EXECUÇÃO NA EDT (EVENT DISPATCH THREAD) PARA NÃO CONGELAR O POS
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                
                button.setFocusable(false);
                
                KeyStroke stroke = KeyStroke.getKeyStroke(keyCode, 0);

                // 1. Limpeza segura dos mapas do Swing
                button.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).remove(stroke);
                button.getActionMap().remove(actionName);

                // 2. Registo do novo atalho
                button.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(stroke, actionName);
                button.getActionMap().put(actionName, new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        // Garante que o clique do botão também corre na thread visual
                        if (button.isEnabled() && button.isVisible()) {
                            button.doClick();
                        }
                    }
                });

                // 3. Força o componente a redesenhar suavemente sem bloquear a janela
                JComponent parent = (JComponent) button.getParent();
                if (parent != null) {
                    parent.revalidate();
                    parent.repaint();
                }
            }
        });
    }
}
