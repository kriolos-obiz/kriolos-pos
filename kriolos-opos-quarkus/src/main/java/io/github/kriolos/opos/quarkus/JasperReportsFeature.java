package io.github.kriolos.opos.quarkus;

import io.quarkus.runtime.annotations.RegisterForReflection;
import io.quarkus.runtime.annotations.RegisterResourceBundle;
import io.quarkus.runtime.annotations.RegisterResources;
import java.lang.reflect.Method;
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
        "net.sf.jasperreports.engine.export.MatcherExportFilterMappingExtensionsRegistryFactory"
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

    public JasperReportsFeature() {
        // Required default constructor for GraalVM Feature SPI
    }

    @Override
    public String getDescription() {
        return "JasperReports 7 engine, resource bundles, and viewer reflection configuration";
    }

    @Override
    public void beforeAnalysis(BeforeAnalysisAccess access) {
        Module module = JasperReportsFeature.class.getModule();
        int bundleCount = 0;
        for (String bundleName : REPORT_MESSAGE_BUNDLES) {
            try {
                RuntimeResourceAccess.addResourceBundle(module, bundleName);
                bundleCount++;
            } catch (Throwable ignored) {
            }
        }
        System.out.printf("[JasperReportsFeature] Registered %d report message bundles and XML report loaders.%n", bundleCount);
    }
}
