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
package com.openbravo.pos.forms;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.*;
import com.openbravo.format.Formats;
import com.openbravo.pos.admin.ResourceInfo;
import com.openbravo.pos.util.ThumbNailBuilder;
import com.openbravo.pos.voucher.VoucherInfo;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/**
 *
 * @author JG uniCenta
 */
public class DataLogicSystem extends BeanFactoryDataSingle implements SecurityService {

    private final static Logger LOGGER = Logger.getLogger(DataLogicSystem.class.getName());

    private Session session;
    private String m_dbVersion;
    private TableDefinition<ResourceInfo> m_tresources;

    public DataLogicSystem() {}

    @Override
    public void init(Session session) {
        this.session = session;
        this.m_dbVersion = this.session.DB.getName();

        m_tresources = new TableDefinition(session,
                "resources",
                new String[]{
                    "ID", "NAME", "RESTYPE", "CONTENT"},
                new String[]{
                    "ID",
                    AppLocal.getIntString("label.name"),
                    AppLocal.getIntString("label.type"),
                    "CONTENT"},
                new Datas[]{
                    Datas.STRING, Datas.STRING, Datas.INT, Datas.BYTES},
                new Formats[]{
                    Formats.STRING, Formats.STRING, Formats.INT, Formats.NULL},
                new int[]{0}
        );

    }


    public final TableDefinition<ResourceInfo> getTableResources() {
        return m_tresources;
    }

    public String getDBVersion() {
        return m_dbVersion;
    }

    public final String findVersion() throws BasicException {
        final SentenceFind<String> m_version = new PreparedSentence<String, String>(this.session,
                "SELECT VERSION FROM applications WHERE ID = ?",
                SerializerWriteString.INSTANCE, SerializerReadString.INSTANCE);

        return m_version.find(AppLocal.APP_ID);
    }

    public final String getUser() throws BasicException {
        return ("");
    }


//// <editor-fold defaultstate="collapsed" desc="START OF PEOPLE">
  
    /**
     *
     * @return @throws BasicException
     */
    public final List<AppUser> listPeopleVisible() throws BasicException {
        final SentenceList m_peoplevisible = new StaticSentence(this.session,
                "SELECT ID, NAME, APPPASSWORD, CARD, ROLE "
                + "FROM people "
                + "WHERE VISIBLE = " + this.session.DB.TRUE() + " ORDER BY NAME",
                new AppuserReader());

        return m_peoplevisible.list();
    }

    /**
     *
     * @param role
     * @return
     * @throws BasicException
     */
    public final List<String> getPermissions(String role) throws BasicException {
        final SentenceList<String> m_permissionlist = new StaticSentence(this.session,
                "SELECT PERMISSIONS FROM permissions WHERE ID = ?",
                SerializerWriteString.INSTANCE,
                new SerializerReadBasic(new Datas[]{Datas.STRING}));

        return m_permissionlist.list(role);
    }

    /**
     *
     * @param card
     * @return
     * @throws BasicException
     */
    public final AppUser findPeopleByCard(String card) throws BasicException {

        final SentenceFind<AppUser> m_peoplebycard = new PreparedSentence<String, AppUser>(this.session,
                "SELECT ID, NAME, APPPASSWORD, CARD, ROLE, IMAGE "
                + "FROM people "
                + "WHERE CARD = ? AND VISIBLE = " + this.session.DB.TRUE(),
                SerializerWriteString.INSTANCE,
                new AppuserReader());
        return m_peoplebycard.find(card);
    }

    /**
     *
     * @param sRole
     * @return
     */
    public final String findRolePermissions(String sRole) {

        final SentenceFind m_rolepermissions = new PreparedSentence(this.session,
                "SELECT PERMISSIONS FROM roles WHERE ID = ?",
                SerializerWriteString.INSTANCE,
                SerializerReadBytes.INSTANCE);

        String content = new String();
        try {
            content = Formats.BYTEA.formatValue((byte[]) m_rolepermissions.find(sRole));
        } catch (BasicException e) {
            LOGGER.log(Level.SEVERE, "Exception on format permissions for role: " + sRole, e);
        }
        return content;
    }

