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

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;

/**
 * Immutable domain entity representing a persisted cash drawer closure snapshot record (WORM).
 * Implements {@link AuditableDocument} for cross-system compliance and verification.
 *
 * @author poolborges
 */
public final class ClosedCashSnapshot implements AuditableDocument, Serializable {

    private static final long serialVersionUID = 1L;

    private final String id;
    private final String closedCashId;
    private final String host;
    private final int hostSequence;
    private final Date dateStart;
    private final Date dateEnd;

    // Financial totals (Exact BigDecimal precision)
    private final BigDecimal totalGrossSales;
    private final BigDecimal totalNetSales;
    private final BigDecimal totalTaxes;
    private final BigDecimal totalPayments;
    private final BigDecimal cashInDrawer;
    private final BigDecimal cashDifference;

    // Serialized WORM payload & integrity
    private final String payload;
    private final String payloadFormat;
    private final int payloadVersion;
    private final String payloadChecksum;

    // Forward integration slots (#87 Series, #86 Journal, Chaining)
    private final String seriesId;
    private final String documentNo;
    private final String journalEventId;
    private final String previousChecksum;

    // Traceability metadata
    private final String appVersion;
    private final String createdBy;
    private final Date createdAt;

    private ClosedCashSnapshot(Builder builder) {
        this.id = Objects.requireNonNull(builder.id, "id must not be null");
        this.closedCashId = Objects.requireNonNull(builder.closedCashId, "closedCashId must not be null");
        this.host = Objects.requireNonNull(builder.host, "host must not be null");
        this.hostSequence = builder.hostSequence;
        this.dateStart = builder.dateStart != null ? new Date(builder.dateStart.getTime()) : new Date();
        this.dateEnd = builder.dateEnd != null ? new Date(builder.dateEnd.getTime()) : new Date();
        this.totalGrossSales = builder.totalGrossSales != null ? builder.totalGrossSales : BigDecimal.ZERO;
        this.totalNetSales = builder.totalNetSales != null ? builder.totalNetSales : BigDecimal.ZERO;
        this.totalTaxes = builder.totalTaxes != null ? builder.totalTaxes : BigDecimal.ZERO;
        this.totalPayments = builder.totalPayments != null ? builder.totalPayments : BigDecimal.ZERO;
        this.cashInDrawer = builder.cashInDrawer != null ? builder.cashInDrawer : BigDecimal.ZERO;
        this.cashDifference = builder.cashDifference != null ? builder.cashDifference : BigDecimal.ZERO;
        this.payload = Objects.requireNonNull(builder.payload, "payload must not be null");
        this.payloadFormat = builder.payloadFormat != null ? builder.payloadFormat : "JSON";
        this.payloadVersion = builder.payloadVersion > 0 ? builder.payloadVersion : 1;
        this.payloadChecksum = Objects.requireNonNull(builder.payloadChecksum, "payloadChecksum must not be null");
        this.seriesId = builder.seriesId;
        this.documentNo = builder.documentNo;
        this.journalEventId = builder.journalEventId;
        this.previousChecksum = builder.previousChecksum;
        this.appVersion = builder.appVersion != null ? builder.appVersion : "5.0";
        this.createdBy = builder.createdBy != null ? builder.createdBy : "system";
        this.createdAt = builder.createdAt != null ? new Date(builder.createdAt.getTime()) : new Date();
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String getId() {
        return id;
    }

    public String getClosedCashId() {
        return closedCashId;
    }

    public String getHost() {
        return host;
    }

    public int getHostSequence() {
        return hostSequence;
    }

    public Date getDateStart() {
        return new Date(dateStart.getTime());
    }

    public Date getDateEnd() {
        return new Date(dateEnd.getTime());
    }

    public BigDecimal getTotalGrossSales() {
        return totalGrossSales;
    }

    public BigDecimal getTotalNetSales() {
        return totalNetSales;
    }

    public BigDecimal getTotalTaxes() {
        return totalTaxes;
    }

    public BigDecimal getTotalPayments() {
        return totalPayments;
    }

    public BigDecimal getCashInDrawer() {
        return cashInDrawer;
    }

    public BigDecimal getCashDifference() {
        return cashDifference;
    }

    @Override
    public String getPayload() {
        return payload;
    }

    @Override
    public String getPayloadFormat() {
        return payloadFormat;
    }

    @Override
    public int getPayloadVersion() {
        return payloadVersion;
    }

    @Override
    public String getPayloadChecksum() {
        return payloadChecksum;
    }

    @Override
    public String getSeriesId() {
        return seriesId;
    }

    @Override
    public String getDocumentNo() {
        return documentNo;
    }

    @Override
    public String getJournalEventId() {
        return journalEventId;
    }

    @Override
    public String getPreviousChecksum() {
        return previousChecksum;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public Date getCreatedAt() {
        return new Date(createdAt.getTime());
    }

    public static final class Builder {
        private String id;
        private String closedCashId;
        private String host;
        private int hostSequence;
        private Date dateStart;
        private Date dateEnd;
        private BigDecimal totalGrossSales;
        private BigDecimal totalNetSales;
        private BigDecimal totalTaxes;
        private BigDecimal totalPayments;
        private BigDecimal cashInDrawer;
        private BigDecimal cashDifference;
        private String payload;
        private String payloadFormat = "JSON";
        private int payloadVersion = 1;
        private String payloadChecksum;
        private String seriesId;
        private String documentNo;
        private String journalEventId;
        private String previousChecksum;
        private String appVersion;
        private String createdBy;
        private Date createdAt;

        private Builder() {}

        public Builder id(String id) { this.id = id; return this; }
        public Builder closedCashId(String closedCashId) { this.closedCashId = closedCashId; return this; }
        public Builder host(String host) { this.host = host; return this; }
        public Builder hostSequence(int hostSequence) { this.hostSequence = hostSequence; return this; }
        public Builder dateStart(Date dateStart) { this.dateStart = dateStart; return this; }
        public Builder dateEnd(Date dateEnd) { this.dateEnd = dateEnd; return this; }
        public Builder totalGrossSales(BigDecimal totalGrossSales) { this.totalGrossSales = totalGrossSales; return this; }
        public Builder totalNetSales(BigDecimal totalNetSales) { this.totalNetSales = totalNetSales; return this; }
        public Builder totalTaxes(BigDecimal totalTaxes) { this.totalTaxes = totalTaxes; return this; }
        public Builder totalPayments(BigDecimal totalPayments) { this.totalPayments = totalPayments; return this; }
        public Builder cashInDrawer(BigDecimal cashInDrawer) { this.cashInDrawer = cashInDrawer; return this; }
        public Builder cashDifference(BigDecimal cashDifference) { this.cashDifference = cashDifference; return this; }
        public Builder payload(String payload) { this.payload = payload; return this; }
        public Builder payloadFormat(String payloadFormat) { this.payloadFormat = payloadFormat; return this; }
        public Builder payloadVersion(int payloadVersion) { this.payloadVersion = payloadVersion; return this; }
        public Builder payloadChecksum(String payloadChecksum) { this.payloadChecksum = payloadChecksum; return this; }
        public Builder seriesId(String seriesId) { this.seriesId = seriesId; return this; }
        public Builder documentNo(String documentNo) { this.documentNo = documentNo; return this; }
        public Builder journalEventId(String journalEventId) { this.journalEventId = journalEventId; return this; }
        public Builder previousChecksum(String previousChecksum) { this.previousChecksum = previousChecksum; return this; }
        public Builder appVersion(String appVersion) { this.appVersion = appVersion; return this; }
        public Builder createdBy(String createdBy) { this.createdBy = createdBy; return this; }
        public Builder createdAt(Date createdAt) { this.createdAt = createdAt; return this; }

        public ClosedCashSnapshot build() {
            return new ClosedCashSnapshot(this);
        }
    }
}
