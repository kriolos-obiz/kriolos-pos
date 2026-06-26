//    KrOS POS
//    Copyright (c) 2019-2023 KriolOS
//    
//
//     
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
//    along with KrOS POS.  If not, see <http://www.gnu.org/licenses/>.
package com.openbravo.pos.ticket;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.DataRead;
import com.openbravo.data.loader.SerializableRead;
import com.openbravo.format.Formats;
import com.openbravo.pos.customers.CustomerInfoExt;
import com.openbravo.pos.forms.AppConfig;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.payment.PaymentInfo;
import com.openbravo.pos.payment.PaymentInfoMagcard;
import com.openbravo.pos.payment.PaymentInfoTicket;
import com.openbravo.pos.util.StringUtils;
import java.io.*;
import java.util.*;

/**
 *
 * @author adrianromero
 */
public final class TicketInfo implements SerializableRead, Externalizable {

    private static final long serialVersionUID = 2765650092387265178L;

    /**
     * TICKET TYPES: RECEIPT NORMAL
     */
    public static final int RECEIPT_NORMAL = 0;
    /**
     * TICKET TYPES: RECEIPT REFOUND
     */
    public static final int RECEIPT_REFUND = 1;
    /**
     * TICKET TYPES: RECEIPT PAYMENT
     */
    public static final int RECEIPT_PAYMENT = 2;
    /**
     * TICKET TYPES: RECEIPT NO SALES
     */
    public static final int RECEIPT_NOSALE = 3;

    /**
     * REFOUND TYPES: NON-REFOUND
     */
    public static final int REFUND_NOT = 0; // Non-refunded ticket 
    /**
     * REFOUND TYPES: PARTIAL REFOUND
     */
    public static final int REFUND_PARTIAL = 1; //Partial Refound 
    /**
     * REFOUND TYPES: FULL REFOUND
     */
    public static final int REFUND_ALL = 2; //Full Refound

    private String host;
    private String id;
    private int ticketType;
    private int ticketId;
    private int pickupId;
    private Date creationDate;
    private Properties attributes;
    private UserInfo userInfo;
    private Double multiply;
    private CustomerInfoExt customer;
    private String m_sActiveCash;
    private List<TicketLineInfo> ticketLines;
    private List<PaymentInfo> payments;
    private List<TicketTaxInfo> taxes;
    private final String response;
    private String loyaltyCardNumber;
    private Boolean isOldTicket;
    private boolean isTip;
    private PaymentInfoTicket m_paymentInfo;
    private boolean isProcessed;
    private final String locked;
    private int ticketStatus;

    /**
     * Creates new TicketModel
     */
    public TicketInfo() {
        id = UUID.randomUUID().toString();
        ticketType = RECEIPT_NORMAL;
        ticketId = 0;
        creationDate = new Date();
        attributes = new Properties();
        userInfo = null;
        customer = null;
        m_sActiveCash = null;
        ticketLines = new ArrayList<>();
        payments = new ArrayList<>();
        taxes = null;
        response = null;
        isOldTicket = false;

        isTip = Boolean.valueOf(getConfig().getProperty("machine.showTip"));
        isProcessed = false;
        locked = null;
        ticketStatus = 0;
        this.host = getConfig().getProperty("machine.hostname");
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(id);
        out.writeInt(ticketType);
        out.writeInt(ticketId);
        out.writeObject(customer);
        out.writeObject(creationDate);
        out.writeObject(attributes);
        out.writeObject(ticketLines);
        out.writeInt(ticketStatus);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        id = (String) in.readObject();
        ticketType = in.readInt();
        ticketId = in.readInt();
        customer = (CustomerInfoExt) in.readObject();
        creationDate = (Date) in.readObject();
        attributes = (Properties) in.readObject();
        ticketLines = (List<TicketLineInfo>) in.readObject();
        ticketStatus = in.readInt();
        userInfo = null;
        m_sActiveCash = null;
        payments = new ArrayList<>();
        taxes = null;
    }

    /**
     *
     * @param dr
     * @throws BasicException
     */
    @Override
    public void readValues(DataRead dr) throws BasicException {
        id = dr.getString(1);
        ticketType = dr.getInt(2);
        ticketId = dr.getInt(3);
        creationDate = dr.getTimestamp(4);
        m_sActiveCash = dr.getString(5);
        try {
            byte[] attByteArray = dr.getBytes(6);
            if (attByteArray != null) {
                attributes.loadFromXML(new ByteArrayInputStream(attByteArray));
            }
        } catch (IOException e) {
        }
        userInfo = new UserInfo(dr.getString(7), dr.getString(8));
        customer = new CustomerInfoExt(dr.getString(9));
        ticketLines = new ArrayList<>();
        payments = new ArrayList<>();
        taxes = null;

        ticketStatus = dr.getInt(10);

    }

