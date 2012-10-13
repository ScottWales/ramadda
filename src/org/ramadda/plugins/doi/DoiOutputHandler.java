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

package org.ramadda.plugins.doi;


import org.ramadda.repository.*;
import org.ramadda.repository.metadata.*;
import org.ramadda.repository.auth.AccessException;
import org.ramadda.repository.output.*;
import org.ramadda.util.HtmlUtils;

import edu.ucsb.nceas.ezid.*;


import org.w3c.dom.*;


import ucar.unidata.xml.XmlUtil;


import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Date;
import java.util.List;


/**
 *
 *
 */
public class DoiOutputHandler extends OutputHandler {


    public static final String PROP_EZID_USERNAME = "ezid.username";
    public static final String PROP_EZID_PASSWORD = "ezid.password";
    public static final String PROP_DOI_PREFIX = "doi.prefix";
    //"doi:10.5072/FK2


    public static final String METADATA_TARGET = "_target";
    public static final String METADATA_PROFILE = "_profile";

    public static final String PROFILE_ERC = "erc";
    public static final String PROFILE_DATACITE = "datacite";
    public static final String PROFILE_DC = "dc";


    public static final String ARG_SUBMIT = "submit";


    public static final String ARG_DATACITE_CREATOR = "datacite.creator";
    public static final String ARG_DATACITE_TITLE = "datacite.title";
    public static final String ARG_DATACITE_PUBLISHER = "datacite.publisher";
    public static final String ARG_DATACITE_PUBLICATIONYEAR = "datacite.publicationyear";
    public static final String ARG_DATACITE_RESOURCETYPE = "datacite.resourcetype";

    public static final String[] METADATA_DATACITE_ARGS = {
        ARG_DATACITE_CREATOR,
        ARG_DATACITE_TITLE,
        ARG_DATACITE_PUBLISHER,
        ARG_DATACITE_PUBLICATIONYEAR,
        ARG_DATACITE_RESOURCETYPE,
    };

    public static final String[] METADATA_DATACITE_LABELS = {
        "Creator",
        "Title",
        "Publisher",
        "Publication Year",
        "Resource Type",
    };


    public static final String ARG_ERC_WHO = "erc.who";
    public static final String ARG_ERC_WHAT = "erc.what";
    public static final String ARG_ERC_WHEN = "erc.when";


    public static final String[] METADATA_ERC_ARGS = {
        ARG_ERC_WHO,
        ARG_ERC_WHAT,
        ARG_ERC_WHEN,
    };

    public static final String[] METADATA_ERC_LABELS = {
        "Who",
        "What",
        "When",
    };


    public static final String ARG_DC_CREATOR = "dc.creator";
    public static final String ARG_DC_TITLE = "dc.title";
    public static final String ARG_DC_PUBLISHER = "dc.publisher";
    public static final String ARG_DC_DATE = "dc.date";


    public static final String[] METADATA_DC_ARGS = {
        ARG_DC_CREATOR,
        ARG_DC_TITLE,
        ARG_DC_PUBLISHER,
        ARG_DC_DATE,
    };

    public static final String[] METADATA_DC_LABELS = {
        "Creator",
        "Title",
        "Publisher",
        "Date",
    };

    public static final String[] METADATA_ARGS = METADATA_DATACITE_ARGS; 
    public static final String[] METADATA_LABELS = METADATA_DATACITE_LABELS; 


    private boolean enabled =  false;


    /** Map output type */
    public static final OutputType OUTPUT_DOI_CREATE =
        new OutputType("Create DOI", "doi.create",
                       OutputType.TYPE_EDIT, "",
                       ICON_MAP);



