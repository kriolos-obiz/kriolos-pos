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
import com.openbravo.data.loader.*;
import com.openbravo.data.model.Field;
import com.openbravo.data.model.Row;
import com.openbravo.data.user.EditorRecord;
import com.openbravo.data.user.ListProviderCreator;
import com.openbravo.data.user.DefaultSaveProvider;
import com.openbravo.format.Formats;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.DataLogicSales;
import com.openbravo.pos.panels.JPanelTable2;
import com.openbravo.pos.reports.JParamsLocation;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.UUID;

/**
 *
 * @author adrianromero
 */
public class ProductsWarehousePanel extends JPanelTable2 {

    private JParamsLocation m_paramslocation;
    private ProductsWarehouseEditor jeditor;

    /** Creates a new instance of ProductsWarehousePanel */
    public ProductsWarehousePanel() {
    }

    /**
     *
     */
    @Override
    protected void init() {

        m_paramslocation = new JParamsLocation();
        m_paramslocation.init(app);
        m_paramslocation.addActionListener(new ReloadActionListener());

        row = new Row(
                new Field("ID", Datas.STRING, Formats.STRING),
                new Field("PRODUCT_ID", Datas.STRING, Formats.STRING),
                new Field(AppLocal.getIntString("label.prodref"), Datas.STRING, Formats.STRING, true, true, true),
                new Field(AppLocal.getIntString("label.prodname"), Datas.STRING, Formats.STRING, true, true, true),
                new Field("LOCATION", Datas.STRING, Formats.STRING),
                new Field("STOCKSECURITY", Datas.DOUBLE, Formats.DOUBLE),
                new Field("STOCKMAXIMUM", Datas.DOUBLE, Formats.DOUBLE),
                new Field("UNITS", Datas.DOUBLE, Formats.DOUBLE));

        lpr = new ListProviderCreator(new PreparedSentence(app.getSession(),
                DataLogicSales.SQL_WAREHOUSE_STOCK_LIST,
                new SerializerWriteBasicExt(new Datas[] { Datas.OBJECT, Datas.STRING }, new int[] { 1, 1 }),
                new WarehouseSerializerRead()),
                m_paramslocation);

        SentenceExec updatesent = new SentenceExecTransaction(app.getSession()) {
            @Override
            public int execInTransaction(Object[] params) throws BasicException {
                Object[] values = params;
                if (values[0] == null) {
                    // INSERT
                    values[0] = UUID.randomUUID().toString();
                    return new PreparedSentence(app.getSession(), DataLogicSales.SQL_STOCKLEVEL_INSERT,
                            new SerializerWriteBasicExt(row.getDatas(), new int[] { 0, 4, 1, 5, 6 })).exec(params);
                } else {
                    // UPDATE
                    return new PreparedSentence(app.getSession(), DataLogicSales.SQL_STOCKLEVEL_UPDATE,
                            new SerializerWriteBasicExt(row.getDatas(), new int[] { 5, 6, 0 })).exec(params);
                }
            }
        };

        spr = new DefaultSaveProvider(updatesent, null, null);

        jeditor = new ProductsWarehouseEditor(dirty);
    }

    /**
     *
     * @return
     */
    @Override
    public Component getFilter() {
        return m_paramslocation.getComponent();
    }

    /**
     *
     * @return
     */
    @Override
    public EditorRecord getEditor() {
        return jeditor;
    }

    /**
     *
     * @throws BasicException
     */
    @Override
    public void activate() throws BasicException {

        m_paramslocation.activate();
        super.activate();
    }

    /**
     *
     * @return
     */
    @Override
    public String getTitle() {
        return AppLocal.getIntString("Menu.ProductsWarehouse");
    }

    private class ReloadActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                ProductsWarehousePanel.this.bd.actionLoad();
            } catch (BasicException w) {
            }
        }
    }

    private class WarehouseSerializerRead implements SerializerRead {
        @Override
        public Object readValues(DataRead dr) throws BasicException {
            return new Object[] {
                    dr.getString(1),
                    dr.getString(2),
                    dr.getString(3),
                    dr.getString(4),
                    ((Object[]) m_paramslocation.createValue())[1],
                    dr.getDouble(5),
                    dr.getDouble(6),
                    dr.getDouble(7)
            };
        }
    }
}