    /**
     *
     * @return
     */
    public TicketInfo copyTicket() {
        TicketInfo t = new TicketInfo();

        t.ticketType = ticketType;
        t.ticketId = ticketId;
        t.creationDate = creationDate;
        t.m_sActiveCash = m_sActiveCash;
        t.attributes = (Properties) attributes.clone();
        t.userInfo = userInfo;
        t.customer = customer;

        t.ticketLines = new ArrayList<>();
        ticketLines.forEach((l) -> {
            t.ticketLines.add(l.copyTicketLine());
        });
        t.refreshLines();

        t.payments = new LinkedList<>();
        payments.forEach((p) -> {
            t.payments.add(p.copyPayment());
        });
        t.isOldTicket = isOldTicket;
        // taxes are not copied, must be calculated again.

        t.ticketStatus = ticketStatus;

        return t;
    }

    public String getId() {
        return id;
    }

    public int getTicketType() {
        return ticketType;
    }

    public void setTicketType(int tickettype) {
        this.ticketType = tickettype;
    }

    public int getTicketId() {
        return ticketId;
    }

    public void setTicketId(int iTicketId) {
        ticketId = iTicketId;
    }

    public int getTicketStatus() {
        return ticketStatus;
    }

    public void setTicketStatus(int ticketstatus) {
        if (ticketId > 0) {
            this.ticketStatus = ticketId;
        } else {
            this.ticketStatus = ticketstatus;
        }
    }

    public void setPickupId(int iTicketId) {
        pickupId = iTicketId;
    }

    public int getPickupId() {
        return pickupId;
    }

    public String getName(String info) {
// JG Aug 2014 - Add User info
        List<String> name = new ArrayList<>();

        String nameprop = getProperty("name");
        if (nameprop != null) {
            name.add(nameprop);
        }

        if (userInfo != null) {
            name.add(userInfo.getName());
        }

        if (info == null) {
            if (ticketId == 0) {
                name.add("(" + Formats.HOURMIN.formatValue(creationDate) + ")");
            } else {
                name.add(Integer.toString(ticketId));
            }
        } else {
            name.add(info);
        }

        if (customer != null) {
            name.add(customer.getName());
        }

        return org.apache.commons.lang3.StringUtils.join(name, " - ");
    }

    public String getName() {
        return getName(null);
    }

    public java.util.Date getDate() {
        return creationDate;
    }

    public void setDate(java.util.Date dDate) {
        creationDate = dDate;
    }

    public String getHost() {
        return host;
    }

    public UserInfo getUser() {
        return userInfo;
    }

    public void setUser(UserInfo value) {
        userInfo = value;
    }

    public CustomerInfoExt getCustomer() {
        return customer;
    }

    public void setCustomer(CustomerInfoExt value) {
        customer = value;
    }

    public String getCustomerId() {
        if (customer == null) {
            return null;
        } else {
            return customer.getId();
        }
    }

    public String getTransactionID() {
        return (!getPayments().isEmpty())
                ? (getPayments().get(getPayments().size() - 1)).getTransactionID()
                : StringUtils.getCardNumber(); //random transaction ID
    }

    public String getReturnMessage() {
        return ((getPayments().get(getPayments().size() - 1)) instanceof PaymentInfoMagcard)
                ? ((PaymentInfoMagcard) (getPayments().get(getPayments().size() - 1))).getReturnMessage()
                : AppLocal.getIntString("button.ok");
    }

    public void setActiveCash(String value) {
        m_sActiveCash = value;
    }

    public String getActiveCash() {
        return m_sActiveCash;
    }

    public String getProperty(String key) {
        return attributes.getProperty(key);
    }

    public String getProperty(String key, String defaultvalue) {
        return attributes.getProperty(key, defaultvalue);
    }

    public void setProperty(String key, String value) {
        attributes.setProperty(key, value);
    }

    public Properties getProperties() {
        return attributes;
    }

    public TicketLineInfo getLine(int index) {
        return ticketLines.get(index);
    }

    public void addLine(TicketLineInfo oLine) {
        oLine.setTicket(id, ticketLines.size());
        ticketLines.add(oLine);
    }

