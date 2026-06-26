package com.openbravo.pos.sales.restaurant;

import com.openbravo.basic.BasicException;
import java.util.List;

public interface PlaceService {
    List<Floor> getFloors() throws BasicException;

    List<Place> getPlaces() throws BasicException;
}
