
var root = "${urlroot}";
var urlroot = "${urlroot}";
var icon_close = "${urlroot}/icons/close.gif";
var icon_rightarrow = "${urlroot}/icons/grayrightarrow.gif";
var icon_downdart ="${urlroot}/icons/downdart.gif";
var icon_rightdart ="${urlroot}/icons/rightdart.gif";
var icon_downdart ="${urlroot}/icons/application_side_contract.png";
var icon_rightdart ="${urlroot}/icons/application_side_expand.png";
var icon_progress = "${urlroot}/icons/progress.gif";
var icon_information = "${urlroot}/icons/information.png";
var icon_folderclosed = "${urlroot}/icons/folderclosed.png";
var icon_folderopen = "${urlroot}/icons/togglearrowdown.gif";
var icon_menuarrow = "${urlroot}/icons/downdart.gif";
var icon_blank = "${urlroot}/icons/blank.gif";


function Util () {
    this.loadXML = function (url, callback,arg) {
        var req = false;
        if(window.XMLHttpRequest) {
            try {
                req = new XMLHttpRequest();
            } catch(e) {
                req = false;
            }
        } else if(window.ActiveXObject)  {
            try {
                req = new ActiveXObject("Msxml2.XMLHTTP");
            } catch(e) {
                try {
                    req = new ActiveXObject("Microsoft.XMLHTTP");
                } catch(e) {
                    req = false;
                }
            }
        }
        if(req) {
            req.onreadystatechange = function () { 
                if (req.readyState == 4 && req.status == 200)   {
                    callback(req,arg); 
                }
            };
            req.open("GET", url, true);
            req.send("");
        }
    }


    this.loadUrl = function (url, callback,arg) {
        var req = false;
        if(window.XMLHttpRequest) {
            try {
                req = new XMLHttpRequest();
            } catch(e) {
                req = false;
            }
        } else if(window.ActiveXObject)  {
            try {
                req = new ActiveXObject("Msxml2.XMLHTTP");
            } catch(e) {
                try {
                    req = new ActiveXObject("Microsoft.XMLHTTP");
                } catch(e) {
                    req = false;
                }
            }
        }
        if(req) {
            req.onreadystatechange = function () { 
                if (req.readyState == 4 && req.status == 200)   {
                    callback(req,arg); 
                }
            };
            req.open("GET", url, true);
            req.send("");
        }
    }



    this.getUrlArg  = function( name, dflt ) {
        name = name.replace(/[\[]/,"\\\[").replace(/[\]]/,"\\\]");
        var regexS = "[\\?&]"+name+"=([^&#]*)";
        var regex = new RegExp( regexS );
        var results = regex.exec( window.location.href );
        if( results == null || results=="" )
            return dflt;
        else
            return results[1];
    }

    this.setCursor = function(c) {
        var cursor = document.cursor;
        if(!cursor && document.getElementById) {
            cursor =  document.getElementById('cursor');
        }
        if(!cursor) {
            document.body.style.cursor = c;
        }
    }


    this.getDomObject = function(name) {
        obj = new DomObject(name);
        if(obj.obj) return obj;
        return null;
    }




    this.getKeyChar = function(event) {
        event = ramaddaUtil.getEvent(event);
        if(event.keyCode) {
            return String.fromCharCode(event.keyCode);
        }
        if(event.which)  {
            return String.fromCharCode(event.which);
        }
        return '';
    }


    this.getEvent = function (event) {
        if(event) return event;
        return window.event;
    }


    this.getEventX =    function (event) {
        if (event.pageX) {
            return  event.pageX;
        }
        return  event.clientX + document.body.scrollLeft
        + document.documentElement.scrollLeft;
    }

    this.getEventY =function (event) {
        if (event.pageY) {
            return  event.pageY;
        }
        return  event.clientY + document.body.scrollTop
        + document.documentElement.scrollTop;

    }

    this.getTop = function (obj) {
        if(!obj) return 0;
        return obj.offsetTop+this.getTop(obj.offsetParent);
    }


    this.getBottom = function (obj) {
        if(!obj) return 0;
        return this.getTop(obj) + obj.offsetHeight;
    }


    this.setPosition = function(obj,x,y) {
        obj.style.top = y;
        obj.style.left = x;
    }

    this.getLeft =  function(obj) {
        if(!obj) return 0;
        return obj.offsetLeft+this.getLeft(obj.offsetParent);
    }
    this.getRight =  function(obj) {
        if(!obj) return 0;
        return obj.offsetRight+this.getRight(obj.offsetParent);
    }

    this.getStyle = function(obj) {
        if(obj.style) return obj.style;
        if (document.layers)  { 		
            return   document.layers[obj.name];
        }        
        return null;
    }

}