    public void insertLine(int index, TicketLineInfo oLine) {
        ticketLines.add(index, oLine);
        refreshLines();
    }

    public void setLine(int index, TicketLineInfo oLine) {
        oLine.setTicket(id, index);
        ticketLines.set(index, oLine);
    }

    public void removeLine(int index) {
        ticketLines.remove(index);
        refreshLines();

    }

    public void refreshLines() {
        for (int i = 0; i < ticketLines.size(); i++) {
            getLine(i).setTicket(id, i);
        }
    }

    public int getLinesCount() {
        return ticketLines.size();
    }

    public double getArticlesCount() {
        double dArticles = 0.0;
        TicketLineInfo oLine;

        for (Iterator<TicketLineInfo> i = ticketLines.iterator(); i.hasNext();) {
            oLine = i.next();
            dArticles += oLine.getMultiply();
        }

        return dArticles;
    }

    public double getSubTotal() {
        double sum = 0.0;
        sum = ticketLines.stream().map((line)
                -> line.getSubValue()).reduce(sum, (accumulator, item)
                -> accumulator + item);
        return sum;
    }

    public double getTax() {

        double sum = 0.0;
        if (hasTaxesCalculated()) {
            for (TicketTaxInfo tax : taxes) {
                sum += tax.getTax(); // Taxes are already rounded...
            }
        } else {
            sum = ticketLines.stream().map((line)
                    -> line.getTax()).reduce(sum, (accumulator, _item)
                    -> accumulator + _item);
        }
        return sum;
    }

    public double getTotal() {
        return getSubTotal() + getTax();

    }

    public double getServiceCharge() {
        return (getTotal() + getTax());

    }

    /**
     * Total paid, Exclude payment with name "debtpaid"
     *
     * @return total payd
     */
    public double getTotalPaid() {
        double sum = 0.0;
        sum = payments.stream().filter((p)
                -> (!"debtpaid".equals(p.getName()))).map((p)
                -> p.getTotal()).reduce(sum, (accumulator, _item)
                -> accumulator + _item);
        return sum;
    }

    public double getTendered() {
        return getTotalPaid();
    }

    public List<TicketLineInfo> getLines() {
        return ticketLines;
    }

    public void setLines(List<TicketLineInfo> l) {
        ticketLines = l;
    }

    public List<PaymentInfo> getPayments() {
        return payments;
    }

    public void setPayments(List<PaymentInfo> l) {
        payments = l;
    }

    public void resetPayments() {
        payments = new ArrayList<>();
    }

    public List<TicketTaxInfo> getTaxes() {
        return taxes;
    }

    public boolean hasTaxesCalculated() {
        return taxes != null;
    }

    public void setTaxes(List<TicketTaxInfo> l) {
        taxes = l;
    }

    public void resetTaxes() {
        taxes = null;
    }

    public void setTip(boolean tips) {
        isTip = tips;
    }

    public boolean hasTip() {
        return isTip;
    }

    public void setIsProcessed(boolean isP) {
        isProcessed = isP;
    }

    public TicketTaxInfo getTaxLine(TaxInfo tax) {

        for (TicketTaxInfo taxline : taxes) {
            if (tax.getId().equals(taxline.getTaxInfo().getId())) {
                return taxline;
            }
        }

        return new TicketTaxInfo(tax);
    }

    public TicketTaxInfo[] getTaxLines() {

        Map<String, TicketTaxInfo> m = new HashMap<>();

        TicketLineInfo oLine;
        for (Iterator<TicketLineInfo> i = ticketLines.iterator(); i.hasNext();) {
            oLine = i.next();

            TicketTaxInfo t = m.get(oLine.getTaxInfo().getId());
            if (t == null) {
                t = new TicketTaxInfo(oLine.getTaxInfo());
                m.put(t.getTaxInfo().getId(), t);
            }
            t.add(oLine.getSubValue());
        }

        // return dSuma;       
        Collection<TicketTaxInfo> avalues = m.values();
        return avalues.toArray(new TicketTaxInfo[avalues.size()]);
    }

    public String printId() {
        String tmpTicketId = "";
        String receiptSize = (getConfig().getProperty("till.receiptsize"));
        String receiptPrefix = (getConfig().getProperty("till.receiptprefix"));

        if (ticketId > 0) {
            tmpTicketId = Integer.toString(ticketId);

            if (receiptSize != null) {
                int leadZero = Integer.parseInt(receiptSize);
                tmpTicketId = formatDocumentNumber(receiptPrefix, ticketId, null, leadZero);
            }
        }
        return tmpTicketId;

    }

