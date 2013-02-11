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
package org.ramadda.geodata.cdmdata;


import org.ramadda.repository.Entry;
import org.ramadda.repository.Link;
import org.ramadda.repository.Repository;
import org.ramadda.repository.Request;
import org.ramadda.repository.Result;
import org.ramadda.repository.output.OutputHandler;
import org.ramadda.repository.output.OutputHandler.State;
import org.ramadda.repository.output.OutputType;
import org.ramadda.util.HtmlUtils;
import org.ramadda.util.TempDir;

import org.w3c.dom.Element;

import ucar.nc2.Variable;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dataset.NetcdfDataset;

import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.TwoFacedObject;

import java.io.File;

import java.util.ArrayList;
import java.util.List;


/**
 * Class description
 *
 *
 * @version        $version$, Mon, Feb 11, '13
 * @author         Enter your name here...    
 */
public class CDOOutputHandler extends OutputHandler {

    /** CDO program path */
    private static final String PROP_CDO_PATH = "cdo.path";

    /** operation identifier */
    private static final String ARG_CDO_OPERATION = "cdo.operation";

    /** CDO Output Type */
    public static final OutputType OUTPUT_CDO =
        new OutputType("CDO Analysis", "cdo", OutputType.TYPE_OTHER,
                       OutputType.SUFFIX_NONE, "/cdmdata/cdo.png",
                       CdmDataOutputHandler.GROUP_DATA);

    /** info operator */
    private static final String OP_INFO = "info";

    /** short info operator */
    private static final String OP_SINFO = "sinfo";

    /** number of years operator */
    private static final String OP_NYEAR = "nyear";

    /** info types */
    @SuppressWarnings("unchecked")
        List<TwoFacedObject> INFO_TYPES = Misc.toList(new Object[] {
                new TwoFacedObject("Info", OP_INFO),
                new TwoFacedObject(
                                   "Short Info",
                                   OP_SINFO), new TwoFacedObject(
                                                                 "Number of Years",
                                                                 OP_NYEAR), });


    /** the product directory */
    private TempDir productDir;

    /** the path to cdo program */
    private String cdoPath;

    /**
     * Constructor
     *
     * @param repository   the Repository
     * @param element      the Element
     * @throws Exception   problem creating handler
     */
    public CDOOutputHandler(Repository repository, Element element)
        throws Exception {
        super(repository, element);
        addType(OUTPUT_CDO);
        cdoPath = getProperty(PROP_CDO_PATH, null);
    }


    /**
     * Check to see if we have cdo installed
     *
     * @return  true if path to cdo is set
     */
    private boolean haveCdo() {
        return cdoPath != null;
    }


    /**
     * This method gets called to determine if the given entry or entries can be displays as las xml
     *
     * @param request  the Request
     * @param state    the State
     * @param links    the list of Links to add to
     *
     * @throws Exception Problem adding links
     */
    public void getEntryLinks(Request request, State state, List<Link> links)
        throws Exception {
        if ( !haveCdo()) {
            return;
        }
        if ((state.entry != null) && state.entry.isFile()
            && state.entry.getResource().getPath().endsWith(".nc")) {
            links.add(makeLink(request, state.entry, OUTPUT_CDO));
        }
    }

    /**
     * Get the product directory
     *
     * @return  the directory
     *

     * @throws Exception  problem getting directory
     */
    private File getProductDir() throws Exception {
        if (productDir == null) {
            TempDir tempDir = getStorageManager().makeTempDir("ncoproducts");
            //keep things around for 1 hour
            tempDir.setMaxAge(1000 * 60 * 60 * 1);
            productDir = tempDir;
        }

        return productDir.getDir();
    }


    public CdmDataOutputHandler getDataOutputHandler() throws Exception {
        return (CdmDataOutputHandler) getRepository().getOutputHandler(
                                                                       CdmDataOutputHandler.OUTPUT_OPENDAP.toString());
    }




    /**
     * Create the entry display
     *
     * @param request   the Request
     * @param outputType  the output type
     * @param entry     the entry to output
     *
     * @return the entry or form
     *
     * @throws Exception problem making the form
     */
    public Result outputEntry(Request request, OutputType outputType,
                              Entry entry)
        throws Exception {


        if (request.defined(ARG_SUBMIT)) {
            return outputCDO(request, entry);
        }
        StringBuffer sb      = new StringBuffer();
        addForm(request, entry, sb);
        return new Result("CDO Form", sb);
    }

