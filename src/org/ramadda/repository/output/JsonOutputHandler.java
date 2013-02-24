/*
* Copyright 2008-2012 Jeff McWhirter/ramadda.org
*                     Don Murray/CU-CIRES
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

package org.ramadda.repository.output;


import com.google.gson.*;

import org.ramadda.repository.*;
import org.ramadda.repository.auth.*;
import org.ramadda.repository.metadata.Metadata;
import org.ramadda.repository.type.Column;

import org.ramadda.util.HtmlUtils;
import org.ramadda.util.Json;


import org.w3c.dom.*;

import ucar.unidata.data.gis.KmlUtil;

import ucar.unidata.geoloc.Bearing;
import ucar.unidata.geoloc.LatLonPointImpl;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;
import ucar.unidata.xml.XmlUtil;


import java.io.*;

import java.io.File;

import java.text.DateFormat;

import java.text.SimpleDateFormat;
import java.text.StringCharacterIterator;

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;



/**
 *
 *
 * @author RAMADDA Development Team
 * @version $Revision: 1.3 $
 */
public class JsonOutputHandler extends OutputHandler {

    // Parameters for the output

    /** _more_ */
    public static final String ARG_EXTRACOLUMNS = "extracolumns";

    /** _more_ */
    public static final String ARG_METADATA = "metadata";

    /** _more_ */
    public static final String ARG_LINKS = "links";

    /** _more_ */
    public static final String ARG_ONLYENTRY = "onlyentry";


    /** _more_ */
    public static final OutputType OUTPUT_JSON =
        new OutputType("JSON", "json",
                       OutputType.TYPE_FEEDS | OutputType.TYPE_FORSEARCH, "",
                       ICON_JSON);



