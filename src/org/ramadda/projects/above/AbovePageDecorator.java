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
import ucar.unidata.util.Misc;
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


    public void addToMap(Request request, MapInfo mapInfo, List<Entry> entriesToUse, boolean detailed) {
        mapInfo.addJS(mapInfo.getVariableName() + ".addWMSLayer('Bailey\\'s Ecoregions Division','http://webmap.ornl.gov/fcgi-bin/mapserv.exe?map=D:/CONFIG/OGCBROKER/mapfile//10010/10010_1_wms.map','10010_1_band1',true);");
        mapInfo.addJS(mapInfo.getVariableName() + ".addWMSLayer('Bailey\\'s Ecoregions Domain','http://webmap.ornl.gov/fcgi-bin/mapserv.exe?map=D:/CONFIG/OGCBROKER/mapfile//10010/10010_2_wms.map','10010_2_band1',true);");
        mapInfo.addJS(mapInfo.getVariableName() + ".addWMSLayer('Bailey\\'s Ecoregions Province','http://webmap.ornl.gov/fcgi-bin/mapserv.exe?map=D:/CONFIG/OGCBROKER/mapfile//10010/10010_3_wms.map','10010_3_band1',true);");

    }


}
