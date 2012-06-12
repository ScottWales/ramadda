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
public class ExecAction extends MonitorAction {

    /** _more_ */
    public static final String PROP_EXEC_EXECLINE = "exec.execline";

    /** _more_ */
    private String execLine;


    /**
     * _more_
     */
    public ExecAction() {}

    /**
     * _more_
     *
     * @param id _more_
     */
    public ExecAction(String id) {
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
        return "Exec Action";
    }

    /**
     * _more_
     *
     *
     * @param entryMonitor _more_
     * @return _more_
     */
    public String getSummary(EntryMonitor entryMonitor) {
        return "Execute external program on server";
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param monitor _more_
     */
    public void applyEditForm(Request request, EntryMonitor monitor) {
        super.applyEditForm(request, monitor);
        this.execLine = request.getString(getArgId(PROP_EXEC_EXECLINE), "");
    }


    /**
     * _more_
     *
     * @param monitor _more_
     * @param sb _more_
     */
    public void addToEditForm(EntryMonitor monitor, StringBuffer sb) {
        sb.append(HtmlUtil.formTable());
        sb.append(HtmlUtil.colspan("Exec Action", 2));

        sb.append(
            HtmlUtil.formEntry(
                "Execute:",
                HtmlUtil.input(
                    getArgId(PROP_EXEC_EXECLINE), execLine,
                    HtmlUtil.SIZE_60) + HtmlUtil.title(macroTooltip)));
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
        if ( !monitor.getRepository().getProperty(PROP_MONITOR_ENABLE_EXEC,
                false)) {
            throw new IllegalArgumentException("Exec action not enabled");
        }
        Resource resource = entry.getResource();
        String command =
            monitor.getRepository().getEntryManager().replaceMacros(entry,
                execLine);
        try {
            Process process = Runtime.getRuntime().exec(command);
            int     result  = process.waitFor();
            if (result == 0) {
                monitor.getRepository().getLogManager().logInfo(
                    "ExecMonitor executed:" + command);
            } else {
                try {
                    InputStream is    = process.getErrorStream();
                    byte[]      bytes = IOUtil.readBytes(is);
                    monitor.getRepository().getLogManager().logError(
                        "ExecMonitor failed executing:" + command + "\n"
                        + new String(bytes));
                } catch (Exception noop) {
                    monitor.getRepository().getLogManager().logError(
                        "ExecMonitor failed:" + command);
                }
            }
        } catch (Exception exc) {
            monitor.handleError("Error execing monitor", exc);
        }
    }

    /**
     * Set the ExecLine property.
     *
     * @param value The new value for ExecLine
     */
    public void setExecLine(String value) {
        execLine = value;
    }

    /**
     * Get the ExecLine property.
     *
     * @return The ExecLine
     */
    public String getExecLine() {
        return execLine;
    }



}
