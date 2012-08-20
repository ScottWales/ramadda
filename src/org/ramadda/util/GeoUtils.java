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


import org.w3c.dom.Element;

import ucar.unidata.util.IOUtil;
import ucar.unidata.util.StringUtil;
import ucar.unidata.xml.XmlUtil;


/**
 */
public class GeoUtils {


    /**
     * Look up the location of the given address
     *
     * @param address The address
     *
     * @return The location or null if not found
     */
    public static double[] getLocationFromAddress(String address) {
        if (address == null) {
            return null;
        }
        address = address.trim();
        if (address.length() == 0) {
            return null;
        }

        String latString      = null;
        String lonString      = null;
        String encodedAddress = StringUtil.replace(address, " ", "%20");

        try {

            String url =
                "http://where.yahooapis.com/geocode?appid=ramadda&q="
                + encodedAddress;
            String  result  = IOUtil.readContents(url, GeoUtils.class);
            Element root    = XmlUtil.getRoot(result);
            Element latNode = XmlUtil.findDescendant(root, "latitude");
            Element lonNode = XmlUtil.findDescendant(root, "longitude");
            if ((latNode != null) && (lonNode != null)) {
                latString = XmlUtil.getChildText(latNode);
                lonString = XmlUtil.getChildText(lonNode);
            }
        } catch (Exception exc) {}
        if ((latString != null) && (lonString != null)) {
            return new double[] { Double.parseDouble(latString),
                                  Double.parseDouble(lonString) };

        }

        return null;
    }

}
