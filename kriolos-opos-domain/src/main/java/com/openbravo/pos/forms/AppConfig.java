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
//    along with this program.  If not, see <http://www.gnu.org/licenses/>
package com.openbravo.pos.forms;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.Session;
import com.openbravo.pos.util.AltEncrypter;
import java.awt.Component;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

/**
 * Creation and Editing of stored settings
 *
 * @author JG uniCenta
 */
public class AppConfig implements AppProperties {

    private static final Logger LOGGER = Logger.getLogger(AppConfig.class.getName());

    private static volatile AppConfig INSTANCE;
    private Properties properties;
    private File configfile;

    private static final String APP_CONFIG_FILE_NAME = AppLocal.APP_ID + ".properties";

    /**
     * Configuration file
     *
     * @param configfile resource file
     */
    public AppConfig(File configfile) {
        if (configfile != null) {
            this.configfile = configfile;
        } else {
            this.configfile = getDefaultConfigFile();
        }
        this.properties = new SortedStoreProperties();
    }

    private static File getBaseApplicationDataDirectory() {

        String os = System.getProperty("os.name").toLowerCase();
        String userHome = System.getProperty("user.home");
        // Cross-Platform data directory
        String baseDirectory;

        if (os.contains("win")) {
            // Windows: %APPDATA%/KriolOS/data
            baseDirectory = Paths.get(System.getenv("APPDATA"), "KriolOS").toString();
        } else if (os.contains("mac")) {
            // macOS: ~/Library/Application Support/KriolOS/data
            baseDirectory = Paths.get(userHome, "Library", "Application Support", "KriolOS").toString();
        } else {
            // XDG_DATA_HOME is an environment variable used by Unix-like operating systems
            // to determine where applications should store user-specific data files
            // Linux/Unix (ex. of XDG_DATA_HOME): ~/.config/kriolos/

            //Linux/Unix DEFAULT: ~/.local/share/kriolos
            String xdgData = System.getenv("XDG_DATA_HOME");
            baseDirectory = (xdgData != null && !xdgData.isEmpty())
                    // XDG_DATA_HOME
                    ? Paths.get(xdgData, "kriolos").toString()
                    //DEFAULT
                    : Paths.get(userHome, ".local", "share", "kriolos").toString();
        }

        // Directory must exist
        File configDir = new File(baseDirectory);
        if (!configDir.exists()) {
            configDir.mkdirs();
        }

        LOGGER.info("Application Data Directory: " + baseDirectory);

        return configDir;
    }

    private static File getDefaultConfigFile() {
        // 1. Try to get custom config file from system property or environment variable
        String customPath = System.getProperty("kriolos.config");
        if (customPath == null || customPath.isBlank()) {
            customPath = System.getProperty("app.config");
        }
        if (customPath == null || customPath.isBlank()) {
            customPath = System.getProperty("config.file");
        }
        if (customPath == null || customPath.isBlank()) {
            customPath = System.getenv("KRIOLOS_CONFIG");
        }
        if (customPath == null || customPath.isBlank()) {
            customPath = System.getenv("APP_CONFIG");
        }

        if (customPath != null && !customPath.isBlank()) {
            File customFile = new File(customPath.trim());
            if (customFile.isDirectory()) {
                return new File(customFile, APP_CONFIG_FILE_NAME);
            }
            LOGGER.info("Using custom configuration file from system variable: " + customFile.getAbsolutePath());
            return customFile;
        }

        File baseDirectory = getBaseApplicationDataDirectory();
        return new File(baseDirectory, APP_CONFIG_FILE_NAME);
    }

    public String getAppDataDirectory() {
        return getBaseApplicationDataDirectory().getAbsolutePath();
    }

    /**
     * Get key pair value from properties resource (with System property
     * override fallback)
     *
     * @param sKey key pair value
     * @return key pair from .properties filename
     */
    @Override
    public String getProperty(String sKey) {
        String sysVal = System.getProperty(sKey);
        if (sysVal != null && !sysVal.isBlank()) {
            return sysVal;
        }
        return properties.getProperty(sKey);
    }

