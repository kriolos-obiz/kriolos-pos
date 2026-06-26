package com.openbravo.pos.payment;

import com.openbravo.basic.BasicException;
import java.util.Date;

/**
 * Service for cash management operations (opening/closing cash).
 */
public interface CashManagementService {

    void closeCash(String host, int sequence, String money, Date dateEnd, int noSales) throws BasicException;

    CloseCash getCloseCashBySequence(String host, int sequence) throws BasicException;

    int getNumOfNoSales(Date startDate) throws BasicException;

    int getNumOfRemovedLines(Date startDate) throws BasicException;

    int getNumOfVoidLines(Date startDate) throws BasicException;
}
