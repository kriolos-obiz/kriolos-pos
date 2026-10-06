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

import com.openbravo.pos.customers.CustomerInfoExt;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.DataLogicSales;
import com.openbravo.pos.sales.simple.JTicketsBagSimple;
import com.openbravo.pos.ticket.TicketInfo;
import com.openbravo.pos.ui.api.sales.SaleLayoutManager;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * Abstract controller and container for managing active sales tickets within the POS system.
 * <p>
 * This class serves as the foundational component for handling different ticketing workflows
 * (e.g., standard retail tabs, restaurant table maps, or simplified direct checkouts).
 * It acts as a bridge between the core sales data logic ({@link DataLogicSales}) and the
 * visual ticket editor UI ({@link TicketsEditor}).
 * </p>
 * <p>
 * Subclasses implement specific behaviors for ticket persistence, multi-ticket switching,
 * and context-specific user interface components.
 * </p>
 *
 * @author JG uniCenta
 * @see javax.swing.JPanel
 * @see com.openbravo.pos.sales.shared.JTicketsBagShared
 * @see com.openbravo.pos.sales.restaurant.JTicketsBagRestaurantMap
 * @see com.openbravo.pos.sales.simple.JTicketsBagSimple
 */

public abstract class JTicketsBag extends JPanel {
    
    protected final static System.Logger LOGGER = System.getLogger(JTicketsBag.class.getName());
    /**
     *
     */
    protected AppView m_App;     

    /**
     *
     */
    protected DataLogicSales m_dlSales;

    /**
     *
     */
    protected TicketsEditor m_panelticket;    
    
    /**
     * 
     * @param oApp
     * @param panelticket 
     */
    public JTicketsBag(AppView oApp, TicketsEditor panelticket) {        
        m_App = oApp;     
        m_panelticket = panelticket;        
        m_dlSales = m_App.getBean(DataLogicSales.class);
    }
    
    
    protected AppView getAppView(){
        return m_App;
    }
    
    /**
     * Active panel (Call on active panel)
     */
    public abstract void activate();

    /**
     * Desactive panel (Call on Desactive)
     * @return
     */
    public abstract boolean deactivate();

    /**
     * Delete Current tocket
     */
    public abstract void deleteTicket();
    
    /**
     *
     * @return
     */
    protected abstract JComponent getBagComponent();

    /**
     *
     * @return
     */
    protected abstract JComponent getNullComponent();

    /**
     * Notification callback when the customer associated with the ticket is updated or cleared.
     * Subclasses (such as restaurant maps) may synchronize table details.
     *
     * @param customer updated customer info or null if cleared
     * @param ticketId active ticket identifier
     */
    public void customerUpdated(CustomerInfoExt customer, String ticketId) {
    }

    /**
     * Notification callback when a ticket checkout is completed.
     * Subclasses (such as restaurant maps) may clear table assignments.
     *
     * @param ticket ticket that was closed
     * @param ticketExt extra ticket information (e.g. table name)
     */
    public void ticketClosed(TicketInfo ticket, String ticketExt) {
    }

    /**
     *
     * @param sName
     * @param app
     * @param panelticket
     * @return
     */
    public static JTicketsBag createTicketsBag(String sName, AppView app, TicketsEditor panelticket) {
        JComponent comp = SaleLayoutManager.createLayout(sName, app, panelticket);
        if (comp instanceof JTicketsBag bag) {
            return bag;
        }
        return new JTicketsBagSimple(app, panelticket);
    }   
}