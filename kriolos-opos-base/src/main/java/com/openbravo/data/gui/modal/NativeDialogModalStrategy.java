//    KriolOS POS
//    Copyright (c) 2019-2026 KriolOS
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
package com.openbravo.data.gui.modal;

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JDialog;
import javax.swing.WindowConstants;

/**
 * Native {@link JDialog} implementation strategy for {@link PosUIModal}.
 * <p>
 * Ensures robust window owner hierarchy resolution and FlatLaf / Wayland buffer validation.
 * </p>
 *
 * @author KriolOS
 */
public class NativeDialogModalStrategy implements ModalStrategy {

    private JDialog dialog;

    @Override
    public void display(PosUIModal modal) {
        Window owner = PosUIModal.resolveWindowOwner(modal.getParent());

        if (owner instanceof Frame) {
            dialog = new JDialog((Frame) owner, modal.getTitle(), modal.isModal());
        } else if (owner instanceof Dialog) {
            dialog = new JDialog((Dialog) owner, modal.getTitle(), modal.isModal());
        } else {
            dialog = new JDialog(owner, modal.getTitle(),
                    modal.isModal() ? Dialog.ModalityType.APPLICATION_MODAL : Dialog.ModalityType.MODELESS);
        }

        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setResizable(modal.isResizable());

        if (modal.getPreferredSize() != null) {
            dialog.setPreferredSize(modal.getPreferredSize());
        }

        dialog.getContentPane().add(modal.getContent());

        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                modal.notifyClosed();
            }
        });

        // Robust layout, positioning, and Wayland/FlatLaf rendering validation
        dialog.pack();
        dialog.setLocationRelativeTo(owner);
        dialog.revalidate();
        dialog.repaint();

        modal.setDialog(dialog);
        dialog.setVisible(true);
    }

    @Override
    public void close(PosUIModal modal) {
        if (dialog != null) {
            dialog.setVisible(false);
            dialog.dispose();
            dialog = null;
        }
        modal.notifyClosed();
    }

    public JDialog getDialog() {
        return dialog;
    }
}