ramaddaUtil = new Util();


function HtmlUtil() {
     this.qt = function (value) {
        return "'" + value +"'";
     }
     this.attr = function(name, value) {
      return " " + name +"=" + this.qt(value) +" ";
    }
    this.input =   function(name, value, attrs) {
       if(!attrs) attrs = "";
       return "<input " + attrs + this.attr("name", name) + this.attr("value",value) +"/>";
    }

  }
  var htmlUtil = new HtmlUtil();






var blockCnt=0;
function DomObject(name) {
    this.obj = null;
    // DOM level 1 browsers: IE 5+, NN 6+
    if (document.getElementById)	{    	
        this.obj = document.getElementById(name);
        if(this.obj) 
            this.style = this.obj.style;
    }
    // IE 4
    else if (document.all)	{  			
        this.obj = document.all[name];
        if(this.obj) 
            this.style = this.obj.style;
    }
    // NN 4
    else if (document.layers)  { 		
        this.obj = document.layers[name];
        this.style = document.layers[name];
    }
   if(this.obj) {
      this.id = this.obj.id;
      if(!this.id) {
	this.id = "obj"+ (blockCnt++);
      }
   } 
   
}



function noop() {
}



var popupObject;
var popupSrcId;
var popupTime;

document.onmousemove = mouseMove;
document.onmousedown = mouseDown;
document.onmouseup   = mouseUp;


var mouseIsDown = 0;
var dragSource;
var draggedEntry;
var draggedEntryName;
var draggedEntryIcon;
var mouseMoveCnt =0;
var objectToHide;

function hidePopupObject() {
    if(objectToHide!=popupObject) {
        //	return;
    }
    if(popupObject) {
        hideObject(popupObject);
        popupObject = null;
        popupSrcId = null;
    }
}



function mouseDown(event) {
    if(popupObject) {
        if(checkToHidePopup()) {
            theObjectToHide = popupObject;
            thePopupSrcId  = popupSrcId;
            var callback = function() {
                var shouldClear = (popupObject == theObjectToHide);
                hideObject(theObjectToHide);
                if(shouldClear) {
                    popupSrcId = null;
                    popupObject = null;
                }
            }
            setTimeout(callback,250);
        }
    }
    event = ramaddaUtil.getEvent(event);
    mouseIsDown = 1;
    mouseMoveCnt =0;
    return true;
}



function mouseUp(event) {
    event = ramaddaUtil.getEvent(event);
    mouseIsDown = 0;
    draggedEntry   = null;
    ramaddaUtil.setCursor('default');
    var obj = ramaddaUtil.getDomObject('floatdiv');
    if(obj) {
        var dragSourceObj= ramaddaUtil.getDomObject(dragSource);
        if(dragSourceObj) {
            var tox = ramaddaUtil.getLeft(dragSourceObj.obj);
            var toy = ramaddaUtil.getTop(dragSourceObj.obj);
            var fromx = parseInt(obj.style.left);
            var fromy = parseInt(obj.style.top);
            var steps = 10;
            var dx=(tox-fromx)/steps;
            var dy=(toy-fromy)/steps;
            flyBackAndHide('floatdiv',0,steps,fromx,fromy,dx,dy);
        } else {
            hideObject(obj);
        }
    }
    return true;
}




function flyBackAndHide(id, step,steps,fromx,fromy,dx,dy) {
    var obj = ramaddaUtil.getDomObject(id);
    if(!obj) {
        return;
    }
    step=step+1;
    obj.style.left = fromx+dx*step+"px";
    obj.style.top = fromy+dy*step+"px";
    var opacity = 80*(steps-step)/steps;
    //    ramaddaUtil.print(opacity);
    //    obj.style.filter="alpha(opacity="+opacity+")";
    //    obj.style.opacity="0." + opacity;

    if(step<steps) {
        var callback = "flyBackAndHide('" + id +"'," + step+","+steps+","+fromx+","+fromy+","+dx+","+dy+");"
        setTimeout(callback,30);
    } else {
        setTimeout("finalHide('" + id+"')",150);
        //        hideObject(obj);
    }
}

