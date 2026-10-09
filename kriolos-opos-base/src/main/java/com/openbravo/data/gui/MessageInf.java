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
package com.openbravo.data.gui;

import com.openbravo.pos.forms.AppLocal;
import java.awt.*;
import javax.swing.*;

/**
 *
 * @author JG uniCenta
 */
public class MessageInf {

    /**
     * SIGNAL DANGER
     */
    public final static int SGN_DANGER = 0xFF000000; // Death or serious injury will occur

    /**
     * SIGNAL WARNING
     */
    public final static int SGN_WARNING = 0xFE000000; // Death or serious injury may occur

    /**
     * SIGNAL CAUTION
     */
    public final static int SGN_CAUTION = 0xFD000000; // Minor or moderate injury may occur

    /**
     * SIGNAL NOTICE
     */
    public final static int SGN_NOTICE = 0xFC000000; // Damage to property may occur

    /**
     * SIGNAL IMPORTANT
     */
    public final static int SGN_IMPORTANT = 0xFA000000; // Operating or maintenance instructions or additional information

    /**
     * SIGNAL SUCCESS
     */
    public final static int SGN_SUCCESS = 0xFB000000;

    /**
     * ERROR CLASS'ES GENERIC
     */
    public final static int CLS_GENERIC = 0x00000000;

    // ERROR_CODE'S
    // VARIABLES
    private int m_infCode; // = SIGNAL_WORD (0xFF000000) | ERROR_CLASS (0x00FF0000) | ERROR_CODE (0x0000FFFF)
    private String m_infMessage;
    private String m_sConsequences;
    private String m_sAvoiding;

    // CAUSE
    private Object m_eCause;

    /**
     * Creates a new instance of MessageInf
     *
     * @param infCode
     * @param infMessage
     * @param infObject
     */
    public MessageInf(int infCode, String infMessage, Object infObject) {
        m_infCode = infCode;
        m_infMessage = infMessage;
        m_sConsequences = "";
        m_sAvoiding = "";
        m_eCause = infObject;
    }

    /**
     * Creates a new instance of MessageInf
     *
     * @param infCode
     * @param infMessage
     */
    public MessageInf(int infCode, String infMessage) {
        this(infCode, infMessage, null);
    }

    /**
     * Creates a new instance of MessageInf
     *
     * @param e
     */
    public MessageInf(Throwable e) {
        this(SGN_WARNING, e.getLocalizedMessage(), e);
    }

    /**
     *
     * @param parent
     */
    public void show(Component parent) {
        JMessagePanel.showMessage(parent, this);
    }
    
    public static void showDialog(Component parent, int infCode, String infMessage, Object infObject){
        new MessageInf(infCode, infMessage, infObject).show(parent);
    }
    
    public static void showDialogWarn(Component parent, String infMessage, Object infObject){
        new MessageInf(MessageInf.SGN_WARNING, infMessage, infObject).show(parent);
    }

    /**
     *
     * @return
     */
    public Object getCause() {
        return m_eCause;
    }

    /**
     *
     * @return
     */
    public int getSignalWord() {
        return m_infCode & 0xFF000000;
    }
    
    public int getMessageCode() {
        return m_infCode & 0xFF000000;
    }

    /**
     *
     * @return
     */
    public Icon getSignalWordIcon() {
        int iSignalWord = getSignalWord();
        switch (iSignalWord) {
            case SGN_DANGER:
                return UIManager.getIcon("OptionPane.errorIcon");
            case SGN_WARNING:
                return UIManager.getIcon("OptionPane.warningIcon");
            case SGN_CAUTION:
                return UIManager.getIcon("OptionPane.warningIcon");
            case SGN_NOTICE:
                return UIManager.getIcon("OptionPane.informationIcon");
            case SGN_IMPORTANT:
                return UIManager.getIcon("OptionPane.informationIcon");
            case SGN_SUCCESS:
                return UIManager.getIcon("OptionPane.informationIcon");
            default:
                return UIManager.getIcon("OptionPane.questionIcon");
        }
    }

    /**
     *
     * @return
     */
    public String getErrorCodeMsg() {

        StringBuilder sb = new StringBuilder();
        int iSignalWord = getSignalWord();
        switch (iSignalWord) {
            case SGN_DANGER:
                sb.append("DNG_");
                break;
            case SGN_WARNING:
                sb.append("WRN_");
                break;
            case SGN_CAUTION:
                sb.append("CAU_");
                break;
            case SGN_NOTICE:
                sb.append("NOT_");
                break;
            case SGN_IMPORTANT:
                sb.append("IMP_");
                break;
            case SGN_SUCCESS:
                sb.append("INF_");
                break;
            default:
                sb.append("UNK_");
                break;
        }
        sb.append(toHex((m_infCode & 0x00FF0000) >> 16, 2));
        sb.append('_');
        sb.append(toHex(m_infCode & 0x0000FFFF, 4));
        return sb.toString();
    }

    private String toHex(int i, int iChars) {
        String s = Integer.toHexString(i);
        return s.length() >= iChars ? s : fillString(iChars - s.length()) + s;
    }

    private String fillString(int iChars) {
        char[] aStr = new char[iChars];
        for (int i = 0; i < aStr.length; i++) {
            aStr[i] = '0';
        }
        return new String(aStr);
    }

    /**
     *
     * @return
     */
    public String getMessageMsg() {

        StringBuilder sb = new StringBuilder();
        int iSignalWord = getSignalWord();
        switch (iSignalWord) {
            case SGN_DANGER:
                sb.append(AppLocal.getIntString("sgn.danger"));
                break;
            case SGN_WARNING:
                sb.append(AppLocal.getIntString("sgn.warning"));
                break;
            case SGN_CAUTION:
                sb.append(AppLocal.getIntString("sgn.caution"));
                break;
            case SGN_NOTICE:
                sb.append(AppLocal.getIntString("sgn.notice"));
                break;
            case SGN_IMPORTANT:
                sb.append(AppLocal.getIntString("sgn.important"));
                break;
            case SGN_SUCCESS:
                sb.append(AppLocal.getIntString("sgn.success"));
                break;
            default:
                sb.append(AppLocal.getIntString("sgn.unknown"));
                break;
        }
        sb.append(m_infMessage);
        sb.append(m_sConsequences);
        sb.append(m_sAvoiding);
        if(m_eCause instanceof Throwable throwable){
            sb.append(throwable.getLocalizedMessage());
        }else if (m_eCause instanceof String){
            sb.append(m_eCause.toString());
        }   
        return sb.toString();
    }
    
    public String getCode() {

        StringBuilder sb = new StringBuilder();
        int iSignalWord = getSignalWord();
        switch (iSignalWord) {
            case SGN_DANGER:
                sb.append(AppLocal.getIntString("sgn.danger"));
                break;
            case SGN_WARNING:
                sb.append(AppLocal.getIntString("sgn.warning"));
                break;
            case SGN_CAUTION:
                sb.append(AppLocal.getIntString("sgn.caution"));
                break;
            case SGN_NOTICE:
                sb.append(AppLocal.getIntString("sgn.notice"));
                break;
            case SGN_IMPORTANT:
                sb.append(AppLocal.getIntString("sgn.important"));
                break;
            case SGN_SUCCESS:
                sb.append(AppLocal.getIntString("sgn.success"));
                break;
            default:
                sb.append(AppLocal.getIntString("sgn.unknown"));
                break;
        }
        return sb.toString();
    }
    
    public String getMessage() {
        return m_infMessage;
    }
}
