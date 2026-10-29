package io.github.kriolos.opos.quarkus;

import io.quarkus.runtime.annotations.RegisterForReflection;
import io.quarkus.runtime.annotations.RegisterResourceBundle;
import io.quarkus.runtime.annotations.RegisterResources;
import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.graalvm.nativeimage.hosted.Feature;
import org.graalvm.nativeimage.hosted.RuntimeClassInitialization;
import org.graalvm.nativeimage.hosted.RuntimeReflection;

/**
 * Core KriolOS POS GraalVM Native Image Feature.
 *
 * <p>Handles {@code com.openbravo.*} and {@code io.github.kriolos.*} DataLogic
 * services, UI panels, persistence entities, serialization targets, and domain models.
 *
 * @author KriolOS
 */
@RegisterResourceBundle(bundleName = "pos_messages")
@RegisterResourceBundle(bundleName = "erp_messages")
@RegisterResourceBundle(bundleName = "data_messages")
@RegisterResourceBundle(bundleName = "beans_messages")
@RegisterResources(globs = {
    "com/openbravo/images/**",
    "com/openbravo/svg/**",
    "com/openbravo/pos/scripts/**",
    "com/openbravo/pos/templates/**",
    "com/openbravo/pos/util/**",
    "com/openbravo/pos/images/**",
    "com/openbravo/pos/printer/**",
    "pos_liquidbase/**",
    "db/changelog/**",
    "db/migrations/**",
    "pos_messages*.properties",
    "erp_messages*.properties",
    "data_messages*.properties",
    "beans_messages*.properties",
    "org/apache/velocity/**",
    "bsh/**",
    "icons/**"
})
@RegisterForReflection(
    targets = {
        // ── BeanContainer DataLogic & Service Beans ──────────────────────────
        com.openbravo.pos.admin.DataLogicSystem.class,
        com.openbravo.pos.admin.DataLogicAdmin.class,
        com.openbravo.pos.config.ApplicationConfigurationServiceImpl.class,
        com.openbravo.pos.sales.DataLogicSales.class,
        com.openbravo.pos.sales.DataLogicReceipts.class,
        com.openbravo.pos.sales.DataLogicOrders.class,
        com.openbravo.pos.sales.DataLogicAudit.class,
        com.openbravo.pos.sales.DataLogicTax.class,
        com.openbravo.pos.sales.restaurant.DataLogicRestaurant.class,
        com.openbravo.pos.customers.DataLogicCustomers.class,
        com.openbravo.pos.catalog.CatalogServiceImpl.class,
        com.openbravo.pos.pim.DataLogicProducts.class,
        com.openbravo.pos.pim.DataLogicCategories.class,
        com.openbravo.pos.pim.DataLogicUom.class,
        com.openbravo.pos.suppliers.DataLogicSuppliers.class,
        com.openbravo.pos.inventory.DataLogicInventory.class,
        com.openbravo.pos.inventory.DataLogicAttribute.class,
        com.openbravo.pos.payment.DataLogicPayments.class,
        com.openbravo.pos.voucher.DataLogicVouchers.class,
        com.openbravo.pos.epm.DataLogicPresenceManagement.class,
        com.openbravo.pos.imports.DataLogicImport.class,

        // ── BeanContainer UI Panels (Menu.Root.bs) ───────────────────────────
        com.openbravo.pos.menu.JRootMenu.class,
        com.openbravo.pos.sales.JPanelTicketSales.class,
        com.openbravo.pos.sales.JPanelTicketEdits.class,
        com.openbravo.pos.customers.CustomersPayment.class,
        com.openbravo.pos.panels.JPanelPayments.class,
        com.openbravo.pos.panels.JPanelCloseMoney.class,
        com.openbravo.pos.customers.CustomersPanel.class,
        com.openbravo.pos.suppliers.SuppliersPanel.class,
        com.openbravo.pos.inventory.CategoriesPanel.class,
        com.openbravo.pos.inventory.ProductsPanel.class,
        com.openbravo.pos.inventory.ProductsWarehousePanel.class,
        com.openbravo.pos.inventory.StockManagement.class,
        com.openbravo.pos.inventory.AuxiliarPanel.class,
        com.openbravo.pos.inventory.BundlePanel.class,
        com.openbravo.pos.inventory.AttributesPanel.class,
        com.openbravo.pos.inventory.AttributeValuesPanel.class,
        com.openbravo.pos.inventory.AttributeSetsPanel.class,
        com.openbravo.pos.inventory.AttributeUsePanel.class,
        com.openbravo.pos.inventory.UomPanel.class,
        com.openbravo.pos.admin.ResourcesPanel.class,
        com.openbravo.pos.admin.RolesPanel.class,
        com.openbravo.pos.admin.PeoplePanel.class,
        com.openbravo.pos.inventory.TaxCategoriesPanel.class,
        com.openbravo.pos.inventory.TaxCustCategoriesPanel.class,
        com.openbravo.pos.inventory.TaxPanel.class,
        com.openbravo.pos.inventory.LocationsPanel.class,
        com.openbravo.pos.sales.restaurant.JPanelFloors.class,
        com.openbravo.pos.sales.restaurant.JPanelPlaces.class,
        com.openbravo.pos.voucher.VoucherPanel.class,
        com.openbravo.pos.epm.BreaksPanel.class,
        com.openbravo.pos.epm.LeavesPanel.class,
        com.openbravo.pos.epm.JPanelEmployeePresence.class,
        com.openbravo.pos.imports.JPanelCSVImport.class,
        com.openbravo.pos.imports.StockQtyImport.class,
        com.openbravo.pos.imports.CustomerCSVImport.class,
        com.openbravo.pos.imports.JPanelCSVCleardb.class,
        com.openbravo.pos.config.JPanelConfiguration.class,
        com.openbravo.pos.panels.JPanelPrinter.class,

        // ── Data Loader & Persistence Entities ───────────────────────────────
        com.openbravo.pos.cash.CashRegister.class,

        // ── Velocity Template Rendering Models ───────────────────────────────
        com.openbravo.pos.ticket.TicketInfo.class,
        com.openbravo.pos.ticket.TicketLineInfo.class,
        com.openbravo.pos.ticket.TicketTaxInfo.class,
        com.openbravo.pos.ticket.TaxInfo.class,
        com.openbravo.pos.ticket.ProductInfoExt.class,
        com.openbravo.pos.ticket.UserInfo.class,
        com.openbravo.pos.ticket.FindTicketsInfo.class,
        com.openbravo.pos.payment.PaymentInfo.class,
        com.openbravo.pos.payment.PaymentInfoCash.class,
        com.openbravo.pos.payment.PaymentInfoTicket.class,
        com.openbravo.pos.payment.PaymentInfoMagcard.class,
        com.openbravo.pos.payment.PaymentInfoFree.class,
        com.openbravo.pos.customers.CustomerInfo.class,
        com.openbravo.pos.customers.CustomerInfoExt.class,
        com.openbravo.pos.pim.CategoryInfo.class,
        com.openbravo.pos.pim.UomInfo.class,
        com.openbravo.pos.inventory.InventoryRecord.class,
        com.openbravo.pos.inventory.InventoryLine.class,
        com.openbravo.pos.inventory.LocationInfo.class,
        com.openbravo.pos.inventory.MovementReason.class,
        com.openbravo.pos.suppliers.SupplierInfo.class,
        com.openbravo.pos.sales.TaxesLogic.class,

        // ── Openbravo Custom Listener Arrays (EventListenerList.getListeners) ─
        com.openbravo.data.user.StateListener.class,
        com.openbravo.data.user.StateListener[].class,
        com.openbravo.data.user.EditorListener.class,
        com.openbravo.data.user.EditorListener[].class,
        com.openbravo.data.user.BrowseListener.class,
        com.openbravo.data.user.BrowseListener[].class
    },
    classNames = {
        "com.openbravo.app.config.MicroProfileBootstrapConfigProvider",
        "com.openbravo.app.config.LegacyBootstrapConfigProvider",
        "com.openbravo.app.config.MicroProfileConfigSource",
        "io.github.kriolos.opos.scripting.bsvelocity.BsVelocityScriptEngineProvider",
        "com.openbravo.pos.hardware.DefaultPeripheralProvider",
        "com.openbravo.pos.catalog.ClassicCatalogFactory",
        "com.openbravo.pos.sales.modern.ModernCatalogFactory",
        "com.openbravo.pos.sales.layout.CoreSaleLayoutFactory",
        "com.openbravo.pos.sales.layout.ModernSaleLayoutFactory",
        "com.openbravo.pos.l10n.ptcv.PtCvLocalizationProvider",
        "org.hsqldb.jdbc.JDBCDriver",
        "org.hsqldb.jdbcDriver"
    },
    methods = true,
    fields = true
)
public final class KriolosPOSFeature implements Feature {

