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
load "$NCL_RESOURCES/remove_middle_contours.ncl"
;************************************************

;*********************************************************
; Compute the maxmin of a field
; args:
;         data:  data to look at
;     equalize:  make max min reciprocal (-max = min)
; return:
;     array of (min,max)
;********************************************************
undef("maxmin")
function maxmin(data, equalize:logical)

local maxX,minX

begin

  minX = ceil(min(data))
  maxX = floor(max(data))
  if (equalize) then
    if (abs(minX) .gt. maxX) then
       maxX = abs(minX)
    else
       minX = -maxX
    end if
  end if
  return (/minX,maxX/)

end

;**********************************************************
; Read data from a file
;   ncfile - file to read
;   opts   - options
;*********************************************************
undef("readdata")
function readdata(ncfile:string, opts:logical)

local minLat,maxLat,minLon,maxLon,varname,varlevel,vardata

begin

; Region parameters
  minLat=opts@minLat
  maxLat=opts@maxLat
  minLon=opts@minLon
  maxLon=opts@maxLon
  noLatLon = ismissing(minLat) .or. \
             ismissing(maxLon) .or. \
             ismissing(minLat) .or. \
             ismissing(maxLat)
  
  varname=opts@variable
  varlevel=opts@level

;--  Open the data file
  datafile = addfile(ncfile ,"r")  ; open input data file
  vars = getfilevarnames(datafile)
  nvars = dimsizes(vars)
  do v = 0, nvars-1
     testvar = vars(v)
     if (testvar .eq. varname) then
        break
     end if
     if (dimsizes(getfilevardims(datafile, testvar)) .gt. 2) then
        varname = testvar
        break
     end if
  end do
  vartype = getfilevartypes(datafile,varname)
  dNames= getfilevardims(datafile,varname)
  numdims=dimsizes(dNames)

;-- get the lat/lon info for normalizing
  lonname=dNames(numdims-1)
  lonvar = datafile->$lonname$
  numlons=dimsizes(lonvar)
  minDLon=lonvar(0)
  maxDLon=lonvar(numlons(0)-1)
  if (minDLon .gt. maxDLon) then
      tmp = maxDLon
      maxDLat = minDLon
      minDLon = tmp
      delete(tmp)
  end if
  latname=dNames(numdims-2)
  latvar = datafile->$latname$
  numlats=dimsizes(latvar)
  minDLat=latvar(0)
  maxDLat=latvar(numlats(0)-1)
  if (minDLat .gt. maxDLat) then
      tmp = maxDLat
      maxDLat = minDLat
      minDLat = tmp
      delete(tmp)
  end if
  
; if not passed in, then set from data
  if (noLatLon) then
    minLon = minDLon
    maxLon = maxDLon
    minLat = minDLat
    maxLat = maxDLat
  end if
  
; check for 0-360
  if(minDLon.gt.180 .or. maxDLon.gt.180) then
    if (minLon.lt.0 .or. maxLon.lt.0) then
      minLon = minLon+360
      maxLon = maxLon+360
    end if
  else 
    if (minDLon.lt.0 .or. maxDLon.lt.0) then
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
  opts@ratio=ratio

  if (numdims .eq. 3) then  ; (time,lat,lon)
    if (vartype .eq. "short") then
      vardata = short2flt(datafile->$varname$(:,{minLat:maxLat},{minLon:maxLon}))
    else
      vardata = datafile->$varname$(:,{minLat:maxLat},{minLon:maxLon})
    end if
  else ; (time,level,lat,lon)
    if (vartype .eq. "short") then
      if (ismissing(varlevel)) then
        vardata = short2flt(datafile->$varname$(:,0,{minLat:maxLat},{minLon:maxLon}))
      else
        vardata = short2flt(datafile->$varname$(:,{varlevel},{minLat:maxLat},{minLon:maxLon}))
      end if
    else
      if (ismissing(varlevel)) then
        vardata = datafile->$varname$(:,0,{minLat:maxLat},{minLon:maxLon})
      else
        vardata = datafile->$varname$(:,{varlevel},{minLat:maxLat},{minLon:maxLon})
      end if
    end if
  end if
  
  ;printVarSummary(vardata)
  if (noLatLon) then
    opts@minLat = minLat
    opts@maxLat = maxLat
    opts@minLon = minLon
    opts@maxLon = maxLon
  end if

  return(vardata)

