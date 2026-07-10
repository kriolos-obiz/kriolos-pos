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
package com.openbravo.pos.sales;

import com.openbravo.basic.BasicException;
import com.openbravo.pos.catalog.CatalogSelector;
import com.openbravo.pos.catalog.JCatalog;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.ticket.ProductInfoExt;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 *
 * @author JG uniCenta
 */
public class JPanelTicketSales extends JPanelTicket {

    private static final long serialVersionUID = 1L;
    private CatalogSelector m_cat;

    public JPanelTicketSales(AppView app) {
        super(app);
        getTicketlines().addListSelectionListener(new CatalogSelectionListener());
    }

    @Override
    public String getTitle() {
        return "";
    }

    /**
     * 
     * @return 
     */
    @Override
    protected Component getSouthComponent() {
        LOGGER.log(System.Logger.Level.DEBUG,"JPanelTicketSales :: getSouthComponent");
        m_cat = new JCatalog(getDataLogicSales());
        m_cat.addActionListener(new CatalogListener());
        return m_cat.getComponent();
    }

    @Override
    protected void resetSouthComponent() {
        m_cat.showCatalogPanel(null);
    }

    @Override
    protected JTicketsBag getJTicketsBag() {
        return JTicketsBag.createTicketsBag(getTicketBagMode(), getAppView(), this);
    }

    @Override
    public void activate() throws BasicException {
        super.activate();
        reLoadCatalog();
        LOGGER.log(System.Logger.Level.DEBUG,"JPanelTicketSales activate");
    }

    public void reLoadCatalog() {
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
                    // Look for the first non auxiliar product.
                    while (i >= 0 && getActiveTicket().getLine(i).isProductCom()) {
                        i--;
                    }

                    // Show the accurate catalog panel...
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
