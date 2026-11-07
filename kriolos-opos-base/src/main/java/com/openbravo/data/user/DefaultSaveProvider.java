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

package com.openbravo.data.user;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.SentenceExec;
import com.openbravo.data.loader.TableDefinition;

/**
 * Standard SaveProvider implementation delegating to SentenceExec instances.
 *
 * @param <TYPE> the entity type handled by this save provider
 * @author JG uniCenta
 * @author KriolOS
 */
public class DefaultSaveProvider<TYPE> implements SaveProvider<TYPE> {

    protected SentenceExec m_sentupdate;
    protected SentenceExec m_sentinsert;
    protected SentenceExec m_sentdelete;

    public DefaultSaveProvider(SentenceExec sentupdate, SentenceExec sentinsert, SentenceExec sentdelete) {
        m_sentupdate = sentupdate;
        m_sentinsert = sentinsert;
        m_sentdelete = sentdelete;
    }

    public DefaultSaveProvider(TableDefinition<TYPE> table) {
        m_sentupdate = table.getUpdateSentence();
        m_sentdelete = table.getDeleteSentence();
        m_sentinsert = table.getInsertSentence();
    }

    public DefaultSaveProvider(TableDefinition<TYPE> table, int[] fields) {
        m_sentupdate = table.getUpdateSentence(fields);
        m_sentdelete = table.getDeleteSentence();
        m_sentinsert = table.getInsertSentence(fields);
    }

    @Override
    public boolean canDelete() {
        return m_sentdelete != null;      
    }

    @Override
    public int deleteData(TYPE value) throws BasicException {
        return m_sentdelete.exec(value);
    }

    @Override
    public boolean canInsert() {
        return m_sentinsert != null;          
    }

    @Override
    public int insertData(TYPE value) throws BasicException {
        return m_sentinsert.exec(value);
    }

    @Override
    public boolean canUpdate() {
        return m_sentupdate != null;      
    }

    @Override
    public int updateData(TYPE value) throws BasicException {
        return m_sentupdate.exec(value);
    }
}
