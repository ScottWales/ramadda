/*
 * Copyright 2008-2011 Jeff McWhirter/ramadda.org
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

package org.ramadda.plugins.db;



import org.w3c.dom.*;

import ucar.unidata.data.gis.KmlUtil;
import ucar.unidata.util.IOUtil;



import org.ramadda.repository.*;
import org.ramadda.repository.map.*;
import org.ramadda.repository.auth.*;
import org.ramadda.repository.metadata.*;
import org.ramadda.repository.output.CalendarOutputHandler;
import org.ramadda.repository.output.MapOutputHandler;
import org.ramadda.repository.output.OutputHandler;
import org.ramadda.repository.output.OutputType;
import org.ramadda.repository.output.RssOutputHandler;
import org.ramadda.repository.type.*;
import ucar.unidata.sql.*;
import ucar.unidata.util.DateUtil;

import ucar.unidata.geoloc.LatLonPointImpl;
import ucar.unidata.util.HtmlUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;
import ucar.unidata.util.TwoFacedObject;
import ucar.unidata.xml.XmlEncoder;
import ucar.unidata.xml.XmlUtil;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

import java.sql.Statement;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;



import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;


/**
 *
 */

public class DbTypeHandler extends BlobTypeHandler {

    /** _more_          */
    public static final String TAG_DBVALUES = "dbvalues";


    /** _more_          */
    public static final String OUTPUT_HTML = "html";

    /** _more_          */
    public static final String OUTPUT_CSV = "csv";


    /** _more_          */
    public static final String VIEW_NEW = "new";

    /** _more_          */
    public static final String VIEW_TABLE = "table";

    /** _more_          */
    public static final String VIEW_CALENDAR = "calendar";

    /** _more_          */
    public static final String VIEW_ICAL = "ical";

    /** _more_          */
    public static final String VIEW_TIMELINE = "timeline";

    /** _more_          */
    public static final String VIEW_MAP = "map";

    /** _more_          */
    public static final String VIEW_SEARCH = "search";

    /** _more_          */
    public static final String VIEW_CHART = "chart";

    /** _more_          */
    public static final String VIEW_GRID = "grid";

    /** _more_          */
    public static final String VIEW_CATEGORY = "category";

    /** _more_          */
    public static final String VIEW_CSV = "csv";

    /** _more_          */
    public static final String VIEW_KML = "kml";

    /** _more_          */
    public static final String VIEW_STICKYNOTES = "stickynotes";

    /** _more_          */
    public static final String VIEW_RSS = "rss";


    /** _more_          */
    public static final String ARG_DB_VIEW = "db.view";


    public static final String ARG_ENUM_ICON = "db.icon";
    public static final String ARG_ENUM_COLOR = "db.color";


    /** _more_          */
    public static final String ARG_DB_BULKCOL = "db.bulkcol";

    /** _more_          */
    public static final String ARG_DB_BULK = "db.bulk";

    /** _more_          */
    public static final String ARG_DB_DO = "db.do";

    /** _more_          */
    public static final String ARG_DB_SORTBY = "db.sortby";

    /** _more_          */
    public static final String ARG_DB_SORTDIR = "db.sortdir";

    /** _more_          */
    public static final String ARG_DB_OUTPUT = "db.output";

    /** _more_          */
    public static final String ARG_DB_NEWFORM = "db.newform";

    /** _more_          */
    public static final String ARG_DB_SEARCHFORM = "db.searchform";

    /** _more_          */
    public static final String ARG_DB_SEARCH = "db.search";

    /** _more_          */
    public static final String ARG_DB_LIST = "db.list";

    /** _more_          */
    public static final String ARG_DB_EDITFORM = "db.editform";

    /** _more_          */
    public static final String ARG_DB_SETPOS = "db.setpos";

    /** _more_          */
    public static final String ARG_DB_ENTRY = "db.entry";

    /** _more_          */
    public static final String ARG_DB_CREATE = "db.create";

    /** _more_          */
    public static final String ARG_DB_EDIT = "db.edit";

    /** _more_          */
    public static final String ARG_DB_COPY = "db.copy";

    /** _more_          */
    public static final String ARG_DB_COLUMN = "db.column";


    /** _more_          */
    public static final String ARG_DB_DELETE = "db.delete";

    /** _more_          */
    public static final String ARG_DB_DELETECONFIRM = "db.delete.confirm";

    /** _more_          */
    public static final String ARG_DB_ACTION = "db.action";

    /** _more_          */
    public static final String ARG_DB_STICKYLABEL = "db.stickylabel";


    /** _more_          */
    public static final String ARG_DBID = "dbid";

    /** _more_          */
    public static final String ARG_DBIDS = "dbids";

    /** _more_          */
    public static final String ARG_DBID_SELECTED = "dbid_selected";




    /** _more_          */
    public static final String ACTION_LIST = "db.list";


    /** _more_          */
    public static final String ACTION_DELETE = "db.delete";

    /** _more_          */
    public static final String ACTION_EMAIL = "db.email";

    /** _more_          */
    public static final String ACTION_CALENDAR = "db.calendar";

    /** _more_          */
    public static final String ACTION_MAP = "db.map";

    /** _more_          */
    public static final String ACTION_CSV = "db.csv";


    /** _more_ */
    public static String ARG_EMAIL_FROMADDRESS = "email.fromaddress";

    /** _more_          */
    public static String ARG_EMAIL_TO = "email.to";

    /** _more_ */
    public static String ARG_EMAIL_FROMNAME = "email.fromname";

    /** _more_ */
    public static String ARG_EMAIL_SUBJECT = "email.subject";

    /** _more_ */
    public static String ARG_EMAIL_MESSAGE = "email.message";

    /** _more_ */
    public static String ARG_EMAIL_BCC = "email.bcc";

    /** _more_          */
    public static final String PROP_STICKY_LABELS = "sticky.labels";


    /** _more_          */
    public static final String PROP_STICKY_POSX = "sticky.posx";

    /** _more_          */
    public static final String PROP_STICKY_POSY = "sticky.posy";

    /** _more_          */
    public static final String PROP_CAT_COLOR = "cat.color";

    public static final String PROP_CAT_ICON = "cat.icon";



    /** _more_          */
    public static final String COL_DBID = "db_id";

    /** _more_          */
    public static final String COL_DBUSER = "db_user";

    /** _more_          */
    public static final String COL_DBCREATEDATE = "db_createdate";

    /** _more_          */
    public static final String COL_DBPROPS = "db_props";

    /** _more_          */
    public static final int IDX_DBID = 0;

    /** _more_          */
    public static final int IDX_DBUSER = 1;

    /** _more_          */
    public static final int IDX_DBCREATEDATE = 2;

    /** _more_          */
    public static final int IDX_DBPROPS = 3;

    /** _more_          */
    private DbAdminHandler dbAdmin;

    /** _more_          */
    private GenericTypeHandler tableHandler;

    /** _more_          */
    private Hashtable<String, Column> columnMap = new Hashtable<String,
                                                      Column>();

    /** _more_          */
    private boolean hasLocation = false;

    /** _more_          */
    private boolean hasEmail = false;

    private List<String> icons;

    /** _more_          */
    private boolean[] doSums;

    /** _more_          */
    private boolean doSum = false;

    /** _more_          */
    private boolean hasDate = false;

    /** _more_          */
    private boolean hasNumber = false;

    /** _more_          */
    private List<Column> numberColumns = new ArrayList<Column>();

    /** _more_          */
    private List<Column> dateColumns = new ArrayList<Column>();

    /** _more_          */
    private List<Column> categoryColumns = new ArrayList<Column>();

    private List<Column> enumColumns = new ArrayList<Column>();

    /** _more_          */
    private List<Column> columns;

    /** _more_          */
    private Column dfltSortColumn;

    /** _more_          */
    private boolean dfltSortAsc = true;

    /** _more_          */
    private Column labelColumn;

    /** _more_          */
    private Column descColumn;

    /** _more_          */
    private Column urlColumn;

    /** _more_          */
    private Column latLonColumn;

    /** _more_          */
    private List<TwoFacedObject> viewList;

    /** _more_          */
    private String tableIcon = "";

    /** _more_          */
    SimpleDateFormat rssSdf =
        new SimpleDateFormat("EEE dd, MMM yyyy HH:mm:ss Z");

    /** _more_          */
    XmlEncoder xmlEncoder = new XmlEncoder();

