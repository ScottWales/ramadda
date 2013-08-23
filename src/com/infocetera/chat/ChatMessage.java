/*
* Copyright 2008-2013 Geode Systems LLC
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

package com.infocetera.chat;


import com.infocetera.glyph.*;

import com.infocetera.util.*;

import java.awt.*;



import java.util.Vector;


/**
 * Class ChatMessage _more_
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class ChatMessage extends HtmlGlyph {

    /** _more_ */
    private ChatUser from;

    /**
     * _more_
     *
     * @param canvas _more_
     * @param from _more_
     * @param message _more_
     */
    public ChatMessage(DisplayCanvas canvas, ChatUser from, String message) {
        super(canvas, message);
        this.from  = from;
        fixedWidth = true;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean isFromChat() {
        return from.getName().equals(ChatApplet.USER_CHAT);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getUserName() {
        return from.getName();
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public ChatUser getFrom() {
        return from;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getFormattedName() {
        return "(" + from.getName() + ")";

    }


}
