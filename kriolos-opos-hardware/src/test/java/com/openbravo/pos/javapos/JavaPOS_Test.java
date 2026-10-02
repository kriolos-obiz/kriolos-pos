/*
 * Copyright (C) 2025 Paulo Borges
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.openbravo.pos.javapos;

import java.util.Enumeration;
import jpos.JposException;
import jpos.config.JposEntry;
import jpos.config.JposEntryRegistry;
import jpos.loader.JposServiceConnection;
import jpos.loader.JposServiceLoader;

/**
 *
 * @author poolborges
 */
public class JavaPOS_Test {

    public static void main(String[] args) {

        //JposServiceLoader.findService(logicalName);
        JposServiceLoader.getManager().getProperties().loadJposProperties();
        JposEntryRegistry registry = JposServiceLoader.getManager().getEntryRegistry();

        Enumeration entriesEnum = registry.getEntries();

        while (entriesEnum.hasMoreElements()) {
            JposEntry entry = (JposEntry) entriesEnum.nextElement();

            System.out.println("Connection for existing entry with logical name = " + entry.getLogicalName());

            JposServiceConnection connection;
            try {
                connection = JposServiceLoader.findService(entry.getLogicalName());

                System.out.println("JposServiceConnection.getLogicalName() == " + connection.getLogicalName()
                        + " JposEntry.getLogicalName() == " + entry.getLogicalName());
            } catch (JposException ex) {
                System.getLogger(JavaPOS_Test.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            }

        }
    }
}
