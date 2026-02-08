package com.openbravo.pos.sales;

import com.openbravo.pos.ticket.ProductInfoExt;
import com.openbravo.pos.ticket.TicketInfo;
import com.openbravo.pos.ticket.TicketLineInfo;

/**
 * Service interface for Sales/Ticket operations.
 * Extracts business logic from UI components (JPanelTicket).
 */
public interface SalesService {

        /**
         * Create a new empty ticket.
         * 
         * @return New TicketInfo instance
         */
        TicketInfo createTicket();

        /**
         * Add a product line to the ticket.
         * 
         * @param ticket   The ticket to modify
         * @param product  The product to add
         * @param quantity The quantity to add
         * @return The created TicketLineInfo
         */
        TicketLineInfo addLine(TicketInfo ticket, ProductInfoExt product, double quantity);

        /**
         * Add a product line to the ticket with explicit price and tax inclusion logic.
         * 
         * @param ticket           The ticket to modify
         * @param product          The product to add
         * @param quantity         The quantity to add
         * @param price            The price to use
         * @param priceIncludesTax Whether the price includes tax
         * @return The created TicketLineInfo
         */
        TicketLineInfo addLine(TicketInfo ticket, ProductInfoExt product, double quantity, double price,
                        boolean priceIncludesTax);

        /**
         * Create a TicketLineInfo without adding it to the ticket.
         * 
         * @param ticket           The ticket ctx
         * @param product          The product
         * @param quantity         The quantity
         * @param price            The price
         * @param priceIncludesTax Whether price includes tax
         * @return The created line
         */
        TicketLineInfo createLine(TicketInfo ticket, ProductInfoExt product, double quantity, double price,
                        boolean priceIncludesTax);

        /**
         * Remove a line from the ticket.
         * 
         * @param ticket The ticket to modify
         * @param index  The index of the line to remove
         */
        void removeLine(TicketInfo ticket, int index);

        /**
         * Calculate taxes for the ticket.
         * 
         * @param ticket The ticket to calculate taxes for
         * @throws TaxesException if tax calculation fails
         */
        void calculateTaxes(TicketInfo ticket) throws TaxesException;

        /**
         * Get the total amount of the ticket.
         * 
         * @param ticket The ticket
         * @return Total amount
         */
        double getTotal(TicketInfo ticket);
}
