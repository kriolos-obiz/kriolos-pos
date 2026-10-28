/*
 * Copyright (C) 2026 KriolOS
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
package com.openbravo.pos.menu;

import com.openbravo.pos.domain.navigation.Menu;

/**
 *
 * @author dev
 */
public class MenuStatic {

    public static void setMenu(Menu menu) {

// GROUP MAIN 
        var group = menu.addGroup("Menu.Main");
        group.addPanel("/com/openbravo/images/sale.png", "Menu.Ticket", com.openbravo.pos.sales.JPanelTicketSales.class.getName());
        group.addPanel("/com/openbravo/images/saleedit.png", "Menu.TicketEdit", com.openbravo.pos.sales.JPanelTicketEdits.class.getName());
        group.addPanel("/com/openbravo/images/customerpay.png", "Menu.CustomersPayment", com.openbravo.pos.customers.CustomersPayment.class.getName());
        group.addPanel("/com/openbravo/images/payments.png", "Menu.Payments", com.openbravo.pos.panels.JPanelPayments.class.getName());
        group.addPanel("/com/openbravo/images/calculator.png", "Menu.CloseTPV", com.openbravo.pos.panels.JPanelCloseMoney.class.getName());

// GROUP BACKOFFICE 
        group = menu.addGroup("Menu.Backoffice");
        var submenu = group.addSubmenu("/com/openbravo/images/customer.png", "Menu.Customers", "com.openbravo.pos.forms.MenuCustomers");
        submenu.addTitle("Menu.Panel.Customers.Forms");
        submenu.addPanel("/com/openbravo/images/customer.png", "Menu.CustomersManagement", com.openbravo.pos.customers.CustomersPanel.class.getName());
        submenu.addTitle("Menu.Panel.Customers.Reports");
        submenu.addPanel("/com/openbravo/images/reports.png", "Menu.Customers.Reports.Report", "/com/openbravo/reports/customers.bs");
        submenu.addPanel("/com/openbravo/images/reports.png", "Menu.Customers.Reports.Sales", "/com/openbravo/reports/customers_sales.bs");
        submenu.addPanel("/com/openbravo/images/reports.png", "Menu.Customers.Reports.Debtors", "/com/openbravo/reports/customers_debtors.bs");
        submenu.addPanel("/com/openbravo/images/reports.png", "Menu.Customers.Reports.Diary", "/com/openbravo/reports/customers_diary.bs");
        submenu.addPanel("/com/openbravo/images/reports.png", "Menu.Customers.Reports.Cards", "/com/openbravo/reports/customers_cards.bs");
        submenu.addPanel("/com/openbravo/images/reports.png", "Menu.Customers.Reports.List", "/com/openbravo/reports/customers_list.bs");
        submenu.addPanel("/com/openbravo/images/reports.png", "Menu.Customers.Reports.Export", "/com/openbravo/reports/customers_export.bs");
        submenu.addPanel("/com/openbravo/images/reports.png", "Menu.Customers.Reports.Vouchers", "/com/openbravo/reports/customers_vouchers.bs");

// GROUP SUPPLIERS 
        submenu = group.addSubmenu("/com/openbravo/images/stockmaint.png", "Menu.Suppliers", "com.openbravo.pos.forms.MenuSuppliers");
        submenu.addTitle("Menu.Panel.Suppliers.Forms");
        submenu.addPanel("/com/openbravo/images/customer.png", "Menu.SuppliersManagement", com.openbravo.pos.suppliers.SuppliersPanel.class.getName());
        submenu.addTitle("Menu.Panel.Suppliers.Reports");
        submenu.addPanel("/com/openbravo/images/reports.png", "Menu.Suppliers.Reports.Report", "/com/openbravo/reports/suppliers.bs");
        submenu.addPanel("/com/openbravo/images/reports.png", "Menu.Suppliers.Reports.Products", "/com/openbravo/reports/suppliers_products.bs");
        submenu.addPanel("/com/openbravo/images/reports.png", "Menu.Suppliers.Reports.Creditors", "/com/openbravo/reports/suppliers_creditors.bs");
        submenu.addPanel("/com/openbravo/images/reports.png", "Menu.Suppliers.Reports.Diary", "/com/openbravo/reports/suppliers_diary.bs");
        submenu.addPanel("/com/openbravo/images/reports.png", "Menu.Suppliers.Reports.Sales", "/com/openbravo/reports/suppliers_sales.bs");
        submenu.addPanel("/com/openbravo/images/reports.png", "Menu.Suppliers.Reports.List", "/com/openbravo/reports/suppliers_list.bs");
        submenu.addPanel("/com/openbravo/images/reports.png", "Menu.Suppliers.Reports.Export", "/com/openbravo/reports/suppliers_export.bs");

// GROUP STOCK 
        submenu = group.addSubmenu("/com/openbravo/images/products.png", "Menu.StockManagement", "com.openbravo.pos.forms.MenuStockManagement");
        submenu.addTitle("Menu.Panel.Stock.Forms");

        submenu.addPanel("/com/openbravo/images/category.png", "Menu.Categories", com.openbravo.pos.inventory.CategoriesPanel.class.getName());
        submenu.addPanel("/com/openbravo/images/products.png", "Menu.Products", com.openbravo.pos.inventory.ProductsPanel.class.getName());
        submenu.addPanel("/com/openbravo/images/location.png", "Menu.ProductsWarehouse", com.openbravo.pos.inventory.ProductsWarehousePanel.class.getName());
        submenu.addPanel("/com/openbravo/images/stockmaint.png", "Menu.StockMovement", com.openbravo.pos.inventory.StockManagement.class.getName());
        submenu.addPanel("/com/openbravo/images/auxiliary.png", "Menu.Auxiliar", com.openbravo.pos.inventory.AuxiliarPanel.class.getName());
        submenu.addPanel("/com/openbravo/images/bundle.png", "Menu.Bundle", com.openbravo.pos.inventory.BundlePanel.class.getName());
        submenu.addPanel("/com/openbravo/images/attributes.png", "Menu.Attributes", com.openbravo.pos.inventory.AttributesPanel.class.getName());
        submenu.addPanel("/com/openbravo/images/attributes.png", "Menu.AttributeValues", com.openbravo.pos.inventory.AttributeValuesPanel.class.getName());
        submenu.addPanel("/com/openbravo/images/attributes.png", "Menu.AttributeSets", com.openbravo.pos.inventory.AttributeSetsPanel.class.getName());
        submenu.addPanel("/com/openbravo/images/attributes.png", "Menu.AttributeUse", com.openbravo.pos.inventory.AttributeUsePanel.class.getName());
        submenu.addPanel("/com/openbravo/images/products.png", "Menu.Uom", com.openbravo.pos.inventory.UomPanel.class.getName());

        submenu.addTitle("Menu.Panel.Stock.Reports");
        submenu.addPanel("/com/openbravo/images/reports.png", "Menu.Inventory", "/com/openbravo/reports/inventory.bs");
        submenu.addPanel("/com/openbravo/images/reports.png", "Menu.Inventory2", "/com/openbravo/reports/inventoryb.bs");
        submenu.addPanel("/com/openbravo/images/reports.png", "Menu.Inventory.Reports.Diary", "/com/openbravo/reports/inventory_diary.bs");
        submenu.addPanel("/com/openbravo/images/reports.png", "Menu.InventoryBroken", "/com/openbravo/reports/inventorybroken.bs");
        submenu.addPanel("/com/openbravo/images/reports.png", "Menu.InventoryDiff", "/com/openbravo/reports/inventorydiff.bs");
        submenu.addPanel("/com/openbravo/images/reports.png", "Menu.InventoryDiffDetail", "/com/openbravo/reports/inventorydiffdetail.bs");
        submenu.addPanel("/com/openbravo/images/reports.png", "Menu.InventoryListDetail", "/com/openbravo/reports/inventorylistdetail.bs");
        submenu.addPanel("/com/openbravo/images/reports.png", "Menu.Products", "/com/openbravo/reports/products.bs");
        submenu.addPanel("/com/openbravo/images/reports.png", "Menu.ProductCatalog", "/com/openbravo/reports/productscatalog.bs");
        submenu.addPanel("/com/openbravo/images/reports.png", "Menu.SaleCatalog", "/com/openbravo/reports/salecatalog.bs");
        submenu.addPanel("/com/openbravo/images/reports.png", "Menu.ProductLabels", "/com/openbravo/reports/productlabels.bs");
        submenu.addPanel("/com/openbravo/images/reports.png", "Menu.BarcodeSheet", "/com/openbravo/reports/barcode_sheet.bs");
        submenu.addPanel("/com/openbravo/images/reports.png", "Menu.BarcodeSheet_JM", "/com/openbravo/reports/barcode_sheet_jm.bs");
        submenu.addPanel("/com/openbravo/images/reports.png", "Menu.ShelfEdgeLabels", "/com/openbravo/reports/barcode_shelfedgelabels.bs");

// GROUP SALES 
        submenu = group.addSubmenu("/com/openbravo/images/sales.png", "Menu.SalesManagement", "com.openbravo.pos.forms.MenuSalesManagement");
        submenu.addTitle("Menu.Panel.Sales.Reports");
        submenu.addPanel("/com/openbravo/images/reports.png", "Menu.Closing", "/com/openbravo/reports/sales_closedpos.bs");
        submenu.addPanel("/com/openbravo/images/reports.png", "Menu.Closing1", "/com/openbravo/reports/sales_closedpos_1.bs");
        submenu.addPanel("/com/openbravo/images/reports.png", "Menu.CashRegisterLog", "/com/openbravo/reports/sales_cashregisterlog.bs");
        submenu.addPanel("/com/openbravo/images/reports.png", "Menu.ExtendedCashRegisterLog", "/com/openbravo/reports/sales_extendedcashregisterlog.bs");
        submenu.addPanel("/com/openbravo/images/reports.png", "Menu.CashFlow", "/com/openbravo/reports/sales_cashflow.bs");
        submenu.addPanel("/com/openbravo/images/reports.png", "Menu.PaymentReport", "/com/openbravo/reports/sales_paymentreport.bs");
        submenu.addPanel("/com/openbravo/images/reports.png", "Menu.CategorySales", "/com/openbravo/reports/sales_categorysales.bs");
        submenu.addPanel("/com/openbravo/images/reports.png", "Menu.CategorySales1", "/com/openbravo/reports/sales_categorysales_1.bs");
        submenu.addPanel("/com/openbravo/images/reports.png", "Menu.ClosedProducts", "/com/openbravo/reports/sales_closedproducts.bs");
        submenu.addPanel("/com/openbravo/images/reports.png", "Menu.ClosedProducts1", "/com/openbravo/reports/sales_closedproducts_1.bs");
        submenu.addPanel("/com/openbravo/images/reports.png", "Menu.ExtendedByProducts", "/com/openbravo/reports/sales_extproducts.bs");
        submenu.addPanel("/com/openbravo/images/reports.png", "Menu.SalesProfit", "/com/openbravo/reports/sales_productsalesprofit.bs");
        submenu.addPanel("/com/openbravo/images/reports.png", "Menu.SaleTaxes", "/com/openbravo/reports/sales_saletaxes.bs");
        submenu.addPanel("/com/openbravo/images/reports.png", "Menu.TaxCatSales", "/com/openbravo/reports/sales_taxcatsales.bs");
        submenu.addPanel("/com/openbravo/images/reports.png", "Menu.ReportTaxes", "/com/openbravo/reports/sales_taxes.bs");
        submenu.addTitle("Menu.Panel.Sales.Charts");
        submenu.addPanel("/com/openbravo/images/chart.png", "Menu.ProductSales", "/com/openbravo/reports/sales_chart_productsales.bs");
        submenu.addPanel("/com/openbravo/images/chart.png", "Menu.ProductCategorySalesPieChart", "/com/openbravo/reports/sales_chart_piesalescat.bs");
        submenu.addPanel("/com/openbravo/images/chart.png", "Menu.SalesChart", "/com/openbravo/reports/sales_chart_chartsales.bs");
        submenu.addPanel("/com/openbravo/images/chart.png", "Menu.TimeSeriesProduct", "/com/openbravo/reports/sales_chart_timeseriesproduct.bs");
        submenu.addPanel("/com/openbravo/images/chart.png", "Menu.Top10Sales", "/com/openbravo/reports/sales_chart_top10sales.bs");

// GROUP MAINTAIN 
        submenu = group.addSubmenu("/com/openbravo/images/maintain.png", "Menu.Maintenance", "com.openbravo.pos.forms.MenuMaintenance");
        submenu.addTitle("Menu.Panel.Maintenance.Forms");
        submenu.addPanel("/com/openbravo/images/resources.png", "Menu.Resources", com.openbravo.pos.admin.ResourcesPanel.class.getName());
        submenu.addPanel("/com/openbravo/images/roles.png", "Menu.Roles", com.openbravo.pos.admin.RolesPanel.class.getName());
        submenu.addPanel("/com/openbravo/images/user.png", "Menu.Users", com.openbravo.pos.admin.PeoplePanel.class.getName());
        submenu.addPanel("/com/openbravo/images/bookmark.png", "Menu.TaxCategories", com.openbravo.pos.inventory.TaxCategoriesPanel.class.getName());
        submenu.addPanel("/com/openbravo/images/bookmark.png", "Menu.TaxCustCategories", com.openbravo.pos.inventory.TaxCustCategoriesPanel.class.getName());
        submenu.addPanel("/com/openbravo/images/bookmark.png", "Menu.Taxes", com.openbravo.pos.inventory.TaxPanel.class.getName());
        submenu.addPanel("/com/openbravo/images/location.png", "Menu.Locations", com.openbravo.pos.inventory.LocationsPanel.class.getName());
        submenu.addPanel("/com/openbravo/images/floors.png", "Menu.Floors", com.openbravo.pos.sales.restaurant.JPanelFloors.class.getName());
        submenu.addPanel("/com/openbravo/images/tables.png", "Menu.Tables", com.openbravo.pos.sales.restaurant.JPanelPlaces.class.getName());
        submenu.addPanel("/com/openbravo/images/products.png", "Menu.Vouchers", com.openbravo.pos.voucher.VoucherPanel.class.getName());

        submenu.addTitle("Menu.Panel.Maintenance.Reports");
        submenu.addPanel("/com/openbravo/images/reports.png", "Menu.UsersReport", "/com/openbravo/reports/users.bs");
        submenu.addPanel("/com/openbravo/images/reports.png", "Menu.UserSells", "/com/openbravo/reports/usersales.bs");
        submenu.addPanel("/com/openbravo/images/reports.png", "Menu.UserNoSales", "/com/openbravo/reports/usernosales.bs");
        submenu.addPanel("/com/openbravo/images/reports.png", "Menu.UserVoids", "/com/openbravo/reports/uservoids.bs");
        submenu.addPanel("/com/openbravo/images/reports.png", "Menu.Customers.Reports.Vouchers", "/com/openbravo/reports/customers_vouchers.bs");

// EPM
        submenu = group.addSubmenu("/com/openbravo/images/users.png", "Menu.PresenceManagement", "com.openbravo.pos.forms.MenuEmployees");
        submenu.addTitle("Menu.PresenceManagement");
        submenu.addPanel("/com/openbravo/images/coffee.png", "Menu.Breaks", com.openbravo.pos.epm.BreaksPanel.class.getName());
        submenu.addPanel("/com/openbravo/images/leaves.png", "Menu.Leaves", com.openbravo.pos.epm.LeavesPanel.class.getName());
        submenu.addTitle("Menu.Employees.Reports");
        submenu.addPanel("/com/openbravo/images/reports.png", "Menu.EPM.DailyPresenceReport", "/com/openbravo/reports/epm_dailypresence.bs");
        submenu.addPanel("/com/openbravo/images/reports.png", "Menu.EPM.DailyScheduleReport", "/com/openbravo/reports/epm_dailyschedule.bs");
        submenu.addPanel("/com/openbravo/images/reports.png", "Menu.EPM.PerformanceReport", "/com/openbravo/reports/epm_performance.bs");

// UTILIES        
        group = menu.addGroup("Menu.Utilities");
        submenu = group.addSubmenu("/com/openbravo/images/utilities.png", "Menu.Tools", com.openbravo.pos.imports.JPanelCSV.class.getName());
        submenu.addTitle("Menu.Import");
        submenu.addPanel("/com/openbravo/images/import.png", "Menu.CSVImport", com.openbravo.pos.imports.JPanelCSVImport.class.getName());
        submenu.addPanel("/com/openbravo/images/import.png", "Menu.CSVStockQty", com.openbravo.pos.imports.StockQtyImport.class.getName());
        submenu.addPanel("/com/openbravo/images/import.png", "Menu.CustomerCSVImport", com.openbravo.pos.imports.CustomerCSVImport.class.getName());
        submenu.addPanel("/com/openbravo/images/database.png", "Menu.CSVReset", com.openbravo.pos.imports.JPanelCSVCleardb.class.getName());
        submenu.addPanel("/com/openbravo/images/database.png", "Menu.Transfer", "com.openbravo.pos.transfer.Transfer");
        submenu.addTitle("Menu.Import.Reports");
        submenu.addPanel("/com/openbravo/images/reports.png", "Menu.Tools.NewProducts", "/com/openbravo/reports/tools_newproducts.bs");
        submenu.addPanel("/com/openbravo/images/reports.png", "Menu.Tools.UpdatedPrices", "/com/openbravo/reports/tools_updatedprices.bs");
        submenu.addPanel("/com/openbravo/images/reports.png", "Menu.Tools.StockQtyUpdate", "/com/openbravo/reports/tools_inventoryqtyupdate.bs");
        submenu.addPanel("/com/openbravo/images/reports.png", "Menu.Tools.BadPrices", "/com/openbravo/reports/tools_badprice.bs");
        submenu.addPanel("/com/openbravo/images/reports.png", "Menu.Tools.InvalidCategory", "/com/openbravo/reports/tools_invalidcategory.bs");
        submenu.addPanel("/com/openbravo/images/reports.png", "Menu.Tools.MissingData", "/com/openbravo/reports/tools_missingdata.bs");
        submenu.addPanel("/com/openbravo/images/reports.png", "Menu.Tools.InvalidData", "/com/openbravo/reports/tools_invaliddata.bs");

// SYSTEM     
        group = menu.addGroup("Menu.System");
        group.addChangePasswordAction();
        group.addPanel("/com/openbravo/images/configuration.png", "Menu.Configuration", com.openbravo.pos.config.JPanelConfiguration.class.getName());
        group.addPanel("/com/openbravo/images/printer.png", "Menu.Printer", com.openbravo.pos.panels.JPanelPrinter.class.getName());
        group.addPanel("/com/openbravo/images/timer.png", "Menu.CheckInCheckOut", com.openbravo.pos.epm.JPanelEmployeePresence.class.getName());

        group.addExitAction();

    }
}
