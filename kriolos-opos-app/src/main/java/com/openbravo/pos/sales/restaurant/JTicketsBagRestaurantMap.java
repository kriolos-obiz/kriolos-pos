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

import com.openbravo.pos.customers.CustomerInfo;
import com.openbravo.pos.forms.AppConfig;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.sales.JTicketsBag;
import com.openbravo.pos.sales.TicketsEditor;
import com.openbravo.pos.ticket.TicketInfo;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

/**
 * Pure-code Swing view for the restaurant table map.
 * Business logic and ticket persistence are delegated to {@link RestaurantMapController},
 * while button formatting is handled by {@link PlaceButtonRenderer}.
 */
public class JTicketsBagRestaurantMap extends JTicketsBag {

    private static final System.Logger LOGGER = System.getLogger(JTicketsBagRestaurantMap.class.getName());

    private final RestaurantMapController controller;
    private final JTicketsBagRestaurant ticketsBagRestaurant;
    private final JTicketsBagRestaurantRes restaurantReservation;

    // UI Components
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel mapPanel = new JPanel(new BorderLayout());
    private final JTabbedPane restaurantFloorsJTabbedPane = new JTabbedPane();
    private final JLabel lblInfo = new JLabel();
    private final JLabel lblAutoRefreshTimer = new JLabel();
    private final JButton btnReservations = new JButton();
    private final JButton btnRefresh = new JButton();
    private final JButton btnLayout = new JButton();
    private final JButton btnSaveLayout = new JButton();

    private final Map<String, PlaceButton> placeButtons = new HashMap<>();

    private int currentSelectedTabIndex = 0;
    private boolean transBtns;
    private boolean actionEnabled = true;
    private Timer autoRefreshTimer = null;

    public JTicketsBagRestaurantMap(AppView app, TicketsEditor panelticket) {
        super(app, panelticket);

        this.controller = new RestaurantMapController(app, panelticket);
        this.ticketsBagRestaurant = new JTicketsBagRestaurant(app, this);
        this.restaurantReservation = new JTicketsBagRestaurantRes(app, this);
        this.transBtns = AppConfig.getInstance().getBoolean("table.transbtn");

        initUI();
        initAutoRefreshTimer();

        loadData();
        printState();
    }

