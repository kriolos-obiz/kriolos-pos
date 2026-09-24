/*
 * Copyright (C) 2023 Paulo Borges
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

module kriolos.opos.base {
    requires java.base;
    requires transitive java.desktop;
    requires java.sql;
    requires java.sql.rowset;
    requires java.logging;
    
    exports com.openbravo.basic;
    exports com.openbravo.beans; 
    exports com.openbravo.data.gui; 
    exports com.openbravo.data.gui.modal; 
    exports com.openbravo.data.loader; 
    exports com.openbravo.data.model; 
    exports com.openbravo.data.user; 
    exports com.openbravo.editor;  
    exports com.openbravo.format; 
    exports com.openbravo.pos.forms; 
    exports com.openbravo.pos.util;
}
