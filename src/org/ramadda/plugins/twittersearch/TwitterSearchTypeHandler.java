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

package org.ramadda.plugins.twittersearch;


import org.ramadda.repository.*;
import org.ramadda.repository.type.*;


import org.w3c.dom.*;


import org.ramadda.util.HtmlUtils;
import ucar.unidata.util.StringUtil;

import java.util.Date;
import java.util.List;


/**
 *
 *
 */
public class TwitterSearchTypeHandler extends GenericTypeHandler {



    /**
     * _more_
     *
     * @param repository _more_
     * @param entryNode _more_
     *
     * @throws Exception _more_
     */
    public TwitterSearchTypeHandler(Repository repository, Element entryNode)
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
        StringBuffer sb = new StringBuffer();
        String template =
            getRepository().getResource(
                "/org/ramadda/plugins/twittersearch/template.html");
        String string      = entry.getValue(0, "");
        String width       = entry.getValue(1, "350");
        String height      = entry.getValue(2, "300");
        String orientation = entry.getValue(3, "vertical");

        String html        = template;

        if (orientation.equals("horizontal")) {
            sb.append("<table><tr valign=top>");
        }
        for (String tok : StringUtil.split(string, "\n", true, true)) {
            html = html.replace("${string}", tok);
            html = html.replace("${title}", entry.getName());
            html = html.replace("${caption}", entry.getDescription());
            html = html.replace("${width}", width);
            html = html.replace("${height}", height);
            if (orientation.equals("horizontal")) {
                sb.append("<td>");
            }

            sb.append(HtmlUtils.href("http://twitter.com/#search?q="
                                    + HtmlUtils.urlEncode(tok), tok));
            sb.append(HtmlUtils.br());
            sb.append(html);
            if (orientation.equals("horizontal")) {
                sb.append("</td>");
            } else {
                sb.append(HtmlUtils.p());
            }
        }
        if (orientation.equals("horizontal")) {
            sb.append("</table>");
        }
        return new Result(msg("Twitter Search"), sb);
    }




}
