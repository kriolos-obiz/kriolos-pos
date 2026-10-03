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
package com.openbravo.pos.spi.hardware.scale;

import com.openbravo.pos.spi.hardware.HardwareException;

/**
 * Exception raised when an error occurs while communicating with or reading from a weight scale.
 *
 * @author KriolOS Team
 * @since 1.0.0
 */
public class ScaleException extends HardwareException {

    private static final long serialVersionUID = 1L;

    public ScaleException() {
        super();
    }

    public ScaleException(String message) {
        super(message);
    }

    public ScaleException(String message, Throwable cause) {
        super(message, cause);
    }

    public ScaleException(Throwable cause) {
        super(cause);
    }
}
