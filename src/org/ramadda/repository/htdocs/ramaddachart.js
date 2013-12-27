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


function RamaddaChart(id, pointData) {
    var theChart = this;
    this.pointData  = pointData;
    this.id = id;
    this.chartDivId =id +"_chart";

    var fields = this.pointData.getRecordFields();

    var html = "<table width=100%><tr valign=top><td><div class=chart-fields>";
    this.displayedFields = [fields[0]];

    var checkboxClass = id +"_checkbox";
    for(i=0;i<fields.length;i++) { 
        var field = fields[i];
        field.checkboxId  = this.chartDivId +"_cbx" + i;
        html += "<input id=\"" + field.checkboxId +"\" class=\""  + checkboxClass +"\"  type=checkbox value=true ";
        if(this.displayedFields.indexOf(field)>=0) {
            html+= " checked ";
        }
        html += "/> ";
        html += field.label;
        html+= "<br>";
    }
    html += "</div></td><td>";
    html += "<div id=\"" + this.chartDivId +"\" style=\"width: 900px; height: 500px;\"></div>\n";
    html += "</td></tr></table>";

    $("#" + this.id).html(html);

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
        var dataList = [];
        var fieldNames = ["index"];
        for(i=0;i<this.displayedFields.length;i++) { 
            var field = this.displayedFields[i];
            fieldNames.push(field.getId());
        }
        dataList.push(fieldNames);
        var records = this.pointData.getData();
        for(j=0;j<records.length;j++) { 
            var record = records[j];
            var values = [];
            values.push(j);
            for(i=0;i<this.displayedFields.length;i++) { 
                var field = this.displayedFields[i];
                values.push(record.getValue(field.getIndex()));
            }
            dataList.push(values);
        }
        var dataTable = google.visualization.arrayToDataTable(dataList);
        var options = {
            series: [{targetAxisIndex:0},{targetAxisIndex:1},{targetAxisIndex:2},],
            title: this.pointData.getName(),
            chartArea:{xleft:20,xtop:0,xwidth:"50%",height:"85%"}
        };
        this.chart.draw(dataTable, options);
    }

    this.chart = new google.visualization.LineChart(document.getElementById(this.chartDivId));

    this.loadData();

}


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


function RecordField(index, id, label, type, missing, unit) {
    this.index = index;
    this.id = id;
    this.label = label;
    this.type = type;
    this.missing = missing;
    this.unit = unit;

    this.getId = function() {
        return this.id;
    }

    this.getLabel = function() {
        return this.label;
    }

    this.getIndex = function() {
        return this.index;
    }
}

function Record(lat, lon, elevation, time, data) {
    this.latitude = lat;
    this.longitude = lon;
    this.elevation = elevation;
    this.time = time;
    this.data = data;
    newRecord(this);
}

function newRecord(record) {
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
    return record;
}


function makeTestData() {
    var recordFields =  [new RecordField(0, "Temperature","Temperature","double",-9999.99, "celsius"),
                         new RecordField(1, "Pressure","Pressure","double",-9999.99, "hPa")];
    var data =  [
                 new Record(-64.77,-64.06,45, null,[8.0,1000]),
                 new Record(-65.77,-64.06,45, null,[8.0,1000])
                 ];

    return new  PointData("Test point data",  recordFields, data);
}

//var chart = new  RamaddaChart("chartdiv", makeTestData());