    /**
     * _more_
     *
     *
     * @param dbAdmin _more_
     * @param repository _more_
     * @param tableName _more_
     * @param tableNode _more_
     * @param desc _more_
     *
     * @throws Exception _more_
     */
    public DbTypeHandler(DbAdminHandler dbAdmin, Repository repository,
                         String tableName, Element tableNode, String desc)
            throws Exception {
        super(repository, tableName, desc);
        this.dbAdmin = dbAdmin;
        this.tableIcon = XmlUtil.getAttribute(tableNode, "icon",
                "/db/database.png");

        //Initialize this type handler with a string blob
        Element root = XmlUtil.getRoot("<type></type>");
        Element node = XmlUtil.create("column", root, new String[] {
            "name", "contents", Column.ATTR_TYPE, "clob", Column.ATTR_SIZE,
            "256000", Column.ATTR_ADDTOFORM, "false", Column.ATTR_SHOWINHTML,
            "false"
        });
        List<Element> nodes = new ArrayList<Element>();
        nodes.add(node);
        super.init(nodes);

        setCategory("Database");
        tableHandler = new GenericTypeHandler(repository, "db_" + tableName,
                desc) {
            protected String getEnumValueKey(Column column, Entry entry) {
                if (entry != null) {
                    return entry.getId() + "_" + column.getName();
                }
                return column.getName();
            }

            public Clause getEnumValuesClause(Column column, Entry entry)
                    throws Exception {
                return Clause.eq(COL_ID, entry.getId());
            }
        };

        //        init((List<Element>)columnNodes);

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
        super.addToEntryNode(entry, node);
        List<Object[]> valueList = readValues(Clause.eq(COL_ID,
                                       entry.getId()), "", -1);
        Element dbvalues = XmlUtil.create(TAG_DBVALUES, node);
        XmlUtil.createCDataNode(dbvalues, xmlEncoder.toXml(valueList, false));
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
        super.initializeEntryFromXml(request, entry, node);
        String values = XmlUtil.getGrandChildText(node, TAG_DBVALUES,
                            (String) null);
        if (values == null) {
            return;
        }
        List<Object[]> valueList = (List<Object[]>) xmlEncoder.toObject(
                                       new String(
                                           XmlUtil.decodeBase64(values)));
        if (valueList == null) {
            throw new IllegalArgumentException(
                "Could not read database value list");
        }
        String sql = makeInsertOrUpdateSql(entry, null);
        PreparedStatement insertStmt =
            getRepository().getDatabaseManager().getPreparedStatement(sql);
        try {
            for (Object[] tuple : valueList) {
                tuple[IDX_DBID] = getRepository().getGUID();
                tableHandler.setStatement(entry, tuple, insertStmt, true);
                insertStmt.execute();
            }
        } finally {
            getRepository().getDatabaseManager().closeAndReleaseConnection(
                insertStmt);
        }
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public String getTableName() {
        return "properties_" + super.getTableName();
    }



    /**
     * _more_
     *
     * @param newEntry _more_
     * @param oldEntry _more_
     *
     * @throws Exception _more_
     */
    public void intializeCopiedEntry(Entry newEntry, Entry oldEntry)
            throws Exception {
        super.initializeCopiedEntry(newEntry, oldEntry);
        List<String> colNames = tableHandler.getColumnNames();
        Statement stmt = getDatabaseManager().select(SqlUtil.comma(colNames),
                             Misc.newList(tableHandler.getTableName()),
                             Clause.eq(COL_ID, oldEntry.getId()), "", -1);


        String sql = makeInsertOrUpdateSql(newEntry, null);
        PreparedStatement insertStmt =
            getRepository().getDatabaseManager().getPreparedStatement(sql);


        try {
            SqlUtil.Iterator iter = getDatabaseManager().getIterator(stmt);
            ResultSet        results;
            while ((results = iter.getNext()) != null) {
                Object[] values   = tableHandler.makeEntryValueArray();
                int      valueIdx = 2;
                for (Column column : columns) {
                    valueIdx = column.readValues(results, values, valueIdx);
                }
                //Just set a new id and a new create date
                values[IDX_DBID]         = getRepository().getGUID();
                values[IDX_DBCREATEDATE] = new Date();
                tableHandler.setStatement(newEntry, values, insertStmt, true);
                insertStmt.execute();
            }
        } finally {
            getRepository().getDatabaseManager().closeAndReleaseConnection(
                stmt);
            getRepository().getDatabaseManager().closeAndReleaseConnection(
                insertStmt);
        }
    }

    /**
     * _more_
     *
     * @param columnNodes _more_
     *
     * @throws Exception _more_
     */
    public void init(List<Element> columnNodes) throws Exception {

        putProperty("icon", tableIcon);
        putProperty("form.date.show", "false");
        putProperty("form.area.show", "false");
        putProperty("form.resource.show", "false");
        putProperty("form.datatype.show", "false");


        columns = tableHandler.getColumns();

        tableHandler.init(columnNodes);
        List<String> columnNames =
            new ArrayList<String>(tableHandler.getColumnNames());
        namesArray = StringUtil.listToStringArray(columnNames);
        columnMap  = new Hashtable<String, Column>();

        doSums     = new boolean[columns.size()];
        int cnt = 0;
        numberColumns   = new ArrayList<Column>();
        categoryColumns = new ArrayList<Column>();
        enumColumns = new ArrayList<Column>();
        dateColumns     = new ArrayList<Column>();
        hasDate         = false;
        labelColumn     = null;
        descColumn      = null;
        urlColumn       = null;
        dfltSortColumn  = null;


        for (Column column : columns) {
            doSums[cnt] = Misc.equals(column.getProperty("dosum"), "true");

            if (Misc.equals(column.getProperty("label"), "true")) {
                labelColumn = column;
            }
            if ((descColumn == null)
                    && column.getType().equals(Column.TYPE_STRING)
                    && (column.getRows() > 1)) {
                descColumn = column;
            }
            if (Misc.equals(column.getProperty("defaultsort"), "true")) {
                dfltSortColumn = column;
                dfltSortAsc = Misc.equals(column.getProperty("ascending"),
                                          "true");
            }


            if (doSums[cnt]) {
                doSum = true;
            }
            cnt++;
            if (column.getType().equals(Column.TYPE_EMAIL)) {
                hasEmail = true;
            }
            if (column.getType().equals(Column.TYPE_URL)) {
                urlColumn = column;
            }

            if (column.getType().equals(Column.TYPE_LATLONBBOX)
                    || column.getType().equals(Column.TYPE_LATLON)) {
                hasLocation  = true;
                latLonColumn = column;
            }
            if (column.getType().equals(Column.TYPE_DATE)) {
                hasDate = true;
                dateColumns.add(column);
            }
            if (column.isNumeric()) {
                numberColumns.add(column);
                hasNumber = true;
            }
            if (column.isEnumeration()) {
                enumColumns.add(column);
            }

            if (column.isEnumeration()
                    && Misc.equals(column.getProperty("iscategory"),
                                   "true")) {
                categoryColumns.add(column);
            }
            columnMap.put(column.getName(), column);
            for (String name : column.getColumnNames()) {
                columnMap.put(name, column);
            }
        }


        viewList = new ArrayList<TwoFacedObject>();
        viewList.add(new TwoFacedObject("Table", VIEW_TABLE));
        viewList.add(new TwoFacedObject("Sticky Notes", VIEW_STICKYNOTES));
        if (hasDate) {
            viewList.add(new TwoFacedObject("Calendar", VIEW_CALENDAR));
            viewList.add(new TwoFacedObject("Timeline", VIEW_TIMELINE));
            viewList.add(new TwoFacedObject("ICAL", VIEW_ICAL));
        }
        if (hasLocation) {
            viewList.add(new TwoFacedObject("Map", VIEW_MAP));
            viewList.add(new TwoFacedObject("KML", VIEW_KML));
        }
        if (numberColumns.size() > 0) {
            viewList.add(new TwoFacedObject("Chart", VIEW_CHART));
        }
        for (Column gridColumn : categoryColumns) {
            viewList.add(new TwoFacedObject(gridColumn.getLabel() + " "
                                            + "Category", VIEW_CATEGORY
                                                + gridColumn.getName()));
        }
        viewList.add(new TwoFacedObject("RSS", VIEW_RSS));
        viewList.add(new TwoFacedObject("CSV", VIEW_CSV));

    }

    /**
     * _more_
     *
     * @return _more_
     */
    private String getTitle() {
        return getDescription();

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
        if ( !getRepository().getAccessManager().canAccessFile(request,
                entry)) {
            return null;
        }

        request.remove(ARG_OUTPUT);
        List<Object[]> valueList = readValues(request, entry,
                                       Clause.eq(COL_ID, entry.getId()));
        StringBuffer sb = new StringBuffer();
        sb.append(HtmlUtil.cssLink(getRepository().getUrlBase()
                                   + "/db/dbstyle.css"));
        makeTable(request, entry, valueList, false, sb, false);
        return sb.toString();
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

        StringBuffer sb = new StringBuffer();
        if ( !getRepository().getAccessManager().canAccessFile(request,
                entry)) {
            sb.append(
                getRepository().showDialogWarning(
                    msg("You do not have permission to view database")));
            return new Result(getTitle(), sb);
        }

        boolean      canEdit = getAccessManager().canEditEntry(request,
                                   entry);

        List<String> colNames = tableHandler.getColumnNames();
        String       view     = request.getString(ARG_DB_VIEW, VIEW_TABLE);

        if (request.get(ARG_DB_SETPOS, false)) {
            if ( !canEdit) {
                throw new IllegalArgumentException(
                    "You cannot change the position");
            }
            String posx = request.getString("posx", "").replace("px", "");
            String posy = request.getString("posy", "").replace("px", "");
            if (request.exists(ARG_DB_STICKYLABEL)) {
                Hashtable props = getProperties(entry);
                String    label = request.getString(ARG_DB_STICKYLABEL, "");
                props.put(PROP_STICKY_POSX + "." + label, posx);
                props.put(PROP_STICKY_POSY + "." + label, posy);
                setProperties(entry, props);
                getEntryManager().storeEntry(entry);
            } else {
                Object[] values = tableHandler.getValues(makeClause(entry,
                                      request.getString(ARG_DBID, "")));
                putProp("posx", values, new Integer(posx));
                putProp("posy", values, new Integer(posy));
                doStore(entry, values, false);
            }
            return new Result("",
                              new StringBuffer("<contents>ok</contents>"),
                              "text/xml");
        }


        if (request.exists(ARG_DB_EDITFORM)) {
            if ( !canEdit) {
                throw new AccessException("You cannot edit this database",
                                          request);
            }
            return handleForm(request, entry,
                              request.getString(ARG_DBID, (String) null),
                              true);
        }


        if (request.exists(ARG_DB_NEWFORM) || view.equals(VIEW_NEW)) {
            if ( !canEdit) {
                throw new AccessException("You cannot edit this database",
                                          request);
            }
            return handleForm(request, entry, null, true);
        }

        if (request.exists(ARG_DB_CREATE)) {
            if ( !canEdit) {
                throw new AccessException("You cannot edit this database",
                                          request);
            }
            return handleNewOrEdit(request, entry, null);
        }

        if (request.exists(ARG_DB_EDIT) || request.exists(ARG_DB_COPY)) {
            if ( !canEdit) {
                throw new AccessException("You cannot edit this database",
                                          request);
            }
            return handleNewOrEdit(request, entry,
                                   request.getString(ARG_DBID,
                                       (String) null));
        }

        if (request.exists(ARG_DB_SEARCHFORM)) {
            return handleSearchForm(request, entry);
        }


        if (request.exists(ARG_DB_SEARCH)) {
            return handleSearch(request, entry);
        }

        if (request.exists(ARG_DB_ENTRY)) {
            return handleView(request, entry,
                              request.getString(ARG_DBID, (String) null));
        }


        String action = "";
        if (request.exists(ARG_DB_DO)) {
            action = request.getString(ARG_DB_ACTION, "");
        }


        if (request.exists(ARG_DB_DELETECONFIRM)) {
            if ( !canEdit) {
                throw new AccessException("You cannot edit this database",
                                          request);
            }
            return handleDeleteConfirm(request, entry);
        }


        if (request.exists(ARG_DB_DELETE) || action.equals(ACTION_DELETE)) {
            if ( !canEdit) {
                throw new AccessException("You cannot edit this database",
                                          request);
            }
            return handleDeleteAsk(request, entry);
        }


        return handleList(request, entry, action);

    }




    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param sb _more_
     * @param view _more_
     * @param numValues _more_
     * @param fromSearch _more_
     *
     * @throws Exception _more_
     */
    private void addViewHeader(Request request, Entry entry, StringBuffer sb,
                               String view, int numValues, boolean fromSearch)
            throws Exception {

        addViewHeader(request, entry, sb, view, numValues, fromSearch, null);
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param sb _more_
     * @param view _more_
     * @param numValues _more_
     * @param fromSearch _more_
     * @param extraLinks _more_
     *
     * @throws Exception _more_
     */
    private void addViewHeader(Request request, Entry entry, StringBuffer sb,
                               String view, int numValues,
                               boolean fromSearch, String extraLinks)
            throws Exception {


        List<String> headerToks = new ArrayList<String>();
        String baseUrl =
            HtmlUtil.url(request.url(getRepository().URL_ENTRY_SHOW),
                         new String[] { ARG_ENTRYID,
                                        entry.getId() });
        boolean addNext = false;
        if (view.equals(VIEW_TABLE)) {
            addNext = true;
            headerToks.add(HtmlUtil.b(msg("List")));
        } else {
            headerToks.add(HtmlUtil.href(baseUrl + "&" + ARG_DB_VIEW + "="
                                         + VIEW_TABLE, msg("List")));
        }


        if (view.equals(VIEW_NEW)) {
            headerToks.add(HtmlUtil.b(msg("New")));
        } else {
            headerToks.add(HtmlUtil.href(baseUrl + "&" + ARG_DB_VIEW + "="
                                         + VIEW_NEW, msg("New")));
        }

        if (view.equals(VIEW_SEARCH)) {
            headerToks.add(HtmlUtil.b(msg("Search")));
        } else {
            headerToks.add(HtmlUtil.href(baseUrl + "&" + ARG_DB_VIEW + "="
                                         + VIEW_SEARCH, msg("Search")));
        }

        /*
        if(view.equals(VIEW_STICKYNOTES)) {
            addNext = true;
            headerToks.add(HtmlUtil.b(msg("Sticky Notes")));
        } else {
            headerToks.add(HtmlUtil.href(baseUrl+"&" +ARG_DB_VIEW +"=" + VIEW_STICKYNOTES,
                                         msg("Sticky Notes")));
        }
        */

        if (hasDate) {
            if (view.equals(VIEW_CALENDAR)) {
                headerToks.add(HtmlUtil.b(msg("Calendar")));
            } else {
                headerToks.add(
                    HtmlUtil.href(
                        baseUrl + "&" + ARG_DB_VIEW + "=" + VIEW_CALENDAR,
                        msg("Calendar")));
            }

            /*
            if(view.equals(VIEW_TIMELINE)) {
                addNext = true;
                headerToks.add(HtmlUtil.b(msg("Timeline")));
            } else {
                headerToks.add(HtmlUtil.href(baseUrl+"&" +ARG_DB_VIEW +"=" + VIEW_TIMELINE,
                                             msg("Timeline")));
            }
            if(view.equals(VIEW_ICAL)) {
                headerToks.add(HtmlUtil.b(msg("ICAL")));
            } else {
                String icalUrl = HtmlUtil.url(request.url(getRepository().URL_ENTRY_SHOW)+"/" + entry.getName()+".ics",
                                       new String[]{
                                           ARG_ENTRYID, entry.getId()});
                headerToks.add(HtmlUtil.href(icalUrl+"&" +ARG_DB_VIEW +"=" + VIEW_ICAL,
                                             msg("ICAL")));
                                             }*/
        }

        if (hasLocation) {
            if (view.equals(VIEW_MAP)) {
                addNext = true;
                headerToks.add(HtmlUtil.b(msg("Map")));
            } else {
                headerToks.add(HtmlUtil.href(baseUrl + "&" + ARG_DB_VIEW
                                             + "=" + VIEW_MAP, msg("Map")));
            }
            if (view.equals(VIEW_KML)) {
                //                addNext = true;
                //                headerToks.add(HtmlUtil.b(msg("Map")));
            } else {
                String kmlUrl =
                    HtmlUtil.url(request.url(getRepository().URL_ENTRY_SHOW)
                                 + "/" + entry.getName()
                                 + ".kml", new String[] { ARG_ENTRYID,
                        entry.getId() });

                headerToks.add(HtmlUtil.href(kmlUrl + "&" + ARG_DB_VIEW + "="
                                             + VIEW_KML, msg("KML")));
            }
        }

        if (hasNumber) {
            addNext = true;
            if (view.equals(VIEW_CHART)) {
                headerToks.add(HtmlUtil.b(msg("Chart")));
            } else {
                headerToks.add(HtmlUtil.href(baseUrl + "&" + ARG_DB_VIEW
                                             + "="
                                             + VIEW_CHART, msg("Chart")));
            }
        }


        if (categoryColumns.size() > 0) {
            String theColumn = request.getString(ARG_DB_COLUMN,
                                                 categoryColumns.get(0).getName());
            for (Column column : categoryColumns) {
                String label = column.getLabel();
                if (view.equals(VIEW_CATEGORY + column.getName())) {
                    headerToks.add(HtmlUtil.b(label));
                } else {
                    headerToks.add(HtmlUtil.href(baseUrl + "&" + ARG_DB_VIEW
                            + "=" + VIEW_CATEGORY + column.getName() + "&"
                            + ARG_DB_COLUMN + "=" + column.getName(), label));
                }
            }
        }

        sb.append(HtmlUtil.cssLink(getRepository().getUrlBase()
                                   + "/db/dbstyle.css"));
        if (headerToks.size() > 1) {
            sb.append(HtmlUtil.div(StringUtil.join("&nbsp;|&nbsp;",
                    headerToks), HtmlUtil.cssClass("dbheader")));
        }


        if (request.defined(ARG_MESSAGE)) {
            sb.append(
                getRepository().showDialogNote(
                    request.getString(ARG_MESSAGE, "")));
            request.remove(ARG_MESSAGE);
        }

        if (extraLinks != null) {
            sb.append(HtmlUtil.div(extraLinks,
                                   HtmlUtil.cssClass("dbheader")));
        }


        if (fromSearch) {
            sb.append(
                HtmlUtil.inset(
                    HtmlUtil.makeShowHideBlock(
                        msg("Search again"),
                        getSearchForm(request, entry).toString(),
                        false), 10));
        }


        if (addNext) {
            if ((numValues > 0)
                    && ((numValues == request.get(ARG_MAX, DB_MAX_ROWS))
                        || request.defined(ARG_SKIP))) {
                getRepository().getHtmlOutputHandler().showNext(request,
                        numValues, sb);
                sb.append(HtmlUtil.br());

            }
        }

        sb.append(HtmlUtil.importJS(getRepository().fileUrl("/db/db.js")));
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param action _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result handleList(Request request, Entry entry, String action)
            throws Exception {
        return handleList(request, entry, Clause.eq(COL_ID, entry.getId()),
                          action, false);
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param clause _more_
     * @param action _more_
     * @param fromSearch _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result handleList(Request request, Entry entry, Clause clause,
                             String action, boolean fromSearch)
            throws Exception {
        String         view = request.getString(ARG_DB_VIEW, VIEW_TABLE);

        List<Object[]> valueList;

        if ((dateColumns.size() > 0) && request.defined(ARG_YEAR)
                && request.defined(ARG_MONTH)) {
            int year  = request.get(ARG_YEAR, 0);
            int month = request.get(ARG_MONTH, 0) + 1;
            GregorianCalendar cal =
                new GregorianCalendar(DateUtil.TIMEZONE_GMT);
            SimpleDateFormat sdf   = new SimpleDateFormat("yyyy/MM/dd");
            Date             date1 = sdf.parse(year + "/" + month + "/1");
            cal.setTime(date1);
            cal.add(GregorianCalendar.MONTH, 1);
            Date date2 = cal.getTime();
            clause = Clause.and(
                clause,
                Clause.and(
                    Clause.ge(dateColumns.get(0).getName(), date1),
                    Clause.le(dateColumns.get(0).getName(), date2)));

        }
        valueList = readValues(request, entry, clause);

        if (action.equals(ACTION_CSV) || view.equals(VIEW_CSV)) {
            return handleListCsv(request, entry, valueList);
        }

        if (view.equals(VIEW_RSS)) {
            return handleListRss(request, entry, valueList);
        }

        if (action.equals(ACTION_EMAIL)) {
            return handleListEmail(request, entry, valueList);
        }

        if (view.equals(VIEW_SEARCH)) {
            return handleSearchForm(request, entry);
        }

        if (view.equals(VIEW_MAP)) {
            return handleListMap(request, entry, valueList, fromSearch);
        }

        if (view.equals(VIEW_STICKYNOTES)) {
            return handleListStickyNotes(request, entry, valueList,
                                         fromSearch);
        }

        if (view.equals(VIEW_KML)) {
            return handleListKml(request, entry, valueList, fromSearch);
        }

        if (view.startsWith(VIEW_GRID)) {
            return handleListGrid(request, entry, valueList, fromSearch);
        }

        if (view.startsWith(VIEW_CATEGORY)) {
            return handleListCategory(request, entry, valueList, fromSearch);
        }

        if (view.equals(VIEW_TIMELINE)) {
            return handleListTimeline(request, entry, valueList, fromSearch);
        }

        if (view.equals(VIEW_CHART)) {
            return handleListChart(request, entry, valueList, fromSearch);
        }

        if (view.equals(VIEW_CALENDAR)) {
            return handleListCalendar(request, entry, valueList, fromSearch);
        }


        if (view.equals(VIEW_ICAL)) {
            return handleListIcal(request, entry, valueList, fromSearch);
        }

        return handleListTable(request, entry, valueList, fromSearch);
    }








    /**
     * _more_
     *
     * @param request _more_
     * @param formBuffer _more_
     * @param entry _more_
     *
     * @throws Exception _more_
     */
    public void addToEntryForm(Request request, StringBuffer formBuffer,
                               Entry entry)
            throws Exception {
        if ((urlColumn != null) && (entry != null)) {
            String baseUrl =
                getRepository().absoluteUrl(
                    HtmlUtil.url(
                        request.url(getRepository().URL_ENTRY_SHOW),
                        new String[] { ARG_ENTRYID,
                                       entry.getId() }));
            String url = baseUrl + "&" + ARG_DB_VIEW + "=" + VIEW_NEW;
            String jsUrl = "javascript:document.location='" + url + "'+'&"
                           + urlColumn.getFullName() + "='+"
                           + "document.location";

            if (labelColumn != null) {
                jsUrl = jsUrl + "+'&" + labelColumn.getFullName()
                        + "='+document.title";
            }

            if (descColumn != null) {
                String selected =
                    "(window.getSelection? window.getSelection():document.getSelection?document.getSelection():document.selection?document.selection:'')";

                jsUrl = jsUrl + "+'&" + descColumn.getFullName() + "='+"
                        + selected;
            }

            String href = HtmlUtil.href(jsUrl,
                                        " Add URL to " + entry.getName());
            formBuffer.append(
                HtmlUtil.row(
                    HtmlUtil.colspan(
                        "Bookmark this link to add new items to the database: "
                        + href, 2)));
        }

        super.addToEntryForm(request, formBuffer, entry);
        Hashtable props = getProperties(entry);
        if(entry!=null) {
            addEnumerationAttributes(request, entry, formBuffer);
        }

        formBuffer.append(
            HtmlUtil.row(
                HtmlUtil.colspan(
                    HtmlUtil.div(
                        msg("Sticky Notes"),
                        " class=\"formgroupheader\" "), 2)));


        String stickyLabelString = (String) props.get(PROP_STICKY_LABELS);
        if (stickyLabelString == null) {
            stickyLabelString = "";
        }
        formBuffer.append(
                          formEntry(request,
                msg("Labels"),
                HtmlUtil.textArea(
                    PROP_STICKY_LABELS, stickyLabelString, 5, 30)));


    }

    private void addEnumerationAttributes(Request request, Entry entry, StringBuffer formBuffer) throws Exception {
        if(enumColumns.size()==0) {
            return;
        }
        Hashtable props = getProperties(entry);
            
        String[] colors = {
            "#fff",
            "#000",
            "#444",
            "#888",
            "#eee",
            "red",
            "orange",
            "yellow",
            "green",
            "blue",
            "cyan",
            "purple",
        };

        //        if(icons == null) {
            icons = StringUtil.split(getRepository().getResource("/org/ramadda/plugins/db/icons.txt"),"\n",true, true);
            //        }

        for(Column col: enumColumns) {
            formBuffer.append(
                              HtmlUtil.row(
                                           HtmlUtil.colspan(
                                                            HtmlUtil.div(
                                                                         msg("Settings for") + " " + col.getName(),
                                                                         HtmlUtil.cssClass("formgroupheader")), 2)));

            String colorID = PROP_CAT_COLOR+"." + col.getName();
            String iconID = PROP_CAT_ICON+"." + col.getName();
            Hashtable<String,String> colorMap = (Hashtable<String,String>) props.get(colorID);
            if(colorMap==null) {
                colorMap = new Hashtable<String,String>();
            }
            Hashtable<String,String> iconMap = (Hashtable<String,String>) props.get(iconID);
            if(iconMap==null) {
                iconMap = new Hashtable<String,String>();
            }
            StringBuffer sb = new StringBuffer("");
            for(String value:   getEnumValues(entry, col)) {
                String currentColor = colorMap.get(value);
                String currentIcon = iconMap.get(value);
                if(currentColor==null) currentColor="";
                if(currentIcon==null) currentIcon="";
                String colorArg = colorID +"." + value;
                String iconArg = iconID +"." + value;
                StringBuffer colorSB = new StringBuffer();
                colorSB.append(HtmlUtil.radio(colorArg,"",currentColor.equals("")));
                colorSB.append(msg("None"));
                colorSB.append(" ");
                for(String c: colors) {
                    colorSB.append(HtmlUtil.span(HtmlUtil.radio(colorArg,c,currentColor.equals(c)), HtmlUtil.style("margin-left:2px; margin-right:2px; padding-left:5px; padding-right:7px; border:1px solid #000; background-color:" + c)));
                }
                StringBuffer iconSB = new StringBuffer();
                iconSB.append(HtmlUtil.radio(iconArg,"", currentIcon.equals("")));
                iconSB.append(msg("None"));
                iconSB.append(" ");
                for(String icon: icons) {
                    if(icon.startsWith("#")) {
                        continue;
                    }
                    if(icon.equals("br")) {
                        iconSB.append("<br>");
                        continue;
                    }
                    iconSB.append(HtmlUtil.radio(iconArg,icon,currentIcon.equals(icon)));
                    iconSB.append(HtmlUtil.img(getIconUrl(icon), IOUtil.getFileTail(icon)));
                }
                formBuffer.append(HtmlUtil.formEntry(msgLabel("Value"), value));
                formBuffer.append(HtmlUtil.formEntryTop(msgLabel("Color"), colorSB.toString()));
                String iconMsg = "";
                if(currentIcon.length()>0) {
                    iconMsg = HtmlUtil.img(getIconUrl(currentIcon));
                }
                formBuffer.append(HtmlUtil.formEntryTop(msgLabel("Icon"), HtmlUtil.makeShowHideBlock(iconMsg,
                                                                                                        iconSB.toString(),false)));
            }
            formBuffer.append(HtmlUtil.formEntry("", sb.toString()));
        }

    }


    private String getIconUrl(String icon) {
        if(icon.startsWith("http:")) return icon;
        return getRepository().getUrlBase()+ "/db/icons/" + icon;
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
        super.initializeEntryFromForm(request, entry, parent, newEntry);
        Hashtable props          = getProperties(entry);
        String stickyLabelString = request.getString(PROP_STICKY_LABELS, "");
        props.put(PROP_STICKY_LABELS,
                  StringUtil.join("\n",
                                  StringUtil.split(stickyLabelString, "\n",
                                      true, true)));

        for(Column col: enumColumns) {
            String colorID = PROP_CAT_COLOR+"." + col.getName();
            Hashtable<String,String> colorMap = (Hashtable<String,String>) props.get(colorID);
            if(colorMap==null) {
                colorMap = new Hashtable<String,String>();
            }

            String iconID = PROP_CAT_ICON+"." + col.getName();
            Hashtable<String,String> iconMap = (Hashtable<String,String>) props.get(iconID);
            if(iconMap==null) {
                iconMap = new Hashtable<String,String>();
            }
            List<String> enumValues = getEnumValues(entry, col);
            for(String value:   enumValues) {
                String iconArg = iconID +"." + value;
                String iconValue = request.getString(iconArg, "");
                if(iconValue.equals("")) {
                    iconMap.remove(value);
                } else {
                    iconMap.put(value, iconValue);
                }

            }
            for(String value:   enumValues) {
                String colorArg = colorID +"." + value;
                String colorValue = request.getString(colorArg, "");
                if(colorValue.equals("")) {
                    colorMap.remove(value);
                } else {
                    colorMap.put(value, colorValue);
                }
            }
            props.put(colorID,colorMap);
            props.put(iconID,iconMap);
        }



        setProperties(entry, props);

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
    public StringBuffer getSearchForm(Request request, Entry entry)
            throws Exception {
        StringBuffer sb      = new StringBuffer();
        String       formUrl = request.url(getRepository().URL_ENTRY_SHOW);
        sb.append(HtmlUtil.form(formUrl));
        sb.append(HtmlUtil.hidden(ARG_ENTRYID, entry.getId()));
        sb.append(HtmlUtil.formTable());

        List<Clause> where = new ArrayList<Clause>();
        for (Column column : columns) {
            column.addToSearchForm(request, sb, where, entry);
        }
        sb.append(formEntry(request,msgLabel("View Results As"),
                                     HtmlUtil.select(ARG_DB_VIEW, viewList,
                                         request.getString(ARG_DB_VIEW,
                                             ""))));

        sb.append(formEntry(request,msgLabel("Count"),
                                     HtmlUtil.input(ARG_MAX,
                                         request.get(ARG_MAX, 100),
                                         HtmlUtil.SIZE_5)));
        sb.append(formEntry(request,"",
                                     HtmlUtil.submit(msg("Search"),
                                         ARG_DB_SEARCH) + HtmlUtil.space(2)
                                             + HtmlUtil.submit(msg("Cancel"),
                                                 ARG_DB_LIST)));

        sb.append(HtmlUtil.formTableClose());
        return sb;
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
    public Result handleSearchForm(Request request, Entry entry)
            throws Exception {
        StringBuffer sb = new StringBuffer();
        addViewHeader(request, entry, sb, VIEW_SEARCH, 0, false);
        sb.append(getSearchForm(request, entry));
        return new Result(getTitle(), sb);
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
    public Result handleSearch(Request request, Entry entry)
            throws Exception {
        StringBuffer sb    = new StringBuffer();
        List<Clause> where = new ArrayList<Clause>();
        where.add(Clause.eq(COL_ID, entry.getId()));
        StringBuffer searchCriteria = new StringBuffer();
        for (Column column : columns) {
            column.assembleWhereClause(request, where, searchCriteria);
        }
        return handleList(request, entry, Clause.and(where), "", true);
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
    public Result handleDeleteConfirm(Request request, Entry entry)
            throws Exception {
        List      dbids     = request.get(ARG_DBID_SELECTED, new ArrayList());
        Statement statement = getDatabaseManager().createStatement();
        try {
            for (Object dbid : dbids) {
                String query =
                    SqlUtil.makeDelete(tableHandler.getTableName(), COL_DBID,
                                       SqlUtil.quote(dbid.toString()));
                statement.execute(query);
            }
        } finally {
            getRepository().getDatabaseManager().closeAndReleaseConnection(
                statement);
        }




        String url =
            HtmlUtil.url(request.url(getRepository().URL_ENTRY_SHOW),
                         new String[] { ARG_ENTRYID,
                                        entry.getId(), ARG_MESSAGE,
                                        "Entries deleted" });

        return new Result(url);
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
    public Result handleDeleteAsk(Request request, Entry entry)
            throws Exception {
        StringBuffer sb    = new StringBuffer();
        List         dbids = request.get(ARG_DBID_SELECTED, new ArrayList());

        if (dbids.size() == 0) {
            sb.append(
                getRepository().showDialogWarning(
                    msg("No entries were selected")));
        } else {
            String formUrl = request.url(getRepository().URL_ENTRY_SHOW);
            sb.append(HtmlUtil.form(formUrl));
            sb.append(HtmlUtil.hidden(ARG_ENTRYID, entry.getId()));

            for (Object dbid : dbids) {
                sb.append(HtmlUtil.hidden(ARG_DBID_SELECTED,
                                          dbid.toString()));
            }

            addViewHeader(request, entry, sb, "", 0, false);
            sb.append(
                getRepository()
                    .showDialogQuestion(
                        msg(
                        "Are you sure you want to delete the selected entries?"), HtmlUtil
                            .submit(
                                msg("Yes"), ARG_DB_DELETECONFIRM) + HtmlUtil
                                    .space(2) + HtmlUtil
                                    .submit(msg("Cancel"), ARG_DB_LIST)));
        }



        sb.append(HtmlUtil.formClose());
        return new Result(getTitle(), sb);
    }


    /**
     * _more_
     *
     * @param prop _more_
     * @param values _more_
     * @param obj _more_
     *
     * @throws Exception _more_
     */
    private void putProp(String prop, Object[] values, Object obj)
            throws Exception {
        Hashtable props = getProps(values);
        if (props == null) {
            props = new Hashtable();
        }
        props.put(prop, obj);
        values[IDX_DBPROPS] = xmlEncoder.toXml(props);
    }

    /**
     * _more_
     *
     * @param prop _more_
     * @param values _more_
     * @param dflt _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    private double getProp(String prop, Object[] values, double dflt)
            throws Exception {
        Hashtable props = getProps(values);
        if (props == null) {
            return dflt;
        }
        Double d = (Double) props.get(prop);
        if (d == null) {
            return dflt;
        }
        return d.doubleValue();
    }



    /**
     * _more_
     *
     * @param prop _more_
     * @param values _more_
     * @param dflt _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    private int getProp(String prop, Object[] values, int dflt)
            throws Exception {
        Hashtable props = getProps(values);
        if (props == null) {
            return dflt;
        }
        Integer d = (Integer) props.get(prop);
        if (d == null) {
            return dflt;
        }
        return d.intValue();
    }

    /**
     * _more_
     *
     * @param values _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    private Hashtable getProps(Object[] values) throws Exception {
        String value = (String) values[IDX_DBPROPS];
        if ((value == null) || (value.length() == 0)) {
            return null;
        }
        return (Hashtable) xmlEncoder.toObject(value);
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param dbid _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result handleNewOrEdit(Request request, Entry entry, String dbid)
            throws Exception {
        if (request.exists(ARG_DB_COPY)) {
            dbid = null;
        }

        StringBuffer sb       = new StringBuffer();
        List<String> colNames = tableHandler.getColumnNames();
        Object[]     values   = ((dbid != null)
                                 ? tableHandler.getValues(makeClause(entry,
                                     dbid))
                                 : tableHandler.makeEntryValueArray());
        //The first entry is the db_id
        values[IDX_DBID] = ((dbid == null)
                            ? getRepository().getGUID()
                            : dbid);

        if (dbid == null) {
            values[IDX_DBUSER]       = request.getUser().getId();
            values[IDX_DBCREATEDATE] = new Date();
            values[IDX_DBPROPS]      = "";
        }

        for (Column column : columns) {
            column.setValue(request, entry, values);
        }

        doStore(entry, values, dbid == null);
        String url =
            HtmlUtil.url(request.url(getRepository().URL_ENTRY_SHOW),
                         new String[] {
            ARG_ENTRYID, entry.getId(), ARG_DBID, (String) values[IDX_DBID],
            ARG_DB_EDITFORM, "true"
        });
        return new Result(url);
    }


    /**
     * _more_
     *
     * @param entry _more_
     * @param values _more_
     * @param isNew _more_
     *
     * @throws Exception _more_
     */
    private void doStore(Entry entry, Object[] values, boolean isNew)
            throws Exception {
        String dbid = (String) values[IDX_DBID];
        String sql  = makeInsertOrUpdateSql(entry, (isNew
                ? null
                : dbid));
        PreparedStatement stmt =
            getRepository().getDatabaseManager().getPreparedStatement(sql);
        try {
            int stmtIdx = tableHandler.setStatement(entry, values, stmt,
                              isNew);
            if ( !isNew) {
                stmt.setString(stmtIdx, dbid);
            }
            stmt.execute();
        } finally {
            getRepository().getDatabaseManager().closeAndReleaseConnection(
                stmt);
        }

    }

    /** _more_          */
    private String[] namesArray;


    /**
     * _more_
     *
     * @param entry _more_
     * @param dbid _more_
     *
     * @return _more_
     */
    private Clause makeClause(Entry entry, String dbid) {
        return Clause.and(Clause.eq(COL_ID, entry.getId()),
                          Clause.eq(COL_DBID, dbid));

    }


    /**
     * _more_
     *
     * @param entry _more_
     * @param dbid _more_
     *
     * @return _more_
     */
    public String makeInsertOrUpdateSql(Entry entry, String dbid) {
        if (dbid == null) {
            return SqlUtil.makeInsert(
                tableHandler.getTableName(),
                SqlUtil.comma(tableHandler.getColumnNames()),
                SqlUtil.getQuestionMarks(
                    tableHandler.getColumnNames().size()));
        } else {
            Clause clause = makeClause(entry, dbid);
            return SqlUtil.makeUpdate(tableHandler.getTableName(), clause,
                                      namesArray);

        }

    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param valueList _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result handleListEmail(Request request, Entry entry,
                                  List<Object[]> valueList)
            throws Exception {

        Column theColumn = null;
        for (Column column : columns) {
            if (column.getType().equals(Column.TYPE_EMAIL)) {
                theColumn = column;
            }
        }
        if (theColumn == null) {
            throw new IllegalStateException("No email data found");
        }
        StringBuffer sb = new StringBuffer();
        makeForm(request, entry, sb);
        sb.append(HtmlUtil.hidden(ARG_DB_ACTION, ACTION_EMAIL));
        sb.append(HtmlUtil.submit(msg("Send Message")));
        sb.append(HtmlUtil.space(2));
        sb.append(HtmlUtil.submit(msg("Cancel"), ARG_DB_LIST));
        sb.append(HtmlUtil.formTable());

        for (Object[] values : valueList) {
            String toId = (String) values[IDX_DBID];
            sb.append(HtmlUtil.hidden(ARG_DBID_SELECTED, toId));
        }


        sb.append(HtmlUtil.hidden(ARG_ENTRYID, entry.getId()));
        sb.append(formEntry(request, msgLabel("From name"),
                                     HtmlUtil.input(ARG_EMAIL_FROMNAME,
                                         request.getUser().getName(),
                                         HtmlUtil.SIZE_40)));
        sb.append(formEntry(request, msgLabel("From email"),
                                     HtmlUtil.input(ARG_EMAIL_FROMADDRESS,
                                         request.getUser().getEmail(),
                                         HtmlUtil.SIZE_40)));
        String bcc = HtmlUtil.checkbox(ARG_EMAIL_BCC, "true", false)
                     + HtmlUtil.space(1) + msg("Send as BCC");

        sb.append(
            formEntry(
                      request, msgLabel("Subject"),
                HtmlUtil.input(ARG_EMAIL_SUBJECT, "", HtmlUtil.SIZE_40)
                + HtmlUtil.space(2) + bcc));
        sb.append(HtmlUtil.formEntryTop(msgLabel("Message"),
                                        HtmlUtil.textArea(ARG_EMAIL_MESSAGE,
                                                          "", 30, 60)));
        sb.append(HtmlUtil.formTableClose());
        sb.append(HtmlUtil.submit(msg("Send Message")));


        sb.append(HtmlUtil.formTableClose());
        sb.append(HtmlUtil.formClose());

        return new Result(getTitle(), sb);
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param valueList _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result handleListCsv(Request request, Entry entry,
                                List<Object[]> valueList)
            throws Exception {
        StringBuffer sb = new StringBuffer();
        for (int cnt = 0; cnt < valueList.size(); cnt++) {
            Object[] values = valueList.get(cnt);
            for (int i = 1; i < columns.size(); i++) {
                StringBuffer cb = new StringBuffer();
                columns.get(i).formatValue(entry, cb, Column.OUTPUT_CSV,
                            values);
                String colValue = cb.toString();
                colValue = colValue.replaceAll(",", "_");
                colValue = colValue.replaceAll("\n", " ");
                if (i > 1) {
                    sb.append(",");
                }
                sb.append(colValue);
            }
            sb.append("\n");
        }
        return new Result("", sb, "text/csv");
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param valueList _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result handleListRss(Request request, Entry entry,
                                List<Object[]> valueList)
            throws Exception {
        StringBuffer sb = new StringBuffer();

        sb.append(XmlUtil.XML_HEADER + "\n");
        sb.append(XmlUtil.openTag(RssOutputHandler.TAG_RSS_RSS,
                                  XmlUtil.attrs(ATTR_RSS_VERSION, "2.0")));
        sb.append(XmlUtil.openTag(RssOutputHandler.TAG_RSS_CHANNEL));
        sb.append(XmlUtil.tag(RssOutputHandler.TAG_RSS_TITLE, "",
                              entry.getName()));
        for (int cnt = 0; cnt < valueList.size(); cnt++) {
            Object[] values = valueList.get(cnt);
            String   label  = getLabel(entry, values);
            Date     date   = null;
            if (dateColumns.size() > 0) {
                date = (Date) values[dateColumns.get(0).getOffset()];
            } else {
                date = (Date) values[IDX_DBCREATEDATE];
            }
            String dbid = (String) values[IDX_DBID];

            String info = getHtml(request, entry, dbid, columns, values);
            sb.append(XmlUtil.openTag(RssOutputHandler.TAG_RSS_ITEM));
            sb.append(XmlUtil.tag(RssOutputHandler.TAG_RSS_PUBDATE, "",
                                  rssSdf.format(date)));
            sb.append(XmlUtil.tag(RssOutputHandler.TAG_RSS_TITLE, "", label));


            String url = getViewUrl(request, entry, "" + values[IDX_DBID]);
            url = repository.absoluteUrl(url);
            sb.append(XmlUtil.tag(RssOutputHandler.TAG_RSS_LINK, "",
                                  XmlUtil.getCdata(url)));


            sb.append(XmlUtil.tag(RssOutputHandler.TAG_RSS_GUID, "",
                                  XmlUtil.getCdata(url)));
            sb.append(XmlUtil.openTag(RssOutputHandler.TAG_RSS_DESCRIPTION,
                                      ""));
            XmlUtil.appendCdata(sb, info);
            sb.append(XmlUtil.closeTag(RssOutputHandler.TAG_RSS_DESCRIPTION));
            if (hasLocation) {
                double[] ll = latLonColumn.getLatLon(values);
                sb.append(XmlUtil.tag(RssOutputHandler.TAG_RSS_GEOLAT, "",
                                      "" + ll[0]));
                sb.append(XmlUtil.tag(RssOutputHandler.TAG_RSS_GEOLON, "",
                                      "" + ll[1]));
            }
            sb.append(XmlUtil.closeTag(RssOutputHandler.TAG_RSS_ITEM));
        }
        sb.append(XmlUtil.closeTag(RssOutputHandler.TAG_RSS_CHANNEL));
        sb.append(XmlUtil.closeTag(RssOutputHandler.TAG_RSS_RSS));

        return new Result("", sb, "application/rss+xml");
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param values _more_
     * @param rowId _more_
     * @param divId _more_
     *
     * @return _more_
     */
    public String getEventJS(Request request, Entry entry, Object[] values,
                             String rowId, String divId) {
        String xmlUrl = getViewUrl(request, entry, "" + values[IDX_DBID])
                        + "&result=xml";
        String event = HtmlUtil.onMouseOver(
                           HtmlUtil.call(
                               "dbRowOver",
                               HtmlUtil.squote(rowId))) + HtmlUtil.onMouseOut(
                                   HtmlUtil.call(
                                       "dbRowOut",
                                       HtmlUtil.squote(
                                           rowId))) + HtmlUtil.onMouseClick(
                                               HtmlUtil.call(
                                                   "dbRowClick",
                                                   "event,"
                                                   + HtmlUtil.squote(divId)
                                                   + ","
                                                   + HtmlUtil.squote(
                                                       xmlUrl)));
        return event;
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param valueList _more_
     * @param fromSearch _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result handleListTable(Request request, Entry entry,
                                  List<Object[]> valueList,
                                  boolean fromSearch)
            throws Exception {
        StringBuffer sb = new StringBuffer();
        String links = getHref(request, entry, VIEW_STICKYNOTES,
                               msg("Sticky Notes")) + "&nbsp;|&nbsp;"
                                   + getHref(request, entry, VIEW_RSS,
                                             msg("RSS"),
                                             "/" + entry.getName()
                                             + ".rss") + "&nbsp;|&nbsp;"
                                                 + getHref(request, entry,
                                                     VIEW_CSV, msg("CSV"),
                                                     "/" + entry.getName()
                                                     + ".csv");
        addViewHeader(request, entry, sb, VIEW_TABLE, valueList.size(),
                      fromSearch, links);
        makeTable(request, entry, valueList, fromSearch, sb, true);
        return new Result(getTitle(), sb);
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param valueList _more_
     * @param fromSearch _more_
     * @param sb _more_
     * @param doForm _more_
     *
     * @throws Exception _more_
     */
    public void makeTable(Request request, Entry entry,
                          List<Object[]> valueList, boolean fromSearch,
                          StringBuffer sb, boolean doForm)
            throws Exception {

        Hashtable entryProps     = getProperties(entry);

        if (doForm) {
            String formUrl = request.url(getRepository().URL_ENTRY_SHOW);
            sb.append(HtmlUtil.form(formUrl));
            sb.append(HtmlUtil.hidden(ARG_ENTRYID, entry.getId()));
        }
        boolean   canEdit = getAccessManager().canEditEntry(request, entry);
        HashSet<String> except  = new HashSet<String>();
        except.add(ARG_DB_SORTBY);
        except.add(ARG_DB_SORTDIR);

        String  baseUrl = request.getUrl(except, null);
        boolean asc     = request.getString(ARG_DB_SORTDIR, (dfltSortAsc
                ? "asc"
                : "desc")).equals("asc");
        String sortBy = request.getString(ARG_DB_SORTBY,
                                          ((dfltSortColumn == null)
                                           ? ""
                                           : dfltSortColumn.getName()));
        if (valueList.size() > 0) {
            List<TwoFacedObject> actions = new ArrayList<TwoFacedObject>();
            //TODO uncomment            if(hasEmail && getRepository().getAdmin().isEmailCapable()) {
            if (hasEmail) {
                actions.add(new TwoFacedObject("Send mail", ACTION_EMAIL));
            }
            if (canEdit) {
                actions.add(new TwoFacedObject("Delete selected",
                        ACTION_DELETE));
            }
            if (actions.size() > 0) {
                if (doForm) {
                    sb.append(HtmlUtil.submit(msgLabel("Do"), ARG_DB_DO));
                    sb.append(HtmlUtil.select(ARG_DB_ACTION, actions));
                }
            }


            sb.append(HtmlUtil.p());
            sb.append(
                "<table class=\"dbtable\"  border=1 cellspacing=\"0\" cellpadding=\"0\" width=\"100%\">");
            sb.append("<tr>");
            sb.append("<td class=dbtableheader>&nbsp;</td>");
            for (int i = 1; i < columns.size(); i++) {
                if ( !columns.get(i).getCanList()) {
                    continue;
                }
                String label      = columns.get(i).getLabel();
                String sortColumn = columns.get(i).getName();
                String extra;
                if (sortColumn.equals(sortBy)) {
                    if (asc) {
                        extra = " "
                                + HtmlUtil.img(
                                    getRepository().iconUrl(ICON_UPDART));
                    } else {
                        extra = " "
                                + HtmlUtil.img(
                                    getRepository().iconUrl(ICON_DOWNDART));
                    }
                    asc = !asc;
                } else {
                    extra =
                        " "
                        + HtmlUtil.img(getRepository().iconUrl(ICON_BLANK),
                                       "", HtmlUtil.attr("width", "10"));
                }

                String link = HtmlUtil.href(baseUrl + "&" + ARG_DB_SORTBY
                                            + "=" + sortColumn + "&"
                                            + ARG_DB_SORTDIR + (asc
                        ? "=asc"
                        : "=desc"), label) + extra;
                sb.append(HtmlUtil.col(link,
                                       HtmlUtil.cssClass("dbtableheader")));

            }
            sb.append("</tr>");
        }

        Hashtable<String, Double> sums = new Hashtable<String, Double>();

        for (int cnt = 0; cnt < valueList.size(); cnt++) {
            Object[] values = valueList.get(cnt);
            String   rowId  = "row_" + values[IDX_DBID];
            String   divId  = "div_" + values[IDX_DBID];
            String   event  = getEventJS(request, entry, values, rowId,
                                         divId);
            sb.append("\n");
            sb.append(HtmlUtil.open(HtmlUtil.TAG_TR,
                                    HtmlUtil.attrs(HtmlUtil.ATTR_VALIGN,
                                        "top") + HtmlUtil.cssClass("dbrow")
                                            + HtmlUtil.id(rowId) + event));
            sb.append(
                "<td width=\"10\" style=\"white-space:nowrap;\"><div id=\""
                + divId + "\" >");

            String dbid  = (String) values[IDX_DBID];
            String cbxId = ARG_DBID + (cnt);
            String call = HtmlUtil.attr(
                              HtmlUtil.ATTR_ONCLICK,
                              HtmlUtil.call(
                                  "checkboxClicked",
                                  HtmlUtil.comma(
                                      "event",
                                      HtmlUtil.squote(ARG_DBID_SELECTED),
                                      HtmlUtil.squote(cbxId))));

            if (doForm) {
                sb.append(HtmlUtil.checkbox(ARG_DBID_SELECTED, dbid, false,
                                            HtmlUtil.id(cbxId) + call));
            }
            if (canEdit) {
                String editUrl = getEditUrl(request, entry, dbid);
                sb.append(
                    HtmlUtil.href(
                        editUrl,
                        HtmlUtil.img(
                            getRepository().getUrlBase()
                            + "/db/database_edit.png", msg("Edit entry"))));
            }

            String viewUrl = getViewUrl(request, entry, dbid);
            sb.append(
                HtmlUtil.href(
                    viewUrl,
                    HtmlUtil.img(
                        getRepository().getUrlBase() + "/db/database_go.png",
                        msg("View entry"))));

            sb.append("</div></td>");



            for (int i = 1; i < columns.size(); i++) {
                if ( !columns.get(i).getCanList()) {
                    continue;
                }
                Column column = columns.get(i);
                if (doSums[i]) {
                    Double d = sums.get(column.getName());
                    if (d == null) {
                        d = new Double(0);
                    }
                    d = new Double(d.doubleValue()
                                   + ((Double) values[column.getOffset()])
                                       .doubleValue());
                    sums.put(column.getName(), d);
                }
                if (column.isString()) {
                    sb.append("<td>");
                } else {
                    sb.append("<td align=\"right\">");
                }


                if (column.isEnumeration()) {
                    StringBuffer prefix = new StringBuffer();
                    String iconID = PROP_CAT_ICON+"." + column.getName();
                    Hashtable<String,String> iconMap = (Hashtable<String,String>) entryProps.get(iconID);
                    if(iconMap!=null) {
                        String icon = iconMap.get((String)values[column.getOffset()]);
                        if(icon!=null) {
                            prefix.append(HtmlUtil.img(getIconUrl(icon)));
                            prefix.append(" ");
                        }
                    }
                    String style = "";
                    String content = "&nbsp;&nbsp;&nbsp;&nbsp;";
                    String colorID = PROP_CAT_COLOR+"." + column.getName();
                    Hashtable<String,String> colorMap = (Hashtable<String,String>) entryProps.get(colorID);
                    if(colorMap!=null) {
                        String bgColor = colorMap.get((String)values[column.getOffset()]);
                        if(bgColor!=null) {
                            style = style+"background-color:" + bgColor;
                            prefix.append(HtmlUtil.span(content, HtmlUtil.style(style)));
                        }
                    }
                    sb.append(prefix.toString());
                }

                sb.append("&nbsp;");
                column.formatValue(entry, sb, Column.OUTPUT_HTML, values);
                sb.append("</td>\n");
            }
            sb.append("</tr>");
        }



        if (valueList.size() > 0) {
            if (doSum) {
                sb.append("<tr><td align=right>" + msgLabel("Sum") + "</td>");
                for (int i = 1; i < columns.size(); i++) {
                    Column column = columns.get(i);
                    if ( !column.getCanList()) {
                        continue;
                    }
                    if (doSums[i]) {
                        Double d = sums.get(column.getName());
                        if (d != null) {
                            sb.append("<td align=right>" + d + "</td>");
                        } else {
                            sb.append("<td align=right>" + 0 + "</td>");
                        }
                    } else {
                        sb.append("<td>&nbsp;</td>");
                    }
                }
            }
            sb.append("</table>\n");
        } else {
            if ( !fromSearch) {
                sb.append(HtmlUtil.br());
                sb.append(
                    getRepository().showDialogNote(
                        msg("No entries in: " + getTitle())));
            } else {
                sb.append(
                    getRepository().showDialogNote(msg("Nothing found")));
            }


        }
        sb.append(HtmlUtil.formClose());
    }


    private String getIconFor(Entry entry, Hashtable entryProps, Object[] values) {
        for(Column column: enumColumns) {
            String value  = column.getString(values);
            String attrIcon = getIconFor(entry, entryProps, column,  value);
            if(attrIcon!=null) return attrIcon;
        }
        return null;
    }



    private String getIconFor(Entry entry, Hashtable entryProps, Column column, String value) {
        return getAttributeFor(entry, entryProps, column, value, PROP_CAT_ICON);
    }

    private String getColorFor(Entry entry, Hashtable entryProps, Column column, String value) {
        return getAttributeFor(entry, entryProps, column, value, PROP_CAT_COLOR);
    }


    private String getAttributeFor(Entry entry, Hashtable entryProps, Column column, String value, String type) {
        if (!column.isEnumeration() || value == null) {
            return null;
        }
        String iconID = type+"." + column.getName();
        Hashtable<String,String> map = (Hashtable<String,String>) entryProps.get(iconID);
        if(map!=null) {
            return  map.get(value);
        }
        return null;
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param dbid _more_
     *
     * @return _more_
     */
    private String getEditUrl(Request request, Entry entry, String dbid) {
        return HtmlUtil.url(request.url(getRepository().URL_ENTRY_SHOW),
                            new String[] {
            ARG_ENTRYID, entry.getId(), ARG_DBID, dbid, ARG_DB_EDITFORM,
            "true"
        });
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param dbid _more_
     *
     * @return _more_
     */
    private String getViewUrl(Request request, Entry entry, String dbid) {
        return HtmlUtil.url(request.url(getRepository().URL_ENTRY_SHOW),
                            new String[] {
            ARG_ENTRYID, entry.getId(), ARG_DBID, dbid, ARG_DB_ENTRY, "true"
        });
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param valueList _more_
     * @param fromSearch _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result handleListMap(Request request, Entry entry,
                                List<Object[]> valueList, boolean fromSearch)
            throws Exception {

        Hashtable entryProps     = getProperties(entry);
        boolean      canEdit = getAccessManager().canEditEntry(request,
                                   entry);
        StringBuffer sb      = new StringBuffer();
        addViewHeader(request, entry, sb, VIEW_MAP, valueList.size(),
                      fromSearch);
        Column  theColumn = null;
        boolean bbox      = true;
        for (Column column : tableHandler.getColumns()) {
            if (column.getType().equals(Column.TYPE_LATLONBBOX)) {
                theColumn = column;
                break;
            }
            if (column.getType().equals(Column.TYPE_LATLON)) {
                theColumn = column;
                bbox      = false;
                break;
            }
        }
        if (theColumn == null) {
            throw new IllegalStateException("No email data found");
        }


        int          width      = 800;
        int          height     = 500;
        MapInfo map = getRepository().getMapManager().createMap(request, width, height, false);
        boolean      makeRectangles = valueList.size() <= 20;

        String       icon           = getRepository().getUrlBase()
                                      + tableIcon;
        StringBuffer rightSide      = new StringBuffer();
        for (Object[] values : valueList) {
            String dbid  = (String) values[IDX_DBID];
            double lat   = 0;
            double lon   = 0;
            double north = 0,
                   west  = 0,
                   south = 0,
                   east  = 0;

            if ( !bbox) {
                double[] ll = theColumn.getLatLon(values);
                lat = ll[0];
                lon = ll[1];
            } else {
                double[] ll = theColumn.getLatLonBbox(values);
                north = ll[0];
                west  = ll[1];
                south = ll[2];
                east  = ll[3];
            }

            if (bbox) {
                map.addBox("", new MapProperties("red", false),  north, west , south,east);
            }
            rightSide.append("\n");
            String iconToUse = icon;
            String attrIcon  = getIconFor(entry, entryProps, values);
            if(attrIcon!=null) {
                iconToUse =  getIconUrl(attrIcon);
                rightSide.append(HtmlUtil.img(iconToUse));
            }
            if (canEdit) {
                String editUrl = getEditUrl(request, entry, dbid);
                rightSide.append(
                    HtmlUtil.href(
                        editUrl,
                        HtmlUtil.img(
                            getRepository().getUrlBase()
                            + "/db/database_edit.png", msg("Edit entry"))));
            }
            String viewUrl = getViewUrl(request, entry, dbid);
            rightSide.append(
                HtmlUtil.href(
                    viewUrl,
                    HtmlUtil.img(
                        getRepository().getUrlBase() + "/db/database_go.png",
                        msg("View entry"))));
            rightSide.append(" ");
            rightSide.append(map.getHiliteHref(dbid, getLabel(entry, values)));


            rightSide.append(HtmlUtil.br());
            String info = getHtml(request, entry, dbid, columns, values);
            info = info.replace("\r", " ");
            info = info.replace("\n", " ");
            info = info.replace("\"", "\\\"");
            if ( !bbox) {
                map.addMarker(dbid,  new LatLonPointImpl(lat,lon), iconToUse, info);
            } else {
                if ( !makeRectangles) {
                    map.addMarker(dbid, new LatLonPointImpl(south, east), iconToUse, info);
                } else {
                    map.addMarker(dbid, new LatLonPointImpl(south+ (north - south) / 2, west + (east - west) / 2), icon, info);
                }
            }
        }

        sb.append(
            "<table cellpadding=5 border=\"0\" width=\"100%\"><tr valign=\"top\"><td width="
            + width + ">");
        map.center();
        sb.append(map.getHtml());
        sb.append("</td><td>");
        sb.append(rightSide);
        sb.append("</td></tr></table>");

        return new Result(getTitle(), sb);

    }



    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param valueList _more_
     * @param fromSearch _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result handleListKml(Request request, Entry entry,
                                List<Object[]> valueList, boolean fromSearch)
            throws Exception {

        Column  theColumn = null;
        boolean bbox      = true;
        for (Column column : tableHandler.getColumns()) {
            if (column.getType().equals(Column.TYPE_LATLONBBOX)) {
                theColumn = column;
                break;
            }
            if (column.getType().equals(Column.TYPE_LATLON)) {
                theColumn = column;
                bbox      = false;
                break;
            }
        }
        if (theColumn == null) {
            throw new IllegalStateException("No email data found");
        }

        Element root   = KmlUtil.kml(entry.getName());
        Element folder = KmlUtil.folder(root, entry.getName(), false);
        KmlUtil.open(folder, true);
        if (entry.getDescription().length() > 0) {
            KmlUtil.description(folder, entry.getDescription());
        }



        for (Object[] values : valueList) {
            String dbid = (String) values[IDX_DBID];
            double lat  = 0;
            double lon  = 0;

            if ( !bbox) {
                double[] ll = theColumn.getLatLon(values);
                lat = ll[0];
                lon = ll[1];
            } else {
                double[] ll = theColumn.getLatLonBbox(values);
                //Lower right
                lat = ll[2];
                lon = ll[3];
            }
            String label = getLabel(entry, values);
            String viewUrl = getRepository().absoluteUrl(getViewUrl(request,
                                 entry, dbid));
            String       href = HtmlUtil.href(viewUrl, label);
            StringBuffer desc = new StringBuffer(href + "<br>");
            getHtml(request, desc, entry, values);
            KmlUtil.placemark(folder, label, desc.toString(), lat, lon, 0,
                              null);
        }
        StringBuffer sb = new StringBuffer(XmlUtil.toString(root));
        return new Result("", sb, "text/kml");
    }




    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param valueList _more_
     * @param fromSearch _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result handleListChart(Request request, Entry entry,
                                  List<Object[]> valueList,
                                  boolean fromSearch)
            throws Exception {

        boolean      canEdit = getAccessManager().canEditEntry(request,
                                   entry);
        StringBuffer sb      = new StringBuffer();
        addViewHeader(request, entry, sb, VIEW_CHART, valueList.size(),
                      fromSearch);

        String            dateFormat = "yyyy/MM/dd HH:mm:ss";
        GregorianCalendar cal = new GregorianCalendar(DateUtil.TIMEZONE_GMT);
        SimpleDateFormat  sdf        = new SimpleDateFormat(dateFormat);


        sb.append("\n\n");
        int height = valueList.size() * 30;
        String fillerIcon = getRepository().getUrlBase()
                            + "/db/bluesquare.png";
        for (Column column : columns) {
            if (column.isNumeric()) {
                List data   = new ArrayList();
                List labels = new ArrayList();
                boolean isPercentage =
                    column.getType().equals(Column.TYPE_PERCENTAGE);
                boolean isInt    = column.getType().equals(Column.TYPE_INT);
                double  maxValue = Double.NaN;
                for (Object[] values : valueList) {
                    if (isPercentage) {
                        maxValue = 100;
                        break;
                    } else if (isInt) {
                        int v =
                            ((Integer) values[column.getOffset()]).intValue();
                        if (maxValue != maxValue) {
                            maxValue = v;
                        }
                        maxValue = Math.max(v, maxValue);
                    } else {
                        double v =
                            ((Double) values[column.getOffset()])
                                .doubleValue();
                        if (maxValue != maxValue) {
                            maxValue = v;
                        } else {
                            maxValue = Math.max(v, maxValue);
                        }
                    }
                }

                sb.append(column.getLabel());
                sb.append("\n");
                sb.append(HtmlUtil.br());
                sb.append("\n");
                sb.append(
                    "<table xwidth=100% border=1 cellspacing=0 cellpadding=1>\n");
                for (Object[] values : valueList) {
                    double value;
                    if (isPercentage) {
                        Double d = (Double) values[column.getOffset()];
                        value = (int) (d.doubleValue() * 100);
                    } else if (isInt) {
                        value =
                            ((Integer) values[column.getOffset()]).intValue();
                    } else {
                        value =
                            ((Double) values[column.getOffset()])
                                .doubleValue();
                    }
                    String url = canEdit
                                 ? getEditUrl(request, entry,
                                     (String) values[IDX_DBID])
                                 : getViewUrl(request, entry,
                                     (String) values[IDX_DBID]);

                    String href  = HtmlUtil.href(url,
                                       getLabel(entry, values));
                    String rowId = "row_" + values[IDX_DBID];
                    String divId = "div_" + values[IDX_DBID];
                    String event = getEventJS(request, entry, values, rowId,
                                       divId);
                    sb.append("<tr " + HtmlUtil.id(rowId)
                              + "> <td  width=10% " + HtmlUtil.id(divId)
                              + event + ">" + HtmlUtil.space(2) + href
                              + "&nbsp;</td>");
                    sb.append("<td align=right width=5%>" + value
                              + "&nbsp;</td><td> ");
                    double percentage = value / maxValue;
                    sb.append("<img src=" + fillerIcon + " height=10 width="
                              + (int) (8 * percentage * 100) + "></td>");
                    sb.append("</tr>\n");
                }
                sb.append("</table>\n");

                /*
                sb.append(column.getLabel());
                sb.append(HtmlUtil.br());

                String chxt = "y";
                if(column.getType().equals(Column.TYPE_PERCENTAGE)) {
                    chxt = chxt+",x";
                }
                sb.append(HtmlUtil.img("http://chart.apis.google.com/chart?chs=400x" + height +"&&cht=bhs&chxt=" + chxt+"&chd=t:" + StringUtil.join(",",data) +
                                       "&chxl=0:|" + StringUtil.join("|",labels)+"|"));
                */
                sb.append(HtmlUtil.p());
            }


        }
        return new Result(getTitle(), sb);

    }



    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param valueList _more_
     * @param fromSearch _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result handleListGrid(Request request, Entry entry,
                                 List<Object[]> valueList, boolean fromSearch)
            throws Exception {

        boolean      canEdit = getAccessManager().canEditEntry(request,
                                   entry);
        StringBuffer sb         = new StringBuffer();
        String       view       = request.getString(ARG_DB_VIEW, "");
        Column       gridColumn = null;
        for (Column column : categoryColumns) {
            if (Misc.equals(view, VIEW_GRID + column.getName())) {
                gridColumn = column;
                break;
            }
        }
        if (gridColumn == null) {
            throw new IllegalStateException("No grid columns defined");
        }
        String links = getHref(request, entry,
                               VIEW_CATEGORY + gridColumn.getName(),
                               msg("Category View"));
        addViewHeader(request, entry, sb, VIEW_GRID + gridColumn.getName(),
                      valueList.size(), fromSearch, links);




        List<String> enumValues = getEnumValues(entry, gridColumn);
        

        sb.append(
            "\n<style type=\"text/css\">\n.gridtable td {padding:5px;padding-bottom:0px;padding-top:8px;}\n.gridon {background: #88C957;}\n.gridoff {background: #eee;}</style>\n");
        sb.append(
            "<table cellspacing=0 cellpadding=0 border=1 width=100% class=\"gridtable\">\n");
        sb.append("<tr>");
        int width = 100/(enumValues.size()+1);
        sb.append(HtmlUtil.col("&nbsp;", HtmlUtil.attr(HtmlUtil.ATTR_WIDTH,width+"%")+HtmlUtil.cssClass("dbtableheader")));
        String key = tableHandler.getTableName() + "." + gridColumn.getName();
        for (String value : enumValues) {
            String searchUrl =
                HtmlUtil.url(request.url(getRepository().URL_ENTRY_SHOW),
                             new String[] {
                ARG_ENTRYID, entry.getId(), ARG_DB_SEARCH, "true", key, value
            });
            sb.append(
                HtmlUtil.col(
                    "&nbsp;" + HtmlUtil.href(searchUrl, value),
                    HtmlUtil.attr(HtmlUtil.ATTR_WIDTH,""+width+"%") + HtmlUtil.cssClass("dbtableheader")));
        }
        for (Object[] valuesArray : valueList) {
            sb.append("<tr>\n");
            String url = canEdit
                         ? getEditUrl(request, entry,
                                      (String) valuesArray[IDX_DBID])
                         : getViewUrl(request, entry,
                                      (String) valuesArray[IDX_DBID]);

            String rowId = "row_" + valuesArray[IDX_DBID];
            String event = getEventJS(request, entry, valuesArray, rowId,
                                      rowId);
            String href = HtmlUtil.href(url, getLabel(entry, valuesArray));
            //            href= HtmlUtil.span(href,HtmlUtil.cssClass("xdbcategoryrow")+);
            sb.append(HtmlUtil.col("&nbsp;" + href,
                                   HtmlUtil.id(rowId) + event
                                   + HtmlUtil.cssClass("dbcategoryrow")));
            String rowValue = (String) valuesArray[gridColumn.getOffset()];
            for (String value : enumValues) {
                if (Misc.equals(value, rowValue)) {
                    sb.append(HtmlUtil.col("&nbsp;",
                                           HtmlUtil.cssClass("dbgridon")));
                } else {
                    sb.append(HtmlUtil.col("&nbsp;",
                                           HtmlUtil.cssClass("dbgridoff")));
                }
            }
        }
        sb.append("</table>");
        return new Result(getTitle(), sb);
    }


        private List<String> getEnumValues(Entry entry, Column column) throws Exception {
            if (column.getType().equals(Column.TYPE_ENUMERATION)) {
                return (List<String>) column.getValues();
            } else {
                return  (List<String>) tableHandler.getEnumValues(column, entry);
            }
        }


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param view _more_
     * @param label _more_
     *
     * @return _more_
     */
    public String getHref(Request request, Entry entry, String view,
                          String label) {
        return HtmlUtil.href(getUrl(request, entry, view), label);
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param view _more_
     * @param label _more_
     * @param suffix _more_
     *
     * @return _more_
     */
    public String getHref(Request request, Entry entry, String view,
                          String label, String suffix) {
        return HtmlUtil.href(getUrl(request, entry, view, suffix), label);
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param view _more_
     *
     * @return _more_
     */
    public String getUrl(Request request, Entry entry, String view) {
        return getUrl(request, entry, view, "");
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param view _more_
     * @param suffix _more_
     *
     * @return _more_
     */
    public String getUrl(Request request, Entry entry, String view,
                         String suffix) {
        return HtmlUtil.url(request.url(getRepository().URL_ENTRY_SHOW)
                            + suffix, new String[] { ARG_ENTRYID,
                entry.getId(), ARG_DB_VIEW, view });
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param valueList _more_
     * @param fromSearch _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result handleListCategory(Request request, Entry entry,
                                     List<Object[]> valueList,
                                     boolean fromSearch)
            throws Exception {
        boolean      canEdit = getAccessManager().canEditEntry(request,
                                   entry);
        StringBuffer sb         = new StringBuffer();
        String       view       = request.getString(ARG_DB_VIEW, "");
        Column       gridColumn = null;
        for (Column column : categoryColumns) {
            if (Misc.equals(view, VIEW_CATEGORY + column.getName())) {
                gridColumn = column;
                break;
            }
        }
        if (gridColumn == null) {
            throw new IllegalStateException("No grid columns defined");
        }
        String links = getHref(request, entry,
                               VIEW_GRID + gridColumn.getName(),
                               msg("Grid View"));
        addViewHeader(request, entry, sb,
                      VIEW_CATEGORY + gridColumn.getName(), valueList.size(),
                      fromSearch, links);

        Hashtable<String, StringBuffer> map = new Hashtable<String,
                                                  StringBuffer>();
        List<String> rowValues = new ArrayList<String>();
        int          cnt       = 0;

        for (Object[] valuesArray : valueList) {
            String url = canEdit
                         ? getEditUrl(request, entry,
                                      (String) valuesArray[IDX_DBID])
                         : getViewUrl(request, entry,
                                      (String) valuesArray[IDX_DBID]);
            String href = HtmlUtil.href(url, getLabel(entry, valuesArray));
            String rowValue     =
                (String) valuesArray[gridColumn.getOffset()];
            StringBuffer buffer = map.get(rowValue);
            if (buffer == null) {
                map.put(rowValue, buffer = new StringBuffer());
                rowValues.add(rowValue);
            }
            String rowId = "row_" + valuesArray[IDX_DBID];
            String event = getEventJS(request, entry, valuesArray, rowId,
                                      rowId);
            buffer.append(HtmlUtil.div(href,
                                       HtmlUtil.cssClass("dbcategoryrow")
                                       + HtmlUtil.id(rowId) + event));
            cnt++;
        }
        for (String rowValue : rowValues) {
            String block = HtmlUtil.makeShowHideBlock(
                               rowValue,
                               HtmlUtil.div(
                                   map.get(rowValue).toString(),
                                   HtmlUtil.cssClass(
                                       "dbcategoryblock")), false);
            sb.append(block);
        }

        return new Result(getTitle(), sb);
    }


    /**
     * _more_
     *
     * @param column _more_
     *
     * @return _more_
     */
    private boolean isDataColumn(Column column) {
        if (column.getName().equals(COL_DBID)
                || column.getName().equals(COL_DBUSER)
                || column.getName().equals(COL_DBCREATEDATE)
                || column.getName().equals(COL_DBPROPS)) {
            return false;
        }
        return true;
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param valueList _more_
     * @param fromSearch _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result handleListMotionChart(Request request, Entry entry,
                                        List<Object[]> valueList,
                                        boolean fromSearch)
            throws Exception {
        StringBuffer sb = new StringBuffer();
        addViewHeader(request, entry, sb, VIEW_CHART, valueList.size(),
                      fromSearch);

        sb.append(
            "<script type=\"text/javascript\" src=\"http://www.google.com/jsapi\"></script>\n");
        sb.append(
            "<script type=\"text/javascript\">\ngoogle.load('visualization', '1', {'packages':['motionchart']});\ngoogle.setOnLoadCallback(drawChart);\nfunction drawChart() {\n        var data = new google.visualization.DataTable();\n");
        StringBuffer init      = new StringBuffer();

        int          columnCnt = 0;
        init.append("data.addColumn('string', 'Name');\n");
        init.append("data.addColumn('date', 'Date');\n");

        String            dateFormat = "yyyy/MM/dd HH:mm:ss";
        GregorianCalendar cal = new GregorianCalendar(DateUtil.TIMEZONE_GMT);
        SimpleDateFormat  sdf        = new SimpleDateFormat(dateFormat);

        Column            dateColumn = null;
        for (Column column : columns) {
            if ( !isDataColumn(column)) {
                continue;
            }
            if (column.getType().equals(Column.TYPE_DATE)) {
                if (dateColumn == null) {
                    dateColumn = column;
                }
                continue;
            }
            String varName = column.getName();
            if (column.isNumeric()) {
                init.append("data.addColumn('number', '" + varName + "');\n");
            } else if (column.isString()) {
                init.append("data.addColumn('string', '" + varName + "');\n");
            } else if (column.getType().equals(Column.TYPE_DATE)) {
                init.append("data.addColumn('date', '" + varName + "');\n");
            }
        }
        sb.append("data.addRows(" + valueList.size() + ");\n");
        sb.append(init);
        int row = 0;
        for (Object[] values : valueList) {
            columnCnt = 0;
            String label = getLabel(entry, values);
            sb.append("data.setValue(" + row + ", " + columnCnt + ","
                      + HtmlUtil.squote(label) + ");\n");
            columnCnt++;

            Date date = (Date) values[dateColumn.getOffset()];
            cal.setTime(date);
            sb.append("theDate = new Date(" + cal.get(cal.YEAR) + ","
                      + cal.get(cal.MONTH) + "," + cal.get(cal.DAY_OF_MONTH)
                      + ");\n");

            sb.append("theDate.setHours(" + cal.get(cal.HOUR) + ","
                      + cal.get(cal.MINUTE) + "," + cal.get(cal.SECOND) + ","
                      + cal.get(cal.MILLISECOND) + ");\n");

            sb.append("data.setValue(" + row + ", " + columnCnt
                      + ", theDate);\n");
            columnCnt++;

            for (Column column : columns) {
                if ( !isDataColumn(column)) {
                    continue;
                }
                if (column == dateColumn) {
                    continue;
                }
                if (column.isNumeric()) {
                    sb.append("data.setValue(" + row + ", " + columnCnt + ","
                              + values[column.getOffset()] + ");\n");
                    columnCnt++;
                } else if (column.isString()) {
                    sb.append("data.setValue(" + row + ", " + columnCnt + ","
                              + HtmlUtil.squote(""
                                  + values[column.getOffset()]) + ");\n");
                    columnCnt++;
                } else if (column.getType().equals(Column.TYPE_DATE)) {
                    columnCnt++;
                }
            }
            row++;
        }



        sb.append(
            "var chart = new google.visualization.MotionChart(document.getElementById('chart_div'));\n");
        sb.append("chart.draw(data, {width: 800, height:500});\n");
        sb.append("}\n");
        sb.append("</script>\n");
        sb.append(
            "<div id=\"chart_div\" style=\"width: 800px; height: 500px;\"></div>\n");

        return new Result(getTitle(), sb);
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param valueList _more_
     * @param fromSearch _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result handleListTimeline(Request request, Entry entry,
                                     List<Object[]> valueList,
                                     boolean fromSearch)
            throws Exception {
        Column theColumn = null;
        for (Column column : columns) {
            if (column.getType().equals(Column.TYPE_DATE)) {
                theColumn = column;
                break;
            }
        }
        if (theColumn == null) {
            throw new IllegalStateException("No date data found");
        }



        StringBuffer sb = new StringBuffer();
        addViewHeader(request, entry, sb, VIEW_TIMELINE, valueList.size(),
                      fromSearch);
        //        String links = getHref(request, entry,VIEW_CALENDAR, msg("Calendar"));
        //        sb.append(HtmlUtil.center(links));
        String timelineAppletTemplate =
            getRepository().getResource(PROP_HTML_TIMELINEAPPLET);
        List times  = new ArrayList();
        List labels = new ArrayList();
        List ids    = new ArrayList();
        for (Object[] values : valueList) {
            times.add(SqlUtil.format((Date) values[theColumn.getOffset()]));
            String label = getLabel(entry, values).trim();
            if (label.length() == 0) {
                label = "NA";
            }
            label = label.replaceAll(",", "_");
            label = label.replaceAll("\"", " ");
            labels.add(label);
            ids.add(values[IDX_DBID]);
        }
        String tmp = StringUtil.replace(timelineAppletTemplate, "${times}",
                                        StringUtil.join(",", times));
        tmp = StringUtil.replace(tmp, "${root}",
                                 getRepository().getUrlBase());
        tmp = StringUtil.replace(tmp, "${labels}",
                                 StringUtil.join(",", labels));
        tmp = StringUtil.replace(tmp, "${ids}", StringUtil.join(",", ids));
        String formUrl = request.url(getRepository().URL_ENTRY_SHOW);
        String url =
            HtmlUtil.url(request.url(getRepository().URL_ENTRY_SHOW),
                         new String[] { ARG_ENTRYID,
                                        entry.getId(), ARG_DBIDS, "%ids%" });

        tmp = StringUtil.replace(tmp, "${loadurl}", url);
        sb.append(tmp);
        return new Result("", sb);
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param valueList _more_
     * @param fromSearch _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result handleListCalendar(Request request, Entry entry,
                                     List<Object[]> valueList,
                                     boolean fromSearch)
            throws Exception {
        boolean      canEdit = getAccessManager().canEditEntry(request,
                                   entry);
        StringBuffer sb      = new StringBuffer();
        String links = getHref(request, entry, VIEW_TIMELINE,
                               msg("Timeline")) + "&nbsp;|&nbsp;"
                                   + getHref(request, entry, VIEW_ICAL,
                                             msg("ICAL"));
        addViewHeader(request, entry, sb, VIEW_CALENDAR, valueList.size(),
                      fromSearch, links);



        CalendarOutputHandler calendarOutputHandler =
            (CalendarOutputHandler) getRepository().getOutputHandler(
                CalendarOutputHandler.OUTPUT_CALENDAR);

        List<CalendarOutputHandler.CalendarEntry> calEntries =
            new ArrayList<CalendarOutputHandler.CalendarEntry>();

        Column theColumn = null;
        for (Column column : columns) {
            if (column.getType().equals(Column.TYPE_DATE)) {
                theColumn = column;
                break;
            }
        }
        if (theColumn == null) {
            throw new IllegalStateException("No date data found");
        }
        for (Object[] values : valueList) {
            String       dbid  = (String) values[IDX_DBID];
            Date         date  = (Date) values[theColumn.getOffset()];
            String       url   = getViewUrl(request, entry, dbid);
            String       label = getLabel(entry, values).trim();
            StringBuffer html  = new StringBuffer();
            if (label.length() == 0) {
                label = "NA";
            }
            String href = HtmlUtil.href(url, label);

            if (canEdit) {
                String editUrl = getEditUrl(request, entry, dbid);
                href = HtmlUtil.href(
                    editUrl,
                    HtmlUtil.img(
                        getRepository().getUrlBase()
                        + "/db/database_edit.png", msg("Edit entry"))) + " "
                            + href;
            }
            //            html.append(href);
            String rowId = "row_" + values[IDX_DBID];
            String event = getEventJS(request, entry, values, rowId, rowId);
            href = HtmlUtil.div(href,
                                HtmlUtil.cssClass("dbcategoryrow")
                                + HtmlUtil.id(rowId) + event);
            //            getHtml(request, html, entry, values);
            String block = HtmlUtil.makeShowHideBlock(href, html.toString(),
                               false);
            calEntries.add(new CalendarOutputHandler.CalendarEntry(date,
                    href, href));
        }
        calendarOutputHandler.outputCalendar(request, calEntries, sb);

        return new Result(getTitle(), sb);
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param valueList _more_
     * @param fromSearch _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result handleListStickyNotes(Request request, Entry entry,
                                        List<Object[]> valueList,
                                        boolean fromSearch)
            throws Exception {

        Hashtable entryProps     = getProperties(entry);
        String stickyLabelString =
            (String) entryProps.get(PROP_STICKY_LABELS);


        boolean      canEdit = getAccessManager().canEditEntry(request,
                                   entry);
        StringBuffer sb      = new StringBuffer();
        StringBuffer js      = new StringBuffer();
        addViewHeader(request, entry, sb, VIEW_STICKYNOTES, valueList.size(),
                      fromSearch);


        //        String links = getHref(request, entry,VIEW_TABLE, msg("Table"));
        //        sb.append(HtmlUtil.center(links));


        int cnt    = 0;
        int poscnt = 0;
        sb.append(
            HtmlUtil.importJS(getRepository().fileUrl("/db/dom-drag.js")));
        for (Object[] values : valueList) {
            Hashtable props = getProps(values);
            String    dbid  = (String) values[IDX_DBID];
            String    url   = getViewUrl(request, entry, dbid);
            String    label = getLabel(entry, values).trim();
            if (label.length() == 0) {
                label = "NA";
            }
            String href = HtmlUtil.href(url, label);


            if (canEdit) {
                String editUrl = getEditUrl(request, entry, dbid);
                href = HtmlUtil.href(
                    editUrl,
                    HtmlUtil.img(
                        getRepository().getUrlBase()
                        + "/db/database_edit.png", msg("Edit entry"))) + " "
                            + href;
            }
            int top  = Misc.getProperty(props, "posy", 150 + poscnt * 20);
            int left = Misc.getProperty(props, "posx", 150 + poscnt * 20);
            if ((props == null) || (props.get("posx") == null)) {
                poscnt++;
            }
            String info = getHtml(request, entry, dbid, columns, values);
            String contents = href
                              + HtmlUtil.makeShowHideBlock("...", info,
                                  false);
            sb.append(HtmlUtil.div(contents,
                                   HtmlUtil.cssClass("dbstickynote")
                                   + HtmlUtil.id(dbid) + " style=\"top:"
                                   + top + "px;  left:" + left + "px;\""));

            cnt++;
            String jsid = "id" + cnt;
            js.append("var " + jsid + " = '" + dbid + "';\n");
            js.append("var draggableDiv = document.getElementById(" + jsid
                      + ");\n");
            js.append("Drag.init(draggableDiv);\n");
            if (canEdit) {
                String posUrl =
                    HtmlUtil.url(request.url(getRepository().URL_ENTRY_SHOW),
                                 new String[] {
                    ARG_ENTRYID, entry.getId(), ARG_DBID, dbid, ARG_DB_SETPOS,
                    "true"
                });

                js.append(
                    "draggableDiv.onDragEnd  = function(x,y){stickyDragEnd("
                    + jsid + "," + HtmlUtil.squote(posUrl) + ");}\n");
            }
        }

        if (stickyLabelString != null) {
            int labelCnt = 0;
            for (String label :
                    StringUtil.split(stickyLabelString, "\n", true, true)) {
                String id   = "label_" + label;
                int    top  = 100;
                int    left = 150 + labelCnt * 30;
                String posx = (String) entryProps.get(PROP_STICKY_POSX + "."
                                  + label);
                String posy = (String) entryProps.get(PROP_STICKY_POSY + "."
                                  + label);
                if ((posx != null) && (posy != null)) {
                    top  = Integer.parseInt(posy);
                    left = Integer.parseInt(posx);
                } else {
                    labelCnt++;
                }
                String text = label;
                sb.append(HtmlUtil.div(text,
                                       HtmlUtil.cssClass("dbstickylabel")
                                       + HtmlUtil.id(id) + " style=\"top:"
                                       + top + "px;  left:" + left
                                       + "px;\""));

                js.append("var draggableDiv = document.getElementById("
                          + HtmlUtil.squote(id) + ");\n");
                js.append("Drag.init(draggableDiv);\n");
                if (canEdit) {
                    String posUrl =
                        HtmlUtil.url(
                            request.url(getRepository().URL_ENTRY_SHOW),
                            new String[] {
                        ARG_ENTRYID, entry.getId(), ARG_DB_STICKYLABEL, label,
                        ARG_DB_SETPOS, "true"
                    });

                    js.append(
                        "draggableDiv.onDragEnd  = function(x,y){stickyDragEnd("
                        + HtmlUtil.squote(id) + "," + HtmlUtil.squote(posUrl)
                        + ");}\n");
                }
            }
        }

        sb.append(HtmlUtil.script(js.toString()));
        return new Result(getTitle(), sb);
    }





    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param valueList _more_
     * @param fromSearch _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result handleListIcal(Request request, Entry entry,
                                 List<Object[]> valueList, boolean fromSearch)
            throws Exception {
        SimpleDateFormat sdf =
            RepositoryUtil.makeDateFormat("yyyyMMdd'T'HHmmss");
        StringBuffer sb = new StringBuffer();
        sb.append("BEGIN:VCALENDAR\n");
        sb.append("PRODID:-//Unidata/UCAR//RAMADDA Calendar//EN\n");
        sb.append("VERSION:2.0\n");
        sb.append("CALSCALE:GREGORIAN\n");
        sb.append("METHOD:PUBLISH\n");
        for (Object[] values : valueList) {
            String dbid        = (String) values[IDX_DBID];
            Date   date1       =
                (Date) values[dateColumns.get(0).getOffset()];
            Date   date2       = (Date) values[(dateColumns.size() > 1)
                    ? dateColumns.get(1).getOffset()
                    : dateColumns.get(0).getOffset()];
            String dateString1 = sdf.format(date1) + "Z";
            String dateString2 = sdf.format(date2) + "Z";
            String url         = getViewUrl(request, entry, dbid);
            url = repository.absoluteUrl(url);
            String label = getLabel(entry, values).trim();

            if (label.length() == 0) {
                label = "NA";
            }

            sb.append("BEGIN:VEVENT\n");
            sb.append("UID:" + values[IDX_DBID] + "\n");
            sb.append("CREATED:" + dateString1 + "\n");
            sb.append("DTSTAMP:" + dateString1 + "\n");
            sb.append("DTSTART:" + dateString1 + "\n");
            sb.append("DTEND:" + dateString2 + "\n");
            sb.append("SUMMARY:" + label + "\n");
            sb.append("ATTACH:" + url + "\n");
            sb.append("END:VEVENT\n");
        }
        sb.append("END:VCALENDAR\n");
        return new Result("", sb, "text/calendar");
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param clause _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public List<Object[]> readValues(Request request, Entry entry,
                                     Clause clause)
            throws Exception {
        if (request.exists(ARG_DBIDS)) {
            List<Object[]> result = new ArrayList<Object[]>();
            String         ids    = request.getString(ARG_DBIDS, "");
            request.remove(ARG_DBIDS);
            for (String id : StringUtil.split(ids, ",", true, true)) {
                result.addAll(readValues(request, entry,
                                         makeClause(entry, id)));
            }
            request.put(ARG_DBIDS, ids);
            return result;
        }


        List<String> colNames = tableHandler.getColumnNames();
        String       extra    = "";

        if ((dfltSortColumn != null) && !request.defined(ARG_DB_SORTBY)) {
            request.put(ARG_DB_SORTBY, dfltSortColumn.getName());
            request.put(ARG_DB_SORTDIR, dfltSortAsc
                                        ? "asc"
                                        : "desc");
        }


        if (request.defined(ARG_DB_SORTBY)) {
            String by     = request.getString(ARG_DB_SORTBY, "");
            Column column = columnMap.get(by);
            if (column != null) {
                by = column.getSortByColumn();
                boolean asc = request.getString(ARG_DB_SORTDIR,
                                  "asc").equals("asc");
                extra += " order by " + by + (asc
                        ? " asc "
                        : " desc ");
            }
        }
        int max  = request.get(ARG_MAX, 100);
        int skip = request.get(ARG_SKIP, 0);
        extra += getDatabaseManager().getLimitString(skip, max);
        return readValues(clause, extra, max);
    }


    /**
     * _more_
     *
     * @param clause _more_
     * @param extra _more_
     * @param max _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    private List<Object[]> readValues(Clause clause, String extra, int max)
            throws Exception {
        List<Object[]> result   = new ArrayList<Object[]>();

        List<String>   colNames = tableHandler.getColumnNames();
        Statement stmt = getDatabaseManager().select(SqlUtil.comma(colNames),
                             Misc.newList(tableHandler.getTableName()),
                             clause, extra, max);

        try {
            SqlUtil.Iterator iter = getDatabaseManager().getIterator(stmt);
            ResultSet        results;
            while ((results = iter.getNext()) != null) {
                Object[] values   = tableHandler.makeEntryValueArray();
                int      valueIdx = 2;
                for (Column column : columns) {
                    valueIdx = column.readValues(results, values, valueIdx);
                }
                result.add(values);
            }
        } finally {
            getRepository().getDatabaseManager().closeAndReleaseConnection(
                stmt);
        }
        return result;
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
        super.deleteEntry(request, statement, id);
        String query = SqlUtil.makeDelete(tableHandler.getTableName(),
                                          COL_ID, SqlUtil.quote(id));

        statement.execute(query);
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param sb _more_
     */
    private void makeForm(Request request, Entry entry, StringBuffer sb) {
        String formUrl = request.url(getRepository().URL_ENTRY_SHOW);
        sb.append(HtmlUtil.formPost(formUrl));
        sb.append(HtmlUtil.hidden(ARG_ENTRYID, entry.getId()));
    }




    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param dbid _more_
     * @param forEdit _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result handleForm(Request request, Entry entry, String dbid,
                             boolean forEdit)
            throws Exception {

        List<String> colNames = tableHandler.getColumnNames();
        StringBuffer sb       = new StringBuffer();
        addViewHeader(request, entry, sb, ((dbid == null)
                                           ? VIEW_NEW
                                           : ""), 0, false);

        StringBuffer formBuffer = new StringBuffer();

        makeForm(request, entry, formBuffer);


        Object[] values = null;
        if (dbid != null) {
            values = tableHandler.getValues(makeClause(entry, dbid));
            formBuffer.append(HtmlUtil.hidden(ARG_DBID, dbid));
            formBuffer.append(HtmlUtil.hidden(ARG_DBID_SELECTED, dbid));
        }


        StringBuffer buttons = new StringBuffer();
        if (forEdit) {
            if (dbid == null) {
                buttons.append(HtmlUtil.submit(msg("Create entry"),
                        ARG_DB_CREATE));
            } else {
                buttons.append(HtmlUtil.submit(msg("Edit entry"),
                        ARG_DB_EDIT));
                buttons.append(HtmlUtil.submit(msg("Copy entry"),
                        ARG_DB_COPY));
                buttons.append(HtmlUtil.submit(msg("Delete entry"),
                        ARG_DB_DELETE));
            }
        }
        buttons.append(HtmlUtil.submit(msg("Cancel"), ARG_DB_LIST));



        formBuffer.append(buttons);

        formBuffer.append(HtmlUtil.formTable());
        tableHandler.addColumnsToEntryForm(request, formBuffer, entry,
                                           values);


        formBuffer.append(HtmlUtil.formTableClose());
        formBuffer.append(buttons);
        formBuffer.append(HtmlUtil.formClose());

        if (false && forEdit && (dbid == null)) {
            StringBuffer bulk = new StringBuffer();
            makeForm(request, entry, bulk);
            StringBuffer bulkButtons = new StringBuffer();
            bulkButtons.append(HtmlUtil.submit(msg("Create entries"),
                    ARG_DB_CREATE));
            bulkButtons.append(HtmlUtil.submit(msg("Cancel"), ARG_DB_LIST));
            bulk.append(bulkButtons);
            bulk.append(HtmlUtil.br());
            List colIds = new ArrayList();

            int  cnt    = 0;
            for (Column column : columns) {
                if (cnt > 0) {
                    colIds.add(new TwoFacedObject(column.getLabel(),
                            column.getName()));
                }
                cnt++;
            }
            cnt = 0;
            for (Column column : columns) {
                if (cnt > 0) {
                    bulk.append(HtmlUtil.select(ARG_DB_BULKCOL + cnt, colIds,
                            columns.get(cnt).getName()));
                }
                cnt++;
            }
            bulk.append(HtmlUtil.br());
            bulk.append(HtmlUtil.textArea(ARG_DB_BULK, "", 10, 80));
            bulk.append(HtmlUtil.formClose());
            String contents =
                HtmlUtil.makeTabs(Misc.newList(msg("Form"),
                    msg("Bulk Create")), Misc.newList(formBuffer.toString(),
                        bulk.toString()), true, "tab_content");
            sb.append(contents);
        } else {
            sb.append(formBuffer);
        }

        return new Result(getTitle(), sb);
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param dbid _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result handleView(Request request, Entry entry, String dbid)
            throws Exception {
        boolean      asXml = request.getString("result", "").equals("xml");
        StringBuffer sb    = new StringBuffer();
        if ( !asXml) {
            addViewHeader(request, entry, sb, "", 0, false);
        }



        Object[] values = tableHandler.getValues(makeClause(entry, dbid));

        getHtml(request, sb, entry, values);
        if (asXml) {
            StringBuffer xml = new StringBuffer("<contents>\n");
            XmlUtil.appendCdata(xml, sb.toString());
            xml.append("</contents>");
            return new Result("", xml, "text/xml");
        }

        return new Result(getTitle(), sb);
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param sb _more_
     * @param entry _more_
     * @param values _more_
     *
     * @throws Exception _more_
     */
    private void getHtml(Request request, StringBuffer sb, Entry entry,
                         Object[] values)
            throws Exception {
        sb.append(HtmlUtil.formTable());
        for (Column column : columns) {
            if ( !isDataColumn(column)) {
                continue;
            }
            StringBuffer tmpSb = new StringBuffer();
            column.formatValue(entry, tmpSb, Column.OUTPUT_HTML, values);
            sb.append(formEntry(request, column.getLabel() + ":",
                                         tmpSb.toString()));
        }
        sb.append(HtmlUtil.formTableClose());

    }

    /**
     * _more_
     *
     * @param entry _more_
     * @param values _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    private String getLabel(Entry entry, Object[] values) throws Exception {
        String lbl = getLabelInner(entry, values);
        if ((lbl == null) || (lbl.trim().length() == 0)) {
            lbl = "---";
        }
        return lbl;
    }

    /**
     * _more_
     *
     * @param entry _more_
     * @param values _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    private String getLabelInner(Entry entry, Object[] values)
            throws Exception {
        StringBuffer sb = new StringBuffer();
        if (labelColumn != null) {
            labelColumn.formatValue(entry, sb, Column.OUTPUT_HTML, values);
            return sb.toString();
        }
        for (Column column : columns) {
            if ( !isDataColumn(column)) {
                continue;
            }
            String type = column.getType();
            if (type.equals(Column.TYPE_STRING)
                    || type.equals(Column.TYPE_ENUMERATION)
                    || type.equals(Column.TYPE_URL)
                    || type.equals(Column.TYPE_EMAIL)
                    || type.equals(Column.TYPE_ENUMERATIONPLUS)) {
                column.formatValue(entry, sb, Column.OUTPUT_HTML, values);
                String label = sb.toString();
                if (label.length() > 0) {
                    return label;
                }
            }
        }

        return "";

    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param dbid _more_
     * @param columns _more_
     * @param values _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    private String getHtml(Request request, Entry entry, String dbid,
                           List<Column> columns, Object[] values)
            throws Exception {
        StringBuffer sb = new StringBuffer();
        sb.append(HtmlUtil.formTable());
        int valueIdx = 0;
        //        String url = getViewUrl(request,  entry,  dbid);
        //        sb.append(HtmlUtil.formEntry(HtmlUtil.href(url,
        //                                                   HtmlUtil.img(getRepository().getUrlBase()+"/db/database_go.png",msg("View entry"))),""));



        for (Column column : columns) {
            if ( !isDataColumn(column)) {
                continue;
            }
            StringBuffer tmpSb = new StringBuffer();
            column.formatValue(entry, tmpSb, Column.OUTPUT_HTML, values);
            sb.append(formEntry(request, column.getLabel() + ":",
                                         tmpSb.toString()));
        }
        sb.append(HtmlUtil.formTableClose());
        return sb.toString();
    }


}
