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

package org.ramadda.geodata.fits;


import nom.tam.fits.*;
import nom.tam.util.*;


import org.ramadda.repository.*;
import org.ramadda.repository.auth.*;
import org.ramadda.repository.output.*;

import org.w3c.dom.*;


import ucar.unidata.sql.SqlUtil;
import ucar.unidata.ui.ImageUtils;
import ucar.unidata.util.DateUtil;
import ucar.unidata.util.HtmlUtil;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;


import ucar.unidata.util.StringUtil;
import ucar.unidata.util.WmsUtil;
import ucar.unidata.xml.XmlUtil;

import java.io.*;

import java.lang.reflect.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;



/**
 *
 *
 */
public class FitsOutputHandler extends OutputHandler {

    /** _more_ */
    public static final String ARG_FITS_HDU = "fits.hdu";

    /** _more_ */
    public static final String ARG_FITS_SUBSET = "fits.hdu";

    /** _more_ */
    public static final OutputType OUTPUT_VIEWER =
        new OutputType("FITS Viewer", "fits.viewer", OutputType.TYPE_VIEW,
                       "", "/fits/fits.gif");


    /** _more_ */
    public static final OutputType OUTPUT_INFO = new OutputType("FITS Info",
                                                     "fits.info",
                                                     OutputType.TYPE_VIEW,
                                                     "", "/fits/fits.gif");


