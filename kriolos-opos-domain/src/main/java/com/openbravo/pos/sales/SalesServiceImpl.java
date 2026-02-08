package com.openbravo.pos.sales;

import com.openbravo.pos.ticket.ProductInfoExt;
import com.openbravo.pos.ticket.TaxInfo;
import com.openbravo.pos.ticket.TicketInfo;
import com.openbravo.pos.ticket.TicketLineInfo;
import java.util.Properties;

/**
 * Default implementation of SalesService.
 */
public class SalesServiceImpl implements SalesService {

    private final TaxesLogic taxesLogic;

    public SalesServiceImpl(TaxesLogic taxesLogic) {
        this.taxesLogic = taxesLogic;
    }

    @Override
    public TicketInfo createTicket() {
        return new TicketInfo();
    }

    @Override
    public TicketLineInfo addLine(TicketInfo ticket, ProductInfoExt product, double quantity) {
        // Basic implementation - needs to be refined with Tax logic integration
        return addLine(ticket, product, quantity, product.getPriceSell(), false);
    }

    @Override
    public TicketLineInfo addLine(TicketInfo ticket, ProductInfoExt product, double quantity, double price,
            boolean priceIncludesTax) {
        TicketLineInfo line = createLine(ticket, product, quantity, price, priceIncludesTax);
        ticket.addLine(line);
        return line;
    }

    @Override
    public TicketLineInfo createLine(TicketInfo ticket, ProductInfoExt product, double quantity, double price,
            boolean priceIncludesTax) {
        TaxInfo tax = taxesLogic.getTaxInfo(product.getTaxCategoryID(), ticket.getCustomer());
        Properties props = (Properties) product.getProperties().clone();

        if (priceIncludesTax) {
            price = com.openbravo.pos.domain.utils.AmountCalculatorUtil.calcPriceWithoutTax(price, tax);
        }

        return new TicketLineInfo(product, quantity, price, tax, props);
    }

    @Override
    public void removeLine(TicketInfo ticket, int index) {
        if (index < 0 || index >= ticket.getLinesCount()) {
            return;
        }

        // Remove the selected line
        ticket.removeLine(index);

        // Remove subsequent lines if they are part of a composite product
        while (index < ticket.getLinesCount()
                && ticket.getLine(index).isProductCom()) {
            ticket.removeLine(index);
        }
    }

    @Override
    public void calculateTaxes(TicketInfo ticket) throws TaxesException {
        // In the original code, taxes are often calculated on the fly or via TaxesLogic
        taxesLogic.calculateTaxes(ticket);
    }

    @Override
    public double getTotal(TicketInfo ticket) {
        return ticket.getTotal();
    }
}
