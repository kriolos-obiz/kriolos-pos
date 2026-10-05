/*
 * Copyright (C) 2025 Paulo Borges
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.openbravo.pos.catalog;

import com.openbravo.basic.BasicException;
import com.openbravo.pos.forms.DataLogicSales;
import com.openbravo.pos.sales.TaxesLogic;
import com.openbravo.pos.pim.CategoryInfo;
import com.openbravo.pos.pim.DataLogicPIM;
import com.openbravo.pos.ticket.ProductInfoExt;
import com.openbravo.pos.ticket.TaxInfo;
import com.openbravo.pos.util.ThumbNailBuilder;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Exceptions;

/**
 *
 * @author poolborges
 */
public class CatalogController {
    
    private final static Logger LOGGER = Logger.getLogger(CatalogController.class.getName());
    
    private static final int TAB_DEFAULT_WIDTH = 94;
    private static final int TAB_DEFAULT_HEIGHT = 80;
   
    private static final int CAT_DEFAULT_WIDTH = 64;
    private static final int CAT_DEFAULT_HEIGHT = 64;
    
    private final ThumbNailBuilder tnbcat;
    private final ThumbNailBuilder tnbbutton;
    private final ThumbNailBuilder tnbsubcat;

    private DataLogicPIM dataLogicPIM;
    private DataLogicSales dlLogicSales;
    private TaxesLogic taxeslogic;
    
    public CatalogController(DataLogicSales dlSales, DataLogicPIM dataLogicPIM){
    
        this.dataLogicPIM = dataLogicPIM;
        this.dlLogicSales = dlSales;
        this.taxeslogic = new TaxesLogic(dlLogicSales.getTaxListAll());
        
        tnbcat = new ThumbNailBuilder(CAT_DEFAULT_WIDTH, CAT_DEFAULT_HEIGHT, "com/openbravo/images/category.png");
        tnbsubcat = new ThumbNailBuilder(CAT_DEFAULT_WIDTH, CAT_DEFAULT_HEIGHT, "com/openbravo/images/subcategory.png");
        tnbbutton = new ThumbNailBuilder(TAB_DEFAULT_WIDTH, TAB_DEFAULT_HEIGHT, "com/openbravo/images/null.png");

    }
    
    public TaxesLogic getTaxesLogic(){
        return taxeslogic;
    }
    
    public List<CategoryInfo> getRootCategories(){
        
        List<CategoryInfo> list = new ArrayList<>();
        try {
            list = filterCategories(dataLogicPIM.getRootCategories());
        }
        catch (BasicException ex) {
            LOGGER.log(Level.WARNING,"get root categories",ex);
        }
        
        return list;
    }
    
    private List<CategoryInfo> filterCategories(List<CategoryInfo> list){
        return list.parallelStream().filter((category) -> (category.getCatalogEnabled() != null && category.getCatalogEnabled())).toList();
    }

    List<ProductInfoExt> getProductConstant() {
        List<ProductInfoExt> list = new ArrayList<>();
        try {
            list = dataLogicPIM.getProductConstant();
        }
        catch (BasicException ex) {
            LOGGER.log(Level.WARNING,"get constant products",ex);
        }
        return list;
    }

    List<CategoryInfo> getSubcategories(String categoryId) {
       
       List<CategoryInfo> list = new ArrayList<>();
        try {
            list = filterCategories(dataLogicPIM.getSubcategories(categoryId));
        }
        catch (BasicException ex) {
            LOGGER.log(Level.WARNING,"get subcategories: "+categoryId, ex);
        }
        
        return list;
    }

    List<ProductInfoExt> findProductByCategory(String categoryId) {
        List<ProductInfoExt> list = new ArrayList<>();
        try {
            list = dataLogicPIM.getProductCatalog(categoryId);
        }
        catch (BasicException ex) {
            LOGGER.log(Level.WARNING,"find product by category: "+categoryId, ex);
        }
        return list;
    }

    TaxInfo getTaxInfo(String taxCategoryID) {
        return taxeslogic.getTaxInfo(taxCategoryID);
    }

    List<ProductInfoExt> getProductCompanion(String productId) {
        List<ProductInfoExt> list = new ArrayList<>();
        try {
            list = dataLogicPIM.getProductComposite(productId);
        }
        catch (BasicException ex) {
            LOGGER.log(Level.WARNING,"get companion products: "+productId, ex);
        }
        return list;
    }

    ProductInfoExt getProductInfo(String productId) {
        ProductInfoExt data = null;
        try {
            data = dataLogicPIM.getProductInfo(productId);
        }
        catch (BasicException ex) {
            LOGGER.log(Level.WARNING,"get product info: "+productId,ex);
        }
        return data;
    }

    Image getThumbNailOrDefault(BufferedImage image) {
        return tnbbutton.getThumbNail(image);
    }
    
    Image getThumbNailOrDefaultCat(BufferedImage image) {
        return tnbcat.getThumbNail(image);
    }
    
    Image getThumbNailOrDefaultSubCat(BufferedImage image) {
        return tnbsubcat.getThumbNail(image);
    }
    
}
