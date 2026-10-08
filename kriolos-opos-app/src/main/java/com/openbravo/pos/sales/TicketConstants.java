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

/**
 * Standard constants for ticket events, properties, and resources.
 */
public final class TicketConstants {

    /** Ticket event: 'ticket.show' */
    public static final String EV_TICKET_SHOW = "ticket.show";

    /** Ticket event: 'ticket.change' */
    public static final String EV_TICKET_CHANGE = "ticket.change";

    /** Ticket event: 'ticket.close' */
    public static final String EV_TICKET_CLOSE = "ticket.close";

    /** Ticket event: 'ticket.save' */
    public static final String EV_TICKET_SAVE = "ticket.save";

    /** Ticket event: 'ticket.total' */
    public static final String EV_TICKET_TOTAL = "ticket.total";

    /** Ticket line property: 'ticket.updated' MUST MOVED TO TicketLineConstants */
    public static final String PROP_TICKET_UPDATED = "ticket.updated";
    
    /** Set priceVisibled in JTicketCatalogLines:  */
    public static final String PROP_CATALOG_PRICE_VISIBLED= "pricevisible";
    
    /** Set priceVisibled in JTicketCatalogLines:  */
    public static final String PROP_CATALOG_TAX_INCLUDED= "taxesincluded";
    
    /** Set priceVisibled in JTicketCatalogLines:  */
    public static final String PROP_CATALOG_PRODUCT_CARD_HEIGHT= "img-height";
    
    /** Set priceVisibled in JTicketCatalogLines:  */
    public static final String PROP_CATALOG_PRODUCT_CARD_WIDTH= "img-width";
    
    /** Set priceVisibled in JTicketCatalogLines:  */
    public static final String PROP_CATALOG_CATEGORY_SIDEBAR_HEIGHT= "cat-height";
    
    /** Set default category on InputPage:  */
    public static final String PROP_TAX_CATEGORY_ID = "taxcategoryid";

    /** Ticket resource: 'Ticket.Buttons' */
    public static final String RES_TICKET_BUTTONS = "Ticket.Buttons";

    /** Ticket resource: 'Ticket.Line' */
    public static final String RES_TICKET_LINES = "Ticket.Line";

    private TicketConstants() {
    }
}