end

;****************************************************
; Set the contour min and max based on the data so
; it is equally spaced if a difference or anomaly
; params:
;    data   - the data
;    center - True you want the contours to be
;              equally centered about a central point
;    opts   - the contour resource
;****************************************************
undef("setMinMax")
procedure setMinMax(data, anom:logical, opts:logical)
begin

  mnmx = maxmin(data, anom)
  opts@cnLevelSelectionMode = "ManualLevels"
  opts@cnMinLevelValF = mnmx(0)
  opts@cnMaxLevelValF = mnmx(1)

end

;************************************************
; Procedure: remapData
; Purpose:   remap data from data2 into the grid of data1
;            if they are not on the same grid
; Params:    data1  - the first data (reference grid)
;            data2  - the second data
; return     data2 interpolated to data1 grid
;************************************************
undef("remapData")
function remapData(data1:float, data2:float)
local data, xi, yi, xo, yo

begin

; Get the dimensions

  xi = data2&lon
  yi = data2&lat
  xo = data1&lon
  yo = data1&lat
  data = linint2_Wrap(xi,yi,data2,False,xo,yo,0)

  return data

end

;*************************************************
; Function: subData
; Purpose:  subtract 2 datasets, normalizing grids
;           and making sure units are the same
; Params:   data1 - the first data (references grid and units)
;           data2 - the other data
; Return:   data1-data2
;*************************************************
undef("changeUnits")
function changeUnits(dataIn:float, unitsIn:string, unitsOut:string)
local dataOut

begin

  dataOut = dataIn
  if (unitsIn .ne. unitsOut) then

    ; Temperature
    if (unitsIn .eq. "K" .or. unitsIn .eq. "degK") then
      if (unitsOut .eq. "C" .or. \
          unitsOut .eq. "Cel" .or. \
          unitsOut .eq. "degC" .or. \
          unitsOut .eq. "Celsius") then
        dataOut = dataIn-273.15
        dataOut@units = "degC"
      end if
      if (unitsOut .eq. "F" .or. \
          unitsOut .eq. "degF" .or. \
          unitsOut .eq. "Fahrenheit") then
        dataOut = dataIn-273.15
        dataOut = (9*dataOut/5)+32
        dataOut@units = "F"
      end if
    end if

    if (unitsIn .eq. "C" .or. \
          unitsIn .eq. "Cel" .or. \
          unitsIn .eq. "degC" .or. \
          unitsIn .eq. "Celsius") then
      if (unitsOut .eq. "K" .or. unitsOut .eq. "degK") then
        dataOut = dataIn+273.15
        dataOut@units = "K"
      end if
      if (unitsOut .eq. "F" .or. \
          unitsOut .eq. "degF" .or. \
          unitsOut .eq. "Fahrenheit") then
        dataOut = dataIn-32
        dataOut = (5*dataOut)/9
        dataOut@units = "F"
      end if
    end if

    if (unitsIn .eq. "F" .or. \
          unitsIn .eq. "degF" .or. \
          unitsIn .eq. "Fahrenheit") then
      if (unitsOut .eq. "C" .or. \
          unitsOut .eq. "Cel" .or. \
          unitsOut .eq. "degC" .or. \
          unitsOut .eq. "Celsius") then
          dataOut = (dataIn-32)*5/9
          dataOut@units = "degC"
      end if
      if (unitsOut .eq. "K" .or. unitsOut .eq. "degK") then
        dataOut = (dataIn-32)*5/9 ; convert to degC
        dataOut = dataOut+273.15
        dataOut@units = "K"
      end if
    end if

    ; precipitation
    if (unitsIn .eq. "kg m-2 s-1" .or. \
        unitsIn .eq. "mm/s") then
      if (unitsOut .eq. "mm/s") then
        ; just a name change
        dataOut@units = unitsOut
      end if
      if (unitsOut .eq. "mm/day") then
        dataOut = dataIn*86400
        dataOut@units = unitsOut
      end if
    end if
    if (unitsIn .eq. "m s-1" .or. \
        unitsIn .eq. "m/s") then
      if (unitsOut .eq. "mm/s") then
        dataOut = dataIn*1000
        dataOut@units = unitsOut
      end if
      if (unitsOut .eq. "mm/day") then
        dataOut = dataIn*86400000 ; 86400*1000
        dataOut@units = unitsOut
      end if
    end if

    ; speed
    if (unitsIn .eq. "m s-1" .or. \
        unitsIn .eq. "m/s") then
      if (unitsOut .eq. "mi/h" .or. \
          unitsOut .eq. "mi h-1" .or. \
          unitsOut .eq. "mph") then
        dataOut = dataIn*2.23694
        dataOut@units = unitsOut
      end if
      if (unitsOut .eq. "kt" .or. \
          unitsOut .eq. "kts" .or. \
          unitsOut .eq. "knots") then
        dataOut = dataIn*1.94384
        dataOut@units = unitsOut
      end if
    end if
    if (unitsIn .eq. "mi/h" .or. \
        unitsIn .eq. "mi h-1") then
      if (unitsOut .eq. "m/s" .or. \
          unitsOut .eq. "m s-1") then
        dataOut = dataIn*0.44704
        dataOut@units = "m s-1"
      end if
      if (unitsOut .eq. "kt" .or. \
          unitsOut .eq. "kts" .or. \
          unitsOut .eq. "knots") then
        dataOut = dataIn*0.868976
        dataOut@units = unitsOut
      end if
    end if

  end if
  return dataOut

