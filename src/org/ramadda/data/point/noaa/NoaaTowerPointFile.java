package org.ramadda.data.point.noaa;

import org.ramadda.data.record.*;
import org.ramadda.data.point.*;
import org.ramadda.data.point.text.*;

import ucar.unidata.util.StringUtil;
import java.io.*;


/**
 */
public class NoaaTowerPointFile extends NoaaPointFile  {

    private static int IDX = 1;
    public static final int IDX_SITE_CODE  = IDX++;
    public static final int IDX_YEAR  = IDX++;
    public static final int IDX_MONTH  = IDX++;
    public static final int IDX_DAY  = IDX++;
    public static final int IDX_HOUR  = IDX++;
    public static final int IDX_MINUTE  = IDX++;
    public static final int IDX_SECOND  = IDX++;
    public static final int IDX_LATITUDE  = IDX++;
    public static final int IDX_LONGITUDE  = IDX++;
    public static final int IDX_ELEVATION  = IDX++;
    public static final int IDX_INTAKE_HEIGHT  = IDX++;
    public static final int IDX_MEASURED_VALUE  = IDX++;
    public static final int IDX_TOTAL_UNCERTAINTY_ESTIMATE  = IDX++;
    public static final int IDX_ATMOSPHERIC_VARIABILTY  = IDX++;
    public static final int IDX_MEASUREMENT_UNCERTAINTY  = IDX++;
    public static final int IDX_SCALE_UNCERTAINTY  = IDX++;
    public static final int IDX_QC_FLAG  = IDX++;

    public static final double MISSING1 = -999.0;
    public static final double MISSING2 = -999.99;


    /**
     * ctor
     *
     * @param filename _more_
     * @throws IOException On badness
     */
    public NoaaTowerPointFile(String filename) throws IOException {
        super(filename);
    }


    /**
     * This  gets called before the file is visited. It reads the header and pulls out metadata
     *
     * @param visitInfo visit info
     *
     * @return possible new visitinfo
     *
     * @throws IOException On badness
     */
    public VisitInfo prepareToVisit(VisitInfo visitInfo) throws IOException {
        super.prepareToVisit(visitInfo);
        String filename = getOriginalFilename(getFilename());
        String siteId =  StringUtil.findPattern(filename,"^(.*)_.*");
        String parameter =  StringUtil.findPattern(filename,".*\\.(.*)");
        //LOOK: this needs to be in the same order as the amrctypes.xml defines in the point plugin
        setFileMetadata(new Object[]{
                siteId,
                parameter
            });

        putFields(new String[]{
                makeField(FIELD_SITE_ID, attrType(TYPE_STRING)),
                makeField(FIELD_YEAR,""),
                makeField(FIELD_MONTH,""),
                makeField(FIELD_DAY,""),
                makeField(FIELD_HOUR,attrType(TYPE_STRING)),
                makeField(FIELD_MINUTE,attrType(TYPE_STRING)),
                makeField(FIELD_SECOND,attrType(TYPE_STRING)),
                makeField(FIELD_LATITUDE),
                makeField(FIELD_LONGITUDE),
                makeField(FIELD_INTAKE_HEIGHT),
                makeField(parameter,  attrChartable(), attrMissing(MISSING1)),
                makeField("total_uncertainty_estimate", attrChartable(), attrMissing(MISSING1)),
                makeField("atmospheric_variablitility",  attrMissing(MISSING2)),
                makeField("measurement_uncertainty",  attrChartable(), attrMissing(MISSING2)),
                makeField("scale_uncertainty",  attrChartable(), attrMissing(MISSING2)),
                makeField(FIELD_QC_FLAG,attrType(TYPE_STRING)),
            });
        setDateIndices(new int[]{IDX_YEAR, IDX_MONTH, IDX_DAY, IDX_HOUR,IDX_MINUTE,IDX_SECOND});
        return visitInfo;
    }

    public static void main(String[]args) {
        PointFile.test(args, NoaaTowerPointFile.class);
    }

}
