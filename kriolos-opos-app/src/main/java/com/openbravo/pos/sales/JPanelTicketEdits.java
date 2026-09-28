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


package com.openbravo.pos.sales;

import com.openbravo.basic.BasicException;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.ticket.ProductInfoExt;
import com.openbravo.pos.ticket.TicketLineInfo;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;


/**
 *
 * @author JG uniCenta
 */
public class JPanelTicketEdits extends JPanelTicket {

    private static final long serialVersionUID = 1L;
    
    private JTicketCatalogLines m_catandlines;
    
    public JPanelTicketEdits(AppView app) {
        super(app);
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public void activate() throws BasicException {      
        super.activate();
        if (m_catandlines != null) {
            m_catandlines.loadCatalog();
        }
    }

    public void reLoadCatalog(){      
    }    

    public void showCatalog() {
        getTicketButtons().setVisible(true);
        m_catandlines.showCatalog();
    }
    

    public void showRefundLines(List<TicketLineInfo> aRefundLines) {
        getTicketButtons().setVisible(false);
        m_catandlines.showRefundLines(aRefundLines);
    }

    @Override
    protected JTicketsBag getJTicketsBag() {
        return new JTicketsBagTicket(getAppView(), this);
    }

    @Override
    protected Component getSouthComponent() {

        m_catandlines = new JTicketCatalogLines(getAppView(), this,                
                "true".equals(getTicketButtons().getProperty("pricevisible")),
                "true".equals(getTicketButtons().getProperty("taxesincluded")),
                Integer.parseInt(getTicketButtons().getProperty("img-width", "64")),
                Integer.parseInt(getTicketButtons().getProperty("img-height", "54")));
        m_catandlines.setPreferredSize(new Dimension(0,
                Integer.parseInt(getTicketButtons().getProperty("cat-height", "245"))));
        m_catandlines.addActionListener(new CatalogListener());
        return m_catandlines;
    } 

    @Override
    protected void resetSouthComponent() {
    }
    
    private class CatalogListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            buttonTransition((ProductInfoExt) e.getSource());
        }  
    }  
       
}
