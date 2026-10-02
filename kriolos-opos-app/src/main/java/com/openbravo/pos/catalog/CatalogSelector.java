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

package com.openbravo.pos.catalog;

import com.openbravo.basic.BasicException;

/**
 * Legacy catalog selector interface.
 *
 * @deprecated Use {@link com.openbravo.pos.ui.api.catalog.CatalogSelector} from the {@code ui-api} module instead.
 * @author adrianromero, KriolOS Team
 */
@Deprecated(since = "1.0.0", forRemoval = false)
public interface CatalogSelector extends com.openbravo.pos.ui.api.catalog.CatalogSelector {

    @Override
    void loadCatalog() throws BasicException;
}
