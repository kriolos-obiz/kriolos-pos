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

    /**
     * Updates supplier notes from a typed command.
     *
     * @param command update notes command
     * @return number of affected rows
     * @throws BasicException on persistence error
     */
    int updateSupplierNotes(UpdateSupplierNotesCommand command) throws BasicException;

    /**
     * Updates supplier notes using the legacy entity representation.
     *
     * @param supplier supplier entity containing ID and notes
     * @return number of affected rows
     * @throws BasicException on persistence error
     * @deprecated Use {@link #updateSupplierNotes(UpdateSupplierNotesCommand)}.
     */
    @Deprecated
    default int updateSupplierExt(SupplierInfoExt supplier) throws BasicException {
        java.util.Objects.requireNonNull(supplier, "supplier must not be null");
        return updateSupplierNotes(new UpdateSupplierNotesCommand(supplier.getId(), supplier.getNotes()));
    }

    TableDefinition<Object[]> getTableSuppliers();

    List<SupplierTransaction> getSuppliersTransactionList(String sId) throws BasicException;
}
