package com.openbravo.pos.forms;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Thread-safe IoC (Inversion of Control) / Service Locator container for KriolOS POS.
 * <p>
 * This container manages the lifecycle, caching, and resolution of application services,
 * data logic layers, and view beans. It guarantees:
 * <ul>
 *   <li><b>Singleton caching:</b> Beans are instantiated once and cached across the application session.</li>
 *   <li><b>Thread safety & reentrancy:</b> Uses double-checked locking on {@link ConcurrentHashMap}
 *       to allow re-entrant, nested bean resolutions during bean construction or {@link BeanFactoryApp#init(AppView)}
 *       without risking {@link IllegalStateException} from concurrent map operations.</li>
 *   <li><b>Legacy class migration:</b> Maps legacy Openbravo POS class names and report paths to their
 *       modern ports and implementations.</li>
 *   <li><b>Polymorphic instantiation:</b> Automatically handles beans implementing {@link BeanFactory},
 *       beans requiring an {@link AppView} constructor, and BeanShell report scripts.</li>
 * </ul>
 *
 * @author KriolOS Team
 */
public final class BeanContainer {

    private static final Logger LOGGER = Logger.getLogger(BeanContainer.class.getName());

    /**
     * Thread-safe cache holding instantiated bean factories indexed by their resolved target key.
     */
    private static final Map<String, BeanFactory> BEAN_FACTORIES = new ConcurrentHashMap<>();

    /**
     * Immutable lookup table for legacy class name migrations, service port decouplings,
     * and report script redirects.
     */
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

    /**
     * Private constructor to prevent direct instantiation of this utility class.
     *
     * @throws UnsupportedOperationException always
     */
    private BeanContainer() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Clears all cached bean factories from memory.
     * <p>
     * Thread-safe operation synchronized on {@link BeanContainer#getClass()} to ensure
     * ongoing bean instantiation is not corrupted during cache invalidation.
     * Typically invoked during session teardown, user logout, or unit testing reset.
     */
    public static void cleanAll() {
        synchronized (BeanContainer.class) {
            BEAN_FACTORIES.clear();
        }
        LOGGER.log(Level.INFO, "BeanContainer registry cache has been successfully cleared.");
    }

    /**
     * Resolves and retrieves a bean using its target Class literal, handling casting automatically.
     *
     * @param <T> The expected return type of the bean.
     * @param beanClass The target class literal serving as resolution key and cast target. Must not be null.
     * @param appView The Application View context passed to the bean constructor or initializer.
     * @return The type-safe bean instance, or {@code null} if casting fails.
     * @throws NullPointerException if {@code beanClass} is null.
     * @throws BeanFactoryException if instantiation or lookup fails.
     */
    public static <T> T getBean(Class<T> beanClass, AppView appView) {
        Objects.requireNonNull(beanClass, "Parameter 'beanClass' cannot be null");
        return getBean(beanClass.getName(), beanClass, appView);
    }

    /**
     * Resolves and retrieves a bean by its String factory key, safely casting the instance
     * to the requested target class type.
     *
     * @param <T> The expected return type of the bean.
     * @param beanfactory The name of the class, interface, or script path acting as the key. Must not be null.
     * @param beanClass The expected class literal to cast the resolved bean to. Must not be null.
     * @param appView The Application View context.
     * @return The type-safe bean instance, or {@code null} if the bean cannot be cast to {@code beanClass}.
     * @throws NullPointerException if {@code beanfactory} or {@code beanClass} is null.
     * @throws BeanFactoryException if instantiation or lookup fails.
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
     * Resolves and retrieves a bean by its String factory key or class name.
     * <p>
     * Follows the double-checked locking idiom on {@link ConcurrentHashMap}:
     * <ol>
     *   <li>Resolves any legacy alias or service port mapping via {@link #mapNewClass(String)}.</li>
     *   <li>Performs a lock-free check on the internal cache for an already created {@link BeanFactory}.</li>
     *   <li>If missing, enters a synchronized block to guarantee single instantiation across threads
     *       while safely supporting re-entrant calls when a bean requests other beans in its constructor
     *       or {@link BeanFactoryApp#init(AppView)}.</li>
     *   <li>Initializes the factory if it implements {@link BeanFactoryApp} and caches it.</li>
     * </ol>
     *
     * @param beanfactory The class name or script path representing the bean. Must not be null.
     * @param appView The Application View context.
     * @return The instantiated bean instance returned by {@link BeanFactory#getBean()}.
     * @throws NullPointerException if {@code beanfactory} is null.
     * @throws BeanFactoryException if reflection or script resolution fails.
     */
    public static Object getBean(String beanfactory, AppView appView) {
        Objects.requireNonNull(beanfactory, "Parameter 'beanfactory' cannot be null");

        String targetKey = mapNewClass(beanfactory);

        BeanFactory factory = BEAN_FACTORIES.get(targetKey);
        if (factory == null) {
            synchronized (BeanContainer.class) {
                factory = BEAN_FACTORIES.get(targetKey);
                if (factory == null) {
                    factory = createFactoryInstance(targetKey, appView);
                    if (factory instanceof BeanFactoryApp beanFactoryApp) {
                        beanFactoryApp.init(appView);
                    }
                    BEAN_FACTORIES.put(targetKey, factory);
                }
            }
        }

        return factory.getBean();
    }

    /**
     * Maps legacy class names or interfaces to modern implementations or scripts.
     *
     * @param classname The raw class name or resource path.
     * @return The mapped target class name or script path, or the original name if no mapping exists.
     */
    private static String mapNewClass(String classname) {
        return OLD_CLASSES_MAP.getOrDefault(classname, classname);
    }

    /**
     * Instantiates a new {@link BeanFactory} for the specified class name or script.
     * <ul>
     *   <li>If the path starts with {@code "/"}, returns a {@link BeanFactoryScript}.</li>
     *   <li>If the class implements {@link BeanFactory}, instantiates it using its no-arg constructor.</li>
     *   <li>Otherwise, instantiates the target class using its {@code (AppView)} constructor and wraps
     *       the resulting instance in a {@link BeanFactoryObj}.</li>
     * </ul>
     *
     * @param className The fully qualified class name or script resource path.
     * @param appView The Application View context.
     * @return A newly created {@link BeanFactory} instance.
     * @throws BeanFactoryException if the class cannot be found, accessed, or instantiated.
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