    private void initUI() {
        setLayout(cardLayout);

        // Top Toolbar
        JPanel topBarPanel = new JPanel(new BorderLayout());
        JPanel leftToolBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));

        configureToolbarButton(btnReservations, AppLocal.getIntString("button.reservations"),
                "/com/openbravo/images/date.png", "Open Reservations screen", e -> {
            showView("res");
            restaurantReservation.activate();
        });
        leftToolBar.add(btnReservations);

        configureToolbarButton(btnRefresh, AppLocal.getIntString("button.reloadticket"),
                "/com/openbravo/images/reload.png", "Reload table information", e -> {
            controller.setPlaceClipboard(null);
            controller.setCustomerInfo(null);
            loadData();
            printState();
        });
        leftToolBar.add(btnRefresh);

        lblInfo.setFont(new Font("Arial", Font.PLAIN, 14));
        leftToolBar.add(lblInfo);

        configureToolbarButton(btnLayout, AppLocal.getIntString("button.layout"),
                "/com/openbravo/images/movetable.png", null, e -> toggleLayoutMode());
        leftToolBar.add(btnLayout);

        configureToolbarButton(btnSaveLayout, AppLocal.getIntString("button.save"),
                "/com/openbravo/images/filesave.png", null, e -> controller.savePlacesLayout());
        btnSaveLayout.setVisible(false);
        leftToolBar.add(btnSaveLayout);

        topBarPanel.add(leftToolBar, BorderLayout.LINE_START);

        lblAutoRefreshTimer.setHorizontalAlignment(SwingConstants.RIGHT);
        lblAutoRefreshTimer.setFont(new Font("Arial", Font.PLAIN, 14));
        topBarPanel.add(lblAutoRefreshTimer, BorderLayout.CENTER);

        mapPanel.add(topBarPanel, BorderLayout.NORTH);

        // Tabbed pane for restaurant floors
        restaurantFloorsJTabbedPane.applyComponentOrientation(getComponentOrientation());
        restaurantFloorsJTabbedPane.setBorder(new EmptyBorder(new Insets(5, 5, 5, 5)));
        restaurantFloorsJTabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        restaurantFloorsJTabbedPane.setFocusable(false);
        restaurantFloorsJTabbedPane.setRequestFocusEnabled(false);
        mapPanel.add(restaurantFloorsJTabbedPane, BorderLayout.CENTER);

        add(mapPanel, "map");
        add(restaurantReservation, "res");
    }

    private void configureToolbarButton(JButton button, String text, String iconPath, String tooltip, ActionListener listener) {
        button.setFont(new Font("Arial", Font.PLAIN, 12));
        if (iconPath != null) {
            button.setIcon(new ImageIcon(getClass().getResource(iconPath)));
        }
        button.setText(text);
        if (tooltip != null) {
            button.setToolTipText(tooltip);
        }
        button.setFocusPainted(false);
        button.setFocusable(false);
        button.setPreferredSize(new Dimension(100, 45));
        if (listener != null) {
            button.addActionListener(listener);
        }
    }

    private void initAutoRefreshTimer() {
        ResourceBundle bundle = ResourceBundle.getBundle("pos_messages");
        String autoRefreshProp = getAppView().getProperties().getProperty("till.autoRefreshTableMap");

        if ("true".equalsIgnoreCase(autoRefreshProp)) {
            lblAutoRefreshTimer.setText(bundle.getString("label.autoRefreshTableMapTimerON"));

            int refreshSeconds = 10;
            try {
                String timerProp = getAppView().getProperties().getProperty("till.autoRefreshTimer");
                if (timerProp != null && !timerProp.isBlank()) {
                    refreshSeconds = Math.max(10, Integer.parseInt(timerProp));
                }
            }
            catch (NumberFormatException ignored) {
                refreshSeconds = 10;
            }

            this.autoRefreshTimer = new Timer(refreshSeconds * 1000, new TableMapRefreshActionListener());
        } else {
            lblAutoRefreshTimer.setText(bundle.getString("label.autoRefreshTableMapTimerOFF"));
        }
    }

    private class TableMapRefreshActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            // Guard: never refresh or disturb state if a table is currently open
            if (controller.getPlaceCurrent() != null) {
                return;
            }
            LOGGER.log(System.Logger.Level.INFO, "Table Map Refresh at: " + new Date());
            loadData();
            printState();
        }
    }

    private void ensureTimerStart() {
        LOGGER.log(System.Logger.Level.INFO, "Table Refresh timer start");
        if (this.autoRefreshTimer != null && !this.autoRefreshTimer.isRunning()) {
            this.autoRefreshTimer.start();
        }
    }

    private void ensureTimerStop() {
        LOGGER.log(System.Logger.Level.INFO, "Table Refresh timer stop");
        if (this.autoRefreshTimer != null && this.autoRefreshTimer.isRunning()) {
            this.autoRefreshTimer.stop();
        }
    }

    private void ensurePermissionSalesLayout() {
        boolean showLayout = getAppView().hasPermission("sales.Layout");
        btnLayout.setVisible(showLayout);
        btnSaveLayout.setVisible(false);
    }

    private void toggleLayoutMode() {
        ResourceBundle bundle = ResourceBundle.getBundle("pos_messages");
        if (bundle.getString("button.layout").equals(btnLayout.getText())) {
            actionEnabled = false;
            btnSaveLayout.setVisible(true);
            btnLayout.setText(bundle.getString("button.disablelayout"));

            for (PlaceButton btn : placeButtons.values()) {
                if (transBtns) {
                    btn.setOpaque(true);
                    btn.setContentAreaFilled(true);
                    btn.setBorderPainted(true);
                }
            }
            ensureTimerStop();
        } else {
            actionEnabled = true;
            btnSaveLayout.setVisible(false);
            btnLayout.setText(bundle.getString("button.layout"));

            for (PlaceButton btn : placeButtons.values()) {
                if (transBtns) {
                    btn.setOpaque(false);
                    btn.setContentAreaFilled(false);
                    btn.setBorderPainted(false);
                }
            }
            ensureTimerStart();
        }
    }

    private void loadData() {
        currentSelectedTabIndex = restaurantFloorsJTabbedPane.getSelectedIndex();

        ensurePermissionSalesLayout();
        controller.loadData(this);

        restaurantFloorsJTabbedPane.removeAll();

        Map<String, Floor> floorMap = new HashMap<>();
        for (Floor floor : controller.getFloorList()) {
            floorMap.put(floor.getID(), floor);
            floor.getContainer().applyComponentOrientation(getComponentOrientation());

            JScrollPane scrollPane = new JScrollPane();
            scrollPane.applyComponentOrientation(getComponentOrientation());

            JPanel contentPanel = new JPanel();
            contentPanel.applyComponentOrientation(getComponentOrientation());

            restaurantFloorsJTabbedPane.addTab(floor.getName(), floor.getIcon(), scrollPane);
            scrollPane.setViewportView(contentPanel);
            contentPanel.add(floor.getContainer());
        }

        if (currentSelectedTabIndex >= restaurantFloorsJTabbedPane.getTabCount()) {
            currentSelectedTabIndex = 0;
        }
        if (restaurantFloorsJTabbedPane.getTabCount() > 0) {
            restaurantFloorsJTabbedPane.setSelectedIndex(currentSelectedTabIndex);
        }

        // Attach tables to their corresponding floor container
        placeButtons.clear();
        for (Place pl : controller.getPlaceList()) {
            Floor floor = floorMap.get(pl.getFloor());
            PlaceButton btn = new PlaceButton(pl);
            placeButtons.put(pl.getId(), btn);

            if (floor != null) {
                floor.getContainer().add(btn);
                btn.updateBounds();
            }

            if (transBtns) {
                btn.setOpaque(false);
                btn.setContentAreaFilled(false);
                btn.setBorderPainted(false);
            }

            // Drag-and-drop listener for layout mode
            btn.addMouseMotionListener(new MouseAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    if (!actionEnabled) {
                        if (btn.getDiffX() == 0) {
                            btn.setDiffX(btn.getX() - pl.getX());
                            btn.setDiffY(btn.getY() - pl.getY());
                        }
                        int newX = e.getX() + btn.getX();
                        int newY = e.getY() + btn.getY();
                        btn.setBounds(newX + btn.getDiffX(), newY + btn.getDiffY(),
                                btn.getWidth(), btn.getHeight());
                        pl.setX(newX);
                        pl.setY(newY);
                    }
                }
            });

            btn.addActionListener(e -> onPlaceSelection(pl));
        }

        java.awt.EventQueue.invokeLater(() -> {
            mapPanel.revalidate();
            mapPanel.repaint();
        });
    }

    private void onPlaceSelection(Place place) {
        if (!actionEnabled) {
            PlaceButton btn = placeButtons.get(place.getId());
            if (btn != null) {
                btn.setDiffX(0);
                btn.setDiffY(0);
            }
            return;
        }

        boolean opened = controller.handleTableSelection(place, this);
        if (opened) {
            ensureTimerStop();
        }
        printState();
    }

    private void printState() {
        Place clipboard = controller.getPlaceClipboard();
        CustomerInfo cust = controller.getCustomerInfo();

        if (clipboard == null) {
            if (cust == null) {
                lblInfo.setText(null);
                for (PlaceButton btn : placeButtons.values()) {
                    PlaceButtonRenderer.renderButton(btn, controller.getPlaceService(), getAppView());
                }
                btnReservations.setEnabled(true);
            } else {
                lblInfo.setText(AppLocal.getIntString("label.restaurantcustomer", new Object[]{cust.getName()}));
                for (PlaceButton btn : placeButtons.values()) {
                    btn.setEnabled(!btn.getPlace().hasPeople());
                }
                btnReservations.setEnabled(false);
            }
        } else {
            lblInfo.setText(AppLocal.getIntString("label.restaurantmove", new Object[]{clipboard.getName()}));
            for (PlaceButton btn : placeButtons.values()) {
                btn.setEnabled(true);
            }
            btnReservations.setEnabled(false);
        }
    }

    private void showView(String view) {
        cardLayout.show(this, view);
    }

    // -------------------------------------------------------------
    // JTicketsBag contract methods
    // -------------------------------------------------------------

    @Override
    public void activate() {
        LOGGER.log(System.Logger.Level.INFO, "Activate restaurant map");
        controller.setPlaceClipboard(null);
        controller.setCustomerInfo(null);
        ensurePermissionSalesLayout();
        loadData();
        printState();

        m_panelticket.setActiveTicket(new TicketInfo(), null);
        ticketsBagRestaurant.activate();

        showView("map");
        ensureTimerStart();
    }

    @Override
    public boolean deactivate() {
        LOGGER.log(System.Logger.Level.INFO, "Deactivate restaurant map");
        ensureTimerStop();

        if (viewTables()) {
            controller.setPlaceClipboard(null);
            controller.setCustomerInfo(null);

            Place placeCurrent = controller.getPlaceCurrent();
            if (placeCurrent != null) {
                controller.releaseCurrentTicket(this);
            }
            printState();
            m_panelticket.setActiveTicket(null, null);
            return true;
        }
        return false;
    }

    @Override
    public void deleteTicket() {
        controller.deleteCurrentTicket(this);
        printState();
        m_panelticket.setActiveTicket(null, null);
        ensureTimerStart();
    }

    @Override
    protected JComponent getBagComponent() {
        return ticketsBagRestaurant;
    }

    @Override
    protected JComponent getNullComponent() {
        return this;
    }

    public TicketInfo getActiveTicket() {
        return m_panelticket.getActiveTicket();
    }

    public void moveTicket() {
        controller.moveCurrentTicket(this);
        printState();
        m_panelticket.setActiveTicket(null, null);
    }

    public boolean viewTables(CustomerInfo c) {
        if (restaurantReservation.deactivate()) {
            showView("map");
            controller.setPlaceClipboard(null);
            controller.setCustomerInfo(c);
            printState();
            return true;
        }
        return false;
    }

    public boolean viewTables() {
        return viewTables(null);
    }

    public void newTicket() {
        controller.releaseCurrentTicket(this);
        printState();
        m_panelticket.setActiveTicket(null, null);
        ensureTimerStart();
    }

    public String getTable() {
        Place current = controller.getPlaceCurrent();
        return current != null ? current.getId() : null;
    }

    public String getTableName() {
        Place current = controller.getPlaceCurrent();
        return current != null ? current.getName() : null;
    }

    public void setButtonTextBags(String btnText) {
        Place clipboard = controller.getPlaceClipboard();
        if (clipboard != null) {
            PlaceButton btn = placeButtons.get(clipboard.getId());
            if (btn != null) {
                btn.setText(btnText);
            }
        }
    }
}
