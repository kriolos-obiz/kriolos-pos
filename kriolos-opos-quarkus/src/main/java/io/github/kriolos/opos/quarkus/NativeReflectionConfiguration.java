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
import org.graalvm.nativeimage.hosted.RuntimeJNIAccess;
import org.graalvm.nativeimage.hosted.RuntimeReflection;
import org.graalvm.nativeimage.hosted.RuntimeResourceAccess;

/**
 * Central code-first registry for GraalVM Native Image configuration (reflection,
 * resource bundles, classpath resources, and runtime class initialization).
 *
 * <p><strong>Architectural Requirement:</strong> All classes, resource bundles,
 * classpath resource globs, and runtime class initialization rules required for
 * GraalVM/Mandrel native compilation are declared as Java code in this single class.
 *
 * <p>Quarkus processes {@link RegisterForReflection}, {@link RegisterResourceBundle},
 * and {@link RegisterResources} at augmentation time, and GraalVM invokes
 * {@link #beforeAnalysis(BeforeAnalysisAccess)} via the {@link Feature} SPI.
 *
 * @author KriolOS
 */
@RegisterResourceBundle(bundleName = "com.formdev.flatlaf.resources.Bundle")
@RegisterResourceBundle(bundleName = "pos_messages")
@RegisterResourceBundle(bundleName = "erp_messages")
@RegisterResourceBundle(bundleName = "data_messages")
@RegisterResourceBundle(bundleName = "beans_messages")
@RegisterResourceBundle(bundleName = "net/sf/jasperreports/view/viewer")
@RegisterResourceBundle(bundleName = "jasperreports_messages")
@RegisterResources(globs = {
    "com/openbravo/images/**",
    "com/openbravo/svg/**",
    "com/openbravo/reports/**",
    "com/openbravo/pos/scripts/**",
    "com/openbravo/pos/templates/**",
    "com/openbravo/pos/util/**",
    "com/openbravo/pos/images/**",
    "com/openbravo/pos/printer/**",
    "net/sf/jasperreports/**",
    "default.jasperreports.properties",
    "jasperreports*.properties",
    "pos_liquidbase/**",
    "db/changelog/**",
    "db/migrations/**",
    "com/formdev/flatlaf/**",
    "org/jdesktop/swingx/**",
    "io/github/kriolos/opos/ui/flatlaf/**/*.properties",
    "META-INF/services/**",
    "META-INF/usbdevices.xml",
    "javax.usb.properties",
    "native/**",
    "org/usb4java/**",
    "org/hsqldb/**",
    "org/apache/velocity/**",
    "bsh/**",
    "pos_messages*.properties",
    "erp_messages*.properties",
    "data_messages*.properties",
    "beans_messages*.properties",
    "icons/**"
})
@RegisterForReflection(
    targets = {
        // ── Look & Feel themes ───────────────────────────────────────────────
        com.formdev.flatlaf.FlatLaf.class,
        com.formdev.flatlaf.FlatDarkLaf.class,
        com.formdev.flatlaf.FlatLightLaf.class,
        com.formdev.flatlaf.FlatIntelliJLaf.class,
        com.formdev.flatlaf.FlatDarculaLaf.class,

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
        com.openbravo.pos.reports.CashReport.class,
        com.openbravo.pos.reports.PaymentsListLine.class,
        com.openbravo.pos.reports.CategorySalesLine.class,
        com.openbravo.pos.reports.ProductSalesLine.class,
        com.openbravo.pos.reports.SalesLine.class,
        com.openbravo.pos.reports.RemovedProductLines.class,
        com.openbravo.pos.reports.DrawerOpenedLines.class,
        com.openbravo.pos.reports.FinancialReport.class,
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
        com.openbravo.data.user.BrowseListener[].class,

        // ── BeanShell & Script Runtime Targets ───────────────────────────────
        java.lang.System.class,
        java.io.PrintStream.class,
        java.lang.Math.class,
        javax.swing.JOptionPane.class,
        javax.swing.UIManager.class,
        javax.swing.JLabel.class,
        javax.swing.JDialog.class,
        javax.swing.plaf.FontUIResource.class,
        java.awt.Font.class,
        java.awt.Dimension.class,
        java.awt.Window.class,
        com.openbravo.pos.payment.JPaymentCashPos.class,
        com.openbravo.pos.payment.JPaymentCashPos.ScriptPaymentCash.class,
        com.openbravo.pos.payment.JPaymentSelect.class,
        com.openbravo.pos.payment.JPaymentSelectReceipt.class,
        com.openbravo.pos.payment.JPaymentSelectRefund.class,
        com.openbravo.pos.payment.JPaymentSelectCustomer.class,
        com.openbravo.format.Formats.class,

        // ── Report UI Panels & Filter Components (instantiated in BeanShell report scripts) ──
        com.openbravo.pos.reports.PanelReportBean.class,
        com.openbravo.pos.reports.JPanelReport.class,
        com.openbravo.pos.reports.InventoryReportBean.class,
        com.openbravo.pos.reports.JParamsCustomer.class,
        com.openbravo.pos.reports.JParamsDatesInterval.class,
        com.openbravo.pos.reports.JParamsLocation.class,
        com.openbravo.pos.reports.JParamsLocationWithFirst.class,
        com.openbravo.pos.reports.JParamsReason.class,
        com.openbravo.pos.reports.JParamsSuppliers.class,
        com.openbravo.pos.reports.JParamsText.class,
        com.openbravo.pos.reports.JParamsUser.class,
        com.openbravo.pos.reports.JParamsComposed.class,
        com.openbravo.pos.reports.ReportEditorCreator.class,
        com.openbravo.pos.ticket.CategoryFilter.class,
        com.openbravo.pos.ticket.ProductFilter.class,
        com.openbravo.pos.ticket.ProductFilter1.class,
        com.openbravo.pos.ticket.ProductFilter2.class,
        com.openbravo.pos.ticket.ProductFilter3.class,
        com.openbravo.pos.ticket.ProductFilter4.class,
        com.openbravo.beans.DateUtils.class,
        com.openbravo.data.loader.Datas.class
    },
    classNames = {
        // ── ServiceLoader SPI Providers & Runtime-scoped Implementations ─────
        "com.formdev.flatlaf.swingx.FlatSwingXDefaultsAddon",
        "io.github.kriolos.opos.ui.flatlaf.FlatPOSThemeFactory",
        "io.github.kriolos.opos.ui.flatlaf.FlatPOSThemeFactory$SpectrumDarkVariant",
        "io.github.kriolos.opos.ui.flatlaf.FlatPOSThemeFactory$SpectrumLightVariant",
        "io.github.kriolos.opos.ui.nativefallback.NativeThemeFactory",
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

        // ── Non-Quarkus JDBC Drivers (loaded via Class.forName in Session / Liquibase) ──
        // (Quarkus extensions for H2, MariaDB, PostgreSQL, and SQLite register their own drivers)
        "org.hsqldb.jdbc.JDBCDriver",
        "org.hsqldb.jdbcDriver",

        // ── Hardware Native Drivers (usb4java javax.usb Service) ─────────────
        "org.usb4java.javax.Services"
    },
    methods = true,
    fields = true
)
public final class NativeReflectionConfiguration implements Feature {

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

