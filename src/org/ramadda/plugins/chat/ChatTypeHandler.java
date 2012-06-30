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

package org.ramadda.plugins.chat;


import org.ramadda.repository.*;
import org.ramadda.repository.auth.*;
import org.ramadda.repository.metadata.*;
import org.ramadda.repository.output.OutputHandler;
import org.ramadda.repository.type.*;


import org.w3c.dom.*;

import java.util.ArrayList;
import java.util.List;


/**
 *
 *
 */
public class ChatTypeHandler extends ExtensibleGroupTypeHandler {

    /** _more_ */
    private ChatOutputHandler chatOutputHandler;

    /**
     * _more_
     *
     * @param repository _more_
     * @param entryNode _more_
     *
     * @throws Exception _more_
     */
    public ChatTypeHandler(Repository repository, Element entryNode)
            throws Exception {
        super(repository, entryNode);
    }


    private ChatOutputHandler getChatOutputHandler()  {
        if(chatOutputHandler == null) {
            try {
                chatOutputHandler =
                    (ChatOutputHandler) getRepository().getOutputHandler(
                                                                         ChatOutputHandler.OUTPUT_CHATROOM);
            } catch(Exception exc) {
                throw new RuntimeException(exc);

            }
        }
        return chatOutputHandler;
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entry _more_
     * @param group _more_
     * @param subGroups _more_
     * @param entries _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result getHtmlDisplay(Request request, Entry entry,
                                 List<Entry> subGroups, List<Entry> entries)
            throws Exception {
        return getChatOutputHandler().outputEntry(
            request, chatOutputHandler.OUTPUT_CHATROOM, entry);
    }

    @Override
    public boolean getForUser() {
        if(getChatOutputHandler()==null) return false;
        if(getChatOutputHandler().getChatPort()>0) {
            return true;
        }
        return false;
    }


}
