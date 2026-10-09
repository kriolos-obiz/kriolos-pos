/*
 * Copyright (C) 2022-2026 KriolOS
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
package com.openbravo.pos.printer;

import java.util.List;

/**
 * Accessor contract for terminal printers, display, and fiscal hardware,
 * along with common receipt text alignment utilities.
 *
 * @author JG uniCenta / KriolOS Team
 */
public interface DeviceTicket {

    DeviceFiscalPrinter getFiscalPrinter();

    DeviceDisplay getDeviceDisplay();

    DevicePrinter getDevicePrinter(String key);

    List<DevicePrinter> getDevicePrinterAll();

   
}
