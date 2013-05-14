package gov.noaa.esrl.psd.repository.data.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ramadda.geodata.model.ClimateModelFileTypeHandler;
import org.ramadda.repository.Entry;
import org.ramadda.repository.Repository;
import org.w3c.dom.Element;

import ucar.unidata.util.IOUtil;


public class CMIP5ModelFileTypeHandler extends ClimateModelFileTypeHandler {
    
    //var_model_experiment_member
    public static final String CMIP5_FILE_REGEX = "([^_]+)_([^_]+)_([^_]+)_([^_]+)_(r\\d+i\\d+p\\d+)(_([^_.]+))?(\\.1x1)?.nc";

    public static final Pattern pattern = Pattern.compile(CMIP5_FILE_REGEX);
    


    /** type identifier */
    public final static String TYPE_CMIP5_MODEL_FILE =
        "cmip5_model_file";


    public CMIP5ModelFileTypeHandler(Repository repository, Element entryNode)
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
        if(values[1]!=null && !values[1].toString().isEmpty()) {
            //System.err.println ("already have  values set");
            return;
        }
        //System.err.println ("no values set");
        String filepath = entry.getFile().toString();
        String filename = IOUtil.getFileTail(entry.getFile().toString());
        // Filename looks like  var_model_scenario_ens??_<date>.nc
        Matcher m = pattern.matcher(filename);
        if (!m.find()) {
            System.err.println ("no match for: "+filename);
            return;
        }
        String var = m.group(1);
        String model = m.group(3);
        String experiment = m.group(4);
        String member = m.group(5);
        String date = m.group(7);
        String frequency = "Monthly";
        if (filepath.indexOf("Daily") >= 0) {
            frequency = "Daily";
        }

        
        /*
     <column name="collection_id" type="string"  label="Collection ID" showinhtml="false" showinform="false"/>
     <column name="model" type="enumerationplus"  label="Model"  showinhtml="true" />
     <column name="experiment" type="enumerationplus"  label="Experiment" />
     <column name="ensemble" type="string"  label="Ensemble Member"/>
     <column name="frequency" type="string"  label="Frequency"  showinhtml="true" />
     <column name="variable" type="enumerationplus"  label="Variable"  />
        */

        int idx=1;
        values[idx++] = model;
        values[idx++] = experiment;
        values[idx++] = member;
        values[idx++] = frequency;
        values[idx++] = var;

    }

}
