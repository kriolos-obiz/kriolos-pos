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

import java.math.BigDecimal;
import java.math.BigInteger;
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
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Struct;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.Map;

/**
 * Utility class to validate whether a Java object's runtime type is officially
 * supported by standard
 * {@link java.sql.PreparedStatement#setObject(int, Object)} mappings.
 *
 * <h2>Summary of JDBC Version Milestones</h2>
 * <ul>
 * <li><b>JDBC 1.0 (JDK 1.1):</b> Core primitives/wrappers, {@code String},
 * {@code BigDecimal}, {@code byte[]}, and
 * {@code java.sql.{Date, Time, Timestamp}}.</li>
 * <li><b>JDBC 2.0 (J2SE 1.2):</b> Advanced SQL types ({@code Blob},
 * {@code Clob}, {@code Array}, {@code Struct}, {@code Ref},
 * {@code SQLData}).</li>
 * <li><b>JDBC 3.0 (J2SE 1.4):</b> Added {@link java.net.URL} ({@code DATALINK})
 * and {@code BOOLEAN} as a distinct SQL type alongside {@code BIT}.</li>
 * <li><b>JDBC 4.0 (Java SE 6):</b> Added National Character types
 * ({@code NClob}), {@code SQLXML}, and {@code RowId}.</li>
 * <li><b>JDBC 4.1 (Java SE 7):</b> Introduced
 * {@code getObject(index, Class<T>)} on {@link java.sql.ResultSet}, though no
 * major new {@code setObject} input types were added.</li>
 * <li><b>JDBC 4.2 (Java SE 8):</b> Added {@code java.time.*} classes
 * ({@code LocalDate}, {@code LocalTime}, {@code LocalDateTime},
 * {@code OffsetTime}, {@code OffsetDateTime}) and
 * {@link java.sql.SQLType} / {@link java.sql.JDBCType} enums.
 * <i>(JDBC 4.3 in Java 9 did not add new Java type mappings.)</i></li>
 * </ul>
 */
public class JdbcTypeValidator {

    /**
     * Latest JDBC specification version considered by default.
     */
    public static final double LATEST_JDBC_VERSION = 4.3;

    // Exact class matches mapped to the JDBC version that introduced them
    private static final Map<Class<?>, Double> EXACT_SUPPORTED_TYPES = Map.ofEntries(
            // --- JDBC 1.0 (JDK 1.1) ---
            Map.entry(String.class, 1.0),
            Map.entry(BigDecimal.class, 1.0),
            Map.entry(Boolean.class, 1.0),
            Map.entry(Byte.class, 1.0),
            Map.entry(Short.class, 1.0),
            Map.entry(Integer.class, 1.0),
            Map.entry(Long.class, 1.0),
            Map.entry(Float.class, 1.0),
            Map.entry(Double.class, 1.0),
            Map.entry(byte[].class, 1.0),
            Map.entry(java.sql.Date.class, 1.0),
            Map.entry(java.sql.Time.class, 1.0),
            Map.entry(java.sql.Timestamp.class, 1.0),
            
            // --- JDBC 3.0 (J2SE 1.4) ---
            Map.entry(URL.class, 3.0), // Mapped to DATALINK

            // --- JDBC 4.2 (Java SE 8) ---
            Map.entry(LocalDate.class, 4.2), // Mapped to DATE
            Map.entry(LocalTime.class, 4.2), // Mapped to TIME
            Map.entry(LocalDateTime.class, 4.2), // Mapped to TIMESTAMP
            Map.entry(OffsetTime.class, 4.2), // Mapped to TIME_WITH_TIMEZONE
            Map.entry(OffsetDateTime.class, 4.2), // Mapped to TIMESTAMP_WITH_TIMEZONE

            // --- De-facto / Driver-dependent (Widely supported from JDBC 4.0+ era) ---
            Map.entry(BigInteger.class, 4.0)
    );

