package com.openbravo.pos.reports;

import com.openbravo.basic.BasicException;
import java.util.Date;

/**
 * Service for generating financial reports.
 */
public interface FinancialReportService {

    FinancialReport getFinancialReport(String money, Date dateStart, Date dateEnd) throws BasicException;
}
