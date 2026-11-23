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

package com.openbravo.pos.admin;

import com.openbravo.basic.AppLocal;
import com.openbravo.basic.BasicException;
import com.openbravo.data.user.DirtyManager;
import com.openbravo.data.user.EditorRecord;
import com.openbravo.format.Formats;
import com.openbravo.pos.panels.AppView;
import com.openbravo.ui.data.binding.SwingBinder;
import java.awt.Component;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import javax.swing.JTextField;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

/**
 * Strongly typed editor component for Role security profiles and XML permissions.
 *
 * @author adrianromero
 * @author KriolOS
 */
public final class RolesView extends javax.swing.JPanel implements EditorRecord<RoleInfo> {

    private static final long serialVersionUID = 1L;
    private static final System.Logger LOGGER = System.getLogger(RolesView.class.getName());

    private String m_oId;
    private DirtyManager dirty;
    private SwingBinder<RoleInfo> binder;

    private final RSyntaxTextArea m_RSyntaxTextArea;
    private final RTextScrollPane m_RTextScrollPane;

    /**
     * Headless constructor for testing without active AppView session.
     *
     * @param dirty dirty state manager
     */
    public RolesView(DirtyManager dirty) {
        this((AppView) null, dirty);
    }

    /**
     * Full constructor with optional AppView context.
     *
     * @param app application view context
     * @param dirty dirty state manager
     */
    public RolesView(AppView app, DirtyManager dirty) {
        this.dirty = dirty;
        m_RSyntaxTextArea = new RSyntaxTextArea(20, 60);
        m_RSyntaxTextArea.setCodeFoldingEnabled(true);
        if (dirty != null) {
            m_RSyntaxTextArea.getDocument().addDocumentListener(dirty);
        }
        m_RSyntaxTextArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_XML);
        m_RTextScrollPane = new RTextScrollPane(m_RSyntaxTextArea);

        initComponents();

        if (dirty != null) {
            m_jName.getDocument().addDocumentListener(dirty);
        }

        initValidator();
        writeValueEOF();
    }

    private void initValidator() {
        binder = new SwingBinder<>(new RoleInfo(null, null, null));

        // Mandatory Role Name
        binder.forField(m_jName)
                .asRequired(AppLocal.getIntString("validation.required", AppLocal.getIntString("label.name")))
                .bind(RoleInfo::getName, RoleInfo::setName);

        // Two-way binding for XML permissions text area
        binder.forField(m_RSyntaxTextArea)
                .bind(
                        role -> role != null && role.getPermissions() != null
                                ? Formats.BYTEA.formatValue(role.getPermissions())
                                : "",
                        (role, text) -> {
                            if (role != null) {
                                byte[] bytes = null;
                                if (text != null && !text.trim().isEmpty()) {
                                    try {
                                        bytes = (byte[]) Formats.BYTEA.parseValue(text);
                                    } catch (BasicException ex) {
                                        bytes = text.getBytes(StandardCharsets.UTF_8);
                                    }
                                }
                                role.setPermissions(bytes);
                            }
                        }
                );

        binder.addStatusChangeListener(() -> {
            if (dirty != null) {
                dirty.setDirty(true);
            }
        });
    }

    public boolean isValid() {
        return binder == null || binder.validate();
    }

    public boolean isDataValid() {
        return isValid();
    }

    public SwingBinder<RoleInfo> getBinder() {
        return binder;
    }

    JTextField getNameField() {
        return m_jName;
    }

    public RSyntaxTextArea getSyntaxTextArea() {
        return m_RSyntaxTextArea;
    }

    @Override
    public void writeValueEOF() {
        m_oId = null;
        if (binder != null) {
            binder.clear();
        } else {
            m_jName.setText(null);
            m_RSyntaxTextArea.setText(null);
        }
        m_jName.setEnabled(false);
        m_RSyntaxTextArea.setEnabled(false);
    }

    @Override
    public void writeValueInsert() {
        m_oId = UUID.randomUUID().toString();
        if (binder != null) {
            binder.clear();
        } else {
            m_jName.setText(null);
            m_RSyntaxTextArea.setText(null);
        }
        m_jName.setEnabled(true);
        m_RSyntaxTextArea.setEnabled(true);
    }

    @Override
    public void writeValueDelete(RoleInfo role) {
        if (role == null) {
            writeValueEOF();
            return;
        }
        m_oId = role.getID();
        if (binder != null) {
            binder.readBean(role);
        } else {
            m_jName.setText(Formats.STRING.formatValue(role.getName()));
            m_RSyntaxTextArea.setText(Formats.BYTEA.formatValue(role.getPermissions()));
        }
        m_RSyntaxTextArea.setCaretPosition(0);
        m_jName.setEnabled(false);
        m_RSyntaxTextArea.setEnabled(false);
    }

    @Override
    public void writeValueEdit(RoleInfo role) {
        if (role == null) {
            writeValueEOF();
            return;
        }
        m_oId = role.getID();
        if (binder != null) {
            binder.readBean(role);
        } else {
            m_jName.setText(Formats.STRING.formatValue(role.getName()));
            m_RSyntaxTextArea.setText(Formats.BYTEA.formatValue(role.getPermissions()));
        }
        m_RSyntaxTextArea.setCaretPosition(0);
        m_jName.setEnabled(true);
        m_RSyntaxTextArea.setEnabled(true);
    }

    @Override
    public RoleInfo createValue() throws BasicException {
        RoleInfo role = new RoleInfo();
        role.setID(m_oId == null ? UUID.randomUUID().toString() : m_oId);
        role.setName(m_jName.getText() != null ? m_jName.getText() : "");
        byte[] permissions = null;
        String text = m_RSyntaxTextArea.getText();
        if (text != null && !text.trim().isEmpty()) {
            try {
                permissions = (byte[]) Formats.BYTEA.parseValue(text);
            } catch (BasicException ex) {
                permissions = text.getBytes(StandardCharsets.UTF_8);
            }
        }
        role.setPermissions(permissions);

        if (binder != null) {
            try {
                binder.writeBean(role);
            } catch (SwingBinder.ValidationException ex) {
                LOGGER.log(System.Logger.Level.WARNING, "Validation in createValue: " + ex.getMessage());
            }
        }
        if (role.getName() == null) {
            role.setName(m_jName.getText() != null ? m_jName.getText() : "");
        }
        return role;
    }

    @Override
    public Component getComponent() {
        return this;
    }

    @Override
    public void refresh() {
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        jLabel2 = new javax.swing.JLabel();
        m_jName = new javax.swing.JTextField();

        jLabel2.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jLabel2.setText(AppLocal.getIntString("label.namem")); // NOI18N

        m_jName.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(m_RTextScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 477, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(m_jName, javax.swing.GroupLayout.PREFERRED_SIZE, 260, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_jName, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(m_RTextScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 312, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel2;
    private javax.swing.JTextField m_jName;
    // End of variables declaration//GEN-END:variables

}