end

;*************************************************
; Function: subData
; Purpose:  subtract 2 datasets, normalizing grids
;           and making sure units are the same
; Params:   data1 - the first data (references grid and units)
;           data2 - the other data
; Return:   data1-data2
;*************************************************
undef("subData")
function subData(data1:float, data2:float)
local unit1, unit2, newData

begin

  remappedData = remapData(data1,data2)
  unit1 = data1@units
  unit2 = data2@units
  if (unit1 .ne. unit2) then
     remappedData = changeUnits(remappedData, unit2, unit1)
  end if
  return data1-remappedData
end

;*************************************************
; Procedure:  addLogo
; Purpose:    add a logo to an image
; Params:     imageFilePath - the path to the image
;             logoFilePath - the logo path
;             annotation   - the annotation
;*************************************************
undef ("addLogo")
procedure addLogo (imageFilePath:string, logoFilePath:string)
begin
        tmpFilePath = imageFilePath + ".tmp"    
        system(%convert% + " " + imageFilePath + " " + logoFilePath + " -geometry +20+555 -composite " + tmpFilePath)
        system("mv -f " + tmpFilePath + " " + imageFilePath)
end

;*************************************************
; Procedure:  addAnnotation
; Purpose:    add some text
; Params:     wksname - workstation name to add to
;*************************************************
undef ("addAnnotation")
procedure addAnnotation(wksname, text)
begin
    txres               = True
    txres@txFontHeightF = 0.02 
    gsn_text_ndc(wksname,text,0.5,0.025,txres)
end



;************************************************
; Procedure: plotmap
; Purpose: plot map(s)
; Params:  files  - the data files to plot
;          wks_name - workstation name
;          wks_type - workstation type (x11, png, etc)
;          opts - allow passing optional args
;***********************************************
undef("plotmap")
procedure plotmap(files[*]:string, wks_name:string, wks_type:string,  opts:logical)
local data, numfiles, plots, dodiff, dopanel, isanom

