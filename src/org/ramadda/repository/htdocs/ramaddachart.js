/**
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


/*
This package supports charting and mapping of georeferenced time series data
It requires pointdata.js

Use:
<div id="example"></div>
...
var recordFields  = [new RecordField(...), ... see below]
var data  = [new Record(...), ...]
var pointData = new  PointData("Example data set",  recordFields, data);
var chart = new  RamaddaChart("example" , pointData);

*/


var ramaddaGlobalChart;


/*
Create a chart
id - the id of this chart. Has to correspond to a div tag id 
pointData - A PointData object (see below)
 */
function RamaddaChart(id, pointData) {
    var theChart  = this;
    ramaddaGlobalChart = this;
    this.id = id;
    this.realpointData  = pointData;
    this.pointData  = null;
    this.fieldsDivId =id +"_fields";
    this.chartDivId =id +"_chart";
    init_RamaddaChart(this);
    this.createChart();
    this.chart = new google.visualization.LineChart(document.getElementById(this.chartDivId));
    this.setPointData(pointData);
    //    setTimeout(function(){theChart.setPointData(null);},3000);
}

function init_RamaddaChart(theChart) {
    //        $.getJSON(url, function(data) {
    theChart.getId = function() {
        return this.id;
    }


    theChart.setPointData = function(pointData) {
        this.pointData = pointData;
        this.addFields();
        this.loadData();
    }


    theChart.createChart = function() {
        var fieldsDiv =  "<div id=\"" + this.fieldsDivId +"\" class=chart-fields>";
        var chartDiv =  "<div id=\"" + this.chartDivId +"\" style=\"width: 900px; height: 500px;\"></div>\n";

        var html = "";
        html += "<table width=100%>";
        html += "<tr valign=top><td>";
        html += fieldsDiv;
        html += "</td><td>";
        html += chartDiv;
        html += "</td></tr></table>";
        $("#" + this.id).html(html);


    }


    theChart.addFields = function() {
        if(!this.haveData()) {
            $("#" + this.fieldsDivId).html("No fields selected");
            return;
        }
        var html = "";
        var fields = this.pointData.getRecordFields();
        this.displayedFields = [fields[0]];
        var checkboxClass = this.id +"_checkbox";
        for(i=0;i<fields.length;i++) { 
            var field = fields[i];
            field.checkboxId  = this.id +"_cbx" + i;
            html += "<input id=\"" + field.checkboxId +"\" class=\""  + checkboxClass +"\"  type=checkbox value=true ";
            if(this.displayedFields.indexOf(field)>=0) {
                html+= " checked ";
            }
            html += "/> ";
            html += field.label;
            html+= "<br>";
        }
        $("#" + this.fieldsDivId).html(html);
        //Listen for changes to the checkboxes
        $("." + checkboxClass).change(function() {
                theChart.loadData();
            });

    }

    theChart.setDisplayedFields = function() {
        this.displayedFields = [];
        var fields = this.pointData.getRecordFields();
        for(i=0;i<fields.length;i++) { 
            var field = fields[i];
            if($("#" + field.checkboxId).is(':checked')) {
                this.displayedFields.push(field);
            }
        }

        if(this.displayedFields.length==0) {
            var fields = this.pointData.getRecordFields();
            if(fields.length==0) return;
            this.displayedFields.push(fields[0]);
        }
    }



    theChart.loadData = function() {
        if(!this.haveData()) {
            this.chart.clearChart();
            return;
        }
        
        this.setDisplayedFields();

        if(this.displayedFields.length==0) {
            $("#" + this.chartDivId).html("No fields selected");
            return;
        }
        var dataList = [];
       
        //The first entry in the dataList is the array of names
        //The first field is the domain, e.g., time or index
        //        var fieldNames = ["domain","depth"];
        var fieldNames = ["domain"];
        for(i=0;i<this.displayedFields.length;i++) { 
            var field = this.displayedFields[i];
            var name  = field.getLabel();
            if(field.getUnit()!=null) {
                name += " (" + field.getUnit()+")";
            }
            fieldNames.push(name);
        }
        dataList.push(fieldNames);

        //These are Record objects
        var records = this.pointData.getData();
        for(j=0;j<records.length;j++) { 
            var record = records[j];
            var values = [];
            var date = record.getDate();
            //Add the date or index field
            if(date!=null) {
                values.push(j);
                //                values.push(date);
            } else {
                values.push(j);
            }
            //            values.push(record.getElevation());
            for(var i=0;i<this.displayedFields.length;i++) { 
                var field = this.displayedFields[i];
                var value = record.getValue(field.getIndex());
                values.push(value);
            }
            dataList.push(values);
        }

        var dataTable = google.visualization.arrayToDataTable(dataList);

        //Not quite sure about the axis settings
        var options = {
            series: [
        {targetAxisIndex:0},
        {targetAxisIndex:1},
        {targetAxisIndex:0},
        {targetAxisIndex:1},
        {targetAxisIndex:0},
                     ],
            title: this.pointData.getName(),
            chartArea:{xxleft:20,xxtop:0,height:"85%"}
        };
        this.chart.draw(dataTable, options);
    }

    theChart.haveData = function() {
        return this.pointData!=null;
    }
}

