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
package com.openbravo.pos.forms;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.Session;
import com.openbravo.pos.util.AltEncrypter;
import java.awt.Component;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

/**
 *
 * @author adrianromero
 */
public class AppViewConnection {

    private static final Logger LOGGER = Logger.getLogger(AppViewConnection.class.getName());
    private static final String DEFAULT_DB = "db";
    private static final int MAX_BD = 10;

    private AppViewConnection() {
    }

    public static String getDefaultDB() {
        return DEFAULT_DB;
    }

    public static Session createSession(AppProperties props) throws BasicException {
       return  createSession(null, props);
    }
    
    public static Session createSession(Component parent, AppProperties props) throws BasicException{
    try {
            String dbURL = "";
            String sDBUser = "";
            String sDBPassword = "";
            DBProperties dbProperties = null;

            String dbID = DEFAULT_DB;
            if ("true".equals(props.getProperty("db.multi"))) {
                
                List<String> dbNames = findAllDB(props);

                //expec db.name=DBMain or db1.name=DBSecond
                String chosedDbName = choseDB(parent, props, dbNames.toArray());
                LOGGER.log(Level.INFO, "Database Selected: "+chosedDbName);
                if(chosedDbName !=  null){
                    String[] dbNameParts = chosedDbName.split("[.]", 0);
                    //get prefix
                    if (dbNameParts != null && dbNameParts.length >= 2) {
                        dbID = dbNameParts[0];
                    }
                }
            }
            dbProperties = getDBProperties(props, dbID);

            sDBUser = dbProperties.sDBUser;
            sDBPassword = dbProperties.sDBPassword;
            dbURL = dbProperties.dbURL;
            
            LOGGER.log(Level.INFO, "Creae session for DB: "+dbURL);

            return createSessionForDB(props, dbID);

        } catch (BasicException ex) {
            throw ex;
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Exception on session ", ex);
            throw new BasicException(AppLocal.getIntString("message.databaseconnectionerror"), ex);
        }
    }

    public static Session createDefaultSession(AppProperties props) throws BasicException {
        return createSessionForDB(props, DEFAULT_DB);
    }

    public static Session createSession(AppProperties props, String dbID) throws BasicException {
        return (dbID != null && !dbID.isBlank())
                ? createSessionForDB(props, dbID)
                : createDefaultSession(props);
    }

    public static Session createSessionForDB(AppProperties props, String dbID) throws BasicException {
        try {
            DBProperties dbProperties = getDBProperties(props, dbID != null ? dbID : DEFAULT_DB);
            String sDBUser = dbProperties.sDBUser;
            String sDBPassword = dbProperties.sDBPassword;
            String dbURL = dbProperties.dbURL;

            LOGGER.log(Level.INFO, "Create session for DB (" + dbID + "): " + dbURL);
            return new Session(dbURL, sDBUser, sDBPassword);
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "Exception on session for DB " + dbID, ex);
            throw new BasicException(AppLocal.getIntString("message.databaseconnectionerror"), ex);
        }
    }

    /**
     * Performs an isolated, direct connection test against the specified database configuration.
     * <p>
     * <b>Behavior Specification:</b>
     * <ul>
     *   <li><b>If Connection OK:</b> Completes silently without popup dialogs, recording an INFO log entry.</li>
     *   <li><b>If Connection Fails:</b> Logs the root cause exception at WARNING level and throws
     *       a {@link BasicException} wrapping the {@link SQLException} so callers can display an error dialog.</li>
     * </ul>
     * </p>
     *
     * @param props the application properties
     * @param dbID the database identifier key, or null for default
     * @throws BasicException if the database connection fails or is invalid
     */
    public static void testConnection(AppProperties props, String dbID) throws BasicException {
        String targetDbId = (dbID != null && !dbID.isBlank()) ? dbID : DEFAULT_DB;
        DBProperties dbProperties = getDBProperties(props, targetDbId);
        String sDBUser = dbProperties.sDBUser;
        String sDBPassword = dbProperties.sDBPassword;
        String dbURL = dbProperties.dbURL;

        LOGGER.log(Level.INFO, "Testing database connection for ({0}): {1}", new Object[]{targetDbId, dbURL});
        try (Connection conn = DriverManager.getConnection(dbURL, sDBUser, sDBPassword)) {
            if (conn == null || !conn.isValid(3)) {
                throw new SQLException("Connection test returned invalid status for URL: " + dbURL);
            }
            LOGGER.log(Level.INFO, "Database connection test successful (silent) for ({0})", targetDbId);
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "Database connection test failed for DB " + targetDbId + ": " + ex.getMessage(), ex);
            throw new BasicException(AppLocal.getIntString("message.databaseconnectionerror"), ex);
        }
    }

    /**
     * Performs an isolated connection test against the default database configuration.
     *
     * @param props the application properties
     * @throws BasicException if the default database connection fails
     */
    public static void testConnection(AppProperties props) throws BasicException {
        testConnection(props, DEFAULT_DB);
    }

    public static List<String> findAllDB(AppProperties props) {

        List<String> dbNames = new ArrayList<>();

        var defaulName = props.getProperty(DEFAULT_DB + ".name");
        dbNames.add(DEFAULT_DB + ".name=" + (defaulName != null ? defaulName : "Default DB"));

        if ("true".equals(props.getProperty(DEFAULT_DB + ".multi"))) {
            for (int count = 1; count < MAX_BD; count++) {

                String curDbKey = DEFAULT_DB + count + ".name";
                String curName = props.getProperty(curDbKey);

                if (curName != null) {
                    dbNames.add(curDbKey + "=" + curName);
                }
            }
        }

        return dbNames;
    }

    public static String choseDB(Component parent, AppProperties props, Object[] dbs) {

        ImageIcon icon = new ImageIcon("/com/openbravo/images/app_logo_48x48");
        Object chosedDbName = JOptionPane.showInputDialog(
                parent,
                AppLocal.getIntString("message.databasechoose"),
                "Database Selection",
                JOptionPane.OK_OPTION,
                icon,
                dbs,
                dbs != null && dbs.length > 0 ? dbs[0] : null);

        return (String) chosedDbName;
    }

    private static DBProperties getDBProperties(AppProperties props, String dbID) {

        final DBProperties dbProps = new DBProperties();

        dbProps.sDBUser = props.getProperty(dbID + ".user");
        dbProps.sDBPassword = props.getProperty(dbID + ".password");
        if (dbProps.sDBUser != null && dbProps.sDBPassword != null && dbProps.sDBPassword.startsWith("crypt:")) {
            AltEncrypter cypher = new AltEncrypter("cypherkey" + dbProps.sDBUser);
            dbProps.sDBPassword = cypher.decrypt(dbProps.sDBPassword.substring(6));
        }

        String rawUrl = props.getProperty(dbID + ".URL");
        if (rawUrl == null) {
            rawUrl = props.getProperty(dbID + ".url", "");
        }
        String schema = props.getProperty(dbID + ".schema", "");
        String options = props.getProperty(dbID + ".options", "");
        dbProps.dbURL = (rawUrl != null ? rawUrl : "")
                + (schema != null ? schema : "")
                + (options != null ? options : "");

        return dbProps;
    }

    private static class DBProperties {

        public String dbURL = null;
        public String sDBUser = null;
        public String sDBPassword = null;

        public String toString() {
            return "URL: " + dbURL + ", USER: " + sDBUser + ", PASS: " + sDBPassword;
        }
    }
}
