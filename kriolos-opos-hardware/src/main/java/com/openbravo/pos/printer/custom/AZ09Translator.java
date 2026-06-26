/*
 * Copyright (C) 2026 Paulo Borges
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
package com.openbravo.pos.printer.custom;

import java.text.Normalizer;
import java.util.regex.Pattern;

public class AZ09Translator {

    public AZ09Translator() {
    }
    
    public byte[] translateString(String text) {
        if (text == null) {
            return new byte[] {0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20};
        }

        // 1. Converte para Maiúsculas
        String upper = text.toUpperCase();

        // 2. Transforma dois pontos em espaço (preserva o espaçamento de "PRECO:")
        String fixSpacing = upper.replace(":", " ");

        // 3. Transforma vírgula decimal em ponto decimal
        String fixDecimal = fixSpacing.replace(",", ".");

        // 4. Remove acentos (Ex: "Ç" vira "C")
        String normalized = Normalizer.normalize(fixDecimal, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        String withoutAccents = pattern.matcher(normalized).replaceAll("");

        // 5. Remove tudo o que NÃO for A-Z, 0-9, Espaço ou Ponto (.)
        String cleaned = withoutAccents.replaceAll("[^A-Z0-9 .]", "");

        // 6. Garante exatamente 8 caracteres (Ajusta se "PRECO 12.50" passar de 8)
        // Nota: Se o display processar o ponto como um caractere normal, "PRECO 12.50" tem 11 caracteres e será cortado.
        String finalString = String.format("%-8s", cleaned).substring(0, 8);

        return finalString.getBytes(java.nio.charset.StandardCharsets.US_ASCII);
    }
}
