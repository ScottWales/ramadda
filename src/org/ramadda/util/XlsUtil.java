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

package org.ramadda.util;


import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;


import ucar.unidata.util.*;



import java.io.FileOutputStream;
import java.io.InputStream;

import java.rmi.RemoteException;

import java.text.SimpleDateFormat;

import java.util.ArrayList;

import java.util.Date;
import java.util.HashSet;
import java.util.List;


/**
 * Class description
 *
 *
 * @version        $version$, Fri, Aug 23, '13
 * @author         Enter your name here...    
 */
public class XlsUtil {

    /**
     * Convert excel to csv
     *
     * @param filename excel file
     * @param skipToFirstNumeric _more_
     * @param sdf If non null then use this to format any date cells
     *
     * @return csv
     *
     * @throws Exception On badness
     */
    public static String xlsToCsv(String filename) {
        try {

            StringBuffer sb   = new StringBuffer();
            InputStream myxls = IOUtil.getInputStream(filename,
                                    XlsUtil.class);
            HSSFWorkbook wb         = new HSSFWorkbook(myxls);
            HSSFSheet    sheet      = wb.getSheetAt(0);
            boolean      seenNumber = false;
            for (int rowIdx = sheet.getFirstRowNum();
                    rowIdx <= sheet.getLastRowNum(); rowIdx++) {
                HSSFRow row = sheet.getRow(rowIdx);
                if (row == null) {
                    continue;
                }

                short firstCol = row.getFirstCellNum();
                for (short col = firstCol; col < row.getLastCellNum();
                        col++) {
                    HSSFCell cell = row.getCell(col);
                    if (cell == null) {
                        break;
                    }
                    String value = cell.toString();
                    if (col > firstCol) {
                        sb.append(",");
                    }
                    sb.append(clean(value));
                }
                sb.append("\n");
            }

            return sb.toString();
        } catch (Exception exc) {
            throw new RuntimeException(exc);

        }
    }

    /**
     * _more_
     *
     * @param s _more_
     *
     * @return _more_
     */
    public static String clean(String s) {
        s = s.trim();
        while (s.endsWith(",")) {
            s = s.substring(0, s.length() - 1);
        }
        s = s.replaceAll(",", "_COMMA_");
        s = s.replaceAll("\n", "_NEWLINE_");

        return s;
    }


    /**
     * _more_
     *
     * @param args _more_
     *
     * @throws Exception _more_
     */
    public static void main(String[] args) throws Exception {
        String csv = xlsToCsv(args[0]);
        System.out.println(csv);
    }



}
