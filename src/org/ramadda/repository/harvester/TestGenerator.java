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

package org.ramadda.repository.harvester;


import org.ramadda.repository.*;
import org.ramadda.repository.auth.*;
import org.ramadda.repository.type.*;
import org.ramadda.util.HtmlUtils;


import org.w3c.dom.*;

import ucar.unidata.sql.SqlUtil;
import ucar.unidata.util.DateUtil;

import ucar.unidata.util.IOUtil;
import ucar.unidata.util.LogUtil;
import ucar.unidata.util.Misc;


import ucar.unidata.util.StringUtil;
import ucar.unidata.util.TwoFacedObject;
import ucar.unidata.xml.XmlUtil;

import java.io.File;
import java.io.InputStream;

import java.lang.reflect.*;



import java.net.*;




import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;



import java.util.regex.*;


/**
 *
 *
 *
 * @author RAMADDA Development Team
 * @version $Revision: 1.3 $
 */
public class TestGenerator extends Harvester {

    /**
     * _more_
     *
     * @param repository _more_
     * @param element _more_
     *
     * @throws Exception _more_
     */
    public TestGenerator(Repository repository, Element element)
            throws Exception {
        super(repository, element);
    }


    /**
     * _more_
     *
     * @param repository _more_
     * @param id _more_
     *
     * @throws Exception _more_
     */
    public TestGenerator(Repository repository, String id) throws Exception {
        super(repository, id);
    }



    /**
     * _more_
     *
     * @return _more_
     */
    public String getDescription() {
        return "Test";
    }

    /** _more_ */
    int cnt = 0;


    /**
     * _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public String getExtraInfo() throws Exception {
        StringBuffer sb = new StringBuffer();
        sb.append("Created: " + cnt + "  entries");

        return sb.toString();
    }

    /**
     * _more_
     *
     *
     * @param timestamp _more_
     * @throws Exception _more_
     */
    protected void runInner(int timestamp) throws Exception {
        if ( !canContinueRunning(timestamp)) {
            return;
        }
        cnt = 0;
        List<Entry> entries = new ArrayList<Entry>();
        final User  user    = repository.getUserManager().getDefaultUser();
        List        groups  = new ArrayList();
        for (int j = 0; j < 100; j++) {
            Entry group = getEntryManager().findGroupFromName(getRequest(),
                              "Test/Generated/" + "Group" + j, user, true);
            groups.add(group);
        }

        for (int i = 0; i < 1000; i++) {
            for (int j = 0; j < 100; j++) {
                Entry group = (Entry) groups.get(j);
                for (int k = 0; k < 10; k++) {
                    Date  createDate = new Date();
                    Entry entry      =
                        getTypeHandler().createEntry(repository.getGUID());
                    entry.initEntry("test_" + i + "_" + j + "_" + k, "",
                                    group, user,
                                    new Resource("", Resource.TYPE_UNKNOWN),
                                    "", createDate.getTime(),
                                    createDate.getTime(),
                                    createDate.getTime(),
                                    createDate.getTime(), null);
                    entries.add(entry);
                    getTypeHandler().initializeNewEntry(entry);
                    cnt++;
                    if ( !canContinueRunning(timestamp)) {
                        return;
                    }
                    if (entries.size() > 5000) {
                        getEntryManager().insertEntries(entries, true, true);
                        entries = new ArrayList<Entry>();
                    }
                }
            }
            //            System.err.println("  Added:" + cnt);
        }


    }





}
