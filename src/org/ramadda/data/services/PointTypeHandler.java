
package org.ramadda.data.services;


import org.ramadda.data.point.PointFile;
import org.ramadda.data.point.PointMetadataHarvester;
import org.ramadda.data.record.RecordFile;
import org.ramadda.data.record.RecordFileFactory;
import org.ramadda.data.record.RecordVisitorGroup;

import org.ramadda.data.record.VisitInfo;

import org.ramadda.data.services.RecordEntry;
import org.ramadda.data.services.PointEntry;

import org.ramadda.repository.*;
import org.ramadda.repository.map.*;
import org.ramadda.repository.metadata.*;
import org.ramadda.repository.output.*;
import org.ramadda.repository.type.*;

import org.ramadda.util.HtmlUtils;
import org.ramadda.util.grid.LatLonGrid;



import org.w3c.dom.*;


import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;


import java.io.File;
import java.io.FileOutputStream;


import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;


/**
 *
 *
 * @author Jeff McWhirter
 * @version $Revision: 1.3 $
 */
public   class PointTypeHandler extends RecordTypeHandler {

    /**
     * _more_
     *
     * @param repository ramadda
     * @param node _more_
     * @throws Exception On badness
     */
    public PointTypeHandler(Repository repository, Element node)
            throws Exception {
        super(repository, node);
    }


    public  RecordOutputHandler doMakeRecordOutputHandler() throws Exception {
        RecordOutputHandler poh = (RecordOutputHandler) getRepository().getOutputHandler(PointOutputHandler.class);
        if(poh == null) {
            poh = new PointOutputHandler(getRepository(), null);
        }
        return poh;
    }



    /**
     * _more_
     *
     * @param entry _more_
     *
     * @throws Exception On badness
     */
    @Override
    public void initializeNewEntry(Entry entry) throws Exception {
        File file = entry.getFile();
        if ( !file.exists()) {
            return;
        }
        System.err.println ("initializeNewEntry:" + entry);

        log("initializeNewEntry:" + entry.getResource());
        initializeEntry(entry, file);
        PointOutputHandler outputHandler = (PointOutputHandler) getRecordOutputHandler();
        RecordVisitorGroup visitorGroup = new RecordVisitorGroup();
        PointEntry         pointEntry   = (PointEntry) outputHandler.doMakeEntry(getRepository().getTmpRequest(), entry);

        RecordFile        pointFile    = pointEntry.getRecordFile();
        List<PointEntry> pointEntries = new ArrayList<PointEntry>();
        pointEntries.add(pointEntry);
        PointMetadataHarvester metadata = doMakeMetadataHarvester(pointEntry);
        visitorGroup.addVisitor(metadata);
        Request          request       = getRepository().getTmpRequest();
        final File       quickScanFile = pointEntry.getQuickScanFile();


        DataOutputStream dos           = new DataOutputStream(
                                   new BufferedOutputStream(
                                       new FileOutputStream(quickScanFile)));
        //Make the latlon binary file when we ingest the  datafile
        visitorGroup.addVisitor(
            outputHandler.makeLatLonAltBinVisitor(
                request, entry, pointEntries, null, dos));
        log("initializeNewEntry: visting file");
        pointFile.visit(visitorGroup, new VisitInfo(true), null);
        dos.close();
        log("init new entry: count=" + metadata.getCount());
        handleHarvestedMetadata(pointEntry, metadata);
        log("initializeNewEntry: done");
    }

    /**
     * _more_
     *
     * @param pointEntry _more_
     *
     * @return _more_
     */
    public PointMetadataHarvester doMakeMetadataHarvester(
                                                          RecordEntry pointEntry) {
        return new PointMetadataHarvester();
    }



    /**
     * _more_
     *
     * @param f _more_
     *
     * @param path _more_
     * @param filename _more_
     *
     * @return _more_
     */
    @Override
    public boolean canHandleResource(String path, String filename) {
        try {
            if (filename.endsWith(".csv") || filename.endsWith(".txt") || filename.endsWith(".xyz")|| filename.endsWith(".tsv")) {
                //Look to see if there is also a properties file
                Hashtable props = RecordFile.getPropertiesForFile(path,
                                      PointFile.DFLT_PROPERTIES_FILE);
                if (props.size() == 0) {
                    return false;
                }
            }
            return super.canHandleResource(path, filename);
        } catch (Exception exc) {
            //If the loading flaked out then just keep going
            //            logException("Harvesting file:" + f, exc);
            return false;
        }
    }



