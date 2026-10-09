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

package com.openbravo.beans;

import com.openbravo.pos.forms.AppLocal;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.LineBorder;

/**
 *
 * @author JG uniCenta
 */
public class JCalendarPanel extends javax.swing.JPanel {



    private Date m_date;    
    private JButtonDate[] m_ListDates;
    private JLabel[] m_jDays;
    
    private JButtonDate m_jCurrent;
    private JButtonDate m_jBtnMonthInc;
    private JButtonDate m_jBtnMonthDec;
    private JButtonDate m_jBtnYearInc;
    private JButtonDate m_jBtnYearDec;
    private JButtonDate m_jBtnToday;
    
    private DateFormat fmtMonthYear = new SimpleDateFormat("MMMMM yyyy");
    
    public JCalendarPanel() {
        this(new Date());
    }

    /**
     *
     * @param dDate
     */
    public JCalendarPanel(Date dDate) {
        super();
        
        initComponents();
        initComponents2();

        m_date = dDate;
        
        renderMonth();
        renderDay();
    }

    /**
     *
     * @param dNewDate
     */
    public void setDate(Date dNewDate) {        
                     
        // cambiamos la fecha
        Date dOldDate = m_date;  
        m_date = dNewDate;

        // pintamos
        renderMonth();
        renderDay();

        // decimos al mundo que ha cambiado la propiedad fecha
        firePropertyChange("Date", dOldDate, dNewDate);
    }

    /**
     *
     * @return
     */
    public Date getDate() {
        return m_date;
    }
    
    public void setEnabled(boolean bValue) {
           
        super.setEnabled(bValue);   
        
        // pintamos
        renderMonth();
        renderDay();
    }
    
    private void renderMonth() {
        
//        GregorianCalendar oCalRender = new GregorianCalendar();
//        oCalRender.setTime(m_CalendarHelper.getTime());

        GregorianCalendar m_CalendarHelper = new GregorianCalendar();
                
        for (int j = 0; j < 7; j++) {
            m_jDays[j].setEnabled(isEnabled());
        }    
        
        // Borramos todos los dias
        for(int i = 0; i < 42; i++) {
            JButtonDate jAux = m_ListDates[i];
            jAux.DateInf = null;
            jAux.setEnabled(false);
            jAux.setText(null);
            jAux.setForeground((Color)UIManager.getDefaults().get("TextPane.foreground"));
            jAux.setBackground((Color)UIManager.getDefaults().get("TextPane.background"));
            jAux.setBorder(null);
        }
        
        if (m_date == null) {
            m_jLblMonth.setEnabled(isEnabled());
            m_jLblMonth.setText(null);
        } else {
            m_CalendarHelper.setTime(m_date);
            
            m_jLblMonth.setEnabled(isEnabled());
            m_jLblMonth.setText(fmtMonthYear.format(m_CalendarHelper.getTime()));
            
            int iCurrentMonth = m_CalendarHelper.get(Calendar.MONTH);
            m_CalendarHelper.set(Calendar.DAY_OF_MONTH, 1);

            while(m_CalendarHelper.get(Calendar.MONTH) == iCurrentMonth) {

                JButtonDate jAux = getLabelByDate(m_CalendarHelper.getTime());
                jAux.DateInf = m_CalendarHelper.getTime();
                jAux.setEnabled(isEnabled());
                jAux.setText(String.valueOf(m_CalendarHelper.get(Calendar.DAY_OF_MONTH)));

                m_CalendarHelper.add(Calendar.DATE, 1);
            }
        }

        m_jCurrent = null;
    }

