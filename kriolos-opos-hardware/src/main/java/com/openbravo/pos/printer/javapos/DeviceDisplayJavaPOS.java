/*
 * Copyright (C) 2022 KriolOS
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
package com.openbravo.pos.printer.javapos;

import com.openbravo.pos.printer.DeviceDisplay;
import com.openbravo.pos.printer.DeviceDisplayEngine;
import com.openbravo.pos.printer.TicketPrinterException;
import jpos.JposException;
import jpos.LineDisplay;
import jpos.LineDisplayConst;

/**
 *
 * @author JG uniCenta
 */
public class DeviceDisplayJavaPOS implements DeviceDisplay {
    
    private String m_sName;
    private LineDisplay m_ld;
    
    private DeviceDisplayEngine deviceDisplayEngine;
    
    /** Creates a new instance of DeviceDisplayJavaPOS
     * @param sDeviceName
     * @throws com.openbravo.pos.printer.TicketPrinterException */
    public DeviceDisplayJavaPOS(String sDeviceName) throws TicketPrinterException {
        m_sName = sDeviceName;
        
        m_ld = new LineDisplay();
        try {       
            m_ld.open(m_sName);
            m_ld.claim(10000);
            m_ld.setDeviceEnabled(true);
        } catch (JposException e) {
            throw new TicketPrinterException(e.getMessage(), e);
        }

        deviceDisplayEngine = new DeviceDisplayEngine(this);
   }
    
    /**
     *
     * @return
     */
    @Override
    public String getDisplayName() {
        return m_sName;
    }    

    /**
     *
     * @return
     */
    @Override
    public String getDisplayDescription() {
        return null;
    }      
    
    /**
     *
     * @param animation
     * @param sLine1
     * @param sLine2
     */
    @Override
    public void writeVisor(int animation, String sLine1, String sLine2) {
        deviceDisplayEngine.writeVisor(animation, sLine1, sLine2);
    }

    /**
     *
     * @param sLine1
     * @param sLine2
     */
    @Override
    public void writeVisor(String sLine1, String sLine2) {        
        deviceDisplayEngine.writeVisor(sLine1, sLine2);
    }
     
    /**
     *
     */
    @Override
    public void clearVisor() {
        deviceDisplayEngine.clearVisor();
    }
    
    /**
     *
     */
    @Override
    public void repaintLines() {
        try {
            m_ld.displayTextAt(0, 0, deviceDisplayEngine.getLine1(), LineDisplayConst.DISP_DT_NORMAL);
            m_ld.displayTextAt(1, 0, deviceDisplayEngine.getLine2(), LineDisplayConst.DISP_DT_NORMAL);
        } catch (JposException e) {
        }
    }
    
    @Override
    public void finalize() throws Throwable {
   
        m_ld.setDeviceEnabled(false);
        m_ld.release();
        m_ld.close();
        
        super.finalize();
    }
}
