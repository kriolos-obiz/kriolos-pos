package com.openbravo.pos.reports;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.*;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.BeanFactoryException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.sql.ResultSet;

/**
 * Inventory Report Bean that delegates to ReportService.
 */
public class InventoryReportBean extends PanelReportBean {

    private ReportService reportService;

    @Override
    public void init(AppView app) throws BeanFactoryException {
        super.init(app);
        reportService = (ReportService) app.getBean("com.openbravo.pos.reports.ReportService");
    }

    private String sentence;
    private List<String> paramnames = new ArrayList<>();
    private List<Datas> fielddatas = new ArrayList<>();
    private List<String> fieldnames = new ArrayList<>();

    @Override
    public void setSentence(String sentence) {
        this.sentence = sentence;
    }

    @Override
    public void addParameter(String name) {
        paramnames.add(name);
    }

    @Override
    public void addField(String name, Datas data) {
        fieldnames.add(name);
        fielddatas.add(data);
    }

    @Override
    protected BaseSentence getSentence() {
        return null;
    }

    private static class ListResultSet implements DataResultSet {
        private final List<Object[]> data;
        private int index = -1;

        public ListResultSet(List<Object[]> data) {
            this.data = data;
        }

        @Override
        public boolean next() throws BasicException {
            index++;
            return index < data.size();
        }

        @Override
        public Object getCurrent() throws BasicException {
            if (index >= 0 && index < data.size()) {
                return data.get(index);
            }
            return null;
        }

        @Override
        public void close() throws BasicException {
            // nothing
        }

        @Override
        public int updateCount() throws BasicException {
            return data.size();
        }

        @Override
        public Integer getInt(int columnIndex) throws BasicException {
            return 0;
        }

        @Override
        public String getString(int columnIndex) throws BasicException {
            return "";
        }

        @Override
        public Double getDouble(int columnIndex) throws BasicException {
            return 0.0;
        }

        @Override
        public Boolean getBoolean(int columnIndex) throws BasicException {
            return null;
        }

        @Override
        public Date getTimestamp(int columnIndex) throws BasicException {
            return null;
        }

        @Override
        public byte[] getBytes(int columnIndex) throws BasicException {
            return new byte[0];
        }

        @Override
        public Object getObject(int columnIndex) throws BasicException {
            return null;
        }

        @Override
        public DataField[] getDataField() throws BasicException {
            return new DataField[0];
        }
    }
}
