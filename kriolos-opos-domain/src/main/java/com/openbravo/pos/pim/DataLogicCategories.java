package com.openbravo.pos.pim;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.Datas;
import com.openbravo.data.loader.PreparedSentence;
import com.openbravo.data.loader.SentenceExec;
import com.openbravo.data.loader.SentenceList;
import com.openbravo.data.loader.SerializerWriteBasic;
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
 * Data access logic component specifically for product category hierarchy and classification metadata.
 *
 * @author KriolOS
 */
public class DataLogicCategories extends BeanFactoryDataSingle {

    private static final Logger LOGGER = Logger.getLogger(DataLogicCategories.class.getName());

    private Session sessionDB;

    /**
     * Default constructor for BeanFactory initialization.
     */
    public DataLogicCategories() {
    }

    /**
     * Initializes the category data access component with the current database session.
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
     * Returns the table definition for the {@code categories} database table.
     *
     * @return category {@link TableDefinition}
     */
    public TableDefinition getTableCategories() {
        return new TableDefinition(sessionDB,
                "categories",
                new String[]{"ID", "NAME", "PARENTID", "IMAGE", "TEXTTIP", "CATSHOWNAME", "CATORDER",
                    "CATALOGCOLOR",
                    "CATALOGENABLED"},
                new String[]{"ID", AppLocal.getIntString("label.name"), "",
                    AppLocal.getIntString("label.image"), "",
                    "", "", "", ""},
                new Datas[]{Datas.STRING, Datas.STRING, Datas.STRING, Datas.IMAGE, Datas.STRING,
                    Datas.BOOLEAN,
                    Datas.STRING, Datas.STRING, Datas.BOOLEAN},
                new Formats[]{Formats.STRING, Formats.STRING, Formats.STRING, Formats.NULL,
                    Formats.STRING,
                    Formats.BOOLEAN, Formats.STRING, Formats.STRING, Formats.BOOLEAN},
                new int[]{0});
    }

    /**
     * Inserts a new category into the database.
     *
     * @param id       category unique identifier
     * @param name     category display name
     * @param showName whether to display the name on the touch button
     * @throws BasicException on persistence error
     */
    public void createCategory(String id, String name, boolean showName) throws BasicException {
        SentenceExec createCatSentence = new StaticSentence(this.sessionDB,
                "INSERT INTO categories ( ID, NAME, CATSHOWNAME ) "
                + "VALUES (?, ?, ?)",
                new SerializerWriteBasic(new Datas[]{Datas.STRING, Datas.STRING, Datas.BOOLEAN}));
        createCatSentence.exec(new Object[]{id, name, showName});
    }

    /**
     * Inserts a new category into the database using a legacy array.
     *
     * @param category array containing [ID, NAME, CATSHOWNAME]
     * @throws BasicException on persistence error
     * @deprecated Use {@link #createCategory(String, String, boolean)} instead.
     */
    @Deprecated
    public void createCategory(Object[] category) throws BasicException {
        createCategory((String) category[0], (String) category[1], (Boolean) category[2]);
    }

    /**
     * Counts total number of categories in the system.
     *
     * @return total category count
     * @throws BasicException on query error
     */
    public int getCategoriesCount() throws BasicException {
        Integer result = new StaticSentence<Void, Integer>(sessionDB,
                "SELECT COUNT(*) as count FROM categories",
                null,
                com.openbravo.data.loader.SerializerReadInteger.INSTANCE)
                .find();
        return result == null ? 0 : result;
    }

    /**
     * Retrieves category information by ID.
     *
     * @param id category identifier
     * @return CategoryInfo or null if not found
     * @throws BasicException on query error
     */
    public CategoryInfo getCategoryInfo(String id) throws BasicException {
        return new PreparedSentence<String, CategoryInfo>(sessionDB,
                "SELECT "
                + "ID, "
                + "NAME, "
                + "IMAGE, "
                + "TEXTTIP, "
                + "CATSHOWNAME, "
                + "CATORDER, "
                + "CATALOGCOLOR, "
                + "CATALOGENABLED, "
                + "PARENTID "
                + "FROM categories "
                + "WHERE ID = ? "
                + "ORDER BY CATORDER, NAME",
                SerializerWriteString.INSTANCE,
                CategoryInfo.getSerializerRead()).find(id);
    }

