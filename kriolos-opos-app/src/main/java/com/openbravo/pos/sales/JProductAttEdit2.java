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

package com.openbravo.pos.sales;

import com.openbravo.basic.BasicException;
import com.openbravo.data.gui.modal.PosUIModal;
import com.openbravo.data.loader.Session;
import com.openbravo.pos.inventory.AttributeService;
import java.awt.Component;

/**
 * Backward-compatible adapter delegating to {@link JProductAttEditPanel}.
 *
 * @deprecated Use {@link JProductAttEditPanel} instead.
 */
@Deprecated
public class JProductAttEdit2 {

    private final Component parent;
    private final JProductAttEditPanel panel;

    private JProductAttEdit2(Component parent, Session s) {
        this.parent = parent;
        this.panel = new JProductAttEditPanel(s);
    }

    private JProductAttEdit2(Component parent, AttributeService attributeService) {
        this.parent = parent;
        this.panel = new JProductAttEditPanel(attributeService);
    }

    public static JProductAttEdit2 getAttributesEditor(Component parent, Session s) {
        return new JProductAttEdit2(parent, s);
    }

    public static JProductAttEdit2 getAttributesEditor(Component parent, AttributeService attributeService) {
        return new JProductAttEdit2(parent, attributeService);
    }

    public void editAttributes(String attsetid, String attsetinstid) throws BasicException {
        panel.editAttributes(attsetid, attsetinstid);
    }

    public void setVisible(boolean b) {
        if (b) {
            PosUIModal modal = PosUIModal.create(parent, panel)
                    .setTitle(panel.getTitle())
                    .setModal(true)
                    .setResizable(true);
            panel.setModalContext(modal);
            modal.show();
        }
    }

    public boolean isOK() {
        return panel.isOK();
    }

    public String getAttributeSetInst() {
        return panel.getAttributeSetInst();
    }

    public String getAttributeSetInstDescription() {
        return panel.getAttributeSetInstDescription();
    }
}
