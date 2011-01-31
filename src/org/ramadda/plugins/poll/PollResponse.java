/*
 * Copyright 2008-2011 Jeff McWhirter/ramadda.org
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

package org.ramadda.plugins.poll;


import ucar.unidata.xml.XmlEncoder;


import java.util.Date;
import java.util.Hashtable;
import java.util.UUID;


/**
 *
 *
 */
public class PollResponse {

    /** _more_          */
    private String id;

    /** _more_          */
    private String what;

    /** _more_          */
    private Date date;

    /** _more_          */
    private String comment = "";

    /** _more_          */
    private Hashtable<String, String> selected = new Hashtable<String,
                                                     String>();


    /**
     * _more_
     */
    public PollResponse() {}

    /**
     * _more_
     *
     * @param what _more_
     * @param comment _more_
     */
    public PollResponse(String what, String comment) {
        this.id      = UUID.randomUUID().toString();
        this.what    = what;
        this.comment = comment;
        this.date    = new Date();
    }

    /**
     * _more_
     *
     * @param value _more_
     */
    public void set(String value) {
        selected.put(value, "");
    }


    /**
     * _more_
     *
     * @param value _more_
     *
     * @return _more_
     */
    public boolean isSelected(String value) {
        return selected.get(value) != null;
    }

    /**
     *  Set the Comment property.
     *
     *  @param value The new value for Comment
     */
    public void setComment(String value) {
        comment = value;
    }

    /**
     *  Get the Comment property.
     *
     *  @return The Comment
     */
    public String getComment() {
        return comment;
    }


    /**
     *  Set the Id property.
     *
     *  @param value The new value for Id
     */
    public void setId(String value) {
        id = value;
    }

    /**
     *  Get the Id property.
     *
     *  @return The Id
     */
    public String getId() {
        return id;
    }


    /**
     *  Set the What property.
     *
     *  @param value The new value for What
     */
    public void setWhat(String value) {
        what = value;
    }

    /**
     *  Get the What property.
     *
     *  @return The What
     */
    public String getWhat() {
        return what;
    }

    /**
     *  Set the Selected property.
     *
     *  @param value The new value for Selected
     */
    public void setSelected(Hashtable<String, String> value) {
        selected = value;
    }

    /**
     *  Get the Selected property.
     *
     *  @return The Selected
     */
    public Hashtable<String, String> getSelected() {
        return selected;
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
     * _more_
     *
     * @param args _more_
     *
     * @throws Exception _more_
     */
    public static void main(String[] args) throws Exception {
        PollResponse r = new PollResponse("foo", "");
        r.set("x");
        r.set("y");
        System.err.println(new XmlEncoder().toXml(r));
    }

}
