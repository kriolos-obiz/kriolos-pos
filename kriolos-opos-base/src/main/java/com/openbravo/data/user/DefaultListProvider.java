/*
 * Copyright (C) 2026 KriolOS
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
package com.openbravo.data.user;

import com.openbravo.basic.BasicException;
import java.util.*;

/**
 *
 * @author poolborges
 * @param <T>
 */
public class DefaultListProvider<T> implements ListProvider<T> {
    
    private List<T> listData;
    
    public DefaultListProvider(List<T> listData) {
        this.listData = listData;
    }

    @Override
    public List<T> loadData() throws BasicException {       
        return refreshData();
    }

    @Override
    public List<T> refreshData() throws BasicException {
        return listData;
    }    
}
