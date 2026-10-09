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

package com.openbravo.pos.sales.modern;

import com.openbravo.pos.catalog.CatalogService;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.pim.CategoryInfo;
import com.openbravo.pos.pim.DataLogicPIM;
import com.openbravo.pos.sales.modern.two.ModernTwoCatalogPane;
import com.openbravo.pos.ticket.ProductInfoExt;
import com.openbravo.pos.ui.api.catalog.CatalogSelector;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EventListener;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.openbravo.basic.BasicException;
import javax.swing.JPanel;
import javax.swing.event.EventListenerList;

/**
 * Modern touch catalog implementation of {@link CatalogSelector}.
 * <p>
 * Combines full-text search, barcode input, category pills, and a responsive
 * product grid. Can be plugged into any sales layout or screen expecting a
 * {@link CatalogSelector}.
 * </p>
 *
 * @author KriolOS Team
 */
public class ModernTouchCatalog extends JPanel implements CatalogSelector, com.openbravo.pos.catalog.CatalogSelector {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(ModernTouchCatalog.class.getName());

    private final AppView app;
    private final ModernTwoCatalogPane catalogPane;
    private final EventListenerList listeners = new EventListenerList();

    public ModernTouchCatalog(AppView app) {
        super(new BorderLayout());
        this.app = app;
        setOpaque(false);

        this.catalogPane = new ModernTwoCatalogPane(this::fireProductSelected);
        add(catalogPane, BorderLayout.CENTER);
    }

    @Override
    public void loadCatalog() throws BasicException {
        if (app == null) {
            return;
        }

        CatalogService catalogService = app.getBean(CatalogService.class);

        if (catalogService != null) {
            try {
                List<CategoryInfo> categories = catalogService.getRootCategories();
                catalogPane.setCategories(categories);

                java.util.List<ProductInfoExt> allProducts = new java.util.ArrayList<>();
                if (categories != null) {
                    for (CategoryInfo cat : categories) {
                        List<ProductInfoExt> prods = catalogService.getProductCatalog(cat.getID());
                        if (prods != null) {
                            allProducts.addAll(prods);
                        }
                    }
                }
                catalogPane.setProducts(allProducts);
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, "Failed to load catalog data for ModernTouchCatalog", ex);
            }
        }
    }

    @Override
    public void showCatalogPanel(String id) {
        catalogPane.selectCategory(id);
    }

    @Override
    public void setComponentEnabled(boolean value) {
        setEnabled(value);
        catalogPane.setEnabled(value);
    }

    @Override
    public Component getComponent() {
        return this;
    }

    @Override
    public void addActionListener(ActionListener l) {
        listeners.add(ActionListener.class, l);
    }

    @Override
    public void removeActionListener(ActionListener l) {
        listeners.remove(ActionListener.class, l);
    }

    private void fireProductSelected(ProductInfoExt product) {
        ActionEvent evt = new ActionEvent(product, ActionEvent.ACTION_PERFORMED, null);
        for (EventListener l : listeners.getListeners(ActionListener.class)) {
            ((ActionListener) l).actionPerformed(evt);
        }
    }
}
