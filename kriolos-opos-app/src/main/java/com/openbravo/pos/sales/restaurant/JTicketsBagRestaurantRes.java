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

package com.openbravo.pos.sales.restaurant;

import java.awt.BorderLayout;
import java.awt.Component;
import java.beans.*;
import java.util.*;

import com.openbravo.beans.*;
import com.openbravo.data.gui.*;
import com.openbravo.data.user.*;

import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.AppView;
import com.openbravo.format.Formats;
import com.openbravo.basic.BasicException;
import com.openbravo.pos.customers.DataLogicCustomers;
import com.openbravo.pos.customers.JCustomerFinder;
import com.openbravo.pos.customers.CustomerInfo;

/**
 *
 * @author JG uniCenta
 */
public class JTicketsBagRestaurantRes extends javax.swing.JPanel implements EditorRecord {

    private final JTicketsBagRestaurantMap m_restaurantmap;
    
    private DataLogicCustomers dlCustomers = null;
    
    private final DirtyManager m_Dirty;
    private String m_sID;
    private CustomerInfo customer;
    private Date m_dCreated;
    private final JTimePanel m_timereservation;
    private boolean m_bReceived;
    private final BrowsableEditableData m_bd;
        
    private Date m_dcurrentday;
    
    private final JCalendarPanel m_datepanel;    
    private final JTimePanel m_timepanel;
    private boolean m_bpaintlock = false;

    
    /** Creates new form JPanelReservations
     * @param oApp
     * @param restaurantmap */
    public JTicketsBagRestaurantRes(AppView oApp, JTicketsBagRestaurantMap restaurantmap) {
        
        m_restaurantmap = restaurantmap;
        
        dlCustomers = (DataLogicCustomers) oApp.getBean("com.openbravo.pos.customers.DataLogicCustomers");

        m_dcurrentday = null;
        
        initComponents();
        jCalendar.setVisible(false);
        
        m_datepanel = new JCalendarPanel();
        jPanelDate.add(m_datepanel, BorderLayout.CENTER);
        m_datepanel.addPropertyChangeListener("Date", new DateChangeCalendarListener());
        
        m_timepanel = new JTimePanel(null, JTimePanel.BUTTONS_HOUR);
        m_timepanel.setPeriod(3600000L);
        jPanelTime.add(m_timepanel, BorderLayout.CENTER);
        m_timepanel.addPropertyChangeListener("Date", new DateChangeTimeListener());
        
        m_timereservation = new JTimePanel(null, JTimePanel.BUTTONS_MINUTE);
        m_jPanelTime.add(m_timereservation, BorderLayout.CENTER);   
            
        txtCustomer.addEditorKeys(m_jKeys);
        m_jtxtChairs.addEditorKeys(m_jKeys);
        m_jtxtDescription.addEditorKeys(m_jKeys);

        m_Dirty = new DirtyManager();
        m_timereservation.addPropertyChangeListener("Date", m_Dirty);
        txtCustomer.addPropertyChangeListener("Text", m_Dirty);
        txtCustomer.addPropertyChangeListener("Text", new PropertyChangeListener(){
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                customer = new CustomerInfo(null);
                customer.setTaxid(null);
                customer.setSearchkey(null);
                customer.setName(txtCustomer.getText());            
            }
        });
        m_jtxtChairs.addPropertyChangeListener("Text", m_Dirty);
        m_jtxtDescription.addPropertyChangeListener("Text", m_Dirty);
        
        writeValueEOF();
        
        ListProvider lpr = new ListProviderCreator(dlCustomers.getReservationsList(), new MyDateFilter());            
        DefaultSaveProvider spr = new DefaultSaveProvider(dlCustomers.getReservationsUpdate(), 
            dlCustomers.getReservationsInsert(), dlCustomers.getReservationsDelete());        
        
        m_bd = new BrowsableEditableData(lpr, spr, new CompareReservations(), this, m_Dirty);           
        
