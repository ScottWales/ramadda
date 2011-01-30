/*
 * Copyright 1997-2010 Unidata Program Center/University Corporation for
 * Atmospheric Research, P.O. Box 3000, Boulder, CO 80307,
 * support@unidata.ucar.edu.
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
 */

package ucar.unidata.repository.output;


import org.w3c.dom.*;

import java.text.SimpleDateFormat;
import ucar.unidata.geoloc.Bearing;
import ucar.unidata.geoloc.LatLonPointImpl;

import ucar.unidata.data.gis.KmlUtil;

import ucar.unidata.repository.*;
import ucar.unidata.repository.metadata.Metadata;
import ucar.unidata.repository.auth.*;

import java.text.DateFormat;

import ucar.unidata.util.HtmlUtil;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;
import ucar.unidata.xml.XmlUtil;


import java.io.*;

import java.io.File;

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;


import com.google.gson.*;

/**
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class JsonOutputHandler extends OutputHandler {


    /** _more_ */
    public static final OutputType OUTPUT_JSON =
        new OutputType("JSON", "json",
                       OutputType.TYPE_NONHTML | OutputType.TYPE_FORSEARCH,
                       "", ICON_JSON);



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
        if(state.getEntry()!=null && state.getEntry().getName()!=null) {
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
        //        allEntries.add(group);
        allEntries.addAll(subGroups);
        allEntries.addAll(entries);
        StringBuffer sb = new StringBuffer();
        sb.append("[");
        int cnt=0;
        for(Entry entry: allEntries) {
            if(cnt>0)
                sb.append(",");
            cnt++;
            toJson(entry, sb);
            sb.append("\n");
        }
        sb.append("]");


        return new Result("", sb, "application/json");
    }

    private String qt(String s) {
        return "\"" + s +"\"";
    }

    private void attr(StringBuffer sb, String name, String value) {
        attr(sb,name, value, true);
    }

    private void qtattr(StringBuffer sb, String name, String value) {
        qtattr(sb, name, value, true);
    }

    private void qtattr(StringBuffer sb, String name, String value, boolean addComma) {
        if(value==null) return;
        value = value.replaceAll("\"","\\\"");
        attr(sb,name, qt(value), addComma);
    }

   private void attr(StringBuffer sb, String name, String value, boolean addComma) {
       if(addComma) sb.append(", ");
        sb.append(qt(name));
        sb.append(":");
        sb.append(value);
    }


    private static SimpleDateFormat sdf;
    private String fmt(long dttm) {
        if(sdf == null) {
            sdf =  RepositoryUtil.makeDateFormat("yyyy-MM-dd HH:mm:ss");
        }
        synchronized(sdf) {
            return sdf.format(new Date(dttm));
        }
    }

    private void toJson(Entry entry, StringBuffer sb) throws Exception {
        sb.append("{");
        attr(sb, "id", qt(entry.getId()), false);
        qtattr(sb, "name", entry.getName());
        qtattr(sb, "description", entry.getDescription());
        qtattr(sb, "type", entry.getType());
        if(entry.isGroup()) {
            attr(sb, "isGroup", "true");
        }
        qtattr(sb, "parent", entry.getParentEntryId());
        qtattr(sb, "user", entry.getUser().getId());
        qtattr(sb, "createDate", fmt(entry.getCreateDate()));
        qtattr(sb, "startDate", fmt(entry.getStartDate()));
        qtattr(sb, "endDate", fmt(entry.getEndDate()));

        if(entry.hasNorth())
            attr(sb, "north", ""+entry.getNorth());
        if(entry.hasSouth())
            attr(sb, "south", ""+entry.getSouth());
        if(entry.hasEast())
            attr(sb, "east", ""+entry.getEast());
        if(entry.hasWest())
            attr(sb, "west", ""+entry.getWest());
        if(entry.hasAltitudeTop())
            attr(sb, "altitudeTop", ""+entry.getAltitudeTop());
        if(entry.hasAltitudeBottom())
            attr(sb, "altitudeBottom", ""+entry.getAltitudeBottom());


        Resource resource = entry.getResource();
        //TODO: add services
        if(resource!=null) {
            if(resource.isUrl()) {
                qtattr(sb, "url", resource.getPath());
            } else if(resource.isFile()) {
                qtattr(sb, "filename", getStorageManager().getFileTail(entry));
                if(resource.getFileSize()>0) {
                    attr(sb, "filesize", ""+resource.getFileSize());
                }
                if(resource.getMd5()!=null) {
                    qtattr(sb, "md5", resource.getMd5());
                }
            }
        }
        List<Metadata> metadataItems= getMetadataManager().getMetadata(entry);
        if(metadataItems!=null && metadataItems.size()>0) {
            StringBuffer msb = new StringBuffer("[");
            for(int j=0;j<metadataItems.size();j++) {
                if(j>0) msb.append(",\n ");
                Metadata metadata = metadataItems.get(j);
                msb.append("{");
                qtattr(msb, "id", metadata.getId(), false);
                qtattr(msb, "type", metadata.getType());
                int attrIdx=1;
                while(true) {
                    String attr = metadata.getAttr(attrIdx);
                    if(attr==null) break;
                    if(attr.length()>0) {
                        qtattr(msb, "attr"+attrIdx, attr);
                    }
                    attrIdx++;
                }
                msb.append("}\n");
            }
            msb.append("]");
            attr(sb,"metadata",msb.toString());
        }
        sb.append("}\n");
        
    }

    public static void main(String[]args) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setPrettyPrinting();
        gsonBuilder.setDateFormat(DateFormat.LONG);
        gsonBuilder.setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE);



        Gson gson = gsonBuilder.create();
        Entry entry = new Entry();
        System.err.println(gson.toJson(entry));
        
    }

    private static class EntryExclusionStrategy implements ExclusionStrategy {
        public boolean shouldSkipClass(Class<?> clazz) {
            if(clazz.equals(ucar.unidata.repository.type.TypeHandler.class)) return false;
            if(clazz.equals(ucar.unidata.repository.Repository.class)) return false;
            if(clazz.equals(ucar.unidata.repository.RepositorySource.class)) return false;
            if(clazz.equals(ucar.unidata.repository.RequestUrl.class)) return false;
            System.err.println("class:" + clazz.getName());
            return false;
        }

        public boolean shouldSkipField(FieldAttributes f) {
            if(f.hasModifier(java.lang.reflect.Modifier.STATIC)) return false;
            System.err.println("field:" + f.getName());
            //            return true;
            return false;
        }
    }


}
