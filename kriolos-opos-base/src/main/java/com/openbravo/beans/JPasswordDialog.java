/*
 * Copyright (C) 2023 Paulo Borges
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
package com.openbravo.beans;

import com.openbravo.editor.JEditorPassword;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.util.Hashcypher;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

/**
 * A specialized password entry dialog that extends JEditorTextDialog.
 * It swaps the standard text editor field with a masked password input field.
 * Inherits the Wayland/COSMIC geometric alignment fixes from JEditorTextDialog.
 *
 * @author poolborges
 */
public class JPasswordDialog extends JEditorTextDialog{
    
    public JPasswordDialog(Frame parent, boolean modal) {
        super(parent, modal);
        setupEditor();
    }
    
    public JPasswordDialog(java.awt.Dialog  parent, boolean modal) {
        super(parent, modal);
        setupEditor();
    }
    
    private void setupEditor(){
        this.setEditor(new JEditorPassword());
    }
    
    public static String showEditor(Component parent, String title) {
        return showEditor(parent, title, null, null);
    }

    public static String showEditor(Component parent, String title, String message) {
        return showEditor(parent, title, message, null);
    }

    /**
     * Factory pattern method to instantiate and prepare the password dialog.
     * Delegates window positioning rules to the parent's showDialog handler
     * to prevent Wayland floating decoupling.
     */
    public static String showEditor(Component parent, String title, String message, Icon icon) {

        Window window = getWindow(parent);
        JPasswordDialog dialog;

        if (window instanceof Frame) {
            dialog = new JPasswordDialog((Frame) window, true);
        } else if (window instanceof Dialog) {
            dialog = new JPasswordDialog((Dialog) window, true);
        } else {
            // Fallback: build without a parent but force focus configurations
            dialog = new JPasswordDialog((Frame) null, true);
        }

        // Explicitly enforce modality and hint to the window manager
        // that this cannot exist independently of its ancestor hierarchy.
        dialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);

        // Calls the shared parent method which contains the pack() and positioning logic
        return showDialog(dialog, title, message, icon);
    }

    public static String changePassword(Component parent) {
        // Show the changePassword dialogs but do not check the old password
        
        String sPassword = JPasswordDialog.showEditor(parent,                 
                AppLocal.getIntString("label.Password"), 
                AppLocal.getIntString("label.passwordnew"),
                new ImageIcon(Hashcypher.class.getResource("/com/openbravo/images/password.png")));
        if (sPassword != null) {
            String sPassword2 = JPasswordDialog.showEditor(parent,                 
                    AppLocal.getIntString("label.Password"), 
                    AppLocal.getIntString("label.passwordrepeat"),
                    new ImageIcon(Hashcypher.class.getResource("/com/openbravo/images/password.png")));
            if (sPassword2 != null) {
                if (sPassword.equals(sPassword2)) {
                    return  Hashcypher.hashString(sPassword);
                } else {
                    JOptionPane.showMessageDialog(parent, AppLocal.getIntString("message.changepassworddistinct"), AppLocal.getIntString("message.title"), JOptionPane.WARNING_MESSAGE);
                }
            }
        }   
        
        return null;
    }

    /**
     *
     * @param parent
     * @param sOldPassword
     * @return
     */
    public static String changePassword(Component parent, String sOldPassword) {
        
        String sPassword = JPasswordDialog.showEditor(parent,                 
                AppLocal.getIntString("label.Password"), 
                AppLocal.getIntString("label.passwordold"),
                new ImageIcon(Hashcypher.class.getResource("/com/openbravo/images/password.png")));
        if (sPassword != null) {
            if (Hashcypher.authenticate(sPassword, sOldPassword)) {
                return changePassword(parent);               
            } else {
                JOptionPane.showMessageDialog(parent, AppLocal.getIntString("message.BadPassword"), AppLocal.getIntString("message.title"), JOptionPane.WARNING_MESSAGE);
           }
        }
        return null;
    }
}
