package com.openbravo.pos.reports;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.*;
import com.openbravo.format.Formats;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Implementation of FinancialReportService.
 */
public class FinancialReportServiceImpl implements FinancialReportService {

    private final Session session;

    public FinancialReportServiceImpl(Session session) {
        this.session = session;
    }

    @Override
    public FinancialReport getFinancialReport(String money, Date dateStart, Date dateEnd) throws BasicException {
        FinancialReport report = new FinancialReport();
        report.setDateStart(dateStart);
        report.setDateEnd(dateEnd);

        // 1. Payments (Count, SUM)
        Object[] valtickets = (Object[]) new StaticSentence(session,
                "SELECT COUNT(*), SUM(payments.TOTAL) "
                        + "FROM payments, receipts "
                        + "WHERE payments.RECEIPT = receipts.ID AND receipts.MONEY = ?",
                SerializerWriteString.INSTANCE,
                new SerializerReadBasic(new Datas[] { Datas.INT, Datas.DOUBLE }))
                .find(money);

        if (valtickets == null) {
            report.setPaymentCount(0);
            report.setPaymentTotal(0.0);
        } else {
            report.setPaymentCount((Integer) valtickets[0]);
            report.setPaymentTotal((Double) valtickets[1]);
        }

        // 2. Payment Lines
        List<PaymentsListLine> paymentLines = new StaticSentence(session,
                "SELECT payments.PAYMENT, SUM(payments.TOTAL), payments.NOTES, COUNT(payments.PAYMENT) "
                        + "FROM payments, receipts "
                        + "WHERE payments.RECEIPT = receipts.ID AND receipts.MONEY = ? "
                        + "GROUP BY payments.PAYMENT, payments.NOTES",
                SerializerWriteString.INSTANCE,
                new SerializerReadClass(PaymentsListLine.class))
                .list(money);

        report.setPaymentLines(paymentLines != null ? paymentLines : new ArrayList<>());

        // 3. Category Sales Summary
        Object[] valcategorysales = (Object[]) new StaticSentence(session,
                "SELECT COUNT(*), "
                        + "SUM(ticketlines.UNITS), "
                        + "SUM((ticketlines.PRICE + ticketlines.PRICE * taxes.RATE ) * ticketlines.UNITS) "
                        + "FROM ticketlines, tickets, receipts, taxes "
                        + "WHERE ticketlines.TICKET = tickets.ID AND tickets.ID = receipts.ID "
                        + "AND ticketlines.TAXID = taxes.ID "
                        + "AND ticketlines.PRODUCT IS NOT NULL "
                        + "AND receipts.MONEY = ? "
                        + "GROUP BY receipts.MONEY",
                SerializerWriteString.INSTANCE,
                new SerializerReadBasic(new Datas[] { Datas.INT, Datas.DOUBLE, Datas.DOUBLE }))
                .find(money);

        if (valcategorysales == null) {
            report.setCategorySalesRows(0);
            report.setCategorySalesTotalUnits(0.0);
            report.setCategorySalesTotal(0.0);
        } else {
            report.setCategorySalesRows((Integer) valcategorysales[0]);
            report.setCategorySalesTotalUnits((Double) valcategorysales[1]);
            report.setCategorySalesTotal((Double) valcategorysales[2]);
        }

        // 4. Category Sales Lines
        List<CategorySalesLine> categorys = new StaticSentence(session,
                "SELECT a.NAME, sum(c.UNITS), sum(c.UNITS * (c.PRICE + (c.PRICE * d.RATE))) "
                        + "FROM categories as a "
                        + "LEFT JOIN products as b on a.id = b.CATEGORY "
                        + "LEFT JOIN ticketlines as c on b.id = c.PRODUCT "
                        + "LEFT JOIN taxes as d on c.TAXID = d.ID "
                        + "LEFT JOIN receipts as e on c.TICKET = e.ID "
                        + "WHERE e.MONEY = ? "
                        + "GROUP BY a.NAME",
                SerializerWriteString.INSTANCE,
                new SerializerReadClass(CategorySalesLine.class))
                .list(money);

        report.setCategorySalesLines(categorys != null ? categorys : new ArrayList<>());

        // 5. Sales Summary
        Object[] recsales = (Object[]) new StaticSentence(session,
                "SELECT COUNT(DISTINCT receipts.ID), SUM(ticketlines.UNITS * ticketlines.PRICE) "
                        + "FROM receipts, ticketlines "
                        + "WHERE receipts.ID = ticketlines.TICKET AND receipts.MONEY = ?",
                SerializerWriteString.INSTANCE,
                new SerializerReadBasic(new Datas[] { Datas.INT, Datas.DOUBLE }))
                .find(money);

        if (recsales == null) {
            report.setSalesCount(0);
            report.setSalesBase(0.0);
        } else {
            report.setSalesCount((Integer) recsales[0]);
            report.setSalesBase((Double) recsales[1]);
        }

        // 6. Taxes Summary
        Object[] rectaxes = (Object[]) new StaticSentence(session,
                "SELECT SUM(taxlines.AMOUNT), SUM(taxlines.BASE) "
                        + "FROM receipts, taxlines "
                        + "WHERE receipts.ID = taxlines.RECEIPT AND receipts.MONEY = ?",
                SerializerWriteString.INSTANCE,
                new SerializerReadBasic(new Datas[] { Datas.DOUBLE, Datas.DOUBLE }))
                .find(money);

        if (rectaxes == null) {
            report.setSalesTaxes(0.0);
        } else {
            report.setSalesTaxes((Double) rectaxes[0]);
        }

        // 7. Sales Lines (Taxes breakdown)
        List<SalesLine> asales = new StaticSentence(session,
                "SELECT taxcategories.NAME, SUM(taxlines.AMOUNT), SUM(taxlines.BASE), SUM(taxlines.BASE + taxlines.AMOUNT) "
                        + "FROM receipts, taxlines, taxes, taxcategories "
                        + "WHERE receipts.ID = taxlines.RECEIPT AND taxlines.TAXID = taxes.ID AND taxes.CATEGORY = taxcategories.ID "
                        + "AND receipts.MONEY = ?"
                        + "GROUP BY taxcategories.NAME",
                SerializerWriteString.INSTANCE,
                new SerializerReadClass(SalesLine.class))
                .list(money);

        report.setSalesLines(asales != null ? asales : new ArrayList<>());

        // 8. Removed Lines
        String startDateFormatted = Formats.DATETIME.formatValue(dateStart);
        List<RemovedProductLines> removedLines = new StaticSentence(session,
                "SELECT lineremoved.NAME, lineremoved.TICKETID, lineremoved.PRODUCTNAME, SUM(lineremoved.UNITS) AS TOTAL_UNITS  "
                        + "FROM lineremoved "
                        + "WHERE lineremoved.REMOVEDDATE > ? "
                        + "GROUP BY lineremoved.NAME, lineremoved.TICKETID, lineremoved.PRODUCTNAME",
                SerializerWriteString.INSTANCE,
                new SerializerReadClass(RemovedProductLines.class))
                .list(startDateFormatted);

        report.setRemovedProductLines(removedLines != null ? removedLines : new ArrayList<>());

        // 9. Drawer Opened Lines
        List<DrawerOpenedLines> drawerOpenedLines = new StaticSentence(session,
                "SELECT OPENDATE, NAME, TICKETID  "
                        + "FROM draweropened "
                        + "WHERE TICKETID = 'No Sale' AND OPENDATE > ? "
                        + "GROUP BY NAME, OPENDATE, TICKETID",
                SerializerWriteString.INSTANCE,
                new SerializerReadClass(DrawerOpenedLines.class))
                .list(startDateFormatted);

        report.setDrawerOpenedLines(drawerOpenedLines != null ? drawerOpenedLines : new ArrayList<>());

        // 10. Product Sales Summary
        Object[] valproductsales = (Object[]) new StaticSentence(session,
                "SELECT COUNT(*), SUM(ticketlines.UNITS), "
                        + "SUM((ticketlines.PRICE + ticketlines.PRICE * taxes.RATE ) * ticketlines.UNITS) "
                        + "FROM ticketlines, tickets, receipts, taxes "
                        + "WHERE ticketlines.TICKET = tickets.ID "
                        + "AND tickets.ID = receipts.ID "
                        + "AND ticketlines.TAXID = taxes.ID "
                        + "AND ticketlines.PRODUCT IS NOT NULL "
                        + "AND receipts.MONEY = ? "
                        + "GROUP BY receipts.MONEY",
                SerializerWriteString.INSTANCE,
                new SerializerReadBasic(new Datas[] { Datas.INT, Datas.DOUBLE, Datas.DOUBLE }))
                .find(money);

        if (valproductsales == null) {
            report.setProductSalesRows(0);
            report.setProductSalesTotalUnits(0.0);
            report.setProductSalesTotal(0.0);
        } else {
            report.setProductSalesRows((Integer) valproductsales[0]);
            report.setProductSalesTotalUnits((Double) valproductsales[1]);
            report.setProductSalesTotal((Double) valproductsales[2]);
        }

        // 11. Product Sales Lines
        List<ProductSalesLine> products = new StaticSentence(session,
                "SELECT products.NAME, SUM(ticketlines.UNITS), ticketlines.PRICE, taxes.RATE "
                        + "FROM ticketlines, tickets, receipts, products, taxes "
                        + "WHERE ticketlines.PRODUCT = products.ID "
                        + "AND ticketlines.TICKET = tickets.ID "
                        + "AND tickets.ID = receipts.ID "
                        + "AND ticketlines.TAXID = taxes.ID "
                        + "AND receipts.MONEY = ? "
                        + "GROUP BY products.NAME, ticketlines.PRICE, taxes.RATE",
                SerializerWriteString.INSTANCE,
                new SerializerReadClass(ProductSalesLine.class))
                .list(money);

        report.setProductSalesLines(products != null ? products : new ArrayList<>());

        return report;
    }
}