function finalHide(id) {
    var obj = ramaddaUtil.getDomObject(id);
    if(!obj) {
        return;
    }
    hideObject(obj);
    obj.style.filter="alpha(opacity=80)";
    obj.style.opacity="0.8";
}

function mouseMove(event) {
    event = ramaddaUtil.getEvent(event);
    if(draggedEntry && mouseIsDown) {
        mouseMoveCnt++;
        var obj = ramaddaUtil.getDomObject('floatdiv');
        if(mouseMoveCnt==6) {
            ramaddaUtil.setCursor('move');
        }
        if(mouseMoveCnt>=6&& obj) {
            moveFloatDiv(ramaddaUtil.getEventX(event),ramaddaUtil.getEventY(event));
        }
    }    
    return false;
}






function moveFloatDiv(x,y) {
    var obj = ramaddaUtil.getDomObject('floatdiv');
    if(obj) {
        if(obj.style.visibility!="visible") {
            obj.style.visibility = "visible";
            obj.style.display = "block";
            var icon = "";
            if(draggedEntryIcon) {
                icon = "<img src=\"" +draggedEntryIcon+"\"/> ";
            }
            obj.obj.innerHTML = icon +draggedEntryName+"<br>Drag to a group to copy/move/associate";
        }
        obj.style.top = y;
        obj.style.left = x+10;
    }
}


function mouseOverOnEntry(event, entryId, targetId) {
    event = ramaddaUtil.getEvent(event);
    if(entryId == draggedEntry) return;
    if(mouseIsDown)  {
        var obj = ramaddaUtil.getDomObject(targetId);
        if(!obj)  return;
        //       if(obj.style && obj.style.borderBottom) {
        obj.style.borderBottom="2px black solid";
        //        }
    }
}

function mouseOutOnEntry(event, entryId,targetId) {
    event = ramaddaUtil.getEvent(event);
    if(entryId == draggedEntry) return;
    var obj = ramaddaUtil.getDomObject(targetId);
    if(!obj)  return;
    if(mouseIsDown)  {
        obj.style.borderBottom="";
    }
}




function mouseDownOnEntry(event, entryId, name, sourceIconId, icon) {
    event = ramaddaUtil.getEvent(event);
    dragSource  = sourceIconId;
    draggedEntry = entryId;
    draggedEntryName=name;
    draggedEntryIcon = icon;
    mouseIsDown = 1;
    if(event.preventDefault) {
        event.preventDefault();
    } else {
	event.returnValue = false;
        return false;
    }
}


function mouseUpOnEntry(event, entryId, targetId) {
    event = ramaddaUtil.getEvent(event);
    if(entryId == draggedEntry) {
        return;
    }
    var obj = ramaddaUtil.getDomObject(targetId);
    if(!obj)  {
        return;
    }
    if(mouseIsDown)  {
        obj.style.borderBottom="";
    }
    if(draggedEntry && draggedEntry!=entryId) {
        url = "${urlroot}/entry/copy?action=action.move&from=" + draggedEntry +"&to=" + entryId;
        //	alert(url);
	window.open(url,'move window','') ;
        //        document.location = url;
    }
}


function getTooltip() {
    return $("#tooltipdiv");
}

function handleKeyPress(event) {
    getTooltip().hide();
}


document.onkeypress = handleKeyPress;

var groups = new Array();
var groupList = new Array();




