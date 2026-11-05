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

/**
 * Universal contract for immutable, audit-compliant documents (WORM) in KriolOS POS.
 * Designed to integrate seamlessly with Document Number Series (#87) and Audit Journal (#86).
 *
 * @author poolborges
 */
public interface AuditableDocument {

    /**
     * Unique technical identifier (UUID) of the document.
     *
     * @return document UUID
     */
    String getId();

    /**
     * Optional series identifier referencing Document Series (#87).
     *
     * @return series ID or null if unassigned
     */
    String getSeriesId();

    /**
     * Legal document sequence number (e.g. "Z 2026/0001", "FT 2026/0042").
     *
     * @return formatted document number or null if unassigned
     */
    String getDocumentNo();

    /**
     * Audit journal event correlation identifier (#86).
     *
     * @return journal event ID or null
     */
    String getJournalEventId();

    /**
     * Checksum of the immediately preceding document in sequence (for tamper-evident hash chaining).
     *
     * @return previous document SHA-256 checksum or null if genesis document
     */
    String getPreviousChecksum();

    /**
     * Canonical WORM serialized payload (JSON/XML).
     *
     * @return serialized document payload
     */
    String getPayload();

    /**
     * Format of the payload (e.g., "JSON", "XML").
     *
     * @return payload format identifier
     */
    String getPayloadFormat();

    /**
     * Schema version of the serialized payload.
     *
     * @return version number
     */
    int getPayloadVersion();

    /**
     * Cryptographic SHA-256 hash calculated over the canonical payload.
     *
     * @return 64-character lowercase hexadecimal hash
     */
    String getPayloadChecksum();
}
