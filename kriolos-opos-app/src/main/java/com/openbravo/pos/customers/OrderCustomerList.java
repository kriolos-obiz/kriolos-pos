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
package com.openbravo.pos.customers;

import com.openbravo.pos.businesspartner.TicketSelector;
import com.openbravo.basic.BasicException;
import com.openbravo.data.gui.MessageInf;
import com.openbravo.pos.catalog.JCatalogTab;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.sales.DataLogicReceipts;
import com.openbravo.pos.sales.SharedTicketInfo;
import com.openbravo.pos.sales.TicketsEditor;
import com.openbravo.pos.ticket.TicketInfo;
import com.openbravo.pos.util.ThumbNailBuilder;
import java.awt.Component;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.EventListenerList;

/**
 *
 * @author JG uniCenta - outline/prep for eCommerce connector
 */
public class OrderCustomerList extends JPanel implements TicketSelector {

    protected static final Logger LOGGER = Logger.getLogger("com.openbravo.pos.customers.OrderCustomerList");
    private static final long serialVersionUID = 1L;

    protected AppView application;
    private String currentTicket;
    protected TicketsEditor panelticket;
    protected EventListenerList listeners = new EventListenerList();
    private final DataLogicCustomers dataLogicCustomers;
    private final DataLogicReceipts dataLogicReceipts;
    private final ThumbNailBuilder thumbNailBuilderWithDefault;

    public OrderCustomerList(DataLogicCustomers dlCustomers, AppView app, TicketsEditor panelticket) {
        this.application = app;
        this.panelticket = panelticket;
        this.dataLogicCustomers = dlCustomers;
        this.dataLogicReceipts = application.getBean(DataLogicReceipts.class);
        this.thumbNailBuilderWithDefault = new ThumbNailBuilder(90, 98, "/com/openbravo/images/no_image.png");
        initComponents();
    }

    @Override
    public Component getComponent() {
        return this;
    }

    public void reloadCustomers() throws BasicException {
        loadPartners();
    }