function EntryFormList(formId,img,selectId, initialOn) {

    this.entryRows = new Array();
    this.lastEntryRowClicked=null;
    groups[formId] = this;
    groupList[groupList.length] = this;
    this.formId = formId;
    this.toggleImg  = img;
    this.on = initialOn;
    this.entries = new Array();

    this.groupAddEntry = function(entryId) {
        this.entries[this.entries.length] = entryId;
    }

    this.addEntryRow = function(entryRow) {
        this.groupAddEntry(entryRow.cbxWrapperId);
        this.entryRows[this.entryRows.length] = entryRow;
        if(!this.on) {
            entryRow.getCbx().hide();
        } else {
            entryRow.getCbx().show();
	}
    }

    this.groupAddEntry(selectId);
    if(!this.on) {
        hideObject(selectId);
    }

    this.groupToggleVisibility = function  () {
        this.on = !this.on;
        this.setVisibility();
    }


    this.findEntryRow =function(rowId) {
        var i;
        for (i = 0; i < this.entryRows.length; i++) {
            if(this.entryRows[i].rowId == rowId) {
                return  this.entryRows[i];
            }
        }
        return null;
    }



    this.checkboxClicked = function(event,cbxId) {
        if(!event) return;
        var entryRow;
        for (i = 0; i < this.entryRows.length; i++) {
            if(this.entryRows[i].cbxId ==cbxId) {
                entryRow = this.entryRows[i];
                break;
            }
        }

        if(!entryRow) return;


        var value = entryRow.isSelected();
        if(event.ctrlKey) {
            for (i = 0; i < this.entryRows.length; i++) {
                this.entryRows[i].setCheckbox(value);
            }
        }

        if(event.shiftKey) {
            if(this.lastEntryRowClicked) {
                var pos1 = this.lastEntryRowClicked.getCbx().offset().top;
                var pos2 = entryRow.getCbx().offset().top;
                if(pos1>pos2) {
		    var tmp = pos1;
		    pos1 =pos2;
		    pos2=tmp;
                }
                for (i = 0; i < this.entryRows.length; i++) {
                    var top = this.entryRows[i].getCbx().offset().top;
                    if(top>=pos1 && top<=pos2) {
                        this.entryRows[i].setCheckbox(value);
                    }
                }
            }
            return;
        }
        this.lastEntryRowClicked = entryRow;
    }

    this.setVisibility = function  () {
        if(this.toggleImg) {
            if(this.on) {
                $("#" + this.toggleImg).attr('src',  icon_downdart);
            } else {
                $("#" + this.toggleImg).attr('src',  icon_rightdart);
            }
        }

        var form = ramaddaUtil.getDomObject(this.formId);
        if(form) {
            form = form.obj;
            for(i=0;i<form.elements.length;i++) { 
                if(this.on) {
                    showObject(form.elements[i],"inline");
                } else {
                    hideObject(form.elements[i]);
                }
            }
        }

        for(i=0;i<this.entries.length;i++) {
            obj = ramaddaUtil.getDomObject(this.entries[i]);
            if(!obj) continue;
            if(this.on) {
                showObject(obj,"block");
            } else {
                hideObject(obj);
            }
        }
    }
}


function entryRowCheckboxClicked(event,cbxId) {

    var cbx = ramaddaUtil.getDomObject(cbxId);
    if(!cbx) return;
    cbx = cbx.obj;
    if(!cbx.form) return;
    var visibilityGroup = groups[cbx.form.id];
    if(visibilityGroup) {
        visibilityGroup.checkboxClicked(event,cbxId);
    }
}

function initEntryListForm(formId) {
    var visibilityGroup = groups[formId];
    if(visibilityGroup) {
        visibilityGroup.on = 0;
        visibilityGroup.setVisbility();
    }
}


