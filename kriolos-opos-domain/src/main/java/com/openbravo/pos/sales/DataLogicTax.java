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

package com.openbravo.pos.sales;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.DataRead;
import com.openbravo.data.loader.Datas;
import com.openbravo.data.loader.SentenceList;
import com.openbravo.data.loader.Session;
import com.openbravo.data.loader.StaticSentence;
import com.openbravo.data.loader.TableDefinition;
import com.openbravo.format.Formats;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.BeanFactoryDataSingle;
import com.openbravo.pos.inventory.TaxCategoryInfo;
import com.openbravo.pos.inventory.TaxCustCategoryInfo;
import com.openbravo.pos.ticket.TaxInfo;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data access logic for Tax definitions, categories, and rates.
 */
public class DataLogicTax extends BeanFactoryDataSingle implements TaxService {

    private static final Logger LOGGER = Logger.getLogger(DataLogicTax.class.getName());

    private Session sessionDB;

    @Override
    public void init(Session s) {
        this.sessionDB = s;
    }

    public SentenceList<TaxInfo> getTaxList() {
        return new StaticSentence<>(sessionDB,
                "SELECT "
                + "ID, "
                + "NAME, "
                + "CATEGORY, "
                + "CUSTCATEGORY, "
                + "PARENTID, "
                + "RATE, "
                + "RATECASCADE, "
                + "RATEORDER "
                + "FROM taxes "
                + "ORDER BY NAME",
                null,
                (DataRead dr) -> new TaxInfo(
                        dr.getString(1),
                        dr.getString(2),
                        dr.getString(3),
                        dr.getString(4),
                        dr.getString(5),
                        dr.getDouble(6),
                        dr.getBoolean(7),
                        dr.getInt(8)));
    }

    @Override
    public List<TaxInfo> getTaxListAll() {
        List<TaxInfo> list = null;
        try {
            list = this.getTaxList().list();
        } catch (BasicException ex) {
            LOGGER.log(Level.WARNING, "Cannot get Tax list", ex);
        }
        return list;
    }

    public SentenceList<TaxCustCategoryInfo> getTaxCustCategoriesList() {
        return new StaticSentence<>(sessionDB,
                "SELECT "
                + "ID, "
                + "NAME "
                + "FROM taxcustcategories "
                + "ORDER BY NAME",
                null,
                (DataRead dr) -> new TaxCustCategoryInfo(
                        dr.getString(1),
                        dr.getString(2)));
    }

    @Override
    public List<TaxCustCategoryInfo> getTaxCustCategoriesListAll() {
        List<TaxCustCategoryInfo> list = null;
        try {
            list = this.getTaxCustCategoriesList().list();
        } catch (BasicException ex) {
            LOGGER.log(Level.WARNING, "Cannot get TaxCustCategoryInfo list", ex);
        }
        return list;
    }

    public SentenceList<TaxCategoryInfo> getTaxCategoriesList() {
        return new StaticSentence<>(sessionDB,
                "SELECT "
                + "ID, "
                + "NAME "
                + "FROM taxcategories "
                + "ORDER BY NAME",
                null,
                (DataRead dr) -> new TaxCategoryInfo(dr.getString(1), dr.getString(2)));
    }

    @Override
    public List<TaxCategoryInfo> getTaxCategoriesListAll() {
        List<TaxCategoryInfo> list = null;
        try {
            list = this.getTaxCategoriesList().list();
        } catch (BasicException ex) {
            LOGGER.log(Level.WARNING, "Cannot get TaxCategoryInfo list", ex);
        }
        return list;
    }

    public SentenceList<TaxCategoryInfo> getTaxCategoryInfoList() {
        return getTaxCategoriesList();
    }

    public TableDefinition getTableTaxes() {
        return new TableDefinition(sessionDB,
                "taxes",
                new String[]{"ID", "NAME", "CATEGORY", "CUSTCATEGORY", "PARENTID", "RATE",
                    "RATECASCADE",
                    "RATEORDER"},
                new String[]{"ID", AppLocal.getIntString("label.name"),
                    AppLocal.getIntString("label.taxcategory"),
                    AppLocal.getIntString("label.custtaxcategory"),
                    AppLocal.getIntString("label.taxparent"),
                    AppLocal.getIntString("label.dutyrate"),
                    AppLocal.getIntString("label.cascade"),
                    AppLocal.getIntString("label.order")},
                new Datas[]{Datas.STRING, Datas.STRING, Datas.STRING, Datas.STRING, Datas.STRING,
                    Datas.DOUBLE,
                    Datas.BOOLEAN, Datas.INT},
                new Formats[]{Formats.STRING, Formats.STRING, Formats.STRING, Formats.STRING,
                    Formats.STRING,
                    Formats.PERCENT, Formats.BOOLEAN, Formats.INT},
                new int[]{0});
    }

    public TableDefinition getTableTaxCustCategories() {
        return new TableDefinition(sessionDB,
                "taxcustcategories",
                new String[]{"ID", "NAME"},
                new String[]{"ID", AppLocal.getIntString("label.name")},
                new Datas[]{Datas.STRING, Datas.STRING},
                new Formats[]{Formats.STRING, Formats.STRING},
                new int[]{0});
    }

    public TableDefinition getTableTaxCategories() {
        return new TableDefinition(sessionDB,
                "taxcategories",
                new String[]{"ID", "NAME"},
                new String[]{"ID", AppLocal.getIntString("label.name")},
                new Datas[]{Datas.STRING, Datas.STRING},
                new Formats[]{Formats.STRING, Formats.STRING},
                new int[]{0});
    }
}
