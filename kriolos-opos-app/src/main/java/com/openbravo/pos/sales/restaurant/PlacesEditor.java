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

package com.openbravo.pos.sales.restaurant;

import com.openbravo.basic.AppLocal;
import com.openbravo.basic.BasicException;
import com.openbravo.data.gui.ComboBoxValModel;
import com.openbravo.data.loader.SentenceList;
import com.openbravo.data.user.DirtyManager;
import com.openbravo.data.user.EditorRecord;
import com.openbravo.format.Formats;
import com.openbravo.pos.sales.DataLogicSales;
import com.openbravo.ui.data.binding.Converter;
import com.openbravo.ui.data.binding.SwingBinder;
import java.awt.Component;
import java.util.UUID;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Strongly typed editor component for restaurant Place (table) entities,
 * supporting two-way Swing data binding and real-time validation.
 *
 * @author adrianromero
 * @author KriolOS
 */
public final class PlacesEditor extends JPanel implements EditorRecord<Place> {

    private static final System.Logger LOGGER = System.getLogger(PlacesEditor.class.getName());
    private static final long serialVersionUID = 1L;

    private RestaurantService restaurantService;
    private SentenceList m_sentfloor;
    private ComboBoxValModel m_FloorModel;
    private DirtyManager dirty;
    private SwingBinder<Place> binder;

    private String m_sID;
    private String m_sFloor;

    /**
     * Headless constructor for testing without active GUI/database session.
     *
     * @param dirty dirty state manager
     */
    public PlacesEditor(DirtyManager dirty) {
        this((RestaurantService) null, dirty);
    }

    /**
     * Creates new form PlacesEditor.
     *
     * @param restaurantService restaurant domain service
     * @param dirty dirty state manager
     */
    public PlacesEditor(RestaurantService restaurantService, DirtyManager dirty) {
        initComponents();

        this.dirty = dirty;
        this.restaurantService = restaurantService;
        this.m_sentfloor = restaurantService != null ? restaurantService.getFloorsList() : null;
        this.m_FloorModel = new ComboBoxValModel();

        m_jName.getDocument().addDocumentListener(dirty);
        m_jSeats.getDocument().addDocumentListener(dirty);
        m_jFloor.addActionListener(dirty);
        m_jX.getDocument().addDocumentListener(dirty);
        m_jY.getDocument().addDocumentListener(dirty);

        initValidator();
        writeValueEOF();
    }

    private void initValidator() {
        binder = new SwingBinder<>(new Place());

        // Mandatory Table/Place Name
        binder.forField(m_jName)
                .asRequired(AppLocal.getIntString("validation.required", AppLocal.getIntString("label.name")))
                .bind(Place::getName, Place::setName);

        // Optional Seating Capacity
        binder.forField(m_jSeats)
                .bind(Place::getSeats, Place::setSeats);

        // Table X Coordinate
        binder.forField(m_jX)
                .withConverter(new IntegerConverter())
                .withValidator(v -> v == null || v >= 0, AppLocal.getIntString("validation.number.positive", "X"))
                .bind(Place::getX, Place::setX);

        // Table Y Coordinate
        binder.forField(m_jY)
                .withConverter(new IntegerConverter())
                .withValidator(v -> v == null || v >= 0, AppLocal.getIntString("validation.number.positive", "Y"))
                .bind(Place::getY, Place::setY);

        binder.addStatusChangeListener(() -> {
            if (dirty != null) {
                dirty.setDirty(true);
            }
        });
    }

    public boolean isValid() {
        return binder == null || binder.validate();
    }

    public SwingBinder<Place> getBinder() {
        return binder;
    }

    JTextField getNameField() {
        return m_jName;
    }

    JTextField getSeatsField() {
        return m_jSeats;
    }

    JTextField getXField() {
        return m_jX;
    }

    JTextField getYField() {
        return m_jY;
    }

