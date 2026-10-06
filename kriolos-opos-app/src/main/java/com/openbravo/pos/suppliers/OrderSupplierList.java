//    KriolOS POS
//    Copyright (c) 2019-2023 KriolOS
//    
//
//     
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
package com.openbravo.pos.suppliers;

import com.openbravo.basic.BasicException;
import com.openbravo.data.gui.MessageInf;
import com.openbravo.data.loader.ImageUtils;
import com.openbravo.pos.businesspartner.TicketSelector;
import com.openbravo.pos.catalog.JCatalogTab;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.sales.DataLogicReceipts;
import com.openbravo.pos.sales.SharedTicketInfo;
import com.openbravo.pos.sales.TicketsEditor;
import com.openbravo.pos.ticket.TicketInfo;
import com.openbravo.pos.util.ThumbNailBuilder;
import java.awt.Component;
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
 * @author JG uniCenta - outline/prep for  eCommerce connector
 */
public class OrderSupplierList extends JPanel implements TicketSelector {

    protected static final Logger LOGGER = Logger.getLogger("com.openbravo.pos.suppliers.SuppliersList");
    private static final long serialVersionUID = 1L;

    protected AppView application;
    private String currentTicket;
    protected TicketsEditor panelticket;
    protected EventListenerList listeners = new EventListenerList();
    private final DataLogicSuppliers dataLogicSuppliers;
    private final DataLogicReceipts dataLogicReceipts;

    public OrderSupplierList(DataLogicSuppliers dlSuppliers, AppView app, TicketsEditor panelticket) {
        this.application = app;
        this.panelticket = panelticket;
        this.dataLogicSuppliers = dlSuppliers;
        this.dataLogicReceipts = application.getBean(DataLogicReceipts.class);

        initComponents();
    }

    public Component getComponent() {
        return this;
    }

    public void reloadSuppliers() throws BasicException {
        loadSuppliers();
    }

