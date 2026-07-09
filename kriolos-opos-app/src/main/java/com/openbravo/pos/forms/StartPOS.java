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

        // Explicitly set the WM_CLASS for Linux window managers using a sanitized application name
        String wmClass = AppLocal.APP_NAME.toLowerCase().replaceAll("\\s+", "-");
        System.setProperty("sun.awt.wmclass", wmClass);

        AppConfig config = AppConfig.getInstance();
        config.load();

        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {

                final JRootFrame rootFrame = new JRootFrame(config);
                /*
                TODO: THIS IS NOT WORKING
                 checkSingletonInstance(rootFrame, config);
                 */
                rootFrame.initFrame();
            }
        });
    }

    private static void checkSingletonInstance(JRootFrame rootFrame,AppConfig config) {
        if ("true".equals(config.getProperty("machine.uniqueinstance"))) {

            try {
                InstanceManager.queryInstance().restoreWindow();
            } catch (RemoteException | NotBoundException e) {
                String msg = "Cannot start the application. Another instance is already running";
                LOGGER.log(Level.WARNING, msg, e);
                //Open A Window a Present a message to User
                //Wait maximun 30 second and close
                JOptionPane.showMessageDialog(rootFrame,
                        msg,
                        AppLocal.APP_NAME, JOptionPane.WARNING_MESSAGE);
                System.exit(-1000);
            }

            // Register the running application
            try {
                final InstanceManager instanceManager = new InstanceManager(rootFrame);
                instanceManager.registerInstance();

            } catch (RemoteException | AlreadyBoundException e) {
                String msg = "Cannot start the application. Cannot register a single instance";
                LOGGER.log(Level.WARNING, msg, e);
                //Open A Window a Present a message to User
                //Wait maximun 30 second and close
                JOptionPane.showMessageDialog(rootFrame,
                        msg,
                        AppLocal.APP_NAME, JOptionPane.WARNING_MESSAGE);
                System.exit(-1001);
            }
        }
    }
}
