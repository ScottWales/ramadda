
var  googleEarths = new Array();

function Placemark(id,name,desc,lat,lon, icon, points) {
    this.id = id;
    this.name = name;
    this.description = desc;
    this.lat = lat;
    this.lon = lon;
    this.icon = icon;
    this.points = points;
}



function GoogleEarth(id, url) {
    this.googleEarth = null;
    this.placemarksToAdd = new Array();
    this.placemarks = new Array();
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
    
        var tmpPlacemarks = this.placemarksToAdd;
        this.placemarksToAdd = new Array();
        var firstPlacemark = null;
        for (var i = 0; i < tmpPlacemarks.length; i++) {
            var placemark =  tmpPlacemarks[i];
            if(!firstPlacemark) firstPlacemark = placemark;
            this.addThePlacemark(placemark);
        }
        if(firstPlacemark) {
            this.setLocation(firstPlacemark.lat,
                             firstPlacemark.lon);
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

    this.addPlacemark = function(id,name, desc, lat,lon, icon, points) {
        pm = new Placemark(id,name,desc, lat,lon,icon,points)
        this.placemarks[id] = pm;
        this.addThePlacemark(pm);
    }

    this.addThePlacemark = function(pm) {
        if (!this.googleEarth) {
            this.placemarksToAdd.push(pm);
            return;
        }
        var placemark = this.googleEarth.createPlacemark('');
        pm.placemark = placemark;
        placemark.setName(pm.name);
        if(pm.description) {
            placemark.setDescription(pm.description);
        }
        var point = this.googleEarth.createPoint('');
        point.setLatitude(pm.lat);
        point.setLongitude(pm.lon);
        placemark.setGeometry(point);
        if(pm.icon) {
            var icon = this.googleEarth.createIcon('');
            icon.setHref(pm.icon);
            var style = this.googleEarth.createStyle(''); 
            style.getIconStyle().setIcon(icon); 
            placemark.setStyleSelector(style); 
        }
        this.googleEarth.getFeatures().appendChild(placemark);

        if(pm.points) {
            var lineStringPlacemark = this.googleEarth.createPlacemark('');
            var lineString = this.googleEarth.createLineString('');
            lineStringPlacemark.setGeometry(lineString);
            lineStringPlacemark.setStyleSelector(this.googleEarth.createStyle(''));
            var lineStyle = lineStringPlacemark.getStyleSelector().getLineStyle();
            lineStyle.setWidth(3);
            lineStyle.getColor().set('ff0000ff');  // aabbggrr format
            for (i = 0; i < pm.points.length; i+=2) {
                lineString.getCoordinates().pushLatLngAlt(pm.points[i], pm.points[i+1], 0);
            }
            this.googleEarth.getFeatures().appendChild(lineStringPlacemark);
        }
    }



    this.placemarkClick = function(id) {
        placemark =this.placemarks[id];
        if(!placemark) {
            return;
        }
        this.setLocation(placemark.lat,placemark.lon);
        var content = placemark.description;
        var balloon = this.googleEarth.createHtmlStringBalloon('');
        balloon.setFeature(placemark.placemark);
        balloon.setContentString(content);
        this.googleEarth.setBalloon(balloon);
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







