/*
* Copyright 2008-2013 Jeff McWhirter/ramadda.org
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

package org.ramadda.geodata.model;


import org.ramadda.repository.Entry;
import org.ramadda.repository.Repository;
import org.ramadda.repository.Request;
import org.ramadda.repository.type.GenericTypeHandler;
import org.ramadda.repository.type.GranuleTypeHandler;

import org.w3c.dom.Element;

import ucar.unidata.util.IOUtil;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;


/**
 * A class for handling climate model files
 */
public class ClimateModelFileTypeHandler extends GranuleTypeHandler {

    //var _ model _ experiment _ member

    /** the file regex */
    public static final String FILE_REGEX =
        "([^_]+)_([^_]+)_(.*)_(ens..|mean|sprd|clim)(_([^_]+))?.nc";

    /** _more_ */
    public static final Pattern pattern = Pattern.compile(FILE_REGEX);

    /** ClimateModelFile type */
    public static final String TYPE_CLIMATE_MODELFILE = "climate_modelfile";

    /**
     * _more_
     *
     * @param repository _more_
     * @param entryNode _more_
     *
     * @throws Exception _more_
     */
    public ClimateModelFileTypeHandler(Repository repository,
                                       Element entryNode)
            throws Exception {
        super(repository, entryNode);
    }

    /**
     * Initialize the entry
     *
     * @param entry the Entry
     *
     * @throws Exception  problems during initialization
     */
    @Override
    public void initializeEntry(Entry entry) throws Exception {
        super.initializeEntry(entry);
        Object[] values = getEntryValues(entry);
        if ((values[1] != null) && !values[1].toString().isEmpty()) {
            //System.err.println("already have  values set");

            return;
        }
        //System.err.println("no values set");
        String filepath = entry.getFile().toString();
        String filename = IOUtil.getFileTail(entry.getFile().toString());
        // Filename looks like  var_model_scenario_ens??_<date>.nc
        Matcher m = pattern.matcher(filename);
        if ( !m.find()) {
            System.err.println("no match");

            return;
        }
        String var        = m.group(1);
        String model      = m.group(2);
        String experiment = m.group(3);
        String member     = m.group(4);
        String date       = m.group(6);
        String frequency  = "Monthly";
        if (filepath.indexOf("Daily") >= 0) {
            frequency = "Daily";
        }

        /*
     <column name="collection_id" type="string"  label="Collection ID" showinhtml="false" showinform="false"/>
     <column name="model" type="enumerationplus"  label="Model"  showinhtml="true" xxxxvalues="file:/org/ramadda/data/model/models.txt"/>
     <column name="experiment" type="enumerationplus"  label="Experiment" xxxxvalues="file:/org/ramadda/data/model/experiments.txt" showinhtml="true" />
     <column name="ensemble" type="string"  label="Ensemble"/>
     <column name="variable" type="enumerationplus"  label="Variable"  xxxxxvalues="file:/org/ramadda/data/model/vars.txt"/>
        */

        int idx = 1;
        values[idx++] = model;
        values[idx++] = experiment;
        values[idx++] = member;
        values[idx++] = var;

    }

    /**
     * Test it
     *
     * @param args  the arguments
     */
    public static void main(String[] args) {
        for (String arg : args) {
            Matcher m = pattern.matcher(arg);
            if ( !m.find()) {
                System.err.println("no match x");
            } else {
                System.err.println("match");
                String var = m.group(1);
                System.err.println("var:" + var);
            }
        }
    }


}
