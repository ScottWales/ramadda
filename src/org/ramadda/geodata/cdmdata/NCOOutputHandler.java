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


import org.ramadda.repository.*;
import org.ramadda.repository.output.*;
import org.ramadda.util.HtmlUtils;

import org.ramadda.util.TempDir;

import org.ramadda.data.process.DataProcess;
import org.ramadda.data.process.DataProcessProvider;


import org.w3c.dom.*;

import ucar.nc2.Variable;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dataset.NetcdfDataset;

import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.TwoFacedObject;

import ucar.unidata.xml.XmlUtil;


import java.io.*;

import java.util.ArrayList;
import java.util.List;



/**
 *
 * @author Jeff McWhirter/ramadda.org
 */
public class NCOOutputHandler extends OutputHandler implements DataProcessProvider, DataProcess { 


    /** _more_ */
    public static final String PROP_NCWA_PATH = "nco.ncwa.path";

    /** _more_ */
    public static final String ARG_NCO_FORMAT = "nco.format";

    /** _more_ */
    public static final String ARG_NCO_OPERATION = "nco.operation";

    /** _more_ */
    public static final String ARG_NCO_VARIABLE = "nco.variable";

    /** _more_ */
    public static final String ARG_NCO_VARIABLE_EXCLUDE =
        "nco.variable.exclude";

    /** _more_ */
    public static final String ARG_NCO_COORD = "nco.coord";

    /** _more_ */
    public static final String ARG_NCO_MASK_VARIABLE = "nco.mask.variable";

    /** _more_ */
    public static final String ARG_NCO_MASK_COMP = "nco.mask.comp";

    /** _more_ */
    public static final String ARG_NCO_MASK_VALUE = "nco.mask.value";


    /** _more_ */
    public static final String ARG_NCO_WEIGHT = "nco.weight";


    /** _more_ */
    public static final String ARG_NCO_RDD = "nco.rdd";

    /** _more_ */
    public static final String ARG_NCO_HISTORY = "nco.history";



    /** OPeNDAP Output Type */
    public static final OutputType OUTPUT_NCO_NCWA =
        new OutputType("NCO- Weighted Average", "nco.ncwa",
                       OutputType.TYPE_OTHER, OutputType.SUFFIX_NONE,
                       "/cdmdata/nco.png", CdmDataOutputHandler.GROUP_DATA);



    /** _more_ */
    public static final String OP_AVG = "avg";

    /** _more_ */
    public static final String OP_SQRAVG = "sqravg";

    /** _more_ */
    public static final String OP_AVGSQR = "avgsqr";

    /** _more_ */
    public static final String OP_MAX = "max";

    /** _more_ */
    public static final String OP_MIN = "min";

    /** _more_ */
    public static final String OP_RMS = "rms";

    /** _more_ */
    public static final String OP_RMSSDN = "rmssdn";

    /** _more_ */
    public static final String OP_SQRT = "sqrt";

    /** _more_ */
    public static final String OP_TTL = "ttl";





    /** _more_ */
    List<TwoFacedObject> OPERATION_TYPES = Misc.toList(new Object[] {
        new TwoFacedObject("Mean value", OP_AVG),
        new TwoFacedObject("Square of the mean", OP_SQRAVG),
        new TwoFacedObject("Mean of sum of squares", OP_AVGSQR),
        new TwoFacedObject("Maximium value", OP_MAX),
        new TwoFacedObject("Minimium value", OP_MIN),
        new TwoFacedObject("Root-mean-square (normalized by N)", OP_RMS),
        new TwoFacedObject("Root-mean square (normalized by N-1)", OP_RMSSDN),
        new TwoFacedObject("Square root of the mean", OP_SQRT),
        new TwoFacedObject("Sum of values ", OP_TTL),
    });


    /** _more_ */
    public static final String COMP_EQ = "eq";

    /** _more_ */
    public static final String COMP_NE = "ne";

    /** _more_ */
    public static final String COMP_GT = "gt";

