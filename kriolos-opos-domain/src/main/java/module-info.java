/*
 * Copyright (C) 2026 KriolOS POS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

module kriolos.opos.domain {
    requires java.base;
    requires transitive java.desktop;
    requires transitive java.sql;
    requires java.logging;

    requires transitive kriolos.opos.base;
    requires transitive kriolos.opos.config;
    requires transitive kriolos.opos.spi;

    requires transitive com.fasterxml.jackson.databind;
    requires transitive com.fasterxml.jackson.core;
    requires transitive com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.datatype.jsr310;

    exports com.openbravo.pos.admin;
    exports com.openbravo.pos.businesspartner;
    exports com.openbravo.pos.cash;
    exports com.openbravo.pos.catalog;
    exports com.openbravo.pos.config;
    exports com.openbravo.pos.container;
    exports com.openbravo.pos.customers;
    exports com.openbravo.pos.domain.audit;
    exports com.openbravo.pos.domain.navigation;
    exports com.openbravo.pos.domain.utils;
    exports com.openbravo.pos.epm;
    exports com.openbravo.pos.imports;
    exports com.openbravo.pos.inventory;
    exports com.openbravo.pos.panels;
    exports com.openbravo.pos.payment;
    exports com.openbravo.pos.pim;
    exports com.openbravo.pos.reports;
    exports com.openbravo.pos.resources;
    exports com.openbravo.pos.sales;
    exports com.openbravo.pos.sales.restaurant;
    exports com.openbravo.pos.scripting;
    exports com.openbravo.pos.suppliers;
    exports com.openbravo.pos.ticket;
    exports com.openbravo.pos.voucher;

    opens com.openbravo.pos.domain.audit to com.fasterxml.jackson.databind;
}