    /**
     * _more_
     *
     *
     * @param repository _more_
     * @param element _more_
     * @throws Exception _more_
     */
    public FitsOutputHandler(Repository repository, Element element)
            throws Exception {
        super(repository, element);
        Header.setLongStringsEnabled(true);
        addType(OUTPUT_INFO);
        addType(OUTPUT_VIEWER);
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param state _more_
     * @param links _more_
     *
     *
     * @throws Exception _more_
     */
    public void getEntryLinks(Request request, State state, List<Link> links)
            throws Exception {
        if (state.entry != null) {
            if (state.entry.getType().equals("fits_data")) {
                links.add(makeLink(request, state.entry, OUTPUT_INFO));
                links.add(makeLink(request, state.entry, OUTPUT_VIEWER));
            }
        }
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param outputType _more_
     * @param entry _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result outputEntry(Request request, OutputType outputType,
                              Entry entry)
            throws Exception {
        if (outputType.equals(OUTPUT_VIEWER)) {
            return outputEntryViewer(request, entry);
        }
        if (request.exists(ARG_FITS_SUBSET)) {
            return outputEntrySubset(request, entry);
        }
        return outputEntryInfo(request, entry);
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
    public Result outputEntrySubset(Request request, Entry entry)
            throws Exception {
        Fits    fits = new Fits(entry.getFile());
        HashSet hdus = new HashSet();
        for (String hdu :
                (List<String>) request.get(ARG_FITS_HDU, new ArrayList())) {
            hdus.add(hdu);
        }
        OutputStream os = request.getHttpServletResponse().getOutputStream();

        String filename =
            IOUtil.stripExtension(getStorageManager().getFileTail(entry));
        filename = IOUtil.stripExtension(filename);
        request.setReturnFilename(filename + "_subset.fits");
        Result result = new Result();
        result.setNeedToWrite(false);
        BufferedDataOutputStream bdos = new BufferedDataOutputStream(os);

        for (int headerIdx = 0; headerIdx < fits.size(); headerIdx++) {
            if ( !hdus.contains("" + headerIdx)) {
                continue;
            }
            BasicHDU hdu = fits.getHDU(headerIdx);
            hdu.write(bdos);
        }
        bdos.close();
        os.close();
        return result;
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
    public Result outputEntryViewer(Request request, Entry entry)
            throws Exception {

        StringBuffer sb = new StringBuffer();
        String fileUrl = getEntryManager().getEntryResourceUrl(request,
                             entry, false);
        //TODO: set the path right
        sb.append(
            "<applet archive=\"/repository/fits/fits1.3.jar\" code=\"eap.fitsbrowser.BrowserApplet\" width=700 height=700 ><param name=\"FILE\" value=\""
            + fileUrl
            + "\">Your browser is ignoring the applet tag</applet>");
        return new Result("", sb);
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
    public Result outputEntryInfo(Request request, Entry entry)
            throws Exception {

        StringBuffer sb = new StringBuffer();
        sb.append(request.form(getRepository().URL_ENTRY_SHOW));
        sb.append(HtmlUtil.submit(msg("Subset"), ARG_FITS_SUBSET));
        sb.append(HtmlUtil.hidden(ARG_ENTRYID, entry.getId()));
        sb.append(HtmlUtil.hidden(ARG_OUTPUT, OUTPUT_INFO.getId()));

        Fits fits = new Fits(entry.getFile());
        for (int hduIdx = 0; hduIdx < fits.size(); hduIdx++) {
            BasicHDU            hdu       = fits.getHDU(hduIdx);
            nom.tam.fits.Header header    = hdu.getHeader();
            StringBuffer        subSB     = new StringBuffer();

            String              hduType   = "N/A";
            TableData           tableData = null;
            if (hdu instanceof AsciiTableHDU) {
                hduType   = "Ascii Table";
                tableData = (TableData) hdu.getData();
            } else if (hdu instanceof ImageHDU) {
                hduType = "Image";
            } else if (hdu instanceof BinaryTableHDU) {
                hduType   = "Binary Table";
                tableData = (TableData) hdu.getData();

            }
            if (tableData != null && tableData.getNRows()<5000) {
                StringBuffer tableSB = new StringBuffer();
                tableSB.append("<div style=\"margin-left:25px;\">");
                tableSB.append("<table cellspacing=2 cellpadding=2>");
                if (header.getStringValue("TTYPE1") != null) {
                    tableSB.append("<tr>");
                    for (int colIdx = 0; colIdx < tableData.getNCols();
                            colIdx++) {
                        String colName = header.getStringValue("TTYPE"
                                             + (colIdx + 1));
                        if (colName == null) {
                            colName = "&nbsp;";
                        }
                        tableSB.append("<td align=center><b>" + colName
                                       + "</td>");
                    }
                    tableSB.append("</tr>");
                }
                for (int rowIdx = 0; rowIdx < tableData.getNRows();
                        rowIdx++) {
                    Object[] row = tableData.getRow(rowIdx);
                    tableSB.append("<tr align=right>");
                    for (Object item : row) {
                        tableSB.append("<td>");
                        tableSB.append(getRowItem(item));
                       tableSB.append("</td>");
                    }
                    tableSB.append("</tr>");
                }
                tableSB.append("</table>");
                tableSB.append("</div>");
                subSB.append(HtmlUtil.makeShowHideBlock("Data",
                        tableSB.toString(), false));
            }


            subSB.append("<div style=\"margin-left:25px;\">");
            subSB.append("<table>");
            int numCards = header.getNumberOfCards();
            for (int cardIdx = 0; cardIdx < numCards; cardIdx++) {
                String card = header.getCard(cardIdx);
                card = card.trim();
                if (card.length() == 0) {
                    continue;
                }
                List<String> toks = StringUtil.splitUpTo(card, "=", 2);
                subSB.append("<tr>");
                //Look for an '=' in the comment
                if ((toks.size() == 1)
                        || (toks.get(0).trim().indexOf(" ") >= 0)) {
                    if (card.startsWith("/")) {
                        card = card.substring(1);
                    }
                    subSB.append("<td colspan=3><i>"
                                 + HtmlUtil.entityEncode(card) + "</i></td>");
                } else {
                    String key     = toks.get(0).trim();
                    String comment = "";
                    String value   = toks.get(1);
                    int    idx;
                    if (value.startsWith("'")) {
                        idx     = value.indexOf("'", 1);
                        comment = value.substring(idx + 1).trim();
                        value   = value.substring(1, idx);
                        if (comment.startsWith("/")) {
                            comment = comment.substring(1);
                        }
                    } else {
                        idx = value.indexOf("/");
                        if (idx >= 0) {
                            comment = value.substring(idx + 1).trim();
                            value   = value.substring(0, idx).trim();
                        }
                    }

                    subSB.append("<td><b>" + HtmlUtil.entityEncode(key)
                                 + "</b></td><td>"
                                 + HtmlUtil.entityEncode(value)
                                 + "</td><td><i>"
                                 + HtmlUtil.entityEncode(comment)
                                 + "</i></td></tr>");
                }
                subSB.append("</tr>");
            }
            subSB.append("</table>");
            subSB.append("</div>");
            String label = HtmlUtil.checkbox(ARG_FITS_HDU, "" + hduIdx, true)
                           + " " + hduType;
            sb.append(HtmlUtil.makeShowHideBlock(label, subSB.toString(),
                    false));
        }
        sb.append(HtmlUtil.formClose());
        return new Result("", sb);


    }

    /**
     * _more_
     *
     * @param item _more_
     *
     * @return _more_
     */
    private String getRowItem(Object item) {
        try {
        int length = Array.getLength(item);
        if (length > 0) {
            return Array.get(item, 0).toString();
        }
        return item.toString();
        } catch(Exception exc) {
            return item.toString();
        }
    }






}
