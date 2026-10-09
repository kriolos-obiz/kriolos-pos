package com.openbravo.pos.pim;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.DataRead;
import com.openbravo.data.loader.IKeyed;
import com.openbravo.data.loader.SerializerRead;
import java.io.Serializable;
import java.util.Objects;

/**
 * Unit of Measure (UOM) domain model characterizing product quantities and packaging.
 *
 * @author KriolOS
 */
public class UomInfo implements IKeyed, Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String name;

    /**
     * Constructs a new Unit of Measure instance.
     *
     * @param id   the unique identifier of the UOM
     * @param name the display name of the UOM
     */
    public UomInfo(String id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * Returns the identifier of this Unit of Measure.
     *
     * @return the UOM identifier
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the identifier of this Unit of Measure.
     *
     * @param id the UOM identifier
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Legacy getter alias for the identifier.
     *
     * @return the UOM identifier
     * @deprecated Use {@link #getId()} instead.
     */
    @Deprecated
    public String getID() {
        return id;
    }

    /**
     * Legacy setter alias for the identifier.
     *
     * @param id the UOM identifier
     * @deprecated Use {@link #setId(String)} instead.
     */
    @Deprecated
    public void setID(String id) {
        this.id = id;
    }

    /**
     * Returns the display name of this Unit of Measure.
     *
     * @return the UOM name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the display name of this Unit of Measure.
     *
     * @param name the UOM name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the lookup key for table selection models.
     *
     * @return the UOM identifier key
     */
    @Override
    public Object getKey() {
        return id;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UomInfo uomInfo = (UomInfo) o;
        return Objects.equals(id, uomInfo.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Returns a {@link SerializerRead} for deserializing UomInfo records from JDBC queries.
     *
     * @return database row serializer for {@link UomInfo}
     */
    public static SerializerRead<UomInfo> getSerializerRead() {
        return (DataRead dr) -> new UomInfo(dr.getString(1), dr.getString(2));
    }
}
