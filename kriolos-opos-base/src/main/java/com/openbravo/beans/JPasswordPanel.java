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
package com.openbravo.beans;

import com.openbravo.data.gui.modal.PosUIModal;
import com.openbravo.editor.JEditorPassword;
import java.awt.Component;
import javax.swing.Icon;

/**
 * Masked password entry panel based on {@link JEditorTextPanel}, presentable via {@link PosUIModal}.
 *
 * @author KriolOS
 */
public class JPasswordPanel extends JEditorTextPanel {

    private static final long serialVersionUID = 1L;

    public JPasswordPanel() {
        setEditor(new JEditorPassword());
        setName("kriolos:auth:password-panel");
    }

    public static String show(Component parent, String title, String message, Icon icon) {
        JPasswordPanel panel = new JPasswordPanel();
        panel.setMessage(message, icon);

        PosUIModal modal = PosUIModal.create(parent, panel)
                .setTitle(title)
                .setModal(true)
                .setResizable(false);

        panel.setModalContext(modal);
        modal.show();

        return panel.isAccepted() ? panel.getValue() : null;
    }

    public static String show(Component parent, String title, String message) {
        return show(parent, title, message, null);
    }

    public static String show(Component parent, String title) {
        return show(parent, title, null, null);
    }
}
