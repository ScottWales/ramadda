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

package org.ramadda.plugins.time;


import org.ramadda.repository.*;
import org.ramadda.repository.output.*;
import org.ramadda.repository.type.*;

import org.w3c.dom.*;

import org.ramadda.util.HtmlUtils;
import ucar.unidata.util.StringUtil;

import java.util.ArrayList;
import java.util.Date;

import java.util.List;


/**
 *
 *
 */
public class CountdownTypeHandler extends GenericTypeHandler {


    /**
     * _more_
     *
     * @param repository _more_
     * @param entryNode _more_
     *
     * @throws Exception _more_
     */
    public CountdownTypeHandler(Repository repository, Element entryNode)
            throws Exception {
        super(repository, entryNode);
    }

    private int countdownCnt =0;
    private String countdownHtml;

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
        if(countdownHtml==null) {
            countdownHtml = getRepository().getResource(
                                                        "/org/ramadda/plugins/countdown/countdown.html");
        }
        String orient = entry.getValue(0,"");
        String howMany = entry.getValue(1,"");
        if(howMany.length()==0) howMany = "4";
        StringBuffer sb = new StringBuffer(countdownHtml);
        sb.append("<table><tr><td><center>");
        sb.append(getRepository().formatDate(request,entry.getStartDate(),
                                             getEntryManager().getTimezone(entry)));
        Date to  = new Date(entry.getStartDate());
        String id = "countdownid_" + (countdownCnt++);
        //        sb.append(HtmlUtils.cssBlock(".countdown-clock {font-size: 150%;}\n.countdown-number {color:#A94DEA;\n.countdown-label {color:#000;}\n"));
        String inner = HtmlUtils.div("", HtmlUtils.id(id)+HtmlUtils.cssClass("countdown-clock"));
        sb.append("<table><td><td>" +HtmlUtils.div(inner,HtmlUtils.cssClass("countdown"))+"</td></tr></table>");
        sb.append(HtmlUtils.script("$(document).ready(function() {countdownStart(" + HtmlUtils.squote(entry.getName())+","+HtmlUtils.squote(id)+"," + (to.getTime()/1000)+"," + HtmlUtils.squote(orient) +"," +howMany +");});\n"));
        sb.append("</center></td></tr></table>");
        return new Result("Countdown", sb);
    }




}
