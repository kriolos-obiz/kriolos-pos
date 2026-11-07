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

package com.openbravo.pos.admin;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.DataRead;

/**
 * Extended role entity for backwards compatibility.
 *
 * @author adrianromero Created on 27 de febrero de 2007, 23:46
 * @author KriolOS
 */
public class RoleInfoExt extends RoleInfo {

    public RoleInfoExt() {
        super();
    }

    @Override
    public void readValues(DataRead dr) throws BasicException {
        m_sName = dr.getString(1);
        m_aPermissions = dr.getBytes(2);
    }

    public static String[] getHeaders() {
        return new String[]{"Name"};
    }
    
    public String[] toStringArray() {
        return new String[]{m_sName};
    }
    
    public Comparable[] toComparableArray() {
        return new Comparable[]{m_sName};
    }
}
