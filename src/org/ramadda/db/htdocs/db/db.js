
function dbRowOver(rowId) {
    row = util.getDomObject(rowId);
    if(!row) return;
    row.style.backgroundColor = "#edf5ff";
}

function dbRowOut(rowId) {
    row = util.getDomObject(rowId);
    if(!row) return;
    row.style.backgroundColor = "#fff";
}

function dbRowClick(event, divId, url) {
    row = util.getDomObject(divId);
    if(!row) return;
    left = util.getLeft(row.obj);
    eventX = util.getEventX(event);
    //Don't pick up clicks on the left side
    if(eventX-left<50) return;
    util.loadXML( url, dbHandleXml,divId);
}


function dbHandleXml(request,divId) {
    row = util.getDomObject(divId);
    if(!row) return;
    div = util.getDomObject("tooltipdiv");
    if(!div) return;
    var xmlDoc=request.responseXML.documentElement;
    text = getChildText(xmlDoc);
    util.setPosition(div, util.getLeft(row.obj), util.getBottom(row.obj));

    div.obj.innerHTML = "<div class=tooltip-inner><div id=\"tooltipwrapper\" ><table><tr valign=top><img width=\"16\" onmousedown=\"hideEntryPopup();\" id=\"tooltipclose\"  src=" + icon_close +"></td><td>" + text+"</table></div></div>";
    showObject(div);
}


function stickyDragEnd(id, url) {
    div  = util.getDomObject(id);
    if(!div) return;
    url = url +"&posx=" + div.style.left +"&posy=" + div.style.top;
    util.loadXML( url, stickyNOOP,id);
}


function stickyNOOP(request,divId) {
}