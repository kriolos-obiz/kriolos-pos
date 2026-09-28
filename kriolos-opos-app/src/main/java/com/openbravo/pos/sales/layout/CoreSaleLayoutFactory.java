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

package com.openbravo.pos.sales.layout;

import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.sales.TicketsEditor;
import com.openbravo.pos.sales.restaurant.JTicketsBagRestaurantMap;
import com.openbravo.pos.sales.shared.JTicketsBagShared;
import com.openbravo.pos.sales.simple.JTicketsBagSimple;
import com.openbravo.pos.ui.api.sales.SaleLayoutDefinition;
import com.openbravo.pos.ui.api.sales.SaleLayoutFactory;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JComponent;

/**
 * Built-in core provider supplying classical sales layouts:
 * <ul>
 *   <li><b>simple</b>: Single ticket quick sales workflow</li>
 *   <li><b>standard</b>: Multi-ticket shared tabs workflow</li>
 *   <li><b>restaurant</b>: Floor map and table management workflow</li>
 * </ul>
 *
 * @author KriolOS Team
 */
public class CoreSaleLayoutFactory implements SaleLayoutFactory {

    public static final String SIMPLE = "simple";
    public static final String STANDARD = "standard";
    public static final String RESTAURANT = "restaurant";

    private static final Map<String, SaleLayoutDefinition> DEFINITIONS = new LinkedHashMap<>();

    static {
        DEFINITIONS.put(SIMPLE, new SaleLayoutDefinition(
                SIMPLE,
                "Simple (Venda Direta)",
                "Layout cl\u00e1ssico com comanda \u00fanica e sem m\u00faltiplas abas.",
                false,
                false
        ));
        DEFINITIONS.put(STANDARD, new SaleLayoutDefinition(
                STANDARD,
                "Standard (M\u00faltiplas Abas)",
                "Layout cl\u00e1ssico com suporte a m\u00faltiplas contas e abas numeradas.",
                false,
                false
        ));
        DEFINITIONS.put(RESTAURANT, new SaleLayoutDefinition(
                RESTAURANT,
                "Restaurante (Mapa de Mesas)",
                "Layout cl\u00e1ssico com gest\u00e3o gr\u00e1fica de mesas e pisos.",
                false,
                true
        ));
    }

    @Override
    public boolean hasLayout(String layoutId) {
        return layoutId != null && DEFINITIONS.containsKey(layoutId.toLowerCase());
    }

    @Override
    public Collection<SaleLayoutDefinition> getAvailableLayouts() {
        return List.copyOf(DEFINITIONS.values());
    }

    @Override
    public JComponent createLayout(String layoutId, Object app, Object panelticket) {
        AppView appView = (AppView) app;
        TicketsEditor editor = (TicketsEditor) panelticket;

        if (layoutId == null) {
            return new JTicketsBagSimple(appView, editor);
        }
        return switch (layoutId.toLowerCase()) {
            case STANDARD -> new JTicketsBagShared(appView, editor);
            case RESTAURANT -> new JTicketsBagRestaurantMap(appView, editor);
            case SIMPLE -> new JTicketsBagSimple(appView, editor);
            default -> new JTicketsBagSimple(appView, editor);
        };
    }

    @Override
    public boolean isFullView(String layoutId) {
        return false;
    }

    @Override
    public boolean isRestaurantLayout(String layoutId) {
        return RESTAURANT.equalsIgnoreCase(layoutId);
    }
}
