package com.openbravo.pos.reports;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.BaseSentence;
import com.openbravo.data.loader.Datas;
import com.openbravo.data.loader.QBFBuilder;
import com.openbravo.data.loader.SerializerReadBasic;
import com.openbravo.data.loader.SerializerWrite;
import com.openbravo.data.loader.Session;
import com.openbravo.data.loader.StaticSentence;
import java.util.List;

/**
 * Implementation of ReportService.
 */
public class ReportServiceImpl implements ReportService {

    private final Session session;

    public ReportServiceImpl(Session session) {
        this.session = session;
    }

    @Override
    public List<Object[]> executeReport(String sentence, List<String> paramNames, List<Datas> fieldDatas, Object params,
            SerializerWrite sw) throws BasicException {
        // We need to construct the QBFBuilder.
        QBFBuilder qbf = new QBFBuilder(sentence, paramNames.toArray(new String[paramNames.size()]));

        // Use the passed SerializerWrite and field definitions
        StaticSentence s = new StaticSentence(session, qbf, sw,
                new SerializerReadBasic(fieldDatas.toArray(new Datas[fieldDatas.size()])));

        return s.list(params);
    }
}
