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
package com.openbravo.data.gui;

import com.openbravo.data.loader.LocalRes;
import java.awt.*;
import javax.swing.*;

/**
 *
 * @author adrian
 */
public class JMessageDialog extends javax.swing.JDialog {

    private int optionChosed = Integer.MIN_VALUE;

    private static final long serialVersionUID = 1L;

    /**
     * Creates new form JMessageDialog
     */
    private JMessageDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
    }

    /**
     * Creates new form JMessageDialog
     */
    private JMessageDialog(java.awt.Dialog parent, boolean modal) {
        super(parent, modal);
    }

    public int getOptionChosed() {
        return optionChosed;
    }

    /**
     * Resolves the appropriate parent {@link Window} hierarchy for the modal dialog.
     * <p>
     * Proper window hierarchy resolution is critical under modern desktop window managers
     * (especially Linux Wayland / XWayland composites). If an isolated {@code new JFrame()}
     * is created without attachment, the dialog lacks transient-for hints and top-level
     * window grouping.
     * </p>
     *
     * @param parent the candidate parent component, or {@code null}
     * @return the resolved ancestor {@link Window} (Frame or Dialog), the first active showing Frame,
     *         or {@link JOptionPane#getRootFrame()} as a safe fallback
     */
    private static Window getWindow(Component parent) {
        if (parent != null) {
            if (parent instanceof Frame || parent instanceof Dialog) {
                return (Window) parent;
            }
            Window w = SwingUtilities.getWindowAncestor(parent);
            if (w != null) {
                return w;
            }
        }
        // Fallback to active/showing Frame to ensure proper window ownership and transient-for hints
        for (Frame f : Frame.getFrames()) {
            if (f.isShowing()) {
                return f;
            }
        }
        return JOptionPane.getRootFrame();
    }

    /**
     *
     * @param parent
     * @param inf
     */
    public static void showMessage(Component parent, MessageInf inf) {

        createMessageDialog(parent, inf, false);
    }

    public static int showConfirmDialog(Component parent, MessageInf inf) {
        return createMessageDialog(parent, inf, true);
    }

    /**
     * Constructs, initializes, lays out, and displays the modal {@link JMessageDialog}.
     *
     * @param parent the parent component used to anchor and center the dialog
     * @param inf the message metadata and payload model (icon, signal code, text, cause)
     * @param showConfirm {@code true} to display the Cancel button for confirmation mode,
     *                    {@code false} for informational/alert mode
     * @return the chosen option index (0 for OK, -1 for Cancel or close)
     */
    private static int createMessageDialog(Component parent, MessageInf inf, boolean showConfirm) {
        Window window = getWindow(parent);
        JMessageDialog myMsg;
        if (window instanceof Frame) {
            myMsg = new JMessageDialog((Frame) window, true);
        } else {
            myMsg = new JMessageDialog((Dialog) window, true);
        }

        myMsg.initComponents();
        if (parent != null) {
            myMsg.applyComponentOrientation(parent.getComponentOrientation());
        }
        myMsg.jscrException.setVisible(false);
        myMsg.getRootPane().setDefaultButton(myMsg.jcmdOK);

        myMsg.jlblIcon.setIcon(inf.getSignalWordIcon());
        myMsg.jlblIcon.setText(inf.getCode() + " " + inf.getErrorCodeMsg());
        myMsg.jlblMessage.setText(inf.getMessage());

        myMsg.jcmdCancel.setEnabled(showConfirm);
        myMsg.jcmdCancel.setVisible(showConfirm);

        // Capturamos el texto de la excepcion...
        if (inf.getCause() == null) {
            myMsg.jtxtException.setText(null);
        } else {
            StringBuilder sb = new StringBuilder();

            if (inf.getCause() instanceof Throwable) {
                Throwable t = (Throwable) inf.getCause();
                while (t != null) {
                    sb.append(t.getClass().getName());
                    sb.append(": \n");
                    sb.append(t.getMessage());
                    sb.append("\n\n");
                    t = t.getCause();
                }
            } else if (inf.getCause() instanceof Throwable[]) {
                Throwable[] m_aExceptions = (Throwable[]) inf.getCause();
                for (int i = 0; i < m_aExceptions.length; i++) {
                    sb.append(m_aExceptions[i].getClass().getName());
                    sb.append(": \n");
                    sb.append(m_aExceptions[i].getMessage());
                    sb.append("\n\n");
                }
            } else if (inf.getCause() instanceof Object[]) {
                Object[] m_aObjects = (Object[]) inf.getCause();
                for (int i = 0; i < m_aObjects.length; i++) {
                    sb.append(m_aObjects[i].toString());
                    sb.append("\n\n");
                }
            } else if (inf.getCause() instanceof String) {
                sb.append(inf.getCause().toString());
            } else {
                sb.append(inf.getCause().getClass().getName());
                sb.append(": \n");
                sb.append(inf.getCause().toString());
            }
            myMsg.jtxtException.setText(sb.toString());
        }
        myMsg.jtxtException.setCaretPosition(0);

        // Prepare dialog layout and surface validation prior to mapping
        myMsg.prepareAndValidateDialog(window);

        myMsg.setVisible(true);
        return myMsg.getOptionChosed();
    }

    /**
     * Prepares and forces layout validation and surface repainting for the dialog.
     * <p>
     * <b>Rationale (BUG-001):</b> On Linux composited window managers (Wayland / FlatLaf),
     * displaying unvalidated dialog geometries can lead to uninitialized black/blank frame
     * buffers until user interaction triggers OS damage events. Explicitly invoking
     * {@link #pack()}, {@link #setLocationRelativeTo(Component)}, {@code revalidate()},
     * and {@code repaint()} synchronizes component layout dimensions and peer surface buffers
     * before or immediately after visibility changes.
     * </p>
     *
     * @param owner the relative owner window to center upon, or {@code null} to center on screen
     */
    private void prepareAndValidateDialog(Window owner) {
        pack();
        setLocationRelativeTo(owner);
        getContentPane().revalidate();
        getContentPane().repaint();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel4 = new javax.swing.JPanel();
        jlblIcon = new javax.swing.JLabel();
        jlblMessage = new javax.swing.JLabel();
        jscrException = new javax.swing.JScrollPane();
        jtxtException = new javax.swing.JTextArea();
        jPanel3 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jcmdCancel = new javax.swing.JButton();
        jcmdOK = new javax.swing.JButton();
        jcmdMore = new javax.swing.JButton();

        setTitle(LocalRes.getIntString("title.message")); // NOI18N
        setMaximumSize(new java.awt.Dimension(480, 300));
        setMinimumSize(new java.awt.Dimension(300, 200));
        setPreferredSize(new java.awt.Dimension(260, 277));
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        jPanel4.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jPanel4.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jPanel4.setLayout(new javax.swing.BoxLayout(jPanel4, javax.swing.BoxLayout.Y_AXIS));

        jlblIcon.setText("jlblIcon");
        jlblIcon.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        jlblIcon.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        jPanel4.add(jlblIcon);

        jlblMessage.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jlblMessage.setText("jlblMessage");
        jlblMessage.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        jlblMessage.setMaximumSize(new java.awt.Dimension(460, 90));
        jlblMessage.setMinimumSize(new java.awt.Dimension(200, 30));
        jlblMessage.setPreferredSize(new java.awt.Dimension(200, 64));
        jPanel4.add(jlblMessage);

        jscrException.setAlignmentX(0.0F);
        jscrException.setPreferredSize(new java.awt.Dimension(200, 150));

        jtxtException.setEditable(false);
        jtxtException.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jscrException.setViewportView(jtxtException);

        jPanel4.add(jscrException);

        getContentPane().add(jPanel4, java.awt.BorderLayout.CENTER);

        jPanel3.setPreferredSize(new java.awt.Dimension(200, 50));
        jPanel3.setLayout(new java.awt.BorderLayout());

        jPanel2.setPreferredSize(new java.awt.Dimension(260, 50));

        jcmdCancel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jcmdCancel.setText(LocalRes.getIntString("button.cancel"));
        jcmdCancel.setMaximumSize(new java.awt.Dimension(80, 42));
        jcmdCancel.setMinimumSize(new java.awt.Dimension(80, 42));
        jcmdCancel.setPreferredSize(new java.awt.Dimension(80, 42));
        jcmdCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jcmdCancelActionPerformed(evt);
            }
        });
        jPanel2.add(jcmdCancel);

        jcmdOK.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jcmdOK.setText(LocalRes.getIntString("button.ok"));
        jcmdOK.setMaximumSize(new java.awt.Dimension(80, 42));
        jcmdOK.setMinimumSize(new java.awt.Dimension(80, 42));
        jcmdOK.setPreferredSize(new java.awt.Dimension(80, 42));
        jcmdOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jcmdOKActionPerformed(evt);
            }
        });
        jPanel2.add(jcmdOK);

        jcmdMore.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jcmdMore.setText(LocalRes.getIntString("button.information"));
        jcmdMore.setMaximumSize(new java.awt.Dimension(80, 42));
        jcmdMore.setMinimumSize(new java.awt.Dimension(80, 42));
        jcmdMore.setPreferredSize(new java.awt.Dimension(80, 42));
        jcmdMore.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jcmdMoreActionPerformed(evt);
            }
        });
        jPanel2.add(jcmdMore);

        jPanel3.add(jPanel2, java.awt.BorderLayout.LINE_END);

        getContentPane().add(jPanel3, java.awt.BorderLayout.SOUTH);

        setSize(new java.awt.Dimension(482, 345));
        //REMOVED SET LOCATION RELATIVE TO
    }// </editor-fold>//GEN-END:initComponents

    private void jcmdMoreActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jcmdMoreActionPerformed
        handleToggleExceptionDetails();
    }//GEN-LAST:event_jcmdMoreActionPerformed

    /**
     * Toggles visibility of the exception stack trace details text area.
     * <p>
     * Expands or collapses the exception scroll pane and triggers dialog recalculation
     * via {@link #prepareAndValidateDialog(Window)} to dynamically adapt the window size
     * and prevent visual tearing or unrendered frame buffers on composited window managers.
     * </p>
     */
    private void handleToggleExceptionDetails() {
        jcmdMore.setEnabled(true);
        jscrException.setVisible(!jscrException.isVisible());
        prepareAndValidateDialog(getOwner());
    }

    private void jcmdOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jcmdOKActionPerformed
        handleAccept();
    }//GEN-LAST:event_jcmdOKActionPerformed

    /**
     * Handles acceptance or confirmation action triggered by the OK button.
     * <p>
     * Sets option choice {@code 0} (OK), hides the dialog, and disposes native window resources.
     * </p>
     */
    private void handleAccept() {
        this.optionChosed = 0;
        setVisible(false);
        dispose();
    }

    /**
     * Closes the dialog
     */
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        handleWindowClosing();
    }//GEN-LAST:event_closeDialog

    /**
     * Handles the dialog window closing event (e.g. user clicked window manager close button).
     * <p>
     * Sets option choice {@code -1} (Cancel/Dismiss), hides the dialog, and disposes native window resources.
     * </p>
     */
    private void handleWindowClosing() {
        this.optionChosed = -1;
        setVisible(false);
        dispose();
    }

    private void jcmdCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jcmdCancelActionPerformed
        handleCancel();
    }//GEN-LAST:event_jcmdCancelActionPerformed

    /**
     * Handles cancellation action triggered by the Cancel button.
     * <p>
     * Sets option choice {@code -1} (Cancel), hides the dialog, and disposes native window resources.
     * </p>
     */
    private void handleCancel() {
        this.optionChosed = -1;
        setVisible(false);
        dispose();
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JButton jcmdCancel;
    private javax.swing.JButton jcmdMore;
    private javax.swing.JButton jcmdOK;
    private javax.swing.JLabel jlblIcon;
    private javax.swing.JLabel jlblMessage;
    private javax.swing.JScrollPane jscrException;
    private javax.swing.JTextArea jtxtException;
    // End of variables declaration//GEN-END:variables

}
