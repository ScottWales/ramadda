/*
* Copyright 2008-2012 Jeff McWhirter/ramadda.org
*                     Don Murray/CU-CIRES
*
* Permission is hereby granted, free of charge, to any person obtaining a copy of this 
* software and associated documentation files (the "Software"), to deal in the Software 
* without restriction, including without limitation the rights to use, copy, modify, 
* merge, publish, distribute, sublicense, and/or sell copies of the Software, and to 
* permit persons to whom the Software is furnished to do so, subject to the following conditions:
* 
* The above copyright notice and this permission notice shall be included in all copies 
* or substantial portions of the Software.
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
* PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE 
* FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR 
* OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER 
* DEALINGS IN THE SOFTWARE.
*/

package org.ramadda.projects.above;

import org.ramadda.repository.*;
import org.ramadda.repository.map.*;
import org.ramadda.repository.output.*;
import org.ramadda.util.HtmlUtils;
import org.ramadda.util.Utils;
import ucar.unidata.util.Misc;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Jeff McWhirter
 */
public class AbovePageDecorator extends PageDecorator {

    private List<WmsMapLayer> mapLayers = null;

    public AbovePageDecorator() {
        this(null);
    }


    /*
     * ctor
     */
    public AbovePageDecorator(Repository repository) {
        super(repository);
    }


    private List<WmsMapLayer> getMapLayers() {
        if(mapLayers == null) {
            mapLayers = WmsMapLayer.makeLayers(getRepository(), "above.map");
        }
        return mapLayers;
    }


@Override
    public void addToMap(Request request, MapInfo mapInfo) {
    //        if(mapInfo.forSelection()) return;
    //http://wms.alaskamapped.org/extras?

        List<String>titles = new ArrayList<String>();
        List<String>tabs = new ArrayList<String>();
        for(WmsMapLayer mapLayer: getMapLayers()) {
            if(Utils.stringDefined(mapLayer.getLegendLabel())) {
                titles.add(mapLayer.getLegendLabel());
                StringBuffer sb = new StringBuffer(mapLayer.getLegendText());
                if(Utils.stringDefined(mapLayer.getLegendImage())) {
                    sb.append(HtmlUtils.img(mapLayer.getLegendImage()));
                }
                tabs.add(HtmlUtils.div(sb.toString(), HtmlUtils.cssClass("map-legend-div")));
            }
            mapLayer.addToMap(request, mapInfo);
        }

        StringBuffer rightSide = new StringBuffer();
        rightSide.append("<b>Legends</b><br>");
        rightSide.append(OutputHandler.makeTabs(titles, tabs, true));

        mapInfo.addRightSide(getPageHandler().makeStickyPopup(HtmlUtils.img(getRepository().fileUrl("/icons/map_go.png")), 
                                                             rightSide.toString(),null));
        //        mapInfo.addRightSide(HtmlUtils.makeShowHideBlock("", rightSide.toString(),false));
        //        mapInfo.addRightSide(HtmlUtils.makeShowHideBlock("", rightSide.toString(),false));

    }




}
