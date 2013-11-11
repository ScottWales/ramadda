/*
* Copyright 2008-2013 Geode Systems LLC
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

package org.ramadda.repository;


import org.ramadda.util.HtmlUtils;

import ucar.unidata.util.IOUtil;
import ucar.unidata.xml.XmlUtil;

import java.io.*;

import java.io.UnsupportedEncodingException;

import java.net.*;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;


/**
 *
 *
 * @author RAMADDA Development Team
 */
public class RepositoryUtil {


    //When we make any real change to the css or javascript change this version
    //so the browsers will pick up the new resource
    //The imports.html header has a ${htdocs_version} macro in it
    //that gets replaced with  this. Repository checks incoming paths and strips this off

    /** _more_ */
    public static final String HTDOCS_VERSION = "htdocs_v1_5";

    /** _more_ */
    public static final String HTDOCS_VERSION_SLASH = "/" + HTDOCS_VERSION;


    /** timezone */
    public static final TimeZone TIMEZONE_DEFAULT =
        TimeZone.getTimeZone("UTC");

    /** the file separator id */
    public static final String FILE_SEPARATOR = "_file_";

    /** The regular expression that matches the entry id */
    public static final String ENTRY_ID_REGEX =
        "[a-f|0-9]{8}-([a-f|0-9]{4}-){3}[a-f|0-9]{12}_";



    /**
     * _more_
     *
     * @param password _more_
     *
     * @return _more_
     */
    public static String hashString(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            md.update(password.getBytes("UTF-8"));
            byte[] bytes  = md.digest();
            String result = encodeBase64(bytes);

            return result.trim();
        } catch (NoSuchAlgorithmException nsae) {
            throw new IllegalStateException(nsae.getMessage());
        } catch (UnsupportedEncodingException uee) {
            throw new IllegalStateException(uee.getMessage());
        }
    }





    /**
     * Encode the byte string as a base64 encoded string
     *
     * @param b  the bytes
     *
     * @return  the encoded string
     */
    public static String encodeBase64(byte[] b) {
        return javax.xml.bind.DatatypeConverter.printBase64Binary(b);
    }

    /**
     * Decode the given base64 String
     *
     * @param s Holds the base64 encoded bytes
     * @return The decoded bytes
     */
    public static byte[] decodeBase64(String s) {
        return javax.xml.bind.DatatypeConverter.parseBase64Binary(s);
    }





    /**
     * Make a date format from the format string
     *
     * @param formatString  the format string
     *
     * @return  the date formatter
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
     * @param fileName the filename
     *
     * @return  the pruned filename
     */
    public static String getFileTail(String fileName) {
        int idx = fileName.indexOf(FILE_SEPARATOR);
        if (idx >= 0) {
            fileName = fileName.substring(idx + FILE_SEPARATOR.length());
        } else {
            /*
               We have this here for files from old versions of RAMADDA where we did
               not add the StorageManager.FILE_SEPARATOR delimiter and it looked something like:
                     "62712e31-6123-4474-a96a-5e4edb608fd5_<filename>"
            */
            fileName = fileName.replaceFirst(ENTRY_ID_REGEX, "");
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
     * MissingEntry Exception
     *
     * @author RAMADDA Development Team
     */
    public static class MissingEntryException extends Exception {

        /**
         * Create an exception with the message
         *
         * @param msg  the message
         */
        public MissingEntryException(String msg) {
            super(msg);
        }
    }


    /**
     * Make a header from the String
     *
     * @param h  the header text
     *
     * @return  the header
     */
    public static String header(String h) {
        return HtmlUtils.div(h, HtmlUtils.cssClass("ramadda-heading-1"));
    }


    /**
     * Encode the input string
     *
     * @param s  the string to encode
     *
     * @return  the encoded String
     */
    public static final String encodeUntrustedText(String s) {
        //        s = s.replaceAll("&","&amp;;");
        //
        //Note: if this is wrong then we can get an XSS attack from the anonymous upload.
        //If we encode possible attack vectors (<,>) as entities then we edit the entry they
        //get turned into the raw character and we're owned.
        s = s.replaceAll("&", "_AMP_");
        s = s.replaceAll("<", "_LT_");
        s = s.replaceAll(">", "_GT_");
        s = s.replaceAll("\"", "&quot;");

        //        s = HtmlUtils.urlEncode(s);
        //       s = s.replace("+", " ");
        return s;
    }







    /**
     * Make a hash of the plain text password
     *
     * @param password  the password
     *
     * @return  the hashed pw
     */
    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            md.update(password.getBytes("UTF-8"));
            byte[] bytes  = md.digest();
            String result = encodeBase64(bytes);

            return result.trim();
        } catch (NoSuchAlgorithmException nsae) {
            throw new IllegalStateException(nsae.getMessage());
        } catch (UnsupportedEncodingException uee) {
            throw new IllegalStateException(uee.getMessage());
        }
    }


    /**
     * This is a routine created by Matias Bonet to handle pre-existing passwords that
     * were hashed via md5
     *
     * @param password The password
     *
     * @return hashed password
     */
    public static String hashPasswordForOldMD5(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(password.getBytes("UTF-8"));
            byte         messageDigest[] = md.digest();
            StringBuffer hexString       = new StringBuffer();
            for (int i = 0; i < messageDigest.length; i++) {
                String hex = Integer.toHexString(0xFF & messageDigest[i]);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            //            System.out.println(hexString.toString());
            return hexString.toString();
        } catch (NoSuchAlgorithmException nsae) {
            throw new IllegalStateException(nsae.getMessage());
        } catch (UnsupportedEncodingException uee) {
            throw new IllegalStateException(uee.getMessage());
        }
    }




}