    private void renderDay() {
        
        GregorianCalendar m_CalendarHelper = new GregorianCalendar();
        
        m_jBtnToday.setEnabled(isEnabled());
        
        if (m_date == null) {
            m_jBtnMonthDec.setEnabled(false);
            m_jBtnMonthInc.setEnabled(isEnabled());
            m_jBtnYearDec.setEnabled(isEnabled());
            m_jBtnYearInc.setEnabled(isEnabled());
        } else {
            m_CalendarHelper.setTime(m_date);

            m_CalendarHelper.add(Calendar.MONTH, -1);
            m_jBtnMonthDec.DateInf = m_CalendarHelper.getTime();
            m_jBtnMonthDec.setEnabled(isEnabled());
            m_CalendarHelper.add(Calendar.MONTH, 2);
            m_jBtnMonthInc.DateInf = m_CalendarHelper.getTime();
            m_jBtnMonthInc.setEnabled(isEnabled());

            m_CalendarHelper.setTime(m_date);
            m_CalendarHelper.add(Calendar.YEAR, -1);
            m_jBtnYearDec.DateInf = m_CalendarHelper.getTime();
            m_jBtnYearDec.setEnabled(isEnabled());
            m_CalendarHelper.add(Calendar.YEAR, 2);
            m_jBtnYearInc.DateInf = m_CalendarHelper.getTime();
            m_jBtnYearInc.setEnabled(isEnabled());
        
            if(m_jCurrent != null) {
                m_jCurrent.setForeground((Color)UIManager.getDefaults().get("TextPane.foreground"));
                m_jCurrent.setBackground((Color)UIManager.getDefaults().get("TextPane.background"));
                m_jCurrent.setBorder(null);
            }

            JButtonDate jAux = getLabelByDate(m_date);
            jAux.setBackground((Color)UIManager.getDefaults().get("TextPane.selectionBackground"));
            jAux.setForeground((Color)UIManager.getDefaults().get("TextPane.selectionForeground"));
            jAux.setBorder(new LineBorder((Color)UIManager.getDefaults().get("TitledBorder.titleColor")));
            m_jCurrent = jAux;
        }
    }

    private JButtonDate getLabelByDate(Date d) {
        
        GregorianCalendar oCalRender = new GregorianCalendar();
        oCalRender.setTime(d);
        int iDayOfMonth = oCalRender.get(Calendar.DAY_OF_MONTH);
        
        oCalRender.set(Calendar.DAY_OF_MONTH, 1);
       
        int iCol = oCalRender.get(Calendar.DAY_OF_WEEK) - oCalRender.getFirstDayOfWeek();
        if (iCol < 0) {
            iCol += 7;
        }
        return m_ListDates[iCol + iDayOfMonth - 1];
    }

