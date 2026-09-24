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

package com.openbravo.beans;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import javax.swing.Icon;
import javax.swing.SwingUtilities;

/**
 *
 * @author  adrian
 */
public class JDoubleDialog extends JNumberDialog<Double> {

    public JDoubleDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        m_jnumber = new com.openbravo.editor.JEditorDoublePositive();
        init();
    }

    public JDoubleDialog(java.awt.Dialog parent, boolean modal) {
        super(parent, modal);
        m_jnumber = new com.openbravo.editor.JEditorDoublePositive();
        init();
    }

    private void init() {
        m_jnumber.setValue(0.0);
        setup();
    }

    public static Double showComponent(Component parent, String title) {
        return showComponent(parent, title, null, null);
    }

    public static Double showComponent(Component parent, String title, String message) {
        return showComponent(parent, title, message, null);
    }

    public static Double showComponent(Component parent, String title, String message, Icon icon) {
        return JDoublePanel.show(parent, title, message, icon);
    }
}
