


source ../../record/generate.tcl


generateRecordClass org.unavco.data.lidar.binary.DoubleLatLonAltRecord  -super org.ramadda.data.point.PointRecord   -fields  { 
    { latitude double}
    { longitude double}
    { altitude double}
} 


generateRecordClass org.unavco.data.lidar.binary.DoubleLatLonAltIntensityRecord  -super org.ramadda.data.point.PointRecord   -fields  { 
    { latitude double}
    { longitude double}
    { altitude double}
    { intensity double}
} 

generateRecordClass org.unavco.data.lidar.binary.FloatLatLonAltRecord  -super org.ramadda.data.point.PointRecord   -fields  { 
    { lat float}
    { lon float}
    { alt float}
}  -extraBody {
    public double getLatitude() {
        return (double) lat;
    }
    public double getLongitude() {
        return (double) lon;
    }
    public double getAltitude() {
        return (double) alt;
    }
}
