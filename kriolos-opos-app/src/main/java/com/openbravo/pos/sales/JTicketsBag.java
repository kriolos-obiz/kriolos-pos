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

import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.DataLogicSales;
import com.openbravo.pos.sales.restaurant.JTicketsBagRestaurantMap;
import com.openbravo.pos.sales.shared.JTicketsBagShared;
import com.openbravo.pos.sales.simple.JTicketsBagSimple;
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
        m_dlSales = (DataLogicSales) m_App.getBean("com.openbravo.pos.forms.DataLogicSales");
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
     *
     * @param sName
     * @param app
     * @param panelticket
     * @return
     */
    public static JTicketsBag createTicketsBag(String sName, AppView app, TicketsEditor panelticket) {
        switch (sName) {
            case "standard":
                return new JTicketsBagShared(app, panelticket);
            case "restaurant":
                return new JTicketsBagRestaurantMap(app, panelticket);
            case "simple":
            default:
                return new JTicketsBagSimple(app, panelticket);
            
        }
    }   
}