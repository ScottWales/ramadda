/**
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

package org.ramadda.plugins.map;


import org.ramadda.repository.*;
import org.ramadda.repository.metadata.*;
import org.ramadda.repository.output.*;
import org.ramadda.repository.map.*;
import org.ramadda.repository.type.*;


import org.w3c.dom.*;

import ucar.unidata.util.DateUtil;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.HtmlUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.data.gis.KmlUtil;

import ucar.unidata.util.StringUtil;
import ucar.unidata.util.TwoFacedObject;
import ucar.unidata.xml.XmlUtil;


import java.text.SimpleDateFormat;


import java.io.File;

import java.util.ArrayList;
import java.util.List;
import java.util.Date;



/**
 */
public class GpxTypeHandler extends GenericTypeHandler {



    /**
     * _more_
     *
     * @param repository _more_
     * @param node _more_
     * @throws Exception _more_
     */
    public GpxTypeHandler(Repository repository, Element node)
        throws Exception {
        super(repository, node);
    }

    private Element readXml(Entry entry) throws Exception {
        return  XmlUtil.getRoot(getStorageManager().readSystemResource(entry.getFile()));
    }

    public void initializeEntryFromForm(Request request, Entry entry,
                                        Entry parent, boolean newEntry)
        throws Exception {
        Element root = readXml(entry);
        Element metadata = XmlUtil.findChild(root, GpxUtil.TAG_METADATA);
        Element bounds = null;

        if(metadata!=null) {
            bounds = XmlUtil.findChild(metadata, GpxUtil.TAG_BOUNDS);
        }

        if(bounds == null) {
            bounds = XmlUtil.findChild(root, GpxUtil.TAG_BOUNDS);
        }
        if(bounds!=null) {
            entry.setNorth(XmlUtil.getAttribute(bounds, GpxUtil.ATTR_MAXLAT, Entry.NONGEO));
            entry.setSouth(XmlUtil.getAttribute(bounds, GpxUtil.ATTR_MINLAT, Entry.NONGEO));
            entry.setWest(XmlUtil.getAttribute(bounds, GpxUtil.ATTR_MINLON, Entry.NONGEO));
            entry.setEast(XmlUtil.getAttribute(bounds, GpxUtil.ATTR_MAXLON, Entry.NONGEO));
        }
        
        entry.setName(XmlUtil.getGrandChildText(root,GpxUtil.TAG_NAME, entry.getName()));
        if(entry.getDescription().length()==0) {
            entry.setDescription(XmlUtil.getGrandChildText(root,GpxUtil.TAG_DESC,""));
        }

        String keywords = XmlUtil.getGrandChildText(root,GpxUtil.TAG_KEYWORDS,null);
        if(keywords!=null) {
            for(String word: StringUtil.split(keywords,",",true,true)) {
                entry.addMetadata(
                                  new Metadata(
                                               getRepository().getGUID(), entry.getId(),
                                               ContentMetadataHandler.TYPE_KEYWORD, false, word, "",
                                               "", "", ""));
            }
        }

        String url = XmlUtil.getGrandChildText(root,GpxUtil.TAG_URL,null);
        String urlName = XmlUtil.getGrandChildText(root,GpxUtil.TAG_URLNAME,"");
        if(url!=null) {
            entry.addMetadata(
                              new Metadata(
                                           getRepository().getGUID(), entry.getId(),
                                           ContentMetadataHandler.TYPE_URL, false, url, urlName,
                                           "", "", ""));

        }

        String author = XmlUtil.getGrandChildText(root,GpxUtil.TAG_AUTHOR,null);
        if(author!=null) {
            entry.addMetadata(
                              new Metadata(
                                           getRepository().getGUID(), entry.getId(),
                                           ContentMetadataHandler.TYPE_AUTHOR, false,author,"",
                                           "", "", ""));

        }


        String email = XmlUtil.getGrandChildText(root,GpxUtil.TAG_EMAIL,null);
        if(email!=null) {
            entry.addMetadata(
                              new Metadata(
                                           getRepository().getGUID(), entry.getId(),
                                           ContentMetadataHandler.TYPE_EMAIL, false,email,"",
                                           "", "", ""));

        }


        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

        long minTime = -1;
        long maxTime = -1;
        for(Element child: ((List<Element>)XmlUtil.findChildren(root, GpxUtil.TAG_WPT))) {
            String time = XmlUtil.getGrandChildText(child, GpxUtil.TAG_TIME, null);
            if(time!=null) {
                Date dttm  = sdf.parse(time);
                minTime = minTime==-1?dttm.getTime():Math.min(minTime, dttm.getTime());
                maxTime = maxTime==-1?dttm.getTime():Math.max(maxTime, dttm.getTime());
            }
        }
        if(minTime>0) {
            entry.setStartDate(minTime);
            entry.setEndDate(maxTime);
        }
        
        


    }