    public void loadSuppliers() throws BasicException {

        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                JCatalogTab flowTab = new JCatalogTab();

                jPanelSuppliers.removeAll();
                jPanelSuppliers.add(flowTab);

                List<SupplierInfoExt> suppliers = null;
                List<SharedTicketInfo> ticketList = null;

                long time = System.currentTimeMillis();

                try {
                    LOGGER.log(Level.INFO, "Time of getSuppliersWithOutImage {0}", (System.currentTimeMillis() - time));
                    time = System.currentTimeMillis();

                    ticketList = dataLogicReceipts.getSharedTicketList();
                    LOGGER.log(Level.INFO, "Time of getSharedTicketList {0}", (System.currentTimeMillis() - time));
                    time = System.currentTimeMillis();
                } catch (BasicException ex) {
                    Logger.getLogger(OrderSupplierList.class.getName()).log(Level.SEVERE, null, ex);
                }

                HashMap<SharedTicketInfo, SupplierInfoExt> orderMap = 
                        mapSharedTicket(ticketList, suppliers);
                // sort
                SupplierComparator bvc = new SupplierComparator(orderMap);
                TreeMap<SharedTicketInfo, SupplierInfoExt> sortedMap = new TreeMap<>(bvc);
                sortedMap.putAll(orderMap);

                LOGGER.log(Level.INFO, "Time of orderMap {0}", (System.currentTimeMillis() - time));
                time = System.currentTimeMillis();

                // set button list
                for (Map.Entry<SharedTicketInfo, SupplierInfoExt> entry : sortedMap.entrySet()) {
                    SharedTicketInfo ticket = entry.getKey();
                    SupplierInfoExt supplier = entry.getValue();

                    String name = ticket.getName();
                    BufferedImage image = ImageUtils.getImageFromClasspath("/com/openbravo/images/no_image.png");
                    String username;
                    if (name.indexOf("[") != 0) {
                        username = name.substring(0, name.indexOf("[") - 1);
                        username = username.replace("-", "");
                    } else {
                        username = "unknown";
                    }
                    String orderId = name.substring(name.indexOf("["), name.indexOf("]") + 1);
                    String text = username.trim() + " : " + orderId.trim();
                    String htmltext = "<html><center>" + username.trim() + "<br> " + orderId.trim() + "</center></html>";

                    final ThumbNailBuilder tnbbutton = new ThumbNailBuilder(90, 98, image);
                    ImageIcon icon = new ImageIcon(tnbbutton.getThumbNail());
                    flowTab.addButton(
                            icon,
                            new SelectedSupplierAction(ticket.getId()),
                            htmltext,
                            text);
                }
                LOGGER.log(Level.INFO, "Time of finished loadSupplierOrders {0}", (System.currentTimeMillis() - time));
            }
        });
    }

    private HashMap<SharedTicketInfo, SupplierInfoExt> mapSharedTicket(List<SharedTicketInfo> ticketList, List<SupplierInfoExt> suppliers) {
        HashMap<SharedTicketInfo, SupplierInfoExt> orderMap = new HashMap<>();

        for (SharedTicketInfo sharedTicketInfo : ticketList) {

            String ticketName = sharedTicketInfo.getName().trim();

            if (ticketName.contains("[") && ticketName.contains("]")) {

                // found order
                if (ticketName.startsWith("[")) {
                    orderMap.put(sharedTicketInfo, null);
                } else if (suppliers != null && !suppliers.isEmpty()) {
                    for (SupplierInfoExt supplier : suppliers) {
                        if (supplier != null) {
                            String name = supplier.getName().trim();
                            if (ticketName.startsWith(name)) {
                                orderMap.put(sharedTicketInfo, supplier);
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
    public void setComponentEnabled(boolean value) {
        jPanelSuppliers.setEnabled(value);

        synchronized (jPanelSuppliers.getTreeLock()) {
            int compCount = jPanelSuppliers.getComponentCount();
            for (int i = 0; i < compCount; i++) {
                jPanelSuppliers.getComponent(i).setEnabled(value);
            }
        }
        this.setEnabled(value);
    }

    public void addActionListener(ActionListener actionListener) {
        listeners.add(ActionListener.class, actionListener);
    }

    public void removeActionListener(ActionListener actionListener) {
        listeners.remove(ActionListener.class, actionListener);
    }

    private void setActiveTicket(String id) throws BasicException {

        currentTicket = panelticket.getActiveTicket().getId();

        TicketInfo ticket = dataLogicReceipts.getSharedTicket(id);
        if (ticket == null) {
            throw new BasicException(AppLocal.getIntString("message.noticket"));
        } else {
            dataLogicReceipts.deleteSharedTicket(id);
            currentTicket = id;
            panelticket.setActiveTicket(ticket, null);
            fireTicketSelectionChanged(ticket.getId());
        }
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

    @Override
    public void loadPartners() throws BasicException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private class SelectedSupplierAction implements ActionListener {

        private final String ticketId;

        public SelectedSupplierAction(String ticketId) {
            this.ticketId = ticketId;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                if (ticketId != null) {
                    setActiveTicket(ticketId);
                }
            } catch (BasicException ex) {
                new MessageInf(ex).show(OrderSupplierList.this);
            }
        }
    }

    class SupplierComparator implements Comparator<SharedTicketInfo> {

        Map<SharedTicketInfo, SupplierInfoExt> base;

        public SupplierComparator(Map<SharedTicketInfo, SupplierInfoExt> base) {
            this.base = base;
        }

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

        jPanelSuppliers = new javax.swing.JPanel();

        setMinimumSize(new java.awt.Dimension(256, 560));
        setPreferredSize(new java.awt.Dimension(256, 560));
        setLayout(new java.awt.BorderLayout());

        jPanelSuppliers.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jPanelSuppliers.setLayout(new java.awt.CardLayout());
        add(jPanelSuppliers, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanelSuppliers;
    // End of variables declaration//GEN-END:variables
}
