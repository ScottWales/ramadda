
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
import org.ramadda.repository.output.OutputHandler;
import org.ramadda.repository.database.*;
import org.ramadda.geodata.cdmdata.CDOOutputHandler;
import org.ramadda.geodata.cdmdata.NCOOutputHandler;
import org.ramadda.geodata.cdmdata.NCLOutputHandler;
import org.ramadda.repository.type.*;
import org.ramadda.data.analysis.AnalysisProvider;
import org.ramadda.data.analysis.Analysis;
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

    public static final String ARG_ANALYSIS_ID = "analysis_id";

    private List<Analysis> analysese = new ArrayList<Analysis>();

    private NCLOutputHandler nclOutputHandler;

    public ClimateCollectionTypeHandler(Repository repository, Element entryNode)
        throws Exception {
        super(repository, entryNode);
        analysese.addAll(new CDOOutputHandler(repository).getAnalysese());
        analysese.addAll(new NCOOutputHandler(repository).getAnalysese());
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
        String searchButton = JQ.button("Search", formId+"_search",js, HtmlUtils.call(formId +".search","event"));
        String analysisButtons = 
            JQ.button("Download Data", formId+"_do_download",js, HtmlUtils.call(formId +".download","event")) + " " +
            JQ.button("Plot", formId+"_do_image",js, HtmlUtils.call(formId +".makeImage","event")) + " " +
            JQ.button("Google Earth", formId+"_do_kmz",js, HtmlUtils.call(formId +".makeKMZ","event"));
        selectorSB.append(HtmlUtils.formEntry("", searchButton));
        selectorSB.append(HtmlUtils.formTableClose());



        sb.append("<table width=100% border=0 cellspacing=0 cellpadding=0><tr valign=top>");
        sb.append("<td width=30%>");
        sb.append(header(msg("Select Data")));
        sb.append(HtmlUtils.div(selectorSB.toString(), HtmlUtils.cssClass("entryselect")));
        sb.append("</td><td>");
        sb.append(HtmlUtils.div("", HtmlUtils.cssClass("entryoutput") +HtmlUtils.id(formId+"_output_list")));
        sb.append("</td></tr>");
        sb.append("<tr valign=top><td>");

        List<String> analysisTabs = new ArrayList<String>();
        List<String> analysisTitles = new ArrayList<String>();

        StringBuffer settingsSB = new StringBuffer();
        settingsSB.append(HtmlUtils.radio(ARG_ANALYSIS_ID, "none",true));
        settingsSB.append(HtmlUtils.space(1));
        settingsSB.append(msg("No Analysis"));
        settingsSB.append(HtmlUtils.br());

        analysisTitles.add(msg("Settings"));
            analysisTabs.add(HtmlUtils.div(settingsSB.toString(),
                                           HtmlUtils.style("min-height:200px;")));
        for(Analysis analysis: analysese) {
            //TODO: add radio buttons
            StringBuffer tmpSB = new StringBuffer();
            tmpSB.append(HtmlUtils.radio(ARG_ANALYSIS_ID, analysis.getAnalysisId(),false));
            tmpSB.append(HtmlUtils.space(1));
            tmpSB.append(msg("Select"));
            tmpSB.append(HtmlUtils.br());
            analysis.addToForm(request, entry, tmpSB);
            analysisTabs.add(HtmlUtils.div(tmpSB.toString(),
                                           HtmlUtils.style("min-height:200px;")));
            analysisTitles.add(analysis.getAnalysisLabel());
        }

        sb.append(header(msg("Analyze Selected Data")));
        HtmlUtils.makeAccordian(sb, analysisTitles, analysisTabs);
        sb.append(analysisButtons);
        sb.append("</td><td>");
        sb.append(HtmlUtils.div("", HtmlUtils.cssClass("entryoutput") +HtmlUtils.id(formId+"_output_image")));
        sb.append("</td></tr></table>");

        sb.append(HtmlUtils.script(js.toString()));
        sb.append(HtmlUtils.formClose());
        //        appendSearchResults(request, entry, sb);
        return new Result(msg(getLabel()), sb);
    }


    public static final String REQUEST_IMAGE =  "image";
    public static final String REQUEST_KMZ =  "kmz";

    public Result processRequest(Request request, Entry entry) throws Exception {
        Result result = super.processRequest(request, entry);
        if(result!=null) return result;
        String what = request.getString(ARG_REQUEST,(String) null);
        if(what == null) return null;
        if(what.equals(REQUEST_IMAGE) || what.equals(REQUEST_KMZ)) {
            return processDataRequest(request, entry, false);
        }
        return null;
    }


    public Result processDataRequest(Request request, Entry entry, boolean doDownload) throws Exception {
        //        File   imageFile =     getStorageManager().getTmpFile(request, "test.png");
        //        BufferedImage image = new BufferedImage(600,400,BufferedImage.TYPE_INT_RGB);
        //        Graphics2D g = (Graphics2D) image.getGraphics();

        //Get the entries
        List<Entry> entries = processSearch(request, entry, true);
        List<File> files = new ArrayList<File>();

        //Process each one in turn
        

        boolean didAnalysis  = false;
        String selectedAnalysis = request.getString(ARG_ANALYSIS_ID, (String) null);
        if(selectedAnalysis!=null) {
            for(Analysis analysis: analysese) {
                if(analysis.getAnalysisId().equals(selectedAnalysis)) {
                    System.err.println("MODEL: applying analysis:" + analysis.getAnalysisLabel());
                    didAnalysis   = true;
                    for(Entry granule: entries) {
                        File outFile =analysis.processRequest(request, granule);
                        files.add(outFile);
                    }
                }
            }
        } 
        if(!didAnalysis) {
            for(Entry granule: entries) {
                if(granule.isFile()) {
                    files.add(granule.getFile());
                }
            }
        }

        if(doDownload) {
            return zipFiles(request, IOUtil.stripExtension(entry.getName())+".zip",files);
        }

        //Make the image
        File imageFile = nclOutputHandler.processRequest(request, files.get(0));

        //And return the results
        String extension = IOUtil.getFileExtension(imageFile.toString());
        return new Result("",
                          getStorageManager().getFileInputStream(imageFile),
                          getRepository().getMimeTypeFromSuffix(extension));
    }


    /**
       Overwrite the base class and route it through processImageRequest
     */
    @Override
    public Result processDownloadRequest(Request request, Entry entry) throws Exception {
        return processDataRequest(request, entry, true);
    }

}
