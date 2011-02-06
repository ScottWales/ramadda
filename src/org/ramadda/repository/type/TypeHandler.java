/*
 * Copyright 1997-2010 Unidata Program Center/University Corporation for
 * Atmospheric Research, P.O. Box 3000, Boulder, CO 80307,
 * support@unidata.ucar.edu.
 * Copyright 2010- ramadda.org
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

package org.ramadda.repository.type;


import org.w3c.dom.Element;

import org.ramadda.repository.*;
import org.ramadda.repository.auth.*;
import org.ramadda.repository.database.*;

import org.ramadda.repository.metadata.*;
import org.ramadda.repository.output.*;

import ucar.unidata.sql.Clause;


import ucar.unidata.sql.SqlUtil;
import ucar.unidata.util.DateUtil;

import ucar.unidata.util.HtmlUtil;
import ucar.unidata.util.HttpServer;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.LogUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;
import ucar.unidata.util.TwoFacedObject;
import ucar.unidata.xml.XmlUtil;

import java.io.BufferedInputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import java.sql.PreparedStatement;

import java.sql.ResultSet;
import java.sql.Statement;




import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;


/**
 * Class TypeHandler _more_
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class TypeHandler extends RepositoryManager {

    /** _more_          */
    public static final String CATEGORY_DEFAULT = "General";

    /** _more_ */
    public static final String TYPE_ANY = Constants.TYPE_ANY;

    /** _more_ */
    public static final String TYPE_FILE = Constants.TYPE_FILE;

    /** _more_ */
    public static final String TYPE_GROUP = Constants.TYPE_GROUP;

    /** _more_ */
    public static final String TYPE_HOMEPAGE = "homepage";

    /** _more_ */
    public static final String TYPE_CONTRIBUTION = "contribution";

    /** _more_ */
    public static final String TYPE_OPENDAPLINK = "opendaplink";



    /** _more_ */
    public static final String TAG_COLUMN = "column";

    /** _more_ */
    public static final String TAG_PROPERTY = "property";

    /** _more_ */
    public static final String ATTR_NAME = "name";


    /** _more_ */
    public static final String ATTR_CATEGORY = "category";

    /** _more_ */
    public static final String ATTR_VALUE = "value";

    /** _more_ */
    public static final String ATTR_DATATYPE = "datatype";

    /** _more_ */
    public static final String TAG_TYPE = "type";

    /** _more_ */
    public static final String TAG_METADATA = "metadata";

    /** _more_ */
    public static final String TAG_HANDLER = "handler";


    /** _more_ */
    public static final int MATCH_UNKNOWN = 0;

    /** _more_ */
    public static final int MATCH_TRUE = 1;

    /** _more_ */
    public static final int MATCH_FALSE = 2;


    /** _more_ */
    public static final TwoFacedObject ALL_OBJECT = new TwoFacedObject("All",
                                                        "");

    /** _more_ */
    public static final TwoFacedObject NONE_OBJECT =
        new TwoFacedObject("None", "");



    /** _more_          */
    private TypeHandler parent;

    /** _more_          */
    private List<TypeHandler> childrenTypes = new ArrayList<TypeHandler>();


    /** _more_ */
    private String type;

    /** _more_ */
    private String description;


    /** _more_ */
    private String category = CATEGORY_DEFAULT;

    /** _more_ */
    private Hashtable dontShowInForm = new Hashtable();

    /** _more_ */
    public Hashtable properties = new Hashtable();

    /** _more_ */
    private String defaultDataType;

    /** _more_ */
    private String displayTemplatePath;


    /** _more_ */
    private List<String> requiredMetadata = new ArrayList<String>();

    /** _more_          */
    private boolean forUser = true;

    /**
     * _more_
     *
     * @param repository _more_
     */
    public TypeHandler(Repository repository) {
        super(repository);
    }


    /**
     * _more_
     *
     * @param repository _more_
     * @param entryNode _more_
     */
    public TypeHandler(Repository repository, Element entryNode) {
        this(repository);
        displayTemplatePath = XmlUtil.getAttribute(entryNode,
                "displaytemplate", (String) null);

        this.category = XmlUtil.getAttribute(entryNode, ATTR_CATEGORY,
                                             category);
        List metadataNodes = XmlUtil.findChildren(entryNode, TAG_METADATA);
        for (int i = 0; i < metadataNodes.size(); i++) {
            Element metadataNode = (Element) metadataNodes.get(i);
            requiredMetadata.add(XmlUtil.getAttribute(metadataNode, ATTR_ID));
        }
    }

    /**
     * _more_
     *
     * @param repository _more_
     * @param type _more_
     */
    public TypeHandler(Repository repository, String type) {
        this(repository, type, "");
    }


    /**
     * _more_
     *
     * @param repository _more_
     * @param type _more_
     * @param description _more_
     */
    public TypeHandler(Repository repository, String type,
                       String description) {
        super(repository);
        this.type        = type;
        this.description = description;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public int getTotalNumberOfValues() {
        int cnt = getNumberOfMyValues();
        if (parent != null) {
            cnt += parent.getTotalNumberOfValues();
        }
        return cnt;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public int getNumberOfMyValues() {
        return 0;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public int getValuesOffset() {
        if (parent != null) {
            return parent.getTotalNumberOfValues();
        }
        return 0;
    }


    /**
     * _more_
     *
     * @param entryNode _more_
     *
     * @throws Exception _more_
     */
    protected void init(Element entryNode) throws Exception {
        forUser = XmlUtil.getAttribute(entryNode, ATTR_FORUSER, forUser);
        setType(XmlUtil.getAttribute(entryNode, ATTR_DB_NAME));
        setProperties(entryNode);
        setDescription(XmlUtil.getAttribute(entryNode, ATTR_DB_DESCRIPTION,
                                            getType()));

        String superType = XmlUtil.getAttribute(entryNode, ATTR_SUPER,
                               (String) null);
        if (superType != null) {
            parent = getRepository().getTypeHandler(superType, false, false);
            if (parent == null) {
                throw new IllegalArgumentException("Cannot find parent type:"
                        + superType);
            }
            parent.addChildTypeHandler(this);
        }
    }

    public List<Comment> getComments(Request request, Entry entry) throws Exception  {
        return null;
    }


    /**
     * _more_
     *
     * @param tableNames _more_
     */
    public void getTableNames(List<String> tableNames) {
        String tableName = getTableName();
        if ( !tableNames.contains(tableName)) {
            tableNames.add(tableName);
        }
        //        for(TypeHandler child: childrenTypes) {
        //            child.getTableNames(tableNames);
        //        }
        if (getParent() != null) {
            getParent().getTableNames(tableNames);
        }
    }

    /**
     * _more_
     *
     * @param types _more_
     */
    public void getChildTypes(List<String> types) {
        if ( !types.contains(getType())) {
            types.add(getType());
        }
        for (TypeHandler child : childrenTypes) {
            child.getChildTypes(types);
        }
    }

    /**
     * _more_
     *
     * @param child _more_
     */
    public void addChildTypeHandler(TypeHandler child) {
        if ( !childrenTypes.contains(child)) {
            childrenTypes.add(child);
        }
    }

    /**
     *  Set the Parent property.
     *
     *  @param value The new value for Parent
     */
    public void xxxsetParent(TypeHandler value) {
        parent = value;
    }

    /**
     *  Get the Parent property.
     *
     *  @return The Parent
     */
    public TypeHandler getParent() {
        return parent;
    }


    /**
     * _more_
     *
     * @param entry _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public TypeHandler getTypeHandlerForCopy(Entry entry) throws Exception {
        return this;
    }

    public Resource getResourceForCopy(Request request, Entry oldEntry, Entry newEntry) throws Exception {
        Resource newResource = new Resource(oldEntry.getResource());
        if (newResource.isFile()) {
            String newFileName =
                getStorageManager().getFileTail(
                                                oldEntry.getResource().getTheFile().getName());
            String newFile =
                getStorageManager()
                .copyToStorage(
                               request, oldEntry.getTypeHandler()
                               .getResourceInputStream(
                                                       oldEntry), getRepository().getGUID()
                               + "_" + newFileName).toString();
            newResource.setPath(newFile);
        }
        return newResource;
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param mapVarName _more_
     * @param jsBuff _more_
     */
    public void addToMap(Request request, Entry entry, String mapVarName,
                         StringBuffer jsBuff) {}

    public String getEntryText(Entry entry) {
        return entry.getDescription();
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
            throws Exception {}

    public String getApplication1PermissionName() {
        return "Application specific";
    }


    /**
     * _more_
     *
     * @param tableName _more_
     *
     * @return _more_
     */
    public boolean shouldExportTable(String tableName) {
        return true;
    }

    /**
     * _more_
     *
     * @throws Exception _more_
     */
    public void initAfterDatabaseImport() throws Exception {}


    /**
     * _more_
     *
     * @param entry _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public InputStream getResourceInputStream(Entry entry) throws Exception {
        return new BufferedInputStream(
            getStorageManager().getFileInputStream(getFileForEntry(entry)));
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
    public Result getHtmlDisplay(Request request, Entry entry)
            throws Exception {
        if (parent != null) {
            return parent.getHtmlDisplay(request, entry);
        }
        return null;
    }




    /**
     * _more_
     *
     * @param request _more_
     * @param group _more_
     * @param subGroups _more_
     * @param entries _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result getHtmlDisplay(Request request, Entry group,
                                 List<Entry> subGroups, List<Entry> entries)
            throws Exception {
        if (parent != null) {
            return parent.getHtmlDisplay(request, group, subGroups, entries);
        }
        return null;
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
    public String getInlineHtml(Request request, Entry entry)
            throws Exception {
        if (parent != null) {
            return parent.getInlineHtml(request, entry);
        }
        return null;
    }


    /**
     * _more_
     *
     * @param request _more_
     *
     * @return _more_
     */
    public boolean canBeCreatedBy(Request request) {
        if (parent != null) {
            return parent.canBeCreatedBy(request);
        }
        return true;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public boolean adminOnly() {
        if (parent != null) {
            return parent.adminOnly();
        }
        return false;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean isSynthType() {
        if (parent != null) {
            return parent.isSynthType();
        }
        return false;
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param mainEntry _more_
     * @param ancestor _more_
     * @param synthId _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public List<String> getSynthIds(Request request, Entry mainEntry,
                                    Entry ancestor, String synthId)
            throws Exception {
        if (parent != null) {
            return parent.getSynthIds(request, mainEntry, ancestor, synthId);
        }
        throw new IllegalArgumentException(
            "getSynthIds  not implemented in class:" + getClass().getName());
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param parentEntry _more_
     * @param id _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Entry makeSynthEntry(Request request, Entry parentEntry, String id)
            throws Exception {
        if (parent != null) {
            return parent.makeSynthEntry(request, parentEntry, id);
        }
        throw new IllegalArgumentException("makeSynthEntry  not implemented");
    }



    public String getProperty(String name, String dflt) {
        return getProperty(null, name, dflt);
    }


    /**
     * _more_
     *
     * @param name _more_
     *
     * @return _more_
     */
    public String getProperty(Entry entry,String name) {
        String result = (String) properties.get(name);
        if (result != null) {
            return result;
        }
        if (parent != null) {
            return parent.getProperty(entry, name);
        }
        return null;
    }


    /**
     * _more_
     *
     * @param name _more_
     * @param dflt _more_
     *
     * @return _more_
     */
    public String getProperty(Entry entry,String name, String dflt) {
        String result = (String) properties.get(name);
        if (result != null) {
            return result;
        }
        if (parent != null) {
            return parent.getProperty(entry, name, dflt);
        }
        return dflt;
    }

    /**
     * _more_
     *
     * @param name _more_
     * @param dflt _more_
     *
     * @return _more_
     */
    public int getProperty(Entry entry,String name, int dflt) {
        //TODO:check for parent
        return Misc.getProperty(properties, name, dflt);
    }


    /**
     * _more_
     *
     * @param name _more_
     * @param dflt _more_
     *
     * @return _more_
     */
    public boolean getProperty(Entry entry,String name, boolean dflt) {
        return Misc.getProperty(properties, name, dflt);
    }


    /**
     * _more_
     *
     * @param name _more_
     * @param value _more_
     */
    public void putProperty(String name, String value) {
        properties.put(name, value);
    }


    /** _more_ */
    static int cnt = 0;

    /** _more_ */
    int mycnt = cnt++;



    /**
     * _more_
     *
     * @param arg _more_
     * @param dflt _more_
     *
     * @return _more_
     */
    public String getFormLabel(Entry entry,String arg, String dflt) {
        return getProperty(entry, "form." + arg+".label", dflt);
    }


    /**
     * _more_
     *
     * @param arg _more_
     *
     * @return _more_
     */
    public boolean okToShowInForm(Entry entry, String arg) {
        return okToShowInForm(entry, arg, true);
    }

    /**
     * _more_
     *
     * @param arg _more_
     * @param dflt _more_
     *
     * @return _more_
     */
    public boolean okToShowInForm(Entry entry,String arg, boolean dflt) {
        String value = getProperty(entry, "form." + arg+".show", "" + dflt);
        return value.equals("true");
    }


    /**
     * _more_
     *
     * @param arg _more_
     * @param dflt _more_
     *
     * @return _more_
     */
    public String getFormDefault(Entry entry,String arg, String dflt) {
        String prop = getProperty(entry, "form." + arg+".default");
        if (prop == null) {
            return dflt;
        }
        return prop;
    }


    /**
     * _more_
     *
     * @param id _more_
     *
     * @return _more_
     */
    public Entry createEntry(String id) {
        return new Entry(id, this);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean returnToEditForm() {
        if (parent != null) {
            return parent.returnToEditForm();
        }
        return false;
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param node _more_
     *
     * @throws Exception _more_
     */
    public void initializeEntryFromXml(Request request, Entry entry,
                                       Element node)
            throws Exception {
        if (parent != null) {
            parent.initializeEntryFromXml(request, entry, node);
        }
    }


    /**
     * _more_
     *
     * @param entry _more_
     * @param node _more_
     *
     * @throws Exception _more_
     */
    public void addToEntryNode(Entry entry, Element node) throws Exception {
        if (parent != null) {
            parent.addToEntryNode(entry, node);
        }
    }



    /**
     * _more_
     *
     * @param obj _more_
     *
     * @return _more_
     */
    public boolean equals(Object obj) {
        if ( !(obj.getClass().equals(getClass()))) {
            return false;
        }
        return Misc.equals(type, ((TypeHandler) obj).getType());
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getNodeType() {
        if (parent != null) {
            return parent.getNodeType();
        }
        return NODETYPE_ENTRY;
    }



    /**
     * _more_
     *
     * @return _more_
     */
    public String getType() {
        return type;
    }


    /**
     * _more_
     *
     * @param value _more_
     */
    public void setType(String value) {
        this.type = value;
    }

    /**
     * _more_
     *
     * @param type _more_
     *
     * @return _more_
     */
    public boolean isType(String type) {
        if (this.type.equals(type)) {
            return true;
        }
        if (parent != null) {
            return parent.isType(type);
        }
        return false;
    }


    /**
     * _more_
     *
     * @param results _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public final Entry createEntryFromDatabase(ResultSet results)
            throws Exception {
        return createEntryFromDatabase(results, false);
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
    public void initializeEntryFromForm(Request request, Entry entry,
                                        Entry parent, boolean newEntry)
            throws Exception {
        if (this.parent != null) {
            this.parent.initializeEntryFromForm(request, entry, parent,
                    newEntry);
        }
    }




    public boolean canHarvestFile(File f) {
        return false;
    }

    /**
     * _more_
     *
     * @param results _more_
     * @param abbreviated _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public final Entry createEntryFromDatabase(ResultSet results,
            boolean abbreviated)
            throws Exception {
        if (parent != null) {}

        //id,type,name,desc,group, user,file,createdata,fromdate,todate
        int             col        = 3;
        String          id         = results.getString(1);
        Entry           entry      = createEntry(id);
        DatabaseManager dbm        = getDatabaseManager();
        Date            createDate = null;

        entry.initEntry(results.getString(col++), results.getString(col++), 
                        getEntryManager().findGroup(null, results.getString(col++)), 
                        getUserManager()
            .findUser(results
                .getString(col++), true), new Resource(getStorageManager()
                .resourceFromDB(results.getString(col++)), results
                .getString(col++), results.getString(col++), results
                .getLong(col++)), results.getString(col++), (createDate =
                    dbm.getDate(results, col++)).getTime(), dbm
                        .getDate(results, col++, createDate).getTime(), dbm
                        .getDate(results, col++).getTime(), dbm
                        .getDate(results, col++).getTime(), null);
        entry.setSouth(results.getDouble(col++));
        entry.setNorth(results.getDouble(col++));
        entry.setEast(results.getDouble(col++));
        entry.setWest(results.getDouble(col++));
        entry.setAltitudeTop(results.getDouble(col++));
        entry.setAltitudeBottom(results.getDouble(col++));

        if ( !abbreviated) {
            initializeEntryFromDatabase(entry);
        }
        return entry;
    }

    /**
     * _more_
     *
     * @param entry _more_
     *
     * @throws Exception _more_
     */
    public void initializeEntryFromDatabase(Entry entry) throws Exception {
        if (parent != null) {
            parent.initializeEntryFromDatabase(entry);
        }
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     *
     * @throws Exception _more_
     */
    public void applyNewForm(Request request, Entry entry) throws Exception {
        Hashtable<String, Metadata> existingMetadata = new Hashtable<String,
                                                           Metadata>();
        List<Metadata> metadataList = new ArrayList<Metadata>();
        for (String metadataId : requiredMetadata) {
            for (MetadataHandler handler :
                    getMetadataManager().getMetadataHandlers()) {
                if (handler.canHandle(metadataId)) {
                    handler.handleForm(request, entry,
                                       getRepository().getGUID(), "",
                                       existingMetadata, metadataList, true);
                    break;
                }
            }
        }
        //        System.err.println("Added:" + metadataList);
    }





    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param html _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    protected String processDisplayTemplate(Request request, Entry entry,
                                            String html)
            throws Exception {
        html = html.replace("${" + ARG_NAME + "}", entry.getName());
        html = html.replace("${" + ARG_LABEL + "}", entry.getLabel());
        html = html.replace("${" + ARG_DESCRIPTION + "}",
                            entry.getDescription());
        html = html.replace("${" + ARG_CREATEDATE + "}",
                            formatDate(request, entry.getCreateDate(),
                                       entry));
        html = html.replace("${" + ARG_CHANGEDATE + "}",
                            formatDate(request, entry.getChangeDate(),
                                       entry));
        html = html.replace("${" + ARG_FROMDATE + "}",
                            formatDate(request, entry.getStartDate(), entry));
        html = html.replace("${" + ARG_TODATE + "}",
                            formatDate(request, entry.getEndDate(), entry));
        html = html.replace("${" + ARG_CREATOR + "}",
                            entry.getUser().getLabel());

        return html;
    }


    /**
     * _more_
     *
     * @param entry _more_
     * @param request _more_
     * @param showDescription _more_
     * @param showResource _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public StringBuffer getEntryContent(Entry entry, Request request,
                                        boolean showDescription,
                                        boolean showResource)
            throws Exception {
        if (parent != null) {
            //            return parent.getEntryContent(entry, request, showDescription, showResource);
        }

        StringBuffer sb     = new StringBuffer();
        OutputType   output = request.getOutput();
        if (true) {
            if (displayTemplatePath != null) {
                String html =
                    getRepository().getResource(displayTemplatePath);
                return new StringBuffer(processDisplayTemplate(request,
                        entry, html));
            }
            sb.append("<table cellspacing=\"0\" cellpadding=\"2\">");
            sb.append(getInnerEntryContent(entry, request, output,
                                           showDescription, showResource,
                                           true));


            /*
            List<Metadata> metadataList = getRepository().getMetadata(entry);
            if (metadataList.size() > 0) {
                sb.append(HtmlUtil.formEntry("<p>", ""));
                StringBuffer mSB = new StringBuffer();
                mSB.append("<ul>");
                for (Metadata metadata : metadataList) {
                    mSB.append("<li>");
                    if (metadata.getType().equals(Metadata.TYPE_LINK)) {
                        mSB.append(metadata.getAttr1() + ": ");
                        mSB.append(HtmlUtil.href(metadata.getAttr2(),
                                metadata.getAttr3()));
                    } else {
                        mSB.append(metadata.getAttr1());
                        mSB.append(" ");
                        mSB.append(metadata.getAttr2());
                    }
                }
                mSB.append("</ul>");
                sb.append(HtmlUtil.formEntry(msgLabel("Metadata"), mSB.toString()));
            }
            */

            sb.append("</table>\n");


        } else if (output.equals(XmlOutputHandler.OUTPUT_XML)) {}
        return sb;

    }


    /**
     * _more_
     *
     * @param entryNode _more_
     */
    protected void setProperties(Element entryNode) {
        List propertyNodes = XmlUtil.findChildren(entryNode, TAG_PROPERTY);
        for (int propIdx = 0; propIdx < propertyNodes.size(); propIdx++) {
            Element propertyNode = (Element) propertyNodes.get(propIdx);
            if (XmlUtil.hasAttribute(propertyNode, ATTR_VALUE)) {
                putProperty(XmlUtil.getAttribute(propertyNode, ATTR_NAME),
                            XmlUtil.getAttribute(propertyNode, ATTR_VALUE));
            } else {
                putProperty(XmlUtil.getAttribute(propertyNode, ATTR_NAME),
                            XmlUtil.getChildText(propertyNode));
            }
        }
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
        return new ArrayList<Service>();
    }


    public boolean isGroup() {
        return false;
    }


    /**
     * _more_
     *
     * @param entry _more_
     * @param request _more_
     * @param links _more_
     *
     *
     * @throws Exception _more_
     */
    public void getEntryLinks(Request request, Entry entry, List<Link> links)
            throws Exception {

        if (parent != null) {
            parent.getEntryLinks(request, entry, links);
            return;
        }

        boolean isGroup = entry.isGroup();
        boolean canDoNew = isGroup
                           && getAccessManager().canDoAction(request, entry,
                               Permission.ACTION_NEW);

        if (canDoNew) {
            links.add(
                new Link(
                    request.url(
                        getRepository().URL_ENTRY_FORM, ARG_GROUP,
                        entry.getId(), ARG_TYPE,
                        TYPE_GROUP), getRepository().iconUrl(
                            ICON_FOLDER_ADD), "New Folder",
                                OutputType.TYPE_FILE));
            links.add(
                new Link(
                    request.url(
                        getRepository().URL_ENTRY_FORM, ARG_GROUP,
                        entry.getId(), ARG_TYPE,
                        TYPE_FILE), getRepository().iconUrl(ICON_ENTRY_ADD),
                                    "New File", OutputType.TYPE_FILE));

            links.add(
                new Link(
                    request.url(
                        getRepository().URL_ENTRY_NEW, ARG_GROUP,
                        entry.getId()), getRepository().iconUrl(ICON_NEW),
                                        "New Entry",
                                        OutputType.TYPE_FILE
                                        | OutputType.TYPE_TOOLBAR));
            Link hr = new Link(true);
            hr.setLinkType(OutputType.TYPE_FILE);
            links.add(hr);

        }
        if (request.getUser().getAdmin()) {
            links.add(
                new Link(
                    HtmlUtil.url(
                        getRepository().URL_ENTRY_EXPORT.toString() + "/"
                        + IOUtil.stripExtension(entry.getName())
                        + ".zip", new String[] { ARG_ENTRYID,
                    entry.getId() }), getRepository().iconUrl(ICON_EXPORT),
                                      "Export Entries",
                                      OutputType.TYPE_FILE));

            if (canDoNew) {
                links.add(
                    new Link(
                        request.url(
                            getRepository().URL_ENTRY_IMPORT, ARG_GROUP,
                            entry.getId()), getRepository().iconUrl(
                                ICON_IMPORT), "Import Entries",
                                    OutputType.TYPE_FILE));

                links.add(new Link(request
                    .url(getHarvesterManager().URL_HARVESTERS_IMPORTCATALOG,
                        ARG_GROUP, entry.getId()), getRepository()
                            .iconUrl(ICON_CATALOG), "Import THREDDS Catalog",
                                OutputType.TYPE_FILE));
            }
            Link hr = new Link(true);
            hr.setLinkType(OutputType.TYPE_FILE);
            links.add(hr);
        }



        if ( !canDoNew && isGroup
                && getAccessManager().canDoAction(request, entry,
                    Permission.ACTION_UPLOAD)) {
            links.add(
                new Link(
                    request.url(
                        getRepository().URL_ENTRY_UPLOAD, ARG_GROUP,
                        entry.getId()), getRepository().iconUrl(ICON_UPLOAD),
                                        "Upload a file",
                                        OutputType.TYPE_FILE
                                        | OutputType.TYPE_TOOLBAR));
        }


        if (getAccessManager().canEditEntry(request, entry)) {
            links.add(
                new Link(
                    request.entryUrl(getRepository().URL_ENTRY_FORM, entry),
                    getRepository().iconUrl(ICON_EDIT), "Edit Entry",
                    OutputType.TYPE_EDIT | OutputType.TYPE_TOOLBAR));

            if (getEntryManager().isAnonymousUpload(entry)) {
                links.add(
                    new Link(
                        request.entryUrl(
                            getRepository().URL_ENTRY_CHANGE, entry,
                            ARG_JUSTPUBLISH, "true"), getRepository().iconUrl(
                                ICON_PUBLISH), "Make Entry Public",
                                    OutputType.TYPE_EDIT
                                    | OutputType.TYPE_TOOLBAR));
            }

            links.add(
                new Link(
                    request.entryUrl(
                        getMetadataManager().URL_METADATA_FORM,
                        entry), getRepository().iconUrl(ICON_METADATA_EDIT),
                                "Edit Properties",
                                OutputType.TYPE_EDIT));
            links.add(
                new Link(
                    request.entryUrl(
                        getMetadataManager().URL_METADATA_ADDFORM,
                        entry), getRepository().iconUrl(ICON_METADATA_ADD),
                                "Add property", OutputType.TYPE_EDIT));
            links.add(
                new Link(
                    request.entryUrl(getRepository().URL_ACCESS_FORM, entry),
                    getRepository().iconUrl(ICON_ACCESS),"Access",
                    OutputType.TYPE_EDIT));

        }

        if (getAccessManager().canDoAction(request, entry,
                                           Permission.ACTION_DELETE)) {
            links.add(
                new Link(
                    request.entryUrl(
                        getRepository().URL_ENTRY_DELETE,
                        entry), getRepository().iconUrl(ICON_DELETE),
                                "Delete Entry",
                                OutputType.TYPE_EDIT
                                | OutputType.TYPE_TOOLBAR));

        }



        Link downloadLink = getEntryDownloadLink(request, entry);
        if (downloadLink != null) {
            links.add(downloadLink);
        }
        links.add(
            new Link(
                request.entryUrl(getRepository().URL_COMMENTS_SHOW, entry),
                getRepository().iconUrl(ICON_COMMENTS),
                "Add/View Comments",
                OutputType.TYPE_EDIT | OutputType.TYPE_TOOLBAR));

        if ((request.getUser() != null)
                && !request.getUser().getAnonymous()) {
            links.add(
                new Link(
                    request.entryUrl(
                        getRepository().URL_ENTRY_COPY, entry,
                        ARG_FROM), getRepository().iconUrl(ICON_MOVE),
                                   "Copy/Move Entry",
                                   OutputType.TYPE_EDIT
                                   | OutputType.TYPE_TOOLBAR));
        }




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
    public boolean canDownload(Request request, Entry entry)
            throws Exception {
        if (parent != null) {
            return parent.canDownload(request, entry);
        }


        if ( !entry.isFile()) {
            return false;
        }
        return true;
    }


    /**
     * _more_
     *
     * @param entry _more_
     *
     * @return _more_
     */
    public String getPathForEntry(Entry entry) {
        return entry.getResource().getPath();
    }


    /**
     * _more_
     *
     * @param entry _more_
     *
     * @return _more_
     */
    public File getFileForEntry(Entry entry) {
        return entry.getResource().getTheFile();
    }



    /**
     * _more_
     *
     *
     * @param request _more_
     * @param entry _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Link getEntryDownloadLink(Request request, Entry entry)
            throws Exception {
        if ( !getAccessManager().canDownload(request, entry)) {
            return null;
        }
        String size = " ("
                      + formatFileLength(entry.getResource().getFileSize())
                      + ")";

        String fileTail = getStorageManager().getFileTail(entry);
        fileTail = HtmlUtil.urlEncodeExceptSpace(fileTail);
        return new Link(getEntryManager().getEntryResourceUrl(request,
                entry), getRepository().iconUrl(ICON_FETCH),
                        msg("Download file") + size, OutputType.TYPE_FILE);
    }



    /**
     * _more_
     *
     * @param entry _more_
     * @param request _more_
     * @param output _more_
     * @param showDescription _more_
     * @param showResource _more_
     * @param linkToDownload _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public StringBuffer getInnerEntryContent(Entry entry, Request request,
                                             OutputType output,
                                             boolean showDescription,
                                             boolean showResource,
                                             boolean linkToDownload)
            throws Exception {

        if (parent != null) {
            return parent.getInnerEntryContent(entry, request, output,
                    showDescription, showResource, linkToDownload);
        }

        boolean showImage = false;
        if (showResource && entry.getResource().isImage()) {
            if (entry.getResource().isFile()
                    && getAccessManager().canDownload(request, entry)) {
                showImage = true;
            }
        }

        StringBuffer sb = new StringBuffer();
        if (true || output.equals(OutputHandler.OUTPUT_HTML)) {
            OutputHandler outputHandler =
                getRepository().getOutputHandler(request);
            String nextPrev = StringUtil.join("",
                                  outputHandler.getNextPrevLinks(request,
                                      entry, output));

            if (showDescription) {
                String nameString = entry.getName();
                nameString = HtmlUtil.href(
                    HtmlUtil.url(
                        request.url(getRepository().URL_ENTRY_SHOW),
                        ARG_ENTRYID, entry.getId()), nameString);

                sb.append(formEntry(request, msgLabel("Name"), nameString));

                String desc = entry.getDescription();
                if ((desc != null) && (desc.length() > 0) && (!isWikiText(desc))) {
                    sb.append(formEntry(request, msgLabel("Description"),
                            getEntryManager().getEntryText(request, entry,
                                desc)));
                }
            }
            String userSearchLink =
                HtmlUtil.href(
                    HtmlUtil.url(
                        request.url(getRepository().URL_USER_PROFILE),
                        ARG_USER_ID,
                        entry.getUser().getId()), entry.getUser().getLabel(),
                            "title=\"View user profile\"");

            sb.append(formEntry(request, msgLabel("Created by"),
                                         userSearchLink + " @ "
                                         + formatDate(request,
                                             entry.getCreateDate(), entry)));

            Resource resource     = entry.getResource();
            String   resourceLink = resource.getPath();
            if (resourceLink.length() > 0) {
                if (entry.getResource().isUrl()) {
                    resourceLink = getResourceUrl(request, entry);
                    resourceLink = HtmlUtil.href(resourceLink, resourceLink);

                } else if (entry.getResource().isFile()) {
                    resourceLink =
                        getStorageManager().getFileTail(resourceLink);
                    resourceLink =
                        HtmlUtil.urlEncodeExceptSpace(resourceLink);
                    if (getAccessManager().canDownload(request, entry)) {
                        resourceLink =
                            HtmlUtil.href(getEntryResourceUrl(request,
                                entry), resourceLink);

                    }
                }
                if (entry.getResource().getFileSize() > 0) {
                    resourceLink =
                        resourceLink + HtmlUtil.space(2)
                        + formatFileLength(entry.getResource().getFileSize());
                }
                if (showImage) {
                    /*                    String nextPrev = HtmlUtil.href(request.entryUrl(getRepository().URL_ENTRY_SHOW,
                                                                     entry, ARG_PREVIOUS,
                                                                     "true"), iconUrl(ICON_LEFT),
                                                    msg("View Previous")) +
                        HtmlUtil.href(request.entryUrl(getRepository().URL_ENTRY_SHOW,
                                                       entry, ARG_NEXT,
                                                       "true"), iconUrl(ICON_LEFT),
                                                       msg("View Next"));*/
                    resourceLink = nextPrev + HtmlUtil.space(1)
                                   + resourceLink;

                }
                sb.append(formEntry(request, msgLabel("Resource"),
                                             resourceLink));

                if (entry.isFile()) {
                    //                    sb.append(formEntry(request, msgLabel("Size"),
                    //                            entry.getResource().getFileSize()
                    //                            + HtmlUtil.space(1) + msg("bytes")));
                }
            }

            if ((entry.getCreateDate() != entry.getStartDate())
                    || (entry.getCreateDate() != entry.getEndDate())) {
                if (entry.getEndDate() != entry.getStartDate()) {
                    String startDate = formatDate(request,
                                           entry.getStartDate(), entry);
                    String endDate = formatDate(request, entry.getEndDate(),
                                         entry);
                    String searchUrl =
                        HtmlUtil.url(
                            request.url(getRepository().URL_ENTRY_SEARCH),
                            Misc.newList(
                                ARG_FROMDATE, startDate, ARG_TODATE,
                                endDate));
                    String searchLink =
                        HtmlUtil.href(
                            searchUrl,
                            HtmlUtil.img(
                                getRepository().iconUrl(ICON_SEARCH),
                                "Search for entries with this date range",
                                " border=0 "));
                    sb.append(formEntry(request, msgLabel("Date Range"),
                            searchLink + HtmlUtil.space(1) + startDate
                            + HtmlUtil.space(1)
                            + HtmlUtil.img(iconUrl(ICON_RANGE))
                            + HtmlUtil.space(1) + endDate));
                } else {
                    sb.append(formEntry(request, msgLabel("Date"),
                            formatDate(request, entry.getStartDate(),
                                       entry)));
                }
            }
            String typeDesc = entry.getTypeHandler().getDescription();
            if ((typeDesc == null) || (typeDesc.trim().length() == 0)) {
                typeDesc = entry.getTypeHandler().getType();
            }
            if ( !showImage) {
                sb.append(formEntry(request, msgLabel("Type"), typeDesc));
            }

            String datatype = entry.getDataType();
            if ( !entry.getTypeHandler().hasDefaultDataType()
                    && (datatype != null) && (datatype.length() > 0)) {
                sb.append(formEntry(request, msgLabel("Data Type"),
                                             entry.getDataType()));
            }

            boolean showMap = true;
            if (showMap) {
                if (entry.hasLocationDefined()) {
                    sb.append(formEntry(request, msgLabel("Location"),
                            entry.getSouth() + "/" + entry.getEast()));
                } else if (entry.hasAreaDefined()) {
                    /*
                    String img =
                        HtmlUtil.img(request.url(getRepository().URL_GETMAP,
                            ARG_SOUTH, "" + entry.getSouth(), ARG_WEST,
                            "" + entry.getWest(), ARG_NORTH,
                            "" + entry.getNorth(), ARG_EAST,
                            "" + entry.getEast()));
                    //                    sb.append(HtmlUtil.formEntry(msgLabel("Area"), img));
                    String areaHtml = "<table><tr align=center><td>"
                                      + entry.getNorth()
                                      + "</td></tr><tr align=center><td>"
                                      + entry.getWest() + "  "
                                      + entry.getEast()
                                      + "</td></tr><tr align=center><td>"
                                      + entry.getSouth()
                                      + "</td></tr></table>";
                    sb.append(HtmlUtil.formEntry(msgLabel("Area"), areaHtml));
                    */
                }
            }

            if (showResource && entry.getResource().isImage()) {
                if (entry.getResource().isFile()
                        && getAccessManager().canDownload(request, entry)) {
                    sb.append(formEntryTop(request, msgLabel("Image"),
                            HtmlUtil.img(getEntryResourceUrl(request, entry),
                                         "", "width=600")));


                } else if (entry.getResource().isUrl()) {
                    sb.append(formEntryTop(request,msgLabel("Image"),
                            HtmlUtil.img(entry.getResource().getPath())));
                }
            }

        } else if (output.equals(XmlOutputHandler.OUTPUT_XML)) {}
        return sb;

    }

    public static boolean isWikiText(String desc) {
        if(desc == null) return false;
        return  (desc.trim().startsWith("<wiki>"));
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
    public String getResourceUrl(Request request, Entry entry)
            throws Exception {
        Resource resource = entry.getResource();
        return resource.getPath();
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
    public String getEntryResourceUrl(Request request, Entry entry)
            throws Exception {
        return getEntryManager().getEntryResourceUrl(request, entry);
    }


    /**
     * _more_
     *
     * @param entry _more_
     *
     * @throws Exception _more_
     */
    public void initializeNewEntry(Entry entry) throws Exception {
        if (parent != null) {
            parent.initializeNewEntry(entry);
        }
    }

    /**
     * _more_
     *
     * @param newEntry _more_
     * @param oldEntry _more_
     *
     * @throws Exception _more_
     */
    public void initializeCopiedEntry(Entry newEntry, Entry oldEntry)
            throws Exception {
        if (parent != null) {
            parent.initializeCopiedEntry(newEntry, oldEntry);
        }
    }


    /**
     * _more_
     *
     * @param longName _more_
     *
     * @return _more_
     */
    public List<TwoFacedObject> getListTypes(boolean longName) {
        return new ArrayList<TwoFacedObject>();

    }

    /**
     * _more_
     *
     * @param request _more_
     * @param what _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result processList(Request request, String what) throws Exception {
        return new Result("Error",
                          new StringBuffer(msgLabel("Unknown listing type")
                                           + what));
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getTableName() {
        return Tables.ENTRIES.NAME;
    }


    /**
     * _more_
     *
     * @param s _more_
     *
     * @return _more_
     */
    private String cleanQueryString(String s) {
        s = s.replace("\r\n", " ");
        s = StringUtil.stripAndReplace(s, "'", "'", "'dummy'");
        return s;
    }




    /**
     * _more_
     *
     * @param request _more_
     * @param what _more_
     * @param clause _more_
     * @param extra _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Statement select(Request request, String what, Clause clause,
                            String extra)
            throws Exception {
        List<Clause> clauses = new ArrayList<Clause>();
        clauses.add(clause);
        return select(request, what, clauses, extra);
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param what _more_
     * @param clauses _more_
     * @param extra _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Statement select(Request request, String what,
                            List<Clause> clauses, String extra)
            throws Exception {

        clauses = new ArrayList<Clause>(clauses);
        //We do the replace because (for some reason) any CRNW screws up the pattern matching
        String       whatString   = cleanQueryString(what);
        String       extraString  = cleanQueryString(extra);

        List<String> myTableNames = new ArrayList<String>();
        getTableNames(myTableNames);

        List<String> tableNames = (List<String>) Misc.toList(new String[] {
                                      Tables.ENTRIES.NAME,
                                      Tables.METADATA.NAME,
                                      Tables.USERS.NAME,
                                      Tables.ASSOCIATIONS.NAME });
        tableNames.addAll(myTableNames);
        HashSet seenTables = new HashSet();

        List    tables     = new ArrayList();
        boolean didEntries = false;
        boolean didMeta    = false;

        int     cnt        = 0;
        for (String tableName : tableNames) {
            String pattern = ".*[, =\\(]+" + tableName + "\\..*";
            if (Clause.isColumnFromTable(clauses, tableName)
                    || whatString.matches(pattern)
                    || (extraString.matches(pattern))) {
                tables.add(tableName);
                if (tableName.equals(Tables.ENTRIES.NAME)) {
                    didEntries = true;
                } else if (tableName.equals(Tables.METADATA.NAME)) {
                    didMeta = true;
                } else if (myTableNames.contains(tableName)) {
                    seenTables.add(tableName);
                }
            }
            cnt++;
        }

        if (didMeta) {
            tables.add(Tables.METADATA.NAME);
            didEntries = true;
        }


        int metadataCnt = 0;

        while (true) {
            String subTable = Tables.METADATA.NAME + "_" + metadataCnt;
            metadataCnt++;
            if ( !Clause.isColumnFromTable(clauses, subTable)) {
                break;
            }
            tables.add(Tables.METADATA.NAME + " " + subTable);
        }

        if (didEntries) {
            List<String> typeList = (List<String>) request.get(ARG_TYPE,
                                        new ArrayList());
            typeList.remove(TYPE_ANY);
            if (typeList.size() > 0) {
                List<String> types = new ArrayList<String>();
                for (String type : typeList) {
                    TypeHandler typeHandler =
                        getRepository().getTypeHandler(type, false, false);
                    if (typeHandler == null) {
                        continue;
                    }
                    typeHandler.getChildTypes(types);
                }
                String typeString;
                if (request.get(ARG_TYPE_EXCLUDE, false)) {
                    typeString = "!" + StringUtil.join(",!", types);
                } else {
                    typeString = StringUtil.join(",", types);
                }
                if ( !Clause.isColumn(clauses, Tables.ENTRIES.COL_TYPE)) {
                    addOrClause(Tables.ENTRIES.COL_TYPE, typeString, clauses);
                }
            }
        }


        if (isOrSearch(request)) {
            Clause clause = Clause.or(clauses);
            clauses = new ArrayList<Clause>();
            clauses.add(clause);
        }

        //The join
        if (didEntries) {
            for (String otherTableName : myTableNames) {
                if (seenTables.contains(otherTableName)
                        && !Tables.ENTRIES.NAME.equalsIgnoreCase(
                            otherTableName)) {
                    clauses.add(0, Clause.join(Tables.ENTRIES.COL_ID,
                            otherTableName + ".id"));
                }
            }
        }


        return getDatabaseManager().select(what, tables, Clause.and(clauses),
                                           extra,
                                           getRepository().getMax(request));

    }



    /**
     * _more_
     *
     * @param request _more_
     * @param sb _more_
     * @param entry _more_
     *
     * @throws Exception _more_
     */
    public void addToEntryForm(Request request, StringBuffer sb, Entry entry)
            throws Exception {
        addBasicToEntryForm(request, sb, entry);
        addSpecialToEntryForm(request, sb, entry);
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param sb _more_
     * @param entry _more_
     *
     * @throws Exception _more_
     */
    public void addSpecialToEntryForm(Request request, StringBuffer sb,
                                      Entry entry)
            throws Exception {
        if (parent != null) {
            parent.addSpecialToEntryForm(request, sb, entry);
            return;
        }
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param sb _more_
     * @param entry _more_
     *
     * @throws Exception _more_
     */
    public void addBasicToEntryForm(Request request, StringBuffer sb,
                                    Entry entry)
            throws Exception {


        

        String size = HtmlUtil.SIZE_70;

        boolean forUpload = (entry == null)
                            && getType().equals(TYPE_CONTRIBUTION);

        if (forUpload) {
            sb.append(
                      formEntry(request,
                    msgLabel("Your Name"),
                    HtmlUtil.input(ARG_CONTRIBUTION_FROMNAME, "", size)));
            sb.append(
                      formEntry(request,
                    msgLabel("Your Email"),
                    HtmlUtil.input(ARG_CONTRIBUTION_FROMEMAIL, "", size)));
        }



        if ( !forUpload && okToShowInForm(entry, ARG_NAME)) {
            sb.append(formEntry(request,
                                         msgLabel(getFormLabel(entry,ARG_NAME, "Name")),
                                         HtmlUtil.input(ARG_NAME,
                                             ((entry != null)
                    ? entry.getName()
                                              : getFormDefault(entry,ARG_NAME, "")), size)));
        } else {
            String nameDefault = getFormDefault(entry,ARG_NAME, null);
            if (nameDefault != null) {
                sb.append(HtmlUtil.hidden(ARG_NAME, nameDefault));
            }
        }

        if (okToShowInForm(entry, ARG_DESCRIPTION)) {
            String desc    = "";
            String buttons = "";
            int    rows    = getProperty(entry, "form.description.rows", 3);
            if (entry != null) {
                desc = entry.getDescription();
                if (desc.length() > 100) {
                    rows = rows * 2;
                }
                if (isWikiText(desc)) {
                    rows = 20;
                    buttons =
                        getRepository().getHtmlOutputHandler()
                            .makeWikiEditBar(request, entry,
                                             ARG_DESCRIPTION) + HtmlUtil.br();
                }
            }
            sb.append(
                      formEntryTop(request,
                                      msgLabel(getFormLabel(entry,ARG_DESCRIPTION,  "Description")),
                    buttons
                    + HtmlUtil.textArea(
                        ARG_DESCRIPTION, desc, rows, 
                        getProperty(entry, "form.description.columns", 60),
                        HtmlUtil.id(ARG_DESCRIPTION))));
        }

        if (request.getUser().getAdmin()) {
            sb.append(formEntry(request,msgLabel("User"),
                                         HtmlUtil.input(ARG_USER_ID,
                                             ((entry != null)
                    ? entry.getUser().getId()
                    : ""), HtmlUtil.SIZE_20)));
        }

        boolean showFile      = okToShowInForm(entry, ARG_FILE);
        boolean showLocalFile = showFile && request.getUser().getAdmin();
        boolean showUrl       = (forUpload
                                 ? false
                                 : okToShowInForm(entry,ARG_URL));
        boolean showResourceForm =  okToShowInForm(entry,ARG_RESOURCE);
        if (showResourceForm) {
            boolean showDownload  =okToShowInForm(entry,ARG_RESOURCE_DOWNLOAD);
            List<String> tabTitles  = new ArrayList<String>();
            List<String> tabContent = new ArrayList<String>();
            String urlLabel  = getFormLabel(entry,ARG_URL, "URL");
            String fileLabel = getFormLabel(entry,ARG_FILE, "File");
            if (showFile) {
                String formContent = HtmlUtil.fileInput(ARG_FILE, size);
                tabTitles.add(msg(fileLabel));
                tabContent.add(HtmlUtil.inset(formContent, 8));
            }
            if (showUrl) {
                String download = !showDownload
                                  ? ""
                                  : HtmlUtil.space(1)
                                    + HtmlUtil
                                        .checkbox(
                                            ARG_RESOURCE_DOWNLOAD) + HtmlUtil
                                                .space(1) + msg("Download");
                String formContent = HtmlUtil.input(ARG_URL, "", size)
                                     + BLANK + download;
                tabTitles.add(urlLabel);
                tabContent.add(HtmlUtil.inset(formContent, 8));
            }

            if (showLocalFile) {
                String formContent = HtmlUtil.input(ARG_LOCALFILE, "", size);
                tabTitles.add(msg("Local File"));
                tabContent.add(HtmlUtil.inset(formContent, 8));
            }

            String addMetadata = HtmlUtil.checkbox(ARG_METADATA_ADD)
                                 + HtmlUtil.space(1) + msg("Add properties")
                                 + HtmlUtil.space(1)
                                 + HtmlUtil.checkbox(ARG_METADATA_ADDSHORT)
                                 + HtmlUtil.space(1)
                                 + msg("Just spatial/temporal properties");

            List datePatterns = new ArrayList();
            datePatterns.add(new TwoFacedObject("", BLANK));
            for (int i = 0; i < DateUtil.DATE_PATTERNS.length; i++) {
                datePatterns.add(DateUtil.DATE_FORMATS[i]);
            }

            String unzipWidget =
                HtmlUtil.checkbox(ARG_FILE_UNZIP) + HtmlUtil.space(1)
                + msg("Unzip archive")
                + HtmlUtil.checkbox(ARG_FILE_PRESERVEDIRECTORY)
                + HtmlUtil.space(1) + msg("Make folders from archive");


            String datePatternWidget = msgLabel("Date pattern")
                                       + HtmlUtil.space(1)
                                       + HtmlUtil.select(ARG_DATE_PATTERN,
                                           datePatterns) + " ("
                                               + msg("Use file name") + ")";



            String extra = HtmlUtil.makeShowHideBlock(msg("More..."),
                               addMetadata + HtmlUtil.br() + unzipWidget
                               + HtmlUtil.br() + datePatternWidget, false);
            if (forUpload || !showDownload) {
                extra = "";
            }
            if ( !okToShowInForm(entry,"resource.extra")) {
                extra = "";
            }

            if (entry == null) {
                if (tabTitles.size() > 1) {
                    sb.append(formEntryTop(request, msgLabel("Resource"),
                            OutputHandler.makeTabs(tabTitles, tabContent, true,
                                "tab_content",
                                "tab_contents_noborder") + extra));
                } else if (tabTitles.size() == 1) {
                    sb.append(formEntry(request,tabTitles.get(0) + ":",
                            tabContent.get(0) + extra));
                }
            } else {
                //                if (entry.getResource().isFile()) {
                //If its the admin then show the full path
                if (request.getUser().getAdmin()) {
                    sb.append(formEntryTop(request,msgLabel("Resource"),
                            entry.getResource().getPath()));
                } else {
                    sb.append(formEntry(request,msgLabel("Resource"),
                            getStorageManager().getFileTail(entry)));
                }

                if (tabTitles.size() > 1) {
                    sb.append(formEntry(request,"",
                            msg("Upload new resource")));
                    sb.append(formEntryTop(request,msgLabel("Resource"),
                            OutputHandler.makeTabs(tabTitles, tabContent, true,
                                "tab_content",
                                "tab_contents_noborder") + extra));
                } else if (tabTitles.size() == 1) {
                    sb.append(formEntry(request,"",
                            msg("Upload new resource")));
                    sb.append(formEntry(request,tabTitles.get(0) + ":",
                            tabContent.get(0) + extra));
                }


                if (showFile) {
                    if (entry.getResource().isStoredFile()) {
                        String formContent = HtmlUtil.fileInput(ARG_FILE,
                                                 size);
                        /*                            sb.append(
                                  formEntry(request,
                                                     msgLabel("Upload new file"), formContent));
                        */
                    }
                }
                /*                } else {
                sb.append(formEntry(request,msgLabel("Resource"),
                        entry.getResource().getPath()));
                        }*/
            }



            if ( !hasDefaultDataType() && okToShowInForm(entry,ARG_DATATYPE)) {
                String selected = "";
                if (entry != null) {
                    selected = entry.getDataType();
                }
                List   types  = getRepository().getDefaultDataTypes();
                String widget = ((types.size() > 1)
                                 ? HtmlUtil.select(ARG_DATATYPE_SELECT,
                                     types, selected) + HtmlUtil.space(1)
                                         + msgLabel("Or")
                                 : "") + HtmlUtil.input(ARG_DATATYPE);
                sb.append(formEntry(request,msgLabel("Data Type"), widget));
            }

        }




        String dateHelp = " (e.g., 2007-12-11 00:00:00)";
        /*        String fromDate = ((entry != null)
                           ? formatDate(request,
                                        new Date(entry.getStartDate()))
                           : BLANK);
        String toDate = ((entry != null)
                         ? formatDate(request, new Date(entry.getEndDate()))
                         : BLANK);*/

        String timezone = ((entry == null)
                           ? null
                           : getEntryManager().getTimezone(entry));

        Date   fromDate = ((entry != null)
                           ? new Date(entry.getStartDate())
                           : null);
        Date   toDate   = ((entry != null)
                           ? new Date(entry.getEndDate())
                           : null);


        if (okToShowInForm(entry,ARG_DATE)) {
            if ( !okToShowInForm(entry,ARG_TODATE)) {
                sb.append(
                    formEntry(request,
                        "Date:",
                        getRepository().makeDateInput(
                            request, ARG_FROMDATE, "entryform", fromDate,
                            timezone)));

            } else {
                sb.append(
                    formEntry(request,
                                       msgLabel("Date Range"),
                        getRepository().makeDateInput(
                            request, ARG_FROMDATE, "entryform", fromDate,
                            timezone) + HtmlUtil.space(1)
                                      + HtmlUtil.img(iconUrl(ICON_RANGE))
                                      + HtmlUtil.space(1) +
                //                        " <b>--</b> " +
                getRepository().makeDateInput(request, ARG_TODATE,
                        "entryform", toDate, timezone) + HtmlUtil.space(2)));
            }

        }

        if (okToShowInForm(entry,ARG_LOCATION, false)) {
            String lat = "";
            String lon = "";
            if (entry != null) {
                if (entry.hasNorth()) {
                    lat = "" + entry.getNorth();
                }
                if (entry.hasWest()) {
                    lon = "" + entry.getWest();
                }
            }
            String locationWidget =
                msgLabel("Latitude") + " "
                + HtmlUtil.input(ARG_LOCATION_LATITUDE, lat, HtmlUtil.SIZE_6)
                + "  " + msgLabel("Longitude") + " "
                + HtmlUtil.input(ARG_LOCATION_LONGITUDE, lon,
                                 HtmlUtil.SIZE_6);

            sb.append(formEntry(request,msgLabel("Location"), locationWidget));
        } else if (okToShowInForm(entry,ARG_AREA)) {
            StringBuffer mapSB = new StringBuffer();
            MapOutputHandler mapOutputHandler =
                (MapOutputHandler) getRepository().getOutputHandler(
                    MapOutputHandler.OUTPUT_MAP.getId());
            if (mapOutputHandler != null) {
                List<Entry> entries = new ArrayList<Entry>();
                if (entry != null) {
                    entries.add(entry);
                }
                //                mapOutputHandler.getMap( request, entries,mapSB, 300,200,false);
            }
            String[] pts = null;
            if (entry != null) {
                pts = new String[] { entry.hasSouth()
                                     ? "" + entry.getSouth()
                                     : "", entry.hasNorth()
                                           ? "" + entry.getNorth()
                                           : "", entry.hasEast()
                        ? "" + entry.getEast()
                        : "", entry.hasWest()
                              ? "" + entry.getWest()
                              : "" };
            }
            String extraMapStuff = "";
            if ((entry != null) && entry.isGroup()) {
                extraMapStuff =
                    HtmlUtil.br()
                    + HtmlUtil.checkbox(ARG_SETBOUNDSFROMCHILDREN, "true",
                                        false) + " "
                                            + msg("Set bounds from children");
            }

            String mapSelector =
                getRepository().getMapManager().makeMapSelector(ARG_AREA,
                                                                true, "","", pts, null) + extraMapStuff;

            sb.append(formEntry(request,msgLabel("Location"), mapSelector));

        }



        if (okToShowInForm(entry,ARG_ALTITUDE, false)) {
            String altitude = "";
            if ((entry != null) && entry.hasAltitude()) {
                altitude = "" + entry.getAltitude();
            }
            sb.append(formEntry(request,"Altitude:",
                                         HtmlUtil.input(ARG_ALTITUDE,
                                             altitude, HtmlUtil.SIZE_10)));
        } else if (okToShowInForm(entry,ARG_ALTITUDE_TOP, false)) {
            String altitudeTop    = "";
            String altitudeBottom = "";
            if (entry != null) {
                if (entry.hasAltitudeTop()) {
                    altitudeTop = "" + entry.getAltitudeTop();
                }
                if (entry.hasAltitudeBottom()) {
                    altitudeBottom = "" + entry.getAltitudeBottom();
                }
            }
            sb.append(
                formEntry(request,
                    "Altitude Range:",
                    HtmlUtil.input(
                        ARG_ALTITUDE_BOTTOM, altitudeBottom,
                        HtmlUtil.SIZE_10) + " - "
                                          + HtmlUtil.input(
                                              ARG_ALTITUDE_TOP, altitudeTop,
                                                  HtmlUtil.SIZE_10) + " "
                                                      + msg("meters")));
        }


        if (entry == null) {
            for (String metadataId : requiredMetadata) {
                for (MetadataHandler handler :
                        getMetadataManager().getMetadataHandlers()) {
                    if (handler.canHandle(metadataId)) {
                        handler.makeAddForm(request, null,
                                            handler.findType(metadataId), sb);
                        break;
                    }
                }
            }
        }


    }



    /**
     * _more_
     *
     *
     * @param request _more_
     * @param entry _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public String getIconUrl(Request request, Entry entry) throws Exception {
        if (entry.isGroup()) {
            if (getAccessManager().hasPermissionSet(entry,
                    Permission.ACTION_VIEWCHILDREN)) {
                if ( !getAccessManager().canDoAction(request, entry,
                        Permission.ACTION_VIEWCHILDREN)) {
                    return iconUrl(ICON_FOLDER_CLOSED_LOCKED);
                }
            }
            return iconUrl(ICON_FOLDER_CLOSED);
        }
        Resource resource = entry.getResource();
        String   path     = resource.getPath();
        return getIconUrlFromPath(path);
    }


    public String getIconUrlFromPath(String path) throws Exception {
        String img = ICON_FILE;
        if (path != null) {
            String suffix = IOUtil.getFileExtension(path.toLowerCase());
            String prop   = getRepository().getProperty("icon" + suffix);
            if (prop != null) {
                img = prop;
            }
        }
        return iconUrl(img);
    }




    /**
     * _more_
     *
     * @param request _more_
     * @param sb _more_
     */
    public void addTextSearch(Request request, StringBuffer sb) {
        String name           = (String) request.getString(ARG_TEXT, "");
        String searchMetaData = " ";
        /*HtmlUtil.checkbox(ARG_SEARCHMETADATA,
                                    "true",
                                    request.get(ARG_SEARCHMETADATA,
                                    false)) + " "
                                    + msg("Search metadata");*/

        String searchExact = " "
                             + HtmlUtil.checkbox(ARG_EXACT, "true",
                                 request.get(ARG_EXACT, false)) + " "
                                     + msg("Match exactly");
        String extra = HtmlUtil.p() + searchExact + searchMetaData;
        extra = HtmlUtil.makeToggleInline(msg("More..."), extra, false);
        sb.append(formEntryTop(request,msgLabel("Text"),
                                        HtmlUtil.input(ARG_TEXT, name,
                                            HtmlUtil.SIZE_50
                                            + " autofocus ") + " " + extra));
    }


    /**
     * _more_
     *
     * @param formBuffer _more_
     * @param request _more_
     * @param where _more_
     * @param advancedForm _more_
     *
     * @throws Exception _more_
     */
    public void addToSearchForm(Request request, StringBuffer formBuffer,
                                List<Clause> where, boolean advancedForm)
            throws Exception {

        if (parent != null) {
            parent.addToSearchForm(request, formBuffer, where, advancedForm);
            return;
        }

        if (request.defined(ARG_TYPE)) {
            TypeHandler typeHandler = getRepository().getTypeHandler(request);
            if ( !typeHandler.isAnyHandler()) {
                //                typeHandlers.clear();
                //                typeHandlers.add(typeHandler);
            }
        }


        /*
        if(minDate==null || maxDate == null) {
            Statement stmt = select(request,
                                           SqlUtil.comma(
                                                         SqlUtil.min(Tables.ENTRIES.COL_FROMDATE),
                                                         SqlUtil.max(
                                                                     Tables.ENTRIES.COL_TODATE)), where);

            ResultSet dateResults = stmt.getResultSet();
            if (dateResults.next()) {
                if (dateResults.getDate(1) != null) {
                    if(minDate == null)
                        minDate = SqlUtil.getDateString("" + dateResults.getDate(1));
                    if(maxDate == null)
                        maxDate = SqlUtil.getDateString("" + dateResults.getDate(2));
                }
            }
            }
*/

        //        minDate = "";
        //        maxDate = "";


        StringBuffer basicSB    = new StringBuffer(HtmlUtil.formTable());
        StringBuffer advancedSB = new StringBuffer(HtmlUtil.formTable());


        addTextSearch(request, basicSB);
        if (request.defined(ARG_USER_ID)) {
            basicSB.append(formEntry(request,msgLabel("User"),
                    HtmlUtil.input(ARG_USER_ID,
                                   request.getString(ARG_USER_ID, ""))));
        }


        List<TypeHandler> typeHandlers = getRepository().getTypeHandlers();
        if (true || (typeHandlers.size() > 1)) {
            List tmp = new ArrayList();
            for (TypeHandler typeHandler : typeHandlers) {
                if(!typeHandler.getForUser()) {
                    continue;
                }
                tmp.add(new TwoFacedObject(typeHandler.getLabel(),
                                           typeHandler.getType()));
            }
            TwoFacedObject anyTfo = new TwoFacedObject(TYPE_ANY, TYPE_ANY);
            if ( !tmp.contains(anyTfo)) {
                tmp.add(0, anyTfo);
            }
            List typeList = request.get(ARG_TYPE, new ArrayList());
            typeList.remove(TYPE_ANY);

            String typeSelect = HtmlUtil.select(ARG_TYPE, tmp, typeList,
                                    (advancedForm
                                     ? " MULTIPLE SIZE=4 "
                                     : ""));
            String groupCbx = (advancedForm
                               ? HtmlUtil.checkbox(ARG_TYPE_EXCLUDE, "true",
                                   request.get(ARG_TYPE_EXCLUDE,
                                       false)) + HtmlUtil.space(1)
                                           + msg("Exclude")
                               : "");
            basicSB.append(
                formEntry(request,
                    msgLabel("Type"),
                    typeSelect + HtmlUtil.space(1)
                    + HtmlUtil.submitImage(
                        getRepository().iconUrl(ICON_SEARCH), "submit_type",
                        msg(
                        "Show search form with this type")) + HtmlUtil.space(
                            1) + groupCbx));
        } else if (typeHandlers.size() == 1) { 
           basicSB.append(HtmlUtil.hidden(ARG_TYPE,
                                           typeHandlers.get(0).getType()));
            basicSB.append(formEntry(request,msgLabel("Type"),
                    typeHandlers.get(0).getDescription()));
        }



        /**
         * List<Entry> collectionGroups =
         *   getEntryManager().getTopGroups(request);
         * List<TwoFacedObject> collections = new ArrayList<TwoFacedObject>();
         * collections.add(new TwoFacedObject("All", ""));
         * for (Entry group : collectionGroups) {
         *   collections.add(new TwoFacedObject(group.getLabel(),
         *           group.getId()));
         *
         * }
         *
         *
         * Entry collection = request.getCollectionEntry();
         * String collectionSelect = HtmlUtil.select(ARG_COLLECTION,
         *                             collections, ((collection != null)
         *       ? collection.getId()
         *       : null), 100);
         *
         * if (collection == null) {
         *   basicSB.append(formEntry(request,msgLabel("Collection"),
         *           collectionSelect));
         * }
         */



        List dateTypes = new ArrayList();
        dateTypes.add(new TwoFacedObject(msg("Overlaps range"),
                                         DATE_SEARCHMODE_OVERLAPS));
        dateTypes.add(new TwoFacedObject(msg("Contained by range"),
                                         DATE_SEARCHMODE_CONTAINEDBY));
        dateTypes.add(new TwoFacedObject(msg("Contains range"),
                                         DATE_SEARCHMODE_CONTAINS));


        List dateSelect = new ArrayList();
        dateSelect.add(new TwoFacedObject(msg("---"), "none"));
        dateSelect.add(new TwoFacedObject(msg("Last hour"), "-1 hour"));
        dateSelect.add(new TwoFacedObject(msg("Last 3 hours"), "-3 hours"));
        dateSelect.add(new TwoFacedObject(msg("Last 6 hours"), "-6 hours"));
        dateSelect.add(new TwoFacedObject(msg("Last 12 hours"), "-12 hours"));
        dateSelect.add(new TwoFacedObject(msg("Last day"), "-1 day"));
        dateSelect.add(new TwoFacedObject(msg("Last 7 days"), "-7 days"));



        String dateSelectValue;
        for(Constants.DateArg arg: Constants.DATEARGS) {
            if (request.exists(arg.relative)) {
                dateSelectValue = request.getString(arg.relative, "");
            } else {
                dateSelectValue = "none";
            }
            
            String dateSelectInput = HtmlUtil.select(arg.relative,
                                                     dateSelect, dateSelectValue);
            String minDate = request.getDateSelect(arg.from, (String) null);
            String maxDate = request.getDateSelect(arg.to, (String) null);
            //        request.remove(arg.from);
            //        request.remove(arg.to);
            //        List<TypeHandler> typeHandlers =
            //            getRepository().getTypeHandlers(request);


            String dateTypeValue = request.getString(arg.mode,
                                                     DATE_SEARCHMODE_DEFAULT);
            String dateTypeInput = HtmlUtil.select(arg.mode,
                                                   dateTypes, dateTypeValue);

            String noDataMode = request.getString(ARG_DATE_NODATAMODE, "");
            String noDateInput = HtmlUtil.checkbox(ARG_DATE_NODATAMODE,
                                                   VALUE_NODATAMODE_INCLUDE,
                                                   noDataMode.equals(VALUE_NODATAMODE_INCLUDE));
            String dateExtra;
            if(arg.hasRange) {
                dateExtra = HtmlUtil.space(4)
                    + HtmlUtil.makeToggleInline(msg("More..."),
                                                HtmlUtil.p()
                                                + HtmlUtil.formTable(new String[] {
                                                        msgLabel("Search for data whose time is"), dateTypeInput,
                                                        msgLabel("Or search relative"), dateSelectInput, "",
                                                        noDateInput + HtmlUtil.space(1)
                                                        + msg("Include entries with no data times")
                                                    }), false);
            } else {
                dateExtra = HtmlUtil.space(4)
                    + HtmlUtil.makeToggleInline(msg("More..."),
                                                HtmlUtil.p()
                                                + HtmlUtil.formTable(new String[] {
                                                        msgLabel("Or search relative"), dateSelectInput}), false);


            }

            basicSB.append(
                           formEntryTop(request,
                                                 msgLabel(arg.label),
                                                 getRepository().makeDateInput(
                                                                               request, arg.from, "searchform",
                                                                               null) + HtmlUtil.space(1)
                                                 + HtmlUtil.img(getRepository().iconUrl(ICON_RANGE))
                                                 + HtmlUtil.space(1)
                                                 + getRepository().makeDateInput(
                                                                                 request, arg.to, "searchform",
                                                                                 null) + dateExtra));


        }


        if (advancedForm || request.defined(ARG_GROUP)) {
            String groupArg = (String) request.getString(ARG_GROUP, "");
            String searchChildren = " "
                                    + HtmlUtil.checkbox(ARG_GROUP_CHILDREN,
                                        "true",
                                        request.get(ARG_GROUP_CHILDREN,
                                            false)) + " ("
                                                + msg("Search sub-folders")
                                                + ")";
            if (groupArg.length() > 0) {
                basicSB.append(HtmlUtil.hidden(ARG_GROUP, groupArg));
                Entry group = getEntryManager().findGroup(request, groupArg);
                if (group != null) {
                    basicSB.append(formEntry(request,msgLabel("Folder"),
                            group.getFullName() + "&nbsp;" + searchChildren));

                }
            } else {

                /**
                 * Statement stmt =
                 *   select(request,
                 *          SqlUtil.distinct(Tables.ENTRIES.COL_PARENT_GROUP_ID),
                 *          where, "");
                 *
                 * List<Entry> groups =
                 *   getRepository().getGroups(SqlUtil.readString(stmt, 1));
                 * getDatabaseManager().closeAndReleaseStatement(stmt);
                 *
                 * if (groups.size() > 1) {
                 *   List groupList = new ArrayList();
                 *   groupList.add(ALL_OBJECT);
                 *   for (Entry group : groups) {
                 *       groupList.add(
                 *           new TwoFacedObject(group.getFullName(), group.getId()));
                 *   }
                 *   String groupSelect = HtmlUtil.select(ARG_GROUP,
                 *                            groupList, null, 100);
                 *   advancedSB.append(formEntry(request,msgLabel("Folder"),
                 *           groupSelect + searchChildren));
                 * } else if (groups.size() == 1) {
                 *   advancedSB.append(HtmlUtil.hidden(ARG_GROUP,
                 *           groups.get(0).getId()));
                 *   advancedSB.append(formEntry(request,msgLabel("Folder"),
                 *           groups.get(0).getFullName() + searchChildren));
                 * }
                 */
            }
            advancedSB.append("\n");
        }


        if (advancedForm) {
            String nonGeo =
                HtmlUtil.checkbox(ARG_INCLUDENONGEO, "true",
                                  request.get(ARG_INCLUDENONGEO,
                                      true)) + " Include non-geographic";


            String radio = HtmlUtil.radio(
                               ARG_AREA_MODE, VALUE_AREA_OVERLAPS,
                               request.getString(
                                   ARG_AREA_MODE, VALUE_AREA_OVERLAPS).equals(
                                   VALUE_AREA_OVERLAPS)) + msg("Overlaps")
                                       + HtmlUtil.space(3)
                                       + HtmlUtil.radio(
                                           ARG_AREA_MODE,
                                           VALUE_AREA_CONTAINS,
                                           request.getString(
                                               ARG_AREA_MODE,
                                               VALUE_AREA_OVERLAPS).equals(
                                                   VALUE_AREA_CONTAINS)) + msg(
                                                       "Contained by");

            String mapSelector =
                getRepository().getMapManager().makeMapSelector(request,
                    ARG_AREA, true, "", radio);

            basicSB.append(formEntry(request,msgLabel("Area"), mapSelector));
            basicSB.append("\n");

            addSearchField(request, ARG_FILESUFFIX, basicSB);

        }




        /*
        if (collection != null) {
            basicSB.append(formEntry(request,msgLabel("Collection"),
                    collectionSelect));
                    }*/


        basicSB.append(HtmlUtil.formTableClose());
        advancedSB.append(HtmlUtil.formTableClose());


        formBuffer.append(HtmlUtil.makeShowHideBlock(msg("Basic"),
                basicSB.toString(), true));
        //        formBuffer.append(HtmlUtil.makeShowHideBlock(msg("Advanced"),
        //                advancedSB.toString(), false));

    }


    /**
     * _more_
     *
     * @param request _more_
     * @param what _more_
     * @param sb _more_
     */
    public void addSearchField(Request request, String what,
                               StringBuffer sb) {
        if (what.equals(ARG_FILESUFFIX)) {
            sb.append(formEntry(request,msgLabel("File Suffix"),
                                         HtmlUtil.input(ARG_FILESUFFIX, "",
                                             " size=\"8\" ")));
        }
    }




    /**
     * _more_
     *
     * @return _more_
     */
    public boolean isAnyHandler() {
        return getType().equals(TypeHandler.TYPE_ANY);
    }





    /**
     * _more_
     *
     * @param request _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public List<Clause> assembleWhereClause(Request request)
            throws Exception {
        return assembleWhereClause(request, new StringBuffer());
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param searchCriteria _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public List<Clause> assembleWhereClause(Request request,
                                            StringBuffer searchCriteria)
            throws Exception {

        if (parent != null) {
            return parent.assembleWhereClause(request, searchCriteria);
        }

        List<Clause> where    = new ArrayList<Clause>();
        List         typeList = request.get(ARG_TYPE, new ArrayList());
        typeList.remove(TYPE_ANY);
        if (typeList.size() > 0) {
            if (request.get(ARG_TYPE_EXCLUDE, false)) {
                addCriteria(request, searchCriteria, "Entry Type!=",
                            StringUtil.join(",", typeList));
            } else {
                addCriteria(request, searchCriteria, "Entry Type=",
                            StringUtil.join(",", typeList));
            }
        }

        if (request.defined(ARG_RESOURCE)) {
            addCriteria(request, searchCriteria, "Resource=",
                        request.getString(ARG_RESOURCE, ""));
            String resource = request.getString(ARG_RESOURCE, "");
            resource = getStorageManager().resourceFromDB(resource);
            addOrClause(Tables.ENTRIES.COL_RESOURCE, resource, where);
        }

        if (request.defined(ARG_DATATYPE)) {
            addCriteria(request, searchCriteria, "Datatype=",
                        request.getString(ARG_DATATYPE, ""));
            addOrClause(Tables.ENTRIES.COL_DATATYPE,
                        request.getString(ARG_DATATYPE, ""), where);
        }

        if (request.defined(ARG_USER_ID)) {
            addCriteria(request, searchCriteria, "User=",
                        request.getString(ARG_USER_ID, ""));
            addOrClause(Tables.ENTRIES.COL_USER_ID,
                        request.getString(ARG_USER_ID, ""), where);
        }

        /**
         * if (request.defined(ARG_COLLECTION)) {
         *   Entry collectionEntry = getEntryManager().getEntry(request,
         *                               request.getString(ARG_COLLECTION,
         *                                   ""));
         *   if (collectionEntry != null) {
         *       addCriteria(request,searchCriteria, "Collection=",
         *                   collectionEntry.getName());
         *   } else {
         *       addCriteria(request,searchCriteria, "Collection=", "Unknown");
         *   }
         *   addOrClause(Tables.ENTRIES.COL_TOP_GROUP_ID,
         *               request.getString(ARG_COLLECTION, ""), where);
         *               }
         */

        if (request.defined(ARG_FILESUFFIX)) {
            addCriteria(request, searchCriteria, "File Suffix=",
                        request.getString(ARG_FILESUFFIX, ""));
            List<Clause> clauses = new ArrayList<Clause>();
            for (String tok :
                    (List<String>) StringUtil.split(
                        request.getString(ARG_FILESUFFIX, ""), ",", true,
                        true)) {
                clauses.add(Clause.like(Tables.ENTRIES.COL_RESOURCE,
                                        "%" + tok));
            }
            if (clauses.size() == 1) {
                where.add(clauses.get(0));
            } else {
                where.add(Clause.or(clauses));
            }
        }

        if (request.defined(ARG_GROUP)) {
            String  groupId = (String) request.getString(ARG_GROUP,
                                  "").trim();
            boolean doNot   = groupId.startsWith("!");
            if (doNot) {
                groupId = groupId.substring(1);
            }
            if (groupId.endsWith("%")) {
                Entry group = getEntryManager().findGroup(request,
                                                          groupId.substring(0, groupId.length() - 1));
                if (group != null) {
                    addCriteria(request, searchCriteria, "Folder=",
                                group.getName());
                }
                where.add(Clause.like(Tables.ENTRIES.COL_PARENT_GROUP_ID,
                                      groupId));
            } else {
                Entry group = getEntryManager().findGroup(request);
                if (group == null) {
                    throw new IllegalArgumentException(
                        msgLabel("Could not find folder") + groupId);
                }
                addCriteria(request, searchCriteria, "Folder" + (doNot
                        ? "!="
                        : "="), group.getName());
                String searchChildren =
                    (String) request.getString(ARG_GROUP_CHILDREN,
                        (String) null);
                if (Misc.equals(searchChildren, "true")) {
                    Clause sub = (doNot
                                  ? Clause.notLike(
                                      Tables.ENTRIES.COL_PARENT_GROUP_ID,
                                      group.getId() + Entry.IDDELIMITER + "%")
                                  : Clause.like(
                                      Tables.ENTRIES.COL_PARENT_GROUP_ID,
                                      group.getId() + Entry.IDDELIMITER
                                      + "%"));
                    Clause equals = (doNot
                                     ? Clause.neq(
                                         Tables.ENTRIES.COL_PARENT_GROUP_ID,
                                         group.getId())
                                     : Clause.eq(
                                         Tables.ENTRIES.COL_PARENT_GROUP_ID,
                                         group.getId()));
                    where.add(Clause.or(sub, equals));
                } else {
                    if (doNot) {
                        where.add(
                            Clause.neq(
                                Tables.ENTRIES.COL_PARENT_GROUP_ID,
                                group.getId()));
                    } else {
                        where.add(
                            Clause.eq(
                                Tables.ENTRIES.COL_PARENT_GROUP_ID,
                                group.getId()));
                    }
                }
            }
        }


        List<Clause> dateClauses = new ArrayList<Clause>();
        for(Constants.DateArg arg: Constants.DATEARGS) {
            Date[] dateRange = request.getDateRange(arg.from, arg.to, arg.relative,
                                                    new Date());
            if ((dateRange[0] != null) || (dateRange[1] != null)) {
                Date date1 = dateRange[0];
                Date date2 = dateRange[1];
                if (date1 == null) {
                    date1 = date2;
                }
                if (date2 == null) {
                    date2 = date1;
                }

                if(arg.equals(Constants.createDate) || arg.equals(Constants.changeDate)) {
                    String column = arg.equals(Constants.createDate)?Tables.ENTRIES.COL_CREATEDATE:
                        Tables.ENTRIES.COL_CHANGEDATE;
                    if (date1 != null) {
                        addCriteria(request, searchCriteria, msg(arg.label) +">=",
                                    date1);
                        dateClauses.add(Clause.ge(column, date1));
                    }
                    if (date2 != null) {
                        addCriteria(request, searchCriteria, msg(arg.label)+"<=",
                                    date2);
                        dateClauses.add(Clause.le(column, date2));
                    }
                    continue;
                }


                String dateSearchMode = request.getString(arg.mode,
                                                          DATE_SEARCHMODE_DEFAULT);
                if (dateSearchMode.equals(DATE_SEARCHMODE_OVERLAPS)) {
                    addCriteria(request, searchCriteria, "To&nbsp;Date&gt;=",
                                date1);
                    addCriteria(request, searchCriteria, "From&nbsp;Date&lt;=",
                                date2);
                    dateClauses.add(Clause.le(Tables.ENTRIES.COL_FROMDATE,
                                              date2));
                    dateClauses.add(Clause.ge(Tables.ENTRIES.COL_TODATE, date1));
                } else if (dateSearchMode.equals(DATE_SEARCHMODE_CONTAINEDBY)) {
                    addCriteria(request, searchCriteria, "From&nbsp;Date&gt;=",
                                date1);
                    addCriteria(request, searchCriteria, "To&nbsp;Date&lt;=",
                                date2);
                    dateClauses.add(Clause.ge(Tables.ENTRIES.COL_FROMDATE,
                                              date1));
                    dateClauses.add(Clause.le(Tables.ENTRIES.COL_TODATE, date2));
                } else {
                    //DATE_SEARCHMODE_CONTAINS
                    addCriteria(request, searchCriteria, "From&nbsp;Date&lt;=",
                                date1);
                    addCriteria(request, searchCriteria, "To&nbsp;Date&gt;=",
                                date2);
                    dateClauses.add(Clause.le(Tables.ENTRIES.COL_FROMDATE,
                                              date1));
                    dateClauses.add(Clause.ge(Tables.ENTRIES.COL_TODATE, date2));
                }
            }


        String noDataMode = request.getString(ARG_DATE_NODATAMODE, "");
        if (noDataMode.equals(VALUE_NODATAMODE_INCLUDE)
                && (dateClauses.size() > 0)) {
            Clause dateClause = Clause.and(dateClauses);
            dateClauses = new ArrayList<Clause>();
            Clause allEqualClause =
                Clause.and(
                    Clause.join(
                        Tables.ENTRIES.COL_CREATEDATE,
                        Tables.ENTRIES.COL_FROMDATE), Clause.join(
                            Tables.ENTRIES.COL_FROMDATE,
                            Tables.ENTRIES.COL_TODATE));

            dateClauses.add(allEqualClause);
            dateClauses.add(Clause.or(dateClause, allEqualClause));
            addCriteria(request, searchCriteria, "Include no data times", "");
        }


        }






        if (dateClauses.size() > 1) {
            where.add(Clause.and(dateClauses));
        } else if (dateClauses.size() == 1) {
            where.add(dateClauses.get(0));
        }

        boolean includeNonGeo = request.get(ARG_INCLUDENONGEO, false);
        boolean contains = !(request.getString(
                               ARG_AREA_MODE, VALUE_AREA_OVERLAPS).equals(
                               VALUE_AREA_OVERLAPS));


        List<Clause> areaExpressions = new ArrayList<Clause>();
        String[]     areaNames       = { "South", "North", "East", "West" };
        String[]     areaSuffixes = { "_south", "_north", "_east", "_west" };
        String[] areaCols = { Tables.ENTRIES.COL_SOUTH,
                              Tables.ENTRIES.COL_NORTH,
                              Tables.ENTRIES.COL_EAST,
                              Tables.ENTRIES.COL_WEST };
        boolean[] areaLE = { false, true, true, false };
        Clause    areaClause;
        if ( !contains) {
            boolean gotThemAll = true;
            for (int i = 0; i < 4; i++) {
                String areaArg = ARG_AREA + areaSuffixes[i];
                if ( !request.defined(areaArg)) {
                    gotThemAll = false;
                    break;
                }
            }
            if (gotThemAll) {
                areaClause = Clause.le(Tables.ENTRIES.COL_SOUTH,
                                       request.get(ARG_AREA_NORTH, 0.0));
                areaExpressions.add(
                    Clause.and(
                        Clause.neq(
                            Tables.ENTRIES.COL_SOUTH,
                            new Double(Entry.NONGEO)), areaClause));
                areaClause = Clause.ge(Tables.ENTRIES.COL_NORTH,
                                       request.get(ARG_AREA_SOUTH, 0.0));
                areaExpressions.add(
                    Clause.and(
                        Clause.neq(
                            Tables.ENTRIES.COL_SOUTH,
                            new Double(Entry.NONGEO)), areaClause));

                areaClause = Clause.ge(Tables.ENTRIES.COL_EAST,
                                       request.get(ARG_AREA_WEST, 0.0));
                areaExpressions.add(
                    Clause.and(
                        Clause.neq(
                            Tables.ENTRIES.COL_EAST,
                            new Double(Entry.NONGEO)), areaClause));

                areaClause = Clause.le(Tables.ENTRIES.COL_WEST,
                                       request.get(ARG_AREA_EAST, 0.0));
                areaExpressions.add(
                    Clause.and(
                        Clause.neq(
                            Tables.ENTRIES.COL_WEST,
                            new Double(Entry.NONGEO)), areaClause));
                //                System.err.println (areaExpressions);
            }

        } else {
            for (int i = 0; i < 4; i++) {
                String areaArg = ARG_AREA + areaSuffixes[i];
                if (request.defined(areaArg)) {
                    addCriteria(request, searchCriteria,
                                areaNames[i] + (areaLE[i]
                            ? "<="
                            : ">="), request.getString(areaArg, ""));
                    double areaValue = request.get(areaArg, 0.0);
                    areaClause = areaLE[i]
                                 ? Clause.le(areaCols[i], areaValue)
                                 : Clause.ge(areaCols[i], areaValue);
                    areaExpressions.add(Clause.and(Clause.neq(areaCols[i],
                            new Double(Entry.NONGEO)), areaClause));
                }
            }
        }

        if (areaExpressions.size() > 0) {
            Clause areaExpr = Clause.and(areaExpressions);
            if (includeNonGeo) {
                areaExpr = Clause.or(areaExpr,
                                     Clause.eq(Tables.ENTRIES.COL_SOUTH,
                                         new Double(Entry.NONGEO)));
            }
            where.add(areaExpr);
            //            System.err.println (areaExpr);
        }


        Hashtable args           = request.getArgs();
        String    metadataPrefix = ARG_METADATA_ATTR1 + ".";
        Hashtable<String,List<Metadata>> typeMap        = new Hashtable<String,List<Metadata>>();
        List<String>      types          = new ArrayList<String>();
        for (Enumeration keys = args.keys(); keys.hasMoreElements(); ) {
            String arg = (String) keys.nextElement();
            if (!arg.startsWith(metadataPrefix)) {
                continue;
            }
            if ( !request.defined(arg)) {
                continue;
            }
            String type = arg.substring(ARG_METADATA_ATTR1.length()+1);
            List[] urlArgs = new List[]{
                request.get(ARG_METADATA_ATTR1 + "." + type, new ArrayList<String>()),
                request.get(ARG_METADATA_ATTR2 + "." + type, new ArrayList<String>()),
                request.get(ARG_METADATA_ATTR3 + "." + type, new ArrayList<String>()),
                request.get(ARG_METADATA_ATTR4 + "." + type, new ArrayList<String>())
            };

            int index=0;
            while(true) {
                boolean ok = false;
                String[] valueArray = {"","","",""};
                for(int valueIdx=0;valueIdx<urlArgs.length;valueIdx++) {
                    if(index<urlArgs[valueIdx].size()) {
                        ok = true;
                        valueArray[valueIdx] = (String)urlArgs[valueIdx].get(index);
                    }
                }
                if(!ok) break;
                index++;

                Metadata metadata =
                    new Metadata(
                                 type,
                                 valueArray[0],
                                 valueArray[1],
                                 valueArray[2],
                                 valueArray[3],
                                 "");


                metadata.setInherited(request.get(ARG_METADATA_INHERITED + "."
                                                  + type, false));
                List<Metadata> values = typeMap.get(type);
                if (values == null) {
                    typeMap.put(type, values = new ArrayList<Metadata>());
                    types.add(type);
                }
                values.add(metadata);
            }
        }



        List<Clause> metadataAnds = new ArrayList<Clause>();
        for (int typeIdx = 0; typeIdx < types.size(); typeIdx++) {
            String       type        =  types.get(typeIdx);
            List<Metadata>         values      = typeMap.get(type);
            List<Clause> attrOrs = new ArrayList<Clause>();
            String       subTable    = Tables.METADATA.NAME + "_" + typeIdx;
            for (Metadata     metadata: values) {
                String tmp = "";
                List<Clause> attrAnds = new ArrayList<Clause>();
                for (int attrIdx = 1; attrIdx <= 4; attrIdx++) {
                    String attr = metadata.getAttr(attrIdx);
                    if (attr.trim().length() > 0) {
                        attrAnds.add(Clause.eq(subTable + ".attr"
                                + attrIdx, attr));
                        tmp = tmp + ((tmp.length() == 0)
                                     ? ""
                                     : " &amp; ") + attr;
                    }
                }

                Clause attrClause = Clause.and(attrAnds);
                attrOrs.add(attrClause);
                MetadataHandler handler =
                    getRepository().getMetadataManager().findMetadataHandler(
                        type);
                MetadataType metadataType = handler.findType(type);
                if (metadataType != null) {
                    addCriteria(request, searchCriteria,
                                metadataType.getLabel() + "=", tmp);
                }
            }

            List<Clause> subClauses = new ArrayList<Clause>();
            subClauses.add(Clause.join(subTable + ".entry_id",
                                       Tables.ENTRIES.COL_ID));
            subClauses.add(Clause.eq(subTable + ".type", type));
            subClauses.add(Clause.or(attrOrs));
            metadataAnds.add(Clause.and(subClauses));
        }

        if (metadataAnds.size() > 0) {
            if (isOrSearch(request)) {
                where.add(Clause.or(metadataAnds));
                //                System.err.println ("metadata:" +Clause.or(metadataAnds));
            } else {
                where.add(Clause.and(metadataAnds));
                //                System.err.println ("metadata:" +Clause.and(metadataAnds));
            }
        }


        String textToSearch = (String) request.getString(ARG_TEXT, "").trim();

        //A hook to allow the database manager do its own text search based on the dbms type
        if (textToSearch.length() > 0) {
            getDatabaseManager().addTextSearch( request, this,  textToSearch,  searchCriteria, where);
        }            
        return where;
    }


    public void addTextSearch(Request request, String textToSearch, StringBuffer searchCriteria, List<Clause> where) throws Exception {
            List<Clause> textOrs = new ArrayList<Clause>();
            for (String textTok :
                    (List<String>) StringUtil.split(textToSearch, ",", true,
                        true)) {
                List<String> nameToks = StringUtil.splitWithQuotes(textTok);
                boolean      doLike   = false;
                if ( !request.get(ARG_EXACT, false)) {
                    addCriteria(request, searchCriteria, "Text like",
                                textTok);
                    List tmp = StringUtil.split(textTok, ",", true, true);
                    textTok = "%" + StringUtil.join("%,%", tmp) + "%";
                    doLike  = true;
                } else {
                    addCriteria(request, searchCriteria, "Text =",
                                textToSearch);
                }
                //            System.err.println (doLike +" toks:" + nameToks);
                List<Clause> ands = new ArrayList<Clause>();
                boolean searchMetadata = request.get(ARG_SEARCHMETADATA,
                                             false);
                searchMetadata = false;
                String[] attrCols = { Tables.METADATA.COL_ATTR1  /*,
                                        Tables.METADATA.COL_ATTR2,
                                        Tables.METADATA.COL_ATTR3,
                                        Tables.METADATA.COL_ATTR4*/
                };
                for (String nameTok : nameToks) {
                    boolean doNot = nameTok.startsWith("!");
                    if (doNot) {
                        nameTok = nameTok.substring(1);
                    }


                    if (doLike) {
                        nameTok = "%" + nameTok + "%";
                    }
                    List<Clause> ors = new ArrayList<Clause>();
                    if (searchMetadata) {
                        List<Clause> metadataOrs = new ArrayList<Clause>();
                        for (String attrCol : attrCols) {
                            if (doLike) {
                                metadataOrs.add(Clause.like(attrCol, nameTok,
                                        doNot));
                            } else {
                                metadataOrs.add(Clause.eq(attrCol, nameTok,
                                        doNot));
                            }
                        }
                        ors.add(
                            Clause.and(
                                Clause.or(metadataOrs),
                                Clause.join(
                                    Tables.METADATA.COL_ENTRY_ID,
                                    Tables.ENTRIES.COL_ID)));
                    }
                    if (doLike) {
                        ors.add(Clause.like(Tables.ENTRIES.COL_NAME, nameTok,
                                            doNot));
                        ors.add(Clause.like(Tables.ENTRIES.COL_DESCRIPTION,
                                            nameTok, doNot));
                        ors.add(Clause.like(Tables.ENTRIES.COL_RESOURCE,
                                            nameTok, doNot));
                    } else {
                        ors.add(Clause.eq(Tables.ENTRIES.COL_NAME, nameTok,
                                          doNot));
                        ors.add(Clause.eq(Tables.ENTRIES.COL_DESCRIPTION,
                                          nameTok, doNot));
                        ors.add(Clause.eq(Tables.ENTRIES.COL_RESOURCE,
                                          nameTok, doNot));

                    }
                    ands.add(Clause.or(ors));
                }
                if (ands.size() > 1) {
                    //                System.err.println ("ands:" + ands);
                    textOrs.add(Clause.and(ands));
                } else if (ands.size() == 1) {
                    //                System.err.println ("ors:" + ands.get(0));
                    textOrs.add(ands.get(0));
                }
            }
            if (textOrs.size() > 1) {
                where.add(Clause.or(textOrs));
            } else if (textOrs.size() == 1) {
                where.add(textOrs.get(0));
            }
        }




    /**
     * _more_
     *
     * @param request _more_
     *
     * @return _more_
     */
    public boolean isOrSearch(Request request) {
        return request.getString("search.or", "false").equals("true");
    }


    /**
     * _more_
     *
     * @param entry _more_
     * @param stmt _more_
     * @param isNew _more_
     *
     * @throws Exception _more_
     */
    public void setStatement(Entry entry, PreparedStatement stmt,
                             boolean isNew)
            throws Exception {}

    /**
     * _more_
     *
     *
     * @param isNew _more_
     * @param typeInserts _more_
     * @return _more_
     */
    public void getInsertSql(boolean isNew,
                             List<TypeInsertInfo> typeInserts) {
        if (parent != null) {
            parent.getInsertSql(isNew, typeInserts);
        }
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param statement _more_
     * @param entry _more_
     *
     * @throws Exception _more_
     */
    public void deleteEntry(Request request, Statement statement, Entry entry)
            throws Exception {
        if (parent != null) {
            parent.deleteEntry(request, statement, entry);
        }
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param statement _more_
     * @param id _more_
     *
     * @throws Exception _more_
     */
    public void deleteEntry(Request request, Statement statement, String id)
            throws Exception {
        if (parent != null) {
            parent.deleteEntry(request, statement, id);
        }
    }

    /**
     * _more_
     *
     * @param request _more_
     *
     * @return _more_
     */
    protected List getTablesForQuery(Request request) {
        return getTablesForQuery(request, new ArrayList());
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param initTables _more_
     *
     * @return _more_
     */
    protected List getTablesForQuery(Request request, List initTables) {
        if (parent != null) {
            parent.getTablesForQuery(request, initTables);
        }
        if ( !initTables.contains(Tables.ENTRIES.NAME)) {
            initTables.add(Tables.ENTRIES.NAME);
        }
        return initTables;
    }



    /**
     * _more_
     *
     * @param columnName _more_
     * @param value _more_
     *
     * @return _more_
     */
    public Object convert(String columnName, String value) {
        if (parent != null) {
            return parent.convert(columnName, value);
        }
        return value;
    }

    /**
     * _more_
     *
     * @param map _more_
     *
     * @return _more_
     */
    public Object[] makeValues(Hashtable map) {
        if (parent != null) {
            return parent.makeValues(map);
        }
        return null;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String[] getValueNames() {
        if (parent != null) {
            return parent.getValueNames();
        }
        return null;
    }

    /**
     * _more_
     *
     * @param column _more_
     * @param value _more_
     * @param list _more_
     * @param quoteThem _more_
     *
     * @return _more_
     */
    protected boolean addOr(String column, String value, List list,
                            boolean quoteThem) {
        if ((value != null) && (value.trim().length() > 0)
                && !value.toLowerCase().equals("all")) {
            list.add("(" + SqlUtil.makeOrSplit(column, value, quoteThem)
                     + ")");
            return true;
        }
        return false;
    }


    /**
     * _more_
     *
     * @param column _more_
     * @param value _more_
     * @param clauses _more_
     *
     * @return _more_
     */
    protected boolean addOrClause(String column, String value,
                                  List<Clause> clauses) {
        if ((value != null) && (value.trim().length() > 0)
                && !value.toLowerCase().equals("all")) {
            clauses.add(Clause.makeOrSplit(column, value));
            return true;
        }
        return false;
    }




    /**
     * Set the Description property.
     *
     * @param value The new value for Description
     */
    public void setDescription(String value) {
        description = value;
    }

    /**
     * Get the Description property.
     *
     * @return The Description
     */
    public String getDescription() {
        return description;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getLabel() {
        if ((description == null) || (description.trim().length() == 0)) {
            return getType();
        }
        return description;
    }

    /**
     * _more_
     *
     * @param arg _more_
     * @param value _more_
     * @param entry _more_
     *
     * @return _more_
     */
    public int matchValue(String arg, Object value, Entry entry) {
        return MATCH_UNKNOWN;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public String toString() {
        return type + " " + description;
    }

    /**
     * Set the DfltDataType property.
     *
     * @param value The new value for DfltDataType
     */
    public void setDefaultDataType(String value) {
        defaultDataType = value;
    }

    /**
     * Get the DfltDataType property.
     *
     * @return The DfltDataType
     */
    public String getDefaultDataType() {
        return defaultDataType;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean hasDefaultDataType() {
        return (defaultDataType != null) && (defaultDataType.length() > 0);
    }



    /** _more_ */
    private Hashtable<String, HashSet> columnEnumValues =
        new Hashtable<String, HashSet>();


    /**
     * _more_
     *
     * @param column _more_
     * @param entry _more_
     *
     * @return _more_
     */
    protected String getEnumValueKey(Column column, Entry entry) {
        return column.getName();
    }

    /**
     * _more_
     *
     * @param column _more_
     * @param entry _more_
     * @param theValue _more_
     *
     * @throws Exception _more_
     */
    protected void addEnumValue(Column column, Entry entry, String theValue)
            throws Exception {
        if ((theValue == null) || (theValue.length() == 0)) {
            return;
        }
        HashSet set = getEnumValuesInner(column, entry);
        set.add(theValue);
    }

    /**
     * _more_
     *
     * @param column _more_
     * @param entry _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public List getEnumValues(Column column, Entry entry) throws Exception {
        HashSet set = getEnumValuesInner(column, entry);
        List    tmp = new ArrayList();
        tmp.addAll(set);
        return Misc.sort(tmp);
    }


    /**
     * _more_
     *
     * @param column _more_
     * @param entry _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    private HashSet getEnumValuesInner(Column column, Entry entry)
            throws Exception {
        String  key = getEnumValueKey(column, entry);
        HashSet set = columnEnumValues.get(key);
        if (set != null) {
            return set;
        }
        Clause clause = getEnumValuesClause(column, entry);
        Statement stmt = getRepository().getDatabaseManager().select(
                             SqlUtil.distinct(column.getName()),
                             column.getTableName(), clause);
        String[] values =
            SqlUtil.readString(
                getRepository().getDatabaseManager().getIterator(stmt), 1);
        set = new HashSet();
        set.addAll(Misc.toList(values));
        columnEnumValues.put(key, set);
        return set;
    }

    /**
     * _more_
     *
     * @param column _more_
     * @param entry _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Clause getEnumValuesClause(Column column, Entry entry)
            throws Exception {
        return null;
    }


    /**
     *  Set the Category property.
     *
     *  @param value The new value for Category
     */
    public void setCategory(String value) {
        this.category = value;
    }

    /**
     *  Get the Category property.
     *
     *  @return The Category
     */
    public String getCategory() {
        if (Misc.equals(this.category, CATEGORY_DEFAULT)
                && (parent != null)) {
            return parent.getCategory();
        }
        return this.category;
    }


    /**
     *  Get the ForUser property.
     *
     *  @return The ForUser
     */
    public boolean getForUser() {
        return forUser;
    }



}
