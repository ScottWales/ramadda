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

package org.ramadda.plugins.wiki;


import org.ramadda.repository.*;
import org.ramadda.repository.auth.*;

import java.util.Date;


/**
 *
 */

public class WikiPageHistory {

    /** _more_ */
    int version;

    /** _more_ */
    User user;

    /** _more_ */
    Date date;

    /** _more_ */
    String description;

    /** _more_ */
    String text;

    /**
     * _more_
     *
     * @param version _more_
     * @param user _more_
     * @param date _more_
     * @param description _more_
     */
    public WikiPageHistory(int version, User user, Date date,
                           String description) {
        this(version, user, date, description, null);
    }

    /**
     * _more_
     *
     * @param version _more_
     * @param user _more_
     * @param date _more_
     * @param description _more_
     * @param text _more_
     */
    public WikiPageHistory(int version, User user, Date date,
                           String description, String text) {
        this.version     = version;
        this.user        = user;
        this.date        = date;
        this.description = description;
        this.text        = text;
    }

    /**
     *  Set the Version property.
     *
     *  @param value The new value for Version
     */
    public void setVersion(int value) {
        version = value;
    }

    /**
     *  Get the Version property.
     *
     *  @return The Version
     */
    public int getVersion() {
        return version;
    }

    /**
     *  Set the User property.
     *
     *  @param value The new value for User
     */
    public void setUser(User value) {
        user = value;
    }

    /**
     *  Get the User property.
     *
     *  @return The User
     */
    public User getUser() {
        return user;
    }

    /**
     *  Set the Date property.
     *
     *  @param value The new value for Date
     */
    public void setDate(Date value) {
        date = value;
    }

    /**
     *  Get the Date property.
     *
     *  @return The Date
     */
    public Date getDate() {
        return date;
    }

    /**
     *  Set the Description property.
     *
     *  @param value The new value for Description
     */
    public void setDescription(String value) {
        description = value;
    }

    /**
     *  Get the Description property.
     *
     *  @return The Description
     */
    public String getDescription() {
        return description;
    }


    /**
     *  Set the Text property.
     *
     *  @param value The new value for Text
     */
    public void setText(String value) {
        text = value;
    }

    /**
     *  Get the Text property.
     *
     *  @return The Text
     */
    public String getText() {
        return text;
    }



}
