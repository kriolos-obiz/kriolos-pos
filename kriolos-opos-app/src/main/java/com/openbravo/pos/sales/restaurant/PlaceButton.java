//    KriolOS POS
//    Copyright (c) 2019-2023 KriolOS
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
//    along with this program.  If not, see <http://www.gnu.org/licenses/>.
package com.openbravo.pos.sales.restaurant;

import java.awt.Dimension;
import java.awt.Insets;
import javax.swing.JButton;
import javax.swing.SwingConstants;

/**
 * Presentation component representing a table place in the floor map view.
 */
public class PlaceButton extends JButton {

    private final Place place;
    private int diffX = 0;
    private int diffY = 0;

    public PlaceButton(Place place) {
        this.place = place;
        setFocusPainted(false);
        setFocusable(false);
        setRequestFocusEnabled(false);
        setHorizontalTextPosition(SwingConstants.CENTER);
        setVerticalTextPosition(SwingConstants.BOTTOM);
        setText(place.getName());
        setMargin(new Insets(2, 5, 2, 5));
    }

    public Place getPlace() {
        return place;
    }

    public int getDiffX() {
        return diffX;
    }

    public void setDiffX(int diffX) {
        this.diffX = diffX;
    }

    public int getDiffY() {
        return diffY;
    }

    public void setDiffY(int diffY) {
        this.diffY = diffY;
    }

    /**
     * Calculates and applies bounds on the parent floor container based on place coordinates.
     */
    public void updateBounds() {
        Dimension d = getPreferredSize();
        setPreferredSize(new Dimension(d.width + 90, d.height + 45));
        d = getPreferredSize();
        setBounds(place.getX() - d.width / 2, place.getY() - d.height / 2, d.width, d.height);
    }
}
