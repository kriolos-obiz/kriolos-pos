package com.openbravo.pos.payment;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.DataRead;
import com.openbravo.data.loader.SerializableRead;
import java.io.Serializable;
import java.util.Date;

public class CloseCash implements SerializableRead, Serializable {

    private String money;
    private String host;
    private Integer hostsequence;
    private Date datestart;
    private Date dateend;

    @Override
    public void readValues(DataRead dr) throws BasicException {
        money = dr.getString(1);
        host = dr.getString(2);
        hostsequence = dr.getInt(3);
        datestart = dr.getTimestamp(4);
        dateend = dr.getTimestamp(5);
    }

    public String getMoney() {
        return money;
    }

    public void setMoney(String money) {
        this.money = money;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getHostsequence() {
        return hostsequence;
    }

    public void setHostsequence(Integer hostsequence) {
        this.hostsequence = hostsequence;
    }

    public Date getDatestart() {
        return datestart;
    }

    public void setDatestart(Date datestart) {
        this.datestart = datestart;
    }

    public Date getDateend() {
        return dateend;
    }

    public void setDateend(Date dateend) {
        this.dateend = dateend;
    }
}