    @Override
    public String getProperty(String sKey, String defaultValue) {
        String sysVal = System.getProperty(sKey);
        if (sysVal != null && !sysVal.isBlank()) {
            return sysVal;
        }
        return properties.getProperty(sKey, defaultValue);
    }

    /**
     *
     * @return Machine name
     */
    @Override
    public String getHost() {
        return getProperty("machine.hostname");
    }

    /**
     *
     * @return .properties filename
     */
    @Override
    public File getConfigFile() {
        return configfile;
    }

    public String getTicketHeaderLine1() {
        return getProperty("tkt.header1");
    }

    public String getTicketHeaderLine2() {
        return getProperty("tkt.header2");
    }

    public String getTicketHeaderLine3() {
        return getProperty("tkt.header3");
    }

    public String getTicketHeaderLine4() {
        return getProperty("tkt.header4");
    }

    public String getTicketHeaderLine5() {
        return getProperty("tkt.header5");
    }

    public String getTicketHeaderLine6() {
        return getProperty("tkt.header6");
    }

    public String getTicketFooterLine1() {
        return getProperty("tkt.footer1");
    }

    public String getTicketFooterLine2() {
        return getProperty("tkt.footer2");
    }

    public String getTicketFooterLine3() {
        return getProperty("tkt.footer3");
    }

    public String getTicketFooterLine4() {
        return getProperty("tkt.footer4");
    }

    public String getTicketFooterLine5() {
        return getProperty("tkt.footer5");
    }

    public String getTicketFooterLine6() {
        return getProperty("tkt.footer6");
    }

    /**
     * Update .properties resource key pair values
     *
     * @param sKey key pair left side
     * @param sValue key pair right side value
     */
    public void setProperty(String sKey, String sValue) {
        if (sValue == null) {
            properties.remove(sKey);
        } else {
            properties.setProperty(sKey, sValue);
        }
    }

    /**
     * Local machine identity
     *
     * @return Machine name from OS
     */
    private String getLocalHostName() {
        try {
            return java.net.InetAddress.getLocalHost().getHostName();
        }
        catch (java.net.UnknownHostException eUH) {
            return "localhost";
        }
    }

    public synchronized static AppConfig getInstance() {
        AppConfig m_inst = INSTANCE;

        //Double check locking pattern
        //Check for the first time
        if (m_inst == null) {

            synchronized (AppConfig.class) {
                m_inst = INSTANCE;
                //if there is no instance available... create new one
                if (m_inst == null) {
                    INSTANCE = m_inst = new AppConfig(getDefaultConfigFile());
                }
            }
        }

        return INSTANCE;
    }

    /**
     * Initializes or returns the singleton AppConfig with a specific
     * configuration file.
     *
     * @param configFile the specific configuration file to use
     * @return the AppConfig singleton instance
     */
    public synchronized static AppConfig getInstance(File configFile) {
        if (configFile != null) {
            synchronized (AppConfig.class) {
                INSTANCE = new AppConfig(configFile);
                return INSTANCE;
            }
        }
        return getInstance();
    }

    /**
     * Explicitly reinitializes the singleton instance with a given
     * configuration file.
     *
     * @param configFile the configuration file
     * @return the newly initialized AppConfig instance
     */
    public synchronized static AppConfig init(File configFile) {
        synchronized (AppConfig.class) {
            INSTANCE = new AppConfig(configFile);
            return INSTANCE;
        }
    }

    public Boolean getBoolean(String sKey) {
        return Boolean.valueOf(properties.getProperty(sKey));
    }

    public void setBoolean(String sKey, Boolean sValue) {
        if (sValue == null) {
            properties.remove(sKey);
        } else if (sValue) {
            properties.setProperty(sKey, "true");
        } else {
            properties.setProperty(sKey, "false");
        }
    }

    /**
     *
     * @return Delete .properties filename
     */
    public boolean delete() {
        return configfile.delete();
    }

