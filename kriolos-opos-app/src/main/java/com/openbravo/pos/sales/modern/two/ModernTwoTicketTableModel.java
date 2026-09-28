//    KriolOS POS
//    Copyright (c) 2019-2026 KriolOS
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program.  If not, see <http://www.gnu.org/licenses/>.

package com.openbravo.pos.sales.modern.two;

import com.openbravo.pos.ticket.TicketLineInfo;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 * Single-column digital receipt TableModel for ModernTwo.
 * Returns the entire {@link TicketLineInfo} object for each row so that
 * {@link ModernTwoTicketCellRenderer} can format it as a rich receipt card.
 *
 * @author KriolOS Team
 */
public class ModernTwoTicketTableModel extends AbstractTableModel {

    private static final long serialVersionUID = 1L;

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
        return 1;
    }

    @Override
    public String getColumnName(int column) {
        return "";
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return TicketLineInfo.class;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex >= 0 && rowIndex < lines.size()) {
            return lines.get(rowIndex);
        }
        return null;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }
}
