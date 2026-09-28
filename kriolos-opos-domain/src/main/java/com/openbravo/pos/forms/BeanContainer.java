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
package com.openbravo.pos.forms;

import java.util.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author poolborges
 */
public class BeanContainer {

    private static final Logger LOGGER = Logger.getLogger(BeanContainer.class.getName());
    private static final Map<String, BeanFactory> m_aBeanFactories = new HashMap<>();
    private static final HashMap<String, String> m_oldclasses = new HashMap<>();

    private static String mapNewClass(String classname) {
        String newclass = m_oldclasses.get(classname);
        return newclass == null
                ? classname
                : newclass;
    }

    static {
        m_oldclasses.put("com.openbravo.pos.reports.JReportCustomers", "/com/openbravo/reports/customers.bs");
        m_oldclasses.put("com.openbravo.pos.reports.JReportCustomersB", "/com/openbravo/reports/customersb.bs");
        m_oldclasses.put("com.openbravo.pos.reports.JReportClosedPos", "/com/openbravo/reports/closedpos.bs");
        m_oldclasses.put("com.openbravo.pos.reports.JReportClosedProducts", "/com/openbravo/reports/closedproducts.bs");
        m_oldclasses.put("com.openbravo.pos.reports.JChartSales", "/com/openbravo/reports/chartsales.bs");
        m_oldclasses.put("com.openbravo.pos.reports.JReportInventory", "/com/openbravo/reports/inventory.bs");
        m_oldclasses.put("com.openbravo.pos.reports.JReportInventory2", "/com/openbravo/reports/inventoryb.bs");
        m_oldclasses.put("com.openbravo.pos.reports.JReportInventoryBroken", "/com/openbravo/reports/inventorybroken.bs");
        m_oldclasses.put("com.openbravo.pos.reports.JReportInventoryDiff", "/com/openbravo/reports/inventorydiff.bs");
        m_oldclasses.put("com.openbravo.pos.reports.JReportPeople", "/com/openbravo/reports/people.bs");
        m_oldclasses.put("com.openbravo.pos.reports.JReportTaxes", "/com/openbravo/reports/taxes.bs");
        m_oldclasses.put("com.openbravo.pos.reports.JReportUserSales", "/com/openbravo/reports/usersales.bs");
        m_oldclasses.put("com.openbravo.pos.reports.JReportProducts", "/com/openbravo/reports/products.bs");
        m_oldclasses.put("com.openbravo.pos.reports.JReportCatalog", "/com/openbravo/reports/productscatalog.bs");

        m_oldclasses.put("com.openbravo.pos.panels.JPanelTax", "com.openbravo.pos.inventory.TaxPanel");
    }

    /**
     * Resolves and retrieves a bean by its String factory key name.
     * 
     * @param beanfactory The name of the class or the script path acting as the key.
     * @param appView     The Application View context.
     * @return The instantiated bean instance.
     */
    public static Object getBean(String beanfactory, AppView appView) {

        beanfactory = mapNewClass(beanfactory);
        BeanFactory bf = m_aBeanFactories.get(beanfactory);

        if (bf == null) {

            if (beanfactory.startsWith("/")) {
                bf = new BeanFactoryScript(beanfactory);
            } else {
                try {
                    Class<?> bfclass = Class.forName(beanfactory);

                    if (BeanFactory.class.isAssignableFrom(bfclass)) {
                        bf = (BeanFactory) bfclass.getDeclaredConstructor().newInstance();
                    } else {
                        Constructor<?> constMyView = bfclass.getConstructor(new Class[]{AppView.class});
                        Object bean = constMyView.newInstance(new Object[]{appView});
                        bf = new BeanFactoryObj(bean);
                    }

                } catch (ClassNotFoundException | InstantiationException

                        | IllegalAccessException | NoSuchMethodException
                        | SecurityException | IllegalArgumentException | InvocationTargetException e) {
                    LOGGER.log(Level.WARNING, "Cannot find Bean: " + beanfactory, e);
                    throw new BeanFactoryException(e);
                }
            }

            m_aBeanFactories.put(beanfactory, bf);

            if (bf instanceof BeanFactoryApp) {
                ((BeanFactoryApp) bf).init(appView);
            }
        }
        return bf.getBean();
    }

    /**
     * Retrieves a bean by its String factory key and handles casting automatically.
     * 
     * @param <T>         The expected type of the Bean.
     * @param beanfactory The name of the class or script path acting as the key.
     * @param beanClass   The expected class type to cast the bean to.
     * @param appView     The Application View context.
     * @return The type-safe bean instance, or null if a casting error occurs.
     */
    public static <T> T getBean(String beanfactory, Class<T> beanClass, AppView appView) {
        Object bean = getBean(beanfactory, appView);
        if (bean == null) {
            return null;
        }
        try {
            return beanClass.cast(bean);
        } catch (ClassCastException e) {
            LOGGER.log(Level.SEVERE, "Bean resolved from key '" + beanfactory + "' is not of type " + beanClass.getName(), e);
            return null;
        }
    }

    /**
     * Retrieves a bean cleanly using only its Class literal.
     * 
     * @param <T>       The expected type of the Bean.
     * @param beanClass The class type serving as the resolution key.
     * @param appView   The Application View context.
     * @return The type-safe bean instance, or null if the class is null or casting fails.
     */
    public static <T> T getBean(Class<T> beanClass, AppView appView) {
        if (beanClass == null) {
            return null;
        }
        return getBean(beanClass.getName(), beanClass, appView);
    }
}
