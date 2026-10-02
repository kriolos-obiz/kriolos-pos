//    KriolOS POS
//    Copyright (c) 2019-2026 KriolOS
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
package com.openbravo.data.gui.modal;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.SecondaryLoop;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;

/**
 * In-frame overlay modal strategy for {@link PosUIModal}.
 * <p>
 * Displays modal panels directly within the parent window's {@link JLayeredPane}
 * using a semi-transparent scrim/backdrop and a {@link SecondaryLoop} to achieve
 * true modal blocking without creating separate OS-level windows.
 * </p>
 *
 * @author KriolOS
 */
public class InFrameOverlayModalStrategy implements ModalStrategy {

    private JPanel overlayPanel;
    private JLayeredPane targetLayeredPane;
    private SecondaryLoop secondaryLoop;

    @Override
    public void display(PosUIModal modal) {
        targetLayeredPane = resolveLayeredPane(modal.getParent());
        if (targetLayeredPane == null) {
            // Fallback to NativeDialogModalStrategy if no host container is available
            new NativeDialogModalStrategy().display(modal);
            return;
        }

        overlayPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setColor(new Color(0, 0, 0, 128));
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
            }
        };
        overlayPanel.setOpaque(false);
        overlayPanel.setBounds(0, 0, targetLayeredPane.getWidth(), targetLayeredPane.getHeight());

        // Intercept all mouse inputs to block background interaction
        MouseAdapter blocker = new MouseAdapter() {};
        overlayPanel.addMouseListener(blocker);
        overlayPanel.addMouseMotionListener(blocker);

        // Keep overlay panel sized to target layered pane
        targetLayeredPane.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (overlayPanel != null) {
                    overlayPanel.setBounds(0, 0, targetLayeredPane.getWidth(), targetLayeredPane.getHeight());
                    overlayPanel.revalidate();
                    overlayPanel.repaint();
                }
            }
        });

        // Build the modal card
        JPanel cardPanel = new JPanel(new BorderLayout());
        cardPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180), 1),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));

        // Title bar
        if (modal.getTitle() != null && !modal.getTitle().isBlank()) {
            JPanel titleBar = new JPanel();
            titleBar.setLayout(new BoxLayout(titleBar, BoxLayout.X_AXIS));
            titleBar.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 8));
            titleBar.setBackground(new Color(245, 245, 245));

            JLabel titleLabel = new JLabel(modal.getTitle());
            titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 13f));
            titleBar.add(titleLabel);
            titleBar.add(Box.createHorizontalGlue());

            JButton closeBtn = new JButton("✕");
            closeBtn.setBorderPainted(false);
            closeBtn.setContentAreaFilled(false);
            closeBtn.setFocusPainted(false);
            closeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            closeBtn.addActionListener(e -> close(modal));
            titleBar.add(closeBtn);

            cardPanel.add(titleBar, BorderLayout.NORTH);
        }

        // Content
        if (modal.getPreferredSize() != null) {
            modal.getContent().setPreferredSize(modal.getPreferredSize());
        }
        cardPanel.add(modal.getContent(), BorderLayout.CENTER);
        overlayPanel.add(cardPanel);

        // Add to MODAL_LAYER
        targetLayeredPane.add(overlayPanel, JLayeredPane.MODAL_LAYER);
        targetLayeredPane.revalidate();
        targetLayeredPane.repaint();

        if (modal.isModal() && EventQueue.isDispatchThread()) {
            secondaryLoop = Toolkit.getDefaultToolkit().getSystemEventQueue().createSecondaryLoop();
            secondaryLoop.enter();
        }
    }

    @Override
    public void close(PosUIModal modal) {
        if (overlayPanel != null && targetLayeredPane != null) {
            targetLayeredPane.remove(overlayPanel);
            targetLayeredPane.revalidate();
            targetLayeredPane.repaint();
            overlayPanel = null;
        }

        if (secondaryLoop != null) {
            secondaryLoop.exit();
            secondaryLoop = null;
        }

        modal.notifyClosed();
    }

    private static JLayeredPane resolveLayeredPane(Component parent) {
        if (parent != null) {
            if (parent instanceof RootPaneContainer rpc) {
                return rpc.getLayeredPane();
            }
            JRootPane rootPane = SwingUtilities.getRootPane(parent);
            if (rootPane != null) {
                return rootPane.getLayeredPane();
            }
        }
        Window owner = PosUIModal.resolveWindowOwner(parent);
        if (owner instanceof RootPaneContainer rpc) {
            return rpc.getLayeredPane();
        }
        return null;
    }
}