function EntryRow (entryId, rowId, cbxId,cbxWrapperId, showDetails) {
    this.entryId = entryId;

    this.onColor = "#FFFFCC";
    this.overColor = "#f6f6f6";
    this.overColor = "#edf5ff";
    this.overColor = "#ffffee";
    this.overColor = "#f4f4f4";
    this.rowId = rowId;
    this.cbxId = cbxId;
    this.cbxWrapperId = cbxWrapperId;
    this.showDetails = showDetails;
    this.getRow = function() {
        return $("#" + this.rowId);
    }

    this.getCbx = function() {
        return $("#" + this.cbxId);
    }

    var form = this.getCbx().closest('form');
    if(form.size()) {
        var visibilityGroup = groups[form.attr('id')];
        if(visibilityGroup) {
            visibilityGroup.addEntryRow(this);
        }
    } else {
        this.getCbx().hide();
    }


    this.setCheckbox = function(value) {
        this.getCbx().attr('checked', value);
        this.setRowColor();
    }

    
    this.isSelected = function() {
        return this.getCbx().attr('checked');
    }

    this.setRowColor = function() {
        if(this.isSelected()) {
            this.getRow().css("background-color", this.onColor);
        } else {
            this.getRow().css("background-color", "#ffffff");
        }
    }


    this.mouseOver = function(event) {
        $("#" + "entrymenuarrow_" +rowId).attr('src',icon_menuarrow);
        this.getRow().css('background-color',  this.overColor);
    }

    this.mouseOut = function(event) {
        $("#entrymenuarrow_" +rowId).attr('src',icon_blank);
        this.setRowColor();
    }


    this.mouseClick = function(event) {
        eventX = ramaddaUtil.getEventX(event);
        var position = this.getRow().offset();
        //Don't pick up clicks on the left side
        if(eventX-position.left<150) return;
        this.lastClick = eventX;
        var url = "${urlroot}/entry/show?entryid=" + entryId +"&output=metadataxml";
        if(this.showDetails) {
            url+="&details=true";
        } else {
            url+="&details=false";
        }
	ramaddaUtil.loadXML( url, this.handleTooltip,this);
    }

    this.handleTooltip = function(request,entryRow) {
        var xmlDoc=request.responseXML.documentElement;
        text = getChildText(xmlDoc);
        var leftSide  = entryRow.getRow().offset().left;
        var offset = entryRow.lastClick-leftSide;
        getTooltip().html("<div class=tooltip-inner><div id=\"tooltipwrapper\" ><table><tr valign=top><img width=\"16\" onmousedown=\"hideEntryPopup();\" id=\"tooltipclose\"  src=" + icon_close +"></td><td>" + text+"</table></div></div>");
        checkTabs(text);

        var pos = entryRow.getRow().offset();    
        var eWidth = entryRow.getRow().outerWidth();
        var eHeight = entryRow.getRow().outerHeight();
        var mWidth = getTooltip().outerWidth();
        var left =  entryRow.lastClick + "px";
        var top = (3+pos.top+eHeight) + "px";
        //show the menu directly over the placeholder  
        getTooltip().css( { 
                position: 'absolute',
                    zIndex: 5000,
                    left: left, 
                    top: top
                    } );
        getTooltip().show();
    }



}


function checkTabs(html) {
    while(1) {
        var re = new RegExp("id=\"(tabId[^\"]+)\"");
        var m = re.exec(html);
        if(!m) {
            break;
        }
        var s =   m[1];
        if(s.indexOf("-")<0) {
            jQuery(function(){
                    jQuery('#'+ s).tabs();
                });
        }
        var idx = html.indexOf("id=\"tabId");
        if(idx<0) {
            break;
        }
        html = html.substring(idx+20);
    }
}


function hideEntryPopup() {
    getTooltip().hide();
}

function findEntryRow(rowId) {
    var idx;
    for(idx=0;idx<groupList.length;idx++) {
        var entryRow = groupList[idx].findEntryRow(rowId);
        if(entryRow) return entryRow;
    }
    return null;
}


function entryRowOver(rowId) {
    var entryRow = findEntryRow(rowId);
    if(entryRow) entryRow.mouseOver();
}


function entryRowOut(rowId) {
    var entryRow = findEntryRow(rowId);
    if(entryRow) entryRow.mouseOut();
}

function entryRowClick(event,rowId) {
    var entryRow = findEntryRow(rowId);
    if(entryRow) entryRow.mouseClick(event);
}





function indexOf(array,object) {
    for (i = 0; i <= array.length; i++) {
        if(array[i] == object) return i;
    }
    return -1;
}


var lastCbxClicked;

function checkboxClicked(event, cbxPrefix, id) {
    if(!event) return;
    var cbx = ramaddaUtil.getDomObject(id);
    if(!cbx) return;
    cbx = cbx.obj;

    var checkBoxes = new Array();
    if(!cbx.form) return;
    var elements = cbx.form.elements;
    for(i=0;i<elements.length;i++) {
        if(elements[i].name.indexOf(cbxPrefix)>=0 || elements[i].id.indexOf(cbxPrefix)>=0) {
            checkBoxes[checkBoxes.length] = elements[i];
        }
    }


    var value = cbx.checked;
    if(event.ctrlKey) {
        for (i = 0; i < checkBoxes.length; i++) {
	    checkBoxes[i].checked = value;
        }
    }


    if(event.shiftKey) {
        if(lastCbxClicked) {
	    var pos1 = ramaddaUtil.getTop(cbx);
	    var pos2 = ramaddaUtil.getTop(lastCbxClicked);
	    if(pos1>pos2) {
		var tmp = pos1;
		pos1 =pos2;
		pos2=tmp;
	    }
	    for (i = 0; i < checkBoxes.length; i++) {
		var top = ramaddaUtil.getTop(checkBoxes[i]);
		if(top>=pos1 && top<=pos2) {
	                checkBoxes[i].checked = value;
		}
            }
        }
        return;
    }
    lastCbxClicked = cbx;
}






