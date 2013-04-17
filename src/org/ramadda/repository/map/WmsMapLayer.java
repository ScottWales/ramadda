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

package org.ramadda.repository.map;


import org.ramadda.repository.*;
import org.ramadda.repository.metadata.Metadata;
import org.ramadda.repository.metadata.MetadataHandler;
import org.ramadda.util.HtmlUtils;

import ucar.unidata.geoloc.LatLonPointImpl;
import ucar.unidata.geoloc.LatLonRect;
import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;


import java.awt.geom.Rectangle2D;

import java.util.ArrayList;
import java.util.List;



/**
 * A MapInfo class to hold map info
 */
public class WmsMapLayer {
    private String url;
    private String id;
    private String label;
    private String labelArg;
    private String legendImage;
    private String legendText="";
    private String legendLabel;

    public WmsMapLayer(String id,
                       String url,
                       String label,
                       String legendImage,
                       String legendLabel) {
        this.url=url;
        this.id=id;
        this.label=label;
        this.legendImage=legendImage;
        this.legendLabel=legendLabel;
        this.labelArg  = this.label.replaceAll("'","\\\\'");
    }

    public WmsMapLayer(Repository repository, String prefix, String id) { 
        this.id=id;
        prefix = prefix+"." + id;
        this.url= repository.getProperty(prefix+".url","");
        if(this.url.length()==0) {
            throw new IllegalArgumentException("no url defined for wms layer:" + prefix+".url");
        }
        this.label= repository.getProperty(prefix+".label","NONE");
        this.legendImage=repository.getProperty(prefix+".legend.image","");
        this.legendText=repository.getProperty(prefix+".legend.text","");
        this.legendLabel=repository.getProperty(prefix+".legend.label","");
        this.labelArg  = this.label.replaceAll("'","\\\\'");
    }



    public static List<WmsMapLayer> makeLayers(Repository repository, String prefix) {
        List<WmsMapLayer> layers = new ArrayList<WmsMapLayer>();
        for(String id: StringUtil.split(repository.getProperty(prefix+".layers",""),",",true,true)) {
            try {
                layers.add(new WmsMapLayer(repository, prefix, id));
            } catch(Exception exc) {
                repository.getLogManager().logError("Adding WMS layer:" + exc, exc);
            }
        } 
        return layers;
    }



    public void addToMap(Request request, MapInfo mapInfo) {

        mapInfo.addJS(HtmlUtils.call(mapInfo.getVariableName() + ".addWMSLayer",
                                     HtmlUtils.jsMakeArgs(new String[]{HtmlUtils.squote(labelArg), HtmlUtils.squote(url), HtmlUtils.squote(id),"true"},false)));
        mapInfo.addJS("\n");
    }


    /**
       Set the Url property.

       @param value The new value for Url
    **/
    public void setUrl (String value) {
	url = value;
    }

    /**
       Get the Url property.

       @return The Url
    **/
    public String getUrl () {
	return url;
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
       Set the Label property.

       @param value The new value for Label
    **/
    public void setLabel (String value) {
	label = value;
    }

    /**
       Get the Label property.

       @return The Label
    **/
    public String getLabel () {
	return label;
    }

    /**
       Set the LegendImage property.

       @param value The new value for LegendImage
    **/
    public void setLegendImage (String value) {
	legendImage = value;
    }

    /**
       Get the LegendImage property.

       @return The LegendImage
    **/
    public String getLegendImage () {
	return legendImage;
    }

    /**
       Set the LegendLabel property.

       @param value The new value for LegendLabel
    **/
    public void setLegendLabel (String value) {
	legendLabel = value;
    }

    /**
       Get the LegendLabel property.

       @return The LegendLabel
    **/
    public String getLegendLabel () {
	return legendLabel;
    }




    /**
       Set the LegendText property.

       @param value The new value for LegendText
    **/
    public void setLegendText (String value) {
	legendText = value;
    }

    /**
       Get the LegendText property.

       @return The LegendText
    **/
    public String getLegendText () {
	return legendText;
    }



}
