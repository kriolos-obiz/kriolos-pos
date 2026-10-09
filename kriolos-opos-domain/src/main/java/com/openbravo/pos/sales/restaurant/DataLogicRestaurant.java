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

package com.openbravo.pos.sales.restaurant;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.Datas;
import com.openbravo.data.loader.PreparedSentence;
import com.openbravo.data.loader.SentenceExec;
import com.openbravo.data.loader.SentenceExecTransaction;
import com.openbravo.data.loader.SentenceList;
import com.openbravo.data.loader.SerializerReadBasic;
import com.openbravo.data.loader.SerializerReadClass;
import com.openbravo.data.loader.SerializerWriteBasic;
import com.openbravo.data.loader.SerializerWriteBasicExt;
import com.openbravo.data.loader.Session;
import com.openbravo.data.loader.StaticSentence;
import com.openbravo.data.loader.TableDefinition;
import com.openbravo.data.user.DefaultSaveProvider;
import com.openbravo.data.user.EditorCreator;
import com.openbravo.data.user.ListProvider;
import com.openbravo.data.user.ListProviderCreator;
import com.openbravo.data.user.SaveProvider;
import com.openbravo.format.Formats;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.BeanFactoryDataSingle;
import java.util.List;

/**
 * Data access logic for Restaurant / Hospitality domain (Floors, Places, Reservations).
 */
public class DataLogicRestaurant extends BeanFactoryDataSingle implements RestaurantService {

    private Session sessionDB;

    private static final Datas[] RESERVATION_DATA = new Datas[]{
        Datas.STRING,     // R.ID 
        Datas.TIMESTAMP,  // R.CREATED
        Datas.TIMESTAMP,  // R.DATENEW
        Datas.STRING,     // C.CUSTOMER
        Datas.STRING,     // customers.TAXID
        Datas.STRING,     // customers.SEARCHKEY
        Datas.STRING,     // COALESCE(customers.NAME, R.TITLE)
        Datas.INT,        // R.CHAIRS
        Datas.BOOLEAN,    // R.ISDONE
        Datas.STRING      // R.DESCRIPTION
    };

    @Override
    public void init(Session s) {
        this.sessionDB = s;
    }

    public Session getSession() {
        return sessionDB;
    }

    @Override
    public List<FloorsInfo> getFloorsListAll() throws BasicException {
        return getFloorsList().list();
    }

    @Override
    public List<FloorsInfo> getFloorTablesListAll() throws BasicException {
        return getFloorTablesList().list();
    }

    @Override
    public ListProvider getReservationsListProvider(EditorCreator filter) {
        return new ListProviderCreator(getReservationsList(), filter);
    }

    @Override
    public SaveProvider getReservationsSaveProvider() {
        return new DefaultSaveProvider(getReservationsUpdate(), getReservationsInsert(), getReservationsDelete());
    }

    @Override
    public SentenceList<FloorsInfo> getFloorsList() {
        return new StaticSentence(sessionDB,
                "SELECT ID, NAME FROM floors ORDER BY NAME",
                null,
                new SerializerReadClass(FloorsInfo.class));
    }

    public SentenceList<FloorsInfo> getFloorTablesList() {
        return new StaticSentence(sessionDB,
                "SELECT ID, NAME, SEATS FROM places ORDER BY NAME",
                null,
                new SerializerReadClass(FloorsInfo.class));
    }

    public TableDefinition getTableFloors() {
        return new TableDefinition(sessionDB,
                "floors",
                new String[]{"ID", "NAME", "IMAGE"},
                new String[]{"ID", AppLocal.getIntString("label.name"), "IMAGE"},
                new Datas[]{Datas.STRING, Datas.STRING, Datas.IMAGE},
                new Formats[]{Formats.NULL, Formats.STRING},
                new int[]{0});
    }

    public TableDefinition getTablePlaces() {
        return new TableDefinition(sessionDB,
                "places",
                new String[]{"ID", "NAME", "SEATS", "X", "Y", "FLOOR"},
                new String[]{"ID", AppLocal.getIntString("label.name"),
                    AppLocal.getIntString("label.seats"),
                    "X", "Y",
                    AppLocal.getIntString("label.placefloor")},
                new Datas[]{Datas.STRING, Datas.STRING, Datas.STRING, Datas.INT, Datas.INT, Datas.STRING},
                new Formats[]{Formats.STRING, Formats.STRING, Formats.STRING, Formats.INT, Formats.INT, Formats.NULL},
                new int[]{0});
    }

