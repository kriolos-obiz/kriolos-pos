package com.openbravo.pos.payment;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.Datas;
import com.openbravo.data.loader.Session;
import com.openbravo.data.loader.SerializerReadClass;
import com.openbravo.data.loader.SerializerReadInteger;
import com.openbravo.data.loader.SerializerWriteBasic;
import com.openbravo.data.loader.StaticSentence;
import com.openbravo.pos.forms.BeanFactoryDataSingle;
import com.openbravo.pos.forms.DataLogicSystem;
import java.util.Date;

/**
 * Implementation of CashManagementService.
 */
public class CashManagementServiceImpl implements CashManagementService {

    private final Session session;
    private final DataLogicSystem dlSystem;

    public CashManagementServiceImpl(Session session, DataLogicSystem dlSystem) {
        this.session = session;
        this.dlSystem = dlSystem;
    }

    @Override
    public void closeCash(String host, int sequence, String money, Date dateEnd, int noSales) throws BasicException {
        // Update closedcash
        new StaticSentence(session,
                "UPDATE closedcash SET DATEEND = ?, NOSALES = ? WHERE HOST = ? AND MONEY = ?",
                new SerializerWriteBasic(new Datas[] {
                        Datas.TIMESTAMP,
                        Datas.INT,
                        Datas.STRING,
                        Datas.STRING }))
                .exec(new Object[] { dateEnd, noSales, host, money });

        // Note: The logic for creating the NEXT cash sequence is typically handled by
        // the AppView/JRootApp
        // after this method returns, or could be encapsulated here if we pass more
        // context.
        // For now, we replicate the specific UPDATE logic from JPanelCloseMoney.
    }

    @Override
    public CloseCash getCloseCashBySequence(String host, int sequence) throws BasicException {
        return (CloseCash) new StaticSentence(session,
                "SELECT money, host, hostsequence, datestart, dateend "
                        + "FROM closedcash "
                        + "WHERE hostsequence = ? AND dateend IS NOT NULL AND host = ?",
                new SerializerWriteBasic(new Datas[] { Datas.INT, Datas.STRING }),
                new SerializerReadClass(CloseCash.class))
                .find(new Object[] { sequence, host });
    }

    @Override
    public int getNumOfNoSales(Date startDate) throws BasicException {
        Object result = new StaticSentence(session,
                "SELECT COUNT(*) FROM draweropened WHERE TICKETID = 'No Sale' AND OPENDATE > ?",
                new SerializerWriteBasic(new Datas[] { Datas.TIMESTAMP }),
                com.openbravo.data.loader.SerializerReadInteger.INSTANCE)
                .find(new Object[] { startDate });
        return result == null ? 0 : ((Number) result).intValue();
    }

    @Override
    public int getNumOfRemovedLines(Date startDate) throws BasicException {
        Object result = new StaticSentence(session,
                "SELECT COUNT(*) FROM lineremoved WHERE REMOVEDDATE > ?",
                new SerializerWriteBasic(new Datas[] { Datas.TIMESTAMP }),
                com.openbravo.data.loader.SerializerReadInteger.INSTANCE)
                .find(new Object[] { startDate });
        return result == null ? 0 : ((Number) result).intValue();
    }

    @Override
    public int getNumOfVoidLines(Date startDate) throws BasicException {
        Object result = new StaticSentence(session,
                "SELECT COUNT(*) FROM lineremoved WHERE TICKETID = 'Void' AND REMOVEDDATE >= ?",
                new SerializerWriteBasic(new Datas[] { Datas.TIMESTAMP }),
                com.openbravo.data.loader.SerializerReadInteger.INSTANCE)
                .find(new Object[] { startDate });
        return result == null ? 0 : ((Number) result).intValue();
    }
}
