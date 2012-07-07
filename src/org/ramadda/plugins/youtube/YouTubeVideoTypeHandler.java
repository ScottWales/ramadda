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

package org.ramadda.plugins.youtube;


import org.ramadda.repository.*;
import org.ramadda.repository.type.*;


import org.w3c.dom.*;


import ucar.unidata.xml.XmlUtil;
import org.ramadda.util.HtmlUtils;
import ucar.unidata.util.StringUtil;

import java.util.Date;
import java.util.List;


/**
 *
 *
 */
public class YouTubeVideoTypeHandler extends GenericTypeHandler {



    /**
     * _more_
     *
     * @param repository _more_
     * @param entryNode _more_
     *
     * @throws Exception _more_
     */
    public YouTubeVideoTypeHandler(Repository repository, Element entryNode)
            throws Exception {
        super(repository, entryNode);
    }

    public String getDefaultEntryName(String path) {
        //TODO: fetch the web page and get the title
        return "YouTube Video";
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
        String width = entry.getValue(0,"640");
        String height = entry.getValue(1,"390");
        double start = entry.getValue(2, 0.0);
        double end = entry.getValue(3, -1);
        sb.append("<iframe id=\"ytplayer\" type=\"text/html\" frameborder=\"0\" ");
        sb.append(XmlUtil.attr("width",width));
        sb.append(XmlUtil.attr("height",height));
        String url = entry.getResource().getPath();
        String id = StringUtil.findPattern(url,"v=([^&]+)&");
        if(id == null) {
            id = StringUtil.findPattern(url,"v=([^&]+)");
        }
        if(id == null) {
            sb.append(getRepository().showDialogError("Could not find ID in YouTube URL"));
            return new Result(msg("YouTube Video"), sb);
        }

        String embedUrl = "http://www.youtube.com/embed/" + id;
        embedUrl += "?enablejsapi=1";
        embedUrl += "&autoplay=0";
        if(start>0) {
            embedUrl += "&start=" + ((int)(start*60));
        }
        if(end>0) {
            embedUrl += "&end=" + ((int)(end*60));
        }
        sb.append(XmlUtil.attr("src",embedUrl));
        sb.append("/>\n");


        return new Result(msg("YouTube Video"), sb);

    }


    public static void main(String[]args) {
        String pattern="^http://www.youtube.com/watch\\?v=.*";
        String url = "http://www.youtube.com/watch?v=sOU2WXaDEs0&feature=g-vrec";
        System.err.println(url.matches(pattern));
    }

}
