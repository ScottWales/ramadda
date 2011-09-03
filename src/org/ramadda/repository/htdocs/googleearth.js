//
//Provides a wrapper around using the google earth plugin
//Supports multiple google earths in one web page
//

//list of all the GoogleEarth objects 
var  googleEarths = new Array();

//Class for holding placemark info
function MyPlacemark(id,name,desc,lat,lon, icon, polygons) {
    this.id = id;
    this.name = name;
    this.description = desc;
    this.lat = lat;
    this.lon = lon;
    this.icon = icon;
    this.polygons = polygons;
    this.details = null;
}



//Wrapper around an instantiation of a google earth plugin
function GoogleEarth(id, url) {
    this.googleEarth = null;
    this.placemarksToAdd = new Array();
    this.placemarks = new Array();
    this.url = url;
    this.id = id;
    googleEarths[id] = this;
    var DEFAULT_RANGE = 4999999;

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
        var lookAt = this.googleEarth.createLookAt('');
        lookAt.set(40.0, -107, 10000000, this.googleEarth.ALTITUDE_RELATIVE_TO_GROUND,
               0,0,0);
        lookAt.setRange(DEFAULT_RANGE);
        this.googleEarth.getView().setAbstractView(lookAt);
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
                             firstPlacemark.lon,DEFAULT_RANGE);
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
        var polygons = new Array();
        if(points) {
            var tmpArray = new Array();
            polygons.push(tmpArray);
            var lastLon = points[1];
            var lastLat = points[0];
            for(i=0;i<points.length;i+=2) {
                var lat = points[i];
                var lon = points[i+1];
                //TODO: interpolate the latitude
                if(false  && lastLon!=lon) {
                    var crosses = false;
                    if(lastLon<-90 && lon>90) {
                        tmpArray.push(lat);
                        tmpArray.push(-180);
                        tmpArray = new Array();
                        tmpArray.push(lat);
                        tmpArray.push(180);
                        polygons.push(tmpArray);
                    } else  if(lastLon>90 && lon<-90) {
                        tmpArray.push(lat);
                        tmpArray.push(180);
                        tmpArray = new Array();
                        tmpArray.push(lat);
                        tmpArray.push(-180);
                        polygons.push(tmpArray);
                    }
                    lastLon  = lon;
                    lastLat  = lat;
                }
                tmpArray.push(lat);
                tmpArray.push(lon);
            }
        }
        pm = new MyPlacemark(id,name,desc, lat,lon,icon, polygons)
        this.placemarks[id] = pm;
        this.addThePlacemark(pm);
}

    this.addThePlacemark = function(pm) {
        if (!this.googleEarth) {
            this.placemarksToAdd.push(pm);
            return;
        }
        var placemark = this.googleEarth.createPlacemark('');
        var _this = this;
        google.earth.addEventListener(placemark, 'click', function(event) {
                // Prevent the default balloon from appearing.
                event.preventDefault();
                _this.entryClicked(pm.id, true);
            });

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

        if(pm.polygons) {
            var colors = ["ffff0000",
                          "ff00ff00",
                          "ff0000ff",
                          "ffffff00",
                          "ffffffff",
                          "ff000000"];

            var msg = "";
            for(polygonIdx=0;polygonIdx<pm.polygons.length;polygonIdx++) {
                var points = pm.polygons[polygonIdx];
                var lineString = this.googleEarth.createLineString('');
                lineString.setTessellate(true);
                lineString.setAltitudeMode(this.googleEarth.ALTITUDE_CLAMP_TO_GROUND);

                msg += " polygon:";
                for (i = 0; i < points.length; i+=2) {
                    msg+= " " +points[i] +" " + points[i+1];
                    lineString.getCoordinates().pushLatLngAlt(points[i], points[i+1], 0);
                }
                msg+="\n";

                var lineStringPlacemark = this.googleEarth.createPlacemark('');
                lineStringPlacemark.setGeometry(lineString);
                lineStringPlacemark.setStyleSelector(this.googleEarth.createStyle(''));
                var lineStyle = lineStringPlacemark.getStyleSelector().getLineStyle();
                lineStyle.setWidth(2);
                lineStyle.getColor().set('ff0000ff');  // aabbggrr format
                //                lineStyle.getColor().set(colors[polygonIdx]);
                this.googleEarth.getFeatures().appendChild(lineStringPlacemark);
            }
        }
    }



    this.placemarkClick = function(id, popup) {
        placemark =this.placemarks[id];
        if(!placemark) {
            alert("no placemark");
            return;
        }
        this.setLocation(placemark.lat,placemark.lon);
        if(!popup && !showDetails()) {
            return;
        }
        var content = placemark.description;
        var balloon = this.googleEarth.createHtmlStringBalloon('');
        balloon.setFeature(placemark.placemark);
        //        balloon.setMaxHeight(100);
        balloon.setContentString(content);
        this.googleEarth.setBalloon(balloon);
    }


    this.setLocation = function(lat,lon, range) {
        if (!this.googleEarth) return;
        var lookAt = this.googleEarth.getView().copyAsLookAt(this.googleEarth.ALTITUDE_RELATIVE_TO_GROUND);
        lookAt.setLatitude(lat);
        lookAt.setLongitude(lon);
        if(range) {
            lookAt.setRange(DEFAULT_RANGE);
        }
        this.googleEarth.getView().setAbstractView(lookAt);
    }

    this.showDetails = function() {
        var cbx = util.getDomObject("googleearth.showdetails");
        if(cbx) {
            return cbx.obj.checked;
        }
        return true;
    }

    this.getThis = function() {
        return this;
    }

    this.googleEarthClickCnt=0;
    this.entryClicked = function(id, force) {
        var _this = this;
        this.googleEarthClickCnt++;
        var myClick = this.googleEarthClickCnt;
        thePlacemark =this.placemarks[id];
        if(!thePlacemark) {
            return;
        }
        this.googleEarth.setBalloon(null);
        this.setLocation(thePlacemark.lat,thePlacemark.lon);
        if(!force && !this.showDetails()) {
            return;
        }
        //Have we gotten the details already?
        if(thePlacemark.details) {
            this.setBalloon(thePlacemark,thePlacemark.details); 
            return;
        }

        var callback = function(request) {
            if(myClick != _this.googleEarthClickCnt) {
                return;
            }
            var xmlDoc=request.responseXML.documentElement;
            var text = getChildText(xmlDoc);
            checkTabs(text);
            thePlacemark.details = text;
            thePlacemark.placemark.setDescription(text);
            _this.setBalloon(thePlacemark,text); 
        }
        var url = "${urlroot}/entry/show?entryid=" + id +"&output=mapinfo";
        util.loadUrl(url, callback,"");
    }


    this.setBalloon = function(thePlacemark, text) {
        var balloon = this.googleEarth.createHtmlStringBalloon('');
        balloon.setFeature(thePlacemark.placemark);
        balloon.setContentString(text);
        balloon.setMaxHeight(300);
        this.googleEarth.setBalloon(balloon);
    }

    var theGoogleEarth = this;
    var callback = function(instance) {
        theGoogleEarth.initCallback(instance);
    }
    google.earth.createInstance(this.id, callback, this.failureCallback);
}
