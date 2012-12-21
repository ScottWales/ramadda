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

import org.ramadda.util.Utils;
import org.apache.commons.lang.text.StrTokenizer;
import ucar.unidata.util.StringUtil;
import ucar.unidata.util.IOUtil;

import java.io.File;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class Site {

    private String id;
    private double latitude;
    private double longitude;
    
    private String address;
    private String city;
    private String state;
    private String zipCode;


    public Site(String id, double latitude, double longitude) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Override
        public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof Site)) return false;
        return id.equals(((Site)o).id);
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
       Set the Address property.

       @param value The new value for Address
    **/
    public void setAddress (String value) {
	address = value;
    }

    /**
       Get the Address property.

       @return The Address
    **/
    public String getAddress () {
	return address;
    }

    /**
       Set the City property.

       @param value The new value for City
    **/
    public void setCity (String value) {
	city = value;
    }

    /**
       Get the City property.

       @return The City
    **/
    public String getCity () {
	return city;
    }


    /**
       Set the State property.

       @param value The new value for State
    **/
    public void setState (String value) {
	state = value;
    }

    /**
       Get the State property.

       @return The State
    **/
    public String getState () {
	return state;
    }

    /**
       Set the ZipCode property.

       @param value The new value for ZipCode
    **/
    public void setZipCode (String value) {
	zipCode = value;
    }

    /**
       Get the ZipCode property.

       @return The ZipCode
    **/
    public String getZipCode () {
	return zipCode;
    }



}


