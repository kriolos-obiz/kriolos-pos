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

import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.ui.api.catalog.CatalogDefinition;
import com.openbravo.pos.ui.api.catalog.CatalogFactory;
import com.openbravo.pos.ui.api.catalog.CatalogManager;
import com.openbravo.pos.ui.api.catalog.CatalogSelector;
import java.util.Collection;
import java.util.List;

/**
 * SPI provider for the classic Openbravo/uniCenta button grid catalog.
 *
 * @author KriolOS Team
 */
public class ClassicCatalogFactory implements CatalogFactory {

    private static final CatalogDefinition DEFINITION = new CatalogDefinition(
            CatalogManager.CLASSIC,
            "Classic Catalog",
            "Cat\u00e1logo cl\u00e1ssico uniCenta / Openbravo POS com abas verticais e grade de bot\u00f5es."
    );

    @Override
    public boolean hasCatalog(String catalogId) {
        return catalogId != null && CatalogManager.CLASSIC.equalsIgnoreCase(catalogId);
    }

    @Override
    public Collection<CatalogDefinition> getAvailableCatalogs() {
        return List.of(DEFINITION);
    }

    @Override
    public CatalogSelector createCatalog(String catalogId, Object app) {
        return new JCatalog((AppView) app);
    }
}