    // Interfaces and non-final classes mapped to the JDBC version that introduced them
    private static final Map<Class<?>, Double> HIERARCHY_SUPPORTED_TYPES = Map.ofEntries(
            // --- JDBC 1.0 (JDK 1.1) ---

            // --- JDBC 2.0 (J2SE 1.2) ---
            Map.entry(Clob.class, 2.0),
            Map.entry(Blob.class, 2.0),
            Map.entry(Array.class, 2.0),
            Map.entry(Struct.class, 2.0),
            Map.entry(Ref.class, 2.0),
            Map.entry(SQLData.class, 2.0), // Custom UDT mapping

            // --- JDBC 4.0 (Java SE 6) ---
            Map.entry(NClob.class, 4.0),
            Map.entry(SQLXML.class, 4.0),
            Map.entry(RowId.class, 4.0)
    );

    private JdbcTypeValidator() {
        // Prevent instantiation of utility class
    }

    /**
     * Verifies if the given object is a standard JDBC-supported Java type under
     * the latest JDBC specification.
     *
     * @param obj The object to check (returns {@code false} if {@code null}).
     * @return {@code true} if supported by standard JDBC
     * {@code PreparedStatement.setObject()}.
     */
    public static boolean isJdbcSupported(Object obj) {
        return isJdbcSupported(obj, LATEST_JDBC_VERSION);
    }

    /**
     * Verifies if the given object is a standard JDBC-supported Java type for a
     * specific target JDBC specification version.
     *
     * @param obj The object to check (returns {@code false} if {@code null}).
     * @param jdbcVersion The target JDBC version (e.g.,
     * {@code 3.0}, {@code 4.0}, {@code 4.2}).
     * @return {@code true} if the type was officially supported in or before
     * {@code jdbcVersion}.
     */
    public static boolean isJdbcSupported(Object obj, double jdbcVersion) {
        if (obj == null) {
            return false;
        }

        Class<?> clazz = obj.getClass();

        // 1. Check exact class matches
        Double introducedIn = EXACT_SUPPORTED_TYPES.get(clazz);
        if (introducedIn != null) {
            return jdbcVersion >= introducedIn;
        }

        // 2. Check interfaces and subclasses
        for (Map.Entry<Class<?>, Double> entry : HIERARCHY_SUPPORTED_TYPES.entrySet()) {
            if (entry.getKey().isInstance(obj)) {
                return jdbcVersion >= entry.getValue();
            }
        }

        return false;
    }

    /**
     * Verifies if the given object is a standard JDBC-supported Java type for
     * the JDBC specification version reported by the active {@link Connection}.
     *
     * @param obj The object to check (returns {@code false} if {@code null}).
     * @param connection An active JDBC connection (if {@code null}, defaults to
     * {@link #LATEST_JDBC_VERSION}).
     * @return {@code true} if supported by the connection's JDBC specification
     * version.
     * @throws SQLException if a database access error occurs while reading
     * metadata.
     */
    public static boolean isJdbcSupported(Object obj, Connection connection) throws SQLException {
        if (obj == null) {
            return false;
        }
        DatabaseMetaData metaData = (connection != null) ? connection.getMetaData() : null;
        return isJdbcSupported(obj, metaData);
    }

    /**
     * Verifies if the given object is a standard JDBC-supported Java type for
     * the JDBC specification version reported by {@link DatabaseMetaData}.
     *
     * @param obj The object to check (returns {@code false} if {@code null}).
     * @param metaData The connection's metadata (if {@code null}, defaults to
     * {@link #LATEST_JDBC_VERSION}).
     * @return {@code true} if supported by the metadata's JDBC specification
     * version.
     * @throws SQLException if a database access error occurs while reading
     * metadata.
     */
    public static boolean isJdbcSupported(Object obj, DatabaseMetaData metaData) throws SQLException {
        if (obj == null) {
            return false;
        }
        if (metaData == null) {
            return isJdbcSupported(obj, LATEST_JDBC_VERSION);
        }

        double driverJdbcVersion = metaData.getJDBCMajorVersion() + (metaData.getJDBCMinorVersion() / 10.0);
        return isJdbcSupported(obj, driverJdbcVersion);
    }

}
