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

package org.ramadda.geodata.idv;


import org.ramadda.geodata.data.DataOutputHandler;

import org.ramadda.repository.*;
import org.ramadda.repository.metadata.*;
import org.ramadda.repository.output.*;


import org.w3c.dom.*;


import ucar.unidata.sql.SqlUtil;
import ucar.unidata.ui.ImageUtils;
import ucar.unidata.util.DateUtil;
import ucar.unidata.util.HtmlUtil;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;

import ucar.unidata.util.StringBufferCollection;
import ucar.unidata.util.StringUtil;
import ucar.unidata.xml.XmlUtil;


import java.io.*;

import java.io.File;
import java.io.InputStream;



import java.net.*;



import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;



import java.util.regex.*;

import java.util.zip.*;


/**
 * Class SqlUtil _more_
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class IdvWebstartOutputHandler extends OutputHandler {

    /** _more_ */
    private static String jnlpTemplate;



    /** _more_ */
    public static final OutputType OUTPUT_WEBSTART =
        new OutputType("Open data in IDV", "idv.webstart", OutputType.TYPE_OTHER,
                       "", "/idv/idv.gif", IdvOutputHandler.GROUP_DATA);



    /**
     * _more_
     *
     * @param repository _more_
     * @param element _more_
     * @throws Exception _more_
     */
    public IdvWebstartOutputHandler(Repository repository, Element element)
            throws Exception {
        super(repository, element);
        addType(OUTPUT_WEBSTART);
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param state _more_
     * @param links _more_
     *
     * @throws Exception _more_
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
            DataOutputHandler data =
                (DataOutputHandler) getRepository().getOutputHandler(
                    DataOutputHandler.OUTPUT_OPENDAP);
            if (data != null) {
                if (data.canLoadAsCdm(entry)) {
                    String suffix = "/" + entry.getId() + ".jnlp";
                    links.add(makeLink(request, state.getEntry(),
                                       OUTPUT_WEBSTART, suffix));
                }
            }

        }
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param outputType _more_
     * @param group _more_
     * @param subGroups _more_
     * @param entries _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result outputGroup(Request request, OutputType outputType,
                              Entry group, List<Entry> subGroups,
                              List<Entry> entries)
            throws Exception {
        return outputEntry(request, outputType, group);
    }


    /**
     * _more_
     *
     * @param repository _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
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

        String       jnlp = getJnlpTemplate(getRepository());

        StringBuffer args = new StringBuffer();
        if (entry.getResource().getPath().endsWith(".xidv")
                || entry.getResource().getPath().endsWith(".zidv")) {

            String fileTail = getStorageManager().getFileTail(entry);
            String url =
                HtmlUtil.url(request.url(getRepository().URL_ENTRY_GET) + "/"
                             + fileTail, ARG_ENTRYID, entry.getId());
            url = request.getAbsoluteUrl(url);
            args.append("<argument>-bundle</argument>");
            args.append("<argument>" + url + "</argument>");
        } else {

            List<Metadata> metadataList =
                getMetadataManager().findMetadata(entry,
                    ContentMetadataHandler.TYPE_ATTACHMENT, true);

            DataOutputHandler dataOutputHandler =
                (DataOutputHandler) getRepository().getOutputHandler(
                    DataOutputHandler.OUTPUT_OPENDAP);
            if ((dataOutputHandler != null) && dataOutputHandler.canLoadAsCdm(entry)) {
                String embeddedBundle = null;
                String opendapUrl     = dataOutputHandler.getAbsoluteOpendapUrl(request, entry);
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
                    if (entry.getDataType() != null) {
                        if (entry.getDataType().equals("point")) {
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
