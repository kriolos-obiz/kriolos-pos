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
import com.openbravo.pos.printer.TicketParser;
import java.util.Date;

/**
 *
 * @author adrianromero
 */
public interface AppView {
    
    public DeviceTicket getDeviceTicket();

    /**
     * Checks if a hardware scale is configured and operational.
     *
     * @return true if scale is available
     */
    public boolean hasScale();

    /**
     * Reads weight from the configured hardware scale.
     *
     * @return the measured weight or null if unavailable
     */
    public Double readWeight();

    /**
     * Checks if a handheld scanner is configured.
     *
     * @return true if scanner device is configured
     */
    public boolean hasScanner();

    public Session getSession();
    public AppProperties getProperties();

    /**
     *
     * @param beanfactory
     * @return
     * @throws BeanFactoryException
     */
    public Object getBean(String beanfactory) throws BeanFactoryException;
    public <T> T getBean(Class<T> beanClass) throws BeanFactoryException;
     
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

    /**
     * Creates a {@link TicketParser} bound to this application's ticket device
     * and data-logic system. Callers need no extra arguments — all dependencies
     * are already owned by the {@code AppView}.
     *
     * @return a ready-to-use {@link TicketParser}
     */
    default TicketParser createTicketParser() {
        try {
            DataLogicSystem dlSystem = getBean(DataLogicSystem.class);
            return new TicketParser(getDeviceTicket(), dlSystem);
        } catch (BeanFactoryException e) {
            throw new IllegalStateException("DataLogicSystem not available in AppView", e);
        }
    }

    /**
     * Creates a {@link TicketParser} bound to a custom {@link DeviceTicket}
     * (e.g. a preview/reprint ticket device) but still using this application's
     * data-logic system.
     *
     * @param deviceTicket the ticket device to bind (e.g. a preview device)
     * @return a ready-to-use {@link TicketParser}
     */
    default TicketParser createTicketParser(DeviceTicket deviceTicket) {
        try {
            DataLogicSystem dlSystem = getBean(DataLogicSystem.class);
            return new TicketParser(deviceTicket, dlSystem);
        } catch (BeanFactoryException e) {
            throw new IllegalStateException("DataLogicSystem not available in AppView", e);
        }
    }

}

