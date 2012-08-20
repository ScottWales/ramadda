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
        sb.append("[");
        int cnt = 0;
        for (Entry entry : allEntries) {
            if (cnt > 0) {
                sb.append(",");
            }
            cnt++;
            toJson(request, entry, sb);

            sb.append("\n");
        }
        sb.append("]");

        return new Result("", sb, "application/json");
    }

    /**
     * _more_
     *
     * @param s _more_
     *
     * @return _more_
     */
    private String qt(String s) {
        return "\"" + s + "\"";
    }

    /**
     * _more_
     *
     * @param sb _more_
     * @param name _more_
     * @param value _more_
     */
    private void attr(StringBuffer sb, String name, String value) {
        attr(sb, name, value, true);
    }

    /**
     * _more_
     *
     * @param sb _more_
     * @param name _more_
     * @param value _more_
     */
    private void qtattr(StringBuffer sb, String name, String value) {
        qtattr(sb, name, value, true);
    }

    /**
     * _more_
     *
     * @param sb _more_
     * @param name _more_
     * @param value _more_
     * @param addComma _more_
     */
    private void qtattr(StringBuffer sb, String name, String value,
                        boolean addComma) {
        if (value == null) {
            attr(sb, name, qt("no value"), addComma);
        } else {
            value = value.replaceAll("\"", "\\\"");
            attr(sb, name, qt(value), addComma);
        }
    }

    /**
     * _more_
     *
     * @param sb _more_
     * @param name _more_
     * @param value _more_
     * @param addComma _more_
     */
    private void attr(StringBuffer sb, String name, String value,
                      boolean addComma) {
        if (addComma) {
            sb.append(", ");
        }
        sb.append(qt(name));
        sb.append(":");
        sb.append(value);
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
    private String fmt(long dttm) {
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
    private void toJson(Request request, Entry entry, StringBuffer sb)
            throws Exception {

        sb.append("{");

        attr(sb, "id", qt(entry.getId()), false);
        qtattr(sb, "name", replaceForJSON(entry.getName()));
        qtattr(sb, "description", replaceForJSON(entry.getDescription()));
        qtattr(sb, "type", entry.getType());

        if (entry.isGroup()) {
            attr(sb, "isGroup", "true");
        } else {
            attr(sb, "isGroup", "false");
        }

        qtattr(sb, "icon",
               getRepository().getEntryManager().getIconUrl(request, entry));

        qtattr(sb, "parent", entry.getParentEntryId());
        qtattr(sb, "user", entry.getUser().getId());
        qtattr(sb, "resource", entry.getResource().getPath());
        qtattr(sb, "createDate", fmt(entry.getCreateDate()));
        qtattr(sb, "startDate", fmt(entry.getStartDate()));
        qtattr(sb, "endDate", fmt(entry.getEndDate()));


        if (entry.hasNorth()) {
            attr(sb, "north", "" + entry.getNorth());
        } else {
            attr(sb, "north", "-9999");
        }

        if (entry.hasSouth()) {
            attr(sb, "south", "" + entry.getSouth());
        } else {
            attr(sb, "south", "-9999");
        }

        if (entry.hasEast()) {
            attr(sb, "east", "" + entry.getEast());
        } else {
            attr(sb, "east", "-9999");
        }

        if (entry.hasWest()) {
            attr(sb, "west", "" + entry.getWest());
        } else {
            attr(sb, "west", "-9999");
        }

        if (entry.hasAltitudeTop()) {
            attr(sb, "altitudeTop", "" + entry.getAltitudeTop());
        } else {
            attr(sb, "altitudeTop", "-9999");
        }

        if (entry.hasAltitudeBottom()) {
            attr(sb, "altitudeBottom", "" + entry.getAltitudeBottom());
        } else {
            attr(sb, "altitudeBottom", "-9999");
        }



        Resource resource = entry.getResource();

        //TODO: add services
        if (resource != null) {
            if (resource.isUrl()) {

                String temp = replaceForJSON(resource.getPath());
                if (temp == null) {
                    qtattr(sb, "filename", "");
                } else {
                    qtattr(sb, "filename", java.net.URLEncoder.encode(temp));
                }

                attr(sb, "filesize", "" + resource.getFileSize());
                qtattr(sb, "md5", "");
                //TODO MATIAS            } else if(resource.isFileNoCheck()) {
            } else if (resource.isFile()) {
                qtattr(sb, "filename",
                       getStorageManager().getFileTail(entry));
                attr(sb, "filesize", "" + resource.getFileSize());
                if (resource.getMd5() != null) {
                    qtattr(sb, "md5", resource.getMd5());
                } else {
                    qtattr(sb, "md5", "");
                }
            }
        } else {
            qtattr(sb, "filename", "no resource");
            attr(sb, "filesize", "");
            qtattr(sb, "md5", "");
        }


        // Add special columns to the entries depending on the type
        if (request.get(ARG_EXTRACOLUMNS, true)) {
            Object[] extraParameters = entry.getValues();
            if (extraParameters != null) {
                StringBuffer msb = new StringBuffer("[");
                for (int i = 0; i < extraParameters.length; i++) {
                    if (i > 0) {
                        msb.append(",\n ");
                    }
                    msb.append("{");
                    String value = entry.getValue(i, "");
                    // I don't know how to extract column name maybe there is a function but
                    // I don't know so I add the column position. In any case you need to know
                    // the output type in the client side to deal with the parameters.
                    qtattr(msb, Integer.toString(i), replaceForJSON(value),
                           false);

                    msb.append("}");
                }
                msb.append("]");
                attr(sb, "extraColumns", msb.toString());
            } else {
                attr(sb, "extraColumns", "[]");
            }
        }



        if (request.get(ARG_LINKS, false)) {
            List<Link> links = new ArrayList<Link>();
            links = repository.getEntryManager().getEntryLinks(request,
                    entry);
            if (links != null) {
                StringBuffer msb   = new StringBuffer("[");
                int          index = 0;
                for (Link link : links) {
                    if (index > 0) {
                        msb.append(",\n ");
                    }
                    if (link.getLabel() != "") {
                        msb.append("{");
                        qtattr(msb, "label", replaceForJSON(link.getLabel()),
                               false);
                        OutputType outputType = link.getOutputType();
                        if (outputType == null) {
                            qtattr(msb, "type", "unknow");
                        } else {
                            qtattr(msb, "type",
                                   replaceForJSON(outputType.toString()));
                        }

                        if (link.getUrl() != null) {
                            qtattr(msb, "url",
                                   java.net.URLEncoder.encode(
                                       replaceForJSON(link.getUrl())));
                        } else {
                            qtattr(msb, "url", "");
                        }


                        qtattr(msb, "icon", link.getIcon());
                        msb.append("}");
                        index++;
                    }
                }
                msb.append("]");
                attr(sb, "links", msb.toString());
            } else {
                attr(sb, "links", "[]");
            }
        }

        if (request.get(ARG_METADATA, true)) {
            List<Metadata> metadataItems =
                getMetadataManager().getMetadata(entry);
            if ((metadataItems != null) && (metadataItems.size() > 0)) {
                StringBuffer msb = new StringBuffer("[");
                for (int j = 0; j < metadataItems.size(); j++) {
                    if (j > 0) {
                        msb.append(",\n ");
                    }
                    Metadata metadata = metadataItems.get(j);
                    msb.append("{");
                    qtattr(msb, "id", metadata.getId(), false);
                    qtattr(msb, "type", metadata.getType());
                    int attrIdx = 1;
                    //We always add the four attributes to have always the same structure
                    while (attrIdx <= 4) {
                        String attr = metadata.getAttr(attrIdx);
                        if (attr != null) {
                            if (attr.length() > 0) {
                                qtattr(msb, "attr" + attrIdx,
                                       replaceForJSON(attr));
                            } else {
                                qtattr(msb, "attr" + attrIdx, "");
                            }
                        } else {
                            qtattr(msb, "attr" + attrIdx, "");
                        }
                        attrIdx++;
                    }
                    msb.append("}\n");
                }
                msb.append("]");
                attr(sb, "metadata", msb.toString());
            } else {
                attr(sb, "metadata", "[]");
            }
        }
        sb.append("}\n");



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

    /**
     * _more_
     *
     * @param aText _more_
     *
     * @return _more_
     */
    public static String replaceForJSON(String aText) {
        if ((aText == null) || (aText == "")) {
            return "";
        }
        final StringBuilder     result    = new StringBuilder();

        StringCharacterIterator iterator  = new StringCharacterIterator(aText);
        char                    character = iterator.current();
        while (character != StringCharacterIterator.DONE) {
            if (character == '\"') {
                result.append("\\\"");
            } else if (character == '\\') {
                result.append("\\\\");
            } else if (character == '/') {
                result.append("\\/");
            } else if (character == '\b') {
                result.append("\\b");
            } else if (character == '\f') {
                result.append("\\f");
            } else if (character == '\n') {
                result.append("\\n");
            } else if (character == '\r') {
                result.append("\\r");
            } else if (character == '\t') {
                result.append("\\t");
            } else {
                //the char is not a special one
                //add it to the result as is
                result.append(character);
            }
            character = iterator.next();
        }

        return result.toString();
    }

}
