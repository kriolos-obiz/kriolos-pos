package com.openbravo.pos.pim;

import com.openbravo.pos.catalog.CatalogServiceImpl;

/**
 * Legacy Product Information Management monolith accessor.
 * All operations have been decomposed into specific DataLogic components:
 * {@link DataLogicProducts}, {@link DataLogicCategories}, and {@link DataLogicUom}.
 *
 * @author KriolOS
 * @deprecated Use {@link com.openbravo.pos.catalog.CatalogService} or {@link DataLogicProducts} instead.
 */
@Deprecated(since = "10.0.0", forRemoval = true)
public class DataLogicPIM extends CatalogServiceImpl {

    /**
     * Default constructor for BeanFactory initialization.
     */
    public DataLogicPIM() {
        super();
    }
}
