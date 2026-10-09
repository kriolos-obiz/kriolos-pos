package com.openbravo.pos.ticket;

import com.openbravo.data.loader.DataRead;
import com.openbravo.data.loader.SerializerRead;

/**
 *
 * @author poolborges
 */
public class ProductInfoExtA {
    
    private String id;
    private String name;
    private String stockUnits;
    private String stockLocations;
    private double pricesell;
    private double taxerate;
    private double pricesellWithTax;
    
    /**
     * Category ID
     */
    private String categoryId;
    
    /**
     * Is a Companion Product
     */
    private boolean isComProduct;
    
    /**
     * Is a Scale Product (Product weight)
     */
    private boolean isScaleProduct;    
    
    /**
     * Is a constant Product
     */
    private boolean isConstantProduct;
    
    /**
     * Is a Service
     */
    private boolean isService;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPricesell() {
        return pricesell;
    }

    public void setPricesell(double pricesell) {
        this.pricesell = pricesell;
    }

    public double getTaxerate() {
        return taxerate;
    }

    public void setTaxerate(double taxerate) {
        this.taxerate = taxerate;
    }

    public double getPricesellWithTax() {
        return pricesellWithTax;
    }

    public void setPricesellWithTax(double pricesellWithTax) {
        this.pricesellWithTax = pricesellWithTax;
    }

    public String getStockUnits() {
        return stockUnits;
    }

    public void setStockUnits(String stockUnits) {
        this.stockUnits = stockUnits;
    }

    public String getStockLocations() {
        return stockLocations;
    }

    public void setStockLocations(String stockLocations) {
        this.stockLocations = stockLocations;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public boolean isCom() {
        return isComProduct;
    }

    public void setCom(boolean isCom) {
        this.isComProduct = isCom;
    }

    public boolean isScale() {
        return isScaleProduct;
    }

    public void setScale(boolean isScale) {
        this.isScaleProduct = isScale;
    }

    public boolean isConstant() {
        return isConstantProduct;
    }

    public void setConstant(boolean isConstant) {
        this.isConstantProduct = isConstant;
    }

    public boolean isService() {
        return isService;
    }

    public void setService(boolean isService) {
        this.isService = isService;
    }
    
    public static SerializerRead<ProductInfoExtA> getSerializerRead() {
        return (DataRead dr) -> {
            ProductInfoExtA product = new ProductInfoExtA();
            product.id = dr.getString(1);
            product.name = dr.getString(2);
            product.stockUnits = dr.getString(3);
            product.stockLocations = dr.getString(4);
            product.pricesell = dr.getDouble(5);
            product.taxerate = dr.getDouble(6);
            product.pricesellWithTax = dr.getDouble(7);
            product.setCategoryId(dr.getString(8));
            product.setCom(dr.getBoolean(9));
            product.setScale(dr.getBoolean(10));
            product.setConstant(dr.getBoolean(11));
            product.setService(dr.getBoolean(12));
            return product;
        };
    }

}
