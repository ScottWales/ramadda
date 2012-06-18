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

package org.ramadda.plugins.feed;



import org.ramadda.repository.*;
import org.ramadda.repository.metadata.*;
import org.ramadda.repository.type.*;
import org.ramadda.util.AtomUtil;

import org.ramadda.util.RssUtil;


import org.w3c.dom.*;

import ucar.unidata.util.DateUtil;
import org.ramadda.util.HtmlUtils;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.StringUtil;
import ucar.unidata.xml.XmlUtil;

import java.net.URL;

import java.text.SimpleDateFormat;


import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.HashSet;
import java.util.List;


/**
 */
public class FeedTypeHandler extends GenericTypeHandler {

    /**
     * _more_
     *
     * @param repository _more_
     * @param entryNode _more_
     *
     * @throws Exception _more_
     */
    public FeedTypeHandler(Repository repository, Element entryNode)
            throws Exception {
        super(repository, entryNode);
    }



    /**
     * _more_
     *
     * @param request _more_
     * @param mainEntry _more_
     * @param parentEntry _more_
     * @param synthId _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public List<String> getSynthIds(Request request, Entry mainEntry,
                                    Entry parentEntry, String synthId)
            throws Exception {
        List<String> ids = mainEntry.getChildIds();
        if (ids != null) {
            return ids;
        }
        ids = new ArrayList<String>();
        if (synthId != null) {
            return ids;
        }

        for (Entry item : getFeedEntries(request, mainEntry)) {
            ids.add(item.getId());
        }
        mainEntry.setChildIds(ids);
        return ids;
    }

    /**
     * _more_
     *
     * @param parentEntry _more_
     * @param subId _more_
     *
     * @return _more_
     */
    public String getSynthId(Entry parentEntry, String subId) {
        return Repository.ID_PREFIX_SYNTH + parentEntry.getId() + ":" + subId;
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
        if ( !getEntryManager().isSynthEntry(entry.getId())) {
            return getRepository().getTypeHandler(TypeHandler.TYPE_GROUP);
        }
        return getRepository().getTypeHandler(TypeHandler.TYPE_FILE);
    }

    /*
<title>Heading west</title>
    <link>http://scripting.com/stories/2011/01/25/headingWest.html</link>
    <guid>http://scripting.com/stories/2011/01/25/headingWest.html</guid>
    <comments>http://scripting.com/stories/2011/01/25/headingWest.html#disqus_thread</comments>
      <description>
    </description>
        <pubDate>Tue, 25 Jan 2011 14:26:27 GMT</pubDate>
</item>
    */



    /**
     * _more_
     *
     * @param request _more_
     * @param mainEntry _more_
     * @param items _more_
     * @param root _more_
     *
     * @throws Exception _more_
     */
    public void processRss(Request request, Entry mainEntry,
                           List<Entry> items, Element root)
            throws Exception {
        //        Thu, 14 Jun 2012 14:50:14 -05:00
        SimpleDateFormat []sdfs = new SimpleDateFormat[]{
            new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz"),
            new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z"),
        };
        Element channel = XmlUtil.getElement(root, RssUtil.TAG_CHANNEL);
        if (channel == null) {
            throw new IllegalArgumentException("No channel tag");
        }
        NodeList children = XmlUtil.getElements(channel, RssUtil.TAG_ITEM);
        HashSet seen = new HashSet();
        for (int childIdx = 0; childIdx < children.getLength(); childIdx++) {
            Element item = (Element) children.item(childIdx);
            String title = XmlUtil.getGrandChildText(item, RssUtil.TAG_TITLE,
                               "");

            String link = XmlUtil.getGrandChildText(item, RssUtil.TAG_LINK,
                              "");


            
            String guid = XmlUtil.getGrandChildText(item, RssUtil.TAG_GUID,
                                                    link);
            if(seen.contains(guid)) {
                continue;
            }

            seen.add(guid);
            String desc = XmlUtil.getGrandChildText(item,
                              RssUtil.TAG_DESCRIPTION, "");
            String pubDate = XmlUtil.getGrandChildText(item,
                                 RssUtil.TAG_PUBDATE, "").trim();

            String lat = XmlUtil.getGrandChildText(item, RssUtil.TAG_GEOLAT,
                             "").trim();
            if (lat.length() == 0) {
                lat = XmlUtil.getGrandChildText(item, "lat", "").trim();
            }
            String lon = XmlUtil.getGrandChildText(item, RssUtil.TAG_GEOLON,
                             "").trim();
            if (lon.length() == 0) {
                lon = XmlUtil.getGrandChildText(item, "long", "").trim();
            }

            Entry entry = new Entry(getSynthId(mainEntry, guid), this, false);
            Date  dttm  = new Date();
            for(SimpleDateFormat sdf: sdfs) {
                try {
                    dttm = sdf.parse(pubDate);
                    break;
                } catch (Exception exc) {
                }
            }

            if(dttm == null) {
                dttm = DateUtil.parse(pubDate);
            }

            if ((lat.length() > 0) && (lon.length() > 0)) {
                entry.setLocation(Double.parseDouble(lat),
                                  Double.parseDouble(lon), 0);
            }
            //Tue, 25 Jan 2011 05:00:00 GMT
            Resource resource = new Resource(link);
            entry.initEntry(title, desc, mainEntry, mainEntry.getUser(),
                            resource, "", dttm.getTime(), dttm.getTime(),
                            dttm.getTime(), dttm.getTime(), null);

            items.add(entry);
            getEntryManager().cacheEntry(entry);
        }
    }



