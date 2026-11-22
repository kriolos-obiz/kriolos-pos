//    KriolOS POS
//    Copyright (c) 2019-2023 KriolOS
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

package com.openbravo.pos.util;

import com.openbravo.basic.BasicException;
import com.openbravo.format.Formats;
import com.openbravo.ui.data.binding.Converter;
import java.util.Objects;

/**
 * Reusable two-way data binding converter between presentation {@link String}
 * and domain model type {@code T}, strictly backed by internationalized {@link Formats}.
 *
 * @param <T> model type
 * @author KriolOS
 */
public class FormatsConverter<T> implements Converter<String, T> {

    protected final Formats<T> format;

    public FormatsConverter(Formats<T> format) {
        this.format = Objects.requireNonNull(format, "format must not be null");
    }

    @Override
    public T toModel(String presentationValue) {
        if (presentationValue == null || presentationValue.trim().isEmpty()) {
            return null;
        }
        try {
            return format.parseValue(presentationValue.trim());
        } catch (BasicException e) {
            return null;
        }
    }

    @Override
    public String toPresentation(T modelValue) {
        return modelValue != null ? format.formatValue(modelValue) : "";
    }

    public Formats<T> getFormat() {
        return format;
    }
}