    /**
     * _more_
     *
     * @param repository _more_
     * @param element _more_
     * @throws Exception _more_
     */
    public JsonOutputHandler(Repository repository, Element element)
            throws Exception {
        super(repository, element);
        addType(OUTPUT_JSON);
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param state _more_
     * @param links _more_
     *
     *
     * @throws Exception _more_
     */
    public void getEntryLinks(Request request, State state, List<Link> links)
            throws Exception {
        if ((state.getEntry() != null)
                && (state.getEntry().getName() != null)) {
            links.add(
                makeLink(
                    request, state.getEntry(), OUTPUT_JSON,
                    "/" + IOUtil.stripExtension(state.getEntry().getName())
                    + ".json"));
        }
    }




    /**
     * _more_
     *
     * @param request _more_
     * @param outputType _more_
     * @param group _more_
     * @param subGroups _more_
     * @param entries _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result outputGroup(Request request, OutputType outputType,
                              Entry group, List<Entry> subGroups,
                              List<Entry> entries)
            throws Exception {

        List<Entry> allEntries = new ArrayList<Entry>();
        if (request.get(ARG_ONLYENTRY, false)) {
            allEntries.add(group);
        } else {
            allEntries.addAll(subGroups);
            allEntries.addAll(entries);
        }
        StringBuffer sb = new StringBuffer();
        makeJson(request, allEntries, sb);
        return new Result("", sb, "application/json");
    }


    public void makeJson(Request request, List<Entry> entries, StringBuffer sb)
        throws Exception {
        List<String> items = new ArrayList<String>();
        for (Entry entry : entries) {
            items.add(toJson(request, entry));
        }
        sb.append(Json.list(items));
    }



    /** _more_ */
    private static SimpleDateFormat sdf;

    /**
     * _more_
     *
     * @param dttm _more_
     *
     * @return _more_
     */
    private String formatDate(long dttm) {
        if (sdf == null) {
            sdf = RepositoryUtil.makeDateFormat("yyyy-MM-dd HH:mm:ss");
        }
        synchronized (sdf) {
            return sdf.format(new Date(dttm));
        }
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param sb _more_
     *
     * @throws Exception _more_
     */
    private String toJson(Request request, Entry entry)
            throws Exception {
        List<String> items = new ArrayList<String>();
        Json.quoteAttr(items, "id", entry.getId());
        Json.quoteAttr(items, "name", Json.cleanString(entry.getName()));
        Json.quoteAttr(items, "description", Json.cleanString(entry.getDescription()));
        Json.quoteAttr(items, "type", entry.getType());

        //
        if (entry.isGroup()) {
            Json.attr(items, "isGroup", "true");
        } else {
            Json.attr(items, "isGroup", "false");
        }

        Json.quoteAttr(items, "icon",
                       getRepository().getEntryManager().getIconUrl(request, entry));

        Json.quoteAttr(items, "parent", entry.getParentEntryId());
        Json.quoteAttr(items, "user", entry.getUser().getId());
        if(entry.getResource().isUrl()) {
            Json.quoteAttr(items, "url", entry.getResource().getPath());
        }
        Json.quoteAttr(items, "createDate", formatDate(entry.getCreateDate()));
        Json.quoteAttr(items, "startDate", formatDate(entry.getStartDate()));
        Json.quoteAttr(items, "endDate", formatDate(entry.getEndDate()));


        if (entry.hasNorth()) {
            Json.attr(items, "north", "" + entry.getNorth());
        } else {
            Json.attr(items, "north", "-9999");
        }

        if (entry.hasSouth()) {
            Json.attr(items, "south", "" + entry.getSouth());
        } else {
            Json.attr(items, "south", "-9999");
        }

        if (entry.hasEast()) {
            Json.attr(items, "east", "" + entry.getEast());
        } else {
            Json.attr(items, "east", "-9999");
        }

        if (entry.hasWest()) {
            Json.attr(items, "west", "" + entry.getWest());
        } else {
            Json.attr(items, "west", "-9999");
        }

        if (entry.hasAltitudeTop()) {
            Json.attr(items, "altitudeTop", "" + entry.getAltitudeTop());
        } else {
            Json.attr(items, "altitudeTop", "-9999");
        }

        if (entry.hasAltitudeBottom()) {
            Json.attr(items, "altitudeBottom", "" + entry.getAltitudeBottom());
        } else {
            Json.attr(items, "altitudeBottom", "-9999");
        }



        Resource resource = entry.getResource();

        //TODO: add services
        if (resource != null) {
            if (resource.isUrl()) {

                String temp = Json.cleanString(resource.getPath());
                if (temp == null) {
                    Json.quoteAttr(items, "filename", "");
                } else {
                    Json.quoteAttr(items, "filename", java.net.URLEncoder.encode(temp));
                }

                Json.attr(items, "filesize", "" + resource.getFileSize());
                Json.quoteAttr(items, "md5", "");
                //TODO MATIAS            } else if(resource.isFileNoCheck()) {
            } else if (resource.isFile()) {
                Json.quoteAttr(items, "filename",
                       getStorageManager().getFileTail(entry));
                Json.attr(items, "filesize", "" + resource.getFileSize());
                if (resource.getMd5() != null) {
                    Json.quoteAttr(items, "md5", resource.getMd5());
                } else {
                    Json.quoteAttr(items, "md5", "");
                }
            }
        } else {
            Json.quoteAttr(items, "filename", "no resource");
            Json.attr(items, "filesize", "0");
            Json.quoteAttr(items, "md5", "");
        }


        // Add special columns to the entries depending on the type
        if (request.get(ARG_EXTRACOLUMNS, true)) {
            List<String> extraColumns = new ArrayList<String>();
            List<String> columnNames = new ArrayList<String>();
            List<String> columnLabels = new ArrayList<String>();
            Object[] extraParameters = entry.getValues();
            if (extraParameters != null) {
                List<Column> columns = entry.getTypeHandler().getColumns();
                for (int i = 0; i < extraParameters.length; i++) {
                    Column column  = columns.get(i);
                    String name = column.getName();
                    if(name.endsWith("_id")) continue;
                    String value = Json.cleanAndQuote(entry.getValue(i, ""));
                    columnNames.add(name);
                    columnLabels.add(column.getLabel());
                    Json.attr(items, "column." + name, value);
                    extraColumns.add(Json.map(new String[]{name,
                                                           value}));
                }
            }
            Json.attr(items, "columnNames", Json.list(columnNames,true));
            Json.attr(items, "columnLabels", Json.list(columnLabels,true));
            Json.attr(items, "extraColumns", Json.list(extraColumns));
        }



        if (request.get(ARG_LINKS, false)) {
            List<String> links = new ArrayList<String>();
            for(Link link:  repository.getEntryManager().getEntryLinks(request,
                                                                       entry)) {
                OutputType outputType = link.getOutputType();
                links.add(Json.map(new String[]{
                            "label",Json.cleanAndQuote(link.getLabel()),
                            "type",outputType==null?"unknown":
                            Json.cleanAndQuote(outputType.toString()),
                            "url", link.getUrl()==null?Json.quote(""):
                            Json.quote(java.net.URLEncoder.encode(
                                                                  Json.cleanString(link.getUrl()))),
                            "icon", Json.quote(link.getIcon())
                        }));
            }
            Json.attr(items,"links", Json.list(links));
        }

        if (request.get(ARG_METADATA, true)) {
            List<Metadata> metadataList =
                getMetadataManager().getMetadata(entry);
            List<String>metadataItems = new ArrayList<String>();
            if (metadataList != null) {
                for (Metadata metadata:metadataList) {
                    List<String>mapItems = new ArrayList<String>();
                    Json.quoteAttr(mapItems,"id", metadata.getId());
                    Json.quoteAttr(mapItems,"type", metadata.getType());
                    int attrIdx = 1;
                    //We always add the four attributes to have always the same structure
                    while (attrIdx <= 4) {
                        String attr = metadata.getAttr(attrIdx);
                        if (attr != null) {
                            if (attr.length() > 0) {
                                Json.quoteAttr(mapItems, "attr" + attrIdx,
                                               Json.cleanString(attr));
                            } else {
                                Json.quoteAttr(mapItems, "attr" + attrIdx, "");
                            }
                        } else {
                            Json.quoteAttr(mapItems, "attr" + attrIdx, "");
                        }
                        attrIdx++;
                    }
                    metadataItems.add(Json.map(mapItems));
                }
            }
            Json.attr(items, "metadata", Json.list(metadataItems));
        }


        return Json.map(items);
    }

    /**
     * _more_
     *
     * @param args _more_
     */
    public static void main(String[] args) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setPrettyPrinting();
        gsonBuilder.setDateFormat(DateFormat.LONG);
        gsonBuilder.setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE);



        Gson  gson  = gsonBuilder.create();
        Entry entry = new Entry();
        System.err.println(gson.toJson(entry));

    }

    /**
     * Class description
     *
     *
     * @version        $version$, Mon, Sep 5, '11
     * @author         Enter your name here...
     */
    private static class EntryExclusionStrategy implements ExclusionStrategy {

        /**
         * _more_
         *
         * @param clazz _more_
         *
         * @return _more_
         */
        public boolean shouldSkipClass(Class<?> clazz) {
            if (clazz.equals(org.ramadda.repository.type.TypeHandler.class)) {
                return false;
            }
            if (clazz.equals(org.ramadda.repository.Repository.class)) {
                return false;
            }
            if (clazz.equals(org.ramadda.repository.RepositorySource.class)) {
                return false;
            }
            if (clazz.equals(org.ramadda.repository.RequestUrl.class)) {
                return false;
            }
            System.err.println("class:" + clazz.getName());

            return false;
        }

        /**
         * _more_
         *
         * @param f _more_
         *
         * @return _more_
         */
        public boolean shouldSkipField(FieldAttributes f) {
            if (f.hasModifier(java.lang.reflect.Modifier.STATIC)) {
                return false;
            }
            System.err.println("field:" + f.getName());

            //            return true;
            return false;
        }
    }

}
