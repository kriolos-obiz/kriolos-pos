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
package com.openbravo.pos.sales;

import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.AppUser;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.DataLogicSystem;
import com.openbravo.pos.util.ThumbNailBuilder;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author JG uniCenta
 */
public class JPanelButtons extends javax.swing.JPanel {

    private static final Logger LOGGER = Logger.getLogger(JPanelButtons.class.getName());

    private Properties props;
    private Map<String, String> events;
    private DataLogicSystem dlSystem;
    private JPanelButtonListener bListener;

    public JPanelButtons(AppView app, JPanelButtonListener bListener, String sConfigRes) {
        initComponents();
        props = new Properties();
        events = new HashMap<>();
        this.bListener = bListener;
        dlSystem = app.getBean(DataLogicSystem.class);
        try {
            SAXParserFactory spf = SAXParserFactory.newInstance();
            SAXParser m_sp = spf.newSAXParser();
            m_sp.parse(new InputSource(new StringReader(sConfigRes)), new ConfigurationHandler());
        } catch (ParserConfigurationException ePC) {
            LOGGER.log(Level.WARNING, "exception.parserconfig "+sConfigRes, ePC);
        } catch (SAXException eSAX) {
            LOGGER.log(Level.WARNING, "exception.xmlfile: "+sConfigRes, eSAX);
        } catch (IOException eIO) {
            LOGGER.log(Level.WARNING, "exception.iofile: "+sConfigRes, eIO);
        }
    }

    /**
     *
     * @param user
     */
    public void setPermissions(AppUser user) {
        for (Component c : this.getComponents()) {
            String sKey = c.getName();
            if (sKey == null || sKey.equals("")) {
                c.setEnabled(true);
            } else {
                c.setEnabled(user.hasPermission(c.getName()));
            }
        }
    }

    /**
     *
     * @param key
     * @return
     */
    public String getProperty(String key) {
        return props.getProperty(key);
    }

    /**
     *
     * @param key
     * @param defaultvalue
     * @return
     */
    public String getProperty(String key, String defaultvalue) {
        return props.getProperty(key, defaultvalue);
    }

    public String getEvent(String key) {
        return events.get(key);
    }

    private class ConfigurationHandler extends DefaultHandler {

        @Override
        public void startDocument() throws SAXException {
        }

        @Override
        public void endDocument() throws SAXException {
        }

        @Override
        public void startElement(String uri, String localName, String qName,
                Attributes attributes) throws SAXException {
            switch (qName) {
                case "button":
                    // The button title text
                    String titlekey = attributes.getValue("titlekey");

                    titlekey = titlekey == null
                            ? attributes.getValue("name")
                            : null;

                    String title = titlekey == null
                            ? attributes.getValue("title")
                            : AppLocal.getIntString(titlekey);

                    // adding the button to the panel
                    JButton btn = new JButtonFunc(
                            attributes.getValue("key"),
                            attributes.getValue("image"),
                            title);

                    //Resource is: template
                    final String template = attributes.getValue("template");
                    if (template != null) {
                        btn.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent evt) {
                                bListener.print(template);
                            }
                        });
                    } else {
                        //Resource is: code 
                        final String code = attributes.getValue("code");
                        if (code != null) {
                            btn.addActionListener(new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent evt) {
                                    bListener.eval(code);
                                }
                            });
                        }
                    }
                    add(btn);
                    break;
                case "event":
                    events.put(attributes.getValue("key"), attributes.getValue("code"));
                    break;
                default:
                    String value = attributes.getValue("value");
                    if (value != null) {
                        props.setProperty(qName, attributes.getValue("value"));
                    }
                    break;
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
        }
    }

    private class JButtonFunc extends JButton {

        private static final long serialVersionUID = 1L;

        // Load categories default thumbnail
        final ThumbNailBuilder tnbmacro = new ThumbNailBuilder(42, 36, "com/openbravo/images/run_script.png");

        public JButtonFunc(String sKey, String sImage, String title) {

            setName(sKey);
            setText(title);
            setToolTipText(title);
            setIcon(new ImageIcon(tnbmacro.getThumbNail(dlSystem.getResourceAsImage(sImage))));
            setFocusPainted(false);
            setFocusable(false);
            setRequestFocusEnabled(false);
            setMargin(new Insets(8, 14, 8, 14));
            setPreferredSize(new Dimension(80, 45));
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    
    public static interface JPanelButtonListener {

        public void eval(String resource);

        public void print(String resource);
    }
}
