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

package org.ramadda.plugins.cma;

import org.ramadda.repository.*;
import org.ramadda.repository.output.*;
import java.util.List;

/**
 * @author Jeff McWhirter
 */
public class CmaPageDecorator extends PageDecorator {

    /**
     * ctor
     */
    public CmaPageDecorator() {}



    /**
       This is called when no ARG_OUTPUT is specified. It can return the OUTPUT_TYPE to use for the given entry
    */
    @Override
    public String getDefaultOutputType(Repository repository, Request request,
                                       Entry entry, List<Entry> subFolders,List<Entry>subEntries) {
        if(entry.isGroup()) {
            for(Entry child: subEntries) {
                //If there are any images then use the image player
                if (child.getResource().isImage()) {
                    return ImageOutputHandler.OUTPUT_PLAYER.getId();
                }
            }
            return null;
        }

        //Here we have a single entry
        if(entry.isFile()) {
            String file = entry.getResource().getPath();
            //If its a netcdf or grib file then use
            //The "data.cdl" is defined in
            //org/ramadda/geodata/cdmdata/CdmDataOutputHandler.OUTPUT_CDL
            if(file.endsWith(".nc") || file.endsWith("grb")) {
                return "data.cdl";
            }
        }        
        return null;

    }



    /**
     * Decorate the html. This allows you to do anything with the HTML for the given entry
     *
     * @param repository the repository
     * @param request the request
     * @param html The html page template
     * @param entry This is the last entry the user has seen. Note: this may be null.
     *
     * @return The html
     */
@Override
    public String decoratePage(Repository repository, Request request,
                               String html, Entry entry) {
        //Do nothing
        return super.decoratePage(repository, request, html, entry);
    }

}
