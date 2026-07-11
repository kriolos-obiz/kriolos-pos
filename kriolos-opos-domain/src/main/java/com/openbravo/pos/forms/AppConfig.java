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

import com.openbravo.format.Formats;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

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

    private static File getBaseApplicationDataDirectory(){

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

        File baseDirectory = getBaseApplicationDataDirectory();
        return new File(baseDirectory, APP_CONFIG_FILE_NAME);
    }


    public String getAppDataDirectory() {
        return getBaseApplicationDataDirectory().getAbsolutePath();
    }

    /**
     * Get key pair value from properties resource
     *
     * @param sKey key pair value
     * @return key pair from .properties filename
     */
    @Override
    public String getProperty(String sKey) {
        return properties.getProperty(sKey);
    }


    @Override
    public String getProperty(String sKey, String defaultValue) {
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
        } catch (java.net.UnknownHostException eUH) {
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

            //File not empty
            if (getProperty("db.URL") == null) {
                properties = defaultConfig();
                save();
            }
        } else {
            properties = defaultConfig();
            save();
        }
    }catch (IOException e) {
        LOGGER.log(Level.WARNING, MessageFormat.format("IOException on load configuration file: {0}", file.getAbsolutePath()), e);
        try {
            LOGGER.log(Level.INFO, "Providing default configuration: ", e);
            properties = defaultConfig();
        } catch (Exception ex) {
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
        try ( OutputStream out = new FileOutputStream(configfile)) {
            properties.store(out, AppLocal.APP_NAME + ". Configuration file.");
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Fail saving configuration to file: " + configfile.getAbsolutePath(), ex);
        }
    }

    public void setFactoryConfig() {
        this.properties = defaultConfig();
    }

    private Properties defaultConfig() {

        LOGGER.log(Level.INFO, "Default configuration");

        Properties propConfig = new SortedStoreProperties();

        File baseDirectory = getBaseApplicationDataDirectory();

        propConfig.setProperty("db.multi", "false");
        propConfig.setProperty("override.check", "false");
        propConfig.setProperty("override.pin", "");

        propConfig.setProperty("db.driverlib", "");
        propConfig.setProperty("db.engine", "");
        propConfig.setProperty("db.driver", "");

// primary DB
        propConfig.setProperty("db.name", "Main DB");
        propConfig.setProperty("db.URL", "jdbc:hsqldb:file:"+Paths.get(baseDirectory.getAbsolutePath(), "kriolopos").toString());
        propConfig.setProperty("db.schema", "kriolopos");
        propConfig.setProperty("db.options", ";shutdown=true");
        propConfig.setProperty("db.user", "kriolopos");
        propConfig.setProperty("db.password", "kriolopos");

// secondary DB        
        propConfig.setProperty("db1.name", "");
        propConfig.setProperty("db1.URL", "jdbc:mysql://localhost:3306/");
        propConfig.setProperty("db1.schema", "kriolopos");
        propConfig.setProperty("db1.options", "?zeroDateTimeBehavior=convertToNull");
        propConfig.setProperty("db1.user", "kriolopos");
        propConfig.setProperty("db1.password", "kriolopos");

        propConfig.setProperty("machine.hostname", getLocalHostName());

        Locale l = Locale.getDefault();
        propConfig.setProperty("user.language", l.getLanguage());
        propConfig.setProperty("user.country", l.getCountry());
        propConfig.setProperty("user.variant", l.getVariant());

        propConfig.setProperty("swing.defaultlaf", "com.formdev.flatlaf.FlatDarkLaf");

        propConfig.setProperty("machine.printer", "screen");
        propConfig.setProperty("machine.printer.2", "Not defined");
        propConfig.setProperty("machine.printer.3", "Not defined");
        propConfig.setProperty("machine.printer.4", "Not defined");
        propConfig.setProperty("machine.printer.5", "Not defined");
        propConfig.setProperty("machine.printer.6", "Not defined");

        propConfig.setProperty("machine.display", "screen");
        propConfig.setProperty("machine.scale", "Not defined");
        propConfig.setProperty("machine.screenmode", "fullscreen");
        propConfig.setProperty("machine.ticketsbag", "standard");
        propConfig.setProperty("machine.scanner", "Not defined");
        propConfig.setProperty("machine.iButton", "false");
        propConfig.setProperty("machine.iButtonResponse", "5");
        propConfig.setProperty("machine.uniqueinstance", "true");

        propConfig.setProperty("payment.gateway", "external");
        propConfig.setProperty("payment.magcardreader", "Not defined");
        propConfig.setProperty("payment.testmode", "true");
        propConfig.setProperty("payment.commerceid", "");
        propConfig.setProperty("payment.commercepassword", "password");

        propConfig.setProperty("machine.printername", "(Default)");
        propConfig.setProperty("screen.receipt.columns", "42");

        // Receipt printer paper set to 72mmx200mm
        propConfig.setProperty("paper.receipt.x", "10");
        propConfig.setProperty("paper.receipt.y", "10");
        propConfig.setProperty("paper.receipt.width", "190");
        propConfig.setProperty("paper.receipt.height", "546");
        propConfig.setProperty("paper.receipt.mediasizename", "A4");

        // Normal printer paper for A4
        propConfig.setProperty("paper.standard.x", "72");
        propConfig.setProperty("paper.standard.y", "72");
        propConfig.setProperty("paper.standard.width", "451");
        propConfig.setProperty("paper.standard.height", "698");
        propConfig.setProperty("paper.standard.mediasizename", "A4");

        propConfig.setProperty("tkt.header1", "KriolOS POS");
        propConfig.setProperty("tkt.header2", "Open Source Point Of Sale");
        propConfig.setProperty("tkt.header3", "Copyright (c) 2020-2023 KriolOS");
        propConfig.setProperty("tkt.header4", "Change header text in Configuration");

        propConfig.setProperty("tkt.footer1", "Change footer text in Configuration");
        propConfig.setProperty("tkt.footer2", "Thank you for your custom");
        propConfig.setProperty("tkt.footer3", "Please Call Again");

        propConfig.setProperty("table.showcustomerdetails", "true");
        propConfig.setProperty("table.customercolour", "#58B000");
        propConfig.setProperty("table.showwaiterdetails", "true");
        propConfig.setProperty("table.waitercolour", "#258FB0");
        propConfig.setProperty("table.tablecolour", "#D62E52");
        propConfig.setProperty("till.amountattop", "true");
        propConfig.setProperty("till.hideinfo", "true");

        return propConfig;

    }

    public static void applySystemProperties(AppConfig config) {
        // Set the look and feel.
        String lafClass = config.getProperty("swing.defaultlaf");
        try {
            if (lafClass != null && !lafClass.isBlank()) {
                Object laf = Class.forName(lafClass).getDeclaredConstructor().newInstance();
                if (laf instanceof LookAndFeel) {
                    UIManager.setLookAndFeel((LookAndFeel) laf);
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException | NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException e) {
            LOGGER.log(Level.WARNING, "Cannot set Look and Feel: " + lafClass, e);
        }

        //Set I18n or Language 
        String slang = config.getProperty("user.language");
        String scountry = config.getProperty("user.country");
        String svariant = config.getProperty("user.variant");
        if (slang != null && !slang.equals("") && scountry != null && svariant != null) {
            Locale.setDefault(new Locale(slang, scountry, svariant));
        }

        //Set Format/Pattern for: Number, Date, Currency 
        Formats.setIntegerPattern(config.getProperty("format.integer"));
        Formats.setDoublePattern(config.getProperty("format.double"));
        Formats.setCurrencyPattern(config.getProperty("format.currency"));
        Formats.setPercentPattern(config.getProperty("format.percent"));
        Formats.setDatePattern(config.getProperty("format.date"));
        Formats.setTimePattern(config.getProperty("format.time"));
        Formats.setDateTimePattern(config.getProperty("format.datetime"));
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
