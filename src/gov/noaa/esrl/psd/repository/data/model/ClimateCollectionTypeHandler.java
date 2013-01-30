package gov.noaa.esrl.psd.repository.data.model;

import ucar.unidata.sql.Clause;
import ucar.unidata.sql.SqlUtil;
import java.sql.*;


import java.util.Date;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.ramadda.repository.*;
import org.ramadda.repository.database.*;
import org.ramadda.util.HtmlUtils;
import org.ramadda.util.TTLCache;
import org.ramadda.repository.type.Column;
import org.ramadda.repository.type.GenericTypeHandler;
import org.ramadda.repository.type.ExtensibleGroupTypeHandler;

import ucar.unidata.util.Misc;
import ucar.unidata.util.TwoFacedObject;

import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import ucar.unidata.util.IOUtil;

public class ClimateCollectionTypeHandler extends ExtensibleGroupTypeHandler {
    
    
    /** ClimateCollection type */
    public static final String TYPE_CLIMATE_COLLECTION = "noaa_climate_collection";

    public static final String DB_TABLE_NAME = "noaa_climate_modelfile";
    public static final String DB_COLUMN_COLLECTION_ID= DB_TABLE_NAME +".collection_id";

    public static final String ARG_PREFIX = "climate.";

    public static final String ARG_CLIMATE_SEARCH = ARG_PREFIX + "search";

    public static final String ARG_CLIMATE_VARIABLE = "variable";
    public static final String ARG_CLIMATE_MODEL = "model";
    public static final String ARG_CLIMATE_EXPERIMENT = "experiment";
    public static final String ARG_CLIMATE_ENSEMBLE = "ensemble";
    public static final String ARG_CLIMATE_FREQUENCY = "frequency";

    

    public static final String [] FIELDS  = {ARG_CLIMATE_VARIABLE,
                                             ARG_CLIMATE_MODEL,
                                             ARG_CLIMATE_EXPERIMENT,
                                             ARG_CLIMATE_ENSEMBLE,
                                             ARG_CLIMATE_FREQUENCY};

    public static final String [] FIELD_NAMES  = {"Variable",
                                                  "Model",
                                                  "Experiment",
                                                  "Ensemble",
                                                  "Frequency"};

    private TTLCache<String, ClimateMetadata> metadataCache = new TTLCache<String, ClimateMetadata>(24* 60 * 60 * 1000);



    public ClimateCollectionTypeHandler(Repository repository, Element entryNode)
        throws Exception {
        super(repository, entryNode);
    }


    
    /**
     * Get the HTML display for this type
     *
     * @param request  the Request
     * @param group    the group
     * @param subGroups    the subgroups
     * @param entries      the Entries
     *
     * @return  the Result
     *
     * @throws Exception  problem getting the HTML
     */
    public Result getHtmlDisplay(Request request, Entry group,
                                 List<Entry> subGroups, List<Entry> entries)
        throws Exception {

        //Check if the user clicked on tree view, etc.
        if ( !isDefaultHtmlOutput(request)) {
            return null;
        }
        StringBuffer sb     = new StringBuffer();
        ClimateMetadata climateMetadata = getClimateMetadata(group);

        sb.append(group.getDescription());
        sb.append(HtmlUtils.form(request.entryUrl(getRepository().URL_ENTRY_SHOW, group)));
        sb.append(HtmlUtils.formTable());
        sb.append(HtmlUtils.formEntry("",HtmlUtils.submit( "Search", ARG_CLIMATE_SEARCH)));
        for(int i=0;i<FIELDS.length;i++) {
            String column=  FIELDS[i];
            String label=  FIELD_NAMES[i];
            List values = climateMetadata.getValues(column);
            if(values.size()>1) {
                String urlArg = ARG_PREFIX + column;
                sb.append(HtmlUtils.formEntry(msgLabel(label),HtmlUtils.select(urlArg, values,
                                                                               request.getString(urlArg,(String)null),
                                                                               100)));
            }
        }
        sb.append(HtmlUtils.formEntry("",HtmlUtils.submit( "Search", ARG_CLIMATE_SEARCH)));
        sb.append(HtmlUtils.formTableClose());
        sb.append(HtmlUtils.formClose());

        if(request.exists(ARG_CLIMATE_SEARCH)) {
            sb.append(HtmlUtils.p());
            entries =  processSearch(request, group);
            if(entries.size()==0) {
                sb.append(msg("No entries found"));
            } else {
                sb.append("Found " + entries.size() +" results");
                sb.append(HtmlUtils.p());
                for(Entry entry: entries) {
                    sb.append(getEntryManager().getBreadCrumbs(request,
                                                               entry));
                    sb.append(HtmlUtils.br());
                }
            }
        }
        return new Result(msg("Climate Collection"), sb);
    }


