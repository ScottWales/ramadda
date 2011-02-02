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

package org.ramadda.util;


import ucar.unidata.xml.XmlUtil;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.Date;

import java.util.List;


/**
 * A collection of utilities for atom feeds xml.
 *
 * @author IDV development team
 */

public class AtomUtil {

    /** _more_ */
    public static final SimpleDateFormat atomSdf =
        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss Z");

    /** _more_          */
    public static final String XMLNS = "http://www.w3.org/2005/Atom";

    /** _more_          */
    public static final String REL_SELF = "self";

    /** _more_          */
    public static final String REL_IMAGE = "image";


    /** _more_          */
    public static final String TAG_FEED = "feed";

    /** _more_          */
    public static final String TAG_TITLE = "title";

    /** _more_          */
    public static final String TAG_SUBTITLE = "subtitle";

    /** _more_          */
    public static final String TAG_LINK = "link";

    /** _more_          */
    public static final String TAG_UPDATED = "updated";

    /** _more_          */
    public static final String TAG_AUTHOR = "author";

    /** _more_          */
    public static final String TAG_NAME = "name";

    /** _more_          */
    public static final String TAG_URI = "uri";

    /** _more_          */
    public static final String TAG_ID = "id";

    /** _more_          */
    public static final String TAG_ICON = "icon";

    /** _more_          */
    public static final String TAG_RIGHTS = "rights";

    /** _more_          */
    public static final String TAG_ENTRY = "entry";

    /** _more_          */
    public static final String TAG_SUMMARY = "summary";

    /** _more_          */
    public static final String TAG_CONTENT = "content";


    /** _more_          */
    public static final String ATTR_XMLNS = "xmlns";

    /** _more_          */
    public static final String ATTR_HREF = "href";

    /** _more_          */
    public static final String ATTR_REL = "rel";

    /** _more_          */
    public static final String ATTR_TYPE = "type";

    /** _more_          */
    public static final String ATTR_TITLE = "title";


    /**
     * _more_
     *
     * @param date _more_
     *
     * @return _more_
     */
    public static String format(Date date) {
        return atomSdf.format(date);
    }


    /**
     * _more_
     *
     * @param title _more_
     *
     * @return _more_
     */
    public static String makeTitle(String title) {
        return XmlUtil.tag(TAG_TITLE, "", XmlUtil.getCdata(title));
    }


    /**
     * _more_
     *
     * @param href _more_
     *
     * @return _more_
     */
    public static String makeLink(String href) {
        return makeLink(REL_SELF, href);
    }

    /**
     * _more_
     *
     * @param link _more_
     *
     * @return _more_
     */
    public static String makeLink(Link link) {
        if (link.title != null) {
            return XmlUtil.tag(TAG_LINK,
                               XmlUtil.attrs(ATTR_REL, link.rel, ATTR_HREF,
                                             link.url, ATTR_TITLE,
                                             link.title));
        }
        return XmlUtil.tag(TAG_LINK,
                           XmlUtil.attrs(ATTR_REL, link.rel, ATTR_HREF,
                                         link.url));
    }


    /**
     * _more_
     *
     * @param rel _more_
     * @param href _more_
     *
     * @return _more_
     */
    public static String makeLink(String rel, String href) {
        return XmlUtil.tag(TAG_LINK,
                           XmlUtil.attrs(ATTR_REL, rel, ATTR_HREF, href));
    }


    /**
     * _more_
     *
     * @param type _more_
     * @param content _more_
     *
     * @return _more_
     */
    public static String makeContent(String type, String content) {
        return XmlUtil.tag(TAG_LINK, XmlUtil.attrs(ATTR_TYPE, type),
                           XmlUtil.getCdata(content));
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public static String openFeed() {
        return XmlUtil.openTag(TAG_FEED, XmlUtil.attrs(ATTR_XMLNS, XMLNS));
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public static String closeFeed() {
        return XmlUtil.closeTag(TAG_FEED);
    }

    /**
     * _more_
     *
     * @param name _more_
     * @param uri _more_
     *
     * @return _more_
     */
    public static String makeAuthor(String name, String uri) {
        // <author>   <name>Xah Lee</name>   <uri>http://xahlee.org/</uri> </author>
        return XmlUtil.tag(TAG_AUTHOR, "",
                           XmlUtil.tag(TAG_NAME, "", name)
                           + XmlUtil.tag(TAG_URI, "", uri));
    }

    /**
     * _more_
     *
     * @param title _more_
     * @param id _more_
     * @param updated _more_
     * @param summary _more_
     * @param content _more_
     * @param links _more_
     *
     * @return _more_
     */
    public static String makeEntry(String title, String id, Date updated,
                                   String summary, String content,
                                   List<Link> links) {
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
        sb.append(XmlUtil.tag(TAG_TITLE, "", title));
        sb.append(XmlUtil.tag(TAG_ID, "", id));
        if (updated != null) {
            sb.append(XmlUtil.tag(TAG_UPDATED, "", format(updated)));
        }
        if ((summary != null) && (summary.length() > 0)) {
            sb.append(XmlUtil.tag(TAG_SUMMARY, "",
                                  XmlUtil.getCdata(summary)));
        }
        if (content != null) {
            sb.append(XmlUtil.getCdata(content));
        }

        for (Link link : links) {
            sb.append(makeLink(link.rel, link.url));
            sb.append("\n");
        }
        sb.append(XmlUtil.closeTag(TAG_ENTRY));
        sb.append("\n");
        return sb.toString();
    }



    /**
     * Class description
     *
     *
     * @version        $version$, Wed, Feb 2, '11
     * @author         Enter your name here...    
     */
    public static class Link {

        /** _more_          */
        private String rel;

        /** _more_          */
        private String url;

        /** _more_          */
        private String title;

        /**
         * _more_
         *
         * @param url _more_
         */
        public Link(String url) {
            this(REL_SELF, url);
        }

        /**
         * _more_
         *
         * @param rel _more_
         * @param url _more_
         */
        public Link(String rel, String url) {
            this(rel, url, null);
        }

        /**
         * _more_
         *
         * @param rel _more_
         * @param url _more_
         * @param title _more_
         */
        public Link(String rel, String url, String title) {
            this.rel   = rel;
            this.url   = url;
            this.title = title;
        }
    }
}
