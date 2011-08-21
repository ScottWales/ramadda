
var  googleEarths = new Array();

function Placemark(name,lat,lon, icon, points) {
    this.name = name;
    this.lat = lat;
    this.lon = lon;
    this.icon = icon;
    this.points = points;
}



function GoogleEarth(id, url) {
    this.googleEarth = null;
    this.placemarksToAdd = new Array();
    this.url = url;
    this.id = id;
    googleEarths[id] = this;

    this.urlFinished = function(object) {
        if (!object) {
            // wrap alerts in API callbacks and event handlers
            // in a setTimeout to prevent deadlock in some browsers
            setTimeout(function() {
                    alert('Bad or null KML.');
                }, 0);
            return;
        }
        this.googleEarth.getFeatures().appendChild(object);
        var la = this.googleEarth.createLookAt('');
        la.set(40.0, -107, 10000000, this.googleEarth.ALTITUDE_RELATIVE_TO_GROUND,
               0,0,0);
        this.googleEarth.getView().setAbstractView(la);
    }

    this.initCallback = function(instance) {
        this.googleEarth = instance;
        this.googleEarth.getWindow().setVisibility(true);

        // add a navigation control
        this.googleEarth.getNavigationControl().setVisibility(this.googleEarth.VISIBILITY_AUTO);

        // add some layers
        this.googleEarth.getLayerRoot().enableLayerById(this.googleEarth.LAYER_BORDERS, true);
        this.googleEarth.getLayerRoot().enableLayerById(this.googleEarth.LAYER_ROADS, true);
 


        if(this.url) {
            this.loadKml(url);
        }
    
        var placemarks = this.placemarksToAdd;
        this.placemarksToAdd = new Array();
        for (i = 0; i < placemarks.length; i++) {
            var placemark =  placemarks[i];
            this.addPlacemark(placemark.name, placemark.lat, placemark.lon,placemark.icon,placemark.points);
        }
    }
    

    this.loadKml = function(url) {
        var theGoogleEarth = this;
        var callback = function(object) {
            theGoogleEarth.urlFinished(object);
        }
        google.earth.fetchKml(this.googleEarth, url, callback);
    }

    function failureCallback(errorCode) {
        alert("Failure loading the Google Earth Plugin: " + errorCode);
    }

    this.addPlacemark = function(name, lat,lon, iconUrl, points) {
        if (!this.googleEarth) {
            this.placemarksToAdd.push(new Placemark(name,lat,lon,iconUrl,points));
            return;
        }
        var placemark = this.googleEarth.createPlacemark('');
        placemark.setName(name);
        var point = this.googleEarth.createPoint('');
        point.setLatitude(lat);
        point.setLongitude(lon);
        placemark.setGeometry(point);
        if(iconUrl) {
            var icon = this.googleEarth.createIcon('');
            icon.setHref(iconUrl);
            var style = this.googleEarth.createStyle(''); 
            style.getIconStyle().setIcon(icon); 
            placemark.setStyleSelector(style); 
        }
        if(points) {
            var lineStringPlacemark = this.googleEarth.createPlacemark('');
            var lineString = this.googleEarth.createLineString('');

            lineStringPlacemark.setStyleSelector(this.googleEarth.createStyle(''));
            var lineStyle = lineStringPlacemark.getStyleSelector().getLineStyle();
            lineStyle.setWidth(5);
            lineStyle.getColor().set('9900ffff'); 
            lineStringPlacemark.setGeometry(lineString);
            for (i = 0; i < points.length; i+=2) {
                lineString.getCoordinates().pushLatLngAlt(points[i], points[i+1], 0);
            }
            this.googleEarth.getFeatures().appendChild(lineStringPlacemark);
        }

        this.googleEarth.getFeatures().appendChild(placemark);
    }



    this.setLocation = function(lat,lon) {
        var DEFAULT_RANGE = 4999999;
        if (!this.googleEarth) return;
        var lookAt = this.googleEarth.getView().copyAsLookAt(this.googleEarth.ALTITUDE_RELATIVE_TO_GROUND);
        lookAt.setLatitude(lat);
        lookAt.setLongitude(lon);
        lookAt.setRange(DEFAULT_RANGE);
        this.googleEarth.getView().setAbstractView(lookAt);
    }

    var theGoogleEarth = this;
    var callback = function(instance) {
        theGoogleEarth.initCallback(instance);
    }

    google.earth.createInstance(this.id, callback, this.failureCallback);
}







