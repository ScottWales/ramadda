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

package org.ramadda.geodata.idv;


import org.ramadda.geodata.cdmdata.CdmDataOutputHandler;
import org.ramadda.geodata.cdmdata.CdmManager;
import org.ramadda.repository.Entry;
import org.ramadda.repository.Link;
import org.ramadda.repository.Repository;
import org.ramadda.repository.RepositoryUtil;
import org.ramadda.repository.Request;
import org.ramadda.repository.Result;
import org.ramadda.repository.metadata.ContentMetadataHandler;
import org.ramadda.repository.metadata.Metadata;
import org.ramadda.repository.output.OutputHandler;
import org.ramadda.repository.output.OutputType;
import org.ramadda.util.HtmlUtils;

import org.w3c.dom.Element;

import ucar.unidata.util.IOUtil;



import java.io.File;

import java.util.List;


/**
 * OutputHandler for loading IDV through webstart
 */
public class IdvWebstartOutputHandler extends OutputHandler {

    /** the JNLP template */
    private static String jnlpTemplate;


    /** The OutputType definition */
    public static final OutputType OUTPUT_WEBSTART =
        new OutputType("Open in IDV", "idv.webstart", OutputType.TYPE_OTHER,
                       "", "/idv/idv.gif", IdvOutputHandler.GROUP_DATA);



    /**
     * Create an IdvWebstartOutputHandler
     *
     * @param repository  the repository
     * @param element     the Entry to serve
     * @throws Exception  problem creating handler
     */
    public IdvWebstartOutputHandler(Repository repository, Element element)
            throws Exception {
        super(repository, element);
        addType(OUTPUT_WEBSTART);
    }

    /**
     * Get the CdmManager for this
     *
     * @return the CdmManager for this
     *
     * @throws Exception problems getting the CdmManager
     */
    public CdmManager getCdmManager() throws Exception {
        return getDataOutputHandler().getCdmManager();
    }

    /**
     * Get the CdmDataOutputHandler
     *
     * @return  the output handler
     *
     * @throws Exception  problems getting it
     */
    public CdmDataOutputHandler getDataOutputHandler() throws Exception {
        return (CdmDataOutputHandler) getRepository().getOutputHandler(
            CdmDataOutputHandler.OUTPUT_OPENDAP.toString());
    }



    /**
     * Get the entry links
     *
     * @param request  the Request
     * @param state    the Entry
     * @param links    the list of links to add to
     *
     * @throws Exception  problems
     */
    public void getEntryLinks(Request request, State state, List<Link> links)
            throws Exception {

        Entry entry = state.getEntry();
        if (entry == null) {
            return;
        }
        if (entry.getResource().getPath().endsWith(".xidv")
                || entry.getResource().getPath().endsWith(".zidv")) {
            String fileTail = getStorageManager().getFileTail(entry);
            String suffix   = "/" + IOUtil.stripExtension(fileTail) + ".jnlp";
            //                suffix = java.net.URLEncoder.encode(suffix);
            links.add(makeLink(request, state.getEntry(), OUTPUT_WEBSTART,
                               suffix));
        } else {
            if (getCdmManager().canLoadAsCdm(entry)) {
                String suffix = "/" + entry.getId() + ".jnlp";
                links.add(makeLink(request, state.getEntry(),
                                   OUTPUT_WEBSTART, suffix));
            }

        }
    }


    /**
     * Output a group
     *
     * @param request     the Request
     * @param outputType  the OutputType
     * @param group       the group to output
     * @param subGroups   subgroups
     * @param entries     list of Entrys
     *
     * @return  the Result
     *
     * @throws Exception  problems
     */
    public Result outputGroup(Request request, OutputType outputType,
                              Entry group, List<Entry> subGroups,
                              List<Entry> entries)
            throws Exception {
        return outputEntry(request, outputType, group);
    }


