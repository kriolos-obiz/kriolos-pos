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
package com.openbravo.pos.printer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 *
 * @author adrianromero
 */
public class DeviceDisplayEngine {

    public static final int ANIMATION_NULL = 0;
    public static final int ANIMATION_FLYER = 1;
    public static final int ANIMATION_SCROLL = 2;
    public static final int ANIMATION_BLINK = 3;
    public static final int ANIMATION_CURTAIN = 4;
    
    private DeviceDisplay deviceDisplayImpl;    
    private DisplayAnimator displayAnimator;     
    private javax.swing.Timer animatorTimer;    
    private int counter = 0;
    
    public DeviceDisplayEngine(DeviceDisplay impl) {
        this.deviceDisplayImpl = impl; 
        displayAnimator = new NullAnimator("", "");
        animatorTimer = new javax.swing.Timer(50, new PrintTimeAction());
    }
    
    /**
     *
     * @param animation
     * @param sLine1
     * @param sLine2
     */
    public void writeVisor(int animation, String sLine1, String sLine2) {
        
        animatorTimer.stop();
        switch (animation) {
            case ANIMATION_FLYER:
                displayAnimator = new FlyerAnimator(sLine1, sLine2);
                break;
            case ANIMATION_SCROLL:
                displayAnimator = new ScrollAnimator(sLine1, sLine2);
                break;
            case ANIMATION_BLINK:
                displayAnimator = new BlinkAnimator(sLine1, sLine2);
                break;
            case ANIMATION_CURTAIN:
                displayAnimator = new CurtainAnimator(sLine1, sLine2);
                break;
            default: // ANIMATION_NULL
                displayAnimator = new NullAnimator(sLine1, sLine2);
                break;
        }
        
        counter = 0;
        displayAnimator.setTiming(counter);
        deviceDisplayImpl.repaintLines();
        
        if (animation != ANIMATION_NULL) {
            counter = 0;
            animatorTimer.start();
        }
    }
         
    /**
     *
     * @param sLine1
     * @param sLine2
     */
    public void writeVisor(String sLine1, String sLine2) {
        writeVisor(ANIMATION_NULL, sLine1, sLine2);
    }
    
    /**
     *
     */
    public void clearVisor() {
        writeVisor(ANIMATION_NULL, "", "");
    }
    
    /**
     *
     * @return
     */
    public String getLine1() {
        return displayAnimator.getLine1();
    }
    
    /**
     *
     * @return
     */
    public String getLine2() {
        return displayAnimator.getLine2();
    }
    
    private class PrintTimeAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent evt) {
            counter ++;
            displayAnimator.setTiming(counter);
            deviceDisplayImpl.repaintLines();
        }        
    }    
}
