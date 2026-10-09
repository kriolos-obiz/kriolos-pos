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

package com.openbravo.pos.epm;

import com.openbravo.pos.resources.ImageResources;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

/**
 * Modern touch-friendly list cell renderer for Employee items.
 *
 * @author Ali Safdar and Aneeqa Baber, KriolOS Team
 */
public class EmployeeRenderer extends JPanel implements ListCellRenderer<Object> {

    private static final long serialVersionUID = 1L;
    private static final int ICON_SIZE = 32;

    private transient final Icon icoEmployee;
    private final JLabel lblIcon = new JLabel();
    private final JLabel lblName = new JLabel();
    private final JLabel lblSubtitle = new JLabel();
    private final JPanel pnlCenter = new JPanel();

    public EmployeeRenderer() {
        super(new BorderLayout(10, 0));
        setOpaque(true);

        icoEmployee = ImageResources.ICON_USER.getIcon(ICON_SIZE, ICON_SIZE);

        lblIcon.setPreferredSize(new Dimension(ICON_SIZE, ICON_SIZE));
        lblIcon.setMinimumSize(new Dimension(ICON_SIZE, ICON_SIZE));
        lblIcon.setHorizontalAlignment(SwingConstants.CENTER);
        lblIcon.setVerticalAlignment(SwingConstants.CENTER);
        lblIcon.setIcon(icoEmployee);

        pnlCenter.setLayout(new GridLayout(2, 1, 0, 2));
        pnlCenter.setOpaque(false);

        pnlCenter.add(lblName);
        pnlCenter.add(lblSubtitle);

        add(lblIcon, BorderLayout.LINE_START);
        add(pnlCenter, BorderLayout.CENTER);
    }

    @Override
    public Component getListCellRendererComponent(
            JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

        applyComponentOrientation(list.getComponentOrientation());

        Font baseFont = list.getFont() != null ? list.getFont() : new Font("Arial", Font.PLAIN, 12);
        lblName.setFont(baseFont.deriveFont(Font.BOLD, 13f));
        lblSubtitle.setFont(baseFont.deriveFont(Font.PLAIN, 11f));

        if (value instanceof EmployeeInfo emp) {
            lblName.setText(emp.getName() != null ? emp.getName() : "");
            lblSubtitle.setText(emp.getId() != null ? "ID: " + emp.getId() : "");
            setToolTipText(emp.getName());
        } else {
            lblName.setText(value != null ? value.toString() : "");
            lblSubtitle.setText("");
            setToolTipText(null);
        }

        if (isSelected) {
            setBackground(list.getSelectionBackground());
            lblName.setForeground(list.getSelectionForeground());
            lblSubtitle.setForeground(list.getSelectionForeground());
        } else {
            Color alt = UIManager.getColor("List.alternateRowColor");
            if (alt == null) {
                alt = UIManager.getColor("Table.alternateRowColor");
            }
            Color bg = (index % 2 == 1 && alt != null) ? alt : list.getBackground();
            setBackground(bg != null ? bg : Color.WHITE);

            lblName.setForeground(list.getForeground());

            Color muted = UIManager.getColor("Label.disabledForeground");
            lblSubtitle.setForeground(muted != null ? muted : new Color(128, 128, 128));
        }

        Color sepColor = UIManager.getColor("Component.borderColor");
        if (sepColor == null) {
            sepColor = UIManager.getColor("Separator.foreground");
        }
        if (sepColor == null) {
            sepColor = new Color(225, 225, 225);
        }
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, sepColor),
                BorderFactory.createEmptyBorder(6, 10, 6, 12)
        ));

        return this;
    }
}
