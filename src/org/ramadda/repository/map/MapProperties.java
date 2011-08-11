/*
 * Copyright 2008-2011 Jeff McWhirter/ramadda.org
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 */

package org.ramadda.repository.map;



/**
 * A class to hold some map properties
 *
 * @author   RAMADDA development team
 */
public class MapProperties {

    /** color property */
    private String color = MapInfo.DFLT_BOX_COLOR;

    /** selectable property */
    private boolean selectable = false;

    /** zoom to extent property */
    private boolean zoomToExtent = false;

    /**
     * Create a MapProperties
     *
     * @param color  the color 
     * @param selectable  true if selectable
     */
    public MapProperties(String color, boolean selectable) {
        this(color,selectable,false);
    }

    /**
     * Create a MapProperties
     *
     * @param color the color
     * @param selectable  true if selectable
     * @param zoom  true if should zoom to bounds
     */
    public MapProperties(String color, boolean selectable, boolean zoom) {
        this.color      = color;
        this.selectable = selectable;
        this.zoomToExtent = zoom;
    }

    /**
     *  Set the Color property.
     *
     *  @param value The new value for Color
     */
    public void setColor(String value) {
        color = value;
    }

    /**
     *  Get the Color property.
     *
     *  @return The Color
     */
    public String getColor() {
        return color;
    }

    /**
     *  Set the Selectable property.
     *
     *  @param value The new value for Selectable
     */
    public void setSelectable(boolean value) {
        selectable = value;
    }

    /**
     *  Get the Selectable property.
     *
     *  @return The Selectable
     */
    public boolean getSelectable() {
        return selectable;
    }

    /**
     *  Set the ZoomToExtent property.
     *
     *  @param value The new value for ZoomToExtent
     */
    public void setZoomToExtent(boolean value) {
        zoomToExtent = value;
    }

    /**
     *  Get the ZoomToExtent property.
     *
     *  @return The ZoomToExtent
     */
    public boolean getZoomToExtent() {
        return zoomToExtent;
    }



}