    /**
     *
     * @param userdata
     * @throws BasicException
     */
    public final void execChangePassword(Object[] userdata) throws BasicException {

        final SentenceExec m_changepassword = new StaticSentence(this.session,
                "UPDATE people SET APPPASSWORD = ? WHERE ID = ?",
                new SerializerWriteBasic(new Datas[]{Datas.STRING, Datas.STRING}));

        m_changepassword.exec(userdata);
    }

    /**
     *
     * @param permissions
     * @throws BasicException
     */
    public final void execUpdatePermissions(Object[] permissions) throws BasicException {
        final SentenceExec m_updatepermissions = new StaticSentence(this.session,
                "INSERT INTO permissions (ID, PERMISSIONS) "
                + "VALUES (?, ?)",
                new SerializerWriteBasic(new Datas[]{
            Datas.STRING,
            Datas.STRING}));
        m_updatepermissions.exec(permissions);
    }

    //// </editor-fold> 
    

//// <editor-fold defaultstate="collapsed" desc="START OF RESOURCE">
    private byte[] getResource(String name) {

        SentenceFind m_resourcebytes = new PreparedSentence(this.session,
                "SELECT CONTENT FROM resources WHERE NAME = ?",
                SerializerWriteString.INSTANCE,
                SerializerReadBytes.INSTANCE);

        byte[] resource;

        try {
            resource = (byte[]) m_resourcebytes.find(name);
            if (resource != null) {
            } else {
                LOGGER.log(Level.WARNING, "Resource NOT found name: {0}", name);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception while get resource name: " + name, e);
            resource = null;
        }

        return resource;
    }

    /**
     *
     * @param name
     * @param type
     * @param data
     */
    public final void setResource(String name, int type, byte[] data) {

        Datas[] resourcedata = new Datas[]{Datas.STRING, Datas.STRING, Datas.INT, Datas.BYTES};
        Object[] value = new Object[]{UUID.randomUUID().toString(), name, type, data};

        SentenceExec m_resourcebytesinsert = new PreparedSentenceExec(this.session,
                "INSERT INTO resources(ID, NAME, RESTYPE, CONTENT) VALUES (?, ?, ?, ?)",
                resourcedata, new int[]{0, 1, 2, 3});

        SentenceExec m_resourcebytesupdate = new PreparedSentenceExec(this.session,
                "UPDATE resources SET NAME = ?, RESTYPE = ?, CONTENT = ? WHERE NAME = ?",
                resourcedata, new int[]{1, 2, 3, 1});

        try {
            if (m_resourcebytesupdate.exec(value) != 0) {
                LOGGER.log(Level.INFO, "Resource update: " + name);
            } else {
                m_resourcebytesinsert.exec(value);
                LOGGER.log(Level.INFO, "Resource insert: " + name);
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Exception while save resource name: " + name, ex);
        }
    }

    /**
     *
     * @param sName
     * @param data
     */
    public final void setResourceAsBinary(String sName, byte[] data) {
        setResource(sName, 2, data);
    }

    /**
     *
     * @param sName
     * @return
     */
    public final byte[] getResourceAsBinary(String sName) {
        return getResource(sName);
    }

    /**
     *
     * @param sName
     * @return
     */
    public final String getResourceAsText(String sName) {
        return Formats.BYTEA.formatValue(getResource(sName));
    }

    /**
     *
     * @param sName
     * @return
     */
    public final String getResourceAsXML(String sName) {
        return Formats.BYTEA.formatValue(getResource(sName));
    }

    /**
     *
     * @param sName
     * @return
     */
    public final BufferedImage getResourceAsImage(String sName) {

        LOGGER.log(Level.INFO, "Get image resource id: " + sName);
        BufferedImage img = null;
        try {
            InputStream strem = new ByteArrayInputStream(getResource(sName));
            img = ImageIO.read(strem);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Exception on get resource: " + sName, e);
        }
        return img;
    }

    /**
     *
     * @param sName
     * @param p
     */
    public final void setResourceAsProperties(String sName, Properties p) {
        if (p == null) {
            setResource(sName, 0, null); // texto
        } else {
            try {
                ByteArrayOutputStream o = new ByteArrayOutputStream();
                p.storeToXML(o, AppLocal.APP_NAME, "UTF8");
                setResource(sName, 0, o.toByteArray()); // El texto de las propiedades   
            } catch (IOException e) { // no deberia pasar nunca
                LOGGER.log(Level.SEVERE, "Exception on set resource: " + sName, e);
            }
        }
    }

    /**
     *
     * @param sName
     * @return
     */
    public final Properties getResourceAsProperties(String sName) {

        Properties p = new Properties();
        try {
            byte[] xml = getResource(sName);
            if (xml != null) {
                p.loadFromXML(new ByteArrayInputStream(xml));
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Exception on get resource as Properties, name: " + sName, e);
        }
        return p;
    }

    //// </editor-fold> 


/// <editor-fold defaultstate="collapsed" desc="START OF CASH REGISTER">
    

    /**
     *
     * @param drawer
     * @throws BasicException
     */
    public final void execDrawerOpened(String name, String action, Date openDate) throws BasicException {
        final SentenceExec m_draweropened = new PreparedSentence<>(this.session,
                "INSERT INTO draweropened ( NAME, TICKETID, OPENDATE) "
                + "VALUES (?, ?, ?)",
                new SerializerWriteBasic(new Datas[]{Datas.STRING, Datas.STRING, Datas.TIMESTAMP}));
        m_draweropened.exec(new Object[]{name, action, openDate});
    }

//// </editor-fold> 



// <editor-fold defaultstate="collapsed" desc="START OF LOCATION AND PLACES">

    /**
     * @deprecated Use {@link com.openbravo.pos.inventory.DataLogicInventory#findLocationName(String)} instead.
     */
    @Deprecated
    public final String findLocationName(String iLocation) throws BasicException {
        if (app != null) {
            try {
                com.openbravo.pos.inventory.DataLogicInventory dlInv =
                        app.getBean(com.openbravo.pos.inventory.DataLogicInventory.class);
                if (dlInv != null) {
                    return dlInv.findLocationName(iLocation);
                }
            } catch (BeanFactoryException ignored) {
            }
        }
        final SentenceFind m_locationfind = new StaticSentence(this.session,
                "SELECT NAME FROM locations WHERE ID = ?",
                SerializerWriteString.INSTANCE,
                SerializerReadString.INSTANCE);
        return (String) m_locationfind.find(iLocation);
    }

    /**
     * @deprecated Use {@link com.openbravo.pos.sales.restaurant.PlaceService#updatePlaces(int, int, String)}
     *             or {@link com.openbravo.pos.sales.restaurant.DataLogicRestaurant#updatePlaces(int, int, String)} instead.
     */
    @Deprecated
    public final void updatePlaces(int x, int y, String id) throws BasicException {
        if (app != null) {
            try {
                com.openbravo.pos.sales.restaurant.DataLogicRestaurant dlRest =
                        app.getBean(com.openbravo.pos.sales.restaurant.DataLogicRestaurant.class);
                if (dlRest != null) {
                    dlRest.updatePlaces(x, y, id);
                    return;
                }
            } catch (BeanFactoryException ignored) {
            }
        }
        final SentenceExec m_updatePlaces = new StaticSentence(this.session,
                "UPDATE PLACES SET X = ?, Y = ? WHERE ID = ?",
                new SerializerWriteBasic(new Datas[]{Datas.INT, Datas.INT, Datas.STRING}));
        m_updatePlaces.exec(new Object[]{x, y, id});
    }

//// </editor-fold>
    
    /**
     * @deprecated Use {@link com.openbravo.pos.voucher.DataLogicVouchers#getVoucherList()} instead.
     */
    @Deprecated
    public final List<VoucherInfo> getVouchersActiveList() throws BasicException {
        if (app != null) {
            try {
                return app.getBean(com.openbravo.pos.voucher.DataLogicVouchers.class).getVoucherList();
            } catch (BeanFactoryException ignored) {
            }
        }
        com.openbravo.pos.voucher.DataLogicVouchers fallback = new com.openbravo.pos.voucher.DataLogicVouchers();
        fallback.init(this.session);
        return fallback.getVoucherList();
    }

//// <editor-fold defaultstate="collapsed" desc="START OF ORDER">   
    
    /**
     * 
     * @param orderId
     * @param qty
     * @param details
     * @param attributes
     * @param notes
     * @param ticketId
     * @param ordertime
     * @param displayId
     * @param auxiliary
     * @param completetime
     * @throws BasicException 
     */
    private DataLogicOrders getDataLogicOrders() {
        if (app != null) {
            try {
                return app.getBean(DataLogicOrders.class);
            } catch (BeanFactoryException ignored) {
            }
        }
        DataLogicOrders fallback = new DataLogicOrders();
        fallback.init(this.session);
        return fallback;
    }

    /**
     * @deprecated Use {@link com.openbravo.pos.sales.RemoteOrderService#addOrder} instead.
     */
    @Deprecated
    public final void addOrder(com.openbravo.pos.sales.RemoteOrder order) throws BasicException {
        getDataLogicOrders().addOrder(order);
    }

    /**
     * @deprecated Use {@link DataLogicOrders#addOrder} instead.
     */
    @Deprecated
    public final void addOrder(String orderId, Double qty,
            String details, String attributes, String notes, String ticketId,
            String ordertime, String displayId, String auxiliary, String completetime
    ) throws BasicException {
        getDataLogicOrders().addOrder(orderId, qty, details, attributes, notes, ticketId,
                ordertime, displayId, auxiliary, completetime);
    }

    /**
     * @deprecated Use {@link DataLogicOrders#addOrder} instead.
     */
    @Deprecated
    public final void addOrder(String orderId, Integer qty,
            String details, String attributes, String notes, String ticketId,
            String ordertime, Integer displayId, String auxiliary, String completetime
    ) throws BasicException {
        getDataLogicOrders().addOrder(orderId, qty, details, attributes, notes, ticketId,
                ordertime, displayId, auxiliary, completetime);
    }

    /**
     * @deprecated Use {@link com.openbravo.pos.sales.RemoteOrderService#updateOrder} instead.
     */
    @Deprecated
    public final void updateOrder(com.openbravo.pos.sales.RemoteOrder order) throws BasicException {
        getDataLogicOrders().updateOrder(order);
    }

    /**
     * @deprecated Use {@link DataLogicOrders#updateOrder} instead.
     */
    @Deprecated
    public final void updateOrder(String orderId, Double qty,
            String details, String attributes, String notes, String ticketId,
            String ordertime, String displayId, String auxiliary, String completetime
    ) throws BasicException {
        getDataLogicOrders().updateOrder(orderId, qty, details, attributes, notes, ticketId,
                ordertime, displayId, auxiliary, completetime);
    }

    /**
     * @deprecated Use {@link DataLogicOrders#updateOrder} instead.
     */
    @Deprecated
    public final void updateOrder(String orderId, Integer qty,
            String details, String attributes, String notes, String ticketId,
            String ordertime, Integer displayId, String auxiliary, String completetime
    ) throws BasicException {
        getDataLogicOrders().updateOrder(orderId, qty, details, attributes, notes, ticketId,
                ordertime, displayId, auxiliary, completetime);
    }

    /**
     * @deprecated Use {@link DataLogicOrders#deleteOrder} instead.
     */
    @Deprecated
    public void deleteOrder(String orderId) throws BasicException {
        getDataLogicOrders().deleteOrder(orderId);
    }

    //// </editor-fold> 
    
    private final static class AppuserReader implements SerializerRead<AppUser> {

        final ThumbNailBuilder defaultUserTN = new ThumbNailBuilder(32, 32, "com/openbravo/images/user.png");

        @Override
        public AppUser readValues(DataRead dr) throws BasicException {

            return new AppUser(
                    dr.getString(1),
                    dr.getString(2),
                    dr.getString(3),
                    dr.getString(4),
                    dr.getString(5),
                    //new ImageIcon(tnb.getThumbNail(ImageUtils.readImage(dr.getBytes(6)))));
                    new ImageIcon(defaultUserTN.getThumbNail()));
        }
    }

    private final static class ProductIdRead implements SerializerRead<String> {

        @Override
        public String readValues(DataRead dr) throws BasicException {
            return dr.getString(1);
        }
    };
}
