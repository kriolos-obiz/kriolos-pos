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

/**
 *
 * @author  adrian
 */
public abstract class QBFCompareEnum {
    
    /**
     *
     */
    public final static QBFCompareEnum COMP_NONE = new QBFCompareEnum(0, "qbf.none") { 
        @Override
        public String getExpression(String sField, String sSQLValue) { return null; }
    };

    /**
     *
     */
    public final static QBFCompareEnum COMP_ISNULL = new QBFCompareEnum(1, "qbf.null") {
        @Override
        public String getExpression(String sField, String sSQLValue) { return sField + " IS NULL"; }
    };

    /**
     *
     */
    public final static QBFCompareEnum COMP_ISNOTNULL = new QBFCompareEnum(2, "qbf.notnull") {
        @Override
        public String getExpression(String sField, String sSQLValue) { return sField + " IS NOT NULL"; }
    };

    /**
     *
     */
    public final static QBFCompareEnum COMP_RE = new QBFCompareEnum(3, "qbf.re") {
        @Override
        public String getExpression(String sField, String sSQLValue) { return sField + " LIKE " + sSQLValue; }
    };

    /**
     *
     */
    public final static QBFCompareEnum COMP_EQUALS = new QBFCompareEnum(3, "qbf.equals") {
        @Override
        public String getExpression(String sField, String sSQLValue) { return sField + " = " + sSQLValue; }
    };

    /**
     *
     */
    public final static QBFCompareEnum COMP_DISTINCT = new QBFCompareEnum(4, "qbf.distinct") {
        @Override
        public String getExpression(String sField, String sSQLValue) { return sField + " <> " + sSQLValue; }
    };

    /**
     *
     */
    public final static QBFCompareEnum COMP_GREATER = new QBFCompareEnum(5, "qbf.greater") {
        @Override
        public String getExpression(String sField, String sSQLValue) { return sField + " > " + sSQLValue; }
    };

    /**
     *
     */
    public final static QBFCompareEnum COMP_LESS = new QBFCompareEnum(6, "qbf.less") {
        @Override
        public String getExpression(String sField, String sSQLValue) { return sField + " < " + sSQLValue; }
    };

    /**
     *
     */
    public final static QBFCompareEnum COMP_GREATEROREQUALS = new QBFCompareEnum(7, "qbf.greaterequals") {
        @Override
        public String getExpression(String sField, String sSQLValue) { return sField + " >= " + sSQLValue; }
    };

    /**
     *
     */
    public final static QBFCompareEnum COMP_LESSOREQUALS = new QBFCompareEnum(8, "qbf.lessequals") {
        @Override
        public String getExpression(String sField, String sSQLValue) { return sField + " <= " + sSQLValue; }
    };
//    public final static QBFCompareEnum COMP_STARTSWITH = new QBFCompareEnum(9, "qbf.startswith") {
//        public String getExpression(String sField, String sSQLValue) { return sField + " LIKE " ... + sSQLValue; }
//    };
//    public final static int COMP_ENDSWITH = 12;
//    public final static int COMP_CONTAINS = 13;    
    
    private final int m_iValue; 
    private final String m_sKey;
    
    private QBFCompareEnum(int iValue, String sKey) {
        m_iValue = iValue;
        m_sKey = sKey;
    }

    /**
     *
     * @return
     */
    public int getCompareInt() {
        return m_iValue;
    }
    @Override
    public String toString() {
        return AppLocal.getIntString(m_sKey);
    }

    /**
     *
     * @param sField
     * @param sSQLValue
     * @return
     */
    public abstract String getExpression(String sField, String sSQLValue);
}
