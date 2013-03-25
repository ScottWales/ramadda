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

package org.ramadda.repository;

import org.ramadda.repository.map.MapInfo;


import java.util.List;



/**
 *
 *
 * @author RAMADDA Development Team
 * @version $Revision: 1.3 $
 */
public class PageDecorator extends RepositoryManager {

    public PageDecorator() {
        super(null);
    }


    public PageDecorator(Repository repository) {
        super(repository);

    }


    /**
     * _more_
     *
     * @param repository _more_
     * @param request _more_
     * @param html _more_
     * @param entry _more_
     *
     * @return _more_
     */
    public String decoratePage(Repository repository, Request request,
                               String html, Entry entry) {
        return html;
    }

    public String getDefaultOutputType(Repository repository, Request request,
                                       Entry entry, List<Entry> subFolders,List<Entry>subEntries) {
        return null;
    }


    public void addToMap(Request request, MapInfo mapInfo) {}



}
