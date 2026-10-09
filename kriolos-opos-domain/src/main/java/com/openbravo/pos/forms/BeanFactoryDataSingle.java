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

package com.openbravo.pos.forms;

import com.openbravo.data.loader.Session;
import com.openbravo.pos.forms.AppView;

/**
 *
 * @author adrianromero
 */
public abstract class BeanFactoryDataSingle implements BeanFactoryApp {
    
    /** Creates a new instance of BeanFactoryData */
    public BeanFactoryDataSingle() {
    }
    
    protected AppView app;

    /**
     *
     * @param s
     */
    public abstract void init(Session s);

    /**
     *
     * @param app
     * @throws BeanFactoryException
     */
    @Override
    public void init(AppView app) throws BeanFactoryException {        
        this.app = app;
        init(app.getSession());                     
    }

    public AppView getApp() {
        return app;
    }

    /**
     *
     * @return
     */
    @Override
    public Object getBean() {
        return this;
    }  
}
