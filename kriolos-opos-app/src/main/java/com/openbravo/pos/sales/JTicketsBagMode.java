package com.openbravo.pos.sales;

import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.sales.restaurant.JTicketsBagRestaurantMap; // keep import if needed elsewhere
import com.openbravo.pos.sales.shared.JTicketsBagShared;
import com.openbravo.pos.sales.simple.JTicketsBagSimple;
import com.openbravo.pos.sales.TicketsEditor;
import javax.swing.JComponent;

/**
 * A configurable tickets bag controller that delegates to either the simple or shared implementation
 * based on the provided {@link BagMode}. This class reduces the hierarchy by consolidating the two
 * concrete subclasses into a single entry point while preserving existing behavior.
 *
 * <p>Usage example:</p>
 * <pre>
 *   JTicketsBag bag = new JTicketsBagMode(appView, ticketsEditor, BagMode.SIMPLE);
 * </pre>
 */
public class JTicketsBagMode extends JTicketsBag {

    private final JTicketsBag delegate;

    /**
     * Constructs a tickets bag with the specified mode.
     *
     * @param app the application view
     * @param panelticket the tickets editor UI component
     * @param mode the desired operational mode (SIMPLE or SHARED)
     */
    public JTicketsBagMode(AppView app, TicketsEditor panelticket, BagMode mode) {
        super(app, panelticket);
        if (mode == BagMode.SIMPLE) {
            delegate = new JTicketsBagSimple(app, panelticket);
        } else {
            delegate = new JTicketsBagShared(app, panelticket);
        }
    }

    // ---------------------------------------------------------------------
    // Delegated lifecycle methods
    // ---------------------------------------------------------------------
    @Override
    public void activate() {
        delegate.activate();
    }

    @Override
    public boolean deactivate() {
        return delegate.deactivate();
    }

    @Override
    public void deleteTicket() {
        delegate.deleteTicket();
    }

    @Override
    protected JComponent getBagComponent() {
        return delegate.getBagComponent();
    }

    @Override
    protected JComponent getNullComponent() {
        return delegate.getNullComponent();
    }

    // ---------------------------------------------------------------------
    // Optional: forward lifecycle callbacks to the delegate if needed.
    // ---------------------------------------------------------------------
    @Override
    public void customerUpdated(com.openbravo.pos.customers.CustomerInfoExt customer, String ticketId) {
        delegate.customerUpdated(customer, ticketId);
    }

    @Override
    public void ticketClosed(com.openbravo.pos.ticket.TicketInfo ticket, String ticketExt) {
        delegate.ticketClosed(ticket, ticketExt);
    }
}
