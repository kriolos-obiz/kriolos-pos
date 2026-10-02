/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.openbravo.pos.forms;

import com.openbravo.pos.util.SAXParserUtils;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author poolborges
 */
public class AppUserPermissionsLoader {
    
    private static final Logger LOGGER = Logger.getLogger(AppUserPermissionsLoader.class.getName());

    private final DataLogicSystem dlSystem;

    public AppUserPermissionsLoader(DataLogicSystem dlSystem) {
        this.dlSystem = dlSystem;
    }

    public Set<String> getPermissionsForRole(String role) {

        String permisionsXML = dlSystem.findRolePermissions(role);

        Set<String> permissionsSet = new HashSet<>();
        try {

            SAXParserFactory spf = SAXParserUtils.newSecureInstance();
            SAXParser parser = spf.newSAXParser();
            parser.parse(new InputSource(new StringReader(permisionsXML)), new ConfigurationHandler(permissionsSet));

        } catch (ParserConfigurationException ex) {
            LOGGER.log(Level.WARNING, "Exception on SAXParser configuration", ex);
        } catch (SAXException | IOException ex) {
            LOGGER.log(Level.WARNING, "Exception parsing PERMISSION XML content", ex);
        }

        return permissionsSet;
    }
    
    private static class ConfigurationHandler extends DefaultHandler {
        private final Set<String> permissions;
        public ConfigurationHandler(Set<String> permissions){
            this.permissions = permissions;
        }

        @Override
        public void startDocument() throws SAXException {
            //DO NOTHING
        }

        @Override
        public void endDocument() throws SAXException {
            //DO NOTHING
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if ("class".equals(qName)) {
                permissions.add(attributes.getValue("name"));
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            //DO NOTHING
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            //DO NOTHING
        }
    }

}
