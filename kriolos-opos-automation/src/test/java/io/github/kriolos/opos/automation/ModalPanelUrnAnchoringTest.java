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
package io.github.kriolos.opos.automation;

import com.openbravo.beans.DinerNumberPanel;
import com.openbravo.beans.JCalendarDlgPanel;
import com.openbravo.beans.JDoublePanel;
import com.openbravo.beans.JEditorTextPanel;
import com.openbravo.beans.JIntegerPanel;
import com.openbravo.beans.JPasswordPanel;
import com.openbravo.data.gui.JFindPanel;
import com.openbravo.data.gui.JMessagePanel;
import com.openbravo.data.gui.JSortPanel;
import java.awt.Component;
import java.awt.Container;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Validates Rule 7.2 (Component Identification — setName() URN Anchoring)
 * across all refactored modal panel components.
 */
class ModalPanelUrnAnchoringTest {

    private Component findByName(Container container, String name) {
        for (Component c : container.getComponents()) {
            if (name.equals(c.getName())) {
                return c;
            }
            if (c instanceof Container) {
                Component found = findByName((Container) c, name);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    @Test
    @DisplayName("Rule 7.2: JEditorTextPanel and components expose valid kriolos:editor:* URNs")
    void testEditorTextPanelUrnAnchoring() {
        JEditorTextPanel panel = new JEditorTextPanel();
        assertEquals("kriolos:editor:text-panel", panel.getName());
        assertNotNull(findByName(panel, "kriolos:editor:btn-ok"));
        assertNotNull(findByName(panel, "kriolos:editor:btn-cancel"));
        assertNotNull(findByName(panel, "kriolos:editor:keypad"));
    }

    @Test
    @DisplayName("Rule 7.2: JPasswordPanel exposes valid kriolos:auth:password-panel URN")
    void testPasswordPanelUrnAnchoring() {
        JPasswordPanel panel = new JPasswordPanel();
        assertEquals("kriolos:auth:password-panel", panel.getName());
        assertNotNull(findByName(panel, "kriolos:editor:btn-ok"));
    }

    @Test
    @DisplayName("Rule 7.2: JCalendarDlgPanel exposes valid kriolos:calendar:* URNs")
    void testCalendarDlgPanelUrnAnchoring() {
        JCalendarDlgPanel panel = new JCalendarDlgPanel();
        assertEquals("kriolos:calendar:dialog-panel", panel.getName());
        assertNotNull(panel.getOkButton());
        assertEquals("kriolos:calendar:btn-ok", panel.getOkButton().getName());
        assertNotNull(findByName(panel, "kriolos:calendar:btn-cancel"));
    }

    @Test
    @DisplayName("Rule 7.2: JIntegerPanel and JDoublePanel expose valid kriolos:editor:number-* URNs")
    void testNumberPanelsUrnAnchoring() {
        JIntegerPanel intPanel = new JIntegerPanel();
        assertEquals("kriolos:editor:number-panel", intPanel.getName());
        assertNotNull(findByName(intPanel, "kriolos:editor:number-btn-ok"));
        assertNotNull(findByName(intPanel, "kriolos:editor:number-btn-cancel"));

        JDoublePanel dblPanel = new JDoublePanel();
        assertEquals("kriolos:editor:number-panel", dblPanel.getName());
        assertNotNull(findByName(dblPanel, "kriolos:editor:number-btn-ok"));
    }

    @Test
    @DisplayName("Rule 7.2: DinerNumberPanel exposes valid kriolos:dining:guest-count-* URNs")
    void testDinerNumberPanelUrnAnchoring() {
        DinerNumberPanel panel = new DinerNumberPanel();
        assertEquals("kriolos:dining:guest-count-panel", panel.getName());
        assertNotNull(findByName(panel, "kriolos:dining:guest-count-btn-ok"));
        assertNotNull(findByName(panel, "kriolos:dining:guest-count-keypad"));
    }

    @Test
    @DisplayName("Rule 7.2: JFindPanel exposes valid kriolos:find:* URNs")
    void testFindPanelUrnAnchoring() {
        JFindPanel<Object> panel = new JFindPanel<>();
        assertEquals("kriolos:find:search-panel", panel.getName());
        assertNotNull(panel.getOkButton());
        assertEquals("kriolos:find:btn-ok", panel.getOkButton().getName());
        assertNotNull(findByName(panel, "kriolos:find:btn-cancel"));
    }

    @Test
    @DisplayName("Rule 7.2: JSortPanel exposes valid kriolos:sort:* URNs")
    void testSortPanelUrnAnchoring() {
        JSortPanel<Object> panel = new JSortPanel<>();
        assertEquals("kriolos:sort:ordering-panel", panel.getName());
        assertNotNull(panel.getOkButton());
        assertEquals("kriolos:sort:btn-ok", panel.getOkButton().getName());
        assertNotNull(findByName(panel, "kriolos:sort:btn-cancel"));
    }

    @Test
    @DisplayName("Rule 7.2: JMessagePanel exposes valid kriolos:message:* URNs")
    void testMessagePanelUrnAnchoring() {
        JMessagePanel panel = new JMessagePanel();
        assertEquals("kriolos:message:panel", panel.getName());
        assertNotNull(panel.getOkButton());
        assertEquals("kriolos:message:btn-ok", panel.getOkButton().getName());
        assertNotNull(findByName(panel, "kriolos:message:btn-cancel"));
        assertNotNull(findByName(panel, "kriolos:message:btn-more"));
    }

    @Test
    @DisplayName("Rule 7.2: AuthenticationPanel exposes valid kriolos:auth:* URNs")
    void testAuthenticationPanelUrnAnchoring() {
        com.openbravo.pos.forms.components.AuthenticationPanel panel =
                new com.openbravo.pos.forms.components.AuthenticationPanel(null, null, null, null);
        assertEquals("kriolos:auth:panel", panel.getName());
        assertNotNull(findByName(panel, "kriolos:auth:active-database-badge"));
        assertNotNull(findByName(panel, "kriolos:auth:combo-databases"));
        assertNotNull(findByName(panel, "kriolos:auth:btn-select-database"));
        assertNotNull(findByName(panel, "kriolos:auth:btn-configure-database"));
        assertNotNull(findByName(panel, "kriolos:auth:progress-bar"));
        assertNotNull(findByName(panel, "kriolos:auth:status-label"));
        assertNotNull(findByName(panel, "kriolos:auth:lbl-login-header"));
        assertNotNull(findByName(panel, "kriolos:auth:lbl-vendor-image"));
        assertNotNull(findByName(panel, "kriolos:auth:scroll-users"));
        assertNotNull(findByName(panel, "kriolos:auth:txt-barcode-keys"));
    }
}
