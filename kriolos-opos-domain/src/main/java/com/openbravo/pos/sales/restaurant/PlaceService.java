package com.openbravo.pos.sales.restaurant;

import com.openbravo.basic.BasicException;
import java.util.List;

/**
 * Service port interface for Restaurant Floor and Place operations.
 */
public interface PlaceService {

    List<Floor> getFloors() throws BasicException;

    List<Place> getPlaces() throws BasicException;

    void moveCustomer(String newTable, String ticketID);

    void setCustomerNameInTable(String custName, String tableName);

    void setCustomerNameInTableById(String custName, String tableID);

    void setCustomerNameInTableByTicketId(String custName, String ticketID);

    String getCustomerNameInTable(String tableName);

    String getCustomerNameInTableById(String tableId);

    void clearCustomerNameInTable(String tableName);

    void clearCustomerNameInTableById(String tableID);

    void setWaiterNameInTable(String waiterName, String tableName);

    void setWaiterNameInTableById(String waiterName, String tableID);

    String getWaiterNameInTable(String tableName);

    String getWaiterNameInTableById(String tableID);

    void clearWaiterNameInTable(String tableName);

    void clearWaiterNameInTableById(String tableID);

    String getTicketIdInTable(String tableID);

    void setTicketIdInTable(String ticketId, String tableName);

    void clearTicketIdInTable(String tableName);

    void clearTicketIdInTableById(String tableID);

    Integer countTicketIdInTable(String ticketID);

    String getTableDetails(String ticketID);

    void setTableMovedFlag(String tableID);

    String getTableMovedName(String ticketID);

    Boolean getTableMovedFlag(String ticketID);

    void clearTableMovedFlag(String tableID);
}

