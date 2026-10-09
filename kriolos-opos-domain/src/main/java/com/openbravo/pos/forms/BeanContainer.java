package com.openbravo.pos.forms;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Thread-safe IoC / Bean Container for static factory and service resolution.
 * Guarantees atomic initialization and type-safe casting operations.
 *
 * @author KriolOS
 */
public final class BeanContainer {

    private static final Logger LOGGER = Logger.getLogger(BeanContainer.class.getName());

    // Thread-safe registry cache to prevent race conditions during heavy concurrent access
    private static final Map<String, BeanFactory> BEAN_FACTORIES = new ConcurrentHashMap<>();

    // Immutable lookup table for legacy class migrations to eliminate memory overhead
    private static final Map<String, String> OLD_CLASSES_MAP = Map.ofEntries(
            Map.entry("com.openbravo.pos.reports.JReportCustomers", "/com/openbravo/reports/customers.bs"),
            Map.entry("com.openbravo.pos.reports.JReportCustomersB", "/com/openbravo/reports/customersb.bs"),
            Map.entry("com.openbravo.pos.reports.JReportClosedPos", "/com/openbravo/reports/closedpos.bs"),
            Map.entry("com.openbravo.pos.reports.JReportClosedProducts", "/com/openbravo/reports/closedproducts.bs"),
            Map.entry("com.openbravo.pos.reports.JChartSales", "/com/openbravo/reports/chartsales.bs"),
            Map.entry("com.openbravo.pos.reports.JReportInventory", "/com/openbravo/reports/inventory.bs"),
            Map.entry("com.openbravo.pos.reports.JReportInventory2", "/com/openbravo/reports/inventoryb.bs"),
            Map.entry("com.openbravo.pos.reports.JReportInventoryBroken", "/com/openbravo/reports/inventorybroken.bs"),
            Map.entry("com.openbravo.pos.reports.JReportInventoryDiff", "/com/openbravo/reports/inventorydiff.bs"),
            Map.entry("com.openbravo.pos.reports.JReportPeople", "/com/openbravo/reports/people.bs"),
            Map.entry("com.openbravo.pos.reports.JReportTaxes", "/com/openbravo/reports/taxes.bs"),
            Map.entry("com.openbravo.pos.reports.JReportUserSales", "/com/openbravo/reports/usersales.bs"),
            Map.entry("com.openbravo.pos.reports.JReportProducts", "/com/openbravo/reports/products.bs"),
            Map.entry("com.openbravo.pos.reports.JReportCatalog", "/com/openbravo/reports/productscatalog.bs"),
            Map.entry("com.openbravo.pos.panels.JPanelTax", "com.openbravo.pos.inventory.TaxPanel"),
            Map.entry("com.openbravo.pos.sales.AuditService", "com.openbravo.pos.sales.DataLogicAudit"),
            Map.entry("com.openbravo.pos.sales.TaxService", "com.openbravo.pos.sales.DataLogicTax"),
            Map.entry("com.openbravo.pos.customers.CustomerService", "com.openbravo.pos.customers.DataLogicCustomers"),
            Map.entry("com.openbravo.pos.catalog.CatalogService", "com.openbravo.pos.catalog.CatalogServiceImpl"),
            Map.entry("com.openbravo.pos.pim.DataLogicProducts", "com.openbravo.pos.pim.DataLogicProducts"),
            Map.entry("com.openbravo.pos.pim.DataLogicCategories", "com.openbravo.pos.pim.DataLogicCategories"),
            Map.entry("com.openbravo.pos.pim.DataLogicUom", "com.openbravo.pos.pim.DataLogicUom"),
            Map.entry("com.openbravo.pos.sales.SharedTicketService", "com.openbravo.pos.sales.DataLogicReceipts"),
            Map.entry("com.openbravo.pos.sales.TicketLifecycleService", "com.openbravo.pos.forms.DataLogicSales"),
            Map.entry("com.openbravo.pos.suppliers.SupplierService", "com.openbravo.pos.suppliers.DataLogicSuppliers"),
            Map.entry("com.openbravo.pos.admin.PeopleService", "com.openbravo.pos.admin.DataLogicAdmin"),
            Map.entry("com.openbravo.pos.forms.SecurityService", "com.openbravo.pos.forms.DataLogicSystem"),
            Map.entry("com.openbravo.pos.forms.ResourceService", "com.openbravo.pos.forms.DataLogicSystem"),
            Map.entry("com.openbravo.pos.forms.SystemService", "com.openbravo.pos.forms.DataLogicSystem"),
            Map.entry("com.openbravo.pos.sales.RemoteOrderService", "com.openbravo.pos.forms.DataLogicOrders"),
            Map.entry("com.openbravo.pos.inventory.InventoryService", "com.openbravo.pos.inventory.DataLogicInventory"),
            Map.entry("com.openbravo.pos.inventory.StockService", "com.openbravo.pos.inventory.DataLogicInventory"),
            Map.entry("com.openbravo.pos.payment.TreasuryService", "com.openbravo.pos.payment.DataLogicPayments"),
            Map.entry("com.openbravo.pos.inventory.AttributeService", "com.openbravo.pos.inventory.DataLogicAttribute"),
            Map.entry("com.openbravo.pos.voucher.VoucherService", "com.openbravo.pos.voucher.DataLogicVouchers"),
            Map.entry("com.openbravo.pos.epm.ShiftService", "com.openbravo.pos.epm.DataLogicPresenceManagement"),
            Map.entry("com.openbravo.pos.imports.ImportService", "com.openbravo.pos.imports.DataLogicImport"),
            Map.entry("com.openbravo.pos.sales.restaurant.RestaurantService", "com.openbravo.pos.sales.restaurant.DataLogicRestaurant")
    );

