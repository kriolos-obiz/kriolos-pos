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

package com.openbravo.pos.businesspartner;

import com.openbravo.pos.customers.CustomerService;
import com.openbravo.pos.customers.DataLogicCustomers;
import java.awt.Component;

/**
 * Backward-compatible adapter delegating to {@link JBusinessPartnerFinderPanel}.
 *
 * @deprecated Use {@link JBusinessPartnerFinderPanel#show(Component, CustomerService)} instead.
 */
@Deprecated
public class JBusinessPartnerFinder {

    private JBusinessPartnerFinder() {
    }

    public static BusinessPartner show(Component parent, CustomerService customerService) {
        return JBusinessPartnerFinderPanel.show(parent, customerService);
    }

    @Deprecated
    public static BusinessPartner show(Component parent, DataLogicCustomers dlCustomers) {
        return JBusinessPartnerFinderPanel.show(parent, (CustomerService) dlCustomers);
    }
}

