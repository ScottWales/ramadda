/*
 * Copyright 2010 ramadda.org
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

package org.ramadda.util;

import ucar.unidata.xml.XmlUtil;

import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.util.Date;


/**
 * A collection of utilities for atom feeds xml.
 *
 * @author IDV development team
 */

public class AtomUtil {

    /** _more_          */
    public static final SimpleDateFormat atomSdf =
        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss Z");

    public static final String XMLNS="http://www.w3.org/2005/Atom";
    public static final String LINK_SELF = "self";


    public static final String TAG_FEED = "feed";
    public static final String TAG_TITLE = "title";
    public static final String TAG_SUBTITLE = "subtitle";
    public static final String TAG_LINK = "link";
    public static final String TAG_UPDATED = "updated";
    public static final String TAG_AUTHOR = "author";
    public static final String TAG_NAME = "name";
    public static final String TAG_URI = "uri";
    public static final String TAG_ID = "id";
    public static final String TAG_ICON = "icon";
    public static final String TAG_RIGHTS = "rights";
    public static final String TAG_ENTRY = "entry";
    public static final String TAG_SUMMARY = "summary";
    public static final String TAG_CONTENT = "content";


    public static final String ATTR_XMLNS = "xmlns";
    public static final String ATTR_HREF = "href";
    public static final String ATTR_REL = "rel";
    public static final String ATTR_TYPE = "type";


    public static String format(Date date) {
        return atomSdf.format(date);
    }


    public static String makeTitle(String title) {
        return XmlUtil.tag(TAG_TITLE,"",title);
    }


    public static String makeLink(String href) {
        return makeLink("self", href);
    }

    public static String makeLink(String rel, String href) {
        return XmlUtil.tag(TAG_LINK, XmlUtil.attrs(ATTR_REL, rel, ATTR_HREF, href));
    }


    public static String makeContent(String type, String content) {
        return XmlUtil.tag(TAG_LINK, XmlUtil.attrs(ATTR_TYPE, type),content);
    }

    public static String openFeed() {
        return XmlUtil.openTag(TAG_FEED, XmlUtil.attrs(ATTR_XMLNS, XMLNS));
    }

    public static String closeFeed() {
        return XmlUtil.closeTag(TAG_FEED);
    }

    public static String makeAuthor(String name, String uri) {
        // <author>   <name>Xah Lee</name>   <uri>http://xahlee.org/</uri> </author>
        return XmlUtil.tag(TAG_AUTHOR, "", XmlUtil.tag(TAG_NAME,"",name) +
                           XmlUtil.tag(TAG_URI,"",uri));
    }

    public static String makeEntry(String title,
                                   String id,
                                   Date updated,
                                   String summary,
                                   String content,
                                   String[][]links) {
        StringBuffer sb = new StringBuffer();
        /* <entry>
   <title>Batman thoughts</title>
   <id>tag:xahlee.org,2006-09-09:015218</id>
   <updated>2006-09-08T18:52:18-07:00</updated>
   <summary>Some notes after watching movie Batman.</summary>
   <content type="xhtml">
      <div xmlns="http://www.w3.org/1999/xhtml">
      <p>I watched Batman today ...</p>
      <!-- more xhtml here -->
      </div>
   </content>
  <link rel="alternate" href="pd.html"/>
  </entry>*/
        sb.append(XmlUtil.openTag(TAG_ENTRY));
        sb.append(XmlUtil.tag(TAG_TITLE,"",title));
        sb.append(XmlUtil.tag(TAG_ID,"",id));
        if(updated!=null) {
            sb.append(XmlUtil.tag(TAG_UPDATED,"",format(updated)));
        }
        if(summary!=null) {
            sb.append(XmlUtil.tag(TAG_SUMMARY,"", summary));
        }
        if(content!=null) {
            sb.append(content);
        }
        
        for(String[]tuple: links) {
            if(tuple.length==1)
                sb.append(makeLink(tuple[0]));
            else
                sb.append(makeLink(tuple[0],tuple[1]));
        }
        return sb.toString();
    }


}
