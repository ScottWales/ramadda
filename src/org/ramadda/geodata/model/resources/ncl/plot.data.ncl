;************************************************
; Program: plot.data.ncl
; Purpose: plot data using NCL 
; Output:  map plot, kml or time series
; Author:  Don Murray, CU-CIRES
;***********************************************

;--- load in the libraries
;************************************************
load "$NCARG_ROOT/lib/ncarg/nclscripts/csm/gsn_code.ncl"
load "$NCARG_ROOT/lib/ncarg/nclscripts/csm/gsn_csm.ncl"
load "$NCARG_ROOT/lib/ncarg/nclscripts/csm/contributed.ncl"
load "$NCARG_ROOT/lib/ncarg/nclscripts/csm/shea_util.ncl"
load "$NCARG_ROOT/lib/ncarg/nclscripts/contrib/ut_string.ncl"
load "$NCARG_ROOT/lib/ncarg/nclscripts/contrib/calendar_decode2.ncl"
load "$NCL_RESOURCES/kml.ncl"
;************************************************

;************************************************
; Procedure: plotmap
; Purpose: plot map(s)
; Params:  data  - the data to plot
;          wks_name - workstation name
;          wks_type - workstation type (x11, png, etc)
;          opts - allow passing optional args
;***********************************************
undef("plotmap")
procedure plotmap(data:float, wks_name:string, wks_type:string, rescf:logical, opts:logical)

begin

    doGoogle = (opts@plottype .eq. "kmz")
    if (doGoogle) then
        wks_type="ps"
    end if
    
    wks_type@wkBackgroundColor="white"
    wks_type@wkForegroundColor="black"
    
  
    ; map stuff
    rescf@mpFillOn = False
    rescf@mpGeophysicalLineThicknessF = 2.
  
    ; contour stuff
    rescf@gsnSpreadColors = True
    rescf@cnFillOn= True          ; color fill
;    rescf@cnFillMode= "CellFill"  ; Raster mode

    ; labelbar
    rescf@lbLabelBarOn = True         ; turn on label bar
    ratio = opts@ratio
    
    if (doGoogle) then
        kml_map_defaults(rescf) ; set following suggested default plotting resources
        rescf@cnLinesOn = False ; turn off contour lines
        rescf@lbLabelFontColor="White"
        
;        rescf@vpXF = .00
;        rescf@vpYF = 1.00
;        rescf@vpWidthF = 1
;        rescf@vpHeightF= 1
        
;        ; Turn off labels, etc        
;        rescf@pmTickMarkDisplayMode = "NoCreate"
;        rescf@pmLabelBarDisplayMode = "NoCreate"
;        rescf@pmLegendDisplayMode = "NoCreate"
;    
;        rescf@mpShapeMode = "FreeAspect"
;        ;rescf@mpOutlineOn = False
;        rescf@mpPerimOn = False
;        rescf@mpGridAndLimbOn = False
;        rescf@tmXBBorderOn = False
;        rescf@tmXTBorderOn = False
;        rescf@tmYRBorderOn = False
;        rescf@tmYLBorderOn = False
;        rescf@tmXBOn = False
;        rescf@tmXTOn = False
;        rescf@tmYROn = False
;        rescf@tmYLOn = False
;        rescf@tiMainString  = ""
;        rescf@txString=""
;        rescf@lbTitleOn = False
;        rescf@gsnRightString =""
;        rescf@gsnLeftString =""
    else
        wks_type@wkWidth                     = 800
        wks_type@wkHeight                    = 800
        rescf@vpXF = .075
        rescf@vpYF = .95
        rescf@vpWidthF = .85
        rescf@vpHeightF= .5*ratio
    end if
    
    rescf@tiMainFontHeightF=0.012
    rescf@tiMainFontThicknessF=.8
    rescf@cnLineLabelsOn=True
    rescf@cnLineLabelPlacementMode = "Computed"
    rescf@cnLineLabelAngleF=0
    rescf@cnConstFLabelFontHeightF=0.010
    rescf@tmXBLabelFontHeightF=0.011
    rescf@tmYLLabelFontHeightF=0.011
 
    wks = gsn_open_wks(wks_type,wks_name) 
    ;drawNDCGrid(wks)
    gsn_define_colormap(wks, "rainbow")
    plotdata = data(0,:,:)
    plot = gsn_csm_contour_map_ce(wks,plotdata,rescf)
    
    if (doGoogle) then

        kres = True
        
        kres@product_dir=opts@product_dir
  
        ; open a new KML document
        kml = kml_open_document(wks_name, wks_name, kres) ; open new kml document

        ; position resources for ScreenOverlay element
        kres@kmlOverlayXY = True
        kres@kmlOverlayXYx = 0
        kres@kmlOverlayXYy = 0
        kres@kmlOverlayXYxunits = "fraction" ;
        kres@kmlOverlayXYyunits = "fraction" ; pixels, or insetPixels
        kres@kmlScreenXY = True
        kres@kmlScreenXYx = 0
        kres@kmlScreenXYy = 0
        kres@kmlScreenXYxunits = "fraction"
        kres@kmlScreenXYyunits = "fraction"
       
        ; crops the labelbar from the plot and converts it to a png
        lbicon = kml_crop_labelbar(kml, "label bar", wks, plot, kres)
        
        ; add labelbar to KML document as a ScreenOverlay element
        kml = kml_add_screenoverlay ( kml, "Label Bar", lbicon, kres )
         
        ; add cropped labelbar filename to list of files to be compressed in KMZ archive
        kml_add_kmzfile(kml,lbicon)
         
        ; get the latlon coordinates describing the corners of the groundoverlay plot
        LatLonBox = kml_get_vp_latlonbox(plot)
      
        ; get the page coordinates of the actual map from the plot
        cropbox = kml_get_vp_cropbox(wks,plot)
        
        ; "delete(wks)" can not be called within a function, need to find a work around for this
        delete(wks)
        
        ; crops the groundoverlay map and converts it to png
        ; must "delete(wks)" before this command
        icon = kml_crop_plot(cropbox, kres)
      
        ; add cropped map to KML document as a GroundOverlay element
        kml = kml_add_groundoverlay(kml, "GroundOverlay", icon, LatLonBox, 0, kres)
        
        ; add cropped map filename to list of files to be compressed in KMZ archive
        kml_add_kmzfile(kml,icon)
        
        ; close kml document
        kml = kml_close_document(kml)

        ; write kml document to file 
        kml_write(kml)
  
        ; zip kml file and overlay images together in to "example05.kmz"
        kml_make_kmz(kml)
        
     else
        delete(plot)
        delete(wks)
     end if

