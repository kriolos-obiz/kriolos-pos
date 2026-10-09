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


package com.openbravo.pos.epm;

import com.openbravo.pos.forms.JPanelView;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.BeanFactoryException;
import com.openbravo.pos.forms.BeanFactoryApp;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.basic.BasicException;
import com.openbravo.beans.JFlowPanel;
import com.openbravo.data.gui.MessageInf;
import com.openbravo.format.Formats;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.Date;
import javax.swing.*;

/**
 *
 * @author Ali Safdar and Aneeqa Baber
 */
public class JPanelEmployeePresence extends javax.swing.JPanel implements JPanelView, BeanFactoryApp {

    private AppView app;
    private ShiftService shiftService;
    private JFlowPanel jBreaks;
    
    /** Creates new form JPanelEmployeePresence */
    public JPanelEmployeePresence() {
        initComponents();
        this.setVisible(true);
    }

    /**
     *
     * @param app
     * @throws BeanFactoryException
     */
    @Override
    public void init(AppView app) throws BeanFactoryException {
        this.app = app;
        shiftService = app.getBean(ShiftService.class);
    }

    @Deprecated
    public DataLogicPresenceManagement getDataLogicPresenceManagement() {
        return shiftService instanceof DataLogicPresenceManagement dl ? dl : null;
    }

    private void listBreaks()
    {
        try {
            jScrollPane1.getViewport().setView(null);
            jBreaks = new JFlowPanel();
            jBreaks.applyComponentOrientation(getComponentOrientation());
            java.util.List<Break> breaks = shiftService.listBreaksVisible();
            for (int i = 0; i < breaks.size(); i++) {

                Break m_break = (Break) breaks.get(i);

                JButton btn = new JButton(new BreakAction(m_break));
                btn.applyComponentOrientation(getComponentOrientation());
                btn.setFocusPainted(false);
                btn.setFocusable(false);
                btn.setRequestFocusEnabled(false);
                btn.setHorizontalAlignment(SwingConstants.LEADING);
                btn.setMaximumSize(new Dimension(190, 50));
                btn.setPreferredSize(new Dimension(190, 50));
                btn.setMinimumSize(new Dimension(190, 50));

                jBreaks.add(btn);
            }
            jScrollPane1.getViewport().setView(jBreaks);

        } catch (BasicException ee) {
        }
    }

    private class BreakAction extends AbstractAction {

        private Break m_break;

        public BreakAction(Break aBreak) {
            m_break = aBreak;
            putValue(Action.NAME, m_break.getName());
        }

        public Break getBreak() {
            return m_break;
        }

        @Override
        public void actionPerformed(ActionEvent evt) {
            try {
                shiftService.startBreak(app.getAppUserView().getUser().getId(), m_break.getId());
                message.setText(app.getAppUserView().getUser().getName()+" "+AppLocal.getIntString("message.leavefor")+" "+ m_break.getName()+" "+AppLocal.getIntString("message.at")+" "+Formats.TIMESTAMP.formatValue(new Date()));
                BreakAction();
            } catch (BasicException ex) {
                message.setText(AppLocal.getIntString("message.probleminbreak"));
            }
        }
    }

    /**
     *
     * @return
     */
    @Override
    public String getTitle() {
        return AppLocal.getIntString("Menu.CheckInCheckOut");
    }

    /**
     *
     * @throws BasicException
     */
    @Override
    public void activate() throws BasicException {
        boolean isOnLeave = shiftService.isOnLeave(app.getAppUserView().getUser().getId());
        listBreaks();
        if (isOnLeave) {
            message.setText(app.getAppUserView().getUser().getName()+" "+AppLocal.getIntString("message.leavecontrol"));
            LeaveAction();
        } else {
            boolean isCheckedIn = shiftService.isCheckedIn(app.getAppUserView().getUser().getId());
            if (isCheckedIn) {
                Date lastCheckIn = shiftService.getLastCheckIn(app.getAppUserView().getUser().getId());
                message.setText(app.getAppUserView().getUser().getName()+" "+AppLocal.getIntString("message.checkedin")+" "+Formats.TIMESTAMP.formatValue(lastCheckIn));
                CheckInAction();
            } else {
                Date lastCheckOut = shiftService.getLastCheckOut(app.getAppUserView().getUser().getId());
                if (lastCheckOut != null) {
                    message.setText(app.getAppUserView().getUser().getName()+" "+AppLocal.getIntString("message.checkedout")+" "+Formats.TIMESTAMP.formatValue(lastCheckOut));
                } else {
                    message.setText(app.getAppUserView().getUser().getName()+" "+AppLocal.getIntString("message.noshift"));
                }
                CheckOutAction();
            }
            boolean isOnBreak = shiftService.isOnBreak(app.getAppUserView().getUser().getId());
            if (isOnBreak) {
                ShiftBreakActivity lastBreak = shiftService.getLastBreakActivity(app.getAppUserView().getUser().getId());
                message.setText(app.getAppUserView().getUser().getName()+" "+AppLocal.getIntString("message.leavefor")+" "+ lastBreak.breakName() +" "+AppLocal.getIntString("message.at")+" "+ Formats.TIMESTAMP.formatValue(lastBreak.startTime()));
                BreakAction();
            }
        }
    }

