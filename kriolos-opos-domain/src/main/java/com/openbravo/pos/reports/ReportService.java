package com.openbravo.pos.reports;

import com.openbravo.basic.BasicException;
import java.util.List;

import com.openbravo.data.loader.Datas;
import com.openbravo.data.loader.SerializerWrite;

/**
 * Service for generating reports.
 */
public interface ReportService {

    /**
     * Executes a report query.
     * 
     * @param sentence   SQL sentence with QBF placeholders.
     * @param paramNames List of parameter names for QBF.
     * @param fieldDatas List of field / return types.
     * @param params     Filtering parameters.
     * @param sw         SerializerWrite for the parameters.
     * @return List of object arrays representing the report rows.
     * @throws BasicException If an error occurs.
     */
    List<Object[]> executeReport(String sentence, List<String> paramNames, List<Datas> fieldDatas, Object params,
            SerializerWrite sw) throws BasicException;
}
