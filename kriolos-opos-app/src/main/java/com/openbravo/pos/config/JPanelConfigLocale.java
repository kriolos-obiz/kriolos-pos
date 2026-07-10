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
package com.openbravo.pos.config;

import com.openbravo.data.user.DirtyManager;
import com.openbravo.format.Formats;
import java.awt.Component;
import java.util.Locale;
import com.openbravo.pos.forms.AppConfig;
import com.openbravo.pos.forms.AppLocal;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 *
 * @author adrianromero
 */
public class JPanelConfigLocale extends javax.swing.JPanel implements PanelConfig, ActionListener {

    private final DirtyManager dirty = new DirtyManager();

    private final static String DEFAULT_VALUE = "(Default)";

    /**
     * Creates new form JPanelConfigLocale
     */
    public JPanelConfigLocale() {

        initComponents();

        jcboLocale.addActionListener(dirty);
        jcboInteger.addActionListener(dirty);
        jcboDouble.addActionListener(dirty);
        jcboCurrency.addActionListener(dirty);
        jcboPercent.addActionListener(dirty);
        jcboDate.addActionListener(dirty);
        jcboTime.addActionListener(dirty);
        jcboDatetime.addActionListener(dirty);

        List<Locale> availableLocales = new ArrayList<>();
        //TODO To be implemented with provider pattern
        //availableLocales.addAll(Arrays.asList(Locale.getAvailableLocales())); // Available java locales
        addLocale(availableLocales, new Locale("en", "GB", "")); // English GB
        addLocale(availableLocales, new Locale("pt", "", "")); // Portuguese Portugal

        Collections.sort(availableLocales, new LocaleComparator());

        jcboLocale.addItem(new LocaleInfo(null));
        for (Locale l : availableLocales) {
            jcboLocale.addItem(new LocaleInfo(l));
        }

        jcboInteger.addItem(DEFAULT_VALUE);
        jcboInteger.addItem("#0");
        jcboInteger.addItem("#,##0");

        jcboDouble.addItem(DEFAULT_VALUE);
        jcboDouble.addItem("#0.0");
        jcboDouble.addItem("#,##0.#");

        jcboCurrency.addItem(DEFAULT_VALUE);
        jcboCurrency.addItem("\u00A4 #0.00");
        jcboCurrency.addItem("'$' #,##0.00");
        jcboCurrency.addItem("#00 '$'");
        jcboCurrency.addItem("#,##0'$'");

        jcboPercent.addItem(DEFAULT_VALUE);
        jcboPercent.addItem("#,##0.##%");

        jcboDate.addItem(DEFAULT_VALUE);

        jcboTime.addItem(DEFAULT_VALUE);

        jcboDatetime.addItem(DEFAULT_VALUE);

        setupListerner();
        
        System.out.println("Verificar (120 CVE) pt-CV Currency: 120.30 "
                + NumberFormat.getCurrencyInstance(new Locale("pt", "CV")).format(120.30)
                + "\n\rVerificar (120 Kuanza) pt-AO Currency: 120.30 "
                + NumberFormat.getCurrencyInstance(new Locale("pt", "AO")).format(120.30));

    }

    private void setupListerner() {
        //THIS ALLOW TO UPDATE TOOLTIP
        jcboLocale.addActionListener(this);
        jcboInteger.addActionListener(this);
        jcboDouble.addActionListener(this);
        jcboCurrency.addActionListener(this);
        jcboPercent.addActionListener(this);
        jcboDate.addActionListener(this);
        jcboTime.addActionListener(this);
        jcboDatetime.addActionListener(this);
    }

    private void addLocale(List<Locale> ll, Locale l) {
        if (!ll.contains(l)) {
            ll.add(l);
        }
    }

    /**
     *
     * @return
     */
    @Override
    public boolean hasChanged() {
        return dirty.isDirty();
    }

    /**
     *
     * @return
     */
    @Override
    public Component getConfigComponent() {
        return this;
    }

