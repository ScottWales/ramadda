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
public class CopyAction extends MonitorAction {

    /** _more_ */
    public static final String ARG_SUBGROUP = "subgroup";


    /** _more_ */
    private String parentGroupId;

    /** _more_ */
    private String subGroup = "";

    /** _more_ */
    private Entry group;

    /**
     * _more_
     */
    public CopyAction() {}

    /**
     * _more_
     *
     * @param id _more_
     */
    public CopyAction(String id) {
        super(id);
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public String getActionName() {
        return "copy";
    }

    public String getActionLabel() {
        return "Copy Action";
    }

    /**
     * _more_
     *
     * @param entryMonitor _more_
     *
     * @return _more_
     */
    private Entry getGroup(EntryMonitor entryMonitor) {
        try {
            if (group == null) {
                group =
                    (Entry) entryMonitor.getRepository().getEntryManager()
                        .findGroup(null, parentGroupId);
            }
            return group;
        } catch (Exception exc) {
            return null;
        }
    }

    /**
     * _more_
     *
     *
     * @param entryMonitor _more_
     * @return _more_
     */
    public String getSummary(EntryMonitor entryMonitor) {
        Entry group = getGroup(entryMonitor);
        if (group == null) {
            return "Copy entry: Error bad folder";
        }
        return "Copy entry to:" + group.getName();
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param monitor _more_
     */
    public void applyEditForm(Request request, EntryMonitor monitor) {
        super.applyEditForm(request, monitor);
        this.parentGroupId = request.getString(getArgId(ARG_GROUP)
                + "_hidden", "");
        this.group    = null;
        this.subGroup = request.getString(getArgId(ARG_SUBGROUP), "").trim();
    }


    /**
     * _more_
     *
     * @param monitor _more_
     * @param sb _more_
     */
    public void addToEditForm(EntryMonitor monitor, StringBuffer sb) {
        sb.append(HtmlUtil.formTable());
        sb.append(HtmlUtil.colspan("Copy Action", 2));
        try {
            Entry  group      = getGroup(monitor);
            String errorLabel = "";
            if ((group != null) && !monitor.okToAddNew(group)) {
                errorLabel = HtmlUtil.span(
                    monitor.getRepository().msg(
                        "You cannot add to the folder"), HtmlUtil.cssClass(
                        HtmlUtil.CLASS_ERRORLABEL));
            }
            String groupName = ((group != null)
                                ? group.getFullName()
                                : "");
            String inputId   = getArgId(ARG_GROUP);
            String select =
                monitor.getRepository().getHtmlOutputHandler().getSelect(
                    null, inputId,
                    HtmlUtil.img(
                        monitor.getRepository().iconUrl(
                            ICON_FOLDER_OPEN)) + HtmlUtil.space(1)
                                + monitor.getRepository().msg(
                                    "Select"), false, "");
            sb.append(HtmlUtil.hidden(inputId + "_hidden", parentGroupId,
                                      HtmlUtil.id(inputId + "_hidden")));
            sb.append(
                HtmlUtil.formEntry(
                    "Folder:",
                    HtmlUtil.disabledInput(
                        inputId, groupName,
                        HtmlUtil.SIZE_60 + HtmlUtil.id(inputId)) + select));
            sb.append(
                HtmlUtil.formEntry(
                    "Sub-Folder Template:",
                    HtmlUtil.input(
                        getArgId(ARG_SUBGROUP), subGroup, HtmlUtil.SIZE_60)));
        } catch (Exception exc) {
            throw new RuntimeException(exc);
        }
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
            Entry group = getGroup(monitor);
            if (group == null) {
                return;
            }
        } catch (Exception exc) {
            monitor.handleError("Error handling Copy Action", exc);
        }
    }


    /**
     *  Set the ParentGroupId property.
     *
     *  @param value The new value for ParentGroupId
     */
    public void setParentGroupId(String value) {
        this.parentGroupId = value;
    }

    /**
     *  Get the ParentGroupId property.
     *
     *  @return The ParentGroupId
     */
    public String getParentGroupId() {
        return this.parentGroupId;
    }

    /**
     *  Set the SubGroup property.
     *
     *  @param value The new value for SubGroup
     */
    public void setSubGroup(String value) {
        this.subGroup = value;
    }

    /**
     *  Get the SubGroup property.
     *
     *  @return The SubGroup
     */
    public String getSubGroup() {
        return this.subGroup;
    }



}
