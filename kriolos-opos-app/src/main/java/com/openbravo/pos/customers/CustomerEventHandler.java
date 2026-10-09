//    KriolOS POS
//    Copyright (c) 2019-2023 KriolOS
//
//    
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
package com.openbravo.pos.customers;

import java.util.logging.Logger;

/**
 *
 * @author poolborges
 */
public class CustomerEventHandler {
    private static final Logger LOGGER = Logger.getLogger(CustomerEventHandler.class.getName());
    
    /**
     * saveCustomerData Trigger Script Execution on event
     * Execute only in view context of com.openbravo.pos.customers.CustomersView
     * 
     *   Events: customer.created, customer.updated, customer.deleted
     * 
     * @throws BasicException 
     */
    /*
    private void saveCustomerData() throws BasicException {
   
        if (m_editorrecord.getClass().getName().equals("com.openbravo.pos.customers.CustomersView")) {

            // com.openbravo.pos.customers.CustomersView MUST return Array of 27 Objects
            Object[] customer = (Object[]) m_editorrecord.createValue();
            AppView appView = AppContext.getAppView();

            if (m_Dirty.isDirty()) {
                switch (m_iState) {
                    case ST_UPDATE: {
                        triggerCustomerEvent("customer.updated", customer, appView);
                        break;
                    }
                    case ST_INSERT: {
                        triggerCustomerEvent("customer.created", customer, appView);

                        int n = JOptionPane.showConfirmDialog(
                                null,
                                AppLocal.getIntString("message.customerassign"),
                                AppLocal.getIntString("title.editor"),
                                JOptionPane.YES_NO_OPTION);

                        if (n == 0) {
                            CustomerInfoGlobal customerInfoGlobal = CustomerInfoGlobal.getInstance();
                            CustomerInfoExt customerInfoExt = new CustomerInfoExt(customer[0].toString());
                            customerInfoGlobal.setCustomerInfoExt(customerInfoExt);
                            customerInfoExt.setName(customer[3].toString());

                            if (appView != null) {
                                appView.getAppUserView().showTask("com.openbravo.pos.sales.JPanelTicketSales");
                            }
                        }
                        break;
                    }
                    case ST_DELETE: {
                        triggerCustomerEvent("customer.deleted", customer, appView);
                        break;
                    }
                    default:
                        break;
                }
            }
        }
    }
    
    
    private void triggerCustomerEvent(String event, Object[] customer, AppView appContext) {
        try {
            ScriptEngine scriptEngine = ScriptFactory.getScriptEngine(ScriptFactory.BEANSHELL);

            com.openbravo.pos.forms.ResourceService resourceService = appContext.getBean(com.openbravo.pos.forms.ResourceService.class);
            String script = resourceService.getResourceAsXML(event);
            scriptEngine.put("customer", customer);
            scriptEngine.put("device", appContext.getProperties().getProperty("machine.hostname"));
            scriptEngine.eval(script);

        } catch (BeanFactoryException | ScriptException e) {
            LOGGER.log(Level.WARNING, "Exception on executing script: "+event, e);
        }
    }
*/
}