    public String printDate() {
        return Formats.TIMESTAMP.formatValue(creationDate);
    }

    public String printUser() {
        return userInfo == null ? "" : userInfo.getName();

    }

    public String printHost() {
        return StringUtils.encodeXML(this.host);
    }

    /**
     *
     */
    public void clearCardNumber() {
        loyaltyCardNumber = null;
    }

    /**
     *
     * @param cardNumber
     */
    public void setLoyaltyCardNumber(String cardNumber) {
        loyaltyCardNumber = cardNumber;
    }

    /**
     *
     * @return
     */
    public String getLoyaltyCardNumber() {
        return (loyaltyCardNumber);
    }

    public String printCustomer() {
        return customer == null ? "" : customer.getName();
    }

    public String printArticlesCount() {
        return Formats.DOUBLE.formatValue(getArticlesCount());
    }

    public String printSubTotal() {
        return Formats.CURRENCY.formatValue(getSubTotal());
    }

    public String printTax() {
        return Formats.CURRENCY.formatValue(getTax());
    }

    public String printTotal() {
        return Formats.CURRENCY.formatValue(getTotal());
    }

    public String printTotalPaid() {
        return Formats.CURRENCY.formatValue(getTotalPaid());
    }

    public String printTendered() {
        return Formats.CURRENCY.formatValue(getTendered());
    }

    public String VoucherReturned() {
        return Formats.CURRENCY.formatValue(getTotalPaid() - getTotal());
    }

    public boolean getOldTicket() {
        return (isOldTicket);
    }

    public void setOldTicket(Boolean otState) {
        isOldTicket = otState;
    }

    public String getTicketHeaderFooterData(String data) {
        String row = (getConfig().getProperty("tkt." + data));
        return row;
    }

    public String printTicketHeaderLine1() {
        String lineData = getTicketHeaderFooterData("header1");

        if (lineData != null) {
            return lineData;
        } else {
            return "";
        }
    }

    public String printTicketHeaderLine2() {
        String lineData = getTicketHeaderFooterData("header2");

        if (lineData != null) {
            return lineData;
        } else {
            return "";
        }
    }

    public String printTicketHeaderLine3() {
        String lineData = getTicketHeaderFooterData("header3");

        if (lineData != null) {
            return lineData;
        } else {
            return "";
        }
    }

    public String printTicketHeaderLine4() {
        String lineData = getTicketHeaderFooterData("header4");

        if (lineData != null) {
            return lineData;
        } else {
            return "";
        }
    }

    public String printTicketHeaderLine5() {
        String lineData = getTicketHeaderFooterData("header5");

        if (lineData != null) {
            return lineData;
        } else {
            return "";
        }
    }

    public String printTicketHeaderLine6() {
        String lineData = getTicketHeaderFooterData("header6");

        if (lineData != null) {
            return lineData;
        } else {
            return "";
        }
    }

    public String printTicketFooterLine1() {
        String lineData = getTicketHeaderFooterData("footer1");

        if (lineData != null) {
            return lineData;
        } else {
            return "";
        }
    }

    public String printTicketFooterLine2() {
        String lineData = getTicketHeaderFooterData("footer2");

        if (lineData != null) {
            return lineData;
        } else {
            return "";
        }
    }

    public String printTicketFooterLine3() {
        String lineData = getTicketHeaderFooterData("footer3");

        if (lineData != null) {
            return lineData;
        } else {
            return "";
        }
    }

    public String printTicketFooterLine4() {
        String lineData = getTicketHeaderFooterData("footer4");

        if (lineData != null) {
            return lineData;
        } else {
            return "";
        }
    }

    public String printTicketFooterLine5() {
        String lineData = getTicketHeaderFooterData("footer5");

        if (lineData != null) {
            return lineData;
        } else {
            return "";
        }
    }

    public String printTicketFooterLine6() {
        String lineData = getTicketHeaderFooterData("footer6");

        if (lineData != null) {
            return lineData;
        } else {
            return "";
        }
    }

    private AppConfig getConfig() {
        return AppConfig.getInstance();
    }

    public static String formatDocumentNumber(String prefix, int number, String suffix, int leadingZeros) {
        String numberString = String.format("%0" + leadingZeros + "d", number);
        return (prefix != null ? prefix : "") + numberString + (suffix != null ? suffix : "");
    }

}