    /** _more_ */
    public static final String COMP_LT = "lt";

    /** _more_ */
    public static final String COMP_GE = "ge";

    /** _more_ */
    public static final String COMP_LE = "LE";

    /** _more_ */
    List<TwoFacedObject> COMPARATORS = Misc.toList(new Object[] {
        new TwoFacedObject("&lt;", COMP_LT),
        new TwoFacedObject("&lt;=", COMP_LE),
        new TwoFacedObject("&gt;", COMP_GT),
        new TwoFacedObject("&gt;=", COMP_GE),
        new TwoFacedObject("==", COMP_EQ), new TwoFacedObject("!=", COMP_NE),
    });



    /** _more_ */
    private TempDir productDir;



    /** _more_ */
    private String ncwaPath;

    /**
     * ctor
     *
     * @param repository _more_
     * @param element _more_
     * @throws Exception _more_
     */
    public NCOOutputHandler(Repository repository, Element element)
            throws Exception {
        super(repository, element);
        addType(OUTPUT_NCO_NCWA);
        ncwaPath = getProperty(PROP_NCWA_PATH, null);
    }


    public NCOOutputHandler(Repository repository)
            throws Exception {
        super(repository, "NCO");
        ncwaPath = getProperty(PROP_NCWA_PATH, null);
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public boolean isEnabled() {
        return ncwaPath != null;
    }

    /**
       The AnalysisProvider method. Just adds this
     */
    public List<DataProcess> getDataProcesses() {
        List<DataProcess> analysese = new ArrayList<DataProcess>();
        //TODO: put this back
        //        if(isEnabled()) {
        if(true) {
            analysese.add(this);
        }
        return analysese;
    }


    /**
     * Get the Analysis id
     *
     * @return the ID
     */
    public String getDataProcessId() {
        return "NCO";
    }


    public String getDataProcessLabel() {
        return "NCO Weighted Average";
    }


    /**
     * Add this output handlers UI to the form
     *
     * @param request   the Request
     * @param entry     the Entry
     * @param sb        the form HTML
     *
     * @throws Exception  on badness
     */
    public void addToForm(Request request, Entry entry, StringBuffer sb)
            throws Exception {
        sb.append(HtmlUtils.formTable());
        sb.append(HtmlUtils.formEntry(msgLabel("CDO Stuff"),"widgets"));
        sb.append(HtmlUtils.formTableClose());
    }


    public File processRequest(Request request, Entry entry)
            throws Exception {
        return entry.getFile();
    }



    /**
     * This method gets called to determine if the given entry or entries can be displays as las xml
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
        if ( !isEnabled()) {
            return;
        }
        if ((state.entry != null) && state.entry.isFile()
                && state.entry.getResource().getPath().endsWith(".nc")) {
            links.add(makeLink(request, state.entry, OUTPUT_NCO_NCWA));
        }
    }



    /**
     * _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
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





    /**
     * _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public CdmDataOutputHandler getDataOutputHandler() throws Exception {
        return (CdmDataOutputHandler) getRepository().getOutputHandler(
            CdmDataOutputHandler.OUTPUT_OPENDAP.toString());
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


        if (request.defined(ARG_SUBMIT)) {
            return outputNCO(request, entry);
        }

        CdmDataOutputHandler dataOutputHandler = getDataOutputHandler();
        NetcdfDataset        dataset           =
            NetcdfDataset.openDataset(entry.getResource().getPath());
        List<Variable>       variables  = dataset.getVariables();
        List<TwoFacedObject> coordNames = new ArrayList<TwoFacedObject>();
        List<TwoFacedObject> varNames   = new ArrayList<TwoFacedObject>();
        for (Variable var : variables) {
            if (var instanceof CoordinateAxis) {
                coordNames.add(new TwoFacedObject(var.getName(),
                        var.getShortName()));
            }
            varNames.add(new TwoFacedObject(var.getName(),
                                            var.getShortName()));
        }

        dataset.close();


        StringBuffer sb      = new StringBuffer("");
        String       formUrl = request.url(getRepository().URL_ENTRY_SHOW);
        sb.append(HtmlUtils.form(formUrl,
                                 makeFormSubmitDialog(sb,
                                     msg("Apply NCO..."))));

        String buttons = HtmlUtils.submit("Create Weighted Average",
                                          ARG_SUBMIT);
        sb.append(buttons);
        sb.append(" ");
        sb.append(HtmlUtils.href("http://nco.sourceforge.net/nco.html",
                                 "NCO Documentation", " target=_external "));

        sb.append(HtmlUtils.formTable());
        sb.append(HtmlUtils.hidden(ARG_OUTPUT, OUTPUT_NCO_NCWA));
        sb.append(HtmlUtils.hidden(ARG_ENTRYID, entry.getId()));

        addOperationWidget(request, sb);
        addVariableWidget(request, sb, varNames);
        addDimensionWidget(request, sb, coordNames);
        addMaskWidget(request, sb, varNames);
        addWeightWidget(request, sb, varNames);
        addFormatWidget(request, sb);
        addPublishWidget(
            request, entry, sb,
            msg("Select a folder to publish the generated NetCDF file to"));
        sb.append(HtmlUtils.formTableClose());
        sb.append(buttons);

        return new Result("NCO Form", sb);
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param sb _more_
     */
    private void addOperationWidget(Request request, StringBuffer sb) {
        sb.append(HtmlUtils.formEntry(msgLabel("Operation"),
                                      HtmlUtils.select(ARG_NCO_OPERATION,
                                          OPERATION_TYPES)));
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param sb _more_
     * @param coordNames _more_
     */
    private void addDimensionWidget(Request request, StringBuffer sb,
                                    List coordNames) {
        List vars = new ArrayList(coordNames);
        vars.add(0, new TwoFacedObject("--all--", ""));
        sb.append(HtmlUtils.formEntry(msgLabel("Averaging Dimensions"),
                                      HtmlUtils.select(ARG_NCO_COORD, vars,
                                          "", " MULTIPLE SIZE=4 ")));
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param sb _more_
     * @param varNames _more_
     */
    private void addVariableWidget(Request request, StringBuffer sb,
                                   List varNames) {
        List vars = new ArrayList(varNames);
        vars.add(0, new TwoFacedObject("--all--", ""));
        sb.append(
            HtmlUtils.formEntry(
                msgLabel("Variables"),
                HtmlUtils.select(
                    ARG_NCO_VARIABLE, vars, "", " MULTIPLE SIZE=4 ") + " "
                        + HtmlUtils.checkbox(
                            ARG_NCO_VARIABLE_EXCLUDE, "true", false) + " "
                                + msg("Exclude")));
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param sb _more_
     * @param varNames _more_
     */
    private void addWeightWidget(Request request, StringBuffer sb,
                                 List varNames) {
        List vars = new ArrayList(varNames);
        vars.add(0, new TwoFacedObject("--none--", ""));
        sb.append(HtmlUtils.formEntry(msgLabel("Weight by"),
                                      HtmlUtils.select(ARG_NCO_WEIGHT, vars,
                                          "")));
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param sb _more_
     * @param varNames _more_
     */
    private void addMaskWidget(Request request, StringBuffer sb,
                               List varNames) {
        List vars = new ArrayList(varNames);
        vars.add(0, new TwoFacedObject("--none--", ""));
        sb.append(
            HtmlUtils.formEntry(
                msgLabel("Mask"),
                HtmlUtils.select(ARG_NCO_MASK_VARIABLE, vars, "") + " "
                + HtmlUtils.select(ARG_NCO_MASK_COMP, COMPARATORS, COMP_LT)
                + " " + HtmlUtils.input(ARG_NCO_MASK_VALUE, "1.0")));
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param sb _more_
     */
    private void addFormatWidget(Request request, StringBuffer sb) {
        List formats = new ArrayList();
        formats.add(new TwoFacedObject(msg("Classic Format"), "3"));
        formats.add(new TwoFacedObject(msg("NetCDF 4 Format"), "4"));
        formats.add(new TwoFacedObject(msg("NetCDF 3 64-bit Format"), "6"));
        sb.append(HtmlUtils.formEntry(msgLabel("NetCDF Format"),
                                      HtmlUtils.select(ARG_NCO_FORMAT,
                                          formats)));
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
    public Result outputNCO(Request request, Entry entry) throws Exception {

        String tail    = getStorageManager().getFileTail(entry);
        String newName = IOUtil.stripExtension(tail) + "_product.nc";
        tail = getStorageManager().getStorageFileName(tail);
        File         outFile = new File(IOUtil.joinDir(getProductDir(), tail));
        List<String> commands = new ArrayList<String>();
        commands.add(ncwaPath);
        commands.add("-" + request.get(ARG_NCO_FORMAT, 3));
        //        commands.add("--history");
        commands.add("--operation");
        commands.add(request.getString(ARG_NCO_OPERATION, OP_AVG));


        List<String> vars = (List<String>) request.get(ARG_NCO_VARIABLE,
                                new ArrayList<String>());
        if (vars.size() > 0) {
            StringBuffer varSB = new StringBuffer();
            for (String var : vars) {
                if (var.length() > 0) {
                    if (varSB.length() > 0) {
                        varSB.append(",");
                    }
                    varSB.append(var);
                }
            }
            if (varSB.length() > 0) {
                commands.add("--variable");
                commands.add(varSB.toString());
                if (request.get(ARG_NCO_VARIABLE_EXCLUDE, false)) {
                    commands.add("--exclude");
                }
            }
        }


        vars = (List<String>) request.get(ARG_NCO_COORD,
                                          new ArrayList<String>());
        if (vars.size() > 0) {
            StringBuffer varSB = new StringBuffer();
            for (String var : vars) {
                if (var.length() > 0) {
                    if (varSB.length() > 0) {
                        varSB.append(",");
                    }
                    varSB.append(var);
                }
            }
            if (varSB.length() > 0) {
                commands.add("--average");
                commands.add(varSB.toString());
            }
        }


        if (request.defined(ARG_NCO_WEIGHT)) {
            commands.add("-w");
            commands.add(request.getString(ARG_NCO_WEIGHT));
        }


        if (request.defined(ARG_NCO_MASK_VARIABLE)) {
            commands.add("--mask_variable");
            commands.add(request.getString(ARG_NCO_MASK_VARIABLE));

            commands.add("--mask_comparator");
            commands.add(request.getString(ARG_NCO_MASK_COMP));

            commands.add("--mask_value");
            commands.add("" + request.get(ARG_NCO_MASK_VALUE, 1.0));
        }

        System.err.println("cmds:" + commands);

        commands.add(entry.getResource().getPath());
        commands.add(outFile.toString());
        String[] results = getRepository().executeCommand(commands, getProductDir());
        String  errorMsg = results[1];
        String outMsg = results[0];
        if (outMsg.length() > 0) {
            return getErrorResult(request, "NCO-Error","An error occurred:<br>" + outMsg);
        }
        if (errorMsg.length() > 0) {
            return getErrorResult(request, "NCO-Error", "An error occurred:<br>" + errorMsg);
        }
        if ( !outFile.exists()) {
            return getErrorResult(request, "NCL-Error",  "Humm, the NCO generation failed for some reason");
        }

        if (doingPublish(request)) {
            if ( !request.defined(ARG_PUBLISH_NAME)) {
                request.put(ARG_PUBLISH_NAME, newName);
            }

            return getEntryManager().processEntryPublish(request, outFile,
                    null, entry, "generated from");
        }

        return request.returnFile(
            outFile, getStorageManager().getFileTail(outFile.toString()));
    }

}
