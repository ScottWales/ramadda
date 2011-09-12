/*
 * Copyright 2011 Jeff McWhirter, Don Murray & ramadda.org
 */

var mapLayers = null;

// google maps
var map_google_terrain = "google.terrain";
var map_google_streets = "google.streets";
var map_google_hybrid = "google.hybrid";
var map_google_satellite = "google.satellite";

// Microsoft maps - only work for -180 to 180
var map_ms_shaded = "ms.shaded";
var map_ms_hybrid = "ms.hybrid";
var map_ms_aerial = "ms.aerial";

// WMS maps
var map_wms_openlayers = "wms:OpenLayers WMS,http://vmap0.tiles.osgeo.org/wms/vmap0,basic";

// doesn't support EPSG:900913
var map_wms_topographic = "wms:Topo Maps,http://terraservice.net/ogcmap.ashx,DRG";

var defaultLocation = new OpenLayers.LonLat(-104, 40);
var defaultZoomLevel = 3;

var maxExtent = new OpenLayers.Bounds(-20037508, -20037508, 20037508, 20037508);

var earthCS = new OpenLayers.Projection("EPSG:4326");
var sphericalMercatorCS = new OpenLayers.Projection("EPSG:900913");

var maxLatValue = 89;

var initialExtent = new OpenLayers.Bounds(maxLatValue, -180, -maxLatValue, 180);

var positionMarkerID = "location";

