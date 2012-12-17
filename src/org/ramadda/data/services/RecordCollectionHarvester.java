/*
 * Copyright 2010 UNAVCO, 6350 Nautilus Drive, Boulder, CO 80301
 * http://www.unavco.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 */

package org.ramadda.data.services;


import org.ramadda.repository.*;
import org.ramadda.repository.auth.*;
import org.ramadda.repository.harvester.*;
import org.ramadda.repository.metadata.*;
import org.ramadda.repository.output.*;
import org.ramadda.repository.type.*;

import org.ramadda.util.HtmlUtils;

import ucar.unidata.util.TwoFacedObject;
import ucar.unidata.xml.XmlUtil;
import org.w3c.dom.*;
import java.util.regex.*;


import java.io.*;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;


import java.util.Properties;


/**
 * This class extends the RAMADDA file harvester to harvest Point data files.
 * It does a couple of things. It overrides getLastGroupType() to specify the
 * PointCollection type. This way the RAMADDA entry folders  that get created
 * will be of this type.
 * Secondly, for text point files it looks for the <filename>.properties file which
 * is our way of simply describing the CRS of the data file (e.g., UTM, geographic, etc).
 *
 * @author Jeff McWhirter
 */
public abstract class RecordCollectionHarvester extends PatternHarvester {

    /** _more_ */
    private boolean makeRecordCollection = true;

    /** _more_ */
    private static final String ATTR_MAKERECORDCOLLECTION =
        "makerecordcollection";

    /**
     * ctor
     *
     * @param repository the repository
     * @param id harvester id
     *
     * @throws Exception on badness
     */
    public RecordCollectionHarvester(Repository repository, String id)
            throws Exception {
        super(repository, id);
    }

    /**
     * ctor
     *
     * @param repository the repository
     * @param element xml node that defines this harvester
     *
     * @throws Exception on badness
     */
    public RecordCollectionHarvester(Repository repository, Element element)
            throws Exception {
        super(repository, element);
    }



    /**
     * _more_
     *
     * @param element _more_
     *
     * @throws Exception _more_
     */
    protected void init(Element element) throws Exception {
        super.init(element);

        makeRecordCollection = XmlUtil.getAttribute(element,
                ATTR_MAKERECORDCOLLECTION, makeRecordCollection);
    }


    /**
     * _more_
     *
     * @param element _more_
     *
     * @throws Exception _more_
     */
    public void applyState(Element element) throws Exception {
        super.applyState(element);
        element.setAttribute(ATTR_MAKERECORDCOLLECTION,
                             "" + makeRecordCollection);
    }

    public boolean getMakeRecordCollection() {
        return makeRecordCollection;
    }

    /**
     * _more_
     *
     * @param request _more_
     *
     * @throws Exception _more_
     */
    public void applyEditForm(Request request) throws Exception {
        super.applyEditForm(request);

        if (request.exists(ATTR_MAKERECORDCOLLECTION)) {
            makeRecordCollection = request.get(ATTR_MAKERECORDCOLLECTION,
                    makeRecordCollection);
        } else {
            makeRecordCollection = false;
        }
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param sb _more_
     *
     * @throws Exception _more_
     */
    public void createEditForm(Request request, StringBuffer sb)
            throws Exception {
        super.createEditForm(request, sb);
        sb.append(
            HtmlUtils.formEntry(
                msgLabel("Make Record Collection"),
                HtmlUtils.checkbox(
                    ATTR_MAKERECORDCOLLECTION, "true", makeRecordCollection)));
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param typeHandler _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public String makeEntryTypeSelector(Request request,
                                        TypeHandler typeHandler)
            throws Exception {
        Class typeHandlerClass = getTypeHandlerClass();
        if(typeHandlerClass==null) return super.makeEntryTypeSelector(request, typeHandler);
        String   selected = typeHandler.getType();
        List     tmp      = new ArrayList();
        for(TypeHandler th: getRepository().getTypeHandlers()) { 
            if(typeHandlerClass.isAssignableFrom(th.getClass())) {
                tmp.add(new TwoFacedObject(th.getLabel(), th.getType()));
            }
        }
        return HtmlUtils.select(ARG_TYPE, tmp, selected);
    }

    public Class getTypeHandlerClass(){
        return null;
    }



    /**
     * harvester description
     *
     * @return harvester description
     */
    public String getDescription() {
        return "Record Collection";
    }


    /**
     * _more_
     *
     * @param fileInfo _more_
     * @param f _more_
     * @param matcher _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    @Override
    public Entry harvestFile(FileInfo fileInfo, File f, Matcher matcher)
            throws Exception {
        if (f.toString().endsWith(".properties")) {
            return null;
        }
        return super.harvestFile(fileInfo, f, matcher);
    }



    /**
     * Check for a .properties file that corresponds to the given data file.
     * If it exists than add it to the entry
     *
     * @param fileInfo File information
     * @param originalFile Data file
     * @param entry New entry
     *
     * @return The entry
     */
    @Override
    public Entry initializeNewEntry(FileInfo fileInfo, File originalFile,
                                    Entry entry) {
        try {
            getRepository().getLogManager().logInfo(
                "RecordCollectonHarvester:initializeNewEntry:"
                + entry.getResource());
            if (entry.getTypeHandler() instanceof RecordTypeHandler) {
                ((RecordTypeHandler) entry.getTypeHandler()).initializeEntry(
                    entry, originalFile);
            }
            getRepository().getLogManager().logInfo(
                "RecordCollectonHarvester:initializeNewEntry done");
            return entry;
        } catch (Exception exc) {
            throw new RuntimeException(exc);
        }
    }

}
