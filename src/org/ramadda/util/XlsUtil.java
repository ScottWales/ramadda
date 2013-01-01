/*
 * Copyright 1997-2010 Unidata Program Center/University Corporation for
 * Atmospheric Research, P.O. Box 3000, Boulder, CO 80307,
 * support@unidata.ucar.edu.
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

        StringBuffer sb         = new StringBuffer();
        InputStream  myxls = IOUtil.getInputStream(filename, XlsUtil.class);
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
            for(short col=firstCol;col<row.getLastCellNum();col++) {
                HSSFCell cell = row.getCell(col);
                if(cell == null) {
                    break;
                }
                String value =  cell.toString();
                if(col>firstCol) sb.append(",");
                sb.append(clean(value));
            }
            sb.append("\n");
        }
        return sb.toString();
        } catch(Exception exc) {
            throw new RuntimeException(exc);

        }
    }

    public static String clean(String s) {
        s = s.trim();
        while(s.endsWith(",")) {
            s = s.substring(0, s.length()-1);
        }
        s = s.replaceAll(",","_COMMA_");
        s = s.replaceAll("\n","_NEWLINE_");
        return s;
    }
    

    public static void main(String[]args) throws Exception {
        String csv = xlsToCsv(args[0]);
        System.out.println(csv);
    }



}
