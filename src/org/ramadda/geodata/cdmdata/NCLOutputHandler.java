package org.ramadda.geodata.cdmdata;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import org.ramadda.repository.Entry;
import org.ramadda.repository.Link;
import org.ramadda.repository.Repository;
import org.ramadda.repository.Request;
import org.ramadda.repository.Result;
import org.ramadda.repository.StorageManager;
import org.ramadda.repository.output.OutputHandler;
import org.ramadda.repository.output.OutputType;
import org.ramadda.util.HtmlUtils;
import org.ramadda.util.TempDir;
import org.w3c.dom.Element;

import ucar.nc2.dt.grid.GridDataset;
import ucar.unidata.util.IOUtil;

public class NCLOutputHandler extends OutputHandler {

    /** NCL program path */
    private static final String PROP_NCARG_ROOT = "ncl.ncarg_root";
    
    /** NCL map plot script */
    private static final String SCRIPT_MAPPLOT = "plot.map.ncl";

    /** map plot output id */
    public static final OutputType OUTPUT_NCL_MAPPLOT =
        new OutputType("NCL Map Displays", "ncl.mapplot", OutputType.TYPE_OTHER,
                       OutputType.SUFFIX_NONE, "/cdmdata/ncl.gif", CdmDataOutputHandler.GROUP_DATA);

    /** the product directory */
    private TempDir productDir;

    /** the path to NCL program */
    private String ncargRoot;

    public NCLOutputHandler(Repository repository)
            throws Exception {
        super(repository, "NCL");
        ncargRoot = getProperty(PROP_NCARG_ROOT, null);
    }
        
    public NCLOutputHandler(Repository repository, Element element)
                throws Exception {
        super(repository, element);
        addType(OUTPUT_NCL_MAPPLOT);
        ncargRoot = getProperty(PROP_NCARG_ROOT, null);
        if (ncargRoot != null) {
            // write out the template
            String nclScript = getStorageManager().readSystemResource("/org/ramadda/geodata/cdmdata/resources/ncl/"+SCRIPT_MAPPLOT);
            nclScript = nclScript.replaceAll("\\$NCARG_ROOT", ncargRoot);
            String outdir = IOUtil.joinDir(getStorageManager().getResourceDir() , "ncl");
            StorageManager.makeDir(outdir);
            File outputFile = new File(IOUtil.joinDir(outdir,SCRIPT_MAPPLOT));
            InputStream is = new ByteArrayInputStream(nclScript.getBytes());
            OutputStream os = getStorageManager().getFileOutputStream(outputFile);
            IOUtil.writeTo(is, os);
        }
        
    }

    /**
     * Check to see if we have NCL installed
     *
     * @return  true if path to NCL is set
     */
    private boolean haveNcl() {
        return ncargRoot != null;
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
        if ( !haveNcl()) {
            return;
        }
        if ((state.entry != null) && state.entry.isFile()
                && state.entry.getResource().getPath().endsWith(".nc")) {
            links.add(makeLink(request, state.entry, OUTPUT_NCL_MAPPLOT));
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
            TempDir tempDir = getStorageManager().makeTempDir("nclproducts");
            //keep things around for 1 hour
            tempDir.setMaxAge(1000 * 60 * 60 * 1);
            productDir = tempDir;
        }

        return productDir.getDir();
    }

