package com.openbravo.pos.inventory;

/**
 * Legacy Unit of Measure wrapper preserving backward compatibility.
 *
 * @deprecated Use {@link com.openbravo.pos.pim.UomInfo} instead.
 */
@Deprecated(since = "10.0.0")
public class UomInfo extends com.openbravo.pos.pim.UomInfo {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a legacy UomInfo proxying the PIM UomInfo model.
     *
     * @param id   the UOM identifier
     * @param name the UOM name
     */
    public UomInfo(String id, String name) {
        super(id, name);
    }
}
