package org.ramadda.geodata.model;

import java.util.Date;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.ramadda.repository.Entry;
import org.ramadda.repository.Repository;
import org.ramadda.repository.Request;
import org.ramadda.repository.type.GenericTypeHandler;
import org.ramadda.repository.type.GranuleTypeHandler;
import org.w3c.dom.Element;

import ucar.unidata.util.IOUtil;

public class ClimateModelFileTypeHandler extends GranuleTypeHandler {
    
    public static final String FILE_REGEX = "([^_]+)_([^_]+)_(.*)_(ens..|mean|sprd|clim)(_([^_]+))?.nc";
    public static final Pattern pattern = Pattern.compile(FILE_REGEX);
    
    /** ClimateModelFile type */
    public static final String TYPE_CLIMATE_MODELFILE = "noaa_climate_modelfile";

    public ClimateModelFileTypeHandler(Repository repository, Element entryNode)
            throws Exception {
        super(repository, entryNode);
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param parent _more_
     * @param newEntry _more_
     *
     * @throws Exception _more_
     */
@Override
    public void initializeEntry(Entry entry)
            throws Exception {
        super.initializeEntry(entry);
        Object[] values = getEntryValues(entry);
        if(values[1]!=null) {
            System.err.println ("already have  values set");
            return;
        }
        System.err.println ("no values set");
        String filepath = entry.getFile().toString();
        String filename = IOUtil.getFileTail(entry.getFile().toString());
        // Filename looks like  var_model_scenario_ens??_<date>.nc
        Matcher m = pattern.matcher(filename);
        if (!m.find()) {
            System.err.println ("no match");
            return;
        }
        String var = m.group(1);
        String model = m.group(2);
        String experiment = m.group(3);
        String member = m.group(4);
        String date = m.group(6);
        String frequency = "Monthly";
        if (filepath.indexOf("Daily") >= 0) {
            frequency = "Daily";
        }
        
        /*
      <column name="collection_id" type="string"  label="Collection ID" showinhtml="false"/>
        <column name="variable" type="string"  label="Variable"/>
        <column name="model" type="string"  label="Model"  showinhtml="true" />
        <column name="experiment" type="string"  label="Experiment"  showinhtml="true" />
        <column name="ensemble" type="string"  label="Ensemble Member"/>
        <column name="frequency" type="string"  label="Frequency"  showinhtml="true" />
        */

        int idx=1;
        values[idx++] = var;
        values[idx++] = model;
        values[idx++] = experiment;
        values[idx++] = member;
        values[idx++] = frequency;

    }

}
