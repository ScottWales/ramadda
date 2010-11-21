/*
 *
 * 
 * 
 * 
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

