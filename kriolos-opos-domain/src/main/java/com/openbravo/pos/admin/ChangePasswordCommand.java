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

package com.openbravo.pos.admin;

import java.util.Objects;

/**
 * Immutable command record representing a user password update.
 *
 * @param userId      the identifier of the user whose password is to be changed (mandatory)
 * @param newPassword the new password hash or clear value (nullable for clearing password)
 */
public record ChangePasswordCommand(
        String userId,
        String newPassword
) {

    /**
     * Compact constructor validating required invariants.
     */
    public ChangePasswordCommand {
        Objects.requireNonNull(userId, "userId must not be null");
        if (userId.isBlank()) {
            throw new IllegalArgumentException("userId must not be blank");
        }
    }

    /**
     * Convenience factory for creating ChangePasswordCommand instances.
     *
     * @param userId      the user identifier
     * @param newPassword the new password
     * @return new ChangePasswordCommand
     */
    public static ChangePasswordCommand of(String userId, String newPassword) {
        return new ChangePasswordCommand(userId, newPassword);
    }
}