    /**
     * Get the data output handler
     *
     * @return the handler
     *
     * @throws Exception Problem getting that
     */
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
            return outputNCL(request, entry);
        }
        StringBuffer sb = new StringBuffer();
        addForm(request, entry, sb);

        return new Result("NCL Form", sb);
    }

    /**
     * Add the form
     *
     * @param request  the Request
     * @param entry    the Entry
     * @param sb       the HTML
     *
     * @throws Exception problems
     */
    private void addForm(Request request, Entry entry, StringBuffer sb)
            throws Exception {

        String formUrl = request.url(getRepository().URL_ENTRY_SHOW);
        sb.append(HtmlUtils.form(formUrl,
                                 makeFormSubmitDialog(sb,
                                     msg("Plotting Data...."))));

        sb.append(HtmlUtils.hidden(ARG_OUTPUT, OUTPUT_NCL_MAPPLOT));
        sb.append(HtmlUtils.hidden(ARG_ENTRYID, entry.getId()));
        String buttons = HtmlUtils.submit("Plot Data", ARG_SUBMIT);
        //sb.append(buttons);
        sb.append(HtmlUtils.h2("Plot Dataset"));
        sb.append(HtmlUtils.hr());
        addToForm(request, entry, sb);
        sb.append(buttons);
        sb.append(" ");
        addPublishWidget(
            request, entry, sb,
            msg("Select a folder to publish the generated image file to"));

    }

    public void addToForm(Request request, Entry entry, StringBuffer sb)
        throws Exception {
        sb.append(HtmlUtils.formTable());
        CdmDataOutputHandler dataOutputHandler = getDataOutputHandler();
        GridDataset dataset =
            dataOutputHandler.getCdmManager().getGridDataset(entry,
                entry.getResource().getPath());
        sb.append(dataOutputHandler.getVariableForm(dataset, true));
        sb.append(HtmlUtils.formTableClose());
    }

    /**
     * Output the NCL request
     *
     * @param request  the request
     * @param entry    the entry
     *
     * @return  the output
     *
     * @throws Exception  problem executing the command
     */
    public Result outputNCL(Request request, Entry entry) throws Exception {
        
        File input = entry.getTypeHandler().getFileForEntry(entry);
        String wksName = IOUtil.joinDir(getProductDir(), getRepository().getGUID());
        File outFile = new File(wksName+".png");
        
        StringBuffer commandString = new StringBuffer();
        List<String> commands            = new ArrayList<String>();
        commands.add(IOUtil.joinDir(ncargRoot, "bin/ncl"));
        commandString.append(IOUtil.joinDir(ncargRoot, "bin/ncl"));
        commandString.append(" ");
        // add arguments here
        //commands.add("'wks_name=\""+wksName+"\"'");
        commandString.append("wks_name=\""+wksName+"\"");
        commandString.append(" ");
        //commands.add("'ncfile=\""+input.toString()+"\"'");
        commandString.append("ncfile=\""+input.toString()+"\"");
        commandString.append(" ");
        List<String> varNames = new ArrayList<String>();
        Hashtable args     = request.getArgs();
        for (Enumeration keys = args.keys(); keys.hasMoreElements(); ) {
            String arg = (String) keys.nextElement();
            if (arg.startsWith(CdmDataOutputHandler.VAR_PREFIX) && request.get(arg, false)) {
                varNames.add(arg.substring(CdmDataOutputHandler.VAR_PREFIX.length()));
            }
        }
        String varname = varNames.get(0);
        //commands.add("'"+ARG_VARIABLE+"=\""+varname+"\"'");
        commandString.append(ARG_VARIABLE+"=\""+request.getString(ARG_VARIABLE, null)+"\"");
        commandString.append(" ");
        String level = request.getString(CdmDataOutputHandler.ARG_LEVEL, null);
        if (level != null && !level.isEmpty()) {
            //commands.add("'"+CdmDataOutputHandler.ARG_LEVEL+"=\""+level+"\"'");
            commandString.append(CdmDataOutputHandler.ARG_LEVEL+"=\""+level+"\"");
            commandString.append(" ");
        }
        commands.add(IOUtil.joinDir(IOUtil.joinDir(getStorageManager().getResourceDir(), "ncl"),SCRIPT_MAPPLOT));
        commandString.append(IOUtil.joinDir(IOUtil.joinDir(getStorageManager().getResourceDir(), "ncl"),SCRIPT_MAPPLOT));
        //commands.add(commandString.toString());

        System.err.println("cmds:" + commands);

        commands.add(entry.getResource().getPath());
        commands.add(outFile.toString());
        //Use new repository method to execute. This gets back [stdout,stderr]
        String[] results = getRepository().executeCommand(commands, getProductDir());
        String errorMsg =results[1];
        String outMsg =results[0];
        if (outMsg.length() > 0) {
            return getErrorResult(request, "NCL-Error","An error occurred:<br>" + outMsg);
        }
        if (errorMsg.length() > 0) {
            return getErrorResult(request, "NCL-Error", "An error occurred:<br>" + errorMsg);
        }
        if ( !outFile.exists()) {
            return getErrorResult(request, "NCL-Error", "Humm, the NCL image generation failed for some reason");
        }
        if (doingPublish(request)) {
            if ( !request.defined(ARG_PUBLISH_NAME)) {
                request.put(ARG_PUBLISH_NAME, outFile);
            }
            return getEntryManager().processEntryPublish(request, outFile,
                    null, entry, "generated from");
        }

        return request.returnFile(
            outFile, getStorageManager().getFileTail(outFile.toString()));
    }





}
