package com.openbravo.pos.sales.shared;

import com.openbravo.basic.BasicException;
import com.openbravo.data.gui.modal.PosUIModal;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.sales.DataLogicReceipts;
import com.openbravo.pos.sales.SharedTicketInfo;
import com.openbravo.pos.sales.SharedTicketService;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.io.Serial;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;

public class JTicketsBagSharedPanel extends JPanel {

    @Serial
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(JTicketsBagSharedPanel.class.getName());

    private String selectedTicketId;
    private PosUIModal modalContext;
    private JPanel ticketsPanel;

    public JTicketsBagSharedPanel() {
        setName("kriolos:sales:tickets-bag-shared-panel");
        initComponents();
    }

    public void setModalContext(PosUIModal modalContext) {
        this.modalContext = modalContext;
    }

    public String getSelectedTicketId() {
        return selectedTicketId;
    }

    public void loadTickets(List<SharedTicketInfo> tickets, SharedTicketService sharedTicketService) {
        selectedTicketId = null;
        ticketsPanel.removeAll();

        for (var ticketInfo : tickets) {
            var ticketName = ticketInfo.getName();
            var userName = ticketInfo.getUserName();
            var totalText = "---";
            var ticketDate = "";

            try {
                var ticket = sharedTicketService.getSharedTicket(ticketInfo.getId());
                if (ticket != null) {
                    totalText = ticket.printTotal();
                    ticketDate = ticket.printDate(); // Correctly assigning the date here
                } else {
                    LOGGER.log(Level.SEVERE, "Cannot recover TicketInfo for ticket Id: {0}", ticketInfo.getId());
                }
            } catch (BasicException ex) {
                totalText = "Error";
                LOGGER.log(Level.SEVERE, "Exception recovering TicketInfo for ticket Id: " + ticketInfo.getId(), ex);
            }

            ticketsPanel.add(new JButtonTicket(ticketInfo, ticketName, userName, totalText, ticketDate));
        }

        revalidate();
        repaint();
    }

    /**
     * @deprecated Use {@link #loadTickets(List, SharedTicketService)} instead.
     */
    @Deprecated
    public void loadTickets(List<SharedTicketInfo> tickets, DataLogicReceipts dlReceipts) {
        loadTickets(tickets, (SharedTicketService) dlReceipts);
    }

    public static String show(Component parent, List<SharedTicketInfo> tickets, SharedTicketService sharedTicketService) {
        if (tickets == null || tickets.isEmpty()) {
            JOptionPane.showMessageDialog(parent,
                    AppLocal.getIntString("message.nosharedtickets"),
                    AppLocal.getIntString("message.sharedtickettitle"),
                    JOptionPane.INFORMATION_MESSAGE);
            return null;
        }

        var panel = new JTicketsBagSharedPanel();
        panel.loadTickets(tickets, sharedTicketService);
        panel.setPreferredSize(new Dimension(500, 600));

        var modal = PosUIModal.create(parent, panel)
                .setTitle(AppLocal.getIntString("caption.tickets"))
                .setModal(true)
                .setResizable(false);
                
        panel.setModalContext(modal);
        modal.show();
        
        return panel.getSelectedTicketId();
    }

    /**
     * @deprecated Use {@link #show(Component, List, SharedTicketService)} instead.
     */
    @Deprecated
    public static String show(Component parent, List<SharedTicketInfo> tickets, DataLogicReceipts dlReceipts) {
        return show(parent, tickets, (SharedTicketService) dlReceipts);
    }

    private class JButtonTicket extends JButton {

        @Serial
        private static final long serialVersionUID = 1L;

        public JButtonTicket(SharedTicketInfo ticketInfo, String ticketName, String userName, String totalText, String ticketDate) {
            super();
            
            setName("kriolos:sales:btn-ticket-" + ticketInfo.getId());
            setFocusPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            setLayout(new BorderLayout());

            var content = new JPanel(new BorderLayout(10, 5));
            content.setOpaque(false);
            content.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));