    public SentenceList getReservationsList() {
        return new PreparedSentence(sessionDB,
                "SELECT "
                + "R.ID, R.CREATED, R.DATENEW, C.CUSTOMER, customers.TAXID, customers.SEARCHKEY, "
                + "COALESCE(customers.NAME, R.TITLE),  R.CHAIRS, R.ISDONE, R.DESCRIPTION "
                + "FROM reservations R "
                + "LEFT OUTER JOIN reservation_customers C ON R.ID = C.ID "
                + "LEFT OUTER JOIN customers ON C.CUSTOMER = customers.ID "
                + "WHERE R.DATENEW >= ? AND R.DATENEW < ?",
                new SerializerWriteBasic(new Datas[]{Datas.TIMESTAMP, Datas.TIMESTAMP}),
                new SerializerReadBasic(RESERVATION_DATA));
    }

    public SentenceExec getReservationsUpdate() {
        return new SentenceExecTransaction(sessionDB) {
            @Override
            public int execInTransaction(Object[] params) throws BasicException {
                new PreparedSentence(sessionDB,
                        "DELETE FROM reservation_customers WHERE ID = ?",
                        new SerializerWriteBasicExt(RESERVATION_DATA, new int[]{0})).exec(params);

                if (params[3] != null) {
                    new PreparedSentence(sessionDB,
                            "INSERT INTO reservation_customers (ID, CUSTOMER) VALUES (?, ?)",
                            new SerializerWriteBasicExt(RESERVATION_DATA, new int[]{0, 3})).exec(params);
                }
                return new PreparedSentence(sessionDB,
                        "UPDATE reservations SET ID = ?, CREATED = ?, DATENEW = ?, TITLE = ?, CHAIRS = ?, ISDONE = ?, DESCRIPTION = ? WHERE ID = ?",
                        new SerializerWriteBasicExt(RESERVATION_DATA, new int[]{0, 1, 2, 6, 7, 8, 9, 0})).exec(params);
            }
        };
    }

    public SentenceExec getReservationsDelete() {
        return new SentenceExecTransaction(sessionDB) {
            @Override
            public int execInTransaction(Object[] params) throws BasicException {
                new PreparedSentence(sessionDB,
                        "DELETE FROM reservation_customers WHERE ID = ?",
                        new SerializerWriteBasicExt(RESERVATION_DATA, new int[]{0})).exec(params);
                return new PreparedSentence(sessionDB,
                        "DELETE FROM reservations WHERE ID = ?",
                        new SerializerWriteBasicExt(RESERVATION_DATA, new int[]{0})).exec(params);
            }
        };
    }

    public SentenceExec getReservationsInsert() {
        return new SentenceExecTransaction(sessionDB) {
            @Override
            public int execInTransaction(Object[] params) throws BasicException {
                int i = new PreparedSentence(sessionDB,
                        "INSERT INTO reservations (ID, CREATED, DATENEW, TITLE, CHAIRS, ISDONE, DESCRIPTION) VALUES (?, ?, ?, ?, ?, ?, ?)",
                        new SerializerWriteBasicExt(RESERVATION_DATA, new int[]{0, 1, 2, 6, 7, 8, 9})).exec(params);

                if (params[3] != null) {
                    new PreparedSentence(sessionDB,
                            "INSERT INTO reservation_customers (ID, CUSTOMER) VALUES (?, ?)",
                            new SerializerWriteBasicExt(RESERVATION_DATA, new int[]{0, 3})).exec(params);
                }
                return i;
            }
        };
    }

    public void updatePlaces(int x, int y, String id) throws BasicException {
        new StaticSentence(this.sessionDB,
                "UPDATE PLACES SET X = ?, Y = ? WHERE ID = ?",
                new SerializerWriteBasic(new Datas[]{Datas.INT, Datas.INT, Datas.STRING}))
                .exec(new Object[]{x, y, id});
    }
}