    @Override
    public void loadPartners() throws BasicException {

        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                final JCatalogTab flowTab = new JCatalogTab();

                jPanelCustomers.removeAll();
                jPanelCustomers.add(flowTab);

                List<CustomerInfoExt> customers = null;
                List<SharedTicketInfo> ticketList = null;

                long currentTime = System.currentTimeMillis();
                try {
                    LOGGER.log(Level.INFO, "Time of getCustomersWithOutImage {0}", (System.currentTimeMillis() - currentTime));
                    currentTime = System.currentTimeMillis();

                    ticketList = dataLogicReceipts.getSharedTicketList();
                    LOGGER.log(Level.INFO, "Time of getSharedTicketList {0}", (System.currentTimeMillis() - currentTime));
                    currentTime = System.currentTimeMillis();

                }
                catch (BasicException ex) {
                    LOGGER.log(Level.SEVERE, null, ex);
                }
                HashMap<SharedTicketInfo, CustomerInfoExt> orderMap  = 
                        mapSharedTicket(ticketList, customers);

                //
                // sort
                CustomerComparator bvc = new CustomerComparator(orderMap);
                TreeMap<SharedTicketInfo, CustomerInfoExt> sortedMap = new TreeMap<>(bvc);
                sortedMap.putAll(orderMap);

                currentTime = System.currentTimeMillis();

                // set button list
                for (Map.Entry<SharedTicketInfo, CustomerInfoExt> entry : sortedMap.entrySet()) {
                    SharedTicketInfo ticket = entry.getKey();
                    CustomerInfoExt customer = entry.getValue();

                    if (customer != null) {

                        String ticketName = ticket.getName();
                        Image thumbImage = thumbNailBuilderWithDefault.getThumbNail();
                        try {
                            BufferedImage image = dataLogicCustomers.getCustomerInfo(customer.getId()).getImage();
                            if (image != null) {
                                thumbImage = thumbNailBuilderWithDefault.getThumbNail(image);
                            }
                        }
                        catch (BasicException ex) {
                            LOGGER.log(Level.WARNING, "Exception on getting entity image", ex);
                        }

                        ImageIcon icon = new ImageIcon(thumbImage);
                        flowTab.addButton(
                                icon,
                                new SelectedCustomerAction(ticket.getId()),
                                ticketName,
                                ticketName);
                    }

                }
                LOGGER.log(Level.INFO, "Time of finished loadCustomerOrders {0}", (System.currentTimeMillis() - currentTime));
            }
        });
    }

    private HashMap<SharedTicketInfo, CustomerInfoExt> mapSharedTicket(List<SharedTicketInfo> ticketList, List<CustomerInfoExt> customers) {
        HashMap<SharedTicketInfo, CustomerInfoExt> orderMap = new HashMap<>();

        for (SharedTicketInfo sharedTicketInfo : ticketList) {

            String ticketName = sharedTicketInfo.getName().trim();

            if (ticketName.contains("[") && ticketName.contains("]")) {

                // found order
                if (ticketName.startsWith("[")) {
                    // order without customer
                    orderMap.put(sharedTicketInfo, null);
                } else if (customers != null && !customers.isEmpty()) {
                    // find customer to ticket
                    for (CustomerInfoExt customer : customers) {
                        if (customer != null) {
                            String name = customer.getName().trim();
                            if (ticketName.startsWith(name)) {
                                orderMap.put(sharedTicketInfo, customer);
                                break;
                            }
                        }
                    }
                }
            }
        }

        return orderMap;
    }

    /**
     *
     * @param value
     */
    @Override
    public void setComponentEnabled(boolean value) {
        jPanelCustomers.setEnabled(value);

        synchronized (jPanelCustomers.getTreeLock()) {
            int compCount = jPanelCustomers.getComponentCount();
            for (int i = 0; i < compCount; i++) {
                jPanelCustomers.getComponent(i).setEnabled(value);
            }
        }
        this.setEnabled(value);
    }

    /**
     *
     * @param l
     */
    @Override
    public void addActionListener(ActionListener l) {
        listeners.add(ActionListener.class, l);
    }

    /**
     *
     * @param l
     */
    @Override
    public void removeActionListener(ActionListener l) {
        listeners.remove(ActionListener.class, l);
    }

    private void setActiveTicket(String id) throws BasicException {

        currentTicket = panelticket.getActiveTicket().getId();

        // BEGIN TRANSACTION
        TicketInfo ticket = dataLogicReceipts.getSharedTicket(id);
        if (ticket == null) {
            // Does not exists ???
            throw new BasicException(AppLocal.getIntString("message.noticket"));
        } else {
            dataLogicReceipts.deleteSharedTicket(id);
            currentTicket = id;
            panelticket.setActiveTicket(ticket, null);
            fireTicketSelectionChanged(ticket.getId());
        }
        // END TRANSACTION                 
    }

    private void fireTicketSelectionChanged(String ticketId) {
        EventListener[] l = listeners.getListeners(ActionListener.class);
        ActionEvent e = null;
        for (EventListener l1 : l) {
            if (e == null) {
                e = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, ticketId);
            }
            ((ActionListener) l1).actionPerformed(e);
        }
    }

    private class SelectedCustomerAction implements ActionListener {

        private final String ticketId;

        public SelectedCustomerAction(String ticketId) {
            this.ticketId = ticketId;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                if (ticketId != null) {
                    setActiveTicket(ticketId);
                }
            }
            catch (BasicException ex) {
                new MessageInf(ex).show(OrderCustomerList.this);
            }
        }
    }

    class CustomerComparator implements Comparator<SharedTicketInfo> {

        Map<SharedTicketInfo, CustomerInfoExt> base;

        public CustomerComparator(Map<SharedTicketInfo, CustomerInfoExt> base) {
            this.base = base;
        }

        // Note: this comparator imposes orderings that are inconsistent with equals.    
        @Override
        public int compare(SharedTicketInfo a, SharedTicketInfo b) {
            String nameA = base.get(a).getName();
            String nameB = base.get(b).getName();

            if (nameA.compareToIgnoreCase(nameB) < 0) {
                return -1;
            } else {
                return 1;
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanelCustomers = new javax.swing.JPanel();

        setMinimumSize(new java.awt.Dimension(256, 560));
        setPreferredSize(new java.awt.Dimension(256, 560));
        setLayout(new java.awt.BorderLayout());

        jPanelCustomers.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jPanelCustomers.setLayout(new java.awt.CardLayout());
        add(jPanelCustomers, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanelCustomers;
    // End of variables declaration//GEN-END:variables
}