begin

  numfiles = dimsizes(files)
  data = readdata(files(0), opts) 
  plots = new(numfiles,graphic)
  dodiff = opts@outtype .eq. "diff" ; .and. numfiles .gt. 1
  isanom = opts@isanom
  dopanel = (.not. dodiff) .and. numfiles .gt. 1

  rescf = True;
  ;rescf@gsnAddCyclic          = (addCyclic .eq. "true")    ; turn off if subset
  rescf@gsnAddCyclic          = False
  rescf@mpMinLatF            = opts@minLat       ; range to zoom in on
  rescf@mpMaxLatF            = opts@maxLat
  rescf@mpMinLonF            = opts@minLon
  rescf@mpMaxLonF            = opts@maxLon
  centerLonF         = (opts@maxLon + opts@minLon)/2
  centerLatF         = (opts@maxLat + opts@minLat)/2
  rescf@mpCenterLonF         = centerLonF

  doGoogle = (opts@plottype .eq. "kmz")

  rescf@gsnDraw           = False         ; Don't draw plots (for overlay)
  rescf@gsnFrame          = False         ; don't advance frame yet
  
  wks_type@wkBackgroundColor="white"
  wks_type@wkForegroundColor="black"
  
  ; map stuff
  rescf@mpFillOn = False
  rescf@mpGeophysicalLineThicknessF = 2.

  ; contour stuff
  rescf@gsnSpreadColors = True
  rescf@cnFillOn= True          ; color fill
  rescf@cnFillMode= "AreaFill"  

  ; labelbar
  if (.not. dopanel) then
    rescf@lbLabelBarOn = True         ; turn on label bar
  end if

  rescf@tiMainFontHeightF=0.012
  rescf@tiMainFontThicknessF=.8
  rescf@tmXBLabelFontHeightF=0.011
  rescf@tmYLLabelFontHeightF=0.011

  
  if (doGoogle) then

    wks_type="ps"
    kml_map_defaults(rescf) ; turn off many options
    rescf@cnLinesOn = False ; turn off contour lines
    rescf@lbLabelFontColor="White"
      
  else

    rescf@gsnMaximize = True
    ratio = opts@ratio
    wks_type@wkWidth                     = 800
    wks_type@wkHeight                    = 600
