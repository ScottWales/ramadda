/*
* Copyright 2008-2011 Jeff McWhirter/ramadda.org
*
* Permission is hereby granted, free of charge, to any person obtaining a copy of this 
* software and associated documentation files (the "Software"), to deal in the Software 
* without restriction, including without limitation the rights to use, copy, modify, 
* merge, publish, distribute, sublicense, and/or sell copies of the Software, and to 
* permit persons to whom the Software is furnished to do so, subject to the following conditions:
* 
* The above copyright notice and this permission notice shall be included in all copies 
* or substantial portions of the Software.
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
* PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE 
* FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR 
* OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER 
* DEALINGS IN THE SOFTWARE.
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
        this(color, selectable, false);
    }

    /**
     * Create a MapProperties
     *
     * @param color the color
     * @param selectable  true if selectable
     * @param zoom  true if should zoom to bounds
     */
    public MapProperties(String color, boolean selectable, boolean zoom) {
        this.color        = color;
        this.selectable   = selectable;
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
