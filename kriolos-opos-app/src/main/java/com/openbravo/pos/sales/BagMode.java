package com.openbravo.pos.sales;

/**
 * Enum representing the mode of operation for {@link JTicketsBag}.
 * <p>
 *   SIMPLE – single‑ticket checkout flow (formerly JTicketsBagSimple).
 *   SHARED – multi‑ticket/layaway flow (formerly JTicketsBagShared).
 * </p>
 */
public enum BagMode {
    SIMPLE,
    SHARED
}
