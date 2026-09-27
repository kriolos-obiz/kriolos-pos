package com.openbravo.pos.cash;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.Datas;
import com.openbravo.data.loader.PreparedSentence;
import com.openbravo.data.loader.SentenceExec;
import com.openbravo.data.loader.SentenceFind;
import com.openbravo.data.loader.Session;
import com.openbravo.data.loader.SerializerReadClass;
import com.openbravo.data.loader.SerializerReadInteger;
import com.openbravo.data.loader.SerializerReadString;
import com.openbravo.data.loader.SerializerWriteBasic;
import com.openbravo.data.loader.SerializerWriteString;
import com.openbravo.data.loader.StaticSentence;
import java.util.Date;

/**
 * Implementation of CashManagementService.
 */
public class CashManagementServiceImpl implements CashManagementService {

    private final Session session;

    public CashManagementServiceImpl(Session session) {
        this.session = session;
    }

    @Override
    public void addCloseCash(CashRegister cash) throws BasicException {
        final SentenceExec m_insertcash = new StaticSentence(this.session,
                "INSERT INTO closedcash(MONEY, HOST, HOSTSEQUENCE, DATESTART, DATEEND, NOSALES) "
                + "VALUES (?, ?, ?, ?, ?, 0)",
                new SerializerWriteBasic(new Datas[]{Datas.STRING, Datas.STRING, Datas.INT, Datas.TIMESTAMP, Datas.TIMESTAMP}));

        Object[] cashParamenter = new Object[]{cash.getMoney(), cash.getHost(), cash.getHostsequence(), cash.getStartDate(), cash.getEndDate()};
        m_insertcash.exec(cashParamenter);
    }

    @Override
    public void closeCash(String host, int sequence, String money, Date dateEnd, int noSales) throws BasicException {
        // Update closedcash
        new StaticSentence(session,
                "UPDATE closedcash SET DATEEND = ?, NOSALES = ? WHERE HOST = ? AND MONEY = ?",
                new SerializerWriteBasic(new Datas[]{
            Datas.TIMESTAMP,
            Datas.INT,
            Datas.STRING,
            Datas.STRING}))
                .exec(new Object[]{dateEnd, noSales, host, money});

        // Note: The logic for creating the NEXT cash sequence is typically handled by the AppView
        // after this method returns, or could be encapsulated here if we pass more context.
        // For now, we replicate the specific UPDATE logic from JPanelCloseMoney.
    }

    @Override
    public CashRegister getCloseCashBySequence(String host, int sequence) throws BasicException {
        return (CashRegister) new StaticSentence(session,
                "SELECT money, host, hostsequence, datestart, dateend, nosales "
                + "FROM closedcash "
                + "WHERE hostsequence = ? AND dateend IS NOT NULL AND host = ?",
                new SerializerWriteBasic(new Datas[]{Datas.INT, Datas.STRING}),
                new SerializerReadClass(CashRegister.class))
                .find(new Object[]{sequence, host});
    }

    public int getCloseCashSequenceByHost(String host) throws BasicException {
        final SentenceFind m_sequencecash = new PreparedSentence(this.session,
                "SELECT MAX(HOSTSEQUENCE) FROM closedcash WHERE HOST = ?",
                SerializerWriteString.INSTANCE,
                SerializerReadInteger.INSTANCE);

        Integer i = (Integer) m_sequencecash.find(host);
        return (i == null) ? 0 : i;
    }

    @Override
    public boolean isCashActive(String id) throws BasicException {

        return new PreparedSentence(this.session,
                "SELECT MONEY FROM closedcash WHERE DATEEND IS NULL AND MONEY = ?",
                SerializerWriteString.INSTANCE,
                SerializerReadString.INSTANCE).find(id)
                != null;
    }

    @Override
    public CashRegister getCloseCashByMoney(String moneyToken) throws BasicException {

        return (CashRegister) new PreparedSentence(this.session,
                "SELECT money, host, hostsequence, datestart, dateend, nosales "
                + "FROM closedcash WHERE MONEY = ?",
                SerializerWriteString.INSTANCE,
                new SerializerReadClass(CashRegister.class))
                .find(moneyToken);
    }

    @Override
    public int getNumOfNoSales(Date startDate) throws BasicException {
        Object result = new StaticSentence(session,
                "SELECT COUNT(*) FROM draweropened WHERE TICKETID = 'No Sale' AND OPENDATE > ?",
                new SerializerWriteBasic(new Datas[]{Datas.TIMESTAMP}),
                com.openbravo.data.loader.SerializerReadInteger.INSTANCE)
                .find(new Object[]{startDate});
        return result == null ? 0 : ((Number) result).intValue();
    }

    @Override
    public int getNumOfRemovedLines(Date startDate) throws BasicException {
        Object result = new StaticSentence(session,
                "SELECT COUNT(*) FROM lineremoved WHERE REMOVEDDATE > ?",
                new SerializerWriteBasic(new Datas[]{Datas.TIMESTAMP}),
                com.openbravo.data.loader.SerializerReadInteger.INSTANCE)
                .find(new Object[]{startDate});
        return result == null ? 0 : ((Number) result).intValue();
    }

    @Override
    public int getNumOfVoidLines(Date startDate) throws BasicException {
        Object result = new StaticSentence(session,
                "SELECT COUNT(*) FROM lineremoved WHERE TICKETID = 'Void' AND REMOVEDDATE >= ?",
                new SerializerWriteBasic(new Datas[]{Datas.TIMESTAMP}),
                com.openbravo.data.loader.SerializerReadInteger.INSTANCE)
                .find(new Object[]{startDate});
        return result == null ? 0 : ((Number) result).intValue();
    }
}