    private static final String[] JNI_HARDWARE_CLASSES = {
        // nrjavaserial (RXTX) JNI callbacks
        "gnu.io.RXTXPort",
        "gnu.io.RXTXVersion",
        "gnu.io.RXTXCommDriver",
        "gnu.io.CommPortIdentifier",
        "gnu.io.NativeResourceException",
        "gnu.io.SerialPortEvent",
        "gnu.io.UnsupportedCommOperationException",
        // usb4java (libusb) JNI callbacks and descriptors
        "org.usb4java.LibUsb",
        "org.usb4java.Loader",
        "org.usb4java.BufferUtils",
        "org.usb4java.Context",
        "org.usb4java.Device",
        "org.usb4java.DeviceHandle",
        "org.usb4java.DeviceList",
        "org.usb4java.DeviceDescriptor",
        "org.usb4java.ConfigDescriptor",
        "org.usb4java.Interface",
        "org.usb4java.InterfaceDescriptor",
        "org.usb4java.EndpointDescriptor",
        "org.usb4java.BosDescriptor",
        "org.usb4java.BosDevCapabilityDescriptor",
        "org.usb4java.SsEndpointCompanionDescriptor",
        "org.usb4java.SsUsbDeviceCapabilityDescriptor",
        "org.usb4java.Usb20ExtensionDescriptor",
        "org.usb4java.ContainerIdDescriptor",
        "org.usb4java.Transfer",
        "org.usb4java.IsoPacketDescriptor",
        "org.usb4java.HotplugCallbackHandle"
    };

    /**
     * Classpath entry prefixes dynamically scanned and registered for reflection in {@link #registerClasspathComponents}.
     */
    private static final String[] CLASS_PATH_START_WITH = {
        "com/formdev/flatlaf/",
        "org/jdesktop/swingx/",
        "org/apache/velocity/",
        "bsh/",
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
        "com/openbravo/format/",
        "com/openbravo/data/",
        "io/github/kriolos/"
    };

