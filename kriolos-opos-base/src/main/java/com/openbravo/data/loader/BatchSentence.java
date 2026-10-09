//    KriolOS POS
//    Copyright (c) 2019-2023 KriolOS
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program.  If not, see <http://www.gnu.org/licenses/>.
package com.openbravo.data.loader;

import com.openbravo.pos.forms.AppLocal;
import com.openbravo.basic.BasicException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author JG uniCenta
 */
public abstract class BatchSentence extends BaseSentence {

    private static final String L10N_EXCEPTION_NODATASET = "exception.nodataset";

    protected Session session;
    protected HashMap<String, String> parameters;

    public BatchSentence(Session session) {
        this.session = session;
        this.parameters = new HashMap<>();
    }

    public void putParameter(String name, String replacement) {
        parameters.put(name, replacement);
    }

    protected abstract Reader getReader() throws BasicException;

    public class ExceptionsResultSet implements DataResultSet<BasicException> {

        private List<BasicException> excepList;
        private int m_iIndex;

        public ExceptionsResultSet(List<BasicException> excepList) {
            this.excepList = excepList;
            m_iIndex = -1;
        }

        @Override
        public Integer getInt(int columnIndex) throws BasicException {
            throw new BasicException(AppLocal.getIntString(L10N_EXCEPTION_NODATASET));
        }

        @Override
        public String getString(int columnIndex) throws BasicException {
            throw new BasicException(AppLocal.getIntString(L10N_EXCEPTION_NODATASET));
        }

        @Override
        public Double getDouble(int columnIndex) throws BasicException {
            throw new BasicException(AppLocal.getIntString(L10N_EXCEPTION_NODATASET));
        }

        @Override
        public Boolean getBoolean(int columnIndex) throws BasicException {
            throw new BasicException(AppLocal.getIntString(L10N_EXCEPTION_NODATASET));
        }

        @Override
        public java.util.Date getTimestamp(int columnIndex) throws BasicException {
            throw new BasicException(AppLocal.getIntString(L10N_EXCEPTION_NODATASET));
        }
        
        @Override
        public Date getDate(int columnIndex) throws BasicException {
            throw new BasicException(AppLocal.getIntString(L10N_EXCEPTION_NODATASET));
        }

        @Override
        public byte[] getBytes(int columnIndex) throws BasicException {
            throw new BasicException(AppLocal.getIntString(L10N_EXCEPTION_NODATASET));
        }

        @Override
        public Object getObject(int columnIndex) throws BasicException {
            throw new BasicException(AppLocal.getIntString(L10N_EXCEPTION_NODATASET));
        }

        @Override
        public DataField[] getDataField() throws BasicException {
            throw new BasicException(AppLocal.getIntString(L10N_EXCEPTION_NODATASET));
        }

        @Override
        public BasicException getCurrent() throws BasicException {
            if (m_iIndex < 0 || m_iIndex >= excepList.size()) {
                throw new BasicException(AppLocal.getIntString("exception.outofbounds"));
            } else {
                return excepList.get(m_iIndex);
            }
        }

        @Override
        public boolean next() throws BasicException {
            return ++m_iIndex < excepList.size();
        }

        @Override
        public void close() throws BasicException {
        }

        @Override
        public int updateCount() {
            return 0;
        }
    }

    @Override
    public final void closeExec() throws BasicException {
    }

    @Override
    public final DataResultSet moreResults() throws BasicException {
        return null;
    }

    @Override
    public DataResultSet openExec(Object params) throws BasicException {

        BufferedReader br = new BufferedReader(getReader());

        String sLine;
        StringBuffer sSentence = new StringBuffer();
        List<BasicException> aExceptions = new ArrayList<>();

        try {
            while ((sLine = br.readLine()) != null) {
                sLine = sLine.trim();
                if (!sLine.equals("") && !sLine.startsWith("--")) {
                    // No es un comentario ni linea vacia
                    if (sLine.endsWith(";")) {
                        // ha terminado la sentencia
                        sSentence.append(sLine.substring(0, sLine.length() - 1));

                        // File parameters
                        Pattern pattern = Pattern.compile("\\$(\\w+)\\{([^}]*)\\}");
                        Matcher matcher = pattern.matcher(sSentence.toString());
                        List<Object> paramlist = new ArrayList<>();

                        // Replace all occurrences of pattern in input
                        StringBuffer buf = new StringBuffer();
                        while (matcher.find()) {
                            if ("FILE".equals(matcher.group(1))) {
                                paramlist.add(ImageUtils.getBytesFromClasspath(matcher.group(2)));
                                matcher.appendReplacement(buf, "?");
                            } else {
                                String replacement = parameters.get(matcher.group(1));
                                if (replacement == null) {
                                    matcher.appendReplacement(buf, Matcher.quoteReplacement(matcher.group(0)));
                                } else {
                                    paramlist.add(replacement);
                                    matcher.appendReplacement(buf, "?");
                                }
                            }
                        }
                        matcher.appendTail(buf);

                        // La disparo
                        try {
                            BaseSentence sent;
                            if (paramlist.isEmpty()) {
                                sent = new StaticSentence(session, buf.toString());
                                sent.exec();
                            } else {
                                sent = new PreparedSentence(session, buf.toString(), SerializerWriteBuilder.INSTANCE);
                                sent.exec(new VarParams(paramlist));
                            }
                        } catch (BasicException eD) {
                            aExceptions.add(eD);
                        }
                        sSentence = new StringBuffer();

                    } else {
                        // la sentencia continua en la linea siguiente
                        sSentence.append(sLine);
                    }
                }
            }

            br.close();

        } catch (IOException eIO) {
            throw new BasicException(AppLocal.getIntString("exception.noreadfile"), eIO);
        }

        if (sSentence.length() > 0) {
            // ha quedado una sentencia inacabada
            aExceptions.add(new BasicException(AppLocal.getIntString("exception.nofinishedfile")));
        }

        return new ExceptionsResultSet(aExceptions);
    }

    private static class VarParams implements SerializableWrite {

        private List<Object> paramList;

        public VarParams(List<Object> paramList) {
            this.paramList = paramList;
        }

        @Override
        public void writeValues(DataWrite dp) throws BasicException {
            for (int i = 0; i < paramList.size(); i++) {
                Object v = paramList.get(i);
                if (v instanceof String) {
                    dp.setString(i + 1, (String) v);
                } else if (v instanceof byte[]) {
                    dp.setBytes(i + 1, (byte[]) paramList.get(i));
                } else {
                    dp.setObject(i + 1, v);
                }
            }
        }
    }
}
