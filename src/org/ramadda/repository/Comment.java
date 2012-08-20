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


import org.ramadda.repository.auth.*;


import ucar.unidata.util.Misc;


import java.util.Date;



/**
 *
 *
 * @author RAMADDA Development Team
 * @version $Revision: 1.30 $
 */
public class Comment {

    /** _more_ */
    private String id;

    /** _more_ */
    private String subject;

    /** _more_ */
    private String comment;

    /** _more_ */
    private Date date;

    /** _more_ */
    private User user;

    /** _more_ */
    private Entry entry;


    /**
     * _more_
     *
     *
     * @param id _more_
     * @param entry _more_
     * @param user _more_
     * @param date _more_
     * @param subject _more_
     * @param comment _more_
     */
    public Comment(String id, Entry entry, User user, Date date,
                   String subject, String comment) {
        this.id      = id;
        this.entry   = entry;
        this.user    = user;
        this.subject = subject;
        this.comment = comment;
        this.date    = date;
    }



    /**
     * Set the Id property.
     *
     * @param value The new value for Id
     */
    public void setId(String value) {
        id = value;
    }

    /**
     * Get the Id property.
     *
     * @return The Id
     */
    public String getId() {
        return id;
    }

    /**
     * Set the Subject property.
     *
     * @param value The new value for Subject
     */
    public void setSubject(String value) {
        subject = value;
    }

    /**
     * Get the Subject property.
     *
     * @return The Subject
     */
    public String getSubject() {
        return subject;
    }

    /**
     * Set the Comment property.
     *
     * @param value The new value for Comment
     */
    public void setComment(String value) {
        comment = value;
    }

    /**
     * Get the Comment property.
     *
     * @return The Comment
     */
    public String getComment() {
        return comment;
    }

    /**
     * Set the User property.
     *
     * @param value The new value for User
     */
    public void setUser(User value) {
        user = value;
    }

    /**
     * Get the User property.
     *
     * @return The User
     */
    public User getUser() {
        return user;
    }

    /**
     * Set the Date property.
     *
     * @param value The new value for Date
     */
    public void setDate(Date value) {
        date = value;
    }

    /**
     * Get the Date property.
     *
     * @return The Date
     */
    public Date getDate() {
        return date;
    }






    /**
     * _more_
     *
     * @return _more_
     */
    public String toString() {
        return subject;
    }


}
