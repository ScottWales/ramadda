/*
 * Copyright 2008-2012 Jeff McWhirter/ramadda.org
 *                     Don Murray/CU-CIRES
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

package org.ramadda.util;


public class Station {
    private String id;
    private String name;
    private double latitude;
    private double longitude;
    private double elevation;

    public Station(String id, String name, double latitude, double longitude, double elevation) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.elevation = elevation;
    }

    /**
       Set the Id property.

       @param value The new value for Id
    **/
    public void setId (String value) {
	id = value;
    }

    /**
       Get the Id property.

       @return The Id
    **/
    public String getId () {
	return id;
    }

    /**
       Set the Name property.

       @param value The new value for Name
    **/
    public void setName (String value) {
	name = value;
    }

    /**
       Get the Name property.

       @return The Name
    **/
    public String getName () {
	return name;
    }

    /**
       Set the Latitude property.

       @param value The new value for Latitude
    **/
    public void setLatitude (double value) {
	latitude = value;
    }

    /**
       Get the Latitude property.

       @return The Latitude
    **/
    public double getLatitude () {
	return latitude;
    }

    /**
       Set the Longitude property.

       @param value The new value for Longitude
    **/
    public void setLongitude (double value) {
	longitude = value;
    }

    /**
       Get the Longitude property.

       @return The Longitude
    **/
    public double getLongitude () {
	return longitude;
    }

    /**
       Set the Elevation property.

       @param value The new value for Elevation
    **/
    public void setElevation (double value) {
	elevation = value;
    }

    /**
       Get the Elevation property.

       @return The Elevation
    **/
    public double getElevation () {
	return elevation;
    }

    public String toString() {
        return id +" lat:" + latitude +" lon:" + longitude;
    }
}
