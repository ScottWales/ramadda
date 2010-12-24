


function RepositoryMap (mapId, initialLocation) {
    this.mapDivId = mapId;
    if(!this.mapDivId) {
        this.mapDivId= "map";
    }
    this.initialLocation = initialLocation;
    if(!initialLocation) {
        initialLocation = new OpenLayers.LonLat(-104, 40);
    }
    var zoom = 5;
    var map, layer, markers, boxes, lines;

    this.initMap1 = function(mapDivId){

        map = new OpenLayers.Map(this.mapDivId);
        this.addBaseLayers();

        var control = new OpenLayers.Control();
        OpenLayers.Util.extend(control, {
                draw: function () {
                    // this Handler.Box will intercept the shift-mousedown
                    // before Control.MouseDefault gets to see it
                    this.box = new OpenLayers.Handler.Box( control,
                                                           {"done": this.notice},
                                                           {keyMask: OpenLayers.Handler.MOD_SHIFT});
                    this.box.activate();
                },

                    notice: function (bounds) {
                    var ll = map.getLonLatFromPixel(new OpenLayers.Pixel(bounds.left, bounds.bottom)); 
                    var ur = map.getLonLatFromPixel(new OpenLayers.Pixel(bounds.right, bounds.top)); 
                    alert(ll.lon.toFixed(4) + ", " + 
                          ll.lat.toFixed(4) + ", " + 
                          ur.lon.toFixed(4) + ", " + 
                          ur.lat.toFixed(4));
                }
            });

        this.map.addControl(control);
        this.map.setCenter(initialLocation, zoom);
    }

    this.addWMSLayer  = function(name, url, layer) {
        //"http://vmap0.tiles.osgeo.org/wms/vmap0"
        var layer = new OpenLayers.Layer.WMS( name, url,
                                               {layers: layer} );
        this.map.addLayer(layer);
    }


    this.addBaseLayers = function() {
        var gphy = new OpenLayers.Layer.Google("Google Physical",  {type: google.maps.MapTypeId.TERRAIN});
        var gmap = new OpenLayers.Layer.Google("Google Streets",  {numZoomLevels: 20});
        var ghyb = new OpenLayers.Layer.Google("Google Hybrid",{type: google.maps.MapTypeId.HYBRID, numZoomLevels: 20});
        var gsat = new OpenLayers.Layer.Google("Google Satellite",{type: google.maps.MapTypeId.SATELLITE, numZoomLevels: 22});
        //        this.map.addLayers([gphy, gmap, ghyb, gsat]);

        this.map.addLayer(new OpenLayers.Layer.Yahoo( "Yahoo"));
        this.addWMSLayer("Topo Maps", "http://terraservice.net/ogcmap.ashx",  "DRG");


        var shaded = new OpenLayers.Layer.VirtualEarth("Virtual Earth - Shaded", {
                type: VEMapStyle.Shaded
            });
        var hybrid = new OpenLayers.Layer.VirtualEarth("Virtual Earth - Hybrid", {
                type: VEMapStyle.Hybrid
            });
        var aerial = new OpenLayers.Layer.VirtualEarth("Virtual Earth - Aerial", {
                type: VEMapStyle.Aerial
            });

        this.map.addLayers([shaded, hybrid, aerial]);

        var wms = new OpenLayers.Layer.WMS( "OpenLayers WMS",
                                            "http://vmap0.tiles.osgeo.org/wms/vmap0",
                                             {layers: 'basic'} );

        this.map.addLayer(wms);

        this.graticule = new OpenLayers.Control.Graticule({
                numPoints: 2, 
                labelled: true,
                visible: false,
                layerName: "Grid"
            });
        this.map.addControl(this.graticule);


    }

    this.getMap = function(){
        return this.map;
    }


    this.initMap2 = function(){
        this.map = new OpenLayers.Map( this.mapDivId );
        //        this.map.fractionalZoom = true;
        this.map.minResolution = 0.0000001;
        this.map.minScale = 0.0000001;
        //        this.map.numZoomLevels = 32;

        this.addBaseLayers();
        var control = new OpenLayers.Control();
        OpenLayers.Util.extend(control, {
                draw: function () {
                    // this Handler.Box will intercept the shift-mousedown
                    // before Control.MouseDefault gets to see it
                    this.box = new OpenLayers.Handler.Box( control,
                                                           {"done": this.notice},
                                                           {keyMask: OpenLayers.Handler.MOD_SHIFT});
                    this.box.activate();
                },

                notice: function (bounds) {
                    var ll = this.map.getLonLatFromPixel(new OpenLayers.Pixel(bounds.left, bounds.bottom)); 
                    var ur = this.map.getLonLatFromPixel(new OpenLayers.Pixel(bounds.right, bounds.top)); 
                    alert(ll.lon.toFixed(4) + ", " + 
                          ll.lat.toFixed(4) + ", " + 
                          ur.lon.toFixed(4) + ", " + 
                          ur.lat.toFixed(4));
                }
            });

        //        this.map.addControl(control);
        this.map.setCenter(initialLocation, zoom);
        this.map.addControl( new OpenLayers.Control.LayerSwitcher() );
        this.map.addControl( new OpenLayers.Control.MousePosition() );
    }


    this.onPopupClose = function(evt) {
        if(this.currentPopup) {
            this.map.removePopup(this.currentPopup);
            this.currentPopup.destroy();
            this.currentPopup = null;
            this.hiliteBox('');
        }
    }

    this.findObject = function(id, array) {
        for (i = 0; i < array.length; i++) {
            if(array[i].id == id) {
                return array[i];
            }
        }
        return null;
    }


    this.findMarker = function(id) {
        if(!this.markers) {
            return null;
        }
        return this.findObject(id, this.markers.markers);
    }

    this.findBox = function(id) {
        if(!this.boxes) {
            return null;
        }
        return this.findObject(id, this.boxes.markers);
    }

    this.hiliteBox = function(id) {
        if(this.currentBox) {
            this.currentBox.setBorder("blue");
        }
        this.currentBox = this.findBox(id);
        if(this.currentBox ) {
            this.currentBox.setBorder("red");
        }
    }

    this.hiliteMarker = function(id) {
        marker = this.findMarker(id);
        if(!marker) {
            return;
        }
        this.map.setCenter(marker.location);
        this.showMarkerPopup(marker);
    }

    this.centerOnMarkers = function()  {
        if(!this.markers) return;
        bounds = this.markers.getDataExtent();
        this.map.setCenter(bounds.getCenterLonLat());
        this.map.zoomToExtent(bounds);
    }

    this.addMarker = function(id, location, iconUrl, text) {
        var theMap = this;
        if(!iconUrl) {
            iconUrl = 'http://www.openlayers.org/dev/img/marker.png';
        }
        var sz = new OpenLayers.Size(21, 25);
        var calculateOffset = function(size) {
            return new OpenLayers.Pixel(-(size.w/2), -size.h);
        };
        var icon = new OpenLayers.Icon(iconUrl, sz, null, calculateOffset);
        marker = new OpenLayers.Marker(location, icon);
        marker.id = id;

        if(!this.markers) {
            this.markers = new OpenLayers.Layer.Markers("Markers");
            this.map.addLayer(this.markers);
        }
        this.markers.addMarker(marker);
        marker.text = text;
        marker.location = location;
        marker.events.register('mousedown', marker, function(evt) { 
                theMap.showMarkerPopup(this);
                OpenLayers.Event.stop(evt); 
            });
        return marker;
    }


    this.addBox = function(id, north, west, south, east) {
        if(!this.boxes) {
            this.boxes = new OpenLayers.Layer.Boxes("Boxes");
            this.map.addLayer(this.boxes);
            //            var sf = new OpenLayers.Control.SelectFeature(this.boxes);
            //            this.map.addControl(sf);
            //            sf.activate();
        }
        var bounds = new OpenLayers.Bounds(west, south, east, north);
        box = new OpenLayers.Marker.Box(bounds);
        box.events.register("click", box, function (e) {
            });
        box.setBorder("blue");
        this.boxes.addMarker(box);
        box.id = id;
        return box;
    }


    this.addLine = function(id, lat1, lon1, lat2, lon2) {
        var layer_style = OpenLayers.Util.extend({}, OpenLayers.Feature.Vector.style['default']);
        var style_blue = OpenLayers.Util.extend({}, layer_style);
        style_blue.strokeColor = "blue";
        style_blue.strokeColor = "blue";
        style_blue.strokeWidth = 3;

        if(!this.lines) {
            layer_style.fillOpacity = 0.2;
            layer_style.graphicOpacity = 1;
            //            this.lines = new OpenLayers.Layer.Vector("Lines", {style: layer_style});
            this.lines = new OpenLayers.Layer.PointTrack("Lines", {style: layer_style});
            this.map.addLayer(this.lines);
        }
        var points = [];
        points.push(new OpenLayers.Geometry.Point(lon1,lat1));
        points.push(new OpenLayers.Geometry.Point(lon2,lat2));
        var lineString = new OpenLayers.Geometry.LineString(points);
        var line = new OpenLayers.Feature.Vector(lineString, null,
                                                        style_blue);
        this.lines.addFeatures([line]);
        //        pointList.push(new OpenLayers.LonLat(lon1,lat1));
        //        pointList.push(new OpenLayers.LonLat(lon2,lat2));
        line.id = id;
        return line;
    }

    this.showMarkerPopup =function(marker) {
        if(this.currentPopup) {
            this.map.removePopup(this.currentPopup);
            this.currentPopup.destroy();
        }
        this.hiliteBox(marker.id);
        theMap = this;
        popup = new OpenLayers.Popup.FramedCloud("popup", 
                                                 marker.location,
                                                 null,
                                                 marker.text,
                                                 null, true, function() {theMap.onPopupClose()});
        marker.popup = popup;
        popup.marker= marker;
        this.map.addPopup(popup);
        this.currentPopup = popup;

    } 

    this.removeMarker = function(marker) {
        this.markers.removeMarker(marker);
    }

}