    /**
     * Returns sentence query for all categories ordered by name.
     *
     * @return {@link SentenceList} of {@link CategoryInfo}
     */
    public SentenceList<CategoryInfo> getCategoriesList() {
        return new StaticSentence<>(sessionDB,
                "SELECT "
                + "ID, "
                + "NAME, "
                + "IMAGE, "
                + "TEXTTIP, "
                + "CATSHOWNAME, "
                + "CATORDER, "
                + "CATALOGCOLOR, "
                + "CATALOGENABLED, "
                + "PARENTID "
                + "FROM categories "
                + "ORDER BY NAME",
                null,
                CategoryInfo.getSerializerRead());
    }

    /**
     * Retrieves all categories as an in-memory list.
     *
     * @return list of all {@link CategoryInfo}
     */
    public List<CategoryInfo> getCategoriesListAll() {
        List<CategoryInfo> list = null;
        try {
            list = this.getCategoriesList().list();
        } catch (BasicException ex) {
            LOGGER.log(Level.WARNING, "Cannot get categories list", ex);
        }
        return list;
    }

    /**
     * Returns sentence query for root-level categories (PARENTID is null) ordered by name.
     *
     * @return {@link SentenceList} of {@link CategoryInfo}
     */
    public SentenceList<CategoryInfo> getCategoriesList_1() {
        return new StaticSentence<>(sessionDB,
                "SELECT "
                + "ID, "
                + "NAME, "
                + "IMAGE, "
                + "TEXTTIP, "
                + "CATSHOWNAME, "
                + "CATORDER, "
                + "CATALOGCOLOR, "
                + "CATALOGENABLED, "
                + "PARENTID "
                + "FROM categories "
                + "WHERE PARENTID IS NULL "
                + "ORDER BY NAME",
                null,
                CategoryInfo.getSerializerRead());
    }

    /**
     * Retrieves all visible root categories (PARENTID is null and CATSHOWNAME is true).
     *
     * @return list of root {@link CategoryInfo}
     * @throws BasicException on query error
     */
    public List<CategoryInfo> getRootCategories() throws BasicException {
        return new PreparedSentence<Void, CategoryInfo>(sessionDB,
                "SELECT "
                + "ID, "
                + "NAME, "
                + "IMAGE, "
                + "TEXTTIP, "
                + "CATSHOWNAME, "
                + "CATORDER, "
                + "CATALOGCOLOR, "
                + "CATALOGENABLED, "
                + "PARENTID "
                + "FROM categories "
                + "WHERE PARENTID IS NULL AND CATSHOWNAME = " + sessionDB.DB.TRUE()
                + " "
                + "ORDER BY CATORDER, NAME",
                null,
                CategoryInfo.getSerializerRead()).list();
    }

    /**
     * Retrieves child subcategories of a given parent category.
     *
     * @param category parent category identifier
     * @return list of subcategories
     * @throws BasicException on query error
     */
    public List<CategoryInfo> getSubcategories(String category) throws BasicException {
        return new PreparedSentence<String, CategoryInfo>(sessionDB,
                "SELECT "
                + "ID, "
                + "NAME, "
                + "IMAGE, "
                + "TEXTTIP, "
                + "CATSHOWNAME, "
                + "CATORDER, "
                + "CATALOGCOLOR, "
                + "CATALOGENABLED, "
                + "PARENTID "
                + "FROM categories "
                + "WHERE PARENTID = ? "
                + "ORDER BY CATORDER, NAME",
                SerializerWriteString.INSTANCE,
                CategoryInfo.getSerializerRead()).list(category);
    }
}
