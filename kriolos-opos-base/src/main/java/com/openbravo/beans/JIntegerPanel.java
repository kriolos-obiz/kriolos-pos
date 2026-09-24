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
import com.openbravo.editor.JEditorIntegerPositive;
import java.awt.Component;
import javax.swing.Icon;

/**
 * Integer numeric input panel, presentable via {@link PosUIModal} or embedded directly into views.
 *
 * @author KriolOS
 */
public class JIntegerPanel extends JNumberPanel<Integer> {

    private static final long serialVersionUID = 1L;

    public JIntegerPanel() {
        m_jnumber = new JEditorIntegerPositive();
        m_jnumber.setValue(1);
        setup();
    }

    public static Integer show(Component parent, String title, String message, Icon icon) {
        JIntegerPanel panel = new JIntegerPanel();
        panel.setMessage(message, icon);

        PosUIModal modal = PosUIModal.create(parent, panel)
                .setTitle(title)
                .setModal(true)
                .setResizable(false);

        panel.setModalContext(modal);
        modal.show();

        return panel.isAccepted() ? panel.getValue() : null;
    }

    public static Integer show(Component parent, String title, String message) {
        return show(parent, title, message, null);
    }

    public static Integer show(Component parent, String title) {
        return show(parent, title, null, null);
    }
}