    /**
     * Get instance settings
     */
    public void load() {
        File file = getConfigFile();
        LOGGER.log(Level.INFO, "Try Loading configuration file: {0}", configfile.getAbsolutePath());
        try {
            if (file.exists() && file.isFile()) {
                try (InputStream in = new FileInputStream(file)) {
                    properties.load(in);
                }
            } else {
                properties = defaultConfig();
            }

            migrateDBProperties2026();
            save();

        }
        catch (IOException e) {
            LOGGER.log(Level.WARNING, MessageFormat.format("IOException on load configuration file: {0}", file.getAbsolutePath()), e);
            try {
                LOGGER.log(Level.INFO, "Providing default configuration: ", e);
                properties = defaultConfig();
            }
            catch (Exception ex) {
                LOGGER.log(Level.WARNING, "Fail getting default/factory configuration", ex);
            }
        }
    }

    /**
     *
     * @return 0 or 00 number keypad boolean true/false
     */
    public Boolean isPriceWith00() {
        String prop = getProperty("pricewith00");
        if (prop == null) {
            return false;
        } else {
            return prop.equals("true");
        }
    }

    /**
     * Save values to properties file
     *
     * @throws java.io.IOException explicit on OS
     */
    public void save() throws IOException {

        LOGGER.log(Level.INFO, "Saving configuration to file: {0}", configfile.getAbsolutePath());
        try (OutputStream out = new FileOutputStream(configfile)) {
            properties.store(out, AppLocal.APP_NAME + ". Configuration file.");
        }
        catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Fail saving configuration to file: " + configfile.getAbsolutePath(), ex);
        }
    }

    public void setFactoryConfig() {
        this.properties = defaultConfig();
    }

    public DatabaseConfig getPrimary() {
        if (properties == null) {
            return null;
        }

        String name = properties.getProperty(ConfigProperty.DB_NAME.getName());
        String url = properties.getProperty(ConfigProperty.DB_URL.getName());
        String user = properties.getProperty(ConfigProperty.DB_USER.getName());
        String password = properties.getProperty(ConfigProperty.DB_PASSWORD.getName());
        String decryptPass = decryptPassword(user, password);

        return new DatabaseConfig(name, url, user, decryptPass);
    }

    @Override
    public List<DatabaseConfig> getAll() {
        List<DatabaseConfig> dbs = new ArrayList<>();
        if (properties == null) {
            return dbs;
        }

        // 1. Adiciona a primária primeiro
        DatabaseConfig primary = getPrimary();
        if (primary != null) {
            dbs.add(primary);
        }

        // 2. Descobre todos os {DB_ID} dinamicamente a partir das propriedades carregadas
        Set<String> dbIds = new HashSet<>();
        for (String key : properties.stringPropertyNames()) {
            // Verifica se a chave cumpre o padrão "db.{id}.name"
            if (key.startsWith("db.") && key.endsWith(".name") && !key.equals("db.name")) {
                // Extrai o {DB_ID} (o texto entre o primeiro e o último ponto)
                String dbId = key.substring(3, key.length() - 5);
                dbIds.add(dbId);
            }
        }

        // 3. Reconstrói os records secundários usando os IDs encontrados
        for (String dbId : dbIds) {
            String name = properties.getProperty("db." + dbId + ".name");
            String url = properties.getProperty("db." + dbId + ".URL");
            String user = properties.getProperty("db." + dbId + ".user");
            String password = properties.getProperty("db." + dbId + ".password");
            
            String decryptPass = decryptPassword(user, password);

            dbs.add(new DatabaseConfig(name, url, user, decryptPass));
        }

        return dbs;
    }

    /**
     * Migrates secondary database properties from the legacy format to the new
     * 2026 format. Example conversions: - db1.name -> db.1.name - dbmaria.URL
     * -> db.maria.URL
     */
    private void migrateDBProperties2026() {
        if (properties == null) {
            return;
        }

        // Temporary list to track legacy keys for safe removal after iteration
        List<String> keysToRemove = new ArrayList<>();

        // List of valid database configuration property suffixes
        String[] suffixes = {".name", ".URL", ".user", ".password", ".schema", ".options"};

        for (String key : properties.stringPropertyNames()) {
            // Skip the primary database properties as they already match the correct format
            if (isPrimaryKey(key)) {
                continue;
            }

            // Target legacy keys starting with "db" and ending with standard suffixes
            if (key.startsWith("db")) {
                for (String suffix : suffixes) {
                    if (key.endsWith(suffix)) {
                        // Extract the legacy database ID (e.g., "1" from "db1.name", "maria" from "dbmaria.name")
                        String dbId = key.substring(2, key.length() - suffix.length());

                        // If the ID does not start with a dot, it needs migration
                        if (!dbId.startsWith(".")) {
                            String value = properties.getProperty(key);
                            String newKey = "db." + dbId + suffix;

                            // Save the value under the new key structure and queue the old key for deletion
                            properties.setProperty(newKey, value);
                            keysToRemove.add(key);
                        }
                        break; // Move to the next property once matched
                    }
                }
            }
        }

        // Clean up legacy keys to prevent duplicate properties
        for (String oldKey : keysToRemove) {
            properties.remove(oldKey);
        }
    }

    /**
     * Helper method to identify and protect primary database configuration
     * keys.
     */
    private boolean isPrimaryKey(String key) {
        return key.equals("db.name") || key.equals("db.URL")
                || key.equals("db.user") || key.equals("db.password")
                || key.equals("db.schema") || key.equals("db.options");
    }

    private Properties defaultConfig() {

        LOGGER.log(Level.INFO, "Default configuration");

        Properties propConfig = new SortedStoreProperties();

        // 1. Load all defaults from the enum
        for (ConfigProperty prop : ConfigProperty.values()) {
            propConfig.setProperty(prop.getName(), prop.getDefaultValue());
        }

        // 2. Overwrite / Populate dynamic runtime values safely using the enum keys
        File baseDirectory = getBaseApplicationDataDirectory();
        String defaultDBURL = "jdbc:hsqldb:file:" + Paths.get(baseDirectory.getAbsolutePath(), "kriolopos.hsqldb").toString();
        String defaultDBURL1 = "jdbc:sqlite:file:" + Paths.get(baseDirectory.getAbsolutePath(), "kriolopos.db").toString();
        propConfig.setProperty(ConfigProperty.DB_URL.getName(), defaultDBURL);
        propConfig.setProperty(ConfigProperty.DB1_URL.getName(), defaultDBURL1);

        propConfig.setProperty(ConfigProperty.MACHINE_HOSTNAME.getName(), getLocalHostName());

        // Localization - Legacy 
        Locale l = Locale.getDefault();
        propConfig.setProperty(ConfigProperty.USER_LANGUAGE.getName(), l.getLanguage());
        propConfig.setProperty(ConfigProperty.USER_COUNTRY.getName(), l.getCountry());
        propConfig.setProperty(ConfigProperty.USER_VARIANT.getName(), l.getVariant());

        return propConfig;
    }

    /**
     * Performs an isolated, direct connection test against the specified
     * database configuration.
     * <p>
     * <b>Behavior Specification:</b>
     * <ul>
     * <li><b>If Connection OK:</b> Completes silently without popup dialogs,
     * recording an INFO log entry.</li>
     * <li><b>If Connection Fails:</b> Logs the root cause exception at WARNING
     * level and throws a {@link BasicException} wrapping the
     * {@link SQLException} so callers can display an error dialog.</li>
     * </ul>
     * </p>
     *
     * @param props the application properties
     * @param dbID the database identifier key, or null for default
     * @throws BasicException if the database connection fails or is invalid
     */
    public static void testConnection(DatabaseConfig dbConfig) throws BasicException {

        LOGGER.log(Level.INFO, "Testing database connection for ({0}): {1}", new Object[]{dbConfig.name(), dbConfig.url()});
        try (Connection conn = DriverManager.getConnection(dbConfig.url(), dbConfig.username(), dbConfig.password())) {
            if (conn == null || !conn.isValid(3)) {
                throw new SQLException("Connection test returned invalid status for URL: " + dbConfig.url());
            }
            LOGGER.log(Level.INFO, "Database connection test successful (silent) for ({0})", dbConfig.url());
        }
        catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "Database connection test failed for DB " + dbConfig.url() + ": " + ex.getMessage(), ex);
            throw new BasicException(AppLocal.getIntString("message.databaseconnectionerror"), ex);
        }
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

    private String decryptPassword(String username, String passEncrypted) {
        String sDBPassword = passEncrypted;
        if (username != null && passEncrypted != null && passEncrypted.startsWith("crypt:")) {
            AltEncrypter cypher = new AltEncrypter("cypherkey" + username);
            sDBPassword = cypher.decrypt(passEncrypted.substring(6));
        }
        
        return sDBPassword;
    }
}

