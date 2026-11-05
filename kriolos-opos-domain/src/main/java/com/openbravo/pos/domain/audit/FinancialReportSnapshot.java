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

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Immutable Java 17 Record representing the canonical financial snapshot payload
 * for cash register closure (Z-Report / ClosedCash WORM snapshot).
 *
 * @author poolborges
 */
public record FinancialReportSnapshot(
        String schemaVersion,
        String closedCashId,
        String host,
        int hostSequence,
        Instant dateStart,
        Instant dateEnd,
        String createdBy,
        String appVersion,
        FinancialTotals totals,
        List<TaxSummary> taxes,
        List<PaymentSummary> payments,
        DrawerSummary drawer
) {

    public FinancialReportSnapshot {
        Objects.requireNonNull(schemaVersion, "schemaVersion must not be null");
        Objects.requireNonNull(closedCashId, "closedCashId must not be null");
        Objects.requireNonNull(host, "host must not be null");
        Objects.requireNonNull(dateStart, "dateStart must not be null");
        Objects.requireNonNull(dateEnd, "dateEnd must not be null");
        Objects.requireNonNull(totals, "totals must not be null");
        taxes = taxes == null ? List.of() : List.copyOf(taxes);
        payments = payments == null ? List.of() : List.copyOf(payments);
        drawer = drawer == null ? DrawerSummary.empty() : drawer;
    }

    /**
     * Consolidated financial totals with exact BigDecimal precision.
     */
    public record FinancialTotals(
            BigDecimal totalGrossSales,
            BigDecimal totalNetSales,
            BigDecimal totalTaxes,
            BigDecimal totalPayments
    ) {
        public FinancialTotals {
            totalGrossSales = totalGrossSales == null ? BigDecimal.ZERO : totalGrossSales;
            totalNetSales = totalNetSales == null ? BigDecimal.ZERO : totalNetSales;
            totalTaxes = totalTaxes == null ? BigDecimal.ZERO : totalTaxes;
            totalPayments = totalPayments == null ? BigDecimal.ZERO : totalPayments;
        }
    }

    /**
     * Aggregated tax line item breakdown per tax category.
     */
    public record TaxSummary(
            String taxId,
            String taxName,
            BigDecimal rate,
            BigDecimal baseAmount,
            BigDecimal taxAmount
    ) {
        public TaxSummary {
            Objects.requireNonNull(taxId, "taxId must not be null");
            rate = rate == null ? BigDecimal.ZERO : rate;
            baseAmount = baseAmount == null ? BigDecimal.ZERO : baseAmount;
            taxAmount = taxAmount == null ? BigDecimal.ZERO : taxAmount;
        }
    }

    /**
     * Aggregated tender/payment breakdown (Cash, Card, Voucher, etc.).
     */
    public record PaymentSummary(
            String paymentMethod,
            BigDecimal totalAmount,
            int transactionCount
    ) {
        public PaymentSummary {
            Objects.requireNonNull(paymentMethod, "paymentMethod must not be null");
            totalAmount = totalAmount == null ? BigDecimal.ZERO : totalAmount;
        }
    }

    /**
     * Cash drawer physical reconciliation summary.
     */
    public record DrawerSummary(
            BigDecimal openingCash,
            BigDecimal cashInDrawer,
            BigDecimal theoreticalCash,
            BigDecimal cashDifference
    ) {
        public DrawerSummary {
            openingCash = openingCash == null ? BigDecimal.ZERO : openingCash;
            cashInDrawer = cashInDrawer == null ? BigDecimal.ZERO : cashInDrawer;
            theoreticalCash = theoreticalCash == null ? BigDecimal.ZERO : theoreticalCash;
            cashDifference = cashDifference == null ? BigDecimal.ZERO : cashDifference;
        }

        public static DrawerSummary empty() {
            return new DrawerSummary(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        }
    }
}
