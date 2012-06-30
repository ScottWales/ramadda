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
import org.ramadda.repository.output.*;
import org.w3c.dom.*;

import ucar.unidata.data.gis.KmlUtil;
import ucar.unidata.gis.*;
import ucar.unidata.gis.shapefile.*;

import ucar.unidata.xml.XmlUtil;
import ucar.unidata.util.DateUtil;
import ucar.unidata.util.IOUtil;
import org.ramadda.util.HtmlUtils;

import ucar.unidata.data.gis.KmlUtil;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import     ucar.unidata.gis.shapefile.EsriShapefile;

/**
 *
 *
 */
public class KmlEntryOutputHandler extends OutputHandler {


    /** Map output type */
    public static final OutputType OUTPUT_KML_HTML =
        new OutputType("Display as HTML", "kml.html",
                       OutputType.TYPE_VIEW, "",
                       ICON_KML);

    public static final OutputType OUTPUT_KMZ_IMAGE =
        new OutputType("Display as HTML", "kml.image",
                       OutputType.TYPE_VIEW, "",
                       ICON_KML);


    /**
     * Create a MapOutputHandler
     *
     *
     * @param repository  the repository
     * @param element     the Element
     * @throws Exception  problem generating handler
     */
    public KmlEntryOutputHandler(Repository repository, Element element)
            throws Exception {
        super(repository, element);
        addType(OUTPUT_KML_HTML);
    }



    /**
     * Get the entry links
     *
     * @param request  the Request
     * @param state    the repository State
     * @param links    the links
     *
     * @throws Exception  problem creating links
     */
    public void getEntryLinks(Request request, State state, List<Link> links)
            throws Exception {
        if(state.entry==null) return;
        if(state.entry.getTypeHandler().isType("geo_kml")) {
            links.add(makeLink(request, state.entry, OUTPUT_KML_HTML));
        }
    }


    /**
     * Output the entry
     *
     * @param request      the Request
     * @param outputType   the type of output
     * @param entry        the Entry to output
     *
     * @return  the Result
     *
     * @throws Exception  problem outputting entry
     */
    public Result outputEntry(Request request, OutputType outputType,
                              Entry entry)
            throws Exception {
        if(outputType.equals(OUTPUT_KML_HTML)) {
            return outputKmlHtml(request, entry);
        }
        return null;
    }



    private Result outputKmlHtml(Request request, Entry entry)
        throws Exception {

        StringBuffer sb = new StringBuffer();
        Element root  = KmlTypeHandler.readKml(getRepository(), entry);
        if(root == null) {
            sb.append(getRepository().showDialogError("Could not read KML/KMZ file"));
            return new Result("KML/KMZ Error", sb);
        }
        walkTree(request, entry, sb, root);

        Result result = new Result("", sb);
        return result;
    }

    private void walkTree(Request request, Entry entry, StringBuffer sb, Element node) {
        String tagName = node.getTagName();
        if(tagName.equals(KmlUtil.TAG_KML)) {
            walkChildren(request, entry, sb, node);
            return;
        }

        if(tagName.equals(KmlUtil.TAG_FOLDER) || tagName.equals(KmlUtil.TAG_DOCUMENT) ||
           tagName.equals(KmlUtil.TAG_TOUR)) {
            //TODO: encode the text
            sb.append("<li> ");
            appendName(node, sb,tagName);
            sb.append("<ul>");
            walkChildren(request, entry, sb, node);
            sb.append("</ul>");
        } else if(tagName.equals(KmlUtil.TAG_PLACEMARK)) {
            sb.append("<li> ");
            appendName(node, sb, tagName);
        } else if(tagName.equals(KmlUtil.TAG_GROUNDOVERLAY)) {
            sb.append("<li> ");
            appendName(node, sb, tagName);
        } else {
            //            sb.append("<li> ");
            //            sb.append(tagName);
        }
    }

    private void appendName(Node node, StringBuffer sb, String tagName) {
        sb.append(tagName+": ");
        sb.append(XmlUtil.getGrandChildText(node, KmlUtil.TAG_NAME, tagName));
        String desc = XmlUtil.getGrandChildText(node, KmlUtil.TAG_DESCRIPTION, null);
        if(desc!=null) {
            sb.append(HtmlUtils.div(desc, HtmlUtils.cssClass("kml-description")));
        }
        
    }

    private void walkChildren(Request request, Entry entry, StringBuffer sb, Element node) {
        NodeList    children = XmlUtil.getElements(node);
        for (int i = 0; i < children.getLength(); i++) {
            Element child = (Element)  children.item(i);
            walkTree(request, entry, sb, child);
        }
    }





}
