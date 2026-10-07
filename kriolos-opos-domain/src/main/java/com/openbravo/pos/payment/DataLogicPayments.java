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

package com.openbravo.pos.payment;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.Datas;
import com.openbravo.data.loader.PreparedSentence;
import com.openbravo.data.loader.SentenceExec;
import com.openbravo.data.loader.SentenceExecTransaction;
import com.openbravo.data.loader.SerializerWriteBasicExt;
import com.openbravo.data.loader.Session;
import com.openbravo.pos.forms.BeanFactoryDataSingle;

/**
 * Data access logic for treasury, cash drawer movements, and payment sequence numbering.
 */
public class DataLogicPayments extends BeanFactoryDataSingle {

    private Session sessionDB;
    protected Datas[] paymenttabledatas;

    @Override
    public void init(Session sessionDB) {
        this.sessionDB = sessionDB;
        this.paymenttabledatas = new Datas[]{
            Datas.STRING, Datas.STRING, Datas.TIMESTAMP,
            Datas.STRING, Datas.STRING, Datas.DOUBLE,
            Datas.STRING
        };
    }

    public final SentenceExec getPaymentMovementInsert() {
        return new SentenceExecTransaction(sessionDB) {
            @Override
            public int execInTransaction(Object[] params) throws BasicException {
                new PreparedSentence(sessionDB,
                        "INSERT INTO receipts (ID, MONEY, DATENEW) "
                        + "VALUES (?, ?, ?)",
                        new SerializerWriteBasicExt(paymenttabledatas,
                                new int[]{0, 1, 2}))
                        .exec(params);
                return new PreparedSentence(sessionDB,
                        "INSERT INTO payments (ID, RECEIPT, PAYMENT, TOTAL, NOTES) "
                        + "VALUES (?, ?, ?, ?, ?)",
                        new SerializerWriteBasicExt(paymenttabledatas,
                                new int[]{3, 0, 4, 5, 6}))
                        .exec(params);
            }
        };
    }

    public final SentenceExec getPaymentMovementDelete() {
        return new SentenceExecTransaction(sessionDB) {
            @Override
            public int execInTransaction(Object[] params) throws BasicException {
                new PreparedSentence(sessionDB,
                        "DELETE FROM payments WHERE ID = ?",
                        new SerializerWriteBasicExt(paymenttabledatas, new int[]{3}))
                        .exec(params);
                return new PreparedSentence(sessionDB,
                        "DELETE FROM receipts WHERE ID = ?",
                        new SerializerWriteBasicExt(paymenttabledatas, new int[]{0}))
                        .exec(params);
            }
        };
    }

    public final Integer getNextTicketPaymentIndex() throws BasicException {
        return (Integer) sessionDB.DB.getSequenceSentence(sessionDB, "ticketsnum_payment").find();
    }
}
