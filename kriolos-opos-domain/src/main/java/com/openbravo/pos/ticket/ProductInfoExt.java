//    KrOS POS
//    Copyright (c) 2019-2023 KriolOS
//    
//
//     
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with KrOS POS.  If not, see <http://www.gnu.org/licenses/>.
package com.openbravo.pos.ticket;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.DataRead;
import com.openbravo.data.loader.ImageUtils;
import com.openbravo.data.loader.SerializerRead;
import java.util.Properties;
import com.openbravo.format.Formats;
import com.openbravo.pos.domain.utils.AmountCalculatorUtil;
import java.awt.image.BufferedImage;

/**
 * Holds extended information about a product in the POS system.
 * 
 * @author adrianromero
 * @author poolborges
 */
public class ProductInfoExt {

    /** Unique version identifier for serialization. */
    private static final long serialVersionUID = 7587696873036L;

    /** Unique identifier (UUID/Primary Key) of the product. */
    protected String prodId;

    /** Internal reference or SKU number of the product. */
    protected String prodRef;

    /** Barcode, EAN, or UPC code used for scanning the product. */
    protected String prodCode;

    /** The type of barcode format used (e.g., EAN13, Code128). */
    protected String prodCodetype;    

    /** Display name of the product. */
    protected String prodName;

    /** The wholesale/purchase cost price paid to the supplier. */
    protected double prodPriceBuy;

    /** The retail/selling price charged to the customer (excluding or including tax depending on config). */
    protected double prodPriceSell;

    /** Foreign key linking to the category this product belongs to. */
    protected String categoryid;

    /** Foreign key linking to the tax category applied to this product. */
    protected String taxcategoryid;

    /** Foreign key linking to the set of attributes/properties assigned to this product. */
    protected String attributesetid;

    /** Evaluated financial cost of the product currently held in inventory. */
    protected double stockCost;

    /** The physical space/volume the product takes up in storage. */
    protected double stockVolume;

    /** Visual thumbnail image of the product for the POS grid button. */
    protected BufferedImage prodImage;

    /** Indicates whether this product is a modifier, side-dish, or companion to a main item. */
    protected boolean prodIsCompanion;

    /** Indicates whether this product is sold by weight and requires an external scale. */
    protected boolean prodIsScale;

    /** Indicates whether the stock level is locked/constant (does not decrement upon sale). */
    protected boolean prodIsConstant;

    /** Indicates whether the order needs to be printed to the kitchen or bar remote printers. */
    protected boolean prodIsPrintKitchenBar;

    /** Tracks whether the status of this product has been synchronized with external systems. */
    protected boolean prodSendStatus;    

    /** Indicates whether the item is a non-physical service rather than a physical stock item. */
    private boolean prodIsService;

    /** Key-value pairs containing custom properties or metadata for the product. */
    protected Properties attributes;

    /** Custom text layout or HTML formatting used for rendering the product button on screen. */
    protected String prodDisplay;

    /** Indicates whether the product has a variable/free-entry price prompted at checkout. */
    protected boolean prodIsVariablePrice;

    /** Indicates whether the product prompts for mandatory attribute/property selection upon being added. */
    protected boolean prodIsVariableProductAttribute;

    /** Tooltip text or pop-up warning message displayed when selling this product. */
    protected String prodTextTip;

    /** Indicates whether the product comes with or requires a warranty tracking setup. */
    protected boolean prodIsWarranty;

    /** Current available physical stock units left in inventory. */
    protected double prodStockUnits;

    /** The specific target printer name or redirect path dedicated to this item. */
    protected String prodPrinter;

    /** Foreign key linking to the preferred supplier/vendor of this product. */
    protected String prodSupplierId;

    /** Foreign key linking to the Unit of Measure (UOM) used (e.g., Units, Liters, Kg). */
    protected String prodUOMId;   

    /** Temporary or memo date field used for logging specific transactional timelines or batches. */
    protected String memoDate;