    /**
     * Get the JNLP template from the Repository
     *
     * @param repository  the Repository
     *
     * @return  the template
     *
     * @throws Exception problems retreiving JNLP
     */
    public static String getJnlpTemplate(Repository repository)
            throws Exception {
        if (jnlpTemplate == null) {
            String jnlpProperty =
                repository.getProperty("ramadda.idv.jnlp.template",
                                       (String) null);
            String localPath =
                repository.getStorageManager().localizePath(jnlpProperty);

            if (localPath != null) {
                try {
                    jnlpTemplate = IOUtil.readContents(
                        repository.getStorageManager().getInputStream(
                            localPath));
                    repository.getLogManager().logInfo(
                        "IdvWebstartOutputHandler: using jnlp template: "
                        + localPath);
                } catch (Exception ignoreThis) {}
            }
            if (jnlpTemplate == null) {
                jnlpTemplate = repository.getResource(
                    "/org/ramadda/geodata/idv/template.jnlp");
            }
            //Replace the macros
            for (String macro : new String[] { "codebase", "href", "title",
                    "description", "maxheapsize" }) {
                jnlpTemplate = jnlpTemplate.replace("${" + macro + "}",
                        repository.getProperty("ramadda.idv.jnlp." + macro,
                            ""));
            }
        }

        return jnlpTemplate;
    }


    /**
     * Output an Entry
     *
     * @param request     the Request
     * @param outputType  type of Output
     * @param entry       the Entry
     *
     * @return  the Result
     *
     * @throws Exception problems
     */
    public Result outputEntry(Request request, OutputType outputType,
                              Entry entry)
            throws Exception {

        String       jnlp = getJnlpTemplate(getRepository());

        StringBuffer args = new StringBuffer();
        if (entry.getResource().getPath().endsWith(".xidv")
                || entry.getResource().getPath().endsWith(".zidv")) {

            String fileTail = getStorageManager().getFileTail(entry);
            String url      =
                HtmlUtils.url(request.url(getRepository().URL_ENTRY_GET)
                              + "/" + fileTail, ARG_ENTRYID, entry.getId());
            url = request.getAbsoluteUrl(url);
            args.append("<argument>-bundle</argument>");
            args.append("<argument>" + url + "</argument>");
        } else {

            List<Metadata> metadataList =
                getMetadataManager().findMetadata(entry,
                    ContentMetadataHandler.TYPE_ATTACHMENT, true);

            if (getCdmManager().canLoadAsCdm(entry)) {
                String embeddedBundle = null;
                String opendapUrl     =
                    getDataOutputHandler().getAbsoluteOpendapUrl(request,
                        entry);
                if (metadataList != null) {
                    for (Metadata metadata : metadataList) {
                        if (metadata.getAttr1().endsWith(".xidv")) {
                            File xidvFile =
                                new File(IOUtil
                                    .joinDir(getRepository()
                                        .getStorageManager()
                                        .getEntryDir(metadata.getEntryId(),
                                            false), metadata.getAttr1()));
                            embeddedBundle =
                                getStorageManager().readSystemResource(
                                    xidvFile);
                            embeddedBundle =
                                embeddedBundle.replace("${datasource}",
                                    opendapUrl);
                            embeddedBundle = RepositoryUtil.encodeBase64(
                                embeddedBundle.getBytes());

                            break;
                        }
                    }
                }


                if (embeddedBundle != null) {
                    args.append(
                        "<argument>-b64bundle</argument>\n<argument>");
                    args.append(embeddedBundle);
                    args.append("</argument>\n");
                } else {
                    args.append("<argument>-data</argument>\n<argument>");
                    String type = "OPENDAP.GRID";
                    if (entry.getCategory() != null) {
                        if (entry.getCategory().equals("point")) {
                            type = "NetCDF.POINT";
                        }
                    }
                    args.append("type:" + type + ":" + opendapUrl);
                    args.append("</argument>\n");
                }
            }
        }
        jnlp = jnlp.replace("${args}", args.toString());

        return new Result("", new StringBuffer(jnlp),
                          "application/x-java-jnlp-file");
        //        return new Result("",new StringBuffer(jnlp),"text/xml");
    }



}