enum ConfigProperty {

    // THEME & INITIAL
    POS_UI_THEME_ID("pos.ui.theme.id", "system"),
    LOCATION_CONFIGURE("localization.configure", "legacy"),
    LOCATION_LOCALE("localization.locale", ""),
    // CORE OVERRIDES
    DB_MULTI("db.multi", "true"),
    OVERRIDE_CHECK("override.check", "false"),
    OVERRIDE_PIN("override.pin", ""),
    // DB DRIVERS
    DB_DRIVERLIB("db.driverlib", ""),
    DB_ENGINE("db.engine", ""),
    DB_DRIVER("db.driver", ""),
    // PRIMARY DB (Dynamic URL)
    DB_NAME("db.name", "DB 0 ()"),
    DB_URL("db.URL", "jdbc:hsqldb:file:.local/share/kriolos/kriolopos;sql.syntax_mys=true"), // Dynamic
    DB_SCHEMA("db.schema", "kriolopos"),
    DB_OPTIONS("db.options", ";shutdown=true"),
    DB_USER("db.user", "kriolopos"),
    DB_PASSWORD("db.password", "kriolopos"),
    // SECONDARY DB        
    DB1_NAME("db.1.name", "DB 1 (Sqlite)"),
    DB1_URL("db.1.URL", "jdbc:sqlite:.local/share/kriolos/kriolopos.db"),
    DB1_SCHEMA("db.1.schema", "kriolopos"),
    DB1_OPTIONS("db.1.options", ""),
    DB1_USER("db.1.user", "kriolopos"),
    DB1_PASSWORD("db.1.password", "kriolopos"),
    // SECONDARY DB        
    DB2_NAME("db.2.name", "DB 3 (MariaDB)"),
    DB2_URL("db.2.URL", "jdbc:mariadb:localhost:3306/kriolospos?characterEncoding=utf8"),
    DB3_SCHEMA("db.2.schema", "kriolopos"),
    DB2_OPTIONS("db.2.options", "?zeroDateTimeBehavior=convertToNull"),
    DB2_USER("db.2.user", "kriolopos"),
    DB2_PASSWORD("db.2.password", "kriolopos"),
    
