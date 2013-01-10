package gov.noaa.esrl.psd.repository.data.model;

import java.util.Date;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.ramadda.repository.Entry;
import org.ramadda.repository.Repository;
import org.ramadda.repository.Request;
import org.ramadda.repository.type.GenericTypeHandler;
import org.w3c.dom.Element;

import ucar.unidata.util.IOUtil;

public class ClimateModelFileTypeHandler extends GenericTypeHandler {
    
    public static final String FILE_REGEX = "([^_]+)_([^_]+)_(.*)_(ens..|mean|sprd)(_([^_]+))?.nc";
    public static final Pattern pattern = Pattern.compile(FILE_REGEX);
    
    /** ClimateModelFile type */
    public static final String TYPE_CLIMATEMODELFILE = "climatemodelfile";


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
    public void initializeEntryFromForm(Request request, Entry entry,
                                        Entry parent, boolean newEntry)
            throws Exception {
        if ( !newEntry) {
            return;
        }
        if ( !entry.isFile()) {
            return;
        }
        initializeEntry(entry);

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
    public void initializeEntry(Entry entry)
            throws Exception {
        String filepath = entry.getFile().toString();
        String filename = IOUtil.getFileTail(entry.getFile().toString());
        // Filename looks like  var_model_scenario_ens??_<date>.nc
        Matcher m = pattern.matcher(filename);
        if (!m.find()) {
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
        <column name="variable" type="string"  label="Variable"/>
        <column name="model" type="string"  label="Model"  showinhtml="true" />
        <column name="experiment" type="string"  label="Experiment"  showinhtml="true" />
        <column name="ensemble" type="string"  label="Ensemble Member"/>
        <column name="frequency" type="string"  label="Frequency"  showinhtml="true" />
        */

        Object[] values = getEntryValues(entry);
        values[0] = var;
        values[1] = model;
        values[2] = experiment;
        values[3] = member;
        values[4] = frequency;

    }

}
