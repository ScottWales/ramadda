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
import ucar.unidata.util.Misc;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Jeff McWhirter
 */
public class AbovePageDecorator extends PageDecorator {

    public AbovePageDecorator() {
    }


    /*
     * ctor
     */
    public AbovePageDecorator(Repository repository) {
        super(repository);
    }


@Override
    public void addToMap(Request request, MapInfo mapInfo) {
        //        if(mapInfo.forSelection()) return;

        String[] legendLabels = {"Bailey's Division", 
                           "Bailey's Domain",
                           "Bailey's Province",};


        String[] labels = {"Bailey's Ecosystem Division", 
                           "Bailey's Ecosystem Domain",
                           "Bailey's Ecosystem Province",};

        String[] legends = {"http://webmap.ornl.gov/ogcbroker/wms?version=1.1.1&service=WMS&request=GetLegendGraphic&layer=10010_1&format=image/png&STYLE=default",
                            "http://webmap.ornl.gov/ogcbroker/wms?version=1.1.1&service=WMS&request=GetLegendGraphic&layer=10010_2&format=image/png&STYLE=default",
                            "http://webmap.ornl.gov/ogcbroker/wms?version=1.1.1&service=WMS&request=GetLegendGraphic&layer=10010_3&format=image/png&STYLE=default",};

        String[] layers = {"10010_1_band1", "10010_2_band1", "10010_3_band1",};

        String[] urls = {"http://webmap.ornl.gov/fcgi-bin/mapserv.exe?map=D:/CONFIG/OGCBROKER/mapfile//10010/10010_1_wms.map",
                         "http://webmap.ornl.gov/fcgi-bin/mapserv.exe?map=D:/CONFIG/OGCBROKER/mapfile//10010/10010_2_wms.map",
                         "http://webmap.ornl.gov/fcgi-bin/mapserv.exe?map=D:/CONFIG/OGCBROKER/mapfile//10010/10010_3_wms.map",};

        List<String>titles = new ArrayList<String>();
        List<String>tabs = new ArrayList<String>();




        for(int i=0;i< labels.length;i++) {
            titles.add(legendLabels[i]);
            tabs.add(HtmlUtils.img(legends[i]));
            String labelArg  = labels[i].replaceAll("'","\\\\'");
            mapInfo.addJS(HtmlUtils.call(mapInfo.getVariableName() + ".addWMSLayer",
                                         HtmlUtils.jsMakeArgs(new String[]{HtmlUtils.squote(labelArg), HtmlUtils.squote(urls[i]), HtmlUtils.squote(layers[i]),"true"},false)));
            mapInfo.addJS("\n");
        }

        StringBuffer rightSide = new StringBuffer();
        rightSide.append("<b>Legends</b><br>");
        rightSide.append(OutputHandler.makeTabs(titles, tabs, true));

        mapInfo.addRightSide(getRepository().makeStickyPopup(HtmlUtils.img(getRepository().fileUrl("/icons/map_go.png")), 
                                                             rightSide.toString(),null));
        //        mapInfo.addRightSide(HtmlUtils.makeShowHideBlock("", rightSide.toString(),false));
        //        mapInfo.addRightSide(HtmlUtils.makeShowHideBlock("", rightSide.toString(),false));

    }


}