end

undef("plotts")
procedure plotts(data:float, wks_name:string, wks_type:string, rescf:logical, opts:logical)

begin
  ;printVarSummary(data)
  ; weighted Area Mean (use the cosine of the latitudes for weight)
  lat1   = data&lat
  rad    = 4.0*atan(1.0)/180.0
  clat1   = cos(lat1*rad)
  data_FieldAvg_wgt = wgt_areaave(data, clat1, 1.0, 0)
  ;printVarSummary(data_FieldAvg_wgt)

  ;--- get the time dimension
  tdim = data&time
  ;printVarSummary(tdim)
  tnum = dimsizes(tdim);
  endindex = tnum(0)-1
  cals = cd_calendar(tdim,-5)
  yrs = cals(:,0)
  ;print(yrs)
  startYear = yrs(0)
  endYear = yrs(tnum-1)
  ;print(startYear)
  ;print(endYear)

  ;; Gaussian smoothing (21yr)
;  if (nyrs.ge.11) then
;    data_FieldAvg_wgt_21yr = Gauss_Filter_Pad (data_FieldAvg_wgt,21)
;  else
;    data_FieldAvg_wgt_21yr = new(11,float,data@_FillValue)
;  end if
   data_FieldAvg_wgt_21yr = runave(data_FieldAvg_wgt,1,0)

  ;; ***  Create plot ***
  ;; ***  create time series plot (overlay annual and 21yr Gauss smooth) ***
  wks_type = "png"
  wks_type@wkWidth = 800
  wks_type@wkHeight = 600
  wks = gsn_open_wks(wks_type,wks_name)

 ;; *** 3.1: Annual Values

  res                   = True
  res@gsnDraw           = False         ; Don't draw plots (for overlay)
  res@gsnFrame          = False         ; don't advance frame yet

  ;res@tiMainString        = dataSet+" "+climateIndex+" "+season
  res@txFontHeightF   = 0.017
  ;res@gsnLeftString     = "area-weighted average: lat ( "+minLat+".."+maxLat+" ), lon ( "+minLon+".."+maxLon+" )"
  res@tiYAxisFontHeightF = 0.017
  ;res@tiYAxisString   = climateIndex+" [unit: "+Unit(climateIndex)+"]"

  res@gsnMaximize   =True
  res@vpHeightF= 0.4                    ; change aspect ratio of plot
  res@vpWidthF = 0.8
  res@vpXF     = 0.1                   ; start plot at x ndc coord

  res@xyLineThicknesses = 0.5
  ;res@xyDashPattern = 1               ; dashed/solid(=0)
  res@trXMinF = startYear-1		; set minimum X-axis value
  res@trXMaxF = endYear+1		; set maximum X-axis value

  res@xyLineColors      = (/"black"/)

  plot1  = gsn_csm_xy(wks,yrs,data_FieldAvg_wgt,res) ; create plot

 ;; *** 3.2: Smoothed curves
  sres = True                                 ; set up a second resource list
  sres@gsnDraw = False                        ; do not draw the plot
  sres@gsnFrame = False                       ; do not advance the frame

  sres@xyLineThicknesses = 3.0
  sres@xyDashPattern = 0               ; dashed/solid(=0)

  sres@xyLineColors      = (/"black"/)

  plot2 = gsn_csm_xy(wks,yrs,data_FieldAvg_wgt_21yr,sres)

 ;; *** 3.3: Overlay
  overlay(plot1,plot2)
  draw(plot1)

  ;; add text