    /**
     * Resource bundles dynamically accessed by JasperReports and POS report templates.
     */
    private static final String[] REPORT_MESSAGE_BUNDLES = {
        "net/sf/jasperreports/view/viewer",
        "jasperreports_messages",
        "com/openbravo/reports/barcode_sheet_messages",
        "com/openbravo/reports/customers_messages",
        "com/openbravo/reports/epm_dailypresence_messages",
        "com/openbravo/reports/epm_dailyschedule_messages",
        "com/openbravo/reports/epm_performance_messages",
        "com/openbravo/reports/inventoryb_messages",
        "com/openbravo/reports/inventorydiffdetail_messages",
        "com/openbravo/reports/inventorydiff_messages",
        "com/openbravo/reports/inventorylistdetail_messages",
        "com/openbravo/reports/inventory_messages",
        "com/openbravo/reports/productlabels_messages",
        "com/openbravo/reports/productscatalog_messages",
        "com/openbravo/reports/products_messages",
        "com/openbravo/reports/salecatalog_messages",
        "com/openbravo/reports/sales_cashflow_messages",
        "com/openbravo/reports/sales_cashregisterlog_messages",
        "com/openbravo/reports/sales_categorysales_1_messages",
        "com/openbravo/reports/sales_categorysales_messages",
        "com/openbravo/reports/sales_chart_chartsales_messages",
        "com/openbravo/reports/sales_chart_piesalescat_messages",
        "com/openbravo/reports/sales_chart_productsales_messages",
        "com/openbravo/reports/sales_chart_timeseriesproduct_messages",
        "com/openbravo/reports/sales_chart_top10sales_messages",
        "com/openbravo/reports/sales_closedpos_messages",
        "com/openbravo/reports/sales_closedproducts_1_messages",
        "com/openbravo/reports/sales_closedproducts_messages",
        "com/openbravo/reports/sales_extendedcashregisterlog_messages",
        "com/openbravo/reports/sales_extproducts_messages",
        "com/openbravo/reports/sales_paymentreport_messages",
        "com/openbravo/reports/sales_productsalesprofit_messages",
        "com/openbravo/reports/sales_saletaxes_messages",
        "com/openbravo/reports/sales_taxcatsales_messages",
        "com/openbravo/reports/sales_taxes_messages",
        "com/openbravo/reports/suppliers_messages",
        "com/openbravo/reports/tools_badprice_messages",
        "com/openbravo/reports/tools_invalidcategory_messages",
        "com/openbravo/reports/tools_invaliddata_messages",
        "com/openbravo/reports/tools_inventoryqtyupdate_messages",
        "com/openbravo/reports/tools_missingdata_messages",
        "com/openbravo/reports/tools_newproducts_messages",
        "com/openbravo/reports/tools_updatedprices_messages",
        "com/openbravo/reports/usernosales_messages",
        "com/openbravo/reports/usersales_messages",
        "com/openbravo/reports/users_messages",
        "com/openbravo/reports/usertips_messages",
        "com/openbravo/reports/uservoids_messages",
        "com/openbravo/reports/vouchers_messages"
    };

    /**
     * Default constructor required by GraalVM Native Image {@link Feature} loader.
     */
    public NativeReflectionConfiguration() {
        // Instantiated by GraalVM native-image builder
    }

    @Override
    public String getDescription() {
        return "KriolOS POS GraalVM Native Image runtime class initialization and configuration";
    }

    @Override
    public void beforeAnalysis(BeforeAnalysisAccess access) {
        // StringUtils holds a static java.util.Random instance (cardrandom) which
        // must be initialized at runtime rather than baked into the native image heap.
        RuntimeClassInitialization.initializeAtRunTime(com.openbravo.util.StringUtils.class);

        // Defer NetBeans OpenIDE Lookup ActiveQueue (starts a daemon thread in <clinit>)
        RuntimeClassInitialization.initializeAtRunTime("org.openide");

        // Defer FlatLaf native library loading to runtime
        RuntimeClassInitialization.initializeAtRunTime("com.formdev.flatlaf.util.NativeLibrary");

        // Defer PDFBox encryption classes that optionally reference BouncyCastle
        RuntimeClassInitialization.initializeAtRunTime("org.apache.pdfbox");

        // Defer InstanceManager and Java RMI to runtime (uses RMI / SecureRandom for single-instance check)
        RuntimeClassInitialization.initializeAtRunTime(
                "com.openbravo.pos.instance",
                "java.rmi",
                "sun.rmi"
        );

        // Defer hardware native libraries (nrjavaserial & usb4java) so embedded .so/.dll
        // extraction and System.load() execute at runtime rather than image build time.
        RuntimeClassInitialization.initializeAtRunTime(
                "gnu.io",
                "org.usb4java",
                "javax.usb"
        );

        // Register hardware JNI & reflection classes, methods, and fields for native callbacks
        for (String className : JNI_HARDWARE_CLASSES) {
            Class<?> clazz = access.findClassByName(className);
            if (clazz != null) {
                RuntimeReflection.register(clazz);
                RuntimeReflection.register(clazz.getDeclaredConstructors());
                RuntimeReflection.register(clazz.getDeclaredFields());
                RuntimeJNIAccess.register(clazz);
                RuntimeJNIAccess.register(clazz.getDeclaredConstructors());
                RuntimeJNIAccess.register(clazz.getDeclaredFields());
                for (Method method : clazz.getDeclaredMethods()) {
                    // Skip Windows-only JNA WinReg method in RXTXCommDriver when JNA WinReg is absent
                    if ("windowsGetSerialPortsFromRegistry".equals(method.getName())) {
                        continue;
                    }
                    RuntimeReflection.register(method);
                    RuntimeJNIAccess.register(method);
                }
            }
        }

        // Dynamically discover and register all FlatLaf, SwingX, and SPI service providers
        registerClasspathComponents(access);

        // Register JDK and Swing classes dynamically accessed by BeanShell scripts
        registerBeanShellRuntimeTargets(access);

        // Register JasperReports & report resource bundles for native runtime lookup
        Module module = NativeReflectionConfiguration.class.getModule();
        for (String bundleName : REPORT_MESSAGE_BUNDLES) {
            try {
                RuntimeResourceAccess.addResourceBundle(module, bundleName);
            } catch (Throwable ignored) {
            }
        }

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
    }