    /**
     *
     * @return
     */
    @Override
    public boolean deactivate() {
        return true;
    }

    /**
     *
     * @return
     */
    @Override
    public JComponent getComponent() {
        return this;
    }

    /**
     *
     * @return
     */
    @Override
    public Object getBean() {
        return this;
    }

    private void CheckInAction() {
        btnCheckIn.setEnabled(false);
        btnCheckOut.setEnabled(true);
        jBreaks.setEnabled(true);
    }

    private void CheckOutAction() {
        btnCheckIn.setEnabled(true);
        btnCheckOut.setEnabled(false);
        jBreaks.setEnabled(false);
    }
    
    private void BreakAction() {
        btnCheckIn.setEnabled(true);
        btnCheckOut.setEnabled(true);
        jBreaks.setEnabled(false);
    }

    private void LeaveAction() {
        btnCheckIn.setEnabled(false);
        btnCheckOut.setEnabled(false);
        jBreaks.setEnabled(false);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btnCheckIn = new javax.swing.JButton();
        btnCheckOut = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        message = new javax.swing.JLabel();

        setPreferredSize(new java.awt.Dimension(0, 45));

        btnCheckIn.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        btnCheckIn.setText("Check In");
        btnCheckIn.setMaximumSize(new java.awt.Dimension(85, 23));
        btnCheckIn.setPreferredSize(new java.awt.Dimension(0, 45));
        btnCheckIn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCheckInActionPerformed(evt);
            }
        });

        btnCheckOut.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        btnCheckOut.setText("Check Out");
        btnCheckOut.setPreferredSize(new java.awt.Dimension(0, 45));
        btnCheckOut.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCheckOutActionPerformed(evt);
            }
        });

        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        jScrollPane1.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jScrollPane1.setPreferredSize(new java.awt.Dimension(570, 120));

        message.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        message.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        message.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(javax.swing.UIManager.getDefaults().getColor("Button.darkShadow")), javax.swing.BorderFactory.createEmptyBorder(1, 4, 1, 4)));
        message.setOpaque(true);
        message.setPreferredSize(new java.awt.Dimension(160, 25));
        message.setRequestFocusEnabled(false);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnCheckIn, javax.swing.GroupLayout.PREFERRED_SIZE, 256, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnCheckOut, javax.swing.GroupLayout.PREFERRED_SIZE, 248, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(message, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(message, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCheckIn, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnCheckOut, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(80, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnCheckInActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCheckInActionPerformed
        try {
            boolean isOnBreak = shiftService.isOnBreak(app.getAppUserView().getUser().getId());
            if (isOnBreak) {
                shiftService.endBreak(app.getAppUserView().getUser().getId());
                message.setText(app.getAppUserView().getUser().getName()+AppLocal.getIntString("message.breakoverandcheckedin")+" "+Formats.TIMESTAMP.formatValue(new Date()));
            } else {
                shiftService.checkIn(app.getAppUserView().getUser().getId());
                message.setText(app.getAppUserView().getUser().getName()+" "+AppLocal.getIntString("message.checkedin")+" "+Formats.TIMESTAMP.formatValue(new Date()));
            }
        } catch (BasicException ex) {
            MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotcheckin"));
            msg.show(this);
        }
        CheckInAction();
}//GEN-LAST:event_btnCheckInActionPerformed

    private void btnCheckOutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCheckOutActionPerformed
        try {
            boolean isOnBreak = shiftService.isOnBreak(app.getAppUserView().getUser().getId());
            if (isOnBreak) {
                shiftService.endBreak(app.getAppUserView().getUser().getId());
                message.setText(app.getAppUserView().getUser().getName()+" "+AppLocal.getIntString("message.breakoverandcheckedout")+" "+Formats.TIMESTAMP.formatValue(new Date()));
            } else {
                message.setText(app.getAppUserView().getUser().getName()+" "+AppLocal.getIntString("message.checkedout")+" "+Formats.TIMESTAMP.formatValue(new Date()));
            }
            shiftService.checkOut(app.getAppUserView().getUser().getId());
        } catch (BasicException ex) {
            MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotcheckout"));
            msg.show(this);
        }
        CheckOutAction();
}//GEN-LAST:event_btnCheckOutActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCheckIn;
    private javax.swing.JButton btnCheckOut;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel message;
    // End of variables declaration//GEN-END:variables
}
