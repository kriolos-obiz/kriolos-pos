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

import com.openbravo.pos.forms.AppLocal;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;

/**
 * Vertical action toolbar beside the ticket lines table for line item actions
 * (Delete Line, Search/Find Product, Edit Line, Edit Attributes, Check Stock).
 * Designed in pure Java Swing with zero NetBeans form dependencies.
 */
public class TicketToolbarPane extends JPanel {

    private final JButton btnDelete;
    private final JButton btnList;
    private final JButton btnEditLine;
    private final JButton btnEditAttributes;
    private final JButton btnCheckStock;

    public TicketToolbarPane() {
        super(new BorderLayout());
        setOpaque(false);
        setPreferredSize(new Dimension(75, 270));

        JPanel container = new JPanel(new GridLayout(0, 1, 5, 5));
        container.setOpaque(false);
        container.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        container.setPreferredSize(new Dimension(70, 250));

        btnDelete = createButton("/com/openbravo/images/editdelete.png", "tooltip.saleremoveline");
        container.add(btnDelete);

        btnList = createButton("/com/openbravo/images/search32.png", "tooltip.saleproductfind");
        container.add(btnList);

        btnEditLine = createButton("/com/openbravo/images/sale_editline.png", "tooltip.saleeditline");
        container.add(btnEditLine);

        btnEditAttributes = createButton("/com/openbravo/images/attributes.png", "tooltip.saleattributes");
        container.add(btnEditAttributes);

        btnCheckStock = createButton("/com/openbravo/images/info.png", "tooltip.salecheckstock");
        container.add(btnCheckStock);

        add(container, BorderLayout.NORTH);
    }

    private JButton createButton(String iconPath, String tooltipKey) {
        JButton btn = new JButton();
        try {
            java.net.URL res = getClass().getResource(iconPath);
            if (res != null) {
                btn.setIcon(new ImageIcon(res));
            }
        } catch (Exception ignored) {
        }
        btn.setToolTipText(AppLocal.getIntString(tooltipKey));
        btn.setFocusPainted(false);
        btn.setFocusable(false);
        btn.setRequestFocusEnabled(false);
        btn.setMargin(new Insets(8, 4, 8, 4));
        btn.setPreferredSize(new Dimension(50, 45));
        btn.setMinimumSize(new Dimension(42, 36));
        btn.setMaximumSize(new Dimension(80, 45));
        return btn;
    }

    public void setOnDeleteLine(Runnable action) {
        btnDelete.addActionListener(e -> {
            if (action != null) {
                action.run();
            }
        });
    }

    public void setOnFindProduct(Runnable action) {
        btnList.addActionListener(e -> {
            if (action != null) {
                action.run();
            }
        });
    }

    public void setOnEditLine(Runnable action) {
        btnEditLine.addActionListener(e -> {
            if (action != null) {
                action.run();
            }
        });
    }

    public void setOnEditAttributes(Runnable action) {
        btnEditAttributes.addActionListener(e -> {
            if (action != null) {
                action.run();
            }
        });
    }

    public void setOnCheckStock(Runnable action) {
        btnCheckStock.addActionListener(e -> {
            if (action != null) {
                action.run();
            }
        });
    }

    public void setEditLineVisible(boolean visible) {
        btnEditLine.setVisible(visible);
    }

    public void setFindProductVisible(boolean visible) {
        btnList.setVisible(visible);
    }

    public void triggerEditLine() {
        btnEditLine.doClick();
    }

    public void setDeleteLineEnabled(boolean enabled) {
        btnDelete.setEnabled(enabled);
    }

    public void setCheckStockText(String text) {
        btnCheckStock.setText(text);
    }

    public void setStockAvailable(boolean hasStock) {
        if (!hasStock) {
            java.awt.Color errorColor = javax.swing.UIManager.getColor("Component.error.focusedBorderColor");
            if (errorColor == null) {
                errorColor = javax.swing.UIManager.getColor("nb.errorForeground");
            }
            btnCheckStock.setForeground(errorColor != null ? errorColor : javax.swing.UIManager.getColor("Button.foreground"));
        } else {
            btnCheckStock.setForeground(javax.swing.UIManager.getColor("Button.foreground"));
        }
    }

    public JButton getBtnDelete() {
        return btnDelete;
    }

    public JButton getBtnList() {
        return btnList;
    }

    public JButton getBtnEditLine() {
        return btnEditLine;
    }

    public JButton getBtnEditAttributes() {
        return btnEditAttributes;
    }

    public JButton getBtnCheckStock() {
        return btnCheckStock;
    }
}