    /**
     * _more_
     *
     * @param pointEntry _more_
     * @param metadata _more_
     *
     * @throws Exception _more_
     */
    protected void handleHarvestedMetadata(RecordEntry recordEntry,
                                           PointMetadataHarvester metadata)
            throws Exception {

        PointEntry pointEntry = (PointEntry) recordEntry;
        Entry entry = pointEntry.getEntry();
        if (pointEntry.getRecordFile().isCapable(PointFile.ACTION_BOUNDINGPOLYGON)) {
            if ( !entry.hasMetadataOfType(
                    MetadataHandler.TYPE_SPATIAL_POLYGON)) {
                LatLonGrid llg = new LatLonGrid(80, 40,
                                     metadata.getMaxLatitude(),
                                     metadata.getMinLongitude(),
                                     metadata.getMinLatitude(),
                                     metadata.getMaxLongitude());


                PointMetadataHarvester metadata2 =
                    new PointMetadataHarvester(llg);
                pointEntry.getBinaryPointFile().visit(metadata2,
                        new VisitInfo(true), null);
                List<double[]> polygon = llg.getBoundingPolygon();
                StringBuffer[] sb = new StringBuffer[] { new StringBuffer(),
                        new StringBuffer(), new StringBuffer(),
                        new StringBuffer() };
                int idx = 0;
                for (double[] point : polygon) {
                    String toAdd = point[0] + "," + point[1] + ";";
                    if ((sb[idx].length() + toAdd.length())
                            >= (Metadata.MAX_LENGTH - 100)) {
                        idx++;
                        if (idx >= sb.length) {
                            break;
                        }
                    }
                    sb[idx].append(toAdd);
                }
                //                System.err.println ("sb length:" + sb[idx].length() +" " +Metadata.MAX_LENGTH);

                Metadata polygonMetadata =
                    new Metadata(getRepository().getGUID(), entry.getId(),
                                 MetadataHandler.TYPE_SPATIAL_POLYGON,
                                 DFLT_INHERITED, sb[0].toString(),
                                 sb[1].toString(), sb[2].toString(),
                                 sb[3].toString(), Metadata.DFLT_EXTRA);
                entry.addMetadata(polygonMetadata, false);
            }
        }

        Object[] values = entry.getValues();
        if (values == null) {
            values = new Object[2];
        }
        values[0] = new Integer(metadata.getCount());
        Properties properties = metadata.getProperties();
        if(properties!=null) {
            String contents = makePropertiesString(properties);
            //Append the properties file contents
            if(values[1]!=null) {
                values[1] = "\n" + contents;
            } else {
                values[1] =  contents;
            }
        }

        //        xxxxx

        entry.setValues(values);
        entry.setNorth(metadata.getMaxLatitude());
        entry.setSouth(metadata.getMinLatitude());
        entry.setEast(metadata.getMaxLongitude());
        entry.setWest(metadata.getMinLongitude());
        if ( !Double.isNaN(metadata.getMinElevation())) {
            entry.setAltitudeBottom(metadata.getMinElevation());
        }
        if ( !Double.isNaN(metadata.getMaxElevation())) {
            entry.setAltitudeTop(metadata.getMaxElevation());
        }

        if (metadata.hasTimeRange()) {
            entry.setStartDate(metadata.getMinTime());
            entry.setEndDate(metadata.getMaxTime());
        }
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param services _more_
     *
     * @return _more_
     */
    @Override
    public void getServices(Request request, Entry entry,
                            List<Service> services) {
        super.getServices(request, entry, services);
        String url;
        String dfltBbox = entry.getWest() + "," + entry.getSouth() + ","
                          + entry.getEast() + "," + entry.getNorth();


        RecordOutputHandler outputHandler = getRecordOutputHandler();
        //TODO: let the output handler add services
        /****** 
        String[][] values = {
            { outputHandler.OUTPUT_LATLONALTCSV.toString(),
              "Lat/Lon/Alt CSV", ".csv", outputHandler.ICON_POINTS },
            { outputHandler.OUTPUT_LAS.toString(), "LAS 1.2", ".las",
              outputHandler.ICON_POINTS },
            //            {outputHandler.OUTPUT_ASC.toString(),
            //             "ARC Ascii Grid",
            //             ".asc",null},
            { outputHandler.OUTPUT_KMZ.toString(), ".kmz",
              "Google Earth KMZ", getIconUrl(request, ICON_KML) }
        };




        for (String[] tuple : values) {
            String product = tuple[0];
            String name    = tuple[1];
            String suffix  = tuple[2];
            String icon    = tuple[3];
            url = HtmlUtils.url(getRepository().URL_ENTRY_SHOW + "/"
                                + entry.getName() + suffix, new String[] {
                ARG_ENTRYID, entry.getId(), ARG_OUTPUT,
                outputHandler.OUTPUT_PRODUCT.getId(), ARG_PRODUCT, product,
                //ARG_ASYNCH, "false", 
                //                PointOutputHandler.ARG_POINT_SKIP,
                //                macro(PointOutputHandler.ARG_POINT_SKIP), 
                //                ARG_BBOX,  macro(ARG_BBOX), 
                //                ARG_DEFAULTBBOX, dfltBbox
            }, false);
            services.add(new Service(product, name,
                                     request.getAbsoluteUrl(url), icon));
        }

        *****/
    }


    /**                                                                         
     * _more_                                                                   
     *                                                                          
     * @param request _more_                                                    
     * @param entry _more_                                                      
     * @param map _more_                                                        
     *                                                                          
     * @return _more_                                                           
     */
    @Override
        public boolean addToMap(Request request, Entry entry, MapInfo map) {
        try {
            PointOutputHandler outputHandler = (PointOutputHandler) getRecordOutputHandler();
            outputHandler.addToMap(request, entry, map);
            return true;
        } catch (Exception exc) {
            throw new RuntimeException(exc);
        }
    }






}
