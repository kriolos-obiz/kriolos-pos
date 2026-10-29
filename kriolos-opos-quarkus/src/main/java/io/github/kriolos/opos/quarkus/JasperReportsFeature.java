package io.github.kriolos.opos.quarkus;

import io.quarkus.runtime.annotations.RegisterForReflection;
import io.quarkus.runtime.annotations.RegisterResourceBundle;
import io.quarkus.runtime.annotations.RegisterResources;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.graalvm.nativeimage.hosted.Feature;
import org.graalvm.nativeimage.hosted.RuntimeClassInitialization;
import org.graalvm.nativeimage.hosted.RuntimeReflection;
import org.graalvm.nativeimage.hosted.RuntimeResourceAccess;

/**
 * Native configuration and Feature for JasperReports 7 engine, viewers, and report templates.
 *
 * @author KriolOS
 */
@RegisterResourceBundle(bundleName = "net/sf/jasperreports/view/viewer")
@RegisterResourceBundle(bundleName = "jasperreports_messages")
@RegisterResources(globs = {
    "com/openbravo/reports/**",
    "net/sf/jasperreports/**",
    "default.jasperreports.properties",
    "jasperreports*.properties"
})
@RegisterForReflection(
    targets = {
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
        com.openbravo.pos.reports.JRViewer400.class,
        com.openbravo.pos.reports.PrintReportUtils.class,
        com.openbravo.pos.reports.JRDataSourceBasic.class,
        com.openbravo.pos.reports.ReportFieldsArray.class,
        com.openbravo.pos.ticket.CategoryFilter.class,
        com.openbravo.pos.ticket.ProductFilter.class,
        com.openbravo.pos.ticket.ProductFilter1.class,
        com.openbravo.pos.ticket.ProductFilter2.class,
        com.openbravo.pos.ticket.ProductFilter3.class,
        com.openbravo.pos.ticket.ProductFilter4.class,

        // ── Velocity / JasperReports Template Models ─────────────────────────
        com.openbravo.pos.reports.CashReport.class,
        com.openbravo.pos.reports.PaymentsListLine.class,
        com.openbravo.pos.reports.CategorySalesLine.class,
        com.openbravo.pos.reports.ProductSalesLine.class,
        com.openbravo.pos.reports.SalesLine.class,
        com.openbravo.pos.reports.RemovedProductLines.class,
        com.openbravo.pos.reports.DrawerOpenedLines.class,
        com.openbravo.pos.reports.FinancialReport.class
    },
    classNames = {
        // ── JasperReports 7 Core Extension Factories & XML Loaders ───────────
        "com.openbravo.pos.reports.JRViewer400",
        "com.openbravo.pos.reports.PrintReportUtils",
        "com.openbravo.pos.reports.JRDataSourceBasic",
        "com.openbravo.pos.reports.ReportFieldsArray",
        "net.sf.jasperreports.extensions.DefaultExtensionsRegistryFactory",
        "net.sf.jasperreports.engine.xml.JacksonReportLoader",
        "net.sf.jasperreports.engine.xml.JacksonReportWriterFactory",
        "net.sf.jasperreports.engine.design.JasperDesign",
        "net.sf.jasperreports.engine.JasperReport",
        "net.sf.jasperreports.engine.JasperPrint",
        "net.sf.jasperreports.governors.GovernorExtensionsRegistryFactory",
        "net.sf.jasperreports.components.ComponentsExtensionsRegistryFactory",
        "net.sf.jasperreports.parts.PartComponentsExtensionsRegistryFactory",
        "net.sf.jasperreports.repo.DefaultRepositoryExtensionsRegistryFactory",
        "net.sf.jasperreports.engine.style.PropertyStyleProviderExtensionFactory",
        "net.sf.jasperreports.web.util.ContentTypeMappingExtensionsRegistryFactory",
        "net.sf.jasperreports.engine.fonts.SimpleFontExtensionsRegistryFactory",
        "net.sf.jasperreports.engine.export.MatcherExportFilterMappingExtensionsRegistryFactory",

        // ── Woodstox StAX Provider Classes ──────────────────────────────────
        "com.ctc.wstx.stax.WstxInputFactory",
        "com.ctc.wstx.stax.WstxOutputFactory",
        "com.ctc.wstx.stax.WstxEventFactory",
        "org.codehaus.stax2.XMLInputFactory2",
        "org.codehaus.stax2.XMLOutputFactory2"
    },
    methods = true,
    fields = true
)
public final class JasperReportsFeature implements Feature {

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

    private static final String[] JASPER_PACKAGES = {
        "com/ctc/wstx/stax/",
        "com/ctc/wstx/api/",
        "com/ctc/wstx/io/",
        "com/ctc/wstx/sr/",
        "com/ctc/wstx/sw/",
        "com/ctc/wstx/cfg/",
        "com/ctc/wstx/util/",
        "com/ctc/wstx/ent/",
        "com/ctc/wstx/dtd/",
        "org/codehaus/stax2/",
        "net/sf/jasperreports/charts/",
        "net/sf/jasperreports/components/",
        "net/sf/jasperreports/crosstabs/",
        "net/sf/jasperreports/jackson/"
    };

    public JasperReportsFeature() {
        // Required default constructor for GraalVM Feature SPI
    }

    @Override
    public String getDescription() {
        return "JasperReports 7 engine, resource bundles, and viewer reflection configuration";
    }

    @Override
    public void beforeAnalysis(BeforeAnalysisAccess access) {
        // Defer JasperReports engine to runtime (prevents ElementsBlock Random in image heap)
        RuntimeClassInitialization.initializeAtRunTime("net.sf.jasperreports");

        // Defer Log4jApiLogFactory to runtime (references optional log4j-api not on classpath);
        // permit Slf4jLogFactory at build-time (cached by Commons BeanUtils in image heap)
        RuntimeClassInitialization.initializeAtRunTime("org.apache.commons.logging.impl.Log4jApiLogFactory");
        RuntimeClassInitialization.initializeAtBuildTime(
            "org.apache.commons.logging.impl.Slf4jLogFactory",
            "org.apache.commons.logging.impl.Slf4jLogFactory$Slf4jLocationAwareLog"
        );

        // Defer Batik Rhino interpreter to runtime, allow its factory at build time (cached in Batik Service.providerMap heap)
        RuntimeClassInitialization.initializeAtRunTime(
            "org.apache.batik.bridge.RhinoInterpreter",
            "org.apache.batik.script.rhino"
        );
        RuntimeClassInitialization.initializeAtBuildTime("org.apache.batik.bridge.RhinoInterpreterFactory");

        Module module = JasperReportsFeature.class.getModule();
        int bundleCount = 0;
        for (String bundleName : REPORT_MESSAGE_BUNDLES) {
            try {
                RuntimeResourceAccess.addResourceBundle(module, bundleName);
                bundleCount++;
            } catch (Throwable ignored) {
            }
        }

        // Dynamically discover and register Woodstox and JasperReports classes
        int scannedCount = registerClasspathComponents(access);
        System.out.printf("[JasperReportsFeature] Registered %d report message bundles, scanned %d Woodstox/JasperReports classes for reflection.%n",
            bundleCount, scannedCount);
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
                        if (registerClass(access, className)) {
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
        if (name.startsWith("com/ctc/wstx/shaded/") || name.startsWith("com/ctc/wstx/msv/")) {
            return false;
        }
        for (String prefix : JASPER_PACKAGES) {
            if (name.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    private boolean registerClass(BeforeAnalysisAccess access, String className) {
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
