package com.openbravo.pos.forms;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

class LocaleResources {

    private final List<ResourceBundle> m_resources = new LinkedList<>();

    private ClassLoader m_localeloader;

    public LocaleResources() {
        File fresources = new File(new File(System.getProperty("user.dir")), "locales");
        try {
            this.m_localeloader = URLClassLoader.newInstance(new URL[]{fresources
                .toURI().toURL()}, Thread.currentThread().getContextClassLoader());
        } catch (MalformedURLException e) {
            this.m_localeloader = Thread.currentThread().getContextClassLoader();
        }
    }

    private ResourceBundle getBundle(String bundlename) {
        ResourceBundle resource = null;
        try {
            resource = ResourceBundle.getBundle(bundlename, Locale.getDefault(), this.m_localeloader);
        } catch (MissingResourceException ex) {
            try {
                resource = ResourceBundle.getBundle(bundlename);
            } catch (Exception ex1) {
            }
        }
        return resource;
    }

    public void addBundleName(String bundlename) {
        ResourceBundle resource = getBundle(bundlename);
        if (resource != null) {
            this.m_resources.add(resource);
        }
    }

    public String getString(String sKey) {
        if (sKey == null) {
            return null;
        }
        for (ResourceBundle r : this.m_resources) {
            try {
                return r.getString(sKey);
            } catch (MissingResourceException missingResourceException) {
            }
        }
        return "** " + sKey + " **";
    }

    public String getString(String sKey, Object... sValues) {
        if (sKey == null) {
            return null;
        }
        for (ResourceBundle r : this.m_resources) {
            try {
                return MessageFormat.format(r.getString(sKey), sValues);
            } catch (MissingResourceException missingResourceException) {
            }
        }
        StringBuilder sreturn = new StringBuilder();
        sreturn.append("** ");
        sreturn.append(sKey);
        for (Object value : sValues) {
            sreturn.append(" < ");
            sreturn.append(value.toString());
        }
        sreturn.append("** ");
        return sreturn.toString();
    }

    public String getString2(String sKey, String defaultvalue) {
        if (sKey == null) {
            return null;
        }
        for (ResourceBundle r : this.m_resources) {
            try {
                return r.getString(sKey);
            } catch (MissingResourceException missingResourceException) {
            }
        }
        return defaultvalue;
    }
}
