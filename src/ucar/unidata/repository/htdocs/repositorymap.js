/*
 * Copyright 2010 Jeff McWhirter & ramadda.org
 */


var mapLayers = null;
var map_google_terrain = "google.terrain";
var map_google_streets = "google.streets";
var map_google_hybrid = "google.hybrid";
var map_google_satellite = "google.satellite";
var map_ms_shaded = "ms.shaded";
var map_ms_hybrid = "ms.hybrid";
var map_ms_aerial = "ms.aerial";
var map_yahoo = "yahoo";
var map_wms_topographic = "wms:Topo Maps,http://terraservice.net/ogcmap.ashx,DRG";
var map_wms_openlayers = "wms:OpenLayers WMS,http://vmap0.tiles.osgeo.org/wms/vmap0,basic";

var defaultLocation = new OpenLayers.LonLat(-104, 40);
var defaultZoomLevel = 3;





function loadjscssfile(filename, filetype){
    if (filetype=="js") { 
        var fileref=document.createElement('script');
        fileref.setAttribute("type","text/javascript");
        fileref.setAttribute("src", filename);
    } else if (filetype=="css") {
        var fileref=document.createElement("link");
        fileref.setAttribute("rel", "stylesheet");
        fileref.setAttribute("type", "text/css");
        fileref.setAttribute("href", filename);
    }
    if (typeof fileref!="undefined") {
        alert("loading " +filename);
        document.getElementsByTagName("head")[0].appendChild(fileref);
    }
}


/*
var yahooJS= "http://api.maps.yahoo.com/ajaxymap?v=3.0&appid=euzuro-openlayers";
var msJS= "http://dev.virtualearth.net/mapcontrol/mapcontrol.ashx?v=6.1";
var googleJS= "http://maps.google.com/maps/api/js?v=3.2&sensor=false";

loadjscssfile(yahooJS, "js")
//loadjscssfile(msJS, "js")
//loadjscssfile(googleJS, "js")
*/


