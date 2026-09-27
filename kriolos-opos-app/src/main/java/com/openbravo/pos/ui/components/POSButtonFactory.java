/*
 * Copyright (C) 2026 KriolOS
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
package com.openbravo.pos.ui.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class POSButtonFactory {

    /**
     * Creates a JButton configured for POS with native LTR/RTL support without FlatLaf.
     * 
     * @param size The desired size variant (Medium, Large, Extra_Large).
     * @return A customized native JButton.
     */
    public static JButton createButton(ButtonSize size) {
        JButton btn = new JButton();
        
        // 1. Logical Alignment: [Icon TEXT ......] compatible with LTR/RTL
        btn.applyComponentOrientation(btn.getComponentOrientation());
        btn.setHorizontalTextPosition(SwingConstants.TRAILING); 
        btn.setHorizontalAlignment(SwingConstants.LEADING);     

        // 2. Native Swing Sizing: Define height using minimum and preferred structures
        // We use a generic width of 100 as a baseline; layouts like JFlowPanel will respect the height
        Dimension baseSize = new Dimension(100, size.getHeight());
        btn.setPreferredSize(baseSize);
        btn.setMinimumSize(baseSize);

        // 3. Native Padding: Generous margins for touch-ready interactions using EmptyBorder
        btn.setBorder(BorderFactory.createCompoundBorder(
            btn.getBorder(), 
            new EmptyBorder(0, size.getPaddingX(), 0, size.getPaddingX())
        ));
        
        // 4. Native Spacing: Scaled gap between Icon and Text
        btn.setIconTextGap(size.getFontSize() / 4);

        // 5. Touch Accessibility Settings
        btn.setFocusPainted(false);
        btn.setFocusable(false);
        btn.setRequestFocusEnabled(false);

        // 6. Native Typography Scaling
        Font currentFont = btn.getFont();
        if (currentFont != null) {
            btn.setFont(new Font(currentFont.getName(), Font.BOLD, size.getFontSize()));
        }

        return btn;
    }
}
