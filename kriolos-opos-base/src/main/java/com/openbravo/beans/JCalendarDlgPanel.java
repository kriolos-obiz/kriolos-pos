//    KriolOS POS
//    Copyright (c) 2019-2026 KriolOS
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

import com.openbravo.data.gui.modal.PosUIModal;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Date;
import java.util.GregorianCalendar;
import javax.swing.JButton;
import javax.swing.JPanel;

/**
 * Calendar and time picker panel presentable via {@link PosUIModal} or embedded directly into views.
 *
 * @author KriolOS
 */
public class JCalendarDlgPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private static LocaleResources m_resources;

    private Date m_date;
    private JCalendarPanel myCalendar = null;
    private JTimePanel myTime = null;
    private PosUIModal modalContext;
    private boolean accepted = false;

    /**
     * Creates new form JCalendarDlgPanel
     */
    public JCalendarDlgPanel() {
        this(DateUtils.getTodayMinutes(), false);
    }

    /**
     * Creates new form JCalendarDlgPanel initialized with a date and optional time panel.
     *
     * @param date initial date
     * @param bTimePanel whether to include the time picker panel
     */
    public JCalendarDlgPanel(Date date, boolean bTimePanel) {
        initResources();
        initComponents();
        initContent(date, bTimePanel);
        initDomainAdapters();
    }

    private void initDomainAdapters() {
        setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8));

        setName("kriolos:calendar:dialog-panel");
        jcmdOK.setName("kriolos:calendar:btn-ok");
        jcmdCancel.setName("kriolos:calendar:btn-cancel");
        if (myCalendar != null) {
            myCalendar.setName("kriolos:calendar:date-picker");
        }
        if (myTime != null) {
            myTime.setName("kriolos:calendar:time-picker");
        }
    }

    private static synchronized void initResources() {
        if (m_resources == null) {
            m_resources = new LocaleResources();
            m_resources.addBundleName("beans_messages");
        }
    }

    private void initContent(Date date, boolean bTimePanel) {
        Date d = date != null ? date : DateUtils.getTodayMinutes();

        myCalendar = new JCalendarPanel(d);
        myCalendar.addPropertyChangeListener("Date", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (myTime != null) {
                    myTime.setDate(myCalendar.getDate());
                }
            }
        });
        jPanelGrid.add(myCalendar);

        if (bTimePanel) {
            myTime = new JTimePanel(d);
            myTime.addPropertyChangeListener("Date", new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if (myCalendar != null) {
                        myCalendar.setDate(myTime.getDate());
                    }
                }
            });
            jPanelGrid.add(myTime);
        }
    }

    /**
     * Attaches the controlling {@link PosUIModal} context.
     *
     * @param modalContext the modal context
     */
    public void setModalContext(PosUIModal modalContext) {
        this.modalContext = modalContext;
    }

    /**
     * Returns the title for this panel when shown in a dialog or frame.
     *
     * @return the localized calendar title
     */
    public String getTitle() {
        initResources();
        return m_resources.getString("title.calendar");
    }

    /**
     * Returns the default OK button.
     *
     * @return the OK button
     */
    public JButton getOkButton() {
        return jcmdOK;
    }

    /**
     * Returns the selected date if accepted, or null if canceled.
     *
     * @return the selected date or null
     */
    public Date getSelectedDate() {
        return accepted ? m_date : null;
    }

    /**
     * Returns whether the selection was confirmed by the user.
     *
     * @return true if accepted, false if canceled
     */
    public boolean isAccepted() {
        return accepted;
    }

    private void handleAccept() {
        GregorianCalendar dateresult;
        GregorianCalendar date1 = new GregorianCalendar();
        date1.setTime(myCalendar.getDate());

        if (myTime == null) {
            dateresult = new GregorianCalendar(
                    date1.get(GregorianCalendar.YEAR),
                    date1.get(GregorianCalendar.MONTH),
                    date1.get(GregorianCalendar.DAY_OF_MONTH));
        } else {
            GregorianCalendar date2 = new GregorianCalendar();
            date2.setTime(myTime.getDate());
            dateresult = new GregorianCalendar(
                    date1.get(GregorianCalendar.YEAR),
                    date1.get(GregorianCalendar.MONTH),
                    date1.get(GregorianCalendar.DAY_OF_MONTH),
                    date2.get(GregorianCalendar.HOUR_OF_DAY),
                    date2.get(GregorianCalendar.MINUTE));
        }

        this.m_date = dateresult.getTime();
        this.accepted = true;

        if (modalContext != null) {
            modalContext.setResult(this.m_date);
            modalContext.close();
        }
    }

    private void handleCancel() {
        this.m_date = null;
        this.accepted = false;

        if (modalContext != null) {
            modalContext.setResult(null);
            modalContext.close();
        }
    }

    /**
     * Displays date and time picker with hours granularity.
     *
     * @param parent the parent component
     * @param date the initial date
     * @return selected date or null if canceled
     */
    public static Date showCalendarTimeHours(Component parent, Date date) {
        return show(parent, date == null ? DateUtils.getToday() : date, true);
    }

    /**
     * Displays date and time picker with minute granularity.
     *
     * @param parent the parent component
     * @param date the initial date
     * @return selected date or null if canceled
     */
    public static Date showCalendarTime(Component parent, Date date) {
        return show(parent, date == null ? DateUtils.getTodayMinutes() : date, true);
    }

    /**
     * Displays date picker without time components.
     *
     * @param parent the parent component
     * @param date the initial date
     * @return selected date or null if canceled
     */
    public static Date showCalendar(Component parent, Date date) {
        return show(parent, date == null ? DateUtils.getTodayMinutes() : date, false);
    }

    /**
     * Core orchestrator method displaying the calendar panel via {@link PosUIModal}.
     *
     * @param parent the parent component
     * @param date the initial date
     * @param bTimePanel whether to include the time picker
     * @return selected date or null if canceled
     */
    @Override
    public void addNotify() {
        super.addNotify();
        javax.swing.JRootPane root = javax.swing.SwingUtilities.getRootPane(this);
        if (root != null) {
            root.setDefaultButton(jcmdOK);
        }
    }

    public static Date show(Component parent, Date date, boolean bTimePanel) {
        JCalendarDlgPanel panel = new JCalendarDlgPanel(date, bTimePanel);
        PosUIModal modal = PosUIModal.create(parent, panel)
                .setTitle(panel.getTitle())
                .setModal(true)
                .setResizable(false);

        panel.setModalContext(modal);
        modal.show();

        return panel.getSelectedDate();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jcmdOK = new javax.swing.JButton();
        jcmdCancel = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jPanelGrid = new javax.swing.JPanel();

        setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        setLayout(new java.awt.BorderLayout());

        jPanel1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        jcmdOK.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jcmdOK.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/ok.png"))); // NOI18N
        jcmdOK.setText(m_resources.getString("button.ok")); // NOI18N
        jcmdOK.setMargin(new java.awt.Insets(8, 16, 8, 16));
        jcmdOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jcmdOKActionPerformed(evt);
            }
        });
        jPanel1.add(jcmdOK);

        jcmdCancel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jcmdCancel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/cancel.png"))); // NOI18N
        jcmdCancel.setText(m_resources.getString("button.cancel")); // NOI18N
        jcmdCancel.setMargin(new java.awt.Insets(8, 16, 8, 16));
        jcmdCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jcmdCancelActionPerformed(evt);
            }
        });
        jPanel1.add(jcmdCancel);

        add(jPanel1, java.awt.BorderLayout.SOUTH);

        jPanel2.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jPanel2.setLayout(new java.awt.BorderLayout());

        jPanelGrid.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jPanelGrid.setLayout(new java.awt.GridLayout(1, 0, 5, 0));
        jPanel2.add(jPanelGrid, java.awt.BorderLayout.CENTER);

        add(jPanel2, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void jcmdOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jcmdOKActionPerformed
        handleAccept();
    }//GEN-LAST:event_jcmdOKActionPerformed

    private void jcmdCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jcmdCancelActionPerformed
        handleCancel();
    }//GEN-LAST:event_jcmdCancelActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanelGrid;
    private javax.swing.JButton jcmdCancel;
    private javax.swing.JButton jcmdOK;
    // End of variables declaration//GEN-END:variables
}
