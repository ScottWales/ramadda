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
import ucar.unidata.util.Misc;


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


    public static final String PROP_EZID_PROFILE = "ezid.profile";

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

    private boolean enabled =  false;

    private List<String> dataciteResources;

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

        dataciteResources = Misc.toList(new String[]{
                "Collection",
                "Dataset",
                "Event",
                "Film",
                "Image",
                "InteractiveResource",
                "Model",
                "PhysicalObject",
                "Service",
                "Software",
                "Sound",
                "Text",
            });
    }

    private String getMetadataLabel(String profile) {
        if(profile.equals(PROFILE_ERC)) {
            return  "ERC";
        } else if(profile.equals(PROFILE_DATACITE)) {
            return  "Datacite";
        } else {
            return "DC";
        }
    }


    private String[] getMetadataArgs(String profile) {
        if(profile.equals(PROFILE_ERC)) {
            return  METADATA_ERC_ARGS; 
        } else if(profile.equals(PROFILE_DATACITE)) {
            return  METADATA_DATACITE_ARGS; 
        } else {
            return METADATA_DC_ARGS; 
        }
    }

    private String[] getMetadataLabels(String profile) {
        if(profile.equals(PROFILE_ERC)) {
            return METADATA_ERC_LABELS; 
        } else if(profile.equals(PROFILE_DATACITE)) {
            return METADATA_DATACITE_LABELS; 
        } else {
            return METADATA_DC_LABELS; 
        }
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
        
        String profile =  getProperty(PROP_EZID_PROFILE, PROFILE_ERC);
        if(request.defined(PROP_EZID_PROFILE)) { 
            profile =request.getString(PROP_EZID_PROFILE, profile);
        } else if(request.defined(PROFILE_ERC)) { 
            profile =PROFILE_ERC;
        } else if(request.defined(PROFILE_DC)) { 
            profile =PROFILE_DC;
        } else {
            profile =PROFILE_DATACITE;
        }
        if(!request.exists(ARG_SUBMIT)) {
            sb.append(HtmlUtils.formTable());
            sb.append(HtmlUtils.form(getRepository().URL_ENTRY_SHOW.toString()));
            sb.append(HtmlUtils.hidden(ARG_ENTRYID, entry.getId()));
            sb.append(HtmlUtils.hidden(ARG_OUTPUT,OUTPUT_DOI_CREATE.toString()));
            sb.append(HtmlUtils.formEntry(msgLabel("Profile"), getMetadataLabel(profile)));
            addToForm(request, entry, sb, getMetadataArgs(profile), getMetadataLabels(profile));
            StringBuffer buttons = new StringBuffer(HtmlUtils.submit("Create DOI",ARG_SUBMIT));
            if(profile.equals(PROFILE_ERC)) {
                buttons.append(HtmlUtils.space(1));
                buttons.append(HtmlUtils.submit("Use Datacite", PROFILE_DATACITE));
                buttons.append(HtmlUtils.space(1));
                buttons.append(HtmlUtils.submit("Use DC", PROFILE_DC));
            } else if(profile.equals(PROFILE_DC)) {
                buttons.append(HtmlUtils.space(1));
                buttons.append(HtmlUtils.submit("Use Datacite", PROFILE_DATACITE));
                buttons.append(HtmlUtils.space(1));
                buttons.append(HtmlUtils.submit("Use ERC", PROFILE_ERC));
            } else {
                buttons.append(HtmlUtils.space(1));
                buttons.append(HtmlUtils.submit("Use DC", PROFILE_DC));
                buttons.append(HtmlUtils.space(1));
                buttons.append(HtmlUtils.submit("Use ERC", PROFILE_ERC));
            }
            sb.append(HtmlUtils.formEntry("", buttons.toString()));
            sb.append(HtmlUtils.formEntry(HtmlUtils.space(25),""));
            sb.append(HtmlUtils.formClose());
            sb.append(HtmlUtils.formTableClose());
        } else {
            EZIDService ezid = new EZIDService();
            ezid.login(getProperty(PROP_EZID_USERNAME,""), 
                       getProperty(PROP_EZID_PASSWORD,""));
            HashMap<String, String> doiMetadata = new HashMap<String, String>();
            String entryUrl = request.getAbsoluteUrl(request.entryUrl(getRepository().URL_ENTRY_SHOW, entry));
            doiMetadata.put(METADATA_PROFILE, profile);
            doiMetadata.put(METADATA_TARGET, entryUrl);
            addMetadata(request, doiMetadata, getMetadataArgs(profile));
            String doi =  ezid.mintIdentifier(getProperty(PROP_DOI_PREFIX, ""), null);
            Metadata metadata = new Metadata(getRepository().getGUID(),
                                                             entry.getId(), 
                                             DoiMetadataHandler.TYPE_DOI,
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
            if(arg.equals(ARG_DC_TITLE) || arg.equals(ARG_DATACITE_TITLE) || arg.equals(ARG_ERC_WHAT)) {
                value  = entry.getName();
            } else if(arg.equals(ARG_ERC_WHEN) || arg.equals(ARG_DC_DATE)) {
                value = formatDate(request, new Date(entry.getStartDate()));
            } else if(arg.equals(ARG_DATACITE_PUBLICATIONYEAR)) {
                //TODO: does this need to be just a year?
                value = formatDate(request, new Date(entry.getStartDate()));
            } else if(arg.equals(ARG_DATACITE_CREATOR) || arg.equals(ARG_DC_CREATOR) || arg.equals(ARG_ERC_WHO)) {
                value  = entry.getUser().getLabel();
            } else if(arg.equals(ARG_DATACITE_PUBLISHER)|| arg.equals(ARG_DC_PUBLISHER)) {
                value  = request.getUser().getLabel();
            }
            String widget = null;

            if(arg.equals(ARG_DATACITE_RESOURCETYPE)) {
                widget = HtmlUtils.select(arg, dataciteResources);
            }

            if(widget == null) {
                widget = HtmlUtils.input(args[i], value, HtmlUtils.SIZE_30);
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
