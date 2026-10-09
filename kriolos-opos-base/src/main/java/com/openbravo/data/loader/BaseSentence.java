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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Base Statement implementation
 *
 * @author JG uniCenta, poolborges
 * @param <T>
 */
public abstract class BaseSentence<T> implements SentenceList<T>, SentenceFind<T>, SentenceExec {

    public abstract DataResultSet<T> openExec(Object params) throws BasicException;

    public abstract DataResultSet<T> moreResults() throws BasicException;

    public abstract void closeExec() throws BasicException;

    @Override
    public final int exec() throws BasicException {
        return exec((Object) null);
    }

    @Override
    public final int exec(Object params) throws BasicException {
        int count = -1;
        try (DataResultSet<T> SRS = openExec(params)) {
            requireNonNull(SRS);
            count = SRS.updateCount();
        } catch (BasicException ex) {
            throw new BasicException(ex);
        } finally {
            closeExec();
        }
        return count;
    }

    @Override
    public final List<T> list() throws BasicException {
        return list(null);
    }

    @Override
    public final List<T> list(Object params) throws BasicException {
        List<T> list = Collections.<T>emptyList();
        try (DataResultSet<T> dataResultSet = openExec(params)) {
            list = fetchAll(dataResultSet);
        } catch (BasicException ex) {
            throw new BasicException(ex);
        } finally {
            closeExec();
        }
        return list;
    }

    @Override
    public final List<T> listPage(int offset, int length) throws BasicException {
        return listPage(null, offset, length);
    }

    @Override
    public final List<T> listPage(Object params, int offset, int length) throws BasicException {
        List<T> list = Collections.<T>emptyList();
        try (DataResultSet<T> resultSet = openExec(params)) {
            list = fetchPage(resultSet, offset, length);
        } catch (BasicException ex) {
            throw new BasicException(ex);
        } finally {
            closeExec();
        }

        return list;
    }

    @Override
    public final T find() throws BasicException {
        return find((Object) null);
    }

    @Override
    public final T find(Object... params) throws BasicException {
        T obj = null;
        try (DataResultSet<T> resultSet = openExec(params)) {
            obj = fetchOne(resultSet);
        } catch (BasicException ex) {
            throw new BasicException(ex);
        } finally {
            closeExec();
        }

        return obj;
    }

    private List<T> fetchAll(DataResultSet<T> resultSet) throws BasicException {
        requireNonNull(resultSet);

        List<T> list = new ArrayList<>();
        while (resultSet.next()) {
            list.add(resultSet.getCurrent());
        }
        return list;
    }

    private List<T> fetchPage(DataResultSet<T> resultSet, int offset, int length) throws BasicException {
        requireNonNull(resultSet);
        requirePositive(offset);
        requirePositive(length);

        //skip
        while (offset > 0 && resultSet.next()) {
            offset--;
        }
        
        List<T> list = new ArrayList<>();

        while (offset == 0 && length > 0 && resultSet.next()) {
            list.add(resultSet.getCurrent());
            length--;
        }

        return list;
    }

    private T fetchOne(DataResultSet<T> resultSet) throws BasicException {
        requireNonNull(resultSet);
        return resultSet.next() ? resultSet.getCurrent(): null;
    }

    private void requireNonNull(DataResultSet<T> resultSet) throws BasicException {
        if (resultSet == null) {
            throw new BasicException(AppLocal.getIntString("exception.nodataset"),
                    new Exception("DataResultSet should not be null"));
        }
    }

    private void requirePositive(int value) throws BasicException {
        if (value < 0) {
            throw new BasicException(AppLocal.getIntString("exception.nonegativelimits"),
                    new Exception(String.format("Excpected positive value, but %s", value)));
        }
    }

}