    public void processAtom(Request request, Entry mainEntry,
                           List<Entry> items, Element root)
            throws Exception {
        //        Thu, 14 Jun 2012 14:50:14 -05:00
        SimpleDateFormat []sdfs = new SimpleDateFormat[]{
            new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz"),
            new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z"),
        };
        NodeList children = XmlUtil.getElements(root, AtomUtil.TAG_ENTRY);
        HashSet seen = new HashSet();
        for (int childIdx = 0; childIdx < children.getLength(); childIdx++) {
            Element item = (Element) children.item(childIdx);
            String title = XmlUtil.getGrandChildText(item, AtomUtil.TAG_TITLE,
                               "");
            String guid = XmlUtil.getGrandChildText(item, AtomUtil.TAG_ID,
                                                    ""+childIdx);
            if(seen.contains(guid)) {
                continue;
            }
            seen.add(guid);
            String desc = XmlUtil.getGrandChildText(item,
                              AtomUtil.TAG_CONTENT, "");
            String pubDate = XmlUtil.getGrandChildText(item,
                                 AtomUtil.TAG_PUBLISHED, "").trim();

            String lat = XmlUtil.getGrandChildText(item, RssUtil.TAG_GEOLAT,
                             "").trim();
            if (lat.length() == 0) {
                lat = XmlUtil.getGrandChildText(item, "lat", "").trim();
            }
            String lon = XmlUtil.getGrandChildText(item, RssUtil.TAG_GEOLON,
                             "").trim();
            if (lon.length() == 0) {
                lon = XmlUtil.getGrandChildText(item, "long", "").trim();
            }

            Entry entry = new Entry(getSynthId(mainEntry, guid), this, false);
            Date  dttm  = null;
            Date  changeDate  = null;
            for(SimpleDateFormat sdf: sdfs) {
                try {
                    //                    dttm = sdf.parse(pubDate);
                    break;
                } catch (Exception exc) {
                }
            }

            
            if(dttm == null) {
                dttm = DateUtil.parse(pubDate);
            }

            if ((lat.length() > 0) && (lon.length() > 0)) {
                entry.setLocation(Double.parseDouble(lat),
                                  Double.parseDouble(lon), 0);
            }
            String link = XmlUtil.getGrandChildText(item, "feedburner:origLink",
                              "");
            //TODO: look through the link tags 
            Resource resource = new Resource(link);
            entry.initEntry(title, desc, mainEntry, mainEntry.getUser(),
                            resource, "", dttm.getTime(), dttm.getTime(),
                            dttm.getTime(), dttm.getTime(), null);

            items.add(entry);
            getEntryManager().cacheEntry(entry);
        }
    }

    /**
     * _more_
     *
     * @param request _more_
     * @param mainEntry _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public List<Entry> getFeedEntries(Request request, Entry mainEntry)
            throws Exception {
        List<Entry> items = new ArrayList<Entry>();
        String      url   = mainEntry.getResource().getPath();
        if ((url == null) || (url.trim().length() == 0)) {
            return items;
        }

        Element root = XmlUtil.getRoot(url, getClass());
        if (root.getTagName().equals(RssUtil.TAG_RSS)) {
            processRss(request, mainEntry, items, root);
        } else if (root.getTagName().equals(AtomUtil.TAG_FEED)) {
            processAtom(request, mainEntry, items, root);
        } else {
            throw new IllegalArgumentException("Unknown feed type:" + root.getTagName()); 
            //            getRepository().getLogManager().logError("Unknown feed type:" + root.getTagName()); 
        }

        return items;
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param mainEntry _more_
     * @param id _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Entry makeSynthEntry(Request request, Entry mainEntry, String id)
            throws Exception {
        id = getSynthId(mainEntry, id);
        for (Entry item : getFeedEntries(request, mainEntry)) {
            if (item.getId().equals(id)) {
                return item;
            }
        }
        return null;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean isSynthType() {
        return true;
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
    public String getIconUrl(Request request, Entry entry) throws Exception {
        return iconUrl("/feed/blog_icon.png");
    }

    /**
     * _more_
     *
     * @param id _more_
     *
     * @return _more_
     */
    public Entry createEntry(String id) {
        return new Entry(id, this, true);
    }


}
