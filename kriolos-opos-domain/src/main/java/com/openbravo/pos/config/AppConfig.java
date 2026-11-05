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
package com.openbravo.pos.config;

import com.openbravo.basic.BasicException;
import com.openbravo.basic.AppLocal;
import com.openbravo.pos.spi.config.AppProperties;
import com.openbravo.app.config.LegacyBootstrapConfiguration;
import com.openbravo.pos.spi.config.BootstrapConfig;
import java.awt.Component;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

/**
 * Legacy application configuration facade extending {@link LegacyBootstrapConfiguration}.
 * <p>
 * Business settings and peripheral configurations are moving to the database-backed
 * {@link ApplicationConfigurationService} as part of Issue #74.
 *
 * @author JG uniCenta, KriolOS POS
 */
public class AppConfig extends LegacyBootstrapConfiguration implements AppProperties {

    private static final Logger LOGGER = Logger.getLogger(AppConfig.class.getName());

    private static volatile AppConfig INSTANCE;

    /**
     * Configuration file
     *
     * @param configfile resource file
     */
    public AppConfig(File configfile) {
        super(configfile);
    }


    public List<DatabaseConfig> getAll() {
        return getDatabaseConfigs().stream()
                .map(d -> new DatabaseConfig(d.name(), d.url(), d.username(), d.password()))
                .toList();
    }


    public DatabaseConfig getPrimary() {
        var p = getPrimaryDatabaseConfig();
        return (p != null) ? new DatabaseConfig(p.name(), p.url(), p.username(), p.password()) : null;
    }

    public synchronized static AppConfig getInstance() {
        AppConfig m_inst = INSTANCE;
        if (m_inst == null) {
            synchronized (AppConfig.class) {
                m_inst = INSTANCE;
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
    /**
     * Get instance settings
     */
    @Override
    public void load() {
        File file = getConfigFile();
        LOGGER.log(Level.INFO, "Try Loading configuration file: {0}", file.getAbsolutePath());
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

    public void setFactoryConfig() {
        this.properties = defaultConfig();
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
        String defaultDBURL = "jdbc:sqlite:" + Paths.get(baseDirectory.getAbsolutePath(), "kriolopos.db").toString();
        String defaultDBURL1 = "jdbc:sqlite:" + Paths.get(baseDirectory.getAbsolutePath(), "kriolopos_backup.db").toString();
        propConfig.setProperty(ConfigProperty.DB_URL.getName(), defaultDBURL);
        propConfig.setProperty(ConfigProperty.DB1_URL.getName(), defaultDBURL1);

        propConfig.setProperty(ConfigProperty.MACHINE_HOSTNAME.getName(), getHost());

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
     *
     * @param dbConfig the database configuration
     * @throws BasicException if the database connection fails or is invalid
     */
    public static void testConnection(DatabaseConfig dbConfig) throws BasicException {
        testConnection(dbConfig.name(), dbConfig.url(), dbConfig.username(), dbConfig.password());
    }

    private static void testConnection(String name, String url, String username, String password) throws BasicException {
        LOGGER.log(Level.INFO, "Testing database connection for ({0}): {1}", new Object[]{name, url});
        try (Connection conn = DriverManager.getConnection(url, username, password)) {
            if (conn == null || !conn.isValid(3)) {
                throw new SQLException("Connection test returned invalid status for URL: " + url);
            }
            LOGGER.log(Level.INFO, "Database connection test successful (silent) for ({0})", url);
        }
        catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "Database connection test failed for DB " + url + ": " + ex.getMessage(), ex);
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
}

enum ConfigProperty {

    // THEME & INITIAL
    POS_UI_THEME_ID("pos.ui.theme.id", "system"),
    LOCATION_CONFIGURE("localization.configure", "legacy"),
    LOCATION_LOCALE("localization.locale", ""),
    // CORE OVERRIDES
    OVERRIDE_CHECK("override.check", "false"),
    OVERRIDE_PIN("override.pin", ""),
    // DB DRIVERS
    DB_ENGINE("db.engine", ""),
    DB_DRIVER("db.driver", ""),
    // PRIMARY DB (Dynamic URL)
    DB_NAME("db.name", "DB 0 (SQLite)"),
    DB_URL("db.URL", "jdbc:sqlite:kriolopos.db"), // Dynamic
    DB_SCHEMA("db.schema", ""),
    DB_OPTIONS("db.options", ""),
    DB_USER("db.user", ""),
    DB_PASSWORD("db.password", ""),
    // SECONDARY DB        
    DB1_NAME("db.1.name", "DB 1 (Sqlite)"),
    DB1_URL("db.1.URL", "jdbc:sqlite:kriolopos_backup.db"),
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
    //
    PRICE_WITH_ZERO("pricewith00", "false"),
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
