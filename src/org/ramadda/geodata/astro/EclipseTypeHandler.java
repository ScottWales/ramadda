/*
* Copyright 2008-2011 Jeff McWhirter/ramadda.org
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

package org.ramadda.geodata.fits;


import nom.tam.fits.*;


import org.ramadda.repository.*;
import org.ramadda.repository.map.*;
import org.ramadda.repository.metadata.*;
import org.ramadda.repository.output.*;
import org.ramadda.repository.type.*;
import org.ramadda.util.AtomUtil;


import org.w3c.dom.*;

import ucar.unidata.util.DateUtil;
import ucar.unidata.util.HtmlUtil;
import ucar.unidata.util.Misc;


import ucar.unidata.util.StringUtil;
import ucar.unidata.util.TwoFacedObject;
import ucar.unidata.xml.XmlUtil;

import java.awt.geom.Rectangle2D;

import java.io.File;

import java.text.SimpleDateFormat;


import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;

import java.util.List;


/**
 *
 *
 * @author Jeff McWhirter
 * @version $Revision: 1.3 $
 */
public class EclipseTypeHandler extends FitsTypeHandler  {

    public static final int IDX_BASE = FITS_PROPS.length+1;

    public static final int IDX_LOCATION = IDX_BASE  + 0;
    public static final int IDX_SOURCE = IDX_BASE  + 1;
    public static final int IDX_SOURCETYPE = IDX_BASE  + 2;
    public static final int IDX_MAGNITUDE = IDX_BASE  + 3;

    public static final String PROP_MAGNITUDE  ="MAGNITUD";
    /**
     * ctor
     *
     * @param repository the repository
     * @param node the xml node from the types.xml file
     * @throws Exception On badness
     */
    public EclipseTypeHandler(Repository repository, Element node)
            throws Exception {
        super(repository, node);
    }


    public Object[] createValueArray() {
        return new Object[FITS_PROPS.length+4];
    }


    public void processHeader(Entry entry, Header header, Object[] values) {
        super.processHeader(entry, header, values);
        String value = header.getStringValue(PROP_MAGNITUDE);
        if(value!=null) {
            values[IDX_MAGNITUDE] = new Double(value.trim()).doubleValue();
        }

    }

}
