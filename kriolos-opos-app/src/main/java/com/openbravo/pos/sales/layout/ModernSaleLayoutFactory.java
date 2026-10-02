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
import com.openbravo.pos.sales.modern.one.ModernOne;
import com.openbravo.pos.sales.modern.two.ModernTwoSalesLayout;
import com.openbravo.pos.ui.api.sales.SaleLayoutDefinition;
import com.openbravo.pos.ui.api.sales.SaleLayoutFactory;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JComponent;

/**
 * Modern touch layouts provider:
 * <ul>
 *   <li><b>modern_one</b>: Modern Dual-Pane touch interface</li>
 *   <li><b>modern_two</b>: FlexLayout responsive 70/30 touch layout with ActionPane</li>
 * </ul>
 * <p>
 * Prepared for future modular extraction into {@code kriolos-opos-ui-modern}.
 * </p>
 *
 * @author KriolOS Team
 */
public class ModernSaleLayoutFactory implements SaleLayoutFactory {

    public static final String MODERN_ONE = "modern_one";
    public static final String MODERN_TWO = "modern_two";

    private static final Map<String, SaleLayoutDefinition> DEFINITIONS = new LinkedHashMap<>();

    static {
        DEFINITIONS.put(MODERN_ONE, new SaleLayoutDefinition(
                MODERN_ONE,
                "Modern One (Dual-Pane Touch)",
                "Interface moderna touch com recibo digital \u00e0 esquerda e cat\u00e1logo \u00e0 direita.",
                true,
                false
        ));
        DEFINITIONS.put(MODERN_TWO, new SaleLayoutDefinition(
                MODERN_TWO,
                "Modern Two (Flex Touch)",
                "Interface moderna baseada em FlexLayout 70/30 com barra de a\u00e7\u00f5es dedicada.",
                true,
                false
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
            return new ModernTwoSalesLayout(appView, editor);
        }
        return switch (layoutId.toLowerCase()) {
            case MODERN_ONE -> new ModernOne(appView, editor);
            case MODERN_TWO -> new ModernTwoSalesLayout(appView, editor);
            default -> new ModernTwoSalesLayout(appView, editor);
        };
    }

    @Override
    public boolean isFullView(String layoutId) {
        return true;
    }

    @Override
    public boolean isRestaurantLayout(String layoutId) {
        return false;
    }
}
