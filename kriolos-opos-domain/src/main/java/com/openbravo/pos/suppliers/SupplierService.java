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

    void createSupplier(Object[] supplier) throws BasicException;

    int updateSupplierExt(SupplierInfoExt supplier) throws BasicException;

    TableDefinition getTableSuppliers();

    List<SupplierTransaction> getSuppliersTransactionList(String sId) throws BasicException;
}
