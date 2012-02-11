
package org.ramadda.geodata.fits;


import org.ramadda.repository.*;
import org.ramadda.repository.auth.*;
import org.ramadda.repository.map.*;
import org.ramadda.repository.output.*;
import org.ramadda.repository.search.*;
import org.ramadda.repository.type.TypeHandler;


import org.w3c.dom.*;



/**
 *
 *
 * @author Jeff McWhirter
 * @version $Revision: 1.3 $
 */

public class FitsApiHandler extends SpecialSearch     {

    /**
     * _more_
     *
     * @param repository _more_
     * @param node _more_
     * @param props _more_
     *
     * @throws Exception On badness
     */
    public FitsApiHandler(Repository repository, Element node,
                          Hashtable props)
            throws Exception {
        super(repository, node, props);
    }



}
