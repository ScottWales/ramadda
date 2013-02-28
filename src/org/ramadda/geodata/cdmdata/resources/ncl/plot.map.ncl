;************************************************
; Program: plot.maps.ncl
; Purpose: plot NCL maps 
; Parameters: IMG_DIR - directory to store images (environ var)
;             LAST_DATE  environment variable of last date file
; Output:  plot or time series
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
;************************************************

;************************************************
; Program: sstLabelBar.ncl
; Purpose: consistent interface for plotting sst label bars
; Params:  lbres  - the resource
;***********************************************
undef("labelbar")
procedure labelbar(lbres:logical)

begin

  lbres@vpWidthF             = 0.45
  lbres@vpHeightF            = 0.02
  lbres@lbAutoManage         = False

  lbres@lbBottomMarginF      = 0.0
  lbres@lbLeftMarginF        = 0.0
  lbres@lbRightMarginF       = 0.0
  lbres@lbTopMarginF         = 0.0

  lbres@lbLabelsOn           = True
  lbres@lbPerimOn            = False
  lbres@lbMonoFillPattern    = True
  lbres@lbOrientation        = "Horizontal"
;
; This specifies the proportion of the space in the direction
; perpendicular to the labelbar orientation that is occupied by the
; colorbar. By default it is 0.33, or 1/3 of the width specified by
; vpWidthF in the case of a vertically oriented labelbar.
;
  lbres@lbBoxMinorExtentF    = 1.0

  lbres@lbJustification      = "TopLeft"
  lbres@lbLabelAlignment     = "InteriorEdges"
  lbres@lbLabelPosition      = "Bottom"
  lbres@lbLabelAutoStride    = True
  lbres@lbLabelOffsetF       = 0.25
  lbres@lbLabelFontHeightF   = 0.009

;--- title (deg C)
  lbres@lbTitleOn            = True
  lbres@lbTitleString = "~S~o~N~C"  ; ~S to superscript, ~N to normalize
  lbres@lbTitlePosition = "Right"
  lbres@lbTitleFontHeightF = 0.009
  lbres@lbTitleOffsetF = 0.01
  lbres@lbTitleJust = "BottomLeft"
  lbres@lbTitleDirection = "Across"

end

;************************************************
; Procedure: plotmap
; Purpose: plot map(s)
; Params:  data  - the data to plot
;          wks_name - workstation name
;          wks_type - workstation type (x11, png, etc)
;          opts - allow passing optional args
;***********************************************
undef("plotmap")
procedure plotmap(data:float, wks_name:string, wks_type:string, opts:logical)

begin

;  wks_type@wkBackgroundColor="white"
;  wks_type@wkForegroundColor="black"
;  wks_type@wkWidth                     = 800
;  wks_type@wkHeight                    = 800

  rescf = True  ; color-fill params
  rescn = True  ; contour params
;  rescf@vpXF = 0.1
;  rescf@vpYF = 0.945
;  rescf@vpWidthF = 0.8
;  rescf@vpHeightF = 0.8


;--- don't draw or advance frames automatically
;  rescf@gsnDraw  = False               
;  rescf@gsnFrame  = False               
;  rescn@gsnDraw  = False               
;  rescn@gsnFrame  = False               

   rescf@gsnPaperOrientation = "auto"
   rescf@gsnMaximize         = True        ; must include w/ Paper Orientation
   rescf@gsnSpreadColors = True

   rescf@cnFillOn= True
   rescf@cnFillDrawOrder       =  "PreDraw"
   rescf@mpFillOn = True
   rescf@mpCenterLonF = 0.
   rescf@mpGeophysicalLineThicknessF = 1.
   rescf@lbLabelBarOn = True         ; turn off label bar

  wks = gsn_open_wks(wks_type,wks_name) 
  gsn_define_colormap(wks, "rainbow")
  res=True
  plot = gsn_csm_contour_map(wks,data,rescf)
;  draw(plot)
;  delete(wks)
;  delete(plot)
  

end

;**************************************************
; Main routine
;**************************************************

begin

variable=(getenv("variable"))
ncfile=(getenv("ncfile"))
level=(getenv("level"))
wks_name=(getenv("wks_name"))
;ltm=(getenv("ltm"))
;varnameltm=(getenv("variableltm"))

datafile = addfile(ncfile ,"r")  ; open input data file
;dimsNames = getfilevardims(ncFile, $variable$)

if (ismissing(level)) then
   vardata = datafile->$variable$(0,:,:)
else
   vardata = datafile->$variable$(0,{stringtointeger(level)},:,:)
end if
printVarSummary(vardata)
plotmap(vardata, wks_name, "png",True)
status_exit(0)

end