    /**
     * Create a MapOutputHandler
     *
     *
     * @param repository  the repository
     * @param element     the Element
     * @throws Exception  problem generating handler
     */
    public DoiOutputHandler(Repository repository, Element element)
            throws Exception {
        super(repository, element);
        enabled = getProperty(PROP_EZID_USERNAME,(String)null) !=null &&
            getProperty(PROP_EZID_PASSWORD,(String)null) !=null &&
            getProperty(PROP_DOI_PREFIX,(String)null) !=null;
        addType(OUTPUT_DOI_CREATE);
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
        if(!enabled) return;
        if(getAccessManager().canEditEntry(request, state.getEntry())) {
            links.add(makeLink(request, state.getEntry(), OUTPUT_DOI_CREATE));
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
        if(!getAccessManager().canEditEntry(request, entry)) {
            throw new AccessException("Cannot edit:" + entry.getLabel(),
                                      request);
        }

        StringBuffer sb = new StringBuffer();
        

        if(!request.exists(ARG_SUBMIT)) {
            sb.append(HtmlUtils.formTable());
            sb.append(HtmlUtils.form(getRepository().URL_ENTRY_SHOW.toString()));
            sb.append(HtmlUtils.hidden(ARG_ENTRYID, entry.getId()));
            sb.append(HtmlUtils.hidden(ARG_OUTPUT,OUTPUT_DOI_CREATE.toString()));
            addToForm(request, entry, sb, METADATA_ARGS, METADATA_LABELS);
            sb.append(HtmlUtils.formEntry("", HtmlUtils.submit("Create DOI",ARG_SUBMIT)));
            sb.append(HtmlUtils.formClose());
            sb.append(HtmlUtils.formTableClose());
        } else {
            EZIDService ezid = new EZIDService();
            ezid.login(getProperty(PROP_EZID_USERNAME,""), 
                       getProperty(PROP_EZID_PASSWORD,""));
            HashMap<String, String> doiMetadata = new HashMap<String, String>();
            String entryUrl = request.getAbsoluteUrl(request.entryUrl(getRepository().URL_ENTRY_SHOW, entry));
            doiMetadata.put(METADATA_PROFILE, PROFILE_ERC);
            doiMetadata.put("_target", entryUrl);
            addMetadata(request, doiMetadata, METADATA_ARGS);
            String doi =  ezid.mintIdentifier(getProperty(PROP_DOI_PREFIX, ""), null);
            //            http://dx.doi.org/10.5072/FK2Z322C8
            //            doi:10.5072/FK2TB19N4
            Metadata metadata = new Metadata(getRepository().getGUID(),
                                                             entry.getId(), "doi",
                                             false, doi, "", "", "", "");
            getMetadataManager().insertMetadata(metadata);
            entry.addMetadata(metadata);
            sb.append(HtmlUtils.p());
            sb.append("DOI has been created");
            sb.append(HtmlUtils.p());
            sb.append(DoiMetadataHandler.getHref(doi));
        }

        return new Result("",sb);
    }


    private void addToForm(Request request, Entry entry, StringBuffer sb, String[]args, String[]labels) {
        for(int i=0;i<args.length;i++) {
            String arg = args[i];
            String value = "";
            if(arg.indexOf("title")>=0 || arg.indexOf("what")>=0) {
                value  = entry.getName();
            } else if(arg.indexOf("when")>=0 || arg.indexOf("date")>=0) {
                value = new Date(entry.getStartDate()).toString();
            } else if(arg.indexOf("year")>=0) {
                //TODO: does this need to be just a year
                value = new Date(entry.getStartDate()).toString();
            } else if(arg.indexOf("creator")>=0 || arg.indexOf("who")>=0) {
                value  = entry.getUser().getName();
            }
            String widget = null;
            if(widget == null) {
                widget = HtmlUtils.input(args[i], value);
            }
            sb.append(HtmlUtils.formEntry(msgLabel(labels[i]),
                                          widget));
                                              
        }
    }


    private void addMetadata(Request request, HashMap<String,String>metadata, String[]args) {
        for(String arg: args) {
            if(request.defined(arg)) {
                metadata.put(arg, request.getString(arg,""));
            }
        }
    }


    /**
     * Output a group
     *
     * @param request      The Request
     * @param outputType   the type of output
     * @param group        the group Entry
     * @param subGroups    the subgroups
     * @param entries      The list of Entrys
     *
     * @return  the resule
     *
     * @throws Exception    problem on output
     */
    public Result outputGroup(Request request, OutputType outputType,
                              Entry group, List<Entry> subGroups,
                              List<Entry> entries)
            throws Exception {
        return outputEntry(request, outputType, group);
    }


}