    @RegisterForReflection(
        targets = {
            java.util.Date.class,
            java.util.Properties.class,
            java.util.Hashtable.class,
            java.util.ArrayList.class,
            java.util.Vector.class,
            java.util.HashMap.class,
            java.util.LinkedHashMap.class,
            java.lang.String.class,
            java.lang.Number.class,
            java.lang.Integer.class,
            java.lang.Double.class,
            java.lang.Long.class,
            java.lang.Boolean.class,
            com.openbravo.pos.ticket.TicketInfo.class,
            com.openbravo.pos.ticket.TicketLineInfo.class,
            com.openbravo.pos.ticket.TicketTaxInfo.class,
            com.openbravo.pos.ticket.TaxInfo.class,
            com.openbravo.pos.ticket.ProductInfoExt.class,
            com.openbravo.pos.pim.CategoryInfo.class,
            com.openbravo.pos.ticket.UserInfo.class,
            com.openbravo.pos.customers.CustomerInfo.class,
            com.openbravo.pos.customers.CustomerInfoExt.class,
            com.openbravo.pos.sales.SharedTicketInfo.class
        },
        serialization = true
    )
    public static final class SerializationTargets {}

    private static final String[] KRIOLOS_PACKAGES = {
        "com/openbravo/pos/menu/",
        "com/openbravo/pos/sales/",
        "com/openbravo/pos/customers/",
        "com/openbravo/pos/inventory/",
        "com/openbravo/pos/panels/",
        "com/openbravo/pos/suppliers/",
        "com/openbravo/pos/admin/",
        "com/openbravo/pos/imports/",
        "com/openbravo/pos/epm/",
        "com/openbravo/pos/voucher/",
        "com/openbravo/pos/config/",
        "com/openbravo/pos/payment/",
        "com/openbravo/pos/ticket/",
        "com/openbravo/pos/catalog/",
        "com/openbravo/pos/reports/",
        "com/openbravo/pos/forms/",
        "com/openbravo/format/",
        "com/openbravo/data/",
        "io/github/kriolos/"
    };