    private class DateClick implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            JButtonDate oLbl = (JButtonDate)e.getSource();
            if(oLbl.DateInf != null) {
                setDate(oLbl.DateInf);
            }
        }
    }

    private static class JButtonDate extends JButton {

        public Date DateInf;

        public JButtonDate(ActionListener datehandler) {
            super();
            initComponent();
            addActionListener(datehandler);
        }
        public JButtonDate(String sText, ActionListener datehandler) {
            super(sText);
            initComponent();
            addActionListener(datehandler);
        }    
        public JButtonDate(Icon icon, ActionListener datehandler) {
            super(icon);
            initComponent();
            addActionListener(datehandler);
        }   
        
        private void initComponent() {
            DateInf = null;
            setRequestFocusEnabled(false);
            setFocusPainted(false);
            setFocusable(false);
        }
    }

    private void initComponents2() {

        ActionListener dateclick = new DateClick();
        
        m_jBtnYearDec = new JButtonDate(new ImageIcon(getClass().getResource("/com/openbravo/images/2leftarrow.png")), dateclick);
        m_jBtnMonthDec = new JButtonDate(new ImageIcon(getClass().getResource("/com/openbravo/images/1leftarrow.png")), dateclick);
        m_jBtnToday = new JButtonDate(AppLocal.getIntString("button.Today"), dateclick);
        m_jBtnMonthInc = new JButtonDate(new ImageIcon(getClass().getResource("/com/openbravo/images/1rightarrow.png")), dateclick);
        m_jBtnYearInc = new JButtonDate(new ImageIcon(getClass().getResource("/com/openbravo/images/2rightarrow.png")), dateclick);
               
        m_jBtnToday.DateInf = new Date();
        m_jActions.add(m_jBtnYearDec);
        m_jActions.add(m_jBtnMonthDec);
        m_jActions.add(m_jBtnToday);
        m_jActions.add(m_jBtnMonthInc);
        m_jActions.add(m_jBtnYearInc);
        
        m_ListDates = new JButtonDate[42];
        for(int i = 0; i < 42; i++) {
            JButtonDate jAux = new JButtonDate(dateclick);
            // jAux.setFont(new Font("Dialog", 1, 24));
            jAux.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            jAux.setText(null);
            jAux.setOpaque(true);
            jAux.setForeground((Color)UIManager.getDefaults().get("TextPane.foreground"));
            jAux.setBackground((Color)UIManager.getDefaults().get("TextPane.background"));
            jAux.setBorder(null);
            m_ListDates[i] = jAux;
            m_jDates.add(jAux);
        }
        
        m_jDays = new JLabel[7];
        for(int iHead = 0; iHead < 7; iHead++) {
            JLabel JAuxHeader = new JLabel();
            //JAuxHeader.setFont(new Font("Dialog", 1, 24));
            JAuxHeader.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            m_jDays[iHead] = JAuxHeader;
            m_jWeekDays.add(JAuxHeader);
        }
        
        DateFormat fmtWeekDay = new SimpleDateFormat("E");
        Calendar oCalRender = new GregorianCalendar();
        int iCol;
        for (int j = 0; j < 7; j++) {
            oCalRender.add(Calendar.DATE, 1);
            iCol = oCalRender.get(Calendar.DAY_OF_WEEK) - oCalRender.getFirstDayOfWeek();
            if (iCol < 0) {
                iCol += 7;
            }
            m_jDays[iCol].setText(fmtWeekDay.format(oCalRender.getTime()));
        }      
    }

    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        m_jMonth = new javax.swing.JPanel();
        m_jWeekDays = new javax.swing.JPanel();
        m_jDates = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        m_jLblMonth = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        m_jActions = new javax.swing.JPanel();

        setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        setLayout(new java.awt.BorderLayout());

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel1.setLayout(new java.awt.BorderLayout());

        m_jMonth.setLayout(new java.awt.BorderLayout());

        m_jWeekDays.setLayout(new java.awt.GridLayout(1, 7));
        m_jMonth.add(m_jWeekDays, java.awt.BorderLayout.NORTH);

        m_jDates.setBackground(javax.swing.UIManager.getDefaults().getColor("TextPane.background"));
        m_jDates.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        m_jDates.setLayout(new java.awt.GridLayout(6, 7));
        m_jMonth.add(m_jDates, java.awt.BorderLayout.CENTER);

        jPanel1.add(m_jMonth, java.awt.BorderLayout.CENTER);

        m_jLblMonth.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        jPanel2.add(m_jLblMonth);

        jPanel1.add(jPanel2, java.awt.BorderLayout.NORTH);

        add(jPanel1, java.awt.BorderLayout.CENTER);

        jPanel3.setLayout(new java.awt.BorderLayout());

        m_jActions.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 5, 0, 5));
        m_jActions.setLayout(new java.awt.GridLayout(0, 1, 0, 5));
        jPanel3.add(m_jActions, java.awt.BorderLayout.NORTH);

        add(jPanel3, java.awt.BorderLayout.LINE_END);
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel m_jActions;
    private javax.swing.JPanel m_jDates;
    private javax.swing.JLabel m_jLblMonth;
    private javax.swing.JPanel m_jMonth;
    private javax.swing.JPanel m_jWeekDays;
    // End of variables declaration//GEN-END:variables
    
}
