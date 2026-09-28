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
package com.openbravo.pos.sales.modern;

import javax.swing.*;

public class FlexFactory {
    
    /**
     * Cria uma linha flexível horizontal simples (sem quebra de linha).
     * Ideal para barras de ferramentas, rodapés ou cabeçalhos.
     */
    public static JPanel createRow(FlexLayout.JustifyContent justify, FlexLayout.AlignItems align, int gap) {
        JPanel panel = new JPanel();
        panel.setLayout(FlexLayout.builder()
                .direction(FlexLayout.Direction.ROW)
                .wrap(FlexLayout.Wrap.NO_WRAP)
                .justifyContent(justify)
                .alignItems(align)
                .gap(gap)
                .build());
        return panel;
    }

    /**
     * Cria uma linha flexível horizontal com quebra automática (Wrap).
     * Ideal para grelhas de cartões (cards), listagens dinâmicas ou botões de tags.
     */
    public static JPanel createRowWrap(FlexLayout.JustifyContent justify, FlexLayout.AlignItems align, int gap) {
        JPanel panel = new JPanel();
        panel.setLayout(FlexLayout.builder()
                .direction(FlexLayout.Direction.ROW)
                .wrap(FlexLayout.Wrap.WRAP)
                .justifyContent(justify)
                .alignItems(align)
                .gap(gap)
                .build());
        return panel;
    }

    /**
     * Cria uma coluna flexível vertical (sem quebra).
     * Ideal para formulários, menus laterais e empilhamentos verticais clássicos.
     */
    public static JPanel createColumn(FlexLayout.JustifyContent justify, FlexLayout.AlignItems align, int gap) {
        JPanel panel = new JPanel();
        panel.setLayout(FlexLayout.builder()
                .direction(FlexLayout.Direction.COLUMN)
                .wrap(FlexLayout.Wrap.NO_WRAP)
                .justifyContent(justify)
                .alignItems(align)
                .gap(gap)
                .build());
        return panel;
    }
}


