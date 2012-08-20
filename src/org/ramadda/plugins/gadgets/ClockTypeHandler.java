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

package org.ramadda.plugins.gadgets;


import org.ramadda.repository.*;
import org.ramadda.repository.output.*;
import org.ramadda.repository.type.*;

import org.ramadda.util.HtmlUtils;

import org.w3c.dom.*;

import ucar.unidata.util.StringUtil;



import java.util.ArrayList;
import java.util.Date;

import java.util.List;
import java.util.TimeZone;


/**
 *
 *
 */
public class ClockTypeHandler extends GenericTypeHandler {


    /**
     * _more_
     *
     * @param repository _more_
     * @param entryNode _more_
     *
     * @throws Exception _more_
     */
    public ClockTypeHandler(Repository repository, Element entryNode)
            throws Exception {
        super(repository, entryNode);
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param group _more_
     * @param subGroups _more_
     * @param entries _more_
     * @param entry _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result getHtmlDisplay(Request request, Entry entry)
            throws Exception {
        //        String orient = entry.getValue(0,"");
        String timezone       = entry.getValue(0, "");
        int    timezoneOffset = 0;
        if (timezone.length() > 0) {
            TimeZone t = TimeZone.getTimeZone(timezone);
            timezoneOffset = t.getOffset(new Date().getTime()) / 1000 / 3600;
            System.err.println("offset:" + timezoneOffset);
        }
        StringBuffer sb    = new StringBuffer();
        String       title = entry.getName();
        sb.append(
            "<script src=\"//www.gmodules.com/ig/ifr?url=http://www.gstatic.com/ig/modules/datetime_v3/datetime_v3.xml&amp;up_color=grey&amp;up_dateFormat=wmd&amp;up_firstDay=0&amp;up_clocks=%5B%5D&amp;up_mainClock=&amp;up_mainClockDSTOffset=&amp;up_24hourClock=true&amp;up_showWorldClocks=true&amp;up_useServerTime=false&amp;synd=open&amp;w=320&amp;h=2000&amp;" + HtmlUtils.arg("title", title, true) + "&amp;up_mainClockTimeZoneOffset=" + timezoneOffset + "&amp;lang=en&amp;country=ALL&amp;border=http%3A%2F%2Fwww.gmodules.com%2Fig%2Fimages%2F&amp;output=js\"></script>");

        return new Result("Clock", sb);
    }




}