function toggleBlockVisibility(id, imgid, showimg, hideimg) {
    var img = ramaddaUtil.getDomObject(imgid);
    if(toggleVisibility(id,'block')) {
        $("#"+imgid).attr('src', showimg);
    } else {
        $("#"+imgid).attr('src', hideimg);
    }
    ramaddaUpdateMaps();
}


function toggleInlineVisibility(id, imgid, showimg, hideimg) {
    var img = ramaddaUtil.getDomObject(imgid);
    if(toggleVisibility(id,'inline')) {
        if(img) img.obj.src = showimg;
    } else {
        if(img) img.obj.src = hideimg;
    }
    ramaddaUpdateMaps();
}







var originalImages = new Array();
var changeImages = new Array();

function folderClick(uid, url, changeImg) {
    changeImages[uid] = changeImg;
    var jqBlock = $("#"+uid);
    if(jqBlock.size() ==0) {
	return;
    }
    var jqImage = $("#img_" +uid);
    var showing = jqBlock.css('display') != "none";
    if(!showing) {
	originalImages[uid] = jqImage.attr('src');
        jqBlock.show();
        jqImage.attr('src', icon_progress);
	ramaddaUtil.loadXML( url, handleFolderList,uid);
    } else {
	if(changeImg) {
            if(originalImages[uid]) {
                jqImage.attr('src', originalImages[uid]);
            } else 
                jqImage.attr('src', icon_folderclosed);
        }
        jqBlock.hide();
    }
}



function  handleFolderList(request, uid) {
    if(request.responseXML!=null) {
        var xmlDoc=request.responseXML.documentElement;
	var script;
	var html;
	for(i=0;i<xmlDoc.childNodes.length;i++) {
            var childNode = xmlDoc.childNodes[i];
            if(childNode.tagName=="javascript") {
                script =getChildText(childNode);
            } else if(childNode.tagName=="content") {
                html = getChildText(childNode);
            }  else {
            }
	}
        if(!html) {
            html = getChildText(xmlDoc);
        }
	if(html) {
            $("#" + uid).html("<div>"+html+"</div>");
            checkTabs(html);
	}
	if(script) {
            eval(script);
	}
    }
    
    if(changeImages[uid]) {
        $("#img_" +uid).attr('src', icon_folderopen);
    } else {
        $("#img_" +uid).attr('src', originalImages[uid]);
    }
}


var selectors = new Array();

function Selector(event, selectorId, elementId, allEntries, selecttype, localeId) {
    this.id  = selectorId;
    this.elementId  = elementId;
    this.localeId = localeId;
    this.allEntries = allEntries;
    this.selecttype = selecttype;
    this.textComp = ramaddaUtil.getDomObject(this.elementId);

    this.getTextComponent = function() {
        var id = "#" + this.elementId;
        alert('get text comp:' + id + ":" + $(id).size());
        return $(id);
    }

    this.getHiddenComponent = function() {
        var id = "#" + this.elementId+"_hidden";
        return $(id);
    }

    this.clearInput = function() {	
        this.getHiddenComponent().val("");
        this.getTextComponent().val("");
    }


    this.handleClick = function(event) {
        event = ramaddaUtil.getEvent(event);
        x = ramaddaUtil.getEventX(event);
        y = ramaddaUtil.getEventY(event);

        var link = ramaddaUtil.getDomObject(this.id+'.selectlink');
        if(!link) {
            return false;
        }
        this.div = ramaddaUtil.getDomObject('selectdiv');
        if(!this.div) {
            return false;
        }

        if(link && link.obj.offsetLeft && link.obj.offsetWidth) {
            x= ramaddaUtil.getLeft(link.obj);
            y = link.obj.offsetHeight+ramaddaUtil.getTop(link.obj) + 2;
        } else {
            x+=20;
        }
        
        hidePopupObject();
        ramaddaUtil.setPosition(this.div, x+10,y);
        //        popupObject = this.div;
        //        popupSrcId = "";
        showObject(this.div);
        url = "${urlroot}/entry/show?output=selectxml&selecttype=" + this.selecttype+"&allentries=" + this.allEntries+"&target=" + this.id+"&noredirect=true&firstclick=true";
        if(localeId) {
            url = url+"&localeid=" + localeId;
        }
        ramaddaUtil.loadXML( url, handleSelect,this.id);
        return false;
    }
    this.handleClick(event);
}





