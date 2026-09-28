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
import com.openbravo.pos.catalog.CatalogSelector;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.ticket.ProductInfoExt;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * Classic / Legacy Sales Layout View extending {@link JPanelTicket}.
 * <p>
 * Encapsulates the traditional {@link JTicketsBag} toolbar, classical catalog selector,
 * and classic ticket line listings.
 * </p>
 *
 * @author JG uniCenta, KriolOS Team
 */
public class JPanelTicketSalesClassic extends JPanelTicket {

    private static final long serialVersionUID = 1L;
    private CatalogSelector m_cat;

    public JPanelTicketSalesClassic(AppView app) {
        super(app);
        getTicketlines().addListSelectionListener(new CatalogSelectionListener());
    }

    @Override
    public String getTitle() {
        return "";
    }

    @Override
    protected Component getSouthComponent() {
        LOGGER.log(System.Logger.Level.DEBUG, "JPanelTicketSalesClassic :: getSouthComponent");
        m_cat = (CatalogSelector) com.openbravo.pos.ui.api.catalog.CatalogManager.createDefaultCatalog(getAppView());
        m_cat.addActionListener(new CatalogListener());
        return m_cat.getComponent();
    }

    @Override
    protected void resetSouthComponent() {
        if (m_cat != null) {
            m_cat.showCatalogPanel(null);
        }
    }

    @Override
    protected JTicketsBag getJTicketsBag() {
        return JTicketsBag.createTicketsBag(getTicketBagMode(), getAppView(), this);
    }

    @Override
    public void activate() throws BasicException {
        super.activate();
        reLoadCatalog();
        LOGGER.log(System.Logger.Level.DEBUG, "JPanelTicketSalesClassic activate");
    }

    public void reLoadCatalog() {
        if (m_cat == null) {
            return;
        }
        try {
            m_cat.loadCatalog();
        } catch (BasicException ex) {
            LOGGER.log(System.Logger.Level.ERROR, "Exception on : ", ex);
        }
    }

    private class CatalogListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            buttonTransition((ProductInfoExt) e.getSource());
        }
    }

    private class CatalogSelectionListener implements ListSelectionListener {

        @Override
        public void valueChanged(ListSelectionEvent e) {
            if (!e.getValueIsAdjusting()) {
                int i = getTicketlines().getSelectedIndex();

                if (i >= 0) {
                    while (i >= 0 && getActiveTicket().getLine(i).isProductCom()) {
                        i--;
                    }

                    if (i >= 0) {
                        m_cat.showCatalogPanel(getActiveTicket().getLine(i).getProductID());
                    } else {
                        m_cat.showCatalogPanel(null);
                    }
                }
            }
        }
    }
}
