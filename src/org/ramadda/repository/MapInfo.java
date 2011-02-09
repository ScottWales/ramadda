/*
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
 */

package org.ramadda.repository;

import org.ramadda.repository.output.MapOutputHandler;

import ucar.unidata.util.HtmlUtil;
import ucar.unidata.geoloc.LatLonRect;

import java.util.ArrayList;
import java.util.List;




/**
 */
public class MapInfo {
    private static int cnt = 0;
    private String mapVarName;
    private int width = 600;
    private int height = 450;

    StringBuffer js = new StringBuffer();
    StringBuffer html = new StringBuffer();


    public MapInfo() {
        this("map" + (cnt++));
    }


    public MapInfo(String mapVarName) {
        this(mapVarName, 600, 450);
    }

    public MapInfo(String mapVarName, int width, int height) {
        this.mapVarName = mapVarName;
        this.width = width;
        this.height = height;
    }

    /**
       Set the Width property.

       @param value The new value for Width
    **/
    public void setWidth (int value) {
	width = value;
    }

    /**
       Get the Width property.

       @return The Width
    **/
    public int getWidth () {
	return width;
    }

    /**
       Set the Height property.

       @param value The new value for Height
    **/
    public void setHeight (int value) {
	height = value;
    }

    /**
       Get the Height property.

       @return The Height
    **/
    public int getHeight () {
	return height;
    }





    /**
       Set the MapVarName property.

       @param value The new value for MapVarName
    **/
    public void setMapVarName (String value) {
	mapVarName = value;
    }

    /**
       Get the MapVarName property.

       @return The MapVarName
    **/
    public String getMapVarName () {
	return mapVarName;
    }




}
