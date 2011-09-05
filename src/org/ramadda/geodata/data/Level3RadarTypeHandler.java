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

package org.ramadda.geodata.data;


import org.ramadda.repository.*;

import org.ramadda.repository.type.*;


import org.w3c.dom.*;


import ucar.unidata.sql.SqlUtil;
import ucar.unidata.util.DateUtil;

import ucar.unidata.util.HtmlUtil;

import ucar.unidata.util.IOUtil;
import ucar.unidata.util.LogUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.xml.XmlUtil;

import java.sql.PreparedStatement;

import java.sql.ResultSet;
import java.sql.Statement;


import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;


/**
 *
 *
 * @author RAMADDA Development Team
 * @version $Revision: 1.3 $
 */
public class Level3RadarTypeHandler extends GenericTypeHandler {


    /**
     * _more_
     *
     * @param repository _more_
     * @param entryNode _more_
     *
     * @throws Exception _more_
     */
    public Level3RadarTypeHandler(Repository repository, Element entryNode)
            throws Exception {
        super(repository, entryNode);
    }

    /**
     * _more_
     *
     * @param entry _more_
     * @param request _more_
     * @param links _more_
     *
     *
     * @throws Exception _more_
     */
    public void getEntryLinks(Request request, Entry entry, List<Link> links)
            throws Exception {
        super.getEntryLinks(request, entry, links);
        if (entry.getValues() == null) {
            return;
        }
        Object[] values = entry.getValues();
        if ((values.length >= 2) && (values[0] != null)
                && (values[1] != null)) {
            links.add(
                new Link(
                    HtmlUtil.url(
                        "http://radar.weather.gov/radar.php", "rid",
                        (String) entry.getValues()[0], "product",
                        (String) entry.getValues()[1]), iconUrl(
                            "/icons/radar.gif"), "Show NWS Radar Site"));
        }
    }


    /**
     * _more_
     *
     * @param entry _more_
     *
     * @throws Exception _more_
     */
    public void xxxinitializeNewEntry(Entry entry) throws Exception {
        String station = (String) entry.getValues()[0];
        String lat =
            getRepository().getFieldDescription(station + ".lat",
                "/org/ramadda/repository/resources/level3radar.station.properties",
                null);
        String lon =
            getRepository().getFieldDescription(station + ".lon",
                "/org/ramadda/repository/resources/level3radar.station.properties",
                null);


        if ((lat != null) && (lon != null)) {
            double latD = Misc.decodeLatLon(lat);
            double lonD = Misc.decodeLatLon(lon);
            entry.setSouth(latD - 2);
            entry.setNorth(latD + 2);
            entry.setEast(lonD + 2);
            entry.setWest(lonD - 2);
            entry.trimAreaResolution();
        }

    }




}