    // Private constructor prevents accidental instantiation of this static utility class
    private BeanContainer() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Clears all cached bean factories from memory. Useful for context resets,
     * user logouts, or tearing down test suites.
     */
    public static void cleanAll() {
        BEAN_FACTORIES.clear();
        LOGGER.log(Level.INFO, "BeanContainer registry cache has been successfully cleared.");
    }

    /**
     * Retrieves a bean using the class name as the factory key and handles
     * casting automatically.
     *
     * @param <T> The expected type of the Bean.
     * @param beanClass The target class type to resolve and cast the bean to.
     * @param appView The Application View context.
     * @return The type-safe bean instance, or null if a casting error occurs.
     */
    public static <T> T getBean(Class<T> beanClass, AppView appView) {
        Objects.requireNonNull(beanClass, "Parameter 'beanClass' cannot be null");
        return getBean(beanClass.getName(), beanClass, appView);
    }

    /**
     * Retrieves a bean by its String factory key and handles casting
     * automatically.
     *
     * @param <T> The expected type of the Bean.
     * @param beanfactory The name of the class or script path acting as the
     * key.
     * @param beanClass The expected class type to cast the bean to.
     * @param appView The Application View context.
     * @return The type-safe bean instance, or null if a casting error occurs.
     */
    public static <T> T getBean(String beanfactory, Class<T> beanClass, AppView appView) {
        Objects.requireNonNull(beanClass, "Parameter 'beanClass' cannot be null");

        Object bean = getBean(beanfactory, appView);
        if (bean == null) {
            return null;
        }

        try {
            return beanClass.cast(bean);
        }
        catch (ClassCastException e) {
            LOGGER.log(Level.SEVERE, "Bean resolved from key ''{0}'' is not of type {1}",
                    new Object[]{beanfactory, beanClass.getName()});
            return null;
        }
    }

    /**
     * Resolves and retrieves a bean by its String factory key name in a
     * strictly atomic manner.
     *
     * @param beanfactory The name of the class or the script path acting as the
     * key.
     * @param appView The Application View context.
     * @return The instantiated bean instance.
     */
    public static Object getBean(String beanfactory, AppView appView) {
        Objects.requireNonNull(beanfactory, "Parameter 'beanfactory' cannot be null");

        String targetKey = mapNewClass(beanfactory);

        // computeIfAbsent locks only the specific bucket to guarantee single initialization across threads
        BeanFactory factory = BEAN_FACTORIES.computeIfAbsent(targetKey, key -> {
            BeanFactory bf = createFactoryInstance(key, appView);
            if (bf instanceof BeanFactoryApp beanFactoryApp) {
                beanFactoryApp.init(appView);
            }
            return bf;
        });

        return factory.getBean();
    }

    private static String mapNewClass(String classname) {
        return OLD_CLASSES_MAP.getOrDefault(classname, classname);
    }

    /**
     * Isolates Reflection logic to maintain architectural readability and
     * performance.
     */
    private static BeanFactory createFactoryInstance(String className, AppView appView) {
        if (className.startsWith("/")) {
            return new BeanFactoryScript(className);
        }

        try {
            Class<?> bfclass = Class.forName(className);

            if (BeanFactory.class.isAssignableFrom(bfclass)) {
                return (BeanFactory) bfclass.getDeclaredConstructor().newInstance();
            } else {
                // Simplified reflection calls utilizing varargs to bypass redundant array wraps
                var constMyView = bfclass.getConstructor(AppView.class);
                Object bean = constMyView.newInstance(appView);
                return new BeanFactoryObj(bean);
            }
        }
        catch (ClassNotFoundException | NoSuchMethodException e) {
            LOGGER.log(Level.WARNING, "Definition or constructor not found for Bean: {0}", className);
            throw new BeanFactoryException(e);
        }
        catch (InstantiationException | IllegalAccessException | InvocationTargetException | SecurityException e) {
            LOGGER.log(Level.SEVERE, "Critical instantiation error for Bean: " + className, e);
            throw new BeanFactoryException(e);
        }
    }
}
