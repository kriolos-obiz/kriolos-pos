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

/**
 *
 * @author adrian
 */
public class BasicSentenceEnum implements SentenceEnum {
    
    private final BaseSentence sent;
    private DataResultSet SRS;
    
    /** Creates a new instance of AbstractSentenceEnum
     * @param sent */
    public BasicSentenceEnum(BaseSentence sent) {
        this.sent = sent;
        this.SRS = null;
    }
    
    /**
     *
     * @throws BasicException
     */
    @Override
    public void load() throws BasicException {
        load(null);
    }

    /**
     *
     * @param params
     * @throws BasicException
     */
    @Override
    public void load(Object[] params) throws BasicException {
        SRS = sent.openExec(params);
    }

    /**
     *
     * @return
     * @throws BasicException
     */
    @Override
    public Object getCurrent() throws BasicException {
        if (SRS == null) {
            throw new BasicException(AppLocal.getIntString("exception.nodataset"));
        } 
        
        return SRS.getCurrent();  
    }
    
    /**
     *
     * @return
     * @throws BasicException
     */
    @Override
    public boolean next() throws BasicException {
        if (SRS == null) {
            throw new BasicException(AppLocal.getIntString("exception.nodataset"));
        } 
        
        if (SRS.next()) {
            return true;  
        } else {
            SRS.close();
            SRS = null;
            sent.closeExec();
            return false;
        }
    }
}
