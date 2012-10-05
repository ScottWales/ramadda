package org.ramadda.data.record;




import org.ramadda.repository.*;
import org.ramadda.repository.job.*;
import org.ramadda.repository.auth.*;
import org.ramadda.repository.map.*;
import org.ramadda.repository.metadata.Metadata;
import org.ramadda.repository.output.*;


import org.ramadda.data.point.PointFile;



import org.ramadda.data.record.*;
import ucar.unidata.geoloc.Bearing;
import ucar.unidata.geoloc.LatLonPointImpl;



import ucar.unidata.ui.ImageUtils;
import ucar.unidata.util.GuiUtils;
import org.ramadda.util.HtmlUtils;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;
import ucar.unidata.util.TwoFacedObject;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.*;

import java.io.*;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;


import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;


/**
 * Creates most of the web interfaces - the subset/products form, map overview, etc.
 *
 * @author         Jeff McWhirter
 */
public class RecordFormHandler extends RepositoryManager  {

    /** an array of colors */
    public static final Color[] COLORS = {
        Color.blue, Color.black, Color.red, Color.green, Color.orange,
        Color.cyan, Color.magenta, Color.pink, Color.yellow
    };




    /** formats # points */
    private static DecimalFormat pointCountFormat = new DecimalFormat("#,##0");

    /** formats size */
    private DecimalFormat sizeFormat = new DecimalFormat("####0.00");

    /** date format */
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm Z");

    /** the output handler */
    private RecordOutputHandler recordOutputHandler;

    /** _more_ */
    public static final String UNIT_M = "m";


    /**
     * ctor
     *
     * @param lidarOutputHandler output handler
     */
    public RecordFormHandler(RecordOutputHandler recordOutputHandler) {
        super(recordOutputHandler.getRepository());
        this.recordOutputHandler = recordOutputHandler;
    }


    /**
     * get the job manager
     *
     * @return the job manager
     */
    public JobManager getJobManager() {
        return recordOutputHandler.getJobManager();
    }


    /**
     * format the file size
     *
     * @param bytes number of bytes
     *
     * @return formatted size
     */
    public String formatFileSize(long bytes) {
        if (bytes < 1000) {
            return "" + bytes;
        }
        if (bytes < 1000000) {
            return sizeFormat.format(bytes / 1000.0) + "&nbsp;KB";
        }
        if (bytes < 1000000000) {
            return sizeFormat.format(bytes / 1000000.0) + "&nbsp;MB";
        }
        return sizeFormat.format(bytes / 1000000000.0) + "&nbsp;GB";
    }


    /**
     * make the selector for the given format
     *
     * @param t format
     *
     * @return selector
     */
    public HtmlUtils.Selector getSelect(OutputType t) {
        if (t.getIcon() != null) {
            return new HtmlUtils.Selector(
                t.getLabel(), t.getId(),
                getRepository().iconUrl(t.getIcon()));
        }
        return new HtmlUtils.Selector(t.getLabel(), t.getId(), null);
    }


    /**
     * format number of points
     *
     * @param cnt number of points
     *
     * @return formatted points
     */
    public static String formatPointCount(long cnt) {
        synchronized (pointCountFormat) {
            return pointCountFormat.format(cnt);
        }
    }


    /**
     * format date
     *
     * @param date date
     *
     * @return formatted date
     */
    public String formatDate(Date date) {
        synchronized (sdf) {
            return sdf.format(date);
        }
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param recordEntry _more_
     * @param sb _more_
     *
     * @throws Exception _more_
     */
    public void getEntryMetadata(Request request, RecordEntry recordEntry,
                                 StringBuffer sb)
            throws Exception {
        sb.append(recordEntry.getRecordFile().getHtmlDescription());
        List<RecordField> fields     = recordEntry.getRecordFile().getFields();
        long              numRecords = recordEntry.getNumRecords();
        if (numRecords > 0) {
            sb.append(HtmlUtils.b(msgLabel("Number of points")));
            sb.append(" " + numRecords);
        } else {
            sb.append(HtmlUtils.b(msgLabel("Number of points")));
            sb.append(" " + msg("unknown"));
        }
        sb.append(msgHeader("Fields"));
        sb.append(HtmlUtils.formTable());
        sb.append(HtmlUtils.row(HtmlUtils.cols(new Object[] {
            HtmlUtils.b(msg("Field Name")),
            HtmlUtils.b(msg("Label")), HtmlUtils.b(msg("Description")),
            HtmlUtils.b(msg("Unit")), HtmlUtils.b(msg("Type")) })));
        for (RecordField field : fields) {
            String type = field.getRawType();
            if (field.getArity() > 1) {
                //              type = type +" [" + field.getArity() +"]";
            }
            sb.append(HtmlUtils.rowTop(HtmlUtils.cols(new Object[] {
                field.getName(),
                field.getLabel(), field.getDescription(),
                (field.getUnit() == null)
                ? ""
                : field.getUnit(), ((type == null)
                                    ? ""
                                    : type) })));
        }
        sb.append(HtmlUtils.formTableClose());

	StringBuffer info = new StringBuffer();
	recordEntry.getRecordFile().getInfo(info);
	if(info.length()>0) {
	    sb.append(msgHeader("Extra"));
	    sb.append(HtmlUtils.pre(info.toString()));
	}
    }



    /**
     * make a color object
     *
     * @param request the request
     * @param arg which url arg
     * @param colorCnt colors
     *
     * @return the color
     */
    public Color getColor(Request request, String arg, int[] colorCnt) {
        Color c = null;
        if (request.defined(arg)) {
            String cs = request.getString(arg, null);
            if ( !cs.startsWith("#")) {
                cs = "#" + cs;
            }
            c = GuiUtils.decodeColor(cs, (Color) null);
        }
        if (c == null) {
            c = COLORS[colorCnt[0] % COLORS.length];
            colorCnt[0]++;
        }
        return c;
    }




}
