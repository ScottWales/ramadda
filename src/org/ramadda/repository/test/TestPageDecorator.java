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

package org.ramadda.repository.test;


import org.ramadda.repository.*;
import org.ramadda.repository.output.*;
import java.util.List;

/**
 * An example of a page decorator. Change the above package to your package structure.
 * Compile this class and make a jar file, e.g.,:
 * jar -cvf testdecorator.jar TestPageDecorator.class
 *
 * Put the jar file in the ramadda plugins directory, e.g.:
 * ~/.ramadda/plugins
 *
 * Now when ramadda runs decorate page will be called with the page html from the templates.
 * Update the html as needed and return it.
 *
 * @author Jeff McWhirter
 */
public class TestPageDecorator extends PageDecorator {

    /**
     * ctor
     */
    public TestPageDecorator() {}

    /**
     * Decorate the html
     *
     * @param repository the repository
     * @param request the request
     * @param html The html page template
     * @param entry This is the last entry the user has seen. Note: this may be null.
     *
     * @return The html
     */
    public String decoratePage(Repository repository, Request request,
                               String html, Entry entry) {
        Entry secondToTopMostEntry = null;
        if (entry != null) {
            secondToTopMostEntry =
                repository.getEntryManager().getSecondToTopEntry(entry);
        }
        if (secondToTopMostEntry != null) {
            //Use this to change the template
        }

        //Just add on XXXXXX so we cna see this working
        return html + "XXXXXX";
    }


    public String getDefaultOutputType(Repository repository, Request request,
                                       Entry entry, List<Entry> subFolders,List<Entry>subEntries) {
        if(entry.isGroup()) {
            for(Entry child: subEntries) {
                if (child.getResource().isImage()) {
                    return ImageOutputHandler.OUTPUT_PLAYER.getId();
                }
            }
        }
        return null;
    }


}