function RepositoryMap(mapId, params) {
    var map, layer, markers, boxes, lines, selectorBox, selectorMarker;

    this.mapDivId = mapId;
    if (!this.mapDivId) {
        this.mapDivId = "map";
    }
    for ( var key in params) {
        this[key] = params[key];
    }

    if (!this.initialLocation) {
        this.initialLocation = defaultLocation;
    }
    if (!this.initialZoom) {
        this.initialZoom = defaultZoomLevel;
    }

    this.addWMSLayer = function(name, url, layer) {
        var layer = new OpenLayers.Layer.WMS(name, url, {
            layers : layer
        }, {
            wrapDateLine : true
        });
        layer.isBaseLayer = false;
        layer.visibility = false;
        layer.reproject = true;
        this.map.addLayer(layer);
    }

    this.addBaseLayers = function() {
        if (!this.mapLayers) {
            this.mapLayers = [ 
                map_google_terrain, 
                map_google_streets, 
                map_google_satellite,
                map_google_hybrid, 
                map_wms_openlayers,
                // these don't play well with google projection
                // map_wms_topographic,
                // map_ms_aerial,
                // map_ms_shaded,
                // map_ms_hybrid,
            ];
        }

        for (i = 0; i < this.mapLayers.length; i++) {
            mapLayer = this.mapLayers[i];
            if (mapLayer == map_google_terrain) {
                this.map.addLayer(new OpenLayers.Layer.Google("Google Terrain",
                        {
                            'type' : google.maps.MapTypeId.TERRAIN,
                            sphericalMercator : true,
                            wrapDateLine : false
                        }));
            } else if (mapLayer == map_google_streets) {
                this.map.addLayer(new OpenLayers.Layer.Google("Google Streets",
                        {
                            numZoomLevels : 20,
                            sphericalMercator : true,
                            wrapDateLine : false
                        }));
            } else if (mapLayer == map_google_hybrid) {
                this.map.addLayer(new OpenLayers.Layer.Google("Google Hybrid",
                        {
                            'type' : google.maps.MapTypeId.HYBRID,
                            // numZoomLevels: 20,
                            sphericalMercator : true,
                            wrapDateLine : false
                        }));
            } else if (mapLayer == map_google_satellite) {
                this.map.addLayer(new OpenLayers.Layer.Google(
                        "Google Satellite", {
                            'type' : google.maps.MapTypeId.SATELLITE,
                            // numZoomLevels: 22,
                            sphericalMercator : true,
                            wrapDateLine : false
                        }));
            } else if (mapLayer == map_ms_shaded) {
                this.map.addLayer(new OpenLayers.Layer.VirtualEarth(
                        "Virtual Earth - Shaded", {
                            'type' : VEMapStyle.Shaded,
                            sphericalMercator : true,
                            wrapDateLine : false
                        }));
            } else if (mapLayer == map_ms_hybrid) {
                this.map.addLayer(new OpenLayers.Layer.VirtualEarth(
                        "Virtual Earth - Hybrid", {
                            'type' : VEMapStyle.Hybrid,
                            sphericalMercator : true,
                            wrapDateLine : false
                        }));
            } else if (mapLayer == map_ms_aerial) {
                this.map.addLayer(new OpenLayers.Layer.VirtualEarth(
                        "Virtual Earth - Aerial", {
                            'type' : VEMapStyle.Aerial,
                            sphericalMercator : true,
                            wrapDateLine : false
                        }));
            } else {
                var match = /wms:(.*),(.*),(.*)/.exec(mapLayer);
                if (!match) {
                    alert("no match " + mapLayer);
                    continue;
                }
                this.addWMSLayer(match[1], match[2], match[3]);
            }
        }

        this.graticule = new OpenLayers.Control.Graticule( {
            layerName : "Lat/Lon Lines",
            numPoints : 2,
            labelled : true,
            visible : false
        });
        this.map.addControl(this.graticule);

        return;
    }

    this.getMap = function() {
        return this.map;
    }

    this.initMap = function(doRegion) {
        if (this.inited)
            return;
        this.inited = true;
        this.name = "map";
        var theMap = this;
        var mousecontrols = new OpenLayers.Control.Navigation();
        var options = {
            projection : sphericalMercatorCS,
            displayProjection : earthCS,
            units : "m",
            maxResolution : 156543.0339,
            maxExtent : maxExtent
        };

        this.map = new OpenLayers.Map(this.mapDivId, options);
        this.addBaseLayers();

        //this.vectors = new OpenLayers.Layer.Vector("Drawing");
        //this.map.addLayer(this.vectors);
        this.map.addControl(mousecontrols);
        this.map.addControl(new OpenLayers.Control.LayerSwitcher());
        var latLonReadout = util.getDomObject("ramadda-map-latlonreadout");
        if(latLonReadout) {
            this.map.addControl(new OpenLayers.Control.MousePosition( {
                        numDigits : 3,
                            element: latLonReadout.obj,
                            prefix: "Position: "
                    }));
        } else {
            this.map.addControl(new OpenLayers.Control.MousePosition( {
                        numDigits : 3,
                        prefix: "Position: "
                    }));
        }
        this.map.setCenter(this.transformLLPoint(this.initialLocation),
                this.initialZoom);

        if (this.initialBoxes) {
            this.initBoxes(this.initialBoxes);
            this.initialBoxes = null;
        }

        if (this.initialBounds) {
            var llPoint = this.initialBounds.getCenterLonLat();
            var projPoint = this.transformLLPoint(llPoint);
            this.map.setCenter(projPoint);
            this.map.zoomToExtent(this.transformLLBounds(this.initialBounds));
            // this.map.restrictedExtent = this.initialBounds;
            this.initialBounds = null;
        } /* else { this.map.zoomToMaxExtent(); }
             */

        if (this.markers) {
            this.map.addLayer(this.markers);
            var sf = new OpenLayers.Control.SelectFeature(this.markers);
            this.map.addControl(sf);
            sf.activate();
        }

        if (this.initialLines) {
            this.map.addLayer(this.initialLines);
            this.initialLines = null;
        }

        if (doRegion) {
            this.addRegionSelectorControl();
        }
    }

    this.initForDrawing = function() {
        var theMap = this;
        if (!theMap.drawingLayer) {
            theMap.drawingLayer = new OpenLayers.Layer.Vector("Drawing");
            theMap.map.addLayer(theMap.drawingLayer);
        }
        theMap.drawControl = new OpenLayers.Control.DrawFeature(
                theMap.drawingLayer, OpenLayers.Handler.Point);
        // theMap.drawControl.activate();
        theMap.map.addControl(theMap.drawControl);
    }

    this.drawingFeatureAdded = function(feature) {
        // alert(feature);
    }

    this.addClickHandler = function(lonfld, latfld, zoomfld) {
        if (this.clickHandler)
            return;
        if (!this.map)
            return;
        this.clickHandler = new OpenLayers.Control.Click();
        this.clickHandler.setLatLonZoomFld(lonfld, latfld, zoomfld);
        this.clickHandler.setTheMap(this);
        this.map.addControl(this.clickHandler);
        this.clickHandler.activate();
    }

    this.setSelection = function(argBase, doRegion, absolute) {
    	this.selectRegion = doRegion;
        this.argBase = argBase;
        if (!util) {
            return;
        }
        this.fldNorth = util.getDomObject(this.argBase + "_north");
        if (!this.fldNorth)
            this.fldNorth = util.getDomObject(this.argBase + ".north");
        this.fldSouth = util.getDomObject(this.argBase + "_south");
        if (!this.fldSouth)
            this.fldSouth = util.getDomObject(this.argBase + ".south");

        this.fldEast = util.getDomObject(this.argBase + "_east");
        if (!this.fldEast)
            this.fldEast = util.getDomObject(this.argBase + ".east");

        this.fldWest = util.getDomObject(this.argBase + "_west");
        if (!this.fldWest)
            this.fldWest = util.getDomObject(this.argBase + ".west");

        this.fldLat = util.getDomObject(this.argBase + "_latitude");
        if (!this.fldLat)
            this.fldLat = util.getDomObject(this.argBase + ".latitude");

        this.fldLon = util.getDomObject(this.argBase + "_longitude");
        if (!this.fldLon)
            this.fldLon = util.getDomObject(this.argBase + ".longitude");

        if (this.fldLon) {
            this.addClickHandler(this.fldLon.id, this.fldLat.id);
            this.setSelectionMarker(this.fldLon.obj.value, this.fldLat.obj.value);
        }
    }

    this.selectionPopupInit = function() {
        if (!this.inited) {
            this.initMap(this.selectRegion);
            if (this.argBase && !this.fldNorth) {
                this.setSelection(this.argBase);
            }

            if (this.fldNorth) {
                // alert("north = " + this.fldNorth.obj.value);
                this.setSelectionBox(this.fldNorth.obj.value,
                        this.fldWest.obj.value, this.fldSouth.obj.value,
                        this.fldEast.obj.value);
            }
            
            if (this.fldLon) {
                this.addClickHandler(this.fldLon.id, this.fldLat.id);
                this.setSelectionMarker(this.fldLon.obj.value, this.fldLat.obj.value);
            }
        }
    }

    // Assume that north, south, east, and west are in degrees or
    // some variant thereof
    this.setSelectionBox = function(north, west, south, east) {
        if (north == "" || west == "" || south == "" || east == "")
            return;
        if (!this.selectorBox) {
            var args = {
                "color" : "red",
                "selectable" : false
            };
            this.selectorBox = this.addBox("", north, west, south, east, args);
        } else {
            var bounds = new OpenLayers.Bounds(west, Math.max(south,
                    -maxLatValue), east, Math.min(north, maxLatValue));
            this.selectorBox.bounds = this.transformLLBounds(bounds);
            // this.selectorBox.bounds = bounds;
        }
        this.boxes.redraw();
    }

    this.setSelectionMarker = function(lon, lat) {
        if (!lon || !lat || lon == "" || lat == "")
            return;
        var lonlat = new OpenLayers.LonLat(lon,lat);
        if (!this.selectorMarker) {
            this.selectorMarker = this.addMarker(positionMarkerID, lonlat, "", "");
        } else {
            this.selectorMarker.lonlat = this.transformLLPoint(lonlat);
        }
        this.markers.redraw();
    }
    
    this.transformLLBounds = function(bounds) {
        if (!bounds)
            return;
        var llbounds = bounds.clone();
        return llbounds.transform(earthCS, sphericalMercatorCS);
    }

    this.transformLLPoint = function(point) {
        if (!point)
            return;
        var llpoint = point.clone();
        return llpoint.transform(earthCS, sphericalMercatorCS);
    }

    this.transformProjBounds = function(bounds) {
        if (!bounds)
            return;
        var projbounds = bounds.clone();
        return projbounds.transform(sphericalMercatorCS, earthCS);
    }

    this.transformProjPoint = function(point) {
        if (!point)
            return;
        var projpoint = point.clone();
        return projpoint.transform(sphericalMercatorCS, earthCS);
    }

    this.normalizeBounds = function(bounds) {
        if (!this.map) {
            return bounds;
        }
        var newBounds = bounds;
        var newLeft = bounds.left;
        var newRight = bounds.right;
        var extentBounds = this.map.restrictedExtent;
        if (!extentBounds) {
            extentBounds = wmsBounds;
        }
        /*
         * if (extentBounds.left < 0) { // map is -180 to 180 if (bounds.right >
         * 180) { //bounds is 0 to 360 newLeft = bounds.left-360; newRight =
         * bounds.right-360; } } else { // map is 0 to 360
         */
        if (extentBounds.left >= 0) { // map is 0 to 360+
            if (bounds.left < 0) { // left edge is -180 to 180
                newLeft = bounds.left + 360;
            }
            if (bounds.right < 0) { // right edge is -180 to 180
                newRight = bounds.right + 360;
            }
        }
        // just account for crossing the dateline
        if (newLeft > newRight) {
            newRight = bounds.right + 360;
        }
        newLeft = Math.max(newLeft, extentBounds.left);
        newRight = Math.min(newRight, extentBounds.right);
        newBounds = new OpenLayers.Bounds(newLeft, bounds.bottom, newRight,
                bounds.top);
        return newBounds;
    }

    this.findSelectionFields = function() {
        if (this.argBase && !(this.fldNorth || this.fldLon)) {
            this.setSelection(this.argBase);
        }
    }

    this.selectionClear = function() {
        this.findSelectionFields();
        if (this.fldNorth) {
            this.fldNorth.obj.value = "";
            this.fldSouth.obj.value = "";
            this.fldWest.obj.value = "";
            this.fldEast.obj.value = "";
        } else if (this.fldLat) {
            this.fldLon.obj.value = "";
            this.fldLat.obj.value = "";
        }
        if (this.selectorBox && this.boxes) {
            this.boxes.removeMarker(this.selectorBox);
            this.selectorBox = null;
        }
        if (this.selectorMarker && this.markers) {
        	this.markers.removeMarker(this.selectorMarker);
        	this.selectorMarker = null;
        }
    }

    this.addRegionSelectorControl = function() {
        var theMap = this;
        if (theMap.selectorControl)
            return;
        theMap.selectorControl = new OpenLayers.Control();
        OpenLayers.Util.extend(theMap.selectorControl, {
            draw : function() {
                // this Handler.Box will intercept the shift-mousedown
                // before Control.MouseDefault gets to see it
                this.box = new OpenLayers.Handler.Box(theMap.selectorControl, {
                    "done" : this.notice
                }, {
                    keyMask : OpenLayers.Handler.MOD_SHIFT
                });
                this.box.activate();
            },

            notice : function(bounds) {
                var ll = this.map.getLonLatFromPixel(new OpenLayers.Pixel(
                        bounds.left, bounds.bottom));
                var ur = this.map.getLonLatFromPixel(new OpenLayers.Pixel(
                        bounds.right, bounds.top));
                ll = theMap.transformProjPoint(ll);
                ur = theMap.transformProjPoint(ur);
                theMap.setSelectionBox(ur.lat, ll.lon, ll.lat, ur.lon);
                var bounds = new OpenLayers.Bounds(ll.lon, ll.lat, ur.lon,
                        ur.lat);
                theMap.findSelectionFields();
                if (theMap.fldNorth) {
                    // theMap.fldNorth.obj.value = ur.lat;
                    // theMap.fldSouth.obj.value = ll.lat;
                    // theMap.fldWest.obj.value = ll.lon;
                    // theMap.fldEast.obj.value = ur.lon;
                    theMap.fldNorth.obj.value = bounds.top;
                    theMap.fldSouth.obj.value = bounds.bottom;
                    theMap.fldWest.obj.value = bounds.left;
                    theMap.fldEast.obj.value = bounds.right;
                }
                // OpenLayers.Event.stop(evt);
            }
        });
        theMap.map.addControl(theMap.selectorControl);
    }

    this.onPopupClose = function(evt) {
        if (this.currentPopup) {
            this.map.removePopup(this.currentPopup);
            this.currentPopup.destroy();
            this.currentPopup = null;
            this.hiliteBox('');
        }
    }

    this.findObject = function(id, array) {
        for (i = 0; i < array.length; i++) {
            if (array[i].id == id) {
                return array[i];
            }
        }
        return null;
    }

    this.findMarker = function(id) {
        if (!this.markers) {
            return null;
        }
        return this.findObject(id, this.markers.markers);
    }

    this.findBox = function(id) {
        if (!this.boxes) {
            return null;
        }
        return this.findObject(id, this.boxes.markers);
    }

    this.hiliteBox = function(id) {
        if (this.currentBox) {
            this.currentBox.setBorder("blue");
        }
        this.currentBox = this.findBox(id);
        if (this.currentBox) {
            this.currentBox.setBorder("red");
        }
    }

    this.hiliteMarker = function(id) {
        marker = this.findMarker(id);
        if (!marker) {
            return;
        }
        this.map.setCenter(marker.lonlat);
        this.showMarkerPopup(marker);
    }

    // bounds are in lat/lon
    this.centerOnMarkers = function(bounds) {
        // bounds = this.boxes.getDataExtent();
        if (!bounds) {
            if (!this.markers) {
                return;
            }
            // markers are in projection coordinates
            var dataBounds = this.markers.getDataExtent();
            bounds = this.transformProjBounds(dataBounds);
        }
        // alert(bounds);
        if (!this.map) {
            this.initialBounds = bounds;
            return;
        }
        // alert("map centerOn:" + bounds);
        projBounds = this.transformLLBounds(bounds);
        this.map.setCenter(projBounds.getCenterLonLat());
        this.map.zoomToExtent(projBounds);
    }

    this.zoomToMarkers = function() {
        if (!this.markers)
            return;
        bounds = this.markers.getDataExtent();
        this.map.setCenter(bounds.getCenterLonLat());
        this.map.zoomToExtent(bounds);
    }

    this.setInitialCenterAndZoom = function(lon, lat, zoomLevel) {
        this.initialLocation = new OpenLayers.LonLat(lon, lat);
        this.initialZoom = zoomLevel;
    }

    this.addMarker = function(id, location, iconUrl, text) {
        if (!this.markers) {
            this.markers = new OpenLayers.Layer.Markers("Markers");
            // Added this because I was getting an unknown method error
            this.markers.getFeatureFromEvent = function(evt) {
                return null;
            };

            if (this.map) {
                this.map.addLayer(this.markers);
                var sf = new OpenLayers.Control.SelectFeature(this.markers);
                this.map.addControl(sf);
                sf.activate();
            }
        }
        if (!iconUrl) {
            iconUrl = 'http://www.openlayers.org/dev/img/marker.png';
        }
        var sz = new OpenLayers.Size(21, 25);
        var calculateOffset = function(size) {
            return new OpenLayers.Pixel(-(size.w / 2), -size.h);
        };
        var icon = new OpenLayers.Icon(iconUrl, sz, null, calculateOffset);
        projPoint = this.transformLLPoint(location);
        var marker = new OpenLayers.Marker(projPoint, icon);
        marker.id = id;
        marker.text = text;
        marker.location = location;
        var theMap = this;
        marker.events.register('click', marker, function(evt) {
            theMap.showMarkerPopup(marker);
            OpenLayers.Event.stop(evt);
        });
        this.markers.addMarker(marker);
        return marker;
    }

    this.initBoxes = function(theBoxes) {
        if (!this.map) {
            // alert('whoa, no map');
        }
        this.map.addLayer(theBoxes);
        // Added this because I was getting an unknown method error
        theBoxes.getFeatureFromEvent = function(evt) {
            return null;
        };
        var sf = new OpenLayers.Control.SelectFeature(theBoxes);
        this.map.addControl(sf);
        sf.activate();
    }

    this.addBox = function(id, north, west, south, east, params) {
        var args = {
            "color" : "blue",
            "selectable" : true,
            "zoomToExtent" : false
        };

        for (a in params) {
            args[a] = params[a];
        }

        if (!this.boxes) {
            this.boxes = new OpenLayers.Layer.Boxes("Boxes");
            if (!this.map) {
                this.initialBoxes = this.boxes;
            } else {
                this.initBoxes(this.boxes);
            }
        }

        var bounds = new OpenLayers.Bounds(west, Math.max(south, -maxLatValue),
                east, Math.min(north, maxLatValue));
        var projBounds = this.transformLLBounds(bounds);

        box = new OpenLayers.Marker.Box(projBounds);
        var theMap = this;

        if (args["selectable"]) {
            box.events.register("click", box, function(e) {
                theMap.showMarkerPopup(box);
                OpenLayers.Event.stop(evt);
            });
        }
        box.setBorder(args["color"]);
        box.id = id;
        this.boxes.addMarker(box);

        if (args["zoomToExtent"]) {
            this.centerOnMarkers(bounds);
        }
        return box;
    }

    this.addRectangle = function(id, north, west, south, east, attrs) {
        var points = [ new OpenLayers.Geometry.Point(west, north),
                new OpenLayers.Geometry.Point(west, south),
                new OpenLayers.Geometry.Point(east, south),
                new OpenLayers.Geometry.Point(east, north),
                new OpenLayers.Geometry.Point(west, north) ];

        for (i in points) {
            points[i].transform(earthCS, sphericalMercatorCS);
        }

        return this.addPolygon(id, points, attrs);
    }

    this.addLine = function(id, lat1, lon1, lat2, lon2, attrs) {
        var points = [ new OpenLayers.Geometry.Point(lon1, lat1),
                new OpenLayers.Geometry.Point(lon2, lat2) ];
        for (i in points) {
            points[i].transform(earthCS, sphericalMercatorCS);
        }
        return this.addPolygon(id, points, attrs);
    }

    this.addPolygon = function(id, points, attrs) {
        var base_style = OpenLayers.Util.extend( {},
                OpenLayers.Feature.Vector.style['default']);
        var style = OpenLayers.Util.extend( {}, base_style);
        style.strokeColor = "blue";
        style.strokeWidth = 3;
        if (attrs) {
            for (key in attrs) {
                style[key] = attrs[key];
            }
        }

        if (!this.lines) {
            // this.lines = new OpenLayers.Layer.Vector("Lines", {style:
            // base_style});
            this.lines = new OpenLayers.Layer.PointTrack("Lines", {
                style : base_style
            });
            if (this.map) {
                this.map.addLayer(this.lines);
            } else {
                this.initialLines = this.lines;
            }
            /*
             * var sf = new OpenLayers.Control.SelectFeature(this.lines,{
             * onSelect: function(o) { alert(o) } }); this.map.addControl(sf);
             * sf.activate();
             */
        }
        var lineString = new OpenLayers.Geometry.LineString(points);
        var line = new OpenLayers.Feature.Vector(lineString, null, style);
        var theMap = this;
        /*
         * line.events.register("click", line, function (e) { alert("box
         * click"); theMap.showMarkerPopup(box); OpenLayers.Event.stop(evt); });
         */

        this.lines.addFeatures( [ line ]);
        line.id = id;
        return line;
    }

    this.showMarkerPopup = function(marker) {
        if (this.currentPopup) {
            this.map.removePopup(this.currentPopup);
            this.currentPopup.destroy();
        }
        this.hiliteBox(marker.id);
        var theMap = this;
        var markertext = marker.text;
        // set marker text as the location
        if (!markertext || markertext == "") {
        	markerlocation = this.transformProjPoint(marker.lonlat);
        	markertext = "Lon: " + location.lat + "<br>" + "Lat: " + location.lon;
        }
        popup = new OpenLayers.Popup.FramedCloud("popup", marker.lonlat,
                null, markertext, null, true, function() {
                    theMap.onPopupClose()
                });
        marker.popup = popup;
        popup.marker = marker;
        this.map.addPopup(popup);
        this.currentPopup = popup;

    }

    this.removeMarker = function(marker) {
        if (this.markers) {
            this.markers.removeMarker(marker);
        }
    }

}

