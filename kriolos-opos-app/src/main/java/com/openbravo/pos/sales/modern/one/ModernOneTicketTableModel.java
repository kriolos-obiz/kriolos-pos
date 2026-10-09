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
package com.openbravo.pos.sales.modern.one;

import com.openbravo.format.Formats;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.ticket.TicketLineInfo;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 * Clean, lightweight TableModel backing the modern sales ticket lines.
 *
 * @author KriolOS Team
 */
public class ModernOneTicketTableModel extends AbstractTableModel {

    private static final long serialVersionUID = 1L;

    public static final int COL_PRODUCT = 0;
    public static final int COL_QTY = 1;
    public static final int COL_PRICE = 2;
    public static final int COL_TOTAL = 3;

    private static final String[] COLUMN_HEADERS = {
        AppLocal.getIntString("label.item"),
        AppLocal.getIntString("label.units"),
        AppLocal.getIntString("label.price"),
        AppLocal.getIntString("label.total")
    };

    private final List<TicketLineInfo> lines = new ArrayList<>();

    public void setLines(List<TicketLineInfo> newLines) {
        lines.clear();
        if (newLines != null) {
            lines.addAll(newLines);
        }
        fireTableDataChanged();
    }

    public void addLine(TicketLineInfo line) {
        lines.add(line);
        int row = lines.size() - 1;
        fireTableRowsInserted(row, row);
    }

    public void updateLine(int index, TicketLineInfo line) {
        if (index >= 0 && index < lines.size()) {
            lines.set(index, line);
            fireTableRowsUpdated(index, index);
        }
    }

    public TicketLineInfo removeLine(int index) {
        if (index >= 0 && index < lines.size()) {
            TicketLineInfo removed = lines.remove(index);
            fireTableRowsDeleted(index, index);
            return removed;
        }
        return null;
    }

    public void clear() {
        lines.clear();
        fireTableDataChanged();
    }

    public TicketLineInfo getLine(int index) {
        if (index >= 0 && index < lines.size()) {
            return lines.get(index);
        }
        return null;
    }

    public List<TicketLineInfo> getLines() {
        return new ArrayList<>(lines);
    }

    @Override
    public int getRowCount() {
        return lines.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMN_HEADERS.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMN_HEADERS[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex < 0 || rowIndex >= lines.size()) {
            return null;
        }
        TicketLineInfo line = lines.get(rowIndex);
        switch (columnIndex) {
            case COL_PRODUCT:
                return line.printName();
            case COL_QTY:
                return Formats.DOUBLE.formatValue(line.getMultiply());
            case COL_PRICE:
                return Formats.CURRENCY.formatValue(line.getPriceWithTax());
            case COL_TOTAL:
                return Formats.CURRENCY.formatValue(line.getSubTotalWithTax());
            default:
                return null;
        }
    }
}
