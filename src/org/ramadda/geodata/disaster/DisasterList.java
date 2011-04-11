/*
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 */

package org.ramadda.geodata.disaster;




import org.ramadda.repository.*;
import org.ramadda.repository.auth.*;
import org.ramadda.repository.map.*;
import org.ramadda.repository.output.*;
import org.ramadda.repository.search.*;
import org.ramadda.repository.type.TypeHandler;


import java.util.List;
import java.util.ArrayList;
import java.util.Hashtable;

import org.w3c.dom.*;;


/**
 *
 *
 * @author Jeff McWhirter
 * @version $Revision: 1.3 $
 */

public class DisasterList extends SpecialSearch implements RequestHandler {

    /**
     * _more_
     *
     * @param repository _more_
     * @param node _more_
     * @param props _more_
     *
     * @throws Exception _more_
     */
    public DisasterList(Repository repository, Element node,
                          Hashtable props)
            throws Exception {
        super(repository, node, props);
    }



    public void makeEntryList(Request request, StringBuffer sb, List<Entry> entries) throws Exception {
        Result tmpResult =
            getRepository().getHtmlOutputHandler().outputGroup(request,
                                                               HtmlOutputHandler.OUTPUT_HTML,
                                                               getRepository().getEntryManager().getDummyGroup(),
                                                               entries, new ArrayList<Entry>());

        sb.append(new String(tmpResult.getContent()));
    }





}
