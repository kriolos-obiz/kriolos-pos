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
import com.openbravo.data.loader.Transaction;
import com.openbravo.data.user.SaveProvider;
import com.openbravo.pos.container.BeanFactoryDataSingle;
import java.util.Date;

/**
 * Data access logic for treasury, cash drawer movements, and payment sequence numbering.
 */
public class DataLogicPayments extends BeanFactoryDataSingle implements TreasuryService {

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

            @Override
            public int execInTransaction(Object params) throws BasicException {
                if (params instanceof PaymentMovement movement) {
                    recordPaymentMovement(movement);
                    return 1;
                }
                return super.execInTransaction(params);
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

            @Override
            public int execInTransaction(Object params) throws BasicException {
                if (params instanceof PaymentMovement movement) {
                    deletePaymentMovement(movement);
                    return 1;
                }
                return super.execInTransaction(params);
            }
        };
    }

    @Override
    public void recordPaymentMovement(RecordPaymentMovementCommand command) throws BasicException {
        if (command == null) {
            return;
        }
        recordPaymentMovement(new PaymentMovement(
                command.receiptId(),
                command.activeCashIndex(),
                command.date(),
                command.paymentId(),
                command.reason(),
                command.total(),
                command.notes()
        ));
    }

    @Override
    public void recordPaymentMovement(PaymentMovement movement) throws BasicException {
        if (movement == null) {
            return;
        }
        new Transaction<Integer>(sessionDB) {
            @Override
            public Integer transact() throws BasicException {
                new PreparedSentence(sessionDB,
                        "INSERT INTO receipts (ID, MONEY, DATENEW) VALUES (?, ?, ?)",
                        PaymentMovement.getSerializerReceiptInsert())
                        .exec(movement);
                return new PreparedSentence(sessionDB,
                        "INSERT INTO payments (ID, RECEIPT, PAYMENT, TOTAL, NOTES) VALUES (?, ?, ?, ?, ?)",
                        PaymentMovement.getSerializerPaymentInsert())
                        .exec(movement);
            }
        }.execute();
    }

    @Override
    @Deprecated
    public void recordPaymentMovement(
            String receiptId,
            String activeCashIndex,
            Date date,
            String paymentId,
            String reason,
            double total,
            String notes
    ) throws BasicException {
        recordPaymentMovement(RecordPaymentMovementCommand.of(
                receiptId, activeCashIndex, date, paymentId, reason, total, notes));
    }

    @Override
    public void deletePaymentMovement(DeletePaymentMovementCommand command) throws BasicException {
        if (command == null) {
            return;
        }
        deletePaymentMovement(new PaymentMovement(
                command.receiptId(),
                "",
                null,
                command.paymentId(),
                "",
                0.0,
                ""
        ));
    }

    public void deletePaymentMovement(PaymentMovement movement) throws BasicException {
        if (movement == null) {
            return;
        }
        new Transaction<Integer>(sessionDB) {
            @Override
            public Integer transact() throws BasicException {
                new PreparedSentence(sessionDB,
                        "DELETE FROM payments WHERE ID = ?",
                        PaymentMovement.getSerializerPaymentDelete())
                        .exec(movement);
                return new PreparedSentence(sessionDB,
                        "DELETE FROM receipts WHERE ID = ?",
                        PaymentMovement.getSerializerReceiptDelete())
                        .exec(movement);
            }
        }.execute();
    }

    @Override
    @Deprecated
    public void deletePaymentMovement(String receiptId, String paymentId) throws BasicException {
        deletePaymentMovement(DeletePaymentMovementCommand.of(receiptId, paymentId));
    }

    @Override
    public SaveProvider<PaymentMovement> getPaymentMovementSaveProvider() {
        return new SaveProvider<PaymentMovement>() {
            @Override
            public boolean canInsert() {
                return true;
            }

            @Override
            public int insertData(PaymentMovement value) throws BasicException {
                recordPaymentMovement(value);
                return 1;
            }

            @Override
            public boolean canDelete() {
                return true;
            }

            @Override
            public int deleteData(PaymentMovement value) throws BasicException {
                deletePaymentMovement(value);
                return 1;
            }

            @Override
            public boolean canUpdate() {
                return false;
            }

            @Override
            public int updateData(PaymentMovement value) {
                throw new UnsupportedOperationException("Cash drawer movements are append-only.");
            }
        };
    }

    @Override
    public final Integer getNextTicketPaymentIndex() throws BasicException {
        return (Integer) sessionDB.DB.getSequenceSentence(sessionDB, "ticketsnum_payment").find();
    }
}
