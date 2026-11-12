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

import com.openbravo.basic.BasicException;

/**
 *
 * @author adrianromero
 * Created on February 6, 2007, 4:06 PM
 *
 */
public abstract class SentenceExecTransaction implements SentenceExec {
    
    private Session m_s;
    
    /**
     *
     * @param s
     */
    public SentenceExecTransaction(Session s) {
        m_s = s;
    }
    
    /**
     *
     * @return
     * @throws BasicException
     */
    public final int exec() throws BasicException {
        return exec((Object[]) null);
    }

    /**
     *
     * @param params
     * @return
     * @throws BasicException
     */
    public final int exec(Object[] params) throws BasicException {
        Transaction<Integer> t = new Transaction<Integer>(m_s) {
            public Integer transact() throws BasicException{
                return execInTransaction(params);
            }
        };
        
        return t.execute();
    }

    /**
     *
     * @param params
     * @return
     * @throws BasicException
     */
    public int exec(final Object params) throws BasicException {
        if (params instanceof Object[] array) {
            return exec(array);
        }
        Transaction<Integer> t = new Transaction<Integer>(m_s) {
            @Override
            public Integer transact() throws BasicException {
                return execInTransaction(params);
            }
        };
        return t.execute();
    }
    
    /**
     * Executes the sentence with a single non-array object parameter inside a transaction.
     * Subclasses can override this method to support strongly-typed entity parameter execution.
     *
     * @param params parameter object
     * @return number of affected rows
     * @throws BasicException on execution failure
     */
    protected int execInTransaction(Object params) throws BasicException {
        if (params instanceof Object[] array) {
            return execInTransaction(array);
        }
        return 0;
    }
    
    /**
     *
     * @param params
     * @return
     * @throws BasicException
     */
    protected abstract int execInTransaction(Object[] params) throws BasicException; 
}

