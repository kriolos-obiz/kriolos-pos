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
package com.openbravo.data.loader;

import com.openbravo.pos.forms.AppLocal;
import com.openbravo.basic.BasicException;
import java.util.Date;

/**
 *
 * @author adrian
 */
public class UpdateDataResultSet<T> implements DataResultSet<T> {

    private final int m_iUpdateCount;

    public UpdateDataResultSet(int iUpdateCount) {
        m_iUpdateCount = iUpdateCount;
    }

    @Override
    public Integer getInt(int columnIndex) throws BasicException {
        throw new BasicException(AppLocal.getIntString("exception.nodataset"));
    }

    @Override
    public String getString(int columnIndex) throws BasicException {
        throw new BasicException(AppLocal.getIntString("exception.nodataset"));
    }

    @Override
    public Double getDouble(int columnIndex) throws BasicException {
        throw new BasicException(AppLocal.getIntString("exception.nodataset"));
    }

    @Override
    public Boolean getBoolean(int columnIndex) throws BasicException {
        throw new BasicException(AppLocal.getIntString("exception.nodataset"));
    }

    @Override
    public Date getTimestamp(int columnIndex) throws BasicException {
        throw new BasicException(AppLocal.getIntString("exception.nodataset"));
    }

    @Override
    public Date getDate(int columnIndex) throws BasicException {
        throw new BasicException(AppLocal.getIntString("exception.nodataset"));
    }

    @Override
    public byte[] getBytes(int columnIndex) throws BasicException {
        throw new BasicException(AppLocal.getIntString("exception.nodataset"));
    }

    @Override
    public T getObject(int columnIndex) throws BasicException {
        throw new BasicException(AppLocal.getIntString("exception.nodataset"));
    }

    @Override
    public DataField[] getDataField() throws BasicException {
        throw new BasicException(AppLocal.getIntString("exception.nodataset"));
    }

    @Override
    public T getCurrent() throws BasicException {
        throw new BasicException(AppLocal.getIntString("exception.nodataset"));
    }

    @Override
    public boolean next() throws BasicException {
        throw new BasicException(AppLocal.getIntString("exception.nodataset"));
    }

    @Override
    public void close() throws BasicException {}

    @Override
    public int updateCount() throws BasicException {
        return m_iUpdateCount;
    }
}