;    rescf@vpXF = .075
;    rescf@vpYF = .95
;    rescf@vpWidthF = .85
;    rescf@vpHeightF= .5*ratio
    rescf@cnLineLabelsOn=True
    rescf@cnLineLabelPlacementMode = "Computed"
    rescf@cnLineLabelAngleF=0
    rescf@cnConstFLabelFontHeightF=0.010

  end if
    
  wks = gsn_open_wks(wks_type,wks_name) 
  ;drawNDCGrid(wks)
  gsn_define_colormap(wks, opts@colormap)

  ; TODO: Handle multiple times
  if (dodiff) then
    if (numfiles .gt. 1) then
      do i = 1,numfiles-1
        newdata = readdata(files(i),opts)
        data = subData(data,newdata)
        delete(newdata)
      end do
    end if
    mydata = data(0,:,:)
    setMinMax(mydata,True,rescf)
    plots(0) = gsn_csm_contour_map_ce(wks,mydata,rescf)
    res2 = remove_middle_contours(plots(0), rescf, 1)
    plots(0) = gsn_csm_contour_map_ce(wks,mydata,res2)
    addAnnotation(wks,opts@annotation)
  else
    mydata = data(0,:,:)
    setMinMax(mydata,isanom,rescf)
    plots(0) = gsn_csm_contour_map_ce(wks,mydata,rescf)
    if (isanom) then
      res2 = remove_middle_contours(plots(0), rescf, 1)
      plots(0) = gsn_csm_contour_map_ce(wks,mydata,res2)
    end if
    delete(mydata)
    do i = 1,numfiles-1
      nextdata = readdata(files(i),opts)
      mydata = nextdata(0,:,:)
      setMinMax(mydata,isanom,rescf)
      plots(i) = gsn_csm_contour_map_ce(wks,mydata,rescf)
      if (isanom) then
        res2 = remove_middle_contours(plots(i), rescf, 1)
        plots(i) = gsn_csm_contour_map_ce(wks,mydata,res2)
      end if
      if (.not. dopanel) then
        addAnnotation(wks,opts@annotation)
      end if
      delete(mydata)
      delete(nextdata)
    end do
  end if
  
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
      lbicon = kml_crop_labelbar(kml, "label bar", wks, plots(0), kres)
      
      ; add labelbar to KML document as a ScreenOverlay element
      kml = kml_add_screenoverlay ( kml, "Label Bar", lbicon, kres )
       
      ; add cropped labelbar filename to list of files to be compressed in KMZ archive
      kml_add_kmzfile(kml,lbicon)
       
      ; get the latlon coordinates describing the corners of the groundoverlay plot
      LatLonBox = kml_get_vp_latlonbox(plots(0))
    
      ; get the page coordinates of the actual map from the plot
      cropbox = kml_get_vp_cropbox(wks,plots(0))
      
      draw(plots(0))
      frame(wks)
      ; "delete(wks)" can not be called within a function, need to find a work around for this
      delete(wks)
      
      ; crops the groundoverlay map and converts it to png
      ; must "delete(wks)" before this command
      icon = kml_crop_plot(cropbox, kres)
    
      ; add cropped map to KML document as a GroundOverlay element
      kml = kml_add_groundoverlay(kml, "GroundOverlay", icon, LatLonBox, 0, kres)
      
      ; add cropped map filename to list of files to be compressed in KMZ archive
      kml_add_kmzfile(kml,icon)

      ;kml = kml_add_lookat( kml, centerLonF, centerLatF, 0.0, 4616152, kres)
      
      ; close kml document
      kml = kml_close_document(kml)

      ; write kml document to file 
      kml_write(kml)

      ; zip kml file and overlay images together in to "example05.kmz"
      kml_make_kmz(kml)
      
   else
      if (dopanel) then
        ;---Create resources for paneling

        resP                            = True
        resP@gsnMaximize                = True    
        resP@gsnPanelLabelBar           = True    
        resP@txFontHeightF              = .20
        resP@lbLabelFontHeightF         = 0.012
        resP@pmLabelBarWidthF           = 0.80
        resP@pmLabelBarHeightF          = 0.04

        ;---Scale plots based on one of the smaller plots, and not the first one.
        resP@gsnPanelScalePlotIndex     = 1

        ;---Add some extra space between plots since they are too close together
        resP@gsnPanelXWhiteSpacePercent = 2
        resP@gsnPanelYWhiteSpacePercent = 3

        ;---Leave some room for the leftmost and rightmost tickmark labels.
        resP@gsnPanelLeft               = 0.1
        resP@gsnPanelRight              = 0.9

        ;---Panel the plot.
        gsn_panel(wks,plots,(/1,2/),resP)
        addAnnotation(wks,opts@annotation)
      else 
        draw(plots(0))
        frame(wks)
      end if
      delete(wks)
      delete(plots)
   end if

end


;*************************************
; Make an area average of the data
; input:
;    data - map data 
; return:
;    area average of the data
;*************************************
undef("areaavg_data")
function areaavg_data(data:float)

local lat1,rad,clat1,data_FieldAvg_wgt

begin
  lat1=data&lat
  rad    = 4.0*atan(1.0)/180.0
  ; weighted Area Mean (use the cosine of the latitudes for weight)
  clat1   = cos(lat1*rad)
  data_FieldAvg_wgt = wgt_areaave_Wrap(data, clat1, 1.0, 0)
  return data_FieldAvg_wgt
end


;*********************************************************
; Plot a time series
;    files - files to use for time series
;    wks_name - name of the workstation
;    wkx_type - type of image output
;    opts     - holder for passed in options
;*********************************************************

undef("plotts")
procedure plotts(files[*]:string, wks_name:string, wks_type:string, opts:logical)

