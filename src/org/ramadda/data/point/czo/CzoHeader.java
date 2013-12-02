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
package org.ramadda.data.point.czo;


import org.ramadda.util.Utils;

import ucar.unidata.util.IOUtil;
import ucar.unidata.util.StringUtil;

import java.io.*;

import java.util.Hashtable;

import java.util.List;


/**
 * Class description
 *
 *
 * @version        $version$, Mon, Dec 2, '13
 * @author         Enter your name here...    
 */
public class CzoHeader {

    /** _more_          */
    private String name;

    /** _more_          */
    private StringBuffer description = new StringBuffer();

    /** _more_          */
    private String investigator;

    /** _more_          */
    private List<String> keywords;

    /** _more_          */
    private List<String> variableNames;

    /**
     * _more_
     *
     * @param hdr _more_
     *
     * @throws Exception _more_
     */
    public CzoHeader(String hdr) throws Exception {
        processHeader(hdr);
    }

    /**
     * _more_
     *
     * @param hdr _more_
     *
     * @throws Exception _more_
     */
    private void processHeader(String hdr) throws Exception {
        boolean inDoc    = false;
        boolean inHeader = false;
        for (String line : StringUtil.split(hdr, "\n", true, true)) {
            if (line.equals("\\doc")) {
                inDoc = true;

                continue;
            }

            if (line.equals("\\header")) {
                inDoc    = false;
                inHeader = true;

                continue;
            }

            if (inDoc) {
                processDocLine(line);
            } else if (inHeader) {
                processHeaderLine(line);
            }
        }

    }


    /**
     * _more_
     *
     * @param line _more_
     *
     * @throws Exception _more_
     */
    private void processDocLine(String line) throws Exception {
        List<String> toks = StringUtil.splitUpTo(line, ".", 2);
        if (toks.size() != 2) {
            System.err.println("Bad line:" + line);

            return;
        }
        String property = toks.get(0);
        String value    = toks.get(1);
        if ( !Utils.stringDefined(value)) {
            return;
        }
        if (property.equals("TITLE")) {
            name = value;
        } else if (property.equals("ABSTRACT")) {
            description.append(value);
            description.append("\n");
        } else if (property.equals("INVESTIGATOR")) {
            investigator = value;
        } else if (property.equals("KEYWORDS")) {
            keywords = StringUtil.split(value, ",", true, true);
        } else if (property.equals("VARIABLE NAMES")) {
            variableNames = StringUtil.split(value, ",", true, true);
        } else if (property.equals("COMMENTS")) {
            description.append(value);
            description.append("\n");
        }




    }

    /**
     * _more_
     *
     * @param line _more_
     *
     * @throws Exception _more_
     */
    private void processHeaderLine(String line) throws Exception {
        List<String> toks = StringUtil.splitUpTo(line, ".", 2);
        if (toks.size() != 2) {
            System.err.println("Bad line:" + line);

            return;
        }
        String property = toks.get(0);
        String value    = toks.get(1);
        if ( !Utils.stringDefined(value)) {
            return;
        }
        System.err.println(property + "=" + value);

    }



    /**
     * _more_
     *
     * @param args _more_
     *
     * @throws Exception _more_
     */
    public static void main(String[] args) throws Exception {
        for (String file : args) {
            String    contents  = IOUtil.readContents(file, CzoHeader.class);
            CzoHeader czoHeader = new CzoHeader(contents);

        }
    }


}
