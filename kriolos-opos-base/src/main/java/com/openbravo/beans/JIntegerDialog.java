//    KriolOS POS
//    Copyright (c) 2019-2023 KriolOS - joint with Jacinto Rodriguez
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
 * Dec 2017
 * @author  Jack Gerarrd uniCenta
 */
public class JIntegerDialog extends JNumberDialog<Integer> {

    public JIntegerDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        m_jnumber = new com.openbravo.editor.JEditorIntegerPositive();
        init();
    }

    public JIntegerDialog(java.awt.Dialog parent, boolean modal) {
        super(parent, modal);
        m_jnumber = new com.openbravo.editor.JEditorIntegerPositive();
        init();
    }
    
    private void init() {
        m_jnumber.setValue(1);
        setup();
    }

    public static Integer showComponent(Component parent, String title) {
        return showComponent(parent, title, null, null);
    }
    public static Integer showComponent(Component parent, String title, String message) {
        return showComponent(parent, title, message, null);
    }
    public static Integer showComponent(Component parent, String title, String message, Icon icon) {
        return JIntegerPanel.show(parent, title, message, icon);
    }

}