function selectClick(id,entryId,value) {
    selector = selectors[id];
    if (selector.selecttype=="wikilink") {
        insertAtCursor(selector.textComp.obj,"[[" +entryId+"|"+value+"]]");
    } else if (selector.selecttype=="entryid") {
        insertTagsInner(selector.elementId, selector.textComp.obj, "entry=\"" +entryId+"|"+value+"\" "," ","importtype");
    } else { 
        alert('comp:' + selector.getHiddenComponent().size() + " " +
              selector.getTextComponent().size());
        selector.getHiddenComponent().val(entryId);
        selector.getTextComponent().val(value);
    }
    selectCancel();
}

function selectCancel() {
    $("#selectdiv").hide();
}


function selectCreate(event, selectorId,elementId, allEntries,selecttype, localeId) {
    if(!selectors[selectorId]) {
        selectors[selectorId] = new Selector(event,selectorId, elementId,allEntries,selecttype,localeId);
    } else {
        //Don:  alert('have selector'):
        selectors[selectorId].handleClick(event);
    }
}


function selectInitialClick(event, selectorId, elementId, allEntries, selecttype, localeId) {
    selectCreate(event, selectorId, elementId, allEntries, selecttype, localeId);
    return false;
}


function clearSelect(id) {
    selector = selectors[id];
    if(selector) {
        selector.clearInput();
    } else {
        //In case the user never clicked select
        var textComp = ramaddaUtil.getDomObject(id);
        var hiddenComp = ramaddaUtil.getDomObject(id+"_hidden");
	if(hiddenComp) {
            hiddenComp.obj.value =""
        }
	if(textComp) {
            textComp.obj.value =""
        }
    }
}


function handleSelect(request, id) {
    selector = selectors[id];
    var xmlDoc=request.responseXML.documentElement;
    text = getChildText(xmlDoc);
    var close = "<a href=\"javascript:selectCancel();\"><img border=0 src=" + icon_close + "></a>";
    selector.div.obj.innerHTML = "<table width=100%><tr><td align=right>" + close +"</table>" +text;
}




function  getChildText(node) {
    var text = '';
    for(childIdx=0;childIdx<node.childNodes.length;childIdx++) {
        text = text  + node.childNodes[childIdx].nodeValue;
    }
    return text;
	
}


function toggleVisibility(id,style) {
    var display  = $("#" + id).css('display');
    $("#" + id).toggle();
    return display != 'block';

}


function hide(id) {
    $("#" + id).hide();
    //    hideElementById(id);
}

function hideElementById(id) {
    hideObject(ramaddaUtil.getDomObject(id));
}

function checkToHidePopup() {
    if(popupTime) {
        var now = new Date();
        timeDiff = now-popupTime;
        if(timeDiff>1000)  {
            return 1;
        }
    }
}

function showPopup(event, srcId, popupId, alignLeft) {
    if(popupSrcId == srcId) {
        if(checkToHidePopup()) {
            hidePopupObject();
            return;
        }
    }

    popupTime = new Date();
    hidePopupObject();
    var popup = ramaddaUtil.getDomObject(popupId);
    var srcObj = ramaddaUtil.getDomObject(srcId);
    if(!popup || !srcObj) return;
    popupObject = popup;
    popupSrcId = srcId;

    showObject(popup);
    jQuery("#"+popupId ).position({
                of: jQuery( "#" + srcId ),
                my: 'left top',
                at: 'left bottom',
                collision: "none none"
                });
    //Do it again to fix a bug on safari
    jQuery("#"+popupId ).position({
                of: jQuery( "#" + srcId ),
                my: 'left top',
                at: 'left bottom',
                collision: "none none"
                });
}


function showStickyPopup(event, srcId, popupId, alignLeft) {
    var popup = ramaddaUtil.getDomObject(popupId);
    var srcObj = ramaddaUtil.getDomObject(srcId);
    if(!popup || !srcObj) return;
    event = ramaddaUtil.getEvent(event);
    x = ramaddaUtil.getEventX(event);
    y = ramaddaUtil.getEventY(event);
    if(srcObj.obj.offsetLeft && srcObj.obj.offsetWidth) {
        x = ramaddaUtil.getLeft(srcObj.obj);
        y = srcObj.obj.offsetHeight+ramaddaUtil.getTop(srcObj.obj) + 2;
    } 

    if(alignLeft) {
        x = ramaddaUtil.getLeft(srcObj.obj);
        y = srcObj.obj.offsetHeight+ramaddaUtil.getTop(srcObj.obj) + 2;
    } else {
        x+=2;
        x+=3;
    }

    showObject(popup);
    ramaddaUtil.setPosition(popup, x,y);
}



