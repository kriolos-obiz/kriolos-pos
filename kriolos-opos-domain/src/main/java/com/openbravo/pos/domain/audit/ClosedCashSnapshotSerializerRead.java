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

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.DataRead;
import com.openbravo.data.loader.SerializerRead;
import com.openbravo.pos.domain.utils.AmountCalculatorUtil;

import java.math.BigDecimal;

/**
 * Custom SerializerRead for {@link ClosedCashSnapshot} preserving strict domain immutability.
 * Reads SQL result sets and instantiates immutable snapshot domain records.
 *
 * @author poolborges
 */
public class ClosedCashSnapshotSerializerRead implements SerializerRead<ClosedCashSnapshot> {

    public static final ClosedCashSnapshotSerializerRead INSTANCE = new ClosedCashSnapshotSerializerRead();

    @Override
    public ClosedCashSnapshot readValues(DataRead dr) throws BasicException {
        return ClosedCashSnapshot.builder()
                .id(dr.getString(1))
                .closedCashId(dr.getString(2))
                .host(dr.getString(3))
                .hostSequence(dr.getInt(4) == null ? 0 : dr.getInt(4))
                .dateStart(dr.getTimestamp(5))
                .dateEnd(dr.getTimestamp(6))
                .totalGrossSales(dr.getDouble(7) != null ? AmountCalculatorUtil.toBigDecimal(dr.getDouble(7), 4) : BigDecimal.ZERO)
                .totalNetSales(dr.getDouble(8) != null ? AmountCalculatorUtil.toBigDecimal(dr.getDouble(8), 4) : BigDecimal.ZERO)
                .totalTaxes(dr.getDouble(9) != null ? AmountCalculatorUtil.toBigDecimal(dr.getDouble(9), 4) : BigDecimal.ZERO)
                .totalPayments(dr.getDouble(10) != null ? AmountCalculatorUtil.toBigDecimal(dr.getDouble(10), 4) : BigDecimal.ZERO)
                .cashInDrawer(dr.getDouble(11) != null ? AmountCalculatorUtil.toBigDecimal(dr.getDouble(11), 4) : BigDecimal.ZERO)
                .cashDifference(dr.getDouble(12) != null ? AmountCalculatorUtil.toBigDecimal(dr.getDouble(12), 4) : BigDecimal.ZERO)
                .payload(dr.getString(13))
                .payloadFormat(dr.getString(14))
                .payloadVersion(dr.getInt(15) == null ? 1 : dr.getInt(15))
                .payloadChecksum(dr.getString(16))
                .seriesId(dr.getString(17))
                .documentNo(dr.getString(18))
                .journalEventId(dr.getString(19))
                .previousChecksum(dr.getString(20))
                .appVersion(dr.getString(21))
                .createdBy(dr.getString(22))
                .createdAt(dr.getTimestamp(23))
                .build();
    }
}
