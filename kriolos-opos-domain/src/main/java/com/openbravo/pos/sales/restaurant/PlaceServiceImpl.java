package com.openbravo.pos.sales.restaurant;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.SerializerReadClass;
import com.openbravo.data.loader.Session;
import com.openbravo.data.loader.StaticSentence;
import java.util.List;

public class PlaceServiceImpl implements PlaceService {

    private final Session session;

    public PlaceServiceImpl(Session session) {
        this.session = session;
    }

    @Override
    public List<Floor> getFloors() throws BasicException {
        return new StaticSentence(
                session,
                "SELECT ID, NAME, IMAGE FROM floors ORDER BY NAME",
                null,
                new SerializerReadClass(Floor.class))
                .list();
    }

    @Override
    public List<Place> getPlaces() throws BasicException {
        return new StaticSentence(
                session,
                "SELECT ID, NAME, SEATS, X, Y, FLOOR, CUSTOMER, WAITER, TICKETID, TABLEMOVED FROM places ORDER BY FLOOR",
                null,
                new SerializerReadClass(Place.class))
                .list();
    }
}
