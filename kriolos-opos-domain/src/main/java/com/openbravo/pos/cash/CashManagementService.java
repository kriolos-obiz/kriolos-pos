package com.openbravo.pos.cash;

import com.openbravo.basic.BasicException;
import java.util.Date;

/**
 * Service for cash management operations (opening/closing cash).
 */
public interface CashManagementService {

    void addCloseCash(CashRegister cash) throws BasicException;
    
    /**
     * Closes an active cash session using a validated command record.
     *
     * @param command close cash command
     * @throws BasicException on persistence error
     */
    void closeCash(CloseCashCommand command) throws BasicException;

    /**
     * Closes an active cash session.
     *
     * @param host terminal host identifier
     * @param sequence cash sequence number
     * @param money money token/ID
     * @param dateEnd closing timestamp
     * @param noSales count of no-sales
     * @throws BasicException on persistence error
     * @deprecated Use {@link #closeCash(CloseCashCommand)}
     */
    @Deprecated(since = "2.4.0", forRemoval = false)
    default void closeCash(String host, int sequence, String money, Date dateEnd, int noSales) throws BasicException {
        closeCash(CloseCashCommand.of(host, sequence, money, dateEnd, noSales));
    }
    
    boolean isCashActive(String id) throws BasicException;

    CashRegister getCloseCashBySequence(String host, int sequence) throws BasicException;
    
    CashRegister getCloseCashByMoney(String moneyToken) throws BasicException;
    
    int getCloseCashSequenceByHost(String host) throws BasicException;

    int getNumOfNoSales(Date startDate) throws BasicException;

    int getNumOfRemovedLines(Date startDate) throws BasicException;

    int getNumOfVoidLines(Date startDate) throws BasicException;

    /**
     * Persists an immutable WORM financial snapshot for a closed cash session.
     *
     * @param snapshot the snapshot to persist
     * @throws BasicException if a database error occurs
     */
    void saveSnapshot(com.openbravo.pos.domain.audit.ClosedCashSnapshot snapshot) throws BasicException;

    /**
     * Retrieves an immutable financial snapshot by closed cash session ID.
     *
     * @param closedCashId closed cash session identifier
     * @return snapshot or null if not found
     * @throws BasicException if a database error occurs
     */
    com.openbravo.pos.domain.audit.ClosedCashSnapshot getSnapshotByClosedCashId(String closedCashId) throws BasicException;

    /**
     * Retrieves an immutable financial snapshot by host and sequence.
     *
     * @param host terminal host name
     * @param sequence closure sequence number
     * @return snapshot or null if not found
     * @throws BasicException if a database error occurs
     */
    com.openbravo.pos.domain.audit.ClosedCashSnapshot getSnapshotBySequence(String host, int sequence) throws BasicException;
}
