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

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.JButton;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class POSButtonFactoryTest {

    @Test
    @DisplayName("createButton creates touch button with appropriate dimensions and sizing")
    void testCreateButton() {
        JButton btn = POSButtonFactory.createButton(ButtonSize.EXTRA_LARGE);
        assertNotNull(btn);
        assertEquals(48, btn.getPreferredSize().height);
        assertFalse(btn.isFocusable());
        assertFalse(btn.isFocusPainted());
    }

    @Test
    @DisplayName("createActionButton sets text, fontIcon, mnemonic, and annotated tooltip")
    void testCreateActionButton() {
        AtomicBoolean clicked = new AtomicBoolean(false);
        JButton btn = POSButtonFactory.createActionButton(
                "Remover",
                "\uD83D\uDDD1",
                ButtonSize.EXTRA_LARGE,
                KeyEvent.VK_R,
                "Remover item",
                e -> clicked.set(true)
        );

        assertNotNull(btn);
        assertTrue(btn.getText().contains("\uD83D\uDDD1"));
        assertTrue(btn.getText().contains("Remover"));
        assertEquals(KeyEvent.VK_R, btn.getMnemonic());
        assertNotNull(btn.getToolTipText());
        assertTrue(btn.getToolTipText().contains("Alt+R"));
        assertEquals(48, btn.getPreferredSize().height);

        btn.doClick();
        assertTrue(clicked.get());
    }

    @Test
    @DisplayName("createSuccessButton creates emerald green button with massive size")
    void testCreateSuccessButton() {
        JButton btn = POSButtonFactory.createSuccessButton(
                "PAGAR",
                "\uD83D\uDCB3",
                ButtonSize.MASSIVE,
                KeyEvent.VK_P,
                "Pagar pedido",
                null
        );

        assertNotNull(btn);
        assertEquals(56, btn.getPreferredSize().height);
        assertEquals(POSButtonFactory.SUCCESS_COLOR, btn.getBackground());
        assertEquals(Color.WHITE, btn.getForeground());
        assertTrue(btn.getText().contains("\uD83D\uDCB3"));
    }

    @Test
    @DisplayName("createDangerButton creates red button with appropriate color")
    void testCreateDangerButton() {
        JButton btn = POSButtonFactory.createDangerButton(
                "Fechar Caixa",
                "\uD83D\uDD12",
                ButtonSize.EXTRA_LARGE,
                KeyEvent.VK_F,
                "Fechar",
                null
        );

        assertNotNull(btn);
        assertEquals(POSButtonFactory.DANGER_COLOR, btn.getBackground());
        assertEquals(Color.WHITE, btn.getForeground());
    }

    @Test
    @DisplayName("formatButtonText handles null, empty, and non-empty glyphs")
    void testFormatButtonText() {
        assertEquals("\uD83D\uDC64  Cliente", POSButtonFactory.formatButtonText("Cliente", "\uD83D\uDC64"));
        assertEquals("Cliente", POSButtonFactory.formatButtonText("Cliente", (String) null));
        assertEquals("Cliente", POSButtonFactory.formatButtonText("Cliente", ""));
        assertEquals("\uD83D\uDC64", POSButtonFactory.formatButtonText(null, "\uD83D\uDC64"));
    }

    @Test
    @DisplayName("POSButtonFactory overloads work seamlessly with UnicodeIcon enum")
    void testUnicodeIconOverloads() {
        JButton actionBtn = POSButtonFactory.createActionButton(
                "Remover",
                UnicodeIcon.DELETE,
                ButtonSize.LARGE,
                KeyEvent.VK_R,
                "Remover",
                null
        );
        assertTrue(actionBtn.getText().contains(UnicodeIcon.DELETE.getCode()));

        JButton successBtn = POSButtonFactory.createSuccessButton(
                "Pagar",
                UnicodeIcon.PAY,
                ButtonSize.MASSIVE,
                KeyEvent.VK_P,
                "Pagar",
                null
        );
        assertTrue(successBtn.getText().contains(UnicodeIcon.PAY.getCode()));

        JButton dangerBtn = POSButtonFactory.createDangerButton(
                "Fechar",
                UnicodeIcon.CLOSE_CASH,
                ButtonSize.LARGE,
                KeyEvent.VK_F,
                "Fechar",
                null
        );
        assertTrue(dangerBtn.getText().contains(UnicodeIcon.CLOSE_CASH.getCode()));

        assertEquals(UnicodeIcon.CUSTOMER.getCode() + "  Cliente",
                POSButtonFactory.formatButtonText("Cliente", UnicodeIcon.CUSTOMER));
        assertEquals("Cliente",
                POSButtonFactory.formatButtonText("Cliente", (UnicodeIcon) null));
    }
}