function hideObject(obj) {
    if(!obj) {
        return 0;
    }
    $("#" + obj.id).hide();
    return 1;
}


function hideMore(base) {
    var link = ramaddaUtil.getDomObject("morelink_" + base);
    var div = ramaddaUtil.getDomObject("morediv_" + base);
    hideObject(div);
    showObject(link);
}


function showMore(base) {
    var link = ramaddaUtil.getDomObject("morelink_" + base);
    var div = ramaddaUtil.getDomObject("morediv_" + base);
    hideObject(link);
    showObject(div);
}




function showObject(obj, display) {
    if(!obj) return 0;
    $("#" + obj.id).show();
    return;
}



function toggleVisibilityOnObject(obj, display) {
    if(!obj) return 0;
    $("#" + obj.id).toggle();
}






//This gets called from toggleBlockVisibility
//It updates any map on the page to fix some sort of offset problem
function ramaddaUpdateMaps() {
    if (!(typeof ramaddaMaps === 'undefined')) {
        for(i=0;i<ramaddaMaps.length;i++) {
            var ramaddaMap = ramaddaMaps[i];
            if(!ramaddaMap.map) continue;
            ramaddaMap.map.updateSize();
        }
    }
}  




function popupFormLoadingDialog (dialogId) { 
    var dialog =   $(dialogId);
    dialog.dialog({
            resizable: false,
                height:100,
                modal: true
                }
        );
}


function submitEntryForm (dialogId) {
    popupFormLoadingDialog (dialogId);
    return true;
}




function treeViewClick (entryId, url, label) {
    var href="<a href='" + url +"'>" + label+"</a>";
    $("#treeview_header").html(href);
    url = url +"&template=empty";
    $('#treeview_view').attr("src",url); 
}


function treeViewGoTo () {
    var currentUrl =   $('#treeview_view').attr("src"); 
    if(currentUrl) {
        currentUrl = currentUrl.replace("template=","notemplate=");
        $(location).attr('href',currentUrl);
    }
}

function number_format( number, decimals, dec_point, thousands_sep ) {
    // http://kevin.vanzonneveld.net
    // +   original by: Jonas Raoni Soares Silva (http://www.jsfromhell.com)
    // +   improved by: Kevin van Zonneveld (http://kevin.vanzonneveld.net)
    // +     bugfix by: Michael White (http://crestidg.com)
    // +     bugfix by: Benjamin Lupton
    // +     bugfix by: Allan Jensen (http://www.winternet.no)
    // +    revised by: Jonas Raoni Soares Silva (http://www.jsfromhell.com)    
    // *     example 1: number_format(1234.5678, 2, '.', '');
    // *     returns 1: 1234.57     
 
    var n = number, c = isNaN(decimals = Math.abs(decimals)) ? 2 : decimals;
    var d = dec_point == undefined ? "," : dec_point;
    var t = thousands_sep == undefined ? "." : thousands_sep, s = n < 0 ? "-" : "";
    var i = parseInt(n = Math.abs(+n || 0).toFixed(c)) + "", j = (j = i.length) > 3 ? j % 3 : 0;
    
    return s + (j ? i.substr(0, j) + t : "") + i.substr(j).replace(/(\d{3})(?=\d)/g, "$1" + t) + (c ? d + Math.abs(n - i).toFixed(c).slice(2) : "");
}

//from http://snipplr.com/view.php?codeview&id=5949
function size_format (filesize) {
    if (filesize >= 1073741824) {
        filesize = number_format(filesize / 1073741824, 2, '.', '') + ' Gb';
    } else { 
        if (filesize >= 1048576) {
            filesize = number_format(filesize / 1048576, 2, '.', '') + ' Mb';
        } else { 
            if (filesize >= 1024) {
                filesize = number_format(filesize / 1024, 0) + ' Kb';
            } else {
                filesize = number_format(filesize, 0) + ' bytes';
            };
        };
    };
    return filesize;
};