    public ProductInfoExt() {
        prodId = null;
        prodRef = "0000";
        prodCode = "0000";
        prodCodetype = null;
        prodName = null;
        prodPriceBuy = 0.0;
        prodPriceSell = 0.0;
        categoryid = null;
        taxcategoryid = null;
        attributesetid = null;
        stockCost = 0.0;
        stockVolume = 0.0;
        prodImage = null;
        prodIsCompanion = false;
        prodIsScale = false;
        prodIsConstant = false;
        prodIsPrintKitchenBar = false;
        prodSendStatus = false;
        prodIsService = false;
        attributes = new Properties();
        prodDisplay = null;
        prodIsVariablePrice = false;
        prodIsVariableProductAttribute = false;
        prodTextTip = null;
        prodIsWarranty = false;
        prodStockUnits = 0.0;
        prodPrinter = null;
        prodSupplierId = "0";
        prodUOMId = "0";        
        memoDate = null;
    }

    public final String getID() {
        return prodId;
    }
    public final void setID(String id) {
        prodId = id;
    }

    public final String getReference() {
        return prodRef;
    }
    public final void setReference(String sRef) {
        prodRef = sRef;
    }

    public final String getCode() {
        return prodCode;
    }
    public final void setCode(String sCode) {
        prodCode = sCode;
    }

    public final String getCodetype() {
        return prodCodetype;
    }
    public final void setCodetype(String sCodetype) {
        prodCodetype = sCodetype;
    }
    
    public final String getName() {
        return prodName;
    }
    public final void setName(String sName) {
        prodName = sName;
    }

    public final double getPriceBuy() {
        return prodPriceBuy;
    }
    public final void setPriceBuy(double dPrice) {
        prodPriceBuy = dPrice;
    }

    public final double getPriceSell() {
        return prodPriceSell;
    }
    public final void setPriceSell(double dPrice) {
        prodPriceSell = dPrice;
    }    

    public final String getCategoryID() {
        return categoryid;
    }
    public final void setCategoryID(String sCategoryID) {
        categoryid = sCategoryID;
    }

    public final String getTaxCategoryID() {
        return taxcategoryid;
    }
    public final void setTaxCategoryID(String value) {
        taxcategoryid = value;
    }

    public final String getAttributeSetID() {
        return attributesetid;
    }
    public final void setAttributeSetID(String value) {
        attributesetid = value;
    }

    public final double getStockCost() {
        return stockCost;
    }
    public final void setStockCost(double dPrice) {
        stockCost = dPrice;
    }

    public final double getStockVolume() {
        return stockVolume;
    }
    public final void setStockVolume(double dStockVolume) {
        stockVolume = dStockVolume;
    }

    public BufferedImage getImage() {
        return prodImage;
    }
    public void setImage(BufferedImage img) {
        prodImage = img;
    }
    
    public final boolean isCom() {
        return prodIsCompanion;
    }
    public final void setCom(boolean bValue) {
        prodIsCompanion = bValue;
    }

    public final boolean isScale() {
        return prodIsScale;
    }
    public final void setScale(boolean bValue) {
        prodIsScale = bValue;
    }

    public final boolean isConstant() {
        return prodIsConstant;
    }
    public final void setConstant(boolean bValue) {
        prodIsConstant = bValue;
    }

    public final boolean isPrintKB() {
        return prodIsPrintKitchenBar;
    }
    public final void setPrintKB(boolean bValue) {
        prodIsPrintKitchenBar = bValue;
    }
    
    public final boolean isSendStatus() {
        return prodSendStatus;
    }
    public final void setSendStatus(boolean bValue) {
        prodSendStatus = bValue;
    }

    public final boolean isService() {
        return prodIsService;
    }
    public final void setService(boolean bValue) {
        prodIsService = bValue;
    }

    public String getProperty(String key) {
        return attributes.getProperty(key);
    }
    public String getProperty(String key, String defaultvalue) {
        return attributes.getProperty(key, defaultvalue);
    }
    public void setProperty(String key, String value) {
        attributes.setProperty(key, value);
    }
    public Properties getProperties() {
        return attributes;
    }

    public final String getDisplay() {
        return prodDisplay;
    }
    public final void setDisplay(String sDisplay) {
        prodDisplay = sDisplay;
    }

