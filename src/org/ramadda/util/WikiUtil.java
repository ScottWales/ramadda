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

package org.ramadda.util;


import org.ramadda.util.HtmlUtils;

import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;


import ucar.unidata.util.StringUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;


import java.util.regex.*;

import java.util.regex.*;



/**
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class WikiUtil {

    /** _more_          */
    public static final String ATTR_OPEN = "open";

    /** _more_          */
    public static final String ATTR_DECORATE = "decorate";

    /** _more_          */
    public static final String ATTR_TITLE = "title";

    /** _more_          */
    public static final String ATTR_SHOW = "show";





    /** _more_          */
    public static final String PROP_NOHEADING = "noheading";

    /** _more_          */
    public static final String PROP_HEADING = "heading";

    /** _more_          */
    public static final String TAG_PREFIX = "{{";

    /** _more_          */
    public static final String TAG_SUFFIX = "}}";


    /** _more_ */
    private Hashtable properties;

    /** _more_ */
    private List categoryLinks = new ArrayList();

    /** _more_ */
    private List floatBoxes = new ArrayList();

    /** _more_ */
    private boolean makeHeadings = false;

    /** _more_ */
    private boolean replaceNewlineWithP = true;

    /** _more_          */
    private boolean mobile = false;

    /** _more_          */
    private String user;

    /**
     * _more_
     */
    public WikiUtil() {}

    /**
     * _more_
     *
     * @param properties _more_
     */
    public WikiUtil(Hashtable properties) {
        this.properties = properties;
    }

    /**
     * _more_
     *
     * @param key _more_
     */
    public void removeProperty(Object key) {
        if (properties == null) {
            properties = new Hashtable();
        }
        properties.remove(key);

    }

    /**
     * _more_
     *
     * @param key _more_
     * @param value _more_
     */
    public void putProperty(Object key, Object value) {
        if (properties == null) {
            properties = new Hashtable();
        }
        properties.put(key, value);
    }

    /**
     * _more_
     *
     * @param link _more_
     */
    public void addCategoryLink(String link) {
        categoryLinks.add(link);
    }

    /**
     * _more_
     *
     * @param key _more_
     *
     * @return _more_
     */
    public Object getProperty(Object key) {
        if (properties == null) {
            return null;
        }

        return properties.get(key);
    }

    /**
     * WikiPageHandler _more_
     *
     *
     * @author IDV Development Team
     * @version $Revision: 1.3 $
     */
    public static interface WikiPageHandler {

        /**
         * _more_
         *
         * @param wikiUtil _more_
         * @param name _more_
         * @param label _more_
         *
         * @return _more_
         */
        public String getWikiLink(WikiUtil wikiUtil, String name,
                                  String label);

        /**
         * _more_
         *
         * @param wikiUtil _more_
         * @param property _more_
         *
         * @return _more_
         */
        public String getWikiPropertyValue(WikiUtil wikiUtil,
                                           String property);
    }

    /**
     * _more_
     *
     * @param property _more_
     *
     * @return _more_
     */
    public String getInfoBox(String property) {
        StringBuffer sb   = new StringBuffer();
        List<String> toks = (List<String>) StringUtil.split(property, "\n",
                                true, true);
        String firstLine = toks.get(0);
        toks.remove(0);
        /*
          {{Infobox file format
          | name = Network Common Data Form
          | icon =
          | extension = .nc<br/>.cdf
          | mime = application/netcdf<br/>application/x-netcdf
          | owner = [[University Corporation for Atmospheric Research|UCAR]]
          | typecode =
          | magic = CDF\001
          | genre = scientific binary data
          | containerfor =
          | containedby =
          | extendedfrom = [[Common Data Format|CDF]]
          | extendedto =
          | standard =
          }}
        */
        sb.append(HtmlUtils.open(HtmlUtils.TAG_TABLE));
        String title = "";
        for (String line : toks) {
            String[] toks2 = StringUtil.split(line, "=", 2);
            if (toks2 == null) {
                continue;
            }
            String name = toks2[0].trim();
            if (name.startsWith("|")) {
                name = name.substring(1).trim();
            }
            if (name.equals("name")) {
                title = toks2[1].trim();
            } else if (toks2[1].trim().length() > 0) {
                sb.append(
                    HtmlUtils.rowTop(
                        HtmlUtils.col(
                            name,
                            HtmlUtils.cssClass(
                                "wiki-infobox-entry-title")) + HtmlUtils.col(
                                    toks2[1],
                                    HtmlUtils.cssClass(
                                        "wiki-infobox-entry"))));

            }
        }
        sb.append(HtmlUtils.close(HtmlUtils.TAG_TABLE));
        String div = HtmlUtils.makeShowHideBlock(title, sb.toString(), true,
                         HtmlUtils.cssClass("wiki-infobox-title"),
                         HtmlUtils.cssClass("wiki-infobox"));
        div = wikify(div, null);
        floatBoxes.add(div);

        return "";
        //        return "<table class=\"wiki-toc-wrapper\" align=\"right\" width=\"30%\"><tr><td>"
        //                + div + "</td></tr></table><br clear=right>";
    }

    /**
     * _more_
     *
     * @param property _more_
     *
     * @return _more_
     */
    public String getPropertyValue(String property) {
        if (property.startsWith("Infobox")) {
            return getInfoBox(property);
        }

        return null;
    }

    /**
     * _more_
     *
     * @param s _more_
     * @param handler _more_
     *
     * @return _more_
     */
    public String wikify(String s, WikiPageHandler handler) {


        s = s.replace("\\\\[", "_BRACKETOPEN_");

        if (getReplaceNewlineWithP()) {
            s = s.replaceAll("\r\n\r\n", "\n<p>\n");
            s = s.replaceAll("\r\r", "\n<p>\n");
        }
        //        System.err.println (s);
        s = s.replaceAll("'''''([^']+)'''''", "<b><i>$1</i></b>");
        s = s.replaceAll("'''([^']+)'''", "<b>$1</b>");
        s = s.replaceAll("''([^']+)''", "<i>$1</i>");
        Pattern pattern;
        Matcher matcher;






        pattern = Pattern.compile("\\[\\[([^\\]|]+)\\|?([^\\]]*)\\]\\]");
        matcher = pattern.matcher(s);
        while (matcher.find()) {
            String name  = matcher.group(1);
            String label = matcher.group(2);
            int    start = matcher.start(0);
            int    end   = matcher.end(0);
            String link;
            if (handler == null) {
                if (label.trim().length() == 0) {
                    label = name;
                }
                link = "<a href=\"" + name + "\">" + label + "</a>";
            } else {
                link = handler.getWikiLink(this, name, label);
            }
            s       = s.substring(0, start) + link + s.substring(end);
            matcher = pattern.matcher(s);
        }

        int cnt = 0;
        pattern = Pattern.compile("\\[([^\\]]+)\\]");
        matcher = pattern.matcher(s);
        while (matcher.find()) {
            String name  = matcher.group(1).trim();
            int    idx   = name.indexOf(" ");
            int    start = matcher.start(0);
            int    end   = matcher.end(0);
            if (idx > 0) {
                String label = name.substring(idx);
                name = name.substring(0, idx);
                String ahref =
                    "<a title=\"" + name
                    + "\" class=\"wiki-link-external\" target=\"externalpage\" href=\""
                    + name + "\">";
                s = s.substring(0, start) + ahref + label + "</a>"
                    + s.substring(end);
            } else {
                cnt++;
                String ahref =
                    "<a title=\"" + name
                    + "\" class=\"wiki-link-external\" target=\"externalpage\" href=\""
                    + name + "\">";
                s = s.substring(0, start) + ahref + "_BRACKETOPEN_" + cnt
                    + "_BRACKETCLOSE_</a>" + s.substring(end);
            }
            matcher = pattern.matcher(s);
        }





        List headings = new ArrayList();
        pattern = Pattern.compile("(?m)^\\s*(==+)([^=]+)(==+)\\s*$");
        matcher = pattern.matcher(s);
        while (matcher.find()) {
            String prefix = matcher.group(1).trim();
            String label  = matcher.group(2).trim();
            //            System.err.println("MATCH " + prefix + ":" + label);
            int    start = matcher.start(0);
            int    end   = matcher.end(0);
            int    level = prefix.length();
            String value;
            if (label.startsWith(TAG_PREFIX)) {
                value = "<div class=\"wiki-h" + level + "\">" + label
                        + "</div>";
            } else {
                value = "<a name=\"" + label + "\"></a><div class=\"wiki-h"
                        + level + "\">" + label + "</div>";
                //            if(level==1)
                //                value = value+"<hr class=\"wiki-hr\">";
                headings.add(new Object[] { new Integer(level), label });
            }
            s       = s.substring(0, start) + value + s.substring(end);
            matcher = pattern.matcher(s);
        }







        int          ulCnt = 0;
        int          olCnt = 0;
        StringBuffer buff  = new StringBuffer();
        for (String line :
                (List<String>) StringUtil.split(s, "\n", false, false)) {
            String tline = line.trim();
            if (tline.equals("----")) {
                buff.append("<hr>");
                buff.append("\n");

                continue;
            }
            int starCnt = 0;
            while (tline.startsWith("*")) {
                tline = tline.substring(1);
                starCnt++;
            }
            if (starCnt > 0) {
                if (starCnt > ulCnt) {
                    while (starCnt > ulCnt) {
                        buff.append("<ul>\n");
                        ulCnt++;
                    }
                } else {
                    while ((starCnt < ulCnt) && (ulCnt > 0)) {
                        buff.append("</ul>\n");
                        ulCnt--;
                    }
                }
                buff.append("<li> ");
                buff.append(tline);
                buff.append("</li> ");
                buff.append("\n");

                continue;
            }
            while (ulCnt > 0) {
                buff.append("</ul>\n");
                ulCnt--;
            }


            int hashCnt = 0;
            while (tline.startsWith("#")) {
                tline = tline.substring(1);
                hashCnt++;
            }
            if (hashCnt > 0) {
                if (hashCnt > olCnt) {
                    while (hashCnt > olCnt) {
                        buff.append("<ol>\n");
                        olCnt++;
                    }
                } else {
                    while ((hashCnt < olCnt) && (olCnt > 0)) {
                        buff.append("</ol>\n");
                        olCnt--;
                    }
                }
                buff.append("<li> ");
                buff.append(tline);
                buff.append("\n");

                continue;
            }

            while (olCnt > 0) {
                buff.append("</ol>\n");
                olCnt--;
            }

            buff.append(line);
            buff.append("\n");
        }
        while (ulCnt > 0) {
            buff.append("</ul>\n");
            ulCnt--;
        }

        while (olCnt > 0) {
            buff.append("</ol>\n");
            olCnt--;
        }

        s = buff.toString();


        //        System.err.println("S:" + s.trim()+"\n***********************");
        StringBuffer sb      = new StringBuffer();
        int          baseIdx = 0;
        while (true) {
            int idx1 = s.indexOf("{{", baseIdx);
            if (idx1 < 0) {
                //                System.err.println("no idx1");
                sb.append(s.substring(baseIdx));

                break;
            }
            int idx2 = s.indexOf(TAG_SUFFIX, idx1);
            if (idx2 <= idx1) {
                //                System.err.println("no idx2");
                sb.append(s.substring(baseIdx));

                break;
            }
            sb.append(s.substring(baseIdx, idx1));
            String property = s.substring(idx1 + 2, idx2);
            baseIdx = idx2 + 2;

            if (property.equals(PROP_NOHEADING)) {
                makeHeadings = false;
            } else if (property.equals(PROP_HEADING)) {
                makeHeadings = true;
            } else {
                String value = null;
                if (handler != null) {
                    value = handler.getWikiPropertyValue(this, property);
                }
                if (value == null) {
                    value = "Unknown property:" + property;
                }
                sb.append(value);
            }
        }
        s = sb.toString();


        /*
          <block title="foo">xxxxx</block>
         */
        sb = new StringBuffer();
        while (true) {
            int idx1 = s.indexOf("<block");
            if (idx1 < 0) {
                break;
            }
            int idx2 = s.indexOf(">", idx1);
            if (idx2 < 0) {
                break;
            }
            int idx3 = s.indexOf("</block>", idx2);
            if (idx3 < 0) {
                break;
            }
            String    first    = s.substring(0, idx1);
            String    attrs    = s.substring(idx1 + 6, idx2);
            String    inner    = s.substring(idx2 + 1, idx3);
            Hashtable props    = StringUtil.parseHtmlProperties(attrs);


            boolean   open     = Misc.getProperty(props, ATTR_OPEN, true);
            boolean   decorate = Misc.getProperty(props, ATTR_DECORATE, true);
            String    title    = Misc.getProperty(props, ATTR_TITLE, "");
            sb.append(first);

            //<block show="ismobile"
            String  show = Misc.getProperty(props, ATTR_SHOW, (String) null);
            boolean shouldShow = true;

            if (show != null) {
                if (show.equals("mobile")) {
                    if ( !getMobile()) {
                        shouldShow = false;
                    }
                } else if (show.equals("!mobile")) {
                    if (getMobile()) {
                        shouldShow = false;
                    }
                } else if (show.equals("none")) {
                    shouldShow = false;
                } else if (show.startsWith("user")) {
                    if (user == null) {
                        shouldShow = false;
                    } else {
                        shouldShow = true;
                    }
                }
            }


            if (shouldShow) {
                if (decorate) {
                    sb.append(HtmlUtils.makeShowHideBlock(title, inner, open,
                            HtmlUtils.cssClass("wiki-blockheader"),
                            HtmlUtils.cssClass("wiki-block")));
                } else {
                    sb.append(inner);
                }
            }
            s = s.substring(idx3 + "</block>".length());
        }
        sb.append(s);
        s = sb.toString();
        s = s.replace("_BRACKETOPEN_", "[");
        s = s.replace("_BRACKETCLOSE_", "]");
        //        s = s.replaceAll("(\n\r)+","<br>\n");
        //        s = s.replaceAll("\n+","<br>\n");

        if (getMakeHeadings()) {
            if (headings.size() >= 2) {
                StringBuffer toc = new StringBuffer();
                makeHeadings(headings, toc, -1, "");
                String block = HtmlUtils.makeShowHideBlock("Contents",
                                   toc.toString(), true,
                                   HtmlUtils.cssClass("wiki-tocheader"),
                                   HtmlUtils.cssClass("wiki-toc"));
                floatBoxes.add(block);

                String blocks =
                    "<table class=\"wiki-toc-wrapper\" align=\"right\" width=\"30%\"><tr><td>"
                    + StringUtil.join("<br>", floatBoxes)
                    + "</td></tr></table>";
                s = blocks + s;
            }
        }

        if (categoryLinks.size() > 0) {
            s = s + HtmlUtils.div(
                "<b>Categories:</b> "
                + StringUtil.join(
                    "&nbsp;|&nbsp; ", categoryLinks), HtmlUtils.cssClass(
                    "wiki-categories"));
        }



        return s;

    }


    /**
     * _more_
     *
     * @param headings _more_
     * @param toc _more_
     * @param parentLevel _more_
     * @param parentPrefix _more_
     */
    private static void makeHeadings(List headings, StringBuffer toc,
                                     int parentLevel, String parentPrefix) {
        int    cnt          = 0;
        int    currentLevel = -1;
        String prefix       = "";
        while (headings.size() > 0) {
            Object[] pair  = (Object[]) headings.get(0);
            int      level = ((Integer) pair[0]).intValue();
            if ((level > currentLevel) && (currentLevel >= 0)) {
                makeHeadings(headings, toc, currentLevel, prefix);

                continue;
            } else if (level < currentLevel) {
                if (parentLevel >= 0) {
                    return;
                }
            }
            headings.remove(0);
            cnt++;
            String label = (String) pair[1];
            if (parentPrefix.length() > 0) {
                prefix = parentPrefix + "." + cnt;
            } else {
                prefix = "" + cnt;
            }
            //            System.err.println(prefix);
            toc.append(StringUtil.repeat("&nbsp;&nbsp;", level - 1));
            toc.append("<a href=\"#" + label + "\">");
            toc.append(prefix);
            toc.append(HtmlUtils.space(1));
            toc.append(label);
            toc.append("</a><br>\n");
            currentLevel = level;
        }

    }


    /**
     * _more_
     *
     * @param args _more_
     */
    public static void main(String[] args) {
        try {
            String contents = IOUtil.readContents(new java.io.File(args[0]));
            contents = new WikiUtil().wikify(contents, null);
            System.out.println("\ncontents:" + contents);
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }

    /**
     * Set the MakeHeadings property.
     *
     * @param value The new value for MakeHeadings
     */
    public void setMakeHeadings(boolean value) {
        this.makeHeadings = value;
    }

    /**
     * Get the MakeHeadings property.
     *
     * @return The MakeHeadings
     */
    public boolean getMakeHeadings() {
        return this.makeHeadings;
    }


    /**
     * Set the ReplaceNewlineWithP property.
     *
     * @param value The new value for ReplaceNewlineWithP
     */
    public void setReplaceNewlineWithP(boolean value) {
        this.replaceNewlineWithP = value;
    }

    /**
     * Get the ReplaceNewlineWithP property.
     *
     * @return The ReplaceNewlineWithP
     */
    public boolean getReplaceNewlineWithP() {
        return this.replaceNewlineWithP;
    }

    /**
     *  Set the Mobile property.
     *
     *  @param value The new value for Mobile
     */
    public void setMobile(boolean value) {
        mobile = value;
    }

    /**
     *  Get the Mobile property.
     *
     *  @return The Mobile
     */
    public boolean getMobile() {
        return mobile;
    }

    /**
     * Set the User property.
     *
     * @param value The new value for User
     */
    public void setUser(String value) {
        user = value;
    }

    /**
     * Get the User property.
     *
     * @return The User
     */
    public String getUser() {
        return user;
    }



}
