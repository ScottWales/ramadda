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

package org.ramadda.repository;


import ucar.unidata.util.HtmlUtil;
import ucar.unidata.util.IOUtil;

import java.io.*;

import java.net.*;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;



/**
 *
 *
 * @author RAMADDA Development Team
 * @version $Revision: 1.3 $
 */
public class RepositoryUtil {

    /** timezone */
    public static final TimeZone TIMEZONE_DEFAULT =
        TimeZone.getTimeZone("UTC");


    /**
     * _more_
     *
     * @param b1 _more_
     * @param b2 _more_
     *
     * @return _more_
     */
    public static String buttons(String b1, String b2) {
        return b1 + HtmlUtil.space(2) + b2;
    }

    /**
     * _more_
     *
     * @param b1 _more_
     * @param b2 _more_
     * @param b3 _more_
     *
     * @return _more_
     */
    public static String buttons(String b1, String b2, String b3) {
        return b1 + HtmlUtil.space(2) + b2 + HtmlUtil.space(2) + b3;
    }




    /**
     * _more_
     *
     * @param formatString _more_
     *
     * @return _more_
     */
    public static SimpleDateFormat makeDateFormat(String formatString) {
        SimpleDateFormat dateFormat = new SimpleDateFormat();
        dateFormat.setTimeZone(TIMEZONE_DEFAULT);
        dateFormat.applyPattern(formatString);
        return dateFormat;
    }


    /**
     * This will prune out any leading &lt;unique id&gt;_file_&lt;actual file name&gt;
     *
     * @param fileName _more_
     *
     * @return _more_
     */
    public static String getFileTail(String fileName) {
        int idx = fileName.indexOf(StorageManager.FILE_SEPARATOR);
        if (idx >= 0) {
            fileName =
                fileName.substring(idx
                                   + StorageManager.FILE_SEPARATOR.length());
        } else {
            /*
               We had this here for files from old versions of RAMADDA where we did not add the StorageManager.FILE_SEPARATOR delimiter
            */
            int idx1 = fileName.indexOf("-");
            if (idx1 >= 0) {
                int idx2 = fileName.indexOf("-", idx1);
                if (idx2 >= 0) {
                    idx = fileName.indexOf("_");
                    if (idx >= 0) {
                        fileName = fileName.substring(idx + 1);
                    }
                }
            }
        }
        //Check for Rich's problem
        idx = fileName.lastIndexOf("\\");
        if (idx >= 0) {
            fileName = fileName.substring(idx + 1);
        }
        String tail = IOUtil.getFileTail(fileName);
        return tail;


    }


    /**
     * Class MissingEntryException _more_
     *
     *
     * @author RAMADDA Development Team
     * @version $Revision: 1.3 $
     */
    public static class MissingEntryException extends Exception {

        /**
         * _more_
         *
         * @param msg _more_
         */
        public MissingEntryException(String msg) {
            super(msg);
        }
    }


    /**
     * _more_
     *
     * @param h _more_
     *
     * @return _more_
     */
    public static String header(String h) {
        return "<div class=\"pageheading\">" + h + "</div>";
    }


    /**
     * _more_
     *
     * @param s _more_
     *
     * @return _more_
     */
    public static final String encodeInput(String s) {
        s = HtmlUtil.urlEncode(s);
        s = s.replace("+", " ");
        return s;
    }


    /**
     * _more_
     *
     * @param args _more_
     */
    public static void main(String[] args) {
        for (String s : args) {
            System.err.println(encodeInput(s));
        }
    }


    /**
     * _more_
     *
     * @param urls _more_
     *
     * @return _more_
     */
    public static List<RequestUrl> toList(RequestUrl[] urls) {
        List<RequestUrl> l = new ArrayList<RequestUrl>();
        for (RequestUrl r : urls) {
            l.add(r);
        }
        return l;
    }

    /**
     * _more_
     *
     * @param html _more_
     * @param left _more_
     *
     * @return _more_
     */
    public static String leftIndset(String html, int left) {
        return inset(html, 0, left, 0, 0);
    }

    /**
     * _more_
     *
     * @param html _more_
     * @param top _more_
     * @param left _more_
     * @param bottom _more_
     * @param right _more_
     *
     * @return _more_
     */
    public static String inset(String html, int top, int left, int bottom,
                               int right) {
        return HtmlUtil.div(html, HtmlUtil.style(((top == 0)
                ? ""
                : "margin-top:" + top + "px;") + ((left == 0)
                ? ""
                : "margin-left:" + left + "px;") + ((bottom == 0)
                ? ""
                : "margin-bottom:" + bottom + "px;") + ((right == 0)
                ? ""
                : "margin-right:" + top + "px;")));
    }






}
