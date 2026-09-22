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
 
import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.Session;
import com.openbravo.pos.printer.DeviceTicket;
import com.openbravo.pos.scale.DeviceScale;
import com.openbravo.pos.scanpal2.DeviceScanner;
import java.util.Date;

/**
 *
 * @author adrianromero
 */
public interface AppView {
    
    public DeviceScale getDeviceScale();
    public DeviceTicket getDeviceTicket();
    public DeviceScanner getDeviceScanner();
    public Session getSession();
    public AppProperties getProperties();

    /**
     *
     * @param beanfactory
     * @return
     * @throws BeanFactoryException
     */
    public Object getBean(String beanfactory) throws BeanFactoryException;
     
    /*ActiveCash*/
    public void setActiveCash(String value, int iSeq, Date dStart, Date dEnd);
    public String getActiveCashIndex();
    public int getActiveCashSequence();
    public Date getActiveCashDateStart();
    public Date getActiveCashDateEnd();


    public String getInventoryLocation();
    
    public void waitCursorBegin();
    public void waitCursorEnd();
    public AppUserView getAppUserView();
    
    public boolean hasPermission(String permission);
    
    public boolean closeAppView();
    
    public void switchDatabase() throws BasicException;

}

