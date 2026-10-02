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
import com.openbravo.editor.JEditorKeys;
import com.openbravo.editor.JEditorString;
import com.openbravo.editor.JEditorText;
import java.awt.Component;
import javax.swing.Icon;
import javax.swing.JPanel;

/**
 * Text editor panel for input entry, presentable via {@link PosUIModal} or embedded directly into views.
 *
 * @author KriolOS
 */
public class JEditorTextPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private static LocaleResources m_resources;

    private final JEditorKeys m_jKeyPad = new JEditorKeys();
    private JEditorText m_jtextEditor = new JEditorString();
    private PosUIModal modalContext;
    private boolean accepted = false;

    /**
     * Creates new form JEditorTextPanel
     */
    public JEditorTextPanel() {
        initComponents();
        init();
        initDomainAdapters();
    }

    private void initDomainAdapters() {
        setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8));

        setName("kriolos:editor:text-panel");
        m_jKeyPad.setName("kriolos:editor:keypad");
        m_jtextEditor.setName("kriolos:editor:input-text");
        jcmdOK.setName("kriolos:editor:btn-ok");
        jcmdCancel.setName("kriolos:editor:btn-cancel");
        m_lblMessage.setName("kriolos:editor:lbl-message");
    }

    private void init() {
        jPanelKe.add(m_jKeyPad);
        jPanelInput.add(m_jtextEditor);

        if (m_resources == null) {
            m_resources = new LocaleResources();
            m_resources.addBundleName("beans_messages");
        }

        m_jtextEditor.addEditorKeys(m_jKeyPad);
        m_jtextEditor.reset();
        m_jtextEditor.activate();

        m_jPanelTitle.setBorder(RoundedBorder.createGradientBorder());
    }

    /**
     * Attaches the controlling {@link PosUIModal} context.
     *
     * @param modalContext the controlling modal context
     */
    public void setModalContext(PosUIModal modalContext) {
        this.modalContext = modalContext;
    }

    /**
     * Sets the header message and icon.
     *
     * @param message the message text
     * @param icon the message icon
     */
    public void setMessage(String message, Icon icon) {
        m_lblMessage.setText(message);
        m_lblMessage.setIcon(icon);
    }

    /**
     * Replaces the active editor component.
     *
     * @param newTextEditor the new text editor
     */
    public void setEditor(JEditorText newTextEditor) {
        jPanelInput.removeAll();
        m_jtextEditor = newTextEditor;
        jPanelInput.add(m_jtextEditor);
        m_jtextEditor.addEditorKeys(m_jKeyPad);
        m_jtextEditor.reset();
        m_jtextEditor.activate();
        revalidate();
    }

    /**
     * Retrieves the text entered in the editor.
     *
     * @return the entered text
     */
    public String getValue() {
        return m_jtextEditor.getText();
    }

    /**
     * Returns whether the panel entry was confirmed by the user.
     *
     * @return true if accepted, false if canceled
     */
    public boolean isAccepted() {
        return accepted;
    }

    private void handleAccept() {
        this.accepted = true;
        if (modalContext != null) {
            modalContext.setResult(getValue());
            modalContext.close();
        }
    }

    private void handleCancel() {
        this.accepted = false;
        if (modalContext != null) {
            modalContext.close();
        }
    }

    /**
     * Factory presentation method using {@link PosUIModal}.
     *
     * @param parent the parent component or window
     * @param title the modal window/overlay title
     * @param message header message
     * @param icon header icon
     * @return the entered string, or {@code null} if canceled
     */
    public static String show(Component parent, String title, String message, Icon icon) {
        JEditorTextPanel panel = new JEditorTextPanel();
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

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jcmdCancel = new javax.swing.JButton();
        jcmdOK = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jPanelGrid = new javax.swing.JPanel();
        jPanelKe = new javax.swing.JPanel();
        jPanelInput = new javax.swing.JPanel();
        m_jPanelTitle = new javax.swing.JPanel();
        m_lblMessage = new javax.swing.JLabel();

        // Root size intentionally omitted: PosUIModal calls pack() which computes
        // natural size from children. Fixed root sizes are fragile against padding changes.
        setLayout(new java.awt.BorderLayout());

        jPanel1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        jcmdCancel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jcmdCancel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/cancel.png"))); // NOI18N
        jcmdCancel.setMargin(new java.awt.Insets(8, 16, 8, 16));
        jcmdCancel.setPreferredSize(new java.awt.Dimension(80, 45));
        jcmdCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jcmdCancelActionPerformed(evt);
            }
        });
        jPanel1.add(jcmdCancel);

        jcmdOK.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jcmdOK.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/ok.png"))); // NOI18N
        jcmdOK.setMargin(new java.awt.Insets(8, 16, 8, 16));
        jcmdOK.setPreferredSize(new java.awt.Dimension(80, 45));
        jcmdOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jcmdOKActionPerformed(evt);
            }
        });
        jPanel1.add(jcmdOK);

        add(jPanel1, java.awt.BorderLayout.SOUTH);

        jPanel2.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jPanel2.setMaximumSize(new java.awt.Dimension(320, 365));
        jPanel2.setPreferredSize(new java.awt.Dimension(320, 365));
        jPanel2.setLayout(new java.awt.BorderLayout());

        jPanelGrid.setPreferredSize(new java.awt.Dimension(310, 360));

        jPanelKe.setPreferredSize(new java.awt.Dimension(300, 300));
        jPanelKe.setLayout(new javax.swing.BoxLayout(jPanelKe, javax.swing.BoxLayout.Y_AXIS));
        jPanelGrid.add(jPanelKe);

        jPanelInput.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jPanelInput.setPreferredSize(new java.awt.Dimension(300, 50));
        jPanelInput.setLayout(new java.awt.BorderLayout());
        jPanelGrid.add(jPanelInput);

        jPanel2.add(jPanelGrid, java.awt.BorderLayout.CENTER);

        add(jPanel2, java.awt.BorderLayout.CENTER);

        m_jPanelTitle.setMaximumSize(new java.awt.Dimension(310, 35));
        m_jPanelTitle.setMinimumSize(new java.awt.Dimension(300, 35));
        m_jPanelTitle.setPreferredSize(new java.awt.Dimension(310, 35));
        m_jPanelTitle.setLayout(new java.awt.BorderLayout());

        m_lblMessage.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, java.awt.Color.darkGray), javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        m_lblMessage.setMaximumSize(new java.awt.Dimension(300, 30));
        m_lblMessage.setMinimumSize(new java.awt.Dimension(300, 30));
        m_lblMessage.setPreferredSize(new java.awt.Dimension(300, 30));
        m_jPanelTitle.add(m_lblMessage, java.awt.BorderLayout.CENTER);

        add(m_jPanelTitle, java.awt.BorderLayout.NORTH);
    }// </editor-fold>//GEN-END:initComponents

    private void jcmdOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jcmdOKActionPerformed
        handleAccept();
    }//GEN-LAST:event_jcmdOKActionPerformed

    private void jcmdCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jcmdCancelActionPerformed
        handleCancel();
    }//GEN-LAST:event_jcmdCancelActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanelGrid;
    private javax.swing.JPanel jPanelInput;
    private javax.swing.JPanel jPanelKe;
    private javax.swing.JButton jcmdCancel;
    private javax.swing.JButton jcmdOK;
    private javax.swing.JPanel m_jPanelTitle;
    private javax.swing.JLabel m_lblMessage;
    // End of variables declaration//GEN-END:variables
}
