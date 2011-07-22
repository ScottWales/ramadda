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


import org.ramadda.repository.*;
import org.ramadda.repository.output.MapOutputHandler;

import ucar.unidata.geoloc.LatLonPointImpl;
import ucar.unidata.geoloc.LatLonRect;

import ucar.unidata.util.HtmlUtil;

import java.awt.geom.Rectangle2D;

import java.util.ArrayList;
import java.util.List;




/**
 * Class description
 *
 *
 * @version        $version$, Fri, Jul 22, '11
 * @author         Enter your name here...    
 */
public class MapProperties {

    /** _more_          */
    private String color = MapInfo.DFLT_BOX_COLOR;

    /** _more_          */
    private boolean selectable = false;

    /**
     * _more_
     *
     * @param color _more_
     * @param selectable _more_
     */
    public MapProperties(String color, boolean selectable) {
        this.color      = color;
        this.selectable = selectable;
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



}