    private void registerClasspathComponents(BeforeAnalysisAccess access) {
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
                    if (name.endsWith(".class") && isMatch(name)) {
                        String className = name.substring(0, name.length() - 6).replace('/', '.');
                        registerClassForReflection(access, className);
                    }
                }
            } catch (Exception ignored) {
            }
        }
    }

    private boolean isMatch(String name) {
        // Exclude obsolete AWT/Servlet utilities from BeanShell that reference removed JDK methods (e.g. Component.getPeer())
        if (name.startsWith("bsh/util/") || name.startsWith("bsh/servlet/")) {
            return false;
        }
        for (String prefix : CLASS_PATH_START_WITH) {
            if (name.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    private void registerClassForReflection(BeforeAnalysisAccess access, String className) {
        try {
            Class<?> clazz = access.findClassByName(className);
            if (clazz != null) {
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
                        if ("windowsGetSerialPortsFromRegistry".equals(method.getName())) {
                            continue;
                        }
                        RuntimeReflection.register(method);
                    }
                } catch (Throwable ignored) {
                }
            }
        } catch (Throwable ignored) {
        }
    }

    private void registerBeanShellRuntimeTargets(BeforeAnalysisAccess access) {
        Class<?>[] beanShellJdkClasses = {
            java.lang.System.class,
            java.io.PrintStream.class,
            java.lang.Math.class,
            javax.swing.JOptionPane.class,
            javax.swing.UIManager.class,
            javax.swing.JLabel.class,
            javax.swing.JDialog.class,
            javax.swing.plaf.FontUIResource.class,
            java.awt.Font.class,
            java.awt.Dimension.class,
            java.awt.Window.class,
            com.openbravo.pos.payment.JPaymentCashPos.class,
            com.openbravo.pos.payment.JPaymentCashPos.ScriptPaymentCash.class,
            com.openbravo.format.Formats.class,
            com.openbravo.pos.reports.PanelReportBean.class,
            com.openbravo.pos.reports.JPanelReport.class,
            com.openbravo.pos.reports.InventoryReportBean.class,
            com.openbravo.pos.reports.JParamsCustomer.class,
            com.openbravo.pos.reports.JParamsDatesInterval.class,
            com.openbravo.pos.reports.JParamsLocation.class,
            com.openbravo.pos.reports.JParamsLocationWithFirst.class,
            com.openbravo.pos.reports.JParamsReason.class,
            com.openbravo.pos.reports.JParamsSuppliers.class,
            com.openbravo.pos.reports.JParamsText.class,
            com.openbravo.pos.reports.JParamsUser.class,
            com.openbravo.pos.reports.JParamsComposed.class,
            com.openbravo.pos.reports.ReportEditorCreator.class,
            com.openbravo.pos.ticket.CategoryFilter.class,
            com.openbravo.pos.ticket.ProductFilter.class,
            com.openbravo.pos.ticket.ProductFilter1.class,
            com.openbravo.pos.ticket.ProductFilter2.class,
            com.openbravo.pos.ticket.ProductFilter3.class,
            com.openbravo.pos.ticket.ProductFilter4.class,
            com.openbravo.beans.DateUtils.class,
            com.openbravo.data.loader.Datas.class
        };
        for (Class<?> clazz : beanShellJdkClasses) {
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
                RuntimeReflection.register(clazz.getMethods());
                RuntimeReflection.register(clazz.getDeclaredMethods());
            } catch (Throwable ignored) {
            }
        }
    }
}
