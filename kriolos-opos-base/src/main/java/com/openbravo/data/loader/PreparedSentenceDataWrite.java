/*
 * Copyright (C) 2026 Kriolos
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

import com.openbravo.basic.BasicException;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author poolborges
 */
public final class PreparedSentenceDataWrite implements DataWrite {

    private static final Logger LOGGER = Logger.getLogger("com.openbravo.data.loader.PreparedSentenceDataWrite");

    private final PreparedStatement m_ps;

    public PreparedSentenceDataWrite(PreparedStatement ps) {
        this.m_ps = ps;
    }

    private boolean isNull(String value) {
        return (value == null || value.equalsIgnoreCase("NULL"));
    }

    @Override
    public void setInt(int paramIndex, Integer value) throws BasicException {
        try {
            if (value == null) {
                this.m_ps.setNull(paramIndex, Types.INTEGER);
            } else {
                this.m_ps.setInt(paramIndex, value);
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "Exception", ex);
            throw new BasicException(ex);
        }
    }

    @Override
    public void setString(int paramIndex, String value) throws BasicException {
        try {
            if (isNull(value)) {
                this.m_ps.setNull(paramIndex, Types.VARCHAR);
            } else {
                this.m_ps.setString(paramIndex, value);
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "Exception", ex);
            throw new BasicException(ex);
        }
    }

    @Override
    public void setDouble(int paramIndex, Double value) throws BasicException {
        try {
            if (value == null) {
                this.m_ps.setNull(paramIndex, Types.DOUBLE);
            } else {
                this.m_ps.setDouble(paramIndex, value);
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "Exception", ex);
            throw new BasicException(ex);
        }
    }

    @Override
    public void setBoolean(int paramIndex, Boolean value) throws BasicException {
        try {
            if (value == null) {
                this.m_ps.setNull(paramIndex, Types.BOOLEAN);
            } else {
                this.m_ps.setBoolean(paramIndex, value);
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "Exception", ex);
            throw new BasicException(ex);
        }
    }

    @Override
    public void setTimestamp(int paramIndex, Date value) throws BasicException {
        try {
            if (value == null) {
                this.m_ps.setNull(paramIndex, Types.TIMESTAMP);
            } else {
                this.m_ps.setTimestamp(paramIndex, new Timestamp(value.getTime()));
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "Exception", ex);
            throw new BasicException(ex);
        }
    }

    @Override
    public void setBytes(int paramIndex, byte[] value) throws BasicException {
        try {
            if (value == null) {
                this.m_ps.setNull(paramIndex, Types.BINARY);
            } else {
                InputStream bais = new ByteArrayInputStream(value);
                this.m_ps.setBinaryStream(paramIndex, bais);
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "Exception saving byte[]", ex);
            throw new BasicException(ex);
        }
    }

    @Override
    public void setObject(int paramIndex, Object value) throws BasicException {
        try {
            if (value == null) {
                this.m_ps.setNull(paramIndex, Types.BINARY);
            } else if (value instanceof byte[]) {
                this.setBytes(paramIndex, (byte[]) value);
            } else if (value instanceof BufferedImage) {
                byte[] ba = ImageUtils.writeImage((BufferedImage) value);
                this.setBytes(paramIndex, ba);
            } else if (JdbcTypeValidator.isJdbcSupported(value)) {
                 this.m_ps.setObject(paramIndex, value);
            } else  {
                byte[] ba = ImageUtils.writeSerializable(value);
                this.setBytes(paramIndex, ba);
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "Exception set Object param: "+paramIndex, ex);
            throw new BasicException(ex);
        }
    }
}
