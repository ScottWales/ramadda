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
    var theChart = this;
    ramaddaGlobalChart = this;
    this.pointData  = pointData;
    //    this.pointData = makeTestPointData();

    this.id = id;
    this.chartDivId =id +"_chart";
    this.jsTextArea =  id + "_js_textarea";
    this.jsSubmit =  id + "_js_submit";
    this.jsOutputId =  id + "_js_output";


    var jsInput = "<textarea rows=10 cols=80 id=\"" + this.jsTextArea +"\"/><br><input value=\"Try it out\" type=submit id=\"" + this.jsSubmit +"\">";

    var jsOutput = "<div id=\"" + this.jsOutputId +"\"/>";

    var chartDiv =  "<div id=\"" + this.chartDivId +"\" style=\"width: 900px; height: 500px;\"></div>\n";

    var fields = this.pointData.getRecordFields();
    var chartFields = "<div class=chart-fields>";


    this.displayedFields = [fields[0]];

    var checkboxClass = id +"_checkbox";
    for(i=0;i<fields.length;i++) { 
        var field = fields[i];
        field.checkboxId  = this.id +"_cbx" + i;
        chartFields += "<input id=\"" + field.checkboxId +"\" class=\""  + checkboxClass +"\"  type=checkbox value=true ";
        if(this.displayedFields.indexOf(field)>=0) {
            chartFields+= " checked ";
        }
        chartFields += "/> ";
        chartFields += field.label;
        chartFields+= "<br>";
    }
    chartFields += "</div>";


    var html = "";
    html += "<table width=100%>";
    html += "<tr valign=top><td>";
    html += chartFields;
    html += "</td><td>";
    html += chartDiv;
    html += "</td></tr></table>";

    html += "<table width=100%>";
    html += "<tr valign=top><td width=50%>";
    html += jsInput;
    html += "</td><td width=50%>";
    html += jsOutput;
    html += "</td></tr></table>";


    $("#" + this.id).html(html);


    $("#" + this.jsSubmit).button().click(function(event){
            var js = "var chart = ramaddaGlobalChart;\n";
            js += "var data = chart.pointData.getData();\n";
            js += "var fields= chart.pointData.getRecordFields();\n";
            js += "var output= \"#" + theChart.jsOutputId  +"\";\n";
            js += $("#" + theChart.jsTextArea).val();
            eval(js);
        });


    //Listen for changes to the checkboxes
    $("." + checkboxClass).change(function() {
            theChart.loadData();
        });


    this.setDisplayedFields = function() {
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


    this.loadData = function() {
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

    this.chart = new google.visualization.LineChart(document.getElementById(this.chartDivId));

    this.loadData();

}


/*
This encapsulates some instance of point data. 
name - the name of this data
recordFields - array of RecordField objects that define the metadata
data - array of Record objects holding the data
*/
function PointData(name, recordFields, data) {
    this.name = name;
    this.recordFields = recordFields;
    this.data = data;
    this.getRecordFields = function() {
        return this.recordFields;
    }

    this.getData = function() {
        return this.data;
    }

    this.getName = function() {
        return this.name;
    }

}


/*
This class defines the metadata for a record column. 
index - the index i the data array
id - string id
label - string label to show to user
type - for now not used but once we have string or other column types we'll need it
missing - the missing value forthis field. Probably not needed and isn't used
as I think RAMADDA passes in NaN
unit - the unit of the value
 */
function RecordField(index, id, label, type, missing, unit) {
    this.index = index;
    this.id = id;
    this.label = label;
    this.type = type;
    this.missing = missing;
    this.unit = unit;
    init_RecordField(this);
}

function init_RecordField(recordField) {
    recordField.getIndex = function() {
        return this.index;
    }
    recordField.getId = function() {
        return this.id;
    }
    recordField.getLabel = function() {
        return this.label;
    }
    recordField.getType = function() {
        return this.type;
    }
    recordField.getMissing = function() {
        return this.missing;
    }
    recordField.getUnit = function() {
        return recordField.unit;
    }
}


/*
The main data record. This holds a lat/lon/elevation, time and an array of data
The data array corresponds to the RecordField fields
 */
function Record(lat, lon, elevation, time, data) {
    this.latitude = lat;
    this.longitude = lon;
    this.elevation = elevation;
    this.time = time;
    this.data = data;
    init_Record(this);
}

//Add the Record class functions to the object
function init_Record(record) {
    record.getData = function() {
        return this.data;
    }
    record.getValue = function(index) {
        return this.data[index];
    }
    record.getLatitude = function() {
        return this.latitude;
    }
    record.getLongitude = function() {
        return this.longitude;
    }
    record.getElevation = function() {
        return this.elevation;
    }
    record.getDate = function() {
        return this.time;
    }
    return record;
}


function makeTestPointData() {
    var json = {
        fields:
        [{index:0,
               id:"field1",
               label:"Field 1",
               type:"double",
               missing:"-999.0",
               unit:"m"},

        {index:1,
               id:"field2",
               label:"Field 2",
               type:"double",
               missing:"-999.0",
               unit:"c"},
            ],
        data: [
               [-64.77,-64.06,45, null,[8.0,1000]],
               [-65.77,-64.06,45, null,[9.0,500]],
               [-65.77,-64.06,45, null,[10.0,250]],
               ]
    };


    var fields = [];
    for(var i=0;i<json.fields.length;i++) {
        var field  = json.fields[i];
        init_RecordField(field);
        fields.push(field);
    }

    var data =[];
    for(var i=0;i<json.data.length;i++) {
        var tuple = json.data[i];
        //lat,lon,alt,time,data values
        data.push(new Record(tuple[0],tuple[1],tuple[2],tuple[3],tuple[4]));
    }

    return new  PointData("Test point data",  fields, data);
}



function getRanges(fields,data) {
    var ranges = [];
    var maxValues = [];
    var minValues = [];
    for(var i=0;i<fields.length;i++) {
        maxValues.push(NaN);
        minValues.push(NaN);
    }

    for(var row=0;row<data.length;row++) {
        for(var col=0;col<fields.length;col++) {
            var value  = data[row].getValue(col);
            if(isNaN(value)) continue;    
            maxValues[col] = (isNaN(maxValues[col])?value:Math.max(value, maxValues[col]));
            minValues[col] = (isNaN(minValues[col])?value:Math.min(value, minValues[col]));
        }
    }
    var tuple =[minValues,maxValues];
    return tuple;
}