            // Top Section: Name (Left) and Total (Right)
            var topPanel = new JPanel(new BorderLayout());
            topPanel.setOpaque(false);
            
            var nameLabel = new JLabel(ticketName);
            nameLabel.setFont(new Font(Font.DIALOG, Font.BOLD, 18));
            
            var totalLabel = new JLabel(totalText);
            totalLabel.setFont(new Font(Font.DIALOG, Font.BOLD, 20));
            
            topPanel.add(nameLabel, BorderLayout.WEST);
            topPanel.add(totalLabel, BorderLayout.EAST);
            
            // Bottom Section: User (Left) and Date (Right)
            var bottomPanel = new JPanel(new BorderLayout());
            bottomPanel.setOpaque(false);

            var userLabel = new JLabel(userName);
            userLabel.setFont(new Font(Font.DIALOG, Font.PLAIN, 14));

            var dateLabel = new JLabel(ticketDate);
            dateLabel.setFont(new Font(Font.DIALOG, Font.PLAIN, 14));
            
            // Apply muted color to both user and date labels for hierarchy
            var mutedColor = UIManager.getColor("Label.disabledForeground");
            if (mutedColor == null) {
                mutedColor = UIManager.getColor("textInactiveText"); 
            }
            if (mutedColor != null) {
                userLabel.setForeground(mutedColor);
                dateLabel.setForeground(mutedColor);
            }

            bottomPanel.add(userLabel, BorderLayout.WEST);
            bottomPanel.add(dateLabel, BorderLayout.EAST);

            content.add(topPanel, BorderLayout.NORTH);
            content.add(bottomPanel, BorderLayout.SOUTH);

            add(content, BorderLayout.CENTER);

            addActionListener(e -> {
                selectedTicketId = ticketInfo.getId();
                if (modalContext != null) {
                    modalContext.setResult(selectedTicketId);
                    modalContext.close();
                }
            });
        }
    }

    private void initComponents() {
        var mainPanel = new JPanel();
        var scrollPane = new JScrollPane();
        var listContainerPanel = new JPanel();
        var footerPanel = new JPanel();
        var footerRightPanel = new JPanel();
        var btnCancel = new JButton();
        
        ticketsPanel = new JPanel();

        setLayout(new BorderLayout());

        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.setLayout(new BorderLayout());

        listContainerPanel.setLayout(new BorderLayout());

        ticketsPanel.setName("kriolos:sales:tickets-list");
        ticketsPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        ticketsPanel.setLayout(new GridLayout(0, 1, 0, 12));
        listContainerPanel.add(ticketsPanel, BorderLayout.NORTH);

        scrollPane.setName("kriolos:sales:tickets-scrollpane");
        scrollPane.setViewportView(listContainerPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(35, 35));
        scrollPane.getHorizontalScrollBar().setPreferredSize(new Dimension(35, 35));
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        mainPanel.add(scrollPane, BorderLayout.CENTER);
        add(mainPanel, BorderLayout.CENTER);

        footerPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        footerPanel.add(footerRightPanel);

        btnCancel.setName("kriolos:sales:btn-cancel");
        btnCancel.setFont(new Font(Font.DIALOG, Font.BOLD, 14));
        
        var cancelIconUrl = getClass().getResource("/com/openbravo/images/cancel.png");
        if (cancelIconUrl != null) {
            btnCancel.setIcon(new ImageIcon(cancelIconUrl));
        }
        
        btnCancel.setText(AppLocal.getIntString("button.close"));
        btnCancel.setFocusPainted(false);
        btnCancel.setMargin(new Insets(8, 16, 8, 16));
        btnCancel.setPreferredSize(new Dimension(130, 50));
        
        btnCancel.addActionListener(e -> {
            if (modalContext != null) {
                modalContext.close();
            }
        });
        
        footerPanel.add(btnCancel);
        add(footerPanel, BorderLayout.SOUTH);
    }
}