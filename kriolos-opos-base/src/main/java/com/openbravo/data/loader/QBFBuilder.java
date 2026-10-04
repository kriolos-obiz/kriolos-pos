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
package com.openbravo.data.loader;

import com.openbravo.pos.forms.AppLocal;
import com.openbravo.basic.BasicException;
import java.util.logging.Logger;

/**
 *
 * @author adrian
 */
public class QBFBuilder implements ISQLBuilderStatic {

    private static final Logger LOGGER = Logger.getLogger("com.openbravo.data.loader.QBFBuilder");
    
    private final String m_sSentNullFilter;     // For null filter
    private final String m_sSentBeginPart;      // m_sSentBeginPart + ( filtro ) + m_sSentEndPart
    private final String m_sSentEndPart;
    private final String[] m_asFindFields;

    /**
     * 
     * @param tb
     * @param asFindFields 
     */
    public QBFBuilder(TableDefinition tb, String[] asFindFields) {
        StringBuilder sent = new StringBuilder();
        sent.append("select ");
        for (int i = 0; i < tb.getFields().length; i++) {
            if (i > 0) {
                sent.append(", ");
            }
            sent.append(tb.getFields()[i]);
        }
        sent.append(" from ");
        sent.append(tb.getTableName());
        m_sSentNullFilter = sent.toString();
        sent.append(" where ");
        m_sSentBeginPart = sent.toString();
        m_sSentEndPart = "";
        m_asFindFields = asFindFields;
    }
    /**
     *
     * @param sSentence SQL Statement
     * @param asFindFields
     */
    public QBFBuilder(String sSentence, String[] asFindFields) {
        int iPos = sSentence.indexOf("?(QBF_FILTER)");
        if (iPos < 0) {
            m_sSentBeginPart = sSentence;
            m_sSentEndPart = "";
            m_sSentNullFilter = sSentence;
        } else {
            m_sSentBeginPart = sSentence.substring(0, iPos);
            m_sSentEndPart = sSentence.substring(iPos + 13);
            m_sSentNullFilter = m_sSentBeginPart + "(1=1)" + m_sSentEndPart;
        }
        m_asFindFields = asFindFields;
    }

    /**
     *
     * @param sw
     * @param params
     * @return
     * @throws BasicException
     */
    @Override
    public String getSQL(SerializerWrite sw, Object params) throws BasicException {

        QBFDataWrite dwrite = new QBFDataWrite(m_asFindFields);
        if (sw == null || params == null) {
            return m_sSentNullFilter;
        } else {
            sw.writeValues(dwrite, params);
            String sFilter = dwrite.getFilter();
            if (sFilter.length() == 0) {
                return m_sSentNullFilter;
            } else {
                return m_sSentBeginPart + "(" + sFilter + ")" + m_sSentEndPart; // incluimos el filtro
            }
        }
    }

    private static class QBFDataWrite implements DataWrite {

        private final String[] m_asFindFields;
        private final QBFCompareEnum[] m_aiCondFields;
        private final String[] m_aParams;

        public QBFDataWrite(String[] asFindFields) {
            m_asFindFields = asFindFields;
            m_aiCondFields = new QBFCompareEnum[asFindFields.length];
            m_aParams = new String[asFindFields.length];

            for (int i = 0; i < m_aParams.length; i++) {
                m_aParams[i] = DataWriteUtils.getSQLValue((Object) null);
            }
        }

        @Override
        public void setDouble(int paramIndex, Double value) throws BasicException {
            if ((paramIndex - 1) % 2 == 0) {
                throw new BasicException(exceptionMessage(paramIndex, value));
            } else {
                m_aParams[(paramIndex - 1) / 2] = DataWriteUtils.getSQLValue(value);
            }
        }

        @Override
        public void setBoolean(int paramIndex, Boolean value) throws BasicException {
            if ((paramIndex - 1) % 2 == 0) {
                throw new BasicException(exceptionMessage(paramIndex, value));
            } else {
                m_aParams[(paramIndex - 1) / 2] = DataWriteUtils.getSQLValue(value);
            }
        }

        @Override
        public void setInt(int paramIndex, Integer value) throws BasicException {
            if ((paramIndex - 1) % 2 == 0) {
                throw new BasicException(exceptionMessage(paramIndex, value));
            } else {
                m_aParams[(paramIndex - 1) / 2] = DataWriteUtils.getSQLValue(value);
            }
        }

        @Override
        public void setString(int paramIndex, String value) throws BasicException {
            if ((paramIndex - 1) % 2 == 0) {
                throw new BasicException(exceptionMessage(paramIndex, value));
            } else {
                m_aParams[(paramIndex - 1) / 2] = DataWriteUtils.getSQLValue(value);
            }
        }

        @Override
        public void setTimestamp(int paramIndex, java.util.Date value) throws BasicException {
            if ((paramIndex - 1) % 2 == 0) {
                throw new BasicException(exceptionMessage(paramIndex, value));
            } else {
                m_aParams[(paramIndex - 1) / 2] = DataWriteUtils.getSQLValue(value);
            }
        }
//        public void setBinaryStream(int paramIndex, java.io.InputStream in, int length) throws DataException{
//            if ((paramIndex - 1) % 2 == 0) {
//                throw new DataException("Expected comparator for QBF");
//            } else {
//                throw new DataException("Param type not allowed");
//            }            
//        }

        @Override
        public void setBytes(int paramIndex, byte[] value) throws BasicException {
            if ((paramIndex - 1) % 2 == 0) {
                throw new BasicException(exceptionMessage(paramIndex, value));
            } else {
                throw new BasicException("Param type not allowed");
            }
        }

        @Override
        public void setObject(int paramIndex, Object value) throws BasicException {
            if ((paramIndex - 1) % 2 == 0) {
                if (value instanceof QBFCompareEnum) {
                    m_aiCondFields[(paramIndex - 1) / 2] = (QBFCompareEnum) value;
                } else {
                    throw new BasicException(exceptionMessage(paramIndex, value));
                }
            } else {
                m_aParams[(paramIndex - 1) / 2] = DataWriteUtils.getSQLValue(value);
            }
        }

        public String getFilter() {
            StringBuilder sFilter = new StringBuilder();

            String sItem;
            for (int i = 0; i < m_asFindFields.length; i++) {
                sItem = m_aiCondFields[i].getExpression(m_asFindFields[i], m_aParams[i]);
                if (sItem != null) {
                    if (sFilter.length() > 0) {
                        sFilter.append(" AND ");
                    }
                    sFilter.append(sItem);
                }
            }

            return sFilter.toString();
        }

        private void logMissinQBF(int paramIndex, Object value) {
            String valueS = value != null ? value.toString() : "null";
            LOGGER.warning(String.format("Expected comparator for QBF on paramIndex: %d value: %s",
                    paramIndex, valueS));
        }

        private String exceptionMessage(int paramIndex, Object value) {
            String valueS = value != null ? value.toString() : "null";
            return String.format("%s <<<DETAILED>>> paramIndex: %d value: %s",
                    AppLocal.getIntString("exception.nocompare"),
                    paramIndex,
                    valueS);
        }
    }
}