;  addCopyrightText(wks,outputType)

  frame(wks)
    
  delete(wks) ;; closing the file
  delete(plot1)
  delete(plot2)
;  addLogo(convertCmd,wks_name,logoFilePath)
end
;**************************************************
; Main routine
;**************************************************

begin

; --- Read in the environment variables
variable=(getenv("variable"))
ncfile=(getenv("ncfile"))
level=stringtointeger(getenv("level"))
wks_name=(getenv("wks_name"))
productDir=(getenv("productdir"))
if (ismissing(productDir)) then
   productDir=""
end if
;ltm=(getenv("ltm"))
;varnameltm=(getenv("variableltm"))
plotType=getenv("plot_type")
if (ismissing(plotType)) then
    plotType = "png"
end if

;
; Region parameters
minLat=stringtofloat(getenv("minLat"))
maxLat=stringtofloat(getenv("maxLat"))
minLon=stringtofloat(getenv("minLon"))
maxLon=stringtofloat(getenv("maxLon"))
addCyclic=(getenv("addCyclic"))
;print(minLon)
;print(maxLon)

;--  Open the data file
datafile = addfile(ncfile ,"r")  ; open input data file
;dimsNames = getfilevardims(ncFile, $variable$)
vartype = getfilevartypes(datafile,variable)
dNames= getfilevardims(datafile,variable)
numdims=dimsizes(dNames)
lonname=dNames(numdims-1)
lonvar = datafile->$lonname$
;normalizeLons(lonvar,minLon,maxLon)
numlons=dimsizes(lonvar)
minDLon=lonvar(0)
maxDLon=lonvar(numlons(0)-1)

; check for 0-360
if(minDLon.gt.180 .or. maxDLon.gt.180) then
   if (minLon.lt.0 .or. maxLon.lt.0) then
       minLon = minLon+360
       maxLon = maxLon+360
   end if
else if (minDLon.lt.0 .or. maxDLon.lt.0) then
   if (minLon.gt.180 .or. maxLon.gt.180) then
       minLon = minLon-180
       maxLon = maxLon-180
   end if
end if
end if

;print(minLon)
;print(maxLon)

latdiff=abs(maxLat-minLat)
londiff=abs(maxLon-minLon)

ratio=(2*latdiff)/londiff
;print(ratio)

if (ismissing(level)) then
;   vardata = datafile->$variable$(:,:,:)
   if (vartype .eq. "short") then
       vardata = short2flt(datafile->$variable$(:,{minLat:maxLat},{minLon:maxLon}))
   else
       vardata = datafile->$variable$(:,{minLat:maxLat},{minLon:maxLon})
   end if
else
;   vardata = datafile->$variable$(:,{level},:,:)
   if (vartype .eq. "short") then
       vardata = short2flt(datafile->$variable$(:,{level},{minLat:maxLat},{minLon:maxLon}))
   else
       vardata = datafile->$variable$(:,{level},{minLat:maxLat},{minLon:maxLon})
   end if
end if

;printVarSummary(vardata)

rescf = True;
;rescf@gsnAddCyclic          = (addCyclic .eq. "true")    ; turn off if subset
rescf@gsnAddCyclic          = False
rescf@mpMinLatF            = minLat       ; range to zoom in on
rescf@mpMaxLatF            = maxLat
rescf@mpMinLonF            = minLon
rescf@mpMaxLonF            = maxLon
rescf@mpCenterLonF         = (maxLon + minLon)/2
;print(rescf@mpCenterLonF)

options = True
options@plottype=plotType
options@ratio=ratio
options@product_dir=productDir

if (plotType.eq."timeseries") then
  print("plotting timeseries")
  plotts(vardata, wks_name, "png", rescf, options)
else
  print("plotting map")
  plotmap(vardata, wks_name, "png", rescf, options)
end if
status_exit(0)

end