    /**
     * @deprecated Use {@link #PlacesEditor(RestaurantService, DirtyManager)} instead.
     */
    @Deprecated
    public PlacesEditor(DataLogicRestaurant dlRestaurant, DirtyManager dirty) {
        this((RestaurantService) dlRestaurant, dirty);
    }

    /**
     * @deprecated Use {@link #PlacesEditor(RestaurantService, DirtyManager)} instead.
     */
    @Deprecated
    public PlacesEditor(DataLogicSales dlSales, DirtyManager dirty) {
        this(dlSales != null ? dlSales.getRestaurantService() : null, dirty);
    }

    /**
     *
     * @throws BasicException
     */
    public void activate() throws BasicException {
        if (restaurantService != null) {
            m_FloorModel = new ComboBoxValModel(restaurantService.getFloorsListAll());
        } else if (m_sentfloor != null) {
            m_FloorModel = new ComboBoxValModel(m_sentfloor.list());
        } else {
            m_FloorModel = new ComboBoxValModel();
        }
        m_jFloor.setModel(m_FloorModel);
    }

    /**
     *
     */
    @Override
    public void refresh() {
    }

    /**
     *
     */
    @Override
    public void writeValueEOF() {
        m_sID = null;
        m_sFloor = null;
        if (binder != null) {
            binder.clear();
        } else {
            m_jName.setText(null);
            m_jSeats.setText(null);
            m_jX.setText(null);
            m_jY.setText(null);
        }
        m_FloorModel.setSelectedKey(null);

        m_jName.setEnabled(false);
        m_jSeats.setEnabled(false);
        m_jFloor.setEnabled(false);
        m_jX.setEnabled(false);
        m_jY.setEnabled(false);
    }

    /**
     *
     */
    @Override
    public void writeValueInsert() {
        m_sID = UUID.randomUUID().toString();
        m_sFloor = null;
        if (binder != null) {
            binder.clear();
        } else {
            m_jName.setText(null);
            m_jSeats.setText(null);
            m_jX.setText(null);
            m_jY.setText(null);
        }
        m_FloorModel.setSelectedKey(null);

        m_jName.setEnabled(true);
        m_jSeats.setEnabled(true);
        m_jFloor.setEnabled(true);
        m_jX.setEnabled(true);
        m_jY.setEnabled(true);
    }

    /**
     *
     * @param place
     */
    @Override
    public void writeValueDelete(Place place) {
        if (place == null) {
            writeValueEOF();
            return;
        }
        m_sID = place.getId();
        m_sFloor = place.getFloor();
        if (binder != null) {
            binder.readBean(place);
        } else {
            m_jName.setText(place.getName());
            m_jSeats.setText(place.getSeats());
            m_jX.setText(Formats.INT.formatValue(place.getX()));
            m_jY.setText(Formats.INT.formatValue(place.getY()));
        }
        m_FloorModel.setSelectedKey(place.getFloor());

        m_jName.setEnabled(false);
        m_jSeats.setEnabled(false);
        m_jFloor.setEnabled(false);
        m_jX.setEnabled(false);
        m_jY.setEnabled(false);
    }

    @Override
    public void writeValueEdit(Place place) {
        if (place == null) {
            writeValueEOF();
            return;
        }
        m_sID = place.getId();
        m_sFloor = place.getFloor();
        if (binder != null) {
            binder.readBean(place);
        } else {
            m_jName.setText(place.getName());
            m_jSeats.setText(place.getSeats());
            m_jX.setText(Formats.INT.formatValue(place.getX()));
            m_jY.setText(Formats.INT.formatValue(place.getY()));
        }
        m_FloorModel.setSelectedKey(place.getFloor());

        m_jName.setEnabled(true);
        m_jSeats.setEnabled(true);
        m_jFloor.setEnabled(true);
        m_jX.setEnabled(true);
        m_jY.setEnabled(true);
    }