    private ClimateMetadata getClimateMetadata(Entry group) throws Exception {
        ClimateMetadata climateMetadata = metadataCache.get(group.getId());

        if(climateMetadata !=null && !climateMetadata.hasValues()) {
            climateMetadata = null;
        }

        if(climateMetadata ==null) {
            List<List> valueList = new ArrayList<List>();
            Clause clause = Clause.eq(DB_COLUMN_COLLECTION_ID, group.getId());
            for(String column: FIELDS) {
                Statement stmt = getRepository().getDatabaseManager().select(
                                                                             SqlUtil.distinct(DB_TABLE_NAME+"."+column),
                                                                             DB_TABLE_NAME, clause);
                valueList.add(Misc.toList(SqlUtil.readString(getRepository().getDatabaseManager().getIterator(stmt), 1)));
            }
            climateMetadata = new ClimateMetadata(valueList);
            metadataCache.put(group.getId(), climateMetadata);
        }
        return climateMetadata;
    }

    private List<Entry> processSearch(Request request, Entry group) throws Exception {
        List<Clause> clauses = new ArrayList<Clause>();
        clauses.add(Clause.eq(DB_COLUMN_COLLECTION_ID, group.getId()));
        clauses.add(Clause.join(Tables.ENTRIES.COL_ID,
                                DB_TABLE_NAME + ".id"));
        for(int i=0;i<FIELDS.length;i++) {
            String column=  FIELDS[i];
            String urlArg = ARG_PREFIX + column;
            if(request.defined(urlArg)) {
                clauses.add(Clause.makeOrSplit(DB_TABLE_NAME +"." + column, request.getString(urlArg)));
            }
        }
        List[] pair = getEntryManager().getEntries(request, clauses, getRepository().getTypeHandler(ClimateModelFileTypeHandler.TYPE_CLIMATE_MODELFILE));
        //        pair[0] is the folder entries. shouldn't have any here
        return (List<Entry>) pair[1];
    }


    private static List addBlank(List v) {
        v.add(0, new TwoFacedObject("--any--",""));
        return v;
    }

    private class ClimateMetadata {
        List  variables;
        List  models;
        List  experiments;
        List  ensembles;
        List  frequencies;

        public ClimateMetadata(List<List>values) {
            try {
            List<Column> cols = getRepository().getTypeHandler(ClimateModelFileTypeHandler.TYPE_CLIMATE_MODELFILE).getColumns();
            int idx =0;
            variables = addBlank(getEnumValues(cols.get(idx+1), values.get(idx++)));
            models = addBlank(getEnumValues(cols.get(idx+1), values.get(idx++)));
            experiments = addBlank(getEnumValues(cols.get(idx+1), values.get(idx++)));
            ensembles =  addBlank(values.get(idx++));
            frequencies  = addBlank(values.get(idx++));
            } catch (Exception e) {
                System.err.println("unable to make values");
            }
        }

        public List getValues(String key) {
            if(key.equals(ARG_CLIMATE_VARIABLE)) return variables;
            if(key.equals(ARG_CLIMATE_MODEL)) return models;
            if(key.equals(ARG_CLIMATE_EXPERIMENT)) return  experiments;
            if(key.equals(ARG_CLIMATE_ENSEMBLE)) return ensembles;
            if(key.equals(ARG_CLIMATE_FREQUENCY)) return frequencies;
            return null;
        }

        public boolean hasValues() {
            return variables.size()>0 &&
                models.size()>0 &&
                experiments.size()>0 &&
                ensembles.size()>0 &&
                frequencies.size()>0;
        }
        
        public List<TwoFacedObject> getEnumValues(Column col, List vals) {
            
            List<TwoFacedObject> newVals = new ArrayList<TwoFacedObject>();
            List<TwoFacedObject> colVals = col.getValues();
            for (int i = 0; i < vals.size(); i++) {
                Object val = vals.get(i);
                Object tfo = TwoFacedObject.findId(val, colVals);
                if (tfo != null) {
                    newVals.add((TwoFacedObject)tfo);
                } else {
                    newVals.add(new TwoFacedObject(val.toString(), val));
                }
            }
            TwoFacedObject.sort(newVals);
            return newVals;
            
        }



    }



}