    public KriolosPOSFeature() {
        // Required default constructor for GraalVM Feature SPI
    }

    @Override
    public String getDescription() {
        return "KriolOS POS application domain, UI panels, and DataLogic configuration";
    }

    @Override
    public void beforeAnalysis(BeforeAnalysisAccess access) {
        // StringUtils holds a static java.util.Random instance which must be initialized at runtime
        RuntimeClassInitialization.initializeAtRunTime(com.openbravo.util.StringUtils.class);

        // Defer NetBeans OpenIDE Lookup ActiveQueue (starts a daemon thread in <clinit>)
        RuntimeClassInitialization.initializeAtRunTime("org.openide");

        // Defer PDFBox encryption classes that optionally reference BouncyCastle
        RuntimeClassInitialization.initializeAtRunTime("org.apache.pdfbox");

        // Defer InstanceManager and Java RMI to runtime (uses RMI / SecureRandom for single-instance check)
        RuntimeClassInitialization.initializeAtRunTime(
                "com.openbravo.pos.instance",
                "java.rmi",
                "sun.rmi",
                "org.apache.commons.logging.impl.Log4jApiLogFactory"
        );

        // Dynamically discover and register POS domain classes from application JARs
        int registeredCount = registerClasspathComponents(access);

        // Auto-register any entity implementing SerializableRead for reflection (used by SerializerReadClass)
        access.registerSubtypeReachabilityHandler((duringAccess, subtype) -> {
            RuntimeReflection.register(subtype);
            try {
                RuntimeReflection.register(subtype.getDeclaredConstructors());
            } catch (Throwable ignored) {
            }
            try {
                RuntimeReflection.register(subtype.getDeclaredFields());
            } catch (Throwable ignored) {
            }
            try {
                RuntimeReflection.register(subtype.getDeclaredMethods());
            } catch (Throwable ignored) {
            }
        }, com.openbravo.data.loader.SerializableRead.class);

        System.out.printf("[KriolosPOSFeature] Scanned and registered %d POS domain components and SerializableRead reachability handler.%n", registeredCount);
    }

    private int registerClasspathComponents(BeforeAnalysisAccess access) {
        int count = 0;
        for (Path path : access.getApplicationClassPath()) {
            File file = path.toFile();
            if (!file.isFile() || !file.getName().endsWith(".jar")) {
                continue;
            }
            try (JarFile jar = new JarFile(file)) {
                Enumeration<JarEntry> entries = jar.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    String name = entry.getName();
                    boolean isAnonymous = name.matches(".*\\$\\d+\\.class");
                    if (name.endsWith(".class") && !isAnonymous && isMatch(name)) {
                        String className = name.substring(0, name.length() - 6).replace('/', '.');
                        if (registerClassForReflection(access, className)) {
                            count++;
                        }
                    }
                }
            } catch (Throwable ignored) {
            }
        }
        return count;
    }

    private boolean isMatch(String name) {
        for (String prefix : KRIOLOS_PACKAGES) {
            if (name.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    private boolean registerClassForReflection(BeforeAnalysisAccess access, String className) {
        try {
            Class<?> clazz = access.findClassByName(className);
            if (clazz == null) {
                return false;
            }
            RuntimeReflection.register(clazz);
            try {
                RuntimeReflection.register(clazz.getConstructors());
                RuntimeReflection.register(clazz.getDeclaredConstructors());
            } catch (Throwable ignored) {
            }
            try {
                RuntimeReflection.register(clazz.getFields());
                RuntimeReflection.register(clazz.getDeclaredFields());
            } catch (Throwable ignored) {
            }
            try {
                for (Method method : clazz.getMethods()) {
                    RuntimeReflection.register(method);
                }
                for (Method method : clazz.getDeclaredMethods()) {
                    RuntimeReflection.register(method);
                }
            } catch (Throwable ignored) {
            }
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }
}