    private void   addForm(Request request, Entry entry, StringBuffer sb) throws Exception {
        CdmDataOutputHandler dataOutputHandler = getDataOutputHandler();
        NetcdfDataset dataset =
            NetcdfDataset.openDataset(entry.getResource().getPath());
        dataset.close();


        String       formUrl = request.url(getRepository().URL_ENTRY_SHOW);
        sb.append(HtmlUtils.form(formUrl,
                                 makeFormSubmitDialog(sb,
                                                      msg("Apply CDO..."))));

        String buttons = HtmlUtils.submit("Extract Data", ARG_SUBMIT);
        sb.append(buttons);
        sb.append(" ");
        sb.append(
                  HtmlUtils.href(
                                 "https://code.zmaw.de/projects/cdo/wiki/Cdo#Documentation",
                                 "CDO Documentation", " target=_external "));

        sb.append(HtmlUtils.formTable());
        sb.append(HtmlUtils.hidden(ARG_OUTPUT, OUTPUT_CDO));
        sb.append(HtmlUtils.hidden(ARG_ENTRYID, entry.getId()));

        addInfoWidget(request, sb);
        addPublishWidget(
                         request, entry, sb,
                         msg("Select a folder to publish the generated NetCDF file to"));
        sb.append(HtmlUtils.formTableClose());
        sb.append(buttons);

    }


    /**
     * Add the info widget
     *
     * @param request  the Request
     * @param sb       the StringBuffer to add to
     */
    private void addInfoWidget(Request request, StringBuffer sb) {
        sb.append(HtmlUtils.formEntry(msgLabel("Information"),
                                      HtmlUtils.select(ARG_CDO_OPERATION,
                                                       INFO_TYPES)));
    }


    /**
     * Output the cdo request
     *
     * @param request  the request
     * @param entry    the entry
     *
     * @return  the output
     *
     * @throws Exception  problem executing the command
     */
    public Result outputCDO(Request request, Entry entry) throws Exception {

        String tail    = getStorageManager().getFileTail(entry);
        String newName = IOUtil.stripExtension(tail) + "_product.nc";
        tail = getStorageManager().getStorageFileName(tail);
        File         outFile = new File(IOUtil.joinDir(getProductDir(),
                                                       tail));
        List<String> commands = new ArrayList<String>();
        commands.add(cdoPath);
        String operation  = request.getString(ARG_CDO_OPERATION, OP_INFO);


        commands.add(operation);

        System.err.println("cmds:" + commands);

        commands.add(entry.getResource().getPath());
        //commands.add(outFile.toString());
        ProcessBuilder pb = new ProcessBuilder(commands);


        pb.directory(getProductDir());
        Process process = pb.start();
        String errorMsg =
            new String(IOUtil.readBytes(process.getErrorStream()));
        String outMsg =
            new String(IOUtil.readBytes(process.getInputStream()));
        int result = process.waitFor();
        if (errorMsg.length() > 0) {
            return new Result(
                              "CDO-Error",
                              new StringBuffer(
                                               getRepository().showDialogError(
                                                                               "An error occurred:<br>" + errorMsg)));
        }

        //The jeff is here for when I have a fake cdo.sh
        boolean jeff = true;

        if(!jeff) {
            if (outMsg.length() > 0) {
                return new Result(
                                  "CDO-Error",
                                  new StringBuffer(
                                                   getRepository().showDialogError(
                                                                                   "An error occurred:<br>" + outMsg)));
            }
        
            if ( !outFile.exists()) {
                return new Result(
                                  "CDO-Error",
                                  new StringBuffer(
                                                   getRepository().showDialogError(
                                                                                   "Humm, the CDO generation failed for some reason")));
            }
        }

        if (doingPublish(request)) {
            if ( !request.defined(ARG_PUBLISH_NAME)) {
                request.put(ARG_PUBLISH_NAME, newName);
            }
            return getEntryManager().processEntryPublish(request, outFile,
                                                         null, entry, "generated from");
        }

        //Assuming this is some text
        if(operation.equals(OP_INFO)) {
            String info;

            if(!jeff) {
                info = IOUtil.readInputStream(
                                              getStorageManager().getFileInputStream(outFile));
            } else {
                info = outMsg;
            }

            StringBuffer sb = new StringBuffer();
            addForm(request, entry, sb);
            sb.append(header(msg("CDO Information")));
            sb.append(HtmlUtils.pre(info));
            return new Result("CDO", sb);
        }



        return request.returnFile(
                                  outFile, getStorageManager().getFileTail(outFile.toString()));
    }

}
