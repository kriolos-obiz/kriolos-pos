/*
 * Copyright (C) 2022-2026 KriolOS
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
package com.openbravo.pos.forms.components;

import com.openbravo.basic.BasicException;
import com.openbravo.pos.forms.AppProperties;
import com.openbravo.pos.forms.AppProperties.DatabaseConfig;
import com.openbravo.pos.forms.AppUser;
import com.openbravo.pos.forms.SecurityService;
import java.awt.Component;
import java.awt.Container;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.JButton;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthenticationPanelTest {

    private DatabaseConfig db1;
    private DatabaseConfig db2;
    private TestAppProperties appProps;
    private TestSecurityService securityService;
    private AtomicBoolean authSuccess;
    private AuthenticationPanel authPanel;

    @BeforeEach
    void setUp() {
        db1 = new DatabaseConfig("Store DB", "jdbc:hsqldb:mem:test_db1", "sa", "");
        db2 = new DatabaseConfig("Branch DB", "jdbc:hsqldb:mem:test_db2", "sa", "");

        appProps = new TestAppProperties(Arrays.asList(db1, db2));
        securityService = new TestSecurityService();
        authSuccess = new AtomicBoolean(false);

        authPanel = new AuthenticationPanel(
                null,
                securityService,
                appProps,
                user -> authSuccess.set(true)
        );
    }

    @Test
    @DisplayName("Dirty mode disables user list when selected DB differs from active DB, and re-enables on revert")
    void testDirtyModeDisablesAndReEnablesUserList() {
        // 1. User selects and activates db1
        authPanel.getComboDatabases().setSelectedItem(db1);
        authPanel.setActiveDatabase(db1);

        assertFalse(authPanel.isDirtyMode(), "Should not be in dirty mode when selected == active");
        assertFalse(authPanel.isDirty(), "isDirty() alias should match isDirtyMode()");
        assertUserListButtonsEnabled(authPanel, true);

        // 2. User selects db2 (without activating it) -> dirtyMode
        authPanel.getComboDatabases().setSelectedItem(db2);

        assertTrue(authPanel.isDirtyMode(), "Should be in dirty mode when selectedDB != activeDB");
        assertTrue(authPanel.isDirty(), "isDirty() alias should be true");
        assertUserListButtonsEnabled(authPanel, false);

        // 3. User selects db1 again -> dirtyMode cleared, db1 users enabled
        authPanel.getComboDatabases().setSelectedItem(db1);

        assertFalse(authPanel.isDirtyMode(), "Should no longer be dirty after selecting active DB again");
        assertFalse(authPanel.isDirty(), "isDirty() alias should be false");
        assertUserListButtonsEnabled(authPanel, true);
    }

    @Test
    @DisplayName("User list is disabled when no database is active")
    void testUserListDisabledWhenNoActiveDatabase() {
        authPanel.setActiveDatabase(null);

        assertFalse(authPanel.isDirtyMode(), "Dirty mode requires an active DB to differ from");
        assertUserListButtonsEnabled(authPanel, false);
    }

    @Test
    @DisplayName("Login attempt via button action is ignored while in dirty mode")
    void testLoginIgnoredInDirtyMode() {
        // Activate db1
        authPanel.getComboDatabases().setSelectedItem(db1);
        authPanel.setActiveDatabase(db1);

        // Retrieve user button
        List<JButton> buttons = findUserButtons(authPanel);
        assertFalse(buttons.isEmpty(), "User buttons should exist");
        JButton userBtn = buttons.get(0);

        // Switch to db2 -> enters dirty mode
        authPanel.getComboDatabases().setSelectedItem(db2);
        assertTrue(authPanel.isDirtyMode());

        // Attempt click on the button while dirty
        userBtn.getAction().actionPerformed(null);
        assertFalse(authSuccess.get(), "Authentication callback must NOT be triggered in dirty mode");
    }

    private void assertUserListButtonsEnabled(AuthenticationPanel panel, boolean expected) {
        List<JButton> buttons = findUserButtons(panel);
        assertFalse(buttons.isEmpty(), "Expected user buttons in user list");
        for (JButton btn : buttons) {
            if (expected) {
                assertTrue(btn.isEnabled(), "Button '" + btn.getText() + "' should be enabled");
            } else {
                assertFalse(btn.isEnabled(), "Button '" + btn.getText() + "' should be disabled");
            }
        }
    }

    private List<JButton> findUserButtons(AuthenticationPanel panel) {
        List<JButton> buttons = new ArrayList<>();
        collectButtons(panel.getUsersListScrollPane().getViewport().getView(), buttons);
        return buttons;
    }

    private void collectButtons(Component comp, List<JButton> buttons) {
        if (comp == null) {
            return;
        }
        if (comp instanceof JButton btn) {
            buttons.add(btn);
        } else if (comp instanceof Container container) {
            for (Component child : container.getComponents()) {
                collectButtons(child, buttons);
            }
        }
    }

    private static class TestAppProperties implements AppProperties {
        private final List<DatabaseConfig> dbs;

        TestAppProperties(List<DatabaseConfig> dbs) {
            this.dbs = dbs;
        }

        @Override public File getConfigFile() { return null; }
        @Override public String getHost() { return "localhost"; }
        @Override public String getProperty(String sKey) { return null; }
        @Override public String getProperty(String sKey, String defaultValue) { return defaultValue; }
        @Override public List<DatabaseConfig> getAll() { return dbs; }
        @Override public DatabaseConfig getPrimary() { return dbs.isEmpty() ? null : dbs.get(0); }
    }

    private static class TestSecurityService implements SecurityService {
        private final List<AppUser> users = List.of(
                new AppUser("1", "Cashier 01", null, "111", "0", null),
                new AppUser("2", "Manager 01", null, "222", "0", null)
        );

        @Override
        public List<AppUser> listPeopleVisible() throws BasicException {
            return users;
        }

        @Override
        public AppUser findPeopleByCard(String card) throws BasicException {
            return users.stream().filter(u -> card.equals(u.getCard())).findFirst().orElse(null);
        }

        @Override
        public String findRolePermissions(String sRole) {
            return "";
        }

        @Override
        public List<String> getPermissions(String role) throws BasicException {
            return Collections.emptyList();
        }

        @Override
        public void execChangePassword(Object[] userdata) throws BasicException {}

        @Override
        public void execUpdatePermissions(Object[] permissions) throws BasicException {}
    }
}
