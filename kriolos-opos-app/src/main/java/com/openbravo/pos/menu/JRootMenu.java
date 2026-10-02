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
package com.openbravo.pos.menu;

import com.openbravo.data.gui.JMessagePanel;
import com.openbravo.data.gui.MessageInf;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.AppUser;
import com.openbravo.pos.forms.AppUserView;
import com.openbravo.pos.forms.DataLogicSystem;
import com.openbravo.pos.forms.JPanelView;
import com.openbravo.pos.forms.menu.Menu;
import com.openbravo.pos.scripting.ScriptEngine;
import com.openbravo.pos.scripting.ScriptException;
import com.openbravo.pos.scripting.ScriptFactory;
import com.openbravo.pos.util.StringUtils;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JScrollPane;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;

/**
 *
 * @author poolborges
 */
public class JRootMenu {

    private static final Logger LOGGER = Logger.getLogger("com.openbravo.pos.forms.JRootMenu");

    private final Component parent;
    private final AppUserView appview;
    private final ViewManager viewManager = new ViewManager();

    public JRootMenu(Component parent, AppUserView appview) {
        this.parent = parent;
        this.appview = appview;
    }

    public ViewManager getViewManager() {
        return viewManager;
    }

    public void setRootMenu(JScrollPane objJScrollPane, DataLogicSystem dlSystem) {
        Component menuComponent = null;
        
        LOGGER.log(Level.FINE, "Loading Root.Menu from database resource");
        try {
            String menuScrip = dlSystem.getResourceAsText("Menu.Root");
            menuComponent = getScriptMenu(menuScrip);

            if (menuComponent == null) {
                String pah = "/com/openbravo/pos/templates/Menu.Root.bs";
                LOGGER.log(Level.FINE, "Loading Root.Menu from classpath: " + pah);
                menuScrip = StringUtils.readResource(pah);
                menuComponent = getScriptMenu(menuScrip);
            }

            if (menuComponent != null) {
                objJScrollPane.setViewportView(menuComponent);
            } else {
                LOGGER.log(Level.SEVERE, "Failed on build Root.Menu");
            }
        }
        catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Exception on setup Root.Menu", ex);
        }
    }

    private Component getScriptMenu(String menutext) {

        Component menuComponent = null;

        if (menutext == null || menutext.isBlank()) {
            LOGGER.log(Level.SEVERE, "Script content is blank/emp");
            return menuComponent;
        }

        try {
            ScriptMenu menu = new ScriptMenu(parent, appview);

            ScriptEngine eng = ScriptFactory.getScriptEngine(ScriptFactory.BEANSHELL);
            eng.put("menu", menu);
            eng.eval(menutext);
            menuComponent = menu.getTaskPane();
        }
        catch (ScriptException ex) {
            LOGGER.log(Level.SEVERE, "Exception on eval Menu.Root ", ex);
            LOGGER.log(Level.WARNING, "Exception on eval Menu.Root ", menutext);
        }

        return menuComponent;
    }

    public class ScriptMenu implements Menu {

        private final JXTaskPaneContainer taskPane;
        private final Component parent;
        private final AppUserView appview;

        public ScriptMenu(Component parent, AppUserView appview) {
            this.parent = parent;
            this.appview = appview;
            this.taskPane = new JXTaskPaneContainer();
            this.taskPane.applyComponentOrientation(this.parent.getComponentOrientation());
        }

        @Override
        public ScriptMenuGroup addGroup(String key) {
            ScriptMenuGroup group = new ScriptMenuGroup(key, parent, appview);
            this.taskPane.add(group.getTaskGroup());
            return group;
        }

        public JXTaskPaneContainer getTaskPane() {
            return taskPane;
        }
    }

    public class ScriptMenuGroup implements Menu.MenuGroup {

        private final JXTaskPane taskGroup;
        private final AppUserView appUserView;
        private final Component parent;

        private ScriptMenuGroup(String key, Component parent, AppUserView appUserView) {
            this.appUserView = appUserView;
            this.parent = parent;
            this.taskGroup = new JXTaskPane();
            this.taskGroup.applyComponentOrientation(this.parent.getComponentOrientation());
            this.taskGroup.setFocusable(false);
            this.taskGroup.setRequestFocusEnabled(false);
            this.taskGroup.setTitle(AppLocal.getIntString(key));
            this.taskGroup.setVisible(false);
            this.taskGroup.setFont(new java.awt.Font("Arial", 0, 16));
        }

        @Override
        public void addPanel(String icon, String key, String classname) {
            addAction(new MenuPanelAction(this.appUserView, icon, key, classname));
        }

        @Override
        public void addExecution(String icon, String key, String classname) {
            addAction(new MenuExecAction(this.appUserView, icon, key, classname));
        }

        @Override
        public ScriptSubmenu addSubmenu(String icon, String key, String classname) {
            ScriptSubmenu submenu = new ScriptSubmenu(appUserView, key);
            viewManager.getPreparedViews().put(classname, new JPanelMenu(submenu.getMenuDefinition()));
            addAction(new MenuPanelAction(appUserView, icon, key, classname));
            return submenu;
        }

        private void addAction(Action act) {

            AppUser m_appuser = appUserView.getUser();
            if (m_appuser.hasPermission((String) act.getValue(AppUserView.ACTION_TASKNAME))) {
                Component c = taskGroup.add(act);
                c.applyComponentOrientation(parent.getComponentOrientation());
                c.setFocusable(false);

                taskGroup.setVisible(true);
                if (viewManager.getActionfirst() == null) {
                    viewManager.setActionfirst(act);
                }
            }
        }

        public JXTaskPane getTaskGroup() {
            return taskGroup;
        }

        @Override
        public void addChangePasswordAction() {
            
           addAction(new ChangePasswordAction(parent, appview,
                    "/com/openbravo/images/password.png", "Menu.ChangePassword"));
        }

        @Override
        public void addExitAction() {
            addAction(new ExitAction(parent, appview,
                    "/com/openbravo/images/logout.png", "Menu.Exit"));
        }
 
    }

    public class ScriptSubmenu implements Menu.Submenu {

        private final MenuDefinition menudef;
        private final AppUserView appUserView;

        private ScriptSubmenu(AppUserView appUserView, String key) {
            this.appUserView = appUserView;
            menudef = new MenuDefinition(key);
        }

        @Override
        public void addTitle(String key) {
            menudef.addMenuTitle(key);
        }

        @Override
        public void addPanel(String icon, String key, String classname) {
            menudef.addMenuItem(new MenuPanelAction(appUserView, icon, key, classname));
        }

        @Override
        public void addExecution(String icon, String key, String classname) {
            menudef.addMenuItem(new MenuExecAction(appUserView, icon, key, classname));
        }

        @Override
        public ScriptSubmenu addSubmenu(String icon, String key, String classname) {
            ScriptSubmenu submenu = new ScriptSubmenu(appUserView, key);
            viewManager.getPreparedViews().put(classname, new JPanelMenu(submenu.getMenuDefinition()));
            menudef.addMenuItem(new MenuPanelAction(appUserView, icon, key, classname));
            return submenu;
        }

        public MenuDefinition getMenuDefinition() {
            return menudef;
        }
    }

    private class ChangePasswordAction extends AbstractAction {

        private static final long serialVersionUID = 1L;

        private final Component parent;
        private final AppUserView appview;

        private ChangePasswordAction(Component parentP, AppUserView appviewP, String icon, String keytext) {
            parent = parentP;
            appview = appviewP;
            putValue(Action.SMALL_ICON, new ImageIcon(JRootMenu.class.getResource(icon)));
            putValue(Action.NAME, AppLocal.getIntString(keytext));
            putValue(AppUserView.ACTION_TASKNAME, keytext);

        }

        @Override
        public void actionPerformed(ActionEvent evt) {

            try {
                JMessagePanel.showMessage(parent,
                        new MessageInf(MessageInf.SGN_WARNING,
                                AppLocal.getIntString("message.cannotchangepassword")));
                /*TODO 
                AppUser m_appuser = appview.getUser();
                String sNewPassword = JPasswordPanel.changePassword(parent, m_appuser.getPassword());
 
                if (sNewPassword != null) {
                    DataLogicSystem m_dlSystem = (DataLogicSystem) appview.getBean("com.openbravo.pos.forms.DataLogicSystem");
                    m_dlSystem.execChangePassword(new Object[]{sNewPassword, m_appuser.getId()});
                    m_appuser.setPassword(sNewPassword);
                }
*/
            }
            catch (Exception ex) {
                JMessagePanel.showMessage(parent,
                        new MessageInf(MessageInf.SGN_WARNING,
                                AppLocal.getIntString("message.cannotchangepassword"), ex));
            }
        }
    }

    private class ExitAction extends AbstractAction {

        private static final long serialVersionUID = 1L;

        private final AppUserView m_appview;

        public ExitAction(Component _parent, AppUserView _appview, String icon, String keytext) {
            m_appview = _appview;
            putValue(Action.SMALL_ICON, new ImageIcon(JRootMenu.class.getResource(icon)));
            putValue(Action.NAME, AppLocal.getIntString(keytext));
            putValue(AppUserView.ACTION_TASKNAME, keytext);
        }

        @Override
        public void actionPerformed(ActionEvent evt) {
            appview.exitToLogin();
        }
    }

    public class ViewManager {

        private JPanelView m_jLastView;
        private Action m_actionfirst;

        private final Map<String, JPanelView> m_aPreparedViews; // Prepared views   
        private final Map<String, JPanelView> m_aCreatedViews;

        public ViewManager() {

            m_aPreparedViews = new HashMap<>();
            m_aCreatedViews = new HashMap<>();
            m_actionfirst = null;
            m_jLastView = null;
        }

        public JPanelView getLastView() {
            return m_jLastView;
        }

        public Action getActionfirst() {
            return m_actionfirst;
        }

        public void setActionfirst(Action actionfirst) {
            m_actionfirst = actionfirst;
        }

        public void resetActionfirst() {
            if (m_actionfirst != null) {
                m_actionfirst.actionPerformed(null);
                m_actionfirst = null;
            }
        }

        public boolean deactivateLastView() {
            if (m_jLastView == null) {
                return true;
            } else if (m_jLastView.deactivate()) {
                m_jLastView = null;
                return true;
            } else {
                return false;
            }
        }

        public boolean checkIfLastView(JPanelView m_jMyView) {
            return m_jMyView == m_jLastView;
        }

        public void setLastView(JPanelView m_jMyView) {
            m_jLastView = m_jMyView;
        }

        public Map<String, JPanelView> getPreparedViews() {
            return m_aPreparedViews;
        }

        public Map<String, JPanelView> getCreatedViews() {
            return m_aCreatedViews;
        }
    }
}
