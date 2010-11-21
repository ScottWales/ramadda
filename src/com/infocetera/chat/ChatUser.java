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

/**
 * (C) 1999-2002  WTS Systems, L.L.C.
 *
 *   All rights reserved
 */




package com.infocetera.chat;


import java.util.Hashtable;


/**
 * Class ChatUser _more_
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class ChatUser {

    /** _more_ */
    private String id;

    /** _more_ */
    private String name;

    /** _more_ */
    private String iconUrl;

    /**
     *  Contains a mapping between user name and ChatUser
     */
    static Hashtable userMap;



    /** _more_ */
    private boolean ignored = false;


    /**
     * _more_
     *
     * @param id _more_
     * @param theName _more_
     * @param iconUrl _more_
     */
    private ChatUser(String id, String theName, String iconUrl) {
        this.id      = id;
        this.name    = theName;
        this.iconUrl = iconUrl;
        if (name == null) {
            name = "UNKNOWN";
        }
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public String getId() {
        return id;
    }

    /**
     * _more_
     *
     * @param name _more_
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * _more_
     *
     * @param id _more_
     */
    public void setId(String id) {
        this.id = id;
    }


    /**
     * _more_
     *
     * @param b _more_
     */
    public void setIgnored(boolean b) {
        ignored = b;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean getIgnored() {
        return ignored;
    }

    /**
     *  Is this user being ignored
     *
     * @return _more_
     */
    public boolean userOk() {
        return (ignored == false);
    }


    /**
     * _more_
     */
    public static void init() {
        userMap = new Hashtable();
    }


    /**
     *  Find (or create) the ChatUser object with the given name.
     *
     * @param id _more_
     *
     * @return _more_
     */
    public static ChatUser getUser(String id) {
        return getUser(id, id);
    }

    /**
     * _more_
     *
     * @param id _more_
     * @param name _more_
     *
     * @return _more_
     */
    public static ChatUser getUser(String id, String name) {
        return getUser(id, name, false);
    }

    /** _more_ */
    static int cnt = 1;

    /**
     * _more_
     *
     * @param id _more_
     * @param name _more_
     * @param forceNew _more_
     *
     * @return _more_
     */
    public static ChatUser getUser(String id, String name, boolean forceNew) {
        if (id == null) {
            id = "UNKNOWN";
        }

        ChatUser u       = (ChatUser) userMap.get(id);
        ChatUser newUser = null;

        if (u != null) {
            if ( !forceNew) {
                newUser = u;
            } else {
                userMap.put(id, newUser = new ChatUser(id, name, null));
            }
        } else {
            userMap.put(id, newUser = new ChatUser(id, name, null));
        }
        return newUser;
    }



    /**
     * _more_
     *
     * @return _more_
     */
    public String getName() {
        return name;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getIconUrl() {
        return iconUrl;
    }

    /**
     * _more_
     *
     * @param s _more_
     */
    public void setIconUrl(String s) {
        iconUrl = s;
    }

    /**
     * _more_
     *
     * @param o _more_
     *
     * @return _more_
     */
    public boolean equals(Object o) {
        if ( !(o instanceof ChatUser)) {
            return false;
        }
        return id.equals(((ChatUser) o).getId());
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String toString() {
        return name;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public int hashCode() {
        return id.hashCode();
    }

}

