/*
* Copyright 2008-2013 Geode Systems LLC
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

package org.ramadda.plugins.incident;


import org.ramadda.repository.*;
import org.ramadda.repository.type.*;


import org.ramadda.util.HtmlUtils;
import org.ramadda.util.GeoUtils;
import org.ramadda.util.Utils;


import org.w3c.dom.*;

import ucar.unidata.util.StringUtil;

import java.util.Date;
import java.util.List;


/**
 *
 *
 */
public class LocationTypeHandler extends ExtensibleGroupTypeHandler {



    /**
     * _more_
     *
     * @param repository _more_
     * @param entryNode _more_
     *
     * @throws Exception _more_
     */
    public LocationTypeHandler(Repository repository, Element entryNode)
            throws Exception {
        super(repository, entryNode);
    }


@Override
    public String getEntryName(Entry entry) {
        String  name = super.getEntryName(entry);
        if(!Utils.stringDefined(name)) {
            name = entry.getValue(0, "");
        }
        //        System.err.println("NAME:" + name);
        return name;
    }


    @Override
    public void initializeEntryFromForm(Request request, Entry entry,
                                        Entry parent, boolean newEntry)
        throws Exception {
        super.initializeEntryFromForm(request, entry,
                                      parent, newEntry);
        georeferenceEntry(request, entry);
    }

    public void initializeEntryFromXml(Request request, Entry entry,
                                       Element node)
            throws Exception {
        initializeEntryFromXml(request, entry, node);
        georeferenceEntry(request, entry);
    }


    /**
     */
    private   void georeferenceEntry(Request request, Entry entry) {
        if(entry.isGeoreferenced()) return;
        //TODO: if the entry has a location then don't do this?
        String address = entry.getValue(0, (String) null);
        String city = entry.getValue(1, (String) null);
        String state = entry.getValue(2, (String) null);
        if(!Utils.stringDefined(address)) return;
        String fullAddress = address +"," + city +"," + state;
        double[] loc = GeoUtils.getLocationFromAddress(fullAddress);
        if(loc == null) {
            System.err.println("no geo for address:" + fullAddress);
        } else {
            System.err.println("got geo for address:" + fullAddress);
            entry.setLatitude(loc[0]);
            entry.setLongitude(loc[1]);
        } 
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    @Override
    public String getIconUrl(Request request, Entry entry) throws Exception {
        double depth =   entry.getValue(4, 0.0);
        if(depth == 0) 
            return iconUrl("/incident/flag_green.png");
        if(depth<=2) 
            return iconUrl("/incident/flag_blue.png");
        return iconUrl("/incident/flag_red.png");
    }



}
