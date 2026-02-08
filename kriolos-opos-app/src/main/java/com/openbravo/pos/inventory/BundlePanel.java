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

package com.openbravo.pos.inventory;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.Datas;
import com.openbravo.data.model.*;
import com.openbravo.data.user.EditorRecord;
import com.openbravo.format.Formats;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.DataLogicSales;
import com.openbravo.pos.panels.BundleFilter;
import com.openbravo.pos.panels.JPanelTable2;
import com.openbravo.pos.ticket.ProductInfoExt;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 *
 * @author Jack
 */
public class BundlePanel extends JPanelTable2 {

    private BundleEditor editor;
    private BundleFilter filter;

    @Override
    protected void init() {

        filter = new BundleFilter();
        filter.init(app);
        filter.addActionListener(new ReloadActionListener());

        row = new Row(
                new Field("ID", Datas.STRING, Formats.STRING),
                new Field("PRODUCT", Datas.STRING, Formats.STRING),
                new Field("PRODUCT_BUNDLE", Datas.STRING, Formats.STRING),
                new Field("QUANTITY", Datas.DOUBLE, Formats.DOUBLE),
                new Field(AppLocal.getIntString("label.prodref"), Datas.STRING, Formats.STRING, true, true, true),
                new Field(AppLocal.getIntString("label.prodbarcode"), Datas.STRING, Formats.STRING, false, true, true),
                new Field(AppLocal.getIntString("label.prodname"), Datas.STRING, Formats.STRING, true, true, true));
        Table table = new Table(
                "products_bundle",
                new PrimaryKey("ID"),
                new Column("PRODUCT"),
                new Column("PRODUCT_BUNDLE"),
                new Column("QUANTITY"));

        lpr = row.getListProvider(app.getSession(),
                DataLogicSales.SQL_BUNDLE_LIST, filter);
        spr = row.getSaveProvider(app.getSession(), table);

        editor = new BundleEditor(app, dirty);
    }

    @Override
    public void activate() throws BasicException {
        super.activate();
        filter.activate();
        reload(filter);
    }

    @Override
    public Component getFilter() {
        return filter.getComponent();
    }

    @Override
    public EditorRecord getEditor() {
        return editor;
    }

    @Override
    public String getTitle() {
        return AppLocal.getIntString("Menu.Bundle");
    }

    private void reload(BundleFilter filter) throws BasicException {
        ProductInfoExt prod = filter.getProductInfoExt();
        editor.setInsertProduct(prod);
        bd.setEditable(prod != null);
        bd.actionLoad();
    }

    private class ReloadActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                reload((BundleFilter) e.getSource());
            } catch (BasicException w) {
            }
        }
    }
}