    /**
     *
     * @param config
     */
    @Override
    public void loadProperties(AppConfig config) {

        String slang = config.getProperty("user.language");
        String scountry = config.getProperty("user.country");
        String svariant = config.getProperty("user.variant");

        if (slang != null && !slang.equals("") && scountry != null && svariant != null) {
            Locale currentlocale = new Locale(slang, scountry, svariant);
            for (int i = 0; i < jcboLocale.getItemCount(); i++) {
                LocaleInfo l = (LocaleInfo) jcboLocale.getItemAt(i);
                if (currentlocale.equals(l.getLocale())) {
                    jcboLocale.setSelectedIndex(i);
                    break;
                }
            }
        } else {
            jcboLocale.setSelectedIndex(0);
        }

        jcboInteger.setSelectedItem(writeWithDefault(config.getProperty("format.integer")));
        jcboDouble.setSelectedItem(writeWithDefault(config.getProperty("format.double")));
        jcboCurrency.setSelectedItem(writeWithDefault(config.getProperty("format.currency")));
        jcboPercent.setSelectedItem(writeWithDefault(config.getProperty("format.percent")));
        jcboDate.setSelectedItem(writeWithDefault(config.getProperty("format.date")));
        jcboTime.setSelectedItem(writeWithDefault(config.getProperty("format.time")));
        jcboDatetime.setSelectedItem(writeWithDefault(config.getProperty("format.datetime")));

        dirty.setDirty(false);
    }

    private void showHelp() {

        //Set Current Locale to What is selected;
        Locale.setDefault(((LocaleInfo) jcboLocale.getSelectedItem()).getLocale());

        //Set Format/Pattern for: Number, Date, Currency 
        Formats.setIntegerPattern(readWithDefault(jcboInteger.getSelectedItem()));
        Formats.setDoublePattern(readWithDefault(jcboDouble.getSelectedItem()));
        Formats.setCurrencyPattern(readWithDefault(jcboCurrency.getSelectedItem()));
        Formats.setPercentPattern(readWithDefault(jcboPercent.getSelectedItem()));
        Formats.setDatePattern(readWithDefault(jcboDate.getSelectedItem()));
        Formats.setTimePattern(readWithDefault(jcboTime.getSelectedItem()));
        Formats.setDateTimePattern(readWithDefault(jcboDatetime.getSelectedItem()));

        jcboLocale.setToolTipText("<html>IETF BCP 47 Tag: " + Locale.getDefault().toLanguageTag());
        jcboInteger.setToolTipText("<html>123 formated: " + Formats.INT.formatValue(123));
        jcboDouble.setToolTipText("<html>123.45 formated: " + Formats.DOUBLE.formatValue(123.45));
        jcboCurrency.setToolTipText("<html>123.45 formated: " + Formats.CURRENCY.formatValue(123.45));
        jcboPercent.setToolTipText("<html>0.23 formated: " + Formats.PERCENT.formatValue(0.23));
        jcboDate.setToolTipText("<html>Date formated: " + Formats.DATE.formatValue(new Date()));
        jcboTime.setToolTipText("<html>Time formated: " + Formats.TIME.formatValue(new Date()));
        jcboDatetime.setToolTipText("<html>DateTime formated: " + Formats.TIMESTAMP.formatValue(new Date()));

        

    }

    /**
     *
     * @param config
     */
    @Override
    public void saveProperties(AppConfig config) {

        Locale l = ((LocaleInfo) jcboLocale.getSelectedItem()).getLocale();
        if (l == null) {
            config.setProperty("user.language", "");
            config.setProperty("user.country", "");
            config.setProperty("user.variant", "");
        } else {
            config.setProperty("user.language", l.getLanguage());
            config.setProperty("user.country", l.getCountry());
            config.setProperty("user.variant", l.getVariant());
        }

        config.setProperty("format.integer", readWithDefault(jcboInteger.getSelectedItem()));
        config.setProperty("format.double", readWithDefault(jcboDouble.getSelectedItem()));
        config.setProperty("format.currency", readWithDefault(jcboCurrency.getSelectedItem()));
        config.setProperty("format.percent", readWithDefault(jcboPercent.getSelectedItem()));
        config.setProperty("format.date", readWithDefault(jcboDate.getSelectedItem()));
        config.setProperty("format.time", readWithDefault(jcboTime.getSelectedItem()));
        config.setProperty("format.datetime", readWithDefault(jcboDatetime.getSelectedItem()));

        dirty.setDirty(false);
    }

