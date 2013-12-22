

function RamaddaChart(id) {
    this.data= {'fields':[]};
    this.data2 = 
    [["Site_Id","Latitude","Longitude","Elevation","Year","Julian_Day","Month","Day","Time","Temperature","Pressure","Wind_Speed","Wind_Direction","Relative_Humidity","Delta_T"],
     ["BPT",-64.77,-64.06,8.0,2001.0,335.0,12.0,1.0,"0000",-1.1,null,null,null,null,null],
     ["BPT",-64.77,-64.06,8.0,2001.0,335.0,12.0,1.0,"0010",-1.0,null,null,null,null,null],
     ["BPT",-64.77,-64.06,8.0,2001.0,335.0,12.0,1.0,"0020",-1.1,null,null,null,null,null],
     ["BPT",-64.77,-64.06,8.0,2001.0,335.0,12.0,1.0,"0030",-1.1,null,null,null,null,null],
     ["BPT",-64.77,-64.06,8.0,2001.0,335.0,12.0,1.0,"0040",-1.0,null,null,null,null,null]];
}


function RecordField(id label type missing) {
    this.id = id;
    this.label = label;
    this.type = type;
    this.missing = missing;
}