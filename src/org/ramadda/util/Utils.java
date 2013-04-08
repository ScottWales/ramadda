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
import ucar.unidata.util.StringUtil;
import ucar.unidata.util.DateUtil;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.Calendar;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.text.StrTokenizer;


/**
 * A collection of utilities for rss feeds xml.
 *
 * @author Jeff McWhirter
 */

public class Utils {

    public static List<List<String>> tokenize(String source, String rowDelimiter, String columnDelimiter, int skip) {
        int cnt = 0;
        List<List<String>>results =  new ArrayList<List<String>>();
        List<String>lines = StringUtil.split(source,rowDelimiter,true,true);
        for(String line: lines) {
            line = line.trim();
            if(line.length()==0) continue;                
            cnt++;
            if(cnt<=skip) continue;
            if(line.startsWith("#")) continue;
            List<String>toks =  new ArrayList<String>();
            StrTokenizer tokenizer =  StrTokenizer.getCSVInstance(line);
            while(tokenizer.hasNext()) {
                toks.add(tokenizer.nextToken());
            }
            results.add(toks);
        }
        return results;
    }

    public static boolean stringDefined(String s) {
        if(s==null || s.trim().length()==0) return false;
        return true;
    }






    public static double modifiedJulianToJulian(double modifiedJulian) {
        // MJD = JD - 2400000.5 
        return modifiedJulian + 2400000.5;
    }

    /**
       The julian date functions below are from
       http://www.rgagnon.com/javadetails/java-0506.html
     */


    /**
     * Returns the Julian day number that begins at noon of
     * this day, Positive year signifies A.D., negative year B.C.
     * Remember that the year after 1 B.C. was 1 A.D.
     *
     * ref :
     *  Numerical Recipes in C, 2nd ed., Cambridge University Press 1992
     */
    // Gregorian Calendar adopted Oct. 15, 1582 (2299161)
    public static int JGREG= 15 + 31*(10+12*1582);
    public static double HALFSECOND = 0.5;

    public static double toJulian(int[] ymd) {
        int year=ymd[0];
        int month=ymd[1]; // jan=1, feb=2,...
        int day=ymd[2];
        int julianYear = year;
        if (year < 0) julianYear++;
        int julianMonth = month;
        if (month > 2) {
            julianMonth++;
        }
        else {
            julianYear--;
            julianMonth += 13;
        }

        double julian = (java.lang.Math.floor(365.25 * julianYear)
                         + java.lang.Math.floor(30.6001*julianMonth) + day + 1720995.0);
        if (day + 31 * (month + 12 * year) >= JGREG) {
            // change over to Gregorian calendar
            int ja = (int)(0.01 * julianYear);
            julian += 2 - ja + (0.25 * ja);
        }
        return java.lang.Math.floor(julian);
    }

    /**
     * Converts a Julian day to a calendar date
     * ref :
     * Numerical Recipes in C, 2nd ed., Cambridge University Press 1992
     */
    public static int[] fromJulian(double injulian) {
        return fromJulian(injulian, new int[3]);
    }

    public static int[] fromJulian(double injulian, int [] src) {
        int jalpha,ja,jb,jc,jd,je,year,month,day;
        double julian = injulian + HALFSECOND / 86400.0;
        ja = (int) julian;
        if (ja>= JGREG) {
            jalpha = (int) (((ja - 1867216) - 0.25) / 36524.25);
            ja = ja + 1 + jalpha - jalpha / 4;
        }
            
        jb = ja + 1524;
        jc = (int) (6680.0 + ((jb - 2439870) - 122.1) / 365.25);
        jd = 365 * jc + jc / 4;
        je = (int) ((jb - jd) / 30.6001);
        day = jb - jd - (int) (30.6001 * je);
        month = je - 1;
        if (month > 12) month = month - 12;
        year = jc - 4715;
        if (month > 2) year--;
        if (year <= 0) year--;

        src[0]  = year;
        src[1]  = month;
        src[2]  = day;
        return src;
    }



    public static void testJulian(String args[]) {
        // FIRST TEST reference point
        System.out.println("Julian date for May 23, 1968 : "
                           + toJulian( new int[] {1968, 5, 23 } ));
        // output : 2440000
        int results[] = fromJulian(toJulian(new int[] {1968, 5, 23 }));
        System.out.println
            ("... back to calendar : " + results[0] + " "
             + results[1] + " " + results[2]);

        // SECOND TEST today
        Calendar today = Calendar.getInstance();
        double todayJulian = toJulian
            (new int[]{today.get(Calendar.YEAR), today.get(Calendar.MONTH)+1,
                       today.get(Calendar.DATE)});
        System.out.println("Julian date for today : " + todayJulian);
        results = fromJulian(todayJulian);
        System.out.println
            ("... back to calendar : " + results[0] + " " + results[1]
             + " " + results[2]);

        // THIRD TEST
        double date1 = toJulian(new int[]{2005,1,1});
        double date2 = toJulian(new int[]{2005,1,31});
        System.out.println("Between 2005-01-01 and 2005-01-31 : "
                           + (date2 - date1) + " days");

        /*
          expected output :
          Julian date for May 23, 1968 : 2440000.0
          ... back to calendar 1968 5 23
          Julian date for today : 2453487.0
          ... back to calendar 2005 4 26
          Between 2005-01-01 and 2005-01-31 : 30.0 days
        */
    }

    public static String getArticle(String s) {
        s = s.toLowerCase();
        if(s.startsWith("a") || s.startsWith("e") || s.startsWith("i") ||
               s.startsWith("o") || s.startsWith("u")) {
            return "an";
        } else {
            return "a";
        }
    }

    public static Date extractDate(String s) {
        try {
            String yyyymmdd = StringUtil.findPattern(s, "(\\d\\d\\d\\d-\\d\\d-\\d\\d)");
            if(yyyymmdd !=null) {
                return DateUtil.parse(yyyymmdd);
            }
            return null;
        } catch(Exception exc) {
            System.err.println ("Utils.extractDate:" + exc);
            return null;
        }
    }


    public static void main(String args[]) {
        testJulian(args);
    }





}


