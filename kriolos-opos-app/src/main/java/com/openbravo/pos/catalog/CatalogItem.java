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

import java.awt.Image;

/**
 *
 * @author poolborges
 */
public class CatalogItem {
    private String text; 
    private String textTip;
    private String price;
    private Image image;
    private String colorHex;
    
    public CatalogItem(String text) {
        this.text = text;
    }
    
    public CatalogItem(String text, String textTip) {
        this.text = text;
        this.textTip = textTip;
    }

    public CatalogItem(String text, String textTip, Image image) {
        this.text = text;
        this.textTip = textTip;
        this.image = image;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTextTip() {
        return textTip;
    }

    public void setTextTip(String textTip) {
        this.textTip = textTip;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public void setColorHex(String colorHex) {
        this.colorHex = colorHex;
    }

    public String getColorHex() {
        return this.colorHex;
    }
}
