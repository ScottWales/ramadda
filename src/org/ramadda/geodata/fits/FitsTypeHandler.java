package org.ramadda.geodata.fits;


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
public class FitsTypeHandler extends GenericTypeHandler {


    /**
     * ctor
     *
     * @param repository the repository
     * @param node the xml node from the types.xml file
     * @throws Exception On badness
     */
    public FitsTypeHandler(Repository repository, Element node)
            throws Exception {
        super(repository, node);
    }


}
