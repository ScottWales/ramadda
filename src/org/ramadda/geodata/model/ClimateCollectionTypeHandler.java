
package org.ramadda.geodata.model;

import ucar.unidata.sql.Clause;
import ucar.unidata.sql.SqlUtil;

import ucar.unidata.ui.ImageUtils;
import java.sql.*;
import java.awt.*;
import java.io.*;


import java.util.Date;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.awt.image.*;

import org.ramadda.repository.*;
import org.ramadda.repository.database.*;
import org.ramadda.geodata.cdmdata.CDOOutputHandler;
import org.ramadda.geodata.cdmdata.NCLOutputHandler;
import org.ramadda.repository.type.*;
import org.ramadda.util.HtmlUtils;
import org.ramadda.util.JQuery;
import org.ramadda.repository.type.Column;
import org.ramadda.repository.type.GenericTypeHandler;
import org.ramadda.repository.type.ExtensibleGroupTypeHandler;

import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;
import ucar.unidata.util.TwoFacedObject;

import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import ucar.unidata.util.IOUtil;

public class ClimateCollectionTypeHandler extends CollectionTypeHandler  {

    private CDOOutputHandler cdoOutputHandler;
    private NCLOutputHandler nclOutputHandler;

    public ClimateCollectionTypeHandler(Repository repository, Element entryNode)
        throws Exception {
        super(repository, entryNode);
        cdoOutputHandler = new CDOOutputHandler(repository);
        nclOutputHandler = new NCLOutputHandler(repository);
    }


    /**
     * Get the HTML display for this type
     *
     * @param request  the Request
     * @param group    the group
     * @param subGroups    the subgroups
     * @param entries      the Entries
     *
     * @return  the Result
     *
     * @throws Exception  problem getting the HTML
     */
@Override
    public Result getHtmlDisplay(Request request, Entry entry,
                                 List<Entry> subGroups, List<Entry> entries)
        throws Exception {
        //Always call this to initialize things
        getGranuleTypeHandler();

        //Check if the user clicked on tree view, etc.
        if ( !isDefaultHtmlOutput(request)) {
            return null;
        }
        Result result = processRequest(request, entry);
        if(result!=null) return result;


        StringBuffer sb     = new StringBuffer();
        sb.append(entry.getDescription());
        StringBuffer js = new StringBuffer();
        String formId = openForm(request, entry, sb, js);

        StringBuffer selectorSB = new StringBuffer();
        selectorSB.append(HtmlUtils.formTable());
        addSelectorsToForm(request, entry, selectorSB, formId,js);
        String searchButton = JQ.button("Select Data", formId+"_search",js, HtmlUtils.call(formId +".search","event"));
        String analysisButtons = JQ.button("Download Data", formId+"_download",js, HtmlUtils.call(formId +".download","event")) + " " +
            JQ.button("Plot", formId+"_image",js, HtmlUtils.call(formId +".makeImage","event"));
        selectorSB.append(HtmlUtils.formTableClose());
        selectorSB.append(searchButton);


        StringBuffer analysisSB = new StringBuffer();
        StringBuffer productSB = new StringBuffer();

        //        productSB.append(HtmlUtils.submit(msg("Search for files"),ARG_SEARCH));

        sb.append(header(msg("Select Data")));


        sb.append("<table width=100% border=0 cellspacing=0 cellpadding=0><tr valign=top>");
        sb.append(HtmlUtils.col(HtmlUtils.div(selectorSB.toString(), HtmlUtils.cssClass("entryselect")) , " width=30%"));
        sb.append(HtmlUtils.col(HtmlUtils.div("", HtmlUtils.cssClass("entryoutput") +HtmlUtils.id(formId+"_output"))));
        sb.append("</tr></table>");
//        sb.append(HtmlUtils.p());

        cdoOutputHandler.addToForm(request, entry, analysisSB);
//        sb.append(HtmlUtils.p());
        sb.append(header(msg("Analyze Selected Data")));
        sb.append(analysisSB);

        /*
        sb.append(HtmlUtils.p());
        sb.append(header(msg("Do Something with Data")));
        sb.append(productSB);
        */
        sb.append(analysisButtons);

        sb.append(HtmlUtils.script(js.toString()));
        sb.append(HtmlUtils.formClose());
        //        appendSearchResults(request, entry, sb);
        return new Result(msg(getLabel()), sb);
    }


    public static final String REQUEST_IMAGE =  "image";

    public Result processRequest(Request request, Entry entry) throws Exception {
        Result result = super.processRequest(request, entry);
        if(result!=null) return result;
        String what = request.getString(ARG_REQUEST,(String) null);
        if(what == null) return null;
        if(what.equals(REQUEST_IMAGE)) {
            return processImageRequest(request, entry);
        }
        return null;
    }


    public Result processImageRequest(Request request, Entry entry) throws Exception {
        File   imageFile =     getStorageManager().getTmpFile(request, "test.png");
        BufferedImage image = new BufferedImage(600,400,BufferedImage.TYPE_INT_RGB);
        Graphics2D g = (Graphics2D) image.getGraphics();
        g.setColor(Color.red);
        int row = 0;
        for(Entry granule: processSearch(request, entry)) {
            g.drawString(granule.getName(), 10, (row*20)+20);
            row++;
        }
        
        ImageUtils.writeImageToFile(image, imageFile);
        String extension = IOUtil.getFileExtension(imageFile.toString());
        return new Result("",
                          getStorageManager().getFileInputStream(imageFile),
                          getRepository().getMimeTypeFromSuffix(extension));
    }

}
