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

package org.ramadda.repository.monitor;


import org.ramadda.repository.*;
import org.ramadda.repository.auth.*;

import ucar.unidata.util.HtmlUtil;

import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;
import ucar.unidata.xml.XmlUtil;


import java.io.File;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;


/**
 *
 *
 * @author RAMADDA Development Team
 * @version $Revision: 1.30 $
 */
public class LdmAction extends MonitorAction {

    /** _more_ */
    public static final String PROP_LDM_FEED = "ldm.feed";

    /** _more_ */
    public static final String PROP_LDM_PQINSERT = "ldm.pqinsert";

    /** _more_ */
    public static final String PROP_LDM_PRODUCTID = "ldm.productid";

    /** _more_ */
    public static final String PROP_LDM_QUEUE = "ldm.queue";
    /** _more_ */
    public static final String[] LDM_FEED_TYPES = {
        "PPS", "DDS", "HDS", "IDS", "SPARE", "UNIWISC", "PCWS", "FSL2",
        "FSL3", "FSL4", "FSL5", "GPSSRC", "CONDUIT", "FNEXRAD", "LIGHTNING",
        "WSI", "DIFAX", "FAA604", "GPS", "FNMOC", "GEM", "NIMAGE", "NTEXT",
        "NGRID", "NPOINT", "NGRAPH", "NOTHER", "NEXRAD3", "NEXRAD2",
        "NXRDSRC", "EXP", "ANY", "NONE", "DDPLUS", "WMO", "UNIDATA", "FSL",
        "NMC", "NPORT",
    };



    /** _more_ */
    private String queue = "";

    /** _more_ */
    private String pqinsert = "";

    /** _more_ */
    private String feed = "SPARE";

    /** _more_ */
    private String productId = "";


    /**
     * _more_
     */
    public LdmAction() {}

    /**
     * _more_
     *
     * @param id _more_
     */
    public LdmAction(String id) {
        super(id);
    }