begin

  numfiles = dimsizes(files)
  data = areaavg_data(readdata(files(0), opts))
  ;printVarSummary(data)

  ;--- get the time dimension
  tdim = data&time
  ;printVarSummary(tdim)
  tnum = dimsizes(tdim);
  endindex = tnum(0)-1
  cals = ut_calendar(tdim,-5)
  yrs = cals(:,0)
  ;print(yrs)
  startYear = yrs(0)
  endYear = yrs(tnum-1)
  ;print(startYear)
  ;print(endYear)

  dodiff = opts@outtype .eq. "diff"
  if (dodiff) then
    plot_data = new((/1,tnum/),float)
    if (numfiles .gt. 1) then
      do i = 1,numfiles-1
        nextdata = readdata(files(i),opts)
        data2 = areaavg_data(nextdata)
        ;printVarSummary(data2)
        data = data-data2
        delete(data2)
        delete(nextdata)
      end do
      ;printVarSummary(data)
    end if
    plot_data(0,:) = data
  else
    plot_data = new((/numfiles,tnum/),float)
    plot_data(0,:) = data
    do i = 1,numfiles-1
      nextdata = readdata(files(i),opts)
      plot_data(i,:) = areaavg_data(nextdata)
    end do
  end if

;  ; Gaussian smoothing (21yr)
;  if (nyrs.ge.11) then
;    data_FieldAvg_wgt_21yr = Gauss_Filter_Pad (data_FieldAvg_wgt,21)
;  else
;    data_FieldAvg_wgt_21yr = new(11,float,data@_FillValue)
;  end if
;   data_FieldAvg_wgt_21yr = runave(data_FieldAvg_wgt,1,0)

  ;; ***  Create plot ***
  ;; ***  create time series plot (overlay annual and 21yr Gauss smooth) ***
  wks_type = "png"
  wks_type@wkWidth = 800
  wks_type@wkHeight = 600
  wks = gsn_open_wks(wks_type,wks_name)

  res                   = True
  res@gsnDraw           = False         ; Don't draw plots (for overlay)
  res@gsnFrame          = False         ; don't advance frame yet

  ;res@tiMainString        = dataSet+" "+climateIndex+" "+season
  res@txFontHeightF   = 0.017
  ;res@gsnLeftString     = "area-weighted average: lat ( "+minLat+".."+maxLat+" ), lon ( "+minLon+".."+maxLon+" )"
  ;res@tiYAxisFontHeightF = 0.017
  ;res@tiYAxisString   = climateIndex+" [unit: "+Unit(climateIndex)+"]"
  ;res@tiXAxisFontHeightF = 0.017
  res@tiXAxisString   = "Year"

  res@gsnMaximize   =True
  res@vpHeightF= 0.8                    ; change aspect ratio of plot
  res@vpWidthF = 0.9
  res@vpXF     = 0.1                   ; start plot at x ndc coord

  res@xyLineThicknesses = 0.5
  ;res@xyDashPattern = 1               ; dashed/solid(=0)
  res@trXMinF = startYear-1		; set minimum X-axis value
  res@trXMaxF = endYear+1		; set maximum X-axis value

  res@xyLineColors      = (/"black"/)

  plot1  = gsn_csm_xy(wks,yrs,plot_data,res) ; create plot

; ; *** 3.2: Smoothed curves
;  sres = True                                 ; set up a second resource list
;  sres@gsnDraw = False                        ; do not draw the plot
;  sres@gsnFrame = False                       ; do not advance the frame
;
;  sres@xyLineThicknesses = 3.0
;  sres@xyDashPattern = 0               ; dashed/solid(=0)
;
;  sres@xyLineColors      = (/"black"/)
;
;  plot2 = gsn_csm_xy(wks,yrs,data_FieldAvg_wgt_21yr,sres)
;
; ;; *** 3.3: Overlay
;  overlay(plot1,plot2)
  draw(plot1)

  ; add text
;  addAnnotation(wks,opts@annotation)

  frame(wks)
    
  delete(wks) ;; closing the file
  delete(plot1)
;  addLogo(wks_name,logoFilePath)

end

