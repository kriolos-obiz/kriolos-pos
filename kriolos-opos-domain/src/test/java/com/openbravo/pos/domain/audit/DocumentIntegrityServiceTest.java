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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DocumentIntegrityServiceTest {

    @Test
    @DisplayName("Should serialize canonically and compute deterministic SHA-256")
    void shouldSerializeCanonicallyAndComputeDeterministicSha256() {
        FinancialReportSnapshot snapshot = new FinancialReportSnapshot(
                "1.0",
                "session-uuid-1234",
                "POS-TERMINAL-01",
                42,
                Instant.parse("2026-11-02T08:00:00Z"),
                Instant.parse("2026-11-02T16:00:00Z"),
                "operator_admin",
                "5.0.2",
                new FinancialReportSnapshot.FinancialTotals(
                        AmountCalculatorUtil.toBigDecimal(1500.50, 4),
                        AmountCalculatorUtil.toBigDecimal(1250.42, 4),
                        AmountCalculatorUtil.toBigDecimal(250.08, 4),
                        AmountCalculatorUtil.toBigDecimal(1500.50, 4)
                ),
                List.of(
                        new FinancialReportSnapshot.TaxSummary("TAX-01", "Standard VAT 20%",
                                AmountCalculatorUtil.toBigDecimal(0.20, 4),
                                AmountCalculatorUtil.toBigDecimal(1250.42, 4),
                                AmountCalculatorUtil.toBigDecimal(250.08, 4))
                ),
                List.of(
                        new FinancialReportSnapshot.PaymentSummary("cash", AmountCalculatorUtil.toBigDecimal(1000.00, 4), 10),
                        new FinancialReportSnapshot.PaymentSummary("magcard", AmountCalculatorUtil.toBigDecimal(500.50, 4), 5)
                ),
                new FinancialReportSnapshot.DrawerSummary(
                        AmountCalculatorUtil.toBigDecimal(200.00, 4),
                        AmountCalculatorUtil.toBigDecimal(1200.00, 4),
                        AmountCalculatorUtil.toBigDecimal(1200.00, 4),
                        AmountCalculatorUtil.toBigDecimal(0.00, 4)
                )
        );

        String json1 = DocumentIntegrityService.serializeCanonicalJson(snapshot);
        String json2 = DocumentIntegrityService.serializeCanonicalJson(snapshot);

        assertNotNull(json1);
        assertEquals(json1, json2, "Canonical JSON must be 100% deterministic");

        String checksum = DocumentIntegrityService.computeSha256(json1);
        assertNotNull(checksum);
        assertEquals(64, checksum.length(), "SHA-256 checksum must be exactly 64 hex characters");

        assertTrue(DocumentIntegrityService.verifyIntegrity(json1, checksum), "Integrity verification must succeed");
        assertFalse(DocumentIntegrityService.verifyIntegrity(json1 + " ", checksum), "Tampered JSON must fail verification");

        // Deserialization check
        FinancialReportSnapshot deserialized = DocumentIntegrityService.deserializeJson(json1, FinancialReportSnapshot.class);
        assertEquals(snapshot.closedCashId(), deserialized.closedCashId());
        assertEquals(snapshot.host(), deserialized.host());
        assertEquals(snapshot.hostSequence(), deserialized.hostSequence());
        assertEquals(snapshot.totals().totalGrossSales(), deserialized.totals().totalGrossSales());
    }

    @Test
    @DisplayName("Should create ClosedCashSnapshot with immutability and builder pattern")
    void shouldCreateClosedCashSnapshotWithImmutability() {
        String id = UUID.randomUUID().toString();
        String closedCashId = UUID.randomUUID().toString();
        Date now = new Date();

        ClosedCashSnapshot snapshot = ClosedCashSnapshot.builder()
                .id(id)
                .closedCashId(closedCashId)
                .host("TEST-HOST")
                .hostSequence(1)
                .dateStart(now)
                .dateEnd(now)
                .totalGrossSales(BigDecimal.valueOf(100.00))
                .totalNetSales(BigDecimal.valueOf(80.00))
                .totalTaxes(BigDecimal.valueOf(20.00))
                .totalPayments(BigDecimal.valueOf(100.00))
                .cashInDrawer(BigDecimal.valueOf(100.00))
                .cashDifference(BigDecimal.ZERO)
                .payload("{\"test\": true}")
                .payloadChecksum(DocumentIntegrityService.computeSha256("{\"test\": true}"))
                .seriesId("SERIES-2026")
                .documentNo("Z 2026/0001")
                .journalEventId("JOURNAL-001")
                .appVersion("5.0.2")
                .createdBy("admin")
                .createdAt(now)
                .build();

        assertEquals(id, snapshot.getId());
        assertEquals(closedCashId, snapshot.getClosedCashId());
        assertEquals("TEST-HOST", snapshot.getHost());
        assertEquals(1, snapshot.getHostSequence());
        assertEquals("SERIES-2026", snapshot.getSeriesId());
        assertEquals("Z 2026/0001", snapshot.getDocumentNo());
        assertEquals("JOURNAL-001", snapshot.getJournalEventId());
        assertTrue(DocumentIntegrityService.verifyIntegrity(snapshot.getPayload(), snapshot.getPayloadChecksum()));
    }
}
