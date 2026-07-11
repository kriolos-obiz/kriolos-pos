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
//    along with this program.  If not, see <http://www.gnu.org/licenses/>
package com.openbravo.pos.forms;

import com.openbravo.pos.instance.InstanceManager;

import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class StartPOS {

    private static final Logger LOGGER = Logger.getLogger(StartPOS.class.getName());

    public static void main(final String args[]) {

        AppConfig config = AppConfig.getInstance();
        config.load();
        AppConfig.applySystemProperties(config);

        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {

                final JRootFrame rootFrame = new JRootFrame(config);

                //CHECK SINGLE INSTANCE RMI
                 checkSingletonInstance(rootFrame, config);

                rootFrame.initFrame();
            }
        });
    }

    private static void checkSingletonInstance(JRootFrame rootFrame, AppConfig config) {
        if ("true".equals(config.getProperty("machine.uniqueinstance"))) {

            try {
                // Try to find and contact an existing instance
                InstanceManager.queryInstance(config).restoreWindow();

                // If no exception occurs, another instance is already running
                String msg = "Another instance of the application is already running.";
                LOGGER.log(Level.INFO, msg);
                JOptionPane.showMessageDialog(rootFrame,
                        msg,
                        AppLocal.APP_NAME, JOptionPane.INFORMATION_MESSAGE);

                // Exit this second instance cleanly
                System.exit(0);

            } catch (RemoteException | NotBoundException e) {
                // Exception caught means no prior instance exists. Safe to proceed.
                LOGGER.log(Level.INFO, "No previous instance found. Registering this instance...");
            }

            // Register this first running instance into the RMI registry
            try {
                final InstanceManager instanceManager = new InstanceManager(rootFrame, config);
                instanceManager.registerInstance();
                LOGGER.log(Level.INFO, "Application instance registered successfully via RMI.");

            } catch (RemoteException | AlreadyBoundException e) {
                String msg = "Cannot start the application. Cannot register a single instance";
                LOGGER.log(Level.WARNING, msg, e);
                JOptionPane.showMessageDialog(rootFrame,
                        msg,
                        AppLocal.APP_NAME, JOptionPane.WARNING_MESSAGE);
                System.exit(-1001);
            }
        }
    }

}