function RepositoryMap (mapId, params) {
    var map, layer, markers, boxes, lines;

    this.mapDivId = mapId;
    this.initialLocation = defaultLocation;
    if(!this.mapDivId) {
        this.mapDivId= "map";
    }
    for(var key  in params) {
        this[key] = params[key];
    }

    this.addWMSLayer  = function(name, url, layer) {
        var layer = new OpenLayers.Layer.WMS( name, url,
                                               {layers: layer} );
        this.map.addLayer(layer);
    }

    this.addBaseLayers = function() {
        if(!this.mapLayers) {
            this.mapLayers = [
                         map_wms_openlayers,
                         map_yahoo,
                         map_wms_topographic,
                         map_ms_shaded,
                         //map_ms_hybrid,
                         map_ms_aerial,
                         map_google_terrain,
                         //map_google_streets,
                         map_google_hybrid,
                         map_google_satellite
                         ];
        }

            
        for (i = 0; i < this.mapLayers.length; i++) {
            mapLayer = this.mapLayers[i];
            if(mapLayer == map_google_terrain) {
                this.map.addLayer(new OpenLayers.Layer.Google("Google Terrain",  {type: google.maps.MapTypeId.TERRAIN}));
            } else if(mapLayer == map_google_streets) {
                this.map.addLayer(OpenLayers.Layer.Google("Google Streets",  {numZoomLevels: 20}));
            } else if(mapLayer == map_google_hybrid) {
                this.map.addLayer(new OpenLayers.Layer.Google("Google Hybrid",{type: google.maps.MapTypeId.HYBRID, numZoomLevels: 20}));
            } else if(mapLayer == map_google_satellite) {
                this.map.addLayer(new OpenLayers.Layer.Google("Google Satellite",{type: google.maps.MapTypeId.SATELLITE, numZoomLevels: 22}));
            } else if(mapLayer == map_yahoo) {
                this.map.addLayer(new OpenLayers.Layer.Yahoo( "Yahoo"));
            } else if(mapLayer == map_ms_shaded) {
                this.map.addLayer(new OpenLayers.Layer.VirtualEarth("Virtual Earth - Shaded", {
                            type: VEMapStyle.Shaded
                        }));
            } else if(mapLayer == map_ms_hybrid) {
                this.map.addLayer(OpenLayers.Layer.VirtualEarth("Virtual Earth - Hybrid", {
                            type: VEMapStyle.Hybrid
                        }));
            } else if(mapLayer == map_ms_aerial) {
                this.map.addLayer(new OpenLayers.Layer.VirtualEarth("Virtual Earth - Aerial", {
                            type: VEMapStyle.Aerial
                        }));
            } else {
                var match = /wms:(.*),(.*),(.*)/.exec(mapLayer);
                if(!match) {
                    alert("no match " + mapLayer);
                    continue;
                }
                this.addWMSLayer(match[1], match[2], match[3]);
            }
        }
            
        this.graticule = new OpenLayers.Control.Graticule({
                layerName: "Grid",
                numPoints: 2, 
                labelled: true,
                visible: false

            });
        this.map.addControl(this.graticule);
    }

    this.getMap = function(){
        return this.map;
    }


    this.initMap = function(forSelection) {
        var theMap = this;
        this.name  = "map";
        this.inited = true;
        var mousecontrols = new OpenLayers.Control.Navigation();
        var optionsorig = {
            projection: new OpenLayers.Projection("EPSG:900913"),
            displayProjection: new OpenLayers.Projection("EPSG:4326"),
            units: "m",
            maxResolution: 156543.0339,
            maxExtent: new OpenLayers.Bounds(-20037508.34, -20037508.34,
                                             20037508.34, 20037508.34)
,            controls: [mousecontrols]
        };
        var options = {
            //            projection: new OpenLayers.Projection("EPSG:900913"),
            //            displayProjection: new OpenLayers.Projection("EPSG:4326")
        };
        //        this.map = new OpenLayers.Map( this.mapDivId, options );
        this.map = new OpenLayers.Map( this.mapDivId);
        //        this.map.minResolution = 0.0000001;
        //        this.map.minScale = 0.0000001;
        this.vectors = new OpenLayers.Layer.Vector("Drawing");
        this.map.addLayer(this.vectors);

        this.addBaseLayers();
        this.map.setCenter(this.initialLocation, defaultZoomLevel);
        this.map.addControl(mousecontrols);
        this.map.addControl( new OpenLayers.Control.LayerSwitcher() );
        this.map.addControl( new OpenLayers.Control.MousePosition() );

        /*
        var options = {featureAdded:     
                       function(feature) {
                           theMap.drawingFeatureAdded(feature);
                       }
        }; 

        controls = {
            point: new OpenLayers.Control.DrawFeature(this.vectors,
                                                      OpenLayers.Handler.Point, options)
                        line: new OpenLayers.Control.DrawFeature(this.vectors,
                                                     OpenLayers.Handler.Path),
            polygon: new OpenLayers.Control.DrawFeature(this.vectors,
                                                        OpenLayers.Handler.Polygon),
                                                        drag: new OpenLayers.Control.DragFeature(this.vectors)
            
        };
       for(var key in controls) {
            //            this.map.addControl(controls[key]);
        }
//        var control = controls["point"];
//        control.activate();
        */
        //        var draw = new OpenLayers.Control.DrawFeature(this.drawingLayer,
        //                                                      OpenLayers.Handler.Point);
       if(forSelection) {
            this.addRegionSelectorControl();
        }
    }

    this.initForDrawing = function() {
        var theMap = this;
        if(!theMap.drawingLayer) {
            theMap.drawingLayer = new OpenLayers.Layer.Vector("Drawing");
            theMap.map.addLayer(theMap.drawingLayer);
        }
        theMap.drawControl=    new OpenLayers.Control.DrawFeature(theMap.drawingLayer,
                                                                  OpenLayers.Handler.Point);
        //        theMap.drawControl.activate();
        theMap.map.addControl(theMap.drawControl);
    }

    this.drawingFeatureAdded = function(feature) {
        //        alert(feature);
    }


    this.addClickHandler = function(lonfld, latfld, zoomfld) {
        if(this.clickHandler) return;
        if(!this.map) return;
        this.clickHandler = new OpenLayers.Control.Click();
        this.clickHandler.setLatLonZoomFld(lonfld, latfld, zoomfld);
        this.clickHandler.setTheMap(this);
        this.map.addControl(this.clickHandler);
        this.clickHandler.activate();
    }


    this.setSelection = function(argBase, doRegion, absolute) { 
        this.argBase = argBase;
        if(!util) {return;}
        this.fldNorth= util.getDomObject(this.argBase+"_north");
        if(!this.fldNorth)  
            this.fldNorth= util.getDomObject(this.argBase+".north");
        this.fldSouth= util.getDomObject(this.argBase+"_south");
        if(!this.fldSouth)
            this.fldSouth= util.getDomObject(this.argBase+".south");

        this.fldEast= util.getDomObject(this.argBase+"_east");
        if(!this.fldEast)
            this.fldEast= util.getDomObject(this.argBase+".east");

        this.fldWest= util.getDomObject(this.argBase+"_west");
        if(!this.fldWest)
            this.fldWest= util.getDomObject(this.argBase+".west");

        this.fldLat= util.getDomObject(this.argBase+"_latitude");
        if(!this.fldLat) 
            this.fldLat= util.getDomObject(this.argBase+".latitude");

        this.fldLon= util.getDomObject(this.argBase+"_longitude");
        if(!this.fldLon)  
            this.fldLon= util.getDomObject(this.argBase+".longitude");

        if(this.fldLon) {
            this.addClickHandler(this.fldLon.id, this.fldLat.id);
        }
    }

    this.selectionPopupInit = function() {
        if(!this.inited) {
            this.initMap(true);
            if(this.argBase && !this.fldNorth) {
                this.setSelection(this.argBase);
            }

            if(this.fldNorth) {
                //                alert("north = " + this.fldNorth.obj.value);
                this.setSelectionBox(this.fldNorth.obj.value,
                                     this.fldWest.obj.value,
                                     this.fldSouth.obj.value,
                                     this.fldEast.obj.value);
            }
        }
    }

    this.setSelectionBox = function(north, west, south, east) {
        if(north == "" || west == "" || south == "" || east == "") return;
        if(!this.selectorBox) {
            this.selectorBox = this.addBox(north, west, south, east);
        } else {
            var bounds = new OpenLayers.Bounds(west, south, east, north);
            this.selectorBox.bounds = bounds;
        }
        this.boxes.redraw();
    }



    this.selectionClear = function() {
        if(this.fldNorth) {
            this.fldNorth.obj.value = "";
            this.fldSouth.obj.value = "";
            this.fldWest.obj.value = "";
            this.fldEast.obj.value = "";
        } else if(this.fldLat) {
            this.fldLon.obj.value = "";
            this.fldLat.obj.value = "";
        }
    }



    this.addRegionSelectorControl = function() {
        var theMap = this;
        if(theMap.selectorControl) return;
        theMap.selectorControl = new OpenLayers.Control();
        OpenLayers.Util.extend(theMap.selectorControl, {
                draw: function () {
                    // this Handler.Box will intercept the shift-mousedown
                    // before Control.MouseDefault gets to see it
                    this.box = new OpenLayers.Handler.Box( theMap.selectorControl,
                                                           {"done": this.notice},
                                                           {keyMask: OpenLayers.Handler.MOD_SHIFT});
                    this.box.activate();
                },

                notice: function (bounds) {
                    var ll = this.map.getLonLatFromPixel(new OpenLayers.Pixel(bounds.left, bounds.bottom)); 
                    var ur = this.map.getLonLatFromPixel(new OpenLayers.Pixel(bounds.right, bounds.top)); 
                    theMap.setSelectionBox(ur.lat, ll.lon, ll.lat,ur.lon);
                    if(theMap.argBase && util) {
                    }
                    if(theMap.fldNorth) {
                        theMap.fldNorth.obj.value = ur.lat;
                        theMap.fldSouth.obj.value = ll.lat;
                        theMap.fldWest.obj.value = ll.lon;
                        theMap.fldEast.obj.value = ur.lon;
                    }
                    //                    OpenLayers.Event.stop(evt); 
                }
            });

        theMap.map.addControl(theMap.selectorControl);
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
        if(!theMap.markers) {
            theMap.markers = new OpenLayers.Layer.Markers("Markers");
            //Added this because I was getting an unknown method error
            theMap.markers.getFeatureFromEvent = function(evt) {return null;};
            theMap.map.addLayer(theMap.markers);
            var sf = new OpenLayers.Control.SelectFeature(theMap.markers);
            theMap.map.addControl(sf);
            sf.activate();
        }
        if(!iconUrl) {
            iconUrl = 'http://www.openlayers.org/dev/img/marker.png';
        }
        var sz = new OpenLayers.Size(21, 25);
        var calculateOffset = function(size) {
            return new OpenLayers.Pixel(-(size.w/2), -size.h);
        };
        var icon = new OpenLayers.Icon(iconUrl, sz, null, calculateOffset);
        var marker = new OpenLayers.Marker(location, icon);
        marker.id = id;
        marker.text = text;
        marker.location = location;
        marker.events.register('click', marker, function(evt) { 
                theMap.showMarkerPopup(marker);
                OpenLayers.Event.stop(evt); 
            });
        theMap.markers.addMarker(marker);
        return marker;
    }


    this.addBox = function(id, north, west, south, east, attrs) {
        var theMap = this;
        if(!theMap.boxes) {
            theMap.boxes = new OpenLayers.Layer.Boxes("Boxes");
            theMap.map.addLayer(theMap.boxes);

            var t="";
            for(var key in theMap.boxes) {
                //                t = t +" " + theMap.boxes[key];
                t = t +" " + key;
            }
            //            alert(t);
            //Added this because I was getting an unknown method error
            theMap.boxes.getFeatureFromEvent = function(evt) {return null;};
            var sf = new OpenLayers.Control.SelectFeature(theMap.boxes);
            theMap.map.addControl(sf);
            sf.activate();
        }
        var bounds = new OpenLayers.Bounds(west, south, east, north);
        box = new OpenLayers.Marker.Box(bounds);
        box.events.register("click", box, function (e) {
                theMap.showMarkerPopup(box);
                OpenLayers.Event.stop(evt); 
                //                alert("box click");
            });
        box.setBorder("blue");
        box.id = id;
        theMap.boxes.addMarker(box);
        return box;
    }


    this.addRectangle = function(id, north, west, south, east, attrs) {
        var points = [
                      new OpenLayers.Geometry.Point(west,north),
                      new OpenLayers.Geometry.Point(west,south),
                      new OpenLayers.Geometry.Point(east,south),
                      new OpenLayers.Geometry.Point(east,north),
                      new OpenLayers.Geometry.Point(west,north)];
        return this.addPolygon(id, points, attrs);
    }

    this.addLine = function(id, lat1, lon1, lat2, lon2, attrs) {
        var points = [
                      new OpenLayers.Geometry.Point(lon1,lat1),
                      new OpenLayers.Geometry.Point(lon2,lat2)];
        return this.addPolygon(id,points, attrs);
    }

    this.addPolygon = function(id, points,attrs) {
        var theMap = this;
        var base_style = OpenLayers.Util.extend({}, OpenLayers.Feature.Vector.style['default']);
        var style = OpenLayers.Util.extend({}, base_style);
        style.strokeColor = "blue";
        style.strokeWidth = 3;
        if(attrs) {
            for(key in attrs) {
                style[key] = attrs[key];
            }
        }
        if(!this.lines) {
            this.lines = new OpenLayers.Layer.Vector("Lines", {style: base_style});
            //            this.lines = new OpenLayers.Layer.PointTrack("Lines", {style: base_style});
            this.map.addLayer(this.lines);
            /*
            var sf = new OpenLayers.Control.SelectFeature(theMap.lines,{
                    onSelect: function(o) {
                        alert(o)
                    }
                });
            theMap.map.addControl(sf);
            sf.activate();*/
        }
        var lineString = new OpenLayers.Geometry.LineString(points);
        var line = new OpenLayers.Feature.Vector(lineString, null,
                                                        style);
        /*        line.events.register("click", line, function (e) {
                alert("box click");
                theMap.showMarkerPopup(box);
                OpenLayers.Event.stop(evt); 
                });*/

        this.lines.addFeatures([line]);
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



OpenLayers.Control.Click = OpenLayers.Class(OpenLayers.Control, {                
        defaultHandlerOptions: {
            'single': true,
            'double': false,
            'pixelTolerance': 0,
            'stopSingle': false,
            'stopDouble': false
        },

        initialize: function(options) {
            this.handlerOptions = OpenLayers.Util.extend({}, this.defaultHandlerOptions
                                                         );
            OpenLayers.Control.prototype.initialize.apply(
                                                          this, arguments
                                                          ); 
            this.handler = new OpenLayers.Handler.Click(
                                                        this, {
                                                            'click': this.trigger
                                                        }, this.handlerOptions
                                                        );
        }, 

        setLatLonZoomFld: function(lonFld, latFld, zoomFld) {
            this.lonFldId = lonFld;
            this.latFldId = latFld;
            this.zoomFldId = zoomFld;
        },

        setTheMap: function(map) {
            this.theMap = map;
        },

        trigger: function(e) { 
            var lonlat = this.theMap.getMap().getLonLatFromViewPortPx(e.xy);
            if(!this.lonFldId) {
                this.lonFldId = "lonfld";
                this.latFldId = "latfld";
                this.zoomFldId = "zoomfld";  
            }
            lonFld  = util.getDomObject(this.lonFldId);
            latFld  = util.getDomObject(this.latFldId);
            zoomFld  = util.getDomObject(this.zoomFldId);
            if(latFld && lonFld) {
                latFld.obj.value = lonlat.lat;
                lonFld.obj.value = lonlat.lon;
            }
            if(zoomFld) {
                zoomFld.obj.value = this.theMap.getMap().getZoom();
            }
        }

    });


