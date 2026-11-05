package com.openbravo.pos.suppliers;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.SentenceList;
import com.openbravo.data.loader.TableDefinition;
import java.util.List;

/**
 * Service port interface for supplier master data and transaction management.
 */
public interface SupplierService {

    SentenceList<SupplierInfo> getSupplierList();

    List<SupplierInfo> getSupplierListAll();

    SentenceList<SupplierInfo> getSuppList();

    SentenceList<SupplierInfo> getSuppListExt();

    SupplierInfoExt loadSupplierExt(String id) throws BasicException;

    /**
     * Creates a supplier from typed field values.
     *
     * @param supplier supplier creation values
     * @throws BasicException on persistence error
     */
    default void createSupplier(SupplierCreateCommand supplier) throws BasicException {
        createSupplier(new Object[]{supplier.id(), supplier.name(), supplier.searchKey(), supplier.visible()});
    }

    /**
     * Creates a supplier from the legacy positional representation.
     *
     * @param supplier values ordered as ID, name, search key, and visibility
     * @throws BasicException on persistence error
     * @deprecated Use {@link #createSupplier(SupplierCreateCommand)}.
     */
    @Deprecated
    void createSupplier(Object[] supplier) throws BasicException;

    int updateSupplierExt(SupplierInfoExt supplier) throws BasicException;

    TableDefinition getTableSuppliers();

    List<SupplierTransaction> getSuppliersTransactionList(String sId) throws BasicException;
}