    public boolean adminOnly() {
        return true;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public String getActionName() {
        return "LDM Action";
    }

    /**
     * _more_
     *
     *
     * @param entryMonitor _more_
     * @return _more_
     */
    public String getSummary(EntryMonitor entryMonitor) {
        return "Inject into the LDM";
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param monitor _more_
     */
    public void applyEditForm(Request request, EntryMonitor monitor) {
        super.applyEditForm(request, monitor);
        this.pqinsert  = request.getString(getArgId(PROP_LDM_PQINSERT), "");
        this.feed      = request.getString(getArgId(PROP_LDM_FEED), "");
        this.queue     = request.getString(getArgId(PROP_LDM_QUEUE), "");
        this.productId = request.getString(getArgId(PROP_LDM_PRODUCTID), "");
    }


    /**
     * _more_
     *
     * @param monitor _more_
     * @param sb _more_
     */
    public void addToEditForm(EntryMonitor monitor, StringBuffer sb) {
        sb.append(HtmlUtil.formTable());
        sb.append(HtmlUtil.colspan("LDM Action", 2));

        String ldmExtra1 = "";
        if ((pqinsert.length() > 0) && !new File(pqinsert).exists()) {
            ldmExtra1 = HtmlUtil.space(2)
                        + HtmlUtil.span("File does not exist!",
                                        HtmlUtil.cssClass("errorlabel"));
        }
        String ldmExtra2 = "";
        if ((queue.length() > 0) && !new File(queue).exists()) {
            ldmExtra2 = HtmlUtil.space(2)
                        + HtmlUtil.span("File does not exist!",
                                        HtmlUtil.cssClass("errorlabel"));
        }


        sb.append(
            HtmlUtil.formEntry(
                "Path to pqinsert:",
                HtmlUtil.input(
                    getArgId(PROP_LDM_PQINSERT), pqinsert,
                    HtmlUtil.SIZE_60) + ldmExtra1));
        sb.append(HtmlUtil.formEntry("Queue Location:",
                                     HtmlUtil.input(getArgId(PROP_LDM_QUEUE),
                                         queue,
                                         HtmlUtil.SIZE_60) + ldmExtra2));
        sb.append(HtmlUtil.formEntry("Feed:",
                                     HtmlUtil.select(getArgId(PROP_LDM_FEED),
                                         Misc.toList(LDM_FEED_TYPES), feed)));

        sb.append(
            HtmlUtil.formEntry(
                "Product ID:",
                HtmlUtil.input(
                    getArgId(PROP_LDM_PRODUCTID), productId,
                    HtmlUtil.SIZE_60 + HtmlUtil.title(macroTooltip))));

        sb.append(HtmlUtil.formTableClose());
    }


    /**
     * _more_
     *
     *
     * @param monitor _more_
     * @param entry _more_
     */
    protected void entryMatched(EntryMonitor monitor, Entry entry) {
        try {
            Resource resource = entry.getResource();
            if ( !resource.isFile()) {
                monitor.handleError("LdmMonitor:" + this
                                    + " Entry is not a file:" + entry, null);
                return;
            }
            String id = productId.trim();
            id = monitor.getRepository().getEntryManager().replaceMacros(
                entry, id);

            insertIntoQueue(monitor.getRepository(), pqinsert, queue, feed,
                            id, resource.getPath());

        } catch (Exception exc) {
            monitor.handleError("Error posting to LDM", exc);
        }
    }

    /**
     * _more_
     *
     *
     * @param repository _more_
     * @param pqinsert _more_
     * @param queue _more_
     * @param feed _more_
     * @param productId _more_
     * @param file _more_
     *
     * @throws Exception _more_
     */
    public static void insertIntoQueue(Repository repository,
                                       String pqinsert, String queue,
                                       String feed, String productId,
                                       String file)
            throws Exception {
        if (productId.length() > 0) {
            productId = " -p \"" + productId + "\" ";
        }
        String command = pqinsert + " " + productId + " -f " + feed + " -q "
                         + queue + " " + file;
        //        System.err.println("Executing:" + command);
        Process process = Runtime.getRuntime().exec(command);
        int     result  = process.waitFor();
        if (result == 0) {
            repository.getLogManager().logInfo(
                "LdmMonitor inserted into queue:" + file);
        } else {
            try {
                InputStream is    = process.getErrorStream();
                byte[]      bytes = IOUtil.readBytes(is);
                repository.getLogManager().logError(
                    "LdmMonitor failed to insert into queue:" + file + "\n"
                    + new String(bytes));
                System.err.println("Error:" + new String(bytes));
            } catch (Exception noop) {
                repository.getLogManager().logError(
                    "LdmMonitor failed to insert into queue:" + file);
            }
        }
    }


    /**
     * Set the Pqinsert property.
     *
     * @param value The new value for Pqinsert
     */
    public void setPqinsert(String value) {
        pqinsert = value;
    }

    /**
     * Get the Pqinsert property.
     *
     * @return The Pqinsert
     */
    public String getPqinsert() {
        return pqinsert;
    }



    /**
     *  Set the Feed property.
     *
     *  @param value The new value for Feed
     */
    public void setFeed(String value) {
        feed = value;
    }

    /**
     *  Get the Feed property.
     *
     *  @return The Feed
     */
    public String getFeed() {
        return feed;
    }

    /**
     * Set the Queue property.
     *
     * @param value The new value for Queue
     */
    public void setQueue(String value) {
        queue = value;
    }

    /**
     * Get the Queue property.
     *
     * @return The Queue
     */
    public String getQueue() {
        return queue;
    }

    /**
     *  Set the ProductId property.
     *
     *  @param value The new value for ProductId
     */
    public void setProductId(String value) {
        productId = value;
    }

    /**
     *  Get the ProductId property.
     *
     *  @return The ProductId
     */
    public String getProductId() {
        return productId;
    }



}
