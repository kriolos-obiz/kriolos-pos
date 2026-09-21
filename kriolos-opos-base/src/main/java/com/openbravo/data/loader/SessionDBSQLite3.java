package com.openbravo.data.loader;

public class SessionDBSQLite3 implements SessionDB {

    @Override
    public String TRUE() {
        return "1";
    }

    @Override
    public String FALSE() {
        return "0";
    }

    @Override
    public String INTEGER_NULL() {
        return "CAST(NULL AS INTEGER)";
    }

    @Override
    public String CHAR_NULL() {
        return "CAST(NULL AS CHAR(1))";
    }

    @Override
    public String getName() {
        return "SQLite";
    }

    @Override
    public SentenceFind getSequenceSentence(Session s, String sequence) {
        return new SequenceForGeneric(s, sequence);
    }

    @Override
    public SentenceExec resetSequenceSentence(Session s, String sequence){
        var seq = new SequenceForGeneric(s, sequence);
        return seq.reset();  
    }
}
