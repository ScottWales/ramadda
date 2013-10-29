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


/**
 */
public class GoogleChart {

    public static void addChartImport(StringBuffer sb) {
        sb.append(HtmlUtils.importJS("https://www.google.com/jsapi"));
        sb.append(HtmlUtils.script("if (!(typeof ramaddaLoadedGoodleCharts === 'undefined')) {ramaddaLoadedGoodleCharts=true;google.load('visualization', '1.0', {'packages':['corechart']});\n"));
        //        // Set a callback to run when the Google Visualization API is loaded.
        //        google.setOnLoadCallback(drawChart);
    }

    public static class DataTable {
        public static void init(StringBuffer sb) {
            sb.append("var data = new google.visualization.DataTable();\n");
        }

        public static void addColumn(StringBuffer sb, String type, String name) {
            //data.addColumn('string', 'Name');
            sb.append(HtmlUtils.call("data.addColumn", HtmlUtils.squote(type), HtmlUtils.squote(name)));
        }


    }



}
