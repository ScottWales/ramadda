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

package org.ramadda.plugins.map;


import org.ramadda.repository.*;
import org.ramadda.repository.metadata.*;
import org.ramadda.repository.type.*;

import org.ramadda.util.HtmlUtils;


import org.w3c.dom.*;

import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;
import ucar.unidata.util.IOUtil;


import ucar.unidata.xml.XmlUtil;

import java.util.Date;
import java.util.List;


/**
 *
 *
 */
public class GoogleMapsTypeHandler extends GenericTypeHandler {

    /** _more_          */
    public static final int IDX_WIDTH = 0;

    /** _more_          */
    public static final int IDX_HEIGHT = 1;

    /** _more_          */
    public static final int IDX_DISPLAY = 2;

    /** _more_          */
    private int idCnt = 0;

    /**
     * _more_
     *
     * @param repository _more_
     * @param entryNode _more_
     *
     * @throws Exception _more_
     */
    public GoogleMapsTypeHandler(Repository repository, Element entryNode)
            throws Exception {
        super(repository, entryNode);
    }

    //    https://maps.google.com/maps/ms?msid=218276181447368404771.0004b5636fea4bc6d717e&msa=0

    /**
     * _more_
     *
     * @param path _more_
     *
     * @return _more_
     */
    public String getDefaultEntryName(String path) {
        String html = IOUtil.readContents(path, "");
        String title =  StringUtil.findPattern(html,"<title>(.*)</title>");
        if(title == null) {
            title =  StringUtil.findPattern(html,"<TITLE>(.*)</TITLE>");
        }
        System.err.println("title:" + title);
        if(title!=null) {
            title = title.replace("- Google Maps", "");
            return title;
        }
        return "Google Map URL";
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
        String  sdisplay = entry.getValue(IDX_DISPLAY, "true");
        boolean display  = (sdisplay.length() == 0)
                           ? true
                           : Misc.equals("true", sdisplay);
        if ( !display) {
            return null;
        }
        String width  = entry.getValue(IDX_WIDTH, "640");
        String height = entry.getValue(IDX_HEIGHT, "390");
        String      baseUrl = entry.getResource().getPath();
        String       url = baseUrl;
        url = url +"&output=embed";
        url = url.replaceAll("&","&amp;");
        //https://maps.google.com/maps/ms?msid=218276181447368404771.0004b5636fea4bc6d717e&amp;msa=0&amp;ie=UTF8&amp;t=m&amp;ll=39.99143,-105.225842&amp;spn=0.010541,0.008789&amp;output=embed
        String html = "<iframe width=\"${width}\" height=\"${height}\" frameborder=\"1\" scrolling=\"no\" marginheight=\"0\" marginwidth=\"0\" src=\"${url}\"></iframe>";

        html = html.replace("${width}",width);
        html = html.replace("${height}",height);
        html = html.replace("${url}",url);

        StringBuffer sb  = new StringBuffer();
        
        sb.append(entry.getDescription());
        sb.append(HtmlUtils.p());
        sb.append(html);
        sb.append(HtmlUtils.br());
        sb.append(HtmlUtils.href(baseUrl,msg("Link")));
        return new Result(msg("Google Map"), sb);
    }


    /**
     * _more_
     *
     * @param args _more_
     */
    public static void main(String[] args) {
        String pattern = "^http://www.youtube.com/watch\\?v=.*";
        String url     =
            "http://www.youtube.com/watch?v=sOU2WXaDEs0&feature=g-vrec";
        System.err.println(url.matches(pattern));
    }

}
