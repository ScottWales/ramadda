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

import java.text.DateFormat;
import java.text.SimpleDateFormat;

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

    public static void main(String[]args) throws Exception {
        String date = "APR 8 1987 4:53 PM";
        SimpleDateFormat sdf = new SimpleDateFormat("MMM d yyyy h:mm a");
        System.err.println("date:" + date);
        System.err.println("parsed:" + sdf.parse(date));
    }


}


