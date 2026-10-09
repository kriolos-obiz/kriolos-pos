package com.openbravo.pos.pim;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.Datas;
import com.openbravo.data.loader.PreparedSentence;
import com.openbravo.data.loader.SentenceList;
import com.openbravo.data.loader.SerializerWriteString;
import com.openbravo.data.loader.Session;
import com.openbravo.data.loader.StaticSentence;
import com.openbravo.data.loader.TableDefinition;
import com.openbravo.format.Formats;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.BeanFactoryDataSingle;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data access logic component specifically for Units of Measure (UOM) characterizing products.
 *
 * @author KriolOS
 */
public class DataLogicUom extends BeanFactoryDataSingle {

    private static final Logger LOGGER = Logger.getLogger(DataLogicUom.class.getName());

    private Session sessionDB;

    /**
     * Default constructor for BeanFactory initialization.
     */
    public DataLogicUom() {
    }

    /**
     * Initializes the UOM data access component with the current database session.
     *
     * @param sessionDB active database session
     */
    @Override
    public void init(Session sessionDB) {
        this.sessionDB = sessionDB;
    }

    /**
     * Returns the underlying database session.
     *
     * @return current {@link Session}
     */
    public Session getSession() {
        return sessionDB;
    }

    /**
     * Returns the table definition for the {@code uom} database table.
     *
     * @return UOM {@link TableDefinition}
     */
    public TableDefinition getTableUom() {
        return new TableDefinition(sessionDB,
                "uom",
                new String[]{"id", "name"},
                new String[]{"id", AppLocal.getIntString("label.name")},
                new Datas[]{Datas.STRING, Datas.STRING},
                new Formats[]{Formats.STRING, Formats.STRING},
                new int[]{0});
    }

    /**
     * Retrieves Unit of Measure information by ID.
     *
     * @param id UOM identifier
     * @return UomInfo or null if not found
     * @throws BasicException on query error
     */
    public UomInfo getUomInfoById(String id) throws BasicException {
        return (UomInfo) new PreparedSentence(sessionDB,
                "SELECT id, name FROM uom WHERE id = ?",
                SerializerWriteString.INSTANCE, UomInfo.getSerializerRead()).find(id);
    }

    /**
     * Returns sentence query for all Units of Measure ordered by name.
     *
     * @return {@link SentenceList} of {@link UomInfo}
     */
    public SentenceList<UomInfo> getUomList() {
        return new StaticSentence<>(sessionDB, "SELECT ID, NAME FROM uom ORDER BY NAME", null,
                UomInfo.getSerializerRead());
    }

    /**
     * Retrieves all Units of Measure as an in-memory list.
     *
     * @return list of all {@link UomInfo}
     */
    public List<UomInfo> getUomListAll() {
        List<UomInfo> list = null;
        try {
            list = this.getUomList().list();
        } catch (BasicException ex) {
            LOGGER.log(Level.WARNING, "Cannot get UomInfo list", ex);
        }
        return list;
    }
}
