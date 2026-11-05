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
package com.openbravo.pos.domain.audit;

import com.openbravo.pos.domain.utils.AmountCalculatorUtil;
import com.openbravo.pos.reports.FinancialReport;
import com.openbravo.pos.reports.PaymentsListLine;
import com.openbravo.pos.reports.SalesLine;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Factory for creating immutable, cryptographically verifiable {@link ClosedCashSnapshot}
 * records directly from a consolidated {@link FinancialReport}.
 *
 * @author poolborges
 */
public final class ClosedCashSnapshotFactory {

    private ClosedCashSnapshotFactory() {
        // Utility class
    }

    /**
     * Constructs a complete {@link ClosedCashSnapshot} and its embedded canonical JSON payload.
     *
     * @param closedCashId the session token (formerly money)
     * @param report the domain financial report
     * @param host terminal host name
     * @param sequence closure sequence number
     * @param operator username of the closing cashier
     * @param appVersion current application release version
     * @return fully populated, immutable ClosedCashSnapshot
     */
    public static ClosedCashSnapshot createFromFinancialReport(
            String closedCashId,
            FinancialReport report,
            String host,
            int sequence,
            String operator,
            String appVersion) {

        Date dateStart = report.getDateStart() != null ? report.getDateStart() : new Date();
        Date dateEnd = report.getDateEnd() != null ? report.getDateEnd() : new Date();

        BigDecimal netSales = AmountCalculatorUtil.toBigDecimal(report.getSalesBase(), 4);
        BigDecimal taxes = AmountCalculatorUtil.toBigDecimal(report.getSalesTaxes(), 4);
        BigDecimal grossSales = netSales.add(taxes);
        BigDecimal paymentsTotal = AmountCalculatorUtil.toBigDecimal(report.getPaymentTotal(), 4);

        // Map Taxes
        List<FinancialReportSnapshot.TaxSummary> taxSummaries = new ArrayList<>();
        if (report.getSalesLines() != null) {
            for (SalesLine line : report.getSalesLines()) {
                String taxId = line.getTaxName() != null ? line.getTaxName() : "UNKNOWN";
                BigDecimal taxAmt = AmountCalculatorUtil.toBigDecimal(line.getTaxes() != null ? line.getTaxes() : 0.0, 4);
                BigDecimal taxNet = AmountCalculatorUtil.toBigDecimal(line.getTaxNet() != null ? line.getTaxNet() : 0.0, 4);
                taxSummaries.add(new FinancialReportSnapshot.TaxSummary(taxId, taxId, BigDecimal.ZERO, taxNet, taxAmt));
            }
        }

        // Map Payments
        List<FinancialReportSnapshot.PaymentSummary> paymentSummaries = new ArrayList<>();
        if (report.getPaymentLines() != null) {
            for (PaymentsListLine line : report.getPaymentLines()) {
                String method = line.getType() != null ? line.getType() : "unknown";
                BigDecimal amount = AmountCalculatorUtil.toBigDecimal(line.getValue() != null ? line.getValue() : 0.0, 4);
                int count = line.getNumberOfEntries();
                paymentSummaries.add(new FinancialReportSnapshot.PaymentSummary(method, amount, count));
            }
        }

        // Create Payload DTO Record
        FinancialReportSnapshot snapshotPayload = new FinancialReportSnapshot(
                "1.0",
                closedCashId,
                host,
                sequence,
                dateStart.toInstant(),
                dateEnd.toInstant(),
                operator != null ? operator : "system",
                appVersion != null ? appVersion : "5.0",
                new FinancialReportSnapshot.FinancialTotals(grossSales, netSales, taxes, paymentsTotal),
                taxSummaries,
                paymentSummaries,
                FinancialReportSnapshot.DrawerSummary.empty()
        );

        // Canonical JSON & SHA-256
        String canonicalJson = DocumentIntegrityService.serializeCanonicalJson(snapshotPayload);
        String sha256 = DocumentIntegrityService.computeSha256(canonicalJson);

        return ClosedCashSnapshot.builder()
                .id(UUID.randomUUID().toString())
                .closedCashId(closedCashId)
                .host(host)
                .hostSequence(sequence)
                .dateStart(dateStart)
                .dateEnd(dateEnd)
                .totalGrossSales(grossSales)
                .totalNetSales(netSales)
                .totalTaxes(taxes)
                .totalPayments(paymentsTotal)
                .cashInDrawer(paymentsTotal)
                .cashDifference(BigDecimal.ZERO)
                .payload(canonicalJson)
                .payloadFormat("JSON")
                .payloadVersion(1)
                .payloadChecksum(sha256)
                .appVersion(appVersion != null ? appVersion : "5.0")
                .createdBy(operator != null ? operator : "system")
                .createdAt(new Date())
                .build();
    }
}