;*********************************************************
; Plot a PDF
;    files - files to use for time series
;    wks_name - name of the workstation
;    wkx_type - type of image output
;    opts     - holder for passed in options
;*********************************************************

undef("plotpdf")
procedure plotpdf(files[*]:string, wks_name:string, wks_type:string, opts:logical)

begin
  ;printVarSummary(data)
  ; weighted Area Mean (use the cosine of the latitudes for weight)
  myfile = files(0)
  data = readdata(myfile, opts)
  lat1   = data&lat
  rad    = 4.0*atan(1.0)/180.0
  clat1   = cos(lat1*rad)
  data_FieldAvg_wgt = wgt_areaave(data, clat1, 1.0, 0)
  ;printVarSummary(data_FieldAvg_wgt)
  std_FieldAvg = dim_standardize(data_FieldAvg_wgt, 0)

  pdfdata = pdfx(std_FieldAvg, 6, False)
  printVarSummary(pdfdata)
  mnmx = maxmin(pdfdata, True)

  ;; ***  Create plot ***
  wks_type = "png"
  wks_type@wkWidth = 800
  wks_type@wkHeight = 600
  wks = gsn_open_wks(wks_type,wks_name)


  res                   = True
  res@gsnDraw           = False         ; Don't draw plots (for overlay)
  res@gsnFrame          = False         ; don't advance frame yet

  res@tiMainString        = "PDF of " + opts@variable 
  res@txFontHeightF   = 0.017

  res@gsnMaximize   =True
  res@vpHeightF= 0.4                    ; change aspect ratio of plot
  res@vpWidthF = 0.8
  res@vpXF     = 0.1                   ; start plot at x ndc coord


  plot1  = gsn_csm_xy(wks,pdfdata@bin_center,pdfdata,res) ; create plot

  draw(plot1)

  ; add text
;  addCopyrightText(wks,outputType)

  frame(wks)
    
  delete(wks) ;; closing the file
  delete(plot1)
;  addLogo(wks_name,logoFilePath)

end


;**************************************************
; Main routine
;**************************************************

begin

; --- Read in the environment variables

; data variables
variable=(getenv("variable"))
if (ismissing(variable)) then 
   print("no variable")
   status_exit(-1)
end if
level=stringtointeger(getenv("level"))
tmpfiles=getenv("ncfiles")
if (ismissing(tmpfiles)) then 
   print("no files")
   status_exit(-2)
end if
ncfiles=str_split(tmpfiles,",")

; output variables
wks_name=(getenv("wks_name"))
if (ismissing(wks_name)) then
  wks_name="foo"
end if

productDir=(getenv("productdir"))
if (ismissing(productDir)) then
   productDir=""
end if

plotType=getenv("plot_type")
if (ismissing(plotType)) then
    plotType = "png"
end if

outType=getenv("output")
if (ismissing(outType)) then
    outType = "diff"
end if

colormap=getenv("colormap")
if (ismissing(colormap)) then
    colormap = "rainbow"
end if

isanom=getenv("anom") .eq. "true"

;
; Region parameters
minLat=stringtodouble(getenv("minLat"))
maxLat=stringtodouble(getenv("maxLat"))
minLon=stringtodouble(getenv("minLon"))
maxLon=stringtodouble(getenv("maxLon"))

options = True
options@plottype=plotType
options@outtype=outType
options@product_dir=productDir
options@variable=variable
options@level=level
options@minLat = minLat
options@maxLat = maxLat
options@minLon = minLon
options@maxLon = maxLon
options@colormap = colormap
options@isanom = isanom
options@annotation = getenv("annotation")


if (plotType.eq."timeseries") then
  ;print("plotting timeseries")
  plotts(ncfiles, wks_name, "png", options)
else if (plotType.eq."pdf") then
  ;print("plotting map")
  plotpdf(ncfiles, wks_name, "png", options)
else
  ;print("plotting map")
  plotmap(ncfiles, wks_name, "png", options)
end if
end if

status_exit(0)

end
