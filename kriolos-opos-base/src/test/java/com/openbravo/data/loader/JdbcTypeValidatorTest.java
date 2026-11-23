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
package com.openbravo.data.loader;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.RowId;
import java.sql.SQLData;
import java.sql.SQLXML;
import java.sql.Struct;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class JdbcTypeValidatorTest {

    @Test
    @DisplayName("Null object always returns false regardless of JDBC version")
    void testNullObject() {
        assertFalse(JdbcTypeValidator.isJdbcSupported(null));
        assertFalse(JdbcTypeValidator.isJdbcSupported(null, 4.3));
    }

    @Test
    @DisplayName("Null JDBC version defaults to latest version")
    void testNullVersionDefaultsToLatest() {
        assertTrue(JdbcTypeValidator.isJdbcSupported(LocalDate.now()));
        assertFalse(JdbcTypeValidator.isJdbcSupported(new java.util.Date()));
    }

    @ParameterizedTest(name = "{0} should be supported in latest JDBC")
    @MethodSource("provideAllLatestSupportedObjects")
    @DisplayName("All standard types are supported under default/latest JDBC version")
    void testLatestJdbcSupportedTypes(Object supportedObject) {
        assertTrue(JdbcTypeValidator.isJdbcSupported(supportedObject));
    }

    static Stream<Object> provideAllLatestSupportedObjects() throws MalformedURLException {
        long now = System.currentTimeMillis();
        return Stream.of(
                // JDBC 1.0
                "hello",
                BigDecimal.TEN,
                Boolean.TRUE,
                (byte) 1,
                (short) 2,
                42,
                100L,
                3.14f,
                2.718d,
                new byte[]{1, 2, 3},
                new Date(now),
                new Time(now),
                new Timestamp(now),
                // JDBC 2.0 (Driver interface implementations via dynamic proxies)
                mockInterface(Clob.class),
                mockInterface(Blob.class),
                mockInterface(Array.class),
                mockInterface(Struct.class),
                mockInterface(Ref.class),
                mockInterface(SQLData.class),
                // JDBC 3.0
                URI.create("https://example.com").toURL(),
                // JDBC 4.0
                mockInterface(NClob.class),
                mockInterface(SQLXML.class),
                mockInterface(RowId.class),
                BigInteger.ONE,
                // JDBC 4.2
                LocalDate.now(),
                LocalTime.now(),
                LocalDateTime.now(),
                OffsetTime.now(),
                OffsetDateTime.now()
        );
    }

    @ParameterizedTest(name = "[{index}] {0} should NOT be supported")
    @MethodSource("provideUnsupportedObjects")
    @DisplayName("Non-standard or ambiguous Java types return false")
    void testUnsupportedTypes(Object unsupportedObject) {
        assertFalse(
                JdbcTypeValidator.isJdbcSupported(unsupportedObject),
                () -> "Expected false for type: " + unsupportedObject.getClass().getName()
        );
    }

    static Stream<Arguments> provideUnsupportedObjects() {
        return Stream.of(
                Arguments.of(new java.util.Date()), // Must use java.sql.Date / Timestamp
                Arguments.of(Instant.now()), // Not in JDBC 4.2 table (use OffsetDateTime)
                Arguments.of(ZonedDateTime.now()), // Not in JDBC 4.2 table
                Arguments.of(UUID.randomUUID()), // Driver-specific (e.g., PostgreSQL), not core JDBC spec
                Arguments.of(List.of("a", "b")), // Must convert via Connection.createArrayOf()
                Arguments.of((Object) new String[]{"a", "b"}), // Cast to Object so JUnit doesn't unpack the array!
                Arguments.of(new Object())
        );
    }

    @ParameterizedTest(name = "{0} introduced in JDBC {1} (test version {2} -> expected {3})")
    @MethodSource("provideVersionBoundaryCases")
    @DisplayName("Version boundary checks accurately gate types by JDBC spec release")
    void testVersionBoundaries(Object obj, double introducedIn, double testVersion, boolean expected) {
        if (expected) {
            assertTrue(JdbcTypeValidator.isJdbcSupported(obj, testVersion));
        } else {
            assertFalse(JdbcTypeValidator.isJdbcSupported(obj, testVersion));
        }
    }

    static Stream<Arguments> provideVersionBoundaryCases() throws MalformedURLException {
        URL sampleUrl = URI.create("https://example.com").toURL();
        Blob sampleBlob = mockInterface(Blob.class);
        SQLXML sampleXml = mockInterface(SQLXML.class);
        LocalDate sampleLocalDate = LocalDate.now();

        return Stream.of(
                // JDBC 1.0 type available everywhere
                Arguments.of("text", 1.0, 1.0, true),
                Arguments.of("text", 1.0, 0.9, false),
                // JDBC 2.0 (Blob)
                Arguments.of(sampleBlob, 2.0, 1.0, false),
                Arguments.of(sampleBlob, 2.0, 2.0, true),
                Arguments.of(sampleBlob, 2.0, 4.2, true),
                // JDBC 3.0 (URL)
                Arguments.of(sampleUrl, 3.0, 2.0, false),
                Arguments.of(sampleUrl, 3.0, 3.0, true),
                Arguments.of(sampleUrl, 3.0, 4.0, true),
                // JDBC 4.0 (SQLXML)
                Arguments.of(sampleXml, 4.0, 3.0, false),
                Arguments.of(sampleXml, 4.0, 4.0, true),
                Arguments.of(sampleXml, 4.0, 4.1, true),
                // JDBC 4.2 (java.time.LocalDate)
                Arguments.of(sampleLocalDate, 4.2, 4.0, false),
                Arguments.of(sampleLocalDate, 4.2, 4.1, false),
                Arguments.of(sampleLocalDate, 4.2, 4.2, true),
                Arguments.of(sampleLocalDate, 4.2, 4.3, true)
        );
    }

    /**
     * Helper to instantiate lightweight proxy objects for JDBC interfaces
     * (Blob, Clob, SQLXML, etc.) without needing a Mockito dependency.
     */
    @SuppressWarnings("unchecked")
    private static <T> T mockInterface(Class<T> interfaceClass) {
        return (T) Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass},
                (proxy, method, args) -> null
        );
    }

    @Test
    @DisplayName("Connection and DatabaseMetaData overloads detect driver JDBC version accurately")
    void testConnectionAndMetaDataOverloads() throws Exception {
        LocalDate java8Date = LocalDate.now();
        URL jdbc3Url = URI.create("https://example.com").toURL();

        // Simulate a legacy JDBC 4.0 driver (e.g., older driver before Java 8 java.time support)
        DatabaseMetaData jdbc40Meta = mockMetaData(4, 0);
        Connection jdbc40Conn = mockConnection(jdbc40Meta);

        assertFalse(JdbcTypeValidator.isJdbcSupported(java8Date, jdbc40Conn));
        assertFalse(JdbcTypeValidator.isJdbcSupported(java8Date, jdbc40Meta));
        assertTrue(JdbcTypeValidator.isJdbcSupported(jdbc3Url, jdbc40Conn));

        // Simulate a modern JDBC 4.2 driver
        DatabaseMetaData jdbc42Meta = mockMetaData(4, 2);
        Connection jdbc42Conn = mockConnection(jdbc42Meta);

        assertTrue(JdbcTypeValidator.isJdbcSupported(java8Date, jdbc42Conn));
        assertTrue(JdbcTypeValidator.isJdbcSupported(java8Date, jdbc42Meta));

        // Null connection/metadata safely falls back to LATEST_JDBC_VERSION
        assertTrue(JdbcTypeValidator.isJdbcSupported(java8Date, (Connection) null));
        assertTrue(JdbcTypeValidator.isJdbcSupported(java8Date, (DatabaseMetaData) null));
        assertFalse(JdbcTypeValidator.isJdbcSupported(null, jdbc42Conn));
    }

    private static DatabaseMetaData mockMetaData(int majorVersion, int minorVersion) {
        return (DatabaseMetaData) Proxy.newProxyInstance(
                DatabaseMetaData.class.getClassLoader(),
                new Class<?>[]{DatabaseMetaData.class},
                (proxy, method, args) -> {
                    if ("getJDBCMajorVersion".equals(method.getName())) {
                        return majorVersion;
                    }
                    if ("getJDBCMinorVersion".equals(method.getName())) {
                        return minorVersion;
                    }
                    return null;
                }
        );
    }

    private static Connection mockConnection(DatabaseMetaData metaData) {
        return (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class<?>[]{Connection.class},
                (proxy, method, args) -> {
                    if ("getMetaData".equals(method.getName())) {
                        return metaData;
                    }
                    return null;
                }
        );
    }

}