    // MACHINE INFO (Dynamic Hostname)
    MACHINE_HOSTNAME("machine.hostname", ""), // Dynamic

    // LOCALIZATION LEGACY (Dynamic Locale)
    USER_LANGUAGE("user.language", ""), // Dynamic
    USER_COUNTRY("user.country", ""), // Dynamic
    USER_VARIANT("user.variant", ""), // Dynamic

    // PRINTERS
    MACHINE_PRINTER("machine.printer", "screen"),
    MACHINE_PRINTER_2("machine.printer.2", "Not defined"),
    MACHINE_PRINTER_3("machine.printer.3", "Not defined"),
    MACHINE_PRINTER_4("machine.printer.4", "Not defined"),
    MACHINE_PRINTER_5("machine.printer.5", "Not defined"),
    MACHINE_PRINTER_6("machine.printer.6", "Not defined"),
    // MACHINE HARDWARE & BEHAVIOR
    MACHINE_DISPLAY("machine.display", "screen"),
    MACHINE_SCALE("machine.scale", "Not defined"),
    MACHINE_SCREENMODE("machine.screenmode", "fullscreen"),
    MACHINE_TICKETSBAG("machine.ticketsbag", "standard"),
    MACHINE_SCANNER("machine.scanner", "Not defined"),
    MACHINE_IBUTTON("machine.iButton", "false"),
    MACHINE_IBUTTONRESPONSE("machine.iButtonResponse", "5"),
    MACHINE_UNIQUEINSTANCE("machine.uniqueinstance", "true"),
    // PAYMENT
    PAYMENT_GATEWAY("payment.gateway", "external"),
    PAYMENT_MAGCARDREADER("payment.magcardreader", "Not defined"),
    PAYMENT_TESTMODE("payment.testmode", "true"),
    PAYMENT_COMMERCEID("payment.commerceid", ""),
    PAYMENT_COMMERCEPASSWORD("payment.commercepassword", "password"),
    // PRINTER DISPLAY CONFIG
    MACHINE_PRINTERNAME("machine.printername", "(Default)"),
    SCREEN_RECEIPT_COLUMNS("screen.receipt.columns", "42"),
    // RECEIPT PAPER
    PAPER_RECEIPT_X("paper.receipt.x", "10"),
    PAPER_RECEIPT_Y("paper.receipt.y", "10"),
    PAPER_RECEIPT_WIDTH("paper.receipt.width", "190"),
    PAPER_RECEIPT_HEIGHT("paper.receipt.height", "546"),
    PAPER_RECEIPT_MEDIASIZENAME("paper.receipt.mediasizename", "A4"),
    // STANDARD PAPER
    PAPER_STANDARD_X("paper.standard.x", "72"),
    PAPER_STANDARD_Y("paper.standard.y", "72"),
    PAPER_STANDARD_WIDTH("paper.standard.width", "451"),
    PAPER_STANDARD_HEIGHT("paper.standard.height", "698"),
    PAPER_STANDARD_MEDIASIZENAME("paper.standard.mediasizename", "A4"),
    // TICKET HEADERS
    TKT_HEADER1("tkt.header1", "KriolOS POS"),
    TKT_HEADER2("tkt.header2", "Open Source Point Of Sale"),
    TKT_HEADER3("tkt.header3", "Copyright (c) 2020-2023 KriolOS"),
    TKT_HEADER4("tkt.header4", "Change header text in Configuration"),
    // TICKET FOOTERS
    TKT_FOOTER1("tkt.footer1", "Change footer text in Configuration"),
    TKT_FOOTER2("tkt.footer2", "Thank you for your custom"),
    TKT_FOOTER3("tkt.footer3", "Please Call Again"),
    // UI DETAILS
    TABLE_SHOWCUSTOMERDETAILS("table.showcustomerdetails", "true"),
    TABLE_CUSTOMERCOLOUR("table.customercolour", "#58B000"),
    TABLE_SHOWWAITERDETAILS("table.showwaiterdetails", "true"),
    TABLE_WAITERCOLOUR("table.waitercolour", "#258FB0"),
    TABLE_TABLECOLOUR("table.tablecolour", "#D62E52"),
    TILL_AMOUNTATTOP("till.amountattop", "true"),
    TILL_HIDEINFO("till.hideinfo", "true");

    private final String name;
    private final String defaultValue;

    ConfigProperty(String name, String defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue;
    }

    public String getName() {
        return name;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    @Override
    public String toString() {
        return this.name;
    }
}

class SortedStoreProperties extends Properties {

    private static final long serialVersionUID = 1L;

    @Override
    public void store(OutputStream out, String comments) throws IOException {
        Properties sortedProps;
        sortedProps = new Properties() {
            @Override
            public Set<Map.Entry<Object, Object>> entrySet() {

                Set<Map.Entry<Object, Object>> sortedSet = new TreeSet<>(new Comparator<Map.Entry<Object, Object>>() {
                    @Override
                    public int compare(Map.Entry<Object, Object> o1, Map.Entry<Object, Object> o2) {
                        return o1.getKey().toString().compareTo(o2.getKey().toString());
                    }
                }
                );
                sortedSet.addAll(super.entrySet());
                return sortedSet;
            }

            @Override
            public Set<Object> keySet() {
                return new TreeSet<>(super.keySet());
            }

            @Override
            public synchronized Enumeration<Object> keys() {
                return Collections.enumeration(new TreeSet<>(super.keySet()));
            }

        };
        sortedProps.putAll(this);
        sortedProps.store(out, comments);
    }
}