OpenLayers.Control.Click = OpenLayers.Class(OpenLayers.Control, {
    defaultHandlerOptions : {
        'single' : true,
        'double' : false,
        'pixelTolerance' : 0,
        'stopSingle' : false,
        'stopDouble' : false
    },

    initialize : function(options) {
        this.handlerOptions = OpenLayers.Util.extend( {},
                this.defaultHandlerOptions);
        OpenLayers.Control.prototype.initialize.apply(this, arguments);
        this.handler = new OpenLayers.Handler.Click(this, {
            'click' : this.trigger
        }, this.handlerOptions);
    },

    setLatLonZoomFld : function(lonFld, latFld, zoomFld) {
        this.lonFldId = lonFld;
        this.latFldId = latFld;
        this.zoomFldId = zoomFld;
    },

    setTheMap : function(map) {
        this.theMap = map;
    },

    trigger : function(e) {
        var xy = this.theMap.getMap().getLonLatFromViewPortPx(e.xy);
        var lonlat = this.theMap.transformProjPoint(xy)
        if (!this.lonFldId) {
            this.lonFldId = "lonfld";
            this.latFldId = "latfld";
            this.zoomFldId = "zoomfld";
        }
        lonFld = util.getDomObject(this.lonFldId);
        latFld = util.getDomObject(this.latFldId);
        zoomFld = util.getDomObject(this.zoomFldId);
        if (latFld && lonFld) {
            latFld.obj.value = lonlat.lat;
            lonFld.obj.value = lonlat.lon;
        }
        if (zoomFld) {
            zoomFld.obj.value = this.theMap.getMap().getZoom();
        }
        this.theMap.setSelectionMarker(lonlat.lon, lonlat.lat);
    }
});