    private String readWithDefault(Object value) {
        if (DEFAULT_VALUE.equals(value)) {
            return "";
        } else {
            return value.toString();
        }
    }

    private Object writeWithDefault(String value) {
        if (value == null || value.equals("") || value.equals(DEFAULT_VALUE)) {
            return DEFAULT_VALUE;
        } else {
            return value;
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        showHelp();
    }

    private static class LocaleInfo {

        private final Locale locale;

        public LocaleInfo(Locale locale) {
            this.locale = locale;
        }

        public Locale getLocale() {
            return (locale == null) ? Locale.ROOT : locale;
        }

        @Override
        public String toString() {
            return (locale == null || locale == Locale.ROOT)
                    ? "(System default)"
                    : locale.getDisplayName();
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        lblLocale = new javax.swing.JLabel();
        jcboLocale = new javax.swing.JComboBox();
        lblInteger = new javax.swing.JLabel();
        jcboInteger = new javax.swing.JComboBox();
        lblDouble = new javax.swing.JLabel();
        jcboDouble = new javax.swing.JComboBox();
        lblCurrency = new javax.swing.JLabel();
        jcboCurrency = new javax.swing.JComboBox();
        lblPercent = new javax.swing.JLabel();
        jcboPercent = new javax.swing.JComboBox();
        lblDate = new javax.swing.JLabel();
        jcboDate = new javax.swing.JComboBox();
        lblTime = new javax.swing.JLabel();
        jcboTime = new javax.swing.JComboBox();
        lblDateTime = new javax.swing.JLabel();
        jcboDatetime = new javax.swing.JComboBox();

        setMinimumSize(new java.awt.Dimension(0, 0));
        setOpaque(false);
        setPreferredSize(new java.awt.Dimension(650, 450));

        jPanel1.setMaximumSize(new java.awt.Dimension(600, 400));
        jPanel1.setOpaque(false);
        jPanel1.setPreferredSize(new java.awt.Dimension(400, 300));

        lblLocale.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        lblLocale.setText(AppLocal.getIntString("label.locale")); // NOI18N
        lblLocale.setMaximumSize(new java.awt.Dimension(150, 30));
        lblLocale.setMinimumSize(new java.awt.Dimension(150, 30));
        lblLocale.setPreferredSize(new java.awt.Dimension(150, 30));

        jcboLocale.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jcboLocale.setMaximumSize(new java.awt.Dimension(200, 30));
        jcboLocale.setMinimumSize(new java.awt.Dimension(200, 30));
        jcboLocale.setPreferredSize(new java.awt.Dimension(200, 30));

        lblInteger.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        lblInteger.setText(AppLocal.getIntString("label.integer")); // NOI18N
        lblInteger.setMaximumSize(new java.awt.Dimension(150, 30));
        lblInteger.setMinimumSize(new java.awt.Dimension(150, 30));
        lblInteger.setPreferredSize(new java.awt.Dimension(150, 30));

        jcboInteger.setEditable(true);
        jcboInteger.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jcboInteger.setMaximumSize(new java.awt.Dimension(200, 30));
        jcboInteger.setMinimumSize(new java.awt.Dimension(200, 30));
        jcboInteger.setPreferredSize(new java.awt.Dimension(200, 30));

        lblDouble.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        lblDouble.setText(AppLocal.getIntString("label.double")); // NOI18N
        lblDouble.setMaximumSize(new java.awt.Dimension(150, 30));
        lblDouble.setMinimumSize(new java.awt.Dimension(150, 30));
        lblDouble.setPreferredSize(new java.awt.Dimension(150, 30));

        jcboDouble.setEditable(true);
        jcboDouble.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jcboDouble.setMaximumSize(new java.awt.Dimension(200, 30));
        jcboDouble.setMinimumSize(new java.awt.Dimension(200, 30));
        jcboDouble.setPreferredSize(new java.awt.Dimension(200, 30));

        lblCurrency.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        lblCurrency.setText(AppLocal.getIntString("label.currency")); // NOI18N
        lblCurrency.setMaximumSize(new java.awt.Dimension(150, 30));
        lblCurrency.setMinimumSize(new java.awt.Dimension(150, 30));
        lblCurrency.setPreferredSize(new java.awt.Dimension(150, 30));

        jcboCurrency.setEditable(true);
        jcboCurrency.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jcboCurrency.setMaximumSize(new java.awt.Dimension(200, 30));
        jcboCurrency.setMinimumSize(new java.awt.Dimension(200, 30));
        jcboCurrency.setPreferredSize(new java.awt.Dimension(200, 30));

        lblPercent.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        lblPercent.setText(AppLocal.getIntString("label.percent")); // NOI18N
        lblPercent.setMaximumSize(new java.awt.Dimension(150, 30));
        lblPercent.setMinimumSize(new java.awt.Dimension(150, 30));
        lblPercent.setPreferredSize(new java.awt.Dimension(150, 30));

        jcboPercent.setEditable(true);
        jcboPercent.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jcboPercent.setMaximumSize(new java.awt.Dimension(200, 30));
        jcboPercent.setMinimumSize(new java.awt.Dimension(200, 30));
        jcboPercent.setPreferredSize(new java.awt.Dimension(200, 30));

        lblDate.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        lblDate.setText(AppLocal.getIntString("label.date")); // NOI18N
        lblDate.setMaximumSize(new java.awt.Dimension(150, 30));
        lblDate.setMinimumSize(new java.awt.Dimension(150, 30));
        lblDate.setPreferredSize(new java.awt.Dimension(150, 30));

        jcboDate.setEditable(true);
        jcboDate.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jcboDate.setMaximumSize(new java.awt.Dimension(200, 30));
        jcboDate.setMinimumSize(new java.awt.Dimension(200, 30));
        jcboDate.setPreferredSize(new java.awt.Dimension(200, 30));

        lblTime.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        lblTime.setText(AppLocal.getIntString("label.time")); // NOI18N
        lblTime.setMaximumSize(new java.awt.Dimension(150, 30));
        lblTime.setMinimumSize(new java.awt.Dimension(150, 30));
        lblTime.setPreferredSize(new java.awt.Dimension(150, 30));

        jcboTime.setEditable(true);
        jcboTime.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jcboTime.setMaximumSize(new java.awt.Dimension(200, 30));
        jcboTime.setMinimumSize(new java.awt.Dimension(200, 30));
        jcboTime.setPreferredSize(new java.awt.Dimension(200, 30));

        lblDateTime.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        lblDateTime.setText(AppLocal.getIntString("label.datetime")); // NOI18N
        lblDateTime.setMaximumSize(new java.awt.Dimension(150, 30));
        lblDateTime.setMinimumSize(new java.awt.Dimension(150, 30));
        lblDateTime.setPreferredSize(new java.awt.Dimension(150, 30));

        jcboDatetime.setEditable(true);
        jcboDatetime.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jcboDatetime.setMaximumSize(new java.awt.Dimension(200, 30));
        jcboDatetime.setMinimumSize(new java.awt.Dimension(200, 30));
        jcboDatetime.setPreferredSize(new java.awt.Dimension(200, 30));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(lblDateTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jcboDatetime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(lblTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jcboTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(lblDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jcboDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(lblPercent, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jcboPercent, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(lblCurrency, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jcboCurrency, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(lblDouble, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jcboDouble, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(lblInteger, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jcboInteger, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(lblLocale, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jcboLocale, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jcboLocale, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblLocale, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblInteger, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcboInteger, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblDouble, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcboDouble, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblCurrency, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcboCurrency, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblPercent, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcboPercent, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcboDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcboTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblDateTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcboDatetime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(2, 2, 2)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 442, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private javax.swing.JComboBox jcboCurrency;
    private javax.swing.JComboBox jcboDate;
    private javax.swing.JComboBox jcboDatetime;
    private javax.swing.JComboBox jcboDouble;
    private javax.swing.JComboBox jcboInteger;
    private javax.swing.JComboBox jcboLocale;
    private javax.swing.JComboBox jcboPercent;
    private javax.swing.JComboBox jcboTime;
    private javax.swing.JLabel lblCurrency;
    private javax.swing.JLabel lblDate;
    private javax.swing.JLabel lblDateTime;
    private javax.swing.JLabel lblDouble;
    private javax.swing.JLabel lblInteger;
    private javax.swing.JLabel lblLocale;
    private javax.swing.JLabel lblPercent;
    private javax.swing.JLabel lblTime;
    // End of variables declaration//GEN-END:variables

}
