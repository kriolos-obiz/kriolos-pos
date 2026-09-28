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

package com.openbravo.pos.panels;

import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.ticket.ProductInfoExt;
import java.awt.Component;

/**
 * Backward-compatible adapter delegating to {@link JProductFinderPanel}.
 *
 * @deprecated Use {@link JProductFinderPanel#showMessage(Component, AppView)} instead.
 */
@Deprecated
public class JProductFinder {

    public final static int PRODUCT_ALL = JProductFinderPanel.PRODUCT_ALL;
    public final static int PRODUCT_NORMAL = JProductFinderPanel.PRODUCT_NORMAL;
    public final static int PRODUCT_AUXILIAR = JProductFinderPanel.PRODUCT_AUXILIAR;
    public final static int PRODUCT_BUNDLE = JProductFinderPanel.PRODUCT_BUNDLE;

    private JProductFinder() {
    }

    public static ProductInfoExt showMessage(Component parent, AppView app) {
        return JProductFinderPanel.showMessage(parent, app);
    }

    public static ProductInfoExt showMessage(Component parent, AppView app, int productsType) {
        return JProductFinderPanel.showMessage(parent, app, productsType);
    }
}
