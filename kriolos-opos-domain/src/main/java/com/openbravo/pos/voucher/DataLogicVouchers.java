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
package com.openbravo.pos.voucher;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.DataRead;
import com.openbravo.data.loader.Datas;
import com.openbravo.data.loader.PreparedSentence;
import com.openbravo.data.loader.SerializerRead;
import com.openbravo.data.loader.SerializerWriteString;
import com.openbravo.data.loader.Session;
import com.openbravo.data.loader.StaticSentence;
import com.openbravo.data.loader.TableDefinition;
import com.openbravo.format.Formats;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.BeanFactoryDataSingle;
import java.util.List;

/**
 * Legacy DataLogic provider implementing {@link VoucherService}.
 *
 * @author poolborges
 */
public class DataLogicVouchers extends BeanFactoryDataSingle implements VoucherService {

    private Session sessionDB;
    private TableDefinition tableVouchers;

    @Override
    public void init(Session sessionDB) {
        this.sessionDB = sessionDB;
    }

    /**
     * Returns the underlying session.
     *
     * @return database session
     */
    public Session getSession() {
        return sessionDB;
    }

    // <editor-fold defaultstate="collapsed" desc="Voucher MANAGEMENT">

    @Override
    public VoucherInfo getVoucher(String id) throws BasicException {
        return getVoucherInfo(id);
    }

    @Override
    public VoucherInfo getVoucherAll(String id) throws BasicException {
        return getVoucherInfoAll(id);
    }

    @Override
    public String findLastVoucherNumber(String prefix) throws BasicException {
        return (String) getVoucherNumber().find(prefix);
    }

    @Override
    public int deactivateVoucher(String voucherNumber) throws BasicException {
        return updateVoucherNonActive(voucherNumber, this.sessionDB);
    }

    @Override
    public TableDefinition getTableVouchers() {
        if (tableVouchers == null) {
            tableVouchers = new TableDefinition(sessionDB,
                    "vouchers",
                    new String[]{"ID", "VOUCHER_NUMBER", "CUSTOMER", "AMOUNT", "STATUS"},
                    new String[]{"ID", AppLocal.getIntString("label.Number"), AppLocal.getIntString("label.customer"), AppLocal.getIntString("label.paymenttotal"), AppLocal.getIntString("label.status")},
                    new Datas[]{Datas.STRING, Datas.STRING, Datas.STRING, Datas.DOUBLE, Datas.STRING},
                    new Formats[]{Formats.STRING, Formats.STRING, Formats.STRING, Formats.CURRENCY, Formats.NULL},
                    new int[]{0}
            );
        }
        return tableVouchers;
    }

    /**
     * @deprecated Use {@link #findLastVoucherNumber(String)} or {@link #generateNextVoucherNumber()} instead.
     */
    @Deprecated
    public final PreparedSentence getVoucherNumber() {
        return new PreparedSentence(this.sessionDB,
                "SELECT SUBSTRING(MAX(VOUCHER_NUMBER),10,3) AS LAST_NUMBER FROM vouchers "
                + "WHERE SUBSTRING(VOUCHER_NUMBER,1,8) = ?",
                SerializerWriteString.INSTANCE, (SerializerRead<String>) (DataRead dr) -> dr.getString(1));
    }

    /**
     * @deprecated Use {@link #getVoucher(String)} instead.
     */
    @Deprecated
    public final VoucherInfo getVoucherInfo(String id) throws BasicException {
        return (VoucherInfo) new PreparedSentence(this.sessionDB,
                "SELECT vouchers.ID, VOUCHER_NUMBER, CUSTOMER, "
                + "customers.NAME, AMOUNT, STATUS "
                + "FROM vouchers "
                + "JOIN customers ON customers.id = vouchers.CUSTOMER "
                + "WHERE STATUS='A' AND vouchers.ID=?",
                 SerializerWriteString.INSTANCE,
                VoucherInfo.getSerializerRead()).<VoucherInfo>find(id);
    }

    /**
     * @deprecated Use {@link #getVoucherAll(String)} instead.
     */
    @Deprecated
    public final VoucherInfo getVoucherInfoAll(String id) throws BasicException {
        return (VoucherInfo) new PreparedSentence(this.sessionDB,
                "SELECT vouchers.ID, VOUCHER_NUMBER, CUSTOMER, "
                + "customers.NAME, AMOUNT, STATUS "
                + "FROM vouchers "
                + "JOIN customers ON customers.id = vouchers.CUSTOMER "
                + "WHERE vouchers.ID=?",
                SerializerWriteString.INSTANCE,
                VoucherInfo.getSerializerRead()).<VoucherInfo>find(id);
    }

    @Override
    public final List<VoucherInfo> getVoucherList() throws BasicException {
        return new StaticSentence(sessionDB,
                "SELECT vouchers.ID,vouchers.VOUCHER_NUMBER,vouchers.CUSTOMER, "
                + "customers.NAME,AMOUNT, STATUS "
                + "FROM vouchers   "
                + "JOIN customers ON customers.id = vouchers.CUSTOMER  "
                + "WHERE STATUS='A' "
                + "ORDER BY vouchers.VOUCHER_NUMBER ASC",
                null, VoucherInfo.getSerializerRead()).list();
    }

    public final static int updateVoucherNonActive(String voucherNumber, Session sessionDB) throws BasicException  {
        return new PreparedSentence(sessionDB,
                "UPDATE vouchers SET STATUS = 'D' "
                + "WHERE VOUCHER_NUMBER = ?",
                SerializerWriteString.INSTANCE).exec(voucherNumber);
    }
    // </editor-fold>
}