    public void setVprice(Boolean value) {
        prodIsVariablePrice = value;
    }
    
    public final boolean isVprice() {
        return prodIsVariablePrice;
    }

    public void setVerpatrib(Boolean value) {
        prodIsVariableProductAttribute = value;
    }
    
    public final boolean isVerpatrib() {
        return prodIsVariableProductAttribute;
    }

    public final String getTextTip() {
        return prodTextTip;
    }
    public final void setTextTip(String value) {
        prodTextTip = value;
    }

    public final boolean getWarranty() {
        return prodIsWarranty;
    }
    public final void setWarranty(boolean bValue) {
        prodIsWarranty = bValue;
    }

    public final Double getStockUnits() { 
        return prodStockUnits;
    }
    public final void setStockUnits(double dStockUnits) {    
        prodStockUnits = dStockUnits;
    }

    public String printPriceSell() {
        return Formats.CURRENCY.formatValue(getPriceSell());
    }
    
    public final double getPriceSellTax(TaxInfo tax) {
        return AmountCalculatorUtil.calcPriceWithTaxInclusive(prodPriceSell, tax);
    }
    public String printPriceSellTax(TaxInfo tax) {        
        return Formats.CURRENCY.formatValue(getPriceSellTax(tax));
    }
    
    public final String getPrinter() {
        return prodPrinter;
    }
    public final void setPrinter(String value) {
        prodPrinter = value;
    }    

    public final String getSupplierID() {
        return prodSupplierId;
    }
    public final void setSupplierID(String sSupplierID) {
        prodSupplierId = sSupplierID;
    }
    
    public final String getUomID() {
          return prodUOMId;
    }
    public final void setUomID(String sUomID) {
	prodUOMId = sUomID;
    }    

    public String getMemoDate() {
        return memoDate;
    }
    public void setMemoDate(String memodate) {
        this.memoDate = memodate;
    }
    public String printMemoDate() {       
        return Formats.STRING.formatValue(memoDate);
    }    

    public static SerializerRead<ProductInfoExt> getSerializerRead() {
        return new SerializerRead<ProductInfoExt>() {
            @Override
            public ProductInfoExt readValues(DataRead dr) throws BasicException {
                ProductInfoExt product = new ProductInfoExt();
                product.prodId = dr.getString(1);                                 
                product.prodRef = dr.getString(2);                               
                product.prodCode = dr.getString(3);                              
                product.prodCodetype = dr.getString(4);                              
                product.prodName = dr.getString(5);                              
                product.prodPriceBuy = dr.getDouble(6);                          
                product.prodPriceSell = dr.getDouble(7);                         
                product.categoryid = dr.getString(8);                          
                product.taxcategoryid = dr.getString(9);                        
                product.attributesetid = dr.getString(10); 
                product.stockCost = dr.getDouble(11);
                product.stockVolume = dr.getDouble(12);
                product.prodImage = ImageUtils.readImage(dr.getBytes(13));            
                product.prodIsCompanion = dr.getBoolean(14);                              
                product.prodIsScale = dr.getBoolean(15);                            
                product.prodIsConstant = dr.getBoolean(16);                         
                product.prodIsPrintKitchenBar = dr.getBoolean(17);                         
                product.prodSendStatus = dr.getBoolean(18);                                         
                product.prodIsService = dr.getBoolean(19);                                         
                product.attributes = ImageUtils.readProperties(dr.getBytes(20));
                product.prodDisplay = dr.getString(21); 
                product.prodIsVariablePrice = dr.getBoolean(22);                          
                product.prodIsVariableProductAttribute = dr.getBoolean(23);                                       
                product.prodTextTip = dr.getString(24);                          
                product.prodIsWarranty = dr.getBoolean(25);                        
                product.prodStockUnits = dr.getDouble(26); 
                product.prodPrinter = dr.getString(27);
                product.prodSupplierId = dr.getString(28);
                product.prodUOMId = dr.getString(29);
                product.memoDate = dr.getString(30);

                return product;
            }
        };
    }

    @Override
    public final String toString() {
        return prodRef + " - " + prodName;
    }
}