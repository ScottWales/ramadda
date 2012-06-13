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

package org.ramadda.geodata.data;


import org.ramadda.repository.*;
import org.ramadda.repository.output.*;
import org.ramadda.repository.type.*;


import org.w3c.dom.*;

import ucar.unidata.util.DateUtil;
import ucar.unidata.util.HtmlUtil;

import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.PatternFileFilter;


import ucar.unidata.util.StringUtil;
import ucar.unidata.util.TwoFacedObject;
import ucar.unidata.xml.XmlUtil;

import java.awt.geom.Rectangle2D;

import java.io.File;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.HashSet;
import java.util.List;


/**
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class GridAggregationTypeHandler extends ExtensibleGroupTypeHandler {

    /** _more_ */
    public static final int INDEX_TYPE = 0;

    /** _more_ */
    public static final int INDEX_COORDINATE = 1;

    /** _more_ */
    public static final int INDEX_FIELDS = 2;

    /** _more_ */
    public static final int INDEX_FILES = 3;

    /** _more_ */
    public static final int INDEX_PATTERN = 4;


    /** _more_ */
    public static final int INDEX_INGEST = 5;


    public static final int INDEX_HARVESTMETADATA = 6;


    /** _more_ */
    public static final String TYPE_GRIDAGGREGATION = "gridaggregation";


    /**
     * _more_
     *
     * @param repository _more_
     * @param node _more_
     * @throws Exception _more_
     */
    public GridAggregationTypeHandler(Repository repository, Element node)
            throws Exception {
        super(repository, node);
        //We don't need to do this since the Repository loads in the harvesters based on 
        //plugin classes
        //        getRepository().getHarvesterManager().addHarvesterType(
        //            GridAggregationHarvester.class);
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
    public File getNcmlFile(Request request, Entry entry, long[]timestamp) throws Exception {
        if (request == null) {
            request = getRepository().getTmpRequest();
        }
        String ncml = getNcmlString(request, entry, timestamp);
        if (ncml.length() != 0) {
            String ncmlFileName = entry.getId() +"_" + timestamp[0] + ".ncml";
            //Use the timestamp from the files to make the ncml file name based on the input files
            File tmpFile = getStorageManager().getScratchFile(ncmlFileName);
            //File tmpFile =
            //  getRepository().getStorageManager().getTmpFile(request, "grid.ncml");
            if(!tmpFile.exists()) {
                System.err.println("writing new ncml file:" + tmpFile);
                IOUtil.writeFile(tmpFile, ncml);
            } else {
                System.err.println("using existing ncml file:" + tmpFile);
            }
            return tmpFile;
        } else {
            return null;
        }
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     *
     * @return String containing the NCML with the NCML of its childrens
     *
     * @throws Exception _more_
     */
    private String getNcmlString(Request request, Entry entry, long[]timestamp)
            throws Exception {
        if (request == null) {
            request = getRepository().getTmpRequest();
        }
        StringBuffer sb = new StringBuffer();

        NcmlUtil ncmlUtil = new NcmlUtil(entry.getValue(INDEX_TYPE,
                                NcmlUtil.AGG_JOINEXISTING));
        String timeCoordinate = entry.getValue(INDEX_COORDINATE, "time");
        String files          = entry.getValue(INDEX_FILES, "").trim();
        String pattern        = entry.getValue(INDEX_PATTERN, "").trim();
        boolean ingest        = Misc.equals(entry.getValue(INDEX_INGEST, ""),"true");
        boolean harvestMetadata        = Misc.equals(entry.getValue(INDEX_HARVESTMETADATA, ""),"true");


        ncmlUtil.openNcml(sb);
        if (ncmlUtil.isJoinExisting()) {
            sb.append(XmlUtil.openTag(NcmlUtil.TAG_AGGREGATION,
                                      XmlUtil.attrs(new String[] {
                NcmlUtil.ATTR_TYPE, NcmlUtil.AGG_JOINEXISTING,
                NcmlUtil.ATTR_DIMNAME, timeCoordinate,
                NcmlUtil.ATTR_TIMEUNITSCHANGE, "true"
            })));
        } else if (ncmlUtil.isUnion()) {
            sb.append(XmlUtil.openTag(NcmlUtil.TAG_AGGREGATION,
                                      XmlUtil.attrs(new String[] {
                                          NcmlUtil.ATTR_TYPE,
                                          NcmlUtil.AGG_UNION })));
        } else if (ncmlUtil.isJoinNew()) {
            //TODO here
        } else if (ncmlUtil.isEnsemble()) {
            String ensembleDimName = "ens";
            ncmlUtil.addEnsembleVariables(sb, ensembleDimName);
            sb.append(XmlUtil.openTag(NcmlUtil.TAG_AGGREGATION,
                                      XmlUtil.attrs(new String[] {
                                          NcmlUtil.ATTR_DIMNAME,
                                          ensembleDimName,
                                          NcmlUtil.ATTR_TYPE,
                                          NcmlUtil.AGG_JOINNEW })));
            //TODO: What name here
            sb.append(XmlUtil.tag(NcmlUtil.TAG_VARIABLEAGG,
                                  XmlUtil.attrs(new String[] {
                                      NcmlUtil.ATTR_NAME,
                                      "tasmax" })));
        } else {
            throw new IllegalArgumentException("Unknown aggregation type:"
                    + ncmlUtil);
        }

        List<String> sortedChillens      = new ArrayList<String>();
        boolean      childrenAggregation = false;
        List<Entry> childrenEntries =
            getRepository().getEntryManager().getChildren(request, entry);

        //Check if the user specified any files directly
        if (files!=null && files.length() > 0) {
            if ( !entry.getUser().getAdmin()) {
                throw new IllegalArgumentException(
                    "When using the files list in the grid aggregation you must be an administrator");
            }
            List<Entry> dummyEntries = new ArrayList<Entry>();
            List<File> filesToUse = new ArrayList<File>();
            for (String f : StringUtil.split(files, "\n", true, true)) {
                File file = new File(f);
                if (file.isDirectory()) {
                    List<File> childFiles = IOUtil.getFiles(new ArrayList(),
                                                file, false,
                                                ((pattern.length() == 0)
                            ? null
                            : new PatternFileFilter(pattern)));

                    for (File child : childFiles) {
                        if (child.isDirectory()) {
                            //TODO: Do we recurse
                        } else {
                            filesToUse.add(child);
                        }
                    }
                } else {
                    if ( !file.exists()) {
                        //What to do???
                    } else {
                        filesToUse.add(file);
                    }
                }
            }

            for (File dataFile : filesToUse) {
                //Check for access
                getStorageManager().checkLocalFile(dataFile);
                Entry dummyEntry = new Entry();
                dummyEntry.setTypeHandler(getRepository().getTypeHandler(TypeHandler.TYPE_FILE));
                
                dummyEntry.setResource(new Resource(dataFile,
                        Resource.TYPE_LOCAL_FILE));
                dummyEntries.add(dummyEntry);
            }

            if(ingest) {
                //See if we have all of the files
                HashSet seen = new HashSet();
                for(Entry existingEntry: childrenEntries) {
                    seen.add(existingEntry.getFile());
                }
                for(File dataFile: filesToUse) {
                    if(seen.contains(dataFile)) {
                        continue;
                    }
                    System.err.println("New file:" + dataFile);
                    Entry newEntry = getEntryManager().addFileEntry(request, dataFile,entry, dataFile.getName(), entry.getUser(), null, null);
                    childrenEntries.add(newEntry);
                }
            } else {
                childrenEntries = dummyEntries;
            }
        }


        for (Entry child : childrenEntries) {
            if (child.getType().equals(TYPE_GRIDAGGREGATION)) {
                String ncml = getNcmlString(request, child, timestamp);
                //MATIAS:
                if (ncml != null) {
                    //                if (ncml!=""){
                    sb.append(ncml);
                    childrenAggregation = true;
                }
                continue;
            }
            sortedChillens.add(child.getResource().getPath());
        }

        if (ncmlUtil.isJoinExisting()) {
            Collections.sort(sortedChillens);
        }
        //        System.err.println("making ncml:");
        timestamp[0] = 0;
        for (String s : sortedChillens) {
            //            System.err.println("   file:" + s);
            File f = new File(s);
            timestamp[0] = timestamp[0] ^ f.lastModified();
            sb.append(
                XmlUtil.tag(
                    NcmlUtil.TAG_NETCDF,
                    XmlUtil.attrs(
                        NcmlUtil.ATTR_LOCATION,
                        IOUtil.getURL(s, getClass()).toString(),
                        NcmlUtil.ATTR_ENHANCE, "true"), ""));
        }

        sb.append(XmlUtil.closeTag(NcmlUtil.TAG_AGGREGATION));
        sb.append(XmlUtil.closeTag(NcmlUtil.TAG_NETCDF));

        //        System.err.println(sb);

        return sb.toString();

    }


    /**
     * _more_
     *
     * @param entry _more_
     * @param isNew _more_
     *
     * @throws Exception _more_
     */
    public void childEntryChanged(Entry entry, boolean isNew)
            throws Exception {
        super.childEntryChanged(entry, isNew);
        Entry parent = entry.getParentEntry();
        List<Entry> children =
            getEntryManager().getChildren(getRepository().getTmpRequest(),
                                          parent);
        //For good measure
        children.add(entry);
        getEntryManager().setBoundsOnEntry(parent, children);
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     *
     * @return _more_
     */
    public List<Service> getServices(Request request, Entry entry) {
        List<Service> services = super.getServices(request, entry);
        /*
        String url =
            HtmlUtil.url(request.entryUrl(getRepository().URL_ENTRY_SHOW,
                                          entry), new String[] {
            ARG_OUTPUT, LidarOutputHandler.OUTPUT_LATLONALTCSV.toString(),
            LidarOutputHandler.ARG_LIDAR_SKIP,
            macro(LidarOutputHandler.ARG_LIDAR_SKIP), ARG_BBOX,
            macro(ARG_BBOX),
        }, false);
        services.add(new Service("pointcloud", "Point Cloud",
                                 request.getAbsoluteUrl(url),
                                 getIconUrl(LidarOutputHandler.ICON_POINTS)));
        */
        return services;
    }



}
