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
package com.openbravo.pos.sales.restaurant;

import com.openbravo.data.gui.NullIcon;
import com.openbravo.pos.forms.AppView;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;

/**
 * Encapsulates presentation formatting, icons, and HTML styling for Place buttons.
 */
public final class PlaceButtonRenderer {

    private static final Icon ICO_OCU_SM = new ImageIcon(
            Place.class.getResource("/com/openbravo/images/edit_group_sm.png"));
    private static final Icon ICO_WAITER = new NullIcon(1, 1);
    private static final Icon ICO_FRE = new NullIcon(22, 22);

    private PlaceButtonRenderer() {
    }

    /**
     * Updates the text, icon, and tooltip of a PlaceButton based on occupancy and settings.
     *
     * @param btn          the presentation button
     * @param placeService service providing waiter/customer table details
     * @param appView      application context for properties
     */
    public static void renderButton(PlaceButton btn, PlaceServiceImpl placeService, AppView appView) {
        if (btn == null) {
            return;
        }
        renderButton(btn, btn.getPlace(), placeService, appView);
    }

    public static void renderButton(JButton btn, Place place, PlaceServiceImpl placeService, AppView appView) {
        if (btn == null || place == null) {
            return;
        }

        btn.setEnabled(true);

        if (!place.hasPeople()) {
            renderFreePlace(place, btn, appView);
        } else {
            renderOccupiedPlace(place, btn, placeService, appView);
        }
    }

    private static void renderFreePlace(Place place, JButton btn, AppView appView) {
        String tableColour = appView.getProperties().getProperty("table.tablecolour");
        if (tableColour == null || tableColour.isBlank()) {
            tableColour = "black";
        }

        String tableNameHtml = "<style=font-size:10px;font-weight:bold;><font color ="
                + tableColour + ">" + place.getName() + "</font></style>";

        btn.setText("<html><center>" + tableNameHtml + "</center></html>");
        btn.setIcon(ICO_FRE);
    }

    private static void renderOccupiedPlace(Place place, JButton btn, PlaceServiceImpl placeService, AppView appView) {
        boolean showWaiter = Boolean.parseBoolean(appView.getProperties().getProperty("table.showwaiterdetails"));
        boolean showCustomer = Boolean.parseBoolean(appView.getProperties().getProperty("table.showcustomerdetails"));

        String tableColour = appView.getProperties().getProperty("table.tablecolour");
        if (tableColour == null || tableColour.isBlank()) {
            tableColour = "black";
        }
        String tableNameHtml = "<style=font-size:9px;font-weight:bold;><font color ="
                + tableColour + ">" + place.getName() + "</font></style>";

        String waiterHtml = "";
        if (showWaiter) {
            String waiterColour = appView.getProperties().getProperty("table.waitercolour");
            if (waiterColour == null || waiterColour.isBlank()) {
                waiterColour = "red";
            }
            String waiterName = placeService.getWaiterNameInTable(place.getName());
            if (waiterName != null && !waiterName.isBlank()) {
                String waiterById = placeService.getWaiterNameInTableById(place.getId());
                waiterHtml = "<style=font-size:9px;font-weight:bold;><font color ="
                        + waiterColour + ">" + (waiterById != null ? waiterById : waiterName)
                        + "</font></style><br>";
            }
        }

        String customerHtml = "";
        if (showCustomer) {
            String customerColour = appView.getProperties().getProperty("table.customercolour");
            if (customerColour == null || customerColour.isBlank()) {
                customerColour = "blue";
            }
            String customerName = placeService.getCustomerNameInTable(place.getName());
            if (customerName != null && !customerName.isBlank()) {
                String customerById = placeService.getCustomerNameInTableById(place.getId());
                customerHtml = "<style=font-size:9px;font-weight:bold;><font color ="
                        + customerColour + ">" + (customerById != null ? customerById : customerName)
                        + "</font></style><br>";
            }
        }

        if (showWaiter || showCustomer) {
            btn.setText("<html><center>" + customerHtml + waiterHtml + tableNameHtml + "</center></html>");
        } else {
            btn.setText("<html><center>" + tableNameHtml + "</center></html>");
        }

        // Set appropriate icon
        if (showCustomer && showWaiter && placeService.getCustomerNameInTable(place.getName()) != null) {
            btn.setIcon(ICO_WAITER);
        } else if (showWaiter || showCustomer) {
            btn.setIcon(ICO_OCU_SM);
        } else {
            btn.setIcon(ICO_OCU_SM);
        }
    }
}
