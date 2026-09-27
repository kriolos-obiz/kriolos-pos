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
package com.openbravo.pos.forms;

import com.openbravo.basic.BasicException;
import com.openbravo.data.gui.JMessagePanel;
import com.openbravo.data.gui.MessageInf;
import com.openbravo.pos.config.JPanelConfiguration;
import com.openbravo.pos.instance.AppMessage;
import com.openbravo.pos.util.OSValidator;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

/**
 * WindowShell represents the top-level operating system desktop window container.
 *
 * <p>In the Application Shell Pattern, WindowShell manages the outer window lifecycle,
 * fullscreen/kiosk modes, window decorations, OS taskbar branding, and the root layered pane.</p>
 */
public class WindowShell extends javax.swing.JFrame implements AppMessage {

    private static final Logger LOGGER = Logger.getLogger(WindowShell.class.getName());
    private static final long serialVersionUID = 1L;

    private final ApplicationShell m_rootapp;
    private final AppProperties m_props;

    public WindowShell(AppProperties props) {
        initComponents();
        setName("kriolos:window:shell");
        m_props = props;
        m_rootapp = new ApplicationShell(m_props);
    }

    public ApplicationShell getApplicationShell() {
        return m_rootapp;
    }

    public ApplicationShell getRootAppPanel() {
        return m_rootapp;
    }

    public void initFrame() {

        setTitle(AppLocal.APP_NAME + " - " + AppLocal.APP_VERSION);
        String image = "/com/openbravo/images/app_logo_48x48.png";

        try {
            // 1. Load the frame icon (Classic Swing window decoration)
            java.awt.Image appIcon = ImageIO.read(WindowShell.class.getResourceAsStream(image));
            this.setIconImage(appIcon);

            // 2. Set the OS Taskbar icon (Modern cross-platform support)
            if (java.awt.Taskbar.isTaskbarSupported()) {
                java.awt.Taskbar taskbar = java.awt.Taskbar.getTaskbar();
                taskbar.setIconImage(appIcon);
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Exception load icon: " + image, e);
        } catch (UnsupportedOperationException e) {
            LOGGER.log(Level.CONFIG, "Taskbar API is not supported in this environment.", e);
        } catch (SecurityException e) {
            LOGGER.log(Level.WARNING, "Security exception encountered accessing the Taskbar.", e);
        }

        // LOAD APP PANEL
        try {
            m_rootapp.initApp();

            getContentPane().add(m_rootapp, BorderLayout.CENTER);
            sendInitEnvent();

        } catch (BasicException ex) {
            // LOAD CONFIG PANEL
            int opionRes = JMessagePanel.showConfirmDialog(this,
                    new MessageInf(MessageInf.SGN_DANGER,
                            "<html>Application fail to start<br>Do you want to open the configuration panel?", ex));

            if (opionRes == JOptionPane.YES_OPTION) {
                dispose();
                JPanelConfiguration config = new JPanelConfiguration(m_props);
                config.setCloseListener((JPanelConfiguration.CloseEvent e) -> {
                    dispose();
                    System.exit(0);
                });

                getContentPane().add(config, BorderLayout.CENTER);

                setVisible(true);
                return; // void (screenmode) execute
            } else {
                dispose();
                System.exit(0);
            }
        }

        // THIS IS NEED HERE TO PRESENT CONFIG PANEL
        String screenmode = m_props.getProperty("machine.screenmode");
        if (null == screenmode) {
            modeWindow();
        } else {
            switch (screenmode) {
                case "fullscreen":
                    modeKiosk();
                    break;
                case "windowmaximised":
                    modeWindowMaximized();
                    break;
                default:
                    modeWindow();
                    break;
            }
        }
    }

    private void modeWindowMaximized() {
        setExtendedState(MAXIMIZED_BOTH);
        setLocationRelativeTo(null); // center
        setVisible(true);
    }

    private void modeWindow() {
        pack();
        setLocationRelativeTo(null); // center
        setVisible(true);
    }

    private void modeKiosk() {
        dispose();
        setUndecorated(true);
        setResizable(false);

        // LINUX/UNIX
        if (new OSValidator().isUnix()) {
            GraphicsDevice device = GraphicsEnvironment
                    .getLocalGraphicsEnvironment().getDefaultScreenDevice();

            if (device.isFullScreenSupported()) {
                setResizable(true);

                addFocusListener(new FocusListener() {
                    @Override
                    public void focusGained(FocusEvent arg0) {
                        setAlwaysOnTop(true);
                    }

                    @Override
                    public void focusLost(FocusEvent arg0) {
                        setAlwaysOnTop(false);
                    }
                });
                device.setFullScreenWindow(this);
            } else {
                setVisible(true);
            }
        } else {
            Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
            setBounds(0, 0, d.width, d.height);
            setVisible(true);
        }
    }

    private void sendInitEnvent() {
    }

    /**
     * @throws RemoteException
     */
    @Override
    public void restoreWindow() throws RemoteException {
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                // 1. De-iconify (De-minimize) if the frame is minimized
                if ((getExtendedState() & Frame.ICONIFIED) != 0) {
                    setExtendedState(getExtendedState() & ~Frame.ICONIFIED);
                }

                // 2. Force the window to the front layer
                toFront();

                // 3. Request focus safely
                requestFocus();

                // 4. Bypass focus on modern OS
                try {
                    boolean alwaysOnTopState = isAlwaysOnTop();
                    setAlwaysOnTop(true);
                    setAlwaysOnTop(alwaysOnTopState);
                } catch (SecurityException e) {
                    LOGGER.log(Level.CONFIG, "Bypass always-on-top focus failed due to OS security.", e);
                }
            }
        });
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }

            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        m_rootapp.tryToClose();
    }//GEN-LAST:event_formWindowClosing

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
        System.exit(0);
    }//GEN-LAST:event_formWindowClosed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
