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
package com.openbravo.pos.printer.escpos;

import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.printer.DeviceDisplay;
import com.openbravo.pos.printer.DeviceDisplayEngine;
/**
 *
 * @author adrianromero
 */
public abstract class DeviceDisplaySerial implements DeviceDisplay {
    
    private String m_sName;    

    protected PrinterWritter display;

    protected DeviceDisplayEngine baseDeviceDisplay;

    public DeviceDisplaySerial() {
        baseDeviceDisplay = new DeviceDisplayEngine(this);
    }
    
    /**
     *
     * @param display
     */
    protected void init(PrinterWritter display) {                
        m_sName = AppLocal.getIntString("printer.serial");
        this.display = display;      
        initVisor();        
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
        baseDeviceDisplay.writeVisor(animation, sLine1, sLine2);
    }

    /**
     *
     * @param sLine1
     * @param sLine2
     */
    @Override
    public void writeVisor(String sLine1, String sLine2) {        
        baseDeviceDisplay.writeVisor(sLine1, sLine2);
    }
     
    /**
     *
     */
    @Override
    public void clearVisor() {
        baseDeviceDisplay.clearVisor();
    }
    
    /**
     *
     */
    public abstract void initVisor();
}
