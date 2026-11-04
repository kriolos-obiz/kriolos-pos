/*
 * Copyright (C) 2026 KriolOS POS
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
package com.openbravo.pos.imports;

import com.openbravo.data.loader.Session;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Import persistence database contract")
public class ImportPersistenceDatabaseTest {

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException ignored) {
        }
    }

    @ParameterizedTest(name = "customer import persists on {0}")
    @MethodSource("databaseConnections")
    void customerImportPersistsFieldsOnEachDatabase(
            String databaseName,
            String jdbcUrl,
            String username,
            String password) throws Exception {
        Session session = new Session(jdbcUrl, username, password);
        try {
            try (Statement statement = session.getConnection().createStatement()) {
                statement.executeUpdate("CREATE TABLE csvimport ("
                        + "ID VARCHAR(36) PRIMARY KEY, "
                        + "ROWNUMBER VARCHAR(32), "
                        + "CSVERROR VARCHAR(255), "
                        + "SEARCHKEY VARCHAR(255), "
                        + "NAME VARCHAR(255))");
            }

            DataLogicImport service = new DataLogicImport();
            service.init(session);
            service.execCustomerAddCSVEntry(new CustomerCsvImportEntry(
                    "row-2", "27", "new", "CUST-027", "Customer 27"));

            try (PreparedStatement query = session.getConnection().prepareStatement(
                    "SELECT ID, ROWNUMBER, CSVERROR, SEARCHKEY, NAME FROM csvimport WHERE ID = ?")) {
                query.setString(1, "row-2");
                try (ResultSet result = query.executeQuery()) {
                    assertTrue(result.next(), databaseName);
                    assertEquals("row-2", result.getString("ID"), databaseName);
                    assertEquals("27", result.getString("ROWNUMBER"), databaseName);
                    assertEquals("new", result.getString("CSVERROR"), databaseName);
                    assertEquals("CUST-027", result.getString("SEARCHKEY"), databaseName);
                    assertEquals("Customer 27", result.getString("NAME"), databaseName);
                    assertFalse(result.next(), databaseName);
                }
            }
        } finally {
            session.close();
        }
    }

    private static Stream<Arguments> databaseConnections() {
        String databaseName = "customer_import_" + UUID.randomUUID().toString().replace("-", "");
        return Stream.of(
                Arguments.of(
                        "HSQLDB",
                        "jdbc:hsqldb:mem:" + databaseName + ";sql.syntax_mys=true",
                        "SA",
                        ""),
                Arguments.of("SQLite", "jdbc:sqlite::memory:", null, null));
    }
}
