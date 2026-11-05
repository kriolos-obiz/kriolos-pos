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
import java.util.Objects;

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
    public void closeCash(CloseCashCommand command) throws BasicException {
        Objects.requireNonNull(command, "CloseCashCommand must not be null");
        new StaticSentence(session,
                "UPDATE closedcash SET DATEEND = ?, NOSALES = ? WHERE HOST = ? AND MONEY = ?",
                new SerializerWriteBasic(new Datas[]{
            Datas.TIMESTAMP,
            Datas.INT,
            Datas.STRING,
            Datas.STRING}))
                .exec(new Object[]{command.dateEnd(), command.noSales(), command.host(), command.money()});
    }

    @Override
    @Deprecated(since = "2.4.0", forRemoval = false)
    public void closeCash(String host, int sequence, String money, Date dateEnd, int noSales) throws BasicException {
        closeCash(CloseCashCommand.of(host, sequence, money, dateEnd, noSales));
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

    @Override
    public void saveSnapshot(com.openbravo.pos.domain.audit.ClosedCashSnapshot snapshot) throws BasicException {
        new StaticSentence(session,
                "INSERT INTO closedcash_snapshot ("
                + "id, closedcash_id, host, hostsequence, datestart, dateend, "
                + "total_gross_sales, total_net_sales, total_taxes, total_payments, cash_in_drawer, cash_difference, "
                + "payload, payload_format, payload_version, payload_checksum, "
                + "series_id, document_no, journal_event_id, previous_checksum, "
                + "app_version, created_by, created_at) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                new SerializerWriteBasic(new Datas[]{
                    Datas.STRING, Datas.STRING, Datas.STRING, Datas.INT, Datas.TIMESTAMP, Datas.TIMESTAMP,
                    Datas.DOUBLE, Datas.DOUBLE, Datas.DOUBLE, Datas.DOUBLE, Datas.DOUBLE, Datas.DOUBLE,
                    Datas.STRING, Datas.STRING, Datas.INT, Datas.STRING,
                    Datas.STRING, Datas.STRING, Datas.STRING, Datas.STRING,
                    Datas.STRING, Datas.STRING, Datas.TIMESTAMP
                }))
                .exec(new Object[]{
                    snapshot.getId(),
                    snapshot.getClosedCashId(),
                    snapshot.getHost(),
                    snapshot.getHostSequence(),
                    snapshot.getDateStart(),
                    snapshot.getDateEnd(),
                    snapshot.getTotalGrossSales() != null ? snapshot.getTotalGrossSales().doubleValue() : 0.0,
                    snapshot.getTotalNetSales() != null ? snapshot.getTotalNetSales().doubleValue() : 0.0,
                    snapshot.getTotalTaxes() != null ? snapshot.getTotalTaxes().doubleValue() : 0.0,
                    snapshot.getTotalPayments() != null ? snapshot.getTotalPayments().doubleValue() : 0.0,
                    snapshot.getCashInDrawer() != null ? snapshot.getCashInDrawer().doubleValue() : 0.0,
                    snapshot.getCashDifference() != null ? snapshot.getCashDifference().doubleValue() : 0.0,
                    snapshot.getPayload(),
                    snapshot.getPayloadFormat(),
                    snapshot.getPayloadVersion(),
                    snapshot.getPayloadChecksum(),
                    snapshot.getSeriesId(),
                    snapshot.getDocumentNo(),
                    snapshot.getJournalEventId(),
                    snapshot.getPreviousChecksum(),
                    snapshot.getAppVersion(),
                    snapshot.getCreatedBy(),
                    snapshot.getCreatedAt()
                });
    }

    @Override
    public com.openbravo.pos.domain.audit.ClosedCashSnapshot getSnapshotByClosedCashId(String closedCashId) throws BasicException {
        return (com.openbravo.pos.domain.audit.ClosedCashSnapshot) new PreparedSentence(this.session,
                "SELECT id, closedcash_id, host, hostsequence, datestart, dateend, "
                + "total_gross_sales, total_net_sales, total_taxes, total_payments, cash_in_drawer, cash_difference, "
                + "payload, payload_format, payload_version, payload_checksum, "
                + "series_id, document_no, journal_event_id, previous_checksum, "
                + "app_version, created_by, created_at "
                + "FROM closedcash_snapshot WHERE closedcash_id = ?",
                SerializerWriteString.INSTANCE,
                com.openbravo.pos.domain.audit.ClosedCashSnapshotSerializerRead.INSTANCE)
                .find(closedCashId);
    }

    @Override
    public com.openbravo.pos.domain.audit.ClosedCashSnapshot getSnapshotBySequence(String host, int sequence) throws BasicException {
        return (com.openbravo.pos.domain.audit.ClosedCashSnapshot) new StaticSentence(session,
                "SELECT id, closedcash_id, host, hostsequence, datestart, dateend, "
                + "total_gross_sales, total_net_sales, total_taxes, total_payments, cash_in_drawer, cash_difference, "
                + "payload, payload_format, payload_version, payload_checksum, "
                + "series_id, document_no, journal_event_id, previous_checksum, "
                + "app_version, created_by, created_at "
                + "FROM closedcash_snapshot WHERE host = ? AND hostsequence = ?",
                new SerializerWriteBasic(new Datas[]{Datas.STRING, Datas.INT}),
                com.openbravo.pos.domain.audit.ClosedCashSnapshotSerializerRead.INSTANCE)
                .find(new Object[]{host, sequence});
    }
}