        JListNavigator nl = new JListNavigator(m_bd, true);
        nl.setCellRenderer(new JCalendarItemRenderer());  
        m_jPanelList.add(nl, BorderLayout.CENTER);
        
        m_jToolbar.add(new JLabelDirty(m_Dirty));
        m_jToolbar.add(new JCounter(m_bd));
        m_jToolbar.add(new JNavigator(m_bd));
        m_jToolbar.add(new JSaver(m_bd));       
    }
    
    private class MyDateFilter implements EditorCreator {
        @Override
        public Object createValue() throws BasicException {           
            return new Object[] {m_dcurrentday, new Date(m_dcurrentday.getTime() + 3600000L)};
        }
    }
    
    /**
     *
     */
    public void activate() {
        reload(DateUtils.getTodayHours(new Date()));
    }
    
    /**
     *
     */
    @Override
    public void refresh() {
    }

    /**
     *
     * @return
     */
    public boolean deactivate() {
        try {
            return m_bd.actionClosingForm(this);
        } catch (BasicException eD) {
            MessageInf msg = new MessageInf(MessageInf.SGN_NOTICE, 
                AppLocal.getIntString("message.CannotMove"), eD);
            msg.show(this);
            return false;
        }
    }
    
    /**
     *
     */
    @Override
    public void writeValueEOF() {
        m_sID = null;
        m_dCreated = null;
        m_timereservation.setDate(null);
        assignCustomer(new CustomerInfo(null));
        m_jtxtChairs.reset();
        m_bReceived = false;
        m_jtxtDescription.reset();
        m_timereservation.setEnabled(false);
        txtCustomer.setEnabled(false);
        m_jtxtChairs.setEnabled(false);
        m_jtxtDescription.setEnabled(false);
        m_jKeys.setEnabled(false);
        
        m_jbtnReceive.setEnabled(false);
    }    

    /**
     *
     */
    @Override
    public void writeValueInsert() {
        m_sID = null;
        m_dCreated = null;
        m_timereservation.setCheckDates(m_dcurrentday, new Date(m_dcurrentday.getTime() + 3600000L));
        m_timereservation.setDate(m_dcurrentday);
        assignCustomer(new CustomerInfo(null));
        m_jtxtChairs.setValueInteger(2);
        m_bReceived = false;
        m_jtxtDescription.reset();
        m_timereservation.setEnabled(true);
        txtCustomer.setEnabled(true);
        m_jtxtChairs.setEnabled(true);
        m_jtxtDescription.setEnabled(true);
        m_jKeys.setEnabled(true);
        
        m_jbtnReceive.setEnabled(true);
        
        txtCustomer.activate();
    }

    /**
     *
     * @param value
     */
    @Override
    public void writeValueDelete(Object value) {
        Object[] res = (Object[]) value;
        m_sID = (String)res[0];
        m_dCreated = (Date) res[1];
        m_timereservation.setCheckDates(m_dcurrentday, new Date(m_dcurrentday.getTime() + 3600000L));
        m_timereservation.setDate((Date) res[2]);       
        CustomerInfo c = new CustomerInfo((String) res[3]);
        c.setTaxid((String) res[4]);
        c.setSearchkey((String) res[5]);
        c.setName((String) res[6]);
        assignCustomer(c);        
        m_jtxtChairs.setValueInteger(((Integer)res[7]).intValue());
        m_bReceived = ((Boolean)res[8]).booleanValue();
        m_jtxtDescription.setText(Formats.STRING.formatValue((String)res[9]));
        m_timereservation.setEnabled(false);
        txtCustomer.setEnabled(false);
        m_jtxtChairs.setEnabled(false);
        m_jtxtDescription.setEnabled(false);
        m_jKeys.setEnabled(false);
        
        m_jbtnReceive.setEnabled(false); 
    }  

    /**
     *
     * @param value
     */
    @Override
    public void writeValueEdit(Object value) {
        Object[] res = (Object[]) value;
        m_sID = (String)res[0];
        m_dCreated = (Date) res[1];
        m_timereservation.setCheckDates(m_dcurrentday, new Date(m_dcurrentday.getTime() + 3600000L));
        m_timereservation.setDate((Date) res[2]);
        CustomerInfo c = new CustomerInfo((String) res[3]);
        c.setTaxid((String) res[4]);
        c.setSearchkey((String) res[5]);
        c.setName((String) res[6]);
        assignCustomer(c);  
        m_jtxtChairs.setValueInteger(((Integer)res[7]).intValue());
        m_bReceived = ((Boolean)res[8]).booleanValue();
        m_jtxtDescription.setText(Formats.STRING.formatValue((String)res[9]));
        m_timereservation.setEnabled(true);
        txtCustomer.setEnabled(true);
        m_jtxtChairs.setEnabled(true);
        m_jtxtDescription.setEnabled(true);
        m_jKeys.setEnabled(true);

        m_jbtnReceive.setEnabled(!m_bReceived); // se habilita si no se ha recibido al cliente

        txtCustomer.activate();
    }    

    /**
     *
     * @return
     * @throws BasicException
     */
    @Override
    public Object createValue() throws BasicException {
        
        Object[] res = new Object[10];
        
        res[0] = m_sID == null ? UUID.randomUUID().toString() : m_sID; 
        res[1] = m_dCreated == null ? new Date() : m_dCreated; 
        res[2] = m_timereservation.getDate();
        res[3] = customer.getId();
        res[4] = customer.getTaxid();
        res[5] = customer.getSearchkey();
        res[6] = customer.getName();
        res[7] = m_jtxtChairs.getValue();
        res[8] = m_bReceived;
        res[9] = m_jtxtDescription.getText();

        return res;
    }

    /**
     *
     * @return
     */
    @Override
    public Component getComponent() {
        return this;
    }  
    
    private static class CompareReservations implements Comparator {
        @Override
        public int compare(Object o1, Object o2) {
            Object[] a1 = (Object[]) o1;
            Object[] a2 = (Object[]) o2;
            Date d1 = (Date) a1[2];
            Date d2 = (Date) a2[2];
            int c = d1.compareTo(d2);
            if (c == 0) {
                d1 = (Date) a1[1];
                d2 = (Date) a2[1];
                return d1.compareTo(d2);
            } else {
                return c;
            }
        }
    }
    
    private void reload(Date dDate) {
        
        if (!dDate.equals(m_dcurrentday)) {
   
            Date doldcurrentday = m_dcurrentday;
            m_dcurrentday = dDate;
            try {
                m_bd.actionLoad();
            } catch (BasicException eD) {
                MessageInf msg = new MessageInf(MessageInf.SGN_NOTICE, 
                    AppLocal.getIntString("message.noreload"), eD);
                msg.show(this);
                m_dcurrentday = doldcurrentday; // nos retractamos...
            }
        }    

        paintDate();
    }
    
    private void paintDate() {
        
        m_bpaintlock = true;
        m_datepanel.setDate(m_dcurrentday);
        m_timepanel.setDate(m_dcurrentday);
        m_bpaintlock = false;
    }
       
    private void assignCustomer(CustomerInfo c) {
        
        txtCustomer.setText(c.getName());
        customer = c;
    }
    
    private class DateChangeCalendarListener implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (!m_bpaintlock) {
                reload(DateUtils.getTodayHours(DateUtils.getDate(m_datepanel.getDate(), 
                    m_timepanel.getDate())));
            }
        }        
    }
        
    private class DateChangeTimeListener implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (!m_bpaintlock) {
                reload(DateUtils.getTodayHours(DateUtils.getDate(m_datepanel.getDate(), 
                    m_timepanel.getDate())));
            }
        }        
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanNorth = new javax.swing.JPanel();
        m_jToolbar = new javax.swing.JPanel();
        m_jbtnReceive = new javax.swing.JButton();
        m_jbtnTables = new javax.swing.JButton();
        m_jPanelList = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        txtCustomer = new com.openbravo.editor.JEditorString();
        jLabel3 = new javax.swing.JLabel();
        m_jtxtChairs = new com.openbravo.editor.JEditorIntegerPositive();
        jLabel4 = new javax.swing.JLabel();
        m_jtxtDescription = new com.openbravo.editor.JEditorString();
        jLabel1 = new javax.swing.JLabel();
        m_jPanelTime = new javax.swing.JPanel();
        jbtnShowCalendar = new javax.swing.JButton();
        m_jKeys = new com.openbravo.editor.JEditorKeys();
        jPanSouth = new javax.swing.JPanel();
        jCalendar = new javax.swing.JPanel();
        jPanelTime = new javax.swing.JPanel();
        jPanelDate = new javax.swing.JPanel();

        setPreferredSize(new java.awt.Dimension(1000, 750));
        setLayout(new java.awt.BorderLayout());

        jPanNorth.setPreferredSize(new java.awt.Dimension(1000, 350));

        m_jToolbar.setPreferredSize(new java.awt.Dimension(500, 55));
        m_jToolbar.setLayout(new java.awt.BorderLayout());

        m_jbtnReceive.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        m_jbtnReceive.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/receive.png"))); // NOI18N
        m_jbtnReceive.setText(AppLocal.getIntString("button.receive")); // NOI18N
        m_jbtnReceive.setToolTipText("Receive pre-Booked Customer");
        m_jbtnReceive.setFocusPainted(false);
        m_jbtnReceive.setFocusable(false);
        m_jbtnReceive.setPreferredSize(new java.awt.Dimension(130, 45));
        m_jbtnReceive.setRequestFocusEnabled(false);
        m_jbtnReceive.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jbtnReceiveActionPerformed(evt);
            }
        });

        m_jbtnTables.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        m_jbtnTables.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/tables.png"))); // NOI18N
        m_jbtnTables.setText(AppLocal.getIntString("button.tables")); // NOI18N
        m_jbtnTables.setToolTipText("Go to Table Plan");
        m_jbtnTables.setFocusPainted(false);
        m_jbtnTables.setFocusable(false);
        m_jbtnTables.setPreferredSize(new java.awt.Dimension(130, 45));
        m_jbtnTables.setRequestFocusEnabled(false);
        m_jbtnTables.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jbtnTablesActionPerformed(evt);
            }
        });

        m_jPanelList.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        m_jPanelList.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jPanelList.setPreferredSize(new java.awt.Dimension(300, 200));
        m_jPanelList.setLayout(new java.awt.BorderLayout());

        jLabel5.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel5.setText(AppLocal.getIntString("rest.label.selectcustomer")); // NOI18N
        jLabel5.setPreferredSize(new java.awt.Dimension(100, 45));

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/customer_add_sml.png"))); // NOI18N
        jButton1.setToolTipText("Show Customers");
        jButton1.setFocusPainted(false);
        jButton1.setFocusable(false);
        jButton1.setMaximumSize(new java.awt.Dimension(40, 33));
        jButton1.setMinimumSize(new java.awt.Dimension(40, 33));
        jButton1.setPreferredSize(new java.awt.Dimension(80, 45));
        jButton1.setRequestFocusEnabled(false);
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        txtCustomer.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        txtCustomer.setMaximumSize(new java.awt.Dimension(250, 30));
        txtCustomer.setMinimumSize(new java.awt.Dimension(200, 25));
        txtCustomer.setPreferredSize(new java.awt.Dimension(250, 30));

        jLabel3.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel3.setText(AppLocal.getIntString("rest.label.chairs")); // NOI18N
        jLabel3.setPreferredSize(new java.awt.Dimension(100, 30));

        m_jtxtChairs.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jtxtChairs.setMaximumSize(new java.awt.Dimension(50, 25));
        m_jtxtChairs.setMinimumSize(new java.awt.Dimension(50, 25));
        m_jtxtChairs.setPreferredSize(new java.awt.Dimension(100, 30));

        jLabel4.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel4.setText(AppLocal.getIntString("rest.label.notes")); // NOI18N
        jLabel4.setPreferredSize(new java.awt.Dimension(100, 30));

        m_jtxtDescription.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jtxtDescription.setMaximumSize(new java.awt.Dimension(180, 25));
        m_jtxtDescription.setPreferredSize(new java.awt.Dimension(250, 30));

        jLabel1.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel1.setText(AppLocal.getIntString("rest.label.date")); // NOI18N
        jLabel1.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        jLabel1.setPreferredSize(new java.awt.Dimension(100, 30));

        m_jPanelTime.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jPanelTime.setPreferredSize(new java.awt.Dimension(200, 200));
        m_jPanelTime.setLayout(new java.awt.BorderLayout());

        jbtnShowCalendar.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jbtnShowCalendar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/date.png"))); // NOI18N
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("pos_messages"); // NOI18N
        jbtnShowCalendar.setText(bundle.getString("rest.label.showcalendar")); // NOI18N
        jbtnShowCalendar.setToolTipText(bundle.getString("rest.label.showcalendar")); // NOI18N
        jbtnShowCalendar.setPreferredSize(new java.awt.Dimension(129, 45));
        jbtnShowCalendar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbtnShowCalendarActionPerformed(evt);
            }
        });

        m_jKeys.setPreferredSize(new java.awt.Dimension(5, 5));

        javax.swing.GroupLayout jPanNorthLayout = new javax.swing.GroupLayout(jPanNorth);
        jPanNorth.setLayout(jPanNorthLayout);
        jPanNorthLayout.setHorizontalGroup(
            jPanNorthLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanNorthLayout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(jPanNorthLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanNorthLayout.createSequentialGroup()
                        .addGroup(jPanNorthLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanNorthLayout.createSequentialGroup()
                                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(10, 10, 10)
                                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(6, 6, 6)
                                .addComponent(txtCustomer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(4, 4, 4)
                                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(4, 4, 4)
                                .addComponent(m_jtxtChairs, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanNorthLayout.createSequentialGroup()
                                .addComponent(m_jPanelTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanNorthLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jbtnShowCalendar, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanNorthLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanNorthLayout.createSequentialGroup()
                                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(4, 4, 4)
                                .addComponent(m_jtxtDescription, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanNorthLayout.createSequentialGroup()
                                .addGap(0, 0, 0)
                                .addComponent(m_jPanelList, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap())
                    .addGroup(jPanNorthLayout.createSequentialGroup()
                        .addComponent(m_jToolbar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(m_jKeys, javax.swing.GroupLayout.PREFERRED_SIZE, 1, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(m_jbtnReceive, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(m_jbtnTables, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(42, 42, 42))))
        );
        jPanNorthLayout.setVerticalGroup(
            jPanNorthLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanNorthLayout.createSequentialGroup()
                .addGroup(jPanNorthLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(m_jToolbar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanNorthLayout.createSequentialGroup()
                        .addGap(24, 24, 24)
                        .addComponent(m_jKeys, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanNorthLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(m_jbtnTables, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(m_jbtnReceive, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanNorthLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanNorthLayout.createSequentialGroup()
                        .addGap(5, 5, 5)
                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanNorthLayout.createSequentialGroup()
                        .addGap(7, 7, 7)
                        .addComponent(txtCustomer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanNorthLayout.createSequentialGroup()
                        .addGap(7, 7, 7)
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanNorthLayout.createSequentialGroup()
                        .addGap(7, 7, 7)
                        .addComponent(m_jtxtChairs, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanNorthLayout.createSequentialGroup()
                        .addGap(7, 7, 7)
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanNorthLayout.createSequentialGroup()
                        .addGap(7, 7, 7)
                        .addComponent(m_jtxtDescription, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanNorthLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanNorthLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.CENTER, jPanNorthLayout.createSequentialGroup()
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jbtnShowCalendar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(m_jPanelTime, javax.swing.GroupLayout.Alignment.CENTER, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(m_jPanelList, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        add(jPanNorth, java.awt.BorderLayout.CENTER);

        jPanSouth.setPreferredSize(new java.awt.Dimension(1000, 350));

        jCalendar.setPreferredSize(new java.awt.Dimension(1000, 350));

        jPanelTime.setPreferredSize(new java.awt.Dimension(250, 250));
        jPanelTime.setLayout(new java.awt.BorderLayout());
        jCalendar.add(jPanelTime);

        jPanelDate.setPreferredSize(new java.awt.Dimension(700, 345));
        jPanelDate.setLayout(new java.awt.BorderLayout());
        jCalendar.add(jPanelDate);

        javax.swing.GroupLayout jPanSouthLayout = new javax.swing.GroupLayout(jPanSouth);
        jPanSouth.setLayout(jPanSouthLayout);
        jPanSouthLayout.setHorizontalGroup(
            jPanSouthLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1000, Short.MAX_VALUE)
            .addGroup(jPanSouthLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanSouthLayout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(jCalendar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );
        jPanSouthLayout.setVerticalGroup(
            jPanSouthLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 350, Short.MAX_VALUE)
            .addGroup(jPanSouthLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanSouthLayout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(jCalendar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );

        add(jPanSouth, java.awt.BorderLayout.SOUTH);
    }// </editor-fold>//GEN-END:initComponents

    private void m_jbtnReceiveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jbtnReceiveActionPerformed
             
        m_bReceived = true;
        m_Dirty.setDirty(true);
        
        try {
            m_bd.saveData();
            m_restaurantmap.viewTables(customer);      
        } catch (BasicException eD) {
            MessageInf msg = new MessageInf(MessageInf.SGN_NOTICE, 
                AppLocal.getIntString("message.nosaveticket"), eD);
            msg.show(this);
        }       
        
    }//GEN-LAST:event_m_jbtnReceiveActionPerformed

    private void m_jbtnTablesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jbtnTablesActionPerformed

        m_restaurantmap.viewTables();
        
    }//GEN-LAST:event_m_jbtnTablesActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed

        JCustomerFinder finder = JCustomerFinder.getCustomerFinder(this, dlCustomers);
        finder.search(customer);
        finder.setVisible(true);
        
        CustomerInfo c = finder.getSelectedCustomer(); 
        
        if (c == null) {       
            assignCustomer(new CustomerInfo(null));
        } else {
            assignCustomer(c);
        }
        
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jbtnShowCalendarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbtnShowCalendarActionPerformed
        if (jCalendar.isShowing()) {
            jCalendar.setVisible(false);
            jbtnShowCalendar.setText("Show Calendar");
        }else{
            jbtnShowCalendar.setText("Hide Calendar");            
            jCalendar.setVisible(true);
        }
    }//GEN-LAST:event_jbtnShowCalendarActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JPanel jCalendar;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanNorth;
    private javax.swing.JPanel jPanSouth;
    private javax.swing.JPanel jPanelDate;
    private javax.swing.JPanel jPanelTime;
    private javax.swing.JButton jbtnShowCalendar;
    private com.openbravo.editor.JEditorKeys m_jKeys;
    private javax.swing.JPanel m_jPanelList;
    private javax.swing.JPanel m_jPanelTime;
    private javax.swing.JPanel m_jToolbar;
    private javax.swing.JButton m_jbtnReceive;
    private javax.swing.JButton m_jbtnTables;
    private com.openbravo.editor.JEditorIntegerPositive m_jtxtChairs;
    private com.openbravo.editor.JEditorString m_jtxtDescription;
    private com.openbravo.editor.JEditorString txtCustomer;
    // End of variables declaration//GEN-END:variables
    
}