    @Override
    public Place createValue() throws BasicException {
        Integer x = null;
        Integer y = null;
        try {
            x = (Integer) Formats.INT.parseValue(m_jX.getText());
        } catch (BasicException ignored) {
        }
        try {
            y = (Integer) Formats.INT.parseValue(m_jY.getText());
        } catch (BasicException ignored) {
        }

        String floorKey = (String) m_FloorModel.getSelectedKey();
        if (floorKey == null) {
            floorKey = m_sFloor;
        }

        Place place = new Place(
                m_sID,
                m_jName.getText(),
                m_jSeats.getText(),
                x != null ? x : 0,
                y != null ? y : 0,
                floorKey
        );

        if (binder != null) {
            try {
                binder.writeBean(place);
            } catch (SwingBinder.ValidationException ex) {
                LOGGER.log(System.Logger.Level.WARNING, "Validation in createValue: " + ex.getMessage());
            }
        }

        if (place.getName() == null) {
            place.setName(m_jName.getText());
        }
        if (place.getSeats() == null) {
            place.setSeats(m_jSeats.getText());
        }
        if (place.getFloor() == null) {
            place.setFloor(floorKey);
        }

        return place;
    }

    private static class IntegerConverter implements Converter<String, Integer> {

        @Override
        public Integer toModel(String p) {
            if (p == null || p.trim().isEmpty()) {
                return 0;
            }
            try {
                return (Integer) Formats.INT.parseValue(p.trim(), 0);
            } catch (BasicException e) {
                try {
                    return Integer.parseInt(p.trim());
                } catch (NumberFormatException nfe) {
                    return -1;
                }
            }
        }

        @Override
        public String toPresentation(Integer m) {
            return m != null ? Formats.INT.formatValue(m) : "";
        }
    }
    
    /**
     *
     * @return
     */
    @Override
    public Component getComponent() {
        return this;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        m_jName = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        m_jFloor = new javax.swing.JComboBox();
        jLabel6 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        m_jX = new javax.swing.JTextField();
        m_jY = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        m_jSeats = new javax.swing.JTextField();

        setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N

        jLabel2.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel2.setText(AppLocal.getIntString("label.name")); // NOI18N
        jLabel2.setPreferredSize(new java.awt.Dimension(110, 30));

        m_jName.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jName.setPreferredSize(new java.awt.Dimension(0, 30));

        jLabel1.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel1.setText(AppLocal.getIntString("label.placefloor")); // NOI18N
        jLabel1.setPreferredSize(new java.awt.Dimension(110, 30));

        m_jFloor.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jFloor.setPreferredSize(new java.awt.Dimension(0, 30));

        jLabel6.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel6.setText(AppLocal.getIntString("label.placeposition")); // NOI18N
        jLabel6.setPreferredSize(new java.awt.Dimension(0, 30));

        jLabel5.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel5.setText("Across");
        jLabel5.setPreferredSize(new java.awt.Dimension(0, 30));

        jLabel3.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel3.setText("Down");
        jLabel3.setPreferredSize(new java.awt.Dimension(0, 30));

        m_jX.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jX.setPreferredSize(new java.awt.Dimension(56, 30));

        m_jY.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jY.setPreferredSize(new java.awt.Dimension(56, 30));

        jLabel8.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel8.setText(AppLocal.getIntString("label.seats")); // NOI18N
        jLabel8.setPreferredSize(new java.awt.Dimension(110, 30));

        m_jSeats.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jSeats.setMinimumSize(new java.awt.Dimension(56, 30));
        m_jSeats.setPreferredSize(new java.awt.Dimension(56, 30));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(m_jX, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(33, 33, 33)
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(m_jY, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(m_jName, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(m_jFloor, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(18, 18, 18)
                        .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(m_jSeats, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_jFloor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_jName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_jSeats, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(m_jX, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_jY, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JComboBox m_jFloor;
    private javax.swing.JTextField m_jName;
    private javax.swing.JTextField m_jSeats;
    private javax.swing.JTextField m_jX;
    private javax.swing.JTextField m_jY;
    // End of variables declaration//GEN-END:variables
    
}