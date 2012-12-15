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

package org.ramadda.plugins.police;

import org.ramadda.util.Utils;
import org.apache.commons.lang.text.StrTokenizer;
import ucar.unidata.util.StringUtil;
import ucar.unidata.util.IOUtil;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class CellSite {

    private String id;
    private double latitude;
    private double longitude;
    
    public CellSite(String id, double latitude, double longitude) {
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
        if(!(o instanceof CellSite)) return false;
        return id.equals(((CellSite)o).id);
    }

    private static Hashtable<String,CellSite> sites;

    public  Hashtable<String,CellSite> getSites() {
        if(sites == null) {
            Hashtable<String,CellSite>   tmp = new Hashtable<String,CellSite>();
            String[] siteFiles= {"Denver_LEA.V0.20110308.0100.csv",
                                 "Sacramento_LEA.V0.20110308.0100.csv",
                                 "SaltLake_LEA.V0.20110308.0101.csv",
                                 "SanFrancisco_LEA.V0.20110308.0100.csv"};
            for(String siteFile: siteFiles) {
                readSites(tmp, "/org/ramadda/plugins/police/resources/sites/" + siteFile);
            }
            sites = tmp;
        }
        return sites;
    }



    public static Hashtable<String,CellSite> readSites(Hashtable<String,CellSite>   sites, String file) throws Exception {
        String delimiter = "\r";
        String contents = IOUtil.readContents(file, CellSite.class);
        for(List<String>toks: Utils.tokenize(contents, "\r", ",",3)) {
            if(toks.size()!=19) {
                System.err.println("bad:" + toks.size() +" " + StringUtil.join("',", toks));
                continue;
            }

            //Market SID,Switch Number,Switch Name,Cell Number,E-911 Latitude Degrees (NAD83),E-911 Longitude Degrees (NAD83),Street Address,City,State,Zip Code,Sector,Technology,Azimuth (deg),Antenna H-BW (deg),PN Offset,Extended Base ID,PN Increment,Max Antenna Range (feet),CDMA Channel Type
            
            if(toks.get(3).length()==0) {
                System.err.println("bad:" +  " " + StringUtil.join("',", toks));
                continue;
            }
            int cellNumber = Integer.parseInt(toks.get(3));
            double latitude  =  Double.parseDouble(toks.get(4));
            double longitude  =  Double.parseDouble(toks.get(5));
            CellSite site = new CellSite(toks.get(3), latitude,longitude);
            sites.put(site.id, site);
            //            System.out.println(cellNumber+", " + latitude + ", " + longitude);
        }

        return sites;
    }


    public static void  main(String []args) throws Exception {
        Hashtable<String,CellSite>   sites = getSites();
        //        System.out.println("");
        for(String callFile: args) {
            String delimiter = "\r";
            String contents = IOUtil.readContents(callFile, CellSite.class);
            for(List<String>toks: Utils.tokenize(contents, "\r", ",",1)) {
                if(toks.size()!=11) {
                    System.err.println("bad:" + StringUtil.join(",",toks));
                    continue;
                }
                String site = toks.get(6);
                CellSite cellSite =  sites.get(site);
                if(cellSite==null) {
                    System.err.println("No location for site:" + site +"\nline:" + StringUtil.join(",", toks));
                    continue;
                } 
                //Network Element Name 0
                //Mobile Directory Number 1
                //Dialed Digit Number 2
                //Call Direction 3
                //Seizure Dt Tm 4 
                //Seizure Duration 5
                //First Serving Cell Site 6
                //First Serving Cell Face 7
                //Last Serving Cell Site
                //Last Serving Cell Face
                //Calling Party Number

                String from = toks.get(1);
                String to = toks.get(2);
                String time = toks.get(4);
                System.out.println(cellSite.getLatitude() + "," + cellSite.getLatitude() +"," + time+"," + to);
            }
        }


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




}