    public static void main(String[]args) throws Exception {
        //        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        String dttm = "2010-01-27T00:39:16Z";
        sdf.parse("2010-01-27T00:39:00Z");
    }


    public void getEntryLinks(Request request, Entry entry, List<Link> links)
            throws Exception {
        super.getEntryLinks(request, entry, links);
        links.add(
                  new Link(
                           request.entryUrl(getRepository().URL_ENTRY_ACCESS, entry,"type","kml"),
                           getRepository().iconUrl(ICON_KML), "Convert GPX to KML",
                           OutputType.TYPE_FILE));
    }


    public Result processEntryAccess(Request request, Entry entry) throws Exception {
        Element gpxRoot = readXml(entry);

        Element root  = KmlUtil.kml(entry.getName());
        Element folder = KmlUtil.folder(root, entry.getName(), true);

        for(Element child: ((List<Element>)XmlUtil.findChildren(gpxRoot, GpxUtil.TAG_WPT))) {
            String name = XmlUtil.getGrandChildText(child, GpxUtil.TAG_NAME,"");
            String desc = XmlUtil.getGrandChildText(child, GpxUtil.TAG_DESC,"");
            String sym = XmlUtil.getGrandChildText(child, GpxUtil.TAG_SYM,"");
            double lat = XmlUtil.getAttribute(child, GpxUtil.ATTR_LAT,0.0);
            double lon = XmlUtil.getAttribute(child, GpxUtil.ATTR_LON,0.0);
            KmlUtil.placemark(folder, name, desc,lat,lon,0,null);
        }

        for(Element track: ((List<Element>)XmlUtil.findChildren(gpxRoot, GpxUtil.TAG_TRK))) {
            for(Element trackSeg: ((List<Element>)XmlUtil.findChildren(track, GpxUtil.TAG_TRKSEG))) {
                List<double[]> points = new ArrayList<double[]>();
                for(Element trackPoint: ((List<Element>)XmlUtil.findChildren(trackSeg, GpxUtil.TAG_TRKPT))) {
                    double lat = XmlUtil.getAttribute(trackPoint, GpxUtil.ATTR_LAT,0.0);
                    double lon = XmlUtil.getAttribute(trackPoint, GpxUtil.ATTR_LON,0.0);
                    points.add(new double[]{lat,lon});
                }
                float[][] coords = new float[2][points.size()];
                int cnt=0;
                for(double[]point: points) {
                    coords[0][cnt] = (float)point[0];
                    coords[1][cnt] = (float)point[1];
                    cnt++;
                }
                KmlUtil.placemark(folder,"track","",coords, java.awt.Color.RED,2);
            }
        }



        StringBuffer sb = new StringBuffer(XmlUtil.XML_HEADER);
        sb.append(XmlUtil.toString(root));

        Result result = new  Result("GPX Entry", sb.toString().getBytes(),
                                    getRepository().getMimeTypeFromSuffix(".kml"), false);
        result.setReturnFilename(IOUtil.stripExtension(entry.getName()) +".kml");
        return result;
    }

    public void addToMap(Request request, Entry entry, MapInfo map)     {
        try {
            Element root =readXml(entry);
            int cnt = 0;
            for(Element child: ((List<Element>)XmlUtil.findChildren(root, GpxUtil.TAG_WPT))) {
                if(cnt++>500) break;
                String name = XmlUtil.getGrandChildText(child, GpxUtil.TAG_NAME,"");
                String desc = XmlUtil.getGrandChildText(child, GpxUtil.TAG_DESC,"");
                String sym = XmlUtil.getGrandChildText(child, GpxUtil.TAG_SYM,"");
                double lat = XmlUtil.getAttribute(child, GpxUtil.ATTR_LAT,0.0);
                double lon = XmlUtil.getAttribute(child, GpxUtil.ATTR_LON,0.0);
                String info = name+"<br>" + desc;
                info = info.replaceAll("\n","<br>");
                info = info.replaceAll("'","\\'");
                map.addMarker("id", lat, lon, null, info);
            }

            for(Element track: ((List<Element>)XmlUtil.findChildren(root, GpxUtil.TAG_TRK))) {
                for(Element trackSeg: ((List<Element>)XmlUtil.findChildren(track, GpxUtil.TAG_TRKSEG))) {
                    List<double[]> points = new ArrayList<double[]>();
                    for(Element trackPoint: ((List<Element>)XmlUtil.findChildren(trackSeg, GpxUtil.TAG_TRKPT))) {
                        double lat = XmlUtil.getAttribute(trackPoint, GpxUtil.ATTR_LAT,0.0);
                        double lon = XmlUtil.getAttribute(trackPoint, GpxUtil.ATTR_LON,0.0);
                        points.add(new double[]{lat,lon});
                    }
                    if(points.size()>1) {
                        map.addLines("", points);
                    }
                }
            }



        } catch(Exception exc) {
            throw new RuntimeException(exc);
        }
        
         
    }



}
