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
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.AppProperties;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.JPanelView;
import com.openbravo.pos.ui.api.sales.SaleLayoutManager;
import java.awt.BorderLayout;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * Top-level Sales View Bean for KriolOS POS.
 * <p>
 * Implements {@link JPanelView} and acts as a dynamic router and host:
 * <ul>
 *   <li>Routes to {@link JPanelTicketSalesClassic} for legacy layouts (simple, standard, restaurant).</li>
 *   <li>Routes to Modern layouts (e.g. ModernOne, ModernTwo) via SPI {@link SaleLayoutManager}.</li>
 * </ul>
 * Features:
 * <ul>
 *   <li><b>Lazy layout initialization:</b> Construction is lightweight; layout and heavy components
 *       are resolved when the view is first activated.</li>
 *   <li><b>Dynamic reconfiguration:</b> Automatically detects when layout configuration changes in Settings
 *       and recreates the appropriate view on subsequent activations.</li>
 *   <li><b>Fault tolerance:</b> Gracefully falls back to Classic layout if a Modern layout fails to load.</li>
 * </ul>
 *
 * @author JG uniCenta, KriolOS Team
 */
public class JPanelTicketSales extends JPanel implements JPanelView {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(JPanelTicketSales.class.getName());

    private final AppView app;
    private JPanelView currentView;
    private String activeLayoutMode;

    /**
     * Constructs a lightweight Sales View container.
     * <p>
     * Layout resolution is deferred until {@link #activate()} to guarantee fast bean instantiation
     * and prevent premature dependency resolution.
     *
     * @param app the application view context; must not be null
     */
    public JPanelTicketSales(AppView app) {
        this.app = Objects.requireNonNull(app, "Parameter 'app' cannot be null");
        setLayout(new BorderLayout());
    }

    /**
     * Resolves the configured sales layout mode and installs the appropriate view component.
     * <p>
     * If the layout mode has not changed since the last installation, this method returns immediately.
     * If modern layout resolution fails, it automatically falls back to {@link JPanelTicketSalesClassic}.
     */
    public synchronized void resolveAndInstallLayout() {
        String layoutMode = "standard";
        AppProperties props = app.getProperties();
        if (props != null) {
            String prop = props.getProperty("machine.ticketsbag");
            if (prop != null && !prop.isBlank()) {
                layoutMode = prop.trim();
            }
        }

        // Avoid re-creating if layout mode hasn't changed
        if (currentView != null && layoutMode.equalsIgnoreCase(activeLayoutMode)) {
            return;
        }

        // Deactivate and clean previous view if active
        if (currentView != null) {
            try {
                currentView.deactivate();
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, "Error deactivating previous sales layout view", ex);
            }
            removeAll();
        }

        activeLayoutMode = layoutMode;

        if (SaleLayoutManager.isFullView(layoutMode)) {
            LOGGER.log(Level.INFO, "Installing Modern Sales Layout: {0}", layoutMode);
            try {
                JComponent modernComp = SaleLayoutManager.createLayout(layoutMode, app, null);
                if (modernComp instanceof JPanelView modernView) {
                    currentView = modernView;
                } else if (modernComp != null) {
                    currentView = new ModernViewAdapter(modernComp);
                } else {
                    throw new IllegalStateException("Modern layout provider returned null component for mode: " + layoutMode);
                }
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Failed to instantiate modern sales layout ''{0}'', falling back to Classic layout", layoutMode);
                currentView = new JPanelTicketSalesClassic(app);
            }
        } else {
            LOGGER.log(Level.INFO, "Installing Classic Sales Layout: {0}", layoutMode);
            currentView = new JPanelTicketSalesClassic(app);
        }

        add(currentView.getComponent(), BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    @Override
    public JComponent getComponent() {
        return this;
    }

    @Override
    public String getTitle() {
        if (currentView != null) {
            String title = currentView.getTitle();
            if (title != null && !title.isBlank()) {
                return title;
            }
        }
        return AppLocal.getIntString("Menu.Ticket");
    }

    @Override
    public void activate() throws BasicException {
        // Re-check or lazily initialize on first activation
        resolveAndInstallLayout();
        if (currentView != null) {
            currentView.activate();
        }
    }

    @Override
    public boolean deactivate() {
        if (currentView != null) {
            try {
                return currentView.deactivate();
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, "Error during sales view deactivation", ex);
                return true;
            }
        }
        return true;
    }

    /**
     * Returns the currently active nested sales layout view, lazily resolving it if not yet created.
     *
     * @return the active {@link JPanelView} delegate
     */
    public synchronized JPanelView getCurrentView() {
        if (currentView == null) {
            resolveAndInstallLayout();
        }
        return currentView;
    }

    /**
     * Returns the currently active layout mode key (e.g. "standard", "simple", "restaurant", "modern-1").
     *
     * @return active layout mode name or null if not yet resolved
     */
    public String getActiveLayoutMode() {
        return activeLayoutMode;
    }

    /**
     * Fallback adapter in case a custom full-view layout does not directly implement JPanelView.
     */
    private static class ModernViewAdapter implements JPanelView {
        private final JComponent component;

        public ModernViewAdapter(JComponent component) {
            this.component = component;
        }

        @Override
        public JComponent getComponent() {
            return component;
        }

        @Override
        public String getTitle() {
            return "";
        }

        @Override
        public void activate() throws BasicException {
        }

        @Override
        public boolean deactivate() {
            return true;
        }
    }
}
