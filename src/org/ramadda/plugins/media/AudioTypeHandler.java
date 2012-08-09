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

package org.ramadda.plugins.media;


import org.ramadda.repository.*;
import org.ramadda.repository.metadata.*;
import org.ramadda.repository.type.*;


import org.w3c.dom.*;


import ucar.unidata.xml.XmlUtil;

import org.ramadda.util.HtmlUtils;
import ucar.unidata.util.StringUtil;
import ucar.unidata.util.Misc;

import java.util.Date;
import java.util.List;


/**
 *
 *
 */
public class AudioTypeHandler extends GenericTypeHandler {


    /**
     * _more_
     *
     * @param repository _more_
     * @param entryNode _more_
     *
     * @throws Exception _more_
     */
    public AudioTypeHandler(Repository repository, Element entryNode)
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
        StringBuffer sb     = new StringBuffer();
        String html = "<table><tr><td><div class=\"audio-player\"><object>\n<param name=\"autostart\" value=\"false\">\n<param name=\"src\" value=\"${url}\">\n<param name=\"autoplay\" value=\"false\">\n<param name=\"controller\" value=\"true\">\n<embed src=\"${url}\" controller=\"true\" autoplay=\"false\" autostart=\"False\" type=\"audio/wav\" /\n></object></div></td></tr></table>\n";

        String fileUrl  = entry.getTypeHandler().getEntryResourceUrl(request,
                                                                   entry);
        html = html.replace("${url}", fileUrl);
        sb.append(html);
        sb.append(HtmlUtils.p());
        sb.append(entry.getDescription());
        return new Result(msg("Audio"), sb);
    }



}
